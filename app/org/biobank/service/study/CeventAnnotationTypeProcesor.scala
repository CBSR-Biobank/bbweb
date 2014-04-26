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
import akka.persistence.SnapshotOffer
import scalaz._
import scalaz.Scalaz._

class CeventAnnotationTypeProcessor(
  collectionEventAnnotationTypeRepository: CollectionEventAnnotationTypeRepositoryComponent#CollectionEventAnnotationTypeRepository,
  collectionEventTypeRepository: CollectionEventTypeRepositoryComponent#CollectionEventTypeRepository)
    extends Processor {

  case class SnapshotState(ceventAnnotationTypes: Set[CollectionEventAnnotationType])

  val receiveRecover: Receive = {
    case event: CollectionEventAnnotationTypeAddedEvent => recoverEvent(event)

    case event: CollectionEventAnnotationTypeUpdatedEvent => recoverEvent(event)

    case event: CollectionEventAnnotationTypeRemovedEvent => recoverEvent(event)

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.ceventAnnotationTypes.foreach{ annotType =>
	collectionEventAnnotationTypeRepository.put(annotType) }
  }


  val receiveCommand: Receive = {

    case cmd: AddCollectionEventAnnotationTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

    case cmd: UpdateCollectionEventAnnotationTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

    case cmd: RemoveCollectionEventAnnotationTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

    case _ =>
      throw new Error("invalid message received")
  }


  def validateCmd(cmd: AddCollectionEventAnnotationTypeCmd):
      DomainValidation[CollectionEventAnnotationTypeAddedEvent] = {
    val id = collectionEventAnnotationTypeRepository.nextIdentity
    for {
      newItem <- CollectionEventAnnotationType.create(
	StudyId(cmd.studyId), id, -1L, cmd.name, cmd.description, cmd.valueType,
	cmd.maxValueCount, cmd.options)
      event <- CollectionEventAnnotationTypeAddedEvent(
        newItem.studyId.id, newItem.id.id, newItem.version, newItem.name, newItem.description,
        newItem.valueType, newItem.maxValueCount, newItem.options).success
    } yield event
  }


  def validateCmd(cmd: UpdateCollectionEventAnnotationTypeCmd):
      DomainValidation[CollectionEventAnnotationTypeUpdatedEvent] = {
    null
    //     cmd match {
    //       case cmd: UpdateCollectionEventAnnotationTypeCmd =>
    //         CollectionEventAnnotationType(
    //           oldAnnotationType.id, cmd.expectedVersion.getOrElse(-1L) + 1L, StudyId(cmd.studyId),
    //           cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options)
    //     }
  }

  //   private def updateCollectionEventAnnotationType(
  //     cmd: UpdateCollectionEventAnnotationTypeCmd,
  //     study: DisabledStudy): DomainValidation[CollectionEventAnnotationTypeUpdatedEvent] = {
  //     for {
  //       updatedItem <- updateAnnotationType(collectionEventAnnotationTypeRepository, cmd, AnnotationTypeId(cmd.id), study)
  //       event <- CollectionEventAnnotationTypeUpdatedEvent(
  //         updatedItem.studyId.id, updatedItem.id.id, updatedItem.version, updatedItem.name,
  //         updatedItem.description, updatedItem.valueType, updatedItem.maxValueCount,
  //         updatedItem.options).success
  //     } yield event
  //   }

  def validateCmd(cmd: RemoveCollectionEventAnnotationTypeCmd):
      DomainValidation[CollectionEventAnnotationTypeAddedEvent] = {
    null
    //     cmd match {
    //       case cmd: RemoveCollectionEventAnnotationTypeCmd =>
    //         CollectionEventAnnotationType(
    //           AnnotationTypeId(cmd.id), cmd.expectedVersion.getOrElse(-1), StudyId(cmd.studyId),
    //           oldAnnotationType.name, oldAnnotationType.description, oldAnnotationType.valueType,
    //           oldAnnotationType.maxValueCount, oldAnnotationType.options)
    //     }
  }

  //   private def removeCollectionEventAnnotationType(
  //     cmd: RemoveCollectionEventAnnotationTypeCmd,
  //     study: DisabledStudy): DomainValidation[CollectionEventAnnotationTypeRemovedEvent] = {
  //     for {
  //       removedItem <- removeAnnotationType(collectionEventAnnotationTypeRepository, cmd, AnnotationTypeId(cmd.id), study)
  //       event <- CollectionEventAnnotationTypeRemovedEvent(
  //         removedItem.studyId.id, removedItem.id.id).success
  //     } yield event
  //   }

  def checkNotInUse(annotationType: CollectionEventAnnotationType): DomainValidation[Boolean] = {
    if (collectionEventTypeRepository.annotationTypeInUse(annotationType)) {
      DomainError(s"annotation type is in use by collection event type: ${annotationType.id}").failNel
    } else {
      true.success
    }
  }

  private def recoverEvent(event: CollectionEventAnnotationTypeAddedEvent): Unit = {
  }

  private def recoverEvent(event: CollectionEventAnnotationTypeUpdatedEvent): Unit = {
  }

  private def recoverEvent(event: CollectionEventAnnotationTypeRemovedEvent): Unit = {
  }
}
