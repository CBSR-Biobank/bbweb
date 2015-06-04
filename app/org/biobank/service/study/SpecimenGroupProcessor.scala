package org.biobank.service.study

import org.biobank.service.{ Processor, WrappedEvent }
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  DomainValidation,
  DomainError
}
import org.biobank.domain. {
  AnatomicalSourceType,
  PreservationType,
  PreservationTemperatureType,
  SpecimenType
}
import org.biobank.domain.user.UserId
import org.biobank.domain.study.{
  CollectionEventTypeRepository,
  SpecimenGroup,
  SpecimenGroupId,
  SpecimenGroupRepository,
  SpecimenLinkTypeRepository,
  Study,
  StudyId
}
import javax.inject.{Inject => javaxInject}

import akka.actor._
import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object SpecimenGroupProcessor {

  def props = Props[SpecimenGroupProcessor]

}

/** The SpecimenGroupProcessor is responsible for maintaining state changes for all
  * [[org.biobank.domain.study.SpecimenGroup]] aggregates. This particular processor uses Akka-Persistence's
  * [[akka.persistence.PersistentActor]]. It receives Commands and if valid will persist the generated
  * events, afterwhich it will updated the current state of the [[org.biobank.domain.study.SpecimenGroup]]
  * being processed.
  *
  * It is a child actor of  [[org.biobank.service.study.StudiesProcessorComponent.StudiesProcessor]].
  *
  * It handles commands that deal with a Specimen Group.
  *
  */
