package domain

import domain.AnnotationValueType._
import infrastructure._

import scalaz._
import Scalaz._

abstract class AnnotationType extends ConcurrencySafeEntity[AnnotationTypeId] {

  val version: Long
  val name: String
  val description: String
  val valueType: AnnotationValueType
  val maxValueCount: Int
  val options: Map[String, String]

  private def validateValueType: DomainValidation[Boolean] = {
    if (valueType.equals(AnnotationValueType.Select)) {
      if (options.isEmpty) ("select annotation type with no values to select").fail
    } else {
      if (!options.isEmpty) ("non select annotation type with values to select").fail
    }
    true.success
  }

}
