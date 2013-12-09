package service
package study

import service.commands.StudyCommands._
import service.events.StudyEvents._
import service._
import domain._
import domain.study._

import akka.actor._
import akka.pattern.ask
import scala.concurrent._
import scala.concurrent.duration._
import org.eligosource.eventsourced.core._
import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

trait StudyServiceComponent {

  val studyService: StudyService

  trait StudyService extends ApplicationService {

    def getAll: Set[Study]

    def getStudy(id: String): DomainValidation[Study]

    def specimenGroupWithId(
      studyId: String,
      specimenGroupId: String): DomainValidation[SpecimenGroup]

    def specimenGroupsForStudy(studyId: String): Set[SpecimenGroup]

    def collectionEventAnnotationTypeWithId(
      studyId: String,
      annotationTypeId: String): DomainValidation[CollectionEventAnnotationType]

    def collectionEventAnnotationTypesForStudy(id: String): Set[CollectionEventAnnotationType]

    def collectionEventTypeWithId(
      studyId: String,
      collectionEventTypeId: String): DomainValidation[CollectionEventType]

    def collectionEventTypesForStudy(studyId: String): Set[CollectionEventType]

    def addStudy(cmd: AddStudyCmd)(
      implicit userId: UserId): Future[DomainValidation[StudyAddedEvent]]

    def updateStudy(cmd: UpdateStudyCmd)(
      implicit userId: UserId): Future[DomainValidation[StudyUpdatedEvent]]

    def enableStudy(cmd: EnableStudyCmd)(
      implicit userId: UserId): Future[DomainValidation[StudyEnabledEvent]]

    def disableStudy(cmd: DisableStudyCmd)(
      implicit userId: UserId): Future[DomainValidation[StudyDisabledEvent]]

    // specimen groups
    def specimenGroupInUse(studyId: String, specimenGroupId: String): DomainValidation[Boolean]

    def addSpecimenGroup(cmd: AddSpecimenGroupCmd)(
      implicit userId: UserId): Future[DomainValidation[SpecimenGroupAddedEvent]]

    def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd)(
      implicit userId: UserId): Future[DomainValidation[SpecimenGroupUpdatedEvent]]

    def removeSpecimenGroup(cmd: RemoveSpecimenGroupCmd)(
      implicit userId: UserId): Future[DomainValidation[SpecimenGroupRemovedEvent]]

    // collection event types
    def addCollectionEventType(cmd: AddCollectionEventTypeCmd)(
      implicit userId: UserId): Future[DomainValidation[CollectionEventTypeAddedEvent]]

    def updateCollectionEventType(cmd: UpdateCollectionEventTypeCmd)(
      implicit userId: UserId): Future[DomainValidation[CollectionEventTypeUpdatedEvent]]

    def removeCollectionEventType(cmd: RemoveCollectionEventTypeCmd)(
      implicit userId: UserId): Future[DomainValidation[CollectionEventTypeRemovedEvent]]

    // collection event annotation types
    def isCollectionEventAnnotationTypeInUse(
      studyId: String, annotationTypeId: String): DomainValidation[Boolean]

    def addCollectionEventAnnotationType(
      cmd: AddCollectionEventAnnotationTypeCmd)(
        implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationTypeAddedEvent]]

    def updateCollectionEventAnnotationType(
      cmd: UpdateCollectionEventAnnotationTypeCmd)(
        implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationTypeUpdatedEvent]]

    def removeCollectionEventAnnotationType(
      cmd: RemoveCollectionEventAnnotationTypeCmd)(
        implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationTypeRemovedEvent]]

    // participant annotation types
    def isParticipantAnnotationTypeInUse(
      studyId: String,
      annotationTypeId: String): DomainValidation[Boolean]

    def participantAnnotationTypesForStudy(studyId: String): Set[ParticipantAnnotationType]

    def participantAnnotationTypeWithId(
      studyId: String,
      annotationTypeId: String): DomainValidation[ParticipantAnnotationType]

    def addParticipantAnnotationType(
      cmd: AddParticipantAnnotationTypeCmd)(
        implicit userId: UserId): Future[DomainValidation[ParticipantAnnotationTypeAddedEvent]]

    def updateParticipantAnnotationType(
      cmd: UpdateParticipantAnnotationTypeCmd)(
        implicit userId: UserId): Future[DomainValidation[ParticipantAnnotationTypeUpdatedEvent]]

    def removeParticipantAnnotationType(
      cmd: RemoveParticipantAnnotationTypeCmd)(
        implicit userId: UserId): Future[DomainValidation[ParticipantAnnotationTypeRemovedEvent]]

    // specimen link annotation types
    def specimenLinkAnnotationTypeWithId(
      studyId: String,
      annotationTypeId: String): DomainValidation[SpecimenLinkAnnotationType]

    def isSpecimenLinkAnnotationTypeInUse(
      studyId: String,
      annotationTypeId: String): DomainValidation[Boolean]

    def specimenLinkAnnotationTypesForStudy(id: String): Set[SpecimenLinkAnnotationType]

    def addSpecimenLinkAnnotationType(
      cmd: AddSpecimenLinkAnnotationTypeCmd)(
        implicit userId: UserId): Future[DomainValidation[SpecimenLinkAnnotationTypeAddedEvent]]

    def updateSpecimenLinkAnnotationType(
      cmd: UpdateSpecimenLinkAnnotationTypeCmd)(
        implicit userId: UserId): Future[DomainValidation[SpecimenLinkAnnotationTypeUpdatedEvent]]

    def removeSpecimenLinkAnnotationType(
      cmd: RemoveSpecimenLinkAnnotationTypeCmd)(
        implicit userId: UserId): Future[DomainValidation[SpecimenLinkAnnotationTypeRemovedEvent]]
  }

}

