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

}
