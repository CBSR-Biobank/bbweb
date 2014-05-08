package org.biobank.controllers

import org.biobank.domain.FactoryComponent
import org.biobank.fixture.TestComponentImpl
import org.biobank.service.ServiceComponentImpl
import org.biobank.service.json.Study._
import akka.actor.Props
import org.scalatest.FunSpec
import org.scalatest.GivenWhenThen
import org.scalatest.Matchers
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.test.FakeApplication
import play.api.libs.json._
import play.api.Logger

/**
  *
  * Note need to pass timeout to status if not compiler complains about ambiguous implicit values.
  */
class StudyControllerSpec
    extends FunSpec
    with TestComponentImpl
    with FactoryComponent
    with ServiceComponentImpl
    with GivenWhenThen
    with Matchers {

  override val studyProcessor = system.actorOf(Props(new StudyProcessor), "studyproc")
  override val userProcessor = null

  override val studyService = null
  override val userService = null

  def fakeApplication = FakeApplication(
    withoutPlugins = List("com.typesafe.plugin.CommonsMailerPlugin"))

  describe("Study REST API") {
    describe("GET /studies") {
      it("should list no studies") {
        running(fakeApplication) {

          Given("no parameter")
          val result = route(FakeRequest(GET, "/studies")).get

          Then("StatusCode is 200")
          status(result)(timeout) should be (OK)

          And("ContentType is application/json")
          contentType(result)(timeout) should be (Some("application/json"))

          And("Content is empty json")
          contentAsString(result)(timeout) should include ("[]")
        }
      }
    }

    describe("GET /studies") {
      it("should list a study") {
        running(fakeApplication) {
          Given("with a study in the repository")
          val study = factory.createDisabledStudy
          val result = route(FakeRequest(GET, "/studies")).get

          Then("StatusCode is 200")
          status(result)(timeout) should be (OK)

          And("ContentType is application/json")
          contentType(result)(timeout) should be (Some("application/json"))

          And("Content is json for a single study")
          val expectedJson = Json.toJson(study)
          val actualJson = Json.parse(contentAsString(result)(timeout))
          assert(actualJson \ "id" === expectedJson \ "id")
        }
      }
    }

  }

}
