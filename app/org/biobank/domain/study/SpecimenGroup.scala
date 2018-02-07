package org.biobank.domain.study

import java.time.OffsetDateTime
import org.biobank.ValidationKey
import org.biobank.domain._
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._
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
                               timeAdded:                   OffsetDateTime,
                               timeModified:                Option[OffsetDateTime],
                               slug:                        String,
                               name:                        String,
                               description:                 Option[String],
                               units:                       String,
                               anatomicalSourceType:        AnatomicalSourceType,
                               preservationType:            PreservationType,
                               preservationTemperatureType: PreservationTemperatureType,
                               specimenType:                SpecimenType)
    extends ConcurrencySafeEntity[SpecimenGroupId]
    with HasUniqueName
    with HasOptionalDescription
    with HasStudyId {

  override def toString: String =
    s"""|SpecimenGroup:{
        |  studyId:                     $studyId,
        |  id:                          $id,
        |  version:                     $version,
        |  slug:                        $slug,
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
    v.map(_.copy(timeModified = Some(OffsetDateTime.now)))
  }
}

trait SpecimenGroupValidations {

  case object UnitsRequired extends ValidationKey

}


/**
  * Factory object used to create a [[SpecimenGroup]].
  */
object SpecimenGroup extends SpecimenGroupValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

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
       validateNonEmptyString(name, NameRequired) |@|
       validateNonEmptyStringOption(description, InvalidDescription) |@|
       validateNonEmptyString(units, UnitsRequired)) { case _ =>
        SpecimenGroup(studyId                     = studyId,
                      id                          = id,
                      version                     = version,
                      timeAdded                   = OffsetDateTime.now,
                      timeModified                = None,
                      slug                        = Slug(name),
                      name                        = name,
                      description                 = description,
                      units                       = units,
                      anatomicalSourceType        = anatomicalSourceType,
                      preservationType            = preservationType,
                      preservationTemperatureType = preservationTemperatureType,
                      specimenType                = specimenType)
    }
  }

  implicit val specimenGroupWrites: Writes[SpecimenGroup] = Json.writes[SpecimenGroup]

}
