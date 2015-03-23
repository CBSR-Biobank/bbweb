package org.biobank.controllers

import org.biobank.fixture.{ ControllerFixture, NameGenerator }
import org.biobank.domain.user.UserRepository
import org.biobank.Global

import org.scalatest.Tag
import org.slf4j.LoggerFactory
import play.api.Play.current
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import org.scalatest._
import org.scalatestplus.play._

class ApplicationSpec extends ControllerFixture {
  import TestGlobal._

  val log = LoggerFactory.getLogger(this.getClass)

  "Application" must {

    "send 404 on a bad request" in {
      route(FakeRequest(GET, "/xyz")) mustBe (None)
    }

    "return results for index" in {
      val home = route(FakeRequest(GET, "/")).get

      status(home) mustBe (OK)
      contentType(home) mustBe (Some("text/html"))
    }

    "return initial aggregate counts" in {
      val json = makeRequest(GET, "/counts")
      val jsonObj = (json \ "data").as[JsObject]

      (jsonObj \ "studies").as[Int] mustBe (0)
        (jsonObj \ "centres").as[Int] mustBe (0)
        (jsonObj \ "users").as[Int] mustBe (0)
    }

    "return correct aggregate counts" in {
      studyRepository.put(factory.createDisabledStudy)
      centreRepository.put(factory.createDisabledCentre)
      userRepository.put(factory.createRegisteredUser)

      val json = makeRequest(GET, "/counts")
      val jsonObj = (json \ "data").as[JsObject]

      (jsonObj \ "studies").as[Int] mustBe (1)
        (jsonObj \ "centres").as[Int] mustBe (1)
        (jsonObj \ "users").as[Int] mustBe (1)
    }

  }
}
