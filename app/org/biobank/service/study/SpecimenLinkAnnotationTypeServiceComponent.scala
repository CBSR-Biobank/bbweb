package org.biobank.service.study

import org.biobank.service.Messages._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.domain.study.Study
import org.biobank.domain.AnnotationValueType._

import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

trait SpecimenLinkAnnotationTypeServiceComponent {
  self: RepositoryComponent =>

  // val specimenLinkAnnotationTypeService = new SpecimenLinkAnnotationTypeService

  // private val log = LoggerFactory.getLogger(this.getClass)

  // class SpecimenLinkAnnotationTypeService
  //   extends StudyAnnotationTypeService[SpecimenLinkAnnotationType] {

  //   /**
  //    * This partial function handles each command. The command is contained within the
  //    * StudyProcessorMsg.
  //    *
  //    *  If the command is invalid, then this method throws an Error exception.
  //    */
  //   def process = {

  //     case msg: StudyProcessorMsg =>
  //       msg.cmd match {
  //         case cmd: AddSpecimenLinkAnnotationTypeCmd =>
  //           log.debug("repsitory is: {}", specimenLinkAnnotationTypeRepository)
  //           addSpecimenLinkAnnotationType(cmd, msg.study)
  //         case cmd: UpdateSpecimenLinkAnnotationTypeCmd =>
  //           updateSpecimenLinkAnnotationType(cmd, msg.study)
  //         case cmd: RemoveSpecimenLinkAnnotationTypeCmd =>
  //           removeSpecimenLinkAnnotationType(cmd, msg.study)

  //         case _ =>
  //           throw new Error("invalid command received")
  //       }

  //     case _ =>
  //       throw new Error("invalid message received")
  //   }

  //   override def createNewAnnotationType(
  //     cmd: StudyAnnotationTypeCommand, id: AnnotationTypeId): SpecimenLinkAnnotationType = {
  //     cmd match {
  //       case cmd: AddSpecimenLinkAnnotationTypeCmd =>
  //         SpecimenLinkAnnotationType(
  //           id, 0L, StudyId(cmd.studyId), cmd.name, cmd.description, cmd.valueType,
  //           cmd.maxValueCount, cmd.options)
  //     }
  //   }

  //   override def createUpdatedAnnotationType(
  //     oldAnnotationType: SpecimenLinkAnnotationType,
  //     cmd: StudyAnnotationTypeCommand): SpecimenLinkAnnotationType = {
  //     cmd match {
  //       case cmd: UpdateSpecimenLinkAnnotationTypeCmd =>
  //         SpecimenLinkAnnotationType(
  //           oldAnnotationType.id, cmd.expectedVersion.getOrElse(-1L) + 1L, StudyId(cmd.studyId),
  //           cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options)
  //     }
  //   }

  //   override def createRemovalAnnotationType(
  //     oldAnnotationType: SpecimenLinkAnnotationType,
  //     cmd: StudyAnnotationTypeCommand): SpecimenLinkAnnotationType = {
  //     cmd match {
  //       case cmd: RemoveSpecimenLinkAnnotationTypeCmd =>
  //         SpecimenLinkAnnotationType(
  //           AnnotationTypeId(cmd.id), cmd.expectedVersion.getOrElse(-1), StudyId(cmd.studyId),
  //           oldAnnotationType.name, oldAnnotationType.description, oldAnnotationType.valueType,
  //           oldAnnotationType.maxValueCount, oldAnnotationType.options)
  //     }
  //   }

  //   override def checkNotInUse(annotationType: SpecimenLinkAnnotationType): DomainValidation[Boolean] = {
  //     true.success
  //   }

  //   private def addSpecimenLinkAnnotationType(
  //     cmd: AddSpecimenLinkAnnotationTypeCmd,
  //     study: DisabledStudy): DomainValidation[SpecimenLinkAnnotationTypeAddedEvent] = {
  //     for {
  //       newItem <- addAnnotationType(specimenLinkAnnotationTypeRepository, cmd, study)
  //       event <- SpecimenLinkAnnotationTypeAddedEvent(
  //         newItem.studyId.id, newItem.id.id, newItem.version, newItem.name, newItem.description,
  //         newItem.valueType, newItem.maxValueCount, newItem.options).success
  //     } yield event
  //   }

  //   private def updateSpecimenLinkAnnotationType(
  //     cmd: UpdateSpecimenLinkAnnotationTypeCmd,
  //     study: DisabledStudy): DomainValidation[SpecimenLinkAnnotationTypeUpdatedEvent] = {
  //     for {
  //       updatedItem <- updateAnnotationType(specimenLinkAnnotationTypeRepository, cmd, AnnotationTypeId(cmd.id), study)
  //       event <- SpecimenLinkAnnotationTypeUpdatedEvent(
  //         updatedItem.studyId.id, updatedItem.id.id, updatedItem.version, updatedItem.name,
  //         updatedItem.description, updatedItem.valueType, updatedItem.maxValueCount,
  //         updatedItem.options).success
  //     } yield event
  //   }

  //   private def removeSpecimenLinkAnnotationType(
  //     cmd: RemoveSpecimenLinkAnnotationTypeCmd,
  //     study: DisabledStudy): DomainValidation[SpecimenLinkAnnotationTypeRemovedEvent] = {
  //     for {
  //       removedItem <- removeAnnotationType(specimenLinkAnnotationTypeRepository, cmd, AnnotationTypeId(cmd.id), study)
  //       event <- SpecimenLinkAnnotationTypeRemovedEvent(
  //         removedItem.studyId.id, removedItem.id.id).success
  //     } yield event
  //   }
  // }

}
