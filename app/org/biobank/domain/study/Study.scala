package org.biobank.domain.study

import org.biobank.infrastructure.{
  CollectionEventTypeSpecimenGroupData,
  CollectionEventTypeAnnotationTypeData}
import org.biobank.domain.{
  AnnotationTypeId,
  ConcurrencySafeEntity,
  DomainError,
  DomainValidation,
  HasUniqueName,
  HasDescriptionOption }
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.AnnotationValueType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._
import org.biobank.domain.validation.StudyValidationHelper

import com.github.nscala_time.time.Imports._
import scalaz._
import scalaz.Scalaz._

/**
  * A Study represents a collection of participants and specimens collected for a particular
  * research study. This is an aggregate root.
  *
  * A study can be in one of 3 states: diabled, enabled, or retired. These are represented by
  * the sub classes.
  *
  */
sealed trait Study
    extends ConcurrencySafeEntity[StudyId]
    with HasUniqueName
    with HasDescriptionOption {

  /** Contains the current state of the object, one of: Disabled, Enalbed, or Retired. */
  val status: String

  override def toString =
    s"""|${this.getClass.getSimpleName}: {
        |  id: $id,
        |  version: $version,
        |  addedDate: $addedDate,
        |  lastUpdateDate: $lastUpdateDate,
        |  name: $name,
        |  description: $description
        |}""".stripMargin

}

/**
  * This is the initial state for a study.  In this state, only configuration changes are allowed.
  * Collection and processing of specimens cannot be recorded.
  *
  * This class has a private constructor and instances of this class can only be created using
  * the [[DisabledStudy.create]] method on the factory object.
  */
case class DisabledStudy private (
  id: StudyId,
  version: Long,
  addedDate: DateTime,
  lastUpdateDate: Option[DateTime],
  name: String,
  description: Option[String])
    extends Study {

  override val status: String = "Disabled"


  /** Used to change the name or the description. */
  def update(
    expectedVersion: Option[Long],
    dateTime: DateTime,
    name: String,
    description: Option[String]): DomainValidation[DisabledStudy] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      validatedStudy <- DisabledStudy.create(id, version, addedDate, name, description)
      updatedStudy <- validatedStudy.copy(
        lastUpdateDate = Some(dateTime)).success
    } yield updatedStudy
  }

  /** Used to enable a study after the study has been configured, or had configuration changes made on it. */
  def enable(
    expectedVersion: Option[Long],
    dateTime: DateTime,
    specimenGroupCount: Int,
    collectionEventTypeCount: Int): DomainValidation[EnabledStudy] = {

    def checkSpecimenGroupCount =
      if (specimenGroupCount > 0) true.success else DomainError("no specimen groups").failNel

    def checkCollectionEventTypeCount =
      if (collectionEventTypeCount > 0) true.success else DomainError("no collection event types").failNel

    for {
      validVersion <- requireVersion(expectedVersion)
      sgCount <- checkSpecimenGroupCount
      cetCount <- checkCollectionEventTypeCount
      enabledStudy <- EnabledStudy.create(this, dateTime)
    } yield enabledStudy
  }

  /** When a study will no longer collect specimens from participants it can be retired. */
  def retire(
    expectedVersion: Option[Long],
    dateTime: DateTime): DomainValidation[RetiredStudy] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      retiredStudy <- RetiredStudy.create(this, dateTime)
    } yield retiredStudy
  }

  /**
    * Adds a [[ParticipantAnnotationType]] to this study.
    */
  def addParticipantAnnotationType(
    id: AnnotationTypeId,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]],
    required: Boolean): DomainValidation[ParticipantAnnotationType] = {
    ParticipantAnnotationType.create(this.id, id, version, name, description,
      valueType, maxValueCount, options, required)
  }

  /**
    * Adds a [[SpecimenGroup]] to this study.
    */
  def addSpecimenGroup(
    id: SpecimenGroupId,
    version: Long = -1,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType): DomainValidation[SpecimenGroup] =  {
    SpecimenGroup.create(this.id, id, version, name, description, units,
    anatomicalSourceType, preservationType, preservationTemperatureType, specimenType)
  }

  def addColletionEventType(
    id: CollectionEventTypeId,
    version: Long = -1,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: List[CollectionEventTypeSpecimenGroupData],
    annotationTypeData: List[CollectionEventTypeAnnotationTypeData]): DomainValidation[CollectionEventType] =  {
    CollectionEventType.create(this.id, id, version, dateTime, name, description, recurring,
      specimenGroupData, annotationTypeData)
  }

  def addColletionEventAnnotationType(
    id: AnnotationTypeId,
    version: Long = -1,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]]): DomainValidation[CollectionEventAnnotationType] =  {
    CollectionEventAnnotationType.create(
      this.id, id, version, dateTime, name, description, valueType, maxValueCount, options)
  }

  def addSpecimenLinkAnnotationType(
    id: AnnotationTypeId,
    version: Long = -1,
    dateTime: DateTime,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]]): DomainValidation[SpecimenLinkAnnotationType] =  {
    SpecimenLinkAnnotationType.create(this.id, id, version, name, description, valueType,
      maxValueCount, options)
  }

}

