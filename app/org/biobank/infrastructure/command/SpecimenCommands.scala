package org.biobank.infrastructure.command

import org.biobank.infrastructure.JsonUtils._

import Commands._

import play.api.libs.json._
import org.joda.time.DateTime

object SpecimenCommands {

  trait SpecimenCommand
      extends HasCollectionEventIdentity

  trait SpecimenModifyCommand
      extends SpecimenCommand
      with HasIdentity
      with HasExpectedVersion

  case class AddSpecimenCmd(userId:            Option[String],
                            id:                String,
                            specimenGroupId:   String,
                            collectionEventId: String,
                            timeCreated:       DateTime,
                            originLocationId:  String,
                            locationId:        String,
                            containerId:       Option[String],
                            positionId:        Option[String],
                            amount:            BigDecimal,
                            usable:            Boolean)
      extends SpecimenCommand


  case class MoveSpecimenCmd(userId:            Option[String],
                             id:                String,
                             collectionEventId: String,
                             expectedVersion:   Long,
                             locationId:        String)
      extends SpecimenModifyCommand

  case class AssignSpecimenPositionCmd(userId:            Option[String],
                                       id:                String,
                                       collectionEventId: String,
                                       expectedVersion:   Long,
                                       positionId:        Option[String])
      extends SpecimenModifyCommand

  case class RemoveSpecimenAmountCmd(userId:            Option[String],
                                     id:                String,
                                     collectionEventId: String,
                                     expectedVersion:   Long,
                                     amount:            BigDecimal)
      extends SpecimenModifyCommand

  case class UpdateSpecimenUsableCmd(userId:            Option[String],
                                     id:                String,
                                     collectionEventId: String,
                                     expectedVersion:   Long,
                                     usable:            Boolean)
      extends SpecimenModifyCommand

  case class RemoveSpecimenCmd(userId:            Option[String],
                               id:                String,
                               collectionEventId: String,
                               expectedVersion:   Long,
                               usable:            Boolean)
      extends SpecimenModifyCommand

  implicit val addSpecimenCmdReads            = Json.reads[AddSpecimenCmd]
  implicit val moveSpecimenCmdReads           = Json.reads[MoveSpecimenCmd]
  implicit val assignSpecimenPositionCmdReads = Json.reads[AssignSpecimenPositionCmd]
  implicit val removeSpecimenAmountCmdReads   = Json.reads[RemoveSpecimenAmountCmd]
  implicit val updateSpecimenUsableCmdReads   = Json.reads[UpdateSpecimenUsableCmd]
  implicit val removeSpecimenCmdReads         = Json.reads[RemoveSpecimenCmd]

}
