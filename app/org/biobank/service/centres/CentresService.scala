package org.biobank.service.centres

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.biobank.domain.DomainValidation
import org.biobank.domain.centre._
import org.biobank.domain.study.StudyRepository
import org.biobank.dto._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.infrastructure.event.CentreEvents._
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[CentresServiceImpl])
trait CentresService {

  def getAll: Set[Centre]

  def centreLocations(): Set[CentreLocation]

  def getCentres[T <: Centre]
    (filter: String, status: String, sortFunc: (Centre, Centre) => Boolean, order: SortOrder)
      : DomainValidation[Seq[Centre]]

  def getCountsByStatus(): CentreCountsByStatus

  def getCentreNames(filter: String, order: SortOrder): Seq[NameDto]

  def getCentre(id: String): DomainValidation[Centre]

  def processCommand(cmd: CentreCommand): Future[DomainValidation[Centre]]
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
  import org.biobank.CommonValidations._

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  /**
   * FIXME: use paging and sorting
   */
  def getAll: Set[Centre] = {
    centreRepository.getValues.toSet
  }

  def centreLocations(): Set[CentreLocation] = {
    centreRepository.getValues.flatMap { centre =>
      centre.locations.map { location =>
        CentreLocation(centre.id.id, location.uniqueId, centre.name, location.name)
      }
    }.toSet
  }

  def getCountsByStatus(): CentreCountsByStatus = {
    // FIXME should be replaced by DTO query to the database
    val centres = centreRepository.getValues
    CentreCountsByStatus(
      total         = centres.size,
      disabledCount = centres.collect { case s: DisabledCentre => s }.size,
      enabledCount  = centres.collect { case s: EnabledCentre => s }.size
    )
  }

  def getCentres[T <: Centre](filter: String,
                              status: String,
                              sortFunc: (Centre, Centre) => Boolean,
                              order: SortOrder)
      : DomainValidation[Seq[Centre]] =  {
    val allCentres = centreRepository.getValues

    val centresFilteredByName = if (!filter.isEmpty) {
        val filterLowerCase = filter.toLowerCase
        allCentres.filter { centre => centre.name.toLowerCase.contains(filterLowerCase) }
      } else {
        allCentres
      }

    val centresFilteredByStatus = status match {
      case "all" =>
        centresFilteredByName.success
      case "DisabledCentre" =>
        centresFilteredByName.collect { case s: DisabledCentre => s }.success
      case "EnabledCentre" =>
        centresFilteredByName.collect { case s: EnabledCentre => s }.success
      case _ =>
        InvalidStatus(s"centre: $status").failureNel
    }

    centresFilteredByStatus.map { centres =>
      val result = centres.toSeq.sortWith(sortFunc)

      if (order == AscendingOrder) result
      else result.reverse
    }
  }

  def getCentreNames(filter: String, order: SortOrder): Seq[NameDto] = {
    val centres = centreRepository.getValues

    val filteredCentres = if (filter.isEmpty) {
      centres
    } else {
      centres.filter { s => s.name.contains(filter) }
    }

    val orderedCentres = filteredCentres.toSeq
    val result = orderedCentres.map { s =>
      NameDto(s.id.id, s.name, s.getClass.getSimpleName)
    } sortWith(NameDto.compareByName)

    if (order == AscendingOrder) {
      result
    } else {
      result.reverse
    }
  }

  def getCentre(id: String): DomainValidation[Centre] = {
    centreRepository.getByKey(CentreId(id))
  }

  def processCommand(cmd: CentreCommand): Future[DomainValidation[Centre]] =
    ask(processor, cmd).mapTo[DomainValidation[CentreEvent]].map { validation =>
      for {
        event <- validation
        centre <- centreRepository.getByKey(CentreId(event.id))
      } yield centre
    }

}
