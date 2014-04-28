package org.biobank.domain.study

import org.biobank.domain.{
  ConcurrencySafeEntity,
  ContainerTypeId,
  DomainError,
  DomainValidation,
  HasUniqueName,
  HasDescriptionOption
}
import org.biobank.domain.validation.StudyValidationHelper

import scalaz._
import scalaz.Scalaz._

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
  inputContainerTypeId: Option[ContainerTypeId],
  outputContainerTypeId: Option[ContainerTypeId])
    extends ConcurrencySafeEntity[CollectionEventTypeId]
    with HasStudyId {

  def update(
    expectedVersion: Option[Long],
    name: String,
    description: Option[String],
    enaled: Boolean): DomainValidation[SpecimenLinkType] = {
    for {
      validVersion <- requireVersion(expectedVersion)
      newItem <- SpecimenLinkType.create(procesingTypeId, id, version,  expectedInputChange,
	expectedOutputChange, inputCount, outputCount, inputGroupId, outputGroupId,
	inputContainerTypeId, outputContainerTypeId)
    } yield newItem
  }

  override def toString: String =
    s"""|CollectionEventType:{
        |  procesingTypeId: $procesingTypeId,
        |  id: $id,
        |  version: $version,
        |  name: $name,
        |  description: $description,
        |  enabled: $enabled
        |}""".stripMargin
}

object SpecimenLinkType extends StudyValidationHelper {

  def create(
    procesingTypeId: ProcessingTypeId,
    id: SpecimenLinkTypeId,
    version: Long,
    expectedInputChange: BigDecimal,
    expectedOutputChange: BigDecimal,
    inputCount: Int,
    outputCount: Int,
    inputGroupId: SpecimenGroupId,
    outputGroupId: SpecimenGroupId,
    inputContainerTypeId: Option[ContainerTypeId],
    outputContainerTypeId: Option[ContainerTypeId]): DomainValidation[SpecimenLinkType] = {
    (validateId(procesingTypeId).toValidationNel |@|
      validateId(id).toValidationNel |@|
      validateAndIncrementVersion(version).toValidationNel) {

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
