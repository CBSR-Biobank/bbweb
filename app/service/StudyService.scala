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

class StudyService(
  studyRepository: ReadRepository[StudyId, Study],
  specimenGroupRepository: ReadRepository[SpecimenGroupId, SpecimenGroup],
  collectionEventTypeRepository: ReadRepository[CollectionEventTypeId, CollectionEventType],
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

  def addSpecimenGroupToCollectionEventType(cmd: AddSpecimenGroupToCollectionEventTypeCmd): Future[DomainValidation[SpecimenGroupCollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroupCollectionEventType]])

  def removeSpecimenGroupFromCollectionEventType(cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd): Future[DomainValidation[SpecimenGroupCollectionEventType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroupCollectionEventType]])

  def addAnnotationToCollectionEventType(cmd: AddAnnotationTypeToCollectionEventTypeCmd): Future[DomainValidation[CollectionEventTypeAnnotationType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventTypeAnnotationType]])

  def removeAnnotationFromCollectionEventType(cmd: RemoveAnnotationTypeFromCollectionEventTypeCmd): Future[DomainValidation[CollectionEventTypeAnnotationType]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[CollectionEventTypeAnnotationType]])

}

// -------------------------------------------------------------------------------------------------------------
//  InvoiceProcessor is single writer to studiesRef, so we can have reads and writes in separate transactions
// -------------------------------------------------------------------------------------------------------------
class StudyProcessor(
  studyRepository: ReadWriteRepository[StudyId, Study],
  specimenGroupRepository: ReadWriteRepository[SpecimenGroupId, SpecimenGroup],
  collectionEventTypeRepository: ReadWriteRepository[CollectionEventTypeId, CollectionEventType],
  sg2cetRepo: ReadWriteRepository[String, SpecimenGroupCollectionEventType],
  cetAnnotationTypeRepo: ReadWriteRepository[String, CollectionEventTypeAnnotationType])
  extends Processor { this: Emitter =>

  val specimenGroupDomainService = new SpecimenGroupDomainService(
    studyRepository, specimenGroupRepository)

  val collectionEventTypeDomainService = new CollectionEventTypeDomainService(
    studyRepository, collectionEventTypeRepository, specimenGroupRepository,
    sg2cetRepo, cetAnnotationTypeRepo)

  def receive = {
    case cmd: AddStudyCmd =>
      process(addStudy(cmd, emitter("listeners")))

    case cmd: UpdateStudyCmd =>
      process(updateStudy(cmd, emitter("listeners")))

    case cmd: EnableStudyCmd =>
      process(enableStudy(cmd, emitter("listeners")))

    case cmd: DisableStudyCmd =>
      process(disableStudy(cmd, emitter("listeners")))

    case cmd: SpecimenGroupCommand =>
      process(validateStudy(cmd.studyId) { study =>
        specimenGroupDomainService.process(study, cmd, emitter("listeners"))
      })

    case cmd: CollectionEventTypeCommand =>
      process(validateStudy(cmd.studyId) { study =>
        collectionEventTypeDomainService.process(study, cmd, emitter("listeners"))
      })

    case _ =>
      throw new Error("invalid command received")
  }

  private def addStudy(cmd: AddStudyCmd, listeners: MessageEmitter): DomainValidation[DisabledStudy] = {
    studyRepository.getValues.find(s => s.name.equals(cmd.name)) match {
      case Some(study) =>
        DomainError("study with name already exists: %s" format cmd.name).fail
      case None =>
        val study = Study.add(cmd.name, cmd.description)
        study match {
          case Success(study) =>
            studyRepository.updateMap(study)
            listeners sendEvent StudyAddedEvent(study.id, study.name, study.description)
          case _ => // nothing to do in this case
        }
        study
    }
  }

  private def updateStudy(cmd: UpdateStudyCmd, listeners: MessageEmitter): DomainValidation[DisabledStudy] = {
    val studyId = new StudyId(cmd.studyId)
    Entity.update(studyRepository.getByKey(studyId), studyId, cmd.expectedVersion) { prevStudy =>
      val study = DisabledStudy(studyId, prevStudy.version + 1, cmd.name, cmd.description)
      studyRepository.updateMap(study)
      listeners sendEvent StudyUpdatedEvent(study.id, study.name, study.description)
      study.success
    }
  }

  private def enableStudy(cmd: EnableStudyCmd, listeners: MessageEmitter): DomainValidation[EnabledStudy] = {
    val studyId = new StudyId(cmd.studyId)
    Entity.update(studyRepository.getByKey(studyId), studyId, cmd.expectedVersion) { prevStudy =>
      val study = EnabledStudy(studyId, prevStudy.version + 1, prevStudy.name, prevStudy.description)
      studyRepository.updateMap(study)
      listeners sendEvent StudyEnabledEvent(study.id)
      study.success
    }
  }

  private def disableStudy(cmd: DisableStudyCmd, listeners: MessageEmitter): DomainValidation[DisabledStudy] = {
    val studyId = new StudyId(cmd.studyId)
    Entity.update(studyRepository.getByKey(studyId), studyId, cmd.expectedVersion) { prevStudy =>
      val study = DisabledStudy(studyId, prevStudy.version + 1, prevStudy.name, prevStudy.description)
      studyRepository.updateMap(study)
      listeners sendEvent StudyDisabledEvent(study.id)
      study.success
    }
  }

  private def validateStudy(studyIdAsString: String)(f: DisabledStudy => DomainValidation[_]) = {
    val studyId = new StudyId(studyIdAsString)
    studyRepository.getByKey(studyId) match {
      case None => StudyService.noSuchStudy(studyId).fail
      case Some(study) => study match {
        case study: EnabledStudy => StudyService.notDisabledError(study.name).fail
        case study: DisabledStudy => f(study)
      }
    }
  }
}

object StudyService {

  def noSuchStudy(studyId: StudyId) =
    DomainError("no study with id: %s" format studyId)

  def notDisabledError(name: String) =
    DomainError("study is not disabled: %s" format name)

  def notEnabledError(name: String) =
    DomainError("study is not enabled: %s" format name)
}
