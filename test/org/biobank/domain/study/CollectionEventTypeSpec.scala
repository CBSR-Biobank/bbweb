package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.infrastructure._
import org.biobank.fixture.NameGenerator

import org.slf4j.LoggerFactory
import scalaz._
import scalaz.Scalaz._

class CollectionEventTypeSpec extends DomainSpec {

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
      val specimenGroupData = List(CollectionEventTypeSpecimenGroupData("x", 1, Option(1)))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData)

      validation should be ('success)
      validation map { cet =>
	cet shouldBe a[CollectionEventType]

	cet should have (
	'studyId (studyId),
	'id (id),
	'version (0L),
	'name (name),
	'description (description),
	'recurring (recurring)
	)

	cet.specimenGroupData should have length 1
	cet.annotationTypeData should have length 1
      }
    }
  }

  "A collection event type" should {

    "not be created with an empty study id" in {
      val studyId = StudyId("")
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroupData("x", 1, Option(1)))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData)
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("study id is null or empty"))
      }
    }

    "not be created with an empty id" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId("")
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroupData("x", 1, Option(1)))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData)
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("collection event type id is null or empty"))
      }
    }

    "not be created with an invalid version" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -2L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroupData("x", 1, Option(1)))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData)
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("invalid version value: -2"))
      }
    }

    "not be created with an null or empty name" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      var name: String = null
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroupData("x", 1, Option(1)))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData)
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("name is null or empty"))
      }

      name = ""
      val validation2 = CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData)
      validation2 should be ('failure)
      validation2.swap.map { err =>
          err.list should (have length 1 and contain("name is null or empty"))
      }
    }

    "not be created with an empty description option" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      var description: Option[String] = Some(null)
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroupData("x", 1, Option(1)))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData)
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("description is null or empty"))
      }

      description = Some("")
      val validation2 = CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData)
      validation2 should be ('failure)
      validation2.swap.map { err =>
          err.list should (have length 1 and contain("description is null or empty"))
      }
    }

    "not be created with an empty specimen group id" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroupData("", 1, Option(1)))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData)
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("specimen group id is null or empty"))
      }
    }

    "not be created with an negative specimen group max count" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroupData("x", -1, Option(1)))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData)
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("max count is not a positive number"))
      }
    }

    "not be created with an negative specimen group amount" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroupData("x", 1, Option(-1)))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("x", false))

      val validation = CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData)
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("amount not is a positive number"))
      }
    }

    "not be created with an invalid annotation type id" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroupData("x", 1, Option(1)))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("", false))

      val validation = CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData)
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should (have length 1 and contain("annotation type id is null or empty"))
      }
    }

    "have more than one validation fail" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -2L
      val name = ""
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroupData("x", 1, Option(1)))
      val annotationTypeData = List(CollectionEventTypeAnnotationTypeData("1", false))

      val validation = CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData)
      validation should be ('failure)
      validation.swap.map { err =>
          err.list should have length 2
	  err.list.head should be ("invalid version value: -2")
	  err.list.tail.head should be ("name is null or empty")
      }
    }

  }

}
