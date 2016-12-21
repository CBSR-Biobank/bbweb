package org.biobank.infrastructure.command

import org.biobank.infrastructure.command.Commands._
import org.biobank.infrastructure.command.ShipmentCommands._
import play.api.libs.json._
import play.api.libs.json.Reads._

object ShipmentSpecimenCommands {

  trait ShipmentSpecimenCommand extends ShipmentCommand with HasShipmentIdentity

  trait ShipmentSpecimenModifyCommand extends ShipmentSpecimenCommand

  final case class ShipmentAddSpecimensCmd(userId:               String,
                                           shipmentId:           String,
                                           shipmentContainerId:  Option[String],
                                           specimenInventoryIds: List[String])
      extends ShipmentSpecimenCommand

  final case class ShipmentSpecimenRemoveCmd(userId:             String,
                                             shipmentId:         String,
                                             expectedVersion:    Long,
                                             shipmentSpecimenId: String)
      extends ShipmentSpecimenModifyCommand

  final case class ShipmentSpecimenUpdateContainerCmd(userId:               String,
                                                      shipmentId:           String,
                                                      shipmentContainerId:  Option[String],
                                                      specimenInventoryIds: List[String])
      extends ShipmentSpecimenModifyCommand

  final case class ShipmentReceiveSpecimensCmd(userId:               String,
                                               shipmentId:           String,
                                               specimenInventoryIds: List[String])
      extends ShipmentSpecimenModifyCommand

  final case class ShipmentSpecimenMissingCmd(userId:               String,
                                              shipmentId:           String,
                                              specimenInventoryIds: List[String])
      extends ShipmentSpecimenModifyCommand

  final case class ShipmentSpecimenExtraCmd(userId:               String,
                                            shipmentId:           String,
                                            specimenInventoryIds: List[String])
      extends ShipmentSpecimenModifyCommand

  implicit val shipmentAddSpecimenCmdReads: Reads[ShipmentAddSpecimensCmd] =
    Json.reads[ShipmentAddSpecimensCmd]

  implicit val shipmentSpecimenUpdateContainerCmdReads: Reads[ShipmentSpecimenUpdateContainerCmd] =
    Json.reads[ShipmentSpecimenUpdateContainerCmd]

  implicit val shipmentSpecimenReceivedCmdReads: Reads[ShipmentReceiveSpecimensCmd] =
    Json.reads[ShipmentReceiveSpecimensCmd]

  implicit val shipmentSpecimenMissingCmdReads: Reads[ShipmentSpecimenMissingCmd] =
    Json.reads[ShipmentSpecimenMissingCmd]

  implicit val shipmentSpecimenExtraCmdReads: Reads[ShipmentSpecimenExtraCmd] =
    Json.reads[ShipmentSpecimenExtraCmd]

}
