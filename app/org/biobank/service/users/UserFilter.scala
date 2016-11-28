package org.biobank.service.users

import org.biobank.service._
import org.biobank.service.Comparator._
import org.biobank.service.QueryFilterParserGrammar._
import org.biobank.service.{ServiceValidation, ServiceError}
import org.biobank.domain.user.{User, UserPredicates}
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * Functions that filter a set of users from an expression contained in a filter string.
 *
 */
object UserFilter
    extends EntityNameFilter[User]
    with EntityStateFilter[User]
    with UserPredicates {

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

  private def emailFilter(comparator: Comparator, emails: List[String]) = { //
    val emailSet = emails.toSet
    comparator match {
      case Equal =>
        if ((emails.size == 1) && emails(0).contains("*")) {
          emailContains(emails(0)).successNel[String]
        } else {
          emailIsOneOf(emailSet).successNel[String]
        }
      case In =>
        emailIsOneOf(emailSet).successNel[String]
      case NotEqualTo | NotIn =>
        complement(emailIsOneOf(emailSet)).successNel[String]
      case _ =>
        ServiceError(s"invalid filter on courier email: $comparator").failureNel[UserFilter]
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  protected def stateFilter(comparator: Comparator, stateNames: List[String]):
      ServiceValidation[EntityStateFilter] = {
    stateFilter(comparator, stateNames, User.userStates)
  }

}
