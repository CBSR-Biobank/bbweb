package org.biobank.service.study

import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEventsJson._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service._
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
import scalaz.Validation.FlatMap._

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

  val participantRepository = inject [ParticipantRepository]

  case class SnapshotState(annotationTypes: Set[ParticipantAnnotationType])

  /**
    * These are the events that are recovered during journal recovery. They cannot fail and must be
    * processed to recreate the current state of the aggregate.
    */
  val receiveRecover: Receive = {
    case wevent: WrappedEvent[_] =>
      wevent.event match {
        case event: ParticipantAnnotationTypeAddedEvent =>
          recoverParticipantAnnotationTypeAddedEvent(event, wevent.userId, wevent.dateTime)
        case event: ParticipantAnnotationTypeUpdatedEvent =>
          recoverParticipantAnnotationTypeUpdatedEvent(event, wevent.userId, wevent.dateTime)
        case event: ParticipantAnnotationTypeRemovedEvent =>
          recoverParticipantAnnotationTypeRemovedEvent(event, wevent.userId, wevent.dateTime)

        case event => log.error(s"event not handled: $event")
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
        case cmd: AddParticipantAnnotationTypeCmd =>    processAddParticipantAnnotationTypeCmd(cmd)
        case cmd: UpdateParticipantAnnotationTypeCmd => processUpdateParticipantAnnotationTypeCmd(cmd)
        case cmd: RemoveParticipantAnnotationTypeCmd => processRemoveParticipantAnnotationTypeCmd(cmd)
      }

    case "snap" =>
      saveSnapshot(SnapshotState(annotationTypeRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"ParticipantAnnotationTypeProcessor: message not handled: $cmd")
  }

  /** Updates to annotation types only allowed if they are not being used by any participants.
    */
  def update
    (cmd: StudyAnnotationTypeModifyCommand)
    (fn: ParticipantAnnotationType => DomainValidation[ParticipantAnnotationType])
      : DomainValidation[ParticipantAnnotationType] = {
    for {
      annotType        <- annotationTypeRepository.withId(StudyId(cmd.studyId), cmd.id)
      notInUse         <- checkNotInUse(annotType)
      validVersion     <- annotType.requireVersion( cmd.expectedVersion)
      updatedAnnotType <- fn(annotType)
    } yield updatedAnnotType
  }

  private def processAddParticipantAnnotationTypeCmd
    (cmd: AddParticipantAnnotationTypeCmd)
    (implicit userId: Option[UserId])
      : Unit = {
    val timeNow = DateTime.now
    val id = annotationTypeRepository.nextIdentity
    val event = for {
      nameValid <- nameAvailable(cmd.name)
      newItem <- ParticipantAnnotationType.create(
        StudyId(cmd.studyId), id, -1L, timeNow,
        cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options, cmd.required)
      event <- ParticipantAnnotationTypeAddedEvent(
        studyId          = newItem.studyId.id,
        annotationTypeId = newItem.id.id,
        name             = Some(newItem.name),
        description      = newItem.description,
        valueType        = Some(newItem.valueType.toString),
        maxValueCount    = newItem.maxValueCount,
        options          = newItem.options,
        required         = Some(newItem.required)).success
    } yield event

    process(event){ wevent =>
      recoverParticipantAnnotationTypeAddedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }


  /** Updates are only allowed if the study has no participants.
    */
  private def processUpdateParticipantAnnotationTypeCmd
    (cmd: UpdateParticipantAnnotationTypeCmd)
    (implicit userId: Option[UserId])
      : Unit = {
    val timeNow = DateTime.now
    val v = update(cmd) { at =>
      for {
        noParticipants <- participantRepository.getValues.filter(p =>
          p.studyId.equals(at.studyId)).isEmpty.success
        nameAvailable <- nameAvailable(cmd.name, AnnotationTypeId(cmd.id))
        newItem <- at.update(
          cmd.name, cmd.description, cmd.valueType, cmd.maxValueCount, cmd.options, cmd.required)
      } yield newItem
    }

    val event = v.fold(
      err => DomainError(s"error $err occurred on $cmd").failureNel,
      at => ParticipantAnnotationTypeUpdatedEvent(
        studyId          = at.studyId.id,
        annotationTypeId = at.id.id,
        version          = Some(at.version),
        name             = Some(at.name),
        description      = at.description,
        valueType        = Some(at.valueType.toString),
        maxValueCount    = at.maxValueCount,
        options          = at.options,
        required         = Some(at.required)).success
    )
    process(event){ wevent =>
      recoverParticipantAnnotationTypeUpdatedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def processRemoveParticipantAnnotationTypeCmd
    (cmd: RemoveParticipantAnnotationTypeCmd)
    (implicit userId: Option[UserId])
      : Unit = {
    val v = update(cmd) { at => at.success }

    val event = v.fold(
      err => DomainError(s"error $err occurred on $cmd").failureNel,
      at =>  ParticipantAnnotationTypeRemovedEvent(at.studyId.id, at.id.id).success
    )

    process(event){ wevent =>
      recoverParticipantAnnotationTypeRemovedEvent(wevent.event, wevent.userId, wevent.dateTime)
    }
  }

  private def recoverParticipantAnnotationTypeAddedEvent
    (event: ParticipantAnnotationTypeAddedEvent, userId: Option[UserId], dateTime: DateTime) = {
    log.debug(s"recoverEvent: $event")
    annotationTypeRepository.put(ParticipantAnnotationType(
      studyId       = StudyId(event.studyId),
      id            = AnnotationTypeId(event.annotationTypeId),
      version       = 0L,
      timeAdded     = dateTime,
      timeModified  = None,
      name          = event.getName,
      description   = event.description,
      valueType     = AnnotationValueType.withName(event.getValueType),
      maxValueCount = event.maxValueCount,
      options       = event.options,
      required      = event.getRequired))
    ()
  }

  private def recoverParticipantAnnotationTypeUpdatedEvent
    (event: ParticipantAnnotationTypeUpdatedEvent, userId: Option[UserId], dateTime: DateTime) = {
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
          required      = event.getRequired,
          timeModified  = Some(dateTime)))
        ()
      }
    )
  }

  protected def recoverParticipantAnnotationTypeRemovedEvent
    (event: ParticipantAnnotationTypeRemovedEvent, userId: Option[UserId], dateTime: DateTime): Unit = {
    recoverStudyAnnotationTypeRemovedEvent(AnnotationTypeId(event.annotationTypeId))
  }

  def checkNotInUse(annotationType: ParticipantAnnotationType)
      : DomainValidation[ParticipantAnnotationType] = {
    // FIXME: this is a stub for now
    //
    // it needs to be replaced with the real check on the participant repository
    annotationType.success
  }


}
