package org.biobank.controllers

import org.biobank.domain.user.RegisteredUser
import org.biobank.fixture.NameGenerator
import org.biobank.fixture.ControllerFixture
import play.api.mvc.Cookie
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import com.typesafe.plugin._
import play.api.Play.current

class ApplicationSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def createUserInRepository(plainPassword: String): RegisteredUser = {
    val salt = use[BbwebPlugin].passwordHasher.generateSalt

    val user = factory.createRegisteredUser.copy(
      salt = salt,
      password = use[BbwebPlugin].passwordHasher.encrypt(plainPassword, salt)
    )
    use[BbwebPlugin].userRepository.put(user)
    user
  }

  "Application" should {

    "send 404 on a bad request" in new WithApplication(fakeApplication()) {
      route(FakeRequest(GET, "/xyz")) should be (None)
    }

    "return results for index" in new WithApplication(fakeApplication()) {
      val home = route(FakeRequest(GET, "/")).get

      status(home) should be (OK)
      contentType(home) should be (Some("text/html"))
    }

    "allow a user to log in" in new WithApplication(fakeApplication()) {
      val plainPassword = nameGenerator.next[String]
      val user = createUserInRepository(plainPassword)

      val cmdJson = Json.obj(
        "email"     -> user.email,
        "password"  -> plainPassword)
      val json = makeRequest(POST, "/login", json = cmdJson)

      (json \ "token").as[String].length should be > 0
    }

    "prevent an invalid user login request" in new WithApplication(fakeApplication()) {
      val cmdJson = Json.obj(
        "email"     -> nameGenerator.nextEmail[String],
        "password"  -> nameGenerator.next[String])
      val json = makeRequest(POST, "/login", BAD_REQUEST, json = cmdJson)

      (json \ "status").as[String] should include ("error")
        (json \ "message").as[String] should include ("not found")
    }

    "prevent a user logging in with bad password" in new WithApplication(fakeApplication()) {
      val plainPassword = nameGenerator.next[String]
      val salt = use[BbwebPlugin].passwordHasher.generateSalt

      val user = factory.createRegisteredUser.copy(
        salt = salt,
        password = use[BbwebPlugin].passwordHasher.encrypt(plainPassword, salt)
      )
      use[BbwebPlugin].userRepository.put(user)

      val cmdJson = Json.obj(
        "email"     -> user.email,
        "password"  -> nameGenerator.next[String])
      val json = makeRequest(POST, "/login", BAD_REQUEST, json = cmdJson)

      (json \ "status").as[String] should include ("error")
        (json \ "message").as[String] should include ("invalid password")
    }

    "disallow access to logged out users" in new WithApplication(fakeApplication()) {
      doLogin

      // this request is valid since user is logged in
      var json = makeRequest(GET, "/users")
      val jsonList = json.as[List[JsObject]]
      jsonList should have size 1

      // the user is now logged out
      json = makeRequest(POST, "/logout")
      (json \ "status").as[String] should include ("success")

      // the following request should fail
      json = makeRequest(GET, "/users", UNAUTHORIZED)

      (json \ "status").as[String] should include ("error")
      (json \ "message").as[String] should include ("invalid token")
    }

    "not allow a request with an invalid token" in new WithApplication(fakeApplication()) {
      doLogin

      val badToken = nameGenerator.next[String]

      // this request is valid since user is logged in
      val resp = route(FakeRequest(GET, "/users")
        .withHeaders("X-XSRF-TOKEN" -> badToken)
        .withCookies(Cookie("XSRF-TOKEN", badToken)))
      resp should not be (None)
      resp.map{ result =>
        status(result) should be(UNAUTHORIZED)
        contentType(result) should be(Some("application/json"))
        val json = Json.parse(contentAsString(result))
          (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("invalid token")
      }
    }

    "not allow mismatched tokens in request" in new WithApplication(fakeApplication()) {
      val validToken = doLogin
      val badToken = nameGenerator.next[String]

      // this request is valid since user is logged in
      val resp = route(FakeRequest(GET, "/users")
        .withHeaders("X-XSRF-TOKEN" -> validToken)
        .withCookies(Cookie("XSRF-TOKEN", badToken)))
      resp should not be (None)
      resp.map { result =>
        status(result) should be(UNAUTHORIZED)
        contentType(result) should be(Some("application/json"))
        val json = Json.parse(contentAsString(result))
          (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("Token mismatch")
      }
    }

    "not allow requests missing XSRF-TOKEN cookie" in new WithApplication(fakeApplication()) {
      doLogin

      val resp = route(FakeRequest(GET, "/users"))
      resp should not be (None)
      resp.map { result =>
        status(result) should be(UNAUTHORIZED)
        contentType(result) should be(Some("application/json"))
        val json = Json.parse(contentAsString(result))
          (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("Invalid XSRF Token cookie")
      }
    }

    "not allow requests missing X-XSRF-TOKEN in header" in new WithApplication(fakeApplication()) {
      val token = doLogin

      val resp = route(FakeRequest(GET, "/users").withCookies(Cookie("XSRF-TOKEN", token)))
      resp should not be (None)
      resp.map { result =>
        status(result) should be(UNAUTHORIZED)
        contentType(result) should be(Some("application/json"))
        val json = Json.parse(contentAsString(result))
          (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("No token")
      }
    }

  }
}
