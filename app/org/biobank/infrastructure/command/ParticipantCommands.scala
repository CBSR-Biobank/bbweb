package org.biobank.infrastructure.command

import org.biobank.infrastructure._
import org.biobank.domain.Annotation
import org.biobank.infrastructure.JsonUtils._

import Commands._

import play.api.libs.json._
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
                               annotations: List[Annotation])
      extends ParticipantCommand

  case class UpdateParticipantUniqueIdCmd(userId:          Option[String],
                                          id:              String,
                                          expectedVersion: Long,
                                          uniqueId:        String)
      extends ParticipantModifyCommand

  case class ParticipantAddAnnotationCmd(userId:          Option[String],
                                         id:              String,
                                         expectedVersion: Long,
                                         stringValue:      Option[String],
                                         numberValue:      Option[String],
                                         selectedValues:   Set[String])
      extends ParticipantModifyCommand

  case class ParticipantUpdateAnnotationCmd(userId:          Option[String],
                                            id:              String,
                                            expectedVersion: Long,
                                            annotationTypeId: String,
                                            stringValue:      Option[String],
                                            numberValue:      Option[String],
                                            selectedValues:   Set[String])
      extends ParticipantModifyCommand

  case class RemoveParticipantAnnotationCmd(userId:           Option[String],
                                            id:               String,
                                            expectedVersion:  Long,
                                            annotationTypeId: String)
      extends ParticipantModifyCommand

  //--

  trait CollectionEventCommand
      extends ParticipantCommand

  trait CollectionEventModifyCommand
      extends CollectionEventCommand
      with HasIdentity
      with HasExpectedVersion

  case class AddCollectionEventCmd(userId:                Option[String],
                                   participantId:         String,
                                   collectionEventTypeId: String,
                                   timeCompleted:         DateTime,
                                   visitNumber:           Int,
                                   annotations:           List[Annotation])
      extends CollectionEventCommand

  case class UpdateCollectionEventVisitNumberCmd(userId:          Option[String],
                                                 id:              String,
                                                 expectedVersion: Long,
                                                 visitNumber:     Int)
      extends CollectionEventModifyCommand

  case class UpdateCollectionEventTimeCompletedCmd(userId:          Option[String],
                                                   id:              String,
                                                   expectedVersion: Long,
                                                   timeCompleted:   DateTime)
      extends CollectionEventModifyCommand

  case class UpdateCollectionEventAnnotationCmd(userId:          Option[String],
                                                id:              String,
                                                expectedVersion: Long,
                                                annotation:      Annotation)
      extends CollectionEventModifyCommand

  case class RemoveCollectionEventAnnotationCmd(userId:           Option[String],
                                                id:               String,
                                                expectedVersion:  Long,
                                                annotationTypeId: String)
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

  implicit val addParticipantCmdReads              = Json.reads[AddParticipantCmd]
  implicit val updateParticipantUniqueIdCmdReads   = Json.reads[UpdateParticipantUniqueIdCmd]
  implicit val updateParticipantAnnotationCmdReads = Json.reads[UpdateParticipantAnnotationCmd]
  implicit val removeParticipantAnnotationCmdReads = Json.reads[RemoveParticipantAnnotationCmd]
  implicit val addCollectionEventCmdReads          = Json.reads[AddCollectionEventCmd]

  implicit val updateCollectionEventVisitNumberCmdReads   = Json.reads[UpdateCollectionEventVisitNumberCmd]
  implicit val updateCollectionEventTimeCompletedCmdReads = Json.reads[UpdateCollectionEventTimeCompletedCmd]
  implicit val updateCollectionEventAnnotationCmdReads    = Json.reads[UpdateCollectionEventAnnotationCmd]

  implicit val removeCollectionEventCmdReads  = Json.reads[RemoveCollectionEventCmd]
  implicit val addSpecimenCmdReads            = Json.reads[AddSpecimenCmd]
  implicit val moveSpecimenCmdReads           = Json.reads[MoveSpecimenCmd]
  implicit val assignSpecimenPositionCmdReads = Json.reads[AssignSpecimenPositionCmd]
  implicit val removeSpecimenAmountCmdReads   = Json.reads[RemoveSpecimenAmountCmd]
  implicit val updateSpecimenUsableCmdReads   = Json.reads[UpdateSpecimenUsableCmd]
  implicit val removeSpecimenCmdReads         = Json.reads[RemoveSpecimenCmd]

}
