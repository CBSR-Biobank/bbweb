package org.biobank.domain.study

import org.biobank.infrastructure._
import org.biobank.domain.{
  AnnotationTypeId,
  DomainError,
  DomainValidation
}
import org.biobank.domain.AnatomicalSourceType._
import org.biobank.domain.AnnotationValueType._
import org.biobank.domain.PreservationTemperatureType._
import org.biobank.domain.PreservationType._
import org.biobank.domain.SpecimenType._

import scalaz._
import Scalaz._

case class DisabledStudy(
  id: StudyId,
  version: Long = -1,
  name: String,
  description: Option[String])
  extends Study {

  override val status = "Disabled"

}
