package org.biobank.service.centres

import org.biobank.service._
import org.biobank.service.Comparator._
import org.biobank.service.QueryFilterParserGrammar._
import org.biobank.service.{ServiceValidation, ServiceError}
import org.biobank.domain.PredicateHelper
import org.biobank.domain.centre.{ShipmentItemState, ShipmentSpecimen, ShipmentSpecimenPredicates}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._
import org.slf4j.LoggerFactory

/**
 * Functions that filter a set of shipment specimens from an expression contained in a filter string.
 *
 */
object ShipmentSpecimenFilter extends PredicateHelper with ShipmentSpecimenPredicates {
  import org.biobank.CommonValidations._

  val log = LoggerFactory.getLogger(this.getClass)

  def filterShipmentSpecimens(shipmentSpecimens: Set[ShipmentSpecimen],
                              filter:            FilterString):ServiceValidation[Set[ShipmentSpecimen]] = {
    QueryFilterParser.expressions(filter).flatMap { filterExpression =>
      filterExpression match {
        case None =>
          shipmentSpecimens.successNel[String]
        case Some(c: Comparison) =>
          comparisonToPredicates(c).map(shipmentSpecimens.filter)
        case Some(e: AndExpression) =>
          comparisonToPredicates(e).map(shipmentSpecimens.filter)
        case Some(e: OrExpression) =>
          comparisonToPredicates(e).map(shipmentSpecimens.filter)
        case _ =>
          ServiceError(s"bad filter expression: $filterExpression").failureNel[Set[ShipmentSpecimen]]
      }
    }
  }

  def comparisonToPredicates(expression: Expression): ServiceValidation[ShipmentSpecimenFilter] = {
    expression match {
      case Comparison(selector, comparator, args) =>
        selector match {
          case "state"          => stateFilter(comparator, args)
          case _ =>
            ServiceError(s"invalid filter selector: $selector").failureNel[ShipmentSpecimenFilter]
        }
      case AndExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => every(x:_*))
      case OrExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => any(x:_*))
      case _ =>
        ServiceError(s"invalid filter expression: $expression").failureNel[ShipmentSpecimenFilter]
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
