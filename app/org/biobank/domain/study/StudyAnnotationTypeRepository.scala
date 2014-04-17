package org.biobank.domain.study

import org.biobank.domain._

import scalaz._
import scalaz.Scalaz._

trait StudyAnnotationTypeRepository[A <: StudyAnnotationType] {

  def nextIdentity: AnnotationTypeId

  def annotationTypeWithId(studyId: StudyId, annotationTypeId: AnnotationTypeId): DomainValidation[A]

  def annotationTypeWithId(studyId: StudyId, annotationTypeId: String): DomainValidation[A]

  def allAnnotationTypesForStudy(studyId: StudyId): Set[A]

 //  def add(annotationType: A): DomainValidation[A]

//   def update(oldAnnotationType: A, newAnnotationType: A): DomainValidation[A]

//   def remove(annotationType: A): DomainValidation[A]
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

  private def nameAvailable(annotationType: A): DomainValidation[Boolean] = {
    val exists = getValues.exists { item =>
      item.studyId.equals(annotationType.studyId) &&
        item.name.equals(annotationType.name) &&
        !item.id.equals(annotationType.id)
    }

    if (exists)
      DomainError("annotation type with name already exists: %s" format annotationType.name).failNel
    else
      true.success
  }

  // def add(annotationType: A): DomainValidation[A] = {
  //   annotationTypeWithId(annotationType.studyId, annotationType.id) match {
  //     case Success(prevItem) =>
  //       DomainError("annotation type with ID already exists: %s" format annotationType.id).failNel
  //     case Failure(x) =>
  //       for {
  //         nameValid <- nameAvailable(annotationType)
  //         item <- updateMap(annotationType).success
  //       } yield item
  //   }
  // }

  // def update(oldAnnotationType: A, newAnnotationType: A): DomainValidation[A] = {
  //   for {
  //     prevItem <- annotationTypeWithId(oldAnnotationType.studyId, oldAnnotationType.id)
  //     validVersion <- prevItem.requireVersion(Some(newAnnotationType.version - 1L))
  //     nameValid <- nameAvailable(newAnnotationType)
  //     repoItem <- updateMap(newAnnotationType).success
  //   } yield repoItem
  // }

  // def remove(annotationType: A): DomainValidation[A] = {
  //   for {
  //     item <- annotationTypeWithId(annotationType.studyId, annotationType.id)
  //     validVersion <- item.requireVersion(Some(annotationType.version))
  //     removedItem <- removeFromMap(item).success
  //   } yield removedItem

  // }

}
