package org.biobank.controllers

import org.biobank.service.TopComponentImpl
import play.api.Play.current
import play.libs.Akka
import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.ConfigFactory

object ApplicationComponent extends TopComponentImpl {
  //implicit override val system: akka.actor.ActorSystem = Akka.system

  implicit override val system: ActorSystem = ActorSystem("bbweb-test", ApplcationComponent.config())

  override val studyProcessor = system.actorOf(Props(new StudyProcessor), "studyproc")
  override val userProcessor = system.actorOf(Props(new UserProcessor), "userproc")

  override val studyService = new StudyServiceImpl(studyProcessor)
  override val userService = new UserService(userProcessor)
}


object ApplcationComponent {

  def config() = ConfigFactory.parseString(
    s"""
      |akka.persistence.journal.plugin = "casbah-journal"
      |akka.persistence.snapshot-store.plugin = "casbah-snapshot-store"
      |akka.persistence.journal.max-deletion-batch-size = 3
      |akka.persistence.publish-plugin-commands = on
      |akka.persistence.publish-confirmations = on
      |casbah-journal.mongo-journal-url = "mongodb://localhost/bbweb.messages"
      |casbah-journal.mongo-journal-write-concern = "acknowledged"
      |casbah-journal.mongo-journal-write-concern-timeout = 10000
      |casbah-snapshot-store.mongo-snapshot-url = "mongodb://localhost/bbweb.snapshots"
      |casbah-snapshot-store.mongo-snapshot-write-concern = "acknowledged"
      |casbah-snapshot-store.mongo-snapshot-write-concern-timeout = 10000
    """.stripMargin)

}
