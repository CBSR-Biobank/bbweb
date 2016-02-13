package org.biobank.domain

import scalaz.Scalaz._

trait HasAnnotationTypes extends AnnotationTypeValidations {

  val annotationTypes: Set[AnnotationType]

  /** adds a participant annotation type to this study. */
  protected def checkAddAnnotationType(annotationType: AnnotationType)
      : DomainValidation[Boolean] = {
    (validate(annotationType) |@| nameNotUsed(annotationType)) {
      case (_, _) => true
    }
  }

  /** removes a participant annotation type from this study. */
  protected def checkRemoveAnnotationType(annotationTypeUniqueId: String)
      : DomainValidation[AnnotationType] = {
    annotationTypes
      .find { x => x.uniqueId == annotationTypeUniqueId }
      .toSuccess(s"annotation type does not exist: $annotationTypeUniqueId")
      .toValidationNel
  }

  protected def nameNotUsed(annotationType: AnnotationType): DomainValidation[Boolean] = {
    annotationTypes
      .find { x => (x.name == annotationType.name) && (x.uniqueId != annotationType.uniqueId)  }
      .fold
      { true.successNel[DomainError] }
      { _ => DomainError(s"annotation type name already used: ${annotationType.name}").failureNel[Boolean] }
  }

}
