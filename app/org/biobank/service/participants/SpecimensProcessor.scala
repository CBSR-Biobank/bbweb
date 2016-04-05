package org.biobank.service.participants

import akka.actor._
import akka.persistence.SnapshotOffer
import javax.inject.{Inject, Singleton}
import org.biobank.domain.DomainValidation
import org.biobank.domain.participants._
import org.biobank.domain.study.{CollectionEventType, CollectionEventTypeRepository}
import org.biobank.infrastructure.command.SpecimenCommands._
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
class SpecimensProcessor @Inject() (val specimenRepository:            SpecimenRepository,
                                    val collectionEventRepository:     CollectionEventRepository,
                                    val collectionEventTypeRepository: CollectionEventTypeRepository,
                                    val ceventSpecimenRepository:      CeventSpecimenRepository)
// FIXME add container repository when implemented
//val containerRepository:       ContainerRepository)
    extends Processor {

  import SpecimenEvent.EventType
  import org.biobank.infrastructure.event.EventUtils._
  import org.biobank.CommonValidations._

  override def persistenceId = "specimens-processor-id"

  case class SnapshotState(specimens: Set[Specimen])

  def specimenSpecNotFound(id: String) = IdNotFound(s"collection specimen spec id: $id")

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
    case cmd: AddSpecimensCmd           => processAddCmd(cmd)
    case cmd: MoveSpecimensCmd          => processMoveCmd(cmd)
    case cmd: SpecimenAssignPositionCmd => processAssignPositionCmd(cmd)
    case cmd: SpecimenRemoveAmountCmd   => processRemoveAmountCmd(cmd)
    case cmd: SpecimenUpdateUsableCmd   => processUpdateUsableCmd(cmd)
    case cmd: RemoveSpecimenCmd         => processRemoveCmd(cmd)

    case "snap" =>
      saveSnapshot(SnapshotState(specimenRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"specimensProcessor: message not handled: $cmd")

  }

  private def processAddCmd(cmd: AddSpecimensCmd): Unit = {
    val specimenId = specimenRepository.nextIdentity

    if (specimenRepository.getByKey(specimenId).isSuccess) {
      log.error(s"processAddCmd: collection event with id already exsits: $specimenId")
    }

    var v = for {
        collectionEvent <- collectionEventRepository.getByKey(CollectionEventId(cmd.collectionEventId))
        ceventType      <- collectionEventTypeRepository.getByKey(collectionEvent.collectionEventTypeId)
        specIdsValid    <- validateSpecimenData(cmd.specimenData, ceventType)
      } yield SpecimenEvent(cmd.userId).update(
        _.time                    := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.added.collectionEventId := collectionEvent.id.id,
        _.added.specimenData      := cmd.specimenData.map { specimenDataToEvent(_) })

    process(v) { applyAddedEvent(_) }
  }

  private def validateSpecimenData(specimenData: List[SpecimenData], ceventType: CollectionEventType)
      : DomainValidation[Boolean] = {

    val cmdSpecIds    = specimenData.map(s => s.specimenSpecId).toSet
    val ceventSpecIds = ceventType.specimenSpecs.map(s => s.uniqueId).toSet
    val notBelonging  = cmdSpecIds.diff(ceventSpecIds)

    log.info(s"cmdSpecIds: $cmdSpecIds")
    log.info(s"ceventSpecIds: $ceventSpecIds")

    if (notBelonging.isEmpty) {
      true.success
    } else {
      EntityCriteriaError("specimen specs do not belong to collection event type: "
                            + notBelonging.mkString(", ")).failureNel
    }
  }

  private def processMoveCmd(cmd: MoveSpecimensCmd): Unit = {
    ???
  }

  private def processAssignPositionCmd(cmd: SpecimenAssignPositionCmd): Unit = {
    ???
  }

  private def processRemoveAmountCmd(cmd: SpecimenRemoveAmountCmd): Unit = {
    ???
  }

  private def processUpdateUsableCmd(cmd: SpecimenUpdateUsableCmd): Unit = {
    ???
  }

  private def processRemoveCmd(cmd: RemoveSpecimenCmd): Unit = {
    // val v = update(cmd) { (participant, cevent) =>
    //   createEvent(participant, cmd).withSpecimenRemoved(
    //     SpecimenRemovedEvent(Some(cevent.id.id))).success
    // }
    // process(v) { applyRemovedEvent(_) }
    ???
  }

  private def applyAddedEvent(event: SpecimenEvent): Unit = {
    val v = for {
        validEventType <- validEventType(event.eventType.isAdded)
        specimens      <- {
          event.getAdded.specimenData.toList.traverseU { data =>
            UsableSpecimen.create(id               = SpecimenId(data.getSpecimenId),
                                  specimenSpecId   = data.getSpecimenSpecId,
                                  version          = 0L,
                                  originLocationId = data.getLocationId,
                                  locationId       = data.getLocationId,
                                  containerId      = None,
                                  positionId       = None,
                                  timeCreated      = ISODateTimeParser.parseDateTime(data.getTimeCreated),
                                  amount           = BigDecimal(data.getAmount))
          }
        }
      } yield specimens

    logIfError(event, v)
    v.map { specimens =>
      val ceventId = CollectionEventId(event.getAdded.getCollectionEventId)
      specimens.foreach { specimen =>
        specimenRepository.put(specimen)
        ceventSpecimenRepository.put(CeventSpecimen(ceventId, specimen.id))
      }
    }
    ()
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
    val v = for {
        validEventType <- validEventType(event.eventType.isRemoved)
        specimen       <- specimenRepository.getByKey(SpecimenId(event.getRemoved.getSpecimenId))
        validVersion   <- specimen.requireVersion(event.getRemoved.getVersion)
      } yield specimen

    logIfError(event, v)

    v.map(specimenRepository.remove(_))
    ()
  }

  def update(cmd: SpecimenModifyCommand)
            (fn: (CollectionEvent, Specimen) => DomainValidation[SpecimenEvent])
      : DomainValidation[SpecimenEvent] = {
    ???
    // val specimenId = SpecimenId(cmd.id)
    // val collectionEventId = CollectionEventId(cmd.collectionEventId)

    // for {
    //   specimen     <- specimenRepository.getByKey(specimenId)
    //   cevent       <- collectionEventRepository.getByKey(collectionEventId)
    //   validVersion <- cevent.requireVersion(cmd.expectedVersion)
    //   event        <- fn(cevent, specimen)
    // } yield event
  }

  private def validEventType(eventType: Boolean): DomainValidation[Boolean] =
    if (eventType) true.success
    else s"invalid event type".failureNel

  private def logIfError[T](event: SpecimenEvent, validation: DomainValidation[T]) =
      validation.leftMap { err =>
        log.error(s"event: $event: ${err.list.toList.mkString}")
      }

}
