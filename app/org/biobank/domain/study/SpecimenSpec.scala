package org.biobank.domain.study

import org.biobank.ValidationKey
import org.biobank.domain.{
  DomainValidation,
  HasUniqueName,
  HasDescriptionOption
}
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._

import play.api.libs.json._
import scalaz.Scalaz._

/** Used to configure a [[SpecimenType]] used by a [[Study]].
 *
 * It records ownership, summary, storage, and classification information that applies to an
 * entire group or collection of [[Specimen]]s. A specimen group is defined either for
 * specimen types collected from participants, or for specimen types that are processed.
 *
 * This class has a private constructor and instances of this class can only be created using
 * the [[SpecimenSpec.create]] method on the factory object.
 */
trait SpecimenSpec extends HasUniqueName with HasDescriptionOption {

  /** The unique ID for this object. */
  val uniqueId: String

  /** A short identifying name that is unique to the study. */
  val name: String

  val description: Option[String]

  /** Specifies how the specimen amount is measured (e.g. volume, weight, length, etc.). */
  val units: String

  /** See [[AnatomicalSourceType]]. */
  val anatomicalSourceType: AnatomicalSourceType

  /** See [[PreservationType]]. */
  val preservationType: PreservationType

  /** See [[PreservationType]]. */
  val preservationTemperatureType: PreservationTemperatureType

  /** See [[SpecimenType]]. */
  val specimenType: SpecimenType

  override def equals(that: Any): Boolean = {
    that match {
      case that: SpecimenSpec => this.uniqueId.equalsIgnoreCase(that.uniqueId)
      case _ => false
    }
  }

  override def hashCode:Int = {
    uniqueId.toUpperCase.hashCode
  }

  override def toString: String =
    s"""|SpecimenSpec:{
        |  uniqueId:                    $uniqueId,
        |  name:                        $name,
        |  description:                 $description,
        |  units:                       $units,
        |  anatomicalSourceType:        $anatomicalSourceType,
        |  preservationType:            $preservationType,
        |  preservationTemperatureType: $preservationTemperatureType,
        |  specimenType:                $specimenType
        |}""".stripMargin

}

trait SpecimenSpecValidations {
  import org.biobank.domain.CommonValidations._

  case object UnitsRequired extends ValidationKey

  case object MaxCountInvalid extends ValidationKey

  case object AmountInvalid extends ValidationKey

  /**
    * The factory method to create a specimen group. Note that it increments the version number
    * by one.
    *
    * Performs validation on fields.
    *
    * @param version the previous version number for the specimen group. If the specimen group is
    * new then this value should be 0L.
    */
  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validate(name:                        String,
               description:                 Option[String],
               units:                       String,
               anatomicalSourceType:        AnatomicalSourceType,
               preservationType:            PreservationType,
               preservationTemperatureType: PreservationTemperatureType,
               specimenType:                SpecimenType)
      : DomainValidation[Boolean] =  {
    (validateString(name, NameRequired) |@|
       validateNonEmptyOption(description, InvalidDescription) |@|
       validateString(units, UnitsRequired)) {
      case (_, _, _) => true
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validate(specimenSpec: SpecimenSpec): DomainValidation[Boolean] =  {
    validate(specimenSpec.name,
             specimenSpec.description,
             specimenSpec.units,
             specimenSpec.anatomicalSourceType,
             specimenSpec.preservationType,
             specimenSpec.preservationTemperatureType,
             specimenSpec.specimenType)
  }

}

final case class CollectionSpecimenSpec(uniqueId:                    String,
                                        name:                        String,
                                        description:                 Option[String],
                                        units:                       String,
                                        anatomicalSourceType:        AnatomicalSourceType,
                                        preservationType:            PreservationType,
                                        preservationTemperatureType: PreservationTemperatureType,
                                        specimenType:                SpecimenType,
                                        maxCount:                    Int,
                                        amount:                      Option[BigDecimal])
    extends SpecimenSpec

object CollectionSpecimenSpec extends SpecimenSpecValidations {
  import org.biobank.domain.CommonValidations._

  implicit val collectionSpecimenSpecWrites: Writes[CollectionSpecimenSpec] =
    Json.writes[CollectionSpecimenSpec]

  val hashidsSalt = "biobank-collection-event-types"


  def create(name:                        String,
             description:                 Option[String],
             units:                       String,
             anatomicalSourceType:        AnatomicalSourceType,
             preservationType:            PreservationType,
             preservationTemperatureType: PreservationTemperatureType,
             specimenType:                SpecimenType,
             maxCount:                    Int,
             amount:                      Option[BigDecimal])
      : DomainValidation[CollectionSpecimenSpec] = {
    validate(name,
             description,
             units,
             anatomicalSourceType,
             preservationType,
             preservationTemperatureType,
             specimenType,
             maxCount,
             amount).map { _ =>
      val uniqueId = java.util.UUID.randomUUID.toString.replaceAll("-","").toUpperCase
      CollectionSpecimenSpec(uniqueId,
                             name,
                             description,
                             units,
                             anatomicalSourceType,
                             preservationType,
                             preservationTemperatureType,
                             specimenType,
                             maxCount,
                             amount)
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validate(name:                        String,
               description:                 Option[String],
               units:                       String,
               anatomicalSourceType:        AnatomicalSourceType,
               preservationType:            PreservationType,
               preservationTemperatureType: PreservationTemperatureType,
               specimenType:                SpecimenType,
               maxCount:                    Int,
               amount:                      Option[BigDecimal])
      : DomainValidation[Boolean] = {
    (validate(name,
              description,
              units,
              anatomicalSourceType,
              preservationType,
              preservationTemperatureType,
              specimenType) |@|
        validatePositiveNumber(maxCount, MaxCountInvalid) |@|
       validatePositiveNumberOption(amount, AmountInvalid)) {
      case (_, _, _) => true
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validate(specimenSpec: CollectionSpecimenSpec): DomainValidation[Boolean] = {
    validate(specimenSpec.name,
             specimenSpec.description,
             specimenSpec.units,
             specimenSpec.anatomicalSourceType,
             specimenSpec.preservationType,
             specimenSpec.preservationTemperatureType,
             specimenSpec.specimenType,
             specimenSpec.maxCount,
             specimenSpec.amount)
  }


}
