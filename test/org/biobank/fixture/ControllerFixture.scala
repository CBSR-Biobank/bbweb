package org.biobank.fixture

import org.biobank.domain.FactoryComponent
import org.biobank.domain.RepositoryComponentImpl

import org.scalatest.FunSpec
import org.scalatest.GivenWhenThen
import org.scalatest.Matchers
import play.api.test.FakeApplication
import com.mongodb.casbah.Imports._
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test.FakeRequest

/** This trait allows a test suite to run tests on a Play Framework fake application.
  *
  * It include a [[FactoryComponent]] to make the creation of domain model objects easier. It uses
  * the [[https://github.com/ddevore/akka-persistence-mongo/ Mongo Journal for Akka Persistence]]
  * to make it easier to drop all items in the database prior to running a test suite.
  */
trait ControllerFixture
    extends FunSpec
    with Matchers
    with RepositoryComponentImpl
    with FactoryComponent {

  private val dbName = "bbweb-test"

  // override the database settings
  private val akkaPersistenceConfig = Map(
    "akka.persistence.journal.plugin"          -> "casbah-journal",
    "akka.persistence.snapshot-store.plugin"   -> "casbah-snapshot-store",
    "casbah-journal.mongo-journal-url"         -> s"mongodb://localhost/$dbName.messages",
    "casbah-snapshot-store.mongo-snapshot-url" -> s"mongodb://localhost/$dbName.snapshots"
  )

  // ensure the database is empty
  MongoConnection()(dbName)("messages").drop
  MongoConnection()(dbName)("snapshots").drop

  protected def fakeApplication = FakeApplication(
    withoutPlugins = List("com.typesafe.plugin.CommonsMailerPlugin"),
    additionalConfiguration = akkaPersistenceConfig
  )

  def makeJsonRequest(method: String, path: String): JsValue = {
    val result = route(FakeRequest(method, path)).get
    status(result) should be (OK)
    contentType(result) should be (Some("application/json"))
    Json.parse(contentAsString(result))
  }

}
