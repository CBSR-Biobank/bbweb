package service.study

import service.commands._
import service.events._
import domain._
import domain.study.{
  CollectionEventAnnotationType,
  CollectionEventType,
  CollectionEventTypeAnnotationType,
  CollectionEventTypeId,
  CollectionEventTypeRepository,
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
protected[service] class CollectionEventTypeService() extends CommandHandler {

  val log = LoggerFactory.getLogger(this.getClass)

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

    val item = for {
      cetId <- id.toSuccess(DomainError("collection event type ID is missing"))
      newItem <- CollectionEventTypeRepository.add(CollectionEventType(
        CollectionEventTypeId(cetId), 0L, study.id, cmd.name, cmd.description, cmd.recurring,
        cmd.specimenGroupData, cmd.annotationTypeData))
      event <- listeners.sendEvent(CollectionEventTypeAddedEvent(
        study.id, newItem.id, newItem.name, newItem.description, newItem.recurring,
        newItem.specimenGroupData, newItem.annotationTypeData)).success
    } yield newItem
    logMethod(log, "addCollectionEventType", cmd, item)
    item
  }

  private def updateCollectionEventType(
    cmd: UpdateCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {

    val item = for {
      oldItem <- CollectionEventTypeRepository.collectionEventTypeWithId(
        study.id, CollectionEventTypeId(cmd.id))
      newItem <- CollectionEventTypeRepository.update(CollectionEventType(
        CollectionEventTypeId(cmd.id), cmd.expectedVersion.getOrElse(-1), study.id, cmd.name,
        cmd.description, cmd.recurring, cmd.specimenGroupData, cmd.annotationTypeData))
      event <- listeners.sendEvent(CollectionEventTypeUpdatedEvent(
        study.id, newItem.id, newItem.name, newItem.description, newItem.recurring,
        newItem.specimenGroupData, newItem.annotationTypeData)).success
    } yield newItem
    logMethod(log, "updateCollectionEventType", cmd, item)
    item
  }

  private def removeCollectionEventType(
    cmd: RemoveCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {
    val item = for {
      oldItem <- CollectionEventTypeRepository.collectionEventTypeWithId(
        study.id, CollectionEventTypeId(cmd.id))
      itemToRemove <- CollectionEventType(
        CollectionEventTypeId(cmd.id), cmd.expectedVersion.getOrElse(-1), study.id,
        oldItem.name, oldItem.description, oldItem.recurring, oldItem.specimenGroupData,
        oldItem.annotationTypeData).success
      removedItem <- CollectionEventTypeRepository.remove(itemToRemove)
      event <- listeners.sendEvent(CollectionEventTypeRemovedEvent(
        removedItem.studyId, removedItem.id)).success
    } yield oldItem
    logMethod(log, "removeCollectionEventType", cmd, item)
    item
  }
}

