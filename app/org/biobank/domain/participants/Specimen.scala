package org.biobank.domain.participants

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.biobank.ValidationKey
import org.biobank.dto.SpecimenDto
import org.biobank.domain._
import org.biobank.domain.containers.{ContainerId, ContainerSchemaPositionId}
import org.biobank.domain.studies.{CollectionSpecimenDefinition, SpecimenDefinitionId, StudyValidations}
import org.biobank.domain.{ConcurrencySafeEntity, DomainValidation}
import org.biobank.infrastructure.EnumUtils._
import org.biobank.services.centres.CentreLocationInfo
import play.api.libs.json._
import scalaz.Scalaz._

/**
 * Represents something that was obtained from a [[domain.participants.Participant Participant]] in a
 * [[domain.studies.Study Study]].
 *
 * A Specimen collected from a [[domain.participants.Participant Participant]] can be created with
 * this aggregate and then added to a [[domain.participants.CollectionEvent CollectionEvent]]. When a
 * specimen is created it must be assigned the corresponding [[domain.studies.SpecimenDefinition
 * SpecimenDefinition]] defined in either the [[domain.participants.CollectionEvent
 * CollectionEvent]] or the specimen link type to which it corresponds .
 */
sealed trait Specimen
    extends ConcurrencySafeEntity[SpecimenId]
    with HasSlug {

  val state: EntityState

  /** The inventory ID assigned to this specimen. */
  val inventoryId: String

  /** The [[domain.studies.CollectionSpecimenDefinition CollectionSpecimenDefinition]] this specimen
    * belongs to, defined by the study it belongs to. */
  val specimenDefinitionId: SpecimenDefinitionId

  /** The [[domain.centres.Centre Centre]] where this specimen was created. */
  val originLocationId: LocationId

  /** The [[domain.centres.Centre Centre]] where this specimen is currently located. */
  val locationId: LocationId

  /** The [[domain.containers.Container Container]] this specimen is stored in. */
  val containerId: Option[ContainerId]

  /**
   * The [[domain.containers.ContainerSchemaPosition ContainerSchemaPosition]] (i.e. position or label) this
   * specimen has in its container.
   */
  val positionId: Option[ContainerSchemaPositionId]

  /**
   * The date and time when the specimen was physically created.
   *
   * Not necessarily when this specimen was added to the application.
   */
  val timeCreated: OffsetDateTime

  /**
   * The amount, in units specified in the [[domain.studies.SpecimenDefinition SpecimenDefinition]], for this
   * specimen.
   */
  val amount: scala.math.BigDecimal

  def createDto(collectionEvent:    CollectionEvent,
                eventTypeName:      String,
                specimenDesc:       CollectionSpecimenDefinition,
                originLocationInfo: CentreLocationInfo,
                locationInfo:       CentreLocationInfo): SpecimenDto =
    SpecimenDto(id                      = this.id.id,
                version                 = this.version,
                timeAdded               = this.timeAdded.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                timeModified            = this.timeModified.map(_.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)),
                state                   = this.state.id,
                slug                    = this.slug,
                inventoryId             = this.inventoryId,
                collectionEventId       = collectionEvent.id.id,
                specimenDefinitionId    = this.specimenDefinitionId.id,
                specimenDefinitionName  = specimenDesc.name,
                specimenDefinitionUnits = specimenDesc.units,
                originLocationInfo      = originLocationInfo,
                locationInfo            = locationInfo,
                containerId             = this.containerId.map(_.id),
                positionId              = this.positionId.map(_.id),
                timeCreated             = this.timeCreated.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                amount                  = this.amount,
                units                   = specimenDesc.units,
                isDefaultAmount         = (this.amount == specimenDesc.amount),
                eventTypeName           = eventTypeName)

  override def toString: String =
    s"""|${this.getClass.getSimpleName}: {
        |  id:                   $id
        |  version:              $version
        |  timeAdded:            $timeAdded
        |  timeModified:         $timeModified
        |  slug:                 $slug,
        |  inventoryId:          $inventoryId
        |  specimenDefinitionId: $specimenDefinitionId
        |  originLocationId:     $originLocationId
        |  locationId:           $locationId
        |  containerId:          $containerId
        |  positionId:           $positionId
        |  timeCreated:          $timeCreated
        |  amount:               $amount
        |}""".stripMargin
}

object Specimen {
  val usableState: EntityState = new EntityState("usable")
  val unusableState: EntityState = new EntityState("unusable")

