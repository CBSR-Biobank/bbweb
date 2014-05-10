package org.biobank.fixture

import org.biobank.controllers.BbwebPlugin
import org.biobank.domain.study.{ Study, StudyId }
import org.biobank.domain.{ FactoryComponent, RepositoryComponentImpl, ReadWriteRepository }

import org.scalatest.FunSpec
import org.scalatest.BeforeAndAfter
import org.scalatest.Matchers
import play.api.Play
import play.api.test.FakeApplication
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test.FakeRequest
import com.mongodb.casbah.Imports._
import play.api.Logger

/** This trait allows a test suite to run tests on a Play Framework fake application.
  *
  * It include a [[FactoryComponent]] to make the creation of domain model objects easier. It uses
  * the [[https://github.com/ddevore/akka-persistence-mongo/ Mongo Journal for Akka Persistence]]
  * to make it easier to drop all items in the database prior to running a test suite.
  */
trait ControllerFixture
    extends FunSpec
    with Matchers
    with FactoryComponent
    with RepositoryComponentImpl {

  private val dbName = "bbweb-test"

  // ensure the database is empty
  MongoConnection()(dbName)("messages").drop
  MongoConnection()(dbName)("snapshots").drop

  protected def fakeApplication = {
    FakeApplication(withoutPlugins = List("com.typesafe.plugin.CommonsMailerPlugin"))
  }

  def makeJsonRequest(method: String, path: String, expectedStatus: Int = OK, json: JsValue = JsNull): JsValue = {
    val result = route(FakeRequest(method, path).withJsonBody(json)).get
    status(result) should be (expectedStatus)
    contentType(result) should be (Some("application/json"))
    Logger.info(s"makeJsonRequest: result: ${contentAsString(result)}")
    Json.parse(contentAsString(result))
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

  }

}

