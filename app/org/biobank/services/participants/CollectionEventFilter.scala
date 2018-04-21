package org.biobank.services.participants

import org.biobank.domain.participants.{CollectionEvent, CollectionEventPredicates}
import org.biobank.domain.PredicateHelper
import org.biobank.services._
import org.biobank.services.Comparator._
import org.biobank.services.{ServiceValidation, ServiceError}
import scalaz.Scalaz._

/**
 * Functions that filter a set of studys from an expression contained in a filter string.
 *
 */
object CollectionEventFilter
    extends EntityFilter[CollectionEvent]
    with PredicateHelper
    with CollectionEventPredicates {

  def filterCollectionEvents(cevents: Set[CollectionEvent], filter: FilterString):
      ServiceValidation[Set[CollectionEvent]] = {
    filterEntities(cevents, filter, cevents.filter)
  }

  protected def predicateFromSelector(selector: String, comparator: Comparator, args: List[String])
      : ServiceValidation[CollectionEvent => Boolean] = {
    selector match {
      case "visitNumber" => visitNumberFilter(comparator, args)
      case _ => ServiceError(s"invalid filter selector: $selector").failureNel[CollectionEventFilter]
    }
  }

  private def visitNumberFilter(comparator: Comparator, visitNumbers: List[String]) = {
    val visitNumberSet = visitNumbers.toSet
    comparator match {
      case Equal | In =>
        visitNumberIsOneOf(visitNumberSet).successNel[String]
      case NotEqualTo | NotIn =>
        complement(visitNumberIsOneOf(visitNumberSet)).successNel[String]
      case _ =>
        ServiceError(s"invalid filter on courier name: $comparator").failureNel[CollectionEventFilter]
    }
  }

}
