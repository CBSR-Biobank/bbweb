package org.biobank.controllers

import org.biobank.domain.FactoryComponent
import org.biobank.fixture.TestComponentImpl
import org.biobank.service.ServiceComponentImpl
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

  val fakeApplication = FakeApplication(
    withoutPlugins = List("com.typesafe.plugin.CommonsMailerPlugin"))

  describe("Study REST API") {
    describe("GET /studies") {
      it("should list no studies") {
        running(fakeApplication) {

          Given("no parameter")
          val Some(result) = route(FakeRequest(GET, "/studies"))

          Then("StatusCode is 200")
          status(result)(timeout) should be (OK)

          And("ContentType is application/json")
          contentType(result)(timeout) should be (Some("application/json"))

          And("Content is all studies' json")
          contentAsString(result)(timeout) should include ("[]")

          val study1 = factory.createDisabledStudy
          //val study1Json = Json.toJson(study1)
          studyRepository.put(study1)

          //result = study.StudyController.list(FakeRequest())
          //status(result)(timeout) should be (OK)
          //contentAsJson(result)(timeout) should include ("[]")
        }
      }
    }

    describe("GET /studies") {
      it("should list a study") {
        running(fakeApplication) {
          Given("with a study in the repository")
          val study1 = factory.createDisabledStudy
          val Some(result) = route(FakeRequest(GET, "/studies"))

          Then("StatusCode is 200")
          status(result)(timeout) should be (OK)

          And("ContentType is application/json")
          contentType(result)(timeout) should be (Some("application/json"))

          And("Content is all studies' json")
          contentAsString(result)(timeout) should include ("[]")
        }
      }
    }

  }

}
