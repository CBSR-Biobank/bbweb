package domain.service

import org.eligosource.eventsourced.core._

import domain._
import domain.study._
import infrastructure._
import infrastructure.commands._
import infrastructure.events._
import domain.study.{
  CollectionEventType,
  CollectionEventTypeId,
  DisabledStudy,
  EnabledStudy,
  SpecimenGroup,
  SpecimenGroupId,
  Study,
  StudyId
}
import scalaz._
import scalaz.Scalaz._

class CollectionEventTypeDomainService(
  studyRepository: ReadRepository[StudyId, Study],
  collectionEventTypeRepository: ReadWriteRepository[CollectionEventTypeId, CollectionEventType],
  specimenGroupRepository: ReadRepository[SpecimenGroupId, SpecimenGroup]) {

  def process = PartialFunction[Any, DomainValidation[CollectionEventType]] {
    case _@ (study: DisabledStudy, cmd: AddCollectionEventTypeCmd, listeners: MessageEmitter) =>
      addCollectionEventType(study, cmd, listeners)
    case _@ (study: DisabledStudy, cmd: UpdateCollectionEventTypeCmd, listeners: MessageEmitter) =>
      updateCollectionEventType(study, cmd, listeners)
    case _@ (study: DisabledStudy, cmd: RemoveCollectionEventTypeCmd, listeners: MessageEmitter) =>
      removeCollectionEventType(study, cmd, listeners)
    case _@ (study: DisabledStudy, cmd: AddSpecimenGroupToCollectionEventTypeCmd, listeners: MessageEmitter) =>
      addSpecimenGroupToCollectionEventType(study, cmd, listeners)
    case _@ (study: DisabledStudy, cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd, listeners: MessageEmitter) =>
      removeSpecimenGroupFromCollectionEventType(study, cmd, listeners)
    case _@ (study: DisabledStudy, cmd: AddAnnotationToCollectionEventTypeCmd, listeners: MessageEmitter) =>
      addAnnotationToCollectionEventType(study, cmd, listeners)
    case _@ (study: DisabledStudy, cmd: RemoveAnnotationFromCollectionEventTypeCmd, listeners: MessageEmitter) =>
      removeAnnotationFromCollectionEventType(study, cmd, listeners)
    case _ =>
      throw new Error("invalid command received")
  }

  private def addCollectionEventType(study: DisabledStudy,
    cmd: AddCollectionEventTypeCmd,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {
    val collectionEventTypes = collectionEventTypeRepository.getMap.filter(
      cet => cet._2.studyId.equals(study.id))
    val v = study.addCollectionEventType(collectionEventTypes, cmd)
    v match {
      case Success(cet) =>
        collectionEventTypeRepository.updateMap(cet)
        listeners sendEvent CollectionEventTypeAddedEvent(
          cmd.studyId, cet.name, cet.description, cet.recurring)
      case _ => // nothing to do in this case
    }
    v
  }

  private def updateCollectionEventType(study: DisabledStudy, cmd: UpdateCollectionEventTypeCmd,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {
    val collectionEventTypeId = new CollectionEventTypeId(cmd.collectionEventTypeId)
    Entity.update(collectionEventTypeRepository.getByKey(collectionEventTypeId),
      collectionEventTypeId, cmd.expectedVersion) { prevCet =>
        val cet = CollectionEventType(collectionEventTypeId, study.id, prevCet.version + 1,
          cmd.name, cmd.description, cmd.recurring)
        collectionEventTypeRepository.updateMap(cet)
        listeners sendEvent CollectionEventTypeUpdatedEvent(
          cmd.studyId, cmd.collectionEventTypeId, cet.name, cet.description, cet.recurring)
        cet.success
      }
  }

  private def removeCollectionEventType(study: DisabledStudy, cmd: RemoveCollectionEventTypeCmd,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {
    val collectionEventTypeId = new CollectionEventTypeId(cmd.collectionEventTypeId)
    collectionEventTypeRepository.getByKey(collectionEventTypeId) match {
      case None =>
        DomainError("collection event type does not exist: %s" format cmd.collectionEventTypeId).fail
      case Some(cet) =>
        collectionEventTypeRepository.remove(cet)
        listeners sendEvent CollectionEventTypeRemovedEvent(cmd.studyId, cmd.collectionEventTypeId)
        cet.success
    }
  }

  private def addSpecimenGroupToCollectionEventType(study: DisabledStudy,
    cmd: AddSpecimenGroupToCollectionEventTypeCmd,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {
    listeners sendEvent SpecimenGroupAddedToCollectionEventTypeEvent(
      cmd.studyId, cmd.collectionEventTypeId, cmd.specimenGroupId)
    ???
  }

  private def removeSpecimenGroupFromCollectionEventType(study: DisabledStudy,
    cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {
    listeners sendEvent SpecimenGroupRemovedFromCollectionEventTypeEvent(
      cmd.studyId, cmd.collectionEventTypeId, cmd.specimenGroupId)
    ???
  }

  private def addAnnotationToCollectionEventType(study: DisabledStudy,
    cmd: AddAnnotationToCollectionEventTypeCmd,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {
    listeners sendEvent AnnotationAddedToCollectionEventTypeEvent(
      cmd.studyId, cmd.collectionEventTypeId, cmd.collectionEventAnnotationTypeId)
    ???
  }

  private def removeAnnotationFromCollectionEventType(study: DisabledStudy,
    cmd: RemoveAnnotationFromCollectionEventTypeCmd,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {
    listeners sendEvent AnnotationRemovedFromCollectionEventTypeEvent(
      cmd.studyId, cmd.collectionEventTypeId, cmd.collectionEventAnnotationTypeId)
    ???
  }

}