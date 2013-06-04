package domain.study

import domain.AnatomicalSourceId
import domain.DomainError
import domain.DomainValidation
import domain.ConcurrencySafeEntity
import domain.Identity
import domain.PreservationId
import scalaz._
import scalaz.Scalaz._

sealed abstract class Study extends ConcurrencySafeEntity[StudyId] {
  def name: String
  def description: String

  override def toString =
    "{ id:%s, name:%s, description:%s }" format (id.toString, name, description)
}

object Study {

  def add(name: String, description: String): DomainValidation[DisabledStudy] =
    DisabledStudy(StudyIdentityService.nextIdentity, version = 0L, name, description).success
}

case class DisabledStudy(id: StudyId, version: Long = -1, name: String, description: String)
  extends Study {

  def addSpecimenGroup(specimenGroups: Map[SpecimenGroupId, SpecimenGroup], studyId: StudyId,
    name: String, description: String, units: String, anatomicalSourceId: AnatomicalSourceId,
    preservationId: PreservationId, specimenTypeId: SpecimenTypeId): DomainValidation[SpecimenGroup] =
    specimenGroups.find(sg => sg._2.name.equals(name)) match {
      case Some(sg) => DomainError("specimen group with name already exists: %s" format name).fail
      case None =>
        SpecimenGroup.add(studyId, name, description, units, anatomicalSourceId, preservationId,
          specimenTypeId)
    }

  def enable(specimenGroupCount: Int, collectionEventTypecount: Int): DomainValidation[EnabledStudy] =
    if ((specimenGroupCount == 0) || (collectionEventTypecount == 0))
      DomainError("study has no specimen groups and / or no collection event types").fail
    else
      EnabledStudy(id, version + 1, name, description).success

}

case class EnabledStudy(id: StudyId, version: Long = -1, name: String, description: String)
  extends Study {

  def disable: DomainValidation[DisabledStudy] =
    DisabledStudy(id, version + 1, name, description).success
}

