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

  val log = LoggerFactory.getLogger(this.getClass)

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
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      val annotType = SpecimenLinkAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options) | fail
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

      (annotType.timeAdded to DateTime.now).millis must be < 100L
      annotType.timeModified mustBe (None)
    }

    "be updated" in {
      val annotType = factory.createSpecimenLinkAnnotationType

      val name = nameGenerator.next[SpecimenLinkAnnotationType]
      val description = some(nameGenerator.next[SpecimenLinkAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(annotType.maxValueCount.getOrElse(0) + 100)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      //      log.info(s"$annotType")

      val annotType2 = annotType.update(name, description, valueType, maxValueCount, options) | fail
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
      annotType2.timeModified mustBe (None)
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
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options).fold(
        err => err.list must (have length 1 and contain("IdRequired")),
          user => fail
      )
    }

    "not be created with an empty id" in {
      val studyId = StudyId(nameGenerator.next[SpecimenLinkAnnotationType])
      val id = AnnotationTypeId("")
      val version = -1L
      val name = nameGenerator.next[SpecimenLinkAnnotationType]
      val description = some(nameGenerator.next[SpecimenLinkAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options).fold(
        err => err.list must (have length 1 and contain("IdRequired")),
          user => fail
      )
    }

    "not be created with an invalid version" in {
      val studyId = StudyId(nameGenerator.next[SpecimenLinkAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType])
      val version = -2L
      val name = nameGenerator.next[SpecimenLinkAnnotationType]
      val description = some(nameGenerator.next[SpecimenLinkAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options).fold(
        err => err.list must (have length 1 and contain("InvalidVersion")),
          user => fail
      )
    }

    "not be created with an null or empty name" in {
      val studyId = StudyId(nameGenerator.next[SpecimenLinkAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType])
      val version = -1L
      var name: String = null
      val description = some(nameGenerator.next[SpecimenLinkAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options).fold(
        err => err.list must (have length 1 and contain("NameRequired")),
          user => fail
      )

      name = ""
      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options).fold(
        err => err.list must (have length 1 and contain("NameRequired")),
          user => fail
      )
    }

    "not be created with an empty description option" in {
      val studyId = StudyId(nameGenerator.next[SpecimenLinkAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType])
      val version = -1L
      val name = nameGenerator.next[SpecimenLinkAnnotationType]
      var description: Option[String] = Some(null)
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options).fold(
        err => err.list must (have length 1 and contain("NonEmptyDescription")),
          user => fail
      )

      description = Some("")
      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options).fold(
        err => err.list must (have length 1 and contain("NonEmptyDescription")),
          user => fail
      )
    }

    "not be created with an negative max value count" in {
      val studyId = StudyId(nameGenerator.next[SpecimenLinkAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType])
      val version = -1L
      val name = nameGenerator.next[SpecimenLinkAnnotationType]
      val description = some(nameGenerator.next[SpecimenLinkAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(-1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options).fold(
        err => err.list must (have length 1 and contain("MaxValueCountError")),
          user => fail
      )
    }


    "not be created with an invalid options" in {
      val studyId = StudyId(nameGenerator.next[SpecimenLinkAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType])
      val version = -1L
      val name = nameGenerator.next[SpecimenLinkAnnotationType]
      val description = some(nameGenerator.next[SpecimenLinkAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      var options = Some(Seq(""))

      SpecimenLinkAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType, maxValueCount,
        options).fold(
        err => err.list must (have length 1 and contain("OptionRequired")),
          user => fail
      )

      options = Some(Seq("duplicate", "duplicate"))
      SpecimenLinkAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType, maxValueCount,
        options).fold(
        err => err.list must (have length 1 and contain("DuplicateOptionsError")),
          user => fail
      )
    }

    "have more than one validation fail" in {
      val studyId = StudyId(nameGenerator.next[SpecimenLinkAnnotationType])
      val id = AnnotationTypeId(nameGenerator.next[SpecimenLinkAnnotationType])
      val version = -2L
      val name = ""
      val description = some(nameGenerator.next[SpecimenLinkAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(1)
      val options = Some(Seq(
        nameGenerator.next[String],
        nameGenerator.next[String]))

      SpecimenLinkAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType, maxValueCount,
        options).fold(
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
