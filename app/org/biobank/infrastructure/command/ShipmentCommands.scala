package org.biobank.infrastructure.command

import org.biobank.infrastructure.command.Commands._
import play.api.libs.json._
//import play.api.libs.json.Reads._
import org.biobank.infrastructure.JsonUtils._
import org.joda.time.DateTime

object ShipmentCommands {

  trait ShipmentCommand extends Command with HasUserId

  trait ShipmentModifyCommand extends ShipmentCommand with HasIdentity with HasExpectedVersion

  trait ShipmentCommandWithShipmentId extends ShipmentCommand with HasShipmentIdentity

  final case class AddShipmentCmd(userId:         String,
                                  courierName:    String,
                                  trackingNumber: String,
                                  fromLocationId: String,
                                  toLocationId:   String)
      extends ShipmentCommand

  final case class UpdateShipmentCourierNameCmd(userId:          String,
                                                id:              String, // shipment ID
                                                expectedVersion: Long,
                                                courierName:     String)
      extends ShipmentModifyCommand

  final case class UpdateShipmentTrackingNumberCmd(userId:          String,
                                                   id:              String, // shipment ID
                                                   expectedVersion: Long,
                                                   trackingNumber:  String)
      extends ShipmentModifyCommand

  final case class UpdateShipmentFromLocationCmd(userId:          String,
                                                 id:              String, // shipment ID
                                                 expectedVersion: Long,
                                                 locationId:      String)
      extends ShipmentModifyCommand

  final case class UpdateShipmentToLocationCmd(userId:          String,
                                               id:              String, // shipment ID
                                               expectedVersion: Long,
                                               locationId:      String)
      extends ShipmentModifyCommand

  final case class ShipmentPackedCmd(userId:          String,
                                     id:              String, // shipment ID
                                     expectedVersion: Long,
                                     time:            DateTime)
      extends ShipmentModifyCommand

  final case class ShipmentSentCmd(userId:          String,
                                   id:              String, // shipment ID
                                   expectedVersion: Long,
                                   time:            DateTime)
      extends ShipmentModifyCommand

  final case class ShipmentReceivedCmd(userId:          String,
                                       id:              String, // shipment ID
                                       expectedVersion: Long,
                                       time:            DateTime)
      extends ShipmentModifyCommand

  final case class ShipmentUnpackedCmd(userId:          String,
                                       id:              String, // shipment ID
                                       expectedVersion: Long,
                                       time:            DateTime)
      extends ShipmentModifyCommand

  final case class ShipmentLostCmd(userId:          String,
                                   id:              String, // shipment ID
                                   expectedVersion: Long)
      extends ShipmentModifyCommand

  final case class ShipmentRemoveCmd(userId:          String,
                                     id:              String, // shipment ID
                                     expectedVersion: Long)
      extends ShipmentModifyCommand

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

  implicit val shipmentPackedCmdReads: Reads[ShipmentPackedCmd] =
    Json.reads[ShipmentPackedCmd]

  implicit val shipmentSentCmdReads: Reads[ShipmentSentCmd] =
    Json.reads[ShipmentSentCmd]

  implicit val shipmentReceivedCmdReads: Reads[ShipmentReceivedCmd] =
    Json.reads[ShipmentReceivedCmd]

  implicit val shipmentUnpackedCmdReads: Reads[ShipmentUnpackedCmd] =
    Json.reads[ShipmentUnpackedCmd]

  implicit val shipmentLostCmdReads: Reads[ShipmentLostCmd] =
    Json.reads[ShipmentLostCmd]

}
