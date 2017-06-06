package org.biobank.service.centres

import com.google.inject.ImplementedBy
import javax.inject.Inject
import org.biobank.service._
import org.biobank.service.Comparator._
import org.biobank.service.QueryFilterParserGrammar._
import org.biobank.service.{ServiceValidation, ServiceError}
import org.biobank.domain.PredicateHelper
import org.biobank.domain.centre._
import org.slf4j.{Logger, LoggerFactory}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[ShipmentFilterImpl])
trait ShipmentFilter {

  def filterShipments(shipments: Set[Shipment], filter: FilterString):ServiceValidation[Set[Shipment]];

}

/**
 * Functions that filter a set of shipments from an expression contained in a filter string.
 *
 */
class ShipmentFilterImpl @Inject() (val shipmentRepository: ShipmentRepository,
                                    val centreRepository:   CentreRepository)
 extends ShipmentFilter with PredicateHelper with ShipmentPredicates {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def filterShipments(shipments: Set[Shipment], filter: FilterString): ServiceValidation[Set[Shipment]] = {
    QueryFilterParser.expressions(filter).flatMap { filterExpression =>
      filterExpression match {
        case None =>
          shipments.successNel[String]
        case Some(c: Comparison) =>
          comparisonToPredicates(c).map(shipments.filter)
        case Some(e: AndExpression) =>
          comparisonToPredicates(e).map(shipments.filter)
        case Some(e: OrExpression) =>
          comparisonToPredicates(e).map(shipments.filter)
        case _ =>
          ServiceError(s"bad filter expression: $filterExpression").failureNel[Set[Shipment]]
      }
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def comparisonToPredicates(expression: Expression): ServiceValidation[ShipmentFilter] = {
    expression match {
      case Comparison(selector, comparator, args) =>
        selector match {
          case "fromCentre"     => fromCentreFilter(comparator, args)
          case "toCentre"       => toCentreFilter(comparator, args)
          case "withCentre"     => withCentreFilter(comparator, args)
          case "courierName"    => courierNameFilter(comparator, args)
          case "trackingNumber" => trackingNumberFilter(comparator, args)
          case "state"          => stateFilter(comparator, args)
          case _ =>
            ServiceError(s"invalid filter selector: $selector").failureNel[ShipmentFilter]
        }
      case AndExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => every(x:_*))
      case OrExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => any(x:_*))
      case _ =>
        ServiceError(s"invalid filter expression: $expression").failureNel[ShipmentFilter]
    }
  }

  private def fromCentreFilter(comparator: Comparator, names: List[String]) = {
    centreIdsFilter(comparator,
                    fromCentreIdIsOneOf,
                    names,
                    ServiceError(s"invalid filter on 'from centre' name: $comparator"))
  }

  private def toCentreFilter(comparator: Comparator, names: List[String]) = {
    centreIdsFilter(comparator,
                    toCentreIdIsOneOf,
                    names,
                    ServiceError(s"invalid filter on 'to centre' name: $comparator"))
  }

  private def withCentreFilter(comparator: Comparator, names: List[String]) = {
    centreIdsFilter(comparator,
                    withCentreIdIsOneOf,
                    names,
                    ServiceError(s"invalid filter on 'with centre' name: $comparator"))
  }

  private def centreIdsFilter(comparator:     Comparator,
                              shipmentFilter: Set[CentreId] => ShipmentFilter,
                              names:          List[String],
                              error:          ServiceError) = {
    val centreIds = centreRepository.getByNames(names.toSet).map(_.id)

    comparator match {
      case Equal | In =>
        shipmentFilter(centreIds).successNel[String]
      case NotEqualTo | NotIn =>
        complement(shipmentFilter(centreIds)).successNel[String]
      case _ =>
        error.failureNel[ShipmentFilter]
    }
  }

  private def courierNameFilter(comparator: Comparator, names: List[String]) = {
    val nameSet = names.toSet
    comparator match {
      case Equal | In =>
        courierNameIsOneOf(nameSet).successNel[String]
      case NotEqualTo | NotIn =>
        complement(courierNameIsOneOf(nameSet)).successNel[String]
      case Like =>
        courierNameIsLike(nameSet).successNel[String]
      case _ =>
        ServiceError(s"invalid filter on courier name: $comparator").failureNel[ShipmentFilter]
    }
  }

  private def trackingNumberFilter(comparator: Comparator, trackingNumbers: List[String]) = {
    val trackingNumberSet = trackingNumbers.toSet
    comparator match {
      case Equal | In =>
        trackingNumberIsOneOf(trackingNumberSet).successNel[String]
      case NotEqualTo | NotIn =>
        complement(trackingNumberIsOneOf(trackingNumberSet)).successNel[String]
      case Like =>
        trackingNumberIsLike(trackingNumberSet).successNel[String]
      case _ =>
        ServiceError(s"invalid filter on tracking number: $comparator").failureNel[ShipmentFilter]
    }
  }

  private def stateFilter(comparator: Comparator, stateNames: List[String]) = {
    stateNames.
      map { str =>
        Shipment.shipmentStates.find(_.id == str).toSuccessNel(s"shipment state does not exist: $str")
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
          ServiceError(s"invalid filter on state: $comparator").failureNel[ShipmentFilter]
        }
      }
  }
}
