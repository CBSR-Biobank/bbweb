package service

import infrastructure._
import infrastructure.commands._
import infrastructure.events._
import domain.{
  AnnotationTypeId,
  ConcurrencySafeEntity,
  Entity,
  UserId
}
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._
import domain.AnnotationValueType._
import domain.study._
import domain.service.{
  CollectionEventTypeDomainService,
  SpecimenGroupDomainService,
  StudyAnnotationTypeDomainService
}
import Study._

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import akka.event.Logging
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
class StudyProcessor(
  studyRepository: ReadWriteRepository[StudyId, Study],
  specimenGroupRepository: ReadWriteRepository[SpecimenGroupId, SpecimenGroup],
  cetRepo: ReadWriteRepository[CollectionEventTypeId, CollectionEventType],
  annotationTypeRepo: ReadWriteRepository[AnnotationTypeId, StudyAnnotationType],
  sg2cetRepo: ReadWriteRepository[String, SpecimenGroupCollectionEventType],
  at2cetRepo: ReadWriteRepository[String, CollectionEventTypeAnnotationType])
  extends Processor with akka.actor.ActorLogging { this: Emitter =>

  /**
   * The domain service that handles specimen group commands.
   */
  val specimenGroupDomainService = new SpecimenGroupDomainService(
    studyRepository, specimenGroupRepository)

  /**
   * The domain service that handles collection event type commands.
   */
  val collectionEventTypeDomainService = new CollectionEventTypeDomainService(
    studyRepository, cetRepo, specimenGroupRepository, annotationTypeRepo,
    sg2cetRepo, at2cetRepo)

  val annotationTypeDomainService = new StudyAnnotationTypeDomainService(
    annotationTypeRepo)

  def receive = {
    case msg: org.eligosource.eventsourced.core.Message =>
      msg.event match {
        case bbMsg: BiobankMsg =>
          bbMsg.cmd match {
            case cmd: AddStudyCmdWithId =>
              process(addStudy(StudyMessage(bbMsg.cmd, bbMsg.userId: UserId, msg.timestamp,
                emitter("listeners"))))
          }

        case bbMsg: BiobankMsg =>
      }

    //    case cmd: UpdateStudyCmd =>
    //      process(updateStudy(cmd, emitter("listeners")))
    //
    //    case cmd: EnableStudyCmd =>
    //      process(enableStudy(cmd, emitter("listeners")))
    //
    //    case cmd: DisableStudyCmd =>
    //      process(disableStudy(cmd, emitter("listeners")))
    //
    //    case cmd: SpecimenGroupCommand =>
    //      process(validateStudy(studyRepository, cmd.studyId) { study =>
    //        specimenGroupDomainService.process(cmd, study, emitter("listeners"))
    //      })
    //
    //    case cmd: CollectionEventTypeCommand =>
    //      process(validateStudy(studyRepository, cmd.studyId) { study =>
    //        collectionEventTypeDomainService.process(cmd, study, emitter("listeners"))
    //      })
    //
    //    case cmd: StudyAnnotationTypeCommand =>
    //      process(validateStudy(studyRepository, cmd.studyId) { study =>
    //        annotationTypeDomainService.process(cmd, study, emitter("listeners"))
    //      })

    case _ =>
      throw new Error("invalid command received: ")
  }

  def logMethod(methodName: String, cmd: Any, study: DomainValidation[Study]) {
    if (log.isDebugEnabled) {
      log.debug("%s: %s".format(methodName, cmd))
      study match {
        case Success(item) =>
          log.debug("%s: %s".format(methodName, item))
        case Failure(msglist) =>
          log.debug("%s: { msg: %s }".format(methodName, msglist.head))
      }
    }
  }

  private def addStudy(
    id: StudyId,
    version: Long,
    name: String,
    description: Option[String],
    addedBy: UserId,
    timeAdded: Long,
    updatedBy: Option[UserId] = None,
    timeUpdated: Option[Long] = None): DomainValidation[DisabledStudy] = {

    def nameCheck(id: StudyId, name: String): DomainValidation[Boolean] = {
      studyRepository.getValues.exists {
        item => !item.id.equals(id) && item.name.equals(name)
      } match {
        case true => DomainError("study with name already exists: %s" format name).fail
        case false => true.success
      }
    }

    for {
      nameCheck <- nameCheck(id, name)
      newItem <- DisabledStudy(id, version, name, description, addedBy, timeAdded,
        updatedBy, timeUpdated).success
    } yield newItem
  }

  private def addStudy(msg: BiobankMsg): DomainValidation[DisabledStudy] = {

    def addItem(item: Study): Study = {
      studyRepository.updateMap(item)
      msg.listeners sendEvent StudyAddedEvent(item.id, item.name, item.description)
      item
    }

    val item = for {
      newItem <- addStudy(new StudyId(cmd.id), version = 0L, cmd.name, cmd.description, userId, time)
      addedItem <- addItem(newItem).success
    } yield newItem

    logMethod("addStudy", cmd, item)
    item
  }

  private def updateStudy(
    cmd: UpdateStudyCmd,
    listeners: MessageEmitter)(implicit userId: UserId, time: Long): DomainValidation[DisabledStudy] = {
    val studyId = new StudyId(cmd.id)

    def updateItem(item: Study) = {
      studyRepository.updateMap(item)
      listeners sendEvent StudyUpdatedEvent(item.id, item.name, item.description)
    }

    val item = for {
      prevItem <- studyRepository.getByKey(new StudyId(cmd.id))
      newItem <- addStudy(prevItem.id, prevItem.version + 1, cmd.name, cmd.description,
        prevItem.addedBy, prevItem.timeAdded, Some(userId), Some(time))
      updatedItem <- updateItem(newItem).success
    } yield newItem

    logMethod("updateStudy", cmd, item)
    item
  }

  private def enableStudy(cmd: EnableStudyCmd, listeners: MessageEmitter): DomainValidation[EnabledStudy] = {
    def enableStudy(study: Study): DomainValidation[EnabledStudy] = {
      study match {
        case es: EnabledStudy =>
          DomainError("study is already enabled: {id: %s}".format(es.id)).fail
        case ds: DisabledStudy =>
          val specimenGroupCount = specimenGroupRepository.getValues.find(
            s => s.studyId.equals(ds.id)).size
          val collectionEventTypecount = cetRepo.getValues.find(
            s => s.studyId.equals(ds.id)).size
          ds.enable(specimenGroupCount, collectionEventTypecount)
      }
    }

    def updateItem(item: Study) = {
      studyRepository.updateMap(item)
      listeners sendEvent StudyEnabledEvent(item.id)
    }

    val item = for {
      prevItem <- studyRepository.getByKey(new StudyId(cmd.id))
      newItem <- enableStudy(prevItem)
      updatedItem <- updateItem(newItem).success
    } yield newItem

    logMethod("enableStudy", cmd, item)
    item
  }

  private def disableStudy(cmd: DisableStudyCmd, listeners: MessageEmitter): DomainValidation[DisabledStudy] = {
    def disableStudy(study: Study): DomainValidation[DisabledStudy] = {
      study match {
        case ds: DisabledStudy =>
          DomainError("study is already disnabled: {id: %s}".format(ds.id)).fail
        case es: EnabledStudy =>
          es.disable
      }
    }

    def updateItem(item: Study) = {
      studyRepository.updateMap(item)
      listeners sendEvent StudyDisabledEvent(item.id)
    }

    val item = for {
      prevItem <- studyRepository.getByKey(new StudyId(cmd.id))
      newItem <- disableStudy(prevItem)
      updatedItem <- updateItem(newItem).success
    } yield newItem

    logMethod("disableStudy", cmd, item)
    item
  }
}
