package fixture

import service._

import akka.actor._

trait UserProcessorFixture extends TestFixture {

  override val studyProcessor = null
  override val userProcessor = system.actorOf(Props(new UserProcessorImpl), "userproc")

  override val studyService = null
  override val userService = null

}