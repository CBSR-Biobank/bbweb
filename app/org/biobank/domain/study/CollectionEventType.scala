package org.biobank.domain.study

import org.biobank.infrastructure.{
  CollectionEventTypeSpecimenGroup,
  CollectionEventTypeAnnotationType}
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

/**
  * Defines a classification name, unique to the Study, to a participant visit.
  *
  * A participant visit is a record of when specimens were collected from a
  * [[org.biobank.domain.participant.Participant]] at a collection [[Centre]]. Each collection event type is
  * assigned one or more [[SpecimenGroup]]s to specify the [[SpecimenType]]s that are collected.
  *
  * A study must have at least one collection event type defined in order to record collected specimens.
  *
  * @param recurring Set to true when the collection event type occurs more than once during the
  *        lifetime of the study. False otherwise.

  * @param specimenGroupData One or more [[SpecimenGroup]]s that need to be collected with this
  *        type of collection event. See [[CollectionEventTypeSpecimenGroup]].

  * @param annotationTypeData The [[AnnotationType]]s for a collection event type.
  *
  */
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
    with HasDescriptionOption
    with HasStudyId {

  override def toString: String =
    s"""|CollectionEventType:{
        |  studyId: $studyId,
        |  id: $id,
        |  version: $version,
        |  name: $name,
        |  description: $description,
        |  recurring: $recurring,
        |  specimenGroupData: { $specimenGroupData },
        |  annotationTypeData: { $annotationTypeData }
        |}""".stripMargin
}

object CollectionEventType extends StudyValidationHelper {

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


  protected def validateId(id: CollectionEventTypeId): Validation[String, CollectionEventTypeId] = {
    validateStringId(id.toString, "collection event type id is null or empty") match {
      case Success(idString) => id.success
      case Failure(err) => err.fail
    }
  }

  /**
    *  Validates each item in the set and returns all failures.
    */
  protected def validateSpecimenGroupData(
    specimenGroupData: List[CollectionEventTypeSpecimenGroup]): DomainValidation[List[CollectionEventTypeSpecimenGroup]] = {

    def validateSpecimenGroupItem(
      specimenGroupItem: CollectionEventTypeSpecimenGroup): DomainValidation[CollectionEventTypeSpecimenGroup] = {
      (validateStringId(specimenGroupItem.specimenGroupId, "specimen group id is null or empty").toValidationNel |@|
	validatePositiveNumber(specimenGroupItem.maxCount, "max count is not a positive number").toValidationNel |@|
	validatePositiveNumberOption(specimenGroupItem.amount, "amount not is a positive number").toValidationNel) {
        CollectionEventTypeSpecimenGroup(_, _, _)
      }
    }

    specimenGroupData.map(validateSpecimenGroupItem).sequenceU
  }

  /**
    *  Validates each item in the set and returns all failures.
    */
  protected def validateAnnotationTypeData(
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
}

