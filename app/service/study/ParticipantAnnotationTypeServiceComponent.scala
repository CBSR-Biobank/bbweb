package service.study

import service.Messages._
import infrastructure.command.StudyCommands._
import infrastructure.event.StudyEvents._
import service._
import domain._
import domain.study._
import domain.study.Study._
import domain.AnnotationValueType._

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
            addParticipantAnnotationType(cmd, msg.study)
          case cmd: UpdateParticipantAnnotationTypeCmd =>
            updateParticipantAnnotationType(cmd, msg.study)
          case cmd: RemoveParticipantAnnotationTypeCmd =>
            removeParticipantAnnotationType(cmd, msg.study)

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
            cmd.maxValueCount, cmd.options, cmd.required)
      }
    }

    override def createUpdatedAnnotationType(
      oldAnnotationType: ParticipantAnnotationType,
      cmd: StudyAnnotationTypeCommand): ParticipantAnnotationType = {
      cmd match {
        case cmd: UpdateParticipantAnnotationTypeCmd =>
          ParticipantAnnotationType(
            oldAnnotationType.id, cmd.expectedVersion.getOrElse(-1L) + 1L, StudyId(cmd.studyId),
            cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options, cmd.required)
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
            oldAnnotationType.maxValueCount, oldAnnotationType.options, oldAnnotationType.required)
      }
    }

    override def checkNotInUse(annotationType: ParticipantAnnotationType): DomainValidation[Boolean] = {
      true.success
    }

    private def addParticipantAnnotationType(
      cmd: AddParticipantAnnotationTypeCmd,
      study: DisabledStudy): DomainValidation[ParticipantAnnotationTypeAddedEvent] = {
      for {
        newItem <- addAnnotationType(participantAnnotationTypeRepository, cmd, study)
        event <- ParticipantAnnotationTypeAddedEvent(
          newItem.studyId.id, newItem.id.id, newItem.version, newItem.name, newItem.description,
          newItem.valueType, newItem.maxValueCount, newItem.options).success
      } yield event
    }

    private def updateParticipantAnnotationType(
      cmd: UpdateParticipantAnnotationTypeCmd,
      study: DisabledStudy): DomainValidation[ParticipantAnnotationTypeUpdatedEvent] = {
      for {
        updatedItem <- updateAnnotationType(participantAnnotationTypeRepository, cmd, AnnotationTypeId(cmd.id), study)
        event <- ParticipantAnnotationTypeUpdatedEvent(
          updatedItem.studyId.id, updatedItem.id.id, updatedItem.version, updatedItem.name,
          updatedItem.description, updatedItem.valueType, updatedItem.maxValueCount,
          updatedItem.options).success
      } yield event
    }

    private def removeParticipantAnnotationType(
      cmd: RemoveParticipantAnnotationTypeCmd,
      study: DisabledStudy): DomainValidation[ParticipantAnnotationTypeRemovedEvent] = {
      for {
        removedItem <- removeAnnotationType(participantAnnotationTypeRepository, cmd, AnnotationTypeId(cmd.id), study)
        event <- ParticipantAnnotationTypeRemovedEvent(
          removedItem.studyId.id, removedItem.id.id).success
      } yield event
    }
  }

}
