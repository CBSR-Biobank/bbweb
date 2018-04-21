package org.biobank.services.centres

import org.biobank.services._
import org.biobank.services.Comparator._
import org.biobank.services.{ServiceValidation, ServiceError}
import org.biobank.domain.PredicateHelper
import org.biobank.domain.centres.{ShipmentItemState, ShipmentSpecimen, ShipmentSpecimenPredicates}
import org.slf4j.{Logger, LoggerFactory}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * Functions that filter a set of shipment specimens from an expression contained in a filter string.
 *
 */
object ShipmentSpecimenFilter
    extends EntityFilter[ShipmentSpecimen]
    with PredicateHelper
    with ShipmentSpecimenPredicates {
  import org.biobank.CommonValidations._

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def filterShipmentSpecimens(shipmentSpecimens: Set[ShipmentSpecimen],
                              filter:            FilterString):ServiceValidation[Set[ShipmentSpecimen]] = {
    filterEntities(shipmentSpecimens, filter, shipmentSpecimens.filter)
  }

  protected def predicateFromSelector(selector: String, comparator: Comparator, args: List[String])
      : ServiceValidation[ShipmentSpecimen => Boolean] = {
    selector match {
      case "state"          => stateFilter(comparator, args)
      case _ =>
        ServiceError(s"invalid filter selector: $selector").failureNel[ShipmentSpecimenFilter]
    }
  }

  private def stateFilter(comparator: Comparator, stateNames: List[String]) = {
    stateNames.
      map { str =>
        ShipmentItemState.values.find(_.toString == str).
          toSuccessNel(InvalidState(s"shipment specimen state does not exist: $str").toString)
      }.
      toList.
      sequenceU.
      flatMap { states =>
        val stateSet = states.toSet

        comparator match {
          case Equal | In =>
            stateIsOneOf(stateSet).successNel[String]
          case NotEqualTo | NotIn =>
            complement(stateIsOneOf(stateSet)).successNel[String]
          case _ =>
          ServiceError(s"invalid filter on state: $comparator").failureNel[ShipmentSpecimenFilter]
        }
      }
  }
}
