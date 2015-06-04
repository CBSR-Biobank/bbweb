package org.biobank.domain.study

import org.biobank.domain.{
  AnnotationTypeValidations,
  ValidationKey }
import org.biobank.domain.AnnotationType

/**
  * StudyAnnotationTypes allow a study to collect custom named and defined pieces of data on
  * [[CollectionEvent]]s, [[ProcessingEvent]]s, and [[Participant]]s. Annotations are
  * optional and are not a requirement for specimen collection or processing.
  */
trait StudyAnnotationType extends AnnotationType with HasStudyId

trait StudyAnnotationTypeValidations extends AnnotationTypeValidations {

  case object StudyIdRequired extends ValidationKey

}
