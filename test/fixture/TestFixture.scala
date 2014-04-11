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
import scala.concurrent.duration._
import scala.language.postfixOps
import org.scalatest.time._

/**
 * Test fixture to make it easier to write specifications.
 */
trait TestFixture
  extends TestComponentImpl
  with ScalaFutures
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def beforeAll: Unit = {
  }

  /**
   * Shuts down the actor system.
   */
  override def afterAll: Unit = {
    // Cleanup
    system.shutdown()
    system.awaitTermination(10 seconds)
  }

  /**
   * Awaits the completion of a future in a non-blocking fashion. When the future completes
   * {@link fun} is executed. Note that the future may have timed out instead.
   */
  def waitNonBlocking[T, U](future: Future[T])(fun: T => U): U = {
    // use blocking for now so that tests can be run in parallel
    whenReady(future, timeout(timeout.duration))(fun)
  }

  /**
   * Blocks until the future completes or times out.
   */
  def waitBlocking[T](f: Future[T]): T = {
    // use blocking for now so that tests can be run in parallel
    Await.result(f, timeout.duration)
  }
}
