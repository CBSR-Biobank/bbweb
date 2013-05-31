package service.commands

import domain.AnatomicalSourceId
import domain.study.CollectionEventId
import domain.study.SpecimenTypeId
import domain.PreservationId

// study commands
case class AddStudyCmd(name: String, description: String)
case class UpdateStudy(id: String, expectedVersion: Option[Long], name: String, description: String)
case class EnableStudy(id: String, expectedVersion: Option[Long])
case class DisableStudy(id: String, expectedVersion: Option[Long])

// specimen group commands
case class AddSpecimenGroupCmd(studyId: String, expectedVersion: Option[Long],
  name: String, description: String, units: String, amatomicalSourceId: AnatomicalSourceId,
  preservationId: PreservationId, specimenTypeId: SpecimenTypeId)
case class UpdateSpecimenGroupCmd(studyId: String, expectedVersion: Option[Long],
  specimenGroupId: String, name: String, description: String, units: String,
  amatomicalSourceId: AnatomicalSourceId, preservationId: PreservationId,
  specimenTypeId: SpecimenTypeId)
case class RemoveSpecimenGroupCmd(studyId: String, expectedVersion: Option[Long],
  specimenGroupId: String)

// collection event commands
case class AddCollectionEventType(studyId: String, expectedVersion: Option[Long], name: String,
  description: String, recurring: Boolean);
case class UpdateCollectionEventType(studyId: String, expectedVersion: Option[Long],
  collectionEventId: CollectionEventId, name: String, description: String, recurring: Boolean);