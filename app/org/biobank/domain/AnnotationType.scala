package org.biobank.domain

import org.biobank.domain.AnnotationValueType._
import org.biobank.infrastructure._

import scalaz._
import Scalaz._

/** Annotation types define an [[Annotation]].
  *
  * Annotations allow sub classes to collect custom named and defined pieces of data.
  */
trait AnnotationType
  extends ConcurrencySafeEntity[AnnotationTypeId]
    with HasUniqueName
    with HasDescriptionOption {

  /** The type of information stored by the annotation. I.e. text, number, date, or an item from a drop down
    * list. See [[AnnotationValueType]].
    */
  val valueType: AnnotationValueType


  /** When valueType is [[AnnotationValueType.Select]] (i.e. a drop down list), this is the number of items
    * allowed to be selected. If the value is 0 then any number of values can be selected.
    */
  val maxValueCount: Option[Int]


  /** When valueType is [[AnnotationValueType.Select]], these are the list of options allowed to
    * be selected.
    *
    * @todo describe why this is a map.
    */
  val options: Option[Seq[String]]

  private def validateValueType: DomainValidation[Boolean] = {
    if (valueType.equals(AnnotationValueType.Select)) {
      if (options.isEmpty) ("select annotation type with no values to select").failNel
    } else {
      if (!options.isEmpty) ("non select annotation type with values to select").failNel
    }
    true.success
  }

}
