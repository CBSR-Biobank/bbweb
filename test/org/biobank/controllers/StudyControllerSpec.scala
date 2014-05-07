package org.biobank.controllers

import org.biobank.fixture.TestComponentImpl
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures
import play.api.test.FakeRequest
import play.api.test.Helpers._

class StudyControllerSpec extends TestComponentImpl with ScalaFutures with WordSpecLike with Matchers {

  "A study controller" should {

    "list all the studies" in {
      val result = study.StudyController.list(FakeRequest())

      status(result) should be (OK)
    }

  }

}
