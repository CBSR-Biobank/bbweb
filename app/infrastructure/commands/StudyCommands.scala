package infrastructure.commands

import domain.study.CollectionEventId
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._
import domain.AnnotationValueType._

// study commands
trait StudyCommand extends Command {}
trait StudyId { val studyId: String }

case class AddStudyCmd(name: String, description: Option[String]) extends StudyCommand

case class UpdateStudyCmd(id: String, expectedVersion: Option[Long], name: String,
  description: Option[String])
  extends StudyCommand with Identity with ExpectedVersion

case class EnableStudyCmd(id: String, expectedVersion: Option[Long])
  extends StudyCommand with Identity with ExpectedVersion

case class DisableStudyCmd(studyId: String, expectedVersion: Option[Long])
  extends StudyCommand with Identity with ExpectedVersion

// specimen group commands
trait SpecimenGroupCommand extends StudyCommand with StudyId

case class AddSpecimenGroupCmd(studyId: String, name: String, description: Option[String],
  units: String, anatomicalSourceType: AnatomicalSourceType, preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType, specimenType: SpecimenType)
  extends SpecimenGroupCommand

case class UpdateSpecimenGroupCmd(specimenGroupId: String, expectedVersion: Option[Long],
  studyId: String, name: String, description: Option[String], units: String,
  anatomicalSourceType: AnatomicalSourceType, preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType, specimenType: SpecimenType)
  extends SpecimenGroupCommand with Identity with ExpectedVersion

case class RemoveSpecimenGroupCmd(specimenGroupId: String, expectedVersion: Option[Long])
  extends SpecimenGroupCommand with Identity with ExpectedVersion

// collection event commands
sealed trait CollectionEventTypeCommand extends StudyCommand {
}
case class AddCollectionEventTypeCmd(studyIdentity: String, name: String,
  description: Option[String], recurring: Boolean)
  extends { val studyId = studyIdentity } with CollectionEventTypeCommand

case class UpdateCollectionEventTypeCmd(collectionEventTypeId: String,
  expectedVersion: Option[Long], studyIdentity: String, name: String, description: String,
  recurring: Boolean) extends { val studyId = studyIdentity } with CollectionEventTypeCommand

case class RemoveCollectionEventTypeCmd(collectionEventTypeId: String, expectedVersion: Option[Long],
  studyIdentity: String)
  extends { val studyId = studyIdentity } with CollectionEventTypeCommand

case class AddSpecimenGroupToCollectionEventTypeCmd(studyIdentity: String,
  specimenGroupId: String, collectionEventTypeId: String, count: Int, amount: BigDecimal)
  extends { val studyId = studyIdentity } with CollectionEventTypeCommand

case class RemoveSpecimenGroupFromCollectionEventTypeCmd(sg2cetId: String, studyIdentity: String)
  extends { val studyId = studyIdentity } with CollectionEventTypeCommand

case class AddAnnotationTypeToCollectionEventTypeCmd(studyIdentity: String,
  collectionEventTypeId: String, annotationTypeId: String, required: Boolean)
  extends { val studyId = studyIdentity } with CollectionEventTypeCommand

case class RemoveAnnotationTypeFromCollectionEventTypeCmd(cetAtId: String, studyIdentity: String)
  extends { val studyId = studyIdentity } with CollectionEventTypeCommand

// study annotation type commands
sealed trait StudyAnnotationTypeCommand extends StudyCommand {
}
case class AddCollectionEventAnnotationTypeCmd(studyIdentity: String,
  name: String, description: Option[String], valueType: AnnotationValueType,
  maxValueCount: Option[Int], options: Option[Map[String, String]])
  extends { val studyId = studyIdentity } with StudyAnnotationTypeCommand

case class UpdateCollectionEventAnnotationTypeCmd(annotationTypeId: String,
  expectedVersion: Option[Long], studyIdentity: String, name: String,
  description: Option[String], valueType: AnnotationValueType, maxValueCount: Int,
  options: Map[String, String])
  extends { val studyId = studyIdentity } with StudyAnnotationTypeCommand

case class RemoveCollectionEventAnnotationTypeCmd(annotationTypeId: String,
  expectedVersion: Option[Long], studyIdentity: String)
  extends { val studyId = studyIdentity } with StudyAnnotationTypeCommand
