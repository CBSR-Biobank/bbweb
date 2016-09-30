package org.biobank.infrastructure.command

import org.biobank.domain.centre.ShipmentState._
import org.biobank.infrastructure.command.Commands._
import org.joda.time.DateTime
import play.api.libs.json._

object ShipmentCommands {

  import org.biobank.infrastructure.JsonUtils._

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

  final case class ShipmentChangeStateCmd(userId:          String,
                                          id:              String, // shipment ID
                                          expectedVersion: Long,
                                          newState:        ShipmentState,
                                          datetime:        Option[DateTime])
      extends ShipmentModifyCommand

  final case class ShipmentSkipStateToSentCmd(userId:          String,
                                              id:              String, // shipment ID
                                              expectedVersion: Long,
                                              timePacked:      DateTime,
                                              timeSent:        DateTime)
      extends ShipmentModifyCommand

  final case class ShipmentSkipStateToUnpackedCmd(userId:          String,
                                                  id:              String, // shipment ID
                                                  expectedVersion: Long,
                                                  timeReceived:    DateTime,
                                                  timeUnpacked:    DateTime)
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

  implicit val shipmentChangeStateCmdReads: Reads[ShipmentChangeStateCmd] =
    Json.reads[ShipmentChangeStateCmd]

  implicit val shipmentSkipStateToSentCmdReads: Reads[ShipmentSkipStateToSentCmd] =
    Json.reads[ShipmentSkipStateToSentCmd]

  implicit val shipmentSkipStateToUnpackedCmdReads: Reads[ShipmentSkipStateToUnpackedCmd] =
    Json.reads[ShipmentSkipStateToUnpackedCmd]

  implicit val shipmentRemoveCmdReads: Reads[ShipmentRemoveCmd] =
    Json.reads[ShipmentRemoveCmd]

}
