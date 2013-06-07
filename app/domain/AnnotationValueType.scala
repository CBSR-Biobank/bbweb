package domain

object AnnotationValueType extends Enumeration {
  type AnnotationValueType = Value
  val String = Value("String")
  val Name = Value("Name")
  val Date = Value("Date")
  val Select = Value("Select")
}