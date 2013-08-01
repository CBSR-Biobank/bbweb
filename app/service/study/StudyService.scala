package service.study

import service.commands._
import service.events._
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

    /**
     * FIXME: use paging and sorting
     */
    def getAll: Set[Study]

    def getStudy(id: String): DomainValidation[Study]

    def specimenGroupWithId(studyId: String, specimenGroupId: String): DomainValidation[SpecimenGroup]

    def specimenGroupsForStudy(studyId: String): Set[SpecimenGroup]

    def collectionEventAnnotationTypeWithId(
      studyId: String,
      annotationTypeId: String): DomainValidation[CollectionEventAnnotationType]

    def collectionEventAnnotationTypesForStudy(id: String): Set[CollectionEventAnnotationType]

    def collectionEventTypeWithId(
      studyId: String,
      collectionEventTypeId: String): DomainValidation[CollectionEventType]

    def collectionEventTypesForStudy(studyId: String): Set[CollectionEventType]

    def addStudy(cmd: AddStudyCmd)(implicit userId: UserId): Future[DomainValidation[DisabledStudy]]

    def updateStudy(cmd: UpdateStudyCmd)(implicit userId: UserId): Future[DomainValidation[DisabledStudy]]

    def enableStudy(cmd: EnableStudyCmd)(implicit userId: UserId): Future[DomainValidation[EnabledStudy]]

    def disableStudy(cmd: DisableStudyCmd)(implicit userId: UserId): Future[DomainValidation[DisabledStudy]]

    // specimen groups
    def addSpecimenGroup(cmd: AddSpecimenGroupCmd)(implicit userId: UserId): Future[DomainValidation[SpecimenGroup]]

    def specimenGroupInUse(studyId: String, specimenGroupId: String): DomainValidation[Boolean]

    def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd)(implicit userId: UserId): Future[DomainValidation[SpecimenGroup]]

    def removeSpecimenGroup(cmd: RemoveSpecimenGroupCmd)(implicit userId: UserId): Future[DomainValidation[SpecimenGroup]]

    // collection event types
    def addCollectionEventType(cmd: AddCollectionEventTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventType]]

    def updateCollectionEventType(cmd: UpdateCollectionEventTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventType]]

    def removeCollectionEventType(cmd: RemoveCollectionEventTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventType]]

    // collection event annotation types
    def addCollectionEventAnnotationType(
      cmd: AddCollectionEventAnnotationTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationType]]

    def collectionEventAnnotationTypeInUse(
      studyId: String, annotationTypeId: String): DomainValidation[Boolean]

    def updateCollectionEventAnnotationType(cmd: UpdateCollectionEventAnnotationTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationType]]

    def removeCollectionEventAnnotationType(cmd: RemoveCollectionEventAnnotationTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationType]]
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
      collectionEventAnnotationTypeRepository.allCollectionEventAnnotationTypesForStudy(StudyId(id))
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

    def addStudy(cmd: AddStudyCmd)(implicit userId: UserId): Future[DomainValidation[DisabledStudy]] = {
      commandBus.ask(
        Message(ServiceMsg(cmd, userId, Some(StudyIdentityService.nextIdentity)))).map(
          _.asInstanceOf[DomainValidation[DisabledStudy]])
    }

    def updateStudy(cmd: UpdateStudyCmd)(implicit userId: UserId): Future[DomainValidation[DisabledStudy]] =
      commandBus.ask(Message(ServiceMsg(cmd, userId))).map(
        _.asInstanceOf[DomainValidation[DisabledStudy]])

    def enableStudy(cmd: EnableStudyCmd)(implicit userId: UserId): Future[DomainValidation[EnabledStudy]] =
      commandBus.ask(Message(ServiceMsg(cmd, userId))).map(
        _.asInstanceOf[DomainValidation[EnabledStudy]])

    def disableStudy(cmd: DisableStudyCmd)(implicit userId: UserId): Future[DomainValidation[DisabledStudy]] =
      commandBus.ask(Message(ServiceMsg(cmd, userId))).map(
        _.asInstanceOf[DomainValidation[DisabledStudy]])

    // specimen groups
    def addSpecimenGroup(cmd: AddSpecimenGroupCmd)(implicit userId: UserId): Future[DomainValidation[SpecimenGroup]] = {
      commandBus.ask(
        Message(ServiceMsg(cmd, userId, Some(SpecimenGroupIdentityService.nextIdentity)))).map(
          _.asInstanceOf[DomainValidation[SpecimenGroup]])
    }

    def specimenGroupInUse(studyId: String, specimenGroupId: String): DomainValidation[Boolean] = {
      for {
        sg <- specimenGroupWithId(studyId, specimenGroupId)
        inUse <- collectionEventTypeRepository.specimenGroupInUse(sg).success
      } yield inUse
    }

    def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd)(implicit userId: UserId): Future[DomainValidation[SpecimenGroup]] =
      commandBus.ask(
        Message(ServiceMsg(cmd, userId))).map(_.asInstanceOf[DomainValidation[SpecimenGroup]])

    def removeSpecimenGroup(cmd: RemoveSpecimenGroupCmd)(implicit userId: UserId): Future[DomainValidation[SpecimenGroup]] =
      commandBus.ask(
        Message(ServiceMsg(cmd, userId))).map(_.asInstanceOf[DomainValidation[SpecimenGroup]])

    // collection event types
    def addCollectionEventType(cmd: AddCollectionEventTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventType]] = {
      commandBus.ask(
        Message(ServiceMsg(cmd, userId, Some(CollectionEventTypeIdentityService.nextIdentity)))).map(
          _.asInstanceOf[DomainValidation[CollectionEventType]])
    }

    def updateCollectionEventType(cmd: UpdateCollectionEventTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventType]] =
      commandBus.ask(Message(ServiceMsg(cmd, userId))).map(_.asInstanceOf[DomainValidation[CollectionEventType]])

    def removeCollectionEventType(cmd: RemoveCollectionEventTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventType]] =
      commandBus.ask(
        Message(ServiceMsg(cmd, userId))).map(_.asInstanceOf[DomainValidation[CollectionEventType]])

    // collection event annotation types
    def addCollectionEventAnnotationType(
      cmd: AddCollectionEventAnnotationTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationType]] = {
      commandBus.ask(
        Message(ServiceMsg(cmd, userId, Some(CollectionEventAnnotationTypeIdentityService.nextIdentity)))).map(
          _.asInstanceOf[DomainValidation[CollectionEventAnnotationType]])
    }

    def collectionEventAnnotationTypeInUse(
      studyId: String, annotationTypeId: String): DomainValidation[Boolean] = {
      for {
        at <- collectionEventAnnotationTypeWithId(studyId, annotationTypeId)
        inUse <- collectionEventTypeRepository.annotationTypeInUse(at).success
      } yield inUse
    }

    def updateCollectionEventAnnotationType(cmd: UpdateCollectionEventAnnotationTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationType]] =
      commandBus.ask(Message(ServiceMsg(cmd, userId))).map(
        _.asInstanceOf[DomainValidation[CollectionEventAnnotationType]])

    def removeCollectionEventAnnotationType(cmd: RemoveCollectionEventAnnotationTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationType]] =
      commandBus.ask(
        Message(ServiceMsg(cmd, userId))).map(
          _.asInstanceOf[DomainValidation[CollectionEventAnnotationType]])
  }
}