package org.biobank.domain.study

import org.biobank.infrastructure._
import org.biobank.domain.{
  AnnotationTypeId,
  ConcurrencySafeEntity,
  DomainValidation,
  HasName,
  HasDescriptionOption
}
import org.biobank.domain.validation.StudyValidationHelper

import scalaz._
import scalaz.Scalaz._

case class CollectionEventType private (
  studyId: StudyId,
  id: CollectionEventTypeId,
  version: Long,
  name: String,
  description: Option[String],
  recurring: Boolean,
  specimenGroupData: List[CollectionEventTypeSpecimenGroup],
  annotationTypeData: List[CollectionEventTypeAnnotationType])
  extends ConcurrencySafeEntity[CollectionEventTypeId]
  with HasName
  with HasDescriptionOption {

  override def toString: String =
    s"""|CollectionEventType:{
        |  id: %s,
        |  version: %d,
        |  studyId: %s,
        |  name: %s,
        |  description: %s,
        |  recurring: %s,
        |  specimenGroupData: { %s },
        |  annotationTypeData: { %s }
        |}""".stripMargin
}

object CollectionEventType extends StudyValidationHelper {

    def validateId(id: CollectionEventTypeId): Validation[String, CollectionEventTypeId] = {
    validateStringId(id.toString, "collection event type id is null or empty") match {
      case Success(idString) => id.success
      case Failure(err) => err.fail
    }
  }

  /**
    *  Validates each item in the set and returns all failures.
    */
  def validateSpecimenGroupData(
    specimenGroupData: List[CollectionEventTypeSpecimenGroup]): DomainValidation[List[CollectionEventTypeSpecimenGroup]] = {

    def validateSpecimenGroupItem(
      specimenGroupItem: CollectionEventTypeSpecimenGroup): DomainValidation[CollectionEventTypeSpecimenGroup] = {
      (validateStringId(specimenGroupItem.specimenGroupId, "specimen group id is null or empty").toValidationNel |@|
	validatePositiveNumber(specimenGroupItem.maxCount, "max count is not a positive number").toValidationNel |@|
	validatePositiveNumber(specimenGroupItem.amount, "amount not is a positive number").toValidationNel) {
        CollectionEventTypeSpecimenGroup(_, _, _)
      }
    }

    specimenGroupData.map(validateSpecimenGroupItem).sequenceU
  }

  /**
    *  Validates each item in the set and returns all failures.
    */
  def validateAnnotationTypeData(
    annotationTypeData: List[CollectionEventTypeAnnotationType]): DomainValidation[List[CollectionEventTypeAnnotationType]] = {

    def validateAnnotationTypeItem(
      annotationTypeItem: CollectionEventTypeAnnotationType): DomainValidation[CollectionEventTypeAnnotationType] = {
      validateStringId(
	annotationTypeItem.annotationTypeId,
	"annotation type id is null or empty") match {
	case Success(id) => CollectionEventTypeAnnotationType(id, annotationTypeItem.required).success
	case Failure(err) => err.failNel
      }
    }

    annotationTypeData.map(validateAnnotationTypeItem).sequenceU
  }

  def create(
    studyId: StudyId,
    id: CollectionEventTypeId,
    version: Long = -1,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: List[CollectionEventTypeSpecimenGroup],
    annotationTypeData: List[CollectionEventTypeAnnotationType]): DomainValidation[CollectionEventType] = {
    (validateId(studyId).toValidationNel |@|
      validateId(id).toValidationNel |@|
      validateAndIncrementVersion(version).toValidationNel |@|
      validateNonEmpty(name, "name is null or empty").toValidationNel |@|
      validateNonEmptyOption(description, "description is null or empty").toValidationNel |@|
      validateSpecimenGroupData(specimenGroupData) |@|
      validateAnnotationTypeData(annotationTypeData)) {
      CollectionEventType(_, _, _, _, _, recurring, _, _)
    }
  }

}

