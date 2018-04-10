package org.biobank.service.access

import org.biobank.domain.access._
import org.biobank.service._
import org.biobank.service.QueryFilterParserGrammar._
import org.biobank.service.{ServiceValidation, ServiceError}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * Functions that filter a set of memberships from an expression contained in a filter string.
 *
 */
object MembershipFilter
    extends EntityNameFilter[Membership]
    with MembershipPredicates {

  def filterMemberships(memberships: Set[Membership], filter: FilterString)
      :ServiceValidation[Set[Membership]] = {
    QueryFilterParser.expressions(filter).flatMap { filterExpression =>
      filterExpression match {
        case None =>
          memberships.successNel[String]
        case Some(c: Comparison) =>
          comparisonToPredicates(c).map(memberships.filter)
        case Some(e: AndExpression) =>
          comparisonToPredicates(e).map(memberships.filter)
        case Some(e: OrExpression) =>
          comparisonToPredicates(e).map(memberships.filter)
        case _ =>
          ServiceError(s"bad filter expression: $filterExpression").failureNel[Set[Membership]]
      }
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def comparisonToPredicates(expression: Expression): ServiceValidation[MembershipFilter] = {
    expression match {
      case Comparison(selector, comparator, args) =>
        selector match {
          case "name"  => nameFilter(comparator, args)
          case _ =>
            ServiceError(s"invalid filter selector: $selector").failureNel[MembershipFilter]
        }
      case AndExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => every(x:_*))
      case OrExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => any(x:_*))
      case _ =>
        ServiceError(s"invalid filter expression: $expression").failureNel[MembershipFilter]
    }
  }

}
