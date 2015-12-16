package org.biobank.service.participants

import org.biobank.service.Processor
import org.biobank.infrastructure.command.ParticipantCommands._
import org.biobank.infrastructure.event.ParticipantEvents._
import org.biobank.domain.{
  AnnotationTypeId,
  AnnotationOption,
  Location,
  LocationId,
  LocationRepository
}
import org.biobank.domain.user.UserId
import org.biobank.domain.{ DomainValidation, DomainError }
import org.biobank.domain.centre.{ CentreLocation, CentreLocationsRepository }
import org.biobank.domain.study.{ SpecimenGroupId, SpecimenGroupRepository }
import org.biobank.domain.participants._

import javax.inject.{Inject => javaxInject}
import akka.actor._
import akka.pattern.ask
import org.slf4j.LoggerFactory
import akka.persistence.SnapshotOffer
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object SpecimensProcessor {

  def props = Props[SpecimensProcessor]

}

/**
 * Responsible for handing collection event commands to add, update and remove.
 */
class SpecimensProcessor @javaxInject() (
  val specimenRepository:        SpecimenRepository,
  val specimenGroupRepository:   SpecimenGroupRepository,
  val collectionEventRepository: CollectionEventRepository,
  val locationRepository:        LocationRepository,
  val centreLocationsRepository: CentreLocationsRepository) //,
  //val containerRepository:       ContainerRepository)
    extends Processor {

  import ParticipantEvent.EventType
  import org.biobank.infrastructure.event.EventUtils._
  import org.biobank.infrastructure.event.ParticipantEventsUtil._

  override def persistenceId = "specimens-processor-id"

  case class SnapshotState(specimens: Set[Specimen])

  /**
   * These are the events that are recovered during journal recovery. They cannot fail and must be
   * processed to recreate the current state of the aggregate.
   */
  val receiveRecover: Receive = {
    case event: ParticipantEvent => event.eventType match {
      case et: EventType.SpecimenAdded              => applySpecimenAddedEvent(event)
      case et: EventType.SpecimenMoved              => applySpecimenMovedEvent(event)
      case et: EventType.SpecimenPosisitionAssigned => applySpecimenPositionAssignedEvent(event)
      case et: EventType.SpecimenAmountRemoved      => applySpecimenAmountRemovedEvent(event)
      case et: EventType.SpecimenUsableUpdated      => applySpecimenUsableUpdatedEvent(event)
      case et: EventType.SpecimenRemoved            => applySpecimenRemovedEvent(event)

      case event => log.error(s"event not handled: $event")
    }

    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.specimens.foreach{ specimenRepository.put(_) }
  }

  /**
   * These are the commands that are requested. A command can fail, and will send the failure as a response
   * back to the user. Each valid command generates one or more events and is journaled.
   */
  val receiveCommand: Receive = {
    case cmd: AddSpecimenCmd            => processAddSpecimenCmd(cmd)
    case cmd: MoveSpecimenCmd           => processMoveSpecimenCmd(cmd)
    case cmd: AssignSpecimenPositionCmd => processAssignSpecimenPositionCmd(cmd)
    case cmd: RemoveSpecimenAmountCmd   => processRemoveSpecimenAmountCmd(cmd)
    case cmd: UpdateSpecimenUsableCmd   => processUpdateSpecimenUsableCmd(cmd)
    case cmd: RemoveSpecimenCmd         => processRemoveSpecimenCmd(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(specimenRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"specimensProcessor: message not handled: $cmd")

  }

  private def processAddSpecimenCmd(cmd: AddSpecimenCmd): Unit = {
    val specimenId = specimenRepository.nextIdentity

    if (specimenRepository.getByKey(specimenId).isSuccess) {
      log.error(s"processAddSpecimenCmd: collection event with id already exsits: $specimenId")
    }

    var v = for {
      collectionEvent <- collectionEventRepository.getByKey(CollectionEventId(cmd.collectionEventId))
      specimenGroup   <- specimenGroupRepository.getByKey(SpecimenGroupId(cmd.specimenGroupId))
      location        <- validateLocationId(cmd.locationId)
      originLocation  <- validateLocationId(cmd.originLocationId)
      //---
      event           <- createEvent(collectionEvent.participantId, cmd).withSpecimenAdded(
        SpecimenAddedEvent(
          specimenId        = Some(specimenId.id),
          specimenGroupId   = Some(cmd.specimenGroupId),
          collectionEventId = Some(cmd.collectionEventId),
          originLocationId  = Some(cmd.originLocationId),
          locationId        = Some(cmd.locationId),
          containerId       = cmd.containerId,
          positionId        = cmd.positionId,
          timeCreated       = Some(ISODateTimeFormatter.print(cmd.timeCreated)),
          amount            = Some(cmd.amount.toString),
          usable            = Some(cmd.usable))).success

    } yield event

    process(v) { applySpecimenAddedEvent(_) }
  }

  def validateLocationId(id: String): DomainValidation[CentreLocation] = {
    for {
      location  <- locationRepository.getByKey(LocationId(id))
      centreLoc <- centreLocationsRepository.withLocationId(location.id)
    } yield centreLoc
  }

  private def processMoveSpecimenCmd(cmd: MoveSpecimenCmd): Unit = {
    ???
  }

  private def processAssignSpecimenPositionCmd(cmd: AssignSpecimenPositionCmd): Unit = {
    ???
  }

  private def processRemoveSpecimenAmountCmd(cmd: RemoveSpecimenAmountCmd): Unit = {
    ???
  }

  private def processUpdateSpecimenUsableCmd(cmd: UpdateSpecimenUsableCmd): Unit = {
    ???
  }

  private def processRemoveSpecimenCmd(cmd: RemoveSpecimenCmd): Unit = {
    // val v = update(cmd) { (participant, cevent) =>
    //   createEvent(participant, cmd).withSpecimenRemoved(
    //     SpecimenRemovedEvent(Some(cevent.id.id))).success
    // }
    // process(v) { applySpecimenRemovedEvent(_) }
    ???
  }

  def update
    (cmd: SpecimenModifyCommand)
    (fn: (CollectionEvent, Specimen) => DomainValidation[ParticipantEvent])
      : DomainValidation[ParticipantEvent] = {
    val specimenId = SpecimenId(cmd.id)
    val collectionEventId = CollectionEventId(cmd.collectionEventId)

    for {
      specimen     <- specimenRepository.getByKey(specimenId)
      cevent       <- collectionEventRepository.getByKey(collectionEventId)
      validVersion <- cevent.requireVersion(cmd.expectedVersion)
      event        <- fn(cevent, specimen)
    } yield event
  }

  private def applySpecimenAddedEvent(event: ParticipantEvent): Unit = {
    ???
  }

  private def applySpecimenMovedEvent(event: ParticipantEvent): Unit = {
    ???
  }

  private def applySpecimenPositionAssignedEvent(event: ParticipantEvent): Unit = {
    ???
  }

  private def applySpecimenAmountRemovedEvent(event: ParticipantEvent): Unit = {
    ???
  }

  private def applySpecimenUsableUpdatedEvent(event: ParticipantEvent): Unit = {
    ???
  }

  private def applySpecimenRemovedEvent(event: ParticipantEvent): Unit = {
    if (event.eventType.isSpecimenRemoved) {
      specimenRepository.getByKey(
        SpecimenId(event.getSpecimenRemoved.getSpecimenId))
      .fold(
        err => log.error(s"removing collection event from event failed: $err"),
        sg => {
          specimenRepository.remove(sg)
          ()
        }
      )
    } else {
      log.error(s"applySpecimenRemovedEvent: invalid event type: $event")
    }
  }

}
