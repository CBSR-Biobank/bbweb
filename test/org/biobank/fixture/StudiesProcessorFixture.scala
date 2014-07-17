package org.biobank.fixture

import org.biobank.service._
import org.biobank.service.study._
import org.biobank.domain.RepositoriesComponentImpl

import akka.actor.Props
import akka.util.Timeout

/**
 * Used to test the study service.
 */
trait StudiesProcessorFixture extends TestFixture {

  override val studiesProcessor = system.actorOf(Props(new StudiesProcessor), "studyproc")
  override val centresProcessor = null
  override val usersProcessor = null

  override val studiesService = null
  override val centresService = null
  override val usersService = null
}
