package org.biobank.infrastructure.commands

import org.biobank.infrastructure.commands.Commands._
import org.biobank.infrastructure.commands.ShipmentCommands._
import play.api.libs.json._
import play.api.libs.json.Reads._

object ShipmentSpecimenCommands {

  trait ShipmentSpecimenCommand extends ShipmentCommand with HasShipmentIdentity

  trait ShipmentSpecimenModifyCommand extends ShipmentSpecimenCommand

  final case class ShipmentAddSpecimensCmd(sessionUserId:        String,
                                           shipmentId:           String,
                                           shipmentContainerId:  Option[String],
                                           specimenInventoryIds: List[String])
      extends ShipmentSpecimenCommand

  final case class ShipmentSpecimenRemoveCmd(sessionUserId:      String,
                                             shipmentId:         String,
                                             expectedVersion:    Long,
                                             shipmentSpecimenId: String)
      extends ShipmentSpecimenModifyCommand

  final case class ShipmentSpecimenUpdateContainerCmd(sessionUserId:        String,
                                                      shipmentId:           String,
                                                      shipmentContainerId:  Option[String],
                                                      specimenInventoryIds: List[String])
      extends ShipmentSpecimenModifyCommand

  final case class ShipmentSpecimensPresentCmd(sessionUserId:        String,
                                               shipmentId:           String,
                                               specimenInventoryIds: List[String])
      extends ShipmentSpecimenModifyCommand

  final case class ShipmentSpecimensReceiveCmd(sessionUserId:        String,
                                               shipmentId:           String,
                                               specimenInventoryIds: List[String])
      extends ShipmentSpecimenModifyCommand

  final case class ShipmentSpecimenMissingCmd(sessionUserId:        String,
                                              shipmentId:           String,
                                              specimenInventoryIds: List[String])
      extends ShipmentSpecimenModifyCommand

  final case class ShipmentSpecimenExtraCmd(sessionUserId:        String,
                                            shipmentId:           String,
                                            specimenInventoryIds: List[String])
      extends ShipmentSpecimenModifyCommand

  implicit val shipmentAddSpecimenCmdReads: Reads[ShipmentAddSpecimensCmd] =
    Json.reads[ShipmentAddSpecimensCmd]

  implicit val shipmentSpecimenUpdateContainerCmdReads: Reads[ShipmentSpecimenUpdateContainerCmd] =
    Json.reads[ShipmentSpecimenUpdateContainerCmd]

  implicit val shipmentSpecimensPresentCmdReads: Reads[ShipmentSpecimensPresentCmd] =
    Json.reads[ShipmentSpecimensPresentCmd]

  implicit val shipmentSpecimensReceivedCmdReads: Reads[ShipmentSpecimensReceiveCmd] =
    Json.reads[ShipmentSpecimensReceiveCmd]

  implicit val shipmentSpecimensMissingCmdReads: Reads[ShipmentSpecimenMissingCmd] =
    Json.reads[ShipmentSpecimenMissingCmd]

  implicit val shipmentSpecimensExtraCmdReads: Reads[ShipmentSpecimenExtraCmd] =
    Json.reads[ShipmentSpecimenExtraCmd]

}
