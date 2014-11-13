package org.biobank.domain.study

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

import org.biobank.domain.{ Annotation, AnnotationTypeId, AnnotationOption }

/** Value type.
  *
  */
case class ParticipantAnnotation(
  participantId: ParticipantId,
  annotationTypeId: AnnotationTypeId,
  stringValue: Option[String],
  numberValue: Option[String], // FIXME: should we use java.lang.Number
  selectedValues: Option[List[AnnotationOption]])
    extends Annotation[ParticipantAnnotationType]


object ParticipantAnnotation {

  implicit val participantAnnotationRead: Reads[ParticipantAnnotation] = (
    (__ \ "participantId").read[ParticipantId] and
    (__ \ "annotationTypeId").read[AnnotationTypeId] and
    (__ \ "stringValue").readNullable[String](minLength[String](2)) and
    (__ \ "numberValue").readNullable[String] and
    (__ \ "selectedValue").readNullable[List[AnnotationOption]]
  ){ ParticipantAnnotation(_, _, _, _, _) }

  implicit val participantAnnotationWrites: Writes[ParticipantAnnotation] = (
    (__ \ "participantId").write[ParticipantId] and
    (__ \ "annotationTypeId").write[AnnotationTypeId] and
    (__ \ "stringValue").write[Option[String]] and
    (__ \ "numberValue").write[Option[String]] and
    (__ \ "selectedValue").write[Option[List[AnnotationOption]]]
  )(unlift(ParticipantAnnotation.unapply))

}
