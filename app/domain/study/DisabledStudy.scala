package domain.study

import infrastructure._
import infrastructure.commands._
import domain.{
  AnnotationTypeId,
  CollectionEventTypeRepository,
  CollectionEventAnnotationTypeRepository,
  DomainError,
  DomainValidation,
  SpecimenGroupRepository
}
import domain.AnatomicalSourceType._
import domain.AnnotationValueType._
import domain.PreservationTemperatureType._
import domain.PreservationType._
import domain.SpecimenType._

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
    id: SpecimenGroupId,
    version: Long,
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType): DomainValidation[SpecimenGroup] =
    SpecimenGroupRepository.getValues.exists {
      item => item.studyId.equals(this.id) && !item.id.equals(id) && item.name.equals(name)
    } match {
      case true =>
        DomainError("specimen group with name already exists: %s" format name).fail
      case false =>
        SpecimenGroup(id, version, this.id, name, description, units, anatomicalSourceType,
          preservationType, preservationTemperatureType, specimenType).success
    }

  def addSpecimenGroup(
    cmd: AddSpecimenGroupCmd,
    id: String): DomainValidation[SpecimenGroup] =
    addSpecimenGroup(new SpecimenGroupId(id),
      version = 0L, cmd.name, cmd.description, cmd.units, cmd.anatomicalSourceType,
      cmd.preservationType, cmd.preservationTemperatureType, cmd.specimenType)

  def updateSpecimenGroup(
    cmd: UpdateSpecimenGroupCmd): DomainValidation[SpecimenGroup] =
    for {
      prevItem <- SpecimenGroupRepository.getByKey(new SpecimenGroupId(cmd.id))
      validVersion <- prevItem.requireVersion(cmd.expectedVersion)
      newItem <- addSpecimenGroup(prevItem.id, prevItem.version + 1,
        cmd.name, cmd.description, cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
        cmd.preservationTemperatureType, cmd.specimenType)
    } yield newItem

  def removeSpecimenGroup(
    cmd: RemoveSpecimenGroupCmd): DomainValidation[SpecimenGroup] =
    for {
      item <- SpecimenGroupRepository.getByKey(new SpecimenGroupId(cmd.id))
      validVersion <- item.requireVersion(cmd.expectedVersion)
    } yield item

  def addCollectionEventType(
    id: CollectionEventTypeId,
    version: Long,
    name: String,
    description: Option[String],
    recurring: Boolean): DomainValidation[CollectionEventType] =
    CollectionEventTypeRepository.getValues.exists {
      item => item.studyId.equals(this.id) && !item.id.equals(id) && item.name.equals(name)
    } match {
      case true =>
        DomainError("collection event type with name already exists: %s" format name).fail
      case false =>
        CollectionEventType(id, version, this.id, name, description, recurring).success
    }

  def addCollectionEventType(
    cmd: AddCollectionEventTypeCmd,
    id: String): DomainValidation[CollectionEventType] =
    addCollectionEventType(
      new CollectionEventTypeId(id), version = 0L, cmd.name, cmd.description, cmd.recurring)

  def updateCollectionEventType(
    cmd: UpdateCollectionEventTypeCmd): DomainValidation[CollectionEventType] =
    for {
      prevItem <- CollectionEventTypeRepository.getByKey(new CollectionEventTypeId(cmd.id))
      validVersion <- prevItem.requireVersion(cmd.expectedVersion)
      newItem <- addCollectionEventType(prevItem.id, prevItem.version + 1,
        cmd.name, cmd.description, cmd.recurring)
    } yield newItem

  def removeCollectionEventType(
    cmd: RemoveCollectionEventTypeCmd): DomainValidation[CollectionEventType] =
    for {
      item <- CollectionEventTypeRepository.getByKey(new CollectionEventTypeId(cmd.id))
      validVersion <- item.requireVersion(cmd.expectedVersion)
    } yield item

  def addCollectionEventAnnotationType(
    id: AnnotationTypeId,
    version: Long,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]]): DomainValidation[CollectionEventAnnotationType] = {
    CollectionEventAnnotationTypeRepository.getValues.exists {
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
    cmd: AddCollectionEventAnnotationTypeCmd,
    id: String): DomainValidation[CollectionEventAnnotationType] = {
    addCollectionEventAnnotationType(new AnnotationTypeId(id), version = 0L,
      cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options)
  }

  def updateCollectionEventAnnotationType(
    cmd: UpdateCollectionEventAnnotationTypeCmd): DomainValidation[CollectionEventAnnotationType] = {
    for {
      prevItem <- CollectionEventAnnotationTypeRepository.getByKey(new AnnotationTypeId(cmd.id))
      validVersion <- prevItem.requireVersion(cmd.expectedVersion)
      newItem <- addCollectionEventAnnotationType(prevItem.id, prevItem.version + 1, cmd.name,
        cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options)
    } yield newItem
  }

  def removeCollectionEventAnnotationType(
    cmd: RemoveCollectionEventAnnotationTypeCmd): DomainValidation[CollectionEventAnnotationType] = {
    for {
      item <- CollectionEventAnnotationTypeRepository.getByKey(new AnnotationTypeId(cmd.id))
      validVersion <- item.requireVersion(cmd.expectedVersion)
    } yield item

  }
}
