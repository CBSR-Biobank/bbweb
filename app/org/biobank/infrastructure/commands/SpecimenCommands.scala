package org.biobank.infrastructure.commands

import Commands._
import java.time.OffsetDateTime
import play.api.libs.json._

object SpecimenCommands {

  trait SpecimenCommand extends Command with HasSessionUserId with HasCollectionEventIdentity

  trait SpecimenModifyCommand
      extends SpecimenCommand
      with HasIdentity
      with HasExpectedVersion

  final case class SpecimenInfo(inventoryId:           String,
                                specimenDefinitionId: String,
                                timeCreated:           OffsetDateTime,
                                locationId:            String,
                                amount:                BigDecimal)

  final case class AddSpecimensCmd(sessionUserId:     String,
                                   collectionEventId: String,
                                   specimenData:      List[SpecimenInfo])
      extends SpecimenCommand


  final case class MoveSpecimensCmd(sessionUserId:     String,
                                    collectionEventId: String,
                                    expectedVersion:   Long,
                                    specimenData:      Set[SpecimenInfo])
      extends SpecimenCommand

  final case class SpecimenAssignPositionCmd(sessionUserId:     String,
                                             id:                String,
                                             collectionEventId: String,
                                             expectedVersion:   Long,
                                             positionId:        Option[String])
      extends SpecimenModifyCommand
      with HasIdentity

  final case class SpecimenRemoveAmountCmd(sessionUserId:     String,
                                           id:                String,
                                           collectionEventId: String,
                                           expectedVersion:   Long,
                                           amount:            BigDecimal)
      extends SpecimenModifyCommand

  final case class SpecimenUpdateUsableCmd(sessionUserId:     String,
                                           id:                String,
                                           collectionEventId: String,
                                           expectedVersion:   Long,
                                           usable:            Boolean)
      extends SpecimenModifyCommand

  final case class RemoveSpecimenCmd(sessionUserId:     String,
                                     id:                String,
                                     collectionEventId: String,
                                     expectedVersion:   Long)
      extends SpecimenModifyCommand

  implicit val specimenInfoReads: Reads[SpecimenInfo]                           = Json.reads[SpecimenInfo]
  implicit val addSpecimensCmdReads: Reads[AddSpecimensCmd]                     = Json.reads[AddSpecimensCmd]
  implicit val moveSpecimensCmdReads: Reads[MoveSpecimensCmd]                   = Json.reads[MoveSpecimensCmd]
  implicit val specimenAssignPositionCmdReads: Reads[SpecimenAssignPositionCmd] = Json.reads[SpecimenAssignPositionCmd]
  implicit val specimenRemoveAmountCmdReads: Reads[SpecimenRemoveAmountCmd]     = Json.reads[SpecimenRemoveAmountCmd]
  implicit val specimenUpdateUsableCmdReads: Reads[SpecimenUpdateUsableCmd]     = Json.reads[SpecimenUpdateUsableCmd]
  implicit val removeSpecimenCmdReads: Reads[RemoveSpecimenCmd]                 = Json.reads[RemoveSpecimenCmd]

}
