package org.biobank.service.study

import org.biobank.service.{ Processor, WrappedCommand, WrappedEvent }
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain._
import org.biobank.domain.user.UserId
import org.biobank.domain.study._
import org.biobank.domain.AnnotationValueType._
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}
import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import scalaz._
import scalaz.Scalaz._

/**
  * The CeventAnnotationTypeProcessor is responsible for maintaining state changes for all
  * [[org.biobank.domain.study.CollectionEventAnnotationType]] aggregates. This particular processor uses
  * Akka-Persistence's [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist
  * the generated events, afterwhich it will updated the current state of the
  * [[org.biobank.domain.study.CollectionEventAnnotationType]] being processed.
  *
  * It is a child actor of [[org.biobank.service.study.StudiesProcessorComponent.StudiesProcessor]].
  */
class CeventAnnotationTypeProcessor(implicit inj: Injector)
    extends StudyAnnotationTypeProcessor[CollectionEventAnnotationType]
    with AkkaInjectable{

  override def persistenceId = "cevent-annotation-type-processor-id"

  case class SnapshotState(ceventAnnotationTypes: Set[CollectionEventAnnotationType])

  override val annotationTypeRepository = inject [CollectionEventAnnotationTypeRepository]

  val collectionEventTypeRepository = inject [CollectionEventTypeRepository]

  /**
    * These are the events that are recovered during journal recovery. They cannot fail and must be
    * processed to recreate the current state of the aggregate.
    */
  val receiveRecover: Receive = {
    case wevent: WrappedEvent[_] =>
      wevent.event match {
        case event: CollectionEventAnnotationTypeAddedEvent =>   recoverEvent(event, wevent.userId, wevent.dateTime)
        case event: CollectionEventAnnotationTypeUpdatedEvent => recoverEvent(event, wevent.userId, wevent.dateTime)
        case event: CollectionEventAnnotationTypeRemovedEvent => recoverEvent(event, wevent.userId, wevent.dateTime)

        case event => throw new IllegalStateException(s"event not handled: $event")
      }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.ceventAnnotationTypes.foreach{ annotType =>
        annotationTypeRepository.put(annotType) }
  }


  /**
    * These are the commands that are requested. A command can fail, and will send the failure as a response
    * back to the user. Each valid command generates one or more events and is journaled.
    */
  val receiveCommand: Receive = {
    case procCmd: WrappedCommand =>
      implicit val userId = procCmd.userId
      procCmd.command match {
        case cmd: AddCollectionEventAnnotationTypeCmd =>    process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
        case cmd: UpdateCollectionEventAnnotationTypeCmd => process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
        case cmd: RemoveCollectionEventAnnotationTypeCmd => process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
      }

    case "snap" =>
      saveSnapshot(SnapshotState(annotationTypeRepository.getValues.toSet))
      stash()

    case msg => log.error(s"CeventAnnotationTypeProcessor: message not handled: $msg")
  }

  /** Updates to annotation types only allowed if they are not being used by any collection event types.
    */
  def update
    (cmd: StudyAnnotationTypeModifyCommand)
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
        newItem.studyId.id, newItem.id.id, newItem.name,
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
        at.studyId.id, at.id.id, at.version, at.name, at.description, at.valueType,
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

  private def recoverEvent(event: CollectionEventAnnotationTypeAddedEvent, userId: Option[UserId], dateTime: DateTime): Unit = {
    log.info(s"recoverEvent: $event")
    annotationTypeRepository.put(CollectionEventAnnotationType(
      StudyId(event.studyId), AnnotationTypeId(event.annotationTypeId), 0L, dateTime, None,
      event.name, event.description, event.valueType, event.maxValueCount, event.options))
    ()
  }

  private def recoverEvent(event: CollectionEventAnnotationTypeUpdatedEvent, userId: Option[UserId], dateTime: DateTime): Unit = {
    annotationTypeRepository.getByKey(AnnotationTypeId(event.annotationTypeId)).fold(
      err => throw new IllegalStateException(s"updating annotation type from event failed: $err"),
      at => annotationTypeRepository.put(at.copy(
        version       = event.version,
        name          = event.name,
        description   = event.description,
        valueType     = event.valueType,
        maxValueCount = event.maxValueCount,
        options       = event.options,
        timeModified = Some(dateTime)))
    )
    ()
  }

  protected def recoverEvent(event: CollectionEventAnnotationTypeRemovedEvent, userId: Option[UserId], dateTime: DateTime): Unit = {
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
