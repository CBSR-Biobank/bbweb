package service.study

import service.commands._
import service.events._
import service._
import domain._
import domain.study.{
  CollectionEventAnnotationType,
  CollectionEventAnnotationTypeRepository,
  CollectionEventTypeRepository,
  DisabledStudy,
  Study,
  StudyAnnotationType
}
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
protected[service] class StudyAnnotationTypeService() extends CommandHandler {

  val log = LoggerFactory.getLogger(this.getClass)

  /**
   * This partial function handles each command. The command is contained within the
   * StudyProcessorMsg.
   *
   *  If the command is invalid, then this method throws an Error exception.
   */
  def process = {

    case msg: StudyProcessorMsg =>
      msg.cmd match {
        case cmd: AddCollectionEventAnnotationTypeCmd =>
          addCollectionEventAnnotationType(cmd, msg.study, msg.listeners, msg.id)
        case cmd: UpdateCollectionEventAnnotationTypeCmd =>
          updateCollectionEventAnnotationType(cmd, msg.study, msg.listeners)
        case cmd: RemoveCollectionEventAnnotationTypeCmd =>
          removeCollectionEventAnnotationType(cmd, msg.study, msg.listeners)

        case _ =>
          throw new Error("invalid command received")
      }

    case _ =>
      throw new Error("invalid message received")
  }

  private def addCollectionEventAnnotationType(
    cmd: AddCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter,
    id: Option[String]): DomainValidation[CollectionEventAnnotationType] = {
    val item = for {
      atId <- id.toSuccess(DomainError("annotation type ID is missing"))
      newItem <- CollectionEventAnnotationTypeRepository.add(CollectionEventAnnotationType(
        AnnotationTypeId(atId), 0L, study.id, cmd.name, cmd.description, cmd.valueType,
        cmd.maxValueCount, cmd.options))
      event <- listeners.sendEvent(CollectionEventAnnotationTypeAddedEvent(
        newItem.studyId, newItem.id, newItem.name, newItem.description, newItem.valueType,
        newItem.maxValueCount, newItem.options)).success
    } yield newItem
    logMethod(log, "addCollectionEventAnnotationType", cmd, item)
    item
  }

  private def checkNotInUse(annotationType: CollectionEventAnnotationType): DomainValidation[Boolean] = {
    if (CollectionEventTypeRepository.annotationTypeInUse(annotationType)) {
      DomainError("annotation type is in use by collection event type: " + annotationType.id).fail
    } else {
      true.success
    }
  }

  private def updateCollectionEventAnnotationType(
    cmd: UpdateCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {

    val item = for {
      oldItem <- CollectionEventAnnotationTypeRepository.annotationTypeWithId(
        study.id, AnnotationTypeId(cmd.id))
      notInUse <- checkNotInUse(oldItem)
      newItem <- CollectionEventAnnotationTypeRepository.update(CollectionEventAnnotationType(
        oldItem.id, cmd.expectedVersion.getOrElse(-1), study.id, cmd.name, cmd.description,
        cmd.valueType, cmd.maxValueCount, cmd.options))
      event <- listeners.sendEvent(CollectionEventAnnotationTypeUpdatedEvent(
        newItem.studyId, newItem.id, newItem.name, newItem.description, newItem.valueType,
        newItem.maxValueCount, newItem.options)).success
    } yield newItem
    logMethod(log, "updateCollectionEventAnnotationType", cmd, item)
    item
  }

  private def removeCollectionEventAnnotationType(
    cmd: RemoveCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {

    val item = for {
      oldItem <- CollectionEventAnnotationTypeRepository.annotationTypeWithId(
        study.id, AnnotationTypeId(cmd.id))
      notInUse <- checkNotInUse(oldItem)
      itemToRemove <- CollectionEventAnnotationType(
        AnnotationTypeId(cmd.id), cmd.expectedVersion.getOrElse(-1), study.id,
        oldItem.name, oldItem.description, oldItem.valueType, oldItem.maxValueCount,
        oldItem.options).success
      removedItem <- CollectionEventAnnotationTypeRepository.remove(itemToRemove)
      event <- listeners.sendEvent(CollectionEventAnnotationTypeRemovedEvent(
        removedItem.studyId, removedItem.id)).success
    } yield removedItem
    logMethod(log, "removeCollectionEventAnnotationType", cmd, item)
    item
  }
}

