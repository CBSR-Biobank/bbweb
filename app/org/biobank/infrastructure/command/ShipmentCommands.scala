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

  case class AddShipmentCmd(userId:         String,
                            courierName:    String,
                            trackingNumber: String,
                            fromLocationId: String,
                            toLocationId:   String)
      extends ShipmentCommand

  case class UpdateShipmentCourierNameCmd(userId:          String,
                                          id:              String, // shipment ID
                                          expectedVersion: Long,
                                          courierName:     String)
      extends ShipmentModifyCommand

  case class UpdateShipmentTrackingNumberCmd(userId:          String,
                                             id:              String, // shipment ID
                                             expectedVersion: Long,
                                             trackingNumber:  String)
      extends ShipmentModifyCommand

  case class UpdateShipmentFromLocationCmd(userId:          String,
                                           id:              String, // shipment ID
                                           expectedVersion: Long,
                                           locationId:      String)
      extends ShipmentModifyCommand

  case class UpdateShipmentToLocationCmd(userId:          String,
                                         id:              String, // shipment ID
                                         expectedVersion: Long,
                                         locationId:      String)
      extends ShipmentModifyCommand

  case class ShipmentPackedCmd(userId:          String,
                               id:              String, // shipment ID
                               expectedVersion: Long,
                               time:            DateTime)
      extends ShipmentModifyCommand

  case class ShipmentSentCmd(userId:          String,
                             id:              String, // shipment ID
                             expectedVersion: Long,
                             time:            DateTime)
      extends ShipmentModifyCommand

  case class ShipmentReceivedCmd(userId:          String,
                                 id:              String, // shipment ID
                                 expectedVersion: Long,
                                 time:            DateTime)
      extends ShipmentModifyCommand

  case class ShipmentUnpackedCmd(userId:          String,
                                 id:              String, // shipment ID
                                 expectedVersion: Long,
                                 time:            DateTime)
      extends ShipmentModifyCommand

  case class ShipmentLostCmd(userId:          String,
                             id:              String, // shipment ID
                             expectedVersion: Long)
      extends ShipmentModifyCommand

  implicit val addShipmentCmdReads                  = Json.reads[AddShipmentCmd]
  implicit val updateShipmentCourierNameCmdReads    = Json.reads[UpdateShipmentCourierNameCmd]
  implicit val updateShipmentTrackingNumberCmdReads = Json.reads[UpdateShipmentTrackingNumberCmd]
  implicit val updateShipmentFromLocationCmdReads   = Json.reads[UpdateShipmentFromLocationCmd]
  implicit val updateShipmentToLocationCmdReads     = Json.reads[UpdateShipmentToLocationCmd]
  implicit val shipmentPackedCmdReads               = Json.reads[ShipmentPackedCmd]
  implicit val shipmentSentCmdReads                 = Json.reads[ShipmentSentCmd]
  implicit val shipmentReceivedCmdReads             = Json.reads[ShipmentReceivedCmd]
  implicit val shipmentUnpackedCmdReads             = Json.reads[ShipmentUnpackedCmd]
  implicit val shipmentLostCmdReads                 = Json.reads[ShipmentLostCmd]

}
