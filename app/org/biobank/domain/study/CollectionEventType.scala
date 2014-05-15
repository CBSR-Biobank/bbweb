package org.biobank.domain.study

import org.biobank.infrastructure.{
  CollectionEventTypeSpecimenGroupData,
  CollectionEventTypeAnnotationTypeData}
import org.biobank.domain.{
  AnnotationTypeId,
  ConcurrencySafeEntity,
  DomainValidation,
  HasName,
  HasDescriptionOption
}

import org.biobank.domain.validation.StudyAnnotationTypeValidationHelper
import com.github.nscala_time.time.Imports._
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
  *        type of collection event. See [[CollectionEventTypeSpecimenGroupData]].

  * @param annotationTypeData The [[AnnotationType]]s for a collection event type.
  *
  */
case class CollectionEventType private (
  studyId: StudyId,
  id: CollectionEventTypeId,
  version: Long,
  addedDate: DateTime,
  lastUpdateDate: Option[DateTime],
  name: String,
  description: Option[String],
  recurring: Boolean,
  specimenGroupData: List[CollectionEventTypeSpecimenGroupData],
  annotationTypeData: List[CollectionEventTypeAnnotationTypeData])
    extends ConcurrencySafeEntity[CollectionEventTypeId]
    with HasName
    with HasDescriptionOption
    with HasStudyId {

  def update(
    expectedVersion: Option[Long],
    dateTime: DateTime,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: List[CollectionEventTypeSpecimenGroupData],
    annotationTypeData: List[CollectionEventTypeAnnotationTypeData]): DomainValidation[CollectionEventType] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      validatedCeventType <- CollectionEventType.create(
        studyId, id, version, dateTime, name, description, recurring, specimenGroupData, annotationTypeData)
      updatedCeventType <- validatedCeventType.copy(lastUpdateDate = Some(dateTime)).success
    } yield updatedCeventType
  }

  override def toString: String =
    s"""|CollectionEventType:{
        |  studyId: $studyId,
        |  id: $id,
        |  version: $version,
        |  addedDate: $addedDate,
        |  lastUpdateDate: $lastUpdateDate,
        |  name: $name,
        |  description: $description,
        |  recurring: $recurring,
        |  specimenGroupData: { $specimenGroupData },
        |  annotationTypeData: { $annotationTypeData }
        |}""".stripMargin

}

object CollectionEventType extends StudyAnnotationTypeValidationHelper {

  def create(
    studyId: StudyId,
    id: CollectionEventTypeId,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: List[CollectionEventTypeSpecimenGroupData],
    annotationTypeData: List[CollectionEventTypeAnnotationTypeData]): DomainValidation[CollectionEventType] = {
    (validateId(studyId) |@|
      validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validateNonEmpty(name, "name is null or empty") |@|
      validateNonEmptyOption(description, "description is null or empty") |@|
      validateSpecimenGroupData(specimenGroupData) |@|
      validateAnnotationTypeData(annotationTypeData)) {
      CollectionEventType(_, _, _, dateTime, None, _, _, recurring, _, _)
    }
  }

  /**
    *  Validates each item in the set and returns all failures.
    */
  protected def validateSpecimenGroupData(
    specimenGroupData: List[CollectionEventTypeSpecimenGroupData]): ValidationNel[String, List[CollectionEventTypeSpecimenGroupData]] = {

    def validateSpecimenGroupItem(
      specimenGroupItem: CollectionEventTypeSpecimenGroupData): DomainValidation[CollectionEventTypeSpecimenGroupData] = {
      (validateStringId(specimenGroupItem.specimenGroupId, "specimen group id is null or empty") |@|
        validatePositiveNumber(specimenGroupItem.maxCount, "max count is not a positive number") |@|
        validatePositiveNumberOption(specimenGroupItem.amount, "amount not is a positive number")) {
        CollectionEventTypeSpecimenGroupData(_, _, _)
      }
    }

    specimenGroupData.map(validateSpecimenGroupItem).sequenceU
  }

}

