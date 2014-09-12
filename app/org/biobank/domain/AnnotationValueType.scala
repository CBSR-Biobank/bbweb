package org.biobank.domain

import play.api.libs.json._

/** Used when defining custom annotations. The type of the annotation is the type of information
  * collected for the annotation.
  */
object AnnotationValueType extends Enumeration {
  type AnnotationValueType = Value
  val Text = Value("Text")
  val Number = Value("Number")
  val DateTime = Value("DateTime")
  val Select = Value("Select")

}
