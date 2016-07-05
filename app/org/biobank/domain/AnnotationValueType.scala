package org.biobank.domain

import org.biobank.infrastructure.EnumUtils._
import play.api.libs.json._

/** Used when defining custom annotations. The type of the annotation is the type of information
  * collected for the annotation.
  */
@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object AnnotationValueType extends Enumeration {

  type AnnotationValueType = Value
  val Text     = Value("Text")
  val Number   = Value("Number")
  val DateTime = Value("DateTime")
  val Select   = Value("Select")

  implicit val annotationValueTypeFormat: Format[AnnotationValueType] =
    enumFormat(AnnotationValueType)

}