/**
 * This is the Study Aggregate Application Service.
 *
 * Handles the commands to configure studies. the commands are forwarded to the Study Aggregate
 * Processor.
 *
 * @param studyRepository The repository for study entities.
 * @param specimenGroupRepository The repository for specimen group entities.
 * @param cetRepo The repository for Container Event Type entities.
 * @param The study aggregate processor that command messages should be forwarded to.
 *
 */
trait StudyServiceComponentImpl extends StudyServiceComponent {
  self: RepositoryComponent =>

  class StudyServiceImpl(commandBus: ActorRef)(implicit system: ActorSystem) extends StudyService {
    import system.dispatcher

    val log = LoggerFactory.getLogger(this.getClass)

    /**
     * FIXME: use paging and sorting
     */
    def getAll: Set[Study] = {
      studyRepository.allStudies
    }

    def getStudy(id: String): DomainValidation[Study] = {
      studyRepository.studyWithId(new StudyId(id))
    }

    def specimenGroupWithId(studyId: String, specimenGroupId: String): DomainValidation[SpecimenGroup] = {
      specimenGroupRepository.specimenGroupWithId(
        StudyId(studyId), SpecimenGroupId(specimenGroupId))
    }

    def specimenGroupsForStudy(studyId: String): Set[SpecimenGroup] =
      specimenGroupRepository.allSpecimenGroupsForStudy(StudyId(studyId))

    def collectionEventAnnotationTypeWithId(
      studyId: String,
      annotationTypeId: String): DomainValidation[CollectionEventAnnotationType] = {
      collectionEventAnnotationTypeRepository.annotationTypeWithId(
        StudyId(studyId), AnnotationTypeId(annotationTypeId))
    }

    def collectionEventAnnotationTypesForStudy(id: String): Set[CollectionEventAnnotationType] = {
      collectionEventAnnotationTypeRepository.allAnnotationTypesForStudy(StudyId(id))
    }

    def collectionEventTypeWithId(
      studyId: String,
      collectionEventTypeId: String): DomainValidation[CollectionEventType] = {
      collectionEventTypeRepository.collectionEventTypeWithId(
        StudyId(studyId), CollectionEventTypeId(collectionEventTypeId))
    }

    def collectionEventTypesForStudy(studyId: String): Set[CollectionEventType] = {
      collectionEventTypeRepository.allCollectionEventTypesForStudy(StudyId(studyId))
    }

    def participantAnnotationTypesForStudy(studyId: String): Set[ParticipantAnnotationType] =
      participantAnnotationTypeRepository.allAnnotationTypesForStudy(StudyId(studyId))

    def participantAnnotationTypeWithId(
      studyId: String,
      annotationTypeId: String): DomainValidation[ParticipantAnnotationType] = {
      participantAnnotationTypeRepository.annotationTypeWithId(
        StudyId(studyId), AnnotationTypeId(annotationTypeId))
    }

    def specimenLinkAnnotationTypeWithId(
      studyId: String,
      annotationTypeId: String): DomainValidation[SpecimenLinkAnnotationType] = {
      specimenLinkAnnotationTypeRepository.annotationTypeWithId(
        StudyId(studyId), AnnotationTypeId(annotationTypeId))
    }

