package org.biobank.domain.study

import org.biobank.domain.{
  ConcurrencySafeEntity,
  ContainerTypeId,
  DomainError,
  DomainValidation,
  HasUniqueName,
  HasDescriptionOption
}
import org.biobank.infrastructure._
import org.biobank.domain.validation.StudyAnnotationTypeValidationHelper

import com.github.nscala_time.time.Imports._
import scalaz._
import Scalaz._
import typelevel._

/** [[SpecimenLinkType]]s are assigned to a [[ProcessingType]], and are used to represent a regularly
  * performed processing procedure involving two [[Specimen]]s: an input, which must be in a specific
  * [[SpecimenGroup]], and an output, which must also be in another specific [[SpecimenGroup]].
  *
  * To avoid redundancy, each combination of inputGroup and outputGroup may exist only once per
  * ProcessingType.
  *
  * @param processingTypeId the [[ProcessingType]] this specimen link belongs to.
  *
  * @param expectedInputChange the expected amount to be removed from each input. If the value is
  *        not required then use a value of zero.
  *
  * @param expectedOutputChange the expected amount to be added to each output. If the value is
  *        not required then use a value of zero.
  *
  * @param inputCount the number of expected specimens when the processing is carried out.
  *
  * @param outputCount are the number of resulting specimens when the processing is carried out.
  *        A value of zero for output count implies that the count is the same as the input count.
  *
  * @param inputGroupId The [[SpecimenGroup]] the input specimens are from.
  *
  * @param outputGroupId The [[SpecimenGroup]] the output specimens are from.
  *
  * @param inputContainerTypeId The specimen container type that holds the input specimens. This is
  *        an optional field.
  *
  * @param outputContainerTypeId The specimen container type that the output specimens are stored
  *        into. This is an optional field.
  */
case class SpecimenLinkType private (
  processingTypeId: ProcessingTypeId,
  id: SpecimenLinkTypeId,
  version: Long,
  addedDate: DateTime,
  lastUpdateDate: Option[DateTime],
  expectedInputChange: BigDecimal,
  expectedOutputChange: BigDecimal,
  inputCount: Int,
  outputCount: Int,
  inputGroupId: SpecimenGroupId,
  outputGroupId: SpecimenGroupId,
  inputContainerTypeId: Option[ContainerTypeId],
  outputContainerTypeId: Option[ContainerTypeId],
  annotationTypeData: List[SpecimenLinkTypeAnnotationTypeData])
    extends ConcurrencySafeEntity[SpecimenLinkTypeId] {

  /** Updates a specimen link type with one or more new values.
    */
  def update(
    expectedVersion: Option[Long],
    dateTime: DateTime,
    expectedInputChange: BigDecimal,
    expectedOutputChange: BigDecimal,
    inputCount: Int,
    outputCount: Int,
    inputGroupId: SpecimenGroupId,
    outputGroupId: SpecimenGroupId,
    inputContainerTypeId: Option[ContainerTypeId] = None,
    outputContainerTypeId: Option[ContainerTypeId] = None,
    annotationTypeData: List[SpecimenLinkTypeAnnotationTypeData]): DomainValidation[SpecimenLinkType] = {

    for {
      validVersion <- requireVersion(expectedVersion)
      validatedItem <- SpecimenLinkType.create(
        processingTypeId, id, version, addedDate, expectedInputChange, expectedOutputChange, inputCount,
        outputCount, inputGroupId, outputGroupId, inputContainerTypeId, outputContainerTypeId,
        annotationTypeData)
      newItem <- validatedItem.copy(lastUpdateDate = Some(dateTime)).success
    } yield newItem
  }

  override def toString: String =
    s"""|SpecimenLinkType:{
        |  processingTypeId: $processingTypeId,
        |  id: $id,
        |  version: $version,
        |  addedDate: $addedDate,
        |  lastUpdateDate: $lastUpdateDate,
        |  expectedInputChange: $expectedInputChange,
        |  expectedOutputChange: $expectedOutputChange,
        |  inputCount: $inputCount,
        |  outputCount: $outputCount,
        |  inputGroupId: $inputGroupId,
        |  outputGroupId: $outputGroupId,
        |  inputContainerTypeId: $inputContainerTypeId,
        |  outputContainerTypeId: $outputContainerTypeId
        |  annotationTypeData: $annotationTypeData
        |}""".stripMargin
}

object SpecimenLinkType extends StudyAnnotationTypeValidationHelper {

  def create(
    processingTypeId: ProcessingTypeId,
    id: SpecimenLinkTypeId,
    version: Long,
    dateTime: DateTime,
    expectedInputChange: BigDecimal,
    expectedOutputChange: BigDecimal,
    inputCount: Int,
    outputCount: Int,
    inputGroupId: SpecimenGroupId,
    outputGroupId: SpecimenGroupId,
    inputContainerTypeId: Option[ContainerTypeId] = None,
    outputContainerTypeId: Option[ContainerTypeId] = None,
    annotationTypeData: List[SpecimenLinkTypeAnnotationTypeData] = List.empty): DomainValidation[SpecimenLinkType] = {

    /** The validation code below validates 13 items and to create a SpecimenLinkType only
      *  12 parameters are requried. This function ignores the value returned by the last validation
      *  to create the SpecimenLinkType.
      */
    def applyFunc(
      processingTypeId: ProcessingTypeId,
      id: SpecimenLinkTypeId,
      version: Long,
      expectedInputChange: BigDecimal,
      expectedOutputChange: BigDecimal,
      inputCount: Int,
      outputCount: Int,
      inputGroupId: SpecimenGroupId,
      outputGroupId: SpecimenGroupId,
      inputContainerTypeId: Option[ContainerTypeId] = None,
      outputContainerTypeId: Option[ContainerTypeId] = None,
      annotationTypeData: List[SpecimenLinkTypeAnnotationTypeData],
      ignore: Boolean): SpecimenLinkType = {
      SpecimenLinkType(processingTypeId, id, version, dateTime, None, expectedInputChange,
        expectedOutputChange, inputCount, outputCount, inputGroupId, outputGroupId,
        inputContainerTypeId, outputContainerTypeId, annotationTypeData)
    }

    (validateId(processingTypeId) :^:
      validateId(id) :^:
      validateAndIncrementVersion(version) :^:
      validatePositiveNumber(
        expectedInputChange,
        "expected input change is not a positive number") :^:
      validatePositiveNumber(
        expectedOutputChange,
        "expected output change is not a positive number") :^:
      validatePositiveNumber(
        inputCount,
        "input count is not a positive number") :^:
      validatePositiveNumber(
        outputCount,
        "output count is not a positive number") :^:
      validateId(inputGroupId) :^:
      validateId(outputGroupId) :^:
      validateId(inputContainerTypeId) :^:
      validateId(outputContainerTypeId) :^:
      validateAnnotationTypeData(annotationTypeData) :^:
      validateSpecimenGroups(inputGroupId, outputGroupId) :^:
      KNil).applyP(applyFunc _ curried)
  }

  private def validateSpecimenGroups(
    inputGroupId: SpecimenGroupId,
    outputGroupId: SpecimenGroupId): DomainValidation[Boolean] = {
    if (inputGroupId.equals(outputGroupId)) {
      DomainError("input and output specimen groups are the same").failNel
    } else {
      true.success
    }
  }

}
