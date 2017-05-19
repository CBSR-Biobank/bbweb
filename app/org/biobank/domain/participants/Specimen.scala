package org.biobank.domain.participants

import org.biobank.ValidationKey
import org.biobank.dto.{CentreLocationInfo, SpecimenDto}
import org.biobank.domain._
import org.biobank.domain.containers.{ContainerId, ContainerSchemaPositionId}
import org.biobank.domain.study.{CollectionSpecimenDescription, SpecimenDescriptionId, StudyValidations}
import org.biobank.domain.{ConcurrencySafeEntity, DomainValidation, Location}
import org.biobank.infrastructure.EnumUtils._
import org.joda.time.DateTime
import play.api.libs.json._
import scalaz.Scalaz._

/**
 * Represents something that was obtained from a [[Participant]] in a [[study.Study]].
 *
 * A Specimen collected from a [[Participant]] can be created with this aggregate and then added to a
 * [[CollectionEvent]]. When a specimen is created it must be assigned the corresponding [[SpecimenDescription]]
 * defined in either the [[CollectionEvent]] or the specimen link type to which it corresponds .
 */
sealed trait Specimen
    extends ConcurrencySafeEntity[SpecimenId] {

  val state: EntityState

  /** The inventory ID assigned to this specimen. */
  val inventoryId: String

  /** The [[CollectionSpecimenDescription]] this specimen belongs to, defined by the study it belongs to. */
  val specimenDescriptionId: SpecimenDescriptionId

  /** The [[Centre]] where this specimen was created. */
  val originLocationId: LocationId

  /** The [[Centre]] where this specimen is currently located. */
  val locationId: LocationId

  /** The [[Container]] this specimen is stored in. */
  val containerId: Option[ContainerId]

  /** The [[ContainerSchemaPosition]] (i.e. position or label) this specimen has in its container. . */
  val positionId: Option[ContainerSchemaPositionId]

  /**
   * The date and time when the specimen was physically created.
   *
   * Not necessarily when this specimen was added to the application.
   */
  val timeCreated: DateTime

  /** The amount, in units specified in the [[SpecimenDescription]], for this specimen. */
  val amount: scala.math.BigDecimal

  def createDto(collectionEvent:    CollectionEvent,
                specimenDesc:       CollectionSpecimenDescription,
                originLocationInfo: CentreLocationInfo,
                locationInfo:       CentreLocationInfo): SpecimenDto =
    SpecimenDto(id                    = this.id.id,
                state                 = this.state,
                inventoryId           = this.inventoryId,
                collectionEventId     = collectionEvent.id.id,
                specimenDescriptionId = this.specimenDescriptionId.id,
                specimenSpecName      = specimenDesc.name,
                version               = this.version,
                timeAdded             = this.timeAdded,
                timeModified          = this.timeModified,
                originLocationInfo    = originLocationInfo,
                locationInfo          = locationInfo,
                containerId           = this.containerId.map(_.id),
                positionId            = this.positionId.map(_.id),
                timeCreated           = this.timeCreated,
                amount                = this.amount,
                units                 = specimenDesc.units)

  override def toString: String =
    s"""|${this.getClass.getSimpleName}: {
        |  id:                    $id
        |  inventoryId:           $inventoryId
        |  specimenDescriptionId: $specimenDescriptionId
        |  version:               $version
        |  timeAdded:             $timeAdded
        |  timeModified:          $timeModified
        |  originLocationId:      $originLocationId
        |  locationId:            $locationId
        |  containerId:           $containerId
        |  positionId:            $positionId
        |  timeCreated:           $timeCreated
        |  amount:                $amount
        |}""".stripMargin
}

object Specimen {
  import org.biobank.infrastructure.JsonUtils._

  val usableState: EntityState = new EntityState("usable")
  val unusableState: EntityState = new EntityState("unusable")