    def addStudy(cmd: AddStudyCmd)(
      implicit userId: UserId): Future[DomainValidation[StudyAddedEvent]] = {
      commandBus ?
        Message(ServiceMsg(cmd, userId, Some(StudyIdentityService.nextIdentity))) map (
          _.asInstanceOf[DomainValidation[StudyAddedEvent]])
    }

    def updateStudy(cmd: UpdateStudyCmd)(
      implicit userId: UserId): Future[DomainValidation[StudyUpdatedEvent]] =
      commandBus ?
        Message(ServiceMsg(cmd, userId)) map (
          _.asInstanceOf[DomainValidation[StudyUpdatedEvent]])

    def enableStudy(cmd: EnableStudyCmd)(
      implicit userId: UserId): Future[DomainValidation[StudyEnabledEvent]] =
      commandBus ? Message(ServiceMsg(cmd, userId)) map (
        _.asInstanceOf[DomainValidation[StudyEnabledEvent]])

    def disableStudy(cmd: DisableStudyCmd)(
      implicit userId: UserId): Future[DomainValidation[StudyDisabledEvent]] =
      commandBus ? Message(ServiceMsg(cmd, userId)) map (
        _.asInstanceOf[DomainValidation[StudyDisabledEvent]])

    // specimen groups
    //
    // FIXME: rename this to specimenGroupCanBeUpdated
    def specimenGroupInUse(studyId: String, specimenGroupId: String): DomainValidation[Boolean] = {
      for {
        sg <- specimenGroupWithId(studyId, specimenGroupId)
        inUse <- collectionEventTypeRepository.specimenGroupInUse(sg).success
      } yield inUse
    }

    def addSpecimenGroup(cmd: AddSpecimenGroupCmd)(
      implicit userId: UserId): Future[DomainValidation[SpecimenGroupAddedEvent]] = {
      commandBus ?
        Message(ServiceMsg(cmd, userId, Some(SpecimenGroupIdentityService.nextIdentity))) map (
          _.asInstanceOf[DomainValidation[SpecimenGroupAddedEvent]])
    }

    def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd)(
      implicit userId: UserId): Future[DomainValidation[SpecimenGroupUpdatedEvent]] =
      commandBus ?
        Message(ServiceMsg(cmd, userId)) map (_.asInstanceOf[DomainValidation[SpecimenGroupUpdatedEvent]])

    def removeSpecimenGroup(cmd: RemoveSpecimenGroupCmd)(
      implicit userId: UserId): Future[DomainValidation[SpecimenGroupRemovedEvent]] =
      commandBus ?
        Message(ServiceMsg(cmd, userId)) map (_.asInstanceOf[DomainValidation[SpecimenGroupRemovedEvent]])

    // collection event types
    def addCollectionEventType(cmd: AddCollectionEventTypeCmd)(
      implicit userId: UserId): Future[DomainValidation[CollectionEventTypeAddedEvent]] = {
      commandBus ?
        Message(ServiceMsg(cmd, userId, Some(CollectionEventTypeIdentityService.nextIdentity))) map (
          _.asInstanceOf[DomainValidation[CollectionEventTypeAddedEvent]])
    }

    def updateCollectionEventType(cmd: UpdateCollectionEventTypeCmd)(
      implicit userId: UserId): Future[DomainValidation[CollectionEventTypeUpdatedEvent]] =
      commandBus ? Message(ServiceMsg(cmd, userId)) map (
        _.asInstanceOf[DomainValidation[CollectionEventTypeUpdatedEvent]])

    def removeCollectionEventType(cmd: RemoveCollectionEventTypeCmd)(
      implicit userId: UserId): Future[DomainValidation[CollectionEventTypeRemovedEvent]] =
      commandBus ? Message(ServiceMsg(cmd, userId)) map (
        _.asInstanceOf[DomainValidation[CollectionEventTypeRemovedEvent]])

    // collection event annotation types
    //
    // FIXME: rename this to collectionEventAnnotationTypeCanBeUpdated
    def isCollectionEventAnnotationTypeInUse(
      studyId: String, annotationTypeId: String): DomainValidation[Boolean] = {
      for {
        at <- collectionEventAnnotationTypeWithId(studyId, annotationTypeId)
        inUse <- collectionEventTypeRepository.annotationTypeInUse(at).success
      } yield inUse
    }

