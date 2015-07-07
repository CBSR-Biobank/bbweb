package org.biobank.infrastructure.command

import org.biobank.infrastructure._
import org.biobank.domain.study._
import org.biobank.domain.ContainerTypeId

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


  implicit val addParticipantCmdReads        = Json.reads[AddParticipantCmd]
  implicit val updateParticipantCmdReads     = Json.reads[UpdateParticipantCmd]
  implicit val addCollectionEventCmdReads    = Json.reads[AddCollectionEventCmd]
  implicit val updateCollectionEventCmdReads = Json.reads[UpdateCollectionEventCmd]
  implicit val removeCollectionEventCmdReads = Json.reads[RemoveCollectionEventCmd]
}
