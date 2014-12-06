package org.biobank.domain.study

import org.biobank.domain._
import scalaz._
import Scalaz._

trait StudyAnnotationTypeRepository[A <: StudyAnnotationType]
    extends ReadWriteRepository [AnnotationTypeId, A] {

  def withId(studyId: StudyId, annotationTypeId: AnnotationTypeId): DomainValidation[A]

  def withId(studyId: StudyId, annotationTypeId: String): DomainValidation[A]

  def allForStudy(studyId: StudyId): Set[A]
 }

trait StudyAnnotationTypeRepositoryImpl[A <: StudyAnnotationType]
  extends ReadWriteRepository[AnnotationTypeId, A]
  with StudyAnnotationTypeRepository[A] {

  def nextIdentity: AnnotationTypeId = new AnnotationTypeId(nextIdentityAsString)

  def withId(
    studyId: StudyId,
    annotationTypeId: AnnotationTypeId): DomainValidation[A] = {
    getByKey(annotationTypeId).fold(
      err =>
      DomainError(
        s"annotation type does not exist: { studyId: $studyId, annotationTypeId: $annotationTypeId }").failNel,
      annotType =>
      if (annotType.studyId == studyId) annotType.success
      else DomainError(
        "study does not have annotation type: { studyId: %s, annotationTypeId: %s }".format(
          studyId, annotationTypeId)).failNel
    )
  }

  def withId(
    studyId: StudyId,
    annotationTypeId: String): DomainValidation[A] = {
    withId(studyId, AnnotationTypeId(annotationTypeId))
  }

  def allForStudy(studyId: StudyId): Set[A] = {
    getValues.filter(x => x.studyId == studyId).toSet
  }

}
