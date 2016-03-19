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

  case class AddCollectionEventTypeCmd(
    userId:             Option[String],
    studyId:            String,
    name:               String,
    description:        Option[String],
    recurring:          Boolean)
      extends CollectionEventTypeCommand

  case class UpdateCollectionEventTypeNameCmd(
    userId:          Option[String],
    studyId:         String,
    id:              String,
    expectedVersion: Long,
    name:            String)
      extends CollectionEventTypeModifyCommand

  case class UpdateCollectionEventTypeDescriptionCmd(
    userId:          Option[String],
    studyId:         String,
    id:              String,
    expectedVersion: Long,
    description:     Option[String])
      extends CollectionEventTypeModifyCommand

  case class UpdateCollectionEventTypeRecurringCmd(
    userId:          Option[String],
    studyId:         String,
    id:              String,
    expectedVersion: Long,
    recurring:       Boolean)
      extends CollectionEventTypeModifyCommand

  case class RemoveCollectionEventTypeCmd(
    userId:          Option[String],
    studyId:         String,
    id:              String,
    expectedVersion: Long)
      extends CollectionEventTypeModifyCommand

  case class CollectionEventTypeAddAnnotationTypeCmd(
    userId:          Option[String],
    studyId:         String,
    id:              String,
    expectedVersion: Long,
    name:            String,
    description:     Option[String],
    valueType:       AnnotationValueType,
    maxValueCount:   Option[Int] = None,
    options:         Seq[String],
    required:        Boolean)
      extends CollectionEventTypeModifyCommand

  case class CollectionEventTypeUpdateAnnotationTypeCmd(
    userId:          Option[String],
    studyId:         String,
    id:              String,
    expectedVersion: Long,
    uniqueId:        String,
    name:            String,
    description:     Option[String],
    valueType:       AnnotationValueType,
    maxValueCount:   Option[Int] = None,
    options:         Seq[String],
    required:        Boolean)
      extends CollectionEventTypeModifyCommand

  case class RemoveCollectionEventTypeAnnotationTypeCmd(
    userId:          Option[String],
    studyId:         String,
    id:              String,
    expectedVersion: Long,
    uniqueId:        String)
      extends CollectionEventTypeModifyCommand

  case class AddCollectionSpecimenSpecCmd(
    userId:                      Option[String],
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
    amount:                      Option[BigDecimal])
      extends CollectionEventTypeModifyCommand

  case class UpdateCollectionSpecimenSpecCmd(
    userId:                      Option[String],
    studyId:                     String,
    id:                          String,
    expectedVersion:             Long,
    uniqueId:                    String,
    name:                        String,
    description:                 Option[String],
    units:                       String,
    anatomicalSourceType:        AnatomicalSourceType,
    preservationType:            PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType:                SpecimenType,
    maxCount:                    Int,
    amount:                      Option[BigDecimal])
      extends CollectionEventTypeModifyCommand

  case class RemoveCollectionSpecimenSpecCmd(
    userId:          Option[String],
    studyId:         String,
    id:              String,
    expectedVersion: Long,
    uniqueId:        String)
      extends CollectionEventTypeModifyCommand


  //--

  implicit val addCollectionEventTypeCmdReads = Json.reads[AddCollectionEventTypeCmd]
  implicit val removeCollectionEventTypeCmdReads = Json.reads[RemoveCollectionEventTypeCmd]
  implicit val updateCollectionEventTypeNameCmdReads = Json.reads[UpdateCollectionEventTypeNameCmd]
  implicit val updateCollectionEventTypeDescriptionCmdReads = Json.reads[UpdateCollectionEventTypeDescriptionCmd]
  implicit val updateCollectionEventTypeRecurringCmdReads = Json.reads[UpdateCollectionEventTypeRecurringCmd]
  implicit val collectionEventTypeAddAnnotationTypeCmdReads = Json.reads[CollectionEventTypeAddAnnotationTypeCmd]

  implicit val collectionEventTypeUpdateAnnotationTypeCmdReads = Json.reads[CollectionEventTypeUpdateAnnotationTypeCmd]
  implicit val removeCollectionEventAnnotationTypeCmdReads = Json.reads[RemoveCollectionEventTypeAnnotationTypeCmd]
  implicit val addCollectionSpecimenSpecCmdReads = Json.reads[AddCollectionSpecimenSpecCmd]
  implicit val updateCollectionSpecimenSpecCmdReads = Json.reads[UpdateCollectionSpecimenSpecCmd]
  implicit val removeCollectionSpecimenSpecCmdReads = Json.reads[RemoveCollectionSpecimenSpecCmd]

}
