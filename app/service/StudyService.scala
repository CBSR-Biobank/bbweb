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
  specimenGroupRepository: Repository[SpecimenGroupId, SpecimenGroup],
  studyProcessor: ActorRef)(implicit system: ActorSystem)
  extends ApplicationService {
  import system.dispatcher

  def addStudy(cmd: AddStudyCmd): Future[DomainValidation[DisabledStudy]] =
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
}

// -------------------------------------------------------------------------------------------------------------
//  InvoiceProcessor is single writer to studiesRef, so we can have reads and writes in separate transactions
// -------------------------------------------------------------------------------------------------------------
class StudyProcessor(
  studyRepository: Repository[StudyId, Study],
  specimenGroupRepository: Repository[SpecimenGroupId, SpecimenGroup])
  extends Processor { this: Emitter =>

  def receive = {
    case cmd: AddStudyCmd =>
      processUpdate(addStudy(cmd), studyRepository) { study =>
        emitter("listeners") sendEvent StudyAddedEvent(study.id, study.name, study.description)
      }

    case cmd: EnableStudyCmd =>
      processUpdate(enableStudy(cmd), studyRepository) { study =>
        emitter("listeners") sendEvent StudyEnabledEvent(study.id)
      }

    case cmd: DisableStudyCmd =>
      processUpdate(disableStudy(cmd), studyRepository) { study =>
        emitter("listeners") sendEvent StudyDisabledEvent(study.id)
      }

    case cmd: AddSpecimenGroupCmd =>
      processUpdate(addSpecimenGroup(cmd), specimenGroupRepository) { sg =>
        emitter("listeners") sendEvent StudySpecimenGroupAddedEvent(sg.studyId, sg.id,
          cmd.name, cmd.description, cmd.units, cmd.anatomicalSourceId,
          cmd.preservationId, cmd.specimenTypeId)
      }

    case cmd: UpdateSpecimenGroupCmd =>
      processUpdate(updateSpecimenGroup(cmd), specimenGroupRepository) { sg =>
        emitter("listeners") sendEvent StudySpecimenGroupUpdatedEvent(sg.studyId,
          sg.id, cmd.name, cmd.description, cmd.units,
          cmd.anatomicalSourceId, cmd.preservationId, cmd.specimenTypeId)
      }

    case cmd: RemoveSpecimenGroupCmd =>
      processRemove(removeSpecimenGroup(cmd), specimenGroupRepository) { sg =>
        emitter("listeners") sendEvent StudySpecimenGroupRemovedEvent(sg.studyId, sg.id)
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
      val specimenGroupCount = specimenGroupRepository.getValues.filter(
        sg => sg.studyId.equals(studyId)).size
      study.enable(specimenGroupCount, 0)
    }
  }

  def disableStudy(cmd: DisableStudyCmd): DomainValidation[DisabledStudy] = {
    val studyId = new StudyId(cmd.id)
    updateEnabledStudy(studyId, cmd.expectedVersion) { study =>
      study.disable
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
          val studySpecimenGroups = specimenGroupRepository.getMap.filter(
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

  def removeSpecimenGroup(cmd: RemoveSpecimenGroupCmd): DomainValidation[SpecimenGroup] = {
    val studyId = new StudyId(cmd.studyId)
    studyRepository.getByKey(studyId) match {
      case None => StudyProcessor.noSuchStudy(studyId).fail
      case Some(study) => study match {
        case study: EnabledStudy => StudyProcessor.notDisabledError(study.name).fail
        case study: DisabledStudy =>
          val specimenGroupId = new SpecimenGroupId(cmd.specimenGroupId)
          specimenGroupRepository.getByKey(specimenGroupId) match {
            case None => StudyProcessor.noSuchStudy(studyId).fail
            case Some(sg) => sg.success
          }
      }
    }
  }

  def updateSpecimenGroup(studyId: StudyId, specimenGroupId: SpecimenGroupId,
    expectedVersion: Option[Long])(f: SpecimenGroup => DomainValidation[SpecimenGroup]): DomainValidation[SpecimenGroup] = {
    studyRepository.getByKey(studyId) match {
      case None => StudyProcessor.noSuchStudy(studyId).fail
      case Some(sg) =>
        updateEntity(specimenGroupRepository.getByKey(specimenGroupId), specimenGroupId,
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

  def updateEnabledStudy[T <: Study](id: StudyId,
    expectedVersion: Option[Long])(f: EnabledStudy => DomainValidation[T]): DomainValidation[T] =
    updateStudy(id, expectedVersion) { study =>
      study match {
        case study: EnabledStudy => f(study)
        case study: Study => StudyProcessor.notEnabledError(study.name).fail
      }
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
