package org.biobank.services

import org.biobank.domain._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * Functions that filter a set of studys from an expression contained in a filter string.
 *
 */
trait EntityFilter[T <: ConcurrencySafeEntity[_]] extends PredicateHelper {

  import org.biobank.services.Comparator._
  import org.biobank.services.QueryFilterParserGrammar._

  def filterEntities(entities: Set[T],
                     filter: FilterString,
                     filterFunc: (T => Boolean) => Set[T]):ServiceValidation[Set[T]] = {
    QueryFilterParser.expressions(filter).flatMap { filterExpression =>
      filterExpression match {
        case None =>
          entities.successNel[String]
        case Some(c: Comparison) =>
          comparisonToPredicates(c).map(filterFunc)
        case Some(e: AndExpression) =>
          comparisonToPredicates(e).map(filterFunc)
        case Some(e: OrExpression) =>
          comparisonToPredicates(e).map(filterFunc)
        case _ =>
          ServiceError(s"bad filter expression: $filterExpression").failureNel[Set[T]]
      }
    }
  }

  protected def predicateFromSelector(selector: String, comparator: Comparator, args: List[String])
      : ServiceValidation[T => Boolean]

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def comparisonToPredicates(expression: Expression): ServiceValidation[T => Boolean] = {
    expression match {
      case Comparison(selector, comparator, args) =>
        predicateFromSelector(selector, comparator, args)
      case AndExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => every(x:_*))
      case OrExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => any(x:_*))
      case _ =>
        ServiceError(s"invalid filter expression: $expression").failureNel[T => Boolean]
    }
  }

}
