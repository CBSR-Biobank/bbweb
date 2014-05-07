package org.biobank.fixture

import org.biobank.domain._

import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.Span
import org.scalatest.time.Seconds
import org.scalatest.time.Millis
import scala.concurrent.duration._
import scala.language.postfixOps
import org.scalatest.time._

/**
 * Test fixture to make it easier to write specifications.
 */
trait TestFixture
    extends TestComponentImpl
    with FactoryComponent
    with ScalaFutures
    with WordSpecLike
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach {

  // need to configure scalatest to have more patience when waiting for future results
  implicit val defaultPatience =
    PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))

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

}
