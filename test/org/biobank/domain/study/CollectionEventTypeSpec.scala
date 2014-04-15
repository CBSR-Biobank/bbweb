package org.biobank.domain.study

import org.biobank.infrastructure._
import org.biobank.fixture.NameGenerator

import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.slf4j.LoggerFactory
import scalaz._
import scalaz.Scalaz._

class CollectionEventTypeSpec extends WordSpecLike with Matchers {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass.getName)

  "A collection event type" can {

    "be created" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroup("x", 1, 1))
      val annotationTypeData = List(CollectionEventTypeAnnotationType("x", false))

      val v = CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData)
      val cet = v.getOrElse(fail)
      cet shouldBe a[CollectionEventType]

      cet.studyId should be(studyId)
      cet.id should be(id)
      cet.version should be(0L)
      cet.name should be(name)
      cet.description should be(description)
      cet.recurring should be(recurring)
      cet.specimenGroupData should have length 1
      cet.annotationTypeData should have length 1
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
      val specimenGroupData = List(CollectionEventTypeSpecimenGroup("x", 1, 1))
      val annotationTypeData = List(CollectionEventTypeAnnotationType("x", false))

      CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("study id is null or empty")
      }
    }

    "not be created with an empty id" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId("")
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroup("x", 1, 1))
      val annotationTypeData = List(CollectionEventTypeAnnotationType("x", false))

      CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("id is null or empty")
      }
    }

    "not be created with an invalid version" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -2L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroup("x", 1, 1))
      val annotationTypeData = List(CollectionEventTypeAnnotationType("x", false))

      CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("invalid version value")
      }
    }

    "not be created with an null or empty name" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      var name: String = null
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroup("x", 1, 1))
      val annotationTypeData = List(CollectionEventTypeAnnotationType("x", false))

      CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("name is null or empty")
      }

      name = ""
      CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("name is null or empty")
      }
    }

    "not be created with an empty description option" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      var description: Option[String] = some(null)
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroup("x", 1, 1))
      val annotationTypeData = List(CollectionEventTypeAnnotationType("x", false))

      CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("description is null or empty")
      }

      description = Some("")
      CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("description is null or empty")
      }
    }

    "not be created with an empty specimen group id" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroup("", 1, 1))
      val annotationTypeData = List(CollectionEventTypeAnnotationType("x", false))

      CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("specimen group id is null or empty")
      }
    }

    "not be created with an negative specimen group max count" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroup("x", -1, 1))
      val annotationTypeData = List(CollectionEventTypeAnnotationType("x", false))

      CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("max count is not a positive number")
      }
    }

    "not be created with an negative specimen group amount" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroup("x", 1, -1))
      val annotationTypeData = List(CollectionEventTypeAnnotationType("x", false))

      CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("amount not is a positive number")
      }
    }

    "not be created with an negative annotation type id" in {
      val studyId = StudyId(nameGenerator.next[CollectionEventType])
      val id = CollectionEventTypeId(nameGenerator.next[CollectionEventType])
      val version = -1L
      val name = nameGenerator.next[CollectionEventType]
      val description = some(nameGenerator.next[CollectionEventType])
      val recurring = false
      val specimenGroupData = List(CollectionEventTypeSpecimenGroup("x", 1, 1))
      val annotationTypeData = List(CollectionEventTypeAnnotationType("", false))

      CollectionEventType.create(studyId, id, version, name, description, recurring,
	specimenGroupData, annotationTypeData) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list.mkString(",") should include("annotation type id is null or empty")
      }
    }


  }

}
