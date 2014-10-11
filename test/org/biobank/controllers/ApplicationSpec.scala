package org.biobank.controllers

import org.biobank.fixture.{ ControllerFixture, NameGenerator }

import com.typesafe.plugin._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import play.api.Play.current
import play.api.libs.json._
import play.api.test._
import play.api.test.Helpers._
import org.scalatest._
import org.scalatestplus.play._

class ApplicationSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  "Application" must {

    "send 404 on a bad request" in new App(fakeApp) {
      route(FakeRequest(GET, "/xyz")) mustBe (None)
    }

    "return results for index" in new App(fakeApp) {
      val home = route(FakeRequest(GET, "/")).get

      status(home) mustBe (OK)
      contentType(home) mustBe (Some("text/html"))
    }

    "return initial aggregate counts" in new App(fakeApp) {
      doLogin
      val json = makeRequest(GET, "/counts")
      val jsonObj = (json \ "data").as[JsObject]

      (jsonObj \ "studies").as[Int] mustBe (0)
      (jsonObj \ "centres").as[Int] mustBe (0)

      // mustBe 1 because of default user
      (jsonObj \ "users").as[Int] mustBe (1)
    }

    "return correct aggregate counts" in new App(fakeApp) {
      doLogin

      use[BbwebPlugin].studyRepository.put(factory.createDisabledStudy)
      use[BbwebPlugin].centreRepository.put(factory.createDisabledCentre)
      use[BbwebPlugin].userRepository.put(factory.createRegisteredUser)

      val json = makeRequest(GET, "/counts")
      val jsonObj = (json \ "data").as[JsObject]

      (jsonObj \ "studies").as[Int] mustBe (1)
      (jsonObj \ "centres").as[Int] mustBe (1)

      // mustBe 1 because of default user
      (jsonObj \ "users").as[Int] mustBe (2)
    }

  }
}
