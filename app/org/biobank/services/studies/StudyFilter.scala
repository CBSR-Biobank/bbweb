package org.biobank.services.studies

import org.biobank.services._
import org.biobank.services.Comparator._
import org.biobank.services.{ServiceValidation, ServiceError}
import org.biobank.domain.studies.{Study}
import scalaz.Scalaz._

/**
 * Functions that filter a set of studys from an expression contained in a filter string.
 *
 */
object StudyFilter
    extends EntityFilter[Study]
    with EntityNameFilter[Study]
    with EntityStateFilter[Study] {

  def filterStudies(studies: Set[Study], filter: FilterString):ServiceValidation[Set[Study]] = {
    filterEntities(studies, filter, studies.filter)
  }

  protected def predicateFromSelector(selector: String, comparator: Comparator, args: List[String])
      : ServiceValidation[Study => Boolean] = {
    selector match {
      case "name"  => nameFilter(comparator, args)
      case "state" => stateFilter(comparator, args)
      case _ =>
        ServiceError(s"invalid filter selector: $selector").failureNel[Study => Boolean]
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  protected def stateFilter(comparator: Comparator, stateNames: List[String]):
      ServiceValidation[EntityStateFilter] = {
    stateFilter(comparator, stateNames, Study.studyStates)
  }

}
