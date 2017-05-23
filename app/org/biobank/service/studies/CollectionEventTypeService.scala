package org.biobank.service.studies

import akka.actor.ActorRef
import akka.pattern.ask
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.biobank.domain.access._
import org.biobank.domain.study._
import org.biobank.domain.participants.CollectionEventRepository
import org.biobank.domain.user.UserId
import org.biobank.infrastructure.command.CollectionEventTypeCommands._
import org.biobank.infrastructure.event.CollectionEventTypeEvents._
import org.biobank.service._
import org.biobank.service.access.AccessService
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * This is the CollectionEventType Aggregate Application Service.
 *
 * Handles the commands to configure Collection Event Types. the commands are forwarded to the
 * CollectionEventType Aggregate Processor.
 *
 */
@ImplementedBy(classOf[CollectionEventTypeServiceImpl])
trait CollectionEventTypeService extends BbwebService {

  def collectionEventTypeWithId(requestUserId:         UserId,
                                studyId:               StudyId,
                                collectionEventTypeId: CollectionEventTypeId)
      : ServiceValidation[CollectionEventType]

  def collectionEventTypeInUse(requestUserId: UserId, collectionEventTypeId: CollectionEventTypeId)
      : ServiceValidation[Boolean]

  def collectionEventTypesForStudy(requestUserId: UserId, studyId: StudyId)
      : ServiceValidation[Set[CollectionEventType]]

  def processCommand(cmd: CollectionEventTypeCommand)
      : Future[ServiceValidation[CollectionEventType]]

  def processRemoveCommand(cmd: RemoveCollectionEventTypeCmd)
      : Future[ServiceValidation[Boolean]]

  def snapshotRequest(requestUserId: UserId): ServiceValidation[Unit]

}

class CollectionEventTypeServiceImpl @Inject()(
  @Named("collectionEventType") val processor: ActorRef,
  val accessService:                 AccessService,
  val collectionEventTypeRepository: CollectionEventTypeRepository,
  val studyRepository:               StudyRepository,
  val specimenGroupRepository:       SpecimenGroupRepository,
  val collectionEventRepository:     CollectionEventRepository)
    extends CollectionEventTypeService with ServiceWithPermissionChecks {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def collectionEventTypeWithId(requestUserId:         UserId,
                                studyId:               StudyId,
                                collectionEventTypeId: CollectionEventTypeId)
      : ServiceValidation[CollectionEventType] = {
    whenPermitted(requestUserId, PermissionId.StudyRead) { () =>
      withStudy(studyId) { study =>
        collectionEventTypeRepository.withId(study.id, collectionEventTypeId)
      }
    }
  }

  def collectionEventTypeInUse(requestUserId: UserId, id: CollectionEventTypeId): ServiceValidation[Boolean] = {
    whenPermitted(requestUserId, PermissionId.StudyRead) { () =>
      collectionEventTypeRepository.getByKey(id).map { ceventType =>
        collectionEventRepository.collectionEventTypeInUse(id)
      }
    }
  }

  def collectionEventTypesForStudy(requestUserId: UserId, studyId: StudyId)
      : ServiceValidation[Set[CollectionEventType]] = {
    whenPermitted(requestUserId, PermissionId.StudyRead) { () =>
      withStudy(studyId) { study =>
        collectionEventTypeRepository.allForStudy(study.id).successNel[String]
      }
    }
  }

  def processCommand(cmd: CollectionEventTypeCommand): Future[ServiceValidation[CollectionEventType]] = {
    val v = for {
        validCommand <- cmd match {
          case c: RemoveCollectionEventTypeCmd =>
            ServiceError(s"invalid service call: $cmd, use processRemoveCommand").failureNel[DisabledStudy]
          case c => c.successNel[String]
        }
        study  <- studyRepository.getDisabled(StudyId(cmd.studyId))
      } yield study

    v.fold(
      err => Future.successful(err.failure[CollectionEventType]),
      study => whenPermittedAndIsMemberAsync(UserId(cmd.sessionUserId),
                                             PermissionId.StudyUpdate,
                                             Some(study.id),
                                             None) { () =>
        ask(processor, cmd).mapTo[ServiceValidation[CollectionEventTypeEvent]].map { validation =>
          for {
            event  <- validation
            result <- collectionEventTypeRepository.getByKey(CollectionEventTypeId(event.id))
          } yield result
        }
      }
    )
  }

  def processRemoveCommand(cmd: RemoveCollectionEventTypeCmd): Future[ServiceValidation[Boolean]] = {
    studyRepository.getDisabled(StudyId(cmd.studyId)).fold(
      err => Future.successful(err.failure[Boolean]),
      study => whenPermittedAndIsMemberAsync(UserId(cmd.sessionUserId),
                                             PermissionId.StudyUpdate,
                                             Some(study.id),
                                             None) { () =>
        ask(processor, cmd).mapTo[ServiceValidation[CollectionEventTypeEvent]].map { validation =>
          validation.map(_ => true)
        }
      }
    )
  }

  private def withStudy[T](studyId: StudyId)(fn: Study => ServiceValidation[T]): ServiceValidation[T] = {
    for {
      study <- studyRepository.getByKey(studyId)
      result <- fn(study)
    } yield result
  }
}
