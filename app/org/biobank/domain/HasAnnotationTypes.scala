package org.biobank.domain

import scalaz.Scalaz._

trait HasAnnotationTypes extends AnnotationTypeValidations {

  val annotationTypes: Set[AnnotationType]

  /** adds an annotation type. */
  protected def checkAddAnnotationType(annotationType: AnnotationType)
      : DomainValidation[Boolean] = {
    (validate(annotationType) |@| nameNotUsed(annotationType)) {
      case (_, _) => true
    }
  }

  /** removes an annotation type. */
  protected def checkRemoveAnnotationType(annotationTypeId: String)
      : DomainValidation[AnnotationType] = {
    annotationTypes
      .find { x => x.uniqueId == annotationTypeId }
      .toSuccessNel(s"annotation type does not exist: $annotationTypeId")
  }

  protected def nameNotUsed(annotationType: AnnotationType): DomainValidation[Boolean] = {
    annotationTypes
      .find { x => (x.name == annotationType.name) && (x.uniqueId != annotationType.uniqueId)  }
      .fold
      { true.successNel[DomainError] }
      { _ => DomainError(s"annotation type name already used: ${annotationType.name}").failureNel[Boolean] }
  }

}
