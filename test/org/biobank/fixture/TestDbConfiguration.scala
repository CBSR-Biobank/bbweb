package org.biobank.fixture

import org.biobank.service._

import akka.actor.ActorSystem
import akka.util.Timeout
import com.mongodb.casbah.Imports._
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import scala.language.postfixOps
import scaldi.Module
import scaldi.akka.AkkaInjectable
import scaldi.MutableInjectorAggregation

trait TestDbConfiguration {

  // clear the event store
  MongoConnection()(TestDbConfiguration.dbName)("messages").drop
  MongoConnection()(TestDbConfiguration.dbName)("snapshots").drop

}

object TestDbConfiguration {

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
