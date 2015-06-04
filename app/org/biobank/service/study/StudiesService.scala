 package org.biobank.service.study

import org.biobank.dto._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.dto.{ CollectionDto, ProcessingDto }
import org.biobank.domain.{
  AnnotationTypeId,
  DomainValidation,
  DomainError
}
import org.biobank.domain.user.UserId
import org.biobank.domain.study._

import akka.util.Timeout
import akka.actor._
import akka.pattern.ask
import scala.concurrent._
import scala.concurrent.duration._
import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import javax.inject._
import com.google.inject.ImplementedBy

import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[StudiesServiceImpl])
trait StudiesService {

  def getAll: Seq[StudyNameDto]

  def getCountsByStatus(): StudyCountsByStatus

  def getStudies[T <: Study]
    (filter: String, status: String, sortFunc: (Study, Study) => Boolean, order: SortOrder)
      : DomainValidation[Seq[Study]]

  def getStudyNames(filter: String, order: SortOrder)
      : DomainValidation[Seq[StudyNameDto]]

  def getStudy(id: String): DomainValidation[Study]

  def specimenGroupWithId(studyId: String, specimenGroupId: String)
      : DomainValidation[SpecimenGroup]

  def specimenGroupsForStudy(studyId: String): DomainValidation[Set[SpecimenGroup]]

  def specimenGroupsInUse(studyId: String): DomainValidation[Set[SpecimenGroupId]]

  def collectionEventAnnotationTypeWithId
    (studyId: String, annotationTypeId: String)
      : DomainValidation[CollectionEventAnnotationType]

  def collectionEventAnnotationTypesForStudy(studyId: String)
      : DomainValidation[Set[CollectionEventAnnotationType]]

  def collectionEventTypeWithId
    (studyId: String, collectionEventTypeId: String)
      : DomainValidation[CollectionEventType]

  def collectionEventTypesForStudy(studyId: String): DomainValidation[Set[CollectionEventType]]

  def processingTypeWithId
    (studyId: String, processingTypeId: String)
      : DomainValidation[ProcessingType]

  def processingTypesForStudy(studyId: String): DomainValidation[Set[ProcessingType]]

  def specimenLinkTypeWithId
    (studyId: String, specimenLinkTypeId: String)
      : DomainValidation[SpecimenLinkType]

  def specimenLinkTypesForProcessingType(processingTypeId: String)
      : DomainValidation[Set[SpecimenLinkType]]

  def getCollectionDto(studyId: String): DomainValidation[CollectionDto]

  def getProcessingDto(studyId: String): DomainValidation[ProcessingDto]

  def addStudy(cmd: AddStudyCmd): Future[DomainValidation[Study]]

  def updateStudy(cmd: UpdateStudyCmd): Future[DomainValidation[Study]]

  def enableStudy(cmd: EnableStudyCmd): Future[DomainValidation[Study]]

  def disableStudy(cmd: DisableStudyCmd): Future[DomainValidation[Study]]

  def retireStudy(cmd: RetireStudyCmd): Future[DomainValidation[Study]]

  def unretireStudy(cmd: UnretireStudyCmd): Future[DomainValidation[Study]]

