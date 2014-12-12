package org.biobank.domain.study

import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnnotationTypeId
import org.biobank.domain.AnnotationValueType

object StudyAnnotationTypeSpecUtil {

  type AnnotTypeTuple = Tuple8[
    StudyId, AnnotationTypeId, Long, String, Some[String], AnnotationValueType.Value,
    Option[Int], Option[Seq[String]]]

  val nameGenerator = new NameGenerator(this.getClass)

  def annotationTypeNoValueTypeTuple = {
    val studyId = StudyId(nameGenerator.next[CollectionEventAnnotationType])
    val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
    val version = -1L
    val name = nameGenerator.next[CollectionEventAnnotationType]
    val description = Some(nameGenerator.next[CollectionEventAnnotationType])

    (studyId, id, version, name, description)
  }

  def nonSelectAnnotationTypeTuple = {
    val (studyId, id, version, name, description) = annotationTypeNoValueTypeTuple
    val maxValueCount = None
    val options = None

    (studyId, id, version, name, description, maxValueCount, options)
  }

  def textAnnotationTypeTuple = {
    val (studyId, id, version, name, description, maxValueCount, options) = nonSelectAnnotationTypeTuple
    val valueType = AnnotationValueType.Text
    (studyId, id, version, name, description, valueType, maxValueCount, options)
  }

  def numberAnnotationTypeTuple = {
    val (studyId, id, version, name, description, maxValueCount, options) = nonSelectAnnotationTypeTuple
    val valueType = AnnotationValueType.Number
    (studyId, id, version, name, description, valueType, maxValueCount, options)
  }

  def dateTimeAnnotationTypeTuple = {
    val (studyId, id, version, name, description, maxValueCount, options) = nonSelectAnnotationTypeTuple
    val valueType = AnnotationValueType.DateTime
    (studyId, id, version, name, description, valueType, maxValueCount, options)
  }

  def selectAnnotationTypeTuple = {
    val (studyId, id, version, name, description) = annotationTypeNoValueTypeTuple

    val valueType = AnnotationValueType.Select
    val maxValueCount = Some(1)
    val options = Some(Seq(
      nameGenerator.next[String],
      nameGenerator.next[String]))

    (studyId, id, version, name, description, valueType, maxValueCount, options)
  }

  val AnnotationValueTypeToTuple
      : Map[AnnotationValueType.AnnotationValueType, AnnotTypeTuple] = Map(
    AnnotationValueType.Text     -> textAnnotationTypeTuple,
    AnnotationValueType.Number   -> numberAnnotationTypeTuple,
    AnnotationValueType.DateTime -> dateTimeAnnotationTypeTuple,
    AnnotationValueType.Select   -> selectAnnotationTypeTuple
  )
}
