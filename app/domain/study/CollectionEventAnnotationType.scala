package domain.study

import infrastructure._
import domain._
import domain.AnnotationValueType._

import scalaz._
import Scalaz._

case class CollectionEventAnnotationType(
  id: AnnotationTypeId,
  version: Long = -1,
  studyId: StudyId,
  name: String,
  description: String,
  valueType: AnnotationValueType,
  maxValueCount: Int,
  options: Map[String, String]) extends StudyAnnotationType {

  val toStringFormat = """{ id: %s, version: %d, studyId: %s, name: %s, description: %s,""" +
    """ valueType: %s, maxValueCount: %d, options: %s }"""

  override def toString: String = {
    toStringFormat.format(
      id, version, studyId, name, description, valueType, maxValueCount, options)
  }

}
