package org.biobank.service.studies

import akka.actor.ActorRef
import akka.pattern.ask
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.biobank.domain.access._
import org.biobank.domain.study._
import org.biobank.domain.participants.CollectionEventRepository
import org.biobank.domain.user.UserId
import org.biobank.infrastructure.AscendingOrder
import org.biobank.infrastructure.command.CollectionEventTypeCommands._
import org.biobank.infrastructure.event.CollectionEventTypeEvents._
import org.biobank.service._
import org.biobank.service.access.AccessService
import org.slf4j.{Logger, LoggerFactory}
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

  def eventTypeWithId(requestUserId:         UserId,
                      studyId:               StudyId,
                      eventTypeId: CollectionEventTypeId): ServiceValidation[CollectionEventType]

  def eventTypeBySlug(requestUserId: UserId,
                      studySlug:     String,
                      eventTypeSlug: String): ServiceValidation[CollectionEventType]

  def eventTypeInUse(requestUserId: UserId, slug: String): ServiceValidation[Boolean]

  def list(requestUserId: UserId,
           studyId:       StudyId,
           filter:        FilterString,
           sort:          SortString): ServiceValidation[Seq[CollectionEventType]]

  def listByStudySlug(requestUserId: UserId,
                      studySlug:     String,
                      filter:        FilterString,
                      sort:          SortString): ServiceValidation[Seq[CollectionEventType]]

  def processCommand(cmd: CollectionEventTypeCommand)
      : Future[ServiceValidation[CollectionEventType]]

  def processRemoveCommand(cmd: RemoveCollectionEventTypeCmd)
      : Future[ServiceValidation[Boolean]]

  def snapshotRequest(requestUserId: UserId): ServiceValidation[Unit]

}

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
class CollectionEventTypeServiceImpl @Inject()(
  @Named("collectionEventType") val processor: ActorRef,
  val accessService:                           AccessService,
  val eventTypeRepository:                     CollectionEventTypeRepository,
  val studiesService:                          StudiesService,
  val specimenGroupRepository:                 SpecimenGroupRepository,
  val eventRepository:                         CollectionEventRepository)
                                            (implicit executionContext: BbwebExecutionContext)
    extends CollectionEventTypeService
    with AccessChecksSerivce
    with ServicePermissionChecks {

  import org.biobank.CommonValidations._

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def eventTypeWithId(requestUserId:         UserId,
                      studyId:               StudyId,
                      eventTypeId: CollectionEventTypeId): ServiceValidation[CollectionEventType] = {
    whenPermittedAndIsMember(requestUserId,
                             PermissionId.StudyRead,
                             Some(studyId),
                             None) { () =>
      withStudy(requestUserId, studyId) { study =>
        eventTypeRepository.withId(study.id, eventTypeId)
      }
    }
  }

  def eventTypeBySlug(requestUserId: UserId,
                      studySlug:     String,
                      eventTypeSlug: String): ServiceValidation[CollectionEventType] = {
    studiesService.getStudyBySlug(requestUserId, studySlug).flatMap { study =>
      eventTypeRepository.getBySlug(eventTypeSlug)
    }
  }

  def eventTypeInUse(requestUserId: UserId, slug: String): ServiceValidation[Boolean] = {
    eventTypeRepository.getBySlug(slug).flatMap { ceventType =>
      whenPermittedAndIsMember(requestUserId,
                               PermissionId.StudyRead,
                               Some(ceventType.studyId),
                               None) { () =>
        eventRepository.collectionEventTypeInUse(ceventType.id).successNel[String]
      }
    }
  }

  def list(requestUserId: UserId,
           studyId:       StudyId,
           filter:        FilterString,
           sort:          SortString): ServiceValidation[Seq[CollectionEventType]] = {
    whenPermittedAndIsMember(requestUserId,
                             PermissionId.StudyRead,
                             Some(studyId),
                             None) { () =>
      val sortStr = if (sort.expression.isEmpty) new SortString("name")
                    else sort

      for {
        study           <- studiesService.getStudy(requestUserId, studyId)
        ceventTypes     <- getEventTypes(studyId, filter)
        sortExpressions <- { QuerySortParser(sortStr).
                              toSuccessNel(ServiceError(s"could not parse sort expression: $sort")) }
        firstSort       <- { sortExpressions.headOption.
                              toSuccessNel(ServiceError("at least one sort expression is required")) }
        sortFunc        <- { CollectionEventType.sort2Compare.get(firstSort.name).
                              toSuccessNel(ServiceError(s"invalid sort field: ${firstSort.name}")) }
      } yield {
        val result = ceventTypes.toSeq.sortWith(sortFunc)
        if (firstSort.order == AscendingOrder) result
        else result.reverse
      }
    }
  }

   def listByStudySlug(requestUserId: UserId,
                       studySlug:     String,
                       filter:        FilterString,
                       sort:          SortString): ServiceValidation[Seq[CollectionEventType]] = {
     studiesService.getStudyBySlug(requestUserId, studySlug) flatMap { study =>
       list(requestUserId, study.id, filter, sort)
     }
   }

 def processCommand(cmd: CollectionEventTypeCommand): Future[ServiceValidation[CollectionEventType]] = {
    val v = for {
        validCommand <- cmd match {
          case c: RemoveCollectionEventTypeCmd =>
            ServiceError(s"invalid service call: $cmd, use processRemoveCommand").failureNel[DisabledStudy]
          case c => c.successNel[String]
        }
        study <- getDisabledStudy(cmd.sessionUserId, cmd.studyId)
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
            result <- eventTypeRepository.getByKey(CollectionEventTypeId(event.id))
          } yield result
        }
      }
    )
  }

  def processRemoveCommand(cmd: RemoveCollectionEventTypeCmd): Future[ServiceValidation[Boolean]] = {
    getDisabledStudy(cmd.sessionUserId, cmd.studyId).fold(
      err => Future.successful(err.failure[Boolean]),
      study => whenPermittedAndIsMemberAsync(UserId(cmd.sessionUserId),
                                             PermissionId.StudyUpdate,
                                             Some(study.id),
                                             None) { () =>
        ask(processor, cmd)
          .mapTo[ServiceValidation[CollectionEventTypeEvent]]
          .map { validation =>
            validation.map(event => true)
          }
      }
    )
  }

  private def withStudy[T](sessionUserId: UserId,
                           studyId:       StudyId)
                       (fn: Study => ServiceValidation[T]): ServiceValidation[T] = {
    for {
      study  <- studiesService.getStudy(sessionUserId, studyId)
      result <- fn(study)
    } yield result
  }

  private def getDisabledStudy(sessionUserId: String,
                               studyId:       String): ServiceValidation[DisabledStudy] = {
    studiesService.getStudy(UserId(sessionUserId), StudyId(studyId)).flatMap { study =>
      study match {
        case s: DisabledStudy => s.successNel[String]
        case s => InvalidStatus(s"study not disabled: $id").failureNel[DisabledStudy]
      }
    }
  }

  private def getEventTypes(studyId: StudyId, filter: FilterString)
      : ServiceValidation[Set[CollectionEventType]] = {
    val allCeventTypes = eventTypeRepository.allForStudy(studyId).toSet
    CollectionEventTypeFilter.filterCollectionEvents(allCeventTypes, filter)
  }
}
