package org.biobank.controllers

import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.service.json.JsonHelper._
import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.Study._
import play.api.test.Helpers._
import play.api.test.FakeApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory

/**
  * Tests the REST API for [[Study]].
  */
class StudyControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  describe("Study REST API") {

    describe("GET /studies") {
      it("should list none") {
        running(fakeApplication) {
          val json = makeJsonRequest(GET, "/studies")
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 0

        }
      }

      it("should list a study") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val json = makeJsonRequest(GET, "/studies")
          val jsonList = json.as[List[JsObject]]
          jsonList should have length 1
          compareObj(jsonList(0), study)
        }
      }
    }

    describe("GET /studies") {
      it("should list multiple studies") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val studies = List(factory.createDisabledStudy, factory.createDisabledStudy)
          appRepositories.studyRepository.removeAll
          studies.map(study => appRepositories.studyRepository.put(study))

          val json = makeJsonRequest(GET, "/studies")
          val jsonList = json.as[List[JsObject]]
          jsonList should have size studies.size

          (jsonList zip studies).map { item => compareObj(item._1, item._2) }
        }
      }
    }

    describe("POST /studies") {
      it("should add a study") {
        running(fakeApplication) {
          val study = factory.createDisabledStudy
          val cmdJson = Json.obj(
            "type" -> "AddStudyCmd",
            "name" -> study.name,
            "description" -> study.description)
          val json = makeJsonRequest(POST, "/studies", json = cmdJson)

          (json \ "message").as[String] should include ("study added")
        }
      }
    }

    describe("PUT /studies/:id") {
      it("should update a study") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val cmdJson = Json.obj(
            "type"            -> "UpdateStudyCmd",
            "id"              -> study.id.id,
            "expectedVersion" -> Some(study.version),
            "name"            -> study.name,
            "description"     -> study.description)
          val json = makeJsonRequest(PUT, s"/studies/${study.id.id}", json = cmdJson)

          (json \ "message").as[String] should include ("study updated")
        }
      }
    }

    describe("GET /studies/:id") {
      it("should read a study") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy.enable(Some(0), org.joda.time.DateTime.now, 1, 1) | fail
          appRepositories.studyRepository.put(study)
          val json = makeJsonRequest(GET, s"/studies/${study.id.id}")
          compareObj(json, study)
        }
      }
    }

    describe("POST /studies/enable") {
      it("should enable a study") {
        running(fakeApplication) {

          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)
          appRepositories.specimenGroupRepository.put(factory.createSpecimenGroup)
          appRepositories.collectionEventTypeRepository.put(factory.createCollectionEventType)

          val cmdJson = Json.obj(
            "type" -> "EnableStudyCmd",
            "id" -> study.id.id,
            "expectedVersion" -> Some(study.version))
          val json = makeJsonRequest(POST, "/studies/enable", json = cmdJson)

          (json \ "message").as[String] should include ("study enabled")
        }
      }
    }

    describe("POST /studies/enable") {
      it("should not enable a study") {
        running(fakeApplication) {

          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val cmdJson = Json.obj(
            "type" -> "EnableStudyCmd",
            "id" -> study.id.id,
            "expectedVersion" -> Some(study.version))
          val json = makeJsonRequest(POST, "/studies/enable", BAD_REQUEST, cmdJson)

          (json \ "message").as[String] should include ("no specimen groups")
        }
      }
    }

    describe("POST /studies/disable") {
      it("should disable a study") {
        running(fakeApplication) {

          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy.enable(Some(0), org.joda.time.DateTime.now, 1, 1) | fail
          appRepositories.studyRepository.put(study)

          val cmdJson = Json.obj(
            "type" -> "DisableStudyCmd",
            "id" -> study.id.id,
            "expectedVersion" -> Some(study.version))
          val json = makeJsonRequest(POST, "/studies/disable", json = cmdJson)

          (json \ "message").as[String] should include ("study disabled")
        }
      }
    }

    describe("POST /studies/retire") {
      it("should retire a study") {
        running(fakeApplication) {

          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val cmdJson = Json.obj(
            "type" -> "RetireStudyCmd",
            "id" -> study.id.id,
            "expectedVersion" -> Some(study.version))
          val json = makeJsonRequest(POST, "/studies/retire", json = cmdJson)

          (json \ "message").as[String] should include ("study retired")
        }
      }
    }

    describe("POST /studies/unretire") {
      it("should unretire a study") {
        running(fakeApplication) {

          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy.retire(Some(0), org.joda.time.DateTime.now) | fail
          appRepositories.studyRepository.put(study)

          val cmdJson = Json.obj(
            "type" -> "UnretireStudyCmd",
            "id" -> study.id.id,
            "expectedVersion" -> Some(study.version))
          val json = makeJsonRequest(POST, "/studies/unretire", json = cmdJson)

          (json \ "message").as[String] should include ("study unretired")
        }
      }
    }

  }

}
