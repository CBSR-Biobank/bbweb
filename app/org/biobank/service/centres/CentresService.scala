package org.biobank.service.centre

import org.biobank.service.ApplicationService
import org.biobank.dto._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.infrastructure.event.CentreEvents._
import org.biobank.domain.{ DomainValidation, DomainError, Location, LocationId, LocationRepository }
import org.biobank.domain.user.UserId
import org.biobank.domain.study.StudyId
import org.biobank.domain.centre._

import akka.actor._
import akka.pattern.ask
import scala.concurrent._
import scala.concurrent.duration._
import org.slf4j.LoggerFactory
import ExecutionContext.Implicits.global
import akka.util.Timeout
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}

import scalaz._
import scalaz.Scalaz._

trait CentresService {

  def getAll: Set[Centre]

  def getCentres[T <: Centre]
    (filter: String, status: String, sortFunc: (Centre, Centre) => Boolean, order: SortOrder)
      : DomainValidation[Seq[Centre]]

  def getCountsByStatus(): CentreCountsByStatus

  def getCentre(id: String): DomainValidation[Centre]

  def getCentreLocations(centreId: String, locationIdOpt: Option[String]): DomainValidation[Set[Location]]

  def getCentreStudies(centreId: String): DomainValidation[Set[StudyId]]

  def addCentre(cmd: AddCentreCmd)(
    implicit userId: UserId): Future[DomainValidation[CentreAddedEvent]]

  def updateCentre(cmd: UpdateCentreCmd)(
    implicit userId: UserId): Future[DomainValidation[CentreUpdatedEvent]]

  def enableCentre(cmd: EnableCentreCmd)(
    implicit userId: UserId): Future[DomainValidation[CentreEnabledEvent]]

  def disableCentre(cmd: DisableCentreCmd)(
    implicit userId: UserId): Future[DomainValidation[CentreDisabledEvent]]

  def addCentreLocation(cmd: AddCentreLocationCmd)(
    implicit userId: UserId): Future[DomainValidation[CentreLocationAddedEvent]]

  def removeCentreLocation(cmd: RemoveCentreLocationCmd)(
    implicit userId: UserId): Future[DomainValidation[CentreLocationRemovedEvent]]

  def addStudyToCentre(cmd: AddStudyToCentreCmd)(
    implicit userId: UserId): Future[DomainValidation[CentreAddedToStudyEvent]]

  def removeStudyFromCentre(cmd: RemoveStudyFromCentreCmd)(
    implicit userId: UserId): Future[DomainValidation[CentreRemovedFromStudyEvent]]
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
class CentresServiceImpl(implicit inj: Injector)
    extends CentresService
    with ApplicationService
    with AkkaInjectable {

  implicit val system = inject [ActorSystem]

  implicit val timeout = inject [Timeout] ('akkaTimeout)

  val centreRepository = inject [CentreRepository]

  val locationRepository = inject [LocationRepository]

  val centreStudiesRepository = inject [CentreStudiesRepository]

  val centreLocationsRepository = inject [CentreLocationsRepository]

  val processor = injectActorRef [CentresProcessor] ("centre")

  val log = LoggerFactory.getLogger(this.getClass)

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

  def addCentre(cmd: AddCentreCmd)(implicit userId: UserId)
      : Future[DomainValidation[CentreAddedEvent]] = {
    ask(processor, cmd, userId).mapTo[DomainValidation[CentreAddedEvent]]
  }

  def updateCentre(cmd: UpdateCentreCmd)(implicit userId: UserId)
      : Future[DomainValidation[CentreUpdatedEvent]] =
    ask(processor, cmd, userId).mapTo[DomainValidation[CentreUpdatedEvent]]

  def enableCentre(cmd: EnableCentreCmd)(implicit userId: UserId)
      : Future[DomainValidation[CentreEnabledEvent]] =
    ask(processor, cmd, userId).mapTo[DomainValidation[CentreEnabledEvent]]

  def disableCentre(cmd: DisableCentreCmd)(implicit userId: UserId)
      : Future[DomainValidation[CentreDisabledEvent]] =
    ask(processor, cmd, userId).mapTo[DomainValidation[CentreDisabledEvent]]

  def addCentreLocation(cmd: AddCentreLocationCmd)(implicit userId: UserId)
      : Future[DomainValidation[CentreLocationAddedEvent]] =
    ask(processor, cmd, userId).mapTo[DomainValidation[CentreLocationAddedEvent]]

  def removeCentreLocation(cmd: RemoveCentreLocationCmd)(implicit userId: UserId)
      : Future[DomainValidation[CentreLocationRemovedEvent]] =
    ask(processor, cmd, userId).mapTo[DomainValidation[CentreLocationRemovedEvent]]

  def addStudyToCentre(cmd: AddStudyToCentreCmd)(implicit userId: UserId)
      : Future[DomainValidation[CentreAddedToStudyEvent]] =
    ask(processor, cmd, userId).mapTo[DomainValidation[CentreAddedToStudyEvent]]

  def removeStudyFromCentre(cmd: RemoveStudyFromCentreCmd)(implicit userId: UserId)
      : Future[DomainValidation[CentreRemovedFromStudyEvent]] =
    ask(processor, cmd, userId).mapTo[DomainValidation[CentreRemovedFromStudyEvent]]


}
