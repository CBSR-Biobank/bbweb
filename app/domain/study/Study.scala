package domain.study

import domain.{ AnnotationTypeId, AnnotationTypeIdentityService, ConcurrencySafeEntity }
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._
import domain.AnnotationValueType._

import infrastructure._
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

  def noSuchStudy(studyId: StudyId) =
    DomainError("no study with id: %s" format studyId)

  def notDisabledError(name: String) =
    DomainError("study is not disabled: %s" format name)

  def notEnabledError(name: String) =
    DomainError("study is not enabled: %s" format name)

  def validateStudy(
    studyIdAsString: String,
    studyRepository: ReadWriteRepository[StudyId, Study])(f: DisabledStudy => DomainValidation[_]) = {
    val studyId = new StudyId(studyIdAsString)
    studyRepository.getByKey(studyId) match {
      case Failure(msglist) => noSuchStudy(studyId).fail
      case Success(study) => study match {
        case study: EnabledStudy => notDisabledError(study.name).fail
        case study: DisabledStudy => f(study)
      }
    }
  }

  def validateSpecimenGroupId(study: DisabledStudy,
    specimenGroupRepository: ReadRepository[SpecimenGroupId, SpecimenGroup],
    specimenGroupId: String): DomainValidation[SpecimenGroup] = {
    specimenGroupRepository.getByKey(new SpecimenGroupId(specimenGroupId)) match {
      case Success(sg) =>
        if (study.id.equals(sg.studyId)) sg.success
        else DomainError("specimen group does not belong to study: %s" format specimenGroupId).fail
      case Failure(x) =>
        DomainError("specimen group does not exist: %s" format specimenGroupId).fail
    }
  }

  def validateCollectionEventTypeId(
    study: DisabledStudy,
    collectionEventTypeRepository: ReadWriteRepository[CollectionEventTypeId, CollectionEventType],
    collectionEventTypeId: String): DomainValidation[CollectionEventType] = {
    collectionEventTypeRepository.getByKey(new CollectionEventTypeId(collectionEventTypeId)) match {
      case Success(cet) =>
        if (study.id.equals(cet.studyId)) cet.success
        else DomainError("collection event type does not belong to study: %s" format collectionEventTypeId).fail
      case Failure(x) =>
        DomainError("collection event type does not exist: %s" format collectionEventTypeId).fail
    }
  }

  /**
   * Validates that the CollectionEventAnnotationType with id {@link annotationTypeId} exists
   * and that it belongs to {@link study}.
   */
  def validateCollectionEventAnnotationTypeId(
    study: DisabledStudy,
    annotationTypeRepo: ReadRepository[AnnotationTypeId, StudyAnnotationType],
    annotationTypeId: String): DomainValidation[CollectionEventAnnotationType] = {
    annotationTypeRepo.getByKey(new AnnotationTypeId(annotationTypeId)) match {
      case Success(annot) =>
        if (study.id.equals(annot.studyId)) {
          annot match {
            case ceAnnot: CollectionEventAnnotationType => ceAnnot.success
            case _ =>
              DomainError("annotation type is not for a collection event type: %s"
                format annotationTypeId).fail
          }
        } else
          DomainError("CE annotation type does not belong to study: %s" format annotationTypeId).fail
      case Failure(x) =>
        DomainError("CE annotation type does not exist: %s" format annotationTypeId).fail
    }
  }
}

