package org.biobank.service.study

import org.biobank.service.{ Processor, WrappedCommand, WrappedEvent }
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEventsJson._
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
import scalaz.Validation.FlatMap._

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
        case event: CollectionEventAnnotationTypeAddedEvent =>
          recoverCollectionEventAnnotationTypeAddedEvent(event, wevent.userId, wevent.dateTime)
        case event: CollectionEventAnnotationTypeUpdatedEvent =>
          recoverCollectionEventAnnotationTypeUpdatedEvent(event, wevent.userId, wevent.dateTime)
        case event: CollectionEventAnnotationTypeRemovedEvent =>
          recoverCollectionEventAnnotationTypeRemovedEvent(event, wevent.userId, wevent.dateTime)

        case event => log.error(s"event not handled: $event")
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
        case cmd: AddCollectionEventAnnotationTypeCmd =>    processAddCollectionEventAnnotationTypeCmd(cmd)
        case cmd: UpdateCollectionEventAnnotationTypeCmd => processUpdateCollectionEventAnnotationTypeCmd(cmd)
        case cmd: RemoveCollectionEventAnnotationTypeCmd => processRemoveCollectionEventAnnotationTypeCmd(cmd)
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

  private def processAddCollectionEventAnnotationTypeCmd
    (cmd: AddCollectionEventAnnotationTypeCmd)
    (implicit userId: Option[UserId])
      : Unit = {
    val timeNow = DateTime.now
    val id = annotationTypeRepository.nextIdentity
    val event = for {
      nameValid <- nameAvailable(cmd.name)
      newItem <- CollectionEventAnnotationType.create(
        StudyId(cmd.studyId), id, -1L, timeNow, cmd.name, cmd.description,
        cmd.valueType, cmd.maxValueCount, cmd.options)
      event <- CollectionEventAnnotationTypeAddedEvent(
        studyId          = newItem.studyId.id,
        annotationTypeId = newItem.id.id,
        name             = Some(newItem.name),
        description      = newItem.description,
        valueType        = Some(newItem.valueType.toString),
        maxValueCount    = newItem.maxValueCount,
        options          = newItem.options).success
    } yield event

    process(event){ wevent =>
      recoverCollectionEventAnnotationTypeAddedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processUpdateCollectionEventAnnotationTypeCmd
    (cmd: UpdateCollectionEventAnnotationTypeCmd)
    (implicit userId: Option[UserId])
      : Unit = {
    val timeNow = DateTime.now
    val v = update(cmd) { at =>
      for {
        nameAvailable <- nameAvailable(cmd.name, AnnotationTypeId(cmd.id))
        newItem <- at.update(cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options)
      } yield newItem
    }

    val event = v.fold(
      err => DomainError(s"error $err occurred on $cmd").failureNel,
      at => CollectionEventAnnotationTypeUpdatedEvent(
        studyId          = at.studyId.id,
        annotationTypeId = at.id.id,
        version          = Some(at.version),
        name             = Some(at.name),
        description      = at.description,
        valueType        = Some(at.valueType.toString),
        maxValueCount    = at.maxValueCount,
        options          = at.options).success
    )

    process(event){ wevent =>
      recoverCollectionEventAnnotationTypeUpdatedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processRemoveCollectionEventAnnotationTypeCmd
    (cmd: RemoveCollectionEventAnnotationTypeCmd)
    (implicit userId: Option[UserId])
      : Unit = {
    val v = update(cmd) { at => at.success }

    val event = v.fold(
      err => DomainError(s"error $err occurred on $cmd").failureNel,
      at =>  CollectionEventAnnotationTypeRemovedEvent(at.studyId.id, at.id.id).success
    )

    process(event){ wevent =>
      recoverCollectionEventAnnotationTypeRemovedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def recoverCollectionEventAnnotationTypeAddedEvent
    (event: CollectionEventAnnotationTypeAddedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    log.debug(s"recoverEvent: $event")
    annotationTypeRepository.put(CollectionEventAnnotationType(
      studyId       = StudyId(event.studyId),
      id            = AnnotationTypeId(event.annotationTypeId),
      version       = 0L,
      timeAdded     = dateTime,
      timeModified  = None,
      name          = event.getName,
      description   = event.description,
      valueType     = AnnotationValueType.withName(event.getValueType),
      maxValueCount = event.maxValueCount,
      options       = event.options))
    ()
  }

  private def recoverCollectionEventAnnotationTypeUpdatedEvent
    (event: CollectionEventAnnotationTypeUpdatedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    annotationTypeRepository.getByKey(AnnotationTypeId(event.annotationTypeId)).fold(
      err => log.error(s"updating annotation type from event failed: $err"),
      at => {
        annotationTypeRepository.put(at.copy(
          version       = event.getVersion,
          name          = event.getName,
          description   = event.description,
          valueType     = AnnotationValueType.withName(event.getValueType),
          maxValueCount = event.maxValueCount,
          options       = event.options,
          timeModified  = Some(dateTime)))
        ()
      }
    )
  }

  protected def recoverCollectionEventAnnotationTypeRemovedEvent
    (event: CollectionEventAnnotationTypeRemovedEvent, userId: Option[UserId], dateTime: DateTime)
      : Unit = {
    recoverStudyAnnotationTypeRemovedEvent(AnnotationTypeId(event.annotationTypeId))
  }

  def checkNotInUse
    (annotationType: CollectionEventAnnotationType)
      : DomainValidation[CollectionEventAnnotationType] = {
    if (collectionEventTypeRepository.annotationTypeInUse(annotationType)) {
      DomainError(s"annotation type is in use by collection event type: ${annotationType.id}").failureNel
    } else {
      annotationType.success
    }
  }
}
