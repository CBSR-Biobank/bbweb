package org.biobank.fixture

import org.biobank.controllers.BbwebPlugin
import org.biobank.domain.study.{ Study, StudyId }
import org.biobank.domain.{ FactoryComponent, RepositoryComponentImpl, ReadWriteRepository }

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
  with BeforeAndAfterEach
  with FactoryComponent
  with RepositoryComponentImpl {

  private val dbName = "bbweb-test"

  var token: String = ""

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
    val request = Json.obj("email" -> "admin@admin.com", "password" -> "password")
    route(FakeRequest(POST, "/login").withJsonBody(request)) match {
      case Some(result) =>
        status(result) should be(OK)
        contentType(result) should be(Some("application/json"))
        val json = Json.parse(contentAsString(result))
        token = (json \ "token").as[String]
      case _ =>
        assert(false)
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
    Logger.info(s"makeRequest: request: $fakeRequest")
    route(fakeRequest).fold {
      Json.parse("{ status: KO, message: request returned None")
    } { result =>
      Logger.info(s"makeRequest: result: ${contentAsString(result)}")
      status(result) should be(expectedStatus)
      contentType(result) should be(Some("application/json"))
      Json.parse(contentAsString(result))
    }
  }

  class AppRepositories {

    val plugin = Play.current.plugin[BbwebPlugin]

    val studyRepository = plugin.map(_.studyRepository).getOrElse {
      sys.error("Bbweb plugin is not registered")
    }

    val specimenGroupRepository = plugin.map(_.specimenGroupRepository).getOrElse {
      sys.error("Bbweb plugin is not registered")
    }

    val collectionEventTypeRepository = plugin.map(_.collectionEventTypeRepository).getOrElse {
      sys.error("Bbweb plugin is not registered")
    }

    val collectionEventAnnotationTypeRepository = plugin.map(_.collectionEventAnnotationTypeRepository).getOrElse {
      sys.error("Bbweb plugin is not registered")
    }

    val participantAnnotationTypeRepository = plugin.map(_.participantAnnotationTypeRepository).getOrElse {
      sys.error("Bbweb plugin is not registered")
    }

    val processingTypeRepository = plugin.map(_.processingTypeRepository).getOrElse {
      sys.error("Bbweb plugin is not registered")
    }

    val specimenLinkTypeRepository = plugin.map(_.specimenLinkTypeRepository).getOrElse {
      sys.error("Bbweb plugin is not registered")
    }

    val specimenLinkAnnotationTypeRepository = plugin.map(_.specimenLinkAnnotationTypeRepository).getOrElse {
      sys.error("Bbweb plugin is not registered")
    }

    val userRepository = plugin.map(_.userRepository).getOrElse {
      sys.error("Bbweb plugin is not registered")
    }

  }

}

