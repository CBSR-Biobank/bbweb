package org.biobank.service.centres

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.biobank.domain.centre._
import org.biobank.domain.study.StudyRepository
import org.biobank.dto._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.infrastructure.event.CentreEvents._
import org.biobank.service._
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[CentresServiceImpl])
trait CentresService {

  /**
   * Returns a set of studies. The entities can be filtered and or sorted using expressions.
   *
   * @param filter the string representation of the filter expression to use to filter the studies.
   *
   * @param sort the string representation of the sort expression to use when sorting the studies.
   */
  def getCentresCount(): Int

  def searchLocations(cmd: SearchCentreLocationsCmd): Set[CentreLocationInfo]

  def getCentres(filter: FilterString, sort: SortString): ServiceValidation[Seq[Centre]]

  def getCountsByStatus(): CentreCountsByStatus

  def getCentreNames(filter: FilterString, sort: SortString): ServiceValidation[Seq[NameDto]]

  def getCentre(id: CentreId): ServiceValidation[Centre]

  def processCommand(cmd: CentreCommand): Future[ServiceValidation[Centre]]
}

/**
 * This is the Centre Aggregate Application Service.
 *
 * Handles the commands to configure centres. the commands are forwarded to the Centre Aggregate
 * Processor.
 *
 * @param centreProcessor
 *
 */
class CentresServiceImpl @Inject() (@Named("centresProcessor") val processor: ActorRef,
                                    val centreRepository:          CentreRepository,
                                    val studyRepository:           StudyRepository)
    extends CentresService {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  def getCentresCount(): Int = {
    centreRepository.getValues.size
  }

  def searchLocations(cmd: SearchCentreLocationsCmd): Set[CentreLocationInfo] =  {
    val allLocationInfos = centreRepository.getValues.flatMap { centre =>
        centre.locations.map { location =>
          CentreLocationInfo(centre.id.id, location.uniqueId, centre.name, location.name)
        }
      }

    val filterLowerCase = cmd.filter.toLowerCase.trim
    val filteredLocationInfos = if (filterLowerCase.isEmpty) { allLocationInfos }
                                else allLocationInfos.filter { l =>
                                  l.name.toLowerCase contains filterLowerCase
                                }

    filteredLocationInfos.
      toSeq.
      sortWith { (a, b) => (a.name compareToIgnoreCase b.name) < 0 }.
      take(cmd.limit).
      toSet
  }

  def getCountsByStatus(): CentreCountsByStatus = {
    // FIXME should be replaced by DTO query to the database
    val centres = centreRepository.getValues
    CentreCountsByStatus(
      total         = centres.size.toLong,
      disabledCount = centres.collect { case s: DisabledCentre => s }.size.toLong,
      enabledCount  = centres.collect { case s: EnabledCentre => s }.size.toLong
    )
  }

  def getCentres(filter: FilterString, sort: SortString):ServiceValidation[Seq[Centre]] =  {
    val allCentres = centreRepository.getValues.toSet
    val sortStr = if (sort.expression.isEmpty) new SortString("name")
                  else sort

    for {
      centres <- CentreFilter.filterCentres(allCentres, filter)
      sortExpressions <- {
        QuerySortParser(sortStr).toSuccessNel(ServiceError(s"could not parse sort expression: $sort"))
      }
      sortFunc <- {
        Centre.sort2Compare.get(sortExpressions(0).name).
          toSuccessNel(ServiceError(s"invalid sort field: ${sortExpressions(0).name}"))
      }
    } yield {
      val result = centres.toSeq.sortWith(sortFunc)
      if (sortExpressions(0).order == AscendingOrder) result
      else result.reverse
    }
  }

  def getCentreNames(filter: FilterString, sort: SortString): ServiceValidation[Seq[NameDto]] = {
    getCentres(filter, sort).map(_.map(s => NameDto(s.id.id, s.name, s.state.id)))
  }

  def getCentre(id: CentreId): ServiceValidation[Centre] = {
    centreRepository.getByKey(id)
  }

  def processCommand(cmd: CentreCommand): Future[ServiceValidation[Centre]] =
    ask(processor, cmd).mapTo[ServiceValidation[CentreEvent]].map { validation =>
      for {
        event <- validation
        centre <- centreRepository.getByKey(CentreId(event.id))
      } yield centre
    }

}
