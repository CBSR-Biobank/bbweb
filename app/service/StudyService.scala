package service

import service.commands._
import service.events._
import domain.{
  AnnotationTypeId,
  ConcurrencySafeEntity,
  DomainValidation,
  DomainError,
  Entity,
  UserId
}
import domain._
import domain.study._
import service.study.{ SpecimenGroupService }
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import scala.language.postfixOps
import org.eligosource.eventsourced.core._
import scalaz._
import Scalaz._
import domain.AnnotationTypeId

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

  /**
   * FIXME: use paging and sorting
   */
  def getAll: Set[Study] = {
    StudyRepository.allStudies
  }

  def getStudy(id: String): DomainValidation[Study] = {
    StudyRepository.studyWithId(new StudyId(id))
  }

  def getSpecimenGroup(
    studyId: String, specimenGroupId: String): DomainValidation[SpecimenGroup] = {
    SpecimenGroupRepository.specimenGroupWithId(
      StudyId(studyId), SpecimenGroupId(specimenGroupId))
  }

  def getSpecimenGroups(studyId: String): Set[SpecimenGroup] =
    SpecimenGroupRepository.allSpecimenGroupsForStudy(StudyId(studyId))

  // FIXME: rename to collectionEventAnnotationType
  def getCollectionEventAnnotationType(
    studyId: String,
    annotationTypeId: String): DomainValidation[CollectionEventAnnotationType] = {
    CollectionEventAnnotationTypeRepository.annotationTypeWithId(new AnnotationTypeId(annotationTypeId))
  }

  // FIXME: rename to allCollectionEventAnnotationType
  def getCollectionEventAnnotationTypes(id: String): DomainValidation[Set[CollectionEventAnnotationType]] = {
    CollectionEventAnnotationTypeRepository.allCollectionEventAnnotationTypesForStudy(StudyId(id))
  }

  // FIXME: rename to collectionEventType
  def getCollectionEventType(
    studyId: String,
    collectionEventTypeId: String): DomainValidation[CollectionEventType] = {
    CollectionEventTypeRepository.collectionEventTypeWithId(
      StudyId(studyId), CollectionEventTypeId(collectionEventTypeId))
  }

  // FIXME: rename to allCollectionEventType
  def getCollectionEventTypes(studyId: String): Set[CollectionEventType] = {
    CollectionEventTypeRepository.allCollectionEventTypesForStudy(StudyId(studyId))
  }

  def getCollectionEventTypeSpecimenGroups(
    studyId: String,
    collectionEventTypeId: String): DomainValidation[Set[String]] = {
    for {
      cet <- getCollectionEventType(studyId, collectionEventTypeId)
      sgIds <- SpecimenGroupCollectionEventTypeRepository.getValues.filter(
        x => x.id.equals(collectionEventTypeId)).map(sgcet => sgcet.specimenGroupId.id).success
    } yield sgIds.toSet
  }

  def getCollectionEventTypeAnnotationTypes(
    studyId: String,
    collectionEventTypeId: String): DomainValidation[Set[String]] = {
    for {
      cet <- getCollectionEventType(studyId, collectionEventTypeId)
      annotTypeIds <- CollectionEventTypeAnnotationTypeRepository.getValues.filter(
        x => x.id.equals(collectionEventTypeId)).map(cetAt => cetAt.annotationTypeId.id).success
    } yield annotTypeIds.toSet
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

  def updateCollectionEventAnnotationType(cmd: UpdateCollectionEventAnnotationTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationType]] =
    studyProcessor.ask(Message(ServiceMsg(cmd, userId))).map(
      _.asInstanceOf[DomainValidation[CollectionEventAnnotationType]])

  def removeCollectionEventAnnotationType(cmd: RemoveCollectionEventAnnotationTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationType]] =
    studyProcessor.ask(
      Message(ServiceMsg(cmd, userId))).map(
        _.asInstanceOf[DomainValidation[CollectionEventAnnotationType]])
}
