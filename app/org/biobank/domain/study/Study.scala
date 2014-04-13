package org.biobank.domain.study

import org.biobank.domain.{
  AnnotationTypeId,
  ConcurrencySafeEntity,
  DomainValidation,
  HasName,
  HasDescriptionOption }
import org.biobank.domain.AnnotationValueType._
import org.biobank.domain.validator.StudyValidator

import scalaz._
import scalaz.Scalaz._

sealed trait Study
    extends ConcurrencySafeEntity[StudyId]
    with HasName
    with HasDescriptionOption {
  val name: String
  val description: Option[String]
  val status: String

  override def toString =
    s"""Study: {
       | id: $id,
       | version: $version,
       | name: $name,
       | description: $description
       |}""".stripMargin
}

case class DisabledStudy private (
  id: StudyId,
  version: Long = -1,
  name: String,
  description: Option[String])
  extends Study {

  override val status: String = "Disabled"

  def enable: DomainValidation[EnabledStudy] = {
    EnabledStudy.create(this)
  }

  def retire: DomainValidation[RetiredStudy] = {
    RetiredStudy.create(this)
  }

  def addParticipantAnnotationType(
    id: AnnotationTypeId,
    version: Long,
    studyId: StudyId,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]],
    required: Boolean): DomainValidation[ParticipantAnnotationType] = {
    if (studyId != this.id) {
      throw new IllegalArgumentException("studyId does not match")
    }
    ParticipantAnnotationType.create(id, version, studyId, name, description,
      valueType, maxValueCount, options, required)
  }

}

object DisabledStudy extends StudyValidator {

  def create(
    id: StudyId,
    version: Long,
    name: String,
    description: Option[String]): DomainValidation[DisabledStudy] = {
    (validateId(id).toValidationNel |@|
      validateAndIncrementVersion(version).toValidationNel |@|
      validateNonEmpty("name", name).toValidationNel |@|
      validateNonEmptyOption("description", description).toValidationNel) {
        DisabledStudy(_, _, _, _)
      }
  }
}


case class EnabledStudy private (
  id: StudyId,
  version: Long,
  name: String,
  description: Option[String])
  extends Study {

  override val status: String = "Enabled"

  def disable: DomainValidation[DisabledStudy] =
    DisabledStudy.create(this.id, this.version, this.name, this.description)
}

object EnabledStudy extends StudyValidator {

  def create(study: DisabledStudy): DomainValidation[EnabledStudy] = {
    (validateId(study.id).toValidationNel |@|
      validateAndIncrementVersion(study.version).toValidationNel |@|
      validateNonEmpty("name", study.name).toValidationNel |@|
      validateNonEmptyOption("description", study.description).toValidationNel) {
        EnabledStudy(_, _, _, _)
      }
  }
}

case class RetiredStudy private (
  id: StudyId,
  version: Long,
  name: String,
  description: Option[String])
  extends Study {

  override val status: String = "Retired"

  def unretire: DomainValidation[DisabledStudy] = {
    DisabledStudy.create(this.id, this.version, this.name, this.description)
  }
}

object RetiredStudy extends StudyValidator {

  def create(study: DisabledStudy): DomainValidation[RetiredStudy] = {
    (validateId(study.id).toValidationNel |@|
      validateAndIncrementVersion(study.version).toValidationNel |@|
      validateNonEmpty("name", study.name).toValidationNel |@|
      validateNonEmptyOption("description", study.description).toValidationNel) {
        RetiredStudy(_, _, _, _)
      }
  }
}
