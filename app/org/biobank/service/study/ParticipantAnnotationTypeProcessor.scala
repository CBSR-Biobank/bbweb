package org.biobank.service.study

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

trait ParticipantAnnotationTypeProcessorComponent {
  self: ParticipantAnnotationTypeRepositoryComponent =>

  class ParticipantAnnotationTypeProcessor extends StudyAnnotationTypeProcessor[ParticipantAnnotationType] {

    override val annotationTypeRepository = participantAnnotationTypeRepository

    case class SnapshotState(annotationTypes: Set[ParticipantAnnotationType])

    val receiveRecover: Receive = {
      case event: ParticipantAnnotationTypeAddedEvent => recoverEvent(event)

      case event: ParticipantAnnotationTypeUpdatedEvent => recoverEvent(event)

      case event: ParticipantAnnotationTypeRemovedEvent => recoverEvent(event)

      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.annotationTypes.foreach{ annotType => annotationTypeRepository.put(annotType) }
    }


    val receiveCommand: Receive = {

      case cmd: AddParticipantAnnotationTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: UpdateParticipantAnnotationTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case cmd: RemoveParticipantAnnotationTypeCmd => process(validateCmd(cmd)){ event => recoverEvent(event) }

      case _ =>
        throw new Error("invalid message received")
    }

    private def validateCmd(cmd: AddParticipantAnnotationTypeCmd):
        DomainValidation[ParticipantAnnotationTypeAddedEvent] = {
      val id = annotationTypeRepository.nextIdentity
      for {
        nameValid <- nameAvailable(cmd.name)
        newItem <- ParticipantAnnotationType.create(
          StudyId(cmd.studyId), id, -1L, org.joda.time.DateTime.now, cmd.name, cmd.description, cmd.valueType,
          cmd.maxValueCount, cmd.options, cmd.required)
        event <- ParticipantAnnotationTypeAddedEvent(
          newItem.studyId.id, newItem.id.id, newItem.addedDate, newItem.name, newItem.description,
          newItem.valueType, newItem.maxValueCount, newItem.options, newItem.required).success
      } yield event
    }


    private def validateCmd(cmd: UpdateParticipantAnnotationTypeCmd):
        DomainValidation[ParticipantAnnotationTypeUpdatedEvent] = {
      val id = AnnotationTypeId(cmd.id)
      for {
        oldItem <- annotationTypeRepository.withId(StudyId(cmd.studyId), id)
        notUsed <- checkNotInUse(oldItem)
        nameValid <- nameAvailable(cmd.name, id)
        newItem <- oldItem.update(
         Some(cmd.expectedVersion), org.joda.time.DateTime.now, cmd.name, cmd.description, cmd.valueType,
          cmd.maxValueCount, cmd.options, cmd.required)
        event <- ParticipantAnnotationTypeUpdatedEvent(
          newItem.studyId.id, newItem.id.id, newItem.version, newItem.lastUpdateDate.get, newItem.name,
          newItem.description, newItem.valueType, newItem.maxValueCount, newItem.options,
          newItem.required).success
      } yield event
    }

    private def validateCmd(cmd: RemoveParticipantAnnotationTypeCmd):
        DomainValidation[ParticipantAnnotationTypeRemovedEvent] = {
      val id = AnnotationTypeId(cmd.id)
      for {
        item <- annotationTypeRepository.withId(StudyId(cmd.studyId), id)
        notUsed <- checkNotInUse(item)
        validVersion <- validateVersion(item,Some(cmd.expectedVersion))
        event <- ParticipantAnnotationTypeRemovedEvent(item.studyId.id, item.id.id).success
      } yield event
    }


    private def recoverEvent(event: ParticipantAnnotationTypeAddedEvent): Unit = {
      val studyId = StudyId(event.studyId)
      val id = AnnotationTypeId(event.annotationTypeId)
      val validation = for {
        newItem <- ParticipantAnnotationType.create(
          studyId, id, -1L, event.dateTime, event.name, event.description,
          event.valueType, event.maxValueCount, event.options, event.required)
        savedItem <- annotationTypeRepository.put(newItem).success
      } yield newItem

      if (validation.isFailure) {
        // this should never happen because the only way to get here is when the
        // command passed validation
        throw new IllegalStateException("recovering collection event type from event failed")
      }
    }

    private def recoverEvent(event: ParticipantAnnotationTypeUpdatedEvent): Unit = {
      val validation = for {
        item <- annotationTypeRepository.getByKey(AnnotationTypeId(event.annotationTypeId))
        updatedItem <- item.update(
          item.versionOption, event.dateTime, event.name, event.description, event.valueType,
          event.maxValueCount, event.options, event.required)
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

    private def recoverEvent(event: ParticipantAnnotationTypeRemovedEvent): Unit = {
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

    def checkNotInUse(annotationType: ParticipantAnnotationType): DomainValidation[Boolean] = {
      // FIXME: this is a stub for now
      //
      // it needs to be replaced with the real check on the participant repository
      true.success
    }


  }

}
