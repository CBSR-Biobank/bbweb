package org.biobank

import play.api.libs.json._
import play.api.libs.json.Reads._

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
        case _ => s"invalid order requested: $order".failureNel[SortOrder]
      }

    }
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

  /** Used to define annotation types to be used by a [[org.biobank.domain.study.SpecimenLinkType]]. */
  final case class SpecimenLinkTypeAnnotationTypeData(annotationTypeId: String, required: Boolean)
      extends AnnotationTypeData

  object SpecimenLinkTypeAnnotationTypeData {

    implicit val annotationTypeDataFormat: Format[SpecimenLinkTypeAnnotationTypeData] =
      Json.format[SpecimenLinkTypeAnnotationTypeData]

  }

}
