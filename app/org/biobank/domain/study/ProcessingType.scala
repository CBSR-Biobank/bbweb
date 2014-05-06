package org.biobank.domain.study

import org.biobank.domain.{
  ConcurrencySafeEntity,
  DomainError,
  DomainValidation,
  HasUniqueName,
  HasDescriptionOption
}
import org.biobank.domain.validation.StudyValidationHelper

import com.github.nscala_time.time.Imports._
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
  addedDate: DateTime,
  lastUpdateDate: Option[DateTime],
  name: String,
  description: Option[String],
  enabled: Boolean)
    extends ConcurrencySafeEntity[ProcessingTypeId]
    with HasUniqueName
    with HasDescriptionOption
    with HasStudyId {

  /** Updates a processing type with new values.
    */
  def update(
    expectedVersion: Option[Long],
    name: String,
    description: Option[String],
    enabled: Boolean): DomainValidation[ProcessingType] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      validatedItem <- ProcessingType.create(studyId, id, version, name, description, enabled)
      newItem <- validatedItem.copy(
        addedDate = this.addedDate,
        lastUpdateDate = Some(org.joda.time.DateTime.now)).success
    } yield newItem
  }

  override def toString: String =
    s"""|ProcessingType:{
        |  studyId: $studyId,
        |  id: $id,
        |  addedDate: $addedDate,
        |  lastUpdateDate: $lastUpdateDate,
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
    (validateId(studyId) |@|
      validateId(id) |@|
      validateAndIncrementVersion(version) |@|
      validateNonEmpty(name, "name is null or empty") |@|
      validateNonEmptyOption(description, "description is null or empty")) {
      ProcessingType(_, _, _, DateTime.now, None, _, _, enabled)
    }
  }
}
