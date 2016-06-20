package org.biobank.service.centres

import akka.actor._
import akka.persistence.{ SnapshotOffer }
import javax.inject.Inject
//import org.biobank.TestData
import org.biobank.domain.Location
import org.biobank.domain.centre._
import org.biobank.domain.participants.SpecimenId
import org.biobank.domain.{ DomainValidation }
import org.biobank.infrastructure.command.ShipmentCommands._
import org.biobank.infrastructure.command.ShipmentSpecimenCommands._
import org.biobank.infrastructure.event.ShipmentEvents._
import org.biobank.infrastructure.event.ShipmentSpecimenEvents._
import org.biobank.service.Processor
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

object ShipmentsProcessor {

  def props = Props[ShipmentsProcessor]

}

/**
 * Handles commands related to shipments.
 */
class ShipmentsProcessor @Inject() (val shipmentRepository:         ShipmentRepository,
                                    val shipmentSpecimenRepository: ShipmentSpecimenRepository,
                                    val centreRepository:           CentreRepository)
    extends Processor
    with ShipmentValidations {

  override def persistenceId = "shipments-processor-id"

  case class SnapshotState(shipments: Set[Shipment],
                           shipmentSpecimens: Set[ShipmentSpecimen])

  val receiveRecover: Receive = {
    case event: ShipmentEvent => event.eventType match {
      case et: ShipmentEvent.EventType.Added                 => applyAddedEvent(event)
      case et: ShipmentEvent.EventType.CourierNameUpdated    => applyCourierNameUpdatedEvent(event)
      case et: ShipmentEvent.EventType.TrackingNumberUpdated => applyTrackingNumberUpdatedEvent(event)
      case et: ShipmentEvent.EventType.FromLocationUpdated   => applyFromLocationUpdatedEvent(event)
      case et: ShipmentEvent.EventType.ToLocationUpdated     => applyToLocationUpdatedEvent(event)
      case et: ShipmentEvent.EventType.Packed                => applyPackedEvent(event)
      case et: ShipmentEvent.EventType.Sent                  => applySentEvent(event)
      case et: ShipmentEvent.EventType.Received              => applyReceivedEvent(event)
      case et: ShipmentEvent.EventType.Unpacked              => applyUnpackedEvent(event)
      case et: ShipmentEvent.EventType.Lost                  => applyLostEvent(event)

      case event => log.error(s"event not handled: $event")
    }

    case event: ShipmentSpecimenEvent => event.eventType match {
      case et: ShipmentSpecimenEvent.EventType.Added            => applySpecimenAddedEvent(event)
      case et: ShipmentSpecimenEvent.EventType.ContainerUpdated => applySpecimenContainerAddedEvent(event)
      case et: ShipmentSpecimenEvent.EventType.Received         => applySpecimenReceivedEvent(event)
      case et: ShipmentSpecimenEvent.EventType.Missing          => applySpecimenMissingEvent(event)
      case et: ShipmentSpecimenEvent.EventType.Extra            => applySpecimenExtraEvent(event)

      case event => log.error(s"event not handled: $event")
    }


    case SnapshotOffer(_, snapshot: SnapshotState) =>
      snapshot.shipments.foreach{ shipmentRepository.put(_) }
      snapshot.shipmentSpecimens.foreach{ shipmentSpecimenRepository.put(_) }
  }

  val receiveCommand: Receive = {

    case cmd: AddShipmentCmd =>
      process(addCmdToEvent(cmd))(applyAddedEvent)

    case cmd: UpdateShipmentCourierNameCmd     =>
      processUpdateCmd(cmd, updateCourierNameCmdToEvent, applyCourierNameUpdatedEvent)

    case cmd: UpdateShipmentTrackingNumberCmd =>
      processUpdateCmd(cmd, updateTrackingNumberCmdToEvent, applyTrackingNumberUpdatedEvent)

    case cmd: UpdateShipmentFromLocationCmd =>
      processUpdateCmd(cmd, updateFromLocationCmdToEvent, applyFromLocationUpdatedEvent)

    case cmd: UpdateShipmentToLocationCmd =>
      processUpdateCmd(cmd, updateToLocationCmdToEvent, applyToLocationUpdatedEvent)

    case cmd: ShipmentPackedCmd =>
      processUpdateCmd(cmd, packedCmdToEvent, applyPackedEvent)

    case cmd: ShipmentSentCmd =>
      processUpdateCmd(cmd, sentCmdToEvent, applySentEvent)

    case cmd: ShipmentReceivedCmd =>
      processUpdateCmd(cmd, recivedCmdToEvent, applyReceivedEvent)

    case cmd: ShipmentUnpackedCmd =>
      processUpdateCmd(cmd, unpackedCmdToEvent, applyUnpackedEvent)

    case cmd: ShipmentLostCmd =>
      processUpdateCmd(cmd, lostCmdToEvent, applyLostEvent)

    case cmd: ShipmentAddSpecimenCmd =>
      process(addSpecimenCmdToEvent(cmd))(applySpecimenAddedEvent)

    case cmd: ShipmentSpecimenUpdateContainerCmd =>
      processSpecimenUpdateCmd(cmd, updateSpecimenContainerCmdToEvent, applySpecimenContainerAddedEvent)

    case cmd: ShipmentSpecimenReceivedCmd =>
      processSpecimenUpdateCmd(cmd, specimenReceivedCmdToEvent, applySpecimenReceivedEvent)

    case cmd: ShipmentSpecimenMissingCmd =>
      processSpecimenUpdateCmd(cmd, specimenMissingCmdToEvent, applySpecimenMissingEvent)

    case cmd: ShipmentSpecimenExtraCmd =>
      processSpecimenUpdateCmd(cmd, specimenExtraCmdToEvent, applySpecimenExtraEvent)

    case "snap" =>
      saveSnapshot(SnapshotState(shipmentRepository.getValues.toSet,
                                 shipmentSpecimenRepository.getValues.toSet))
      stash()

    case cmd => log.error(s"shipmentsProcessor: message not handled: $cmd")
  }

  private def addCmdToEvent(cmd: AddShipmentCmd) = {
    for {
      id         <- validNewIdentity(shipmentRepository.nextIdentity, shipmentRepository)
      fromCentre <- centreRepository.getByLocationId(cmd.fromLocationId)
      toCentre   <- centreRepository.getByLocationId(cmd.toLocationId)
      shipment   <- Shipment.create(id             = id,
                                    version        = 0L,
                                    state          = ShipmentState.Created,
                                    courierName    = cmd.courierName,
                                    trackingNumber = cmd.trackingNumber,
                                    fromLocationId = cmd.fromLocationId,
                                    toLocationId   = cmd.toLocationId,
                                    timePacked     = None,
                                    timeSent       = None,
                                    timeReceived   = None,
                                    timeUnpacked   = None)
    } yield ShipmentEvent(id.id).update(
      _.userId               := cmd.userId,
      _.time                 := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.added.courierName    := shipment.courierName,
      _.added.trackingNumber := shipment.trackingNumber,
      _.added.fromLocationId := shipment.fromLocationId,
      _.added.toLocationId   := shipment.toLocationId)
  }

  private def updateCourierNameCmdToEvent(cmd:      UpdateShipmentCourierNameCmd,
                                          shipment: Shipment): DomainValidation[ShipmentEvent] = {
    shipment.withCourier(cmd.courierName).map { _ =>
      ShipmentEvent(shipment.id.id).update(
        _.userId                         := cmd.userId,
        _.time                           := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.courierNameUpdated.version     := cmd.expectedVersion,
        _.courierNameUpdated.courierName := cmd.courierName)
    }
  }

  private def updateTrackingNumberCmdToEvent(cmd: UpdateShipmentTrackingNumberCmd,
                                             shipment: Shipment): DomainValidation[ShipmentEvent] = {
    shipment.withTrackingNumber(cmd.trackingNumber).map { _ =>
      ShipmentEvent(shipment.id.id).update(
        _.userId                               := cmd.userId,
        _.time                                 := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.trackingNumberUpdated.version        := cmd.expectedVersion,
        _.trackingNumberUpdated.trackingNumber := cmd.trackingNumber)
    }
  }

  private def updateFromLocationCmdToEvent(cmd: UpdateShipmentFromLocationCmd,
                                           shipment: Shipment): DomainValidation[ShipmentEvent] = {
    for {
      location    <- getLocation(cmd.locationId)
      newShipment <- shipment.withFromLocation(location)
    } yield ShipmentEvent(shipment.id.id).update(
      _.userId                         := cmd.userId,
      _.time                           := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.fromLocationUpdated.version    := cmd.expectedVersion,
      _.fromLocationUpdated.locationId := cmd.locationId)
  }

  private def updateToLocationCmdToEvent(cmd: UpdateShipmentToLocationCmd,
                                         shipment: Shipment): DomainValidation[ShipmentEvent] = {
    for {
      location    <- getLocation(cmd.locationId)
      newShipment <- shipment.withToLocation(location)
    } yield ShipmentEvent(shipment.id.id).update(
      _.userId                       := cmd.userId,
      _.time                         := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.toLocationUpdated.version    := cmd.expectedVersion,
      _.toLocationUpdated.locationId := cmd.locationId)
  }

  private def packedCmdToEvent(cmd: ShipmentPackedCmd,
                               shipment: Shipment): DomainValidation[ShipmentEvent] = {
    shipment.packed(cmd.time).map { _ =>
      ShipmentEvent(shipment.id.id).update(
        _.userId                 := cmd.userId,
        _.time                   := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.packed.version         := cmd.expectedVersion,
        _.packed.stateChangeTime := ISODateTimeFormat.dateTime.print(cmd.time))
    }
  }

  private def sentCmdToEvent(cmd: ShipmentSentCmd,
                             shipment: Shipment): DomainValidation[ShipmentEvent] = {
    shipment.sent(cmd.time).map { _ =>
      ShipmentEvent(shipment.id.id).update(
        _.userId               := cmd.userId,
        _.time                 := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.sent.version         := cmd.expectedVersion,
        _.sent.stateChangeTime := ISODateTimeFormat.dateTime.print(cmd.time))
    }
  }

  private def recivedCmdToEvent(cmd: ShipmentReceivedCmd,
                                shipment: Shipment): DomainValidation[ShipmentEvent] = {
    shipment.received(cmd.time).map { _ =>
      ShipmentEvent(shipment.id.id).update(
        _.userId                   := cmd.userId,
        _.time                     := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.received.version         := cmd.expectedVersion,
        _.received.stateChangeTime := ISODateTimeFormat.dateTime.print(cmd.time))
    }
  }

  private def unpackedCmdToEvent(cmd: ShipmentUnpackedCmd,
                                   shipment: Shipment): DomainValidation[ShipmentEvent] = {
    shipment.unpacked(cmd.time).map { _ =>
      ShipmentEvent(shipment.id.id).update(
        _.userId                   := cmd.userId,
        _.time                     := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.unpacked.version         := cmd.expectedVersion,
        _.unpacked.stateChangeTime := ISODateTimeFormat.dateTime.print(cmd.time))
    }
  }

  private def lostCmdToEvent(cmd: ShipmentLostCmd,
                             shipment: Shipment): DomainValidation[ShipmentEvent] = {
    shipment.lost.map { _ =>
      ShipmentEvent(shipment.id.id).update(
        _.userId       := cmd.userId,
        _.time         := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.lost.version := cmd.expectedVersion)
    }
  }

  private def addSpecimenCmdToEvent(cmd : ShipmentAddSpecimenCmd)
      : DomainValidation[ShipmentSpecimenEvent] = {
    for {
      id <- validNewIdentity(shipmentSpecimenRepository.nextIdentity, shipmentSpecimenRepository)
      ss <- ShipmentSpecimen.create(id                  = id,
                                    version             = 0L,
                                    shipmentId          = ShipmentId(cmd.shipmentId),
                                    specimenId          = SpecimenId(cmd.specimenId),
                                    state               = ShipmentItemState.Present,
                                    shipmentContainerId = cmd.shipmentContainerId.map(ShipmentContainerId.apply))
    } yield ShipmentSpecimenEvent(id.id).update(
        _.userId                            := cmd.userId,
        _.time                              := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.added.shipmentId                  := cmd.shipmentId,
        _.added.specimenId                  := cmd.specimenId,
        _.added.optionalShipmentContainerId := cmd.shipmentContainerId
      )
  }

  private def updateSpecimenContainerCmdToEvent(cmd :             ShipmentSpecimenUpdateContainerCmd,
                                                shipment:         Shipment,
                                                shipmentSpecimen: ShipmentSpecimen)
      : DomainValidation[ShipmentSpecimenEvent] = {
    val shipmentContainerId = cmd.shipmentContainerId.map(ShipmentContainerId.apply)

    shipmentSpecimen.withShipmentContainer(shipmentContainerId) map { _ =>
      ShipmentSpecimenEvent(cmd.id).update(
        _.userId                                       := cmd.userId,
        _.time                                         := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.containerUpdated.version                     := cmd.expectedVersion,
        _.containerUpdated.optionalShipmentContainerId := cmd.shipmentContainerId)
    }
  }

  private def specimenReceivedCmdToEvent(cmd :             ShipmentSpecimenReceivedCmd,
                                         shipment:         Shipment,
                                         shipmentSpecimen: ShipmentSpecimen)
      : DomainValidation[ShipmentSpecimenEvent] = {
    shipmentSpecimen.received map { _ =>
      ShipmentSpecimenEvent(cmd.id).update(
        _.userId           := cmd.userId,
        _.time             := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.received.version := cmd.expectedVersion)
    }
  }

  private def specimenMissingCmdToEvent(cmd :             ShipmentSpecimenMissingCmd,
                                        shipment:         Shipment,
                                        shipmentSpecimen: ShipmentSpecimen)
      : DomainValidation[ShipmentSpecimenEvent] = {
    shipmentSpecimen.missing map { _ =>
      ShipmentSpecimenEvent(cmd.id).update(
        _.userId          := cmd.userId,
        _.time            := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.missing.version := cmd.expectedVersion)
    }
  }

  private def specimenExtraCmdToEvent(cmd :             ShipmentSpecimenExtraCmd,
                                      shipment:         Shipment,
                                      shipmentSpecimen: ShipmentSpecimen)
      : DomainValidation[ShipmentSpecimenEvent] = {
    shipmentSpecimen.extra map { _ =>
      ShipmentSpecimenEvent(cmd.id).update(
        _.userId        := cmd.userId,
        _.time          := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.extra.version := cmd.expectedVersion)
    }
  }

  private def applyAddedEvent(event: ShipmentEvent) = {
    if (!event.eventType.isAdded) {
      log.error(s"invalid event type: $event")
    } else {
      val addedEvent = event.getAdded
      val eventTime  = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)
      val add = Shipment.create(id             = ShipmentId(event.id),
                                version        = 0L,
                                state          = ShipmentState.Created,
                                courierName    = addedEvent.getCourierName,
                                trackingNumber = addedEvent.getTrackingNumber,
                                fromLocationId = addedEvent.getFromLocationId,
                                toLocationId   = addedEvent.getToLocationId,
                                timePacked     = None,
                                timeSent       = None,
                                timeReceived   = None,
                                timeUnpacked   = None)
        .map { s => shipmentRepository.put(s.copy(timeAdded = eventTime)) }

      if (add.isFailure) {
        log.error(s"could not add shipment from event: $event, err: $add")
      }
    }
  }

  private def applyCourierNameUpdatedEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isCourierNameUpdated,
                           event.getCourierNameUpdated.getVersion) { (shipment, _, time) =>
      shipment.withCourier(event.getCourierNameUpdated.getCourierName).map { s =>
        shipmentRepository.put(s.copy(timeModified = Some(time)))
      }
    }
  }

  private def applyTrackingNumberUpdatedEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isTrackingNumberUpdated,
                           event.getTrackingNumberUpdated.getVersion) { (shipment, _, time) =>
      shipment.withTrackingNumber(event.getTrackingNumberUpdated.getTrackingNumber).map { s =>
        shipmentRepository.put(s.copy(timeModified = Some(time)))
      }
    }
  }

  private def applyFromLocationUpdatedEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isFromLocationUpdated,
                           event.getFromLocationUpdated.getVersion) { (shipment, _, time) =>
      for {
        location <- getLocation(event.getFromLocationUpdated.getLocationId)
        updated  <- shipment.withFromLocation(location)
      } yield {
        shipmentRepository.put(updated.copy(timeModified = Some(time)))
      }
    }
  }

  private def applyToLocationUpdatedEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isToLocationUpdated,
                           event.getToLocationUpdated.getVersion) { (shipment, _, time) =>
      for {
        location <- getLocation(event.getToLocationUpdated.getLocationId)
        updated  <- shipment.withToLocation(location)
      } yield {
        shipmentRepository.put(updated.copy(timeModified = Some(time)))
      }
    }
  }

  private def applyPackedEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isPacked,
                           event.getPacked.getVersion) { (shipment, _, time) =>
      val stateChangeTime = ISODateTimeFormat.dateTime.parseDateTime(event.getPacked.getStateChangeTime)
      shipment.packed(stateChangeTime).map { s =>
        shipmentRepository.put(s.copy(timeModified = Some(time)))
      }
    }
  }

  private def applySentEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isSent,
                           event.getSent.getVersion) { (shipment, _, time) =>
      val stateChangeTime = ISODateTimeFormat.dateTime.parseDateTime(event.getSent.getStateChangeTime)
      shipment.sent(stateChangeTime).map { s =>
        shipmentRepository.put(s.copy(timeModified = Some(time)))
      }
    }
  }

  private def applyReceivedEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isReceived,
                           event.getReceived.getVersion) { (shipment, _, time) =>
      val stateChangeTime = ISODateTimeFormat.dateTime.parseDateTime(event.getReceived.getStateChangeTime)
      shipment.received(stateChangeTime).map { s =>
        shipmentRepository.put(s.copy(timeModified = Some(time)))
      }
    }
  }

  private def applyUnpackedEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isUnpacked,
                           event.getUnpacked.getVersion) { (shipment, _, time) =>
      val stateChangeTime = ISODateTimeFormat.dateTime.parseDateTime(event.getUnpacked.getStateChangeTime)
      shipment.unpacked(stateChangeTime).map { s =>
        shipmentRepository.put(s.copy(timeModified = Some(time)))
      }
    }
  }

  private def applyLostEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isLost,
                           event.getLost.getVersion) { (shipment, _, time) =>
      shipment.lost.map { s =>
        shipmentRepository.put(s.copy(timeModified = Some(time)))
      }
    }
  }

  private def applySpecimenAddedEvent(event: ShipmentSpecimenEvent): Unit = {
    if (!event.eventType.isAdded) {
      log.error(s"invalid event type: $event")
    } else {
      val addedEvent          = event.getAdded
      val eventTime           = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)
      val shipmentContainerId = addedEvent.shipmentContainerId.map(ShipmentContainerId.apply)

      val add = ShipmentSpecimen.create(id                  = ShipmentSpecimenId(event.id),
                                        version             = 0L,
                                        shipmentId          = ShipmentId(addedEvent.getShipmentId),
                                        specimenId          = SpecimenId(addedEvent.getSpecimenId),
                                        state               = ShipmentItemState.Present,
                                        shipmentContainerId = shipmentContainerId)
        .map { s => shipmentSpecimenRepository.put(s.copy(timeAdded = eventTime)) }

      if (add.isFailure) {
        log.error(s"could not add shipment specimen from event: $event, err: $add")
      }
    }
  }

  private def applySpecimenContainerAddedEvent(event: ShipmentSpecimenEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isContainerUpdated,
                           event.getContainerUpdated.getVersion) { (shipmentSpecimen, _, time) =>
      val shipmentContainerId = event.getContainerUpdated.shipmentContainerId.map(ShipmentContainerId.apply)
      shipmentSpecimen.withShipmentContainer(shipmentContainerId).map { s =>
        shipmentSpecimenRepository.put(s.copy(timeModified = Some(time)))
      }
    }
  }

  private def applySpecimenReceivedEvent(event: ShipmentSpecimenEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isReceived,
                           event.getReceived.getVersion) { (shipmentSpecimen, _, time) =>
      shipmentSpecimen.received.map { s =>
        shipmentSpecimenRepository.put(s.copy(timeModified = Some(time)))
      }
    }
  }

  private def applySpecimenMissingEvent(event: ShipmentSpecimenEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isMissing,
                           event.getMissing.getVersion) { (shipmentSpecimen, _, time) =>
      shipmentSpecimen.missing.map { s =>
        shipmentSpecimenRepository.put(s.copy(timeModified = Some(time)))
      }
    }
  }

  private def applySpecimenExtraEvent(event: ShipmentSpecimenEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isExtra,
                           event.getExtra.getVersion) { (shipmentSpecimen, _, time) =>
      shipmentSpecimen.extra.map { s =>
        shipmentSpecimenRepository.put(s.copy(timeModified = Some(time)))
      }
    }
  }

  def processUpdateCmd[T <: ShipmentModifyCommand]
    (cmd: T,
     cmdToEvent: (T, Shipment) => DomainValidation[ShipmentEvent],
     applyEvent: ShipmentEvent => Unit): Unit = {
    var event = for {
        shipment     <- shipmentRepository.getByKey(ShipmentId(cmd.id))
        validVersion <- shipment.requireVersion(cmd.expectedVersion)
        event        <- cmdToEvent(cmd, shipment)
      } yield event
    process(event)(applyEvent)
  }

  def processSpecimenUpdateCmd[T <: ShipmentSpecimenModifyCommand]
    (cmd: T,
     cmdToEvent: (T, Shipment, ShipmentSpecimen) => DomainValidation[ShipmentSpecimenEvent],
     applyEvent: ShipmentSpecimenEvent => Unit): Unit = {
    var event = for {
        shipmentSpecimen <- shipmentSpecimenRepository.getByKey(ShipmentSpecimenId(cmd.id))
        shipment         <- shipmentRepository.getByKey(shipmentSpecimen.shipmentId)
        validVersion     <- shipment.requireVersion(cmd.expectedVersion)
        event            <- cmdToEvent(cmd, shipment, shipmentSpecimen)
      } yield event
    process(event)(applyEvent)
  }

  private def onValidEventAndVersion(event:        ShipmentEvent,
                                     eventType:    Boolean,
                                     eventVersion: Long)
                                    (applyEvent:  (Shipment,
                                                   ShipmentEvent,
                                                   DateTime) => DomainValidation[Shipment])
      : Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      shipmentRepository.getByKey(ShipmentId(event.id)).fold(
        err => log.error(s"shipment from event does not exist: $err"),
        shipment => {
          if (shipment.version != eventVersion) {
            log.error(s"event version check failed: shipment version: ${shipment.version}, event: $event")
          } else {
            val eventTime = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)
            val update = applyEvent(shipment, event, eventTime)
            if (update.isFailure) {
              log.error(s"shipment update from event failed: $update")
            }
          }
        }
      )
    }
  }

  private def onValidEventAndVersion(event:        ShipmentSpecimenEvent,
                                     eventType:    Boolean,
                                     eventVersion: Long)
                                    (applyEvent:  (ShipmentSpecimen,
                                                   ShipmentSpecimenEvent,
                                                   DateTime) => DomainValidation[ShipmentSpecimen])
      : Unit = {
    if (!eventType) {
      log.error(s"invalid event type: $event")
    } else {
      shipmentSpecimenRepository.getByKey(ShipmentSpecimenId(event.id)).fold(
        err => log.error(s"shipmentSpecimen from event does not exist: $err"),
        shipmentSpecimen => {
          if (shipmentSpecimen.version != eventVersion) {
            log.error(s"event version check failed: shipment specimen version: ${shipmentSpecimen.version}, event: $event")
          } else {
            val eventTime = ISODateTimeFormat.dateTime.parseDateTime(event.getTime)
            val update = applyEvent(shipmentSpecimen, event, eventTime)

            if (update.isFailure) {
              log.error(s"shipment specimen update from event failed: $update")
            }
          }
        }
      )
    }
  }

  private def getLocation(locationId: String): DomainValidation[Location] = {
    for {
      centre      <- centreRepository.getByLocationId(locationId)
      location    <- centre.locationWithId(locationId)
    } yield location
  }

}
