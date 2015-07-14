package org.biobank.infrastructure.command

import org.biobank.infrastructure._
import org.biobank.domain.participants._

import Commands._

import play.api.libs.json._
import play.api.libs.json.Reads._
import org.joda.time.DateTime

object ParticipantCommands {

  trait ParticipantCommand extends Command

  trait ParticipantModifyCommand
      extends ParticipantCommand
      with HasIdentity
      with HasExpectedVersion

  case class AddParticipantCmd(userId:      Option[String],
                               studyId:     String,
                               uniqueId:    String,
                               annotations: List[ParticipantAnnotation])
      extends ParticipantCommand

  case class UpdateParticipantCmd(userId:    Option[String],
                                  studyId:         String,
                                  id:              String,
                                  expectedVersion: Long,
                                  uniqueId:        String,
                                  annotations:     List[ParticipantAnnotation])
      extends ParticipantModifyCommand

  //--

  trait CollectionEventCommand
      extends ParticipantCommand
      with HasParticipantIdentity

  trait CollectionEventModifyCommand
      extends CollectionEventCommand
      with HasIdentity
      with HasExpectedVersion

  case class AddCollectionEventCmd(userId:                Option[String],
                                   participantId:         String,
                                   collectionEventTypeId: String,
                                   timeCompleted:         DateTime,
                                   visitNumber:           Int,
                                   annotations:           List[CollectionEventAnnotation])
      extends CollectionEventCommand

  case class UpdateCollectionEventCmd(userId:                Option[String],
                                      id:                    String,
                                      participantId:         String,
                                      collectionEventTypeId: String,
                                      expectedVersion:       Long,
                                      timeCompleted:         DateTime,
                                      visitNumber:           Int,
                                      annotations:           List[CollectionEventAnnotation])
      extends CollectionEventModifyCommand

  case class RemoveCollectionEventCmd(userId:          Option[String],
                                      id:              String,
                                      participantId:   String,
                                      expectedVersion: Long)
      extends CollectionEventModifyCommand

  //--

  trait SpecimenCommand
      extends ParticipantCommand
      with HasCollectionEventIdentity

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

  implicit val addParticipantCmdReads         = Json.reads[AddParticipantCmd]
  implicit val updateParticipantCmdReads      = Json.reads[UpdateParticipantCmd]
  implicit val addCollectionEventCmdReads     = Json.reads[AddCollectionEventCmd]
  implicit val updateCollectionEventCmdReads  = Json.reads[UpdateCollectionEventCmd]
  implicit val removeCollectionEventCmdReads  = Json.reads[RemoveCollectionEventCmd]
  implicit val addSpecimenCmdReads            = Json.reads[AddSpecimenCmd]
  implicit val moveSpecimenCmdReads           = Json.reads[MoveSpecimenCmd]
  implicit val assignSpecimenPositionCmdReads = Json.reads[AssignSpecimenPositionCmd]
  implicit val removeSpecimenAmountCmdReads   = Json.reads[RemoveSpecimenAmountCmd]
  implicit val updateSpecimenUsableCmdReads   = Json.reads[UpdateSpecimenUsableCmd]
  implicit val removeSpecimenCmdReads         = Json.reads[RemoveSpecimenCmd]

}
