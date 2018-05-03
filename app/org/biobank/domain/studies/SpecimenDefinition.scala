package org.biobank.domain.studies

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

/** Identifies a unique [[SpecimenDefinition]] in a Collection Event Type.
  *
  * Used as a value object to maintain associations to with entities in the system.
  */
final case class SpecimenDefinitionId(id: String) extends IdentifiedValueObject[String]

object SpecimenDefinitionId {

  // Do not want JSON to create a sub object, we just want it to be converted
  // to a single string
  implicit val specimenDefinitionIdFormat: Format[SpecimenDefinitionId] = new Format[SpecimenDefinitionId] {

      override def writes(id: SpecimenDefinitionId): JsValue = JsString(id.id)

      override def reads(json: JsValue): JsResult[SpecimenDefinitionId] =
        Reads.StringReads.reads(json).map(SpecimenDefinitionId.apply _)
    }

}

/**
 * Used to configure a [[SpecimenType]] used by a [[Study]].
 *
 * It records ownership, summary, storage, and classification information that applies to an
 * entire group or collection of [[Specimen]]s. A specimen description is defined either for
 * specimen types collected from participants, or for specimen types that are processed.
 *
 * This class has a private constructor and instances of this class can only be created using
 * the [[SpecimenDefinition.create]] method on the factory object.
 */
trait SpecimenDefinition
    extends IdentifiedValueObject[SpecimenDefinitionId]
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
    s"""|SpecimenDefinition:{
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
  def validate(specimenDesc: SpecimenDefinition): DomainValidation[Boolean] =  {
    validate(specimenDesc.name, specimenDesc.description, specimenDesc.units)
  }

}

/**
 * Used to define a [[domain.participants.Specimen Specimen]] that is collected by a [[domain.studies.Study
 * Study]].
 *
 * It records ownership, summary, storage, and classification information that applies to an entire group or
 * collection of [[domain.participants. Specimen Specimens]]. A specimen description is defined either for
 * specimen types collected from participants, or for specimen types that are processed.
 */
final case class CollectionSpecimenDefinition(id:                      SpecimenDefinitionId,
                                              slug:                    Slug,
                                              name:                    String,
                                              description:             Option[String],
                                              units:                   String,
                                              anatomicalSourceType:    AnatomicalSourceType,
                                              preservationType:        PreservationType,
                                              preservationTemperature: PreservationTemperature,
                                              specimenType:            SpecimenType,
                                              maxCount:                Int,
                                              amount:                  BigDecimal)
    extends SpecimenDefinition

object CollectionSpecimenDefinition extends SpecimenSpecValidations {
  import org.biobank.CommonValidations._

  implicit val collectionSpecimenDefinitionWrites: Format[CollectionSpecimenDefinition] =
    Json.format[CollectionSpecimenDefinition]

  val hashidsSalt: String = "biobank-collection-event-types"

  /**
   * Creates a [[domain.studies.CollectionSpecimenDefinition.create CollectionSpecimenDefinition]] with the
   * given properties.
   */
  def create(name:                        String,
             description:                 Option[String],
             units:                       String,
             anatomicalSourceType:        AnatomicalSourceType,
             preservationType:            PreservationType,
             preservationTemperature: PreservationTemperature,
             specimenType:                SpecimenType,
             maxCount:                    Int,
             amount:                      BigDecimal)
      : DomainValidation[CollectionSpecimenDefinition] = {
    validate(name,
             description,
             units,
             anatomicalSourceType,
             preservationType,
             preservationTemperature,
             specimenType,
             maxCount,
             amount).map { _ =>
      val id = SpecimenDefinitionId(java.util.UUID.randomUUID.toString.replaceAll("-","").toUpperCase)
      CollectionSpecimenDefinition(id                          = id,
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
  def validate(specimenDesc: CollectionSpecimenDefinition): DomainValidation[Boolean] = {
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

final case class ProcessingSpecimenDefinition(id:                      SpecimenDefinitionId,
                                               slug: Slug,
                                               name:                    String,
                                               description:             Option[String],
                                               units:                   String,
                                               anatomicalSourceType:    AnatomicalSourceType,
                                               preservationType:        PreservationType,
                                               preservationTemperature: PreservationTemperature,
                                               specimenType:            SpecimenType)
    extends SpecimenDefinition

object ProcessingSpecimenDefinition extends SpecimenSpecValidations {

  implicit val processingSpecimenSpecWrites: Format[ProcessingSpecimenDefinition] =
    Json.format[ProcessingSpecimenDefinition]

  def create(name:                    String,
             description:             Option[String],
             units:                   String,
             anatomicalSourceType:    AnatomicalSourceType,
             preservationType:        PreservationType,
             preservationTemperature: PreservationTemperature,
             specimenType:            SpecimenType)
      : DomainValidation[ProcessingSpecimenDefinition] = {
    validate(name,
             description,
             units,
             anatomicalSourceType,
             preservationType,
             preservationTemperature,
             specimenType).map { _ =>
      val id = SpecimenDefinitionId(java.util.UUID.randomUUID.toString.replaceAll("-","").toUpperCase)
      ProcessingSpecimenDefinition(id                      = id,
                                    slug                    = Slug(name),
                                    name                    = name,
                                    description             = description,
                                    units                   = units,
                                    anatomicalSourceType    = anatomicalSourceType,
                                    preservationType        = preservationType,
                                    preservationTemperature = preservationTemperature,
                                    specimenType            = specimenType)
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  @silent def validate(name:                    String,
                       description:             Option[String],
                       units:                   String,
                       anatomicalSourceType:    AnatomicalSourceType,
                       preservationType:        PreservationType,
                       preservationTemperature: PreservationTemperature,
                       specimenType:            SpecimenType)
      : DomainValidation[Boolean] = {
    validate(name, description, units).map { _ => true }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def validate(specimenDesc: ProcessingSpecimenDefinition): DomainValidation[Boolean] = {
    validate(specimenDesc.name,
             specimenDesc.description,
             specimenDesc.units,
             specimenDesc.anatomicalSourceType,
             specimenDesc.preservationType,
             specimenDesc.preservationTemperature,
             specimenDesc.specimenType)
  }


}
