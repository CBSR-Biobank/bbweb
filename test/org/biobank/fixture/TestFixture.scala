package org.biobank.fixture

import org.biobank.domain._
import org.biobank.domain.study._
import org.biobank.domain.participants._
import org.biobank.domain.centre._
import org.biobank.domain.user._
import org.biobank.domain.user.UserId
import org.biobank.service._
//import org.biobank.query._

import akka.actor.ActorRef
import akka.actor._
import akka.util.Timeout
import javax.inject.{ Inject, Named }
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time._
import play.api.inject.guice.GuiceApplicationBuilder
import scala.concurrent.duration._

/**
 * Test fixture to make it easier to write specifications.
 */
trait TestFixture
    extends WordSpecLike
    with ScalaFutures
    with MustMatchers
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with TestDbConfiguration {

  val app = new GuiceApplicationBuilder().build

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
    //Await.result(system.terminate(), 10 seconds)
    ()
  }

  val passwordHasher = app.injector.instanceOf[PasswordHasher]

  val collectionEventTypeRepository            = app.injector.instanceOf[CollectionEventTypeRepository]
  val processingTypeRepository                 = app.injector.instanceOf[ProcessingTypeRepository]
  val specimenGroupRepository                  = app.injector.instanceOf[SpecimenGroupRepository]
  val specimenLinkTypeRepository               = app.injector.instanceOf[SpecimenLinkTypeRepository]
  val studyRepository                          = app.injector.instanceOf[StudyRepository]

  val participantRepository                    = app.injector.instanceOf[ParticipantRepository]
  val collectionEventRepository                = app.injector.instanceOf[CollectionEventRepository]
  val ceventSpecimenRepository                 = app.injector.instanceOf[CeventSpecimenRepository]

  val userRepository = app.injector.instanceOf[UserRepository]

  val centreRepository          = app.injector.instanceOf[CentreRepository]

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

}

case class NamedUsersProcessor @Inject() (@Named("usersProcessor") processor: ActorRef)

case class NamedCentresProcessor @Inject() (@Named("centresProcessor") processor: ActorRef)

case class NamedShipmentsProcessor @Inject() (@Named("shipmentsProcessor") processor: ActorRef)

case class NamedCollectionEventTypeProcessor @Inject()        (
  @Named("collectionEventType") processor: ActorRef)

case class NamedProcessingTypeProcessor @Inject() (
  @Named("processingType") processor: ActorRef)

case class NamedSpecimenLinkTypeProcessor @Inject() (
  @Named("specimenLinkType") processor: ActorRef)

case class NamedStudiesProcessor @Inject() (
  @Named("studiesProcessor") processor: ActorRef)
