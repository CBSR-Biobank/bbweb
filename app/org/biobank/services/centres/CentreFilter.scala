package org.biobank.services.centres

import org.biobank.services._
import org.biobank.services.Comparator._
import org.biobank.services.QueryFilterParserGrammar._
import org.biobank.services.{ServiceValidation, ServiceError}
import org.biobank.domain.centres.{Centre, CentrePredicates}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * Functions that filter a set of centres from an expression contained in a filter string.
 *
 */
object CentreFilter
    extends EntityNameFilter[Centre]
    with EntityStateFilter[Centre]
    with CentrePredicates {

  def filterCentres(centres: Set[Centre], filter: FilterString):ServiceValidation[Set[Centre]] = {
    QueryFilterParser.expressions(filter).flatMap { filterExpression =>
      filterExpression match {
        case None =>
          centres.successNel[String]
        case Some(c: Comparison) =>
          comparisonToPredicates(c).map(centres.filter)
        case Some(e: AndExpression) =>
          comparisonToPredicates(e).map(centres.filter)
        case Some(e: OrExpression) =>
          comparisonToPredicates(e).map(centres.filter)
        case _ =>
          ServiceError(s"bad filter expression: $filterExpression").failureNel[Set[Centre]]
      }
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def comparisonToPredicates(expression: Expression): ServiceValidation[CentreFilter] = {
    expression match {
      case Comparison(selector, comparator, args) =>
        selector match {
          case "name"  => nameFilter(comparator, args)
          case "state" => stateFilter(comparator, args)
          case _ =>
            ServiceError(s"invalid filter selector: $selector").failureNel[CentreFilter]
        }
      case AndExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => every(x:_*))
      case OrExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => any(x:_*))
      case _ =>
        ServiceError(s"invalid filter expression: $expression").failureNel[CentreFilter]
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  protected def stateFilter(comparator: Comparator, stateNames: List[String]):
      ServiceValidation[EntityStateFilter] = {
    stateFilter(comparator, stateNames, Centre.centreStates)
  }
}