class SpecimenGroupProcessor @javaxInject() (
  val collectionEventTypeRepository: CollectionEventTypeRepository,
  val specimenGroupRepository:       SpecimenGroupRepository,
  val specimenLinkTypeRepository:    SpecimenLinkTypeRepository)
    extends Processor {
  import org.biobank.infrastructure.event.StudyEventsUtil._
  import StudyEvent.EventType

  override def persistenceId = "specimen-group-processor-id"

  case class SnapshotState(specimenGroups: Set[SpecimenGroup])

  /**
    * These are the events that are recovered during journal recovery. They cannot fail and must be
    * processed to recreate the current state of the aggregate.
    */
  val receiveRecover: Receive = {
    case event: StudyEvent => event.eventType match {
      case et: EventType.SpecimenGroupAdded   => applySpecimenGroupAddedEvent(event)
      case et: EventType.SpecimenGroupUpdated => applySpecimenGroupUpdatedEvent(event)
      case et: EventType.SpecimenGroupRemoved => applySpecimenGroupRemovedEvent(event)

      case event => log.error(s"event not handled: $event")
    }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.specimenGroups.foreach{ specimenGroup => specimenGroupRepository.put(specimenGroup) }
  }

  /**
    * These are the commands that are requested. A command can fail, and will send the failure as a response
    * back to the user. Each valid command generates one or more events and is journaled.
    */
  val receiveCommand: Receive = {
    case cmd: AddSpecimenGroupCmd    => processAddSpecimenGroupCmd(cmd)
    case cmd: UpdateSpecimenGroupCmd => processUpdateSpecimenGroupCmd(cmd)
    case cmd: RemoveSpecimenGroupCmd => processRemoveSpecimenGroupCmd(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(specimenGroupRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"SpecimenGroupProcessor: message not handled: $cmd")

  }

  private def processAddSpecimenGroupCmd(cmd: AddSpecimenGroupCmd): Unit = {
    val timeNow = DateTime.now
    val studyId = StudyId(cmd.studyId)
    val sgId = specimenGroupRepository.nextIdentity

    if (specimenGroupRepository.getByKey(sgId).isSuccess) {
      log.error(s"specimen group with id already exsits: $id")
    }

    val v = for {
      nameValid <- nameAvailable(cmd.name, studyId)
      newItem <-SpecimenGroup.create(
        StudyId(cmd.studyId), sgId, -1, timeNow, cmd.name, cmd.description,
        cmd.units, cmd.anatomicalSourceType, cmd.preservationType,
        cmd.preservationTemperatureType, cmd.specimenType)
      newEvent <- createStudyEvent(newItem.studyId, cmd).withSpecimenGroupAdded(
        SpecimenGroupAddedEvent(
          specimenGroupId             = Some(newItem.id.id),
          name                        = Some(newItem.name),
          description                 = newItem.description,
          units                       = Some(newItem.units),
          anatomicalSourceType        = Some(newItem.anatomicalSourceType.toString),
          preservationType            = Some(newItem.preservationType.toString),
          preservationTemperatureType = Some(newItem.preservationTemperatureType.toString),
          specimenType                = Some(newItem.specimenType.toString))).success
    } yield newEvent

    process(v){ applySpecimenGroupAddedEvent(_) }
  }

  private def processUpdateSpecimenGroupCmd(cmd: UpdateSpecimenGroupCmd): Unit = {
    val timeNow = DateTime.now
    val studyId = StudyId(cmd.studyId)
    val specimenGroupId = SpecimenGroupId(cmd.id)

    val v = update(cmd) { sg =>
      for {
        nameAvailable <- nameAvailable(cmd.name, studyId, specimenGroupId)
        updatedSg <- sg.update(cmd.name,
                               cmd.description,
                               cmd.units,
                               cmd.anatomicalSourceType,
                               cmd.preservationType,
                               cmd.preservationTemperatureType,
                               cmd.specimenType)
        event <- createStudyEvent(updatedSg.studyId, cmd).withSpecimenGroupUpdated(
          SpecimenGroupUpdatedEvent(
            specimenGroupId             = Some(updatedSg.id.id),
            version                     = Some(updatedSg.version),
            name                        = Some(cmd.name),
            description                 = cmd.description,
            units                       = Some(cmd.units),
            anatomicalSourceType        = Some(cmd.anatomicalSourceType.toString),
            preservationType            = Some(cmd.preservationType.toString),
            preservationTemperatureType = Some(cmd.preservationTemperatureType.toString),
            specimenType                = Some(cmd.specimenType.toString))).success
      } yield event
    }

    process(v) { applySpecimenGroupUpdatedEvent(_) }
  }

  private def processRemoveSpecimenGroupCmd(cmd: RemoveSpecimenGroupCmd): Unit = {
    val v = update(cmd) { sg =>
      createStudyEvent(sg.studyId, cmd).withSpecimenGroupRemoved(
        SpecimenGroupRemovedEvent(Some(sg.id.id))).success
    }
    process(v){ applySpecimenGroupRemovedEvent(_) }
  }

  def update
    (cmd: SpecimenGroupModifyCommand)
    (fn: SpecimenGroup => DomainValidation[StudyEvent])
      : DomainValidation[StudyEvent] = {
    for {
      sg           <- specimenGroupRepository.withId(StudyId(cmd.studyId), SpecimenGroupId(cmd.id))
      notInUse     <- checkNotInUse(sg.studyId, sg.id)
      validVersion <- sg.requireVersion(cmd.expectedVersion)
      event        <- fn(sg)
    } yield event
  }

  private def applySpecimenGroupAddedEvent(event: StudyEvent) : Unit = {
    if (event.eventType.isSpecimenGroupAdded) {
      val addedEvent = event.getSpecimenGroupAdded

      specimenGroupRepository.put(
        SpecimenGroup(
          studyId                     = StudyId(event.id),
          id                          = SpecimenGroupId(addedEvent.getSpecimenGroupId),
          version                     = 0L,
          timeAdded                   = ISODateTimeFormat.dateTime.parseDateTime(event.getTime),
          timeModified                = None,
          name                        = addedEvent.getName,
          description                 = addedEvent.description,
          units                       = addedEvent.getUnits,
          anatomicalSourceType        = AnatomicalSourceType.withName(addedEvent.getAnatomicalSourceType),
          preservationType            = PreservationType.withName(addedEvent.getPreservationType),
          preservationTemperatureType = PreservationTemperatureType.withName(addedEvent.getPreservationTemperatureType),
          specimenType                = SpecimenType.withName(addedEvent.getSpecimenType)
        ))
      ()
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applySpecimenGroupUpdatedEvent(event: StudyEvent) : Unit = {
    if (event.eventType.isSpecimenGroupUpdated) {
      val updatedEvent = event.getSpecimenGroupUpdated

      specimenGroupRepository.getByKey(SpecimenGroupId(updatedEvent.getSpecimenGroupId)).fold(
        err => log.error(s"updating annotation type from event failed: $err"),
        sg => {
          specimenGroupRepository.put(
            sg.copy(
              version                     = updatedEvent.getVersion,
              timeModified                = Some(ISODateTimeFormat.dateTime.parseDateTime(event.getTime)),
              name                        = updatedEvent.getName,
              description                 = updatedEvent.description,
              units                       = updatedEvent.getUnits,
              anatomicalSourceType        = AnatomicalSourceType.withName(updatedEvent.getAnatomicalSourceType),
              preservationType            = PreservationType.withName(updatedEvent.getPreservationType),
              preservationTemperatureType = PreservationTemperatureType.withName(updatedEvent.getPreservationTemperatureType),
              specimenType                = SpecimenType.withName(updatedEvent.getSpecimenType)
            ))
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  private def applySpecimenGroupRemovedEvent(event: StudyEvent) : Unit = {
    if (event.eventType.isSpecimenGroupRemoved) {
      specimenGroupRepository.getByKey(
        SpecimenGroupId(event.getSpecimenGroupRemoved.getSpecimenGroupId))
      .fold(
        err => log.error(s"updating annotation type from event failed: $err"),
        sg => {
          specimenGroupRepository.remove(sg)
          ()
        }
      )
    } else {
      log.error(s"invalid event type: $event")
    }
  }

  val ErrMsgNameExists = "specimen group with name already exists"

  private def nameAvailable(specimenGroupName: String, studyId: StudyId)
      : DomainValidation[Boolean] = {
    nameAvailableMatcher(specimenGroupName, specimenGroupRepository, ErrMsgNameExists) { item =>
      (item.name == specimenGroupName) && (item.studyId == studyId)
    }
  }

  private def nameAvailable(specimenGroupName: String,
                            studyId: StudyId,
                            id: SpecimenGroupId)
      : DomainValidation[Boolean] = {
    nameAvailableMatcher(specimenGroupName, specimenGroupRepository, ErrMsgNameExists) { item =>
      (item.name == specimenGroupName) && (item.studyId == studyId) && (item.id != id)
    }
  }

  private def checkNotInUse(studyId: StudyId,
                            specimenGroupId: SpecimenGroupId)
      : DomainValidation[Boolean] = {

    def checkNotInUseByCollectionEventType: DomainValidation[Boolean] = {
      if (collectionEventTypeRepository.specimenGroupCanBeUpdated(studyId, specimenGroupId)) {
        DomainError(s"specimen group is in use by collection event type: $specimenGroupId").failureNel
      } else {
        true.success
      }
    }

    def checkNotInUseBySpecimenLinkType: DomainValidation[Boolean] = {
      if (specimenLinkTypeRepository.specimenGroupCanBeUpdated(specimenGroupId)) {
        DomainError(s"specimen group is in use by specimen link type: $specimenGroupId").failureNel
      } else {
        true.success
      }
    }

    (checkNotInUseByCollectionEventType |@| checkNotInUseBySpecimenLinkType) { case (_, _) => true }
  }

}
