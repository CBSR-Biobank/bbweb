package org.biobank.service.study

import org.biobank.service.{ Processor, WrappedCommand, WrappedEvent }
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.infrastructure.event.StudyEventsJson._
import org.biobank.domain.{ AnnotationTypeId, AnnotationOption, DomainValidation, DomainError }
import org.biobank.domain.user.UserId
import org.biobank.domain.study._

import akka.actor. { ActorRef, Props }
import akka.pattern.ask
import org.slf4j.LoggerFactory
import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}
import scalaz._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
  * The ParticipantsProcessor is responsible for maintaining state changes for all
  * [[org.biobank.domain.study.Study]] aggregates. This particular processor uses Akka-Persistence's
  * [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist the generated
  * events, afterwhich it will updated the current state of the [[org.biobank.domain.study.Study]] being
  * processed.
  */
class ParticipantsProcessor(implicit inj: Injector) extends Processor with AkkaInjectable {

  override def persistenceId = "participant-processor-id"

  case class SnapshotState(participants: Set[Participant])

  val participantRepository = inject [ParticipantRepository]

  val annotationTypeRepository = inject [ParticipantAnnotationTypeRepository]

  // used to check the status of a study
  val studyRepository = inject [StudyRepository]

  /**
    * These are the events that are recovered during journal recovery. They cannot fail and must be
    * processed to recreate the current state of the aggregate.
    */
  val receiveRecover: Receive = {
    case wevent: WrappedEvent[_] =>
      wevent.event match {
        case event: ParticipantAddedEvent   =>
          recoverParticipantAddedEvent(event, wevent.userId, wevent.dateTime)
        case event: ParticipantUpdatedEvent =>
          recoverParticipantUpdatedEvent(event, wevent.userId, wevent.dateTime)

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
    case procCmd: WrappedCommand =>
      implicit val userId = procCmd.userId

      // order is important in the pattern match used below
      procCmd.command match {
        case cmd: AddParticipantCmd    => processAddParticipantCmd(cmd)
        case cmd: UpdateParticipantCmd => processUpdateParticipantCmd(cmd)

        case cmd => log.error(s"invalid wrapped command: $cmd")
      }

    case "snap" =>
      saveSnapshot(SnapshotState(participantRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"ParticipantsProcessor: message not handled: $cmd")

  }

  private def enabledStudy(studyId: StudyId): DomainValidation[EnabledStudy] = {
    studyRepository.getByKey(studyId).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => study match {
        case st: EnabledStudy => st.success
        case _ => DomainError(s"study is not enabled: $studyId").failureNel
      }
    )
  }

  val errMsgUniqueIdExists = "participant with unique ID already exists"

  /** Searches the repository for a matching item.
    */
  protected def uniqueIdAvailableMatcher(uniqueId: String)(matcher: Participant => Boolean)
      : DomainValidation[Boolean] = {
    val exists = participantRepository.getValues.exists { item =>
      matcher(item)
    }
    if (exists) {
      DomainError(s"$errMsgUniqueIdExists: $uniqueId").failureNel
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

  private def processAddParticipantCmd(cmd: AddParticipantCmd)(implicit userId: Option[UserId])
      : Unit = {
    val studyId = StudyId(cmd.studyId)
    val participantId = participantRepository.nextIdentity

    if (participantRepository.getByKey(participantId).isSuccess) {
      log.error(s"participant with id already exsits: $participantId")
    }

    val event = for {
      study <- enabledStudy(studyId)
      uniqueIdAvailable <- uniqueIdAvailable(cmd.uniqueId)
      annotTypes <- validateAnnotationTypes(studyId, cmd.annotations.toSet)
      newParticip <- Participant.create(
        studyId, participantId, -1L, DateTime.now, cmd.uniqueId, cmd.annotations.toSet)
      event <- ParticipantAddedEvent(
        studyId       = studyId.id,
        participantId = participantId.id,
        uniqueId      = Some(cmd.uniqueId),
        annotations   = convertAnnotationToEvent(cmd.annotations)).success
    } yield event

    process(event){ w =>
      recoverParticipantAddedEvent(w.event, w.userId, w.dateTime)
    }
  }

  private def processUpdateParticipantCmd(cmd: UpdateParticipantCmd)(implicit userId: Option[UserId])
      : Unit = {
    val studyId = StudyId(cmd.studyId)
    val participantId = ParticipantId(cmd.id)

    val event = for {
      study <- enabledStudy(studyId)
      uniqueIdAvailable <- uniqueIdAvailable(cmd.uniqueId, participantId)
      annotTypes <- validateAnnotationTypes(studyId, cmd.annotations.toSet)
      particip <- participantRepository.getByKey(participantId)
      newParticip <- Participant.create(
        studyId, participantId, particip.version, DateTime.now, cmd.uniqueId, cmd.annotations.toSet)
      event <- ParticipantUpdatedEvent(
        studyId       = studyId.id,
        participantId = participantId.id,
        version       = Some(particip.version),
        uniqueId      = Some(cmd.uniqueId),
        annotations   = convertAnnotationToEvent(cmd.annotations)).success
    } yield event

    process(event){ w =>
      recoverParticipantUpdatedEvent(w.event, w.userId, w.dateTime)
    }
  }

  private def recoverParticipantAddedEvent
    (event: ParticipantAddedEvent, userId: Option[UserId], dateTime: DateTime) {
    participantRepository.put(Participant(
      studyId      = StudyId(event.studyId),
      id           = ParticipantId(event.participantId),
      version      = 0L,
      timeAdded    = dateTime,
      timeModified = None,
      uniqueId     = event.getUniqueId,
      annotations  = convertAnnotationsFromEvent(event.annotations)))
    ()
  }

  private def recoverParticipantUpdatedEvent
    (event: ParticipantUpdatedEvent, userId: Option[UserId], dateTime: DateTime) {
    val studyId = StudyId(event.studyId)
    val participantId = ParticipantId(event.participantId)

    participantRepository.withId(studyId, participantId).fold(
      err => log.error(s"updating participant from event failed: $err"),
      p => {
        participantRepository.put(p.copy(
        version      = event.getVersion,
        uniqueId     = event.getUniqueId,
        timeModified = Some(dateTime),
        annotations  = convertAnnotationsFromEvent(event.annotations)))
        ()
      }
    )
  }

  private def convertAnnotationToEvent
    (annotations: List[ParticipantAnnotation])
      : Seq[ParticipantAddedEvent.ParticipantAnnotation] = {
    annotations.map { annot =>
      ParticipantAddedEvent.ParticipantAnnotation(
        annotationTypeId = annot.annotationTypeId.id,
        stringValue      = annot.stringValue,
        numberValue      = annot.numberValue,
        selectedValues   = annot.selectedValues.map(_.value)
      )
    }
  }

  private def convertAnnotationsFromEvent
    (annotations: Seq[ParticipantAddedEvent.ParticipantAnnotation])
      : Set[ParticipantAnnotation] = {
    annotations.map { eventAnnot =>
      ParticipantAnnotation(
        annotationTypeId = AnnotationTypeId(eventAnnot.annotationTypeId),
        stringValue      = eventAnnot.stringValue,
        numberValue      = eventAnnot.numberValue,
        selectedValues   = eventAnnot.selectedValues.map { selectedValue =>
          AnnotationOption(
            annotationTypeId = AnnotationTypeId(eventAnnot.annotationTypeId),
            value            = selectedValue
          )
        } toList
      )
    } toSet
  }
}
