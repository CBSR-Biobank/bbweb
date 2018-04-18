package org.biobank.services.studies

import akka.actor._
import akka.pattern._
import javax.inject.{ Inject, Named }
import org.biobank.fixture._
import org.biobank.domain.studies._
import org.biobank.services._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import play.api.libs.json._
import scala.concurrent.duration._
import scala.concurrent.Await

final case class NamedStudiesProcessor1 @Inject() (@Named("studiesProcessor") processor: ActorRef)
final case class NamedStudiesProcessor2 @Inject() (@Named("processingType") processor: ActorRef)
final case class NamedStudiesProcessor3 @Inject() (@Named("specimenLinkType") processor: ActorRef)

class StudiesProcessorSpec extends ProcessorTestFixture {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.commands.StudyCommands._
  import org.biobank.infrastructure.events.StudyEvents._

  private var studiesProcessor = app.injector.instanceOf[NamedStudiesProcessor1].processor
  private val processingTypeProcessor = app.injector.instanceOf[NamedStudiesProcessor2].processor
  private val specimenLinkTypeProcessor = app.injector.instanceOf[NamedStudiesProcessor3].processor

  private val studyRepository = app.injector.instanceOf[StudyRepository]

  override def beforeEach() {
    studyRepository.removeAll
    super.beforeEach()
  }

  private def restartProcessor(processor: ActorRef) = {
    val stopped = gracefulStop(processor, 5 seconds, PoisonPill)
    Await.result(stopped, 6 seconds)

    val actor = system.actorOf(Props(new StudiesProcessor(
                                       processingTypeProcessor,
                                       specimenLinkTypeProcessor,
                                       studyRepository,
                                       app.injector.instanceOf[ProcessingTypeRepository],
                                       app.injector.instanceOf[SpecimenGroupRepository],
                                       app.injector.instanceOf[CollectionEventTypeRepository],
                                       app.injector.instanceOf[SnapshotWriter])),
                               "studies")
    Thread.sleep(250)
    actor
  }

  describe("A studies processor must") {

    it("allow recovery from journal", PersistenceTest) {
      val study = factory.createDisabledStudy
      val cmd = AddStudyCmd(sessionUserId = None,
                            name          = study.name,
                            description   = study.description)
      val v = ask(studiesProcessor, cmd).mapTo[ServiceValidation[StudyEvent]].futureValue
      v.isSuccess must be (true)
      studyRepository.getValues.map { s => s.name } must contain (study.name)

      studyRepository.removeAll
      studiesProcessor = restartProcessor(studiesProcessor)

      studyRepository.getValues.size must be (1)
      studyRepository.getValues.map { s => s.name } must contain (study.name)
    }

    it("recovers a snapshot", PersistenceTest) {
      val snapshotFilename = "testfilename"
      val studies = (1 to 2).map { _ => factory.createDisabledStudy }
      val snapshotStudy = studies(1)
      val snapshotState = StudiesProcessor.SnapshotState(Set(snapshotStudy))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      studies.foreach(studyRepository.put)
      (studiesProcessor ? "snap").mapTo[String].futureValue

      studyRepository.removeAll
      studiesProcessor = restartProcessor(studiesProcessor)

      studyRepository.getValues.size must be (1)
      studyRepository.getByKey(snapshotStudy.id) mustSucceed { repoStudy =>
        repoStudy.name must be (snapshotStudy.name)
        ()
      }
    }

  }

}
