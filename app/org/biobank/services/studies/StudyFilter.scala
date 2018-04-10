package org.biobank.services.studies

import org.biobank.services._
import org.biobank.services.Comparator._
import org.biobank.services.QueryFilterParserGrammar._
import org.biobank.services.{ServiceValidation, ServiceError}
import org.biobank.domain.studies.{Study, StudyPredicates}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * Functions that filter a set of studys from an expression contained in a filter string.
 *
 */
object StudyFilter
    extends EntityNameFilter[Study]
    with EntityStateFilter[Study]
    with StudyPredicates {

  def filterStudies(studies: Set[Study], filter: FilterString):ServiceValidation[Set[Study]] = {
    QueryFilterParser.expressions(filter).flatMap { filterExpression =>
      filterExpression match {
        case None =>
          studies.successNel[String]
        case Some(c: Comparison) =>
          comparisonToPredicates(c).map(studies.filter)
        case Some(e: AndExpression) =>
          comparisonToPredicates(e).map(studies.filter)
        case Some(e: OrExpression) =>
          comparisonToPredicates(e).map(studies.filter)
        case _ =>
          ServiceError(s"bad filter expression: $filterExpression").failureNel[Set[Study]]
      }
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def comparisonToPredicates(expression: Expression): ServiceValidation[StudyFilter] = {
    expression match {
      case Comparison(selector, comparator, args) =>
        selector match {
          case "name"  => nameFilter(comparator, args)
          case "state" => stateFilter(comparator, args)
          case _ =>
            ServiceError(s"invalid filter selector: $selector").failureNel[StudyFilter]
        }
      case AndExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => every(x:_*))
      case OrExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => any(x:_*))
      case _ =>
        ServiceError(s"invalid filter expression: $expression").failureNel[StudyFilter]
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  protected def stateFilter(comparator: Comparator, stateNames: List[String]):
      ServiceValidation[EntityStateFilter] = {
    stateFilter(comparator, stateNames, Study.studyStates)
  }

}
