package org.biobank.fixture

import org.biobank.service._
import org.biobank.service.study._

import akka.actor.Props
import akka.util.Timeout

/**
 * Used to test the study service.
 */
trait StudyProcessorFixture extends TestFixture {

  override val studyProcessor = system.actorOf(Props(new StudyProcessorImpl), "studyproc")
  override val userProcessor = null

  override val studyService = null
  override val userService = null
}
