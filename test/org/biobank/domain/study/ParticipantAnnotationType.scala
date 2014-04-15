package org.biobank.domain.study

import org.biobank.domain.AnnotationTypeId
import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnnotationValueType

import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import scalaz._
import scalaz.Scalaz._


class ParticipantAnnotationTypeSpec extends WordSpecLike with Matchers {

  val nameGenerator = new NameGenerator(this.getClass.getName)

  "A participant annotation type" can {

    "be created" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -1L
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))
      val required = true

      val v = ParticipantAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options, required)
      val annotType = v.getOrElse(fail)
      annotType shouldBe a[ParticipantAnnotationType]

      annotType.studyId should be(studyId)
      annotType.id should be(id)
      annotType.version should be(0L)
      annotType.name should be(name)
      annotType.description should be(description)
      annotType.valueType should be (valueType)
      annotType.maxValueCount should be (maxValueCount)
      annotType.options should be(options)
      annotType.required should be (required)
    }

  }

  "A participant annotation type" can {

    "not be created with an empty study id" in {
      val studyId = StudyId("")
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -1L
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))
      val required = true

      ParticipantAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options, required) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("study id is null or empty")
      }
    }

    "not be created with an empty id" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId("")
      val version = -1L
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))
      val required = true

      ParticipantAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options, required) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("id is null or empty")
      }
    }

    "not be created with an invalid version" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -2L
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))
      val required = true

      ParticipantAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options, required) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("invalid version value")
      }
    }

    "not be created with an null or empty name" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -2L
      var name: String = null
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))
      val required = true

      ParticipantAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options, required) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("name is null or empty")
      }

      name = ""
      ParticipantAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options, required) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("name is null or empty")
      }
    }

    "not be created with an empty description option" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -2L
      val name = nameGenerator.next[ParticipantAnnotationType]
      var description: Option[String] = Some(null)
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))
      val required = true

      ParticipantAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options, required) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("description is null or empty")
      }

      description = Some("")
      ParticipantAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options, required) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("description is null or empty")
      }
    }

    "not be created with an negative max value count" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -1L
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      var options = Some(Map("" -> "a"))
      val required = true

      ParticipantAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options, required) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("max value count is not a positive number")
      }

      options = Some(Map("1" -> ""))
      ParticipantAnnotationType.create(studyId, id, version, name, description, valueType,
	maxValueCount, options, required) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("max value count is not a positive number")
      }
    }

  }

}
