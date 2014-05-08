package org.biobank.controllers

import org.biobank.service.TopComponentImpl
import play.api.Play.current
import play.libs.Akka
import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.ConfigFactory

object ApplicationComponent extends TopComponentImpl {
  implicit override val system: akka.actor.ActorSystem = Akka.system

  override val studyProcessor = system.actorOf(Props(new StudyProcessor), "studyproc")
  override val userProcessor = system.actorOf(Props(new UserProcessor), "userproc")

  override val studyService = new StudyServiceImpl(studyProcessor)
  override val userService = new UserService(userProcessor)
}
