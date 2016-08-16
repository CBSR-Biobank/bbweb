package org.biobank.service.participants

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.biobank.domain.centre.CentreRepository
import org.biobank.domain.participants._
import org.biobank.domain.study._
import org.biobank.dto.{CentreLocationInfo, SpecimenDto}
import org.biobank.infrastructure.command.SpecimenCommands._
import org.biobank.infrastructure.event.SpecimenEvents._
import org.biobank.infrastructure.{ SortOrder, AscendingOrder }
import org.biobank.service.ServiceValidation
import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent._
import scala.concurrent.duration._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[SpecimensServiceImpl])
trait SpecimensService {

  def get(specimenId: String): ServiceValidation[SpecimenDto]

  def getByInventoryId(inventoryId: String): ServiceValidation[SpecimenDto]

  def list(collectionEventId: String,
           sortFunc:          (Specimen, Specimen) => Boolean,
           order:             SortOrder)
      : ServiceValidation[Seq[SpecimenDto]]

  def processCommand(cmd: SpecimenCommand): Future[ServiceValidation[CollectionEvent]]

  def processRemoveCommand(cmd: SpecimenCommand): Future[ServiceValidation[Boolean]]

}

class SpecimensServiceImpl @Inject() (
  @Named("specimensProcessor") val processor: ActorRef,
  val studyRepository:                        StudyRepository,
  val collectionEventRepository:              CollectionEventRepository,
  val collectionEventTypeRepository:          CollectionEventTypeRepository,
  val ceventSpecimenRepository:               CeventSpecimenRepository,
  val specimenRepository:                     SpecimenRepository,
  val centreRepository:                       CentreRepository)
    extends SpecimensService {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  private def convertToDto(specimen: Specimen): ServiceValidation[SpecimenDto] = {
    for {
      ceventSpecimen     <- ceventSpecimenRepository.withSpecimenId(specimen.id)
      cevent             <- collectionEventRepository.getByKey(ceventSpecimen.ceventId)
      ceventType         <- collectionEventTypeRepository.getByKey(cevent.collectionEventTypeId)
      specimenSpec       <- ceventType.specimenSpec(specimen.specimenSpecId)
      originCentre       <- centreRepository.getByLocationId(specimen.originLocationId)
      originLocationName <- originCentre.locationName(specimen.originLocationId)
      centre             <- centreRepository.getByLocationId(specimen.locationId)
      locationName       <- centre.locationName(specimen.locationId)
    } yield {
      val originLocationInfo = CentreLocationInfo(originCentre.id.id,
                                                  specimen.originLocationId,
                                                  originLocationName)
      val locationInfo = CentreLocationInfo(centre.id.id,
                                            specimen.locationId,
                                            locationName)
      specimen.createDto(cevent, specimenSpec, originLocationInfo, locationInfo)
    }
  }

  def get(specimenId: String): ServiceValidation[SpecimenDto] = {
    for {
      specimen <- specimenRepository.getByKey(SpecimenId(specimenId))
      dto      <- convertToDto(specimen)
    } yield dto
  }

  def getByInventoryId(inventoryId: String): ServiceValidation[SpecimenDto] = {
    for {
      specimen <- specimenRepository.getByInventoryId(inventoryId)
      dto      <- convertToDto(specimen)
    } yield dto
  }

  def list(collectionEventId: String,
           sortFunc:          (Specimen, Specimen) => Boolean,
           order:             SortOrder)
      : ServiceValidation[Seq[SpecimenDto]] = {

    def getSpecimens(ceventId: CollectionEventId): ServiceValidation[List[Specimen]] = {
      ceventSpecimenRepository.withCeventId(ceventId)
        .map { cs => specimenRepository.getByKey(cs.specimenId) }
        .toList
        .sequenceU
        .map { list => {
                val result = list.sortWith(sortFunc)
                if (order == AscendingOrder) result else result.reverse
              }
      }
    }

    validCevent(collectionEventId) { cevent =>
      getSpecimens(cevent.id).flatMap { specimens => specimens.map(convertToDto).sequenceU }
    }
  }

  def processCommand(cmd: SpecimenCommand): Future[ServiceValidation[CollectionEvent]] =
    ask(processor, cmd).mapTo[ServiceValidation[SpecimenEvent]].map { validation =>
      for {
        event  <- validation
        cevent <- collectionEventRepository.getByKey(CollectionEventId(event.getAdded.getCollectionEventId))
      } yield cevent
    }

  def processRemoveCommand(cmd: SpecimenCommand): Future[ServiceValidation[Boolean]] =
    ask(processor, cmd).mapTo[ServiceValidation[SpecimenEvent]].map { validation =>
      for {
        event  <- validation
        result <- true.successNel[String]
      } yield result
    }

  private def validCevent[T](ceventId: String)(fn: CollectionEvent => ServiceValidation[T])
      : ServiceValidation[T] = {
    for {
      cevent <- collectionEventRepository.getByKey(CollectionEventId(ceventId))
      result <- fn(cevent)
    } yield result
  }

}
