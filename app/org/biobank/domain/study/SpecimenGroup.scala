package org.biobank.domain.study

import org.biobank.ValidationKey
import org.biobank.domain.{
  ConcurrencySafeEntity,
  DomainValidation,
  HasUniqueName,
  HasDescriptionOption
}
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._
import org.biobank.infrastructure.JsonUtils._
import org.joda.time.DateTime

import play.api.libs.json._

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
 *
 * @param units Specifies how the specimen amount is measured (e.g. volume, weight, length, etc.).
 *
 * @param anatomicalSourceType see [[AnatomicalSourceType]].
 *
 * @param preservationType see [[PreservationType]].
 *
 * @param preservationTemperatureType see [[PreservationTemperatureType]].
 *
 * @param specimenType see [[SpecimenType]].
 */
final case class SpecimenGroup(studyId:                     StudyId,
                               id:                          SpecimenGroupId,
                               version:                     Long,
                               timeAdded:                   DateTime,
                               timeModified:                Option[DateTime],
                               name:                        String,
                               description:                 Option[String],
                               units:                       String,
                               anatomicalSourceType:        AnatomicalSourceType,
                               preservationType:            PreservationType,
                               preservationTemperatureType: PreservationTemperatureType,
                               specimenType:                SpecimenType)
    extends ConcurrencySafeEntity[SpecimenGroupId]
    with HasUniqueName
    with HasDescriptionOption
    with HasStudyId {

  override def toString: String =
    s"""|SpecimenGroup:{
        |  studyId:                     $studyId,
        |  id:                          $id,
        |  version:                     $version,
        |  name:                        $name,
        |  timeAdded:                   $timeAdded,
        |  timeModified:                $timeModified,
        |  description:                 $description,
        |  units:                       $units,
        |  anatomicalSourceType:        $anatomicalSourceType,
        |  preservationType:            $preservationType,
        |  preservationTemperatureType: $preservationTemperatureType,
        |  specimenType:                $specimenType
        |}""".stripMargin

  def update(name:                        String,
             description:                 Option[String],
             units:                       String,
             anatomicalSourceType:        AnatomicalSourceType,
             preservationType:            PreservationType,
             preservationTemperatureType: PreservationTemperatureType,
             specimenType:                SpecimenType)
      : DomainValidation[SpecimenGroup] =  {
    val v = SpecimenGroup.create(this.studyId,
                                 this.id,
                                 this.version + 1,
                                 name,
                                 description,
                                 units,
                                 anatomicalSourceType,
                                 preservationType,
                                 preservationTemperatureType,
                                 specimenType)
    v.map(_.copy(timeModified = Some(DateTime.now)))
  }
}

trait SpecimenGroupValidations {

  case object UnitsRequired extends ValidationKey

}


/**
  * Factory object used to create a [[SpecimenGroup]].
  */
object SpecimenGroup extends SpecimenGroupValidations {
  import org.biobank.domain.CommonValidations._

  /**
    * The factory method to create a specimen group. Note that it increments the version number
    * by one.
    *
    * Performs validation on fields.
    *
    * @param version the previous version number for the specimen group. If the specimen group is
    * new then this value should be 0L.
    */
  def create(studyId:                     StudyId,
             id:                          SpecimenGroupId,
             version:                     Long,
             name:                        String,
             description:                 Option[String],
             units:                       String,
             anatomicalSourceType:        AnatomicalSourceType,
             preservationType:            PreservationType,
             preservationTemperatureType: PreservationTemperatureType,
             specimenType:                SpecimenType)
      : DomainValidation[SpecimenGroup] =  {
    (validateId(studyId) |@|
       validateId(id) |@|
       validateVersion(version) |@|
       validateString(name, NameRequired) |@|
       validateNonEmptyOption(description, InvalidDescription) |@|
       validateString(units, UnitsRequired)) {
      case (_, _, _, _, _, _) => SpecimenGroup(studyId,
                                               id,
                                               version,
                                               DateTime.now,
                                               None,
                                               name,
                                               description,
                                               units,
                                               anatomicalSourceType,
                                               preservationType,
                                               preservationTemperatureType,
                                               specimenType)
    }
  }

  implicit val specimenGroupWrites: Writes[SpecimenGroup] = Json.writes[SpecimenGroup]

}