/**
  * Factory object used to create a study.
  */
object DisabledStudy extends StudyValidationHelper {

  /**
    * The factory method to create a study.
    *
    * Performs validation on fields.
    */
  def create(
    id: StudyId,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String]): DomainValidation[DisabledStudy] = {
    (validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validateNonEmpty(name, "name is null or empty") |@|
      validateNonEmptyOption(description, "description is null or empty")) {
        DisabledStudy(_, _, dateTime, None, _, _)
      }
  }
}

/**
  * When a study is in this state, collection and processing of specimens can be recorded.
  *
  * This class has a private constructor and instances of this class can only be created using
  * the [[EnabledStudy.create]] method on the factory object.
  */
case class EnabledStudy private (
  id: StudyId,
  version: Long,
  addedDate: DateTime,
  lastUpdateDate: Option[DateTime],
  name: String,
  description: Option[String])
  extends Study {

  override val status: String = "Enabled"

  def disable(
    expectedVersion: Option[Long],
    dateTime: DateTime): DomainValidation[DisabledStudy] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      validatedStudy <- DisabledStudy.create(id, version, addedDate, name, description)
      disabledStudy <- validatedStudy.copy(
        lastUpdateDate = Some(dateTime)).success
    } yield disabledStudy
  }
}

/**
  * Factory object used to enable a study.
  */
object EnabledStudy extends StudyValidationHelper {

  /** A study must be in a disabled state before it can be enabled. */
  def create(
    study: DisabledStudy,
    dateTime: DateTime): DomainValidation[EnabledStudy] = {
    (validateId(study.id) |@|
      validateAndIncrementVersion(study.version) |@|
      validateNonEmpty(study.name, "name is null or empty") |@|
      validateNonEmptyOption(study.description, "description is null or empty")) {
        EnabledStudy(_, _, study.addedDate, Some(dateTime), _, _)
      }
  }
}

/**
 *  In this state the study cannot be modified and collection and processing of specimens is not allowed.
  *
  * This class has a private constructor and instances of this class can only be created using
  * the [[RetiredStudy.create]] method on the factory object.
 */
case class RetiredStudy private (
  id: StudyId,
  version: Long,
  addedDate: DateTime,
  lastUpdateDate: Option[DateTime],
  name: String,
  description: Option[String])
  extends Study {

  override val status: String = "Retired"

  def unretire(
    expectedVersion: Option[Long],
    dateTime: DateTime): DomainValidation[DisabledStudy] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      validatedStudy <- DisabledStudy.create(id, version, addedDate, name, description)
      disabledStudy <- validatedStudy.copy(
        lastUpdateDate = Some(dateTime)).success
    } yield disabledStudy
  }
}

/**
  * Factory object used to retire a study.
  */
object RetiredStudy extends StudyValidationHelper {

  /** A study must be in a disabled state before it can be retired. */
  def create(
    study: DisabledStudy,
    dateTime: DateTime): DomainValidation[RetiredStudy] = {
    (validateId(study.id) |@|
      validateAndIncrementVersion(study.version) |@|
      validateNonEmpty(study.name, "name is null or empty") |@|
      validateNonEmptyOption(study.description, "description is null or empty")) {
        RetiredStudy(_, _, study.addedDate, Some(dateTime), _, _)
      }
  }
}
