package org.biobank.service.participants

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.biobank.domain._
import org.biobank.domain.participants._
import org.biobank.domain.study._
import org.biobank.domain.centre.CentreRepository
import org.biobank.dto.SpecimenDto
import org.biobank.infrastructure.command.SpecimenCommands._
import org.biobank.infrastructure.event.SpecimenEvents._
import org.biobank.infrastructure.{ SortOrder, AscendingOrder }
import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent._
import scala.concurrent.duration._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[SpecimensServiceImpl])
trait SpecimensService {

  def get(specimenId: String): DomainValidation[Specimen]

  def getByInventoryId(inventoryId: String): DomainValidation[Specimen]

  def list(collectionEventId: String,
           sortFunc:          (Specimen, Specimen) => Boolean,
           order:             SortOrder)
      : DomainValidation[Seq[SpecimenDto]]

  def processCommand(cmd: SpecimenCommand): Future[DomainValidation[CollectionEvent]]

  def processRemoveCommand(cmd: SpecimenCommand): Future[DomainValidation[Boolean]]

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

  private def convertToDto(collectionEventId: CollectionEventId,
                           ceventTypeId: CollectionEventTypeId,
                           specimen: Specimen)
      : DomainValidation[SpecimenDto] = {
    for {
      ceventType         <- collectionEventTypeRepository.getByKey(ceventTypeId)
      specimenSpec       <- ceventType.specimenSpec(specimen.specimenSpecId)
      originCentre       <- centreRepository.getByLocationId(specimen.originLocationId)
      originLocationName <- originCentre.locationName(specimen.originLocationId)
      centre             <- centreRepository.getByLocationId(specimen.locationId)
      centreLocationName <- centre.locationName(specimen.locationId)
    } yield SpecimenDto(id                 = specimen.id.id,
                        inventoryId        = specimen.inventoryId,
                        collectionEventId  = collectionEventId.id,
                        specimenSpecId     = specimen.specimenSpecId,
                        specimenSpecName   = specimenSpec.name,
                        version            = specimen.version,
                        timeAdded          = specimen.timeAdded,
                        timeModified       = specimen.timeModified,
                        originLocationId   = specimen.originLocationId,
                        originLocationName = originLocationName,
                        locationId         = specimen.locationId,
                        locationName       = centreLocationName,
                        containerId        = specimen.containerId.map(_.id),
                        positionId         = specimen.positionId.map(_.id),
                        timeCreated        = specimen.timeCreated,
                        amount             = specimen.amount,
                        units              = specimenSpec.units,
                        status             = specimen.getClass.getSimpleName)
  }

  def get(specimenId: String): DomainValidation[Specimen] = {
    specimenRepository.getByKey(SpecimenId(specimenId)).leftMap(_ =>
      DomainError(s"specimen id is invalid: $specimenId")).toValidationNel
  }

  def getByInventoryId(inventoryId: String): DomainValidation[Specimen] = {
    specimenRepository.getByInventoryId(inventoryId)
  }

  def list(collectionEventId: String,
           sortFunc:          (Specimen, Specimen) => Boolean,
           order:             SortOrder)
      : DomainValidation[Seq[SpecimenDto]] = {

    def getSpecimens(ceventId: CollectionEventId): DomainValidation[List[Specimen]] = {
      ceventSpecimenRepository.withCeventId(ceventId)
        .map { cs => {
                val r = specimenRepository.getByKey(cs.specimenId)
                log.info(s"-------->  $r")
                r
              }
      }
        .toList
        .sequenceU
        .map { list => {
                val result = list.sortWith(sortFunc)
                if (order == AscendingOrder) result else result.reverse
              }
        }
    }

    validCevent(collectionEventId) { cevent =>
      getSpecimens(cevent.id).flatMap { specimens =>
        specimens.map { s => convertToDto(cevent.id, cevent.collectionEventTypeId, s) }.sequenceU
      }
    }
  }

  def processCommand(cmd: SpecimenCommand): Future[DomainValidation[CollectionEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[SpecimenEvent]].map { validation =>
      for {
        event  <- validation
        cevent <- collectionEventRepository.getByKey(CollectionEventId(event.getAdded.getCollectionEventId))
      } yield cevent
    }

  def processRemoveCommand(cmd: SpecimenCommand): Future[DomainValidation[Boolean]] =
    ask(processor, cmd).mapTo[DomainValidation[SpecimenEvent]].map { validation =>
      for {
        event  <- validation
        result <- true.successNel[String]
      } yield result
    }

  private def validCevent[T](ceventId: String)(fn: CollectionEvent => DomainValidation[T])
      : DomainValidation[T] = {
    for {
      cevent <- collectionEventRepository.getByKey(CollectionEventId(ceventId))
      result <- fn(cevent)
    } yield result
  }

}
