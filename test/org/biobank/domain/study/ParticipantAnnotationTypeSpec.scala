package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.domain.AnnotationTypeId
import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnnotationValueType

import com.github.nscala_time.time.Imports._
import scalaz._
import scalaz.Scalaz._


class ParticipantAnnotationTypeSpec extends DomainSpec {

  val nameGenerator = new NameGenerator(this.getClass)

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

      val annotType = ParticipantAnnotationType.create(
        studyId, id, version, name, description, valueType, maxValueCount, options, required) | fail
      annotType shouldBe a[ParticipantAnnotationType]
      annotType should have (
        'studyId (studyId),
        'id (id),
        'version (0L),
        'name (name),
        'description (description),
        'valueType  (valueType),
        'maxValueCount  (maxValueCount),
        'options (options),
        'required  (required)
      )

      (annotType.addedDate to DateTime.now).millis should be < 100L
      annotType.lastUpdateDate should be (None)
    }

    "be updated" in {
      val annotType = factory.createParticipantAnnotationType

      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(annotType.maxValueCount.getOrElse(0) + 100)
      val options = Some(Map(nameGenerator.next[String] -> nameGenerator.next[String]))
      val required = !annotType.required

      val annotType2 = annotType.update(
        annotType.versionOption, name, description, valueType, maxValueCount, options, required) | fail
      annotType2 shouldBe a[ParticipantAnnotationType]
      annotType2 should have (
        'studyId (annotType.studyId),
        'id (annotType.id),
        'version (annotType.version + 1),
        'name (name),
        'description (description),
        'valueType  (valueType),
        'maxValueCount  (maxValueCount),
        'options (options),
        'required  (required)
      )

      annotType2.addedDate should be (annotType.addedDate)
      val updateDate = annotType2.lastUpdateDate | fail
      (updateDate to DateTime.now).millis should be < 100L
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
          err.list should (have length 1 and contain("study id is null or empty"))
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
          err.list should (have length 1 and contain("annotation type id is null or empty"))
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
          err.list should (have length 1 and contain("invalid version value: -2"))
      }
    }

    "not be created with an null or empty name" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -1L
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
          err.list should (have length 1 and contain("name is null or empty"))
      }

      name = ""
      ParticipantAnnotationType.create(studyId, id, version, name, description, valueType,
        maxValueCount, options, required) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("name is null or empty"))
      }
    }

    "not be created with an empty description option" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -1L
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
          err.list should (have length 1 and contain("description is null or empty"))
      }

      description = Some("")
      ParticipantAnnotationType.create(studyId, id, version, name, description, valueType,
        maxValueCount, options, required) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("description is null or empty"))
      }
    }

    "not be created with an negative max value count" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -1L
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(-1)
      var options = Some(Map("1" -> "a"))
      val required = true

      ParticipantAnnotationType.create(studyId, id, version, name, description, valueType,
        maxValueCount, options, required) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("max value count is not a positive number"))
      }
    }

    "not be created with an invalid options" in {
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
          err.list should (have length 1 and contain("option key is null or empty"))
      }

      options = Some(Map("1" -> ""))
      ParticipantAnnotationType.create(studyId, id, version, name, description, valueType,
        maxValueCount, options, required) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("option value is null or empty"))
      }

      options = Some(Map("1" -> null))
      ParticipantAnnotationType.create(studyId, id, version, name, description, valueType,
        maxValueCount, options, required) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("option value is null or empty"))
      }
    }

    "have more than one validation fail" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -2L
      val name = ""
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Map("1" -> "a"))
      val required = true

      ParticipantAnnotationType.create(studyId, id, version, name, description, valueType,
        maxValueCount, options, required) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should have length 2
          err.list.head should be ("invalid version value: -2")
          err.list.tail.head should be ("name is null or empty")
      }
    }

  }

}
