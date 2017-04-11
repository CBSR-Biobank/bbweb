package org.biobank.fixture

import akka.actor.ActorRef
import akka.actor._
import akka.persistence.inmemory.extension.{ InMemoryJournalStorage, InMemorySnapshotStorage, StorageExtension }
import akka.testkit.{TestKit, TestProbe}
import akka.util.Timeout
import javax.inject.{ Inject, Named }
import org.biobank.controllers.FixedEhCache
import org.biobank.domain._
import org.biobank.domain.centre._
import org.biobank.domain.participants._
import org.biobank.domain.study._
import org.biobank.domain.user.UserId
import org.biobank.domain.user._
import org.biobank.service._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time._
import play.api.cache.{ CacheApi /* , EhCacheModule */ }
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import scala.concurrent.duration._

trait InMemoryCleanup extends BeforeAndAfterEach { _: Suite =>

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

/**
 * Test fixture to make it easier to write specifications.
 */
trait TestFixture
    extends WordSpecLike
    with ScalaFutures
    with MustMatchers
    with InMemoryCleanup
    with BeforeAndAfterAll
    with MockitoSugar
    with TestDbConfiguration {

  val snapshotWriterMock = mock[SnapshotWriter]

  val app = new GuiceApplicationBuilder()
    .overrides(bind[CacheApi].to[FixedEhCache])
    .overrides(bind[SnapshotWriter].toInstance(snapshotWriterMock))
    .build

  implicit val system = ActorSystem("bbweb-test", TestDbConfiguration.config())

  implicit val timeout: Timeout = 5.seconds

  val factory = new Factory

  // need to configure scalatest to have more patience when waiting for future results
  implicit val defaultPatience =
    PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))

  val defaultUserIdOpt = Some(UserId("testuser"))

  override def beforeAll: Unit = {
  }

  /**
   * Shuts down the actor system.
   */
  override def afterAll: Unit = {
    // Cleanup
    TestKit.shutdownActorSystem(system)
  }

  val passwordHasher = app.injector.instanceOf[PasswordHasher]

  val collectionEventTypeRepository            = app.injector.instanceOf[CollectionEventTypeRepository]
  val processingTypeRepository                 = app.injector.instanceOf[ProcessingTypeRepository]
  val specimenGroupRepository                  = app.injector.instanceOf[SpecimenGroupRepository]
  val specimenLinkTypeRepository               = app.injector.instanceOf[SpecimenLinkTypeRepository]
  val studyRepository                          = app.injector.instanceOf[StudyRepository]

  val participantRepository                    = app.injector.instanceOf[ParticipantRepository]
  val collectionEventRepository                = app.injector.instanceOf[CollectionEventRepository]
  val specimenRepository                       = app.injector.instanceOf[SpecimenRepository]
  val ceventSpecimenRepository                 = app.injector.instanceOf[CeventSpecimenRepository]

  val userRepository = app.injector.instanceOf[UserRepository]

  val centreRepository = app.injector.instanceOf[CentreRepository]
  val shipmentRepository = app.injector.instanceOf[ShipmentRepository]

  val usersProcessor = app.injector.instanceOf[NamedUsersProcessor].processor

  val centresProcessor =
    app.injector.instanceOf[NamedCentresProcessor].processor

  val collectionEventTypeProcessor =
    app.injector.instanceOf[NamedCollectionEventTypeProcessor].processor

  val processingTypeProcessor =
    app.injector.instanceOf[NamedProcessingTypeProcessor].processor

  val specimenLinkTypeProcessor =
    app.injector.instanceOf[NamedSpecimenLinkTypeProcessor].processor

  val studiesProcessor = app.injector.instanceOf[NamedStudiesProcessor].processor

  //val studyPersistenceQuery =  app.injector.instanceOf[StudyPersistenceQuery]

  val specimensProcessor = app.injector.instanceOf[NamedSpecimensProcessor].processor
  val participantsProcessor = app.injector.instanceOf[NamedParticipantsProcessor].processor
  val collectionEventsProcessor = app.injector.instanceOf[NamedCollectionEventsProcessor].processor

  val shipmentsProcessor = app.injector.instanceOf[NamedShipmentsProcessor].processor
}

case class NamedUsersProcessor @Inject() (@Named("usersProcessor") processor: ActorRef)

case class NamedCentresProcessor @Inject() (@Named("centresProcessor") processor: ActorRef)
case class NamedShipmentsProcessor @Inject() (@Named("shipmentsProcessor") processor: ActorRef)

case class NamedParticipantsProcessor @Inject() (@Named("participantsProcessor") processor: ActorRef)
case class NamedCollectionEventsProcessor @Inject() (@Named("collectionEventsProcessor") processor: ActorRef)
case class NamedSpecimensProcessor @Inject() (@Named("specimensProcessor") processor: ActorRef)

case class NamedCollectionEventTypeProcessor @Inject() (@Named("collectionEventType") processor: ActorRef)
case class NamedProcessingTypeProcessor @Inject() (@Named("processingType") processor: ActorRef)
case class NamedSpecimenLinkTypeProcessor @Inject() (@Named("specimenLinkType") processor: ActorRef)
case class NamedStudiesProcessor @Inject() (@Named("studiesProcessor") processor: ActorRef)
