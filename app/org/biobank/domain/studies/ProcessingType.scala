package org.biobank.domain.studies

import java.time.OffsetDateTime
import org.biobank.ValidationKey
import org.biobank.domain._
import org.biobank.domain.annotations._
import org.biobank.domain.containers._
import play.api.libs.json._
import scalaz.Scalaz._

/**
 * Predicates that can be used to filter collections of [[ProcessingType]]s.
 *
 */
trait ProcessingTypePredicates extends HasNamePredicates[ProcessingType] {

  type ProcessingTypeFilter = ProcessingType => Boolean

}

trait ProcessingTypeValidations extends StudyValidations {

  case object ProcessingTypeIdRequired extends ValidationKey

  case object ProcessedSpecimenDefinitionIdRequired extends ValidationKey

  case object CollectedSpecimenDefinitionIdRequired extends ValidationKey

  case object ContainerTypeIdRequired extends ValidationKey

  case object InvalidPositiveNumber extends ValidationKey
}

/**
 * Records a regularly preformed specimen processing procedure. It defines and allows for
 * recording of procedures performed on different types of [[domain.participants.Specimen Specimen]]s.
 *
 * For speicmen processing to take place, a study must have at least one processing type defined.
 *
 * @param enabled A processing type should have enabled set to `TRUE` when processing of the contained specimen types is
 * taking place. However, throughout the lifetime of the study, it may be decided to stop a processing type
 * in favour of another. In this case enabled is set to `FALSE`.
 *
 * @param expectedInputChange The expected amount to be removed from each input. If the value is not required then use a value of zero.
 *
 * @param expectedOutputChange The expected amount to be added to each output. If the value is not required
 * then use a value of zero.
 *
 * @param inputCount  The number of expected specimens when the processing is carried out.
 *
 * @param outputCount The number of resulting specimens when the processing is carried out. A value of zero
 * for output count implies that the count is the same as the input count.
 *
 * @param specimenDerivation Details for how the processed specimen is derived.
 *
 * @param outputContainerTypeId The [[domain.containers.ContainerType ContainerType]] that the output
 * [[domian.participants.Specimen Specimens]] are stored into. This is an optional field.
 *
 * @param annotationTypes The [[domain.studies.AnnotationType AnnotationType]]s for recorded for this
 * processing type.
 */
