package service.commands

import domain.AnatomicalSourceId
import domain.study.CollectionEventId
import domain.study.SpecimenTypeId
import domain.PreservationId

// study commands
case class AddStudyCmd(name: String, description: String)
case class UpdateStudy(id: String, expectedVersion: Long, name: String, description: String)
case class EnableStudy(id: String, expectedVersion: Long)
case class DisableStudy(id: String, expectedVersion: Long)

// specimen group commands
case class AddSpecimenGroupCmd(studyId: String, name: String, description: String, units: String,
  anatomicalSourceId: AnatomicalSourceId, preservationId: PreservationId, specimenTypeId: SpecimenTypeId)
case class UpdateSpecimenGroupCmd(studyId: String, specimenGroupId: String,
  expectedVersion: Long, name: String, description: String, units: String,
  anatomicalSourceId: AnatomicalSourceId, preservationId: PreservationId,
  specimenTypeId: SpecimenTypeId)
case class RemoveSpecimenGroupCmd(studyId: String, expectedVersion: Long,
  specimenGroupId: String)

// collection event commands
case class AddCollectionEventType(studyId: String, expectedVersion: Option[Long], name: String,
  description: String, recurring: Boolean);
case class UpdateCollectionEventType(studyId: String, expectedVersion: Option[Long],
  collectionEventId: CollectionEventId, name: String, description: String, recurring: Boolean);