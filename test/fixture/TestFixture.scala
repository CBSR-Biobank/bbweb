package fixture

import domain._

import scala.concurrent.Await
import scala.concurrent.Future
import akka.testkit.TestKitBase
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import akka.testkit.ImplicitSender

trait TestFixture
    extends TestComponentImpl
    with ScalaFutures
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.shutdown
  }

  def await[T](f: Future[T]): T = {
    // use blocking for now so that tests can be run in parallel
    Await.result(f, timeout.duration)
  }
}
