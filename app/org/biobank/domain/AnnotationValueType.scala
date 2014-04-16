package org.biobank.domain

/** Used when defining custom annotations. The type of the annotation is the type of information
  * collected for the annotation.
  */
object AnnotationValueType extends Enumeration {
  type AnnotationValueType = Value
  val Text = Value("Text")
  val Number = Value("Number")
  val Date = Value("Date")
  val Select = Value("Select")
}
