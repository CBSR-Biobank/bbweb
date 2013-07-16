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
   * @parm  specimenGroupId the ID of the instance to check.
   * @return Success if the Specimen Group instance is associated with the study.
   */
  def validateSpecimenGroupId(
    study: DisabledStudy,
    specimenGroupId: SpecimenGroupId): DomainValidation[SpecimenGroup] = {
    SpecimenGroupRepository.getByKey(specimenGroupId) match {
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
   * @parm  specimenGroupId the ID of the instance to check.
   * @return Success if the Specimen Group instance is associated with the study.
   */
  def validateSpecimenGroupId(
    study: DisabledStudy,
    specimenGroupId: String): DomainValidation[SpecimenGroup] =
    validateSpecimenGroupId(study, new SpecimenGroupId(specimenGroupId))

  /**
   * Returns Success if the Specimen Groups with {@link specimenGroupIds} exists and are
   * associated with the {@link study}.
   *
   * @param study the study instance
   * @parm  specimenGroupIds set of IDs to check.
   * @return Success if the Specimen Group instance is associated with the study.
   */
  def validateSpecimenGroupIds(
    study: DisabledStudy,
    specimenGroupIds: Set[String]): DomainValidation[Boolean] = {
    specimenGroupIds.forall {
      id => validateSpecimenGroupId(study, id).isSuccess
    }
    true.success
  }

  def validateCollectionEventTypeNameNotPresent(
    study: DisabledStudy,
    name: String,
    id: Option[CollectionEventTypeId] = None): DomainValidation[Boolean] =
    CollectionEventTypeRepository.getValues.exists {
      item => item.studyId.equals(study.id) && !item.id.equals(id) && item.name.equals(name)
    } match {
      case true =>
        DomainError("collection event type with name already exists: %s" format name).fail
      case false =>
        true.success
    }

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
    collectionEventTypeId: CollectionEventTypeId): DomainValidation[CollectionEventType] = {
    CollectionEventTypeRepository.getByKey(collectionEventTypeId) match {
      case Success(cet) =>
        if (study.id.equals(cet.studyId)) cet.success
        else DomainError("collection event type does not belong to study: %s" format collectionEventTypeId).fail
      case Failure(x) =>
        DomainError("collection event type does not exist: %s" format collectionEventTypeId).fail
    }
  }

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
    collectionEventTypeId: String): DomainValidation[CollectionEventType] =
    validateCollectionEventTypeId(study, new CollectionEventTypeId(collectionEventTypeId))

  /**
   * Validates that the CollectionEventAnnotationType with id {@link annotationTypeId} exists
   * and that it belongs to {@link study}.
   *
   * @param study the study instance
   * @parm  annotationTypeId the ID of the instance to check.
   * @return Success if the Collection Event Annotation Type instance is associated with the study.
   */
  def validateCollectionEventAnnotationTypeId(
    study: DisabledStudy,
    annotationTypeId: AnnotationTypeId): DomainValidation[CollectionEventAnnotationType] = {
    CollectionEventAnnotationTypeRepository.getByKey(annotationTypeId) match {
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
   * @parm  annotationTypeId the ID of the instance to check.
   * @return Success if the Collection Event Annotation Type instance is associated with the study.
   */
  def validateCollectionEventAnnotationTypeId(
    study: DisabledStudy,
    annotationTypeId: String): DomainValidation[CollectionEventAnnotationType] =
    validateCollectionEventAnnotationTypeId(study, new AnnotationTypeId(annotationTypeId))

  /**
   * Returns Success if the CollectionEventAnnotationTypes with {@link annotationTypeIds} exist
   * and are associated with the {@link study}.
   *
   * @param study the study instance
   * @parm  specimenGroupIds set of IDs to check.
   * @return Success if the Specimen Group instance is associated with the study.
   */
  def validateCollectionEventAnnotationTypeIds(
    study: DisabledStudy,
    annotationTypeIds: Set[String]): DomainValidation[Boolean] = {
    annotationTypeIds.forall {
      id => validateCollectionEventAnnotationTypeId(study, id).isSuccess
    }
    true.success
  }

}