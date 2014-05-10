package org.biobank.controllers

import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.service.json.JsonHelper._
import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.Study._
//import play.api.Play
import play.api.test.Helpers._
import play.api.test.FakeRequest
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
      it("should list no studies") {
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
          val cmdJson = JsObject(
            "name" -> JsString(study.name) :: "description" -> JsString(study.description.getOrElse("null")) :: Nil)
          val json = makeJsonRequest(POST, "/studies", json = cmdJson)

          (json \ "message").as[String] should include ("Study added")
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

          val cmdJson = JsObject(
            "id" -> JsString(study.id.id) :: "expectedVersion" -> JsNumber(study.version) :: Nil)
          val json = makeJsonRequest(POST, "/studies/enable", json = cmdJson)

          (json \ "message").as[String] should include ("Study enabled")
        }
      }
    }

    describe("POST /studies/enable") {
      it("should not enable a study", Tag("single")) {
        running(fakeApplication) {

          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val cmdJson = JsObject(
            "id" -> JsString(study.id.id) :: "expectedVersion" -> JsNumber(study.version) :: Nil)
          val json = makeJsonRequest(POST, "/studies/enable", BAD_REQUEST, cmdJson)

          (json \ "message").as[String] should include ("no specimen groups")
        }
      }
    }

  }

}
