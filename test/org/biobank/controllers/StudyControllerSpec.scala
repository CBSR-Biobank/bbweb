package org.biobank.controllers

import org.biobank.domain.FactoryComponent
import org.biobank.fixture.TestComponentImpl
import org.biobank.service.ServiceComponentImpl
import akka.actor.Props
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.libs.json._

/**
  *
  * Note need to pass timeout to status if not compiler complains about ambiguous implicit values.
  */
class StudyControllerSpec
    extends TestComponentImpl
    with FactoryComponent
    with ServiceComponentImpl
    with WordSpecLike
    with Matchers {

  override val studyProcessor = system.actorOf(Props(new StudyProcessor), "studyproc")
  override val userProcessor = null

  override val studyService = null
  override val userService = null

  "A study controller" should {

    "list all studies" in {
      var result = study.StudyController.list(FakeRequest())

      status(result)(timeout) should be (OK)
      contentType(result)(timeout) should be (Some("application/json"))
      contentAsString(result)(timeout) should include ("[]")

      val study1 = factory.createDisabledStudy
      //val study1Json = Json.toJson(study1)
      studyRepository.put(study1)

      result = study.StudyController.list(FakeRequest())

      status(result)(timeout) should be (OK)
      contentAsJson(result)(timeout) should include ("[]")
    }

  }

}
