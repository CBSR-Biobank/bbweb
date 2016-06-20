package org.biobank.domain.participants

import org.biobank.ValidationKey
import org.biobank.domain.{
  ConcurrencySafeEntity,
  DomainValidation,
  Location
}
import org.biobank.domain.study.StudyValidations
import org.biobank.domain.containers.{
  ContainerId,
  ContainerSchemaPositionId
}

import org.joda.time.DateTime
import play.api.libs.json._
import scalaz.Scalaz._

/**
 * Represents something that was obtained from a [[Participant]] in a [[study.Study]].
 *
 * A Specimen collected from a [[Participant]] can be created with this aggregate and then added to a
 * [[CollectionEvent]]. When a specimen is created it must be assigned the corresponding [[SpecimenSpec]]
 * defined in either the [[CollectionEvent]] or the specimen link type to which it corresponds .
 */
sealed trait Specimen
    extends ConcurrencySafeEntity[SpecimenId] {

  /** The inventory ID assigned to this specimen. */
  val inventoryId: String

  /** The [[CollectionSpecimenSpec]] this specimen belongs to, defined by the study it belongs to. */
  val specimenSpecId: String

  /** The [[Centre]] where this specimen was created. */
  val originLocationId: String

  /** The [[Centre]] where this specimen is currently located. */
  val locationId: String

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

  /** The amount, in units specified in the [[SpecimenSpec]], for this specimen. */
  val amount: scala.math.BigDecimal

  override def toString: String =
    s"""|${this.getClass.getSimpleName}: {
        |  id:               $id
        |  inventoryId:      $inventoryId
        |  specimenSpecId:   $specimenSpecId
        |  version:          $version
        |  timeAdded:        $timeAdded
        |  timeModified:     $timeModified
        |  originLocationId: $originLocationId
        |  locationId:       $locationId
        |  containerId:      $containerId
        |  positionId:       $positionId
        |  timeCreated:      $timeCreated
        |  amount:           $amount
        |}""".stripMargin
}

object Specimen {
  import org.biobank.infrastructure.JsonUtils._

  implicit val specimenWrites = new Writes[Specimen] {
      def writes(specimen: Specimen) = Json.obj(
          "id"               -> specimen.id,
          "inventoryId"      -> specimen.inventoryId,
          "specimenSpecId"   -> specimen.specimenSpecId,
          "originLocationId" -> specimen.originLocationId,
          "locationId"       -> specimen.locationId,
          "containerId"      -> specimen.containerId,
          "positionId"       -> specimen.positionId,
          "version"          -> specimen.version,
          "timeAdded"        -> specimen.timeAdded,
          "timeModified"     -> specimen.timeModified,
          "timeCreated"      -> specimen.timeCreated,
          "amount"           -> specimen.amount,
          "status"           -> specimen.getClass.getSimpleName
        )
    }

  def compareById(a: Specimen, b: Specimen) =
    (a.id.id compareTo b.id.id) < 0

  def compareByInventoryId(a: Specimen, b: Specimen) =
    (a.inventoryId compareTo b.inventoryId) < 0

  def compareByTimeCreated(a: Specimen, b: Specimen) =
    (a.timeCreated compareTo b.timeCreated) < 0

  def compareByStatus(a: Specimen, b: Specimen) =
    (a.getClass.getSimpleName compareTo b.getClass.getSimpleName) < 0

}

trait SpecimenValidations {

  case object InventoryIdInvalid extends ValidationKey

  case object SpecimenSpecIdInvalid extends ValidationKey

}

/**
 * A usable specimen is a specimen that can be used for processing.
 */
