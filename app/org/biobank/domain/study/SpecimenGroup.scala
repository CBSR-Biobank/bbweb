package org.biobank.domain.study

import org.biobank.infrastructure._
import org.biobank.domain.{
  ConcurrencySafeEntity,
  DomainValidation,
  HasUniqueName,
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

/** Used to configure a [[SpecimenType]] used by the [[Study]].
  *
  * It records ownership, summary, storage, and classification information that applies to an entire group or
  * collection of [[Specimen]]s. A specimen group is defined either for specimen types collected from
  * participants, or for specimen types that are processed.
  *
  * This class has a private constructor and instances of this class can only be created using the
  * [[SpecimenGroup.create]] method on the factory object.
  *
  * @param studyId The ID of the study this specimen group belongs to.
  * @param units Specifies how the specimen amount is measured (e.g. volume, weight, length, etc.).
  * @param name A short identifying name that is unique to the study.
  * @param anatomicalSourceType see [[AnatomicalSourceType]].
  * @param preservationType see [[PreservationType]].
  * @param preservationTemperatureType see [[PreservationTemperatureType]].
  * @param specimenType see [[SpecimenType]].
  */
case class SpecimenGroup private (
  studyId: StudyId,
  id: SpecimenGroupId,
  version: Long,
  name: String,
  description: Option[String],
  units: String,
  anatomicalSourceType: AnatomicalSourceType,
  preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType,
  specimenType: SpecimenType)
    extends ConcurrencySafeEntity[SpecimenGroupId]
    with HasUniqueName
    with HasDescriptionOption {

  override def toString: String =
    s"""|SpecimenGroup:{
        |  studyId: $studyId,
        |  id: $id,
        |  version: $version,
        |  name: $name,
        |  description: $description,
        |  units: $units,
        |  anatomicalSourceType: $anatomicalSourceType,
        |  preservationType: $preservationType,
        |  preservationTemperatureType: $preservationTemperatureType,
        |  specimenType: $specimenType
        |}""".stripMargin

}

/**
  * Factory object used to create a [[SpecimenGroup]].
  */
object SpecimenGroup extends StudyValidationHelper {

  protected def validateId(id: SpecimenGroupId): Validation[String, SpecimenGroupId] = {
    validateStringId(id.toString, "specimen group id is null or empty") match {
      case Success(idString) => id.success
      case Failure(err) => err.fail
    }
  }

  /**
    * The factory method to create a specimen group.
    *
    * Performs validation on fields.
    */
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
