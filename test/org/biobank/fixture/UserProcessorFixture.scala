package org.biobank.fixture

import org.biobank.service._

import akka.actor.Props
import akka.util.Timeout

trait UserProcessorFixture extends TestFixture {
  this: ProcessorComponentImpl =>

  override val studyProcessor = null
  override val userProcessor = system.actorOf(Props(new UserProcessorImpl), "userproc")

  override val studyService = null
  override val userService = null

}
