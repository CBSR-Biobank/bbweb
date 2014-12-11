package org.biobank.service.study

import org.biobank.service.ApplicationService
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.infrastructure.{ CollectionDto, ProcessingDto }
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

  def getAll: Set[Study]

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

  def addStudy(cmd: AddStudyCmd)(implicit userId: UserId)
      : Future[DomainValidation[StudyAddedEvent]]

  def updateStudy(cmd: UpdateStudyCmd)(implicit userId: UserId)
      : Future[DomainValidation[StudyUpdatedEvent]]

  def enableStudy(cmd: EnableStudyCmd)(implicit userId: UserId)
      : Future[DomainValidation[StudyEnabledEvent]]

  def disableStudy(cmd: DisableStudyCmd)(implicit userId: UserId)
      : Future[DomainValidation[StudyDisabledEvent]]

  def retireStudy(cmd: RetireStudyCmd)(implicit userId: UserId)
      : Future[DomainValidation[StudyRetiredEvent]]

  def unretireStudy(cmd: UnretireStudyCmd)(implicit userId: UserId)
      : Future[DomainValidation[StudyUnretiredEvent]]

  // specimen groups
  def addSpecimenGroup(cmd: AddSpecimenGroupCmd)(implicit userId: UserId)
      : Future[DomainValidation[SpecimenGroupAddedEvent]]

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd)(implicit userId: UserId)
      : Future[DomainValidation[SpecimenGroupUpdatedEvent]]

  def removeSpecimenGroup(cmd: RemoveSpecimenGroupCmd)(implicit userId: UserId)
      : Future[DomainValidation[SpecimenGroupRemovedEvent]]

  // collection event types
  def addCollectionEventType
    (cmd: AddCollectionEventTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[CollectionEventTypeAddedEvent]]

  def updateCollectionEventType
    (cmd: UpdateCollectionEventTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[CollectionEventTypeUpdatedEvent]]

  def removeCollectionEventType
    (cmd: RemoveCollectionEventTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[CollectionEventTypeRemovedEvent]]

  // collection event annotation types
  def addCollectionEventAnnotationType
    (cmd: AddCollectionEventAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[CollectionEventAnnotationTypeAddedEvent]]

  def updateCollectionEventAnnotationType
    (cmd: UpdateCollectionEventAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[CollectionEventAnnotationTypeUpdatedEvent]]

  def removeCollectionEventAnnotationType
    (cmd: RemoveCollectionEventAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[CollectionEventAnnotationTypeRemovedEvent]]

  // participant annotation types
  def participantAnnotationTypesForStudy
    (studyId: String)
      : DomainValidation[Set[ParticipantAnnotationType]]

  def participantAnnotationTypeWithId
    (studyId: String, annotationTypeId: String)
      : DomainValidation[ParticipantAnnotationType]

  def addParticipantAnnotationType
    (cmd: AddParticipantAnnotationTypeCmd)(implicit userId: UserId)
      : Future[DomainValidation[ParticipantAnnotationTypeAddedEvent]]

  def updateParticipantAnnotationType
    (cmd: UpdateParticipantAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[ParticipantAnnotationTypeUpdatedEvent]]

  def removeParticipantAnnotationType
    (cmd: RemoveParticipantAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[ParticipantAnnotationTypeRemovedEvent]]

  // specimen link annotation types
  def specimenLinkAnnotationTypeWithId
    (studyId: String, annotationTypeId: String)
      : DomainValidation[SpecimenLinkAnnotationType]

  def specimenLinkAnnotationTypesForStudy(id: String)
      : DomainValidation[Set[SpecimenLinkAnnotationType]]

  def addSpecimenLinkAnnotationType
    (cmd: AddSpecimenLinkAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[SpecimenLinkAnnotationTypeAddedEvent]]

  def updateSpecimenLinkAnnotationType
    (cmd: UpdateSpecimenLinkAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[SpecimenLinkAnnotationTypeUpdatedEvent]]

  def removeSpecimenLinkAnnotationType
    (cmd: RemoveSpecimenLinkAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[SpecimenLinkAnnotationTypeRemovedEvent]]

  // processing types
  def addProcessingType
    (cmd: AddProcessingTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[ProcessingTypeAddedEvent]]

  def updateProcessingType
    (cmd: UpdateProcessingTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[ProcessingTypeUpdatedEvent]]

  def removeProcessingType
    (cmd: RemoveProcessingTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[ProcessingTypeRemovedEvent]]

  // specimen link types
  def addSpecimenLinkType
    (cmd: AddSpecimenLinkTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[SpecimenLinkTypeAddedEvent]]

  def updateSpecimenLinkType
    (cmd: UpdateSpecimenLinkTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[SpecimenLinkTypeUpdatedEvent]]

  def removeSpecimenLinkType
    (cmd: RemoveSpecimenLinkTypeCmd)
    (implicit userId: UserId)
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
    with ApplicationService
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
  def getAll: Set[Study] = {
    studyRepository.allStudies
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

  def addStudy(cmd: AddStudyCmd)(implicit userId: UserId)
      : Future[DomainValidation[StudyAddedEvent]] = {
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[StudyAddedEvent]])
  }

  def updateStudy(cmd: UpdateStudyCmd)(implicit userId: UserId)
      : Future[DomainValidation[StudyUpdatedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[StudyUpdatedEvent]])

  def enableStudy(cmd: EnableStudyCmd)(implicit userId: UserId)
      : Future[DomainValidation[StudyEnabledEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[StudyEnabledEvent]])

  def disableStudy(cmd: DisableStudyCmd)(implicit userId: UserId)
      : Future[DomainValidation[StudyDisabledEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[StudyDisabledEvent]])

  def retireStudy(cmd: RetireStudyCmd)(implicit userId: UserId)
      : Future[DomainValidation[StudyRetiredEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[StudyRetiredEvent]])

  def unretireStudy(cmd: UnretireStudyCmd)(implicit userId: UserId)
      : Future[DomainValidation[StudyUnretiredEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[StudyUnretiredEvent]])

  // specimen groups
  def addSpecimenGroup(cmd: AddSpecimenGroupCmd)(implicit userId: UserId)
      : Future[DomainValidation[SpecimenGroupAddedEvent]] = {
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[SpecimenGroupAddedEvent]])
  }

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd)(implicit userId: UserId)
      : Future[DomainValidation[SpecimenGroupUpdatedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[SpecimenGroupUpdatedEvent]])

  def removeSpecimenGroup(cmd: RemoveSpecimenGroupCmd)(implicit userId: UserId)
      : Future[DomainValidation[SpecimenGroupRemovedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[SpecimenGroupRemovedEvent]])

  // collection event types
  def addCollectionEventType(cmd: AddCollectionEventTypeCmd)(implicit userId: UserId)
      : Future[DomainValidation[CollectionEventTypeAddedEvent]] = {
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[CollectionEventTypeAddedEvent]])
  }

  def updateCollectionEventType(cmd: UpdateCollectionEventTypeCmd)(implicit userId: UserId)
      : Future[DomainValidation[CollectionEventTypeUpdatedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[CollectionEventTypeUpdatedEvent]])

  def removeCollectionEventType(cmd: RemoveCollectionEventTypeCmd)(implicit userId: UserId)
      : Future[DomainValidation[CollectionEventTypeRemovedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[CollectionEventTypeRemovedEvent]])

  // collection event annotation types
  def addCollectionEventAnnotationType
    (cmd: AddCollectionEventAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[CollectionEventAnnotationTypeAddedEvent]] = {
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[CollectionEventAnnotationTypeAddedEvent]])
  }

  def updateCollectionEventAnnotationType
    (cmd: UpdateCollectionEventAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[CollectionEventAnnotationTypeUpdatedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[CollectionEventAnnotationTypeUpdatedEvent]])

  def removeCollectionEventAnnotationType
    (cmd: RemoveCollectionEventAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[CollectionEventAnnotationTypeRemovedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[CollectionEventAnnotationTypeRemovedEvent]])

  // participant annotation types
  def addParticipantAnnotationType
    (cmd: AddParticipantAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[ParticipantAnnotationTypeAddedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[ParticipantAnnotationTypeAddedEvent]])

  def updateParticipantAnnotationType
    (cmd: UpdateParticipantAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[ParticipantAnnotationTypeUpdatedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[ParticipantAnnotationTypeUpdatedEvent]])

  def removeParticipantAnnotationType
    (cmd: RemoveParticipantAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[ParticipantAnnotationTypeRemovedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[ParticipantAnnotationTypeRemovedEvent]])

  // specimen link annotation types
  def specimenLinkAnnotationTypesForStudy(studyId: String)
      : DomainValidation[Set[SpecimenLinkAnnotationType]] = {
    studyRepository.getByKey(StudyId(studyId)).fold(
      err => DomainError(s"invalid study id: $studyId").failureNel,
      study => specimenLinkAnnotationTypeRepository.allForStudy(StudyId(studyId)).success
    )
  }

  def addSpecimenLinkAnnotationType
    (cmd: AddSpecimenLinkAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[SpecimenLinkAnnotationTypeAddedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[SpecimenLinkAnnotationTypeAddedEvent]])

  def updateSpecimenLinkAnnotationType
    (cmd: UpdateSpecimenLinkAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[SpecimenLinkAnnotationTypeUpdatedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[SpecimenLinkAnnotationTypeUpdatedEvent]])

  def removeSpecimenLinkAnnotationType
    (cmd: RemoveSpecimenLinkAnnotationTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[SpecimenLinkAnnotationTypeRemovedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[SpecimenLinkAnnotationTypeRemovedEvent]])

  // processing types
  def addProcessingType
    (cmd: AddProcessingTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[ProcessingTypeAddedEvent]] = {
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[ProcessingTypeAddedEvent]])
  }

  def updateProcessingType
    (cmd: UpdateProcessingTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[ProcessingTypeUpdatedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[ProcessingTypeUpdatedEvent]])

  def removeProcessingType
    (cmd: RemoveProcessingTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[ProcessingTypeRemovedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[ProcessingTypeRemovedEvent]])

  // specimen link types
  def addSpecimenLinkType
    (cmd: AddSpecimenLinkTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[SpecimenLinkTypeAddedEvent]] = {
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[SpecimenLinkTypeAddedEvent]])
  }

  def updateSpecimenLinkType
    (cmd: UpdateSpecimenLinkTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[SpecimenLinkTypeUpdatedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[SpecimenLinkTypeUpdatedEvent]])

  def removeSpecimenLinkType
    (cmd: RemoveSpecimenLinkTypeCmd)
    (implicit userId: UserId)
      : Future[DomainValidation[SpecimenLinkTypeRemovedEvent]] =
    ask(processor, cmd, userId).map (
      _.asInstanceOf[DomainValidation[SpecimenLinkTypeRemovedEvent]])

}
