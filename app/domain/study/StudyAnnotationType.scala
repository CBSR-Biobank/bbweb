package domain.study

import domain._
import domain.AnnotationValueType._

import scalaz._
import Scalaz._

abstract class StudyAnnotationType extends AnnotationType {

  val studyId: StudyId
  val name: String
  val description: String
  val valueType: AnnotationValueType
  val maxValueCount: Int

}
