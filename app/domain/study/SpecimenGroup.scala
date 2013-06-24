package domain.study

import infrastructure._
import domain._
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._

import scalaz._
import Scalaz._

case class SpecimenGroup(
  id: SpecimenGroupId,
  version: Long = -1,
  studyId: StudyId,
  name: String,
  description: Option[String],
  units: String,
  anatomicalSourceType: AnatomicalSourceType,
  preservationType: PreservationType,
  preservationTemperatureType: PreservationTemperatureType,
  specimenType: SpecimenType,
  addedBy: UserId,
  timeAdded: Long,
  updatedBy: Option[UserId] = None,
  timeUpdated: Option[Long] = None)
  extends ConcurrencySafeEntity[SpecimenGroupId]
  with HasName with HasDescriptionOption {

  val toStringFormat = """{ id: %s, version: %d, studyId: %s, name: %s, description: %s,""" +
    """ units: %s, anatomicalSourceType: %s, preservationType: %s,""" +
    """ preservationTemperatureType: %s, specimenType: %s }"""

  override def toString: String = {
    toStringFormat.format(id, version, studyId, name, description, units,
      anatomicalSourceType, preservationType, preservationTemperatureType, specimenType)
  }
}

