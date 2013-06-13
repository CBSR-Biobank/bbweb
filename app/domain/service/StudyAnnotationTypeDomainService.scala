package domain.service

import org.eligosource.eventsourced.core._

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
import Study._
import AnnotationValueType._
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
  annotationTypeRepo: ReadWriteRepository[AnnotationTypeId, StudyAnnotationType])
  extends CommandHandler {

  /**
   * This partial function handles each command. The input is a Tuple3 consisting of:
   *
   *  1. The command to handle.
   *  2. The study entity the command is associated with,
   *  3. The event message listener to be notified if the command is successful.
   *
   *  If the command is invalid, then this method throws an Error exception.
   */
  def process = {

    // collection event  annotations
    case _@ (cmd: AddCollectionEventAnnotationTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      addCollectionEventAnnotationType(cmd, study, listeners)
    case _@ (cmd: UpdateCollectionEventAnnotationTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      updateCollectionEventAnnotationType(cmd, study, listeners)
    case _@ (cmd: RemoveCollectionEventAnnotationTypeCmd, study: DisabledStudy, listeners: MessageEmitter) =>
      removeCollectionEventAnnotationType(cmd, study, listeners)

    case _ =>
      throw new Error("invalid command received")

  }

  private def addCollectionEventAnnotationType(
    cmd: AddCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {

    def addItem(item: StudyAnnotationType) {
      annotationTypeRepo.updateMap(item);
      listeners sendEvent CollectionEventAnnotationTypeAddedEvent(
        study.id, item.id, item.name, item.description, item.valueType, item.maxValueCount,
        item.options)
    }

    for {
      newItem <- study.addCollectionEventAnnotationType(annotationTypeRepo, cmd)
      addItem <- addItem(newItem).success
    } yield newItem
  }

  private def updateCollectionEventAnnotationType(
    cmd: UpdateCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {

    def update(item: CollectionEventAnnotationType): DomainValidation[CollectionEventAnnotationType] = {
      annotationTypeRepo.updateMap(item)
      listeners sendEvent CollectionEventAnnotationTypeUpdatedEvent(
        study.id, item.id, item.name, item.description, item.valueType,
        item.maxValueCount, item.options)
      item.success
    }

    for {
      newItem <- study.updateCollectionEventAnnotationType(annotationTypeRepo, cmd)
      updatedItem <- update(newItem)
    } yield updatedItem
  }

  private def removeCollectionEventAnnotationType(
    cmd: RemoveCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {

    def removeItem(item: CollectionEventAnnotationType) = {
      annotationTypeRepo.remove(item)
      listeners sendEvent CollectionEventAnnotationTypeRemovedEvent(item.studyId, item.id)
    }

    for {
      item <- study.validateCollectionEventAnnotationTypeId(annotationTypeRepo, cmd.annotationTypeId)
      versionCheck <- item.requireVersion(cmd.expectedVersion)
      removedItem <- removeItem(item).success
    } yield item
  }

  private def validateValueType(
    valueType: AnnotationValueType,
    options: Map[String, String]): DomainValidation[Boolean] = {
    if (valueType.equals(AnnotationValueType.Select)) {
      if (options.isEmpty) ("select annotation type with no values to select").fail
    } else {
      if (!options.isEmpty) ("non select annotation type with values to select").fail
    }
    true.success
  }

}
