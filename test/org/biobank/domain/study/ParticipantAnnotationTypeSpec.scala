package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.domain.AnnotationTypeId
import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnnotationValueType

import org.scalatest.OptionValues._
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
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = true

      val annotType = ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required) | fail
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

      (annotType.timeAdded to DateTime.now).millis should be < 200L
      annotType.timeModified should be (None)
    }

    "be updated" in {
      val annotType = factory.createParticipantAnnotationType

      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(annotType.maxValueCount.getOrElse(0) + 100)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = !annotType.required

      val annotType2 = annotType.update(name, description, valueType, maxValueCount, options, required) | fail
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

      annotType2.timeAdded should be (annotType.timeAdded)
      // last update date is assigned by the processor
      annotType2.timeModified should be (None)
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
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = true

      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list should (have length 1 and contain("IdRequired")),
        user => fail
      )
    }

    "not be created with an empty id" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId("")
      val version = -1L
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = true

      ParticipantAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list should (have length 1 and contain("IdRequired")),
        user => fail
      )
    }

    "not be created with an invalid version" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -2L
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = true

      ParticipantAnnotationType.create(studyId, id, version, DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list should (have length 1 and contain("InvalidVersion")),
        user => fail
      )
    }

    "not be created with an null or empty name" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -1L
      var name: String = null
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = true

      ParticipantAnnotationType.create(studyId, id, version, DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list should (have length 1 and contain("NameRequired")),
        user => fail
      )

      name = ""
      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list should (have length 1 and contain("NameRequired")),
        user => fail
      )
    }

    "not be created with an empty description option" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -1L
      val name = nameGenerator.next[ParticipantAnnotationType]
      var description: Option[String] = Some(null)
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = true

      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list should (have length 1 and contain("NonEmptyDescription")),
        user => fail
      )

      description = Some("")
      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list should (have length 1 and contain("NonEmptyDescription")),
        user => fail
      )
    }

    "not be created with an negative max value count" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -1L
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(-1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = true

      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list should (have length 1 and contain("MaxValueCountError")),
        user => fail
      )
    }

    "not be created with an invalid options" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -1L
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      var options = Some(Seq(""))
      val required = true

      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list should (have length 1 and contain("OptionRequired")),
        user => fail
      )

      options = Some(Seq("duplicate", "duplicate"))
      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list should (have length 1 and contain("DuplicateOptionsError")),
          user => fail
      )
    }

    "have more than one validation fail" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -2L
      val name = ""
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = true

      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => {
          err.list should have length 2
          err.list.head should be ("InvalidVersion")
          err.list.tail.head should be ("NameRequired")
        },
        user => fail
      )
    }

  }

}
