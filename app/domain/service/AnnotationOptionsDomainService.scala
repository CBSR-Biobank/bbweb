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
 * This domain service class handled commands that deal with annotation options.
 *
 * @author Nelson Loyola
 */
class AnnotationOptionsDomainService(
  annotationTypeRepo: ReadWriteRepository[AnnotationTypeId, AnnotationType],
  annotationOptionRepo: ReadWriteRepository[String, AnnotationOption]) {

  def addAnnotationOptions(
    annotationType: AnnotationType,
    options: Set[String],
    listeners: MessageEmitter): DomainValidation[AnnotationOption] = {

    def addItem(item: AnnotationOption) {
      annotationOptionRepo.updateMap(item);
      listeners sendEvent AnnotationOptionsAddedEvent(
        item.annotationTypeId, item.id, item.values)
    }

    for {
      annotationOptions <- annotationOptionRepo.getMap.filter(
        map => map._2.annotationTypeId.equals(annotationType.id)).success
      newItem <- annotationType.addOptions(annotationOptions, options)
      addItem <- addItem(newItem).success
    } yield newItem
  }

  def updateAnnotationOptions(
    annotationType: AnnotationType,
    annotationOptionId: String,
    options: Set[String],
    listeners: MessageEmitter): DomainValidation[AnnotationOption] = {
    def update(prevItem: AnnotationOption): AnnotationOption = {
      val item = AnnotationOption(prevItem.id, annotationType.id, options)
      annotationOptionRepo.updateMap(item)
      listeners sendEvent AnnotationOptionsUpdatedEvent(
        item.annotationTypeId, item.id, item.values)
      item
    }

    for {
      prevItem <- validateAnnotationOptionId(annotationOptionRepo, annotationOptionId)
      item <- update(prevItem).success
    } yield item
  }

  def removeAnnotationOptions(
    annotationType: AnnotationType,
    annotationOptionId: String,
    listeners: MessageEmitter): DomainValidation[AnnotationOption] = {
    def remove(prevItem: AnnotationOption): AnnotationOption = {
      annotationOptionRepo.updateMap(prevItem)
      listeners sendEvent AnnotationOptionsUpdatedEvent(
        prevItem.annotationTypeId, prevItem.id, prevItem.values)
      prevItem
    }

    for {
      prevItem <- validateAnnotationOptionId(annotationOptionRepo, annotationOptionId)
      item <- remove(prevItem).success
    } yield item
  }

  /**
   * Validates that the AnnotationType with id {@link annotationTypeId} exists.
   */
  def validateAnnotationTypeId(
    annotationTypeRepo: ReadRepository[AnnotationTypeId, AnnotationType],
    annotationTypeId: String): DomainValidation[AnnotationType] = {
    annotationTypeRepo.getByKey(new AnnotationTypeId(annotationTypeId)) match {
      case Some(annot) =>
        annot.success
      case None =>
        DomainError("annotation type does not exist: %s" format annotationTypeId).fail
    }
  }

  /**
   * Validates that the AnnotationOption with id {@link annotationOptionId} exists.
   */
  private def validateAnnotationOptionId(
    annotationOptionRepo: ReadRepository[String, AnnotationOption],
    annotationOptionId: String): DomainValidation[AnnotationOption] = {
    annotationOptionRepo.getByKey(annotationOptionId) match {
      case Some(option) =>
        option.success
      case None =>
        DomainError("annotation type does not exist: %s" format annotationOptionId).fail
    }
  }

}