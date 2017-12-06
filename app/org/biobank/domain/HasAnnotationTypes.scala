package org.biobank.domain

import scalaz.Scalaz._

trait HasAnnotationTypes extends AnnotationTypeValidations {

  import org.biobank.CommonValidations._

  val annotationTypes: Set[AnnotationType]

  protected def checkAddAnnotationType(annotationType: AnnotationType)
      : DomainValidation[Boolean] = {
    (validate(annotationType) |@| nameNotUsed(annotationType)) {
      case _ => true
    }
  }

  protected def checkRemoveAnnotationType(annotationTypeId: AnnotationTypeId)
      : DomainValidation[AnnotationType] = {
    annotationTypes
      .find { x => x.id == annotationTypeId }
      .toSuccessNel(s"annotation type does not exist: $annotationTypeId")
  }

  protected def nameNotUsed(annotationType: AnnotationType): DomainValidation[Boolean] = {
    val nameLowerCase = annotationType.name.toLowerCase
    annotationTypes
      .find { x => (x.name.toLowerCase == nameLowerCase) && (x.id != annotationType.id)  }
      match {
        case Some(_) =>
          EntityCriteriaError(s"annotation type name already used: ${annotationType.name}").failureNel[Boolean]
        case None =>
          true.successNel[DomainError]
      }
  }

}
