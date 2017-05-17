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

  def specimenGroupWithId(requestUserId: UserId, studyId: StudyId, specimenGroupId: String)
      : ServiceValidation[SpecimenGroup]

  def specimenGroupsInUse(studyId: StudyId): ServiceValidation[Set[SpecimenGroupId]]

  def specimenGroupsForStudy(studyId: StudyId): ServiceValidation[Set[SpecimenGroup]]

  def collectionEventTypeWithId(studyId: StudyId, collectionEventTypeId: CollectionEventTypeId)
      : ServiceValidation[CollectionEventType]

  def collectionEventTypeInUse(collectionEventTypeId: CollectionEventTypeId)
      : ServiceValidation[Boolean]

  def collectionEventTypesForStudy(studyId: StudyId): ServiceValidation[Set[CollectionEventType]]

  def processCommand(cmd: CollectionEventTypeCommand)
      : Future[ServiceValidation[CollectionEventType]]

  def processRemoveCommand(cmd: RemoveCollectionEventTypeCmd)
      : Future[ServiceValidation[Boolean]]

}

class CollectionEventTypeServiceImpl @Inject()(
  @Named("collectionEventType") val processor: ActorRef,
  val accessService:                 AccessService,
  val collectionEventTypeRepository: CollectionEventTypeRepository,
  val studyRepository:               StudyRepository,
  val specimenGroupRepository:       SpecimenGroupRepository,
  val collectionEventRepository:     CollectionEventRepository)
    extends CollectionEventTypeService
    with BbwebServiceImpl
    with ServiceWithPermissionChecks {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def specimenGroupWithId(requestUserId: UserId, studyId: StudyId, specimenGroupId: String)
      : ServiceValidation[SpecimenGroup] = {
    whenPermitted(requestUserId, PermissionId.StudyRead) { () =>
      withStudy(studyId) { study =>
        specimenGroupRepository.withId(study.id, SpecimenGroupId(specimenGroupId))
      }
    }
  }

  def specimenGroupsForStudy(studyId: StudyId) : ServiceValidation[Set[SpecimenGroup]] = {
    withStudy(studyId) { study =>
      specimenGroupRepository.allForStudy(study.id).successNel
    }
  }

  def specimenGroupsInUse(studyId: StudyId): ServiceValidation[Set[SpecimenGroupId]] = {
    ???
    // withStudy(studyId) { study =>
    //     val cetSpecimenGroupIds = for {
    //       ceventType <- collectionEventTypeRepository.allForStudy(study.id)
    //       sgItem     <- ceventType.specimenGroupData
    //     } yield SpecimenGroupId(sgItem.specimenGroupId)

    //     val sltSpecimenGroupIds = for {
    //       processingType   <- processingTypeRepository.allForStudy(study.id)
    //       specimenLinkType <- specimenLinkTypeRepository.allForProcessingType(processingType.id)
    //       sgId             <- Set(specimenLinkType.inputGroupId, specimenLinkType.outputGroupId)
    //     } yield sgId

    //     (cetSpecimenGroupIds ++ sltSpecimenGroupIds).success
    //   }
  }

  def collectionEventTypeWithId(studyId: StudyId, collectionEventTypeId: CollectionEventTypeId)
      : ServiceValidation[CollectionEventType] = {
    withStudy(studyId) { study =>
      collectionEventTypeRepository.withId(study.id, collectionEventTypeId)
    }
  }

  def collectionEventTypeInUse(id: CollectionEventTypeId): ServiceValidation[Boolean] = {
    collectionEventTypeRepository.getByKey(id).map { ceventType =>
      collectionEventRepository.collectionEventTypeInUse(id)
    }
  }

  def collectionEventTypesForStudy(studyId: StudyId)
      : ServiceValidation[Set[CollectionEventType]] = {
    withStudy(studyId) { study =>
      collectionEventTypeRepository.allForStudy(study.id).successNel[String]
    }
  }

  def processCommand(cmd: CollectionEventTypeCommand): Future[ServiceValidation[CollectionEventType]] = {
    val commandValid = cmd match {
        case c: RemoveCollectionEventTypeCmd =>
          ServiceError(s"invalid service call: $cmd").failureNel[DisabledStudy]
        case _ =>
          studyRepository.getDisabled(StudyId(cmd.studyId))
      }

    commandValid.fold(
      err => Future.successful(err.failure[CollectionEventType]),
      study => {
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
      study => {
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