  implicit val specimenWrites: Format[Specimen] = new Format[Specimen] {

      override def writes(specimen: Specimen): JsValue =
        ConcurrencySafeEntity.toJson(specimen) ++
        Json.obj(
          "state"                 -> specimen.state.id,
          "inventoryId"           -> specimen.inventoryId,
          "specimenDescriptionId" -> specimen.specimenDescriptionId,
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

  case object SpecimenDescriptionIdInvalid extends ValidationKey

}

/**
 * A usable specimen is a specimen that can be used for processing.
 */
final case class UsableSpecimen(id:                    SpecimenId,
                                inventoryId:           String,
                                specimenDescriptionId: SpecimenDescriptionId,
                                version:               Long,
                                timeAdded:             DateTime,
                                timeModified:          Option[DateTime],
                                originLocationId:      LocationId,
                                locationId:            LocationId,
                                containerId:           Option[ContainerId],
                                positionId:            Option[ContainerSchemaPositionId],
                                timeCreated:           DateTime,
                                amount:                BigDecimal)
    extends { val state: EntityState = Specimen.usableState }
    with Specimen
    with SpecimenValidations
    with ParticipantValidations
    with StudyValidations {

  import org.biobank.domain.CommonValidations._
  import org.biobank.CommonValidations._

  def withInventoryId(inventoryId: String): DomainValidation[Specimen] = {
    validateString(inventoryId, InventoryIdInvalid).map { s =>
      copy(inventoryId  = inventoryId,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  def withAmount(amount: BigDecimal): DomainValidation[Specimen] = {
    validatePositiveNumber(amount, AmountInvalid).map { s =>
      copy(amount       = amount,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  /**
   * Location should belong to a centre.
   */
  def withOriginLocation(location: Location): DomainValidation[Specimen] = {
    validateString(location.uniqueId.id, LocationIdInvalid).map { s =>
      copy(originLocationId = location.uniqueId,
           version          = version + 1,
           timeModified     = Some(DateTime.now))
    }
  }

  /**
   * Location should belong to a centre.
   */
  def withLocation(location: Location): DomainValidation[Specimen] = {
    validateString(location.uniqueId.id, LocationIdInvalid).map { s =>
      copy(locationId   = location.uniqueId,
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  def withPosition(positionId: ContainerSchemaPositionId): DomainValidation[Specimen] = {
    validateId(positionId, PositionInvalid).map { s =>
      copy(positionId   = Some(positionId),
           version      = version + 1,
           timeModified = Some(DateTime.now))
    }
  }

  def makeUnusable(): DomainValidation[UnusableSpecimen] = {
    UnusableSpecimen(id                    = this.id,
                     inventoryId           = this.inventoryId,
                     specimenDescriptionId = this.specimenDescriptionId,
                     version               = this.version + 1,
                     timeAdded             = this.timeAdded,
                     timeModified          = Some(DateTime.now),
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
  import org.biobank.domain.CommonValidations._

  def create(id:                    SpecimenId,
             inventoryId:           String,
             specimenDescriptionId: SpecimenDescriptionId,
             version:               Long,
             originLocationId:      LocationId,
             locationId:            LocationId,
             containerId:           Option[ContainerId],
             positionId:            Option[ContainerSchemaPositionId],
             timeAdded:             DateTime,
             timeCreated:           DateTime,
             amount:                BigDecimal)
      : DomainValidation[UsableSpecimen] = {
    validate(id,
             inventoryId,
             specimenDescriptionId,
             version,
             originLocationId,
             locationId,
             containerId,
             positionId,
             timeCreated,
             amount)
      .map(_ => UsableSpecimen(id,
                               inventoryId,
                               specimenDescriptionId,
                               version,
                               timeAdded,
                               None,
                               originLocationId,
                               locationId,
                               containerId,
                               positionId,
                               timeCreated,
                               amount))
  }

  def validate(id:                    SpecimenId,
               inventoryId:           String,
               specimenDescriptionId: SpecimenDescriptionId,
               version:               Long,
               originLocationId:      LocationId,
               locationId:            LocationId,
               containerId:           Option[ContainerId],
               positionId:            Option[ContainerSchemaPositionId],
               timeCreated:           DateTime,
               amount:                BigDecimal)
      : DomainValidation[Boolean] = {
    (validateId(id) |@|
       validateString(inventoryId, InventoryIdInvalid) |@|
       validateId(specimenDescriptionId, SpecimenDescriptionIdInvalid) |@|
       validateVersion(version) |@|
       validateString(originLocationId.id, OriginLocationIdInvalid) |@|
       validateString(locationId.id, LocationIdInvalid) |@|
       validateId(containerId, ContainerIdInvalid) |@|
       validateId(positionId, PositionInvalid) |@|
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
                                  inventoryId:           String,
                                  specimenDescriptionId: SpecimenDescriptionId,
                                  version:               Long,
                                  timeAdded:             DateTime,
                                  timeModified:          Option[DateTime],
                                  originLocationId:      LocationId,
                                  locationId:            LocationId,
                                  containerId:           Option[ContainerId],
                                  positionId:            Option[ContainerSchemaPositionId],
                                  timeCreated:           DateTime,
                                  amount:                BigDecimal)
    extends { val state: EntityState = Specimen.unusableState }
    with Specimen {

  def makeUsable(): DomainValidation[UsableSpecimen] = {
    UsableSpecimen(id                    = this.id,
                   inventoryId           = this.inventoryId,
                   specimenDescriptionId = this.specimenDescriptionId,
                   version               = this.version + 1,
                   timeAdded             = this.timeAdded,
                   timeModified          = Some(DateTime.now),
                   originLocationId      = this.originLocationId,
                   locationId            = this.locationId,
                   containerId           = this.containerId,
                   positionId            = this.positionId,
                   timeCreated           = this.timeCreated,
                   amount                = this.amount).successNel[String]
  }
}
