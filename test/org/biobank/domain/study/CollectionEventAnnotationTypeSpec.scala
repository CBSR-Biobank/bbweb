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

  val nameGenerator = new NameGenerator(this.getClass)

  "A collection event annotation type" must {

    "be created" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventAnnotationType]
      val description = some(nameGenerator.next[CollectionEventAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq("a"))

      val annotType = CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType, maxValueCount, options) | fail
      annotType mustBe a[CollectionEventAnnotationType]

      annotType must have (
        'studyId (studyId),
        'id (id),
        'version (0L),
        'name (name),
        'description (description),
        'valueType (valueType),
        'maxValueCount (maxValueCount),
        'options (options)
      )

      (annotType.timeAdded to DateTime.now).millis must be < 100L
      annotType.timeModified mustBe (None)
    }

    "be updated" in {
      val annotType = factory.createCollectionEventAnnotationType

      val name = nameGenerator.next[CollectionEventAnnotationType]
      val description = some(nameGenerator.next[CollectionEventAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(annotType.maxValueCount.getOrElse(0) + 100)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      val annotType2 = annotType.update(name, description, valueType, maxValueCount, options) | fail
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

  "A collection event annotation type" can {

    "not be created with an empty study id" in {
      val studyId = StudyId("")
      val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventAnnotationType]
      val description = some(nameGenerator.next[CollectionEventAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      val validation = CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options)
      validation mustFail "StudyIdRequired"
    }

    "not be created with an empty id" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventAnnotationType])
      val id = AnnotationTypeId("")
      val version = -1L
      val name = nameGenerator.next[CollectionEventAnnotationType]
      val description = some(nameGenerator.next[CollectionEventAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      val validation = CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options)
      validation mustFail "IdRequired"
    }

    "not be created with an invalid version" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
      val version = -2L
      val name = nameGenerator.next[CollectionEventAnnotationType]
      val description = some(nameGenerator.next[CollectionEventAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      val validation = CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options)
      validation mustFail "InvalidVersion"
    }

    "not be created with an null or empty name" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
      val version = -1L
      var name: String = null
      val description = some(nameGenerator.next[CollectionEventAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      val validation = CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options)
      validation mustFail "NameRequired"

      name = ""
      val validation2 = CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description,
        valueType, maxValueCount, options)
      validation2 mustFail "NameRequired"
    }

    "not be created with an empty description option" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventAnnotationType]
      var description: Option[String] = Some(null)
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      val validation = CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options)
      validation mustFail "NonEmptyDescription"

      description = Some("")
      val validation2 = CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description,
        valueType, maxValueCount, options)
      validation2 mustFail "NonEmptyDescription"
    }

    "not be created with an negative max value count" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventAnnotationType]
      val description = some(nameGenerator.next[CollectionEventAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(-1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      val validation = CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options)
      validation mustFail "MaxValueCountError"
    }

    "not be created with an invalid options" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventAnnotationType]
      val description = some(nameGenerator.next[CollectionEventAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      var options = Some(Seq(""))

      val validation = CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options)
      validation mustFail "OptionRequired"

      options = Some(Seq("duplicate", "duplicate"))
      val validation2 = CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType, maxValueCount, options)
      validation2 mustFail "DuplicateOptionsError"
    }

    "have more than one validation fail" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType])
      val version = -2L
      val name = ""
      val description = some(nameGenerator.next[CollectionEventAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      val validation = CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType, maxValueCount, options)
      validation.mustFail("InvalidVersion", "NameRequired")
    }

  }
}
