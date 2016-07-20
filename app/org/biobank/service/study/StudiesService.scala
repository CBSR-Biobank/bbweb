 package org.biobank.service.study

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.ImplementedBy
import javax.inject._
import org.biobank.domain.study._
import org.biobank.domain.centre.CentreRepository
import org.biobank.domain.participants.CollectionEventRepository
import org.biobank.domain.{ DomainValidation, DomainError }
import org.biobank.dto._
import org.biobank.dto.{ ProcessingDto }
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.command.CollectionEventTypeCommands._
import org.biobank.infrastructure.event.CollectionEventTypeEvents._
import org.biobank.infrastructure.event.ProcessingTypeEvents._
import org.biobank.infrastructure.event.StudyEvents._
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

  def getStudies(filter:   String,
                 status:   String,
                 sortFunc: (Study, Study) => Boolean,
                 order:    SortOrder)
      : DomainValidation[Seq[Study]]

  def getStudyNames(filter: String, order: SortOrder): Seq[NameDto]

  def getStudy(id: String): DomainValidation[Study]

  def getCentresForStudy(studyId: String): Set[CentreLocation]

  def specimenGroupWithId(studyId: String, specimenGroupId: String)
      : DomainValidation[SpecimenGroup]

  def specimenGroupsForStudy(studyId: String): DomainValidation[Set[SpecimenGroup]]

  def specimenGroupsInUse(studyId: String): DomainValidation[Set[SpecimenGroupId]]

  def collectionEventTypeWithId(studyId: String, collectionEventTypeId: String)
      : DomainValidation[CollectionEventType]

  def collectionEventTypeInUse(collectionEventTypeId: String)
      : DomainValidation[Boolean]

  def collectionEventTypesForStudy(studyId: String): DomainValidation[Set[CollectionEventType]]

  def processingTypeWithId(Id: String, processingTypeId: String)
      : DomainValidation[ProcessingType]

  def processingTypesForStudy(studyId: String): DomainValidation[Set[ProcessingType]]

  def specimenLinkTypeWithId(processingTypeId: String, specimenLinkTypeId: String)
      : DomainValidation[SpecimenLinkType]

  def specimenLinkTypesForProcessingType(processingTypeId: String)
      : DomainValidation[Set[SpecimenLinkType]]

  def getProcessingDto(studyId: String): DomainValidation[ProcessingDto]

  def processCommand(cmd: StudyCommand): Future[DomainValidation[Study]]

  def processCollectionEventTypeCommand(cmd: CollectionEventTypeCommand)
      : Future[DomainValidation[CollectionEventType]]

  def processRemoveCollectionEventTypeCommand(cmd: RemoveCollectionEventTypeCmd)
      : Future[DomainValidation[Boolean]]

  def processProcessingTypeCommand(cmd: StudyCommand)
      : Future[DomainValidation[ProcessingType]]

  def processRemoveProcessingTypeCommand(cmd: StudyCommand)
      : Future[DomainValidation[Boolean]]
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
  import org.biobank.CommonValidations._

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  def getStudyCount(): Int = {
    studyRepository.getValues.size
  }

  def getCountsByStatus(): StudyCountsByStatus = {
    // FIXME should be replaced by DTO query to the database
    val studies = studyRepository.getValues
    StudyCountsByStatus(
      total         = studies.size,
      disabledCount = studies.collect { case s: DisabledStudy => s }.size,
      enabledCount  = studies.collect { case s: EnabledStudy => s }.size,
      retiredCount  = studies.collect { case s: RetiredStudy => s }.size
    )
  }

  def getStudies(filter:   String,
                 status:   String,
                 sortFunc: (Study, Study) => Boolean,
                 order:    SortOrder)
      : DomainValidation[Seq[Study]] = {
    val allStudies = studyRepository.getValues

    val studiesFilteredByName = if (!filter.isEmpty) {
      val filterLowerCase = filter.toLowerCase
      allStudies.filter { study => study.name.toLowerCase.contains(filterLowerCase) }
    } else {
      allStudies
    }

    val studiesFilteredByStatus = status match {
      case "all" =>
        studiesFilteredByName.successNel[String]
      case "DisabledStudy" =>
        studiesFilteredByName.collect { case s: DisabledStudy => s }.successNel[String]
      case "EnabledStudy" =>
        studiesFilteredByName.collect { case s: EnabledStudy => s }.successNel[String]
      case "RetiredStudy" =>
        studiesFilteredByName.collect { case s: RetiredStudy => s }.successNel[String]
      case _ => InvalidStatus(status).failureNel[Seq[Study]]
    }

    studiesFilteredByStatus.map { studies =>
      val result = studies.toSeq.sortWith(sortFunc)

      if (order == AscendingOrder) {
        result
      } else {
        result.reverse
      }
    }
  }

  def getStudyNames(filter: String, order: SortOrder): Seq[NameDto] = {
    val studies = studyRepository.getValues

    val filteredStudies = if (filter.isEmpty) {
      studies
    } else {
      studies.filter { s => s.name.contains(filter) }
    }

    val orderedStudies = filteredStudies.toSeq
    val result = orderedStudies.map { s =>
      NameDto(s.id.id, s.name, s.getClass.getSimpleName)
    } sortWith(NameDto.compareByName)

    if (order == AscendingOrder) {
      result
    } else {
      result.reverse
    }
  }

  def getStudy(id: String) : DomainValidation[Study] = {
    studyRepository.getByKey(StudyId(id))
  }

  def getCentresForStudy(studyId: String): Set[CentreLocation] = {
    centreRepository.withStudy(StudyId(studyId)).flatMap { centre =>
      centre.locations.map { location =>
        CentreLocation(centre.id.id, location.uniqueId, centre.name, location.name)
      }
    }
  }

  def specimenGroupWithId(studyId: String, specimenGroupId: String)
      : DomainValidation[SpecimenGroup] = {
    validStudyId(studyId) { study =>
      specimenGroupRepository.withId(study.id, SpecimenGroupId(specimenGroupId))
    }
  }

  def specimenGroupsForStudy(studyId: String) : DomainValidation[Set[SpecimenGroup]] = {
    validStudyId(studyId) { study =>
      specimenGroupRepository.allForStudy(study.id).successNel
    }
  }

  def specimenGroupsInUse(studyId: String): DomainValidation[Set[SpecimenGroupId]] = {
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

  def collectionEventTypeWithId(studyId: String, collectionEventTypeId: String)
      : DomainValidation[CollectionEventType] = {
    validStudyId(studyId) { study =>
      collectionEventTypeRepository.withId(study.id, CollectionEventTypeId(collectionEventTypeId))
    }
  }

  def collectionEventTypeInUse(collectionEventTypeId: String): DomainValidation[Boolean] = {
    val id = CollectionEventTypeId(collectionEventTypeId)
    collectionEventTypeRepository.getByKey(id).map { ceventType =>
      collectionEventRepository.collectionEventTypeInUse(id)
    }
  }

  def collectionEventTypesForStudy(studyId: String)
      : DomainValidation[Set[CollectionEventType]] = {
    validStudyId(studyId) { study =>
      collectionEventTypeRepository.allForStudy(study.id).successNel[String]
    }
  }

  def processingTypeWithId(studyId: String, processingTypeId: String)
      : DomainValidation[ProcessingType] = {
    validStudyId(studyId) { study =>
      processingTypeRepository.withId(study.id, ProcessingTypeId(processingTypeId))
    }
  }

  def processingTypesForStudy(studyId: String)
      : DomainValidation[Set[ProcessingType]] = {
    validStudyId(studyId) { study =>
      processingTypeRepository.allForStudy(study.id).successNel[String]
    }
  }

  def specimenLinkTypeWithId(processingTypeId: String, specimenLinkTypeId: String)
      : DomainValidation[SpecimenLinkType] = {
    validProcessingTypeId(processingTypeId) { processingType =>
      specimenLinkTypeRepository.withId(processingType.id, SpecimenLinkTypeId(specimenLinkTypeId))
    }
  }

  def specimenLinkTypesForProcessingType(processingTypeId: String)
      : DomainValidation[Set[SpecimenLinkType]] = {
    validProcessingTypeId(processingTypeId) { processingType =>
      specimenLinkTypeRepository.allForProcessingType(processingType.id).successNel[String]
    }
  }

  def getProcessingDto(studyId: String): DomainValidation[ProcessingDto] = {
    "deprectated: annot type refactor".failureNel[ProcessingDto]
  }

  private def validStudyId[T](studyId: String)(fn: Study => DomainValidation[T])
      : DomainValidation[T] = {
    for {
      study <- studyRepository.getByKey(StudyId(studyId))
      result <- fn(study)
    } yield result
  }

  private def validProcessingTypeId[T](processingTypeId: String)(fn: ProcessingType => DomainValidation[T])
      : DomainValidation[T] = {
    for {
      pt <- processingTypeRepository.getByKey(ProcessingTypeId(processingTypeId))
      study <- studyRepository.getByKey(pt.studyId)
      result <- fn(pt)
    } yield result
  }

  def processCommand(cmd: StudyCommand): Future[DomainValidation[Study]] =
    ask(studyProcessor, cmd).mapTo[DomainValidation[StudyEvent]].map { validation =>
      for {
        event <- validation
        study <- studyRepository.getByKey(StudyId(event.id))
      } yield study
    }

  def processCollectionEventTypeCommand(cmd: CollectionEventTypeCommand)
      : Future[DomainValidation[CollectionEventType]] = {
    cmd match {
      case c: RemoveCollectionEventTypeCmd =>
        Future.successful(DomainError(s"invalid service call: $cmd").failureNel[CollectionEventType])
      case _ => {
        studyRepository.getDisabled(StudyId(cmd.studyId)).fold(
          err => Future.successful(err.failure[CollectionEventType]),
          study => {
            ask(ceventTypeProcessor, cmd).mapTo[DomainValidation[CollectionEventTypeEvent]]
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
      : Future[DomainValidation[Boolean]] = {
    studyRepository.getDisabled(StudyId(cmd.studyId)).fold(
      err => Future.successful(err.failure[Boolean]),
      study => {
        ask(ceventTypeProcessor, cmd).mapTo[DomainValidation[CollectionEventTypeEvent]]
          .map { validation => validation.map(_ => true) }
      }
    )
  }

  def processProcessingTypeCommand(cmd: StudyCommand)
      : Future[DomainValidation[ProcessingType]] = {
    cmd match {
      case c: RemoveProcessingTypeCmd =>
        Future.successful(DomainError(s"invalid service call: $cmd").failureNel[ProcessingType])
      case _ =>
        ask(studyProcessor, cmd).mapTo[DomainValidation[ProcessingTypeEvent]].map { validation =>
          for {
            event  <- validation
            result <- processingTypeRepository.getByKey(ProcessingTypeId(event.id))
          } yield result
        }
    }
  }

  def processRemoveProcessingTypeCommand(cmd: StudyCommand)
      : Future[DomainValidation[Boolean]] =
    ask(studyProcessor, cmd).mapTo[DomainValidation[ProcessingTypeEvent]]
      .map { validation => validation.map(_ => true) }

}
