package org.biobank.service.users

import org.biobank.service._
import org.biobank.service.Comparator._
import org.biobank.service.QueryFilterParserGrammar._
import org.biobank.service.{ServiceValidation, ServiceError}
import org.biobank.domain.PredicateHelper
import org.biobank.domain.user.{User, UserState, UserPredicates}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * Functions that filter a set of users from an expression contained in a filter string.
 *
 */
object UserFilter extends PredicateHelper with UserPredicates {
  import org.biobank.CommonValidations._

  def filterUsers(users: Set[User], filter: FilterString):ServiceValidation[Set[User]] = {
    QueryFilterParser.expressions(filter).flatMap { filterExpression =>
      filterExpression match {
        case None =>
          users.successNel[String]
        case Some(c: Comparison) =>
          comparisonToPredicates(c).map(users.filter)
        case Some(e: AndExpression) =>
          comparisonToPredicates(e).map(users.filter)
        case Some(e: OrExpression) =>
          comparisonToPredicates(e).map(users.filter)
        case _ =>
          ServiceError(s"bad filter expression: $filterExpression").failureNel[Set[User]]
      }
    }
  }

  private def comparisonToPredicates(expression: Expression): ServiceValidation[UserFilter] = {
    expression match {
      case Comparison(selector, comparator, args) =>
        selector match {
          case "name"  => nameFilter(comparator, args)
          case "email" => emailFilter(comparator, args)
          case "state" => stateFilter(comparator, args)
          case _ =>
            ServiceError(s"invalid filter selector: $selector").failureNel[UserFilter]
        }
      case AndExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => every(x:_*))
      case OrExpression(expressions) =>
        expressions.map(comparisonToPredicates).sequenceU.map(x => any(x:_*))
      case _ =>
        ServiceError(s"invalid filter expression: $expression").failureNel[UserFilter]
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
        ServiceError(s"invalid filter on courier name: $comparator").failureNel[UserFilter]
    }
  }

  private def emailFilter(comparator: Comparator, emails: List[String]) = { //
    val emailSet = emails.toSet
    comparator match {
      case Equal | In =>
        emailIsOneOf(emailSet).successNel[String]
      case NotEqualTo | NotIn =>
        complement(emailIsOneOf(emailSet)).successNel[String]
      case _ =>
        ServiceError(s"invalid filter on courier email: $comparator").failureNel[UserFilter]
    }
  }

  private def stateFilter(comparator: Comparator, stateNames: List[String]) = {
    stateNames.
      map { str =>
        UserState.values.find(_.toString == str).toSuccessNel(
          InvalidState(s"user state does not exist: $str").toString)
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
          ServiceError(s"invalid filter on state: $comparator").failureNel[UserFilter]
        }
      }
  }

}
