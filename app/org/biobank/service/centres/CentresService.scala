package org.biobank.service.centre

import org.biobank.service.ApplicationService
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

  def getCentre(id: String): DomainValidation[Centre] = {
    centreRepository.getByKey(CentreId(id)).fold(
      err => DomainError(s"invalid centre id: $id").failNel,
      centre => centre.success
    )
  }

  def getCentreLocations(centreId: String, locationIdOpt: Option[String]): DomainValidation[Set[Location]] = {
    centreRepository.getByKey(CentreId(centreId)).fold(
      err => DomainError(s"invalid centre id: $centreId").failNel[Set[Location]],
      centre => {
        val locationIds = centreLocationsRepository.withCentreId(centre.id).map { x => x.locationId }
        val locations = locationRepository.getValues.filter(x => locationIds.contains(x.id)).toSet
        locationIdOpt.fold {
          locations.successNel[String]
        } { locationId =>
          locationRepository.getByKey(LocationId(locationId)).fold(
            err => DomainError(s"invalid location id: $locationId").failNel[Set[Location]],
            location => {
              val locsFound = locations.filter(_.id.id == locationId)
              if (locsFound.isEmpty) {
                DomainError(s"centre does not have location with id: $locationId").failNel[Set[Location]]
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
      err => DomainError(s"invalid centre id: $centreId").failNel,
      centre => centreStudiesRepository.withCentreId(CentreId(centreId)).map(x => x.studyId).toSet.success
    )
  }

  def addCentre(cmd: AddCentreCmd)(implicit userId: UserId)
      : Future[DomainValidation[CentreAddedEvent]] = {
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[CentreAddedEvent]])
  }

  def updateCentre(cmd: UpdateCentreCmd)(implicit userId: UserId)
      : Future[DomainValidation[CentreUpdatedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[CentreUpdatedEvent]])

  def enableCentre(cmd: EnableCentreCmd)(implicit userId: UserId)
      : Future[DomainValidation[CentreEnabledEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[CentreEnabledEvent]])

  def disableCentre(cmd: DisableCentreCmd)(implicit userId: UserId)
      : Future[DomainValidation[CentreDisabledEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[CentreDisabledEvent]])

  def addCentreLocation(cmd: AddCentreLocationCmd)(implicit userId: UserId)
      : Future[DomainValidation[CentreLocationAddedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[CentreLocationAddedEvent]])

  def removeCentreLocation(cmd: RemoveCentreLocationCmd)(implicit userId: UserId)
      : Future[DomainValidation[CentreLocationRemovedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[CentreLocationRemovedEvent]])

  def addStudyToCentre(cmd: AddStudyToCentreCmd)(implicit userId: UserId)
      : Future[DomainValidation[CentreAddedToStudyEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[CentreAddedToStudyEvent]])

  def removeStudyFromCentre(cmd: RemoveStudyFromCentreCmd)(implicit userId: UserId)
      : Future[DomainValidation[CentreRemovedFromStudyEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[CentreRemovedFromStudyEvent]])


}
