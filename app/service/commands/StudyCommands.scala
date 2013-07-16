package service.commands

import domain.study.CollectionEventId
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._
import domain.AnnotationValueType._

// study commands
trait StudyCommand extends Command
trait StudyIdentity { val studyId: String }

case class AddStudyCmd(name: String, description: Option[String] = None) extends StudyCommand

case class UpdateStudyCmd(id: String, expectedVersion: Option[Long], name: String,
  description: Option[String] = None)
  extends StudyCommand with HasExpectedVersion

case class EnableStudyCmd(id: String, expectedVersion: Option[Long])
  extends StudyCommand with HasExpectedVersion

case class DisableStudyCmd(id: String, expectedVersion: Option[Long])
  extends StudyCommand with HasExpectedVersion

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
  extends SpecimenGroupCommand with HasExpectedVersion

case class RemoveSpecimenGroupCmd(id: String, expectedVersion: Option[Long], studyId: String)
  extends SpecimenGroupCommand with HasExpectedVersion

// study annotation type commands
trait StudyAnnotationTypeCommand extends StudyCommand with StudyIdentity

case class AddCollectionEventAnnotationTypeCmd(studyId: String,
  name: String, description: Option[String], valueType: AnnotationValueType,
  maxValueCount: Option[Int] = None, options: Option[Map[String, String]] = None)
  extends StudyAnnotationTypeCommand

case class UpdateCollectionEventAnnotationTypeCmd(id: String,
  expectedVersion: Option[Long], studyId: String, name: String,
  description: Option[String], valueType: AnnotationValueType, maxValueCount: Option[Int] = None,
  options: Option[Map[String, String]] = None)
  extends StudyAnnotationTypeCommand with HasExpectedVersion

case class RemoveCollectionEventAnnotationTypeCmd(id: String,
  expectedVersion: Option[Long], studyId: String)
  extends StudyAnnotationTypeCommand with HasExpectedVersion

// collection event commands
trait CollectionEventTypeCommand extends StudyCommand with StudyIdentity

case class AddCollectionEventTypeCmd(
  studyId: String, name: String, description: Option[String], recurring: Boolean,
  specimenGroupIds: Set[String], annotationTypeIds: Set[String])
  extends CollectionEventTypeCommand

case class UpdateCollectionEventTypeCmd(
  id: String, expectedVersion: Option[Long], studyId: String, name: String,
  description: Option[String], recurring: Boolean, specimenGroupIds: Set[String],
  annotationTypeIds: Set[String])
  extends CollectionEventTypeCommand with HasExpectedVersion

case class RemoveCollectionEventTypeCmd(
  id: String, expectedVersion: Option[Long], studyId: String)
  extends CollectionEventTypeCommand with HasExpectedVersion

