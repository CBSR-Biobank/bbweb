package org.biobank.services.centres

import org.biobank.services._
import org.biobank.services.Comparator._
import org.biobank.services.{ServiceValidation, ServiceError}
import org.biobank.domain.centres.{Centre, CentrePredicates}
import scalaz.Scalaz._

/**
 * Functions that filter a set of centres from an expression contained in a filter string.
 *
 */
object CentreFilter
    extends EntityFilter[Centre]
    with EntityNameFilter[Centre]
    with EntityStateFilter[Centre]
    with CentrePredicates {

  def filterCentres(centres: Set[Centre], filter: FilterString):ServiceValidation[Set[Centre]] = {
    filterEntities(centres, filter, centres.filter)
  }

  protected def predicateFromSelector(selector: String, comparator: Comparator, args: List[String])
      : ServiceValidation[Centre => Boolean] = {
    selector match {
      case "name"  => nameFilter(comparator, args)
      case "state" => stateFilter(comparator, args)
      case _ =>
        ServiceError(s"invalid filter selector: $selector").failureNel[CentreFilter]
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  protected def stateFilter(comparator: Comparator, stateNames: List[String]):
      ServiceValidation[EntityStateFilter] = {
    stateFilter(comparator, stateNames, Centre.centreStates)
  }
}
