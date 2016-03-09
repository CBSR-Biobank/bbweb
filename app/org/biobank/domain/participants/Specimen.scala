package org.biobank.domain.participants

import org.biobank.domain.{
  ConcurrencySafeEntity,
  DomainValidation
}
import org.biobank.domain.study.{ SpecimenGroupId, HasSpecimenGroupId, StudyValidations }
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
 * [[CollectionEvent]]. When a specimen is created it must be assigned the corresponding
 * [[study.SpecimenGroup]] defined in the [[study.Study]].
 */
sealed trait Specimen
    extends ConcurrencySafeEntity[SpecimenId]
    with HasSpecimenGroupId {

  /** The [[SpecimenGroup]] this specimen belongs to, defined by the study it belongs to. */
  val specimenGroupId: SpecimenGroupId

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

  /** The amount, in units specified in the [[SpecimenGroup]], for this specimen. */
  val amount: BigDecimal

  override def toString: String =
    s"""|${this.getClass.getSimpleName}: {
        |  specimenGroupId:  $specimenGroupId
        |  id:               $id
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

  implicit val specimenWrites = new Writes[Specimen] {
    def writes(specimen: Specimen) = Json.obj(
      "id"               -> specimen.id,
      "specimenGroupId"  -> specimen.specimenGroupId,
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
}

/**
 * A usable specimen is a specimen that can be used for processing.
 */
case class UsableSpecimen(id:               SpecimenId,
                          specimenGroupId:  SpecimenGroupId,
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
    with ParticipantValidations
    with StudyValidations {

  import org.biobank.domain.CommonValidations._

  def withAmount(amount: BigDecimal): DomainValidation[Specimen] = {
    validatePositiveNumber(amount, AmountInvalid) fold (
      err => err.failure,
      s   => copy(version = version + 1, amount = amount).success
    )
  }

  def withLocation(locationId: String): DomainValidation[Specimen] = {
    validateString(locationId, LocationIdInvalid) fold (
      err => err.failure,
      s   => copy(version = version + 1, locationId = locationId).success
    )
  }

  def withPosition(positionId: ContainerSchemaPositionId)
      : DomainValidation[Specimen] = {
    validateId(positionId, PositionInvalid) fold (
      err => err.failure,
      s   => copy(version = version + 1, positionId = Some(positionId)).success
    )
  }

  def makeUnusable(): DomainValidation[UnusableSpecimen] = {
    UnusableSpecimen(id               = this.id,
                     specimenGroupId  = this.specimenGroupId,
                     version          = this.version + 1,
                     timeAdded        = this.timeAdded,
                     timeModified     = this.timeModified,
                     originLocationId = this.originLocationId,
                     locationId       = this.locationId,
                     containerId      = this.containerId,
                     positionId       = this.positionId,
                     timeCreated      = this.timeCreated,
                     amount           = this.amount).success
  }
}

object UsableSpecimen extends ParticipantValidations with StudyValidations {
  import org.biobank.domain.CommonValidations._

  def create(id:               SpecimenId,
             specimenGroupId:  SpecimenGroupId,
             version:          Long,
             originLocationId: String,
             locationId:       String,
             containerId:      Option[ContainerId],
             positionId:       Option[ContainerSchemaPositionId],
             timeCreated:      DateTime,
             amount:           BigDecimal)
      : DomainValidation[UsableSpecimen] = {
    (validateId(id) |@|
       validateId(specimenGroupId, InvalidSpecimenGroupId) |@|
       validateVersion(version) |@|
       validateString(originLocationId, OriginLocationIdInvalid) |@|
       validateString(locationId, LocationIdInvalid) |@|
       validateId(containerId, ContainerIdInvalid) |@|
       validateId(positionId, PositionInvalid) |@|
       validatePositiveNumber(amount, AmountInvalid)) {
      case (_, _, _, _, _, _, _, _) => UsableSpecimen(id,
                                                      specimenGroupId,
                                                      version,
                                                      DateTime.now,
                                                      None,
                                                      originLocationId,
                                                      locationId,
                                                      containerId,
                                                      positionId,
                                                      timeCreated,
                                                      amount)
    }
  }

}

/**
 * A Unusable specimen is a specimen that can no longer be used for processing.
 *
 * It may be that the total amount of the spcimen has already been used in processing.
 */
case class UnusableSpecimen(id:               SpecimenId,
                            specimenGroupId:  SpecimenGroupId,
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
                   specimenGroupId  = this.specimenGroupId,
                   version          = this.version + 1,
                   timeAdded        = this.timeAdded,
                   timeModified     = this.timeModified,
                   originLocationId = this.originLocationId,
                   locationId       = this.locationId,
                   containerId      = this.containerId,
                   positionId       = this.positionId,
                   timeCreated      = this.timeCreated,
                   amount           = this.amount).success
  }
}
