package org.biobank.controllers

import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.JsonHelper._

import play.api.test.Helpers._
import play.api.test.FakeApplication
import play.api.libs.json._
import org.slf4j.LoggerFactory

class SpecimenGroupControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  describe("Specimen Group REST API") {
    describe("GET /studies/sgroups") {
      it("should list none") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val idJson = JsObject("id" -> JsString(study.id.id) :: Nil)
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

          val idJson = JsObject("id" -> JsString(study.id.id) :: Nil)
          val json = makeJsonRequest(GET, "/studies/sgroups", json = idJson)
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 1
          compareObj(jsonList(0), sg)
        }
      }
    }

    describe("GET /studies/sgroups") {
      it("should list multiple specimen groups") (pending)
    }

    describe("POST /studies/sgroups") {
      it("should add a specimen group") (pending)
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