  implicit val specimenWrites: Format[Specimen] = new Format[Specimen] {

      override def writes(specimen: Specimen): JsValue =
        ConcurrencySafeEntity.toJson(specimen) ++
        Json.obj(
          "state"                 -> specimen.state.id,
          "slug"                  -> specimen.slug,
          "inventoryId"           -> specimen.inventoryId,
          "specimenDefinitionId" -> specimen.specimenDefinitionId,
          "originLocationId"      -> specimen.originLocationId.id,
          "locationId"            -> specimen.locationId.id,
          "containerId"           -> specimen.containerId,
          "positionId"            -> specimen.positionId,
          "version"               -> specimen.version,
          "timeCreated"           -> specimen.timeCreated,
          "amount"                -> specimen.amount
        )

      override def reads(json: JsValue): JsResult[Specimen] = (json \ "state") match {
          case JsDefined(JsString(usableState.id)) => json.validate[UsableSpecimen]
          case JsDefined(JsString(unusableState.id))  => json.validate[UnusableSpecimen]
          case _ => JsError("error")
        }
    }

  implicit val usableSpecimenReads: Reads[UsableSpecimen] = Json.reads[UsableSpecimen]
  implicit val unusableSpecimenReads: Reads[UnusableSpecimen]   = Json.reads[UnusableSpecimen]

  val sort2Compare: Map[String, (Specimen, Specimen) => Boolean] =
    Map[String, (Specimen, Specimen) => Boolean](
      "inventoryId" -> compareByInventoryId,
      "timeCreated" -> compareByTimeCreated,
      "state"       -> compareByState)

  def compareById(a: Specimen, b: Specimen): Boolean =
    (a.id.id compareTo b.id.id) < 0

  def compareByInventoryId(a: Specimen, b: Specimen): Boolean =
    (a.inventoryId compareTo b.inventoryId) < 0

  def compareByTimeCreated(a: Specimen, b: Specimen): Boolean =
    (a.timeCreated compareTo b.timeCreated) < 0

  def compareByState(a: Specimen, b: Specimen): Boolean =
    (a.state.toString compareTo b.state.toString) < 0
}

trait SpecimenValidations {

  case object InventoryIdInvalid extends ValidationKey

  case object SpecimenDefinitionIdInvalid extends ValidationKey

}

/**
 * A usable specimen is a specimen that can be used for processing.
 */
