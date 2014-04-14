package org.biobank.domain.study

import org.biobank.infrastructure._
import org.biobank.domain.{
  ConcurrencySafeEntity,
  DomainValidation,
  HasName,
  HasDescriptionOption
}
import org.biobank.domain.study._
import org.biobank.domain.validation.StudyValidationHelper
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._

import scalaz._
import Scalaz._

case class SpecimenGroup private (
  studyId: StudyId,
  id: SpecimenGroupId,
  version: Long = -1,
  name: String,
  description: Option[String],
  units: String,
  anatomicalSourceType: AnatomicalSourceType,
  preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType,
  specimenType: SpecimenType)
    extends ConcurrencySafeEntity[SpecimenGroupId]
    with HasName
    with HasDescriptionOption {

  override def toString: String =
    s"""|SpecimenGroup:{
        |  id: %s,
        |  version: %d,
        |  studyId: %s,
        |  name: %s,
        |  description: %s,"
        |  units: %s,
        |  anatomicalSourceType: %s,
        |  preservationType: %,
        |  preservationTemperatureType: %s,
        |  specimenType: %s
        |}""".stripMargin

}

object SpecimenGroup extends StudyValidationHelper {

  def validateId(id: SpecimenGroupId): Validation[String, SpecimenGroupId] = {
    validateStringId(id.toString, "specimen group id is null or empty") match {
      case Success(idString) => id.success
      case Failure(err) => err.fail
    }
  }

  def create(
    studyId: StudyId,
    id: SpecimenGroupId,
    version: Long = -1,
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType): DomainValidation[SpecimenGroup] =  {
    (validateId(studyId).toValidationNel |@|
      validateId(id).toValidationNel |@|
      validateAndIncrementVersion(version).toValidationNel |@|
      validateNonEmpty(name, "name is null or empty").toValidationNel |@|
      validateNonEmptyOption(description, "description is null or empty").toValidationNel |@|
      validateNonEmpty(units, "units is null or empty").toValidationNel) {
      SpecimenGroup(_, _, _, _, _, _, anatomicalSourceType, preservationType,
	preservationTemperatureType, specimenType)
    }
  }
}
