package domain.study

import infrastructure._
import infrastructure.commands._
import domain._
import AnatomicalSourceType._
import AnnotationValueType._
import PreservationTemperatureType._
import PreservationType._
import SpecimenType._

import scalaz._
import Scalaz._

case class DisabledStudy(
  id: StudyId,
  version: Long = -1,
  name: String,
  description: Option[String])
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
    description: Option[String],
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
    cmd: AddSpecimenGroupCmd,
    id: String): DomainValidation[SpecimenGroup] =
    addSpecimenGroup(specimenGroupRepository, new SpecimenGroupId(id),
      version = 0L, cmd.name, cmd.description, cmd.units, cmd.anatomicalSourceType,
      cmd.preservationType, cmd.preservationTemperatureType, cmd.specimenType)

  def updateSpecimenGroup(
    specimenGroupRepository: ReadRepository[SpecimenGroupId, SpecimenGroup],
    cmd: UpdateSpecimenGroupCmd): DomainValidation[SpecimenGroup] =
    for {
      prevItem <- specimenGroupRepository.getByKey(new SpecimenGroupId(cmd.id))
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
      item <- specimenGroupRepository.getByKey(new SpecimenGroupId(cmd.id))
      validVersion <- item.requireVersion(cmd.expectedVersion)
      validStudy <- validateSpecimenGroupId(specimenGroupRepository, item.id)
    } yield item

  def addCollectionEventType(
    collectionEventTypeRepository: ReadRepository[CollectionEventTypeId, CollectionEventType],
    id: CollectionEventTypeId,
    version: Long,
    name: String,
    description: Option[String],
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
    cmd: AddCollectionEventTypeCmd,
    id: String): DomainValidation[CollectionEventType] =
    addCollectionEventType(collectionEventTypeRepository,
      new CollectionEventTypeId(id), version = 0L, cmd.name,
      cmd.description, cmd.recurring)

  def updateCollectionEventType(
    collectionEventTypeRepository: ReadRepository[CollectionEventTypeId, CollectionEventType],
    cmd: UpdateCollectionEventTypeCmd): DomainValidation[CollectionEventType] =
    for {
      prevItem <- collectionEventTypeRepository.getByKey(new CollectionEventTypeId(cmd.id))
      validVersion <- prevItem.requireVersion(cmd.expectedVersion)
      validStudy <- validateCollectionEventTypeId(collectionEventTypeRepository, prevItem.id)
      newItem <- addCollectionEventType(collectionEventTypeRepository, prevItem.id, prevItem.version + 1,
        cmd.name, cmd.description, cmd.recurring)
    } yield newItem

  def removeCollectionEventType(
    collectionEventTypeRepository: ReadRepository[CollectionEventTypeId, CollectionEventType],
    cmd: RemoveCollectionEventTypeCmd): DomainValidation[CollectionEventType] =
    for {
      item <- collectionEventTypeRepository.getByKey(new CollectionEventTypeId(cmd.id))
      validVersion <- item.requireVersion(cmd.expectedVersion)
      validStudy <- validateCollectionEventTypeId(collectionEventTypeRepository, item.id)
    } yield item

  def addCollectionEventAnnotationType(
    annotationTypeRepo: ReadRepository[AnnotationTypeId, StudyAnnotationType],
    id: AnnotationTypeId,
    version: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]]): DomainValidation[CollectionEventAnnotationType] = {
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
    annotationTypeRepo: ReadRepository[AnnotationTypeId, CollectionEventAnnotationType],
    cmd: AddCollectionEventAnnotationTypeCmd,
    id: String): DomainValidation[CollectionEventAnnotationType] = {
    addCollectionEventAnnotationType(annotationTypeRepo,
      new AnnotationTypeId(id), version = 0L,
      cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options)
  }

  def updateCollectionEventAnnotationType(
    annotationTypeRepo: ReadRepository[AnnotationTypeId, CollectionEventAnnotationType],
    cmd: UpdateCollectionEventAnnotationTypeCmd): DomainValidation[CollectionEventAnnotationType] = {
    for {
      prevItem <- annotationTypeRepo.getByKey(new AnnotationTypeId(cmd.id))
      validVersion <- prevItem.requireVersion(cmd.expectedVersion)
      validStudy <- validateCollectionEventAnnotationTypeId(annotationTypeRepo, prevItem.id)
      newItem <- addCollectionEventAnnotationType(annotationTypeRepo, prevItem.id,
        prevItem.version + 1, cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount,
        cmd.options)
    } yield newItem
  }

  def removeCollectionEventAnnotationType(
    studyAnnotationTypeRepository: ReadRepository[AnnotationTypeId, CollectionEventAnnotationType],
    cmd: RemoveCollectionEventAnnotationTypeCmd): DomainValidation[CollectionEventAnnotationType] = {
    for {
      item <- studyAnnotationTypeRepository.getByKey(new AnnotationTypeId(cmd.id))
      validVersion <- item.requireVersion(cmd.expectedVersion)
      ceAttrType <- validateCollectionEventAnnotationTypeId(studyAnnotationTypeRepository, item.id)
    } yield ceAttrType

  }
}
