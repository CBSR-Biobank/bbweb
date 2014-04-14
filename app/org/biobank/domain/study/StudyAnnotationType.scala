package org.biobank.domain.study

import org.biobank.domain._
import org.biobank.domain.AnnotationValueType._

trait StudyAnnotationType extends AnnotationType {

  val studyId: StudyId

}

