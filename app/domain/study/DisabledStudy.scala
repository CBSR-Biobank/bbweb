package domain.study

import infrastructure.{ DomainError, DomainValidation, ReadRepository }
import infrastructure.commands._
import domain.{
  AnatomicalSourceType,
  AnnotationTypeId,
  AnnotationTypeIdentityService,
  AnnotationValueType,
  PreservationTemperatureType,
  PreservationType,
  SpecimenType
}
import AnatomicalSourceType._
import AnnotationValueType._
import PreservationTemperatureType._
import PreservationType._
import SpecimenType._

import scalaz._
import Scalaz._

case class DisabledStudy(id: StudyId, version: Long = -1, name: String, description: String)
  extends Study {

  override val status = "Disabled"

  def enable(specimenGroupCount: Int, collectionEventTypecount: Int): DomainValidation[EnabledStudy] =
    if ((specimenGroupCount == 0) || (collectionEventTypecount == 0))
      DomainError("study has no specimen groups and / or no collection event types").fail
    else
      EnabledStudy(id, version + 1, name, description).success

  def addSpecimenGroup(
    specimenGroupRepository: ReadRepository[SpecimenGroupId, SpecimenGroup],
    id: SpecimenGroupId,
    version: Long,
    name: String,
    description: String,
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType): DomainValidation[SpecimenGroup] =
    specimenGroupRepository.getValues.exists {
      item => item.studyId.equals(this.id) && !item.id.equals(id) && item.name.equals(name)
    } match {
      case true =>
        DomainError("specimen group with name already exists: %s" format name).fail
      case false =>
        SpecimenGroup(id, version, this.id, name, description, units, anatomicalSourceType,
          preservationType, preservationTemperatureType, specimenType).success
    }

  def addSpecimenGroup(
    specimenGroupRepository: ReadRepository[SpecimenGroupId, SpecimenGroup],
    cmd: AddSpecimenGroupCmd): DomainValidation[SpecimenGroup] =
    addSpecimenGroup(specimenGroupRepository, SpecimenGroupIdentityService.nextIdentity,
      version = 0L, cmd.name, cmd.description, cmd.units, cmd.anatomicalSourceType,
      cmd.preservationType, cmd.preservationTemperatureType, cmd.specimenType)

  def updateSpecimenGroup(
    specimenGroupRepository: ReadRepository[SpecimenGroupId, SpecimenGroup],
    cmd: UpdateSpecimenGroupCmd): DomainValidation[SpecimenGroup] =
    for {
      prevItem <- specimenGroupRepository.getByKey(new SpecimenGroupId(cmd.specimenGroupId))
      validVersion <- prevItem.requireVersion(cmd.expectedVersion)
      validStudy <- validateSpecimenGroupId(specimenGroupRepository, prevItem.id)
      newItem <- addSpecimenGroup(specimenGroupRepository, prevItem.id, prevItem.version + 1,
        cmd.name, cmd.description, cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
        cmd.preservationTemperatureType, cmd.specimenType)
    } yield newItem

  def removeSpecimenGroup(
    specimenGroupRepository: ReadRepository[SpecimenGroupId, SpecimenGroup],
    cmd: RemoveSpecimenGroupCmd): DomainValidation[SpecimenGroup] =
    for {
      item <- specimenGroupRepository.getByKey(new SpecimenGroupId(cmd.specimenGroupId))
      validVersion <- item.requireVersion(cmd.expectedVersion)
      validStudy <- validateSpecimenGroupId(specimenGroupRepository, item.id)
    } yield item

  def addCollectionEventType(
    collectionEventTypeRepository: ReadRepository[CollectionEventTypeId, CollectionEventType],
    id: CollectionEventTypeId,
    version: Long,
    name: String,
    description: String,
    recurring: Boolean): DomainValidation[CollectionEventType] =
    collectionEventTypeRepository.getValues.exists {
      item => item.studyId.equals(this.id) && !item.id.equals(id) && item.name.equals(name)
    } match {
      case true =>
        DomainError("collection event type with name already exists: %s" format name).fail
      case false =>
        CollectionEventType(id, version, this.id, name, description, recurring).success
    }

  def addCollectionEventType(
    collectionEventTypeRepository: ReadRepository[CollectionEventTypeId, CollectionEventType],
    cmd: AddCollectionEventTypeCmd): DomainValidation[CollectionEventType] =
    addCollectionEventType(collectionEventTypeRepository,
      CollectionEventTypeIdentityService.nextIdentity, version = 0L, cmd.name,
      cmd.description, cmd.recurring)

  def updateCollectionEventType(
    collectionEventTypeRepository: ReadRepository[CollectionEventTypeId, CollectionEventType],
    cmd: UpdateCollectionEventTypeCmd): DomainValidation[CollectionEventType] =
    for {
      prevItem <- collectionEventTypeRepository.getByKey(new CollectionEventTypeId(cmd.collectionEventTypeId))
      validVersion <- prevItem.requireVersion(cmd.expectedVersion)
      validStudy <- validateCollectionEventTypeId(collectionEventTypeRepository, prevItem.id)
      newItem <- addCollectionEventType(collectionEventTypeRepository, prevItem.id, prevItem.version + 1,
        cmd.name, cmd.description, cmd.recurring)
    } yield newItem

  def removeCollectionEventType(
    collectionEventTypeRepository: ReadRepository[CollectionEventTypeId, CollectionEventType],
    cmd: RemoveCollectionEventTypeCmd): DomainValidation[CollectionEventType] =
    for {
      item <- collectionEventTypeRepository.getByKey(new CollectionEventTypeId(cmd.collectionEventTypeId))
      validVersion <- item.requireVersion(cmd.expectedVersion)
      validStudy <- validateCollectionEventTypeId(collectionEventTypeRepository, item.id)
    } yield item

  def addCollectionEventAnnotationType(
    annotationTypeRepo: ReadRepository[AnnotationTypeId, StudyAnnotationType],
    id: AnnotationTypeId,
    version: Long,
    name: String,
    description: String,
    valueType: AnnotationValueType,
    maxValueCount: Int,
    options: Map[String, String]): DomainValidation[CollectionEventAnnotationType] = {
    annotationTypeRepo.getValues.exists {
      item => item.studyId.equals(this.id) && !item.id.equals(id) && item.name.equals(name)
    } match {
      case true =>
        DomainError("collection event annotation type with name already exists: %s" format name).fail
      case false =>
        CollectionEventAnnotationType(id, version, this.id, name, description, valueType,
          maxValueCount, options).success
    }
  }

  def addCollectionEventAnnotationType(
    annotationTypeRepo: ReadRepository[AnnotationTypeId, StudyAnnotationType],
    cmd: AddCollectionEventAnnotationTypeCmd): DomainValidation[CollectionEventAnnotationType] = {
    addCollectionEventAnnotationType(annotationTypeRepo,
      AnnotationTypeIdentityService.nextIdentity, version = 0L,
      cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options)
  }

  def updateCollectionEventAnnotationType(
    annotationTypeRepo: ReadRepository[AnnotationTypeId, StudyAnnotationType],
    cmd: UpdateCollectionEventAnnotationTypeCmd): DomainValidation[CollectionEventAnnotationType] = {
    for {
      prevItem <- annotationTypeRepo.getByKey(new AnnotationTypeId(cmd.annotationTypeId))
      validVersion <- prevItem.requireVersion(cmd.expectedVersion)
      validStudy <- validateCollectionEventAnnotationTypeId(annotationTypeRepo, prevItem.id)
      newItem <- addCollectionEventAnnotationType(annotationTypeRepo, prevItem.id,
        prevItem.version + 1, cmd.name, cmd.description, cmd.valueType,
        cmd.maxValueCount, cmd.options)
    } yield newItem
  }

  def removeCollectionEventAnnotationType(
    studyAnnotationTypeRepository: ReadRepository[AnnotationTypeId, StudyAnnotationType],
    cmd: RemoveCollectionEventAnnotationTypeCmd): DomainValidation[CollectionEventAnnotationType] = {
    for {
      item <- studyAnnotationTypeRepository.getByKey(new AnnotationTypeId(cmd.annotationTypeId))
      validVersion <- item.requireVersion(cmd.expectedVersion)
      ceAttrType <- validateCollectionEventAnnotationTypeId(studyAnnotationTypeRepository, item.id)
    } yield ceAttrType

  }
}
