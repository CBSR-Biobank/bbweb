package org.biobank.services.participants

import akka.actor._
import akka.pattern._
import javax.inject.{ Inject, Named }
import org.biobank.fixtures._
import org.biobank.domain.studies.StudyRepository
import org.biobank.domain.participants.ParticipantRepository
import org.biobank.services._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import play.api.libs.json._
import scala.concurrent.duration._
import scala.concurrent.Await

case class NamedParticipantsProcessor @Inject() (@Named("participantsProcessor") processor: ActorRef)

class ParticipantsProcessorSpec extends ProcessorTestFixture {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.commands.ParticipantCommands._
  import org.biobank.infrastructure.events.ParticipantEvents._

  private var participantsProcessor = app.injector.instanceOf[NamedParticipantsProcessor].processor

  private val studyRepository = app.injector.instanceOf[StudyRepository]

  private val participantRepository = app.injector.instanceOf[ParticipantRepository]

  private val nameGenerator = new NameGenerator(this.getClass)

  override def beforeEach() {
    participantRepository.removeAll
    super.beforeEach()
  }

  private def restartProcessor(processor: ActorRef) = {
    val stopped = gracefulStop(processor, 5 seconds, PoisonPill)
    Await.result(stopped, 6 seconds)

    val actor = system.actorOf(Props(new ParticipantsProcessor(
                                       participantRepository,
                                       studyRepository,
                                       app.injector.instanceOf[SnapshotWriter])),
                               "participants")
    Thread.sleep(250)
    actor
  }

  describe("A participants processor must") {

    it("allow recovery from journal", PersistenceTest) {
      val participant = factory.createParticipant
      val study = factory.defaultEnabledStudy
      val cmd = AddParticipantCmd(sessionUserId = nameGenerator.next[String],
                                  studyId       = study.id.id,
                                  uniqueId      = participant.uniqueId,
                                  annotations   = List.empty)
      studyRepository.put(study)
      val v = ask(participantsProcessor, cmd).mapTo[ServiceValidation[ParticipantEvent]].futureValue
      v.isSuccess must be (true)
      participantRepository.getValues.map { s => s.uniqueId } must contain (participant.uniqueId)
      participantRepository.removeAll
      participantsProcessor = restartProcessor(participantsProcessor)

      participantRepository.getValues.size must be (1)
      participantRepository.getValues.map { s => s.uniqueId } must contain (participant.uniqueId)
    }

    it("recovers a snapshot", PersistenceTest) {
      val snapshotFilename = "testfilename"
      val participants = (1 to 2).map { _ => factory.createParticipant }
      val snapshotParticipant = participants(1)
      val snapshotState = ParticipantsProcessor.SnapshotState(Set(snapshotParticipant))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      participants.foreach(participantRepository.put)
      (participantsProcessor ? "snap").mapTo[String].futureValue

      participantRepository.removeAll
      participantsProcessor = restartProcessor(participantsProcessor)

      participantRepository.getValues.size must be (1)
      participantRepository.getByKey(snapshotParticipant.id) mustSucceed { repoParticipant =>
        repoParticipant.uniqueId must be (snapshotParticipant.uniqueId)
        ()
      }
    }

  }

}
