package domain.service

import infrastructure._
import infrastructure.commands._
import infrastructure.events._
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

import org.eligosource.eventsourced.core._
import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

case class StudyAnnotationTypeMessage(cmd: Any, study: Study, userId: UserId, time: Long, listeners: MessageEmitter)

/**
 * This domain service class handled commands that deal with study
 * annotation types.
 *
 * @author Nelson Loyola
 */
class StudyAnnotationTypeDomainService(
  annotationTypeRepo: ReadWriteRepository[AnnotationTypeId, StudyAnnotationType])
  extends CommandHandler {
  import StudyAnnotationTypeDomainService._

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

    case msg: StudyAnnotationTypeMessage =>
      msg.cmd match {
        case cmd: AddCollectionEventAnnotationTypeCmdWithId =>
          addCollectionEventAnnotationType(msg)
        case cmd: UpdateCollectionEventAnnotationTypeCmd =>
          updateCollectionEventAnnotationType(msg)
        case cmd: RemoveCollectionEventAnnotationTypeCmd =>
          removeCollectionEventAnnotationType(msg)

        case _ =>
          throw new Error("invalid command received")
      }

    case _ =>
      throw new Error("invalid message received")
  }

  private def addCollectionEventAnnotationType(
    msg: StudyAnnotationTypeMessage): DomainValidation[CollectionEventAnnotationType] = {

    def addItem(item: CollectionEventAnnotationType): CollectionEventAnnotationType = {
      annotationTypeRepo.updateMap(item)
      msg.listeners sendEvent CollectionEventAnnotationTypeAddedEvent(
        study.id, item.id, item.name, item.description, item.valueType, item.maxValueCount,
        item.options)
      item
    }

    val item = for {
      newItem <- study.addCollectionEventAnnotationType(annotationTypeRepo, msg.cmd,
        msg.userId, msg.time)
      addItem <- addItem(newItem).success
    } yield newItem
    CommandHandler.logMethod(log, "addCollectionEventAnnotationType", msg.cmd, item)
    item
  }

  private def updateCollectionEventAnnotationType(
    msg: StudyAnnotationTypeMessage): DomainValidation[CollectionEventAnnotationType] = {

    def update(item: CollectionEventAnnotationType): CollectionEventAnnotationType = {
      annotationTypeRepo.updateMap(item)
      msg.listeners sendEvent CollectionEventAnnotationTypeUpdatedEvent(
        study.id, item.id, item.name, item.description, item.valueType,
        item.maxValueCount, item.options)
      item
    }

    val item = for {
      newItem <- study.updateCollectionEventAnnotationType(annotationTypeRepo, msg.cmd)
      updatedItem <- update(newItem).success
    } yield updatedItem
    CommandHandler.logMethod(log, "updateCollectionEventAnnotationType", msg.cmd, item)
    item
  }

  private def removeCollectionEventAnnotationType(
    msg: StudyAnnotationTypeMessage): DomainValidation[CollectionEventAnnotationType] = {

    def removeItem(item: CollectionEventAnnotationType) = {
      annotationTypeRepo.remove(item)
      msg.listeners sendEvent CollectionEventAnnotationTypeRemovedEvent(item.studyId, item.id)
    }

    val item = for {
      oldItem <- study.removeCollectionEventAnnotationType(annotationTypeRepo, msg.cmd)
      removedItem <- removeItem(oldItem).success
    } yield oldItem
    CommandHandler.logMethod(log, "removeCollectionEventAnnotationType", msg.cmd, item)
    item
  }
}

object StudyAnnotationTypeDomainService {
  val log = LoggerFactory.getLogger(StudyAnnotationTypeDomainService.getClass)
}

