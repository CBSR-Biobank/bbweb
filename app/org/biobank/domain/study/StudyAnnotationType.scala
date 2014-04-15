package org.biobank.domain.study

import org.biobank.domain._
import org.biobank.domain.AnnotationValueType._

/**
  * StudyAnnotationTypes allow a study to collect custom named and defined pieces of data on
  * [[CollectionEvent]]s, [[ProcessingEvent]]s, and [[Participant]]s. Annotations are
  * optional and are not a requirement for specimen collection or processing.
  */
trait StudyAnnotationType extends AnnotationType {

  val studyId: StudyId

}

