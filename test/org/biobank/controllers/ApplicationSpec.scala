package org.biobank.controllers

import org.biobank.fixture.{ ControllerFixture, NameGenerator }

import com.typesafe.plugin._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import play.api.Play.current
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test._

class ApplicationSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  "Application" should {

    "send 404 on a bad request" in new WithApplication(fakeApplication()) {
      route(FakeRequest(GET, "/xyz")) should be (None)
    }

    "return results for index" in new WithApplication(fakeApplication()) {
      val home = route(FakeRequest(GET, "/")).get

      status(home) should be (OK)
      contentType(home) should be (Some("text/html"))
    }

    "return initial aggregate counts" in new WithApplication(fakeApplication()) {
      doLogin
      val json = makeRequest(GET, "/counts")
      val jsonObj = (json \ "data").as[JsObject]

      (jsonObj \ "studies").as[Int] should be (0)
      (jsonObj \ "centres").as[Int] should be (0)

      // should be 1 because of default user
      (jsonObj \ "users").as[Int] should be (1)
    }

    "return correct aggregate counts" in new WithApplication(fakeApplication()) {
      doLogin

      use[BbwebPlugin].studyRepository.put(factory.createDisabledStudy)
      use[BbwebPlugin].centreRepository.put(factory.createDisabledCentre)
      use[BbwebPlugin].userRepository.put(factory.createRegisteredUser)

      val json = makeRequest(GET, "/counts")
      val jsonObj = (json \ "data").as[JsObject]

      (jsonObj \ "studies").as[Int] should be (1)
      (jsonObj \ "centres").as[Int] should be (1)

      // should be 1 because of default user
      (jsonObj \ "users").as[Int] should be (2)
    }

  }
}
