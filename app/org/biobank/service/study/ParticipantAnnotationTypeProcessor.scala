package org.biobank.service.study

import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service.{ Processor, WrappedCommand, WrappedEvent }
import org.biobank.domain._
import org.biobank.domain.user.UserId
import org.biobank.domain.study._
import org.biobank.domain.study.Study
import org.biobank.domain.AnnotationValueType._

import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import scaldi.akka.AkkaInjectable
import scaldi.{Injectable, Injector}
import scalaz._
import scalaz.Scalaz._

/**
  * The ParticipantAnnotationTypeProcessor is responsible for maintaining state changes for all
  * [[org.biobank.domain.study.ParticipantAnnotationType]] aggregates. This particular processor uses
  * Akka-Persistence's [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist
  * the generated events, afterwhich it will updated the current state of the
  * [[org.biobank.domain.study.ParticipantAnnotationType]] being processed.
  *
  * It is a child actor of [[org.biobank.service.study.StudiesProcessorComponent.StudiesProcessor]].
  */
class ParticipantAnnotationTypeProcessor(implicit inj: Injector)
    extends StudyAnnotationTypeProcessor[ParticipantAnnotationType]
    with AkkaInjectable {

  override def persistenceId = "participant-annotation-type-processor-id"

  override val annotationTypeRepository = inject [ParticipantAnnotationTypeRepository]

  case class SnapshotState(annotationTypes: Set[ParticipantAnnotationType])

  /**
    * These are the events that are recovered during journal recovery. They cannot fail and must be
    * processed to recreate the current state of the aggregate.
    */
  val receiveRecover: Receive = {
    case wevent: WrappedEvent[_] =>
      wevent.event match {
        case event: ParticipantAnnotationTypeAddedEvent =>   recoverEvent(event, wevent.userId, wevent.dateTime)
        case event: ParticipantAnnotationTypeUpdatedEvent => recoverEvent(event, wevent.userId, wevent.dateTime)
        case event: ParticipantAnnotationTypeRemovedEvent => recoverEvent(event, wevent.userId, wevent.dateTime)

        case event => throw new IllegalStateException(s"event not handled: $event")
      }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.annotationTypes.foreach{ annotType => annotationTypeRepository.put(annotType) }
  }


  /**
    * These are the commands that are requested. A command can fail, and will send the failure as a response
    * back to the user. Each valid command generates one or more events and is journaled.
    */
  val receiveCommand: Receive = {
    case procCmd: WrappedCommand =>
      implicit val userId = procCmd.userId
      procCmd.command match {
        case cmd: AddParticipantAnnotationTypeCmd =>    process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
        case cmd: UpdateParticipantAnnotationTypeCmd => process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
        case cmd: RemoveParticipantAnnotationTypeCmd => process(validateCmd(cmd)){ wevent => recoverEvent(wevent.event, wevent.userId, wevent.dateTime) }
      }

    case "snap" =>
      saveSnapshot(SnapshotState(annotationTypeRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"message not handled: $cmd")
  }

  /** Updates to annotation types only allowed if they are not being used by any participants.
    */
  def update
    (cmd: ParticipantAnnotationTypeCommand)
    (fn: ParticipantAnnotationType => DomainValidation[ParticipantAnnotationType])
      : DomainValidation[ParticipantAnnotationType] = {
    for {
      annotType        <- annotationTypeRepository.withId(StudyId(cmd.studyId), cmd.id)
      notInUse         <- checkNotInUse(annotType)
      validVersion     <- annotType.requireVersion( cmd.expectedVersion)
      updatedAnnotType <- fn(annotType)
    } yield updatedAnnotType
  }

  private def validateCmd(cmd: AddParticipantAnnotationTypeCmd):
      DomainValidation[ParticipantAnnotationTypeAddedEvent] = {
    val timeNow = DateTime.now
    val id = annotationTypeRepository.nextIdentity
    for {
      nameValid <- nameAvailable(cmd.name)
      newItem <- ParticipantAnnotationType.create(
        StudyId(cmd.studyId), id, -1L, timeNow,
        cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options, cmd.required)
      event <- ParticipantAnnotationTypeAddedEvent(
        newItem.studyId.id, newItem.id.id, timeNow, newItem.name, newItem.description,
        newItem.valueType, newItem.maxValueCount, newItem.options, newItem.required).success
    } yield event
  }


  private def validateCmd(cmd: UpdateParticipantAnnotationTypeCmd):
      DomainValidation[ParticipantAnnotationTypeUpdatedEvent] = {
    val timeNow = DateTime.now
    val v = update(cmd) { at =>
      for {
        nameAvailable <- nameAvailable(cmd.name, AnnotationTypeId(cmd.id))
        newItem <- at.update(
          cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options, cmd.required)
      } yield newItem
    }

    v.fold(
      err => DomainError(s"error $err occurred on $cmd").failNel,
      at => ParticipantAnnotationTypeUpdatedEvent(
        at.studyId.id, at.id.id, at.version, timeNow, at.name, at.description, at.valueType,
        at.maxValueCount, at.options, at.required).success
    )
  }

  private def validateCmd(cmd: RemoveParticipantAnnotationTypeCmd):
      DomainValidation[ParticipantAnnotationTypeRemovedEvent] = {
    val v = update(cmd) { at => at.success }

    v.fold(
      err => DomainError(s"error $err occurred on $cmd").failNel,
      at =>  ParticipantAnnotationTypeRemovedEvent(at.studyId.id, at.id.id).success
    )
  }


  private def recoverEvent(event: ParticipantAnnotationTypeAddedEvent, userId: Option[UserId], dateTime: DateTime) = {
    log.info(s"recoverEvent: $event")
    annotationTypeRepository.put(ParticipantAnnotationType(
      StudyId(event.studyId), AnnotationTypeId(event.annotationTypeId), 0L, event.dateTime, None,
      event.name, event.description, event.valueType, event.maxValueCount, event.options, event.required))
    ()
  }

  private def recoverEvent(event: ParticipantAnnotationTypeUpdatedEvent, userId: Option[UserId], dateTime: DateTime) = {
    annotationTypeRepository.getByKey(AnnotationTypeId(event.annotationTypeId)).fold(
      err => throw new IllegalStateException(s"updating annotation type from event failed: $err"),
      at => annotationTypeRepository.put(at.copy(
        version       = event.version,
        name          = event.name,
        description   = event.description,
        valueType     = event.valueType,
        maxValueCount = event.maxValueCount,
        options       = event.options,
        required      = event.required,
        timeModified = Some(event.dateTime)))
    )
    ()
  }

  protected def recoverEvent(event: ParticipantAnnotationTypeRemovedEvent, userId: Option[UserId], dateTime: DateTime): Unit = {
    recoverEvent(AnnotationTypeId(event.annotationTypeId))
  }

  def checkNotInUse
    (annotationType: ParticipantAnnotationType)
      : DomainValidation[ParticipantAnnotationType] = {
    // FIXME: this is a stub for now
    //
    // it needs to be replaced with the real check on the participant repository
    annotationType.success
  }


}
