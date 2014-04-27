package org.biobank.service.study

import org.biobank.service.Messages._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.domain.study.Study
import org.biobank.domain.AnnotationValueType._

import akka.persistence.SnapshotOffer
import scalaz._
import scalaz.Scalaz._

class SpecimenLinkAnnotationTypeProcessor (
  val annotationTypeRepository: SpecimenLinkAnnotationTypeRepositoryComponent#SpecimenLinkAnnotationTypeRepository)
    extends StudyAnnotationTypeProcessor[SpecimenLinkAnnotationType] {

  case class SnapshotState(annotationTypes: Set[SpecimenLinkAnnotationType])

  val receiveRecover: Receive = {
    case event: SpecimenLinkAnnotationTypeAddedEvent => recoverEvent(event)

    case event: SpecimenLinkAnnotationTypeUpdatedEvent => recoverEvent(event)

    case event: SpecimenLinkAnnotationTypeRemovedEvent => recoverEvent(event)

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.annotationTypes.foreach{ annotType => annotationTypeRepository.put(annotType) }
  }


  val receiveCommand: Receive = {

    case cmd: AddSpecimenLinkAnnotationTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

    case cmd: UpdateSpecimenLinkAnnotationTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

    case cmd: RemoveSpecimenLinkAnnotationTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

    case _ =>
      throw new Error("invalid message received")
  }

  private def validateCmd(cmd: AddSpecimenLinkAnnotationTypeCmd):
      DomainValidation[SpecimenLinkAnnotationTypeAddedEvent] = {
    val id = annotationTypeRepository.nextIdentity
    for {
      nameValid <- nameAvailable(cmd.name)
      newItem <- SpecimenLinkAnnotationType.create(
	StudyId(cmd.studyId), id, -1L, cmd.name, cmd.description, cmd.valueType,
	cmd.maxValueCount, cmd.options)
      event <- SpecimenLinkAnnotationTypeAddedEvent(
        newItem.studyId.id, newItem.id.id, newItem.version, newItem.name, newItem.description,
        newItem.valueType, newItem.maxValueCount, newItem.options).success
    } yield event
  }


  private def validateCmd(cmd: UpdateSpecimenLinkAnnotationTypeCmd):
      DomainValidation[SpecimenLinkAnnotationTypeUpdatedEvent] = {
    val id = AnnotationTypeId(cmd.id)
    for {
      oldItem <- annotationTypeRepository.annotationTypeWithId(StudyId(cmd.studyId), id)
      notUsed <- checkNotInUse(oldItem)
      nameValid <- nameAvailable(cmd.name, id)
      newItem <- oldItem.update(cmd.expectedVersion, cmd.name, cmd.description, cmd.valueType,
	cmd.maxValueCount, cmd.options)
      event <- SpecimenLinkAnnotationTypeUpdatedEvent(
        newItem.studyId.id, newItem.id.id, newItem.version, newItem.name, newItem.description,
	newItem.valueType, newItem.maxValueCount, newItem.options).success
    } yield event
  }

  private def validateCmd(cmd: RemoveSpecimenLinkAnnotationTypeCmd):
      DomainValidation[SpecimenLinkAnnotationTypeRemovedEvent] = {
    val id = AnnotationTypeId(cmd.id)
    for {
      item <- annotationTypeRepository.annotationTypeWithId(StudyId(cmd.studyId), id)
      notUsed <- checkNotInUse(item)
      validVersion <- validateVersion(item, cmd.expectedVersion)
      event <- SpecimenLinkAnnotationTypeRemovedEvent(item.studyId.id, item.id.id).success
    } yield event
  }


  private def recoverEvent(event: SpecimenLinkAnnotationTypeAddedEvent): Unit = {
    val studyId = StudyId(event.studyId)
    val id = AnnotationTypeId(event.annotationTypeId)
    val validation = for {
      newItem <- SpecimenLinkAnnotationType.create(studyId, id, -1L, event.name, event.description,
	event.valueType, event.maxValueCount, event.options)
      savedItem <- annotationTypeRepository.put(newItem).success
    } yield newItem

    if (validation.isFailure) {
      // this should never happen because the only way to get here is when the
      // command passed validation
      throw new IllegalStateException("recovering collection event type from event failed")
    }
  }

  private def recoverEvent(event: SpecimenLinkAnnotationTypeUpdatedEvent): Unit = {
    val validation = for {
      item <- annotationTypeRepository.getByKey(AnnotationTypeId(event.annotationTypeId))
      updatedItem <- item.update(item.versionOption, event.name, event.description, event.valueType,
	event.maxValueCount, event.options)
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

  private def recoverEvent(event: SpecimenLinkAnnotationTypeRemovedEvent): Unit = {
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

  def checkNotInUse(annotationType: SpecimenLinkAnnotationType): DomainValidation[Boolean] = {
    // FIXME: this is a stub for now
    //
    // it needs to be replaced with the real check on the specimenLink repository
    true.success
  }

}
