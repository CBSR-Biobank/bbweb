package domain

import domain.AnnotationValueType._
import infrastructure._

import scalaz._
import Scalaz._

abstract class AnnotationType extends ConcurrencySafeEntity[AnnotationTypeId] {

  val version: Long
  val name: String
  val description: String
  val valueType: AnnotationValueType
  val maxValueCount: Int

  def addOptions(annotationOptions: Map[String, AnnotationOption],
    options: Set[String]): DomainValidation[AnnotationOption] = {
    if (!annotationOptions.isEmpty)
      DomainError("annotation type already has options").fail
    else
      AnnotationOption(AnnotationOptionIdentityService.nextIdentity, this.id, options).success
  }

  def updateOptions(annotationOptions: Map[String, AnnotationOption],
    options: Set[String]): DomainValidation[AnnotationOption] = {
    if (annotationOptions.size != 1)
      DomainError("annotation type has no options").fail
    else
      AnnotationOption(AnnotationOptionIdentityService.nextIdentity, this.id, options).success
  }

}
