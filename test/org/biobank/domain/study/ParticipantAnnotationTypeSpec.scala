package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.domain.AnnotationTypeId
import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnnotationValueType

import com.github.nscala_time.time.Imports._
import org.slf4j.LoggerFactory
import scalaz.Scalaz._


class ParticipantAnnotationTypeSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def textAnnotationTypeTuple = {
    val (studyId, id, version, name, description, valueType, maxValueCount, options) =
      StudyAnnotationTypeSpecUtil.textAnnotationTypeTuple
    val required = false
    (studyId, id, version, name, description, valueType, maxValueCount, options, required)
  }

  def numberAnnotationTypeTuple = {
    val (studyId, id, version, name, description, valueType, maxValueCount, options) =
      StudyAnnotationTypeSpecUtil.numberAnnotationTypeTuple
    val required = false
    (studyId, id, version, name, description, valueType, maxValueCount, options, required)
  }

  def dateTimeAnnotationTypeTuple = {
    val (studyId, id, version, name, description, valueType, maxValueCount, options) =
      StudyAnnotationTypeSpecUtil.dateTimeAnnotationTypeTuple
    val required = false
    (studyId, id, version, name, description, valueType, maxValueCount, options, required)
  }

  def selectAnnotationTypeTuple = {
    val (studyId, id, version, name, description, valueType, maxValueCount, options) =
      StudyAnnotationTypeSpecUtil.selectAnnotationTypeTuple
    val required = false
    (studyId, id, version, name, description, valueType, maxValueCount, options, required)
  }

  type AnnotTypeTuple = Tuple9[
    StudyId, AnnotationTypeId, Long, String, Some[String], AnnotationValueType.Value,
    Option[Int], Seq[String], Boolean]

  val AnnotationValueTypeToTuple
      : Map[AnnotationValueType.AnnotationValueType, AnnotTypeTuple] = Map(
    AnnotationValueType.Text     -> textAnnotationTypeTuple,
    AnnotationValueType.Number   -> numberAnnotationTypeTuple,
    AnnotationValueType.DateTime -> dateTimeAnnotationTypeTuple,
    AnnotationValueType.Select   -> selectAnnotationTypeTuple
  )

  "A participant annotation type" can {

    "be created for each value type" in {
      AnnotationValueType.values.foreach { vt =>
        val (studyId, id, version, name, description, valueType, maxValueCount, options, required) =
          AnnotationValueTypeToTuple(vt)

        val v = ParticipantAnnotationType.create(
          studyId, id, version, DateTime.now, name, description, vt, maxValueCount, options, required)
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

          (annotType.timeAdded to DateTime.now).millis must be < 500L
          annotType.timeModified mustBe (None)
        }
      }
    }

    "be updated for each value type" in {
      val annotType = factory.createParticipantAnnotationType
      AnnotationValueType.values.foreach { vt =>
        val (studyId, id, version, name, description, valueType, maxValueCount, options, required) =
          AnnotationValueTypeToTuple(vt)

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
          at.timeModified must not be (None)
        }
      }
    }
  }

  "A participant annotation type" can {

    "not be created with an empty study id" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options, required) =
        textAnnotationTypeTuple
      val invalidStudyId = StudyId("")

      ParticipantAnnotationType.create(
        invalidStudyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options, required)
        .mustFail(1, "StudyIdRequired")
    }

    "not be created with an empty id" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options, required) =
        textAnnotationTypeTuple
      val invalidId = AnnotationTypeId("")

      ParticipantAnnotationType.create(studyId, invalidId, version, org.joda.time.DateTime.now,
        name, description, valueType, maxValueCount, options, required)
        .mustFail(1, "IdRequired")
    }

    "not be created with an invalid version" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options, required) =
        textAnnotationTypeTuple
      val invalidVersion = -2L

      ParticipantAnnotationType.create(
        studyId, id, invalidVersion, DateTime.now, name, description, valueType,
        maxValueCount, options, required)
        .mustFail(1, "InvalidVersion")
    }

    "not be created with an null or empty name" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options, required) =
        textAnnotationTypeTuple
      var invalidNames: List[String] = List(null, "")

      invalidNames.foreach(invalidName =>
        ParticipantAnnotationType.create(
        studyId, id, version, DateTime.now, invalidName, description, valueType,
        maxValueCount, options, required)
          .mustFail(1, "NameRequired")
      )
    }

    "not be created with an empty description option" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options, required) =
        textAnnotationTypeTuple
      var invalidDescriptions: List[Option[String]] = List(Some(null), Some(""))

      invalidDescriptions.foreach(invalidDescription =>
        ParticipantAnnotationType.create(
          studyId, id, version, org.joda.time.DateTime.now, name, invalidDescription, valueType,
          maxValueCount, options, required)
          .mustFail(1, "NonEmptyDescription")
      )
    }

    "not be created with an negative max value count" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options, required) =
        textAnnotationTypeTuple
      val invalidMaxValueCount = Some(-1)

      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        invalidMaxValueCount, options, required)
        .mustFail(1, "MaxValueCountError")
    }

    "not be created with an invalid options" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options, required) =
        textAnnotationTypeTuple
      var invalidOptions = Seq("")

      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, invalidOptions, required)
        .mustFail(1, "OptionRequired")

      invalidOptions = Seq("duplicate", "duplicate")
      ParticipantAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, invalidOptions, required)
        .mustFail(1, "DuplicateOptionsError")
    }

    "have more than one validation fail" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options, required) =
        textAnnotationTypeTuple
      val invalidVersion = -2L
      val invalidName = ""

      ParticipantAnnotationType.create(
        studyId, id, invalidVersion, org.joda.time.DateTime.now, invalidName, description,
        valueType, maxValueCount, options, required)
        .mustFail("InvalidVersion", "NameRequired")
    }

  }

}
