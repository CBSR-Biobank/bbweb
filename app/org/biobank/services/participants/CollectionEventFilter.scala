package org.biobank.service.participants

import org.biobank.domain.participants.{CollectionEvent, CollectionEventPredicates}
import org.biobank.domain.PredicateHelper
import org.biobank.service._
import org.biobank.service.Comparator._
import org.biobank.service.QueryFilterParserGrammar._
import org.biobank.service.{ServiceValidation, ServiceError}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * Functions that filter a set of studys from an expression contained in a filter string.
 *
 */
object CollectionEventFilter extends PredicateHelper with CollectionEventPredicates {

  def filterCollectionEvents(cevents: Set[CollectionEvent], filter: FilterString):
      ServiceValidation[Set[CollectionEvent]] = {
    QueryFilterParser.expressions(filter).flatMap { filterExpression =>
      filterExpression match {
        case None =>
          cevents.successNel[String]
        case Some(c: Comparison) =>
          comparisonToPredicates(c).map(cevents.filter)
        case Some(e: AndExpression) =>
          comparisonToPredicates(e).map(cevents.filter)
        case Some(e: OrExpression) =>
          comparisonToPredicates(e).map(cevents.filter)
        case _ =>
          ServiceError(s"bad filter expression: $filterExpression").failureNel[Set[CollectionEvent]]
      }
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def comparisonToPredicates(expression: Expression): ServiceValidation[CollectionEventFilter] = {
    expression match {
      case Comparison(selector, comparator, args) =>
        selector match {
          case "visitNumber" => visitNumberFilter(comparator, args)
          case _ => ServiceError(s"invalid filter selector: $selector").failureNel[CollectionEventFilter]
        }
      case AndExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => every(x:_*))
      case OrExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => any(x:_*))
      case _ =>
        ServiceError(s"invalid filter expression: $expression").failureNel[CollectionEventFilter]
    }
  }

  private def visitNumberFilter(comparator: Comparator, visitNumbers: List[String]) = {
    val visitNumberSet = visitNumbers.toSet
    comparator match {
      case Equal | In =>
        visitNumberIsOneOf(visitNumberSet).successNel[String]
      case NotEqualTo | NotIn =>
        complement(visitNumberIsOneOf(visitNumberSet)).successNel[String]
      case _ =>
        ServiceError(s"invalid filter on courier name: $comparator").failureNel[CollectionEventFilter]
    }
  }

}
