package org.biobank.domain.study

import org.biobank.domain.{
  Annotation,
  AnnotationTypeId,
  AnnotationOption,
  DomainValidation
}

import play.api.libs.json._
import play.api.libs.json.Reads._
import scalaz.Scalaz._

/** This is a value type.
  *
  */
case class ParticipantAnnotation(annotationTypeId: AnnotationTypeId,
                                 stringValue: Option[String],
                                 numberValue: Option[String], // FIXME: should we use java.lang.Number
                                 selectedValues: List[AnnotationOption])
    extends Annotation[ParticipantAnnotationType]


object ParticipantAnnotation {

  def create(annotationTypeId: AnnotationTypeId,
             stringValue:      Option[String],
             numberValue:      Option[String],
             selectedValues:   List[AnnotationOption])
      : DomainValidation[ParticipantAnnotation] = {
    Annotation.validate(annotationTypeId, stringValue, numberValue, selectedValues).fold(
      err => err.failure,
      valid => ParticipantAnnotation(annotationTypeId, stringValue, numberValue, selectedValues).success
    )
  }

  implicit val participantAnnotationFormat = Json.format[ParticipantAnnotation]

}
