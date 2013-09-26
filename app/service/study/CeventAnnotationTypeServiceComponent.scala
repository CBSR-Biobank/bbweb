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

trait CeventAnnotationTypeServiceComponent {
  self: RepositoryComponent =>

  val ceventAnnotationTypeService = new CeventAnnotationTypeService

  class CeventAnnotationTypeService
    extends StudyAnnotationTypeService[CollectionEventAnnotationType] {

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

    override def createNewAnnotationType(
      cmd: StudyAnnotationTypeCommand, id: AnnotationTypeId): CollectionEventAnnotationType = {
      cmd match {
        case cmd: AddCollectionEventAnnotationTypeCmd =>
          CollectionEventAnnotationType(
            id, 0L, StudyId(cmd.studyId), cmd.name, cmd.description, cmd.valueType,
            cmd.maxValueCount, cmd.options)
      }
    }

    override def createUpdatedAnnotationType(
      oldAnnotationType: CollectionEventAnnotationType,
      cmd: StudyAnnotationTypeCommand): CollectionEventAnnotationType = {
      cmd match {
        case cmd: UpdateCollectionEventAnnotationTypeCmd =>
          CollectionEventAnnotationType(
            oldAnnotationType.id, cmd.expectedVersion.getOrElse(-1L) + 1L, StudyId(cmd.studyId),
            cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options)
      }
    }

    override def createRemovalAnnotationType(
      oldAnnotationType: CollectionEventAnnotationType,
      cmd: StudyAnnotationTypeCommand): CollectionEventAnnotationType = {
      cmd match {
        case cmd: RemoveCollectionEventAnnotationTypeCmd =>
          CollectionEventAnnotationType(
            AnnotationTypeId(cmd.id), cmd.expectedVersion.getOrElse(-1), StudyId(cmd.studyId),
            oldAnnotationType.name, oldAnnotationType.description, oldAnnotationType.valueType,
            oldAnnotationType.maxValueCount, oldAnnotationType.options)
      }
    }

    override def createAnnotationTypeAddedEvent(
      newItem: CollectionEventAnnotationType): CollectionEventAnnotationTypeAddedEvent =
      CollectionEventAnnotationTypeAddedEvent(
        newItem.studyId, newItem.id, newItem.name, newItem.description, newItem.valueType,
        newItem.maxValueCount, newItem.options)

    override def createAnnotationTypeUpdatedEvent(
      updatedItem: CollectionEventAnnotationType): CollectionEventAnnotationTypeUpdatedEvent =
      CollectionEventAnnotationTypeUpdatedEvent(
        updatedItem.studyId, updatedItem.id, updatedItem.name, updatedItem.description,
        updatedItem.valueType, updatedItem.maxValueCount, updatedItem.options)

    override def createAnnotationTypeRemovedEvent(
      removedItem: CollectionEventAnnotationType): CollectionEventAnnotationTypeRemovedEvent =
      CollectionEventAnnotationTypeRemovedEvent(removedItem.studyId, removedItem.id)

    override def checkNotInUse(annotationType: CollectionEventAnnotationType): DomainValidation[Boolean] = {
      if (collectionEventTypeRepository.annotationTypeInUse(annotationType)) {
        DomainError("annotation type is in use by collection event type: " + annotationType.id).fail
      } else {
        true.success
      }
    }

    private def addCollectionEventAnnotationType(
      cmd: AddCollectionEventAnnotationTypeCmd,
      study: DisabledStudy,
      listeners: MessageEmitter,
      id: Option[String]): DomainValidation[CollectionEventAnnotationType] =
      addAnnotationType(cmd, collectionEventAnnotationTypeRepository, study, listeners, id)

    private def updateCollectionEventAnnotationType(
      cmd: UpdateCollectionEventAnnotationTypeCmd,
      study: DisabledStudy,
      listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] =
      updateAnnotationType(cmd, collectionEventAnnotationTypeRepository, AnnotationTypeId(cmd.id),
        study, listeners)

    private def removeCollectionEventAnnotationType(
      cmd: RemoveCollectionEventAnnotationTypeCmd,
      study: DisabledStudy,
      listeners: MessageEmitter): DomainValidation[CollectionEventAnnotationType] =
      removeAnnotationType(cmd, collectionEventAnnotationTypeRepository, AnnotationTypeId(cmd.id),
        study, listeners)
  }

}