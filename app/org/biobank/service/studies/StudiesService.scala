 package org.biobank.service.studies

import akka.actor._
import akka.pattern.ask
import com.google.inject.ImplementedBy
import javax.inject._
import org.biobank.domain.centre.CentreRepository
import org.biobank.domain.participants.CollectionEventRepository
import org.biobank.domain.study._
import org.biobank.dto._
import org.biobank.dto.{ ProcessingDto }
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.ProcessingTypeEvents._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[StudiesServiceImpl])
trait StudiesService extends BbwebService {

  def getStudyCount(): Int

  def getCountsByStatus(): StudyCountsByStatus

  /**
   * Returns a set of studies. The entities can be filtered and or sorted using expressions.
   *
   * @param filter the string representation of the filter expression to use to filter the studies.
   *
   * @param sort the string representation of the sort expression to use when sorting the studies.
   */
  def getStudies(filter: FilterString, sort: SortString): ServiceValidation[Seq[Study]]

  def getStudyNames(filter: FilterString, sort: SortString): ServiceValidation[Seq[NameDto]]

  def getStudy(id: StudyId): ServiceValidation[Study]

  def getCentresForStudy(studyId: StudyId): Set[CentreLocation]

  def processingTypeWithId(studyId: StudyId, processingTypeId: ProcessingTypeId)
      : ServiceValidation[ProcessingType]

  def processingTypesForStudy(studyId: StudyId): ServiceValidation[Set[ProcessingType]]

  def specimenLinkTypeWithId(processingTypeId: ProcessingTypeId,
                             specimenLinkTypeId: SpecimenLinkTypeId)
      : ServiceValidation[SpecimenLinkType]

  def specimenLinkTypesForProcessingType(processingTypeId: ProcessingTypeId)
      : ServiceValidation[Set[SpecimenLinkType]]

  def getProcessingDto(studyId: StudyId): ServiceValidation[ProcessingDto]

  def processCommand(cmd: StudyCommand): Future[ServiceValidation[Study]]

  // FIXME: move to its own service
  def processProcessingTypeCommand(cmd: StudyCommand)
      : Future[ServiceValidation[ProcessingType]]

  // FIXME: move to its own service
  def processRemoveProcessingTypeCommand(cmd: StudyCommand)
      : Future[ServiceValidation[Boolean]]
}

/**
  * This is the Study Aggregate Application Service.
  *
  * Handles the commands to configure studies. the commands are forwarded to the Study Aggregate
  * Processor.
  *
  * @param studiesProcessor
  *
 */
