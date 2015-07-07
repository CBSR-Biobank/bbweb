package org.biobank

import org.biobank.domain.{
  Annotation,
  AnnotationTypeId,
  AnnotationOption,
  AnnotationValueType
}
import org.biobank.domain.participants.ParticipantAnnotation

import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

/**
 * This code is supposed to be independent of the application code, so Json objects are created from scratch.
 */
object AnnotationTestUtils {

  def annotationOptionToJson(annotationOption: AnnotationOption) = {
    Json.obj(
      "annotationTypeId" -> annotationOption.annotationTypeId,
      "value"            -> annotationOption.value
    )
  }

  /** Converts a participant annotation into a Json object.
   */
  def annotationToJson[T <: Annotation[_]](annotation: T) = {
    val json = Json.obj(
      "annotationTypeId" -> annotation.annotationTypeId,
      "stringValue"      -> annotation.stringValue,
      "numberValue"      -> annotation.numberValue
    )

    json ++ Json.obj("selectedValues" -> annotation.selectedValues.map(value => annotationOptionToJson(value)))
  }

}
