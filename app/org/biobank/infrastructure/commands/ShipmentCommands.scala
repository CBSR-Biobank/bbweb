package org.biobank.infrastructure.command

import java.time.OffsetDateTime
import org.biobank.infrastructure.command.Commands._
import play.api.libs.json._

object ShipmentCommands {

  trait ShipmentCommand extends Command with HasSessionUserId

  trait ShipmentModifyCommand extends ShipmentCommand with HasIdentity with HasExpectedVersion

  final case class AddShipmentCmd(sessionUserId:  String,
                                  courierName:    String,
                                  trackingNumber: String,
                                  fromLocationId: String,
                                  toLocationId:   String)
      extends ShipmentCommand

  final case class UpdateShipmentCourierNameCmd(sessionUserId:   String,
                                                id:              String, // shipment ID
                                                expectedVersion: Long,
                                                courierName:     String)
      extends ShipmentModifyCommand

  final case class UpdateShipmentTrackingNumberCmd(sessionUserId:   String,
                                                   id:              String, // shipment ID
                                                   expectedVersion: Long,
                                                   trackingNumber:  String)
      extends ShipmentModifyCommand

  final case class UpdateShipmentFromLocationCmd(sessionUserId:   String,
                                                 id:              String, // shipment ID
                                                 expectedVersion: Long,
                                                 locationId:      String)
      extends ShipmentModifyCommand

  final case class UpdateShipmentToLocationCmd(sessionUserId:   String,
                                               id:              String, // shipment ID
                                               expectedVersion: Long,
                                               locationId:      String)
      extends ShipmentModifyCommand

  final case class CreatedShipmentCmd(sessionUserId:   String,
                                      id:              String, // shipment ID
                                      expectedVersion: Long)
      extends ShipmentModifyCommand

  final case class PackShipmentCmd(sessionUserId:   String,
                                   id:              String, // shipment ID
                                   expectedVersion: Long,
                                   datetime:        OffsetDateTime)
      extends ShipmentModifyCommand

  final case class SendShipmentCmd(sessionUserId:   String,
                                   id:              String, // shipment ID
                                   expectedVersion: Long,
                                   datetime:        OffsetDateTime)
      extends ShipmentModifyCommand

  final case class ReceiveShipmentCmd(sessionUserId: String,
                                   id:               String, // shipment ID
                                   expectedVersion:  Long,
                                   datetime:         OffsetDateTime)
      extends ShipmentModifyCommand

  final case class UnpackShipmentCmd(sessionUserId:   String,
                                     id:              String, // shipment ID
                                     expectedVersion: Long,
                                     datetime:        OffsetDateTime)
      extends ShipmentModifyCommand

  final case class CompleteShipmentCmd(sessionUserId:   String,
                                       id:              String, // shipment ID
                                       expectedVersion: Long,
                                       datetime:        OffsetDateTime)
      extends ShipmentModifyCommand

  final case class LostShipmentCmd(sessionUserId:   String,
                                   id:              String, // shipment ID
                                   expectedVersion: Long)
      extends ShipmentModifyCommand

  final case class ShipmentSkipStateToSentCmd(sessionUserId:   String,
                                              id:              String, // shipment ID
                                              expectedVersion: Long,
                                              timePacked:      OffsetDateTime,
                                              timeSent:        OffsetDateTime)
      extends ShipmentModifyCommand

  final case class ShipmentSkipStateToUnpackedCmd(sessionUserId:   String,
                                                  id:              String, // shipment ID
                                                  expectedVersion: Long,
                                                  timeReceived:    OffsetDateTime,
                                                  timeUnpacked:    OffsetDateTime)
      extends ShipmentModifyCommand

  final case class ShipmentRemoveCmd(sessionUserId:   String,
                                     id:              String, // shipment ID
                                     expectedVersion: Long)
      extends ShipmentModifyCommand

  final case class ShipmentsSnapshotCmd(sessionUserId: String) extends ShipmentCommand

  implicit val addShipmentCmdReads: Reads[AddShipmentCmd] =
    Json.reads[AddShipmentCmd]

  implicit val updateShipmentCourierNameCmdReads: Reads[UpdateShipmentCourierNameCmd] =
    Json.reads[UpdateShipmentCourierNameCmd]

  implicit val updateShipmentTrackingNumberCmdReads: Reads[UpdateShipmentTrackingNumberCmd] =
    Json.reads[UpdateShipmentTrackingNumberCmd]

  implicit val updateShipmentFromLocationCmdReads: Reads[UpdateShipmentFromLocationCmd] =
    Json.reads[UpdateShipmentFromLocationCmd]

  implicit val updateShipmentToLocationCmdReads: Reads[UpdateShipmentToLocationCmd] =
    Json.reads[UpdateShipmentToLocationCmd]

  implicit val createdShipmentCmdReads: Reads[CreatedShipmentCmd] =
    Json.reads[CreatedShipmentCmd]

  implicit val packShipmentCmdReads: Reads[PackShipmentCmd] =
    Json.reads[PackShipmentCmd]

  implicit val sendShipmentCmdReads: Reads[SendShipmentCmd] =
    Json.reads[SendShipmentCmd]

  implicit val receiveShipmentCmdReads: Reads[ReceiveShipmentCmd] =
    Json.reads[ReceiveShipmentCmd]

  implicit val unpackShipmentCmdReads: Reads[UnpackShipmentCmd] =
    Json.reads[UnpackShipmentCmd]

  implicit val completeShipmentCmdReads: Reads[CompleteShipmentCmd] =
    Json.reads[CompleteShipmentCmd]

  implicit val lostShipmentCmdReads: Reads[LostShipmentCmd] =
    Json.reads[LostShipmentCmd]

  implicit val shipmentSkipStateToSentCmdReads: Reads[ShipmentSkipStateToSentCmd] =
    Json.reads[ShipmentSkipStateToSentCmd]

  implicit val shipmentSkipStateToUnpackedCmdReads: Reads[ShipmentSkipStateToUnpackedCmd] =
    Json.reads[ShipmentSkipStateToUnpackedCmd]

  implicit val shipmentRemoveCmdReads: Reads[ShipmentRemoveCmd] =
    Json.reads[ShipmentRemoveCmd]

  implicit val shipmentsSnapshotReads: Reads[ShipmentsSnapshotCmd] =
    Json.reads[ShipmentsSnapshotCmd]

}
