package org.biobank.domain.study

import com.github.ghik.silencer.silent
import org.biobank.ValidationKey
import org.biobank.domain._
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationTemperature._
import org.biobank.domain.PreservationType._
import org.biobank.domain.SpecimenType._
import org.biobank.domain.{DomainValidation, HasUniqueName, HasOptionalDescription}
import play.api.libs.json._
import scalaz.Scalaz._

/** Identifies a unique [[SpecimenDescription]] in a Collection Event Type.
  *
  * Used as a value object to maintain associations to with entities in the system.
  */
final case class SpecimenDescriptionId(id: String) extends IdentifiedValueObject[String]

object SpecimenDescriptionId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val specimenDescriptionIdFormat: Format[SpecimenDescriptionId] = new Format[SpecimenDescriptionId] {

      override def writes(id: SpecimenDescriptionId): JsValue = JsString(id.id)

      override def reads(json: JsValue): JsResult[SpecimenDescriptionId] =
        Reads.StringReads.reads(json).map(SpecimenDescriptionId.apply _)
    }

}

/** Used to define a [[Specimen]] that is collected by a [[Study]].
 *
 * It records ownership, summary, storage, and classification information that applies to an
 * entire group or collection of [[Specimen]]s. A specimen description is defined either for
 * specimen types collected from participants, or for specimen types that are processed.
 *
 * This class has a private constructor and instances of this class can only be created using
 * the [[SpecimenDescription.create]] method on the factory object.
 */
trait SpecimenDescription
    extends IdentifiedValueObject[SpecimenDescriptionId]
    with HasUniqueName
    with HasSlug
    with HasOptionalDescription {

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
  val preservationTemperature: PreservationTemperature

  /** See [[SpecimenType]]. */
  val specimenType: SpecimenType

  override def toString: String =
    s"""|SpecimenDescription:{
        |  id:                          $id,
        |  slug:                        $slug,
        |  name:                        $name,
        |  description:                 $description,
        |  units:                       $units,
        |  anatomicalSourceType:        $anatomicalSourceType,
        |  preservationType:            $preservationType,
        |  preservationTemperature: $preservationTemperature,
        |  specimenType:                $specimenType
        |}""".stripMargin

}

trait SpecimenSpecValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

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
  def validate(name:        String,
               description: Option[String],
               units:       String)
      : DomainValidation[Boolean] =  {
    (validateString(name, NameRequired) |@|
       validateNonEmptyStringOption(description, InvalidDescription) |@|
       validateString(units, UnitsRequired)) {
      case _ => true
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validate(specimenDesc: SpecimenDescription): DomainValidation[Boolean] =  {
    validate(specimenDesc.name, specimenDesc.description, specimenDesc.units)
  }

}

final case class CollectionSpecimenDescription(id:                          SpecimenDescriptionId,
                                               slug:                        String,
                                               name:                        String,
                                               description:                 Option[String],
                                               units:                       String,
                                               anatomicalSourceType:        AnatomicalSourceType,
                                               preservationType:            PreservationType,
                                               preservationTemperature: PreservationTemperature,
                                               specimenType:                SpecimenType,
                                               maxCount:                    Int,
                                               amount:                      BigDecimal)
    extends SpecimenDescription

object CollectionSpecimenDescription extends SpecimenSpecValidations {
  import org.biobank.CommonValidations._

  implicit val collectionSpecimenSpecWrites: Format[CollectionSpecimenDescription] =
    Json.format[CollectionSpecimenDescription]

  val hashidsSalt: String = "biobank-collection-event-types"


  def create(name:                        String,
             description:                 Option[String],
             units:                       String,
             anatomicalSourceType:        AnatomicalSourceType,
             preservationType:            PreservationType,
             preservationTemperature: PreservationTemperature,
             specimenType:                SpecimenType,
             maxCount:                    Int,
             amount:                      BigDecimal)
      : DomainValidation[CollectionSpecimenDescription] = {
    validate(name,
             description,
             units,
             anatomicalSourceType,
             preservationType,
             preservationTemperature,
             specimenType,
             maxCount,
             amount).map { _ =>
      val id = SpecimenDescriptionId(java.util.UUID.randomUUID.toString.replaceAll("-","").toUpperCase)
      CollectionSpecimenDescription(id                          = id,
                                    slug                        = Slug(name),
                                    name                        = name,
                                    description                 = description,
                                    units                       = units,
                                    anatomicalSourceType        = anatomicalSourceType,
                                    preservationType            = preservationType,
                                    preservationTemperature = preservationTemperature,
                                    specimenType                = specimenType,
                                    maxCount                    = maxCount,
                                    amount                      = amount)
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  @silent def validate(name:                        String,
                       description:                 Option[String],
                       units:                       String,
                       anatomicalSourceType:        AnatomicalSourceType,
                       preservationType:            PreservationType,
                       preservationTemperature: PreservationTemperature,
                       specimenType:                SpecimenType,
                       maxCount:                    Int,
                       amount:                      BigDecimal)
      : DomainValidation[Boolean] = {
    (validate(name, description, units) |@|
       validatePositiveNumber(maxCount, MaxCountInvalid) |@|
       validatePositiveNumber(amount, AmountInvalid)) {
      case _ => true
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validate(specimenDesc: CollectionSpecimenDescription): DomainValidation[Boolean] = {
    validate(specimenDesc.name,
             specimenDesc.description,
             specimenDesc.units,
             specimenDesc.anatomicalSourceType,
             specimenDesc.preservationType,
             specimenDesc.preservationTemperature,
             specimenDesc.specimenType,
             specimenDesc.maxCount,
             specimenDesc.amount)
  }


}
