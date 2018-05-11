package org.biobank.fixtures

import org.biobank.domain.annotations._
import play.api.libs.json._

trait AnnotationTypeJson {

  protected def annotationTypeToJsonNoId(annotType: AnnotationType): JsObject = {
    annotationTypeToJson(annotType) - "id"
  }

  protected def annotationTypeToJson(annotType: AnnotationType): JsObject = {
    Json.obj("annotationTypeId" -> annotType.id,
             "name"             -> annotType.name,
             "description"      -> annotType.description,
             "valueType"        -> annotType.valueType,
             "options"          -> annotType.options,
             "required"         -> annotType.required)
  }

}
