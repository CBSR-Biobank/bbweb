package org.biobank.domain.study

import org.biobank.domain._
import org.biobank.domain.AnnotationValueType._

case class SpecimenLinkAnnotationType(
  id: AnnotationTypeId,
  version: Long = -1,
  studyId: StudyId,
  name: String,
  description: Option[String],
  valueType: AnnotationValueType,
  maxValueCount: Option[Int],
  options: Option[Map[String, String]])
  extends StudyAnnotationType {

  val toStringFormat = """SpecimenLinkAnnotationType:{ id: %s, version: %d, studyId: %s,""" +
    """  name: %s, description: %s, valueType: %s, maxValueCount: %d, options: %s }"""

  override def toString: String = {
    toStringFormat.format(
      id, version, studyId, name, description, valueType, maxValueCount.getOrElse(-1),
      options.getOrElse("None"))
  }

}