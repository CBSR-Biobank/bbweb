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

