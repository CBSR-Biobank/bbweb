package org.biobank.service.study

import org.biobank.service._
import org.biobank.domain._
import org.biobank.domain.study._

/**
 *
 * @author Nelson Loyola
 */
trait StudyAnnotationTypeProcessor[A <: StudyAnnotationType] extends Processor {

  val annotationTypeRepository: StudyAnnotationTypeRepository[A]

  val errMsgNameExists = "annotation type with name already exists"

  protected def nameAvailable(name: String, studyId: StudyId): DomainValidation[Boolean] = {
    nameAvailableMatcher(name,annotationTypeRepository, errMsgNameExists){ item =>
      (item.name == name) && (item.studyId == studyId)
    }
  }

  protected def nameAvailable(name: String,
                              studyId: StudyId,
                              excludeId: AnnotationTypeId)
      : DomainValidation[Boolean] = {
    nameAvailableMatcher(name, annotationTypeRepository, errMsgNameExists){ item =>
      (item.name == name) && (item.studyId == studyId) && (item.id != excludeId)
    }
  }

  protected def applyStudyAnnotationTypeRemovedEvent(annotationTypeId: AnnotationTypeId): Unit = {
    annotationTypeRepository.getByKey(annotationTypeId).fold(
      err => log.error(s"updating annotation type from event failed: $err"),
      at => {
        annotationTypeRepository.remove(at)
        ()
      }
    )
  }

}