case class DisabledStudy(id: StudyId, version: Long = -1, name: String, description: String)
  extends Study {

  def enable(specimenGroupCount: Int, collectionEventTypecount: Int): DomainValidation[EnabledStudy] =
    if ((specimenGroupCount == 0) || (collectionEventTypecount == 0))
      DomainError("study has no specimen groups and / or no collection event types").fail
    else
      EnabledStudy(id, version + 1, name, description).success

  def addSpecimenGroup(
    specimenGroups: Map[SpecimenGroupId, SpecimenGroup],
    id: SpecimenGroupId,
    version: Long,
    name: String,
    description: String,
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType): DomainValidation[SpecimenGroup] =
    specimenGroups.find(sg => sg._2.name.equals(name)) match {
      case Some(sg) =>
        DomainError("specimen group with name already exists: %s" format name).fail
      case None =>
        SpecimenGroup(id, this.id, version, name, description, units, anatomicalSourceType,
          preservationType, preservationTemperatureType, specimenType).success
    }

  def addSpecimenGroup(
    specimenGroups: Map[SpecimenGroupId, SpecimenGroup],
    cmd: AddSpecimenGroupCmd): DomainValidation[SpecimenGroup] =
    addSpecimenGroup(specimenGroups, SpecimenGroupIdentityService.nextIdentity, version = 0L,
      cmd.name, cmd.description, cmd.units, cmd.anatomicalSourceType,
      cmd.preservationType, cmd.preservationTemperatureType, cmd.specimenType)

  def updateSpecimenGroup(
    specimenGroups: Map[SpecimenGroupId, SpecimenGroup],
    prevItem: SpecimenGroup,
    cmd: UpdateSpecimenGroupCmd): DomainValidation[SpecimenGroup] =
    specimenGroups.get(prevItem.id) match {
      case None =>
        DomainError("specimen group with id does not exists: %s" format prevItem.id).fail
      case Some(sg) =>
        addSpecimenGroup(specimenGroups, prevItem.id, prevItem.version + 1, cmd.name,
          cmd.description, cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
          cmd.preservationTemperatureType, cmd.specimenType)
    }

  def addCollectionEventType(
    collectionEventTypes: Map[CollectionEventTypeId, CollectionEventType],
    id: CollectionEventTypeId,
    version: Long,
    name: String,
    description: String,
    recurring: Boolean): DomainValidation[CollectionEventType] =
    collectionEventTypes.values.find(cet => cet.name.equals(name)) match {
      case Some(sg) =>
        DomainError("collection event type with name already exists: %s" format name).fail
      case None =>
        CollectionEventType(id, version = 0L, this.id, name, description, recurring).success
    }

  def addCollectionEventType(
    collectionEventTypes: Map[CollectionEventTypeId, CollectionEventType],
    cmd: AddCollectionEventTypeCmd): DomainValidation[CollectionEventType] =
    addCollectionEventType(collectionEventTypes, CollectionEventTypeIdentityService.nextIdentity,
      version = 0L, cmd.name, cmd.description, cmd.recurring)

  def updateCollectionEventType(
    collectionEventTypes: Map[CollectionEventTypeId, CollectionEventType],
    prevItem: CollectionEventType,
    cmd: UpdateCollectionEventTypeCmd): DomainValidation[CollectionEventType] =
    collectionEventTypes.get(prevItem.id) match {
      case None =>
        DomainError("collection event type does not exists: %s" format cmd.name).fail
      case Some(sg) =>
        addCollectionEventType(collectionEventTypes, prevItem.id, prevItem.version + 1,
          cmd.name, cmd.description, cmd.recurring)
    }

  def addCollectionEventAnnotationType(
    collectionEventAnnotationTypes: Map[AnnotationTypeId, StudyAnnotationType],
    id: AnnotationTypeId,
    version: Long,
    name: String,
    description: String,
    valueType: AnnotationValueType,
    maxValueCount: Int,
    options: Map[String, String]): DomainValidation[CollectionEventAnnotationType] =
    collectionEventAnnotationTypes.values.find(annot => annot.name.equals(name)) match {
      case Some(item) =>
        DomainError("collection event annotation type with name already exists: %s" format name).fail
      case None =>
        CollectionEventAnnotationType(id, version, this.id, name, description, valueType,
          maxValueCount, options).success
    }

  def addCollectionEventAnnotationType(
    collectionEventAnnotationTypes: Map[AnnotationTypeId, StudyAnnotationType],
    cmd: AddCollectionEventAnnotationTypeCmd): DomainValidation[CollectionEventAnnotationType] =
    addCollectionEventAnnotationType(collectionEventAnnotationTypes,
      AnnotationTypeIdentityService.nextIdentity, version = 0L,
      cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options)

  def updateCollectionEventAnnotationType(
    collectionEventAnnotationTypes: Map[AnnotationTypeId, StudyAnnotationType],
    prevItem: CollectionEventAnnotationType,
    cmd: UpdateCollectionEventAnnotationTypeCmd): DomainValidation[CollectionEventAnnotationType] =
    collectionEventAnnotationTypes.get(prevItem.id) match {
      case None =>
        DomainError("collection event annotation type does not exists: %s" format cmd.name).fail
      case Some(sg) =>
        addCollectionEventAnnotationType(collectionEventAnnotationTypes, prevItem.id,
          prevItem.version + 1, cmd.name, cmd.description, cmd.valueType,
          cmd.maxValueCount, cmd.options)
    }
}

case class EnabledStudy(id: StudyId, version: Long = -1, name: String, description: String)
  extends Study {

  def disable: DomainValidation[DisabledStudy] =
    DisabledStudy(id, version + 1, name, description).success
}

