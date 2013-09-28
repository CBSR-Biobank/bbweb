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

trait SpecimenLinkAnnotationTypeServiceComponent {
  self: RepositoryComponent =>

  val specimenLinkAnnotationTypeService = new SpecimenLinkAnnotationTypeService

  private val log = LoggerFactory.getLogger(this.getClass)

  class SpecimenLinkAnnotationTypeService
    extends StudyAnnotationTypeService[SpecimenLinkAnnotationType] {

    /**
     * This partial function handles each command. The command is contained within the
     * StudyProcessorMsg.
     *
     *  If the command is invalid, then this method throws an Error exception.
     */
    def process = {

      case msg: StudyProcessorMsg =>
        msg.cmd match {
          case cmd: AddSpecimenLinkAnnotationTypeCmd =>
            log.debug("repsitory is: {}", specimenLinkAnnotationTypeRepository)
            addSpecimenLinkAnnotationType(cmd, msg.study, msg.listeners, msg.id)
          case cmd: UpdateSpecimenLinkAnnotationTypeCmd =>
            updateSpecimenLinkAnnotationType(cmd, msg.study, msg.listeners)
          case cmd: RemoveSpecimenLinkAnnotationTypeCmd =>
            removeSpecimenLinkAnnotationType(cmd, msg.study, msg.listeners)

          case _ =>
            throw new Error("invalid command received")
        }

      case _ =>
        throw new Error("invalid message received")
    }

    override def createNewAnnotationType(
      cmd: StudyAnnotationTypeCommand, id: AnnotationTypeId): SpecimenLinkAnnotationType = {
      cmd match {
        case cmd: AddSpecimenLinkAnnotationTypeCmd =>
          SpecimenLinkAnnotationType(
            id, 0L, StudyId(cmd.studyId), cmd.name, cmd.description, cmd.valueType,
            cmd.maxValueCount, cmd.options)
      }
    }

    override def createUpdatedAnnotationType(
      oldAnnotationType: SpecimenLinkAnnotationType,
      cmd: StudyAnnotationTypeCommand): SpecimenLinkAnnotationType = {
      cmd match {
        case cmd: UpdateSpecimenLinkAnnotationTypeCmd =>
          SpecimenLinkAnnotationType(
            oldAnnotationType.id, cmd.expectedVersion.getOrElse(-1L) + 1L, StudyId(cmd.studyId),
            cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options)
      }
    }

    override def createRemovalAnnotationType(
      oldAnnotationType: SpecimenLinkAnnotationType,
      cmd: StudyAnnotationTypeCommand): SpecimenLinkAnnotationType = {
      cmd match {
        case cmd: RemoveSpecimenLinkAnnotationTypeCmd =>
          SpecimenLinkAnnotationType(
            AnnotationTypeId(cmd.id), cmd.expectedVersion.getOrElse(-1), StudyId(cmd.studyId),
            oldAnnotationType.name, oldAnnotationType.description, oldAnnotationType.valueType,
            oldAnnotationType.maxValueCount, oldAnnotationType.options)
      }
    }

    override def createAnnotationTypeAddedEvent(
      newItem: SpecimenLinkAnnotationType): SpecimenLinkAnnotationTypeAddedEvent =
      SpecimenLinkAnnotationTypeAddedEvent(
        newItem.studyId, newItem.id, newItem.name, newItem.description, newItem.valueType,
        newItem.maxValueCount, newItem.options)

    override def createAnnotationTypeUpdatedEvent(
      updatedItem: SpecimenLinkAnnotationType): SpecimenLinkAnnotationTypeUpdatedEvent =
      SpecimenLinkAnnotationTypeUpdatedEvent(
        updatedItem.studyId, updatedItem.id, updatedItem.name, updatedItem.description,
        updatedItem.valueType, updatedItem.maxValueCount, updatedItem.options)

    override def createAnnotationTypeRemovedEvent(
      removedItem: SpecimenLinkAnnotationType): SpecimenLinkAnnotationTypeRemovedEvent =
      SpecimenLinkAnnotationTypeRemovedEvent(removedItem.studyId, removedItem.id)

    override def checkNotInUse(annotationType: SpecimenLinkAnnotationType): DomainValidation[Boolean] = {
      true.success
    }

    private def addSpecimenLinkAnnotationType(
      cmd: AddSpecimenLinkAnnotationTypeCmd,
      study: DisabledStudy,
      listeners: MessageEmitter,
      id: Option[String]): DomainValidation[SpecimenLinkAnnotationType] =
      addAnnotationType(specimenLinkAnnotationTypeRepository, cmd, study, listeners, id)

    private def updateSpecimenLinkAnnotationType(
      cmd: UpdateSpecimenLinkAnnotationTypeCmd,
      study: DisabledStudy,
      listeners: MessageEmitter): DomainValidation[SpecimenLinkAnnotationType] =
      updateAnnotationType(specimenLinkAnnotationTypeRepository, cmd, AnnotationTypeId(cmd.id), study, listeners)

    private def removeSpecimenLinkAnnotationType(
      cmd: RemoveSpecimenLinkAnnotationTypeCmd,
      study: DisabledStudy,
      listeners: MessageEmitter): DomainValidation[SpecimenLinkAnnotationType] =
      removeAnnotationType(specimenLinkAnnotationTypeRepository, cmd, AnnotationTypeId(cmd.id), study, listeners)
  }

}
