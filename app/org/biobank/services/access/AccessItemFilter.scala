package org.biobank.services.access

import org.biobank.domain.access._
import org.biobank.services._
import org.biobank.services.{ServiceValidation, ServiceError}
import scalaz.Scalaz._

/**
 * Functions that filter a set of accessItems from an expression contained in a filter string.
 *
 */
object AccessItemFilter
    extends EntityFilter[AccessItem]
    with EntityNameFilter[AccessItem]
    with AccessItemPredicates[AccessItem] {

  import org.biobank.services.Comparator._

  def filterAccessItems(accessItems: Set[AccessItem], filter: FilterString)
      :ServiceValidation[Set[AccessItem]] = {
    filterEntities(accessItems, filter, accessItems.filter)
  }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def filterRoles(roles: Set[Role], filter: FilterString):ServiceValidation[Set[Role]] = {
    val items = roles.map(r => r.asInstanceOf[AccessItem])
    filterEntities(items, filter, items.filter)
      .map(result => result.map(_.asInstanceOf[Role]))
  }

  protected def predicateFromSelector(selector: String, comparator: Comparator, args: List[String])
      : ServiceValidation[AccessItem => Boolean] = {
    selector match {
      case "name"  => nameFilter(comparator, args)
      case _ =>
        ServiceError(s"invalid filter selector: $selector").failureNel[AccessItemFilter]
    }
  }

}
