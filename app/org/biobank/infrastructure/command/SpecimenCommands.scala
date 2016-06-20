package org.biobank.infrastructure.command

import org.biobank.infrastructure.JsonUtils._

import Commands._

import play.api.libs.json._
import org.joda.time.DateTime

object SpecimenCommands {

  trait SpecimenCommand extends Command with HasUserId with HasCollectionEventIdentity

  trait SpecimenModifyCommand
      extends SpecimenCommand
      with HasIdentity
      with HasExpectedVersion

  case class SpecimenInfo(inventoryId:    String,
                          specimenSpecId: String,
                          timeCreated:    DateTime,
                          locationId:     String,
                          amount:         BigDecimal)

  case class AddSpecimensCmd(userId:            String,
                             collectionEventId: String,
                             specimenData:      List[SpecimenInfo])
      extends SpecimenCommand


  case class MoveSpecimensCmd(userId:            String,
                              collectionEventId: String,
                              expectedVersion:   Long,
                              locationId:        String,
                              specimenData:      Set[SpecimenInfo])
      extends SpecimenCommand

  case class SpecimenAssignPositionCmd(userId:            String,
                                       id:                String,
                                       collectionEventId: String,
                                       expectedVersion:   Long,
                                       positionId:        Option[String])
      extends SpecimenModifyCommand
      with HasIdentity

  case class SpecimenRemoveAmountCmd(userId:            String,
                                     id:                String,
                                     collectionEventId: String,
                                     expectedVersion:   Long,
                                     amount:            BigDecimal)
      extends SpecimenModifyCommand

  case class SpecimenUpdateUsableCmd(userId:            String,
                                     id:                String,
                                     collectionEventId: String,
                                     expectedVersion:   Long,
                                     usable:            Boolean)
      extends SpecimenModifyCommand

  case class RemoveSpecimenCmd(userId:            String,
                               id:                String,
                               collectionEventId: String,
                               expectedVersion:   Long)
      extends SpecimenModifyCommand

  implicit val specimenInfoReads              = Json.reads[SpecimenInfo]
  implicit val addSpecimensCmdReads           = Json.reads[AddSpecimensCmd]
  implicit val moveSpecimensCmdReads          = Json.reads[MoveSpecimensCmd]
  implicit val specimenAssignPositionCmdReads = Json.reads[SpecimenAssignPositionCmd]
  implicit val specimenRemoveAmountCmdReads   = Json.reads[SpecimenRemoveAmountCmd]
  implicit val specimenUpdateUsableCmdReads   = Json.reads[SpecimenUpdateUsableCmd]
  implicit val removeSpecimenCmdReads         = Json.reads[RemoveSpecimenCmd]

}
