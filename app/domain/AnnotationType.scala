package domain

import domain.AnnotationValueType._
import infrastructure._

import scalaz._
import Scalaz._

abstract class AnnotationType
  extends ConcurrencySafeEntity[AnnotationTypeId]
  with HasName with HasDescriptionOption {

  val name: String
  val description: Option[String]
  val valueType: AnnotationValueType
  val maxValueCount: Option[Int]
  val options: Option[Map[String, String]]

  private def validateValueType: DomainValidation[Boolean] = {
    if (valueType.equals(AnnotationValueType.Select)) {
      if (options.isEmpty) ("select annotation type with no values to select").fail
    } else {
      if (!options.isEmpty) ("non select annotation type with values to select").fail
    }
    true.success
  }

}
