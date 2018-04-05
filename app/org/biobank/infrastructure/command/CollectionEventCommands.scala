package org.biobank.infrastructure.command

import Commands._
import java.time.OffsetDateTime
import org.biobank.domain.annotations.Annotation
import play.api.libs.json._

object CollectionEventCommands {

  trait CollectionEventCommand extends Command with HasSessionUserId

  trait CollectionEventModifyCommand
      extends CollectionEventCommand
      with HasIdentity
      with HasExpectedVersion

  final case class AddCollectionEventCmd(sessionUserId:         String,
                                         participantId:         String,
                                         collectionEventTypeId: String,
                                         timeCompleted:         OffsetDateTime,
                                         visitNumber:           Int,
                                         annotations:           List[Annotation])
      extends CollectionEventCommand

  final case class UpdateCollectionEventVisitNumberCmd(sessionUserId:   String,
                                                       id:              String,
                                                       expectedVersion: Long,
                                                       visitNumber:     Int)
      extends CollectionEventModifyCommand

  final case class UpdateCollectionEventTimeCompletedCmd(sessionUserId:   String,
                                                         id:              String,
                                                         expectedVersion: Long,
                                                         timeCompleted:   OffsetDateTime)
      extends CollectionEventModifyCommand

  final case class CollectionEventUpdateAnnotationCmd(sessionUserId:    String,
                                                      id:               String,
                                                      expectedVersion:  Long,
                                                      annotationTypeId: String,
                                                      stringValue:      Option[String],
                                                      numberValue:      Option[String],
                                                      selectedValues:   Set[String])
      extends CollectionEventModifyCommand

  final case class RemoveCollectionEventAnnotationCmd(sessionUserId:    String,
                                                      id:               String,
                                                      expectedVersion:  Long,
                                                      annotationTypeId: String)
      extends CollectionEventModifyCommand

  final case class RemoveCollectionEventCmd(sessionUserId:   String,
                                            id:              String,
                                            participantId:   String,
                                            expectedVersion: Long)
      extends CollectionEventModifyCommand

  implicit val addCollectionEventCmdReads: Reads[AddCollectionEventCmd]                                 = Json.reads[AddCollectionEventCmd]
  implicit val updateCollectionEventVisitNumberCmdReads: Reads[UpdateCollectionEventVisitNumberCmd]     = Json.reads[UpdateCollectionEventVisitNumberCmd]
  implicit val updateCollectionEventTimeCompletedCmdReads: Reads[UpdateCollectionEventTimeCompletedCmd] = Json.reads[UpdateCollectionEventTimeCompletedCmd]
  implicit val updateCollectionEventAnnotationCmdReads: Reads[CollectionEventUpdateAnnotationCmd]       = Json.reads[CollectionEventUpdateAnnotationCmd]
  implicit val removeCollectionEventCmdReads: Reads[RemoveCollectionEventCmd]                           = Json.reads[RemoveCollectionEventCmd]

}
