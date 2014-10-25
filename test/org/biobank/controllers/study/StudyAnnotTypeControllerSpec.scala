package org.biobank.controllers.study

import org.biobank.fixture._
import org.biobank.domain.study.{ Study, StudyAnnotationType }

import play.api.libs.json._

abstract class StudyAnnotTypeControllerSpec[T <: StudyAnnotationType]
    extends ControllerFixture {

  protected def uri(study: Study): String

  protected def uri(study: Study, annotationType: T): String =
    uri(study) + s"/${annotationType.id.id}"

  protected def uriWithQuery(study: Study, annotationType: T): String =
    uri(study) + s"?annotTypeId=${annotationType.id.id}"

  protected def uri(study: Study, annotationType: T, version: Long): String =
    uri(study, annotationType) + s"/${version}"

  protected  def annotTypeToAddCmdJson(annotType: T) = {
    Json.obj(
      "studyId"       -> annotType.studyId.id,
      "name"          -> annotType.name,
      "description"   -> annotType.description,
      "valueType"     -> annotType.valueType.toString,
      "maxValueCount" -> annotType.maxValueCount,
      "options"       -> annotType.options
    )
  }

  protected def annotTypeToUpdateCmdJson(annotType: T) = {
    annotTypeToAddCmdJson(annotType) ++ Json.obj(
      "id"              -> annotType.id.id,
      "expectedVersion" -> Some(annotType.version)
    )
  }


}

