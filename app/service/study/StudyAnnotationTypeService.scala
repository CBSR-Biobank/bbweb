package service.study

import service.commands._
import service.events._
import service._
import domain._
import domain.study._
import domain.study.Study._
import domain.AnnotationValueType._

import org.eligosource.eventsourced.core._
import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

/**
 * This domain service class handled commands that deal with study
 * annotation types.
 *
 * @author Nelson Loyola
 */
abstract class StudyAnnotationTypeService[A <: StudyAnnotationType] extends CommandHandler {

  private val log = LoggerFactory.getLogger(this.getClass)

  protected def createNewAnnotationType(cmd: StudyAnnotationTypeCommand, id: AnnotationTypeId): A

  protected def createUpdatedAnnotationType(oldAnnotationType: A, cmd: StudyAnnotationTypeCommand): A

  protected def createRemovalAnnotationType(oldAnnotationType: A, cmd: StudyAnnotationTypeCommand): A

  protected def createAnnotationTypeAddedEvent(newItem: A): Any

  protected def createAnnotationTypeUpdatedEvent(updatedItem: A): Any

  protected def createAnnotationTypeRemovedEvent(removedItem: A): Any

  protected def checkNotInUse(annotationType: A): DomainValidation[Boolean]

  protected def addAnnotationType(
    cmd: StudyAnnotationTypeCommand,
    repository: StudyAnnotationTypeRepository[A],
    study: DisabledStudy,
    listeners: MessageEmitter,
    id: Option[String]): DomainValidation[A] = {
    val item = for {
      atId <- id.toSuccess(DomainError("annotation type ID is missing"))
      newItem <- repository.add(createNewAnnotationType(cmd, AnnotationTypeId(atId)))
      event <- listeners.sendEvent(createAnnotationTypeAddedEvent(newItem)).success
    } yield newItem
    logMethod(log, "addAnnotationType", cmd, item)
    item
  }

  protected def updateAnnotationType(
    cmd: StudyAnnotationTypeCommand,
    repository: StudyAnnotationTypeRepository[A],
    annotationTypeId: AnnotationTypeId,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[A] = {

    val item = for {
      oldAnnotationType <- repository.annotationTypeWithId(study.id, annotationTypeId)
      notInUse <- checkNotInUse(oldAnnotationType)
      newItem <- repository.update(oldAnnotationType, createUpdatedAnnotationType(oldAnnotationType, cmd))
      event <- listeners.sendEvent(createAnnotationTypeUpdatedEvent(newItem)).success
    } yield newItem
    logMethod(log, "updateAnnotationType", cmd, item)
    item
  }

  protected def removeAnnotationType(
    cmd: StudyAnnotationTypeCommand,
    repository: StudyAnnotationTypeRepository[A],
    annotationTypeId: AnnotationTypeId,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[A] = {
    val item = for {
      oldItem <- repository.annotationTypeWithId(study.id, annotationTypeId)
      notInUse <- checkNotInUse(oldItem)
      itemToRemove <- createRemovalAnnotationType(oldItem, cmd).success
      removedItem <- repository.remove(itemToRemove)
      event <- listeners.sendEvent(createAnnotationTypeRemovedEvent(removedItem)).success
    } yield removedItem
    logMethod(log, "removeAnnotationType", cmd, item)
    item
  }
}
