package org.biobank.domain

import org.biobank.domain.annotations._
import org.biobank.fixtures.NameGenerator

object AnnotationTypeSpecUtil {

  type AnnotTypeTuple = Tuple6[String,
                               Some[String],
                               AnnotationValueType.Value,
                               Option[Int],
                               Seq[String],
                               Boolean]

  val nameGenerator = new NameGenerator(this.getClass)

  def annotationTypeNoValue = {
    val name        = nameGenerator.next[AnnotationType]
    val description = Some(nameGenerator.next[AnnotationType])

    (name, description)
  }

  def nonSelectAnnotationTypeTuple = {
    val (name, description) = annotationTypeNoValue
    val maxValueCount = None
    val options = Seq.empty
    val required = false

    (name, description, maxValueCount, options, required)
  }

  def textAnnotationTypeTuple(): AnnotTypeTuple = {
    val (name, description, maxValueCount, options, required) =
      nonSelectAnnotationTypeTuple
    val valueType = AnnotationValueType.Text
    (name, description, valueType, maxValueCount, options, required)
  }

  def numberAnnotationTypeTuple(): AnnotTypeTuple = {
    val (name, description, maxValueCount, options, required) =
      nonSelectAnnotationTypeTuple
    val valueType = AnnotationValueType.Number
    (name, description, valueType, maxValueCount, options, required)
  }

  def dateTimeAnnotationTypeTuple(): AnnotTypeTuple = {
    val (name, description, maxValueCount, options, required) =
      nonSelectAnnotationTypeTuple
    val valueType = AnnotationValueType.DateTime
    (name, description, valueType, maxValueCount, options, required)
  }

  def selectAnnotationTypeTuple(): AnnotTypeTuple = {
    val (name, description) = annotationTypeNoValue

    val valueType = AnnotationValueType.Select
    val maxValueCount = Some(1)
    val options = Seq(nameGenerator.next[String], nameGenerator.next[String])
    val required = false

    (name, description, valueType, maxValueCount, options, required)
  }

  val AnnotationValueTypeToTuple
      : Map[AnnotationValueType.AnnotationValueType, AnnotTypeTuple] = Map(
    AnnotationValueType.Text     -> textAnnotationTypeTuple,
    AnnotationValueType.Number   -> numberAnnotationTypeTuple,
    AnnotationValueType.DateTime -> dateTimeAnnotationTypeTuple,
    AnnotationValueType.Select   -> selectAnnotationTypeTuple
  )
}
