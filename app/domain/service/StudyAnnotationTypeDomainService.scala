package domain.service

import org.eligosource.eventsourced.core._

import StudyValidationUtil._
import domain._
import domain.study.{
  CollectionEventAnnotationType,
  CollectionEventType,
  CollectionEventTypeId,
  DisabledStudy,
  EnabledStudy,
  Study,
  StudyAnnotationType,
  StudyId
}
import infrastructure._
import infrastructure.commands._
import infrastructure.events._
import scalaz._
import Scalaz._

/**
 * This domain service class handled commands that deal with study
 * annotation types.
 *
 * @author Nelson Loyola
 */
class StudyAnnotationTypeDomainService(
  annotationTypeRepo: ReadWriteRepository[AnnotationTypeId, StudyAnnotationType],
  annotationOptionRepo: ReadWriteRepository[String, AnnotationOption])
  extends DomainService {

  /** @inheritdoc */
  def process = PartialFunction[Any, DomainValidation[_]] {

    // collection event  annotations
    case _@ (cmd: AddCollectionEventAnnotationTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      addCollectionEventAnnotationType(cmd, study, listeners)
    case _@ (cmd: UpdateCollectionEventAnnotationTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      updateCollectionEventAnnotationType(cmd, study, listeners)
    case _@ (cmd: RemoveCollectionEventAnnotationTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      removeCollectionEventAnnotationType(cmd, study, listeners)

    // annotation options
    case _@ (cmd: AddAnnotationOptionsCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      addAnnotationOptions(cmd, study, listeners)
    case _@ (cmd: UpdateAnnotationOptionsCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      updateAnnotationOptions(cmd, study, listeners)
    case _@ (cmd: RemoveAnnotationOptionsCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      removeAnnotationOptions(cmd, study, listeners)

    case _ =>
      throw new Error("invalid command received")

  }

  /** test */
  private def addCollectionEventAnnotationType(
    cmd: AddCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {
    def addItem(item: StudyAnnotationType) {
      annotationTypeRepo.updateMap(item);
      listeners sendEvent CollectionEventAnnotationTypeAddedEvent(
        study.id, item.id, item.name, item.description, item.valueType, item.maxValueCount)
    }

    for {
      ceAnnotationTypes <- annotationTypeRepo.getMap.filter(
        cet => cet._2.studyId.equals(study.id)).success
      newItem <- study.addCollectionEventAnnotationType(ceAnnotationTypes, cmd)
      addItem <- addItem(newItem).success
    } yield newItem
  }

  private def updateCollectionEventAnnotationType(
    cmd: UpdateCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {
    def update(prevItem: CollectionEventAnnotationType): DomainValidation[CollectionEventAnnotationType] = {
      val item = CollectionEventAnnotationType(prevItem.id, prevItem.version + 1, study.id,
        cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount)
      annotationTypeRepo.updateMap(item)
      listeners sendEvent CollectionEventAnnotationTypeUpdatedEvent(
        study.id, item.id, item.name, item.description, item.valueType,
        item.maxValueCount)
      item.success
    }

    for {
      prevItem <- validateCollectionEventAnnotationTypeId(study, annotationTypeRepo, cmd.annotationTypeId)
      versionCheck <- prevItem.requireVersion(cmd.expectedVersion)
      item <- update(prevItem)
    } yield item
  }

  private def removeCollectionEventAnnotationType(
    cmd: RemoveCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {

    def removeItem(item: CollectionEventAnnotationType): CollectionEventAnnotationType = {
      annotationTypeRepo.remove(item)
      listeners sendEvent CollectionEventAnnotationTypeRemovedEvent(item.studyId, item.id)
      item
    }

    for {
      item <- validateCollectionEventAnnotationTypeId(study, annotationTypeRepo, cmd.annotationTypeId)
      versionCheck <- item.requireVersion(cmd.expectedVersion)
      removedItem <- removeItem(item).success
    } yield removedItem
  }

  private def addAnnotationOptions(
    cmd: AddAnnotationOptionsCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[AnnotationOption] = {

    //annotationOptionRepo

    ???
  }

  private def updateAnnotationOptions(
    cmd: UpdateAnnotationOptionsCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[AnnotationOption] = {
    ???
  }

  private def removeAnnotationOptions(
    cmd: RemoveAnnotationOptionsCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[AnnotationOption] = {
    ???
  }

}