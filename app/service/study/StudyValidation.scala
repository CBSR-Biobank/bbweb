package service.study

import infrastructure._
import service._
import domain._
import domain.study._

import scalaz._
import Scalaz._

object StudyValidation {

  /**
   * Returns Success if the Specimen Group with id {@link specimenGroupId} exists and is associated
   * with the {@link study}.
   *
   * @param study the study instance
   * @param specimenGroupRepository the repository holding all the Specimen Groups
   * @parm  specimenGroupId the ID of the instance to check.
   * @return Success if the Specimen Group instance is associated with the study.
   */
  def validateSpecimenGroupId(
    study: DisabledStudy,
    specimenGroupRepository: SpecimenGroupReadRepository,
    specimenGroupId: SpecimenGroupId): DomainValidation[SpecimenGroup] = {
    specimenGroupRepository.getByKey(specimenGroupId) match {
      case Success(sg) =>
        if (study.id.equals(sg.studyId)) sg.success
        else DomainError("specimen group does not belong to study: %s" format specimenGroupId).fail
      case Failure(x) =>
        DomainError("specimen group does not exist: %s" format specimenGroupId).fail
    }
  }

  /**
   * Returns Success if the Specimen Group with id {@link specimenGroupId} exists and is associated
   * with the {@link study}.
   *
   * @param study the study instance
   * @param specimenGroupRepository the repository holding all the Specimen Groups
   * @parm  specimenGroupId the ID of the instance to check.
   * @return Success if the Specimen Group instance is associated with the study.
   */
  def validateSpecimenGroupId(
    study: DisabledStudy,
    specimenGroupRepository: SpecimenGroupReadRepository,
    specimenGroupId: String): DomainValidation[SpecimenGroup] =
    validateSpecimenGroupId(study, specimenGroupRepository, new SpecimenGroupId(specimenGroupId))

  /**
   * Returns Success if the Collection Event Type with id {@link collectionEventTypeId} exists and
   * is associated with the {@link study}.
   *
   * @param study the study instance
   * @param collectionEventTypeRepository the repository holding all the Collection Event Types
   * @parm  collectionEventTypeId the ID of the instance to check.
   * @return Success if the Collection Event Type instance is associated with the study.
   */
  def validateCollectionEventTypeId(
    study: DisabledStudy,
    collectionEventTypeRepository: CollectionEventTypeReadRepository,
    collectionEventTypeId: CollectionEventTypeId): DomainValidation[CollectionEventType] = {
    collectionEventTypeRepository.getByKey(collectionEventTypeId) match {
      case Success(cet) =>
        if (study.id.equals(cet.studyId)) cet.success
        else DomainError("collection event type does not belong to study: %s" format collectionEventTypeId).fail
      case Failure(x) =>
        DomainError("collection event type does not exist: %s" format collectionEventTypeId).fail
    }
  }

  /**
   * Returns Success if the Specimen Group with id {@link specimenGroupId} exists and is associated
   * with the {@link study}.
   *
   * @param study the study instance
   * @param specimenGroupRepository the repository holding all the Specimen Groups
   * @parm  specimenGroupId the ID of the instance to check.
   * @return Success if the Specimen Group instance is associated with the study.
   */
  def validateSpecimenGroupIds(
    study: DisabledStudy,
    specimenGroupRepository: SpecimenGroupReadRepository,
    specimenGroupIds: Set[String]): DomainValidation[SpecimenGroup] =
    specimenGroupIds.foreach(
      x => validateSpecimenGroupId(study, specimenGroupRepository, x))

  /**
   * Returns Success if the Collection Event Type with id {@link collectionEventTypeId} exists and
   * is associated with the {@link study}.
   *
   * @param study the study instance
   * @param collectionEventTypeRepository the repository holding all the Collection Event Types
   * @parm  collectionEventTypeId the ID of the instance to check.
   * @return Success if the Collection Event Type instance is associated with the study.
   */
  def validateCollectionEventTypeId(
    study: DisabledStudy,
    collectionEventTypeRepository: CollectionEventTypeReadRepository,
    collectionEventTypeId: String): DomainValidation[CollectionEventType] =
    validateCollectionEventTypeId(
      study, collectionEventTypeRepository, new CollectionEventTypeId(collectionEventTypeId))

  /**
   * Validates that the CollectionEventAnnotationType with id {@link annotationTypeId} exists
   * and that it belongs to {@link study}.
   *
   * @param study the study instance
   * @param annotationTypeRepo the repository holding all the Collection Event Annotation Types
   * @parm  annotationTypeId the ID of the instance to check.
   * @return Success if the Collection Event Annotation Type instance is associated with the study.
   */
  def validateCollectionEventAnnotationTypeId(
    study: DisabledStudy,
    annotationTypeRepo: CollectionEventAnnotationTypeReadRepository,
    annotationTypeId: AnnotationTypeId): DomainValidation[CollectionEventAnnotationType] = {
    annotationTypeRepo.getByKey(annotationTypeId) match {
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

  /**
   * Validates that the CollectionEventAnnotationType with id {@link annotationTypeId} exists
   * and that it belongs to {@link study}.
   *
   * @param study the study instance
   * @param annotationTypeRepo the repository holding all the Collection Event Annotation Types
   * @parm  annotationTypeId the ID of the instance to check.
   * @return Success if the Collection Event Annotation Type instance is associated with the study.
   */
  def validateCollectionEventAnnotationTypeId(
    study: DisabledStudy,
    annotationTypeRepo: CollectionEventAnnotationTypeReadRepository,
    annotationTypeId: String): DomainValidation[CollectionEventAnnotationType] =
    validateCollectionEventAnnotationTypeId(
      study, annotationTypeRepo, new AnnotationTypeId(annotationTypeId))

}