package org.biobank.service.centres

import akka.actor._
import akka.persistence.{ SnapshotOffer }
import javax.inject.Inject
import org.biobank.domain.centre._
import org.biobank.domain.participants.SpecimenId
import org.biobank.infrastructure.command.ShipmentCommands._
import org.biobank.infrastructure.command.ShipmentSpecimenCommands._
import org.biobank.infrastructure.event.ShipmentEvents._
import org.biobank.infrastructure.event.ShipmentSpecimenEvents._
import org.biobank.service.{Processor, ServiceError, ServiceValidation}
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

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
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
      case et: ShipmentEvent.EventType.Removed               => applyRemoveEvent(event)

      case event => log.error(s"event not handled: $event")
    }

    case event: ShipmentSpecimenEvent => event.eventType match {
      case et: ShipmentSpecimenEvent.EventType.Added            => applySpecimenAddedEvent(event)
      case et: ShipmentSpecimenEvent.EventType.Removed          => applySpecimenRemovedEvent(event)
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

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
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

    case cmd: ShipmentRemoveCmd =>
      processUpdateCmd(cmd, removeCmdToEvent, applyRemoveEvent)

    case cmd: ShipmentSpecimenAddCmd =>
      process(addSpecimenCmdToEvent(cmd))(applySpecimenAddedEvent)

    case cmd: ShipmentSpecimenRemoveCmd =>
      processSpecimenUpdateCmd(cmd, removeSpecimenCmdToEvent, applySpecimenRemovedEvent)

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
                                    fromCentreId   = fromCentre.id,
                                    fromLocationId = cmd.fromLocationId,
                                    toCentreId     = toCentre.id,
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
      _.added.fromCentreId   := shipment.fromCentreId.id,
      _.added.fromLocationId := shipment.fromLocationId,
      _.added.toCentreId     := shipment.toCentreId.id,
      _.added.toLocationId   := shipment.toLocationId)
  }

  private def updateCourierNameCmdToEvent(cmd:      UpdateShipmentCourierNameCmd,
                                          shipment: Shipment): ServiceValidation[ShipmentEvent] = {
    for {
      isCreated <- shipment.isCreated
      updated   <- shipment.withCourier(cmd.courierName)
    } yield ShipmentEvent(shipment.id.id).update(
      _.userId                         := cmd.userId,
      _.time                           := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.courierNameUpdated.version     := cmd.expectedVersion,
      _.courierNameUpdated.courierName := cmd.courierName)
  }

  private def updateTrackingNumberCmdToEvent(cmd: UpdateShipmentTrackingNumberCmd,
                                             shipment: Shipment): ServiceValidation[ShipmentEvent] = {
    for {
      isCreated <- shipment.isCreated
      updated   <- shipment.withTrackingNumber(cmd.trackingNumber)
    } yield ShipmentEvent(shipment.id.id).update(
      _.userId                               := cmd.userId,
      _.time                                 := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.trackingNumberUpdated.version        := cmd.expectedVersion,
      _.trackingNumberUpdated.trackingNumber := cmd.trackingNumber)
  }

  private def updateFromLocationCmdToEvent(cmd: UpdateShipmentFromLocationCmd,
                                           shipment: Shipment): ServiceValidation[ShipmentEvent] = {
    for {
      isCreated   <- shipment.isCreated
      centre      <- centreRepository.getByLocationId(cmd.locationId)
      location    <- centre.locationWithId(cmd.locationId)
      newShipment <- shipment.withFromLocation(centre, location)
    } yield ShipmentEvent(shipment.id.id).update(
      _.userId                         := cmd.userId,
      _.time                           := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.fromLocationUpdated.version    := cmd.expectedVersion,
      _.fromLocationUpdated.locationId := cmd.locationId)
  }

  private def updateToLocationCmdToEvent(cmd: UpdateShipmentToLocationCmd,
                                         shipment: Shipment): ServiceValidation[ShipmentEvent] = {
    for {
      isCreated   <- shipment.isCreated
      centre      <- centreRepository.getByLocationId(cmd.locationId)
      location    <- centre.locationWithId(cmd.locationId)
      newShipment <- shipment.withToLocation(centre, location)
    } yield ShipmentEvent(shipment.id.id).update(
      _.userId                       := cmd.userId,
      _.time                         := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.toLocationUpdated.version    := cmd.expectedVersion,
      _.toLocationUpdated.locationId := cmd.locationId)
  }

  private def packedCmdToEvent(cmd: ShipmentPackedCmd,
                               shipment: Shipment): ServiceValidation[ShipmentEvent] = {
    shipment.packed(cmd.time).map { _ =>
      ShipmentEvent(shipment.id.id).update(
        _.userId                 := cmd.userId,
        _.time                   := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.packed.version         := cmd.expectedVersion,
        _.packed.stateChangeTime := ISODateTimeFormat.dateTime.print(cmd.time))
    }
  }

  private def sentCmdToEvent(cmd: ShipmentSentCmd,
                             shipment: Shipment): ServiceValidation[ShipmentEvent] = {
    shipment.sent(cmd.time).map { _ =>
      ShipmentEvent(shipment.id.id).update(
        _.userId               := cmd.userId,
        _.time                 := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.sent.version         := cmd.expectedVersion,
        _.sent.stateChangeTime := ISODateTimeFormat.dateTime.print(cmd.time))
    }
  }

  private def recivedCmdToEvent(cmd: ShipmentReceivedCmd,
                                shipment: Shipment): ServiceValidation[ShipmentEvent] = {
    shipment.received(cmd.time).map { _ =>
      ShipmentEvent(shipment.id.id).update(
        _.userId                   := cmd.userId,
        _.time                     := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.received.version         := cmd.expectedVersion,
        _.received.stateChangeTime := ISODateTimeFormat.dateTime.print(cmd.time))
    }
  }

  private def unpackedCmdToEvent(cmd: ShipmentUnpackedCmd,
                                   shipment: Shipment): ServiceValidation[ShipmentEvent] = {
    shipment.unpacked(cmd.time).map { _ =>
      ShipmentEvent(shipment.id.id).update(
        _.userId                   := cmd.userId,
        _.time                     := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.unpacked.version         := cmd.expectedVersion,
        _.unpacked.stateChangeTime := ISODateTimeFormat.dateTime.print(cmd.time))
    }
  }

  private def lostCmdToEvent(cmd: ShipmentLostCmd,
                             shipment: Shipment): ServiceValidation[ShipmentEvent] = {
    shipment.lost.map { _ =>
      ShipmentEvent(shipment.id.id).update(
        _.userId       := cmd.userId,
        _.time         := ISODateTimeFormat.dateTime.print(DateTime.now),
        _.lost.version := cmd.expectedVersion)
    }
  }

  private def removeCmdToEvent(cmd: ShipmentRemoveCmd, shipment: Shipment)
      : ServiceValidation[ShipmentEvent] = {
    val shipmentId = ShipmentId(cmd.id)
    for {
      shipment     <- shipmentRepository.getByKey(shipmentId)
      isCreated    <- shipment.isCreated
      hasSpecimens <- {
        if (shipmentSpecimenRepository.allForShipment(shipmentId).isEmpty) true.successNel[String]
        else ServiceError(s"shipment has specimens, remove specimens first").failureNel[Boolean]
      }
    } yield ShipmentEvent(shipment.id.id).update(
      _.userId          := cmd.userId,
      _.time            := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.removed.version := cmd.expectedVersion)
  }

  private def addSpecimenCmdToEvent(cmd : ShipmentSpecimenAddCmd)
      : ServiceValidation[ShipmentSpecimenEvent] = {
    val shipmentId = ShipmentId(cmd.shipmentId)
    for {
      shipment  <- shipmentRepository.getByKey(shipmentId)
      isCreated <- shipment.isCreated
      id        <- validNewIdentity(shipmentSpecimenRepository.nextIdentity, shipmentSpecimenRepository)
      ss        <- ShipmentSpecimen.create(id                  = id,
                                           version             = 0L,
                                           shipmentId          = shipment.id,
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

  private def removeSpecimenCmdToEvent(cmd:              ShipmentSpecimenRemoveCmd,
                                       shipment:         Shipment,
                                       shipmentSpecimen: ShipmentSpecimen)
      : ServiceValidation[ShipmentSpecimenEvent] = {
    val shipmentId = ShipmentId(cmd.shipmentId)
    for {
      shipment  <- shipmentRepository.getByKey(shipmentId)
      isCreated <- shipment.isCreated
      isPresent <- shipmentSpecimen.isStatePresent
    } yield ShipmentSpecimenEvent(shipmentSpecimen.id.id).update(
      _.userId          := cmd.userId,
      _.time            := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.removed.version := cmd.expectedVersion)
  }

  private def updateSpecimenContainerCmdToEvent(cmd :             ShipmentSpecimenUpdateContainerCmd,
                                                shipment:         Shipment,
                                                shipmentSpecimen: ShipmentSpecimen)
      : ServiceValidation[ShipmentSpecimenEvent] = {
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
      : ServiceValidation[ShipmentSpecimenEvent] = {
    for {
      isUnpacked <- shipment.isUnpacked
      received   <- shipmentSpecimen.received
    } yield ShipmentSpecimenEvent(cmd.id).update(
      _.userId           := cmd.userId,
      _.time             := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.received.version := cmd.expectedVersion)
  }

  private def specimenMissingCmdToEvent(cmd :             ShipmentSpecimenMissingCmd,
                                        shipment:         Shipment,
                                        shipmentSpecimen: ShipmentSpecimen)
      : ServiceValidation[ShipmentSpecimenEvent] = {
    for {
      isUnpacked <- shipment.isUnpacked
      missing    <- shipmentSpecimen.missing
    } yield ShipmentSpecimenEvent(cmd.id).update(
      _.userId          := cmd.userId,
      _.time            := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.missing.version := cmd.expectedVersion)
  }

  private def specimenExtraCmdToEvent(cmd :             ShipmentSpecimenExtraCmd,
                                      shipment:         Shipment,
                                      shipmentSpecimen: ShipmentSpecimen)
      : ServiceValidation[ShipmentSpecimenEvent] = {
    for {
      isUnpacked <- shipment.isUnpacked
      extra      <- shipmentSpecimen.extra
    } yield ShipmentSpecimenEvent(cmd.id).update(
      _.userId        := cmd.userId,
      _.time          := ISODateTimeFormat.dateTime.print(DateTime.now),
      _.extra.version := cmd.expectedVersion)
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
                                fromCentreId   = CentreId(addedEvent.getFromCentreId),
                                fromLocationId = addedEvent.getFromLocationId,
                                toCentreId     = CentreId(addedEvent.getToCentreId),
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
      val v = shipment.withCourier(event.getCourierNameUpdated.getCourierName)
      v.foreach { s => shipmentRepository.put(s.copy(timeModified = Some(time))) }
      v.map(_ => true)
    }
  }

  private def applyTrackingNumberUpdatedEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isTrackingNumberUpdated,
                           event.getTrackingNumberUpdated.getVersion) { (shipment, _, time) =>
      val v = shipment.withTrackingNumber(event.getTrackingNumberUpdated.getTrackingNumber)
      v.foreach { s => shipmentRepository.put(s.copy(timeModified = Some(time))) }
      v.map(_ => true)
    }
  }

  private def applyFromLocationUpdatedEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isFromLocationUpdated,
                           event.getFromLocationUpdated.getVersion) { (shipment, _, time) =>
      val locationId = event.getFromLocationUpdated.getLocationId
      val v = for {
          centre   <- centreRepository.getByLocationId(locationId)
          location <- centre.locationWithId(locationId)
          updated  <- shipment.withFromLocation(centre, location)
        } yield updated

      v.foreach(s => shipmentRepository.put(s.copy(timeModified = Some(time))))
      v.map(_ => true)
    }
  }

  private def applyToLocationUpdatedEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isToLocationUpdated,
                           event.getToLocationUpdated.getVersion) { (shipment, _, time) =>
      val locationId = event.getToLocationUpdated.getLocationId
      val v = for {
          centre   <- centreRepository.getByLocationId(locationId)
          location <- centre.locationWithId(locationId)
          updated  <- shipment.withToLocation(centre, location)
        } yield updated
      v.foreach(s => shipmentRepository.put(s.copy(timeModified = Some(time))))
      v.map(_ => true)
    }
  }

  private def applyPackedEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isPacked,
                           event.getPacked.getVersion) { (shipment, _, time) =>
      val stateChangeTime = ISODateTimeFormat.dateTime.parseDateTime(event.getPacked.getStateChangeTime)
      val v = shipment.packed(stateChangeTime)
      v.foreach { s => shipmentRepository.put(s.copy(timeModified = Some(time))) }
      v.map(_ => true)
    }
  }

  private def applySentEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isSent,
                           event.getSent.getVersion) { (shipment, _, time) =>
      val stateChangeTime = ISODateTimeFormat.dateTime.parseDateTime(event.getSent.getStateChangeTime)
      val v = shipment.sent(stateChangeTime)
      v.foreach { s => shipmentRepository.put(s.copy(timeModified = Some(time))) }
      v.map(_ => true)
    }
  }

  private def applyReceivedEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isReceived,
                           event.getReceived.getVersion) { (shipment, _, time) =>
      val stateChangeTime = ISODateTimeFormat.dateTime.parseDateTime(event.getReceived.getStateChangeTime)
      val v = shipment.received(stateChangeTime)
      v.foreach { s => shipmentRepository.put(s.copy(timeModified = Some(time))) }
      v.map(_ => true)
    }
  }

  private def applyUnpackedEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isUnpacked,
                           event.getUnpacked.getVersion) { (shipment, _, time) =>
      val stateChangeTime = ISODateTimeFormat.dateTime.parseDateTime(event.getUnpacked.getStateChangeTime)
      val v = shipment.unpacked(stateChangeTime)
      v.foreach { s => shipmentRepository.put(s.copy(timeModified = Some(time))) }
      v.map(_ => true)
    }
  }

  private def applyLostEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isLost,
                           event.getLost.getVersion) { (shipment, _, time) =>
      val v = shipment.lost
      v.foreach { s => shipmentRepository.put(s.copy(timeModified = Some(time))) }
      v.map(_ => true)
    }
  }

  private def applyRemoveEvent(event: ShipmentEvent): Unit = {
    onValidEventAndVersion(event,
                           event.eventType.isRemoved,
                           event.getRemoved.getVersion) { (shipment, _, time) =>
      shipmentRepository.remove(shipment)
      true.successNel[String]
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

  private def applySpecimenRemovedEvent(event: ShipmentSpecimenEvent): Unit = {
    onValidSpecimenEventAndVersion(event,
                                   event.eventType.isRemoved,
                                   event.getRemoved.getVersion) { (shipmentSpecimen, _, time) =>
      shipmentSpecimenRepository.remove(shipmentSpecimen)
      true.successNel[String]
    }
  }

  private def applySpecimenContainerAddedEvent(event: ShipmentSpecimenEvent): Unit = {
    onValidSpecimenEventAndVersion(event,
                                   event.eventType.isContainerUpdated,
                                   event.getContainerUpdated.getVersion) { (shipmentSpecimen, _, time) =>
      val shipmentContainerId = event.getContainerUpdated.shipmentContainerId.map(ShipmentContainerId.apply)
      val v = shipmentSpecimen.withShipmentContainer(shipmentContainerId)
      v.foreach { s => shipmentSpecimenRepository.put(s.copy(timeModified = Some(time))) }
      v.map(_ => true)
    }
  }

  private def applySpecimenReceivedEvent(event: ShipmentSpecimenEvent): Unit = {
    onValidSpecimenEventAndVersion(event,
                                   event.eventType.isReceived,
                                   event.getReceived.getVersion) { (shipmentSpecimen, _, time) =>
      val v = shipmentSpecimen.received
      v.foreach { s => shipmentSpecimenRepository.put(s.copy(timeModified = Some(time))) }
      v.map(_ => true)
    }
  }

  private def applySpecimenMissingEvent(event: ShipmentSpecimenEvent): Unit = {
    onValidSpecimenEventAndVersion(event,
                                   event.eventType.isMissing,
                                   event.getMissing.getVersion) { (shipmentSpecimen, _, time) =>
      val v = shipmentSpecimen.missing
      v.foreach { s => shipmentSpecimenRepository.put(s.copy(timeModified = Some(time))) }
      v.map(_ => true)
    }
  }

  private def applySpecimenExtraEvent(event: ShipmentSpecimenEvent): Unit = {
    onValidSpecimenEventAndVersion(event,
                                   event.eventType.isExtra,
                                   event.getExtra.getVersion) { (shipmentSpecimen, _, time) =>
      val v = shipmentSpecimen.extra
      v.foreach { s => shipmentSpecimenRepository.put(s.copy(timeModified = Some(time))) }
      v.map(_ => true)
    }
  }

  def processUpdateCmd[T <: ShipmentModifyCommand]
    (cmd: T,
     cmdToEvent: (T, Shipment) => ServiceValidation[ShipmentEvent],
     applyEvent: ShipmentEvent => Unit): Unit = {
    val event = for {
        shipment     <- shipmentRepository.getByKey(ShipmentId(cmd.id))
        validVersion <- shipment.requireVersion(cmd.expectedVersion)
        event        <- cmdToEvent(cmd, shipment)
      } yield event
    process(event)(applyEvent)
  }

  def processSpecimenUpdateCmd[T <: ShipmentSpecimenModifyCommand]
    (cmd: T,
     cmdToEvent: (T, Shipment, ShipmentSpecimen) => ServiceValidation[ShipmentSpecimenEvent],
     applyEvent: ShipmentSpecimenEvent => Unit): Unit = {
    val event = for {
        shipmentSpecimen <- shipmentSpecimenRepository.getByKey(ShipmentSpecimenId(cmd.id))
        shipment         <- shipmentRepository.getByKey(shipmentSpecimen.shipmentId)
        validVersion     <- shipmentSpecimen.requireVersion(cmd.expectedVersion)
        event            <- cmdToEvent(cmd, shipment, shipmentSpecimen)
      } yield event
    process(event)(applyEvent)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def onValidEventAndVersion(event:        ShipmentEvent,
                                     eventType:    Boolean,
                                     eventVersion: Long)
                                    (applyEvent:  (Shipment,
                                                   ShipmentEvent,
                                                   DateTime) => ServiceValidation[Boolean])
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

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  private def onValidSpecimenEventAndVersion(event:        ShipmentSpecimenEvent,
                                             eventType:    Boolean,
                                             eventVersion: Long)
                                            (applyEvent:  (ShipmentSpecimen,
                                                           ShipmentSpecimenEvent,
                                                           DateTime) => ServiceValidation[Boolean])
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

}
