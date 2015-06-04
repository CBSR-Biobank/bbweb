package org.biobank.service.centres

import org.biobank.dto._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.infrastructure.event.CentreEvents._
import org.biobank.domain.{ DomainValidation, DomainError, Location, LocationId, LocationRepository }
import org.biobank.domain.user.UserId
import org.biobank.domain.study.{ Study, StudyId, StudyRepository }
import org.biobank.domain.centre._

import akka.actor._
import akka.pattern.ask
import scala.concurrent._
import scala.concurrent.duration._
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global
import akka.util.Timeout
import javax.inject.{Inject => javaxInject, Named}
import com.google.inject.ImplementedBy

import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[CentresServiceImpl])
trait CentresService {

  def getAll: Set[Centre]

  def getCentres[T <: Centre]
    (filter: String, status: String, sortFunc: (Centre, Centre) => Boolean, order: SortOrder)
      : DomainValidation[Seq[Centre]]

  def getCountsByStatus(): CentreCountsByStatus

  def getCentre(id: String): DomainValidation[Centre]

  def getCentreLocations(centreId: String, locationIdOpt: Option[String]): DomainValidation[Set[Location]]

  def getCentreStudies(centreId: String): DomainValidation[Set[StudyId]]

  def addCentre(cmd: AddCentreCmd): Future[DomainValidation[Centre]]

  def updateCentre(cmd: UpdateCentreCmd): Future[DomainValidation[Centre]]

  def enableCentre(cmd: EnableCentreCmd): Future[DomainValidation[Centre]]

  def disableCentre(cmd: DisableCentreCmd): Future[DomainValidation[Centre]]

  def addCentreLocation(cmd: AddCentreLocationCmd): Future[DomainValidation[Location]]

  def removeCentreLocation(cmd: RemoveCentreLocationCmd): Future[DomainValidation[Boolean]]

  def addStudyToCentre(cmd: AddStudyToCentreCmd): Future[DomainValidation[Study]]

  def removeStudyFromCentre(cmd: RemoveStudyFromCentreCmd): Future[DomainValidation[Boolean]]
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
class CentresServiceImpl @javaxInject() (@Named("centresProcessor") val processor: ActorRef,
                                         val centreRepository:          CentreRepository,
                                         val locationRepository:        LocationRepository,
                                         val centreStudiesRepository:   CentreStudiesRepository,
                                         val centreLocationsRepository: CentreLocationsRepository,
                                         val studyRepository:           StudyRepository)
    extends CentresService {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  /**
   * FIXME: use paging and sorting
   */
  def getAll: Set[Centre] = {
    centreRepository.getValues.toSet
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

  private def getStatus(status: String): DomainValidation[String] = {
    status match {
      case "all"      => Centre.status.successNel
      case "disabled" => DisabledCentre.status.successNel
      case "enabled"  => EnabledCentre.status.successNel
      case _          => DomainError(s"invalid centre status: $status").failureNel
    }
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

    val centresFilteredByStatus = getStatus(status).map { status =>
      if (status == Centre.status) {
        centresFilteredByName
      } else {
        centresFilteredByName.filter { centre => centre.status == status }
      }
    }

    centresFilteredByStatus.map { centres =>
      val result = centres.toSeq.sortWith(sortFunc)

      if (order == AscendingOrder) {
        result
      } else {
        result.reverse
      }
    }
  }

  def getCentre(id: String): DomainValidation[Centre] = {
    centreRepository.getByKey(CentreId(id)).fold(
      err => DomainError(s"invalid centre id: $id").failureNel,
      centre => centre.success
    )
  }

  def getCentreLocations(centreId: String,
                         locationIdOpt: Option[String])
      : DomainValidation[Set[Location]] = {
    centreRepository.getByKey(CentreId(centreId)).fold(
      err => DomainError(s"invalid centre id: $centreId").failureNel[Set[Location]],
      centre => {
        val locationIds = centreLocationsRepository.withCentreId(centre.id).map { x => x.locationId }
        val locations = locationRepository.getValues.filter(x => locationIds.contains(x.id)).toSet
        locationIdOpt.fold {
          locations.successNel[String]
        } { locationId =>
          locationRepository.getByKey(LocationId(locationId)).fold(
            err => DomainError(s"invalid location id: $locationId").failureNel[Set[Location]],
            location => {
              val locsFound = locations.filter(_.id.id == locationId)
              if (locsFound.isEmpty) {
                DomainError(s"centre does not have location with id: $locationId").failureNel[Set[Location]]
              } else {
                Set(location).successNel[String]
              }
            }
          )
        }
      }
    )
  }

  def getCentreStudies(centreId: String): DomainValidation[Set[StudyId]] = {
    centreRepository.getByKey(CentreId(centreId)).fold(
      err => DomainError(s"invalid centre id: $centreId").failureNel,
      centre => centreStudiesRepository.withCentreId(CentreId(centreId)).map(x => x.studyId).toSet.success
    )
  }

  def addCentre(cmd: AddCentreCmd): Future[DomainValidation[Centre]] =
    replyWithCentre(ask(processor, cmd).mapTo[DomainValidation[CentreEvent]])

  def updateCentre(cmd: UpdateCentreCmd): Future[DomainValidation[Centre]] =
    replyWithCentre(ask(processor, cmd).mapTo[DomainValidation[CentreEvent]])

  def enableCentre(cmd: EnableCentreCmd): Future[DomainValidation[Centre]] =
    replyWithCentre(ask(processor, cmd).mapTo[DomainValidation[CentreEvent]])

  def disableCentre(cmd: DisableCentreCmd): Future[DomainValidation[Centre]] =
    replyWithCentre(ask(processor, cmd).mapTo[DomainValidation[CentreEvent]])

  def addCentreLocation(cmd: AddCentreLocationCmd): Future[DomainValidation[Location]] = {
    ask(processor, cmd).mapTo[DomainValidation[CentreEvent]] map { validation =>
      for {
        event <- validation
        location <- locationRepository.getByKey(LocationId(event.getLocationAdded.getLocationId))
      } yield location
    }
  }

  def removeCentreLocation(cmd: RemoveCentreLocationCmd): Future[DomainValidation[Boolean]] = {
    ask(processor, cmd).mapTo[DomainValidation[CentreEvent]] map { validation =>
      validation map { event => true }
    }
  }

  def addStudyToCentre(cmd: AddStudyToCentreCmd): Future[DomainValidation[Study]] = {
    ask(processor, cmd).mapTo[DomainValidation[CentreEvent]] map { validation =>
      for {
        event <- validation
        study <- studyRepository.getByKey(StudyId(event.getStudyAdded.getStudyId))
      } yield study
    }
  }

  def removeStudyFromCentre(cmd: RemoveStudyFromCentreCmd): Future[DomainValidation[Boolean]] = {
    ask(processor, cmd).mapTo[DomainValidation[CentreEvent]] map { validation =>
      validation map { event => true }
    }
  }

  private def replyWithCentre(future: Future[DomainValidation[CentreEvent]])
      : Future[DomainValidation[Centre]] = {
    future map { validation =>
      for {
        event <- validation
        centre <- centreRepository.getByKey(CentreId(event.id))
      } yield centre
    }
  }
}
