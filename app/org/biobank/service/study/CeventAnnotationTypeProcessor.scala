package org.biobank.service.study

import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.domain.study.Study
import org.biobank.domain.AnnotationValueType._

import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._

trait CeventAnnotationTypeProcessorComponent {
  self: CollectionEventAnnotationTypeRepositoryComponent
      with CollectionEventTypeRepositoryComponent =>

  /**
    * The CeventAnnotationTypeProcessor is responsible for maintaining state changes for all
    * [[org.biobank.domain.study.CollectionEventAnnotationType]] aggregates. This particular processor uses
    * Akka-Persistence's [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist
    * the generated events, afterwhich it will updated the current state of the
    * [[org.biobank.domain.study.CollectionEventAnnotationType]] being processed.
    *
    * It is a child actor of [[org.biobank.service.study.StudiesProcessorComponent.StudiesProcessor]].
    */
  class CeventAnnotationTypeProcessor extends StudyAnnotationTypeProcessor[CollectionEventAnnotationType] {

    override def persistenceId = "cevent-annotation-type-processor-id"

    case class SnapshotState(ceventAnnotationTypes: Set[CollectionEventAnnotationType])

    override val annotationTypeRepository = collectionEventAnnotationTypeRepository

    /**
      * These are the events that are recovered during journal recovery. They cannot fail and must be
      * processed to recreate the current state of the aggregate.
      */
    val receiveRecover: Receive = {
      case event: CollectionEventAnnotationTypeAddedEvent => recoverEvent(event)
      case event: CollectionEventAnnotationTypeUpdatedEvent => recoverEvent(event)
      case event: CollectionEventAnnotationTypeRemovedEvent => recoverEvent(event)

      case SnapshotOffer(_, snapshot: SnapshotState) =>
        snapshot.ceventAnnotationTypes.foreach{ annotType =>
          annotationTypeRepository.put(annotType) }
    }


    /**
      * These are the commands that are requested. A command can fail, and will send the failure as a response
      * back to the user. Each valid command generates one or more events and is journaled.
      */
    val receiveCommand: Receive = {

      case cmd: AddCollectionEventAnnotationTypeCmd =>
        process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: UpdateCollectionEventAnnotationTypeCmd =>
        process(validateCmd(cmd)){ event => recoverEvent(event) }
      case cmd: RemoveCollectionEventAnnotationTypeCmd =>
        process(validateCmd(cmd)){ event => recoverEvent(event) }
    }

    /** Updates to annotation types only allowed if they are not being used by any collection event types.
      */
    def update
      (cmd: CollectionEventAnnotationTypeCommand)
      (fn: CollectionEventAnnotationType => DomainValidation[CollectionEventAnnotationType])
        : DomainValidation[CollectionEventAnnotationType] = {
      for {
        annotType        <- annotationTypeRepository.withId(StudyId(cmd.studyId), cmd.id)
        notInUse         <- checkNotInUse(annotType)
        validVersion     <- annotType.requireVersion( cmd.expectedVersion)
        updatedAnnotType <- fn(annotType)
      } yield updatedAnnotType
    }

    private def validateCmd
      (cmd: AddCollectionEventAnnotationTypeCmd)
        : DomainValidation[CollectionEventAnnotationTypeAddedEvent] = {
      val timeNow = DateTime.now
      val id = annotationTypeRepository.nextIdentity
      for {
        nameValid <- nameAvailable(cmd.name)
        newItem <- CollectionEventAnnotationType.create(
          StudyId(cmd.studyId), id, -1L, timeNow, cmd.name, cmd.description,
          cmd.valueType, cmd.maxValueCount, cmd.options)
        event <- CollectionEventAnnotationTypeAddedEvent(
          newItem.studyId.id, newItem.id.id, timeNow, newItem.name,
          newItem.description, newItem.valueType, newItem.maxValueCount, newItem.options).success
      } yield event
    }


    private def validateCmd
      (cmd: UpdateCollectionEventAnnotationTypeCmd)
        : DomainValidation[CollectionEventAnnotationTypeUpdatedEvent] = {
      val timeNow = DateTime.now
      val v = update(cmd) { at =>
        for {
          nameAvailable <- nameAvailable(cmd.name, AnnotationTypeId(cmd.id))
          newItem <- at.update(cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options)
        } yield newItem
      }

      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        at => CollectionEventAnnotationTypeUpdatedEvent(
          at.studyId.id, at.id.id, at.version, timeNow, at.name, at.description, at.valueType,
          at.maxValueCount, at.options).success
      )
    }

    private def validateCmd
      (cmd: RemoveCollectionEventAnnotationTypeCmd)
        : DomainValidation[CollectionEventAnnotationTypeRemovedEvent] = {
      val v = update(cmd) { at => at.success }

      v.fold(
        err => DomainError(s"error $err occurred on $cmd").failNel,
        at =>  CollectionEventAnnotationTypeRemovedEvent(at.studyId.id, at.id.id).success
      )
    }

    private def recoverEvent(event: CollectionEventAnnotationTypeAddedEvent): Unit = {
      log.info(s"recoverEvent: $event")
      annotationTypeRepository.put(CollectionEventAnnotationType(
        StudyId(event.studyId), AnnotationTypeId(event.annotationTypeId), 0L, event.dateTime, None,
        event.name, event.description, event.valueType, event.maxValueCount, event.options))
      ()
    }

    private def recoverEvent(event: CollectionEventAnnotationTypeUpdatedEvent): Unit = {
      annotationTypeRepository.getByKey(AnnotationTypeId(event.annotationTypeId)).fold(
        err => throw new IllegalStateException(s"updating annotation type from event failed: $err"),
        at => annotationTypeRepository.put(at.copy(
          version       = event.version,
          name          = event.name,
          description   = event.description,
          valueType     = event.valueType,
          maxValueCount = event.maxValueCount,
          options       = event.options,
          timeModified = Some(event.dateTime)))
      )
      ()
    }

    protected def recoverEvent(event: CollectionEventAnnotationTypeRemovedEvent): Unit = {
      recoverEvent(AnnotationTypeId(event.annotationTypeId))
    }

    def checkNotInUse
      (annotationType: CollectionEventAnnotationType)
        : DomainValidation[CollectionEventAnnotationType] = {
      if (collectionEventTypeRepository.annotationTypeInUse(annotationType)) {
        DomainError(s"annotation type is in use by collection event type: ${annotationType.id}").failNel
      } else {
        annotationType.success
      }
    }
  }

}
