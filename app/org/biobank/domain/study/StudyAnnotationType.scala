package org.biobank.domain.study

import org.biobank.domain._
import org.biobank.domain.AnnotationValueType._

abstract class StudyAnnotationType extends AnnotationType {

  val studyId: StudyId

}
