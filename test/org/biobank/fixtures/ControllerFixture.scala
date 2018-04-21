package org.biobank.fixture

import org.biobank.Global
import org.biobank.controllers.CacheForTesting
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.centres._
import org.biobank.domain.participants._
import org.biobank.domain.processing._
import org.biobank.domain.studies._
import org.biobank.domain.users._
import org.biobank.services.PasswordHasher
import org.scalatest._
import org.scalatest.matchers._
import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import org.slf4j.{Logger, LoggerFactory}
import play.api.Application
import play.api.cache.{AsyncCacheApi, DefaultSyncCacheApi, SyncCacheApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import scala.concurrent.{ Await, Future }
import scala.util.matching.Regex

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
    with GuiceOneServerPerTest
    with OneBrowserPerTest
    with HtmlUnitFactory
    with BeforeAndAfter
    with MustMatchers
    with OptionValues
    with BbwebFakeApplication
    with HttpReplyMatchers {

  override protected val log: Logger = LoggerFactory.getLogger(this.getClass)

  protected val nameGenerator = new NameGenerator(this.getClass())

  protected val factory = new Factory

  override def newAppForTest(testData: TestData) =
    new GuiceApplicationBuilder()
      .overrides(bind[SyncCacheApi].to[DefaultSyncCacheApi])
      .overrides(bind[AsyncCacheApi].to[CacheForTesting])
      .build

  protected def doLogin(email: String = Global.DefaultUserEmail, password: String = "testuser") = {
    val request = Json.obj("email" -> email, "password" -> password)
    route(app, FakeRequest(POST, "/api/users/login").withJsonBody(request)).fold {
      cancel("login failed")
    } { response =>
      status(response) mustBe (OK)
      contentType(response) mustBe Some("application/json")
      val json = Json.parse(contentAsString(response))

      (json \ "data" \ "email").as[String] must be (email)

      val cookies = Await.result(response, defaultAwaitTimeout.duration)
        .newCookies.groupBy(_.name).mapValues(_.head)

      cookies.get("XSRF-TOKEN") match {
        case Some(c) => c.value
        case None =>    ""
      }
    }
  }

  override def makeRequest(method:         String,
                           path:           String,
                           expectedStatus: Int,
                           json:           JsValue,
                           token:          String): JsValue = {

    makeAuthRequest(BbwebRequest(method, path, json, token)).fold {
      fail("HTTP request returned NONE")
    } { reply =>
      status(reply) match {
        case `expectedStatus` =>
          val bodyText = contentAsString(reply)
          if (bodyText.isEmpty) {
            log.debug(s"reply: status: $reply,\nbodyText: EMPTY")
            JsNull
          } else {
            contentType(reply) mustBe Some("application/json")
            val jsonResult = contentAsJson(reply)
            log.debug(s"reply: status: $reply,\nreply: ${Json.prettyPrint(jsonResult)}")
            jsonResult
          }
        case code =>
          contentType(reply) match {
            case Some("application/json") => log.debug("reply: " + Json.prettyPrint(contentAsJson(reply)))
            case _ => log.debug("reply: " + contentAsString(reply))
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

  protected def passwordHasher = app.injector.instanceOf[PasswordHasher]

  protected def accessItemRepository                   = app.injector.instanceOf[AccessItemRepository]
  protected def membershipRepository                   = app.injector.instanceOf[MembershipRepository]

  protected def userRepository                         = app.injector.instanceOf[UserRepository]

  protected def studyRepository                        = app.injector.instanceOf[StudyRepository]
  protected def collectionEventTypeRepository          = app.injector.instanceOf[CollectionEventTypeRepository]
  protected def processingTypeRepository               = app.injector.instanceOf[ProcessingTypeRepository]

  protected def participantRepository                  = app.injector.instanceOf[ParticipantRepository]
  protected def collectionEventRepository              = app.injector.instanceOf[CollectionEventRepository]
  protected def ceventSpecimenRepository               = app.injector.instanceOf[CeventSpecimenRepository]
  protected def specimenRepository                     = app.injector.instanceOf[SpecimenRepository]
  protected def processingEventInputSpecimenRepository = app.injector.instanceOf[ProcessingEventInputSpecimenRepository]


  protected def centreRepository                       = app.injector.instanceOf[CentreRepository]
  protected def shipmentRepository                     = app.injector.instanceOf[ShipmentRepository]
  protected def shipmentSpecimenRepository             = app.injector.instanceOf[ShipmentSpecimenRepository]

  protected def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit = {
    entity match {
      case e: AccessItem          => accessItemRepository.put(e)
      case e: Membership          => membershipRepository.put(e)
      case e: User                => userRepository.put(e)
      case e: Study               => studyRepository.put(e)
      case e: Centre              => centreRepository.put(e)
      case e: CollectionEventType => collectionEventTypeRepository.put(e)
      case e: ProcessingType      => processingTypeRepository.put(e)
      case e: Participant         => participantRepository.put(e)
      case e: CollectionEvent     => collectionEventRepository.put(e)
      case e: Specimen            => specimenRepository.put(e)
      case e: ShipmentSpecimen    => shipmentSpecimenRepository.put(e)
      case e: Shipment            => shipmentRepository.put(e)
      case _                      => fail("invalid entity")
    }
  }

  protected def badRequest(method:      String = GET,
                           url:         String,
                           json:        JsValue = JsNull,
                           errMsgRegex: String): Unit = {
    val reply = makeRequest(method, url, BAD_REQUEST, json)

    (reply \ "status").as[String] must include ("error")

    (reply \ "message").as[String] must include regex(errMsgRegex)

    ()
  }

  protected def hasInvalidVersion(method: String,
                                  url:    String,
                                  json:   JsValue = JsNull): Unit = {
    val reply = makeRequest(method, url, BAD_REQUEST, json)

    (reply \ "status").as[String] must include ("error")

    (reply \ "message").as[String] must include ("expected version doesn't match current version")

    ()
  }

}

trait HttpReplyMatchers {

  case class BbwebRequest(method: String,
                          path: String,
                          json: JsValue = JsNull,
                          token: String = "bbweb-test-token")

  class NotFoundMessageMatcher(errMessage: Regex) extends Matcher[BbwebRequest] {
    override def apply(left: BbwebRequest): MatchResult = {
      makeAuthRequest(left).fold {
        MatchResult(false, "request is invalid", "")
      } { reply =>
        val statusCode = status(reply)
        statusCode match {
          case NOT_FOUND =>
            val bodyText = contentAsString(reply)
            if (bodyText.isEmpty) {
              MatchResult(false, "request has an empty body", "")
            } else {
              contentType(reply) match {
                case Some("application/json") =>
                  val json = contentAsJson(reply)
                  val status = (json \ "status").as[String]
                  val message = (json \ "message").as[String]

                  val found = errMessage.findFirstIn(message) match {
                      case Some(m) => true
                      case None    => false
                    }

                  log.debug(s"reply: status: $status,\nbody: ${Json.prettyPrint(json)}")

                  MatchResult(
                    status.equals("error") && found,
                    s"reply did not match: status: $status, message: $message, expectedMessage: $errMessage",
                    s"reply matched: status: $status, message: $message, expectedMessage: $errMessage"
                  )

                case _ =>
                  MatchResult(false, "request reply body is not in JSON format", "")
              }
            }

          case _ =>
            MatchResult(false, s"request status code invalid: $statusCode", "")

        }
      }
    }
  }

  protected val log: Logger = LoggerFactory.getLogger(this.getClass)

  implicit def app: Application

  def beNotFoundWithMessage(errMessage: Regex) = new NotFoundMessageMatcher(errMessage)

  def makeAuthRequest(request: BbwebRequest): Option[Future[Result]] = {
    val cookie = Cookie("XSRF-TOKEN", request.token)
    val fakeRequest = FakeRequest(request.method, request.path)
      .withJsonBody(request.json)
      .withHeaders("X-XSRF-TOKEN" -> request.token,
                   "Set-Cookie"   -> Cookies.encodeCookieHeader(Seq(cookie)))
      .withCookies(cookie)

    if (request.json != JsNull) {
      log.debug(s"request: ${request.method}, ${request.path},\n${Json.prettyPrint(request.json)}")
    } else {
      log.debug(s"request: ${request.method}, ${request.path}")
    }

    route(app, fakeRequest)
  }

}
