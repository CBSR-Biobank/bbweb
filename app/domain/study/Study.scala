package domain.study

import domain.{ AnnotationTypeId, ConcurrencySafeEntity, DomainError, DomainValidation }
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._

import infrastructure.commands._

import scalaz._
import scalaz.Scalaz._

sealed abstract class Study extends ConcurrencySafeEntity[StudyId] {
  def name: String
  def description: String

  override def toString =
    "{ id:%s, version: %d, name:%s, description:%s }" format (id, version, name, description)
}

object Study {

  def add(name: String, description: String): DomainValidation[DisabledStudy] =
    DisabledStudy(StudyIdentityService.nextIdentity, version = 0L, name, description).success
}

case class DisabledStudy(id: StudyId, version: Long = -1, name: String, description: String)
  extends Study {

  def enable(specimenGroupCount: Int, collectionEventTypecount: Int): DomainValidation[EnabledStudy] =
    if ((specimenGroupCount == 0) || (collectionEventTypecount == 0))
      DomainError("study has no specimen groups and / or no collection event types").fail
    else
      EnabledStudy(id, version + 1, name, description).success

  def addSpecimenGroup(specimenGroups: Map[SpecimenGroupId, SpecimenGroup],
    cmd: AddSpecimenGroupCmd): DomainValidation[SpecimenGroup] =
    specimenGroups.find(sg => sg._2.name.equals(cmd.name)) match {
      case Some(sg) => DomainError("specimen group with name already exists: %s" format cmd.name).fail
      case None =>
        SpecimenGroup.add(this.id, cmd.name, cmd.description, cmd.units, cmd.anatomicalSourceType,
          cmd.preservationType, cmd.preservationTemperatureType, cmd.specimenType)
    }

  def addCollectionEventType(
    collectionEventTypes: Map[CollectionEventTypeId, CollectionEventType],
    cmd: AddCollectionEventTypeCmd): DomainValidation[CollectionEventType] =
    collectionEventTypes.values.find(cet => cet.name.equals(cmd.name)) match {
      case Some(sg) =>
        DomainError("collection event type with name already exists: %s" format cmd.name).fail
      case None =>
        CollectionEventType.add(this.id, cmd.name, cmd.description, cmd.recurring)
    }

  def updateCollectionEventType(
    collectionEventTypes: Map[CollectionEventTypeId, CollectionEventType],
    cmd: UpdateCollectionEventTypeCmd): DomainValidation[CollectionEventType] =
    collectionEventTypes.values.find(cet => cet.name.equals(cmd.name)) match {
      case None =>
        DomainError("collection event type does not exists: %s" format cmd.name).fail
      case Some(sg) =>
        CollectionEventType.add(this.id, cmd.name, cmd.description, cmd.recurring)
    }

  def addCollectionEventAnnotationType(
    collectionEventAnnotationTypes: Map[AnnotationTypeId, StudyAnnotationType],
    cmd: AddCollectionEventAnnotationTypeCmd): DomainValidation[CollectionEventAnnotationType] = {
    collectionEventAnnotationTypes.values.find(annot => annot.name.equals(cmd.name)) match {
      case Some(item) =>
        DomainError("collection event annotation type with name already exists: %s" format cmd.name).fail
      case None =>
        CollectionEventAnnotationType.add(this.id, cmd.name, cmd.description, cmd.valueType,
          cmd.maxValueCount, cmd.options)
    }
  }

  def updateCollectionEventAnnotationType(
    collectionEventAnnotationTypes: Map[AnnotationTypeId, StudyAnnotationType],
    cmd: UpdateCollectionEventAnnotationTypeCmd): DomainValidation[CollectionEventAnnotationType] = {
    collectionEventAnnotationTypes.values.find(annot => annot.name.equals(cmd.name)) match {
      case None =>
        DomainError("collection event annotation type does not exists: %s" format cmd.name).fail
      case Some(item) =>
        CollectionEventAnnotationType(item.id, item.version + 1, this.id, cmd.name,
          cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options).success
    }
  }
}

case class EnabledStudy(id: StudyId, version: Long = -1, name: String, description: String)
  extends Study {

  def disable: DomainValidation[DisabledStudy] =
    DisabledStudy(id, version + 1, name, description).success
}

