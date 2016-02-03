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
import akka.pattern.AskSupport
import akka.util.Timeout
import javax.inject.{ Inject, Named }
import org.scalatest._
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time._
import org.slf4j.LoggerFactory
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

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

  private def dummyUserIdOpt = Some(UserId("dummy_user_id"))

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

  val collectionEventAnnotationTypeRepository  = app.injector.instanceOf[CollectionEventAnnotationTypeRepository]
  val collectionEventTypeRepository            = app.injector.instanceOf[CollectionEventTypeRepository]
  val participantAnnotationTypeRepository      = app.injector.instanceOf[ParticipantAnnotationTypeRepository]
  val processingTypeRepository                 = app.injector.instanceOf[ProcessingTypeRepository]
  val specimenGroupRepository                  = app.injector.instanceOf[SpecimenGroupRepository]
  val specimenLinkAnnotationTypeRepository     = app.injector.instanceOf[SpecimenLinkAnnotationTypeRepository]
  val specimenLinkTypeRepository               = app.injector.instanceOf[SpecimenLinkTypeRepository]
  val studyRepository                          = app.injector.instanceOf[StudyRepository]

  val participantRepository                    = app.injector.instanceOf[ParticipantRepository]
  val collectionEventRepository                = app.injector.instanceOf[CollectionEventRepository]
  val ceventSpecimenRepository                 = app.injector.instanceOf[CeventSpecimenRepository]

  val userRepository = app.injector.instanceOf[UserRepository]

  val centreRepository          = app.injector.instanceOf[CentreRepository]
  val centreLocationsRepository = app.injector.instanceOf[CentreLocationsRepository]
  val centreStudiesRepository   = app.injector.instanceOf[CentreStudiesRepository]
  val locationRepository        = app.injector.instanceOf[LocationRepository]

  val usersProcessor = app.injector.instanceOf[NamedUsersProcessor].processor

  val centresProcessor =
    app.injector.instanceOf[NamedCentresProcessor].processor

  val collectionEventTypeProcessor =
    app.injector.instanceOf[NamedCollectionEventTypeProcessor].processor

  val ceventAnnotationTypeProcessor =
    app.injector.instanceOf[NamedCeventAnnotationTypeProcessor].processor

  val participantAnnotationTypeProcessor =
    app.injector.instanceOf[NamedParticipantAnnotationTypeProcessor].processor

  val processingTypeProcessor =
    app.injector.instanceOf[NamedProcessingTypeProcessor].processor

  val specimenGroupProcessor =
    app.injector.instanceOf[NamedSpecimenGroupProcessor].processor

  val specimenLinkAnnotationTypeProcessor =
    app.injector.instanceOf[NamedSpecimenLinkAnnotationTypeProcessor].processor

  val specimenLinkTypeProcessor =
    app.injector.instanceOf[NamedSpecimenLinkTypeProcessor].processor

  val studiesProcessor = app.injector.instanceOf[NamedStudiesProcessor].processor

  //val studyPersistenceQuery =  app.injector.instanceOf[StudyPersistenceQuery]

}

case class NamedUsersProcessor @Inject() (@Named("usersProcessor") processor: ActorRef)

case class NamedCentresProcessor @Inject() (@Named("centresProcessor") processor: ActorRef)

case class NamedCeventAnnotationTypeProcessor @Inject() (
  @Named("collectionEventType") processor: ActorRef)

case class NamedCollectionEventTypeProcessor @Inject()        (
  @Named("collectionEventType") processor: ActorRef)

case class NamedParticipantAnnotationTypeProcessor  @Inject() (
  @Named("participantAnnotationType") processor: ActorRef)

case class NamedProcessingTypeProcessor @Inject() (
  @Named("processingType") processor: ActorRef)

case class NamedSpecimenGroupProcessor @Inject() (
  @Named("specimenGroup") processor: ActorRef)

case class NamedSpecimenLinkAnnotationTypeProcessor @Inject() (
  @Named("specimenLinkAnnotationType") processor: ActorRef)

case class NamedSpecimenLinkTypeProcessor @Inject() (
  @Named("specimenLinkType") processor: ActorRef)

case class NamedStudiesProcessor @Inject() (
  @Named("studiesProcessor") processor: ActorRef)
