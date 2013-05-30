package service

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import domain._
import domain.study._

import org.eligosource.eventsourced.core._

import scalaz._
import Scalaz._

import scala.language.postfixOps

class StudyService(studiesRef: Ref[Map[StudyId, Study]],
  studyProcessor: ActorRef)(implicit system: ActorSystem) {
  import system.dispatcher

  //
  // Consistent reads
  //

  def getStudiesMap = studiesRef.single.get

  //
  // Updates
  //

  implicit val timeout = Timeout(5 seconds)

  def addStudy(name: String, description: String): Future[DomainValidation[DisabledStudy]] =
    studyProcessor ? Message(AddStudy(name, description)) map (_.asInstanceOf[DomainValidation[DisabledStudy]])

  def addSpecimenGroup(cmd: AddSpecimenGroupCmd): Future[DomainValidation[DisabledStudy]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[DisabledStudy]])

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd): Future[DomainValidation[DisabledStudy]] =
    studyProcessor ? Message(cmd) map (_.asInstanceOf[DomainValidation[DisabledStudy]])
}

// -------------------------------------------------------------------------------------------------------------
//  InvoiceProcessor is single writer to studiesRef, so we can have reads and writes in separate transactions
// -------------------------------------------------------------------------------------------------------------
class StudyProcessor(studiesRef: Ref[Map[StudyId, Study]]) extends Actor { this: Emitter =>

  def receive = {
    case AddStudy(name, description) =>
      process(addStudy(name, description)) { study =>
        emitter("listeners") sendEvent StudyAddedEvent(study.id, study.name, study.description)
      }
    case addSpcgCmd: AddSpecimenGroupCmd =>
      val specimenGroupId = SpecimenGroup.nextIdentity
      process(addSpecimenGroup(addSpcgCmd, specimenGroupId)) { study =>
        // FIXME: need the correct SpecimenGroupId here
        emitter("listeners") sendEvent StudySpecimenGroupAddedEvent(study.id, specimenGroupId,
          addSpcgCmd.name, addSpcgCmd.description, addSpcgCmd.units, addSpcgCmd.amatomicalSourceId,
          addSpcgCmd.preservationId, addSpcgCmd.specimenTypeId)
      }
    case updateSpcgCmd: UpdateSpecimenGroupCmd =>
      val specimenGroupId = new SpecimenGroupId(updateSpcgCmd.specimenGroupId)
      process(updateSpecimenGroup(updateSpcgCmd)) { study =>
        emitter("listeners") sendEvent StudySpecimenGroupUpdatedEvent(study.id,
          specimenGroupId, updateSpcgCmd.name, updateSpcgCmd.description, updateSpcgCmd.units,
          updateSpcgCmd.amatomicalSourceId, updateSpcgCmd.preservationId, updateSpcgCmd.specimenTypeId)
      }
  }

  def process(validation: DomainValidation[Study])(onSuccess: Study => Unit) = {
    validation.foreach { study =>
      updateStudies(study)
      onSuccess(study)
    }
    sender ! validation
  }

  def addStudy(name: String, description: String): DomainValidation[DisabledStudy] = {
    readStudies.find(s => s._2.name.equals(name)) match {
      case Some(study) => DomainError("study with name already exists: %s" format name).fail
      case None => Study.add(Study.nextIdentity, name, description)
    }
  }

  def addSpecimenGroup(cmd: AddSpecimenGroupCmd, specimenGroupId: SpecimenGroupId): DomainValidation[DisabledStudy] = {
    val studyId = new StudyId(cmd.studyId)
    updateStudy(studyId, cmd.expectedVersion) { study =>
      study match {
        case study: DisabledStudy =>
          val specimenGroup = new SpecimenGroup(specimenGroupId, cmd.name, cmd.description,
            cmd.units, cmd.amatomicalSourceId, cmd.preservationId, cmd.specimenTypeId)
          study.addSpecimenGroup(specimenGroup)
        case study: EnabledStudy => StudyProcessor.notDisabledError(study.name).fail
      }
    }
  }

  def updateSpecimenGroup(cmd: UpdateSpecimenGroupCmd): DomainValidation[DisabledStudy] = {
    val studyId = new StudyId(cmd.studyId)
    updateStudy(studyId, cmd.expectedVersion) { study =>
      study match {
        case study: DisabledStudy =>
          val specimenGroupId = new SpecimenGroupId(cmd.specimenGroupId)
          val specimenGroup = new SpecimenGroup(specimenGroupId, cmd.name, cmd.description,
            cmd.units, cmd.amatomicalSourceId, cmd.preservationId, cmd.specimenTypeId)
          study.updateSpecimenGroup(specimenGroup)
        case study: EnabledStudy => StudyProcessor.notDisabledError(study.name).fail
      }
    }
  }

  def updateStudy[B <: Study](studyId: StudyId, expectedVersion: Option[Long])(f: Study => DomainValidation[B]): DomainValidation[B] =
    readStudies.get(studyId) match {
      case None => StudyProcessor.noSuchStudy(studyId).fail
      case Some(invoice) => for {
        current <- Study.requireVersion(invoice, expectedVersion)
        updated <- f(invoice)
      } yield updated
    }

  private def updateStudies(study: Study) =
    studiesRef.single.transform(studies => studies + (study.id -> study))

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