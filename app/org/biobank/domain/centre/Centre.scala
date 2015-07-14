package org.biobank.domain.centre

import org.biobank.domain._
import org.biobank.domain.study.StudyId
import org.biobank.infrastructure.JsonUtils._

import play.api.libs.json._
import org.joda.time.DateTime
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

object Centre {

  implicit val centreWrites = new Writes[Centre] {
    def writes(centre: Centre) = Json.obj(
      "id"           -> centre.id,
      "version"      -> centre.version,
      "timeAdded"    -> centre.timeAdded,
      "timeModified" -> centre.timeModified,
      "name"         -> centre.name,
      "description"  -> centre.description,
      "status"       -> centre.getClass.getSimpleName
    )
  }

  def compareByName(a: Centre, b: Centre) = (a.name compareToIgnoreCase b.name) < 0

  def compareByStatus(a: Centre, b: Centre) = {
    val statusCompare = a.getClass.getSimpleName compare b.getClass.getSimpleName
    if (statusCompare == 0) {
      compareByName(a, b)
    } else {
      statusCompare < 0
    }
  }
}

trait CentreValidations {
  val NameMinLength = 2

  case object InvalidName extends ValidationKey
}


/**
  * This is the initial state for a centre.  In this state, only configuration changes are allowed. Collection
  * and processing of specimens cannot be recorded.
  *
  * This class has a private constructor and instances of this class can only be created using the
  * [[DisabledCentre.create]] method on the factory object.
  */
case class DisabledCentre(id:           CentreId,
                          version:      Long,
                          timeAdded:    DateTime,
                          timeModified: Option[DateTime],
                          name:         String,
                          description:  Option[String])
    extends Centre with CentreValidations {
  import org.biobank.domain.CommonValidations._

  /** Used to change the name. */
  def withName(name: String): DomainValidation[DisabledCentre] = {
    validateString(name, NameMinLength, InvalidName) fold (
      err => err.failure,
      c   => copy(version = version + 1, name = name).success
    )
  }

  /** Used to change the description. */
  def withDescription(description: Option[String]): DomainValidation[DisabledCentre] = {
    validateNonEmptyOption(description, InvalidDescription) fold (
      err => err.failure,
      c   => copy(version = version + 1, description = description).success
    )
  }

  /** Used to enable a centre after it has been configured, or had configuration changes made on it. */
  def enable(): DomainValidation[EnabledCentre] = {
    EnabledCentre(id           = this.id,
                  version      = this.version + 1,
                  timeAdded    = this.timeAdded,
                  timeModified = this.timeModified,
                  name         = this.name,
                  description  = this.description).success
  }
}

/**
  * Factory object used to create a centre.
  */
object DisabledCentre extends CentreValidations {
  import org.biobank.domain.CommonValidations._

  /**
    * The factory method to create a centre.
    *
    * Performs validation on fields.
    */
  def create(id:          CentreId,
             version:     Long,
             name:        String,
             description: Option[String]): DomainValidation[DisabledCentre] = {
    (validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validateString(name, NameMinLength, InvalidName) |@|
      validateNonEmptyOption(description, InvalidDescription)) {
        DisabledCentre(_, _, DateTime.now, None, _, _)
      }
  }
}

/**
  * When a centre is in this state, collection and processing of specimens can be recorded.
  *
  * This class has a private constructor and instances of this class can only be created using
  * the [[EnabledCentre.create]] method on the factory object.
  */
case class EnabledCentre(id:           CentreId,
                         version:      Long,
                         timeAdded:    DateTime,
                         timeModified: Option[DateTime],
                         name:         String,
                         description:  Option[String])
    extends Centre {

  def disable(): DomainValidation[DisabledCentre] = {
    DisabledCentre(id           = this.id,
                   version      = this.version + 1,
                   timeAdded    = this.timeAdded,
                   timeModified = this.timeModified,
                   name         = this.name,
                   description  = this.description).success
  }
}
