package org.biobank.services.studies

import org.biobank.domain.studies.{CollectionEventType, CollectionEventTypePredicates}
import org.biobank.services._
import org.biobank.services.QueryFilterParserGrammar._
import org.biobank.services.{ServiceValidation, ServiceError}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * Functions that filter a set of studys from an expression contained in a filter string.
 *
 */
object CollectionEventTypeFilter
    extends EntityNameFilter[CollectionEventType]
    with CollectionEventTypePredicates {

  def filterCollectionEvents(ceventTypes: Set[CollectionEventType], filter: FilterString):
      ServiceValidation[Set[CollectionEventType]] = {
    QueryFilterParser.expressions(filter).flatMap { filterExpression =>
      filterExpression match {
        case None =>
          ceventTypes.successNel[String]
        case Some(c: Comparison) =>
          comparisonToPredicates(c).map(ceventTypes.filter)
        case Some(e: AndExpression) =>
          comparisonToPredicates(e).map(ceventTypes.filter)
        case Some(e: OrExpression) =>
          comparisonToPredicates(e).map(ceventTypes.filter)
        case _ =>
          ServiceError(s"bad filter expression: $filterExpression").failureNel[Set[CollectionEventType]]
      }
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def comparisonToPredicates(expression: Expression): ServiceValidation[CollectionEventTypeFilter] = {
    expression match {
      case Comparison(selector, comparator, args) =>
        selector match {
          case "name" => nameFilter(comparator, args)
          case _ => ServiceError(s"invalid filter selector: $selector").failureNel[CollectionEventTypeFilter]
        }
      case AndExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => every(x:_*))
      case OrExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => any(x:_*))
      case _ =>
        ServiceError(s"invalid filter expression: $expression").failureNel[CollectionEventTypeFilter]
    }
  }

}
