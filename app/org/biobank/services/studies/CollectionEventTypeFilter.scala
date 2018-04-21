package org.biobank.services.studies

import org.biobank.domain.studies.{CollectionEventType, CollectionEventTypePredicates}
import org.biobank.services._
import scalaz.Scalaz._

/**
 * Functions that filter a set of Collection Events from an expression contained in a filter string.
 *
 */
object CollectionEventTypeFilter
    extends EntityFilter[CollectionEventType]
    with EntityNameFilter[CollectionEventType]
    with CollectionEventTypePredicates {

  import org.biobank.services.Comparator._

  def filterCollectionEventTypes(ceventTypes: Set[CollectionEventType], filter: FilterString):
      ServiceValidation[Set[CollectionEventType]] = {
    filterEntities(ceventTypes, filter, ceventTypes.filter)
  }

  protected def predicateFromSelector(selector: String, comparator: Comparator, args: List[String])
      : ServiceValidation[CollectionEventType => Boolean] = {
    selector match {
      case "name" => nameFilter(comparator, args)
      case _ => ServiceError(s"invalid filter selector: $selector").failureNel[CollectionEventTypeFilter]
    }
  }

}
