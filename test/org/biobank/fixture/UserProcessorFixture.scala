package org.biobank.fixture

import org.biobank.service._

import akka.actor.Props
import akka.util.Timeout

trait UsersProcessorFixture extends TestFixture {

  override val studiesProcessor = null
  override val centresProcessor = null
  override val usersProcessor = system.actorOf(Props(new UsersProcessor), "userproc")

  override val studiesService = null
  override val centresService = null
  override val usersService = null

}
