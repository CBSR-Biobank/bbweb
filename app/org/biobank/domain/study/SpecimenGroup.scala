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

/** Used to configure a [[SpecimenType]] used by a [[Study]].
  *
  * It records ownership, summary, storage, and classification information that applies to an entire group or
  * collection of [[Specimen]]s. A specimen group is defined either for specimen types collected from
  * participants, or for specimen types that are processed.
  *
  * This class has a private constructor and instances of this class can only be created using the
  * [[SpecimenGroup.create]] method on the factory object.
  *
  * @param name A short identifying name that is unique to the study.
  * @param units Specifies how the specimen amount is measured (e.g. volume, weight, length, etc.).
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
    with HasDescriptionOption
    with HasStudyId {

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

  def update(
    expectedVersion: Option[Long],
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType): DomainValidation[SpecimenGroup] =  {
    for {
      validVersion <- requireVersion(expectedVersion)
      updatedSpecimenGroup <- SpecimenGroup.create(studyId, id, version, name, description,
        units, anatomicalSourceType, preservationType, preservationTemperatureType,
        specimenType)
    } yield updatedSpecimenGroup
  }
}

/**
  * Factory object used to create a [[SpecimenGroup]].
  */
object SpecimenGroup extends StudyValidationHelper {

  /**
    * The factory method to create a specimen group. Note that it increments the version number
    * by one.
    *
    * Performs validation on fields.
    *
    * @param version the previous version number for the specimen group. If the specimen group is
    * new then this value should be -1L.
    */
  def create(
    studyId: StudyId,
    id: SpecimenGroupId,
    version: Long,
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType): DomainValidation[SpecimenGroup] =  {
    (validateId(studyId) |@|
      validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validateNonEmpty(name, "name is null or empty") |@|
      validateNonEmptyOption(description, "description is null or empty") |@|
      validateNonEmpty(units, "units is null or empty")) {
      SpecimenGroup(_, _, _, _, _, _, anatomicalSourceType, preservationType,
        preservationTemperatureType, specimenType)
    }
  }
}
