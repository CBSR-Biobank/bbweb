package fixture

import service._

import akka.actor._
import org.specs2.mutable._
import org.specs2.time._

trait UserServiceFixture
  extends Specification
  with NoTimeConversions
  with Tags
  with TestComponentImpl {

  private val userProcessor = system.actorOf(Props(new UserProcessorImpl), "userproc")

  override val studyService = null
  override val userService = new UserServiceImpl(userProcessor)

}