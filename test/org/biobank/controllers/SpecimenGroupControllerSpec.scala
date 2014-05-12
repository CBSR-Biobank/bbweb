package org.biobank.controllers

import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.JsonHelper._

import play.api.test.Helpers._
import play.api.test.FakeApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory

class SpecimenGroupControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  describe("Specimen Group REST API") {
    describe("GET /studies/sgroups") {
      it("should list none"){
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/sgroups", json = idJson)
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 0
        }
      }
    }

    describe("GET /studies/sgroups") {
      it("should list a single specimen group") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val sg = factory.createSpecimenGroup
          appRepositories.specimenGroupRepository.put(sg)

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/sgroups", json = idJson)
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 1
          compareObj(jsonList(0), sg)
        }
      }
    }

    describe("GET /studies/sgroups") {
      it("should list multiple specimen groups") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val sgroups = List(factory.createSpecimenGroup, factory.createSpecimenGroup)
          sgroups map { sg => appRepositories.specimenGroupRepository.put(sg) }

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/sgroups", json = idJson)
          val jsonList = json.as[List[JsObject]]
          jsonList should have size sgroups.size
          (jsonList zip sgroups).map { item => compareObj(item._1, item._2) }
        }
      }
    }

    describe("POST /studies/sgroups") {
      it("should add a specimen group", Tag("single")) {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val sg = factory.createSpecimenGroup
          val cmdJson = Json.obj(
            "studyId" -> sg.studyId.id,
              "name" -> sg.name,
              "description" -> sg.description,
              "units" -> sg.units,
              "anatomicalSourceType" -> sg.anatomicalSourceType.toString,
              "preservationType" -> sg.preservationType.toString,
              "preservationTemperatureType" -> sg.preservationTemperatureType.toString,
              "specimenType" -> sg.specimenType.toString)
          val json = makeJsonRequest(POST, "/studies", json = cmdJson)

          (json \ "message").as[String] should include ("specimen group added")
        }
      }
    }

    describe("POST /studies/sgroups") {
      it("should not add a specimen group to enabled study") (pending)
    }

    describe("PUT /studies/sgroups") {
      it("should update a specimen group") (pending)
    }

    describe("PUT /studies/sgroups") {
      it("should not update a specimen group on an enabled study") (pending)
    }

    describe("DELETE /studies/sgroups") {
      it("should remove a specimen group") (pending)
    }
  }

}
