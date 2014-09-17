package org.biobank.service.study

import org.biobank.service._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.domain.AnnotationValueType._

import scalaz._
import scalaz.Scalaz._

/**
 *
 * @author Nelson Loyola
 */
trait StudyAnnotationTypeProcessor[A <: StudyAnnotationType] extends Processor {

  val annotationTypeRepository: StudyAnnotationTypeRepository[A]

  val errMsgNameExists = "annotation type with name already exists"

  protected def nameAvailable(name: String): DomainValidation[Boolean] = {
    nameAvailableMatcher(name,annotationTypeRepository, errMsgNameExists){ item =>
      item.name.equals(name)
    }
  }

  protected def nameAvailable(name: String, excludeId: AnnotationTypeId): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, annotationTypeRepository, errMsgNameExists){ item =>
      item.name.equals(name) && (item.id != excludeId)
    }
  }

  protected def recoverEvent(annotationTypeId: AnnotationTypeId): Unit = {
    annotationTypeRepository.getByKey(annotationTypeId).fold(
      err => throw new IllegalStateException(s"updating annotation type from event failed: $err"),
      at => annotationTypeRepository.remove(at)
    )
    ()
  }

}
