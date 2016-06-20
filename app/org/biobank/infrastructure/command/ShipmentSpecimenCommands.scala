package org.biobank.infrastructure.command

import org.biobank.infrastructure.command.Commands._
import org.biobank.infrastructure.command.ShipmentCommands._
import play.api.libs.json._
import play.api.libs.json.Reads._

object ShipmentSpecimenCommands {

  trait ShipmentSpecimenCommand extends ShipmentCommand with HasShipmentIdentity

  trait ShipmentSpecimenModifyCommand extends ShipmentSpecimenCommand
      with HasIdentity with HasExpectedVersion

  case class ShipmentAddSpecimenCmd(userId:              String,
                                    shipmentId:          String,
                                    specimenId:          String,
                                    shipmentContainerId: Option[String])
      extends ShipmentSpecimenCommand

  case class ShipmentSpecimenUpdateContainerCmd(userId:              String,
                                                shipmentId:          String,
                                                id:                  String, // shipment specimen ID
                                                expectedVersion:     Long,
                                                shipmentContainerId: Option[String])
      extends ShipmentSpecimenModifyCommand

  case class ShipmentSpecimenReceivedCmd(userId:          String,
                                         shipmentId:      String,
                                         id:              String, // shipment specimen ID
                                         expectedVersion: Long)
      extends ShipmentSpecimenModifyCommand

  case class ShipmentSpecimenMissingCmd(userId:          String,
                                        shipmentId:      String,
                                        id:              String, // shipment specimen ID
                                        expectedVersion: Long)
      extends ShipmentSpecimenModifyCommand

  case class ShipmentSpecimenExtraCmd(userId:          String,
                                      shipmentId:      String,
                                      id:              String, // shipment specimen ID
                                      expectedVersion: Long)
      extends ShipmentSpecimenModifyCommand

  implicit val shipmentAddSpecimenCmdReads             = Json.reads[ShipmentAddSpecimenCmd]
  implicit val shipmentSpecimenUpdateContainerCmdReads = Json.reads[ShipmentSpecimenUpdateContainerCmd]
  implicit val shipmentSpecimenReceivedCmdReads        = Json.reads[ShipmentSpecimenReceivedCmd]
  implicit val shipmentSpecimenMissingCmdReads         = Json.reads[ShipmentSpecimenMissingCmd]
  implicit val shipmentSpecimenExtraCmdReads           = Json.reads[ShipmentSpecimenExtraCmd]

}
