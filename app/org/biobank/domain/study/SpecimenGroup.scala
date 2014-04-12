package org.biobank.domain.study

import org.biobank.infrastructure._
import org.biobank.domain._
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.SpecimenType._

import play.api.libs.json._
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
  specimenType: SpecimenType)
  extends ConcurrencySafeEntity[SpecimenGroupId]
  with HasName with HasDescriptionOption {

  val toStringFormat = """SpecimenGroup:{ id: %s, version: %d, studyId: %s, name: %s, description: %s,""" +
    """ units: %s, anatomicalSourceType: %s, preservationType: %s,""" +
    """ preservationTemperatureType: %s, specimenType: %s }"""

  override def toString: String = {
    toStringFormat.format(id, version, studyId, name, description, units,
      anatomicalSourceType, preservationType, preservationTemperatureType, specimenType)
  }
}