final case class ProcessingType(studyId:               StudyId,
                                id:                    ProcessingTypeId,
                                version:               Long,
                                timeAdded:             OffsetDateTime,
                                timeModified:          Option[OffsetDateTime],
                                slug:                  Slug,
                                name:                  String,
                                description:           Option[String],
                                enabled:               Boolean,
                                expectedInputChange:   BigDecimal,
                                expectedOutputChange:  BigDecimal,
                                inputCount:            Int,
                                outputCount:           Int,
                                specimenDerivation:    SpecimenDerivation,
                                inputContainerTypeId:  Option[ContainerTypeId],
                                outputContainerTypeId: Option[ContainerTypeId],
                                annotationTypes:       Set[AnnotationType])
    extends ConcurrencySafeEntity[ProcessingTypeId]
    with HasUniqueName
    with HasSlug
    with HasOptionalDescription
    with HasStudyId
    with HasAnnotationTypes
    with ProcessingTypeValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  /** Used to change the name. */
  def withName(name: String): DomainValidation[ProcessingType] = {
    validateString(name, NameMinLength, InvalidName) map { _ =>
      copy(slug         = Slug(name),
           name         = name,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  /** Used to change the description. */
  def withDescription(description: Option[String]): DomainValidation[ProcessingType] = {
    validateNonEmptyStringOption(description, InvalidDescription) map { _ =>
      copy(description  = description,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  /** Used to enable the processing type. */
  def enable(): ProcessingType = {
    copy(enabled      = true,
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  /** Used to disable the processing type. */
  def disable(): ProcessingType = {
    copy(enabled      = false,
         version      = version + 1,
         timeModified = Some(OffsetDateTime.now))
  }

  /** Updates the expected input change. */
  def withExpectedInputChange(change: BigDecimal): DomainValidation[ProcessingType] = {
    validatePositiveNumber(change, InvalidPositiveNumber) map { _ =>
      copy(expectedInputChange = change,
           version             = version + 1,
           timeModified        = Some(OffsetDateTime.now))
    }
  }

  /** Updates the expected output change. */
  def withExpectedOutputChange(change: BigDecimal): DomainValidation[ProcessingType] = {
    validatePositiveNumber(change, InvalidPositiveNumber) map { _ =>
      copy(expectedOutputChange = change,
           version             = version + 1,
           timeModified        = Some(OffsetDateTime.now))
    }
  }

  /** Updates the input count. */
  def withInputCount(count: Int): DomainValidation[ProcessingType] = {
    validatePositiveNumber(count, InvalidPositiveNumber) map { _ =>
      copy(inputCount   = count,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  /** Updates the output count. */
  def withOutputCount(count: Int): DomainValidation[ProcessingType] = {
    validatePositiveNumber(count, InvalidPositiveNumber) map { _ =>
      copy(outputCount  = count,
           version      = version + 1,
           timeModified = Some(OffsetDateTime.now))
    }
  }

  def withAnnotationType(annotationType: AnnotationType): DomainValidation[ProcessingType] = {
    checkAddAnnotationType(annotationType).map { _ =>
      // replaces previous annotation type with same unique id
      val newAnnotationTypes = annotationTypes - annotationType + annotationType
      copy(annotationTypes = newAnnotationTypes,
           version         = version + 1,
           timeModified    = Some(OffsetDateTime.now))
    }
  }

  def removeAnnotationType(annotationTypeId: AnnotationTypeId): DomainValidation[ProcessingType] = {
    checkRemoveAnnotationType(annotationTypeId).map { annotationType =>
      val newAnnotationTypes = annotationTypes - annotationType
      copy(annotationTypes = newAnnotationTypes,
           version         = version + 1,
           timeModified    = Some(OffsetDateTime.now))
    }
  }

  override def toString: String =
    s"""|ProcessingType:{
        |  studyId:               $studyId,
        |  id:                    $id,
        |  timeAdded:             $timeAdded,
        |  timeModified:          $timeModified,
        |  version:               $version,
        |  name:                  $name,
        |  description:           $description,
        |  enabled:               $enabled,
        |  expectedInputChange:   $expectedInputChange,
        |  expectedOutputChange:  $expectedOutputChange,
        |  inputCount:            $inputCount,
        |  outputCount:           $outputCount,
        |  specimenDerivation:    $specimenDerivation,
        |  inputContainerTypeId:  $inputContainerTypeId,
        |  outputContainerTypeId: $outputContainerTypeId
        |  annotationTypes:       { $annotationTypes }
        |}""".stripMargin
}

object ProcessingType extends ProcessingTypeValidations {
  import org.biobank.CommonValidations._
  import org.biobank.domain.DomainValidations._

  implicit val processingTypeFormat: Format[ProcessingType] =
    Json.format[ProcessingType]

  def create(studyId:               StudyId,
             id:                    ProcessingTypeId,
             version:               Long,
             name:                  String,
             description:           Option[String],
             enabled:               Boolean,
             expectedInputChange:   BigDecimal,
             expectedOutputChange:  BigDecimal,
             inputCount:            Int,
             outputCount:           Int,
             specimenDerivation:    SpecimenDerivation,
             inputContainerTypeId:  Option[ContainerTypeId],
             outputContainerTypeId: Option[ContainerTypeId],
             annotationTypes:       Set[AnnotationType])
      : DomainValidation[ProcessingType] = {

    def validateNumbers(): DomainValidation[Boolean] = {
      (validatePositiveNumber(expectedOutputChange, InvalidPositiveNumber) |@|
         validatePositiveNumber(expectedInputChange, InvalidPositiveNumber) |@|
         validatePositiveNumber(outputCount, InvalidPositiveNumber) |@|
         validatePositiveNumber(inputCount, InvalidPositiveNumber)) { case _ => true  }
    }

    /*
     * Scalaz applicative builders can only be used with up to 12 parameters.
     */
    (validateId(studyId, StudyIdRequired) |@|
       validateId(id, ProcessingTypeIdRequired) |@|
       validateVersion(version) |@|
       validateNonEmptyString(name, NameRequired) |@|
       validateNonEmptyStringOption(description, InvalidDescription) |@|
       validateNumbers() |@|
       specimenDerivation.validate() |@|
       validateIdOption(inputContainerTypeId, ContainerTypeIdRequired) |@|
       validateIdOption(outputContainerTypeId, ContainerTypeIdRequired) |@|
       annotationTypes.toList.traverseU(AnnotationType.validate)) { case _ =>
        ProcessingType(studyId               = studyId,
                       id                    = id,
                       version               = version,
                       timeAdded             = OffsetDateTime.now,
                       timeModified          = None,
                       slug                  = Slug(name),
                       name                  = name,
                       description           = description,
                       enabled               = enabled,
                       expectedInputChange   = expectedInputChange,
                       expectedOutputChange  = expectedOutputChange,
                       inputCount            = inputCount,
                       outputCount           = outputCount,
                       specimenDerivation    = specimenDerivation,
                       inputContainerTypeId  = inputContainerTypeId,
                       outputContainerTypeId = outputContainerTypeId,
                       annotationTypes       = annotationTypes)

    }
  }

  val sort2Compare: Map[String, (ProcessingType, ProcessingType) => Boolean] =
    Map[String, (ProcessingType, ProcessingType) => Boolean]("name"  -> compareByName)

  def compareByName(a: ProcessingType, b: ProcessingType): Boolean =
    (a.name compareToIgnoreCase b.name) < 0
}
