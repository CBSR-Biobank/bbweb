package org.biobank.service.participants

import org.biobank.service.{ Processor, Utils }
import org.biobank.infrastructure.command.ParticipantCommands._
import org.biobank.infrastructure.event.ParticipantEvents._
import org.biobank.domain.{
  Annotation,
  DomainValidation,
  DomainError }
import org.biobank.domain.study._
import org.biobank.domain.participants._

import javax.inject.{Inject => javaxInject, Named}
import akka.actor._
import akka.persistence.SnapshotOffer
import org.joda.time.format.ISODateTimeFormat
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object ParticipantsProcessor {

  def props = Props[ParticipantsProcessor]

}

/**
 *
 */
class ParticipantsProcessor @javaxInject() (
  @Named("collectionEvents") val collectionEventsProcessor: ActorRef,
  @Named("specimens")        val specimensProcessor: ActorRef,

  val participantRepository:    ParticipantRepository,
  val collectionEventRepository: CollectionEventRepository,
  val studyRepository:          StudyRepository)
    extends Processor {

  import ParticipantEvent.EventType
  import org.biobank.infrastructure.event.ParticipantEventsUtil._

  override def persistenceId = "participant-processor-id"

  case class SnapshotState(participants: Set[Participant])

  /**
   * These are the events that are recovered during journal recovery. They cannot fail and must be
   * processed to recreate the current state of the aggregate.
   */
  val receiveRecover: Receive = {
    case event: ParticipantEvent => event.eventType match {
      case et: EventType.ParticipantAdded   => applyParticipantAddedEvent(event)
      case et: EventType.ParticipantUpdated => applyParticipantUpdatedEvent(event)

      case event => log.error(s"event not handled: $event")
    }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.participants.foreach{ participant => participantRepository.put(participant) }
  }

  /**
   * These are the commands that are requested. A command can fail, and will send the failure as a response
   * back to the user. Each valid command generates one or more events and is journaled.
   */
  val receiveCommand: Receive = {
    case cmd: AddParticipantCmd      => processAddParticipantCmd(cmd)
    case cmd: UpdateParticipantCmd   => processUpdateParticipantCmd(cmd)
    case cmd: ParticipantCommand     => validateAndForward(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(participantRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"ParticipantsProcessor: message not handled: $cmd")
  }

  private def validateAndForward(cmd: ParticipantCommand): Unit = {
    val cmdValidation: DomainValidation[(ParticipantId, ActorRef)] = cmd match {
      case cmd: CollectionEventCommand =>
        (ParticipantId(cmd.participantId), collectionEventsProcessor).success

      case cmd: SpecimenCommand =>
        collectionEventRepository.getByKey(CollectionEventId(cmd.collectionEventId)) map { cevent =>
          (cevent.participantId, specimensProcessor)
        }

      case cmd =>
        DomainError(s"ParticipantsProcessor: participant message not handled: $cmd").failureNel
    }

    val studyValidation = for {
      tuple       <- cmdValidation
      (participantId, subProcessor) = tuple
      participant <- participantRepository.getByKey(participantId)
      study       <- studyRepository.getEnabled(participant.studyId)
    } yield subProcessor

    studyValidation.fold(
      err          => context.sender ! studyValidation,
      subProcessor => subProcessor forward cmd
    )
  }

  val ErrMsgUniqueIdExists = "participant with unique ID already exists"

  /** Searches the repository for a matching item.
   */
  protected def uniqueIdAvailableMatcher(uniqueId: String)(matcher: Participant => Boolean)
      : DomainValidation[Boolean] = {
    val exists = participantRepository.getValues.exists { item =>
      matcher(item)
    }
    if (exists) {
      DomainError(s"$ErrMsgUniqueIdExists: $uniqueId").failureNel
    } else {
      true.success
    }
  }

  private def uniqueIdAvailable(uniqueId: String): DomainValidation[Boolean] = {
    uniqueIdAvailableMatcher(uniqueId){ item =>
      item.uniqueId == uniqueId
    }
  }

  private def uniqueIdAvailable(uniqueId: String, excludeParticipantId: ParticipantId)
      : DomainValidation[Boolean] = {
    uniqueIdAvailableMatcher(uniqueId){ item =>
      (item.uniqueId == uniqueId) && (item.id != excludeParticipantId)
    }
  }

private def processAddParticipantCmd(cmd: AddParticipantCmd): Unit = {
    val studyId = StudyId(cmd.studyId)
    val participantId = participantRepository.nextIdentity

    if (participantRepository.getByKey(participantId).isSuccess) {
      log.error(s"participant with id already exsits: $participantId")
    }

    val event = for {
      study             <- studyRepository.getEnabled(studyId)
      uniqueIdAvailable <- uniqueIdAvailable(cmd.uniqueId)
      annotTypes        <- Annotation.validateAnnotations(study.annotationTypes,
                                                          cmd.annotations.toSet)
      newParticip       <- Participant.create(studyId,
                                              participantId,
                                              -1L,
                                              cmd.uniqueId,
                                              cmd.annotations.toSet)
      event             <- createEvent(newParticip, cmd).withParticipantAdded(
        ParticipantAddedEvent(
          studyId       = Some(newParticip.studyId.id),
          uniqueId      = Some(cmd.uniqueId),
          annotations   = Utils.convertAnnotationsToEvent(cmd.annotations))).success
    } yield event

    process(event) { applyParticipantAddedEvent(_) }
  }

  private def processUpdateParticipantCmd(cmd: UpdateParticipantCmd): Unit = {
    val studyId = StudyId(cmd.studyId)
    val participantId = ParticipantId(cmd.id)
    var annotationsSet = cmd.annotations.toSet

    val event = for {
      study             <- studyRepository.getEnabled(studyId)
      uniqueIdAvailable <- uniqueIdAvailable(cmd.uniqueId, participantId)
      annotTypes        <- Annotation.validateAnnotations(study.annotationTypes, annotationsSet)
      particip          <- participantRepository.getByKey(participantId)
      newParticip       <- Participant.create(studyId,
                                              participantId,
                                              particip.version,
                                              cmd.uniqueId,
                                              annotationsSet)
      event             <- createEvent(newParticip, cmd).withParticipantUpdated(
        ParticipantUpdatedEvent(
          studyId       = Some(newParticip.studyId.id),
          version       = Some(newParticip.version),
          uniqueId      = Some(cmd.uniqueId),
          annotations   = Utils.convertAnnotationsToEvent(cmd.annotations))).success
    } yield event

    process(event){ applyParticipantUpdatedEvent(_) }
  }

  private def applyParticipantAddedEvent(event: ParticipantEvent) = {
    log.debug(s"applyParticipantAddedEvent: event: $event")

    if (event.eventType.isParticipantAdded) {
      val addedEvent = event.getParticipantAdded

      participantRepository.put(
        Participant(id           = ParticipantId(event.id),
                    studyId      = StudyId(addedEvent.getStudyId),
                    version      = 0L,
                    timeAdded    = ISODateTimeFormat.dateTime.parseDateTime(event.getTime),
                    timeModified = None,
                    uniqueId     = addedEvent.getUniqueId,
                    annotations  = Utils.convertAnnotationsFromEvent(addedEvent.annotations)))
      ()
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyParticipantUpdatedEvent(event: ParticipantEvent) = {
    log.debug(s"applyParticipantUpdatedEvent: event/$event")

    if (event.eventType.isParticipantUpdated) {
      val updatedEvent = event.getParticipantUpdated

      val participantId = ParticipantId(event.id)
      val studyId = StudyId(updatedEvent.getStudyId)

      participantRepository.withId(studyId, participantId).fold(
        err => log.error(s"updating participant from event failed: $err"),
        p => {
          participantRepository.put(
            p.copy(version      = updatedEvent.getVersion,
                   uniqueId     = updatedEvent.getUniqueId,
                   timeModified = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime)),
                   annotations  = Utils.convertAnnotationsFromEvent(updatedEvent.annotations)))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

}
