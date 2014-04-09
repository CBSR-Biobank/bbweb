package infrastructure.command

import domain.study._
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._
import domain.AnnotationValueType._
import infrastructure.command.Commands._

object StudyCommands {
  // study commands
  trait StudyCommand extends Command
  trait StudyIdentity { val studyId: String }

  case class AddStudyCmd(
    name: String,
    description: Option[String] = None)
    extends StudyCommand

  case class UpdateStudyCmd(
    id: String,
    expectedVersion: Option[Long],
    name: String,
    description: Option[String] = None)
    extends StudyCommand with HasExpectedVersion

  case class EnableStudyCmd(
    id: String,
    expectedVersion: Option[Long])
    extends StudyCommand with HasExpectedVersion

  case class DisableStudyCmd(
    id: String,
    expectedVersion: Option[Long])
    extends StudyCommand with HasExpectedVersion

  // specimen group commands
  trait SpecimenGroupCommand extends StudyCommand with StudyIdentity

  case class AddSpecimenGroupCmd(
    studyId: String,
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType)
    extends SpecimenGroupCommand

  case class UpdateSpecimenGroupCmd(
    id: String,
    expectedVersion: Option[Long],
    studyId: String,
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType)
    extends SpecimenGroupCommand with HasExpectedVersion

  case class RemoveSpecimenGroupCmd(
    id: String,
    expectedVersion: Option[Long],
    studyId: String)
    extends SpecimenGroupCommand with HasExpectedVersion

  // collection event commands
  trait CollectionEventTypeCommand extends StudyCommand with StudyIdentity

  case class AddCollectionEventTypeCmd(
    studyId: String,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: Set[CollectionEventTypeSpecimenGroup],
    annotationTypeData: Set[CollectionEventTypeAnnotationType])
    extends CollectionEventTypeCommand

  case class UpdateCollectionEventTypeCmd(
    id: String,
    expectedVersion: Option[Long],
    studyId: String,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: Set[CollectionEventTypeSpecimenGroup],
    annotationTypeData: Set[CollectionEventTypeAnnotationType])
    extends CollectionEventTypeCommand with HasExpectedVersion

  case class RemoveCollectionEventTypeCmd(
    id: String,
    expectedVersion: Option[Long],
    studyId: String)
    extends CollectionEventTypeCommand with HasExpectedVersion

  // study annotation type commands
  trait StudyAnnotationTypeCommand extends StudyCommand with StudyIdentity

  // collection event annotation type commands
  trait CollectionEventAnnotationTypeCommand extends StudyAnnotationTypeCommand

  case class AddCollectionEventAnnotationTypeCmd(studyId: String,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Map[String, String]] = None)
    extends CollectionEventAnnotationTypeCommand

  case class UpdateCollectionEventAnnotationTypeCmd(
    id: String,
    expectedVersion: Option[Long],
    studyId: String,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Map[String, String]] = None)
    extends CollectionEventAnnotationTypeCommand with HasExpectedVersion

  case class RemoveCollectionEventAnnotationTypeCmd(
    id: String,
    expectedVersion: Option[Long],
    studyId: String)
    extends CollectionEventAnnotationTypeCommand with HasExpectedVersion

  // participant annotation type
  trait ParticipantAnnotationTypeCommand extends StudyAnnotationTypeCommand

  case class AddParticipantAnnotationTypeCmd(
    studyId: String,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Map[String, String]] = None,
    required: Boolean = false)
    extends ParticipantAnnotationTypeCommand

  case class UpdateParticipantAnnotationTypeCmd(
    id: String,
    expectedVersion: Option[Long],
    studyId: String,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Map[String, String]] = None,
    required: Boolean = false)
    extends ParticipantAnnotationTypeCommand with HasExpectedVersion

  case class RemoveParticipantAnnotationTypeCmd(
    id: String,
    expectedVersion: Option[Long],
    studyId: String)
    extends ParticipantAnnotationTypeCommand with HasExpectedVersion

  // specimen link annotation type
  trait SpecimenLinkAnnotationTypeCommand extends StudyAnnotationTypeCommand

  case class AddSpecimenLinkAnnotationTypeCmd(
    studyId: String,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Map[String, String]] = None)
    extends SpecimenLinkAnnotationTypeCommand

  case class UpdateSpecimenLinkAnnotationTypeCmd(
    id: String,
    expectedVersion: Option[Long],
    studyId: String,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Map[String, String]] = None)
    extends SpecimenLinkAnnotationTypeCommand with HasExpectedVersion

  case class RemoveSpecimenLinkAnnotationTypeCmd(
    id: String,
    expectedVersion: Option[Long],
    studyId: String)
    extends SpecimenLinkAnnotationTypeCommand with HasExpectedVersion

}
