package org.biobank.service.participants

import org.biobank.infrastructure.{ SortOrder, AscendingOrder }
import org.biobank.infrastructure.command.SpecimenCommands._
import org.biobank.infrastructure.event.SpecimenEvents._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.domain.participants._

import javax.inject.{Inject, Named}
import com.google.inject.ImplementedBy
import akka.actor._
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent._
import scala.concurrent.duration._
import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[SpecimensServiceImpl])
trait SpecimensService {

  def get(specimenId: String): DomainValidation[Specimen]

  def list(collectionEventId: String,
           sortFunc:          (Specimen, Specimen) => Boolean,
           order:             SortOrder)
      : DomainValidation[Seq[Specimen]]

  def processAddCommand(cmd: SpecimenCommand): Future[DomainValidation[CollectionEvent]]

  def processRemoveCommand(cmd: SpecimenCommand): Future[DomainValidation[Boolean]]

}

class SpecimensServiceImpl @Inject() (
  @Named("specimensProcessor") val processor: ActorRef,
  val studyRepository:                        StudyRepository,
  val collectionEventRepository:              CollectionEventRepository,
  val ceventSpecimenRepository:               CeventSpecimenRepository,
  val specimenRepository:                     SpecimenRepository)
    extends SpecimensService {

  //import org.biobank.CommonValidations._

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  def get(specimenId: String): DomainValidation[Specimen] = {
    specimenRepository.getByKey(SpecimenId(specimenId)).leftMap(_ =>
      DomainError(s"collection event id is invalid: $specimenId")).toValidationNel
  }

  def list(collectionEventId: String,
           sortFunc:          (Specimen, Specimen) => Boolean,
           order:             SortOrder)
      : DomainValidation[Seq[Specimen]] = {
    validCevent(collectionEventId) { cevent =>
      ceventSpecimenRepository.withCeventId(cevent.id)
        .map { x => specimenRepository.getByKey(x.specimenId) }
        .toList
        .sequenceU
        .map { list => {
                var result = list.toSeq
                result.sortWith(sortFunc)
                if (order == AscendingOrder) {
                  result
                } else {
                  result.reverse
                }
              }
      }
    }
  }

  def processAddCommand(cmd: SpecimenCommand): Future[DomainValidation[CollectionEvent]] =
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
        result <- true.success
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
