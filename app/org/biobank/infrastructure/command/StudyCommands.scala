package org.biobank.infrastructure.command

import org.biobank.infrastructure._
import org.biobank.domain.study._
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._
import org.biobank.domain.AnnotationValueType._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.Commands._

import scala.collection.immutable

object StudyCommands {
  // study commands
  trait StudyCommand extends Command
  trait HasStudyIdentity { val studyId: String }

  case class AddStudyCmd(
    name: String,
    description: Option[String] = None)
      extends StudyCommand

  case class UpdateStudyCmd(
    id: String,
    expectedVersion: Option[Long],
    name: String,
    description: Option[String] = None)
      extends StudyCommand
      with HasIdentity
      with HasExpectedVersion

  case class EnableStudyCmd(
    id: String,
    expectedVersion: Option[Long])
      extends StudyCommand
      with HasIdentity
      with HasExpectedVersion

  case class DisableStudyCmd(
    id: String,
    expectedVersion: Option[Long])
      extends StudyCommand
      with HasIdentity
      with HasExpectedVersion

  case class RetireStudyCmd(
    id: String,
    expectedVersion: Option[Long])
      extends StudyCommand
      with HasIdentity
      with HasExpectedVersion

  case class UnetireStudyCmd(
    id: String,
    expectedVersion: Option[Long])
      extends StudyCommand
      with HasIdentity
      with HasExpectedVersion

  // specimen group commands
  trait SpecimenGroupCommand extends StudyCommand with HasStudyIdentity

  case class AddSpecimenGroupCmd(
    studyId: String,
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType)
      extends SpecimenGroupCommand with HasStudyIdentity

  case class UpdateSpecimenGroupCmd(
    studyId: String,
    id: String,
    expectedVersion: Option[Long],
    name: String,
    description: Option[String],
    units: String,
    anatomicalSourceType: AnatomicalSourceType,
    preservationType: PreservationType,
    preservationTemperatureType: PreservationTemperatureType,
    specimenType: SpecimenType)
      extends SpecimenGroupCommand
      with HasIdentity
      with HasStudyIdentity with HasExpectedVersion

  case class RemoveSpecimenGroupCmd(
    studyId: String,
    id: String,
    expectedVersion: Option[Long])
      extends SpecimenGroupCommand
      with HasIdentity
      with HasExpectedVersion

  // collection event commands
  trait CollectionEventTypeCommand extends StudyCommand with HasStudyIdentity

  case class AddCollectionEventTypeCmd(
    studyId: String,
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: List[CollectionEventTypeSpecimenGroup],
    annotationTypeData: List[CollectionEventTypeAnnotationType])
      extends CollectionEventTypeCommand with HasStudyIdentity

  case class UpdateCollectionEventTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Option[Long],
    name: String,
    description: Option[String],
    recurring: Boolean,
    specimenGroupData: List[CollectionEventTypeSpecimenGroup],
    annotationTypeData: List[CollectionEventTypeAnnotationType])
      extends CollectionEventTypeCommand
      with HasIdentity
      with HasExpectedVersion

  case class RemoveCollectionEventTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Option[Long])
      extends CollectionEventTypeCommand
      with HasIdentity
      with HasExpectedVersion

  // study annotation type commands
  trait StudyAnnotationTypeCommand extends StudyCommand with HasStudyIdentity

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
    studyId: String,
    id: String,
    expectedVersion: Option[Long],
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Map[String, String]] = None)
      extends CollectionEventAnnotationTypeCommand
      with HasIdentity
      with HasStudyIdentity
      with HasExpectedVersion

  case class RemoveCollectionEventAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Option[Long])
      extends CollectionEventAnnotationTypeCommand
      with HasIdentity
      with HasStudyIdentity
      with HasExpectedVersion

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
      with HasStudyIdentity

  case class UpdateParticipantAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Option[Long],
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Map[String, String]] = None,
    required: Boolean = false)
      extends ParticipantAnnotationTypeCommand
      with HasIdentity
      with HasStudyIdentity
      with HasExpectedVersion

  case class RemoveParticipantAnnotationTypeCmd(
    studyId: String,
    id: String,
    expectedVersion: Option[Long])
      extends ParticipantAnnotationTypeCommand
      with HasIdentity
      with HasExpectedVersion

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
      with HasStudyIdentity

  case class UpdateSpecimenLinkAnnotationTypeCmd(
    id: String,
    expectedVersion: Option[Long],
    studyId: String,
    name: String,
    description: Option[String],
    valueType: AnnotationValueType,
    maxValueCount: Option[Int] = None,
    options: Option[Map[String, String]] = None)
      extends SpecimenLinkAnnotationTypeCommand
      with HasIdentity
      with HasStudyIdentity
      with HasExpectedVersion

  case class RemoveSpecimenLinkAnnotationTypeCmd(
    id: String,
    expectedVersion: Option[Long],
    studyId: String)
      extends SpecimenLinkAnnotationTypeCommand
      with HasIdentity
      with HasStudyIdentity
      with HasExpectedVersion

}
