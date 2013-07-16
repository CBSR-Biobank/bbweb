package service.study

import infrastructure._
import infrastructure.commands._
import infrastructure.events._
import domain._
import domain.study.{ CollectionEventAnnotationType, DisabledStudy, Study, StudyAnnotationType }
import domain.study.Study._
import domain.AnnotationValueType._
import service._

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
protected[service] class StudyAnnotationTypeService()
  extends CommandHandler {
  import StudyAnnotationTypeService._

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

    def addItem(item: CollectionEventAnnotationType): CollectionEventAnnotationType = {
      annotationTypeRepo.updateMap(item)
      listeners sendEvent CollectionEventAnnotationTypeAddedEvent(
        item.studyId, item.id, item.name, item.description, item.valueType, item.maxValueCount,
        item.options)
      item
    }

    val item = for {
      atId <- id.toSuccess(DomainError("annotation type ID is missing"))
      newItem <- study.addCollectionEventAnnotationType(annotationTypeRepo, cmd, atId)
      addItem <- addItem(newItem).success
    } yield newItem
    CommandHandler.logMethod(log, "addCollectionEventAnnotationType", cmd, item)
    item
  }

  private def updateCollectionEventAnnotationType(
    cmd: UpdateCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {

    def update(item: CollectionEventAnnotationType): CollectionEventAnnotationType = {
      annotationTypeRepo.updateMap(item)
      listeners sendEvent CollectionEventAnnotationTypeUpdatedEvent(
        item.studyId, item.id, item.name, item.description, item.valueType,
        item.maxValueCount, item.options)
      item
    }

    val item = for {
      validStudy <- StudyValidation.validateCollectionEventAnnotationTypeId(study, annotationTypeRepo, cmd.id)
      newItem <- study.updateCollectionEventAnnotationType(annotationTypeRepo, cmd)
      updatedItem <- update(newItem).success
    } yield updatedItem
    CommandHandler.logMethod(log, "updateCollectionEventAnnotationType", cmd, item)
    item
  }

  private def removeCollectionEventAnnotationType(
    cmd: RemoveCollectionEventAnnotationTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] = {

    def removeItem(item: CollectionEventAnnotationType) = {
      annotationTypeRepo.remove(item)
      listeners sendEvent CollectionEventAnnotationTypeRemovedEvent(item.studyId, item.id)
    }

    val item = for {
      validStudy <- StudyValidation.validateCollectionEventAnnotationTypeId(study, annotationTypeRepo, cmd.id)
      oldItem <- study.removeCollectionEventAnnotationType(annotationTypeRepo, cmd)
      removedItem <- removeItem(oldItem).success
    } yield oldItem
    CommandHandler.logMethod(log, "removeCollectionEventAnnotationType", cmd, item)
    item
  }
}

object StudyAnnotationTypeService {
  val log = LoggerFactory.getLogger(StudyAnnotationTypeService.getClass)
}

