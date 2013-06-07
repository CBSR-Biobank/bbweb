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

class CollectionEventTypeDomainService(
  studyRepository: ReadRepository[StudyId, Study],
  collectionEventTypeRepository: ReadWriteRepository[CollectionEventTypeId, CollectionEventType],
  specimenGroupRepository: ReadRepository[SpecimenGroupId, SpecimenGroup],
  annotationTypeRepo: ReadWriteRepository[AnnotationTypeId, CollectionEventAnnotationType],
  sg2cetRepo: ReadWriteRepository[String, SpecimenGroupCollectionEventType],
  cet2atRepo: ReadWriteRepository[String, CollectionEventTypeAnnotationType]) {

  def process = PartialFunction[Any, DomainValidation[_]] {
    // collection event types
    case _@ (study: DisabledStudy, cmd: AddCollectionEventTypeCmd, listeners: MessageEmitter) =>
      addCollectionEventType(study, cmd, listeners)
    case _@ (study: DisabledStudy, cmd: UpdateCollectionEventTypeCmd, listeners: MessageEmitter) =>
      updateCollectionEventType(study, cmd, listeners)
    case _@ (study: DisabledStudy, cmd: RemoveCollectionEventTypeCmd, listeners: MessageEmitter) =>
      removeCollectionEventType(study, cmd, listeners)

    // specimen group -> collection event types
    case _@ (study: DisabledStudy, cmd: AddSpecimenGroupToCollectionEventTypeCmd, listeners: MessageEmitter) =>
      addSpecimenGroupToCollectionEventType(study, cmd, listeners)
    case _@ (study: DisabledStudy, cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd, listeners: MessageEmitter) =>
      removeSpecimenGroupFromCollectionEventType(study, cmd, listeners)

    // collection event  annotations
    case _@ (study: DisabledStudy, cmd: AddCollectionEventAnnotationTypeCmd, listeners: MessageEmitter) =>
      addCollectionEventAnnotationTypeCmd(study, cmd, listeners)
    case _@ (study: DisabledStudy, cmd: UpdateCollectionEventAnnotationTypeCmd, listeners: MessageEmitter) =>
      updateCollectionEventAnnotationTypeCmd(study, cmd, listeners)
    case _@ (study: DisabledStudy, cmd: RemoveCollectionEventAnnotationTypeCmd, listeners: MessageEmitter) =>
      removeCollectionEventAnnotationTypeCmd(study, cmd, listeners)

    // collection event annotation options
    case _@ (study: DisabledStudy, cmd: AddCollectionEventAnnotationOptionsCmd, listeners: MessageEmitter) =>
      addCollectionEventAnnotationOptionsCmd(study, cmd, listeners)
    case _@ (study: DisabledStudy, cmd: UpdateCollectionEventAnnotationOptionsCmd, listeners: MessageEmitter) =>
      updateCollectionEventAnnotationOptionsCmd(study, cmd, listeners)
    case _@ (study: DisabledStudy, cmd: RemoveCollectionEventAnnotationOptionsCmd, listeners: MessageEmitter) =>
      removeCollectionEventAnnotationOptionsCmd(study, cmd, listeners)

    // annotation types -> collection event types
    case _@ (study: DisabledStudy, cmd: AddAnnotationTypeToCollectionEventTypeCmd, listeners: MessageEmitter) =>
      addAnnotationTypeToCollectionEventType(study, cmd, listeners)
    case _@ (study: DisabledStudy, cmd: RemoveAnnotationTypeFromCollectionEventTypeCmd, listeners: MessageEmitter) =>
      removeAnnotationTypeFromCollectionEventType(study, cmd, listeners)

    case _ =>
      throw new Error("invalid command received")
  }

  private def addCollectionEventType(study: DisabledStudy,
    cmd: AddCollectionEventTypeCmd,
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

  private def updateCollectionEventType(study: DisabledStudy, cmd: UpdateCollectionEventTypeCmd,
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

  private def removeCollectionEventType(study: DisabledStudy, cmd: RemoveCollectionEventTypeCmd,
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

  private def addSpecimenGroupToCollectionEventType(study: DisabledStudy,
    cmd: AddSpecimenGroupToCollectionEventTypeCmd,
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

  private def removeSpecimenGroupFromCollectionEventType(study: DisabledStudy,
    cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd,
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

  private def addAnnotationTypeToCollectionEventType(study: DisabledStudy,
    cmd: AddAnnotationTypeToCollectionEventTypeCmd,
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

  private def removeAnnotationTypeFromCollectionEventType(study: DisabledStudy,
    cmd: RemoveAnnotationTypeFromCollectionEventTypeCmd,
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
    study: DisabledStudy,
    cmd: AddCollectionEventAnnotationTypeCmd,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {
    ???
  }
  private def updateCollectionEventAnnotationTypeCmd(
    study: DisabledStudy,
    cmd: UpdateCollectionEventAnnotationTypeCmd,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {
    ???
  }
  private def removeCollectionEventAnnotationTypeCmd(
    study: DisabledStudy,
    cmd: RemoveCollectionEventAnnotationTypeCmd,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {
    ???
  }
  private def addCollectionEventAnnotationOptionsCmd(
    study: DisabledStudy,
    cmd: AddCollectionEventAnnotationOptionsCmd,
    listeners: MessageEmitter): DomainValidation[AnnotationOption] = {
    ???
  }
  private def updateCollectionEventAnnotationOptionsCmd(
    study: DisabledStudy,
    cmd: UpdateCollectionEventAnnotationOptionsCmd,
    listeners: MessageEmitter): DomainValidation[AnnotationOption] = {
    ???
  }
  private def removeCollectionEventAnnotationOptionsCmd(
    study: DisabledStudy,
    cmd: RemoveCollectionEventAnnotationOptionsCmd,
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
