package org.biobank.domain.study

import org.biobank.domain.AnnotationTypeId
import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnnotationValueType

import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import scalaz._
import scalaz.Scalaz._


class CollectionEventAnnotationTypeSpec extends WordSpecLike with Matchers {

  val nameGenerator = new NameGenerator(this.getClass.getName)

  "A collection event annotation type" can {

    "be created" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventAnnotationType]
      val description = some(nameGenerator.next[CollectionEventAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))

      val v = CollectionEventAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options)
      val annotType = v.getOrElse(fail)
      annotType shouldBe a[CollectionEventAnnotationType]

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

  "A collection event annotation type" can {

    "not be created with an empty study id" in {
      val studyId = StudyId("")
      val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventAnnotationType]
      val description = some(nameGenerator.next[CollectionEventAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))

      CollectionEventAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("study id is null or empty"))
      }
    }

    "not be created with an empty id" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventAnnotationType])
      val id = AnnotationTypeId("")
      val version = -1L
      val name = nameGenerator.next[CollectionEventAnnotationType]
      val description = some(nameGenerator.next[CollectionEventAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))

      CollectionEventAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("annotation type id is null or empty"))
      }
    }

    "not be created with an invalid version" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
      val version = -2L
      val name = nameGenerator.next[CollectionEventAnnotationType]
      val description = some(nameGenerator.next[CollectionEventAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))

      CollectionEventAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("invalid version value: -2"))
      }
    }

    "not be created with an null or empty name" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
      val version = -1L
      var name: String = null
      val description = some(nameGenerator.next[CollectionEventAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))

      CollectionEventAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("name is null or empty"))
      }

      name = ""
      CollectionEventAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("name is null or empty"))
      }
    }

    "not be created with an empty description option" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventAnnotationType]
      var description: Option[String] = Some(null)
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))

      CollectionEventAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("description is null or empty"))
      }

      description = Some("")
      CollectionEventAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("description is null or empty"))
      }
    }

    "not be created with an negative max value count" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventAnnotationType]
      val description = some(nameGenerator.next[CollectionEventAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(-1)
      val options = Some(Map("1" -> "a"))

      CollectionEventAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("max value count is not a positive number"))
      }
    }


    "not be created with an invalid options" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventAnnotationType]
      val description = some(nameGenerator.next[CollectionEventAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      var options = Some(Map("" -> "a"))

      CollectionEventAnnotationType.create(
	studyId, id, version, name, description, valueType, maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("option key is null or empty"))
      }

      options = Some(Map("1" -> ""))
      CollectionEventAnnotationType.create(
	studyId, id, version, name, description, valueType, maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("option value is null or empty"))
      }

      options = Some(Map("1" -> null))
      CollectionEventAnnotationType.create(
	studyId, id, version, name, description, valueType, maxValueCount, options) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("option value is null or empty"))
      }
    }

    "have more than one validation fail" in {
      fail
    }

  }
}
