package org.biobank.domain

import scalaz.Scalaz._

trait HasAnnotations[T <: ConcurrencySafeEntity[_]] {

  val annotations: Set[Annotation]

  def withAnnotation(annotation: Annotation): DomainValidation[T]

  def withoutAnnotation(annotationTypeId: AnnotationTypeId): DomainValidation[T]

  /** removes an annotation type. */
  protected def checkRemoveAnnotation(annotationTypeId: AnnotationTypeId)
      : DomainValidation[Annotation] = {
    annotations
      .find { x => x.annotationTypeId == annotationTypeId }
      .toSuccessNel(s"annotation does not exist: $annotationTypeId")
  }

}
