package service.study

import infrastructure._
import infrastructure.commands._
import infrastructure.events._
import domain._
import domain.study.{
  CollectionEventAnnotationType,
  CollectionEventType,
  CollectionEventTypeAnnotationType,
  CollectionEventTypeId,
  DisabledStudy,
  SpecimenGroup,
  SpecimenGroupId,
  SpecimenGroupCollectionEventType,
  StudyAnnotationType,
  Study,
  StudyId
}
import domain.study.Study._
import service._

import org.eligosource.eventsourced.core._
import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

/**
 * This is the Collection Event Type Domain Service.
 *
 * It handles commands that deal with a Collection Event Type.
 *
 * @param studyRepository The read-only repository for study entities.
 * @param collectionEventTypeRepository The repository for specimen group entities.
 * @param specimenGroupRepository The read-only repository for specimen group entities.
 * @param sg2cetRepo The value object repository that associates a specimen group to a
 *         collection event type.
 * @param at2cetRepo The value object repository that associates a collection event annotation
 *         type to a collection event type.
 */
protected[service] class CollectionEventTypeService(
  studyRepository: StudyReadRepository,
  collectionEventTypeRepository: CollectionEventTypeReadWriteRepository,
  specimenGroupRepository: SpecimenGroupReadRepository,
  annotationTypeRepo: CollectionEventAnnotationTypeReadWriteRepository)
  extends CommandHandler {
  import CollectionEventTypeService._

  /**
   * This partial function handles each command. The command is contained within the
   * StudyProcessorMsg.
   *
   *  If the command is invalid, then this method throws an Error exception.
   */
  def process = {

    case msg: StudyProcessorMsg =>
      msg.cmd match {
        // collection event types
        case cmd: AddCollectionEventTypeCmd =>
          addCollectionEventType(cmd, msg.study, msg.listeners, msg.id)
        case cmd: UpdateCollectionEventTypeCmd =>
          updateCollectionEventType(cmd, msg.study, msg.listeners)
        case cmd: RemoveCollectionEventTypeCmd =>
          removeCollectionEventType(cmd, msg.study, msg.listeners)

        case _ =>
          throw new Error("invalid command received")
      }

    case _ =>
      throw new Error("invalid message received")
  }

  private def addCollectionEventType(
    cmd: AddCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter,
    id: Option[String]): DomainValidation[CollectionEventType] = {
    def addItem(item: CollectionEventType) {
      collectionEventTypeRepository.updateMap(item);
      listeners sendEvent CollectionEventTypeAddedEvent(
        study.id, item.id, item.name, item.description, item.recurring)
    }

    val item = for {
      cetId <- id.toSuccess(DomainError("collection event type ID is missing"))
      newItem <- study.addCollectionEventType(collectionEventTypeRepository, cmd, cetId)
      addItem <- addItem(newItem).success
    } yield newItem
    CommandHandler.logMethod(log, "addCollectionEventType", cmd, item)
    item
  }

  private def updateCollectionEventType(
    cmd: UpdateCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {
    def update(item: CollectionEventType): CollectionEventType = {
      collectionEventTypeRepository.updateMap(item)
      listeners sendEvent CollectionEventTypeUpdatedEvent(
        study.id, item.id, item.name, item.description, item.recurring)
      item
    }

    val item = for {
      validStudy <- StudyValidation.validateCollectionEventTypeId(study, collectionEventTypeRepository, cmd.id)
      newItem <- study.updateCollectionEventType(collectionEventTypeRepository, cmd)
      item <- update(newItem).success
    } yield newItem
    CommandHandler.logMethod(log, "updateCollectionEventType", cmd, item)
    item
  }

  private def removeCollectionEventType(
    cmd: RemoveCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {

    def removeItem(item: CollectionEventType) = {
      collectionEventTypeRepository.remove(item)
      listeners sendEvent CollectionEventTypeRemovedEvent(study.id, item.id)
    }

    val item = for {
      validStudy <- StudyValidation.validateCollectionEventTypeId(study, collectionEventTypeRepository, cmd.id)
      item <- study.removeCollectionEventType(collectionEventTypeRepository, cmd)
      removedItem <- removeItem(item).success
    } yield item
    CommandHandler.logMethod(log, "removeCollectionEventType", cmd, item)
    item
  }

}

object CollectionEventTypeService {
  val log = LoggerFactory.getLogger(SpecimenGroupService.getClass)
}
