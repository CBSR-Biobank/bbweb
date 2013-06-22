package domain.study

import domain._
import domain.AnnotationValueType._

import scalaz._
import Scalaz._

abstract class StudyAnnotationType extends AnnotationType {

  val studyId: StudyId
  val name: String
  val description: Option[String]
  val valueType: AnnotationValueType
  val maxValueCount: Option[Int]
  val options: Option[Map[String, String]]

}
