package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.domain.AnnotationTypeId
import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnnotationValueType

import com.github.nscala_time.time.Imports._
import org.slf4j.LoggerFactory
import scalaz._
import scalaz.Scalaz._

class SpecimenLinkAnnotationTypeSpec extends DomainSpec {
  import org.biobank.TestUtils._
  import StudyAnnotationTypeSpecUtil._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A specimen link annotation type" can {

    "be created for each value type" in {
      AnnotationValueType.values.foreach { vt =>
        val (studyId, id, version, name, description, valueType, maxValueCount, options) =
          AnnotationValueTypeToTuple(vt)

        val v = SpecimenLinkAnnotationType.create(
          studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
          maxValueCount, options)
        v mustSucceed { annotType =>
          annotType mustBe a[SpecimenLinkAnnotationType]

          annotType must have (
            'studyId (studyId),
            'id (id),
            'version (0L),
            'name (name),
            'description (description),
            'valueType  (valueType),
            'maxValueCount  (maxValueCount),
            'options (options)
          )

          (annotType.timeAdded to DateTime.now).millis must be < 500L
          annotType.timeModified mustBe (None)
        }
      }
    }

    "be updated for each value type" in {
      val annotType = factory.createSpecimenLinkAnnotationType
      AnnotationValueType.values.foreach { vt =>
        val (studyId, id, version, name, description, valueType, maxValueCount, options) =
          AnnotationValueTypeToTuple(vt)

        val v = annotType.update(name, description, valueType, maxValueCount, options)
        v mustSucceed { annotType2 =>
          annotType2 mustBe a[SpecimenLinkAnnotationType]

          annotType2 must have (
            'studyId (annotType.studyId),
            'id (annotType.id),
            'version (annotType.version + 1),
            'name (name),
            'description (description),
            'valueType  (valueType),
            'maxValueCount  (maxValueCount),
            'options (options)
          )

          annotType2.timeAdded mustBe (annotType.timeAdded)
          annotType2.timeModified must not be (None)
        }
      }
    }
  }

  "A specimen link annotation type" can {

    "not be created with an empty study id" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options) =
        selectAnnotationTypeTuple
      val badStudyId = StudyId("")

      SpecimenLinkAnnotationType.create(badStudyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options)
        .mustFail(1, "StudyIdRequired")
    }

    "not be created with an empty id" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options) =
        selectAnnotationTypeTuple
      val badAnnotationTypeId = AnnotationTypeId("")

      SpecimenLinkAnnotationType.create(studyId, badAnnotationTypeId, version, org.joda.time.DateTime.now,
        name, description, valueType, maxValueCount, options)
        .mustFail(1, "IdRequired")
    }

    "not be created with an invalid version" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options) =
        selectAnnotationTypeTuple
      val invalidVersion = -2L

      SpecimenLinkAnnotationType.create(studyId, id, invalidVersion, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options)
        .mustFail(1, "InvalidVersion")
  }

    "not be created with an null or empty name" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options) =
        selectAnnotationTypeTuple
      val invalidNames: List[String] = List(null, "")

      invalidNames.foreach(invalidName =>
        SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now,
          invalidName, description, valueType, maxValueCount, options)
          .mustFail(1, "NameRequired")
      )
    }

    "not be created with an empty description option" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options) =
        selectAnnotationTypeTuple
      var invalidDescriptions: List[Option[String]] = List(Some(null), Some(""))

      invalidDescriptions.foreach(invalidDescription =>
        SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now,
          name, invalidDescription, valueType, maxValueCount, options)
          .mustFail(1, "NonEmptyDescription")
      )
    }

    "not be created with an negative max value count" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options) =
        selectAnnotationTypeTuple
      val invalidMaxValueCount = Some(-1)

      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, invalidMaxValueCount, options)
        .mustFail(1, "MaxValueCountError")
    }


    "not be created with an invalid options" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options) =
        selectAnnotationTypeTuple
      var invalidOptions = Seq("")

      SpecimenLinkAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType, maxValueCount,
        invalidOptions)
        .mustFail(1, "OptionRequired")

      invalidOptions = Seq("duplicate", "duplicate")
      SpecimenLinkAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType, maxValueCount,
        invalidOptions)
        .mustFail(1, "DuplicateOptionsError")
    }

    "have more than one validation fail" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options) =
        selectAnnotationTypeTuple
      val invalidVersion = -2L
      val invalidName = ""

      SpecimenLinkAnnotationType.create(
        studyId, id, invalidVersion, org.joda.time.DateTime.now, invalidName, description, valueType,
        maxValueCount, options)
        .mustFail(2, "InvalidVersion", "NameRequired")
    }
  }

}
