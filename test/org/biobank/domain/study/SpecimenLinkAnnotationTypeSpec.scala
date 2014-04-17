package org.biobank.domain.study

import org.biobank.domain.AnnotationTypeId
import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnnotationValueType

import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import scalaz._
import scalaz.Scalaz._

class SpecimenLinkAnnotationTypeSpec extends WordSpecLike with Matchers {

  val nameGenerator = new NameGenerator(this.getClass)

  "A specimen link annotation type" can {

    "be created" in {
      val studyId = StudyId(nameGenerator.next[SpecimenLinkAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType])
      val version = -1L
      val name = nameGenerator.next[SpecimenLinkAnnotationType]
      val description = some(nameGenerator.next[SpecimenLinkAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))

      val v = SpecimenLinkAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options)
      val annotType = v.getOrElse(fail)
      annotType shouldBe a[SpecimenLinkAnnotationType]

      annotType.studyId should be(studyId)
      annotType.id should be(id)
      annotType.version should be(0L)
      annotType.name should be(name)
      annotType.description should be(description)
      annotType.valueType should be (valueType)
      annotType.maxValueCount should be (maxValueCount)
      annotType.options should be(options)
    }

  }

  "A specimen link annotation type" can {

    "not be created with an empty study id" in {
      val studyId = StudyId("")
      val id = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType])
      val version = -1L
      val name = nameGenerator.next[SpecimenLinkAnnotationType]
      val description = some(nameGenerator.next[SpecimenLinkAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))

      SpecimenLinkAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("study id is null or empty"))
      }
    }

    "not be created with an empty id" in {
      val studyId = StudyId(nameGenerator.next[SpecimenLinkAnnotationType])
      val id = AnnotationTypeId("")
      val version = -1L
      val name = nameGenerator.next[SpecimenLinkAnnotationType]
      val description = some(nameGenerator.next[SpecimenLinkAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))

      SpecimenLinkAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("annotation type id is null or empty"))
      }
    }

    "not be created with an invalid version" in {
      val studyId = StudyId(nameGenerator.next[SpecimenLinkAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType])
      val version = -2L
      val name = nameGenerator.next[SpecimenLinkAnnotationType]
      val description = some(nameGenerator.next[SpecimenLinkAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))

      SpecimenLinkAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("invalid version value: -2"))
      }
    }

    "not be created with an null or empty name" in {
      val studyId = StudyId(nameGenerator.next[SpecimenLinkAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType])
      val version = -1L
      var name: String = null
      val description = some(nameGenerator.next[SpecimenLinkAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))

      SpecimenLinkAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("name is null or empty"))
      }

      name = ""
      SpecimenLinkAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("name is null or empty"))
      }
    }

    "not be created with an empty description option" in {
      val studyId = StudyId(nameGenerator.next[SpecimenLinkAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType])
      val version = -1L
      val name = nameGenerator.next[SpecimenLinkAnnotationType]
      var description: Option[String] = Some(null)
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))

      SpecimenLinkAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("description is null or empty"))
      }

      description = Some("")
      SpecimenLinkAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("description is null or empty"))
      }
    }

    "not be created with an negative max value count" in {
      val studyId = StudyId(nameGenerator.next[SpecimenLinkAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType])
      val version = -1L
      val name = nameGenerator.next[SpecimenLinkAnnotationType]
      val description = some(nameGenerator.next[SpecimenLinkAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(-1)
      val options = Some(Map("1" -> "a"))

      SpecimenLinkAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("max value count is not a positive number"))
      }
    }


    "not be created with an invalid options" in {
      val studyId = StudyId(nameGenerator.next[SpecimenLinkAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType])
      val version = -1L
      val name = nameGenerator.next[SpecimenLinkAnnotationType]
      val description = some(nameGenerator.next[SpecimenLinkAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      var options = Some(Map("" -> "a"))

      SpecimenLinkAnnotationType.create(
	studyId, id, version, name, description, valueType, maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("option key is null or empty"))
      }

      options = Some(Map("1" -> ""))
      SpecimenLinkAnnotationType.create(
	studyId, id, version, name, description, valueType, maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("option value is null or empty"))
      }

      options = Some(Map("1" -> null))
      SpecimenLinkAnnotationType.create(
	studyId, id, version, name, description, valueType, maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("option value is null or empty"))
      }
    }

    "have more than one validation fail" in {
      val studyId = StudyId(nameGenerator.next[SpecimenLinkAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType])
      val version = -2L
      val name = ""
      val description = some(nameGenerator.next[SpecimenLinkAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))

      SpecimenLinkAnnotationType.create(
	studyId, id, version, name, description, valueType, maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should have length 2
	  err.list.head should be ("invalid version value: -2")
	  err.list.tail.head should be ("name is null or empty")
      }
    }
  }

}
