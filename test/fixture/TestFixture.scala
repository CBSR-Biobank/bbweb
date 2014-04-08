package fixture

import domain._

import scala.concurrent.Await
import scala.concurrent.Future
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
    shutdown(system)
  }

  def await[T](f: Future[T]): T = {
    // use blocking for now so that tests can be run in parallel
    Await.result(f, timeout.duration)
  }
}