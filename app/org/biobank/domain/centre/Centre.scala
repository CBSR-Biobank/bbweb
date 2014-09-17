package org.biobank.domain.centre

import org.biobank.domain.{
  Comment,
  ConcurrencySafeEntity,
  DomainError,
  DomainValidation,
  HasUniqueName,
  HasDescriptionOption,
  Location
}
import org.biobank.domain.study.StudyId
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import play.api.libs.functional.syntax._

import org.joda.time.DateTime

import scalaz._
import scalaz.Scalaz._

/**
  * A Centre can be one or a combination of the following:
  *
  * - Collection Centre: where specimens are collected from patients (e.g. a clinic).
  *
  * - Processing Centre: where collected specimens are processed.
  *
  * - Storage Centre: where collected and processed specimens are stored. Usually, a storage centre is also a
  *   processing centre.
  *
  * - Request Centre: where collected and processed specimens are sent for analysis. Usually the specimens are
  *   stored first at a storage centre for a period of time.
  *
  */
sealed trait Centre
    extends ConcurrencySafeEntity[CentreId]
    with HasUniqueName
    with HasDescriptionOption {

  /** @param status Contains the current state of the object, one of: Disabled, Enabled. */
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

object Centre {

  implicit val centreWrites = new Writes[Centre] {
    def writes(centre: Centre) = Json.obj(
      "id"             -> centre.id,
      "version"        -> centre.version,
      "addedDate"      -> centre.addedDate,
      "lastUpdateDate" -> centre.lastUpdateDate,
      "name"           -> centre.name,
      "description"    -> centre.description,
      "status"         -> centre.status
    )
  }

}

/**
  * This is the initial state for a centre.  In this state, only configuration changes are allowed. Collection
  * and processing of specimens cannot be recorded.
  *
  * This class has a private constructor and instances of this class can only be created using the
  * [[DisabledCentre.create]] method on the factory object.
  */
case class DisabledCentre(
  id: CentreId,
  version: Long,
  addedDate: DateTime,
  lastUpdateDate: Option[DateTime],
  name: String,
  description: Option[String])
    extends Centre {

  override val status: String = "Disabled"

  /** Used to change the name or the description. */
  def update(
    name: String,
    description: Option[String]): DomainValidation[DisabledCentre] = {
    DisabledCentre.create(this.id, this.version, this.addedDate, name, description)
  }

  /** Used to enable a centre after it has been configured, or had configuration changes made on it. */
  def enable: DomainValidation[EnabledCentre] = {
    EnabledCentre.create(this)
  }
}

/**
  * Factory object used to create a centre.
  */
object DisabledCentre {
  import org.biobank.domain.CommonValidations._

  /**
    * The factory method to create a centre.
    *
    * Performs validation on fields.
    */
  def create(
    id: CentreId,
    version: Long,
    dateTime: DateTime,
    name: String,
    description: Option[String]): DomainValidation[DisabledCentre] = {
    (validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validateString(name, NameRequired) |@|
      validateNonEmptyOption(description, NonEmptyDescription)) {
        DisabledCentre(_, _, dateTime, None, _, _)
      }
  }
}

/**
  * When a centre is in this state, collection and processing of specimens can be recorded.
  *
  * This class has a private constructor and instances of this class can only be created using
  * the [[EnabledCentre.create]] method on the factory object.
  */
case class EnabledCentre(
  id: CentreId,
  version: Long,
  addedDate: DateTime,
  lastUpdateDate: Option[DateTime],
  name: String,
  description: Option[String])
  extends Centre {

  override val status: String = "Enabled"

  def disable: DomainValidation[DisabledCentre] = {
    DisabledCentre.create(this.id, this.version, this.addedDate, name, description)
  }
}


/**
  * Factory object used to enable a centre.
  */
object EnabledCentre {
  import org.biobank.domain.CommonValidations._

  /** A centre must be in a disabled state before it can be enabled. */
  def create(centre: DisabledCentre): DomainValidation[EnabledCentre] = {
    (validateId(centre.id) |@|
      validateAndIncrementVersion(centre.version) |@|
      validateString(centre.name, NameRequired) |@|
      validateNonEmptyOption(centre.description, NonEmptyDescription)) {
        EnabledCentre(_, _, centre.addedDate, None, _, _)
      }
  }
}
