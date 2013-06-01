package domain.study

import domain._
import service.commands._

import scalaz._
import scalaz.Scalaz._

case class SpecimenGroup(
  id: SpecimenGroupId,
  studyId: StudyId,
  version: Long = -1,
  name: String,
  description: String,
  units: String,
  anatomicalSourceId: AnatomicalSourceId,
  preservationId: PreservationId,
  specimenTypeId: SpecimenTypeId) extends Entity[SpecimenGroupId] {
}

object SpecimenGroup {

  // TODO: not sure yet if this is the right place for this method
  def nextIdentity: SpecimenGroupId =
    new SpecimenGroupId(java.util.UUID.randomUUID.toString.toUpperCase)

  def add(cmd: AddSpecimenGroupCmd): DomainValidation[SpecimenGroup] =
    SpecimenGroup(nextIdentity, new StudyId(cmd.studyId), version = 0L, cmd.name, cmd.description,
      cmd.units, cmd.anatomicalSourceId, cmd.preservationId, cmd.specimenTypeId).success

}
