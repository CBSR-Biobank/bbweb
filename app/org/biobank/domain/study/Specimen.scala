package org.biobank.domain.study

import org.biobank.domain.{
  ConcurrencySafeEntity,
  DomainValidation,
  ValidationKey
}
import org.biobank.domain.{ ContainerId, LocationId }
import org.biobank.infrastructure.JsonUtils._

import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import scalaz._
import scalaz.Scalaz._

trait SpecimenValidations {
  case object AmountInvalid extends ValidationKey
  case object LocationIdInvalid extends ValidationKey
  case object ContainerIdInvalid extends ValidationKey
  case object PositionInvalid extends ValidationKey
}

/**
 * The subject for which a set of specimens were collected from. The subject can be human or non human.
 * A specimen belongs to a single study.
 *
 * @param uniqueId A specimen has a unique identifier that is used to identify the specimen in
 *        the system. This identifier is not the same as the SpecimenId value object
 *        used by the domain model.
 *
 * FIXME: change type of position to 'ContainerSchemaPosition'
 *
 */
case class Specimen(specimenGroupId:  SpecimenGroupId,
                    id:               SpecimenId,
                    version:          Long,
                    timeAdded:        DateTime,
                    timeModified:     Option[DateTime],
                    timeCreated:      DateTime,
                    amount:           BigDecimal,
                    originLocationId: LocationId,
                    locationId:       LocationId,
                    containerId:      Option[ContainerId],
                    position:         Option[String],
                    usable:           Boolean)
    extends ConcurrencySafeEntity[SpecimenId]
    with HasSpecimenGroupId {

  override def toString: String =
    s"""|Specimen:{
        |  specimenGroupId:  $specimenGroupId
        |  id:               $id
        |  version:          $version
        |  timeAdded:        $timeAdded
        |  timeModified:     $timeModified
        |  timeCreated:      $timeCreated
        |  amount:           $amount
        |  originLocationId: $originLocationId
        |  locationId:       $locationId
        |  containerId:      $containerId
        |  position:         $position
        |  usable:           $usable
        |}""".stripMargin
}

object Specimen extends SpecimenValidations with StudyValidations {
  import org.biobank.domain.CommonValidations._

  def create(specimenGroupId:  SpecimenGroupId,
             id:               SpecimenId,
             version:          Long,
             dateTime:         DateTime,
             timeCreated:      DateTime,
             amount:           BigDecimal,
             originLocationId: LocationId,
             locationId:       LocationId,
             containerId:      Option[ContainerId],
             position:         Option[String],
             usable:           Boolean)
      : DomainValidation[Specimen] = {
    (validateId(specimenGroupId, InvalidSpecimenGroupId) |@|
      validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validatePositiveNumber(amount, AmountInvalid) |@|
      validateId(originLocationId, LocationIdInvalid) |@|
      validateId(locationId, LocationIdInvalid) |@|
      validateId(containerId, ContainerIdInvalid) |@|
      validateNonEmptyOption(position, PositionInvalid)) {
      Specimen(_, _, _, dateTime, None, timeCreated, _, _, _, _, _, usable)
    }
  }

  implicit val specimenWrites = Json.writes[Specimen]

}
