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
import org.biobank.infrastructure.JsonUtils._
import org.biobank.infrastructure.command.StudyCommands._

import play.api.libs.json._
import play.api.libs.functional.syntax._

import com.github.nscala_time.time.Imports._
import scalaz._
import Scalaz._

/** Used to configure a [[SpecimenType]] used by a [[Study]].
  *
  * It records ownership, summary, storage, and classification information that applies to an
  * entire group or collection of [[Specimen]]s. A specimen group is defined either for
  * specimen types collected from participants, or for specimen types that are processed.
  *
  * This class has a private constructor and instances of this class can only be created using
  * the [[SpecimenGroup.create]] method on the factory object.
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
  addedDate: DateTime,
  lastUpdateDate: Option[DateTime],
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
        |  addedDate: $addedDate,
        |  lastUpdateDate: $lastUpdateDate,
        |  description: $description,
        |  units: $units,
        |  anatomicalSourceType: $anatomicalSourceType,
        |  preservationType: $preservationType,
        |  preservationTemperatureType: $preservationTemperatureType,
        |  specimenType: $specimenType
        |}""".stripMargin

  def update(
    expectedVersion: Option[Long],
    dateTime: DateTime,
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType): DomainValidation[SpecimenGroup] =  {
    for {
      validVersion <- requireVersion(expectedVersion)
      validatedSpecimenGroup <- SpecimenGroup.create(
        studyId, id, version, addedDate, name, description, units, anatomicalSourceType,
        preservationType, preservationTemperatureType, specimenType)
      updatedSpecimenGroup <- validatedSpecimenGroup.copy(lastUpdateDate = Some(dateTime)).success
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
    dateTime: DateTime,
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
      SpecimenGroup(_, _, _, dateTime, None, _, _, _, anatomicalSourceType, preservationType,
        preservationTemperatureType, specimenType)
    }
  }

  implicit val specimenGroupWrites: Writes[SpecimenGroup] = (
    (__ \ "studyId").write[StudyId] and
      (__ \ "id").write[SpecimenGroupId] and
      (__ \ "version").write[Long] and
      (__ \ "addedDate").write[DateTime] and
      (__ \ "lastUpdateDate").write[Option[DateTime]] and
      (__ \ "name").write[String] and
      (__ \ "description").write[Option[String]] and
      (__ \ "units").write[String] and
      (__ \ "anatomicalSourceType").write[AnatomicalSourceType] and
      (__ \ "preservationType").write[PreservationType] and
      (__ \ "preservationTemperatureType").write[PreservationTemperatureType] and
      (__ \ "specimenType").write[SpecimenType]
  )(unlift(SpecimenGroup.unapply))


}
