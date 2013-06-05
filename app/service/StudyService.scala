package service

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import scala.language.postfixOps

import org.eligosource.eventsourced.core._

import domain._
import domain.study._
import infrastructure.commands._
import infrastructure.events._

import scalaz._
import Scalaz._

class StudyService(
  studyRepository: Repository[StudyId, Study],
  specimenGroupRepository: Repository[SpecimenGroupId, SpecimenGroup],
  collectionEventTypeRepository: Repository[CollectionEventTypeId, CollectionEventType],
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

  def addSpecimenGroup(cmd: AddSpecimenGroupCmd): Future[DomainValidation[SpecimenGroup]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroup]])

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd): Future[DomainValidation[SpecimenGroup]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroup]])

  def removeSpecimenGroup(cmd: RemoveSpecimenGroupCmd): Future[DomainValidation[SpecimenGroup]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroup]])

  def addCollectionEventType(cmd: AddCollectionEventTypeCmd): Future[DomainValidation[CollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventType]])

  def updateCollectionEventType(cmd: UpdateCollectionEventTypeCmd): Future[DomainValidation[CollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventType]])

  def removeCollectionEventType(cmd: RemoveCollectionEventTypeCmd): Future[DomainValidation[CollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventType]])

  def addSpecimenGroupToCollectionEventType(cmd: AddSpecimenGroupToCollectionEventTypeCmd): Future[DomainValidation[CollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventType]])

  def removeSpecimenGroupFromCollectionEventType(cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd): Future[DomainValidation[CollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventType]])

  def addAnnotationToCollectionEventType(cmd: AddAnnotationToCollectionEventTypeCmd): Future[DomainValidation[CollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventType]])

  def removeAnnotationFromCollectionEventType(cmd: RemoveAnnotationFromCollectionEventTypeCmd): Future[DomainValidation[CollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventType]])

}

// -------------------------------------------------------------------------------------------------------------
//  InvoiceProcessor is single writer to studiesRef, so we can have reads and writes in separate transactions
// -------------------------------------------------------------------------------------------------------------
class StudyProcessor(
  studyRepository: Repository[StudyId, Study],
  specimenGroupRepository: Repository[SpecimenGroupId, SpecimenGroup],
  collectionEventTypeRepository: Repository[CollectionEventTypeId, CollectionEventType])
  extends Processor { this: Emitter =>

  val studyDomainService = new StudyDomainService(studyRepository, specimenGroupRepository)
  val specimenGroupDomainService = new SpecimenGroupDomainService(studyRepository, specimenGroupRepository)
  val collectionEventTypeDomainService = new CollectionEventTypeDomainService(
    studyRepository, collectionEventTypeRepository, specimenGroupRepository)

  def receive = {
    case cmd: AddStudyCmd =>
      processUpdate(studyRepository)(studyDomainService.addStudy(cmd)) {
        study =>
          emitter("listeners") sendEvent StudyAddedEvent(study.id, study.name, study.description)
      }

    case cmd: UpdateStudyCmd =>
      processUpdate(studyRepository)(studyDomainService.updateStudy(cmd)) {
        study =>
          emitter("listeners") sendEvent StudyUpdatedEvent(study.id, study.name, study.description)
      }

    case cmd: EnableStudyCmd =>
      processUpdate(studyRepository)(studyDomainService.enableStudy(cmd)) {
        study =>
          emitter("listeners") sendEvent StudyEnabledEvent(study.id)
      }

    case cmd: DisableStudyCmd =>
      processUpdate(studyRepository)(studyDomainService.disableStudy(cmd)) { study =>
        emitter("listeners") sendEvent StudyDisabledEvent(study.id)
      }

    case cmd: AddSpecimenGroupCmd =>
      processUpdate(specimenGroupRepository)(specimenGroupDomainService.addSpecimenGroup(cmd)) {
        sg =>
          emitter("listeners") sendEvent StudySpecimenGroupAddedEvent(sg.studyId, sg.id,
            sg.name, sg.description, sg.units, sg.anatomicalSourceType, sg.preservationType,
            sg.preservationTemperatureType, sg.specimenType)
      }

    case cmd: UpdateSpecimenGroupCmd =>
      processUpdate(specimenGroupRepository)(specimenGroupDomainService.updateSpecimenGroup(cmd)) {
        sg =>
          emitter("listeners") sendEvent StudySpecimenGroupUpdatedEvent(sg.studyId,
            sg.id, sg.name, sg.description, sg.units, sg.anatomicalSourceType, sg.preservationType,
            sg.preservationTemperatureType, sg.specimenType)
      }

    case cmd: RemoveSpecimenGroupCmd =>
      processRemove(specimenGroupRepository)(specimenGroupDomainService.removeSpecimenGroup(cmd)) {
        sg =>
          emitter("listeners") sendEvent StudySpecimenGroupRemovedEvent(sg.studyId, sg.id)
      }

    case cmd: AddCollectionEventTypeCmd =>
      processUpdate(collectionEventTypeRepository)(collectionEventTypeDomainService.process(cmd)) {
        cet =>
          emitter("listeners") sendEvent CollectionEventTypeAddedEvent(
            cmd.studyId, cet.name, cet.description, cet.recurring)
      }

    case cmd: UpdateCollectionEventTypeCmd =>
      processUpdate(collectionEventTypeRepository)(collectionEventTypeDomainService.updateCollectionEventType(cmd)) { cet =>
        emitter("listeners") sendEvent CollectionEventTypeUpdatedEvent(
          cmd.studyId, cmd.collectionEventTypeId, cet.name, cet.description, cet.recurring)
      }

    case cmd: RemoveCollectionEventTypeCmd =>
      processRemove(collectionEventTypeRepository)(collectionEventTypeDomainService.removeCollectionEventType(cmd)) { cet =>
        emitter("listeners") sendEvent CollectionEventTypeRemovedEvent(
          cmd.studyId, cmd.collectionEventTypeId)
      }

    case cmd: AddSpecimenGroupToCollectionEventTypeCmd =>
      processUpdate(collectionEventTypeRepository)(collectionEventTypeDomainService.addSpecimenGroupToCollectionEventType(cmd)) { cet =>
        emitter("listeners") sendEvent SpecimenGroupAddedToCollectionEventTypeEvent(
          cmd.studyId, cmd.collectionEventTypeId, cmd.specimenGroupId)
      }

    case cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd =>
      processRemove(collectionEventTypeRepository)(collectionEventTypeDomainService.removeSpecimenGroupFromCollectionEventType(cmd)) { cet =>
        emitter("listeners") sendEvent SpecimenGroupRemovedFromCollectionEventTypeEvent(
          cmd.studyId, cmd.collectionEventTypeId, cmd.specimenGroupId)
      }

    case cmd: AddAnnotationToCollectionEventTypeCmd =>
      processUpdate(collectionEventTypeRepository)(collectionEventTypeDomainService.addAnnotationToCollectionEventType(cmd)) { cet =>
        emitter("listeners") sendEvent AnnotationAddedToCollectionEventTypeEvent(
          cmd.studyId, cmd.collectionEventTypeId, cmd.collectionEventAnnotationTypeId)
      }

    case cmd: RemoveAnnotationFromCollectionEventTypeCmd =>
      processRemove(collectionEventTypeRepository)(collectionEventTypeDomainService.removeAnnotationFromCollectionEventType(cmd)) { cet =>
        emitter("listeners") sendEvent AnnotationRemovedFromCollectionEventTypeEvent(
          cmd.studyId, cmd.collectionEventTypeId, cmd.collectionEventAnnotationTypeId)
      }
  }
}
