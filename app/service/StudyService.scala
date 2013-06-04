package service

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import org.eligosource.eventsourced.core._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import scala.language.postfixOps

import domain._
import domain.study._

import service.commands._
import service.events._

import scalaz._
import Scalaz._

class StudyService(
  studyRepository: Repository[StudyId, Study],
  specimenGroupsRef: Ref[Map[SpecimenGroupId, SpecimenGroup]],
  studyProcessor: ActorRef)(implicit system: ActorSystem) {
  import system.dispatcher

  //
  // Consistent reads
  //
  def getSpecimenGroupsMap = specimenGroupsRef.single.get

  //
  // Updates
  //

  implicit val timeout = Timeout(5 seconds)

  def addStudy(cmd: AddStudyCmd): Future[DomainValidation[DisabledStudy]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[DisabledStudy]])

  def enableStudy(cmd: EnableStudyCmd): Future[DomainValidation[EnabledStudy]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[EnabledStudy]])

  def addSpecimenGroup(cmd: AddSpecimenGroupCmd): Future[DomainValidation[SpecimenGroup]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroup]])

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd): Future[DomainValidation[SpecimenGroup]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroup]])
}

// -------------------------------------------------------------------------------------------------------------
//  InvoiceProcessor is single writer to studiesRef, so we can have reads and writes in separate transactions
// -------------------------------------------------------------------------------------------------------------
class StudyProcessor(
  studyRepository: Repository[StudyId, Study],
  specimenGroupsRef: Ref[Map[SpecimenGroupId, SpecimenGroup]]) extends Processor { this: Emitter =>

  def receive = {
    case cmd: AddStudyCmd =>
      process(addStudy(cmd)) { study =>
        emitter("listeners") sendEvent StudyAddedEvent(study.id, study.name, study.description)
      }
    case cmd: EnableStudyCmd =>
      process(enableStudy(cmd)) { study =>
        emitter("listeners") sendEvent StudyEnabledEvent(study.id)
      }
    case addSpcgCmd: AddSpecimenGroupCmd =>
      process(addSpecimenGroup(addSpcgCmd)) { sg =>
        emitter("listeners") sendEvent StudySpecimenGroupAddedEvent(sg.studyId, sg.id,
          addSpcgCmd.name, addSpcgCmd.description, addSpcgCmd.units, addSpcgCmd.anatomicalSourceId,
          addSpcgCmd.preservationId, addSpcgCmd.specimenTypeId)
      }
    case updateSpcgCmd: UpdateSpecimenGroupCmd =>
      process(updateSpecimenGroup(updateSpcgCmd)) { sg =>
        emitter("listeners") sendEvent StudySpecimenGroupUpdatedEvent(sg.studyId,
          sg.id, updateSpcgCmd.name, updateSpcgCmd.description, updateSpcgCmd.units,
          updateSpcgCmd.anatomicalSourceId, updateSpcgCmd.preservationId, updateSpcgCmd.specimenTypeId)
      }
  }

  def addStudy(cmd: AddStudyCmd): DomainValidation[DisabledStudy] = {
    studyRepository.getValues.find(s => s.name.equals(cmd.name)) match {
      case Some(study) => DomainError("study with name already exists: %s" format cmd.name).fail
      case None => Study.add(cmd.name, cmd.description)
    }
  }

  def enableStudy(cmd: EnableStudyCmd): DomainValidation[EnabledStudy] = {
    val studyId = new StudyId(cmd.id)
    updateDisabledStudy(studyId, cmd.expectedVersion) { study =>
      val specimenGroupCount = specimenGroupsRef.single.get.filter(
        sg => sg._2.studyId.equals(studyId)).size
      study.enable(specimenGroupCount, 0)
    }
  }

  def addSpecimenGroup(cmd: AddSpecimenGroupCmd): DomainValidation[SpecimenGroup] = {
    val studyId = new StudyId(cmd.studyId)
    studyRepository.getByKey(studyId) match {
      case None => StudyProcessor.noSuchStudy(studyId).fail
      case Some(study) => study match {
        case study: EnabledStudy => StudyProcessor.notDisabledError(study.name).fail
        case study: DisabledStudy =>
          // FIXME: lookup other IDs and verify them
          val studySpecimenGroups = specimenGroupsRef.single.get.filter(
            sg => sg._2.studyId.equals(study.id))
          study.addSpecimenGroup(studySpecimenGroups, studyId, cmd.name, cmd.description, cmd.units,
            cmd.anatomicalSourceId, cmd.preservationId, cmd.specimenTypeId)
      }
    }
  }

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd): DomainValidation[SpecimenGroup] = {
    val studyId = new StudyId(cmd.studyId)
    val specimenGroupId = new SpecimenGroupId(cmd.specimenGroupId)
    updateSpecimenGroup(studyId, specimenGroupId, cmd.expectedVersion) { sg =>
      SpecimenGroup(specimenGroupId, studyId, sg.version + 1, cmd.name, cmd.description, cmd.units,
        cmd.anatomicalSourceId, cmd.preservationId, cmd.specimenTypeId).success
    }
  }

  def updateSpecimenGroup(studyId: StudyId, specimenGroupId: SpecimenGroupId,
    expectedVersion: Option[Long])(f: SpecimenGroup => DomainValidation[SpecimenGroup]): DomainValidation[SpecimenGroup] = {
    studyRepository.getByKey(studyId) match {
      case None => StudyProcessor.noSuchStudy(studyId).fail
      case Some(study) =>
        updateEntity(specimenGroupsRef.single.get.get(specimenGroupId), specimenGroupId,
          expectedVersion)(f)
    }
  }

  def updateStudy[T <: Study](id: StudyId,
    expectedVersion: Option[Long])(f: Study => DomainValidation[T]): DomainValidation[T] =
    updateEntity(studyRepository.getByKey(id), id, expectedVersion)(f)

  def updateDisabledStudy[T <: Study](id: StudyId,
    expectedVersion: Option[Long])(f: DisabledStudy => DomainValidation[T]): DomainValidation[T] =
    updateStudy(id, expectedVersion) { study =>
      study match {
        case study: DisabledStudy => f(study)
        case study: Study => StudyProcessor.notDisabledError(study.name).fail
      }
    }

  override def updateRepository[T <: ConcurrencySafeEntity[_]](entity: T) = entity match {
    case study: Study => studyRepository.updateMap(study)
    case sg: SpecimenGroup => specimenGroupsRef.single.transform(groups => groups + (sg.id -> sg))
    case _ => throw new Error("update on invalid entity")
  }
}

object StudyProcessor {
  private[service] def noSuchStudy(studyId: StudyId) =
    DomainError("no study with id: %s" format studyId)

  private[service] def notDisabledError(name: String) =
    DomainError("study is not disabled: %s" format name)

  private[service] def notEnabledError(name: String) =
    DomainError("study is not enabled: %s" format name)
}
