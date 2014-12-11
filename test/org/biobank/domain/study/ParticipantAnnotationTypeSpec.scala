package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.domain.AnnotationTypeId
import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnnotationValueType

import com.github.nscala_time.time.Imports._
import scalaz._
import scalaz.Scalaz._


class ParticipantAnnotationTypeSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val nameGenerator = new NameGenerator(this.getClass)

  "A participant annotation type" can {

    "be created" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -1L
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Select
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = true

      val v = ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required)
      v mustSucceed { annotType =>
        annotType mustBe a[ParticipantAnnotationType]
        annotType must have (
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

        (annotType.timeAdded to DateTime.now).millis must be < 200L
        annotType.timeModified mustBe (None)
      }
    }

    "be updated" in {
      val annotType = factory.createParticipantAnnotationType

      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Select
      val maxValueCount = Some(annotType.maxValueCount.getOrElse(0) + 1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = !annotType.required

      val v = annotType.update(name, description, valueType, maxValueCount, options, required)
      v mustSucceed { at =>
        at mustBe a[ParticipantAnnotationType]
        at must have (
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

        at.timeAdded mustBe (annotType.timeAdded)
        // last update date is assigned by the processor
        at.timeModified mustBe (None)
      }
    }

  }

  "A participant annotation type" can {

    "not be created with an empty study id" in {
      val studyId = StudyId("")
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -1L
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Select
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = true

      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list must (have length 1 and contain("IdRequired")),
        user => fail
      )
    }

    "not be created with an empty id" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId("")
      val version = -1L
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Select
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = true

      ParticipantAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list must (have length 1 and contain("IdRequired")),
        user => fail
      )
    }

    "not be created with an invalid version" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -2L
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Select
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = true

      ParticipantAnnotationType.create(studyId, id, version, DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list must (have length 1 and contain("InvalidVersion")),
        user => fail
      )
    }

    "not be created with an null or empty name" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -1L
      var name: String = null
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Select
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = true

      ParticipantAnnotationType.create(studyId, id, version, DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list must (have length 1 and contain("NameRequired")),
        user => fail
      )

      name = ""
      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list must (have length 1 and contain("NameRequired")),
        user => fail
      )
    }

    "not be created with an empty description option" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -1L
      val name = nameGenerator.next[ParticipantAnnotationType]
      var description: Option[String] = Some(null)
      val valueType = AnnotationValueType.Select
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = true

      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list must (have length 1 and contain("NonEmptyDescription")),
        user => fail
      )

      description = Some("")
      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list must (have length 1 and contain("NonEmptyDescription")),
        user => fail
      )
    }

    "not be created with an negative max value count" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -1L
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Select
      val maxValueCount = Some(-1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = true

      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => {
          err.list must not be ('empty)
          err.list must contain ("MaxValueCountError")
        },
          pat => fail
      )
    }

    "not be created with an invalid options" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -1L
      val name = nameGenerator.next[ParticipantAnnotationType]
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Select
      val maxValueCount = Some(1)
      var options = Some(Seq(""))
      val required = true

      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list must (have length 1 and contain("OptionRequired")),
        user => fail
      )

      options = Some(Seq("duplicate", "duplicate"))
      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => err.list must (have length 1 and contain("DuplicateOptionsError")),
          user => fail
      )
    }

    "have more than one validation fail" in {
      val studyId = StudyId(nameGenerator.next[ParticipantAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType])
      val version = -2L
      val name = ""
      val description = some(nameGenerator.next[ParticipantAnnotationType])
      val valueType = AnnotationValueType.Select
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))
      val required = true

      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required).fold(
        err => {
          err.list must have length 2
          err.list.head mustBe ("InvalidVersion")
          err.list.tail.head mustBe ("NameRequired")
        },
        user => fail
      )
    }

  }

}
