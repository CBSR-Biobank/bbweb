package org.biobank.fixture

import org.biobank.service._

import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.util.Timeout
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import com.mongodb.casbah.Imports._
import scala.language.postfixOps

trait TestComponentImpl extends TopComponent with ServicesComponentImpl {

  implicit val system: ActorSystem = ActorSystem("bbweb-test", TestComponentImpl.config())
  implicit val timeout = Timeout(5 seconds)

  // clear the event store
  MongoConnection()(TestComponentImpl.dbName)("messages").drop
  MongoConnection()(TestComponentImpl.dbName)("snapshots").drop

}

object TestComponentImpl {

  val dbName = "bbweb-test"

  def config() = ConfigFactory.parseString(
    s"""
      |akka.persistence.journal.plugin = "casbah-journal"
      |akka.persistence.snapshot-store.plugin = "casbah-snapshot-store"
      |akka.persistence.journal.max-deletion-batch-size = 3
      |akka.persistence.publish-plugin-commands = on
      |akka.persistence.publish-confirmations = on
      |casbah-journal.mongo-journal-url = "mongodb://localhost/$dbName.messages"
      |casbah-journal.mongo-journal-write-concern = "acknowledged"
      |casbah-journal.mongo-journal-write-concern-timeout = 10000
      |casbah-snapshot-store.mongo-snapshot-url = "mongodb://localhost/$dbName.snapshots"
      |casbah-snapshot-store.mongo-snapshot-write-concern = "acknowledged"
      |casbah-snapshot-store.mongo-snapshot-write-concern-timeout = 10000
    """.stripMargin)

}
