package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.domain.AnnotationTypeId
import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnnotationValueType
import com.github.nscala_time.time.Imports._

import scalaz._
import scalaz.Scalaz._


class CollectionEventAnnotationTypeSpec extends DomainSpec {
  import org.biobank.TestUtils._
  import StudyAnnotationTypeSpecUtil._

  val nameGenerator = new NameGenerator(this.getClass)

  "A collection event annotation type" must {

    "be created for each value type" in {
      AnnotationValueType.values.foreach { vt =>
        val (studyId, id, version, name, description, valueType, maxValueCount, options) =
          AnnotationValueTypeToTuple(vt)

        val v = CollectionEventAnnotationType.create(
          studyId, id, version, DateTime.now, name, description, vt, maxValueCount, options)
        v mustSucceed { annotType =>
          annotType mustBe a[CollectionEventAnnotationType]
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

    "be updated for each annotation type" in {
      val annotType = factory.createCollectionEventAnnotationType
      AnnotationValueType.values.foreach { vt =>
        val (studyId, id, version, name, description, valueType, maxValueCount, options) =
          AnnotationValueTypeToTuple(vt)

        val v = annotType.update(name, description, valueType, maxValueCount, options)
        v mustSucceed { annotType2 =>
          annotType2 mustBe a[CollectionEventAnnotationType]

          annotType2 must have (
            'studyId (annotType.studyId),
            'id (annotType.id),
            'version (annotType.version + 1),
            'name (name),
            'description (description),
            'valueType (valueType),
            'maxValueCount (maxValueCount),
          'options (options)
          )

          annotType2.timeAdded mustBe (annotType.timeAdded)
          annotType2.timeModified mustBe (None)
        }
      }
    }

  }

  "A collection event annotation type" can {

    "not be created with an empty study id" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options) =
        numberAnnotationTypeTuple
      val invalidStudyId = StudyId("")

      val validation = CollectionEventAnnotationType.create(
        invalidStudyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options)
      validation mustFail "StudyIdRequired"
    }

    "not be created with an empty id" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options) =
        numberAnnotationTypeTuple
      val invalidId = AnnotationTypeId("")

      val validation = CollectionEventAnnotationType.create(
        studyId, invalidId, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options)
      validation mustFail "IdRequired"
    }

    "not be created with an invalid version" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options) =
        numberAnnotationTypeTuple
      val invalidVersion = -2L

      val validation = CollectionEventAnnotationType.create(
        studyId, id, invalidVersion, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options)
      validation mustFail "InvalidVersion"
    }

    "not be created with an null or empty name" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options) =
        numberAnnotationTypeTuple
      var invalidNames: List[String] = List(null, "")

      invalidNames.foreach(invalidName =>
        CollectionEventAnnotationType.create(
          studyId, id, version, org.joda.time.DateTime.now, invalidName, description, valueType,
          maxValueCount, options)
          .mustFail("NameRequired")
      )
    }

    "not be created with an empty description option" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options) =
        numberAnnotationTypeTuple
      var invalidDescriptions: List[Option[String]] = List(Some(null), Some(""))

      invalidDescriptions.foreach(invalidDescription =>
        CollectionEventAnnotationType.create(
          studyId, id, version, org.joda.time.DateTime.now, name, invalidDescription, valueType,
          maxValueCount, options)
          .mustFail("NonEmptyDescription")
      )
    }

    "not be created with an negative max value count" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options) =
        numberAnnotationTypeTuple
      val invalidMaxValueCount = Some(-1)

      CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        invalidMaxValueCount, options)
        .mustFail(1, "MaxValueCountError")
    }

    "not be created with an invalid options" in {
      val (studyId, id, version, name, description, valueType, maxValueCount, options) =
        numberAnnotationTypeTuple
      val invalidOptions = Seq("")

      CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, invalidOptions)
        .mustFail(1, "OptionRequired")

      CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, Seq("duplicate", "duplicate"))
        .mustFail(1, "DuplicateOptionsError")
    }

    "have more than one validation fail" in {
            val (studyId, id, version, name, description, valueType, maxValueCount, options) =
        numberAnnotationTypeTuple
      val invalidVersion = -2L
      val invalidName = ""

      val validation = CollectionEventAnnotationType.create(
        studyId, id, invalidVersion, org.joda.time.DateTime.now,
        invalidName, description, valueType, maxValueCount, options)
      validation.mustFail("InvalidVersion", "NameRequired")
    }

  }
}
