package infrastructure.commands

import domain.study.CollectionEventId
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._
import domain.AnnotationValueType._

// study commands
sealed trait StudyCommand extends Command {
  val studyId: String
}
case class AddStudyCmd(name: String, description: String)
case class UpdateStudyCmd(studyId: String, expectedVersion: Option[Long], name: String,
  description: String) extends StudyCommand
case class EnableStudyCmd(studyId: String, expectedVersion: Option[Long])
  extends StudyCommand
case class DisableStudyCmd(studyId: String, expectedVersion: Option[Long])
  extends StudyCommand

// specimen group commands
sealed trait SpecimenGroupCommand extends StudyCommand {
}
case class AddSpecimenGroupCmd(
  studyIdentity: String, name: String, description: String, units: String,
  anatomicalSourceType: AnatomicalSourceType, preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType, specimenType: SpecimenType)
  extends { val studyId = studyIdentity } with SpecimenGroupCommand

case class UpdateSpecimenGroupCmd(studyIdentity: String, specimenGroupId: String,
  expectedVersion: Option[Long], name: String, description: String, units: String,
  anatomicalSourceType: AnatomicalSourceType, preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType, specimenType: SpecimenType)
  extends { val studyId = studyIdentity } with SpecimenGroupCommand

case class RemoveSpecimenGroupCmd(studyIdentity: String, specimenGroupId: String,
  expectedVersion: Option[Long])
  extends { val studyId = studyIdentity } with SpecimenGroupCommand

// collection event commands
sealed trait CollectionEventTypeCommand extends StudyCommand {
}
case class AddCollectionEventTypeCmd(studyIdentity: String, name: String, description: String,
  recurring: Boolean)
  extends { val studyId = studyIdentity } with CollectionEventTypeCommand

case class UpdateCollectionEventTypeCmd(studyIdentity: String, collectionEventTypeId: String,
  expectedVersion: Option[Long], name: String, description: String,
  recurring: Boolean) extends { val studyId = studyIdentity } with CollectionEventTypeCommand

case class RemoveCollectionEventTypeCmd(studyIdentity: String, collectionEventTypeId: String,
  expectedVersion: Option[Long])
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
case class AddCollectionEventAnnotationTypeCmd(studyIdentity: String, name: String,
  description: String, valueType: AnnotationValueType, maxValueCount: Int,
  options: Map[String, String])
  extends { val studyId = studyIdentity } with StudyAnnotationTypeCommand

case class UpdateCollectionEventAnnotationTypeCmd(studyIdentity: String,
  annotationTypeId: String, expectedVersion: Option[Long], name: String,
  description: String, valueType: AnnotationValueType, maxValueCount: Int,
  options: Map[String, String])
  extends { val studyId = studyIdentity } with StudyAnnotationTypeCommand

case class RemoveCollectionEventAnnotationTypeCmd(studyIdentity: String,
  annotationTypeId: String, expectedVersion: Option[Long])
  extends { val studyId = studyIdentity } with StudyAnnotationTypeCommand
