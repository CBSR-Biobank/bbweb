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

class StudyService(studiesRef: Ref[Map[domain.Identity, Study]],
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

  def addSpecimenGroup(studyId: StudyId, expectedVersion: Option[Long], name: String,
    description: String, units: String, amatomicalSourceId: AmatomicalSourceId,
    preservationId: PreservationId, specimenTypeId: SpecimenTypeId): Future[DomainValidation[DisabledStudy]] =
    studyProcessor ? Message(AddSpecimenGroup(studyId, expectedVersion, name, description, units,
      amatomicalSourceId, preservationId, specimenTypeId)) map (_.asInstanceOf[DomainValidation[DisabledStudy]])
}

// -------------------------------------------------------------------------------------------------------------
//  InvoiceProcessor is single writer to studiesRef, so we can have reads and writes in separate transactions
// -------------------------------------------------------------------------------------------------------------
class StudyProcessor(studiesRef: Ref[Map[domain.Identity, Study]]) extends Actor { this: Emitter =>

  def receive = {
    case AddStudy(name, description) =>
      process(addStudy(name, description)) { study =>
        emitter("listeners") sendEvent StudyAdded(name, description)
      }
    case AddSpecimenGroup(studyId, expectedVersion, name, description, units, amatomicalSourceId,
      preservationId, specimenTypeId) =>
      process(addSpecimenGroup(studyId, expectedVersion, name, description, units,
        amatomicalSourceId, preservationId, specimenTypeId)) { study =>
        emitter("listeners") sendEvent StudyAdded(name, description)
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

  def addSpecimenGroup(studyId: StudyId, expectedVersion: Option[Long], name: String,
    description: String, units: String, amatomicalSourceId: AmatomicalSourceId, preservationId: PreservationId,
    specimenTypeId: SpecimenTypeId): DomainValidation[DisabledStudy] = {
    updateStudy(studyId, expectedVersion) { study =>
      study match {
        case study: DisabledStudy => study.addSpecimenGroup(name, description, units,
          amatomicalSourceId, preservationId, specimenTypeId)
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