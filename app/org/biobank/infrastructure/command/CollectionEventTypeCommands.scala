package org.biobank.infrastructure.command

import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.AnnotationValueType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.SpecimenType._
import play.api.libs.json.Reads._
import play.api.libs.json._

object CollectionEventTypeCommands {
  import org.biobank.infrastructure.command.Commands._

  trait CollectionEventTypeCommand extends Command with HasStudyIdentity

  trait CollectionEventTypeModifyCommand
      extends CollectionEventTypeCommand
      with HasIdentity
      with HasExpectedVersion

  final case class AddCollectionEventTypeCmd(userId:             Option[String],
                                             studyId:            String,
                                             name:               String,
                                             description:        Option[String],
                                             recurring:          Boolean)
      extends CollectionEventTypeCommand

  final case class UpdateCollectionEventTypeNameCmd(userId:          Option[String],
                                                    studyId:         String,
                                                    id:              String,
                                                    expectedVersion: Long,
                                                    name:            String)
      extends CollectionEventTypeModifyCommand

  final case class UpdateCollectionEventTypeDescriptionCmd(userId:          Option[String],
                                                           studyId:         String,
                                                           id:              String,
                                                           expectedVersion: Long,
                                                           description:     Option[String])
      extends CollectionEventTypeModifyCommand

  final case class UpdateCollectionEventTypeRecurringCmd(userId:          Option[String],
                                                         studyId:         String,
                                                         id:              String,
                                                         expectedVersion: Long,
                                                         recurring:       Boolean)
      extends CollectionEventTypeModifyCommand

  final case class RemoveCollectionEventTypeCmd(userId:          Option[String],
                                                studyId:         String,
                                                id:              String,
                                                expectedVersion: Long)
      extends CollectionEventTypeModifyCommand

  final case class CollectionEventTypeAddAnnotationTypeCmd(userId:          Option[String],
                                                           studyId:         String,
                                                           id:              String,
                                                           expectedVersion: Long,
                                                           name:            String,
                                                           description:     Option[String],
                                                           valueType:       AnnotationValueType,
                                                           maxValueCount:   Option[Int],
                                                           options:         Seq[String],
                                                           required:        Boolean)
      extends CollectionEventTypeModifyCommand

  final case class CollectionEventTypeUpdateAnnotationTypeCmd(userId:           Option[String],
                                                              studyId:          String,
                                                              id:               String,
                                                              expectedVersion:  Long,
                                                              annotationTypeId: String,
                                                              name:             String,
                                                              description:      Option[String],
                                                              valueType:        AnnotationValueType,
                                                              maxValueCount:    Option[Int],
                                                              options:          Seq[String],
                                                              required:         Boolean)
      extends CollectionEventTypeModifyCommand

  final case class RemoveCollectionEventTypeAnnotationTypeCmd(userId:           Option[String],
                                                              studyId:          String,
                                                              id:               String,
                                                              expectedVersion:  Long,
                                                              annotationTypeId: String)
      extends CollectionEventTypeModifyCommand

  final case class AddCollectionSpecimenDescriptionCmd(userId:                      Option[String],
                                                       studyId:                     String,
                                                       id:                          String,
                                                       expectedVersion:             Long,
                                                       name:                        String,
                                                       description:                 Option[String],
                                                       units:                       String,
                                                       anatomicalSourceType:        AnatomicalSourceType,
                                                       preservationType:            PreservationType,
                                                       preservationTemperatureType: PreservationTemperatureType,
                                                       specimenType:                SpecimenType,
                                                       maxCount:                    Int,
                                                       amount:                      BigDecimal)
      extends CollectionEventTypeModifyCommand

  final case class UpdateCollectionSpecimenDescriptionCmd(userId:                      Option[String],
                                                          studyId:                     String,
                                                          id:                          String,
                                                          expectedVersion:             Long,
                                                          specimenDescriptionId:       String,
                                                          name:                        String,
                                                          description:                 Option[String],
                                                          units:                       String,
                                                          anatomicalSourceType:        AnatomicalSourceType,
                                                          preservationType:            PreservationType,
                                                          preservationTemperatureType: PreservationTemperatureType,
                                                          specimenType:                SpecimenType,
                                                          maxCount:                    Int,
                                                          amount:                      BigDecimal)
      extends CollectionEventTypeModifyCommand

  final case class RemoveCollectionSpecimenDescriptionCmd(userId:                Option[String],
                                                          studyId:               String,
                                                          id:                    String,
                                                          expectedVersion:       Long,
                                                          specimenDescriptionId: String)
      extends CollectionEventTypeModifyCommand


  //--

  implicit val addCollectionEventTypeCmdReads: Reads[AddCollectionEventTypeCmd] =
    Json.reads[AddCollectionEventTypeCmd]

  implicit val removeCollectionEventTypeCmdReads: Reads[RemoveCollectionEventTypeCmd] =
    Json.reads[RemoveCollectionEventTypeCmd]

  implicit val updateCollectionEventTypeNameCmdReads: Reads[UpdateCollectionEventTypeNameCmd] =
    Json.reads[UpdateCollectionEventTypeNameCmd]

  implicit val updateCollectionEventTypeDescriptionCmdReads: Reads[UpdateCollectionEventTypeDescriptionCmd] =
    Json.reads[UpdateCollectionEventTypeDescriptionCmd]

  implicit val updateCollectionEventTypeRecurringCmdReads: Reads[UpdateCollectionEventTypeRecurringCmd] =
    Json.reads[UpdateCollectionEventTypeRecurringCmd]

  implicit val collectionEventTypeAddAnnotationTypeCmdReads: Reads[CollectionEventTypeAddAnnotationTypeCmd] =
    Json.reads[CollectionEventTypeAddAnnotationTypeCmd]

  implicit val collectionEventTypeUpdateAnnotationTypeCmdReads: Reads[CollectionEventTypeUpdateAnnotationTypeCmd] =
    Json.reads[CollectionEventTypeUpdateAnnotationTypeCmd]

  implicit val removeCollectionEventAnnotationTypeCmdReads: Reads[RemoveCollectionEventTypeAnnotationTypeCmd] =
    Json.reads[RemoveCollectionEventTypeAnnotationTypeCmd]

  implicit val addCollectionSpecimenDescriptionCmdReads: Reads[AddCollectionSpecimenDescriptionCmd] =
    Json.reads[AddCollectionSpecimenDescriptionCmd]

  implicit val updateCollectionSpecimenDescriptionCmdReads: Reads[UpdateCollectionSpecimenDescriptionCmd] =
    Json.reads[UpdateCollectionSpecimenDescriptionCmd]

  implicit val removeCollectionSpecimenDescriptionCmdReads: Reads[RemoveCollectionSpecimenDescriptionCmd] =
    Json.reads[RemoveCollectionSpecimenDescriptionCmd]

}
