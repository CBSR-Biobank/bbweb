package org.biobank.service.json

import org.biobank.infrastructure._
import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.domain.AnnotationValueType._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

object StudyAnnotationType {

  implicit val annotationTypeIdReader = (__ \ "id").read[String](minLength[String](2)).map(
    new AnnotationTypeId(_) )

  implicit val annotationTypeIdWriter = Writes{ (id: AnnotationTypeId) => JsString(id.id) }

  implicit val annotationValueTypeReads = EnumUtils.enumReads(org.biobank.domain.AnnotationValueType)

}
