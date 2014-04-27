package org.biobank.domain.study

import org.biobank.domain._

import scalaz._
import scalaz.Scalaz._

trait StudyAnnotationTypeRepository[A <: StudyAnnotationType]
    extends ReadWriteRepository [AnnotationTypeId, A] {

  def nextIdentity: AnnotationTypeId

  def annotationTypeWithId(studyId: StudyId, annotationTypeId: AnnotationTypeId): DomainValidation[A]

  def annotationTypeWithId(studyId: StudyId, annotationTypeId: String): DomainValidation[A]

  def allAnnotationTypesForStudy(studyId: StudyId): Set[A]
 }

trait StudyAnnotationTypeRepositoryImpl[A <: StudyAnnotationType]
  extends ReadWriteRepository[AnnotationTypeId, A]
  with StudyAnnotationTypeRepository[A] {

  def nextIdentity: AnnotationTypeId =
    new AnnotationTypeId(java.util.UUID.randomUUID.toString.toUpperCase)

  def annotationTypeWithId(
    studyId: StudyId,
    annotationTypeId: AnnotationTypeId): DomainValidation[A] = {
    getByKey(annotationTypeId) match {
      case Failure(err) =>
        DomainError(
          s"annotation type does not exist: { studyId: $studyId, annotationTypeId: $annotationTypeId }").failNel
      case Success(annotType) =>
        if (annotType.studyId.equals(studyId)) annotType.success
        else DomainError(
          "study does not have annotation type: { studyId: %s, annotationTypeId: %s }".format(
            studyId, annotationTypeId)).failNel
    }
  }

  def annotationTypeWithId(
    studyId: StudyId,
    annotationTypeId: String): DomainValidation[A] = {
    annotationTypeWithId(studyId, AnnotationTypeId(annotationTypeId))
  }

  def allAnnotationTypesForStudy(studyId: StudyId): Set[A] = {
    getValues.filter(x => x.studyId.equals(studyId)).toSet
  }

}
