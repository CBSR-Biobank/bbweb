package org.biobank.fixture

import org.biobank.domain._

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Test fixture to make it easier to write specifications.
 */
trait TestFixture
    extends WordSpec
    with TestComponentImpl
    with ScalaFutures
    with MustMatchers
    with BeforeAndAfterEach
    with BeforeAndAfterAll {

  val factory = new Factory

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
