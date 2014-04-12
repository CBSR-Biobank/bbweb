package org.biobank.domain.study

import org.biobank.domain._
import org.biobank.domain.validator._

import scalaz._
import scalaz.Scalaz._

sealed trait Study
    extends ConcurrencySafeEntity[StudyId]
    with HasName
    with HasDescriptionOption {
  val name: String
  val description: Option[String]

  override def toString =
    s"""
       | id: $di,
       | version: $version,
       | name: $name,
       | description: $description
       |}
    """.stripMargin
}

case class DisabledStudy private (
  id: StudyId,
  version: Long = -1,
  name: String,
  description: Option[String])
  extends Study {

}

object DisabledStudy {

  def create(
  id: StudyId,
  version: Long = -1,
  name: String,
  description: Option[String]): DomainValidation[DisabledStudy] = {
    (validateId(id).toValidationNel |@|
      validateAndIncrementVersion(version).toValidationNel |@|
      validateNonEmpty("name", name).toValidationNel |@|
      validateNonEmptyOption("description", salt).toValidationNel {
        DisabledStudy(_, _, _, _)
      }
  }

  def enable(study: DiabledStudy): DomainValidation[EnabledStudy] = {
    EnabledStudy.create(this)
  }

  def retire(study: DisabledStudy): DomainValidation[RetiredStudy] = {
    RetiredStudy.create(this)
  }
}


case class EnabledStudy private (
  id: StudyId,
  version: Long = -1,
  name: String,
  description: Option[String])
  extends Study {

  def disable: DomainValidation[DisabledStudy] =
    DisabledStudy(id, version + 1, name, description).success
}

object EnabledStudy {

  def create(study: DisabledStudy): DomainValidation[EnabledStudy] = {
    (validateId(id).toValidationNel |@|
      validateAndIncrementVersion(version).toValidationNel |@|
      validateNonEmpty("name", name).toValidationNel |@|
      validateNonEmptyOption("description", salt).toValidationNel {
        EnabledStudy(_, _, _, _)
      }
  }
}

case class RetiredStudy private (
  id: StudyId,
  version: Long = -1,
  name: String,
  description: Option[String])
  extends Study {

  def unretire: DomainValidation[RetiredStudy]: DomainValidation[DisabledStudy] = {
    DisabledStudy.create(this)
  }
}

object RetiredStudy {

  def create(study: DiabledStudy): DomainValidation[RetiredStudy] = {
    (validateId(id).toValidationNel |@|
      validateAndIncrementVersion(version).toValidationNel |@|
      validateNonEmpty("name", name).toValidationNel |@|
      validateNonEmptyOption("description", salt).toValidationNel {
        RetiredStudy(_, _, _, _)
      }
  }
}