final case class UsableSpecimen(id:                   SpecimenId,
                                version:              Long,
                                timeAdded:            OffsetDateTime,
                                timeModified:         Option[OffsetDateTime],
                                slug:                 Slug,
                                inventoryId:          String,
                                specimenDefinitionId: SpecimenDefinitionId,
                                originLocationId:     LocationId,
                                locationId:           LocationId,
                                containerId:          Option[ContainerId],
                                positionId:           Option[ContainerSchemaPositionId],
                                timeCreated:          OffsetDateTime,
                                amount:               BigDecimal)
    extends { val state: EntityState = Specimen.usableState }
    with HasSlug
    with Specimen
    with SpecimenValidations
    with ParticipantValidations
    with StudyValidations {

  import org.biobank.domain.DomainValidations._
  import org.biobank.CommonValidations._

  def withInventoryId(inventoryId: String): DomainValidation[Specimen] = {
    validateNonEmptyString(inventoryId, InventoryIdInvalid).map { s =>
      copy(slug         = Slug(inventoryId),
           inventoryId  = inventoryId,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  def withAmount(amount: BigDecimal): DomainValidation[Specimen] = {
    validatePositiveNumber(amount, AmountInvalid).map { s =>
      copy(amount       = amount,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  /**
   * Location should belong to a centre.
   */
  def withOriginLocation(id: LocationId): DomainValidation[Specimen] = {
    validateId(id, LocationIdInvalid).map { s =>
      copy(originLocationId = id,
           version          = version + 1,
           timeModified     = Some(OffsetDateTime.now))
    }
  }

  /**
   * Location should belong to a centre.
   */
  def withLocation(id: LocationId): DomainValidation[Specimen] = {
    validateId(id, LocationIdInvalid).map { s =>
      copy(locationId   = id,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  def withPosition(positionId: ContainerSchemaPositionId): DomainValidation[Specimen] = {
    validateId(positionId, PositionInvalid).map { s =>
      copy(positionId   = Some(positionId),
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  def makeUnusable(): DomainValidation[UnusableSpecimen] = {
    UnusableSpecimen(id                    = this.id,
                     version               = this.version + 1,
                     timeAdded             = this.timeAdded,
                     timeModified          = Some(OffsetDateTime.now),
                     slug                  = this.slug,
                     inventoryId           = this.inventoryId,
                     specimenDefinitionId = this.specimenDefinitionId,
                     originLocationId      = this.originLocationId,
                     locationId            = this.locationId,
                     containerId           = this.containerId,
                     positionId            = this.positionId,
                     timeCreated           = this.timeCreated,
                     amount                = this.amount).successNel[String]
  }
}

object UsableSpecimen
    extends SpecimenValidations
    with ParticipantValidations
    with StudyValidations {

  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  def create(id:                    SpecimenId,
             inventoryId:           String,
             specimenDefinitionId: SpecimenDefinitionId,
             version:               Long,
             originLocationId:      LocationId,
             locationId:            LocationId,
             containerId:           Option[ContainerId],
             positionId:            Option[ContainerSchemaPositionId],
             timeAdded:             OffsetDateTime,
             timeCreated:           OffsetDateTime,
             amount:                BigDecimal)
      : DomainValidation[UsableSpecimen] = {
    validate(id,
             inventoryId,
             specimenDefinitionId,
             version,
             originLocationId,
             locationId,
             containerId,
             positionId,
             amount)
      .map(_ => UsableSpecimen(id                    = id,
                               version               = version,
                               timeAdded             = timeAdded,
                               timeModified          = None,
                               slug                  = Slug(inventoryId),
                               inventoryId           = inventoryId,
                               specimenDefinitionId = specimenDefinitionId,
                               originLocationId      = originLocationId,
                               locationId            = locationId,
                               containerId           = containerId,
                               positionId            = positionId,
                               timeCreated           = timeCreated,
                               amount                = amount))
  }

  def validate(id:                    SpecimenId,
               inventoryId:           String,
               specimenDefinitionId: SpecimenDefinitionId,
               version:               Long,
               originLocationId:      LocationId,
               locationId:            LocationId,
               containerId:           Option[ContainerId],
               positionId:            Option[ContainerSchemaPositionId],
               amount:                BigDecimal)
      : DomainValidation[Boolean] = {
    (validateId(id) |@|
       validateNonEmptyString(inventoryId, InventoryIdInvalid) |@|
       validateId(specimenDefinitionId, SpecimenDefinitionIdInvalid) |@|
       validateVersion(version) |@|
       validateNonEmptyString(originLocationId.id, OriginLocationIdInvalid) |@|
       validateNonEmptyString(locationId.id, LocationIdInvalid) |@|
       validateIdOption(containerId, ContainerIdInvalid) |@|
       validateIdOption(positionId, PositionInvalid) |@|
       validatePositiveNumber(amount, AmountInvalid)) {
      case _ => true
    }
  }

}

/**
 * An Unusable specimen is a specimen that can no longer be used for processing.
 *
 * It may be that the total amount of the spcimen has already been used in processing.
 */
final case class UnusableSpecimen(id:                    SpecimenId,
                                  version:               Long,
                                  timeAdded:             OffsetDateTime,
                                  timeModified:          Option[OffsetDateTime],
                                  slug: Slug,
                                  inventoryId:           String,
                                  specimenDefinitionId: SpecimenDefinitionId,
                                  originLocationId:      LocationId,
                                  locationId:            LocationId,
                                  containerId:           Option[ContainerId],
                                  positionId:            Option[ContainerSchemaPositionId],
                                  timeCreated:           OffsetDateTime,
                                  amount:                BigDecimal)
    extends { val state: EntityState = Specimen.unusableState }
    with HasSlug
    with Specimen {

  def makeUsable(): DomainValidation[UsableSpecimen] = {
    UsableSpecimen(id                    = this.id,
                   version               = this.version + 1,
                   timeAdded             = this.timeAdded,
                   timeModified          = Some(OffsetDateTime.now),
                   slug                  = this.slug,
                   inventoryId           = this.inventoryId,
                   specimenDefinitionId = this.specimenDefinitionId,
                   originLocationId      = this.originLocationId,
                   locationId            = this.locationId,
                   containerId           = this.containerId,
                   positionId            = this.positionId,
                   timeCreated           = this.timeCreated,
                   amount                = this.amount).successNel[String]
  }
}
