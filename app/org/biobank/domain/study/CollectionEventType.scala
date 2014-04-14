package org.biobank.domain.study

import org.biobank.infrastructure._
import org.biobank.domain.{
  AnnotationTypeId,
  ConcurrencySafeEntity,
  DomainValidation,
  HasName,
  HasDescriptionOption }
import org.biobank.domain.validation.StudyValidationHelper

import scalaz._
import scalaz.Scalaz._

case class CollectionEventType private (
  studyId: StudyId,
  id: CollectionEventTypeId,
  version: Long = -1,
  name: String,
  description: Option[String],
  recurring: Boolean,
  specimenGroupData: Set[CollectionEventTypeSpecimenGroup],
  annotationTypeData: Set[CollectionEventTypeAnnotationType])
    extends ConcurrencySafeEntity[CollectionEventTypeId]
    with HasName
    with HasDescriptionOption {

  override def toString: String =
    s"""|CollectionEventType:{
        |  id: %s,
        |  version: %d,
        |  studyId: %s,
        |  name: %s,
        |  description: %s,
        |  recurring: %s,
        |  specimenGroupData: { %s },
        |  annotationTypeData: { %s }
        |}""".stripMargin
}

object CollectionEventType extends StudyValidationHelper {

  def validateSpecimenGroupItem(
    pecimenGroupItem: CollectionEventTypeSpecimenGroup): DomainValidation[CollectionEventTypeSpecimenGroup] = {
    (validateStringId(pecimenGroupItem.specimenGroupId, "specimen group id is null or empty").toValidationNel |@|
      validatePositiveNumber(pecimenGroupItem.maxCount, "max count not a positive number").toValidationNel |@|
      validatePositiveNumber(pecimenGroupItem.amount, "amount not a positive number").toValidationNel) {
        CollectionEventTypeSpecimenGroup(_, _, _)
      }
  }

  def validateSpecimenGroupData(
    specimenGroupData: Set[CollectionEventTypeSpecimenGroup]): DomainValidation[Set[CollectionEventTypeSpecimenGroup]] = {
  }

  def create(
  studyId: StudyId,
  id: CollectionEventTypeId,
  version: Long = -1,
  name: String,
  description: Option[String],
  recurring: Boolean,
  specimenGroupData: Set[CollectionEventTypeSpecimenGroup],
  annotationTypeData: Set[CollectionEventTypeAnnotationType]): DomainValidation[CollectionEventType] = {
    // TODO: complete this function
    null
  }

}

