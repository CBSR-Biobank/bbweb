package org.biobank.services.studies

import akka.actor.ActorRef
import akka.pattern.ask
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.biobank.domain.Slug
import org.biobank.domain.access._
import org.biobank.domain.studies._
import org.biobank.domain.users.UserId
import org.biobank.infrastructure.AscendingOrder
import org.biobank.infrastructure.commands.ProcessingTypeCommands._
import org.biobank.infrastructure.events.ProcessingTypeEvents._
import org.biobank.services._
import org.biobank.services.access.AccessService
import org.slf4j.{Logger, LoggerFactory}
import scala.concurrent.Future
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

/**
 * This is the ProcessingType Aggregate Application Service.
 *
 * Handles the commands to configure Collection Event Types. the commands are forwarded to the
 * ProcessingType Aggregate Processor.
 *
 */
@ImplementedBy(classOf[ProcessingTypeServiceImpl])
trait ProcessingTypeService extends BbwebService {

  def processingTypeBySlug(requestUserId:      UserId,
                           studySlug:          Slug,
                           processingTypeSlug: Slug): ServiceValidation[ProcessingType]

  def processingTypesForStudy(requestUserId: UserId, studyId: StudyId)
      : ServiceValidation[Set[ProcessingType]]

  def processCommand(cmd: ProcessingTypeCommand): Future[ServiceValidation[ProcessingType]]

  def processRemoveCommand(cmd: ProcessingTypeCommand): Future[ServiceValidation[Boolean]]

  def snapshotRequest(requestUserId: UserId): ServiceValidation[Unit]

}

@SuppressWarnings(Array("org.wartremover.warts.ImplicitParameter"))
class ProcessingTypeServiceImpl @Inject() (
  @Named("processingType") val processor: ActorRef,
  val accessService:                      AccessService,
  val processingTypeRepository:           ProcessingTypeRepository,
  val studiesService:                     StudiesService
) (
  implicit executionContext: BbwebExecutionContext
)
    extends ProcessingTypeService
    with AccessChecksSerivce
    with ServicePermissionChecks {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def processingTypeBySlug(requestUserId:      UserId,
                           studySlug:          Slug,
                           processingTypeSlug: Slug): ServiceValidation[ProcessingType] = {
    studiesService.getStudyBySlug(requestUserId, studySlug).flatMap { study =>
      processingTypeRepository.getBySlug(processingTypeSlug)
    }
  }

  def list(requestUserId: UserId,
           studyId:       StudyId,
           filter:        FilterString,
           sort:          SortString): ServiceValidation[Seq[ProcessingType]] = {
    whenPermittedAndIsMember(requestUserId,
                             PermissionId.StudyRead,
                             Some(studyId),
                             None) { () =>
      val sortStr = if (sort.expression.isEmpty) new SortString("name")
                    else sort

      for {
        study           <- studiesService.getStudy(requestUserId, studyId)
        processingTypes <- getEventTypes(studyId, filter)
        sortExpressions <- { QuerySortParser(sortStr).
                              toSuccessNel(ServiceError(s"could not parse sort expression: $sort")) }
        firstSort       <- { sortExpressions.headOption.
                              toSuccessNel(ServiceError("at least one sort expression is required")) }
        sortFunc        <- { ProcessingType.sort2Compare.get(firstSort.name).
                              toSuccessNel(ServiceError(s"invalid sort field: ${firstSort.name}")) }
      } yield {
        val result = processingTypes.toSeq.sortWith(sortFunc)
        if (firstSort.order == AscendingOrder) result
        else result.reverse
      }
    }
  }



  def processingTypesForStudy(requestUserId: UserId, studyId: StudyId)
      : ServiceValidation[Set[ProcessingType]] = {
    whenPermittedAndIsMember(requestUserId,
                             PermissionId.StudyRead,
                             Some(studyId),
                             None) { () =>
      withStudy(requestUserId, studyId) { study =>
        processingTypeRepository.allForStudy(study.id).successNel[String]
      }
    }
  }

  def processCommand(cmd: ProcessingTypeCommand)
      : Future[ServiceValidation[ProcessingType]] = {
    cmd match {
      case c: RemoveProcessingTypeCmd =>
        Future.successful(ServiceError(s"invalid service call: $cmd").failureNel[ProcessingType])
      case _ =>
        ask(processor, cmd).mapTo[ServiceValidation[ProcessingTypeEvent]].map { validation =>
          for {
            event  <- validation
            result <- processingTypeRepository.getByKey(ProcessingTypeId(event.id))
          } yield result
        }
    }
  }

  def processRemoveCommand(cmd: ProcessingTypeCommand)
      : Future[ServiceValidation[Boolean]] =
    ask(processor, cmd).mapTo[ServiceValidation[ProcessingTypeEvent]]
      .map { validation => validation.map(_ => true) }

  private def withStudy[T](sessionUserId: UserId,
                           studyId:       StudyId)
                       (fn: Study => ServiceValidation[T]): ServiceValidation[T] = {
    for {
      study  <- studiesService.getStudy(sessionUserId, studyId)
      result <- fn(study)
    } yield result
  }

  private def getEventTypes(studyId: StudyId, filter: FilterString)
      : ServiceValidation[Set[ProcessingType]] = {
    val allProcessingTypes = processingTypeRepository.allForStudy(studyId).toSet
    ProcessingTypeFilter.filterProcessingTypes(allProcessingTypes, filter)
  }

}
