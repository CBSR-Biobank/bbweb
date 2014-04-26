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
  annotationTypeRepository: CollectionEventAnnotationTypeRepositoryComponent#CollectionEventAnnotationTypeRepository,
  collectionEventTypeRepository: CollectionEventTypeRepositoryComponent#CollectionEventTypeRepository)
    extends Processor {

  case class SnapshotState(ceventAnnotationTypes: Set[CollectionEventAnnotationType])

  val receiveRecover: Receive = {
    case event: CollectionEventAnnotationTypeAddedEvent => recoverEvent(event)

    case event: CollectionEventAnnotationTypeUpdatedEvent => recoverEvent(event)

    case event: CollectionEventAnnotationTypeRemovedEvent => recoverEvent(event)

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.ceventAnnotationTypes.foreach{ annotType =>
	annotationTypeRepository.put(annotType) }
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
    val id = annotationTypeRepository.nextIdentity
    for {
      nameValid <- nameAvailable(cmd.name)
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
    val id = AnnotationTypeId(cmd.id)
    for {
      oldItem <- annotationTypeRepository.annotationTypeWithId(StudyId(cmd.studyId), id)
      nameValid <- nameAvailable(cmd.name, id)
      newItem <- oldItem.update(cmd.expectedVersion, cmd.name, cmd.description, cmd.valueType,
	cmd.maxValueCount, cmd.options)
      event <- CollectionEventAnnotationTypeUpdatedEvent(
        newItem.studyId.id, newItem.id.id, newItem.version, newItem.name, newItem.description,
	newItem.valueType, newItem.maxValueCount, newItem.options).success
    } yield event
  }

  def validateCmd(cmd: RemoveCollectionEventAnnotationTypeCmd):
      DomainValidation[CollectionEventAnnotationTypeRemovedEvent] = {
    val id = AnnotationTypeId(cmd.id)
    for {
      item <- annotationTypeRepository.annotationTypeWithId(StudyId(cmd.studyId), id)
      validVersion <- validateVersion(item, cmd.expectedVersion)
      event <- CollectionEventAnnotationTypeRemovedEvent(item.studyId.id, item.id.id).success
    } yield event
  }

  private def recoverEvent(event: CollectionEventAnnotationTypeAddedEvent): Unit = {
    val studyId = StudyId(event.studyId)
    val id = AnnotationTypeId(event.annotationTypeId)
    val validation = for {
      newItem <- CollectionEventAnnotationType.create(studyId, id, -1L, event.name,
	event.description, event.valueType, event.maxValueCount, event.options)
      savedItem <- annotationTypeRepository.put(newItem).success
    } yield newItem

    if (validation.isFailure) {
      // this should never happen because the only way to get here is when the
      // command passed validation
      throw new IllegalStateException("recovering collection event type from event failed")
    }
  }

  private def recoverEvent(event: CollectionEventAnnotationTypeUpdatedEvent): Unit = {
    val validation = for {
      item <- annotationTypeRepository.getByKey(AnnotationTypeId(event.annotationTypeId))
      updatedItem <- item.update(item.versionOption, event.name,
	event.description, event.valueType, event.maxValueCount, event.options)
      savedItem <- annotationTypeRepository.put(updatedItem).success
    } yield updatedItem

    if (validation.isFailure) {
      // this should never happen because the only way to get here is when the
      // command passed validation
      val err = validation.swap.getOrElse(List.empty)
      throw new IllegalStateException(
	s"recovering collection event type update from event failed: $err")
    }
  }

  private def recoverEvent(event: CollectionEventAnnotationTypeRemovedEvent): Unit = {
    val validation = for {
      item <- annotationTypeRepository.getByKey(AnnotationTypeId(event.annotationTypeId))
      removedItem <- annotationTypeRepository.remove(item).success
    } yield removedItem

    if (validation.isFailure) {
      // this should never happen because the only way to get here is when the
      // command passed validation
      val err = validation.swap.getOrElse(List.empty)
      throw new IllegalStateException(
	s"recovering collection event type remove from event failed: $err")
    }
  }

  private def nameAvailable(name: String): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, annotationTypeRepository)(item => item.name.equals(name))
  }

  private def nameAvailable(name: String, excludeId: AnnotationTypeId): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, annotationTypeRepository)(item => item.name.equals(name) && (item.id != excludeId))
  }
}
