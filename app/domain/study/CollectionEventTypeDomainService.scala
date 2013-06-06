package domain.study

import domain._
import domain.study._
import service.Repository
import infrastructure.commands._
import infrastructure.events._

import org.eligosource.eventsourced.core.Emitter

import scalaz._
import Scalaz._

class CollectionEventTypeDomainService(
  studyRepository: Repository[StudyId, Study],
  collectionEventTypeRepository: Repository[CollectionEventTypeId, CollectionEventType],
  specimenGroupRepository: Repository[SpecimenGroupId, SpecimenGroup]) {

  def process = PartialFunction[Any, DomainValidation[CollectionEventType]] {
    case _@ (cmd: AddCollectionEventTypeCmd, emitter: Emitter) =>
      validateStudy(cmd.studyId, emitter) { study => addCollectionEventType(study, cmd, emitter) }
    case cmd: UpdateCollectionEventTypeCmd =>
      validateStudy(cmd.studyId) { study => updateCollectionEventType(study, cmd) }
    case cmd: RemoveCollectionEventTypeCmd =>
      validateStudy(cmd.studyId) { study => removeCollectionEventType(study, cmd) }
    case cmd: AddSpecimenGroupToCollectionEventTypeCmd =>
      validateStudy(cmd.studyId) { study => addSpecimenGroupToCollectionEventType(study, cmd) }
    case cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd =>
      validateStudy(cmd.studyId) { study => removeSpecimenGroupFromCollectionEventType(study, cmd) }
    case cmd: AddAnnotationToCollectionEventTypeCmd =>
      validateStudy(cmd.studyId) { study => addAnnotationToCollectionEventType(study, cmd) }
    case cmd: RemoveAnnotationFromCollectionEventTypeCmd =>
      validateStudy(cmd.studyId) { study => removeAnnotationFromCollectionEventType(study, cmd) }
    case _ =>
      throw new Error("invalid command received")
  }

  private def validateStudy(studyIdAsString: String, emitter: Emitter)(f: DisabledStudy => DomainValidation[CollectionEventType]) = {
    val studyId = new StudyId(studyIdAsString)
    studyRepository.getByKey(studyId) match {
      case None => StudyDomainService.noSuchStudy(studyId).fail
      case Some(study) => study match {
        case study: EnabledStudy => StudyDomainService.notDisabledError(study.name).fail
        case study: DisabledStudy => f(study)
      }
    }
  }

  private def addCollectionEventType(study: DisabledStudy,
    cmd: AddCollectionEventTypeCmd, emitter: Emitter): DomainValidation[CollectionEventType] = {
    val collectionEventTypes = collectionEventTypeRepository.getMap.filter(
      cet => cet._2.studyId.equals(study.id))
    study.addCollectionEventType(collectionEventTypes, cmd.name, cmd.description, cmd.recurring)
  }

  private def updateCollectionEventType(study: DisabledStudy, cmd: UpdateCollectionEventTypeCmd): DomainValidation[CollectionEventType] = {
    val collectionEventTypeId = new CollectionEventTypeId(cmd.collectionEventTypeId)
    Entity.update(collectionEventTypeRepository.getByKey(collectionEventTypeId),
      collectionEventTypeId, cmd.expectedVersion) { sg =>
        CollectionEventType(collectionEventTypeId, study.id, sg.version + 1,
          cmd.name, cmd.description, cmd.recurring).success
      }
  }

  private def removeCollectionEventType(study: DisabledStudy, cmd: RemoveCollectionEventTypeCmd): DomainValidation[CollectionEventType] = {
    val collectionEventTypeId = new CollectionEventTypeId(cmd.collectionEventTypeId)
    collectionEventTypeRepository.getByKey(collectionEventTypeId) match {
      case None => StudyDomainService.noSuchStudy(study.id).fail
      case Some(cet) => cet.success
    }
  }

  private def addSpecimenGroupToCollectionEventType(study: DisabledStudy, cmd: AddSpecimenGroupToCollectionEventTypeCmd): DomainValidation[CollectionEventType] = {
    ???
  }

  private def removeSpecimenGroupFromCollectionEventType(study: DisabledStudy, cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd): DomainValidation[CollectionEventType] = {
    ???
  }

  private def addAnnotationToCollectionEventType(study: DisabledStudy, cmd: AddAnnotationToCollectionEventTypeCmd): DomainValidation[CollectionEventType] = {
    ???
  }

  private def removeAnnotationFromCollectionEventType(study: DisabledStudy, cmd: RemoveAnnotationFromCollectionEventTypeCmd): DomainValidation[CollectionEventType] = {
    ???
  }

}