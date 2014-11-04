package org.biobank.domain.study

import org.biobank.domain.{ Annotation, AnnotationTypeId, AnnotationOption }

/** Value type.
  *
  */
case class ParticipantAnnotation(
  participantId: ParticipantId,
  annotationTypeId: AnnotationTypeId,
  stringValue: Option[String],
  numberValue: Option[java.lang.Number],
  selectedValues: Option[List[AnnotationOption]])
    extends Annotation[ParticipantAnnotationType]
