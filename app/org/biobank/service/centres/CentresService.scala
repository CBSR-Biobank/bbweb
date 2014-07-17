package org.biobank.service.centre

import org.biobank.service.ApplicationService
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.infrastructure.event.CentreEvents._
import org.biobank.domain.{ DomainValidation, DomainError, Location, LocationId, RepositoriesComponent, UserId }
import org.biobank.domain.study.StudyId
import org.biobank.domain.centre._

import akka.actor._
import akka.pattern.ask
import scala.concurrent._
import scala.concurrent.duration._
import org.slf4j.LoggerFactory
import ExecutionContext.Implicits.global

import scalaz._
import scalaz.Scalaz._

trait CentresServiceComponent {

  val centresService: CentresService

  trait CentresService extends ApplicationService {

    def getAll: Set[Centre]

    def getCentre(id: String): DomainValidation[Centre]

    def getCentreLocations(centreId: String): Set[Location]

    def getCentreStudies(centreId: String): Set[StudyId]

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

    def addCentreToStudy(cmd: AddCentreToStudyCmd)(
      implicit userId: UserId): Future[DomainValidation[CentreAddedToStudyEvent]]

    def removeCentreFromStudy(cmd: RemoveCentreFromStudyCmd)(
      implicit userId: UserId): Future[DomainValidation[CentreRemovedFromStudyEvent]]
  }

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
trait CentresServiceComponentImpl extends CentresServiceComponent {
  self: RepositoriesComponent =>

  class CentresServiceImpl(processor: ActorRef) extends CentresService {

    val log = LoggerFactory.getLogger(this.getClass)

    /**
     * FIXME: use paging and sorting
     */
    def getAll: Set[Centre] = {
      centreRepository.getValues.toSet
    }

    def getCentre(id: String): DomainValidation[Centre] = {
      centreRepository.getByKey(new CentreId(id))
    }

    def getCentreLocations(centreId: String): Set[Location] = {
      val locationIds = centreLocationRepository.withCentreId(CentreId(centreId)).map { x => x.locationId }
      locationRepository.getValues.filter(x => locationIds.contains(x.id)).toSet
    }

    def getCentreStudies(centreId: String): Set[StudyId] = {
      studyCentreRepository.withCentreId(CentreId(centreId)).map(x => x.studyId).toSet
    }

    def addCentre(cmd: AddCentreCmd)(
      implicit userId: UserId): Future[DomainValidation[CentreAddedEvent]] = {
      processor ? cmd map (
        _.asInstanceOf[DomainValidation[CentreAddedEvent]])
    }

    def updateCentre(cmd: UpdateCentreCmd)(
      implicit userId: UserId): Future[DomainValidation[CentreUpdatedEvent]] =
      processor ? cmd map (
        _.asInstanceOf[DomainValidation[CentreUpdatedEvent]])

    def enableCentre(cmd: EnableCentreCmd)(
      implicit userId: UserId): Future[DomainValidation[CentreEnabledEvent]] =
      processor ? cmd map (
        _.asInstanceOf[DomainValidation[CentreEnabledEvent]])

    def disableCentre(cmd: DisableCentreCmd)(
      implicit userId: UserId): Future[DomainValidation[CentreDisabledEvent]] =
      processor ? cmd map (
        _.asInstanceOf[DomainValidation[CentreDisabledEvent]])

    def addCentreLocation(cmd: AddCentreLocationCmd)(
      implicit userId: UserId): Future[DomainValidation[CentreLocationAddedEvent]] =
      processor ? cmd map (
        _.asInstanceOf[DomainValidation[CentreLocationAddedEvent]])

    def removeCentreLocation(cmd: RemoveCentreLocationCmd)(
      implicit userId: UserId): Future[DomainValidation[CentreLocationRemovedEvent]] =
      processor ? cmd map (
        _.asInstanceOf[DomainValidation[CentreLocationRemovedEvent]])

    def addCentreToStudy(cmd: AddCentreToStudyCmd)(
      implicit userId: UserId): Future[DomainValidation[CentreAddedToStudyEvent]] =
      processor ? cmd map (
        _.asInstanceOf[DomainValidation[CentreAddedToStudyEvent]])

    def removeCentreFromStudy(cmd: RemoveCentreFromStudyCmd)(
      implicit userId: UserId): Future[DomainValidation[CentreRemovedFromStudyEvent]] =
      processor ? cmd map (
        _.asInstanceOf[DomainValidation[CentreRemovedFromStudyEvent]])


  }

}
