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

  protected def nameAvailable(name: String): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, annotationTypeRepository)(item => item.name.equals(name))
  }

  protected def nameAvailable(name: String, excludeId: AnnotationTypeId): DomainValidation[Boolean] = {
    nameAvailableMatcher(name, annotationTypeRepository)(item => item.name.equals(name) && (item.id != excludeId))
  }

}
