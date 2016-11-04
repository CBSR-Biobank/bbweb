package org.biobank.controllers

import org.biobank.fixture.ControllerFixture

import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._

class ApplicationSpec extends ControllerFixture {

  "Application" must {

    "send 404 on a bad request" in {
      val result = route(app, FakeRequest(GET, "/xyz")).get
      status(result) mustEqual NOT_FOUND
    }

    "return results for index" in {
      val result = route(app, FakeRequest(GET, "/")).get
      status(result) mustBe (OK)
      contentType(result) mustBe (Some("text/html"))
    }

    "return initial aggregate counts" in {
      val json = makeRequest(GET, "/dtos/counts")
      val jsonObj = (json \ "data").as[JsObject]

      (jsonObj \ "studies").as[Int] mustBe (0)

      (jsonObj \ "centres").as[Int] mustBe (0)

      (jsonObj \ "users").as[Int] mustBe (0)
    }

    "return correct aggregate counts" in {
      studyRepository.put(factory.createDisabledStudy)
      centreRepository.put(factory.createDisabledCentre)
      userRepository.put(factory.createRegisteredUser)

      val json = makeRequest(GET, "/dtos/counts")
      val jsonObj = (json \ "data").as[JsObject]

      (jsonObj \ "studies").as[Int] mustBe (1)

      (jsonObj \ "centres").as[Int] mustBe (1)

      (jsonObj \ "users").as[Int] mustBe (1)
    }

  }
}
