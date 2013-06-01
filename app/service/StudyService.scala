package service

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import org.eligosource.eventsourced.core._
import scalaz._
import Scalaz._
import scala.language.postfixOps
import service.events.StudyAddedEvent
import service.events.StudySpecimenGroupUpdatedEvent
import service.events.StudySpecimenGroupAddedEvent
import domain.study.StudyId
import domain.study._
import domain.study.SpecimenGroupId
import domain.study.SpecimenGroup
import domain.Entity
import service.commands.AddStudyCmd
import domain.DomainError
import service.commands.UpdateSpecimenGroupCmd
import domain.DomainValidation
import service.commands.AddSpecimenGroupCmd

class StudyService(
  studiesRef: Ref[Map[StudyId, Study]],
  specimenGroupsRef: Ref[Map[SpecimenGroupId, SpecimenGroup]],
  studyProcessor: ActorRef)(implicit system: ActorSystem) {
  import system.dispatcher

  //
  // Consistent reads
  //

  def getStudiesMap = studiesRef.single.get
  def getSpecimenGroupsMap = specimenGroupsRef.single.get

  //
  // Updates
  //

  implicit val timeout = Timeout(5 seconds)

  def addStudy(cmd: AddStudyCmd): Future[DomainValidation[DisabledStudy]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[DisabledStudy]])

  def addSpecimenGroup(cmd: AddSpecimenGroupCmd): Future[DomainValidation[SpecimenGroup]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroup]])

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd): Future[DomainValidation[SpecimenGroup]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[SpecimenGroup]])
}

// -------------------------------------------------------------------------------------------------------------
//  InvoiceProcessor is single writer to studiesRef, so we can have reads and writes in separate transactions
// -------------------------------------------------------------------------------------------------------------
class StudyProcessor(
  studiesRef: Ref[Map[StudyId, Study]],
  specimenGroupsRef: Ref[Map[SpecimenGroupId, SpecimenGroup]]) extends Actor { this: Emitter =>

  def receive = {
    case addStudyCmd: AddStudyCmd =>
      process(addStudy(addStudyCmd)) { study =>
        emitter("listeners") sendEvent StudyAddedEvent(study.id, study.name, study.description)
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

  def process[T <: Entity[_]](validation: DomainValidation[T])(onSuccess: T => Unit) = {
    validation.foreach { entity =>
      updateRepository(entity)
      onSuccess(entity)
    }
    sender ! validation
  }

  def addStudy(cmd: AddStudyCmd): DomainValidation[DisabledStudy] = {
    readStudies.find(s => s._2.name.equals(cmd.name)) match {
      case Some(study) => DomainError("study with name already exists: %s" format cmd.name).fail
      case None => Study.add(Study.nextIdentity, cmd.name, cmd.description)
    }
  }

  def addSpecimenGroup(cmd: AddSpecimenGroupCmd): DomainValidation[SpecimenGroup] = {
    val studyId = new StudyId(cmd.studyId)
    readStudies.get(studyId) match {
      case None => StudyProcessor.noSuchStudy(studyId).fail
      case Some(study) => study match {
        case study: EnabledStudy => StudyProcessor.notDisabledError(study.name).fail
        case study: DisabledStudy =>
          specimenGroupsRef.single.get.find(
            sg => sg._2.name.equals(cmd.name) && sg._2.studyId.equals(study.id)) match {
              case Some(sg) => DomainError("specimen group with name already exists: %s" format cmd.name).fail
              case None => SpecimenGroup.add(cmd)
            }
      }
    }
  }

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd): DomainValidation[SpecimenGroup] = {
    val studyId = new StudyId(cmd.studyId)
    readStudies.get(studyId) match {
      case None => StudyProcessor.noSuchStudy(studyId).fail
      case Some(study) => study match {
        case study: EnabledStudy => StudyProcessor.notDisabledError(study.name).fail
        case study: DisabledStudy =>
          val specimenGroupId = new SpecimenGroupId(cmd.specimenGroupId)
          specimenGroupsRef.single.get.find(
            sg => sg._2.name.equals(cmd.name) && sg._2.studyId.equals(study.id)) match {
              case None => DomainError("specimen group with name already exists for this study: %s" format cmd.name).fail
              case Some(sg) =>
                new SpecimenGroup(specimenGroupId, studyId, cmd.expectedVersion, cmd.name, cmd.description,
                  cmd.units, cmd.anatomicalSourceId, cmd.preservationId, cmd.specimenTypeId).success
            }
      }
    }
  }

  def updateStudy[B <: Study](studyId: StudyId, expectedVersion: Option[Long])(f: Study => DomainValidation[B]): DomainValidation[B] =
    readStudies.get(studyId) match {
      case None => StudyProcessor.noSuchStudy(studyId).fail
      case Some(study) => for {
        current <- study.requireVersion(expectedVersion)
        updated <- f(study)
      } yield updated
    }

  private def updateRepository[T <: Entity[_]](entity: T) = entity match {
    case study: Study => studiesRef.single.transform(studies => studies + (study.id -> study))
    case sg: SpecimenGroup => specimenGroupsRef.single.transform(groups => groups + (sg.id -> sg))
    case _ => throw new Error("update on invalid entity")
  }

  private def readStudies =
    studiesRef.single.get
}

object StudyProcessor {
  private[service] def noSuchStudy(studyId: StudyId) =
    DomainError("no study with id: %s" format studyId)

  private[service] def notDisabledError(name: String) =
    DomainError("study is not disabled: %s" format name)

  private[service] def notEnabledError(name: String) =
    DomainError("study is not enabled: %s" format name)
}