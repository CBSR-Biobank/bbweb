package fixture

import akka.testkit.TestKitBase
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import akka.testkit.ImplicitSender

trait TestFixture
  extends TestComponentImpl
  with TestKitBase
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.shutdown()
  }

}