package org.biobank.service.study

import org.biobank.service.{ Processor, WrappedCommand, WrappedEvent }
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{ AnnotationTypeId, DomainValidation, DomainError }
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
        case event: ParticipantAddedEvent => recoverEvent(event, wevent.userId, wevent.dateTime)
        case event: ParticipantUpdatedEvent => recoverEvent(event, wevent.userId, wevent.dateTime)

        case event => throw new IllegalStateException(s"event not handled: $event")
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
        case cmd: AddParticipantCmd =>    process(validateCmd(cmd)){ w => recoverEvent(w.event, w.userId, w.dateTime) }
        case cmd: UpdateParticipantCmd => process(validateCmd(cmd)){ w => recoverEvent(w.event, w.userId, w.dateTime) }

        case cmd => log.error(s"invalid wrapped command: $cmd")
      }

    case "snap" =>
      saveSnapshot(SnapshotState(participantRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"ParticipantsProcessor: message not handled: $cmd")

  }

  private def enabledStudy(studyId: StudyId): DomainValidation[EnabledStudy] = {
    studyRepository.getByKey(studyId).fold(
      err => DomainError(s"invalid study id: $studyId").failNel,
      study => study match {
        case st: EnabledStudy => st.success
        case _ => DomainError(s"study is not enabled: $studyId").failNel
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
      DomainError(s"$errMsgUniqueIdExists: $uniqueId").failNel
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
    * Checks that each required annotation is present and that all annotations belong to the same study as the
    * annotation type. If one or more annotations are found that belong to a different study, they are
    * returned in the DomainError.
    */
  private def validateAnnotationTypes(studyId: StudyId, annotations: Set[ParticipantAnnotation])
      : DomainValidation[Boolean]= {
    val annotAnnotTypeIds = annotations.map(v => v.annotationTypeId).toSet
    val requiredAnnotTypeIds = annotationTypeRepository.getValues.filter(at =>
      (at.studyId.id == studyId.id) && at.required).map(at => at.id).toSet

    log.info(s"********** annotAnnotTypeIds: $annotAnnotTypeIds")
    log.info(s"********** requiredAnnotTypeIds: $requiredAnnotTypeIds")

    if (requiredAnnotTypeIds.intersect(annotAnnotTypeIds).size != requiredAnnotTypeIds.size) {
      DomainError("missing required annotation type(s)").failNel
    } else {
      val invalidSet = annotAnnotTypeIds.map { id =>
        (id -> annotationTypeRepository.withId(studyId, id).isSuccess)
      }.filter(x => !x._2).map(_._1)

      if (! invalidSet.isEmpty) {
        DomainError("annotation type(s) do not belong to study: " + invalidSet.mkString(", ")).failNel
      } else {
        true.success
      }
    }
  }

  private def validateCmd(cmd: AddParticipantCmd): DomainValidation[ParticipantAddedEvent] = {
    val studyId = StudyId(cmd.studyId)
    val participantId = participantRepository.nextIdentity

    if (participantRepository.getByKey(participantId).isSuccess) {
      throw new IllegalStateException(s"participant with id already exsits: $participantId")
    }

    for {
      study <- enabledStudy(studyId)
      uniqueIdAvailable <- uniqueIdAvailable(cmd.uniqueId)
      annotTypes <- validateAnnotationTypes(studyId, cmd.annotations.toSet)
      newParticip <- Participant.create(
        studyId, participantId, -1L, DateTime.now, cmd.uniqueId, cmd.annotations.toSet)
      event <- ParticipantAddedEvent(studyId.id, participantId.id, cmd.uniqueId, cmd.annotations).success
    } yield event
  }

  private def validateCmd(cmd: UpdateParticipantCmd): DomainValidation[ParticipantUpdatedEvent] = {
    val studyId = StudyId(cmd.studyId)
    val participantId = ParticipantId(cmd.id)

    for {
      study <- enabledStudy(studyId)
      uniqueIdAvailable <- uniqueIdAvailable(cmd.uniqueId, participantId)
      annotTypes <- validateAnnotationTypes(studyId, cmd.annotations.toSet)
      particip <- participantRepository.getByKey(participantId)
      newParticip <- Participant.create(
        studyId, participantId, particip.version, DateTime.now, cmd.uniqueId, cmd.annotations.toSet)
      event <- ParticipantUpdatedEvent(
        studyId.id, participantId.id, particip.version, cmd.uniqueId, cmd.annotations).success
    } yield event
  }

  private def recoverEvent(event: ParticipantAddedEvent, userId: Option[UserId], dateTime: DateTime) {
    participantRepository.put(Participant(
      StudyId(event.studyId), ParticipantId(event.participantId), 0L, dateTime, None, event.uniqueId,
      event.annotations.toSet))
    ()
  }

  private def recoverEvent(event: ParticipantUpdatedEvent, userId: Option[UserId], dateTime: DateTime) {
    val studyId = StudyId(event.studyId)
    val participantId = ParticipantId(event.participantId)

    participantRepository.withId(studyId, participantId).fold(
      err => throw new IllegalStateException(s"updating participant from event failed: $err"),
      p => participantRepository.put(p.copy(
        version      = event.version,
        uniqueId     = event.uniqueId,
        timeModified = Some(dateTime),
        annotations  = event.annotations.toSet))
    )
    ()
  }

}
