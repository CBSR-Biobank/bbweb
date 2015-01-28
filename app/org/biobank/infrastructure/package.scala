package org.biobank

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import org.joda.time.DateTime

import scalaz._
import scalaz.Scalaz._

package infrastructure {

  sealed trait SortOrder
  case object AscendingOrder extends SortOrder
  case object DescendingOrder extends SortOrder

  object SortOrder {

    def fromString(order: String): ValidationNel[String, SortOrder] = {
      order match {
        case "asc" => AscendingOrder.successNel
        case "desc" => DescendingOrder.successNel
        case _ => s"invalid order requested: $order".failureNel
      }

    }
  }


  trait HasIdentity {

    /** A command or event that includes the ID of the object it references. */
    val id: String

  }

  trait HasStudyIdentity {

    /** An command or event that includes the study ID that it is related to. */
    val studyId: String

  }

  trait HasCentreIdentity {

    /** An command or event that includes the study ID that it is related to. */
    val centreId: String

  }

  trait HasProcessingTypeIdentity {

    /** An command or event that includes the processing type ID that it is related to. */
    val processingTypeId: String

  }



  /** Used to define annotation types associate annotation types to objects that use them.
    *
    */
  sealed trait AnnotationTypeData {

    /** @param annotationTypeId The ID of the corresponding  annotation type. */
    val annotationTypeId: String

    /** When true, then a value for this annotation type is required when the collection event is entered.
      */
    val required: Boolean
  }

  /** Used to define annotation types to be used by a [[org.biobank.domain.study.CollectionEventType]]. */
  case class CollectionEventTypeAnnotationTypeData(
    annotationTypeId: String,
    required: Boolean)
      extends AnnotationTypeData

  /** Used to define annotation types to be used by a [[org.biobank.domain.study.SpecimenLinkType]]. */
  case class SpecimenLinkTypeAnnotationTypeData(
    annotationTypeId: String,
    required: Boolean)
      extends AnnotationTypeData

  /** Used to define which types of specimens (i.e. which [[org.biobank.domain.study.SpecimenGroup]]s) need to
    * be collected with this type of collection event. A single specimen group can be used in multiple
    * collection event types.
    *
    * @param specimenGroupId The ID associated with the [[org.biobank.domain.study.SpecimenGroup]].
    *
    * @param name A copy of the name of the corresponding specimen group. Read only.
    *
    * @param maxCount The number of specimens required to be collected.
    *
    * @param amount The amount of substance that is expected in each collected specimen, or None
    *        if there is no default amount. The unit on the amount is defined in the SpecimenGroup.
    *
    * @param units A copy of the units field from the specimen group. Read only.
    *
    */
  case class CollectionEventTypeSpecimenGroupData(
    specimenGroupId: String,
    maxCount: Int,
    amount: Option[BigDecimal])

  object CollectionEventTypeAnnotationTypeData {

    implicit val annotationTypeDataFormat = Json.format[CollectionEventTypeAnnotationTypeData]

  }

  object CollectionEventTypeSpecimenGroupData {

    implicit val specimenGroupDataFormat = Json.format[CollectionEventTypeSpecimenGroupData]

  }

  object SpecimenLinkTypeAnnotationTypeData {

    implicit val annotationTypeDataFormat = Json.format[SpecimenLinkTypeAnnotationTypeData]

  }

  case class StudyNameDto(id:String, name: String)

  object StudyNameDto {
    def compareByName(a: StudyNameDto, b: StudyNameDto) = (a.name compareToIgnoreCase b.name) < 0

    implicit val studyNameDtoWriter = Json.writes[StudyNameDto]
  }

}
