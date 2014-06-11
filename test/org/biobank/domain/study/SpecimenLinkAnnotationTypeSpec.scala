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
      val options = Some(Map("1" -> "a"))

      val annotType = SpecimenLinkAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options) | fail
      annotType shouldBe a[SpecimenLinkAnnotationType]

      annotType should have (
        'studyId (studyId),
        'id (id),
        'version (0L),
        'name (name),
        'description (description),
        'valueType  (valueType),
        'maxValueCount  (maxValueCount),
        'options (options)
      )

      (annotType.addedDate to DateTime.now).millis should be < 100L
      annotType.lastUpdateDate should be (None)
    }

    "be updated" in {
      val annotType = factory.createSpecimenLinkAnnotationType

      val name = nameGenerator.next[SpecimenLinkAnnotationType]
      val description = some(nameGenerator.next[SpecimenLinkAnnotationType])
      val valueType = AnnotationValueType.Number
      val maxValueCount = Some(annotType.maxValueCount.getOrElse(0) + 100)
      val options = Some(Map(nameGenerator.next[String] -> nameGenerator.next[String]))

      //      log.info(s"$annotType")

      val annotType2 = annotType.update(
        annotType.versionOption, org.joda.time.DateTime.now, name, description, valueType,
        maxValueCount, options) | fail
      annotType2 shouldBe a[SpecimenLinkAnnotationType]

      annotType2 should have (
        'studyId (annotType.studyId),
        'id (annotType.id),
        'version (annotType.version + 1),
        'name (name),
        'description (description),
        'valueType  (valueType),
        'maxValueCount  (maxValueCount),
        'options (options)
      )

      annotType2.addedDate should be (annotType.addedDate)
      val updateDate = annotType2.lastUpdateDate | fail
        (updateDate to DateTime.now).millis should be < 100L
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
      val options = Some(Map("1" -> "a"))

      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options).fold(
        err => err.list should (have length 1 and contain("id is null or empty")),
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
      val options = Some(Map("1" -> "a"))

      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options).fold(
        err => err.list should (have length 1 and contain("id is null or empty")),
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
      val options = Some(Map("1" -> "a"))

      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options).fold(
        err => err.list should (have length 1 and contain("invalid version value: -2")),
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
      val options = Some(Map("1" -> "a"))

      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options).fold(
        err => err.list should (have length 1 and contain("name is null or empty")),
          user => fail
      )

      name = ""
      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options).fold(
        err => err.list should (have length 1 and contain("name is null or empty")),
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
      val options = Some(Map("1" -> "a"))

      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options).fold(
        err => err.list should (have length 1 and contain("description is null or empty")),
          user => fail
      )

      description = Some("")
      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options).fold(
        err => err.list should (have length 1 and contain("description is null or empty")),
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
      val options = Some(Map("1" -> "a"))

      SpecimenLinkAnnotationType.create(studyId, id, version, org.joda.time.DateTime.now, name,
        description, valueType, maxValueCount, options).fold(
        err => err.list should (have length 1 and contain("max value count is not a positive number")),
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
      var options = Some(Map("" -> "a"))

      SpecimenLinkAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType, maxValueCount,
        options).fold(
        err => err.list should (have length 1 and contain("option key is null or empty")),
          user => fail
      )

      options = Some(Map("1" -> ""))
      SpecimenLinkAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType, maxValueCount,
        options).fold(
        err => err.list should (have length 1 and contain("option value is null or empty")),
          user => fail
      )

      options = Some(Map("1" -> null))
      SpecimenLinkAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType, maxValueCount,
        options).fold(
        err => err.list should (have length 1 and contain("option value is null or empty")),
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
      val options = Some(Map("1" -> "a"))

      SpecimenLinkAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType, maxValueCount,
        options).fold(
        err => {
          err.list should have length 2
          err.list.head should be ("invalid version value: -2")
          err.list.tail.head should be ("name is null or empty")
        },
          user => fail
      )
    }
  }

}
