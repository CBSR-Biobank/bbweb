package infrastructure.commands

import domain.study.CollectionEventId
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._
import domain.AnnotationValueType._

// study commands
trait StudyCommand extends Command
trait StudyIdentity { val studyId: String }

case class AddStudyCmd(name: String, description: Option[String]) extends StudyCommand

case class UpdateStudyCmd(id: String, expectedVersion: Option[Long], name: String,
  description: Option[String])
  extends StudyCommand with Identity with ExpectedVersion

case class EnableStudyCmd(id: String, expectedVersion: Option[Long])
  extends StudyCommand with Identity with ExpectedVersion

case class DisableStudyCmd(studyId: String, expectedVersion: Option[Long])
  extends StudyCommand with Identity with ExpectedVersion

// specimen group commands
trait SpecimenGroupCommand extends StudyCommand with StudyIdentity

case class AddSpecimenGroupCmd(studyId: String, name: String, description: Option[String],
  units: String, anatomicalSourceType: AnatomicalSourceType, preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType, specimenType: SpecimenType)
  extends SpecimenGroupCommand

case class UpdateSpecimenGroupCmd(id: String, expectedVersion: Option[Long],
  studyId: String, name: String, description: Option[String], units: String,
  anatomicalSourceType: AnatomicalSourceType, preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType, specimenType: SpecimenType)
  extends SpecimenGroupCommand with Identity with ExpectedVersion

case class RemoveSpecimenGroupCmd(id: String, expectedVersion: Option[Long], studyId: String)
  extends SpecimenGroupCommand with Identity with ExpectedVersion

// collection event commands
trait CollectionEventTypeCommand extends StudyCommand with StudyIdentity

case class AddCollectionEventTypeCmd(
  studyId: String, name: String, description: Option[String], recurring: Boolean)
  extends CollectionEventTypeCommand

case class UpdateCollectionEventTypeCmd(
  id: String, expectedVersion: Option[Long], studyId: String,
  name: String, description: Option[String], recurring: Boolean)
  extends CollectionEventTypeCommand with Identity with ExpectedVersion

case class RemoveCollectionEventTypeCmd(
  id: String, expectedVersion: Option[Long], studyId: String)
  extends CollectionEventTypeCommand with Identity with ExpectedVersion

case class AddSpecimenGroupToCollectionEventTypeCmd(
  studyId: String, specimenGroupId: String, collectionEventTypeId: String,
  count: Int, amount: BigDecimal)
  extends CollectionEventTypeCommand

case class RemoveSpecimenGroupFromCollectionEventTypeCmd(
  id: String, studyIdentity: String)
  extends CollectionEventTypeCommand with Identity

case class AddAnnotationTypeToCollectionEventTypeCmd(
  studyId: String, annotationTypeId: String, collectionEventTypeId: String, required: Boolean)
  extends CollectionEventTypeCommand

case class RemoveAnnotationTypeFromCollectionEventTypeCmd(
  id: String, studyId: String)
  extends CollectionEventTypeCommand with Identity

// study annotation type commands
trait StudyAnnotationTypeCommand extends StudyCommand with StudyIdentity

case class AddCollectionEventAnnotationTypeCmd(studyId: String,
  name: String, description: Option[String], valueType: AnnotationValueType,
  maxValueCount: Option[Int], options: Option[Map[String, String]])
  extends StudyAnnotationTypeCommand

case class UpdateCollectionEventAnnotationTypeCmd(annotationTypeId: String,
  expectedVersion: Option[Long], studyIdentity: String, name: String,
  description: Option[String], valueType: AnnotationValueType, maxValueCount: Option[Int],
  options: Option[Map[String, String]])
  extends StudyAnnotationTypeCommand with Identity with ExpectedVersion

case class RemoveCollectionEventAnnotationTypeCmd(id: String,
  expectedVersion: Option[Long], studyId: String)
  extends StudyAnnotationTypeCommand with Identity with ExpectedVersion

// To be used within the service and domain layers

case class AddSpecimenGroupCmdWithId(
  id: String, studyId: String, name: String, description: Option[String],
  units: String, anatomicalSourceType: AnatomicalSourceType, preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType, specimenType: SpecimenType)
  extends SpecimenGroupCommand with infrastructure.commands.Identity

case class AddCollectionEventTypeCmdWithId(
  id: String, studyId: String, name: String, description: Option[String], recurring: Boolean)
  extends CollectionEventTypeCommand with infrastructure.commands.Identity

case class AddCollectionEventAnnotationTypeCmdWithId(
  id: String, studyId: String, name: String, description: Option[String],
  valueType: AnnotationValueType, maxValueCount: Option[Int], options: Option[Map[String, String]])
  extends StudyAnnotationTypeCommand with infrastructure.commands.Identity

case class AddSpecimenGroupToCollectionEventTypeCmdWithId(
  id: String, studyId: String, specimenGroupId: String, collectionEventTypeId: String,
  count: Int, amount: BigDecimal)
  extends CollectionEventTypeCommand with infrastructure.commands.Identity

case class AddAnnotationTypeToCollectionEventTypeCmdWithId(
  id: String, studyId: String, annotationTypeId: String, collectionEventTypeId: String, required: Boolean)
  extends CollectionEventTypeCommand with infrastructure.commands.Identity
