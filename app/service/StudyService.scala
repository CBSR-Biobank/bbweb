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
}

// -------------------------------------------------------------------------------------------------------------
//  InvoiceProcessor is single writer to studiesRef, so we can have reads and writes in separate transactions
// -------------------------------------------------------------------------------------------------------------
class StudyProcessor(
  studyRepository: Repository[StudyId, Study],
  specimenGroupRepository: Repository[SpecimenGroupId, SpecimenGroup])
  extends Processor { this: Emitter =>

  val studyDomainService = new StudyDomainService(studyRepository, specimenGroupRepository)
  val specimenGroupDomainService = new SpecimenGroupDomainService(studyRepository, specimenGroupRepository)

  def receive = {
    case cmd: AddStudyCmd =>
      processUpdate(studyDomainService.addStudy(cmd.name, cmd.description), studyRepository) {
        study =>
          emitter("listeners") sendEvent StudyAddedEvent(study.id, study.name, study.description)
      }

    case cmd: UpdateStudyCmd =>
      processUpdate(studyDomainService.updateStudy(new StudyId(cmd.studyId), cmd.expectedVersion,
        cmd.name, cmd.description), studyRepository) {
        study =>
          emitter("listeners") sendEvent StudyUpdatedEvent(study.id, study.name, study.description)
      }

    case cmd: EnableStudyCmd =>
      processUpdate(studyDomainService.enableStudy(new StudyId(cmd.studyId), cmd.expectedVersion), studyRepository) {
        study =>
          emitter("listeners") sendEvent StudyEnabledEvent(study.id)
      }

    case cmd: DisableStudyCmd =>
      processUpdate(studyDomainService.disableStudy(new StudyId(cmd.studyId), cmd.expectedVersion), studyRepository) { study =>
        emitter("listeners") sendEvent StudyDisabledEvent(study.id)
      }

    case cmd: AddSpecimenGroupCmd =>
      processUpdate(specimenGroupDomainService.addSpecimenGroup(
        new StudyId(cmd.studyId), cmd.name, cmd.description, cmd.units, cmd.anatomicalSourceId,
        cmd.preservationId, cmd.specimenTypeId), specimenGroupRepository) { sg =>
        emitter("listeners") sendEvent StudySpecimenGroupAddedEvent(sg.studyId, sg.id,
          cmd.name, cmd.description, cmd.units, cmd.anatomicalSourceId,
          cmd.preservationId, cmd.specimenTypeId)
      }

    case cmd: UpdateSpecimenGroupCmd =>
      processUpdate(specimenGroupDomainService.updateSpecimenGroup(
        new StudyId(cmd.studyId), new SpecimenGroupId(cmd.specimenGroupId), cmd.expectedVersion,
        cmd.name, cmd.description, cmd.units, cmd.anatomicalSourceId, cmd.preservationId,
        cmd.specimenTypeId), specimenGroupRepository) { sg =>
        emitter("listeners") sendEvent StudySpecimenGroupUpdatedEvent(sg.studyId,
          sg.id, cmd.name, cmd.description, cmd.units,
          cmd.anatomicalSourceId, cmd.preservationId, cmd.specimenTypeId)
      }

    case cmd: RemoveSpecimenGroupCmd =>
      processRemove(specimenGroupDomainService.removeSpecimenGroup(
        new StudyId(cmd.studyId), new SpecimenGroupId(cmd.specimenGroupId), cmd.expectedVersion),
        specimenGroupRepository) { sg =>
          emitter("listeners") sendEvent StudySpecimenGroupRemovedEvent(sg.studyId, sg.id)
        }
  }
}
