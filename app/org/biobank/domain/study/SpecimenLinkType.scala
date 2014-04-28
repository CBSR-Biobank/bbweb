package org.biobank.domain.study

import org.biobank.domain.validation.StudyValidationHelper

/** [[SpecimenLinkType]]s are assigned to a [[ProcessingType]], and are used to represent a regularly
  * performed processing procedure involving two [[Specimen]]s: an input, which must be in a specific
  * [[SpecimenGroup]], and an output, which must also be in another specific [[SpecimenGroup]].
  */
case class SpecimenLinkType private (
  procesingTypeId: ProcessingTypeId,
  id: SpecimenLinkTypeId,
  version: Long,
  expectedInputChange: BigDecimal,
  expectedOutputChange: BigDecimal,
  inputCount: Int,
  outputCount: Int,
  inputGroupId: SpecimenGroupId,
  outputGroupId: SpecimenGroupId,
  inputContainerTypeId: ContainerTypeId,
  outputContainerTypeId: ContainerTypeId)
    extends ConcurrencySafeEntity[CollectionEventTypeId]
    with HasName
    with HasDescriptionOption
    with HasStudyId {

  def update(
    expectedVersion: Option[Long],
    name: String,
    description: Option[String],
    enaled: Boolean): DomainValidation[SpecimenLinkType] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      newItem <- SpecimenLinkType.create(studyId, id, version, name, description, enabled)
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

object SpecimenLinkType extends StudyValidationHelper {

  def create(
    studyId: StudyId,
    id: SpecimenLinkTypeId,
    version: Long,
    name: String,
    description: Option[String],
    enabled: Boolean): DomainValidation[SpecimenLinkType] = {
    (validateId(studyId).toValidationNel |@|
      validateId(id).toValidationNel |@|
      validateAndIncrementVersion(version).toValidationNel |@|
      validateNonEmpty(name, "name is null or empty").toValidationNel |@|
      validateNonEmptyOption(description, "description is null or empty").toValidationNel) {
      SpecimenLinkType(_, _, _, _, _, enabled)
    }
  }

  protected def validateId(id: SpecimenLinkTypeId): Validation[String, SpecimenLinkTypeId] = {
    validateStringId(id.toString, "collection event type id is null or empty") match {
      case Success(idString) => id.success
      case Failure(err) => err.fail
    }
  }
}
