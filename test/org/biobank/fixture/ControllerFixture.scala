package org.biobank.fixture

import org.biobank.domain._
import org.biobank.domain.user.{ UserRepository, UserRepositoryImpl }
import org.biobank.domain.centre._
import org.biobank.domain.study._
import org.biobank.service.{
  PasswordHasher,
  PasswordHasherImpl
}
import org.biobank.modules._

import com.mongodb.casbah.Imports._
import org.scalatest._
import org.scalatestplus.play._
import play.api.Logger
import play.api.Play
import play.api.libs.json._
import play.api.mvc.Cookie
import play.api.test.FakeApplication
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scaldi.Module

/**
  * This trait allows a test suite to run tests on a Play Framework fake application.
  *
  * It include a [[FactoryComponent]] to make the creation of domain model objects easier. It uses
  * the [[https://github.com/ddevore/akka-persistence-mongo/ Mongo Journal for Akka Persistence]]
  * to make it easier to drop all items in the database prior to running a test suite.
  */
trait ControllerFixture
    extends fixture.WordSpec
    with MustMatchers
    with OptionValues
    with BeforeAndAfterEach
    with MixedFixtures {

  private val dbName = "bbweb-test"

  /** Overrides the injected dependencies and provides access to those dependecies so they can be used
    * in tests.
    */
  object TestGlobal extends org.biobank.Global {

    override def applicationModule = new TestModule ++ new WebModule ++ new UserModule

    def passwordHasher = inject [PasswordHasher]

    def collectionEventAnnotationTypeRepository  = inject [CollectionEventAnnotationTypeRepository]
    def collectionEventTypeRepository            = inject [CollectionEventTypeRepository]
    def participantAnnotationTypeRepository      = inject [ParticipantAnnotationTypeRepository]
    def processingTypeRepository                 = inject [ProcessingTypeRepository]
    def specimenGroupRepository                  = inject [SpecimenGroupRepository]
    def specimenLinkAnnotationTypeRepository     = inject [SpecimenLinkAnnotationTypeRepository]
    def specimenLinkTypeRepository               = inject [SpecimenLinkTypeRepository]
    def studyRepository                          = inject [StudyRepository]
    def participantRepository                    = inject [ParticipantRepository]

    def userRepository = inject [UserRepository]

    def centreRepository = inject [CentreRepository]

    def centreLocationsRepository = inject [CentreLocationsRepository]

    def centreStudiesRepository = inject [CentreStudiesRepository]

    def locationRepository = inject [LocationRepository]

  }

  var adminToken: String = ""

  val factory = new Factory

  /**
   * tests will not work with EhCache, need alternate implementation
   *
   * See FixedEhCachePlugin.
    */
  def fakeApp: FakeApplication =
    FakeApplication(
      withGlobal = Some(TestGlobal),
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
        status(result) mustBe OK
        contentType(result) mustBe Some("application/json")
        val json = Json.parse(contentAsString(result))
        adminToken = (json \ "data").as[String]
        adminToken
      case _ =>
        cancel("login failed")
    }
  }

  def makeRequest(
    method: String,
    path: String,
    expectedStatus: Int = OK,
    json: JsValue = JsNull,
    token: String = adminToken)
      : JsValue = {
    var fakeRequest = FakeRequest(method, path)
      .withJsonBody(json)
      .withHeaders("X-XSRF-TOKEN" -> token)
      .withCookies(Cookie("XSRF-TOKEN", token))
    Logger.info(s"makeRequest: request: $fakeRequest, $json")
    route(fakeRequest).fold {
      fail("HTTP request returned NONE")
    } { result =>
      Logger.info(s"makeRequest: status: ${status(result)}, result: ${contentAsString(result)}")
      status(result) mustBe(expectedStatus)
      contentType(result) mustBe(Some("application/json"))
      Json.parse(contentAsString(result))
    }
  }


}

