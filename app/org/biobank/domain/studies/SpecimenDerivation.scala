package org.biobank.domain.studies

import org.biobank._
import org.biobank.domain._
import play.api.libs.json._
import scalaz.Scalaz._

class SpecimenDerivationType(val id: String) extends AnyVal {
  override def toString: String = id
}

trait SpecimenDerivationValidation {

  case object CollectionEventTypeIdRequired extends ValidationKey

  case object ProcessingTypeIdRequired extends ValidationKey

  case object SpecimenDefinitionIdRequired extends ValidationKey

}

sealed trait SpecimenDerivation {

  val derivationType:SpecimenDerivationType

  /**
   * The ID of the *input* [[domain.studies.SpecimenDefinition SpecimenDefinition]] this
   * processing type is for.
   */
  val inputSpecimenDefinitionId: SpecimenDefinitionId

  /**
   *  The ID of the *output* [[domain.studies.ProcessingSpecimenDefinition ProcessingSpecimenDefinition]] this
   * processing type is for.
   */
  val outputSpecimenDefinition: ProcessingSpecimenDefinition

  def validate(): DomainValidation[Boolean] = {
    this match {
      case derivation:CollectedSpecimenDerivation => derivation.validate()
      case derivation:ProcessedSpecimenDerivation => derivation.validate()
    }
  }

}

object SpecimenDerivation {

  val collectedDerivation: SpecimenDerivationType = new SpecimenDerivationType("collectedDerivation")
  val processedDerivation: SpecimenDerivationType = new SpecimenDerivationType("processedDerivation")

  implicit val collectedSpecimenDerivationFormat: Format[CollectedSpecimenDerivation] =
    Json.format[CollectedSpecimenDerivation]

  implicit val processedSpecimenDerivationFormat: Format[ProcessedSpecimenDerivation] =
    Json.format[ProcessedSpecimenDerivation]

 @SuppressWarnings(Array("org.wartremover.warts.Option2Iterable"))
  implicit val specimenDerivationFormat: Format[SpecimenDerivation] = new Format[SpecimenDerivation] {

      override def reads(json: JsValue): JsResult[SpecimenDerivation] = {
        (json \ "type") match {
          case JsDefined(JsString(collectedDerivation.id)) => json.validate[CollectedSpecimenDerivation]
          case JsDefined(JsString(processedDerivation.id)) => json.validate[ProcessedSpecimenDerivation]
          case _ => JsError("error")
        }
      }

      override def writes(derivation: SpecimenDerivation): JsValue = {
        val json = derivation match {
          case d: CollectedSpecimenDerivation => collectedSpecimenDerivationFormat.writes(d)
          case d: ProcessedSpecimenDerivation => processedSpecimenDerivationFormat.writes(d)
          }
        json.as[JsObject] ++ Json.obj("type" -> derivation.derivationType.id)
      }
    }
}

/**
 * Defines that a [[domain.participant.Specimen Specimen]] is derived from a collected
 * [[domain.participant.Specimen Specimen]] when processed.
 */
final case class CollectedSpecimenDerivation(collectionEventTypeId: CollectionEventTypeId,
                                             inputSpecimenDefinitionId: SpecimenDefinitionId,
                                             outputSpecimenDefinition: ProcessingSpecimenDefinition)
    extends { val derivationType: SpecimenDerivationType = SpecimenDerivation.collectedDerivation }
    with SpecimenDerivation
    with SpecimenDerivationValidation {
  import org.biobank.domain.DomainValidations._

  override def validate(): DomainValidation[Boolean] = {
    (validateId(collectionEventTypeId, CollectionEventTypeIdRequired) |@|
       validateId(inputSpecimenDefinitionId, SpecimenDefinitionIdRequired) |@|
       ProcessingSpecimenDefinition.validate(outputSpecimenDefinition)) { case _ => true }
  }
}

/**
 * Defines that a [[domain.participant.Specimen Specimen]] is derived from a processed
 * [[domain.participant.Specimen Specimen]] when processed.
 */
final case class ProcessedSpecimenDerivation(processingTypeId: ProcessingTypeId,
                                             inputSpecimenDefinitionId: SpecimenDefinitionId,
                                             outputSpecimenDefinition: ProcessingSpecimenDefinition)
    extends { val derivationType: SpecimenDerivationType = SpecimenDerivation.processedDerivation }
    with SpecimenDerivation
    with SpecimenDerivationValidation {
  import org.biobank.domain.DomainValidations._

  override def validate(): DomainValidation[Boolean] = {
    (validateId(processingTypeId, ProcessingTypeIdRequired) |@|
       validateId(inputSpecimenDefinitionId, SpecimenDefinitionIdRequired) |@|
       ProcessingSpecimenDefinition.validate(outputSpecimenDefinition)) { case _ => true }
  }
}

object ProcessedSpecimenDerivation {

}
