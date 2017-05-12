package org.biobank.fixture

import org.biobank.Global
import org.biobank.controllers.FixedEhCache
import org.biobank.domain._
import org.biobank.domain.centre._
import org.biobank.domain.participants._
import org.biobank.domain.processing._
import org.biobank.domain.study._
import org.biobank.domain.user.UserRepository
import org.biobank.service.PasswordHasher
import org.scalatest._
import org.scalatestplus.play._
import play.api.Logger
import play.api.cache.{ CacheApi /* , EhCacheModule */ }
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._

trait BbwebFakeApplication {

  def makeRequest(method:         String,
                  path:           String,
                  expectedStatus: Int,
                  json:           JsValue,
                  token:          String): JsValue

  def makeRequest(method:         String,
                  path:           String,
                  expectedStatus: Int,
                  json:           JsValue): JsValue

  def makeRequest(method:         String,
                  path:           String,
                  expectedStatus: Int): JsValue

  def makeRequest(method: String,
                  path:   String,
                  json:   JsValue): JsValue

  def makeRequest(method: String, path: String): JsValue

}

/**
 * This trait allows a test suite to run tests on a Play Framework fake application.
 *
 * It uses the [[https://github.com/ddevore/akka-persistence-mongo/ Mongo Journal for Akka Persistence]] to
 * make it easier to drop all items in the database prior to running a test in a test suite.
 */
abstract class ControllerFixture
    extends FunSpec
    with OneServerPerTest
    with OneBrowserPerTest
    with HtmlUnitFactory
    with BeforeAndAfterEach
    with MustMatchers
    with OptionValues
    with BbwebFakeApplication {

  val log = Logger(this.getClass())

  val nameGenerator = new NameGenerator(this.getClass())

  var adminToken: String = ""

  val factory = new Factory

  /**
   * tests will not work with EhCache, need alternate implementation for EhCachePlugin.
   */
  override def newAppForTest(testData: TestData) =
    new GuiceApplicationBuilder()
      .overrides(bind[CacheApi].to[FixedEhCache])
      .build()

  def doLogin(email: String = Global.DefaultUserEmail, password: String = "testuser") = {
    val request = Json.obj("email" -> email, "password" -> password)
    route(app, FakeRequest(POST, "/users/login").withJsonBody(request)).fold {
      cancel("login failed")
    } { result =>
      status(result) mustBe (OK)
      contentType(result) mustBe Some("application/json")
      val json = Json.parse(contentAsString(result))

      (json \ "data" \ "email").as[String] must be (email)

      getTokenFromHeader(headers(result))
    }
  }

  private def getTokenFromHeader(headers: Map[String, String]) = {
    val cookie = headers("Set-Cookie")
    cookie must include ("XSRF-TOKEN")
    cookie.split("; ")(0).split("=")(1)
  }

  def makeRequest(method:         String,
                  path:           String,
                  expectedStatus: Int,
                  json:           JsValue,
                  token:          String): JsValue = {
    val fakeRequest = FakeRequest(method, path)
      .withJsonBody(json)
      .withHeaders("X-XSRF-TOKEN" -> token)
      .withCookies(Cookie("XSRF-TOKEN", token))

    if (json != JsNull) {
      log.debug(s"request: $method, $path,\n${Json.prettyPrint(json)}")
    } else {
      log.debug(s"request: $method, $path")
    }

    route(app, fakeRequest).fold {
      fail("HTTP request returned NONE")
    } { result =>
      status(result) match {
        case `expectedStatus` =>
          val bodyText = contentAsString(result)
          if (bodyText.isEmpty) {
            log.debug(s"reply: status: $result,\nbodyText: EMPTY")
            JsNull
          } else {
            contentType(result) mustBe Some("application/json")
            val jsonResult = contentAsJson(result)
            log.debug(s"reply: status: $result,\nresult: ${Json.prettyPrint(jsonResult)}")
            jsonResult
          }
        case code =>
          contentType(result) match {
            case Some("application/json") => log.debug("reply: " + Json.prettyPrint(contentAsJson(result)))
            case _ => log.debug("reply: " + contentAsString(result))
          }
          fail(s"bad HTTP status: status: $code, expected: $expectedStatus")
      }
    }
  }

  def makeRequest(method:         String,
                  path:           String,
                  expectedStatus: Int,
                  json:           JsValue = JsNull): JsValue = {
    makeRequest(method, path, expectedStatus, json, "bbweb-test-token")
  }

  def makeRequest(method:         String,
                  path:           String,
                  expectedStatus: Int): JsValue = {
    makeRequest(method, path, expectedStatus, JsNull, "bbweb-test-token")
  }

  def makeRequest(method: String,
                  path:   String,
                  json:   JsValue): JsValue = {
    makeRequest(method, path, OK, json, "bbweb-test-token")
  }

  def makeRequest(method: String, path: String): JsValue = {
    makeRequest(method, path, OK, JsNull, "bbweb-test-token")
  }

  // for the following getters: a new application is created for each test, therefore,
  // new instances of each of these is created with the new application

  def passwordHasher = app.injector.instanceOf[PasswordHasher]

  def collectionEventTypeRepository          = app.injector.instanceOf[CollectionEventTypeRepository]
  def processingTypeRepository               = app.injector.instanceOf[ProcessingTypeRepository]
  def specimenGroupRepository                = app.injector.instanceOf[SpecimenGroupRepository]
  def specimenLinkTypeRepository             = app.injector.instanceOf[SpecimenLinkTypeRepository]
  def studyRepository                        = app.injector.instanceOf[StudyRepository]

  def participantRepository                  = app.injector.instanceOf[ParticipantRepository]
  def collectionEventRepository              = app.injector.instanceOf[CollectionEventRepository]
  def ceventSpecimenRepository               = app.injector.instanceOf[CeventSpecimenRepository]
  def specimenRepository                     = app.injector.instanceOf[SpecimenRepository]
  def processingEventInputSpecimenRepository = app.injector.instanceOf[ProcessingEventInputSpecimenRepository]

  def userRepository                         = app.injector.instanceOf[UserRepository]

  def centreRepository                       = app.injector.instanceOf[CentreRepository]
  def shipmentRepository                     = app.injector.instanceOf[ShipmentRepository]
  def shipmentSpecimenRepository             = app.injector.instanceOf[ShipmentSpecimenRepository]

}
