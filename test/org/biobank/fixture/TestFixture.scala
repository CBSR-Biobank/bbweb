package org.biobank.fixture

import org.biobank.domain._
import org.biobank.domain.user.UserId
import org.biobank.service.WrappedCommand
import org.biobank.infrastructure.command.Commands._
import org.biobank.infrastructure.event.Events._

import akka.actor.ActorSystem
import akka.util.Timeout
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import akka.pattern.AskSupport
import akka.actor.ActorRef
import scaldi.Module
import scaldi.akka.AkkaInjectable
import scaldi.MutableInjectorAggregation

/**
 * Test fixture to make it easier to write specifications.
 */
trait TestFixture
    extends WordSpec
    with ScalaFutures
    with MustMatchers
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with TestDbConfiguration
    with AkkaInjectable {

  implicit val appModule = new TestModule

  implicit val system = inject [ActorSystem]

  implicit val timeout = inject [Timeout] ('akkaTimeout)

  private def dummyUserIdOpt = Some(UserId("dummy_user_id"))

  val factory = new Factory

  // need to configure scalatest to have more patience when waiting for future results
  implicit val defaultPatience =
    PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))

  val defaultUserIdOpt = Some(UserId("testuser"))

  def ask[T <: Command](processor: ActorRef, command: T, userIdOpt: Option[UserId]): Future[_] = {
    akka.pattern.ask(processor, WrappedCommand(command, userIdOpt))
  }

  def ask[T <: Command](processor: ActorRef, command: T): Future[_] = {
    ask(processor, command, dummyUserIdOpt)
  }

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
