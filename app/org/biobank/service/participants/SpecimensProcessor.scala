package org.biobank.service.participants

import akka.actor._
import akka.persistence.SnapshotOffer
import javax.inject.{Inject, Singleton}
import org.biobank.domain.DomainValidation
import org.biobank.domain.centre.CentreRepository
import org.biobank.domain.participants._
import org.biobank.domain.study.{ SpecimenGroupId, SpecimenGroupRepository }
import org.biobank.infrastructure.command.ParticipantCommands._
import org.biobank.infrastructure.event.SpecimenEvents._
import org.biobank.service.Processor
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
@Singleton
class SpecimensProcessor @Inject() (val specimenRepository:        SpecimenRepository,
                                    val specimenGroupRepository:   SpecimenGroupRepository,
                                    val collectionEventRepository: CollectionEventRepository,
                                    val centreRepository:          CentreRepository)
//,
//val containerRepository:       ContainerRepository)
    extends Processor {

  import SpecimenEvent.EventType
  import org.biobank.infrastructure.event.EventUtils._

  override def persistenceId = "specimens-processor-id"

  case class SnapshotState(specimens: Set[Specimen])

  /**
   * These are the events that are recovered during journal recovery. They cannot fail and must be
   * processed to recreate the current state of the aggregate.
   */
  val receiveRecover: Receive = {
    case event: SpecimenEvent => event.eventType match {
      case et: EventType.Added              => applyAddedEvent(event)
      case et: EventType.Moved              => applyMovedEvent(event)
      case et: EventType.PosisitionAssigned => applyPositionAssignedEvent(event)
      case et: EventType.AmountRemoved      => applyAmountRemovedEvent(event)
      case et: EventType.UsableUpdated      => applyUsableUpdatedEvent(event)
      case et: EventType.Removed            => applyRemovedEvent(event)

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
      location        <- centreRepository.getByLocationId(cmd.locationId)
      originLocation  <- centreRepository.getByLocationId(cmd.originLocationId)
    } yield SpecimenEvent(specimenId.id).update(
        _.optionalUserId            := cmd.userId,
        _.time                      := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.added.specimenSpecId      := cmd.specimenGroupId,
        _.added.collectionEventId   := cmd.collectionEventId,
        _.added.originLocationId    := cmd.originLocationId,
        _.added.locationId          := cmd.locationId,
        _.added.optionalContainerId := cmd.containerId,
        _.added.optionalPositionId  := cmd.positionId,
        _.added.timeCreated         := ISODateTimeFormatter.print(cmd.timeCreated),
        _.added.amount              := cmd.amount.toString,
        _.added.usable              := cmd.usable)

    process(v) { applyAddedEvent(_) }
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
    // process(v) { applyRemovedEvent(_) }
    ???
  }

  def update
    (cmd: SpecimenModifyCommand)
    (fn: (CollectionEvent, Specimen) => DomainValidation[SpecimenEvent])
      : DomainValidation[SpecimenEvent] = {
    val specimenId = SpecimenId(cmd.id)
    val collectionEventId = CollectionEventId(cmd.collectionEventId)

    for {
      specimen     <- specimenRepository.getByKey(specimenId)
      cevent       <- collectionEventRepository.getByKey(collectionEventId)
      validVersion <- cevent.requireVersion(cmd.expectedVersion)
      event        <- fn(cevent, specimen)
    } yield event
  }

  private def applyAddedEvent(event: SpecimenEvent): Unit = {
    ???
  }

  private def onValidEventAndVersion(event:        SpecimenEvent,
                                     eventType:    Boolean,
                                     eventVersion: Long)
                                    (fn: Specimen => Unit): Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      specimenRepository.getByKey(SpecimenId(event.id)).fold(
        err => log.error(s"specimen from event does not exist: $err"),
        spc => {
          if (spc.version != eventVersion) {
            log.error(s"event version check failed: specimen version: ${spc.version}, event: $event")
          } else {
            fn(spc)
          }
        }
      )
    }
  }

  private def applyMovedEvent(event: SpecimenEvent): Unit = {
    ???
  }

  private def applyPositionAssignedEvent(event: SpecimenEvent): Unit = {
    ???
  }

  private def applyAmountRemovedEvent(event: SpecimenEvent): Unit = {
    ???
  }

  private def applyUsableUpdatedEvent(event: SpecimenEvent): Unit = {
    ???
  }

  private def applyRemovedEvent(event: SpecimenEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isRemoved,
                           event.getRemoved.getVersion) { specimen =>
      specimenRepository.remove(specimen)
      ()
    }
  }

}
