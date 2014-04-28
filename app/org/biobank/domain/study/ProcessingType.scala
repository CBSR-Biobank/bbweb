package org.biobank.domain.study

import org.biobank.domain.{
  ConcurrencySafeEntity,
  DomainError,
  DomainValidation,
  HasUniqueName,
  HasDescriptionOption
}
import org.biobank.domain.validation.StudyValidationHelper

import scalaz._
import scalaz.Scalaz._

/** Records a regularly preformed specimen processing procedure. There are one or more associated
  * [[SpecimenLinkType]]s that further define legal procedures, and allow recording of procedures
  * performed on different types of [[Specimen]]s.
  *
  * For speicmen processing to take place, a study must have at least one processing type defined.
  *
  *  @param enabled A processing type should have enabled set to true when processing of the
  *         contained specimen types is taking place. However, throughout the lifetime of the study, it may be
  *         decided to stop a processing type in favour of another.  In this case enabled is set to false.
  *
  */
case class ProcessingType private (
  studyId: StudyId,
  id: ProcessingTypeId,
  version: Long,
  name: String,
  description: Option[String],
  enabled: Boolean)
    extends ConcurrencySafeEntity[ProcessingTypeId]
    with HasUniqueName
    with HasDescriptionOption
    with HasStudyId {

  def update(
    expectedVersion: Option[Long],
    name: String,
    description: Option[String],
    enaled: Boolean): DomainValidation[ProcessingType] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      newItem <- ProcessingType.create(studyId, id, version, name, description, enabled)
    } yield newItem
  }

  override def toString: String =
    s"""|CollectionEventType:{
        |  studyId: $studyId,
        |  id: $id,
        |  version: $version,
        |  name: $name,
        |  description: $description,
        |  enabled: $enabled
        |}""".stripMargin
}

object ProcessingType extends StudyValidationHelper {

  def create(
    studyId: StudyId,
    id: ProcessingTypeId,
    version: Long,
    name: String,
    description: Option[String],
    enabled: Boolean): DomainValidation[ProcessingType] = {
    (validateId(studyId).toValidationNel |@|
      validateId(id).toValidationNel |@|
      validateAndIncrementVersion(version).toValidationNel |@|
      validateNonEmpty(name, "name is null or empty").toValidationNel |@|
      validateNonEmptyOption(description, "description is null or empty").toValidationNel) {
      ProcessingType(_, _, _, _, _, enabled)
    }
  }
}
