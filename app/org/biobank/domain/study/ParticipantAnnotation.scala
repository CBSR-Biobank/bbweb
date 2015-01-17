package org.biobank.domain.study

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import org.biobank.domain.{ Annotation, AnnotationTypeId, AnnotationOption }

/** This is a value type.
  *
  */
case class ParticipantAnnotation(
  annotationTypeId: AnnotationTypeId,
  stringValue: Option[String],
  numberValue: Option[String], // FIXME: should we use java.lang.Number
  selectedValues: List[AnnotationOption])
    extends Annotation[ParticipantAnnotationType]


object ParticipantAnnotation {

  implicit val participantAnnotationFormat = Json.format[ParticipantAnnotation]

}
