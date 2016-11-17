package org.biobank.service.studies

import org.biobank.service._
import org.biobank.service.Comparator._
import org.biobank.service.QueryFilterParserGrammar._
import org.biobank.service.{ServiceValidation, ServiceError}
import org.biobank.domain.PredicateHelper
import org.biobank.domain.study.{Study, StudyState, StudyPredicates}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * Functions that filter a set of studys from an expression contained in a filter string.
 *
 */
object StudyFilter extends PredicateHelper with StudyPredicates {
  import org.biobank.CommonValidations._

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

  private def nameFilter(comparator: Comparator, names: List[String]) = { //
    val nameSet = names.toSet
    comparator match {
      case Equal | In =>
        nameIsOneOf(nameSet).successNel[String]
      case NotEqualTo | NotIn =>
        complement(nameIsOneOf(nameSet)).successNel[String]
      case _ =>
        ServiceError(s"invalid filter on courier name: $comparator").failureNel[StudyFilter]
    }
  }

  private def stateFilter(comparator: Comparator, stateNames: List[String]) = {
    stateNames.
      map { str =>
        StudyState.values.find(_.toString == str).toSuccessNel(
          InvalidState(s"study state does not exist: $str").toString)
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
          ServiceError(s"invalid filter on state: $comparator").failureNel[StudyFilter]
        }
      }
  }

}
