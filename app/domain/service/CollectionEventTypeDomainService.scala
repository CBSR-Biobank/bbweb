package domain.service

import org.eligosource.eventsourced.core._

import domain._
import domain.study._
import infrastructure._
import infrastructure.commands._
import infrastructure.events._
import domain.study.{
  CollectionEventType,
  CollectionEventTypeId,
  DisabledStudy,
  EnabledStudy,
  SpecimenGroup,
  SpecimenGroupId,
  Study,
  StudyId
}
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
  annotationTypeRepo: ReadWriteRepository[AnnotationTypeId, CollectionEventAnnotationType],
  sg2cetRepo: ReadWriteRepository[String, SpecimenGroupCollectionEventType],
  cet2atRepo: ReadWriteRepository[String, CollectionEventTypeAnnotationType]) {

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

    // collection event  annotations
    case _@ (cmd: AddCollectionEventAnnotationTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      addCollectionEventAnnotationTypeCmd(cmd, study, listeners)
    case _@ (cmd: UpdateCollectionEventAnnotationTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      updateCollectionEventAnnotationTypeCmd(cmd, study, listeners)
    case _@ (cmd: RemoveCollectionEventAnnotationTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      removeCollectionEventAnnotationTypeCmd(cmd, study, listeners)

    // collection event annotation options
    case _@ (cmd: AddCollectionEventAnnotationOptionsCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      addCollectionEventAnnotationOptionsCmd(cmd, study, listeners)
    case _@ (cmd: UpdateCollectionEventAnnotationOptionsCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      updateCollectionEventAnnotationOptionsCmd(cmd, study, listeners)
    case _@ (cmd: RemoveCollectionEventAnnotationOptionsCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      removeCollectionEventAnnotationOptionsCmd(cmd, study, listeners)

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
      v1 <- validateSpecimenGroupId(study, cmd.specimenGroupId)
      v2 <- validateCollectionEventTypeId(study, cmd.collectionEventTypeId)
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
      v1 <- validateCollectionEventTypeId(study, cmd.collectionEventTypeId)
      v2 <- validateAnnotationTypeId(study, cmd.annotationTypeId)
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

  private def addCollectionEventAnnotationTypeCmd(
    cmd: AddCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {
    ???
  }
  private def updateCollectionEventAnnotationTypeCmd(
    cmd: UpdateCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {
    ???
  }
  private def removeCollectionEventAnnotationTypeCmd(
    cmd: RemoveCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {
    ???
  }
  private def addCollectionEventAnnotationOptionsCmd(
    cmd: AddCollectionEventAnnotationOptionsCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[AnnotationOption] = {
    ???
  }
  private def updateCollectionEventAnnotationOptionsCmd(
    cmd: UpdateCollectionEventAnnotationOptionsCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[AnnotationOption] = {
    ???
  }
  private def removeCollectionEventAnnotationOptionsCmd(
    cmd: RemoveCollectionEventAnnotationOptionsCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[AnnotationOption] = {
    ???
  }

  private def validateSpecimenGroupId(study: DisabledStudy,
    specimenGroupId: String): DomainValidation[SpecimenGroup] = {
    specimenGroupRepository.getByKey(new SpecimenGroupId(specimenGroupId)) match {
      case Some(sg) =>
        if (study.id.equals(sg.studyId)) sg.success
        else DomainError("specimen group does not belong to study: %s" format specimenGroupId).fail
      case None =>
        DomainError("specimen group does not exist: %s" format specimenGroupId).fail
    }
  }

  private def validateCollectionEventTypeId(study: DisabledStudy,
    collectionEventTypeId: String): DomainValidation[CollectionEventType] = {
    collectionEventTypeRepository.getByKey(new CollectionEventTypeId(collectionEventTypeId)) match {
      case Some(cet) =>
        if (study.id.equals(cet.studyId)) cet.success
        else DomainError("collection event type does not belong to study: %s" format collectionEventTypeId).fail
      case None =>
        DomainError("collection event type does not exist: %s" format collectionEventTypeId).fail
    }
  }

  /**
   * Validates that the CollectionEventAnnotationType with id {@link annotationTypeId} exists
   * and that it belongs to {@link study}.
   */
  private def validateAnnotationTypeId(study: DisabledStudy,
    annotationTypeId: String): DomainValidation[CollectionEventAnnotationType] = {
    annotationTypeRepo.getByKey(new AnnotationTypeId(annotationTypeId)) match {
      case Some(annot) =>
        if (study.id.equals(annot.studyId)) annot.success
        else DomainError("CE annotation type does not belong to study: %s" format annotationTypeId).fail
      case None =>
        DomainError("CE annotation type does not exist: %s" format annotationTypeId).fail
    }
  }

}
