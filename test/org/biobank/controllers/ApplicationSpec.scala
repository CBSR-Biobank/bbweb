package org.biobank.controllers

import org.biobank.fixture.ControllerFixture

import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._

class ApplicationSpec extends ControllerFixture {

  describe("Application") {

    it("send 404 on a bad request") {
      val result = route(app, FakeRequest(GET, "/xyz")).get
      status(result) mustEqual NOT_FOUND
    }

    it("return results for index") {
      val result = route(app, FakeRequest(GET, "/")).get
      status(result) mustBe (OK)
      contentType(result) mustBe (Some("text/html"))
    }

    it("return initial aggregate counts") {
      val json = makeRequest(GET, "/dtos/counts")
      val jsonObj = (json \ "data").as[JsObject]

      (jsonObj \ "studies").as[Int] mustBe (0)

      (jsonObj \ "centres").as[Int] mustBe (0)

      (jsonObj \ "users").as[Int] mustBe (1) // 1 for the default user
    }

    it("return correct aggregate counts") {
      studyRepository.put(factory.createDisabledStudy)
      centreRepository.put(factory.createDisabledCentre)
      userRepository.put(factory.createRegisteredUser)

      val json = makeRequest(GET, "/dtos/counts")
      val jsonObj = (json \ "data").as[JsObject]

      (jsonObj \ "studies").as[Int] mustBe (1)

      (jsonObj \ "centres").as[Int] mustBe (1)

      (jsonObj \ "users").as[Int] mustBe (2) // +1 for the default user
    }

  }
}
