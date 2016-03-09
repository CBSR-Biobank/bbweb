package org.biobank.fixture

import com.typesafe.config.ConfigFactory

trait TestDbConfiguration {

}

object TestDbConfiguration {

  def config() = ConfigFactory.parseString(
    s"""
      |akka.persistence.journal.plugin = "inmemory-journal"
      |akka.persistence.snapshot-store.plugin = "inmemory-snapshot-store"
    """.stripMargin)


}
