package org.biobank.domain

/** Used to store the value for an annotation.
  *
  */
trait Annotation[T <: AnnotationType] {

  val annotationTypeId: AnnotationTypeId

  val stringValue: Option[String]

  val numberValue: Option[String] //Fixme: change to Option[java.lang.Number]

  val selectedValues: Option[List[AnnotationOption]]

}
