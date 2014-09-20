package org.biobank.domain.study

import org.biobank.infrastructure.{
  CollectionEventTypeSpecimenGroupData,
  CollectionEventTypeAnnotationTypeData}
import org.biobank.domain.{
  AnnotationTypeId,
  CommonValidations,
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
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._

/** A Study represents a collection of participants and specimens collected for a particular research
  * study. This is an aggregate root.
  *
  * A study can be in one of 3 states: diabled, enabled, or retired. These are represented by
  * the sub classes.
  *
  */
sealed trait Study
    extends ConcurrencySafeEntity[StudyId]
    with HasUniqueName
    with HasDescriptionOption {

  /** Contains the current state of the object, one of: Disabled, Enabled, or Retired. */
  val status: String

  override def toString =
    s"""|${this.getClass.getSimpleName}: {
        |  id: $id,
        |  version: $version,
        |  timeAdded: $timeAdded,
        |  timeModified: $timeModified,
        |  name: $name,
        |  description: $description
        |}""".stripMargin

}

object Study {

  implicit val studyWrites = new Writes[Study] {
    def writes(study: Study) = Json.obj(
      "id"             -> study.id,
      "version"        -> study.version,
      "timeAdded"      -> study.timeAdded,
      "timeModified" -> study.timeModified,
      "name"           -> study.name,
      "description"    -> study.description,
      "status"         -> study.status
    )
  }
}

/**
  * This is the initial state for a study.  In this state, only configuration changes are allowed.
  * Collection and processing of specimens cannot be recorded.
  *
  * This class has a private constructor and instances of this class can only be created using
  * the [[DisabledStudy.create]] method on the factory object.
  */
case class DisabledStudy(
  id: StudyId,
  version: Long,
  timeAdded: DateTime,
  timeModified: Option[DateTime],
  name: String,
  description: Option[String])
    extends Study {
  import CommonValidations._

  override val status: String = "Disabled"

  /** Used to change the name or the description. */
  def update(name: String, description: Option[String]): DomainValidation[DisabledStudy] = {
    (validateString(name, NameRequired) |@|
      validateNonEmptyOption(description, NonEmptyDescription)) {
      case(n, d) => copy(version = version + 1, name = n, description = d)
    }
  }

  /** Used to enable a study after it has been configured, or had configuration changes made on it. */
  def enable(
    specimenGroupCount: Int,
    collectionEventTypeCount: Int): DomainValidation[EnabledStudy] = {

    def checkSpecimenGroupCount =
      if (specimenGroupCount > 0) true.success else DomainError("no specimen groups").failNel

    def checkCollectionEventTypeCount =
      if (collectionEventTypeCount > 0) true.success else DomainError("no collection event types").failNel

    for {
      sgCount <- checkSpecimenGroupCount
      cetCount <- checkCollectionEventTypeCount
      enabledStudy <- EnabledStudy.create(this)
    } yield enabledStudy
  }

  /** When a study will no longer collect specimens from participants it can be retired. */
  def retire: DomainValidation[RetiredStudy] = {
    RetiredStudy.create(this)
  }

}

/**
  * Factory object used to create a study.
  */
object DisabledStudy {
  import CommonValidations._

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
      validateString(name, NameRequired) |@|
      validateNonEmptyOption(description, NonEmptyDescription)) {
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
case class EnabledStudy(
  id: StudyId,
  version: Long,
  timeAdded: DateTime,
  timeModified: Option[DateTime],
  name: String,
  description: Option[String])
  extends Study {

  override val status: String = "Enabled"

  def disable: DomainValidation[DisabledStudy] = {
    DisabledStudy.create(id, version, timeAdded, name, description)
  }
}

/**
  * Factory object used to enable a study.
  */
object EnabledStudy {
  import CommonValidations._

  /** A study must be in a disabled state before it can be enabled. */
  def create(study: DisabledStudy): DomainValidation[EnabledStudy] = {
    (validateId(study.id) |@|
      validateAndIncrementVersion(study.version) |@|
      validateString(study.name, NameRequired) |@|
      validateNonEmptyOption(study.description, NonEmptyDescription)) {
        EnabledStudy(_, _, study.timeAdded, None, _, _)
      }
  }
}

/**
 *  In this state the study cannot be modified and collection and processing of specimens is not allowed.
  *
  * This class has a private constructor and instances of this class can only be created using
  * the [[RetiredStudy.create]] method on the factory object.
 */
case class RetiredStudy(
  id: StudyId,
  version: Long,
  timeAdded: DateTime,
  timeModified: Option[DateTime],
  name: String,
  description: Option[String])
  extends Study {

  override val status: String = "Retired"

  def unretire: DomainValidation[DisabledStudy] = {
    DisabledStudy.create(id, version, timeAdded, name, description)
  }
}

/**
  * Factory object used to retire a study.
  */
object RetiredStudy {
  import CommonValidations._

  /** A study must be in a disabled state before it can be retired. */
  def create(study: DisabledStudy): DomainValidation[RetiredStudy] = {
    (validateId(study.id) |@|
      validateAndIncrementVersion(study.version) |@|
      validateString(study.name, NameRequired) |@|
      validateNonEmptyOption(study.description, NonEmptyDescription)) {
        RetiredStudy(_, _, study.timeAdded, None, _, _)
      }
  }
}
