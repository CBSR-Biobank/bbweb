package org.biobank.services.studies

import akka.actor.ActorRef
import akka.pattern.ask
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.biobank.domain.Slug
import org.biobank.domain.access._
import org.biobank.domain.studies._
import org.biobank.domain.users.UserId
import org.biobank.dto.{EntityInfoDto, ProcessedSpecimenDefinitionNames}
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

  def processingTypeById(requestUserId: UserId, studyId: StudyId, processingTypeId: ProcessingTypeId)
      : Future[ServiceValidation[ProcessingType]]

  def processingTypeBySlug(requestUserId: UserId, studySlug: Slug, processingTypeSlug: Slug)
      : Future[ServiceValidation[ProcessingType]]

  def processingTypesForStudy(requestUserId: UserId, studySlug: Slug, query: PagedQuery)
      : Future[ServiceValidation[PagedResults[ProcessingType]]]

  def processingTypeInUse(requestUserId: UserId, slug: Slug): ServiceValidation[Boolean]

  def specimenDefinitionsForStudy(requestUserId: UserId, studyId: StudyId)
      : Future[ServiceValidation[Set[ProcessedSpecimenDefinitionNames]]]

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

  def processingTypeById(requestUserId: UserId, studyId: StudyId, processingTypeId: ProcessingTypeId)
      : Future[ServiceValidation[ProcessingType]] = {
    Future {
      studiesService.getStudy(requestUserId, studyId).flatMap { study =>
        processingTypeRepository.getByKey(processingTypeId)
      }
    }
  }

  def processingTypeBySlug(requestUserId: UserId, studySlug: Slug, processingTypeSlug: Slug)
      : Future[ServiceValidation[ProcessingType]] = {
    Future {
      studiesService.getStudyBySlug(requestUserId, studySlug).flatMap { study =>
        processingTypeRepository.getBySlug(processingTypeSlug)
      }
    }
  }

  def processingTypesForStudy(requestUserId: UserId, studySlug: Slug, query: PagedQuery)
      : Future[ServiceValidation[PagedResults[ProcessingType]]] = {
    Future {
      for {
        study     <- studiesService.getStudyBySlug(requestUserId, studySlug)
        types     <- queryInternal(study.id, query.filter, query.sort)
        validPage <- query.validPage(types.size)
        results   <- PagedResults.create(types, query.page, query.limit)
      } yield results
    }
  }

  def processingTypeInUse(requestUserId: UserId, slug: Slug): ServiceValidation[Boolean] = {
    processingTypeRepository.getBySlug(slug).flatMap { processingType =>
      whenPermittedAndIsMember(requestUserId,
                               PermissionId.StudyRead,
                               Some(processingType.studyId),
                               None) { () =>
        processingTypeRepository.processingTypeInUse(processingType.id).successNel[String]
      }
    }
  }

 def specimenDefinitionsForStudy(requestUserId: UserId, studyId: StudyId)
      : Future[ServiceValidation[Set[ProcessedSpecimenDefinitionNames]]] = {
    Future {
      for {
        study           <- studiesService.getStudy(requestUserId, studyId)
        processingTypes <- queryInternal(study.id, new FilterString(""), new SortString(""))
      } yield {
         processingTypes.map { processingType =>
           val definition = processingType.output.specimenDefinition
           ProcessedSpecimenDefinitionNames(
             processingType.id.id,
             processingType.slug,
             processingType.name,
             EntityInfoDto(definition.id.id, definition.slug, definition.name))
         }.toSet
       }
    }
  }

  def processCommand(cmd: ProcessingTypeCommand): Future[ServiceValidation[ProcessingType]] = {
    val v = for {
        validCommand <- {
          cmd match {
              case c: RemoveProcessingTypeCmd =>
                ServiceError(s"invalid service call: $cmd").failureNel[DisabledStudy]
              case c => c.successNel[String]
            }
        }
        study <- studiesService.getDisabledStudy(UserId(cmd.sessionUserId), StudyId(cmd.studyId))
      } yield study

    v.fold(
      err => Future.successful(err.failure[ProcessingType]),
      study => whenPermittedAndIsMemberAsync(UserId(cmd.sessionUserId),
                                            PermissionId.StudyUpdate,
                                            Some(study.id),
                                            None) { () =>
        ask(processor, cmd).mapTo[ServiceValidation[ProcessingTypeEvent]].map { validation =>
          for {
            event  <- validation
            result <- processingTypeRepository.getByKey(ProcessingTypeId(event.id))
          } yield result
        }
      }
    )
  }

  def processRemoveCommand(cmd: ProcessingTypeCommand): Future[ServiceValidation[Boolean]] = {
    studiesService.getDisabledStudy(UserId(cmd.sessionUserId), StudyId(cmd.studyId)).fold(
      err => Future.successful(err.failure[Boolean]),
      study => whenPermittedAndIsMemberAsync(UserId(cmd.sessionUserId),
                                            PermissionId.StudyUpdate,
                                            Some(study.id),
                                            None) { () =>
        ask(processor, cmd).mapTo[ServiceValidation[ProcessingTypeEvent]]
          .map { validation => validation.map(_ => true) }
      }
    )
  }

  private def queryInternal(studyId: StudyId,
                            filter: FilterString,
                            sort: SortString)
      : ServiceValidation[Seq[ProcessingType]] = {
    val sortStr = if (sort.expression.isEmpty) new SortString("name")
                  else sort

    for {
      processingTypes <- getProcessingTypes(studyId, filter)
      sortExpressions <- { QuerySortParser(sortStr).
                           toSuccessNel(ServiceError(s"could not parse sort expression: $sortStr")) }
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

  private def getProcessingTypes(studyId: StudyId, filter: FilterString)
      : ServiceValidation[Set[ProcessingType]] = {
    val allProcessingTypes = processingTypeRepository.allForStudy(studyId).toSet
    ProcessingTypeFilter.filterProcessingTypes(allProcessingTypes, filter)
  }

}
