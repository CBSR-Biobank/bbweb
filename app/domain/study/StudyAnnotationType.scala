package domain.study

import domain._
import domain.AnnotationValueType._

import scalaz._
import Scalaz._

abstract class StudyAnnotationType extends AnnotationType {

  val studyId: StudyId

}
