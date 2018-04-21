package org.biobank.services.access

import org.biobank.domain.access._
import org.biobank.services._
import org.biobank.services.{ServiceValidation, ServiceError}
import scalaz.Scalaz._

/**
 * Functions that filter a set of memberships from an expression contained in a filter string.
 *
 */
object MembershipFilter
    extends EntityFilter[Membership]
    with EntityNameFilter[Membership]
    with MembershipPredicates {

  import org.biobank.services.Comparator._

  def filterMemberships(memberships: Set[Membership], filter: FilterString)
      :ServiceValidation[Set[Membership]] = {
    filterEntities(memberships, filter, memberships.filter)
  }

  protected def predicateFromSelector(selector: String, comparator: Comparator, args: List[String])
      : ServiceValidation[Membership => Boolean] = {
    selector match {
      case "name"  => nameFilter(comparator, args)
      case _ =>
        ServiceError(s"invalid filter selector: $selector").failureNel[MembershipFilter]
    }
  }

}
