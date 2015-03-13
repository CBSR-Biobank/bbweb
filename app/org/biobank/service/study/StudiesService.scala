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
import ExecutionContext.Implicits.global
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}

import scalaz._
import scalaz.Scalaz._

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

  def addStudy(cmd: AddStudyCmd)
      : Future[DomainValidation[StudyAddedEvent]]

  def updateStudy(cmd: UpdateStudyCmd)
      : Future[DomainValidation[StudyUpdatedEvent]]

  def enableStudy(cmd: EnableStudyCmd)
      : Future[DomainValidation[StudyEnabledEvent]]

  def disableStudy(cmd: DisableStudyCmd)
      : Future[DomainValidation[StudyDisabledEvent]]

  def retireStudy(cmd: RetireStudyCmd)
      : Future[DomainValidation[StudyRetiredEvent]]

  def unretireStudy(cmd: UnretireStudyCmd)
      : Future[DomainValidation[StudyUnretiredEvent]]

  // specimen groups
  def addSpecimenGroup(cmd: AddSpecimenGroupCmd)
      : Future[DomainValidation[SpecimenGroupAddedEvent]]

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd)
      : Future[DomainValidation[SpecimenGroupUpdatedEvent]]

  def removeSpecimenGroup(cmd: RemoveSpecimenGroupCmd)
      : Future[DomainValidation[SpecimenGroupRemovedEvent]]

  // collection event types
  def addCollectionEventType(cmd: AddCollectionEventTypeCmd)
    : Future[DomainValidation[CollectionEventTypeAddedEvent]]

  def updateCollectionEventType(cmd: UpdateCollectionEventTypeCmd)
    : Future[DomainValidation[CollectionEventTypeUpdatedEvent]]

  def removeCollectionEventType(cmd: RemoveCollectionEventTypeCmd)
    : Future[DomainValidation[CollectionEventTypeRemovedEvent]]

  // collection event annotation types
  def addCollectionEventAnnotationType(cmd: AddCollectionEventAnnotationTypeCmd)
    : Future[DomainValidation[CollectionEventAnnotationTypeAddedEvent]]

  def updateCollectionEventAnnotationType(cmd: UpdateCollectionEventAnnotationTypeCmd)
    : Future[DomainValidation[CollectionEventAnnotationTypeUpdatedEvent]]

  def removeCollectionEventAnnotationType(cmd: RemoveCollectionEventAnnotationTypeCmd)
    : Future[DomainValidation[CollectionEventAnnotationTypeRemovedEvent]]

  // participant annotation types
  def participantAnnotationTypesForStudy
    (studyId: String)
      : DomainValidation[Set[ParticipantAnnotationType]]

  def participantAnnotationTypeWithId
    (studyId: String, annotationTypeId: String)
      : DomainValidation[ParticipantAnnotationType]

  def addParticipantAnnotationType(cmd: AddParticipantAnnotationTypeCmd)
      : Future[DomainValidation[ParticipantAnnotationTypeAddedEvent]]

  def updateParticipantAnnotationType(cmd: UpdateParticipantAnnotationTypeCmd)
    : Future[DomainValidation[ParticipantAnnotationTypeUpdatedEvent]]

  def removeParticipantAnnotationType(cmd: RemoveParticipantAnnotationTypeCmd)
    : Future[DomainValidation[ParticipantAnnotationTypeRemovedEvent]]

  // specimen link annotation types
  def specimenLinkAnnotationTypeWithId
    (studyId: String, annotationTypeId: String)
      : DomainValidation[SpecimenLinkAnnotationType]

  def specimenLinkAnnotationTypesForStudy(id: String)
      : DomainValidation[Set[SpecimenLinkAnnotationType]]

  def addSpecimenLinkAnnotationType(cmd: AddSpecimenLinkAnnotationTypeCmd)
    : Future[DomainValidation[SpecimenLinkAnnotationTypeAddedEvent]]

  def updateSpecimenLinkAnnotationType(cmd: UpdateSpecimenLinkAnnotationTypeCmd)
    : Future[DomainValidation[SpecimenLinkAnnotationTypeUpdatedEvent]]

  def removeSpecimenLinkAnnotationType(cmd: RemoveSpecimenLinkAnnotationTypeCmd)
    : Future[DomainValidation[SpecimenLinkAnnotationTypeRemovedEvent]]

  // processing types
  def addProcessingType(cmd: AddProcessingTypeCmd)
    : Future[DomainValidation[ProcessingTypeAddedEvent]]

  def updateProcessingType(cmd: UpdateProcessingTypeCmd)
    : Future[DomainValidation[ProcessingTypeUpdatedEvent]]

  def removeProcessingType(cmd: RemoveProcessingTypeCmd)
    : Future[DomainValidation[ProcessingTypeRemovedEvent]]

  // specimen link types
  def addSpecimenLinkType(cmd: AddSpecimenLinkTypeCmd)
    : Future[DomainValidation[SpecimenLinkTypeAddedEvent]]

  def updateSpecimenLinkType(cmd: UpdateSpecimenLinkTypeCmd)
    : Future[DomainValidation[SpecimenLinkTypeUpdatedEvent]]

  def removeSpecimenLinkType(cmd: RemoveSpecimenLinkTypeCmd)
    : Future[DomainValidation[SpecimenLinkTypeRemovedEvent]]

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
class StudiesServiceImpl(implicit inj: Injector)
    extends StudiesService
    with AkkaInjectable {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val system = inject [ActorSystem]

  implicit val timeout = inject [Timeout] ('akkaTimeout)

  val processor = injectActorRef [StudiesProcessor] ("study")

  val studyRepository                         = inject [StudyRepository]
  val processingTypeRepository                = inject [ProcessingTypeRepository]
  val specimenGroupRepository                 = inject [SpecimenGroupRepository]
  val collectionEventTypeRepository           = inject [CollectionEventTypeRepository]
  val specimenLinkTypeRepository              = inject [SpecimenLinkTypeRepository]
  val collectionEventAnnotationTypeRepository = inject [CollectionEventAnnotationTypeRepository]
  val participantAnnotationTypeRepository     = inject [ParticipantAnnotationTypeRepository]
  val specimenLinkAnnotationTypeRepository    = inject [SpecimenLinkAnnotationTypeRepository]
  val participantRepository                   = inject [ParticipantRepository]

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

        ProcessingDto(
          processingTypes.toList,
          specimenLinkTypes.toList,
          annotationTypes.toList,
          specimenGroups.toList).success
      }
    )
  }

  def addStudy(cmd: AddStudyCmd)
      : Future[DomainValidation[StudyAddedEvent]] = {
    ask(processor, cmd).mapTo[DomainValidation[StudyAddedEvent]]
  }

  def updateStudy(cmd: UpdateStudyCmd)
      : Future[DomainValidation[StudyUpdatedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[StudyUpdatedEvent]]

  def enableStudy(cmd: EnableStudyCmd)
      : Future[DomainValidation[StudyEnabledEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[StudyEnabledEvent]]

  def disableStudy(cmd: DisableStudyCmd)
      : Future[DomainValidation[StudyDisabledEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[StudyDisabledEvent]]

  def retireStudy(cmd: RetireStudyCmd)
      : Future[DomainValidation[StudyRetiredEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[StudyRetiredEvent]]

  def unretireStudy(cmd: UnretireStudyCmd)
      : Future[DomainValidation[StudyUnretiredEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[StudyUnretiredEvent]]

  // specimen groups
  def addSpecimenGroup(cmd: AddSpecimenGroupCmd)
      : Future[DomainValidation[SpecimenGroupAddedEvent]] = {
    ask(processor, cmd).mapTo[DomainValidation[SpecimenGroupAddedEvent]]
  }

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd)
      : Future[DomainValidation[SpecimenGroupUpdatedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[SpecimenGroupUpdatedEvent]]

  def removeSpecimenGroup(cmd: RemoveSpecimenGroupCmd)
      : Future[DomainValidation[SpecimenGroupRemovedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[SpecimenGroupRemovedEvent]]

  // collection event types
  def addCollectionEventType(cmd: AddCollectionEventTypeCmd)
      : Future[DomainValidation[CollectionEventTypeAddedEvent]] = {
    ask(processor, cmd).mapTo[DomainValidation[CollectionEventTypeAddedEvent]]
  }

  def updateCollectionEventType(cmd: UpdateCollectionEventTypeCmd)
      : Future[DomainValidation[CollectionEventTypeUpdatedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[CollectionEventTypeUpdatedEvent]]

  def removeCollectionEventType(cmd: RemoveCollectionEventTypeCmd)
      : Future[DomainValidation[CollectionEventTypeRemovedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[CollectionEventTypeRemovedEvent]]

  // collection event annotation types
  def addCollectionEventAnnotationType(cmd: AddCollectionEventAnnotationTypeCmd)
    : Future[DomainValidation[CollectionEventAnnotationTypeAddedEvent]] = {
    ask(processor, cmd).mapTo[DomainValidation[CollectionEventAnnotationTypeAddedEvent]]
  }

  def updateCollectionEventAnnotationType(cmd: UpdateCollectionEventAnnotationTypeCmd)
    : Future[DomainValidation[CollectionEventAnnotationTypeUpdatedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[CollectionEventAnnotationTypeUpdatedEvent]]

  def removeCollectionEventAnnotationType(cmd: RemoveCollectionEventAnnotationTypeCmd)
    : Future[DomainValidation[CollectionEventAnnotationTypeRemovedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[CollectionEventAnnotationTypeRemovedEvent]]

  // participant annotation types
  def addParticipantAnnotationType(cmd: AddParticipantAnnotationTypeCmd)
    : Future[DomainValidation[ParticipantAnnotationTypeAddedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[ParticipantAnnotationTypeAddedEvent]]

  def updateParticipantAnnotationType(cmd: UpdateParticipantAnnotationTypeCmd)
    : Future[DomainValidation[ParticipantAnnotationTypeUpdatedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[ParticipantAnnotationTypeUpdatedEvent]]

  def removeParticipantAnnotationType(cmd: RemoveParticipantAnnotationTypeCmd)
    : Future[DomainValidation[ParticipantAnnotationTypeRemovedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[ParticipantAnnotationTypeRemovedEvent]]

  // specimen link annotation types
  def specimenLinkAnnotationTypesForStudy(studyId: String)
      : DomainValidation[Set[SpecimenLinkAnnotationType]] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => specimenLinkAnnotationTypeRepository.allForStudy(StudyId(studyId)).success
    )
  }

  def addSpecimenLinkAnnotationType(cmd: AddSpecimenLinkAnnotationTypeCmd)
    : Future[DomainValidation[SpecimenLinkAnnotationTypeAddedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[SpecimenLinkAnnotationTypeAddedEvent]]

  def updateSpecimenLinkAnnotationType(cmd: UpdateSpecimenLinkAnnotationTypeCmd)
    : Future[DomainValidation[SpecimenLinkAnnotationTypeUpdatedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[SpecimenLinkAnnotationTypeUpdatedEvent]]

  def removeSpecimenLinkAnnotationType(cmd: RemoveSpecimenLinkAnnotationTypeCmd)
    : Future[DomainValidation[SpecimenLinkAnnotationTypeRemovedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[SpecimenLinkAnnotationTypeRemovedEvent]]

  // processing types
  def addProcessingType(cmd: AddProcessingTypeCmd)
    : Future[DomainValidation[ProcessingTypeAddedEvent]] = {
    ask(processor, cmd).mapTo[DomainValidation[ProcessingTypeAddedEvent]]
  }

  def updateProcessingType(cmd: UpdateProcessingTypeCmd)
    : Future[DomainValidation[ProcessingTypeUpdatedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[ProcessingTypeUpdatedEvent]]

  def removeProcessingType(cmd: RemoveProcessingTypeCmd)
    : Future[DomainValidation[ProcessingTypeRemovedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[ProcessingTypeRemovedEvent]]

  // specimen link types
  def addSpecimenLinkType(cmd: AddSpecimenLinkTypeCmd)
    : Future[DomainValidation[SpecimenLinkTypeAddedEvent]] = {
    ask(processor, cmd).mapTo[DomainValidation[SpecimenLinkTypeAddedEvent]]
  }

  def updateSpecimenLinkType(cmd: UpdateSpecimenLinkTypeCmd)
      : Future[DomainValidation[SpecimenLinkTypeUpdatedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[SpecimenLinkTypeUpdatedEvent]]

  def removeSpecimenLinkType(cmd: RemoveSpecimenLinkTypeCmd)
      : Future[DomainValidation[SpecimenLinkTypeRemovedEvent]] =
    ask(processor, cmd).mapTo[DomainValidation[SpecimenLinkTypeRemovedEvent]]

}
