package org.biobank.service.participants

import org.biobank.service.Processor
import org.biobank.infrastructure.command.ParticipantCommands._
import org.biobank.infrastructure.event.ParticipantEvents._
import org.biobank.infrastructure.event.CommonEvents._
import org.biobank.domain.{ AnnotationTypeId, AnnotationOption, DomainValidation, DomainError }
import org.biobank.domain.user.UserId
import org.biobank.domain.study._
import org.biobank.domain.participants._

import javax.inject.{Inject => javaxInject, Named}
import akka.actor._
import akka.pattern.ask
import org.slf4j.LoggerFactory
import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
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

  val participantRepository:    ParticipantRepository,
  val annotationTypeRepository: ParticipantAnnotationTypeRepository,
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
    case cmd: CollectionEventCommand => validateAndForward(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(participantRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"ParticipantsProcessor: message not handled: $cmd")
  }

  private def validateAndForward(cmd: CollectionEventCommand) = {
    participantRepository.getByKey(ParticipantId(cmd.participantId)).fold(
      err => context.sender ! DomainError(s"invalid participant id: ${cmd.participantId}").failureNel,
      participant => {
        studyRepository.getByKey(participant.studyId).fold(
          err => context.sender ! DomainError(s"invalid study id: ${cmd.participantId}").failureNel,
          study => study match {
            case s: EnabledStudy => collectionEventsProcessor forward cmd
            case _ => context.sender ! DomainError(s"study is not enabled: ${study.id}").failureNel
          }
        )
      }
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

  /**
    * Checks the following:
    *
    *   - no more than one annotation per annotation type
    *   - that each required annotation is present
    *   - that all annotations belong to the same study as the annotation type.
    *
    * A DomainError is the result if these conditions fail.
    */
  private def validateAnnotationTypes(studyId: StudyId, annotations: Set[ParticipantAnnotation])
      : DomainValidation[Boolean]= {
    val annotAnnotTypeIdsAsSet = annotations.map(v => v.annotationTypeId).toSet
    val annotAnnotTypeIdsAsList = annotations.toList.map(v => v.annotationTypeId).toList

    if (annotAnnotTypeIdsAsSet.size != annotAnnotTypeIdsAsList.size) {
      DomainError("duplicate annotation types in annotations").failureNel
    } else {
      val requiredAnnotTypeIds = annotationTypeRepository.getValues.filter(at =>
        at.studyId.equals(studyId) && at.required).map(at => at.id).toSet

      if (requiredAnnotTypeIds.intersect(annotAnnotTypeIdsAsSet).size != requiredAnnotTypeIds.size) {
        DomainError("missing required annotation type(s)").failureNel
      } else {
        val invalidSet = annotAnnotTypeIdsAsSet.map { id =>
          (id -> annotationTypeRepository.withId(studyId, id).isSuccess)
        }.filter(x => !x._2).map(_._1)

        if (! invalidSet.isEmpty) {
          DomainError("annotation type(s) do not belong to study: " + invalidSet.mkString(", ")).failureNel
        } else {
          true.success
        }
      }
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
      annotTypes        <- validateAnnotationTypes(studyId, cmd.annotations.toSet)
      newParticip       <- Participant.create(studyId,
                                              participantId,
                                              -1L,
                                              DateTime.now,
                                              cmd.uniqueId,
                                              cmd.annotations.toSet)
      event             <- createEvent(newParticip, cmd).withParticipantAdded(
        ParticipantAddedEvent(
          studyId       = Some(newParticip.studyId.id),
          uniqueId      = Some(cmd.uniqueId),
          annotations   = convertAnnotationsToEvent(cmd.annotations))).success
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
      annotTypes        <- validateAnnotationTypes(studyId, annotationsSet)
      particip          <- participantRepository.getByKey(participantId)
      newParticip       <- Participant.create(studyId,
                                              participantId,
                                              particip.version,
                                              DateTime.now,
                                              cmd.uniqueId,
                                              annotationsSet)
      event             <- createEvent(newParticip, cmd).withParticipantUpdated(
        ParticipantUpdatedEvent(
          studyId       = Some(newParticip.studyId.id),
          version       = Some(newParticip.version),
          uniqueId      = Some(cmd.uniqueId),
          annotations   = convertAnnotationsToEvent(cmd.annotations))).success
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
                    annotations  = convertAnnotationsFromEvent(addedEvent.annotations)))
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
                   annotations  = convertAnnotationsFromEvent(updatedEvent.annotations)))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def convertAnnotationsToEvent(annotations: List[ParticipantAnnotation])
      : Seq[Annotation] = {
    annotations.map { annot =>
      Annotation(
        annotationTypeId = Some(annot.annotationTypeId.id),
        stringValue      = annot.stringValue,
        numberValue      = annot.numberValue,
        selectedValues   = annot.selectedValues.map(_.value)
      )
    }
  }

  private def convertAnnotationsFromEvent(annotations: Seq[Annotation])
      : Set[ParticipantAnnotation] = {
    annotations.map { eventAnnot =>
      ParticipantAnnotation(
        annotationTypeId = AnnotationTypeId(eventAnnot.getAnnotationTypeId),
        stringValue      = eventAnnot.stringValue,
        numberValue      = eventAnnot.numberValue,
        selectedValues   = eventAnnot.selectedValues.map { selectedValue =>
          AnnotationOption(
            annotationTypeId = AnnotationTypeId(eventAnnot.getAnnotationTypeId),
            value            = selectedValue
          )
        } toList
      )
    } toSet
  }
}
