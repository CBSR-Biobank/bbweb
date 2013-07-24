package service.study

import service.commands._
import service.events._
import service._
import domain.{ AnnotationTypeId, DomainValidation, UserId }
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
class StudyService(
  studyProcessor: ActorRef)(implicit system: ActorSystem)
  extends ApplicationService {
  import system.dispatcher

  val log = LoggerFactory.getLogger(this.getClass)

  /**
   * FIXME: use paging and sorting
   */
  def getAll: Set[Study] = {
    StudyRepository.allStudies
  }

  def getStudy(id: String): DomainValidation[Study] = {
    StudyRepository.studyWithId(new StudyId(id))
  }

  def specimenGroupWithId(studyId: String, specimenGroupId: String): DomainValidation[SpecimenGroup] = {
    SpecimenGroupRepository.specimenGroupWithId(
      StudyId(studyId), SpecimenGroupId(specimenGroupId))
  }

  def specimenGroupsForStudy(studyId: String): Set[SpecimenGroup] =
    SpecimenGroupRepository.allSpecimenGroupsForStudy(StudyId(studyId))

  def collectionEventAnnotationTypeWithId(
    studyId: String,
    annotationTypeId: String): DomainValidation[CollectionEventAnnotationType] = {
    CollectionEventAnnotationTypeRepository.annotationTypeWithId(
      StudyId(studyId), AnnotationTypeId(annotationTypeId))
  }

  def collectionEventAnnotationTypesForStudy(id: String): Set[CollectionEventAnnotationType] = {
    CollectionEventAnnotationTypeRepository.allCollectionEventAnnotationTypesForStudy(StudyId(id))
  }

  def collectionEventTypeWithId(
    studyId: String,
    collectionEventTypeId: String): DomainValidation[CollectionEventType] = {
    CollectionEventTypeRepository.collectionEventTypeWithId(
      StudyId(studyId), CollectionEventTypeId(collectionEventTypeId))
  }

  def collectionEventTypesForStudy(studyId: String): Set[CollectionEventType] = {
    CollectionEventTypeRepository.allCollectionEventTypesForStudy(StudyId(studyId))
  }

  def addStudy(cmd: AddStudyCmd)(implicit userId: UserId): Future[DomainValidation[DisabledStudy]] = {
    play.Logger.debug("addStudy")
    studyProcessor.ask(
      Message(ServiceMsg(cmd, userId, Some(StudyIdentityService.nextIdentity)))).map(
        _.asInstanceOf[DomainValidation[DisabledStudy]])
  }

  def updateStudy(cmd: UpdateStudyCmd)(implicit userId: UserId): Future[DomainValidation[DisabledStudy]] =
    studyProcessor.ask(Message(ServiceMsg(cmd, userId))).map(
      _.asInstanceOf[DomainValidation[DisabledStudy]])

  def enableStudy(cmd: EnableStudyCmd)(implicit userId: UserId): Future[DomainValidation[EnabledStudy]] =
    studyProcessor.ask(Message(ServiceMsg(cmd, userId))).map(
      _.asInstanceOf[DomainValidation[EnabledStudy]])

  def disableStudy(cmd: DisableStudyCmd)(implicit userId: UserId): Future[DomainValidation[DisabledStudy]] =
    studyProcessor.ask(Message(ServiceMsg(cmd, userId))).map(
      _.asInstanceOf[DomainValidation[DisabledStudy]])

  // specimen groups
  def addSpecimenGroup(cmd: AddSpecimenGroupCmd)(implicit userId: UserId): Future[DomainValidation[SpecimenGroup]] = {
    studyProcessor.ask(
      Message(ServiceMsg(cmd, userId, Some(SpecimenGroupIdentityService.nextIdentity)))).map(
        _.asInstanceOf[DomainValidation[SpecimenGroup]])
  }

  def specimenGroupInUse(studyId: String, specimenGroupId: String): DomainValidation[Boolean] = {
    for {
      sg <- specimenGroupWithId(studyId, specimenGroupId)
      canUpdate <- CollectionEventTypeRepository.specimenGroupInUse(sg).success
    } yield canUpdate
  }

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd)(implicit userId: UserId): Future[DomainValidation[SpecimenGroup]] =
    studyProcessor.ask(
      Message(ServiceMsg(cmd, userId))).map(_.asInstanceOf[DomainValidation[SpecimenGroup]])

  def removeSpecimenGroup(cmd: RemoveSpecimenGroupCmd)(implicit userId: UserId): Future[DomainValidation[SpecimenGroup]] =
    studyProcessor.ask(
      Message(ServiceMsg(cmd, userId))).map(_.asInstanceOf[DomainValidation[SpecimenGroup]])

  // collection event types
  def addCollectionEventType(cmd: AddCollectionEventTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventType]] = {
    studyProcessor.ask(
      Message(ServiceMsg(cmd, userId, Some(CollectionEventTypeIdentityService.nextIdentity)))).map(
        _.asInstanceOf[DomainValidation[CollectionEventType]])
  }

  def updateCollectionEventType(cmd: UpdateCollectionEventTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventType]] =
    studyProcessor.ask(Message(ServiceMsg(cmd, userId))).map(_.asInstanceOf[DomainValidation[CollectionEventType]])

  def removeCollectionEventType(cmd: RemoveCollectionEventTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventType]] =
    studyProcessor.ask(
      Message(ServiceMsg(cmd, userId))).map(_.asInstanceOf[DomainValidation[CollectionEventType]])

  // collection event annotation types
  def addCollectionEventAnnotationType(
    cmd: AddCollectionEventAnnotationTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationType]] = {
    studyProcessor.ask(
      Message(ServiceMsg(cmd, userId, Some(CollectionEventAnnotationTypeIdentityService.nextIdentity)))).map(
        _.asInstanceOf[DomainValidation[CollectionEventAnnotationType]])
  }

  def canUpdateCollectionEventAnnotationType(
    studyId: String, annotationTypeId: String): DomainValidation[Boolean] = {
    for {
      at <- collectionEventAnnotationTypeWithId(studyId, annotationTypeId)
      canUpdate <- (!CollectionEventTypeRepository.annotationTypeInUse(at)).success
    } yield canUpdate
  }

  def updateCollectionEventAnnotationType(cmd: UpdateCollectionEventAnnotationTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationType]] =
    studyProcessor.ask(Message(ServiceMsg(cmd, userId))).map(
      _.asInstanceOf[DomainValidation[CollectionEventAnnotationType]])

  def removeCollectionEventAnnotationType(cmd: RemoveCollectionEventAnnotationTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationType]] =
    studyProcessor.ask(
      Message(ServiceMsg(cmd, userId))).map(
        _.asInstanceOf[DomainValidation[CollectionEventAnnotationType]])
}
