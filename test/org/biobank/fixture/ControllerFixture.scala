package org.biobank.fixture

import org.biobank.Global
import org.biobank.domain._
import org.biobank.domain.user.{ UserRepository, UserRepositoryImpl }
import org.biobank.domain.centre._
import org.biobank.domain.study._
import org.biobank.domain.participants._
import org.biobank.service.{
  PasswordHasher,
  PasswordHasherImpl
}

import play.api.inject.guice.GuiceApplicationBuilder
import org.scalatest._
import org.scalatestplus.play._
import play.api.Play
import play.api.libs.json._
import play.mvc.Http.RequestBuilder
import play.api.test.FakeApplication
import play.api.test.FakeHeaders
import play.api.test.Helpers._
import akka.actor.ActorSystem
import scala.concurrent.Future
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging._

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
    extends PlaySpec
    with OneServerPerTest
    with BeforeAndAfterEach
    with MustMatchers
    with OptionValues
    with BbwebFakeApplication {

  val log = Logger(LoggerFactory.getLogger(this.getClass.getSimpleName))

  val nameGenerator = new NameGenerator(this.getClass)

  private val dbName = "bbweb-test"

  var adminToken: String = ""

  // allow tests to access the test [[Factory]].
  val factory = new Factory

  /**
   * tests will not work with EhCache, need alternate implementation for EhCachePlugin
   *
   * See FixedEhCachePlugin.
    */
  implicit override def newAppForTest(testData: TestData): FakeApplication = {
    val fakeApp = FakeApplication(additionalConfiguration = Map("ehcacheplugin" -> "disabled"))
    initializeAkkaPersitence
    fakeApp
  }

  private def initializeAkkaPersitence(): Unit = {
    // ensure the database is empty
  }

  def doLogin(email: String = Global.DefaultUserEmail, password: String = "testuser") = {
    // Log in with test user
    val request = Json.obj("email" -> email, "password" -> password)
    val builder = new RequestBuilder().method(POST).uri("/login").bodyJson(request)
    val result = Future.successful(play.test.Helpers.route(builder).toScala)
    val resultStatus = status(result)

    resultStatus match {
      case OK =>
        val jsonResult = contentAsJson(result)
        adminToken = (jsonResult \ "data").as[String]
        adminToken
      case _ =>
        cancel(s"login failed: status: $resultStatus")
    }
  }

  def makeRequest(method:         String,
                  path:           String,
                  expectedStatus: Int,
                  json:           JsValue,
                  token:          String): JsValue = {
    val builder = new RequestBuilder()
      .method(method)
      .uri(path)
      .bodyJson(json)
      .header("X-XSRF-TOKEN", token)
      .cookie(new play.mvc.Http.Cookie("XSRF-TOKEN", token, 10, "", "", true, true))

    if (json != JsNull) {
      log.info(s"request: $method, $path,\n${Json.prettyPrint(json)}")
    } else {
      log.info(s"request: $method, $path")
    }

    val result = Future.successful(play.test.Helpers.route(builder).toScala)
    val resultStatus = status(result)

    resultStatus match {
      case `expectedStatus` =>
        val jsonResult = contentAsJson(result)
        contentType(result) mustBe Some("application/json")
        log.info(s"reply: status: $resultStatus,\nresult: ${Json.prettyPrint(jsonResult)}")
        jsonResult
      case _ =>
        contentType(result) match {
          case Some("application/json") => log.info("reply: " + Json.prettyPrint(contentAsJson(result)))
          case _ => log.info("reply: " + contentAsString(result))
        }
        fail(s"bad HTTP status: status: $resultStatus, expected: $expectedStatus")
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

  def collectionEventTypeRepository            = app.injector.instanceOf[CollectionEventTypeRepository]
  def processingTypeRepository                 = app.injector.instanceOf[ProcessingTypeRepository]
  def specimenGroupRepository                  = app.injector.instanceOf[SpecimenGroupRepository]
  def specimenLinkTypeRepository               = app.injector.instanceOf[SpecimenLinkTypeRepository]
  def studyRepository                          = app.injector.instanceOf[StudyRepository]

  def participantRepository                    = app.injector.instanceOf[ParticipantRepository]
  def collectionEventRepository                = app.injector.instanceOf[CollectionEventRepository]
  def ceventSpecimenRepository                 = app.injector.instanceOf[CeventSpecimenRepository]

  def userRepository = app.injector.instanceOf[UserRepository]

  def centreRepository          = app.injector.instanceOf[CentreRepository]

}
