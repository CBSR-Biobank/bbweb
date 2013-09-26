package domain.study

import domain._
import domain.AnnotationValueType._

abstract class StudyAnnotationType extends AnnotationType {

  val studyId: StudyId

}
