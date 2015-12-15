package org.biobank.fixture

import akka.actor.ActorSystem
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import scala.language.postfixOps

trait TestDbConfiguration {

  // clear the event store
  //MongoConnection()(TestDbConfiguration.dbName)("messages").drop
  //MongoConnection()(TestDbConfiguration.dbName)("snapshots").drop

}

object TestDbConfiguration {

  val dbName = "bbweb-test"

  def config() = ConfigFactory.parseString(
    s"""
      |akka.persistence.journal.leveldb.dir = "target/bbweb/journal-test"
      |akka.persistence.snapshot-store.local.dir = "target/bbweb/snapshots-test"
      |akka.persistence.journal.leveldb.native = true
    """.stripMargin)

    // def config() = ConfigFactory.parseString(
    // s"""
    //   |akka.persistence.journal.plugin = "akka-contrib-mongodb-persistence-journal"
    //   |akka.persistence.snapshot-store.plugin = "akka-contrib-mongodb-persistence-snapshot"
    //   |akka.persistence.journal.max-deletion-batch-size = 3
    //   |akka.persistence.publish-plugin-commands = on
    //   |akka.persistence.publish-confirmations = on
    //   |casbah-journal.mongo-journal-url = "mongodb://localhost/$dbName.messages"
    //   |casbah-journal.mongo-journal-write-concern = "acknowledged"
    //   |casbah-journal.mongo-journal-write-concern-timeout = 10000
    //   |casbah-snapshot-store.mongo-snapshot-url = "mongodb://localhost/$dbName.snapshots"
    //   |casbah-snapshot-store.mongo-snapshot-write-concern = "acknowledged"
    //   |casbah-snapshot-store.mongo-snapshot-write-concern-timeout = 10000
    // """.stripMargin)


}
