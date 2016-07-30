package org.biobank.domain.study

import org.biobank.ValidationKey
import org.biobank.domain.{
  ConcurrencySafeEntity,
  DomainValidation
}
import org.biobank.infrastructure._
import org.biobank.infrastructure.JsonUtils._
import org.biobank.domain.containers.ContainerTypeId
import org.joda.time.DateTime

import play.api.libs.json._
//import scalaz._
//import Scalaz._

trait SpecimenLinkTypeValidations {

  case object ProcessingTypeIdRequired extends ValidationKey

  case object SpecimenGroupIdRequired extends ValidationKey

  case object ContainerTypeIdRequired extends ValidationKey

  case object InvalidPositiveNumber extends ValidationKey

}

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
final case class SpecimenLinkType(processingTypeId:      ProcessingTypeId,
                                  id:                    SpecimenLinkTypeId,
                                  version:               Long,
                                  timeAdded:             DateTime,
                                  timeModified:          Option[DateTime],
                                  expectedInputChange:   BigDecimal,
                                  expectedOutputChange:  BigDecimal,
                                  inputCount:            Int,
                                  outputCount:           Int,
                                  inputGroupId:          SpecimenGroupId,
                                  outputGroupId:         SpecimenGroupId,
                                  inputContainerTypeId:  Option[ContainerTypeId],
                                  outputContainerTypeId: Option[ContainerTypeId],
                                  annotationTypeData:    List[SpecimenLinkTypeAnnotationTypeData])
    extends ConcurrencySafeEntity[SpecimenLinkTypeId] {

  /** Updates a specimen link type with one or more new values.
    */
  def update(expectedInputChange:   BigDecimal,
             expectedOutputChange:  BigDecimal,
             inputCount:            Int,
             outputCount:           Int,
             inputGroupId:          SpecimenGroupId,
             outputGroupId:         SpecimenGroupId,
             inputContainerTypeId:  Option[ContainerTypeId],
             outputContainerTypeId: Option[ContainerTypeId],
             annotationTypeData:    List[SpecimenLinkTypeAnnotationTypeData])
      : DomainValidation[SpecimenLinkType] = {
    val v = SpecimenLinkType.create(processingTypeId      = this.processingTypeId,
                                    id                    = this.id,
                                    version               = this.version,
                                    expectedInputChange   = expectedInputChange,
                                    expectedOutputChange  = expectedOutputChange,
                                    inputCount            = inputCount,
                                    outputCount           = outputCount,
                                    inputGroupId          = inputGroupId,
                                    outputGroupId         = outputGroupId,
                                    inputContainerTypeId  = inputContainerTypeId,
                                    outputContainerTypeId = outputContainerTypeId,
                                    annotationTypeData    = annotationTypeData)
    v.map(_.copy(timeModified = Some(DateTime.now)))
  }

  override def toString: String =
    s"""|SpecimenLinkType:{
        |  processingTypeId:      $processingTypeId,
        |  id:                    $id,
        |  version:               $version,
        |  timeAdded:             $timeAdded,
        |  timeModified:          $timeModified,
        |  expectedInputChange:   $expectedInputChange,
        |  expectedOutputChange:  $expectedOutputChange,
        |  inputCount:            $inputCount,
        |  outputCount:           $outputCount,
        |  inputGroupId:          $inputGroupId,
        |  outputGroupId:         $outputGroupId,
        |  inputContainerTypeId:  $inputContainerTypeId,
        |  outputContainerTypeId: $outputContainerTypeId
        |  annotationTypeData:    $annotationTypeData
        |}""".stripMargin
}

object SpecimenLinkType extends SpecimenLinkTypeValidations {
  //import org.biobank.domain.CommonValidations._

  def create(processingTypeId:      ProcessingTypeId,
             id:                    SpecimenLinkTypeId,
             version:               Long,
             expectedInputChange:   BigDecimal,
             expectedOutputChange:  BigDecimal,
             inputCount:            Int,
             outputCount:           Int,
             inputGroupId:          SpecimenGroupId,
             outputGroupId:         SpecimenGroupId,
             inputContainerTypeId:  Option[ContainerTypeId],
             outputContainerTypeId: Option[ContainerTypeId],
             annotationTypeData:    List[SpecimenLinkTypeAnnotationTypeData])
      : DomainValidation[SpecimenLinkType] = {

    /** The validation code below validates 13 items and to create a SpecimenLinkType only
      * 12 parameters are requried. This function ignores the value returned by the last validation
      * to create the SpecimenLinkType.
      *
      * Scalza applicative builders can only be used with up to 12 parameters. The <*> operator is used instead.
      *
      * See http://stackoverflow.com/questions/16930347/scalaz-how-can-i-accumulate-failures-or-apply-a-function-to-validations-with-di.
      */
    // def applyFunc(processingTypeId:      ProcessingTypeId,
    //               id:                    SpecimenLinkTypeId,
    //               version:               Long,
    //               expectedInputChange:   BigDecimal,
    //               expectedOutputChange:  BigDecimal,
    //               inputCount:            Int,
    //               outputCount:           Int,
    //               inputGroupId:          SpecimenGroupId,
    //               outputGroupId:         SpecimenGroupId,
    //               inputContainerTypeId:  Option[ContainerTypeId] = None,
    //               outputContainerTypeId: Option[ContainerTypeId] = None,
    //               annotationTypeData:    List[SpecimenLinkTypeAnnotationTypeData],
    //               ignore:                Boolean)
    //     : SpecimenLinkType = {
    //   SpecimenLinkType(processingTypeId, id, version, DateTime.now, None, expectedInputChange,
    //                    expectedOutputChange, inputCount, outputCount, inputGroupId, outputGroupId,
    //                    inputContainerTypeId, outputContainerTypeId, annotationTypeData)
    // }

    ???

    // these have to be in reverse order
    // validateSpecimenGroups(inputGroupId, outputGroupId) <*>
    // (validateAnnotationTypeData(annotationTypeData) <*>
    //   (validateId(outputContainerTypeId, ContainerTypeIdRequired) <*>
    //     (validateId(inputContainerTypeId, ContainerTypeIdRequired) <*>
    //       (validateId(outputGroupId, SpecimenGroupIdRequired) <*>
    //         (validateId(inputGroupId, SpecimenGroupIdRequired) <*>
    //           (validatePositiveNumber(outputCount, InvalidPositiveNumber) <*>
    //             (validatePositiveNumber(inputCount, InvalidPositiveNumber) <*>
    //               (validatePositiveNumber(expectedOutputChange, InvalidPositiveNumber) <*>
    //                 (validatePositiveNumber(expectedInputChange, InvalidPositiveNumber) <*>
    //                   (validateVersion(version) <*>
    //                     (validateId(id, IdRequired) <*>
    //                        (validateId(processingTypeId, ProcessingTypeIdRequired) map (
    //                           SpecimenLinkType.apply _).curried)
    //                     )))))))))))

  }

  // private def validateSpecimenGroups(inputGroupId: SpecimenGroupId, outputGroupId: SpecimenGroupId)
  //     : DomainValidation[Boolean] = {
  //   if (inputGroupId == outputGroupId) {
  //     DomainError("input and output specimen groups are the same").failureNel
  //   } else {
  //     true.success
  //   }
  // }

  implicit val specimenLinkTypeWrites: Writes[SpecimenLinkType] = Json.writes[SpecimenLinkType]

}
