package org.biobank.controllers

import org.biobank.fixture.ControllerFixture
import play.api.test._
import play.api.test.Helpers._

class ApplicationSpec extends ControllerFixture {

  "Application" should {

    "send 404 on a bad request" in new WithApplication(fakeApplication()) {
      route(FakeRequest(GET, "/boum")) should be (None)
    }

    "return results for index" in new WithApplication(fakeApplication()) {
      val home = route(FakeRequest(GET, "/")).get

      status(home) should be (OK)
      contentType(home) should be (Some("text/html"))
    }

    "allow user to log in" in new WithApplication(fakeApplication()) {
      doLogin
    }

  }
}
