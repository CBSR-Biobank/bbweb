 package org.biobank.service.studies

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.ImplementedBy
import javax.inject._
import org.biobank.domain.centre.CentreRepository
import org.biobank.domain.participants.CollectionEventRepository
import org.biobank.domain.study._
import org.biobank.dto._
import org.biobank.dto.{ ProcessingDto }
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.CollectionEventTypeCommands._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.CollectionEventTypeEvents._
import org.biobank.infrastructure.event.ProcessingTypeEvents._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service._
import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent._
import scala.concurrent.duration._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[StudiesServiceImpl])
trait StudiesService {

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

  def specimenGroupWithId(studyId: StudyId, specimenGroupId: String)
      : ServiceValidation[SpecimenGroup]

  def specimenGroupsForStudy(studyId: StudyId): ServiceValidation[Set[SpecimenGroup]]

  def specimenGroupsInUse(studyId: StudyId): ServiceValidation[Set[SpecimenGroupId]]

  def collectionEventTypeWithId(studyId: StudyId, collectionEventTypeId: CollectionEventTypeId)
      : ServiceValidation[CollectionEventType]

  def collectionEventTypeInUse(collectionEventTypeId: CollectionEventTypeId)
      : ServiceValidation[Boolean]

  def collectionEventTypesForStudy(studyId: StudyId): ServiceValidation[Set[CollectionEventType]]

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

  def processCollectionEventTypeCommand(cmd: CollectionEventTypeCommand)
      : Future[ServiceValidation[CollectionEventType]]

  def processRemoveCollectionEventTypeCommand(cmd: RemoveCollectionEventTypeCmd)
      : Future[ServiceValidation[Boolean]]

  def processProcessingTypeCommand(cmd: StudyCommand)
      : Future[ServiceValidation[ProcessingType]]

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
class StudiesServiceImpl @javax.inject.Inject() (
  @Named("studiesProcessor") val studyProcessor:         ActorRef,
  @Named("collectionEventType") val ceventTypeProcessor: ActorRef,
  val studyRepository:                                   StudyRepository,
  val centreRepository:                                  CentreRepository,
  val processingTypeRepository:                          ProcessingTypeRepository,
  val specimenGroupRepository:                           SpecimenGroupRepository,
  val collectionEventTypeRepository:                     CollectionEventTypeRepository,
  val collectionEventRepository:                         CollectionEventRepository,
  val specimenLinkTypeRepository:                        SpecimenLinkTypeRepository)
    extends StudiesService {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

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
        CentreLocation(centre.id.id, location.uniqueId.id, centre.name, location.name)
      }
    }
  }

  def specimenGroupWithId(studyId: StudyId, specimenGroupId: String)
      : ServiceValidation[SpecimenGroup] = {
    validStudyId(studyId) { study =>
      specimenGroupRepository.withId(study.id, SpecimenGroupId(specimenGroupId))
    }
  }

  def specimenGroupsForStudy(studyId: StudyId) : ServiceValidation[Set[SpecimenGroup]] = {
    validStudyId(studyId) { study =>
      specimenGroupRepository.allForStudy(study.id).successNel
    }
  }

  def specimenGroupsInUse(studyId: StudyId): ServiceValidation[Set[SpecimenGroupId]] = {
    ???
    // validStudyId(studyId) { study =>
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
    validStudyId(studyId) { study =>
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
    validStudyId(studyId) { study =>
      collectionEventTypeRepository.allForStudy(study.id).successNel[String]
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
    ask(studyProcessor, cmd).mapTo[ServiceValidation[StudyEvent]].map { validation =>
      for {
        event <- validation
        study <- studyRepository.getByKey(StudyId(event.id))
      } yield study
    }

  def processCollectionEventTypeCommand(cmd: CollectionEventTypeCommand)
      : Future[ServiceValidation[CollectionEventType]] = {
    cmd match {
      case c: RemoveCollectionEventTypeCmd =>
        Future.successful(ServiceError(s"invalid service call: $cmd").failureNel[CollectionEventType])
      case _ => {
        studyRepository.getDisabled(StudyId(cmd.studyId)).fold(
          err => Future.successful(err.failure[CollectionEventType]),
          study => {
            ask(ceventTypeProcessor, cmd).mapTo[ServiceValidation[CollectionEventTypeEvent]]
              .map { validation =>
              for {
                event  <- validation
                result <- collectionEventTypeRepository.getByKey(CollectionEventTypeId(event.id))
              } yield result
            }
          }
        )
      }
    }
  }

  def processRemoveCollectionEventTypeCommand(cmd: RemoveCollectionEventTypeCmd)
      : Future[ServiceValidation[Boolean]] = {
    studyRepository.getDisabled(StudyId(cmd.studyId)).fold(
      err => Future.successful(err.failure[Boolean]),
      study => {
        ask(ceventTypeProcessor, cmd).mapTo[ServiceValidation[CollectionEventTypeEvent]]
          .map { validation => validation.map(_ => true) }
      }
    )
  }

  def processProcessingTypeCommand(cmd: StudyCommand)
      : Future[ServiceValidation[ProcessingType]] = {
    cmd match {
      case c: RemoveProcessingTypeCmd =>
        Future.successful(ServiceError(s"invalid service call: $cmd").failureNel[ProcessingType])
      case _ =>
        ask(studyProcessor, cmd).mapTo[ServiceValidation[ProcessingTypeEvent]].map { validation =>
          for {
            event  <- validation
            result <- processingTypeRepository.getByKey(ProcessingTypeId(event.id))
          } yield result
        }
    }
  }

  def processRemoveProcessingTypeCommand(cmd: StudyCommand)
      : Future[ServiceValidation[Boolean]] =
    ask(studyProcessor, cmd).mapTo[ServiceValidation[ProcessingTypeEvent]]
      .map { validation => validation.map(_ => true) }

}