class StudiesServiceImpl @Inject() (
  @Named("studiesProcessor") val processor: ActorRef,
  val studyRepository:                      StudyRepository,
  val centreRepository:                     CentreRepository,
  val processingTypeRepository:             ProcessingTypeRepository,
  val specimenGroupRepository:              SpecimenGroupRepository,
  val collectionEventTypeRepository:        CollectionEventTypeRepository,
  val collectionEventRepository:            CollectionEventRepository,
  val specimenLinkTypeRepository:           SpecimenLinkTypeRepository)
    extends StudiesService
    with BbwebServiceImpl {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def getStudyCount(): Int = {
    studyRepository.getValues.size
  }

  def getCountsByStatus(): StudyCountsByStatus = {
    // FIXME should be replaced by DTO query to the database
    val studies = studyRepository.getValues
    StudyCountsByStatus(
      total         = studies.size.toLong,
      disabledCount = studies.collect { case s: DisabledStudy => s }.size.toLong,
      enabledCount  = studies.collect { case s: EnabledStudy => s }.size.toLong,
      retiredCount  = studies.collect { case s: RetiredStudy => s }.size.toLong
    )
  }

  def getStudies(filter: FilterString, sort: SortString): ServiceValidation[Seq[Study]] = {
    val allStudies = studyRepository.getValues.toSet
    val sortStr = if (sort.expression.isEmpty) new SortString("name")
                  else sort

    for {
      studies <- StudyFilter.filterStudies(allStudies, filter)
      sortExpressions <- {
        QuerySortParser(sortStr).toSuccessNel(ServiceError(s"could not parse sort expression: $sort"))
      }
      sortFunc <- {
        Study.sort2Compare.get(sortExpressions(0).name).
          toSuccessNel(ServiceError(s"invalid sort field: ${sortExpressions(0).name}"))
      }
    } yield {
      val result = studies.toSeq.sortWith(sortFunc)
      if (sortExpressions(0).order == AscendingOrder) result
      else result.reverse
    }
  }

  def getStudyNames(filter: FilterString, sort: SortString): ServiceValidation[Seq[NameDto]] = {
    getStudies(filter, sort).map(_.map(s => NameDto(s.id.id, s.name, s.state.id)))
  }

  def getStudy(id: StudyId) : ServiceValidation[Study] = {
    studyRepository.getByKey(id)
  }

  def getCentresForStudy(studyId: StudyId): Set[CentreLocation] = {
    centreRepository.withStudy(studyId).flatMap { centre =>
      centre.locations.map { location =>
        CentreLocation(centre.id.id, location.id.id, centre.name, location.name)
      }
    }
  }

  def processingTypeWithId(studyId: StudyId, processingTypeId: ProcessingTypeId)
      : ServiceValidation[ProcessingType] = {
    validStudyId(studyId) { study =>
      processingTypeRepository.withId(study.id, processingTypeId)
    }
  }

  def processingTypesForStudy(studyId: StudyId)
      : ServiceValidation[Set[ProcessingType]] = {
    validStudyId(studyId) { study =>
      processingTypeRepository.allForStudy(study.id).successNel[String]
    }
  }

  def specimenLinkTypeWithId(processingTypeId: ProcessingTypeId,
                             specimenLinkTypeId: SpecimenLinkTypeId)
      : ServiceValidation[SpecimenLinkType] = {
    validProcessingTypeId(processingTypeId) { processingType =>
      specimenLinkTypeRepository.withId(processingType.id, specimenLinkTypeId)
    }
  }

  def specimenLinkTypesForProcessingType(processingTypeId: ProcessingTypeId)
      : ServiceValidation[Set[SpecimenLinkType]] = {
    validProcessingTypeId(processingTypeId) { processingType =>
      specimenLinkTypeRepository.allForProcessingType(processingType.id).successNel[String]
    }
  }

  def getProcessingDto(studyId: StudyId): ServiceValidation[ProcessingDto] = {
    "deprectated: annot type refactor".failureNel[ProcessingDto]
  }

  private def validStudyId[T](studyId: StudyId)(fn: Study => ServiceValidation[T])
      : ServiceValidation[T] = {
    for {
      study <- studyRepository.getByKey(studyId)
      result <- fn(study)
    } yield result
  }

  private def validProcessingTypeId[T](processingTypeId: ProcessingTypeId)
                                   (fn: ProcessingType => ServiceValidation[T])
      : ServiceValidation[T] = {
    for {
      pt <- processingTypeRepository.getByKey(processingTypeId)
      study <- studyRepository.getByKey(pt.studyId)
      result <- fn(pt)
    } yield result
  }

  def processCommand(cmd: StudyCommand): Future[ServiceValidation[Study]] =
    ask(processor, cmd).mapTo[ServiceValidation[StudyEvent]].map { validation =>
      for {
        event <- validation
        study <- studyRepository.getByKey(StudyId(event.id))
      } yield study
    }

  def processProcessingTypeCommand(cmd: StudyCommand)
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

  def processRemoveProcessingTypeCommand(cmd: StudyCommand)
      : Future[ServiceValidation[Boolean]] =
    ask(processor, cmd).mapTo[ServiceValidation[ProcessingTypeEvent]]
      .map { validation => validation.map(_ => true) }

}
