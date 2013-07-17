package domain.study

import infrastructure._
import domain.{
  AnnotationTypeId,
  DomainError,
  DomainValidation
}
import domain.AnatomicalSourceType._
import domain.AnnotationValueType._
import domain.PreservationTemperatureType._
import domain.PreservationType._
import domain.SpecimenType._

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
