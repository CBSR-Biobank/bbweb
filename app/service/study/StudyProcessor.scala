package service.study

import service._
import infrastructure.command.StudyCommands._
import infrastructure.event.StudyEvents._
import service.Messages._

import domain.{
  DomainValidation,
  DomainError,
  RepositoryComponent,
  UserId
}

import domain.study._
import Study._

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import scala.language.postfixOps
import org.slf4j.LoggerFactory
import akka.persistence._

import scalaz._
import scalaz.Scalaz._

case class StudyMessage(cmd: Any, userId: UserId, time: Long)

trait StudyProcessorComponent {

  trait StudyProcessor extends EventsourcedProcessor

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

    val receiveRecover: Receive = {
      case _ =>
    }

    val receiveCommand: Receive = {
      case serviceMsg: ServiceMsg =>
        serviceMsg.cmd match {
          case cmd: AddStudyCmd =>
            addStudy(cmd)

          case cmd: UpdateStudyCmd => updateStudy(cmd)

          case cmd: EnableStudyCmd => enableStudy(cmd)

          case cmd: DisableStudyCmd => disableStudy(cmd)

          case cmd: SpecimenGroupCommand =>
            processEntityMsg(cmd, cmd.studyId, serviceMsg.id, specimenGroupService.process)

          case cmd: CollectionEventTypeCommand =>
            processEntityMsg(cmd, cmd.studyId, serviceMsg.id, collectionEventTypeService.process)

          case cmd: CollectionEventAnnotationTypeCommand =>
            processEntityMsg(cmd, cmd.studyId, serviceMsg.id, ceventAnnotationTypeService.process)

          case cmd: ParticipantAnnotationTypeCommand =>
            processEntityMsg(cmd, cmd.studyId, serviceMsg.id, participantAnnotationTypeService.process)

          case cmd: SpecimenLinkAnnotationTypeCommand =>
            processEntityMsg(cmd, cmd.studyId, serviceMsg.id, specimenLinkAnnotationTypeService.process)

          case other => // must be for another command handler
        }

      case msg =>
        throw new Error("invalid message received: ")
    }

    private def validateStudy(studyId: StudyId): DomainValidation[DisabledStudy] =
      studyRepository.studyWithId(studyId) match {
        case Failure(msglist) => noSuchStudy(studyId).failNel
        case Success(study) => study match {
          case _: EnabledStudy => notDisabledError(study.name).failNel
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

    private def addStudy(cmd: AddStudyCmd): DomainValidation[StudyAddedEvent] = {

      val studyId = studyRepository.nextIdentity

      val e = for {
        nameAvailable <- studyRepository.nameAvailable(cmd.name)
        newStudy <- DisabledStudy(
          studyId,
          version = 0L,
          cmd.name,
          cmd.description).successNel
        event <- StudyAddedEvent(
          newStudy.id.id,
          newStudy.version,
          newStudy.name,
          newStudy.description).successNel
      } yield event

      e.map(event =>
        persist(event) { e =>
          context.system.eventStream.publish(e)
        })
      e
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
