package org.biobank.domain.study

import org.biobank.domain.{
  AnnotationTypeId,
  ConcurrencySafeEntity,
  DomainValidation,
  HasName,
  HasDescriptionOption }
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.AnnotationValueType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._
import org.biobank.domain.validation.StudyValidationHelper

import scalaz._
import scalaz.Scalaz._

/**
  * This is an aggregate root.
  */
sealed trait Study
    extends ConcurrencySafeEntity[StudyId]
    with HasName
    with HasDescriptionOption {
  val name: String
  val description: Option[String]
  val status: String

  override def toString =
    s"""|Study: {
        |  id: $id,
        |  version: $version,
        |  name: $name,
        |  description: $description
        |}""".stripMargin

}

/*
 *  This is the initial state for a study.  In this state collection and processing of specimens is not
 *  allowed.
 *
 */
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
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int],
    options: Option[Map[String, String]],
    required: Boolean): DomainValidation[ParticipantAnnotationType] = {
    ParticipantAnnotationType.create(this.id, id, version, name, description,
      valueType, maxValueCount, options, required)
  }


  def addSpecimenGropu(
    id: SpecimenGroupId,
    version: Long = -1,
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



}

object DisabledStudy extends StudyValidationHelper {

  def create(
    id: StudyId,
    version: Long,
    name: String,
    description: Option[String]): DomainValidation[DisabledStudy] = {
    (validateId(id).toValidationNel |@|
      validateAndIncrementVersion(version).toValidationNel |@|
      validateNonEmpty(name, "name is null or empty").toValidationNel |@|
      validateNonEmptyOption(description, "description is null or empty").toValidationNel) {
        DisabledStudy(_, _, _, _)
      }
  }
}

/*
 * In this state collection and processing of specimens can take place.
 */
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

object EnabledStudy extends StudyValidationHelper {

  def create(study: DisabledStudy): DomainValidation[EnabledStudy] = {
    (validateId(study.id).toValidationNel |@|
      validateAndIncrementVersion(study.version).toValidationNel |@|
      validateNonEmpty(study.name, "name is null or empty").toValidationNel |@|
      validateNonEmptyOption(study.description, "description is null or empty").toValidationNel) {
        EnabledStudy(_, _, _, _)
      }
  }
}

/*
 *  In this state the study cannot be modified and collection and processing of specimens is not allowed.
 */
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

object RetiredStudy extends StudyValidationHelper {

  def create(study: DisabledStudy): DomainValidation[RetiredStudy] = {
    (validateId(study.id).toValidationNel |@|
      validateAndIncrementVersion(study.version).toValidationNel |@|
      validateNonEmpty(study.name, "name is null or empty").toValidationNel |@|
      validateNonEmptyOption(study.description, "description is null or empty").toValidationNel) {
        RetiredStudy(_, _, _, _)
      }
  }
}
