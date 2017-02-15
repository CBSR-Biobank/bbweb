package org.biobank.domain

import org.biobank.infrastructure.EnumUtils._
import play.api.libs.json._

/** Used when defining custom annotations. The type of the annotation is the type of information
  * collected for the annotation.
  */
@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object AnnotationValueType extends Enumeration {

  type AnnotationValueType = Value
  val Text     = Value("text")
  val Number   = Value("number")
  val DateTime = Value("datetime")
  val Select   = Value("select")

  implicit val annotationValueTypeFormat: Format[AnnotationValueType] =
    enumFormat(AnnotationValueType)

}
