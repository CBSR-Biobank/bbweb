package org.biobank.fixture

import akka.actor.ActorSystem
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import scala.language.postfixOps

trait TestDbConfiguration {

}

object TestDbConfiguration {

  def config() = ConfigFactory.parseString(
    s"""
      |akka.persistence.journal.plugin = "inmemory-journal"
      |akka.persistence.snapshot-store.plugin = "inmemory-snapshot-store"
    """.stripMargin)


}
