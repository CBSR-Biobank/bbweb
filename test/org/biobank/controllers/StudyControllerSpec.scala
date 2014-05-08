package org.biobank.controllers

import org.biobank.domain.FactoryComponent
import org.biobank.domain.RepositoryComponentImpl
import org.biobank.service.ServiceComponentImpl
import org.biobank.service.json.Study._
import akka.actor.Props
import org.scalatest.FunSpec
import org.scalatest.GivenWhenThen
import org.scalatest.Matchers
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.test.FakeApplication
import play.api.libs.json._
import play.api.Logger
import com.mongodb.casbah.Imports._
import akka.util.Timeout
import org.biobank.service.TopComponent
import play.libs.Akka

object FactoryComponentImpl extends RepositoryComponentImpl with FactoryComponent

/**
  *
  * Note need to pass timeout to status if not compiler complains about ambiguous implicit values.
  */
class StudyControllerSpec
    extends FunSpec
    with Matchers {

  val dbName = "bbweb-test"

  // override the database settings
  val akkaPersistenceConfig = Map(
    "akka.persistence.journal.plugin"          -> "casbah-journal",
    "akka.persistence.snapshot-store.plugin"   -> "casbah-snapshot-store",
    "casbah-journal.mongo-journal-url"         -> s"mongodb://localhost/$dbName.messages",
    "casbah-snapshot-store.mongo-snapshot-url" -> s"mongodb://localhost/$dbName.snapshots"
  )

  // ensure the database is empty
  MongoConnection()(dbName)("messages").drop
  MongoConnection()(dbName)("snapshots").drop

  def fakeApplication = FakeApplication(
    withoutPlugins = List("com.typesafe.plugin.CommonsMailerPlugin"),
    additionalConfiguration = akkaPersistenceConfig
  )

  describe("Study REST API") {
    describe("GET /studies") {
      it("should list no studies") {
        running(fakeApplication) {
          val result = route(FakeRequest(GET, "/studies")).get
          status(result) should be (OK)
          contentType(result) should be (Some("application/json"))
          contentAsString(result) should include ("[]")
        }
      }
    }

    describe("GET /studies") {
      it("should list a study") {
        running(fakeApplication) {
          val study = FactoryComponentImpl.factory.createDisabledStudy
          ApplicationComponent.studyRepository.put(study)

          val result = route(FakeRequest(GET, "/studies")).get
          status(result) should be (OK)
          contentType(result) should be (Some("application/json"))
          val expectedJson = Json.toJson(study)
          val actualJson = Json.parse(contentAsString(result))
          assert(actualJson \ "id" === expectedJson \ "id")
        }
      }
    }

  }

}
