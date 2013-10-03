package domain.study

import domain._
import domain.AnnotationValueType._

case class ParticipantAnnotationType(
  id: AnnotationTypeId,
  version: Long = -1,
  studyId: StudyId,
  name: String,
  description: Option[String],
  valueType: AnnotationValueType,
  maxValueCount: Option[Int],
  options: Option[Map[String, String]],
  required: Boolean)
  extends StudyAnnotationType {

  val toStringFormat = """ParticipantAnnotationType:{ id: %s, version: %d, studyId: %s,""" +
    """  name: %s, description: %s, valueType: %s, maxValueCount: %d, options: %s, required: %b }"""

  override def toString: String = {
    toStringFormat.format(
      id, version, studyId, name, description, valueType, maxValueCount.getOrElse(-1),
      options.getOrElse("None"), required)
  }

}