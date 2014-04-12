package fixture

import org.biobank.service._

import akka.actor.Props

trait UserProcessorFixture extends TestFixture {

  override val studyProcessor = null
  override val userProcessor = system.actorOf(Props(new UserProcessorImpl), "userproc")

  override val studyService = null
  override val userService = null

}