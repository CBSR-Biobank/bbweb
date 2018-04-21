package org.biobank.services.studies

import org.biobank.domain.studies.{ProcessingType, ProcessingTypePredicates}
import org.biobank.services._
import scalaz.Scalaz._

/**
 * Functions that filter a set of Processing Types from an expression contained in a filter string.
 *
 */
object ProcessingTypeFilter
    extends EntityFilter[ProcessingType]
    with EntityNameFilter[ProcessingType]
    with ProcessingTypePredicates {

  import org.biobank.services.Comparator._

  def filterProcessingTypes(processingTypes: Set[ProcessingType], filter: FilterString):
      ServiceValidation[Set[ProcessingType]] = {
    filterEntities(processingTypes, filter, processingTypes.filter)
  }

  protected def predicateFromSelector(selector: String, comparator: Comparator, args: List[String])
      : ServiceValidation[ProcessingType => Boolean] = {
    selector match {
      case "name" => nameFilter(comparator, args)
      case _ => ServiceError(s"invalid filter selector: $selector").failureNel[ProcessingTypeFilter]
    }
  }

}
