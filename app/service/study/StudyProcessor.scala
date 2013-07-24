package service

import service.commands._
import service.events._
import service.study.{
  CollectionEventTypeService,
  SpecimenGroupService,
  StudyAnnotationTypeService
}
import service.study.SpecimenGroupService

import domain.{
  AnnotationTypeId,
  ConcurrencySafeEntity,
  DomainValidation,
  DomainError,
  Entity,
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

import scalaz._
import Scalaz._

case class StudyMessage(cmd: Any, userId: UserId, time: Long, listeners: MessageEmitter)

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
class StudyProcessor()
  extends Processor { this: Emitter =>

  /**
   * The domain service that handles specimen group commands.
   */
  val specimenGroupService = new SpecimenGroupService()

  /**
   * The domain service that handles collection event type commands.
   */
  val collectionEventTypeService = new CollectionEventTypeService()

  val annotationTypeService = new StudyAnnotationTypeService()

  def receive = {
    case serviceMsg: ServiceMsg =>
      serviceMsg.cmd match {
        case cmd: AddStudyCmd =>
          process(addStudy(cmd, emitter("listeners"), serviceMsg.id))

        case cmd: UpdateStudyCmd =>
          process(updateStudy(cmd, emitter("listeners")))

        case cmd: EnableStudyCmd =>
          process(enableStudy(cmd, emitter("listeners")))

        case cmd: DisableStudyCmd =>
          process(disableStudy(cmd, emitter("listeners")))

        case cmd: SpecimenGroupCommand =>
          processEntityMsg(cmd, cmd.studyId, serviceMsg.id, specimenGroupService.process)

        case cmd: CollectionEventTypeCommand =>
          processEntityMsg(cmd, cmd.studyId, serviceMsg.id, collectionEventTypeService.process)

        case cmd: StudyAnnotationTypeCommand =>
          processEntityMsg(cmd, cmd.studyId, serviceMsg.id, annotationTypeService.process)

        case other => // must be for another command handler
      }

    case _ =>
      throw new Error("invalid message received: ")
  }

  private def validateStudy(studyId: StudyId): DomainValidation[DisabledStudy] =
    StudyRepository.studyWithId(studyId) match {
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
    processFunc: StudyProcessorMsg => DomainValidation[T]) = {
    val updatedItem = for {
      study <- validateStudy(new StudyId(studyId))
      item <- processFunc(StudyProcessorMsg(cmd, study, emitter("listeners"), id))
    } yield item
    process(updatedItem)
  }

  override protected def process[T](validation: DomainValidation[T]) = {
    validation match {
      case Success(domainObject) =>
      // update the addedBy and updatedBy fields on the study aggregate
      case Failure(x) =>
      // do nothing
    }
    super.process(validation)
  }

  private def addStudy(
    cmd: AddStudyCmd,
    listeners: MessageEmitter,
    id: Option[String]): DomainValidation[DisabledStudy] = {

    val item = for {
      studyId <- id.toSuccess(DomainError("study ID is missing"))
      newItem <- StudyRepository.add(
        DisabledStudy(new StudyId(studyId), version = 0L, cmd.name, cmd.description))
      event <- listeners.sendEvent(StudyAddedEvent(
        newItem.id, newItem.name, newItem.description)).success
    } yield newItem

    logMethod("addStudy", cmd, item)
    item
  }

  private def updateStudy(
    cmd: UpdateStudyCmd,
    listeners: MessageEmitter): DomainValidation[DisabledStudy] = {

    val item = for {
      newItem <- StudyRepository.update(DisabledStudy(
        new StudyId(cmd.id), cmd.expectedVersion.getOrElse(-1), cmd.name, cmd.description))
      event <- listeners.sendEvent(StudyUpdatedEvent(
        newItem.id, newItem.name, newItem.description)).success
    } yield newItem

    logMethod("updateStudy", cmd, item)
    item
  }

  private def enableStudy(
    cmd: EnableStudyCmd,
    listeners: MessageEmitter): DomainValidation[EnabledStudy] = {
    val studyId = StudyId(cmd.id)
    val item = StudyRepository.enable(studyId,
      SpecimenGroupRepository.allSpecimenGroupsForStudy(studyId).size,
      CollectionEventTypeRepository.allCollectionEventTypesForStudy(studyId).size)
    logMethod("enableStudy", cmd, item)
    item
  }

  private def disableStudy(
    cmd: DisableStudyCmd,
    listeners: MessageEmitter): DomainValidation[DisabledStudy] = {
    val studyId = StudyId(cmd.id)
    val item = StudyRepository.disable(studyId)
    logMethod("enableStudy", cmd, item)
    item
  }
}
