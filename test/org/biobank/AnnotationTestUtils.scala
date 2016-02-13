package org.biobank

import org.biobank.domain.{
  Annotation,
  AnnotationValueType
}

import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

/**
 * This code is supposed to be independent of the application code, so Json objects are created from scratch.
 */
object AnnotationTestUtils {

  /** Converts a participant annotation into a Json object.
   */
  def annotationToJson(annotation: Annotation) = {
    Json.obj(
      "annotationTypeUniqueId" -> annotation.annotationTypeUniqueId,
      "stringValue"            -> annotation.stringValue,
      "numberValue"            -> annotation.numberValue,
      "selectedValues"         -> annotation.selectedValues
    )
  }

}
