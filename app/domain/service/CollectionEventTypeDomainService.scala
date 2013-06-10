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
import domain.service.StudyValidationUtil._
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
  extends DomainService {

  /**
   * This partial function handles each command. The input is a Tuple3 consisting of:
   *
   *  1. The command to handle.
   *  2. The study entity the command is associated with,
   *  3. The event message listener to be notified if the command is successful.
   *
   *  If the command is invalid, then this method throws an Error exception.
   */
  def process = PartialFunction[Any, DomainValidation[_]] {
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
    val collectionEventTypes = collectionEventTypeRepository.getMap.filter(
      cet => cet._2.studyId.equals(study.id))
    val v = study.addCollectionEventType(collectionEventTypes, cmd)
    v match {
      case Success(cet) =>
        collectionEventTypeRepository.updateMap(cet)
        listeners sendEvent CollectionEventTypeAddedEvent(
          cmd.studyId, cet.name, cet.description, cet.recurring)
      case _ => // nothing to do in this case
    }
    v
  }

  private def updateCollectionEventType(
    cmd: UpdateCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {
    val collectionEventTypeId = new CollectionEventTypeId(cmd.collectionEventTypeId)
    Entity.update(collectionEventTypeRepository.getByKey(collectionEventTypeId),
      collectionEventTypeId, cmd.expectedVersion) { prevCet =>
        val cet = CollectionEventType(collectionEventTypeId, study.id, prevCet.version + 1,
          cmd.name, cmd.description, cmd.recurring)
        collectionEventTypeRepository.updateMap(cet)
        listeners sendEvent CollectionEventTypeUpdatedEvent(
          cmd.studyId, cmd.collectionEventTypeId, cet.name, cet.description, cet.recurring)
        cet.success
      }
  }

  private def removeCollectionEventType(
    cmd: RemoveCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {
    val collectionEventTypeId = new CollectionEventTypeId(cmd.collectionEventTypeId)
    collectionEventTypeRepository.getByKey(collectionEventTypeId) match {
      case None =>
        DomainError("collection event type does not exist: %s" format cmd.collectionEventTypeId).fail
      case Some(cet) =>
        collectionEventTypeRepository.remove(cet)
        listeners sendEvent CollectionEventTypeRemovedEvent(cmd.studyId, cmd.collectionEventTypeId)
        cet.success
    }
  }

  private def addSpecimenGroupToCollectionEventType(
    cmd: AddSpecimenGroupToCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[SpecimenGroupCollectionEventType] = {
    def createItem(sg: SpecimenGroup, cet: CollectionEventType) = {
      val sg2cet = SpecimenGroupCollectionEventType(
        SpecimenGroupCollectionEventTypeIdentityService.nextIdentity,
        sg.id, cet.id, cmd.count, cmd.amount)
      sg2cetRepo.updateMap(sg2cet)
      listeners sendEvent SpecimenGroupAddedToCollectionEventTypeEvent(
        cmd.studyId, cmd.collectionEventTypeId, cmd.specimenGroupId, cmd.count, cmd.amount)
      sg2cet.success
    }

    for {
      v1 <- validateSpecimenGroupId(study, specimenGroupRepository, cmd.specimenGroupId)
      v2 <- validateCollectionEventTypeId(study, collectionEventTypeRepository, cmd.collectionEventTypeId)
      v3 <- createItem(v1, v2)
    } yield v3
  }

  private def removeSpecimenGroupFromCollectionEventType(
    cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[SpecimenGroupCollectionEventType] = {
    sg2cetRepo.getByKey(cmd.sg2cetId) match {
      case Some(cg2cet) =>
        sg2cetRepo.remove(cg2cet)
        listeners sendEvent SpecimenGroupRemovedFromCollectionEventTypeEvent(cmd.sg2cetId)
        cg2cet.success
      case None =>
        DomainError("specimen group -> collection event type does not exist: %s" format cmd.sg2cetId).fail
    }
  }

  private def addAnnotationTypeToCollectionEventType(
    cmd: AddAnnotationTypeToCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventTypeAnnotationType] = {

    def createItem(cet: CollectionEventType, cetAt: CollectionEventAnnotationType) = {
      val cetAnnotationType = CollectionEventTypeAnnotationType(
        CollectionEventTypeAnnotationTypeIdentityService.nextIdentity,
        cet.id, cetAt.id, cmd.required)
      cet2atRepo.updateMap(cetAnnotationType)
      listeners sendEvent AnnotationTypeAddedToCollectionEventTypeEvent(
        cmd.studyId, cmd.collectionEventTypeId, cmd.annotationTypeId)
      cetAnnotationType.success
    }

    for {
      v1 <- validateCollectionEventTypeId(study, collectionEventTypeRepository, cmd.collectionEventTypeId)
      v2 <- validateCollectionEventAnnotationTypeId(study, annotationTypeRepo, cmd.annotationTypeId)
      v3 <- createItem(v1, v2)
    } yield v3
  }

  private def removeAnnotationTypeFromCollectionEventType(
    cmd: RemoveAnnotationTypeFromCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventTypeAnnotationType] = {
    cet2atRepo.getByKey(cmd.cetAtId) match {
      case Some(cet2at) =>
        cet2atRepo.remove(cet2at)
        listeners sendEvent AnnotationTypeRemovedFromCollectionEventTypeEvent(cmd.studyId, cmd.cetAtId)
        cet2at.success
      case None =>
        DomainError("annotation type -> collection event type does not exist: %s" format
          cmd.cetAtId).fail
    }
  }

}