  // specimen groups
  def addSpecimenGroup(cmd: AddSpecimenGroupCmd): Future[DomainValidation[SpecimenGroup]]

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd): Future[DomainValidation[SpecimenGroup]]

  def removeSpecimenGroup(cmd: RemoveSpecimenGroupCmd): Future[DomainValidation[Boolean]]

  // collection event types
  def addCollectionEventType(cmd: AddCollectionEventTypeCmd): Future[DomainValidation[CollectionEventType]]

  def updateCollectionEventType(cmd: UpdateCollectionEventTypeCmd)
    : Future[DomainValidation[CollectionEventType]]

  def removeCollectionEventType(cmd: RemoveCollectionEventTypeCmd): Future[DomainValidation[Boolean]]

  // collection event annotation types
  def addCollectionEventAnnotationType(cmd: AddCollectionEventAnnotationTypeCmd)
    : Future[DomainValidation[CollectionEventAnnotationType]]

  def updateCollectionEventAnnotationType(cmd: UpdateCollectionEventAnnotationTypeCmd)
    : Future[DomainValidation[CollectionEventAnnotationType]]

  def removeCollectionEventAnnotationType(cmd: RemoveCollectionEventAnnotationTypeCmd)
    : Future[DomainValidation[Boolean]]

  // participant annotation types
  def participantAnnotationTypesForStudy(studyId: String)
      : DomainValidation[Set[ParticipantAnnotationType]]

  def participantAnnotationTypeWithId
    (studyId: String, annotationTypeId: String)
      : DomainValidation[ParticipantAnnotationType]

  def addParticipantAnnotationType(cmd: AddParticipantAnnotationTypeCmd)
      : Future[DomainValidation[ParticipantAnnotationType]]

  def updateParticipantAnnotationType(cmd: UpdateParticipantAnnotationTypeCmd)
    : Future[DomainValidation[ParticipantAnnotationType]]

  def removeParticipantAnnotationType(cmd: RemoveParticipantAnnotationTypeCmd)
    : Future[DomainValidation[Boolean]]

  // specimen link annotation types
  def specimenLinkAnnotationTypeWithId(studyId: String, annotationTypeId: String)
      : DomainValidation[SpecimenLinkAnnotationType]

  def specimenLinkAnnotationTypesForStudy(id: String)
      : DomainValidation[Set[SpecimenLinkAnnotationType]]

  def addSpecimenLinkAnnotationType(cmd: AddSpecimenLinkAnnotationTypeCmd)
    : Future[DomainValidation[SpecimenLinkAnnotationType]]

  def updateSpecimenLinkAnnotationType(cmd: UpdateSpecimenLinkAnnotationTypeCmd)
    : Future[DomainValidation[SpecimenLinkAnnotationType]]

  def removeSpecimenLinkAnnotationType(cmd: RemoveSpecimenLinkAnnotationTypeCmd)
    : Future[DomainValidation[Boolean]]

  // processing types
  def addProcessingType(cmd: AddProcessingTypeCmd): Future[DomainValidation[ProcessingType]]

  def updateProcessingType(cmd: UpdateProcessingTypeCmd): Future[DomainValidation[ProcessingType]]

  def removeProcessingType(cmd: RemoveProcessingTypeCmd): Future[DomainValidation[Boolean]]

  // specimen link types
  def addSpecimenLinkType(cmd: AddSpecimenLinkTypeCmd)
    : Future[DomainValidation[SpecimenLinkType]]

  def updateSpecimenLinkType(cmd: UpdateSpecimenLinkTypeCmd)
    : Future[DomainValidation[SpecimenLinkType]]

  def removeSpecimenLinkType(cmd: RemoveSpecimenLinkTypeCmd): Future[DomainValidation[Boolean]]

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
  @Named("studiesProcessor") val processor:    ActorRef,
  val studyRepository:                         StudyRepository,
  val processingTypeRepository:                ProcessingTypeRepository,
  val specimenGroupRepository:                 SpecimenGroupRepository,
  val collectionEventTypeRepository:           CollectionEventTypeRepository,
  val specimenLinkTypeRepository:              SpecimenLinkTypeRepository,
  val collectionEventAnnotationTypeRepository: CollectionEventAnnotationTypeRepository,
  val participantAnnotationTypeRepository:     ParticipantAnnotationTypeRepository,
  val specimenLinkAnnotationTypeRepository:    SpecimenLinkAnnotationTypeRepository,
  val participantRepository:                   ParticipantRepository)
    extends StudiesService {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  /**
    * FIXME: use paging and sorting
    */
  def getAll: Seq[StudyNameDto] = {
    val result = studyRepository.getValues.map { s => StudyNameDto(s.id.id, s.name, s.status) }
    result.toSeq.sortWith(StudyNameDto.compareByName)
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

  private def getStatus(status: String): DomainValidation[String] = {
    status match {
      case "all"      => Study.status.successNel
      case "disabled" => DisabledStudy.status.successNel
      case "enabled"  => EnabledStudy.status.successNel
      case "retired"  => RetiredStudy.status.successNel
      case _          => DomainError(s"invalid study status: $status").failureNel
    }
  }

  def getStudies[T <: Study]
    (filter: String, status: String, sortFunc: (Study, Study) => Boolean, order: SortOrder)
      : DomainValidation[Seq[Study]] = {
    val allStudies = studyRepository.getValues

    val studiesFilteredByName = if (!filter.isEmpty) {
      val filterLowerCase = filter.toLowerCase
      allStudies.filter { study => study.name.toLowerCase.contains(filterLowerCase) }
    } else {
      allStudies
    }

    val studiesFilteredByStatus = getStatus(status).map { status =>
      if (status == Study.status) {
        studiesFilteredByName
      } else {
        studiesFilteredByName.filter { study => study.status == status }
      }
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

  def getStudyNames(filter: String, order: SortOrder)
      : DomainValidation[Seq[StudyNameDto]] = {
    val studies = studyRepository.getValues

    val filteredStudies = if (filter.isEmpty) {
      studies
    } else {
      studies.filter { s => s.name.contains(filter) }
    }

    val orderedStudies = filteredStudies.toSeq
    val result = orderedStudies.map { s =>
      StudyNameDto(s.id.id, s.name, s.status)
    } sortWith(StudyNameDto.compareByName)

    if (order == AscendingOrder) {
      result.success
    } else {
      result.reverse.success
    }
  }

  def getStudy(id: String) : DomainValidation[Study] = {
    studyRepository.getByKey(StudyId(id))
  }

  def specimenGroupWithId(studyId: String, specimenGroupId: String)
      : DomainValidation[SpecimenGroup] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => specimenGroupRepository.withId(study.id, SpecimenGroupId(specimenGroupId))
    )
  }

  def specimenGroupsForStudy(studyId: String) : DomainValidation[Set[SpecimenGroup]] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => specimenGroupRepository.allForStudy(study.id).successNel
    )
  }

  def specimenGroupsInUse(studyId: String)
      : DomainValidation[Set[SpecimenGroupId]] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => {
        val cetSpecimenGroupIds = for {
          ceventType <- collectionEventTypeRepository.allForStudy(study.id)
          sgItem     <- ceventType.specimenGroupData
        } yield SpecimenGroupId(sgItem.specimenGroupId)

        val sltSpecimenGroupIds = for {
          processingType   <- processingTypeRepository.allForStudy(study.id)
          specimenLinkType <- specimenLinkTypeRepository.allForProcessingType(processingType.id)
          sgId             <- Set(specimenLinkType.inputGroupId, specimenLinkType.outputGroupId)
        } yield sgId

        (cetSpecimenGroupIds ++ sltSpecimenGroupIds).success
      }
    )
  }

  def collectionEventAnnotationTypeWithId(studyId: String, annotationTypeId: String)
      : DomainValidation[CollectionEventAnnotationType] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => collectionEventAnnotationTypeRepository.withId(
        study.id, AnnotationTypeId(annotationTypeId))
    )
  }

  def collectionEventAnnotationTypesForStudy(studyId: String)
      : DomainValidation[Set[CollectionEventAnnotationType]] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => collectionEventAnnotationTypeRepository.allForStudy(study.id).successNel
    )
  }

  def collectionEventTypeWithId(studyId: String, collectionEventTypeId: String)
      : DomainValidation[CollectionEventType] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => collectionEventTypeRepository.withId(study.id, CollectionEventTypeId(collectionEventTypeId))
    )
  }

  def collectionEventTypesForStudy(studyId: String)
      : DomainValidation[Set[CollectionEventType]] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => collectionEventTypeRepository.allForStudy(study.id).success
    )
  }

  def participantAnnotationTypesForStudy(studyId: String)
      : DomainValidation[Set[ParticipantAnnotationType]] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => participantAnnotationTypeRepository.allForStudy(study.id).success
    )
  }

  def participantAnnotationTypeWithId(studyId: String, annotationTypeId: String)
      : DomainValidation[ParticipantAnnotationType] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => participantAnnotationTypeRepository.withId(study.id, AnnotationTypeId(annotationTypeId))
    )
  }

  def specimenLinkAnnotationTypeWithId(studyId: String, annotationTypeId: String)
      : DomainValidation[SpecimenLinkAnnotationType] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => specimenLinkAnnotationTypeRepository.withId(study.id, AnnotationTypeId(annotationTypeId))
    )
  }

  def processingTypeWithId(studyId: String, processingTypeId: String)
      : DomainValidation[ProcessingType] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => processingTypeRepository.withId(study.id, ProcessingTypeId(processingTypeId))
    )
  }

  def processingTypesForStudy(studyId: String)
      : DomainValidation[Set[ProcessingType]] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => processingTypeRepository.allForStudy(study.id).success
    )
  }

  def specimenLinkTypeWithId(processingTypeId: String, specimenLinkTypeId: String)
      : DomainValidation[SpecimenLinkType] = {
    processingTypeRepository.getByKey(ProcessingTypeId(processingTypeId)).fold(
      err => DomainError(s"invalid processing type id: $processingTypeId").failureNel,
      pt => specimenLinkTypeRepository.withId(pt.id, SpecimenLinkTypeId(specimenLinkTypeId))
    )
  }

  def specimenLinkTypesForProcessingType(processingTypeId: String)
      : DomainValidation[Set[SpecimenLinkType]] = {
    processingTypeRepository.getByKey(ProcessingTypeId(processingTypeId)).fold(
      err => DomainError(s"invalid processing type id: $processingTypeId").failureNel,
      pt => specimenLinkTypeRepository.allForProcessingType(pt.id).success
    )
  }

  def getCollectionDto(studyId: String): DomainValidation[CollectionDto] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => {
        val collectionEventTypes = collectionEventTypeRepository.allForStudy(study.id)
        val annotationTypes = collectionEventAnnotationTypeRepository.allForStudy(study.id)
        val annotationTypesInUse = collectionEventTypes.flatMap { cet =>
          cet.annotationTypeData.map(atd => atd.annotationTypeId)
        }
        val specimenGroups  = specimenGroupRepository.allForStudy(study.id)

        CollectionDto(
          collectionEventTypes.toList,
          annotationTypes.toList,
          annotationTypesInUse.toList,
          specimenGroups.toList).success
      }
    )
  }

  def getProcessingDto(studyId: String): DomainValidation[ProcessingDto] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => {
        val specimenGroups  = specimenGroupRepository.allForStudy(study.id)
        val processingTypes = processingTypeRepository.allForStudy(study.id)
        val annotationTypes = specimenLinkAnnotationTypeRepository.allForStudy(study.id)
        val specimenLinkTypes = processingTypes.flatMap { pt =>
          specimenLinkTypeRepository.allForProcessingType(pt.id)
        }
        val annotationTypesInUse = specimenLinkTypes.flatMap { slt =>
          slt.annotationTypeData.map(atd => atd.annotationTypeId)
        }

        ProcessingDto(
          processingTypes.toList,
          specimenLinkTypes.toList,
          annotationTypes.toList,
          annotationTypesInUse.toList,
          specimenGroups.toList).success
      }
    )
  }

  def addStudy(cmd: AddStudyCmd): Future[DomainValidation[Study]] = {
    replyWithStudy(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])
  }

  def updateStudy(cmd: UpdateStudyCmd): Future[DomainValidation[Study]] =
    replyWithStudy(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  def enableStudy(cmd: EnableStudyCmd): Future[DomainValidation[Study]] =
    replyWithStudy(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  def disableStudy(cmd: DisableStudyCmd): Future[DomainValidation[Study]] =
    replyWithStudy(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  def retireStudy(cmd: RetireStudyCmd): Future[DomainValidation[Study]] =
    replyWithStudy(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  def unretireStudy(cmd: UnretireStudyCmd): Future[DomainValidation[Study]] =
    replyWithStudy(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  // specimen groups
  def addSpecimenGroup(cmd: AddSpecimenGroupCmd): Future[DomainValidation[SpecimenGroup]] = {
    replyWithSpecimenGroup(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])
  }

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd): Future[DomainValidation[SpecimenGroup]] =
    replyWithSpecimenGroup(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  def removeSpecimenGroup(cmd: RemoveSpecimenGroupCmd): Future[DomainValidation[Boolean]] =
    replyWithBoolean(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  // collection event types
  def addCollectionEventType(cmd: AddCollectionEventTypeCmd)
      : Future[DomainValidation[CollectionEventType]] = {
    replyWithCollectionEventType(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])
  }

  def updateCollectionEventType(cmd: UpdateCollectionEventTypeCmd)
      : Future[DomainValidation[CollectionEventType]] =
    replyWithCollectionEventType(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  def removeCollectionEventType(cmd: RemoveCollectionEventTypeCmd)
      : Future[DomainValidation[Boolean]] =
    replyWithBoolean(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  // collection event annotation types
  def addCollectionEventAnnotationType(cmd: AddCollectionEventAnnotationTypeCmd)
    : Future[DomainValidation[CollectionEventAnnotationType]] =
    replyWithCollectionEventAnnotationType(
      ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  def updateCollectionEventAnnotationType(cmd: UpdateCollectionEventAnnotationTypeCmd)
    : Future[DomainValidation[CollectionEventAnnotationType]] =
    replyWithCollectionEventAnnotationType(
      ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  def removeCollectionEventAnnotationType(cmd: RemoveCollectionEventAnnotationTypeCmd)
    : Future[DomainValidation[Boolean]] =
    replyWithBoolean(
      ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  // participant annotation types
  def addParticipantAnnotationType(cmd: AddParticipantAnnotationTypeCmd)
    : Future[DomainValidation[ParticipantAnnotationType]] =
    replyWithParticipantAnnotationType(
      ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  def updateParticipantAnnotationType(cmd: UpdateParticipantAnnotationTypeCmd)
    : Future[DomainValidation[ParticipantAnnotationType]] =
    replyWithParticipantAnnotationType(
      ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  def removeParticipantAnnotationType(cmd: RemoveParticipantAnnotationTypeCmd)
    : Future[DomainValidation[Boolean]] =
    replyWithBoolean(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  // specimen link annotation types
  def specimenLinkAnnotationTypesForStudy(studyId: String)
      : DomainValidation[Set[SpecimenLinkAnnotationType]] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => specimenLinkAnnotationTypeRepository.allForStudy(StudyId(studyId)).success
    )
  }

  def addSpecimenLinkAnnotationType(cmd: AddSpecimenLinkAnnotationTypeCmd)
    : Future[DomainValidation[SpecimenLinkAnnotationType]] =
    replyWithSpecimenLinkAnnotationType(
      ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  def updateSpecimenLinkAnnotationType(cmd: UpdateSpecimenLinkAnnotationTypeCmd)
    : Future[DomainValidation[SpecimenLinkAnnotationType]] =
    replyWithSpecimenLinkAnnotationType(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  def removeSpecimenLinkAnnotationType(cmd: RemoveSpecimenLinkAnnotationTypeCmd)
    : Future[DomainValidation[Boolean]] =
    replyWithBoolean(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  // processing types
  def addProcessingType(cmd: AddProcessingTypeCmd)
      : Future[DomainValidation[ProcessingType]] = {
    replyWithProcessingType(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])
  }

  def updateProcessingType(cmd: UpdateProcessingTypeCmd)
      : Future[DomainValidation[ProcessingType]] =
    replyWithProcessingType(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  def removeProcessingType(cmd: RemoveProcessingTypeCmd)
    : Future[DomainValidation[Boolean]] =
    replyWithBoolean(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  // specimen link types
  def addSpecimenLinkType(cmd: AddSpecimenLinkTypeCmd)
      : Future[DomainValidation[SpecimenLinkType]] = {
    replyWithSpecimenLinkType(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])
  }

  def updateSpecimenLinkType(cmd: UpdateSpecimenLinkTypeCmd)
      : Future[DomainValidation[SpecimenLinkType]] =
    replyWithSpecimenLinkType(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  def removeSpecimenLinkType(cmd: RemoveSpecimenLinkTypeCmd)
      : Future[DomainValidation[Boolean]] =
    replyWithBoolean(ask(processor, cmd).mapTo[DomainValidation[StudyEvent]])

  private def replyWithStudy(future: Future[DomainValidation[StudyEvent]])
      : Future[DomainValidation[Study]] = {
    future map { validation =>
      for {
        event <- validation
        study <- studyRepository.getByKey(StudyId(event.id))
      } yield study
    }
  }

  private def replyWithSpecimenGroup(future: Future[DomainValidation[StudyEvent]])
      : Future[DomainValidation[SpecimenGroup]] = {
    future map { validation =>
      for {
        event <- validation
        sg <- {
          val specimenGroupId = if (event.eventType.isSpecimenGroupAdded) {
            event.getSpecimenGroupAdded.getSpecimenGroupId
          } else {
            event.getSpecimenGroupUpdated.getSpecimenGroupId
          }
          specimenGroupRepository.getByKey(SpecimenGroupId(specimenGroupId))
        }
      } yield sg
    }
  }

  private def replyWithCollectionEventType(future: Future[DomainValidation[StudyEvent]])
      : Future[DomainValidation[CollectionEventType]] = {
    future map { validation =>
      for {
        event <- validation
        cet <- {
          val cetId = if (event.eventType.isCollectionEventTypeAdded) {
            event.getCollectionEventTypeAdded.getCollectionEventTypeId
          } else {
            event.getCollectionEventTypeUpdated.getCollectionEventTypeId
          }
          collectionEventTypeRepository.getByKey(CollectionEventTypeId(cetId))
        }
      } yield cet
    }
  }

  private def replyWithCollectionEventAnnotationType(future: Future[DomainValidation[StudyEvent]])
      : Future[DomainValidation[CollectionEventAnnotationType]] = {
    future map { validation =>
      for {
        event <- validation
        ceat <- {
          val atId = if (event.eventType.isCollectionEventAnnotationTypeAdded) {
            event.getCollectionEventAnnotationTypeAdded.getAnnotationTypeId
          } else {
            event.getCollectionEventAnnotationTypeUpdated.getAnnotationTypeId
          }
          collectionEventAnnotationTypeRepository.getByKey(AnnotationTypeId(atId))
        }
      } yield ceat
    }
  }

  private def replyWithParticipantAnnotationType(future: Future[DomainValidation[StudyEvent]])
      : Future[DomainValidation[ParticipantAnnotationType]] = {
    future map { validation =>
      for {
        event <- validation
        pat <- {
          val atId = if (event.eventType.isParticipantAnnotationTypeAdded) {
            event.getParticipantAnnotationTypeAdded.getAnnotationTypeId
          } else {
            event.getParticipantAnnotationTypeUpdated.getAnnotationTypeId
          }
          participantAnnotationTypeRepository.getByKey(AnnotationTypeId(atId))
        }
      } yield pat
    }
  }

  private def replyWithSpecimenLinkAnnotationType(future: Future[DomainValidation[StudyEvent]])
      : Future[DomainValidation[SpecimenLinkAnnotationType]] = {
    future map { validation =>
      for {
        event <- validation
        slat <- {
          val atId = if (event.eventType.isSpecimenLinkAnnotationTypeAdded) {
            event.getSpecimenLinkAnnotationTypeAdded.getAnnotationTypeId
          } else {
            event.getSpecimenLinkAnnotationTypeUpdated.getAnnotationTypeId
          }
          specimenLinkAnnotationTypeRepository.getByKey(AnnotationTypeId(atId))
        }
      } yield slat
    }
  }

  private def replyWithProcessingType(future: Future[DomainValidation[StudyEvent]])
      : Future[DomainValidation[ProcessingType]] = {
    future map { validation =>
      for {
        event <- validation
        pt <- {
          val ptId = if (event.eventType.isProcessingTypeAdded) {
            event.getProcessingTypeAdded.getProcessingTypeId
          } else {
            event.getProcessingTypeUpdated.getProcessingTypeId
          }
          processingTypeRepository.getByKey(ProcessingTypeId(ptId))
        }
      } yield pt
    }
  }

  private def replyWithSpecimenLinkType(future: Future[DomainValidation[StudyEvent]])
      : Future[DomainValidation[SpecimenLinkType]] = {
    future map { validation =>
      for {
        event <- validation
        slt <- {
          val sltId = if (event.eventType.isSpecimenLinkTypeAdded) {
            event.getSpecimenLinkTypeAdded.getSpecimenLinkTypeId
          } else {
            event.getSpecimenLinkTypeUpdated.getSpecimenLinkTypeId
          }
          specimenLinkTypeRepository.getByKey(SpecimenLinkTypeId(sltId))
        }
      } yield slt
    }
  }

  /**
   * Returns 'ture' wrapped in a validation if the event does not fail validation.
   */
  private def replyWithBoolean(future: Future[DomainValidation[StudyEvent]])
      : Future[DomainValidation[Boolean]] =
    future map { validation => validation map { event => true } }

}
