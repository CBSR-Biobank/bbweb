package org.biobank.controllers

import org.biobank.domain.study.StudyId
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service.json.JsonHelper._
import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.Study._
import play.api.test.Helpers._
import play.api.test.WithApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory

/**
  * Tests the REST API for [[Study]].
  */
class StudyControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  "Study REST API" when {

    "GET /admin/studies" should {
      "list none" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/admin/studies")
        val jsonList = json.as[List[JsObject]]
        jsonList should have size 0
      }

      "list a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val json = makeRequest(GET, "/admin/studies")
        val jsonList = json.as[List[JsObject]]
        jsonList should have length 1
        compareObj(jsonList(0), study)
      }

      "list multiple studies" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val studies = List(factory.createDisabledStudy, factory.createDisabledStudy)
        appRepositories.studyRepository.removeAll
        studies.map(study => appRepositories.studyRepository.put(study))

        val json = makeRequest(GET, "/admin/studies")
        val jsonList = json.as[List[JsObject]]
        jsonList should have size studies.size

        (jsonList zip studies).map { item => compareObj(item._1, item._2) }
      }
    }

    "POST /admin/studies" should {
      "add a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories
        val study = factory.createDisabledStudy
        val cmdJson = Json.obj(
          "type" -> "AddStudyCmd",
          "name" -> study.name,
          "description" -> study.description)
        val json = makeRequest(POST, "/admin/studies", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "event" \ "id").as[String]
        val validation = appRepositories.studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.name should be ((json \ "data" \ "event" \ "name").as[String])
        }
      }
    }

    "PUT /admin/studies/:id" should {
      "update a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val cmdJson = Json.obj(
          "type"            -> "UpdateStudyCmd",
          "id"              -> study.id.id,
          "expectedVersion" -> Some(study.version),
          "name"            -> study.name,
          "description"     -> study.description)
        val json = makeRequest(PUT, s"/admin/studies/${study.id.id}", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "event" \ "id").as[String]
        val validation = appRepositories.studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.name should be ((json \ "data" \ "event" \ "name").as[String])
          repoStudy.version should be ((json \ "data" \ "event" \ "version").as[Long])
        }
      }
    }

    "GET /admin/studies/:id" should {
      "read a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy.enable(Some(0), org.joda.time.DateTime.now, 1, 1) | fail
        appRepositories.studyRepository.put(study)
        val json = makeRequest(GET, s"/admin/studies/${study.id.id}")
        compareObj(json, study)
      }
    }

    "POST /admin/studies/enable" should {
      "enable a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)
        appRepositories.specimenGroupRepository.put(factory.createSpecimenGroup)
        appRepositories.collectionEventTypeRepository.put(factory.createCollectionEventType)

        val cmdJson = Json.obj(
          "type" -> "EnableStudyCmd",
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, "/admin/studies/enable", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "event" \ "id").as[String]
        val validation = appRepositories.studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.version should be ((json \ "data" \ "event" \ "version").as[Long])
        }
      }
    }

    "POST /admin/studies/enable" should {
      "not enable a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val cmdJson = Json.obj(
          "type" -> "EnableStudyCmd",
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, "/admin/studies/enable", BAD_REQUEST, cmdJson)

        (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("no specimen groups")
      }
    }

    "POST /admin/studies/disable" should {
      "disable a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy.enable(Some(0), org.joda.time.DateTime.now, 1, 1) | fail
        appRepositories.studyRepository.put(study)

        val cmdJson = Json.obj(
          "type" -> "DisableStudyCmd",
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, "/admin/studies/disable", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "event" \ "id").as[String]
        val validation = appRepositories.studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.version should be ((json \ "data" \ "event" \ "version").as[Long])
        }
      }
    }

    "POST /admin/studies/retire" should {
      "retire a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val cmdJson = Json.obj(
          "type" -> "RetireStudyCmd",
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, "/admin/studies/retire", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "event" \ "id").as[String]
        val validation = appRepositories.studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.version should be ((json \ "data" \ "event" \ "version").as[Long])
        }
      }
    }

    "POST /admin/studies/unretire" should {
      "unretire a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy.retire(Some(0), org.joda.time.DateTime.now) | fail
        appRepositories.studyRepository.put(study)

        val cmdJson = Json.obj(
          "type" -> "UnretireStudyCmd",
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, "/admin/studies/unretire", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "event" \ "id").as[String]
        val validation = appRepositories.studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.version should be ((json \ "data" \ "event" \ "version").as[Long])
        }
      }
    }

    "GET /admin/studies/valuetypes" should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/admin/studies/valuetypes")
        val values = json.as[List[String]]
        values.size should be > 0
      }
    }


    "GET /admin/studies/anatomicalsrctypes" should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/admin/studies/anatomicalsrctypes")
        val values = json.as[List[String]]
        values.size should be > 0
      }
    }

    "GET /admin/studies/specimentypes" should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/admin/studies/specimentypes")
        val values = json.as[List[String]]
        values.size should be > 0
      }
    }

    "GET /admin/studies/preservtypes" should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/admin/studies/preservtypes")
        val values = json.as[List[String]]
        values.size should be > 0
      }
    }

    "GET /admin/studies/preservtemptypes " should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/admin/studies/preservtemptypes")
        val values = json.as[List[String]]
        values.size should be > 0
      }
    }

  }

}