case class UsableSpecimen(id:               SpecimenId,
                          inventoryId:      String,
                          specimenSpecId:   String,
                          version:          Long,
                          timeAdded:        DateTime,
                          timeModified:     Option[DateTime],
                          originLocationId: String,
                          locationId:       String,
                          containerId:      Option[ContainerId],
                          positionId:       Option[ContainerSchemaPositionId],
                          timeCreated:      DateTime,
                          amount:           BigDecimal)
    extends Specimen
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
    validateString(location.uniqueId, LocationIdInvalid).map { s =>
      copy(originLocationId = location.uniqueId,
           version          = version + 1,
           timeModified     = Some(DateTime.now))
    }
  }

  /**
   * Location should belong to a centre.
   */
  def withLocation(location: Location): DomainValidation[Specimen] = {
    validateString(location.uniqueId, LocationIdInvalid).map { s =>
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
    UnusableSpecimen(id               = this.id,
                     inventoryId      = this.inventoryId,
                     specimenSpecId   = this.specimenSpecId,
                     version          = this.version + 1,
                     timeAdded        = this.timeAdded,
                     timeModified     = Some(DateTime.now),
                     originLocationId = this.originLocationId,
                     locationId       = this.locationId,
                     containerId      = this.containerId,
                     positionId       = this.positionId,
                     timeCreated      = this.timeCreated,
                     amount           = this.amount).success
  }
}

object UsableSpecimen
    extends SpecimenValidations
    with ParticipantValidations
    with StudyValidations {

  import org.biobank.CommonValidations._
  import org.biobank.domain.CommonValidations._

  def create(id:               SpecimenId,
             inventoryId:      String,
             specimenSpecId:   String,
             version:          Long,
             originLocationId: String,
             locationId:       String,
             containerId:      Option[ContainerId],
             positionId:       Option[ContainerSchemaPositionId],
             timeCreated:      DateTime,
             amount:           BigDecimal)
      : DomainValidation[UsableSpecimen] = {
    validate(id,
             inventoryId,
             specimenSpecId,
             version,
             originLocationId,
             locationId,
             containerId,
             positionId,
             timeCreated,
             amount)
      .map(_ => UsableSpecimen(id,
                               inventoryId,
                               specimenSpecId,
                               version,
                               DateTime.now,
                               None,
                               originLocationId,
                               locationId,
                               containerId,
                               positionId,
                               timeCreated,
                               amount))
  }

  def validate(id:               SpecimenId,
               inventoryId:      String,
               specimenSpecId:   String,
               version:          Long,
               originLocationId: String,
               locationId:       String,
               containerId:      Option[ContainerId],
               positionId:       Option[ContainerSchemaPositionId],
               timeCreated:      DateTime,
               amount:           BigDecimal)
      : DomainValidation[Boolean] = {
    (validateId(id) |@|
       validateString(inventoryId, InventoryIdInvalid) |@|
       validateString(specimenSpecId, SpecimenSpecIdInvalid) |@|
       validateVersion(version) |@|
       validateString(originLocationId, OriginLocationIdInvalid) |@|
       validateString(locationId, LocationIdInvalid) |@|
       validateId(containerId, ContainerIdInvalid) |@|
       validateId(positionId, PositionInvalid) |@|
       validatePositiveNumber(amount, AmountInvalid)) {
      case (_, _, _, _, _, _, _, _, _) => true
    }
  }

}

/**
 * An Unusable specimen is a specimen that can no longer be used for processing.
 *
 * It may be that the total amount of the spcimen has already been used in processing.
 */
case class UnusableSpecimen(id:               SpecimenId,
                            inventoryId:      String,
                            specimenSpecId:   String,
                            version:          Long,
                            timeAdded:        DateTime,
                            timeModified:     Option[DateTime],
                            originLocationId: String,
                            locationId:       String,
                            containerId:      Option[ContainerId],
                            positionId:       Option[ContainerSchemaPositionId],
                            timeCreated:      DateTime,
                            amount:           BigDecimal)
    extends Specimen {

  def makeUsable(): DomainValidation[UsableSpecimen] = {
    UsableSpecimen(id               = this.id,
                   inventoryId      = this.inventoryId,
                   specimenSpecId   = this.specimenSpecId,
                   version          = this.version + 1,
                   timeAdded        = this.timeAdded,
                   timeModified     = Some(DateTime.now),
                   originLocationId = this.originLocationId,
                   locationId       = this.locationId,
                   containerId      = this.containerId,
                   positionId       = this.positionId,
                   timeCreated      = this.timeCreated,
                   amount           = this.amount).success
  }
}