    def addCollectionEventAnnotationType(
      cmd: AddCollectionEventAnnotationTypeCmd)(
        implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationTypeAddedEvent]] = {
      commandBus ?
        Message(ServiceMsg(cmd, userId, Some(CollectionEventAnnotationTypeIdentityService.nextIdentity))) map (
          _.asInstanceOf[DomainValidation[CollectionEventAnnotationTypeAddedEvent]])
    }

    def updateCollectionEventAnnotationType(cmd: UpdateCollectionEventAnnotationTypeCmd)(
      implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationTypeUpdatedEvent]] =
      commandBus ? Message(ServiceMsg(cmd, userId)) map (
        _.asInstanceOf[DomainValidation[CollectionEventAnnotationTypeUpdatedEvent]])

    def removeCollectionEventAnnotationType(cmd: RemoveCollectionEventAnnotationTypeCmd)(
      implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationTypeRemovedEvent]] =
      commandBus ?
        Message(ServiceMsg(cmd, userId)) map (
          _.asInstanceOf[DomainValidation[CollectionEventAnnotationTypeRemovedEvent]])

    // participant annotation types
    //
    // FIXME: rename this to participantAnnotationTypeCanBeUpdated
    def isParticipantAnnotationTypeInUse(
      studyId: String,
      annotationTypeId: String): DomainValidation[Boolean] = {
      // TODO: needs implementation
      //
      // return true if used by any participants
      false.success
    }

    def addParticipantAnnotationType(
      cmd: AddParticipantAnnotationTypeCmd)(
        implicit userId: UserId): Future[DomainValidation[ParticipantAnnotationTypeAddedEvent]] =
      commandBus ?
        Message(ServiceMsg(cmd, userId, Some(ParticipantAnnotationTypeIdentityService.nextIdentity))) map (
          _.asInstanceOf[DomainValidation[ParticipantAnnotationTypeAddedEvent]])

    def updateParticipantAnnotationType(cmd: UpdateParticipantAnnotationTypeCmd)(
      implicit userId: UserId): Future[DomainValidation[ParticipantAnnotationTypeUpdatedEvent]] =
      commandBus ? Message(ServiceMsg(cmd, userId)) map (
        _.asInstanceOf[DomainValidation[ParticipantAnnotationTypeUpdatedEvent]])

    def removeParticipantAnnotationType(cmd: RemoveParticipantAnnotationTypeCmd)(
      implicit userId: UserId): Future[DomainValidation[ParticipantAnnotationTypeRemovedEvent]] =
      commandBus ?
        Message(ServiceMsg(cmd, userId)) map (
          _.asInstanceOf[DomainValidation[ParticipantAnnotationTypeRemovedEvent]])

    // specimen link annotation types
    //
    // FIXME: rename this to specimenLinkAnnotationTypeCanBeUpdated
    def isSpecimenLinkAnnotationTypeInUse(
      studyId: String, annotationTypeId: String): DomainValidation[Boolean] = {
      // TODO: needs implementation
      //
      // return true if used by any participants
      false.success
    }

    def specimenLinkAnnotationTypesForStudy(studyId: String): Set[SpecimenLinkAnnotationType] =
      specimenLinkAnnotationTypeRepository.allAnnotationTypesForStudy(StudyId(studyId))

    def addSpecimenLinkAnnotationType(
      cmd: AddSpecimenLinkAnnotationTypeCmd)(
        implicit userId: UserId): Future[DomainValidation[SpecimenLinkAnnotationTypeAddedEvent]] =
      commandBus ?
        Message(ServiceMsg(cmd, userId, Some(SpecimenLinkAnnotationTypeIdentityService.nextIdentity))) map (
          _.asInstanceOf[DomainValidation[SpecimenLinkAnnotationTypeAddedEvent]])

    def updateSpecimenLinkAnnotationType(cmd: UpdateSpecimenLinkAnnotationTypeCmd)(
      implicit userId: UserId): Future[DomainValidation[SpecimenLinkAnnotationTypeUpdatedEvent]] =
      commandBus ? Message(ServiceMsg(cmd, userId)) map (
        _.asInstanceOf[DomainValidation[SpecimenLinkAnnotationTypeUpdatedEvent]])

    def removeSpecimenLinkAnnotationType(cmd: RemoveSpecimenLinkAnnotationTypeCmd)(
      implicit userId: UserId): Future[DomainValidation[SpecimenLinkAnnotationTypeRemovedEvent]] =
      commandBus ?
        Message(ServiceMsg(cmd, userId)) map (
          _.asInstanceOf[DomainValidation[SpecimenLinkAnnotationTypeRemovedEvent]])
  }
}
