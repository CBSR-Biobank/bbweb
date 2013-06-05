package domain.study

import domain._
import domain.study._
import service.Repository
import infrastructure.commands._

import scalaz._
import Scalaz._

class CollectionEventTypeDomainService(
  studyRepository: Repository[StudyId, Study],
  collectionEventTypeRepository: Repository[CollectionEventTypeId, CollectionEventType],
  specimenGroupRepository: Repository[SpecimenGroupId, SpecimenGroup]) {

  def process = PartialFunction[Any, DomainValidation[CollectionEventType]] {
    case cmd: AddCollectionEventTypeCmd =>
      validateStudy(cmd.studyId) { study => addCollectionEventType(study, cmd) }
    case cmd: UpdateCollectionEventTypeCmd =>
      validateStudy(cmd.studyId)(updateCollectionEventType(cmd))
    case cmd: RemoveCollectionEventTypeCmd =>
      validateStudy(cmd.studyId)(removeCollectionEventType(cmd))
    case cmd: AddSpecimenGroupToCollectionEventTypeCmd =>
      validateStudy(cmd.studyId)(addSpecimenGroupToCollectionEventType(cmd))
    case cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd =>
      validateStudy(cmd.studyId)(removeSpecimenGroupFromCollectionEventType(cmd))
    case cmd: AddAnnotationToCollectionEventTypeCmd =>
      validateStudy(cmd.studyId)(addAnnotationToCollectionEventType(cmd))
    case cmd: RemoveAnnotationFromCollectionEventTypeCmd =>
      validateStudy(cmd.studyId)(removeAnnotationFromCollectionEventType(cmd))
  }

  private def validateStudy(studyIdAsString: String)(validation: DomainValidation[DisabledStudy]): DomainValidation[CollectionEventType] = {
    val studyId = new StudyId(studyIdAsString)
    studyRepository.getByKey(studyId) match {
      case None => StudyDomainService.noSuchStudy(studyId).fail
      case Some(study) => study match {
        case study: EnabledStudy => StudyDomainService.notDisabledError(study.name).fail
        case study: DisabledStudy => (validation)
      }
    }
  }

  def addCollectionEventType(study: DisabledStudy, cmd: AddCollectionEventTypeCmd): DomainValidation[CollectionEventType] = {
    val collectionEventTypes = collectionEventTypeRepository.getMap.filter(
      cet => cet._2.studyId.equals(study.id))
    study.addCollectionEventType(collectionEventTypes, cmd.name, cmd.description, cmd.recurring)
  }

  def updateCollectionEventType(cmd: UpdateCollectionEventTypeCmd): DomainValidation[CollectionEventType] = {
    val studyId = new StudyId(cmd.studyId)
    studyRepository.getByKey(studyId) match {
      case None => StudyDomainService.noSuchStudy(studyId).fail
      case Some(study) => study match {
        case study: EnabledStudy => StudyDomainService.notDisabledError(study.name).fail
        case study: DisabledStudy =>
          val collectionEventTypeId = new CollectionEventTypeId(cmd.collectionEventTypeId)
          Entity.update(collectionEventTypeRepository.getByKey(collectionEventTypeId),
            collectionEventTypeId, cmd.expectedVersion) { sg =>
              CollectionEventType(collectionEventTypeId, studyId, sg.version + 1,
                cmd.name, cmd.description, cmd.recurring).success
            }
      }
    }
  }

  def removeCollectionEventType(cmd: RemoveCollectionEventTypeCmd): DomainValidation[CollectionEventType] = {
    val studyId = new StudyId(cmd.studyId)
    studyRepository.getByKey(studyId) match {
      case None => StudyDomainService.noSuchStudy(studyId).fail
      case Some(study) => study match {
        case study: EnabledStudy => StudyDomainService.notDisabledError(study.name).fail
        case study: DisabledStudy =>
          val collectionEventTypeId = new CollectionEventTypeId(cmd.collectionEventTypeId)
          collectionEventTypeRepository.getByKey(collectionEventTypeId) match {
            case None => StudyDomainService.noSuchStudy(studyId).fail
            case Some(cet) => cet.success
          }
      }
    }
  }

  def addSpecimenGroupToCollectionEventType(cmd: AddSpecimenGroupToCollectionEventTypeCmd): DomainValidation[CollectionEventType] = {
    ???
  }

  def removeSpecimenGroupFromCollectionEventType(cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd): DomainValidation[CollectionEventType] = {
    ???
  }

  def addAnnotationToCollectionEventType(cmd: AddAnnotationToCollectionEventTypeCmd): DomainValidation[CollectionEventType] = {
    ???
  }

  def removeAnnotationFromCollectionEventType(cmd: RemoveAnnotationFromCollectionEventTypeCmd): DomainValidation[CollectionEventType] = {
    ???
  }

}