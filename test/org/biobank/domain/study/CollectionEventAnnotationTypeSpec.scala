package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.domain.AnnotationTypeId
import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnnotationValueType
import com.github.nscala_time.time.Imports._

import org.scalatest.OptionValues._
import scalaz._
import scalaz.Scalaz._


class CollectionEventAnnotationTypeSpec extends DomainSpec {

  val nameGenerator = new NameGenerator(this.getClass)

  "A collection event annotation type" can {

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
      annotType shouldBe a[CollectionEventAnnotationType]

      annotType should have (
        'studyId (studyId),
        'id (id),
        'version (0L),
        'name (name),
        'description (description),
        'valueType (valueType),
        'maxValueCount (maxValueCount),
        'options (options)
      )

      (annotType.timeAdded to DateTime.now).millis should be < 100L
      annotType.timeModified should be (None)
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
      annotType2 shouldBe a[CollectionEventAnnotationType]

      annotType2 should have (
        'studyId (annotType.studyId),
        'id (annotType.id),
        'version (annotType.version + 1),
        'name (name),
        'description (description),
        'valueType (valueType),
        'maxValueCount (maxValueCount),
        'options (options)
      )

      annotType2.timeAdded should be (annotType.timeAdded)
      annotType2.timeModified should be (None)
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
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("StudyIdRequired"))
      }
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
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("IdRequired"))
      }
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
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("InvalidVersion"))
      }
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
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("NameRequired"))
      }

      name = ""
      val validation2 = CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description,
        valueType, maxValueCount, options)
      validation2 should be ('failure)
      validation2.swap.map { err =>
          err.list should (have length 1 and contain("NameRequired"))
      }
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
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("NonEmptyDescription"))
      }

      description = Some("")
      val validation2 = CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description,
        valueType, maxValueCount, options)
      validation2 should be ('failure)
      validation2.swap.map { err =>
          err.list should (have length 1 and contain("NonEmptyDescription"))
      }
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
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("MaxValueCountError"))
      }
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
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("OptionRequired"))
      }

      options = Some(Seq("duplicate", "duplicate"))
      val validation2 = CollectionEventAnnotationType.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, valueType, maxValueCount, options)
      validation2 should be ('failure)
      validation2.swap.map { err =>
          err.list should (have length 1 and contain("DuplicateOptionsError"))
      }
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
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should have length 2
          err.list.head should be ("InvalidVersion")
          err.list.tail.head should be ("NameRequired")
      }
    }

  }
}
