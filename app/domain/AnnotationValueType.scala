package domain

object AnnotationValueType extends Enumeration {
  type AnnotationValueType = Value
  val Text = Value("Text")
  val Number = Value("Number")
  val Date = Value("Date")
  val Select = Value("Select")
}