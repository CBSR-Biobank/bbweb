package org.biobank.controllers

import org.biobank.fixture.ControllerFixture
import org.biobank.domain.user.UserRepository
import org.biobank.domain.study.StudyRepository
import org.biobank.domain.centre.CentreRepository
import org.biobank.Global

import org.scalatest.Tag
import org.slf4j.LoggerFactory
import play.api.Play.current
import play.api.libs.json._
import play.api.test.Helpers._
import play.mvc.Http.RequestBuilder
import scala.concurrent.Future

class ApplicationSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  "Application" must {

    "send 404 on a bad request" taggedAs(Tag("1")) in {
      val request = new RequestBuilder().method("GET").uri("/xyz")
      val result = Future.successful(play.test.Helpers.route(request).toScala)
      status(result) mustEqual NOT_FOUND
    }

    "return results for index" taggedAs(Tag("1")) in {
      val request = new RequestBuilder().method("GET").uri("/")
      val result = Future.successful(play.test.Helpers.route(request).toScala)

      status(result) mustBe (OK)
      contentType(result) mustBe (Some("text/html"))
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
