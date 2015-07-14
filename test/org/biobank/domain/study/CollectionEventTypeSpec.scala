package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.infrastructure._
import org.biobank.fixture.NameGenerator

import com.github.nscala_time.time.Imports._
import org.slf4j.LoggerFactory
import scalaz.Scalaz._

class CollectionEventTypeSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A collection event type" can {

    "be created" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(
        CollectionEventTypeSpecimenGroupData("x", 1, Option(BigDecimal(1.0))))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(
        studyId, id, version, name, description, recurring,
        specimenGroupData, annotationTypeData)

      validation mustSucceed { cet =>
        cet mustBe a[CollectionEventType]

        cet must have (
          'studyId (studyId),
          'id (id),
          'version (0L),
          'name (name),
          'description (description),
          'recurring (recurring)
        )

        cet.specimenGroupData must have length 1
        cet.annotationTypeData must have length 1

        (cet.timeAdded to DateTime.now).millis must be < 200L
        cet.timeModified mustBe (None)
      }
    }

    "be updated" in {
      val cet = factory.createCollectionEventType

      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = !cet.recurring
      val specimenGroupData = List(
        CollectionEventTypeSpecimenGroupData("x", 1, Option(BigDecimal(1.0))))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val v = cet.update(name, description, recurring, specimenGroupData, annotationTypeData)
      v mustSucceed { updatedCet =>
        updatedCet mustBe a[CollectionEventType]

        updatedCet must have (
          'studyId (cet.studyId),
          'id (cet.id),
          'version (cet.version + 1),
          'name (name),
          'description (description),
          'recurring (recurring)
        )

        updatedCet.specimenGroupData must have length 1
        updatedCet.annotationTypeData must have length 1

        (cet.timeAdded to updatedCet.timeAdded).millis must be < 100L
        updatedCet.timeModified must not be (None)
      }
    }

  }

  "A collection event type" must {

    "not be created with an empty study id" in {
      val studyId = StudyId("")
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(
        CollectionEventTypeSpecimenGroupData("x", 1, Option(BigDecimal(1.0))))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(
        studyId, id, version, name, description, recurring,
        specimenGroupData, annotationTypeData)
      validation mustFail "IdRequired"
    }

    "not be created with an empty id" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId("")
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(
        CollectionEventTypeSpecimenGroupData("x", 1, Option(BigDecimal(1.0))))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(
        studyId, id, version, name, description, recurring,
        specimenGroupData, annotationTypeData)
      validation mustFail "IdRequired"
    }

    "not be created with an invalid version" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -2L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(
        CollectionEventTypeSpecimenGroupData("x", 1, Option(BigDecimal(1.0))))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(
        studyId, id, version, name, description, recurring,
        specimenGroupData, annotationTypeData)
      validation mustFail "InvalidVersion"
    }

    "not be created with an null or empty name" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      var name: String = null
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(
        CollectionEventTypeSpecimenGroupData("x", 1, Option(BigDecimal(1.0))))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(
        studyId, id, version, name, description, recurring,
        specimenGroupData, annotationTypeData)
      validation mustFail "NameRequired"

      name = ""
      val validation2 = CollectionEventType.create(
        studyId, id, version, name, description, recurring,
        specimenGroupData, annotationTypeData)
      validation2 mustFail "NameRequired"
    }

    "not be created with an empty description option" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      var description: Option[String] = Some(null)
      val recurring = false
      val specimenGroupData = List(
        CollectionEventTypeSpecimenGroupData("x", 1, Option(BigDecimal(1.0))))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(
        studyId, id, version, name, description, recurring,
        specimenGroupData, annotationTypeData)
      validation mustFail "InvalidDescription"

      description = Some("")
      val validation2 = CollectionEventType.create(
        studyId, id, version, name, description, recurring,
        specimenGroupData, annotationTypeData)
      validation2 mustFail "InvalidDescription"
    }

    "not be created with an empty specimen group id" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(
        CollectionEventTypeSpecimenGroupData("", 1, Option(BigDecimal(1.0))))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(
        studyId, id, version, name, description, recurring,
        specimenGroupData, annotationTypeData)
      validation mustFail "IdRequired"
    }

    "not be created with an negative specimen group max count" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(
        CollectionEventTypeSpecimenGroupData("x", -1, Option(BigDecimal(1.0))))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(
        studyId, id, version, name, description, recurring,
        specimenGroupData, annotationTypeData)
      validation mustFail "MaxCountInvalid"
    }

    "not be created with an negative specimen group amount" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(
        CollectionEventTypeSpecimenGroupData("x", 1, Option(BigDecimal(-1.0))))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(
        studyId, id, version, name, description, recurring,
        specimenGroupData, annotationTypeData)
      validation mustFail "AmountInvalid"
    }

    "not be created with an invalid annotation type id" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(
        CollectionEventTypeSpecimenGroupData("x", 1, Option(BigDecimal(1.0))))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("", false))

      val validation = CollectionEventType.create(
        studyId, id, version, name, description, recurring,
        specimenGroupData, annotationTypeData)
      validation mustFail "IdRequired"
    }

    "have more than one validation fail" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -2L
      val name = ""
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(
        CollectionEventTypeSpecimenGroupData("x", 1, Option(BigDecimal(1.0))))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("1", false))

      val validation = CollectionEventType.create(
        studyId, id, version, name, description, recurring,
        specimenGroupData, annotationTypeData)
      validation.mustFail("InvalidVersion", "NameRequired")
    }

  }

}
