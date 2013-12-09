package service.study

import service._
import service.commands.StudyCommands._
import service.events.StudyEvents._

import domain.{
  AnnotationTypeId,
  ConcurrencySafeEntity,
  DomainValidation,
  DomainError,
  Entity,
  RepositoryComponent,
  UserId
}
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._
import domain.AnnotationValueType._

import domain.study._
import Study._

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import akka.event.Logging
import akka.actor.ActorLogging
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import scala.language.postfixOps
import org.eligosource.eventsourced.core._
import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

case class StudyMessage(cmd: Any, userId: UserId, time: Long, listeners: MessageEmitter)

trait StudyProcessorComponent {

  trait StudyProcessor extends Processor

}

/**
 * This is the Study Aggregate Processor.
 *
 * It handles the commands to configure studies.
 *
 * @param studyRepository The repository for study entities.
 * @param specimenGroupRepository The repository for specimen group entities.
 * @param cetRepo The repository for Container Event Type entities.
 * @param annotationTypeRepo The repository for Collection Event Annotation Type entities.
 * @param sg2cetRepo The value object repository that associates a specimen group to a
 *         collection event type.
 * @param at2cetRepo The value object repository that associates a collection event annotation
 *         type to a collection event type.
 */
trait StudyProcessorComponentImpl extends StudyProcessorComponent {
  self: ProcessorComponentImpl with RepositoryComponent =>

  class StudyProcessorImpl extends StudyProcessor {
    self: Emitter =>

    def receive = {
      case serviceMsg: ServiceMsg =>
        serviceMsg.cmd match {
          case cmd: AddStudyCmd =>
            process(serviceMsg, addStudy(cmd, serviceMsg.id), emitter(Configuration.EventBusChannelId))

          case cmd: UpdateStudyCmd =>
            process(serviceMsg, updateStudy(cmd), emitter(Configuration.EventBusChannelId))

          case cmd: EnableStudyCmd =>
            process(serviceMsg, enableStudy(cmd), emitter(Configuration.EventBusChannelId))

          case cmd: DisableStudyCmd =>
            process(serviceMsg, disableStudy(cmd), emitter(Configuration.EventBusChannelId))

          case cmd: SpecimenGroupCommand =>
            process(
              serviceMsg,
              processEntityMsg(cmd, cmd.studyId, serviceMsg.id, specimenGroupService.process),
              emitter(Configuration.EventBusChannelId))

          case cmd: CollectionEventTypeCommand =>
            process(
              serviceMsg,
              processEntityMsg(cmd, cmd.studyId, serviceMsg.id, collectionEventTypeService.process),
              emitter(Configuration.EventBusChannelId))

          case cmd: CollectionEventAnnotationTypeCommand =>
            process(
              serviceMsg,
              processEntityMsg(cmd, cmd.studyId, serviceMsg.id, ceventAnnotationTypeService.process),
              emitter(Configuration.EventBusChannelId))

          case cmd: ParticipantAnnotationTypeCommand =>
            process(
              serviceMsg,
              processEntityMsg(cmd, cmd.studyId, serviceMsg.id, participantAnnotationTypeService.process),
              emitter(Configuration.EventBusChannelId))

          case cmd: SpecimenLinkAnnotationTypeCommand =>
            process(
              serviceMsg,
              processEntityMsg(cmd, cmd.studyId, serviceMsg.id, specimenLinkAnnotationTypeService.process),
              emitter(Configuration.EventBusChannelId))

          case other => // must be for another command handler
        }

      case msg =>
        throw new Error("invalid message received: ")
    }

    private def validateStudy(studyId: StudyId): DomainValidation[DisabledStudy] =
      studyRepository.studyWithId(studyId) match {
        case Failure(msglist) => noSuchStudy(studyId).fail
        case Success(study) => study match {
          case _: EnabledStudy => notDisabledError(study.name).fail
          case dstudy: DisabledStudy => dstudy.success
        }
      }

    private def processEntityMsg[T](
      cmd: StudyCommand,
      studyId: String,
      id: Option[String],
      processFunc: StudyProcessorMsg => DomainValidation[T]): DomainValidation[T] = {
      for {
        study <- validateStudy(new StudyId(studyId))
        event <- processFunc(StudyProcessorMsg(cmd, study, id))
      } yield event
    }

    private def addStudy(
      cmd: AddStudyCmd,
      id: Option[String]): DomainValidation[StudyAddedEvent] = {

      for {
        studyId <- id.toSuccess(DomainError("study ID is missing"))
        newItem <- studyRepository.add(
          DisabledStudy(new StudyId(studyId), version = 0L, cmd.name, cmd.description))
        event <- StudyAddedEvent(newItem.id.id, newItem.version, newItem.name, newItem.description).success
      } yield event
    }

    private def updateStudy(cmd: UpdateStudyCmd): DomainValidation[StudyUpdatedEvent] = {
      for {
        newItem <- studyRepository.update(DisabledStudy(
          new StudyId(cmd.id), cmd.expectedVersion.getOrElse(-1), cmd.name, cmd.description))
        event <- StudyUpdatedEvent(newItem.id.id, newItem.version, newItem.name, newItem.description).success
      } yield event
    }

    private def enableStudy(cmd: EnableStudyCmd): DomainValidation[StudyEnabledEvent] = {
      val studyId = StudyId(cmd.id)
      for {
        study <- studyRepository.enable(studyId,
          specimenGroupRepository.allSpecimenGroupsForStudy(studyId).size,
          collectionEventTypeRepository.allCollectionEventTypesForStudy(studyId).size)
        event <- StudyEnabledEvent(studyId.id, study.version).success
      } yield event
    }

    private def disableStudy(cmd: DisableStudyCmd): DomainValidation[StudyDisabledEvent] = {
      val studyId = StudyId(cmd.id)
      for {
        study <- studyRepository.disable(studyId)
        event <- StudyDisabledEvent(studyId.id, study.version).success
      } yield event
    }
  }
}
