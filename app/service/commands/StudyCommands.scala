package service.commands

import domain.study.CollectionEventId
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._

// study commands
case class AddStudyCmd(name: String, description: String)
case class UpdateStudyCmd(studyId: String, expectedVersion: Option[Long], name: String, description: String)
case class EnableStudyCmd(studyId: String, expectedVersion: Option[Long])
case class DisableStudyCmd(studyId: String, expectedVersion: Option[Long])

// specimen group commands
case class AddSpecimenGroupCmd(studyId: String, name: String, description: String, units: String,
  anatomicalSourceType: AnatomicalSourceType, preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType, specimenType: SpecimenType)
case class UpdateSpecimenGroupCmd(studyId: String, specimenGroupId: String,
  expectedVersion: Option[Long], name: String, description: String, units: String,
  anatomicalSourceType: AnatomicalSourceType, preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType, specimenType: SpecimenType)
case class RemoveSpecimenGroupCmd(studyId: String, specimenGroupId: String,
  expectedVersion: Option[Long])

// collection event commands
case class AddCollectionEventType(studyId: String, expectedVersion: Option[Long], name: String,
  description: String, recurring: Boolean);
case class UpdateCollectionEventType(studyId: String, expectedVersion: Option[Long],
  collectionEventId: CollectionEventId, name: String, description: String, recurring: Boolean);