package org.biobank.services.users

import org.biobank.services._
import org.biobank.services.Comparator._
import org.biobank.services.{ServiceValidation, ServiceError}
import org.biobank.domain.users.{User, UserPredicates}
import scalaz.Scalaz._

/**
 * Functions that filter a set of users from an expression contained in a filter string.
 *
 */
object UserFilter
    extends EntityFilter[User]
    with EntityNameFilter[User]
    with EntityStateFilter[User]
    with UserPredicates {

  def filterUsers(users: Set[User], filter: FilterString):ServiceValidation[Set[User]] = {
    filterEntities(users, filter, users.filter)
  }

  protected def predicateFromSelector(selector: String, comparator: Comparator, args: List[String])
      : ServiceValidation[User => Boolean] = {
    selector match {
      case "name"  => nameFilter(comparator, args)
      case "email" => emailFilter(comparator, args)
      case "state" => stateFilter(comparator, args)
      case _ =>
        ServiceError(s"invalid filter selector: $selector").failureNel[UserFilter]
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
      case Like =>
        emailIsLike(emailSet).successNel[String]
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
