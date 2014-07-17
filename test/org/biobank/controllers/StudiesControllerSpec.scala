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
class StudiesControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  "Study REST API" when {

    "GET /studies" should {
      "list none" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/studies")
        val jsonList = json.as[List[JsObject]]
        jsonList should have size 0
      }

      "list a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val json = makeRequest(GET, "/studies")
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

        val json = makeRequest(GET, "/studies")
        val jsonList = json.as[List[JsObject]]
        jsonList should have size studies.size

        (jsonList zip studies).map { item => compareObj(item._1, item._2) }
      }
    }

    "POST /studies" should {
      "add a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories
        val study = factory.createDisabledStudy
        val cmdJson = Json.obj(
          "name" -> study.name,
          "description" -> study.description)
        val json = makeRequest(POST, "/studies", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "event" \ "id").as[String]
        val validation = appRepositories.studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.name should be ((json \ "data" \ "event" \ "name").as[String])
        }
      }
    }

    "PUT /studies/:id" should {
      "update a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val cmdJson = Json.obj(
          "id"              -> study.id.id,
          "expectedVersion" -> Some(study.version),
          "name"            -> study.name,
          "description"     -> study.description)
        val json = makeRequest(PUT, s"/studies/${study.id.id}", json = cmdJson)

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

    "GET /studies/:id" should {
      "read a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy.enable(Some(0), org.joda.time.DateTime.now, 1, 1) | fail
        appRepositories.studyRepository.put(study)
        val json = makeRequest(GET, s"/studies/${study.id.id}")
        compareObj(json, study)
      }
    }

    "POST /studies/enable" should {
      "enable a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)
        appRepositories.specimenGroupRepository.put(factory.createSpecimenGroup)
        appRepositories.collectionEventTypeRepository.put(factory.createCollectionEventType)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, "/studies/enable", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "event" \ "id").as[String]
        val validation = appRepositories.studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.version should be ((json \ "data" \ "event" \ "version").as[Long])
        }
      }
    }

    "POST /studies/enable" should {
      "not enable a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, "/studies/enable", BAD_REQUEST, cmdJson)

        (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("no specimen groups")
      }
    }

    "POST /studies/disable" should {
      "disable a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy.enable(Some(0), org.joda.time.DateTime.now, 1, 1) | fail
        appRepositories.studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, "/studies/disable", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "event" \ "id").as[String]
        val validation = appRepositories.studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.version should be ((json \ "data" \ "event" \ "version").as[Long])
        }
      }
    }

    "POST /studies/retire" should {
      "retire a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, "/studies/retire", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "event" \ "id").as[String]
        val validation = appRepositories.studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.version should be ((json \ "data" \ "event" \ "version").as[Long])
        }
      }
    }

    "POST /studies/unretire" should {
      "unretire a study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy.retire(Some(0), org.joda.time.DateTime.now) | fail
        appRepositories.studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, "/studies/unretire", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "event" \ "id").as[String]
        val validation = appRepositories.studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.version should be ((json \ "data" \ "event" \ "version").as[Long])
        }
      }
    }

    "GET /studies/valuetypes" should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/studies/valuetypes")
        val values = json.as[List[String]]
        values.size should be > 0
      }
    }


    "GET /studies/anatomicalsrctypes" should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/studies/anatomicalsrctypes")
        val values = json.as[List[String]]
        values.size should be > 0
      }
    }

    "GET /studies/specimentypes" should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/studies/specimentypes")
        val values = json.as[List[String]]
        values.size should be > 0
      }
    }

    "GET /studies/preservtypes" should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/studies/preservtypes")
        val values = json.as[List[String]]
        values.size should be > 0
      }
    }

    "GET /studies/preservtemptypes " should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/studies/preservtemptypes")
        val values = json.as[List[String]]
        values.size should be > 0
      }
    }

    "GET /studies/sgvaluetypes " should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val jsonReq = makeRequest(GET, "/studies/sgvaluetypes")
        val json = jsonReq.as[JsObject]
        (json \ "anatomicalSourceType").as[List[String]].size        should be > 0
        (json \ "preservationType").as[List[String]].size            should be > 0
        (json \ "preservationTemperatureType").as[List[String]].size should be > 0
        (json \ "specimenType").as[List[String]].size                should be > 0
      }
    }

  }

}
