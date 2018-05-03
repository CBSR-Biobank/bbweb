package org.biobank.fixtures

import akka.actor._
import akka.testkit.{TestKit, TestProbe}
import akka.persistence.inmemory.extension.{ InMemoryJournalStorage, InMemorySnapshotStorage, StorageExtension }
import org.biobank.controllers.CacheForTesting
import org.biobank.services.SnapshotWriter
import org.scalatest._
import org.scalatest.time._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.cache.{AsyncCacheApi, DefaultSyncCacheApi, SyncCacheApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.DefaultAwaitTimeout

trait ProcessorTestFixture
    extends TestFixture
    with ScalaFutures
    with BeforeAndAfterAll
    with MockitoSugar
    with DefaultAwaitTimeout {

  val snapshotWriterMock = mock[SnapshotWriter]

  override val app = new GuiceApplicationBuilder()
    .overrides(bind[SyncCacheApi].to[DefaultSyncCacheApi])
    .overrides(bind[AsyncCacheApi].to[CacheForTesting])
    .overrides(bind[SnapshotWriter].toInstance(snapshotWriterMock))
    .build

  implicit val system: ActorSystem = app.injector.instanceOf[ActorSystem]

  // need to configure scalatest to have more patience when waiting for future results
  implicit val defaultPatience =
    PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))

  /**
   * Shuts down the actor system.
   */
  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

}

trait InMemoryPersistenceCleanup extends BeforeAndAfterEach { _: Suite =>

  implicit val system: ActorSystem

  override protected def beforeEach(): Unit = {
    val tp = TestProbe()
    tp.send(StorageExtension(system).journalStorage, InMemoryJournalStorage.ClearJournal)
    tp.expectMsg(akka.actor.Status.Success(""))
    tp.send(StorageExtension(system).snapshotStorage, InMemorySnapshotStorage.ClearSnapshots)
    tp.expectMsg(akka.actor.Status.Success(""))
    super.beforeEach()
  }
}
