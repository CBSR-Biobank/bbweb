// package org.biobank.service.study

// import org.biobank.service._
// import org.biobank.domain._
// import org.biobank.domain.study._

// /**
//  *
//  * @author Nelson Loyola
//  */
// trait StudyAnnotationTypeProcessor[A <: StudyAnnotationType] extends Processor {

//   val annotationTypeRepository: AnnotationTypeRepository

//   val ErrMsgNameExists = "annotation type with name already exists"

//   protected def nameAvailable(name: String, studyId: StudyId): ServiceValidation[Boolean] = {
//     nameAvailableMatcher(name, annotationTypeRepository, ErrMsgNameExists){ item =>
//       (item.name == name) && (item.studyId == studyId)
//     }
//   }

//   protected def nameAvailable(name: String,
//                               studyId: StudyId,
//                               excludeId: AnnotationTypeId)
//       : ServiceValidation[Boolean] = {
//     nameAvailableMatcher(name, annotationTypeRepository, ErrMsgNameExists){ item =>
//       (item.name == name) && (item.studyId == studyId) && (item.id != excludeId)
//     }
//   }

//   protected def applyParticipantAnnotationTypeRemovedEvent(annotationTypeId: AnnotationTypeId): Unit = {
//     annotationTypeRepository.getByKey(annotationTypeId).fold(
//       err => log.error(s"updating annotation type from event failed: $err"),
//       at => {
//         annotationTypeRepository.remove(at)
//         ()
//       }
//     )
//   }

// }
