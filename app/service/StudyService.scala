package service

import infrastructure._
import infrastructure.commands._
import infrastructure.events._
import domain.{
  AnnotationTypeId,
  ConcurrencySafeEntity,
  Entity,
  UserId
}
import domain.study._
import domain.service.{ CollectionEventTypeDomainService, SpecimenGroupDomainService }

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
  studyRepository: ReadRepository[StudyId, Study],
  specimenGroupRepository: ReadRepository[SpecimenGroupId, SpecimenGroup],
  cetRepo: ReadRepository[CollectionEventTypeId, CollectionEventType],
  studyProcessor: ActorRef)(implicit system: ActorSystem)
  extends ApplicationService {
  import system.dispatcher

  def getAll: List[Study] = {
    studyRepository.getValues.toList
  }

  def getStudy(id: String): DomainValidation[Study] = {
    studyRepository.getByKey(new StudyId(id));
  }

  def addStudy(cmd: AddStudyCmd)(implicit userId: UserId): Future[DomainValidation[DisabledStudy]] = {
    studyProcessor ? Message(BiobankMsgWithId(cmd, userId, StudyIdentityService.nextIdentity)) map (
      _.asInstanceOf[DomainValidation[DisabledStudy]])
  }

  def updateStudy(cmd: UpdateStudyCmd)(implicit userId: UserId): Future[DomainValidation[DisabledStudy]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[DisabledStudy]])

  def enableStudy(cmd: EnableStudyCmd)(implicit userId: UserId): Future[DomainValidation[EnabledStudy]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[EnabledStudy]])

  def disableStudy(cmd: DisableStudyCmd)(implicit userId: UserId): Future[DomainValidation[DisabledStudy]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[DisabledStudy]])

  // specimen groups
  def addSpecimenGroup(cmd: AddSpecimenGroupCmd)(implicit userId: UserId): Future[DomainValidation[SpecimenGroup]] = {
    val cmdWithId = AddSpecimenGroupCmdWithId(SpecimenGroupIdentityService.nextIdentity,
      cmd.studyId, cmd.name, cmd.description, cmd.units, cmd.anatomicalSourceType,
      cmd.preservationType, cmd.preservationTemperatureType, cmd.specimenType)
    studyProcessor ? Message(cmdWithId) map (_.asInstanceOf[DomainValidation[SpecimenGroup]])
  }

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd)(implicit userId: UserId): Future[DomainValidation[SpecimenGroup]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroup]])

  def removeSpecimenGroup(cmd: RemoveSpecimenGroupCmd)(implicit userId: UserId): Future[DomainValidation[SpecimenGroup]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroup]])

  // collection event types
  def addCollectionEventType(cmd: AddCollectionEventTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventType]] = {
    val cmdWithId = AddCollectionEventTypeCmdWithId(CollectionEventTypeIdentityService.nextIdentity,
      cmd.studyId, cmd.name, cmd.description, cmd.recurring)
    studyProcessor ? Message(cmdWithId) map (_.asInstanceOf[DomainValidation[CollectionEventType]])
  }

  def updateCollectionEventType(cmd: UpdateCollectionEventTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventType]])

  def removeCollectionEventType(cmd: RemoveCollectionEventTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventType]])

  // specimen group -> collection event types
  def addSpecimenGroupToCollectionEventType(
    cmd: AddSpecimenGroupToCollectionEventTypeCmd)(implicit userId: UserId): Future[DomainValidation[SpecimenGroupCollectionEventType]] = {
    val cmdWithId = AddSpecimenGroupToCollectionEventTypeCmdWithId(
      SpecimenGroupCollectionEventTypeIdentityService.nextIdentity,
      cmd.studyId, cmd.specimenGroupId, cmd.collectionEventTypeId, cmd.count, cmd.amount)
    studyProcessor ? Message(cmdWithId) map (_.asInstanceOf[DomainValidation[SpecimenGroupCollectionEventType]])
  }

  def removeSpecimenGroupFromCollectionEventType(cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd)(implicit userId: UserId): Future[DomainValidation[SpecimenGroupCollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroupCollectionEventType]])

  // study annotation type
  def addCollectionEventAnnotationType(
    cmd: AddCollectionEventAnnotationTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationType]] = {
    val cmdWithId = AddCollectionEventAnnotationTypeCmdWithId(
      CollectionEventTypeAnnotationTypeIdentityService.nextIdentity,
      cmd.studyId, cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options)
    studyProcessor ? Message(cmdWithId) map (_.asInstanceOf[DomainValidation[CollectionEventAnnotationType]])
  }

  def updateCollectionEventAnnotationType(cmd: UpdateCollectionEventAnnotationTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventAnnotationType]])

  def removeCollectionEventAnnotationType(cmd: RemoveCollectionEventAnnotationTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventAnnotationType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventAnnotationType]])

  // annotation types -> collection event types
  def addAnnotationTypeToCollectionEventType(
    cmd: AddAnnotationTypeToCollectionEventTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventTypeAnnotationType]] = {
    val cmdWithId = AddAnnotationTypeToCollectionEventTypeCmdWithId(
      CollectionEventTypeAnnotationTypeIdentityService.nextIdentity,
      cmd.studyId, cmd.annotationTypeId, cmd.collectionEventTypeId, cmd.required)
    studyProcessor ? Message(cmdWithId) map (_.asInstanceOf[DomainValidation[CollectionEventTypeAnnotationType]])
  }

  def removeAnnotationTypeFromCollectionEventType(cmd: RemoveAnnotationTypeFromCollectionEventTypeCmd)(implicit userId: UserId): Future[DomainValidation[CollectionEventTypeAnnotationType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventTypeAnnotationType]])
}
