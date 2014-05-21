package org.biobank.controllers

import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.JsonHelper._

import play.api.test.Helpers._
import play.api.test.FakeApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class CollectionEventTypeControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  describe("Collection Event Type REST API") {
    describe("GET /studies/cetypes") {
      it("should list none") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/cetypes", json = idJson)
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 0
        }
      }
    }

    describe("GET /studies/sgroups") {
      it("should list a single collection event type", Tag("single")) {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val sg = factory.createCollectionEventType
          appRepositories.collectionEventTypeRepository.put(sg)

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/cetypes", json = idJson)
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 1
          compareObj(jsonList(0), sg)
        }
      }
    }

    describe("GET /studies/sgroups") {
      it("should list multiple collection event types") (pending)
    }

    describe("POST /studies/sgroups") {
      it("should add a collection event type") (pending)
    }

    describe("PUT /studies/sgroups") {
      it("should update a collection event type") (pending)
    }

    describe("DELETE /studies/sgroups") {
      it("should remove a collection event type") (pending)
    }
  }

}
