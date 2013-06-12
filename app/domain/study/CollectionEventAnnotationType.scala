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

  override def toString: String = {
    "{ id:%s, version: %d, name:%s, description:%s, valueType: %s, maxValueCount: %d, options: %s }" format (
      id, version, name, description, valueType, maxValueCount, options)
  }

}

object CollectionEventAnnotationType {

  def add(
    studyId: StudyId,
    name: String,
    description: String,
    valueType: AnnotationValueType,
    maxValueCount: Int,
    options: Map[String, String]): DomainValidation[CollectionEventAnnotationType] =
    CollectionEventAnnotationType(AnnotationTypeIdentityService.nextIdentity, version = 0L,
      studyId, name, description, valueType, maxValueCount, options).success
}