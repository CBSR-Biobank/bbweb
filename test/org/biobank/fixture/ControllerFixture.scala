package org.biobank.fixture

import org.biobank.domain.Factory

import org.scalatest.WordSpec
import org.scalatest.BeforeAndAfterEach
import org.scalatest.Matchers
import play.api.Play
import play.api.mvc.Cookie
import play.api.test.FakeApplication
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test.FakeRequest
import com.mongodb.casbah.Imports._
import play.api.Logger

/**
 * This trait allows a test suite to run tests on a Play Framework fake application.
 *
 * It include a [[FactoryComponent]] to make the creation of domain model objects easier. It uses
 * the [[https://github.com/ddevore/akka-persistence-mongo/ Mongo Journal for Akka Persistence]]
 * to make it easier to drop all items in the database prior to running a test suite.
 */
trait ControllerFixture
  extends WordSpec
  with Matchers
  with BeforeAndAfterEach {

  private val dbName = "bbweb-test"

  var token: String = ""

  val factory = new Factory

  /**
   * tests will not work with EhCache, need alternate implementation
   *
   * See FixedEhCachePlugin.
   */
  protected val fakeApplication = () => FakeApplication(
    additionalPlugins = List("org.biobank.controllers.FixedEhCachePlugin"),
    additionalConfiguration = Map("ehcacheplugin" -> "disabled"))

  override def beforeEach: Unit = {
    // ensure the database is empty
    MongoConnection()(dbName)("messages").drop
    MongoConnection()(dbName)("snapshots").drop
  }

  def doLogin() = {
    // Log in with test user
    val request = Json.obj("email" -> "admin@admin.com", "password" -> "testuser")
    route(FakeRequest(POST, "/login").withJsonBody(request)) match {
      case Some(result) =>
        status(result) should be(OK)
        contentType(result) should be(Some("application/json"))
        val json = Json.parse(contentAsString(result))
        token = (json \ "data").as[String]
        token
      case _ =>
        cancel("login failed")
    }
  }

  def makeRequest(
    method: String,
    path: String,
    expectedStatus: Int = OK,
    json: JsValue = JsNull): JsValue = {
    var fakeRequest = FakeRequest(method, path)
      .withJsonBody(json)
      .withHeaders("X-XSRF-TOKEN" -> token)
      .withCookies(Cookie("XSRF-TOKEN", token))
    Logger.info(s"makeRequest: request: $fakeRequest, $json")
    route(fakeRequest).fold {
      cancel("HTTP request returned NONE")
    } { result =>
      Logger.info(s"makeRequest: status: ${status(result)}, result: ${contentAsString(result)}")
      status(result) should be(expectedStatus)
      contentType(result) should be(Some("application/json"))
      Json.parse(contentAsString(result))
    }
  }

}

