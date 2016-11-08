package org.biobank.service.centres

import org.biobank.service.{QueryFilterParser, QueryFilterParserGrammar}
import org.biobank.service.Comparator._
import org.biobank.service.QueryFilterParserGrammar._
import org.biobank.domain.centre.ShipmentState
import org.biobank.service.{ServiceValidation, ServiceError}
import org.biobank.domain.PredicateHelper
import org.biobank.domain.centre.ShipmentPredicates
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * Functions that convert QueryFilterParser expressions to predicates that can be used to filter a collection
 * of Shipments.
 *
 */
trait ShipmentPredicateConverter extends PredicateHelper with ShipmentPredicates {

  def parseFilter(filter: String):
      ServiceValidation[Option[QueryFilterParserGrammar.Expression]] = {
    if (filter.trim.isEmpty) {
        None.successNel[String]
    } else {
      val parseResult = QueryFilterParser(filter)
      if (parseResult.isEmpty) {
        s"could not parse filter expression: $filter".failureNel[Option[Expression]]
      } else {
        parseResult.successNel[String]
      }
    }
  }

  def comparisonToPredicates(expression: Expression): ServiceValidation[ShipmentFilter] = {
    expression match {
      case Comparison(selector, comparator, args) =>
        selector match {
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

  private def courierNameFilter(comparator: Comparator, names: List[String]) = {
    val nameSet = names.toSet
    comparator match {
      case Equal | In =>
        courierNameIsOneOf(nameSet).successNel[String]
      case NotEqualTo | NotIn =>
        complement(courierNameIsOneOf(nameSet)).successNel[String]
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
      case _ =>
        ServiceError(s"invalid filter on tracking number: $comparator").failureNel[ShipmentFilter]
    }
  }

  private def stateFilter(comparator: Comparator, stateNames: List[String]) = {
    stateNames.
      map { str =>
        ShipmentState.values.find(_.toString == str).toSuccessNel(s"shipment state does not exist: $str")
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
