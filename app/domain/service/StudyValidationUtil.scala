package domain.service

import infrastructure._
import domain._
import domain.study._
import scalaz._
import Scalaz._

object StudyValidationUtil {

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