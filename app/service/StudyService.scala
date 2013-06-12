package service

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import scala.language.postfixOps
import org.eligosource.eventsourced.core._
import domain.{
  AnnotationOption,
  AnnotationTypeId,
  ConcurrencySafeEntity,
  DomainValidation,
  DomainError,
  Entity
}
import domain.study._
import infrastructure.commands._
import infrastructure.events._
import infrastructure._
import domain.service.{ CollectionEventTypeDomainService, SpecimenGroupDomainService }

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

  def addStudy(cmd: AddStudyCmd): Future[DomainValidation[DisabledStudy]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[DisabledStudy]])

  def updateStudy(cmd: UpdateStudyCmd): Future[DomainValidation[DisabledStudy]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[DisabledStudy]])

  def enableStudy(cmd: EnableStudyCmd): Future[DomainValidation[EnabledStudy]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[EnabledStudy]])

  def disableStudy(cmd: DisableStudyCmd): Future[DomainValidation[DisabledStudy]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[DisabledStudy]])

  // specimen groups
  def addSpecimenGroup(cmd: AddSpecimenGroupCmd): Future[DomainValidation[SpecimenGroup]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroup]])

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd): Future[DomainValidation[SpecimenGroup]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroup]])

  def removeSpecimenGroup(cmd: RemoveSpecimenGroupCmd): Future[DomainValidation[SpecimenGroup]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroup]])

  // collection event types
  def addCollectionEventType(cmd: AddCollectionEventTypeCmd): Future[DomainValidation[CollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventType]])

  def updateCollectionEventType(cmd: UpdateCollectionEventTypeCmd): Future[DomainValidation[CollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventType]])

  def removeCollectionEventType(cmd: RemoveCollectionEventTypeCmd): Future[DomainValidation[CollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventType]])

  // specimen group -> collection event types
  def addSpecimenGroupToCollectionEventType(cmd: AddSpecimenGroupToCollectionEventTypeCmd): Future[DomainValidation[SpecimenGroupCollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroupCollectionEventType]])

  def removeSpecimenGroupFromCollectionEventType(cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd): Future[DomainValidation[SpecimenGroupCollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroupCollectionEventType]])

  // study annotation type
  def addCollectionEventAnnotationType(cmd: AddCollectionEventAnnotationTypeCmd): Future[DomainValidation[CollectionEventAnnotationType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventAnnotationType]])

  def updateCollectionEventAnnotationType(cmd: UpdateCollectionEventAnnotationTypeCmd): Future[DomainValidation[CollectionEventAnnotationType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventAnnotationType]])

  def removeCollectionEventAnnotationType(cmd: RemoveCollectionEventAnnotationTypeCmd): Future[DomainValidation[CollectionEventAnnotationType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventAnnotationType]])

  // annotation types -> collection event types
  def addAnnotationTypeToCollectionEventType(cmd: AddAnnotationTypeToCollectionEventTypeCmd): Future[DomainValidation[CollectionEventTypeAnnotationType]] = {
    println("********* here ********")
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventTypeAnnotationType]])
  }

  def removeAnnotationTypeFromCollectionEventType(cmd: RemoveAnnotationTypeFromCollectionEventTypeCmd): Future[DomainValidation[CollectionEventTypeAnnotationType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventTypeAnnotationType]])
}

object StudyService {
}
