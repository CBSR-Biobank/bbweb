package org.biobank.domain.study

import org.biobank.domain.{
  AnnotationTypeId,
  AnnotationTypeValidations,
  DomainValidation,
  DomainError,
  ValidationKey }
import org.biobank.domain.AnnotationType
import org.biobank.domain.AnnotationValueType._
import org.biobank.infrastructure.AnnotationTypeData

import scalaz._
import scalaz.Scalaz._

/**
  * StudyAnnotationTypes allow a study to collect custom named and defined pieces of data on
  * [[CollectionEvent]]s, [[ProcessingEvent]]s, and [[Participant]]s. Annotations are
  * optional and are not a requirement for specimen collection or processing.
  */
trait StudyAnnotationType extends AnnotationType with HasStudyId

trait StudyAnnotationTypeValidations extends AnnotationTypeValidations {
  import org.biobank.domain.CommonValidations._

  case object StudyIdRequired extends ValidationKey

}
