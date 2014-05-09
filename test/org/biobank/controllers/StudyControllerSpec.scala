package org.biobank.controllers

import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.service.json.JsonHelper._
import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.Study._
import play.api.Play
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
      it("should list no studies", Tag("thistest")) {
        running(fakeApplication) {
          val json = makeJsonRequest(GET, "/studies")
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 0

        }
      }

      it("should list a study", Tag("thistest")) {
        running(fakeApplication) {
          val myStudyRepository = Play.current.plugin[BbwebPlugin].map(_.studyRepository).getOrElse {
            sys.error("Bbweb plugin is not registered")
          }

          val study = factory.createDisabledStudy
          myStudyRepository.put(study)
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
          val studyRepository = Play.current.plugin[BbwebPlugin].map(_.studyRepository).getOrElse {
            sys.error("Bbweb plugin is not registered")
          }

          val studies = List(factory.createDisabledStudy, factory.createDisabledStudy)
          studyRepository.removeAll
          studies.map(study => studyRepository.put(study))

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
          val studyRepository = Play.current.plugin[BbwebPlugin].map(_.studyRepository).getOrElse {
            sys.error("Bbweb plugin is not registered")
          }

          val study = factory.createDisabledStudy
          val map = Map("name" -> study.name, "description" -> study.description.getOrElse("null"))
          val json = makeJsonRequest(POST, "/studies", Json.toJson(map))

          log.info(s"$json")
        }
      }
    }

  }

}
