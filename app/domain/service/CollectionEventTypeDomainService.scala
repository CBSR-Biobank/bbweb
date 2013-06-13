package domain.service

import org.eligosource.eventsourced.core._

import domain._
import infrastructure._
import infrastructure.commands._
import infrastructure.events._
import domain.study.{
  CollectionEventAnnotationType,
  CollectionEventType,
  CollectionEventTypeAnnotationType,
  CollectionEventTypeId,
  CollectionEventTypeAnnotationTypeIdentityService,
  DisabledStudy,
  EnabledStudy,
  SpecimenGroup,
  SpecimenGroupId,
  SpecimenGroupCollectionEventType,
  SpecimenGroupCollectionEventTypeIdentityService,
  StudyAnnotationType,
  Study,
  StudyId
}
import Study._
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
class CollectionEventTypeDomainService(
  studyRepository: ReadRepository[StudyId, Study],
  collectionEventTypeRepository: ReadWriteRepository[CollectionEventTypeId, CollectionEventType],
  specimenGroupRepository: ReadRepository[SpecimenGroupId, SpecimenGroup],
  annotationTypeRepo: ReadWriteRepository[AnnotationTypeId, StudyAnnotationType],
  sg2cetRepo: ReadWriteRepository[String, SpecimenGroupCollectionEventType],
  cet2atRepo: ReadWriteRepository[String, CollectionEventTypeAnnotationType])
  extends CommandHandler {

  /**
   * This partial function handles each command. The input is a Tuple3 consisting of:
   *
   *  1. The command to handle.
   *  2. The study entity the command is associated with,
   *  3. The event message listener to be notified if the command is successful.
   *
   *  If the command is invalid, then this method throws an Error exception.
   */
  def process = {
    // collection event types
    case _@ (cmd: AddCollectionEventTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      addCollectionEventType(cmd, study, listeners)
    case _@ (cmd: UpdateCollectionEventTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      updateCollectionEventType(cmd, study, listeners)
    case _@ (cmd: RemoveCollectionEventTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      removeCollectionEventType(cmd, study, listeners)

    // specimen group -> collection event types
    case _@ (cmd: AddSpecimenGroupToCollectionEventTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      addSpecimenGroupToCollectionEventType(cmd, study, listeners)
    case _@ (cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      removeSpecimenGroupFromCollectionEventType(cmd, study, listeners)

    // annotation types -> collection event types
    case _@ (cmd: AddAnnotationTypeToCollectionEventTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      addAnnotationTypeToCollectionEventType(cmd, study, listeners)
    case _@ (cmd: RemoveAnnotationTypeFromCollectionEventTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      removeAnnotationTypeFromCollectionEventType(cmd, study, listeners)

    case _ =>
      throw new Error("invalid command received")
  }

  private def addCollectionEventType(
    cmd: AddCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {
    def addItem(item: CollectionEventType) {
      collectionEventTypeRepository.updateMap(item);
      listeners sendEvent CollectionEventTypeAddedEvent(
        study.id, item.id, item.name, item.description, item.recurring)
    }

    for {
      collectionEventTypes <- collectionEventTypeRepository.getMap.filter {
        case (_, item) => item.studyId.equals(study.id)
      }.success
      newItem <- study.addCollectionEventType(collectionEventTypes, cmd)
      addItem <- addItem(newItem).success
    } yield newItem
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

    for {
      prevItem <- validateCollectionEventTypeId(study, collectionEventTypeRepository, cmd.collectionEventTypeId)
      versionCheck <- prevItem.requireVersion(cmd.expectedVersion)
      collectionEventTypes <- collectionEventTypeRepository.getMap.filter {
        case (_, item) => item.studyId.equals(study.id)
      }.success
      newItem <- study.updateCollectionEventType(collectionEventTypes, prevItem, cmd)
      item <- update(newItem).success
    } yield item
  }

  private def removeCollectionEventType(
    cmd: RemoveCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {

    def removeItem(item: CollectionEventType) = {
      collectionEventTypeRepository.remove(item)
      listeners sendEvent CollectionEventTypeRemovedEvent(study.id, item.id)
    }

    for {
      item <- validateCollectionEventTypeId(study, collectionEventTypeRepository, cmd.collectionEventTypeId)
      versionCheck <- item.requireVersion(cmd.expectedVersion)
      removedItem <- removeItem(item).success
    } yield item
  }

  private def addSpecimenGroupToCollectionEventType(
    cmd: AddSpecimenGroupToCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[SpecimenGroupCollectionEventType] = {

    def createItem(sg: SpecimenGroup, cet: CollectionEventType) = {
      val item = cet.addSpecimenGroup(sg, cmd.count, cmd.amount)
      sg2cetRepo.updateMap(item)
      listeners sendEvent SpecimenGroupAddedToCollectionEventTypeEvent(
        study.id, item.id, item.collectionEventTypeId, item.specimenGroupId, item.count, item.amount)
      item
    }

    for {
      v1 <- validateSpecimenGroupId(study, specimenGroupRepository, cmd.specimenGroupId)
      v2 <- validateCollectionEventTypeId(study, collectionEventTypeRepository, cmd.collectionEventTypeId)
      v3 <- createItem(v1, v2).success
    } yield v3
  }

  private def removeSpecimenGroupFromCollectionEventType(
    cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[SpecimenGroupCollectionEventType] = {

    def removeItem(item: SpecimenGroupCollectionEventType) = {
      sg2cetRepo.remove(item)
      listeners sendEvent SpecimenGroupRemovedFromCollectionEventTypeEvent(
        study.id, item.id, item.collectionEventTypeId, item.specimenGroupId)
      item.success
    }

    for {
      item <- sg2cetRepo.getByKey(cmd.sg2cetId)
      removedItem <- removeItem(item)
    } yield removedItem
  }

  private def addAnnotationTypeToCollectionEventType(
    cmd: AddAnnotationTypeToCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventTypeAnnotationType] = {
    def createItem(cet: CollectionEventType,
      cetAt: CollectionEventAnnotationType): CollectionEventTypeAnnotationType = {
      val item = cet.addAnnotationType(cetAt, cmd.required)
      cet2atRepo.updateMap(item)
      listeners sendEvent AnnotationTypeAddedToCollectionEventTypeEvent(
        study.id, item.id, item.collectionEventTypeId, item.annotationTypeId)
      item
    }

    for {
      v1 <- validateCollectionEventTypeId(study, collectionEventTypeRepository, cmd.collectionEventTypeId)
      v2 <- validateCollectionEventAnnotationTypeId(study, annotationTypeRepo, cmd.annotationTypeId)
      v3 <- createItem(v1, v2).success
    } yield v3
  }

  private def removeAnnotationTypeFromCollectionEventType(
    cmd: RemoveAnnotationTypeFromCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventTypeAnnotationType] = {

    def removeItem(item: CollectionEventTypeAnnotationType): CollectionEventTypeAnnotationType = {
      cet2atRepo.remove(item)
      listeners sendEvent AnnotationTypeRemovedFromCollectionEventTypeEvent(
        study.id, item.id, item.collectionEventTypeId, item.annotationTypeId)
      item
    }

    for {
      item <- cet2atRepo.getByKey(cmd.cetAtId)
      removedItem <- removeItem(item).success
    } yield removedItem
  }

}
