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

abstract class Study extends ConcurrencySafeEntity[StudyId] {
  def name: String
  def description: String

  override def toString =
    "{ id:%s, version: %d, name:%s, description:%s }" format (id, version, name, description)

  val status: String = "invalid"

  def validateSpecimenGroupId(
    specimenGroupRepository: ReadRepository[SpecimenGroupId, SpecimenGroup],
    specimenGroupId: SpecimenGroupId): DomainValidation[SpecimenGroup] = {
    specimenGroupRepository.getByKey(specimenGroupId) match {
      case Success(sg) =>
        if (this.id.equals(sg.studyId)) sg.success
        else DomainError("specimen group does not belong to study: %s" format specimenGroupId).fail
      case Failure(x) =>
        DomainError("specimen group does not exist: %s" format specimenGroupId).fail
    }
  }

  def validateSpecimenGroupId(
    specimenGroupRepository: ReadRepository[SpecimenGroupId, SpecimenGroup],
    specimenGroupId: String): DomainValidation[SpecimenGroup] =
    validateSpecimenGroupId(specimenGroupRepository, new SpecimenGroupId(specimenGroupId))

  def validateCollectionEventTypeId(
    collectionEventTypeRepository: ReadRepository[CollectionEventTypeId, CollectionEventType],
    collectionEventTypeId: CollectionEventTypeId): DomainValidation[CollectionEventType] = {
    collectionEventTypeRepository.getByKey(collectionEventTypeId) match {
      case Success(cet) =>
        if (this.id.equals(cet.studyId)) cet.success
        else DomainError("collection event type does not belong to study: %s" format collectionEventTypeId).fail
      case Failure(x) =>
        DomainError("collection event type does not exist: %s" format collectionEventTypeId).fail
    }
  }

  def validateCollectionEventTypeId(
    collectionEventTypeRepository: ReadRepository[CollectionEventTypeId, CollectionEventType],
    collectionEventTypeId: String): DomainValidation[CollectionEventType] =
    validateCollectionEventTypeId(collectionEventTypeRepository,
      new CollectionEventTypeId(collectionEventTypeId))

  /**
   * Validates that the CollectionEventAnnotationType with id {@link annotationTypeId} exists
   * and that it belongs to {@link study}.
   */
  def validateCollectionEventAnnotationTypeId(
    annotationTypeRepo: ReadRepository[AnnotationTypeId, StudyAnnotationType],
    annotationTypeId: AnnotationTypeId): DomainValidation[CollectionEventAnnotationType] = {
    annotationTypeRepo.getByKey(annotationTypeId) match {
      case Success(annot) =>
        if (this.id.equals(annot.studyId)) {
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

  def validateCollectionEventAnnotationTypeId(
    annotationTypeRepo: ReadRepository[AnnotationTypeId, StudyAnnotationType],
    annotationTypeId: String): DomainValidation[CollectionEventAnnotationType] =
    validateCollectionEventAnnotationTypeId(annotationTypeRepo,
      new AnnotationTypeId(annotationTypeId))
}

object Study {

  def validateStudy(
    studyRepository: ReadRepository[StudyId, Study],
    studyId: StudyId)(f: DisabledStudy => DomainValidation[_]): DomainValidation[_] =
    studyRepository.getByKey(studyId) match {
      case Failure(msglist) => noSuchStudy(studyId).fail
      case Success(study) => study match {
        case study: EnabledStudy => notDisabledError(study.name).fail
        case study: DisabledStudy => f(study)
      }
    }

  def validateStudy(
    studyRepository: ReadRepository[StudyId, Study],
    studyId: String)(f: DisabledStudy => DomainValidation[_]): DomainValidation[_] =
    validateStudy(studyRepository, new StudyId(studyId))(f)

  def add(name: String, description: String): DomainValidation[DisabledStudy] =
    DisabledStudy(StudyIdentityService.nextIdentity, version = 0L, name, description).success

  def noSuchStudy(studyId: StudyId) =
    DomainError("no study with id: %s" format studyId)

  def notDisabledError(name: String) =
    DomainError("study is not disabled: %s" format name)

  def notEnabledError(name: String) =
    DomainError("study is not enabled: %s" format name)
}
