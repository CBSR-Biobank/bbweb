package org.biobank.controllers

import org.biobank.fixture.ControllerFixture

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
          val json = makeJsonRequest(GET, "/studies/sgroup")
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 0
        }
      }
    }

    describe("GET /studies/sgroups") {
      it("should list none") (pending)
    }

    describe("GET /studies/sgroups") {
      it("should list a single specimen group") (pending)
    }

    describe("GET /studies/sgroups") {
      it("should list multiple specimen groups") (pending)
    }

    describe("POST /studies/sgroups") {
      it("should add a specimen group") (pending)
    }

    describe("PUT /studies/sgroups") {
      it("should update a specimen group") (pending)
    }

    describe("DELETE /studies/sgroups") {
      it("should remove a specimen group") (pending)
    }
  }

}
