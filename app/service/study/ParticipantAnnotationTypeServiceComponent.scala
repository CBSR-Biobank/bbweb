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

trait ParticipantAnnotationTypeServiceComponent {
  self: RepositoryComponent =>

  val participantAnnotationTypeService = new ParticipantAnnotationTypeService

  class ParticipantAnnotationTypeService
    extends StudyAnnotationTypeService[ParticipantAnnotationType] {

    /**
     * This partial function handles each command. The command is contained within the
     * StudyProcessorMsg.
     *
     *  If the command is invalid, then this method throws an Error exception.
     */
    def process = {

      case msg: StudyProcessorMsg =>
        msg.cmd match {
          case cmd: AddParticipantAnnotationTypeCmd =>
            addParticipantAnnotationType(cmd, msg.study, msg.listeners, msg.id)
          case cmd: UpdateParticipantAnnotationTypeCmd =>
            updateParticipantAnnotationType(cmd, msg.study, msg.listeners)
          case cmd: RemoveParticipantAnnotationTypeCmd =>
            removeParticipantAnnotationType(cmd, msg.study, msg.listeners)

          case _ =>
            throw new Error("invalid command received")
        }

      case _ =>
        throw new Error("invalid message received")
    }

    override def createNewAnnotationType(
      cmd: StudyAnnotationTypeCommand, id: AnnotationTypeId): ParticipantAnnotationType = {
      cmd match {
        case cmd: AddParticipantAnnotationTypeCmd =>
          ParticipantAnnotationType(
            id, 0L, StudyId(cmd.studyId), cmd.name, cmd.description, cmd.valueType,
            cmd.maxValueCount, cmd.options)
      }
    }

    override def createUpdatedAnnotationType(
      oldAnnotationType: ParticipantAnnotationType,
      cmd: StudyAnnotationTypeCommand): ParticipantAnnotationType = {
      cmd match {
        case cmd: UpdateParticipantAnnotationTypeCmd =>
          ParticipantAnnotationType(
            oldAnnotationType.id, cmd.expectedVersion.getOrElse(-1L) + 1L, StudyId(cmd.studyId),
            cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options)
      }
    }

    override def createRemovalAnnotationType(
      oldAnnotationType: ParticipantAnnotationType,
      cmd: StudyAnnotationTypeCommand): ParticipantAnnotationType = {
      cmd match {
        case cmd: RemoveParticipantAnnotationTypeCmd =>
          ParticipantAnnotationType(
            AnnotationTypeId(cmd.id), cmd.expectedVersion.getOrElse(-1), StudyId(cmd.studyId),
            oldAnnotationType.name, oldAnnotationType.description, oldAnnotationType.valueType,
            oldAnnotationType.maxValueCount, oldAnnotationType.options)
      }
    }

    override def createAnnotationTypeAddedEvent(
      newItem: ParticipantAnnotationType): ParticipantAnnotationTypeAddedEvent =
      ParticipantAnnotationTypeAddedEvent(
        newItem.studyId, newItem.id, newItem.name, newItem.description, newItem.valueType,
        newItem.maxValueCount, newItem.options)

    override def createAnnotationTypeUpdatedEvent(
      updatedItem: ParticipantAnnotationType): ParticipantAnnotationTypeUpdatedEvent =
      ParticipantAnnotationTypeUpdatedEvent(
        updatedItem.studyId, updatedItem.id, updatedItem.name, updatedItem.description,
        updatedItem.valueType, updatedItem.maxValueCount, updatedItem.options)

    override def createAnnotationTypeRemovedEvent(
      removedItem: ParticipantAnnotationType): ParticipantAnnotationTypeRemovedEvent =
      ParticipantAnnotationTypeRemovedEvent(removedItem.studyId, removedItem.id)

    override def checkNotInUse(annotationType: ParticipantAnnotationType): DomainValidation[Boolean] = {
      true.success
    }

    private def addParticipantAnnotationType(
      cmd: AddParticipantAnnotationTypeCmd,
      study: DisabledStudy,
      listeners: MessageEmitter,
      id: Option[String]): DomainValidation[ParticipantAnnotationType] =
      addAnnotationType(participantAnnotationTypeRepository, cmd, study, listeners, id)

    private def updateParticipantAnnotationType(
      cmd: UpdateParticipantAnnotationTypeCmd,
      study: DisabledStudy,
      listeners: MessageEmitter): DomainValidation[ParticipantAnnotationType] =
      updateAnnotationType(participantAnnotationTypeRepository, cmd, AnnotationTypeId(cmd.id), study, listeners)

    private def removeParticipantAnnotationType(
      cmd: RemoveParticipantAnnotationTypeCmd,
      study: DisabledStudy,
      listeners: MessageEmitter): DomainValidation[ParticipantAnnotationType] =
      removeAnnotationType(participantAnnotationTypeRepository, cmd, AnnotationTypeId(cmd.id), study, listeners)
  }

}
