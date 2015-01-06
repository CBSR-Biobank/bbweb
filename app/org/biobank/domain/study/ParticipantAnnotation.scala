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

  implicit val participantAnnotationRead: Reads[ParticipantAnnotation] = (
    (__ \ "annotationTypeId").read[AnnotationTypeId] and
    (__ \ "stringValue").readNullable[String](minLength[String](2)) and
    (__ \ "numberValue").readNullable[String](minLength[String](2)) and
    (__ \ "selectedValues").read[List[AnnotationOption]]
  )(ParticipantAnnotation.apply _)

  implicit val participantAnnotationWrites: Writes[ParticipantAnnotation] = (
    (__ \ "annotationTypeId").write[AnnotationTypeId] and
    (__ \ "stringValue").write[Option[String]] and
    (__ \ "numberValue").write[Option[String]] and
    (__ \ "selectedValues").write[List[AnnotationOption]]
  )(unlift(ParticipantAnnotation.unapply))

}
