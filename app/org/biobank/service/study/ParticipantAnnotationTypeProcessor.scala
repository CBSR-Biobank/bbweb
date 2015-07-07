package org.biobank.service.study

import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain._
import org.biobank.domain.user.UserId
import org.biobank.domain.study._
import org.biobank.domain.study.Study
import org.biobank.domain.AnnotationValueType._
import org.biobank.domain.participants.ParticipantRepository

import akka.actor._
import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object ParticipantAnnotationTypeProcessor {

  def props = Props[ParticipantAnnotationTypeProcessor]

}

/**
  * The ParticipantAnnotationTypeProcessor is responsible for maintaining state changes for all
  * [[org.biobank.domain.study.ParticipantAnnotationType]] aggregates. This particular processor uses
  * Akka-Persistence's [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist
  * the generated events, afterwhich it will updated the current state of the
  * [[org.biobank.domain.study.ParticipantAnnotationType]] being processed.
  *
  * It is a child actor of [[org.biobank.service.study.StudiesProcessorComponent.StudiesProcessor]].
  */
class ParticipantAnnotationTypeProcessor @javax.inject.Inject() (
  val annotationTypeRepository: ParticipantAnnotationTypeRepository,
  val participantRepository:    ParticipantRepository)
    extends StudyAnnotationTypeProcessor[ParticipantAnnotationType] {
  import org.biobank.infrastructure.event.StudyEventsUtil._
  import StudyEvent.EventType

  override def persistenceId = "participant-annotation-type-processor-id"

  case class SnapshotState(annotationTypes: Set[ParticipantAnnotationType])

  /**
    * These are the events that are recovered during journal recovery. They cannot fail and must be
    * processed to recreate the current state of the aggregate.
    */
  val receiveRecover: Receive = {
    case event: StudyEvent => event.eventType match {
      case et: EventType.ParticipantAnnotationTypeAdded =>
        applyParticipantAnnotationTypeAddedEvent(event)
      case et: EventType.ParticipantAnnotationTypeUpdated =>
        applyParticipantAnnotationTypeUpdatedEvent(event)
      case et: EventType.ParticipantAnnotationTypeRemoved =>
        applyParticipantAnnotationTypeRemovedEvent(event)

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
    case cmd: AddParticipantAnnotationTypeCmd =>    processAddParticipantAnnotationTypeCmd(cmd)
    case cmd: UpdateParticipantAnnotationTypeCmd => processUpdateParticipantAnnotationTypeCmd(cmd)
    case cmd: RemoveParticipantAnnotationTypeCmd => processRemoveParticipantAnnotationTypeCmd(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(annotationTypeRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"ParticipantAnnotationTypeProcessor: message not handled: $cmd")
  }

  private def processAddParticipantAnnotationTypeCmd
    (cmd: AddParticipantAnnotationTypeCmd): Unit = {
    val timeNow = DateTime.now
    val id = annotationTypeRepository.nextIdentity
    val v = for {
      nameValid <- nameAvailable(cmd.name, StudyId(cmd.studyId))
      newItem <- ParticipantAnnotationType.create(
        studyId       = StudyId(cmd.studyId),
        id            = id,
        version       = -1L,
        dateTime      = timeNow,
        name          = cmd.name,
        description   = cmd.description,
        valueType     = cmd.valueType,
        maxValueCount = cmd.maxValueCount,
        options       = cmd.options,
        required      = cmd.required)
      event <- createStudyEvent(newItem.studyId, cmd).withParticipantAnnotationTypeAdded(
        ParticipantAnnotationTypeAddedEvent(
          annotationTypeId = Some(newItem.id.id),
          name             = Some(newItem.name),
          description      = newItem.description,
          valueType        = Some(newItem.valueType.toString),
          maxValueCount    = newItem.maxValueCount,
          options          = newItem.options,
          required         = Some(newItem.required))).success
    } yield event

    process(v){ applyParticipantAnnotationTypeAddedEvent(_) }
  }


  /** Updates are only allowed if the study has no participants.
    */
  private def processUpdateParticipantAnnotationTypeCmd
    (cmd: UpdateParticipantAnnotationTypeCmd): Unit = {
    val timeNow = DateTime.now
    val v = update(cmd) { at =>
      for {
        noParticipants <- participantRepository.getValues.filter(p =>
          p.studyId.equals(at.studyId)).isEmpty.success
        nameAvailable <- nameAvailable(cmd.name, StudyId(cmd.studyId), AnnotationTypeId(cmd.id))
        newItem <- at.update(cmd.name,
                             cmd.description,
                             cmd.valueType,
                             cmd.maxValueCount,
                             cmd.options,
                             cmd.required)
        event <- createStudyEvent(newItem.studyId, cmd).withParticipantAnnotationTypeUpdated(
          ParticipantAnnotationTypeUpdatedEvent(
            annotationTypeId = Some(newItem.id.id),
            version          = Some(newItem.version),
            name             = Some(newItem.name),
            description      = newItem.description,
            valueType        = Some(newItem.valueType.toString),
            maxValueCount    = newItem.maxValueCount,
            options          = newItem.options,
            required         = Some(newItem.required))).success
      } yield event
    }
    process(v){ applyParticipantAnnotationTypeUpdatedEvent(_) }
  }

  private def processRemoveParticipantAnnotationTypeCmd
    (cmd: RemoveParticipantAnnotationTypeCmd): Unit = {
    val v = update(cmd) { at =>
      createStudyEvent(at.studyId, cmd).withParticipantAnnotationTypeRemoved(
        ParticipantAnnotationTypeRemovedEvent(Some(at.id.id))).success
    }

    process(v){ applyParticipantAnnotationTypeRemovedEvent(_) }
  }

  /** Updates to annotation types only allowed if they are not being used by any participants.
    */
  def update
    (cmd: StudyAnnotationTypeModifyCommand)
    (fn: ParticipantAnnotationType => DomainValidation[StudyEvent])
      : DomainValidation[StudyEvent] = {
    for {
      annotType    <- annotationTypeRepository.withId(StudyId(cmd.studyId), cmd.id)
      notInUse     <- checkNotInUse(annotType)
      validVersion <- annotType.requireVersion( cmd.expectedVersion)
      event        <- fn(annotType)
    } yield event
  }

  private def applyParticipantAnnotationTypeAddedEvent(event: StudyEvent) = {
    log.debug(s"applyParticipantAnnotationTypeAddedEvent: $event")

    if (event.eventType.isParticipantAnnotationTypeAdded) {
      val addedEvent = event.getParticipantAnnotationTypeAdded

      annotationTypeRepository.put(
        ParticipantAnnotationType(
          studyId       = StudyId(event.id),
          id            = AnnotationTypeId(addedEvent.getAnnotationTypeId),
          version       = 0L,
          timeAdded     = ISODateTimeFormat.dateTime.parseDateTime(event.getTime),
          timeModified  = None,
          name          = addedEvent.getName,
          description   = addedEvent.description,
          valueType     = AnnotationValueType.withName(addedEvent.getValueType),
          maxValueCount = addedEvent.maxValueCount,
          options       = addedEvent.options,
          required      = addedEvent.getRequired))
      ()
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applyParticipantAnnotationTypeUpdatedEvent(event: StudyEvent) = {
    if (event.eventType.isParticipantAnnotationTypeUpdated) {
      val updatedEvent = event.getParticipantAnnotationTypeUpdated

      annotationTypeRepository.getByKey(AnnotationTypeId(updatedEvent.getAnnotationTypeId)).fold(
        err => log.error(s"updating annotation type from event failed: $err"),
        at => {
          annotationTypeRepository.put(
            at.copy(
              version       = updatedEvent.getVersion,
              name          = updatedEvent.getName,
              description   = updatedEvent.description,
              valueType     = AnnotationValueType.withName(updatedEvent.getValueType),
              maxValueCount = updatedEvent.maxValueCount,
              options       = updatedEvent.options,
              required      = updatedEvent.getRequired,
              timeModified  = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime))))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  protected def applyParticipantAnnotationTypeRemovedEvent(event: StudyEvent) = {
    if (event.eventType.isParticipantAnnotationTypeRemoved) {
      applyStudyAnnotationTypeRemovedEvent(
        AnnotationTypeId(event.getParticipantAnnotationTypeRemoved.getAnnotationTypeId))
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  def checkNotInUse(annotationType: ParticipantAnnotationType)
      : DomainValidation[ParticipantAnnotationType] = {
    // FIXME: this is a stub for now
    //
    // it needs to be replaced with the real check on the participant repository
    annotationType.success
  }


}
