package org.biobank.domain.participants

import org.biobank.domain.{
  Annotation,
  AnnotationTypeId,
  AnnotationOption,
  DomainValidation
}
import org.biobank.domain.study._

import scalaz.Scalaz._
import play.api.libs.json._
import play.api.libs.json.Reads._


/** This is a value type.
  *
  */
case class CollectionEventAnnotation(annotationTypeId: AnnotationTypeId,
                                     stringValue: Option[String],
                                     numberValue: Option[String], // FIXME: should we use java.lang.Number
                                     selectedValues: List[AnnotationOption])
    extends Annotation[CollectionEventAnnotationType]


object CollectionEventAnnotation {

  def create(annotationTypeId: AnnotationTypeId,
             stringValue:      Option[String],
             numberValue:      Option[String],
             selectedValues:   List[AnnotationOption])
      : DomainValidation[CollectionEventAnnotation] = {
    Annotation.validate(annotationTypeId, stringValue, numberValue, selectedValues).fold(
      err => err.failure,
      valid => CollectionEventAnnotation(annotationTypeId, stringValue, numberValue, selectedValues).success
    )
  }

  implicit val collectionEventAnnotationFormat = Json.format[CollectionEventAnnotation]

}
