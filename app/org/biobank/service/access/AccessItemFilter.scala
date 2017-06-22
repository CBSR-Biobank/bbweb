package org.biobank.service.access

import org.biobank.domain.access._
import org.biobank.service._
import org.biobank.service.QueryFilterParserGrammar._
import org.biobank.service.{ServiceValidation, ServiceError}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * Functions that filter a set of accessItems from an expression contained in a filter string.
 *
 */
object AccessItemFilter
    extends EntityNameFilter[AccessItem]
    with AccessItemPredicates {

  def filterAccessItems[T <: AccessItem](accessItems: Set[T], filter: FilterString)
      :ServiceValidation[Set[T]] = {
    QueryFilterParser.expressions(filter).flatMap { filterExpression =>
      filterExpression match {
        case None =>
          accessItems.successNel[String]
        case Some(c: Comparison) =>
          comparisonToPredicates(c).map(accessItems.filter)
        case Some(e: AndExpression) =>
          comparisonToPredicates(e).map(accessItems.filter)
        case Some(e: OrExpression) =>
          comparisonToPredicates(e).map(accessItems.filter)
        case _ =>
          ServiceError(s"bad filter expression: $filterExpression").failureNel[Set[T]]
      }
    }
  }

  def filterRoles(roles: Set[Role], filter: FilterString): ServiceValidation[Set[Role]] = {
    filterAccessItems(roles, filter)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def comparisonToPredicates(expression: Expression): ServiceValidation[AccessItemFilter] = {
    expression match {
      case Comparison(selector, comparator, args) =>
        selector match {
          case "name"  => nameFilter(comparator, args)
          case _ =>
            ServiceError(s"invalid filter selector: $selector").failureNel[AccessItemFilter]
        }
      case AndExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => every(x:_*))
      case OrExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => any(x:_*))
      case _ =>
        ServiceError(s"invalid filter expression: $expression").failureNel[AccessItemFilter]
    }
  }

}
