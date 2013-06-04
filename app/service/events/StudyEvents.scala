package service.events

import domain.study.SpecimenTypeId
import domain.study.StudyId
import domain.study.SpecimenGroupId
import domain.PreservationId
import domain.AnatomicalSourceId
import domain.UserId

// study events
//
// FIXME: need a base class here
case class StudyAddedEvent(id: StudyId, name: String, description: String)
case class StudyUpdatedEvent(id: StudyId, name: String, description: String)
case class StudyEnabledEvent(id: StudyId)
case class StudyDisabledEvent(id: StudyId)

case class StudySpecimenGroupAddedEvent(studyId: StudyId, specimenGroupId: SpecimenGroupId,
  name: String, description: String, units: String, amatomicalSourceId: AnatomicalSourceId,
  preservationId: PreservationId, specimenTypeId: SpecimenTypeId)
case class StudySpecimenGroupUpdatedEvent(studyId: StudyId, specimenGroupId: SpecimenGroupId,
  name: String, description: String, units: String, amatomicalSourceId: AnatomicalSourceId,
  preservationId: PreservationId, specimenTypeId: SpecimenTypeId)
case class StudySpecimenGroupRemovedEvent(studyId: StudyId, specimenGroupId: SpecimenGroupId)
