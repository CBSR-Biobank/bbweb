package org.biobank.infrastructure.command

import org.biobank.infrastructure.command.Commands._
import org.biobank.infrastructure.command.ShipmentCommands._
import play.api.libs.json._
import play.api.libs.json.Reads._

object ShipmentSpecimenCommands {

  trait ShipmentSpecimenCommand extends ShipmentCommand with HasShipmentIdentity

  trait ShipmentSpecimenModifyCommand extends ShipmentSpecimenCommand
      with HasIdentity with HasExpectedVersion

  final case class ShipmentAddSpecimenCmd(userId:              String,
                                          shipmentId:          String,
                                          specimenId:          String,
                                          shipmentContainerId: Option[String])
      extends ShipmentSpecimenCommand

  final case class ShipmentSpecimenUpdateContainerCmd(userId:              String,
                                                      shipmentId:          String,
                                                      id:                  String, // shipment specimen ID
                                                      expectedVersion:     Long,
                                                      shipmentContainerId: Option[String])
      extends ShipmentSpecimenModifyCommand

  final case class ShipmentSpecimenReceivedCmd(userId:          String,
                                               shipmentId:      String,
                                               id:              String, // shipment specimen ID
                                               expectedVersion: Long)
      extends ShipmentSpecimenModifyCommand

  final case class ShipmentSpecimenMissingCmd(userId:          String,
                                              shipmentId:      String,
                                              id:              String, // shipment specimen ID
                                              expectedVersion: Long)
      extends ShipmentSpecimenModifyCommand

  final case class ShipmentSpecimenExtraCmd(userId:          String,
                                            shipmentId:      String,
                                            id:              String, // shipment specimen ID
                                            expectedVersion: Long)
      extends ShipmentSpecimenModifyCommand

  implicit val shipmentAddSpecimenCmdReads: Reads[ShipmentAddSpecimenCmd] =
    Json.reads[ShipmentAddSpecimenCmd]

  implicit val shipmentSpecimenUpdateContainerCmdReads: Reads[ShipmentSpecimenUpdateContainerCmd] =
    Json.reads[ShipmentSpecimenUpdateContainerCmd]

  implicit val shipmentSpecimenReceivedCmdReads: Reads[ShipmentSpecimenReceivedCmd] =
    Json.reads[ShipmentSpecimenReceivedCmd]

  implicit val shipmentSpecimenMissingCmdReads: Reads[ShipmentSpecimenMissingCmd] =
    Json.reads[ShipmentSpecimenMissingCmd]

  implicit val shipmentSpecimenExtraCmdReads: Reads[ShipmentSpecimenExtraCmd] =
    Json.reads[ShipmentSpecimenExtraCmd]

}
