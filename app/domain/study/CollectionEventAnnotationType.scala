package domain.study

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
  maxValueCount: Int) extends StudyAnnotationType {

  override def toString: String = {
    "{ id:%s, version: %d, name:%s, description:%s }" format (id, version, name, description)
  }

}

object CollectionEventAnnotationType {

  def add(
    studyId: StudyId,
    name: String,
    description: String,
    valueType: AnnotationValueType,
    maxValueCount: Int): DomainValidation[CollectionEventAnnotationType] =
    CollectionEventAnnotationType(AnnotationTypeIdentityService.nextIdentity, version = 0L,
      studyId, name, description, valueType, maxValueCount).success
}