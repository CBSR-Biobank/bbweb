package org.biobank.service.participants

import akka.actor.ActorRef
import akka.pattern._
import javax.inject.{ Inject, Named }
import org.biobank.fixture._
import org.biobank.domain.study.StudyRepository
import org.biobank.domain.participants.ParticipantRepository
import org.biobank.service.ServiceValidation
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scalaz.Scalaz._

case class NamedParticipantsProcessor @Inject() (@Named("participantsProcessor") processor: ActorRef)

class ParticipantsProcessorSpec extends ProcessorTestFixture {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.ParticipantCommands._
  import org.biobank.infrastructure.event.ParticipantEvents._

  val log = LoggerFactory.getLogger(this.getClass)

  val studyRepository = app.injector.instanceOf[StudyRepository]

  val participantsProcessor = app.injector.instanceOf[NamedParticipantsProcessor].processor

  val participantRepository = app.injector.instanceOf[ParticipantRepository]

  val nameGenerator = new NameGenerator(this.getClass)

  override def beforeEach() {
    participantRepository.removeAll
    super.beforeEach()
  }

  describe("A participants processor must") {

    it("allow recovery from journal") {
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
      participantsProcessor ! "persistence_restart"
      participantRepository.removeAll

      Thread.sleep(250)

      participantRepository.getValues.size must be (1)
      participantRepository.getValues.map { s => s.uniqueId } must contain (participant.uniqueId)
    }

    it("allow a snapshot request") {
      val participants = (1 to 2).map { _ => factory.createParticipant }
      participants.foreach(participantRepository.put)

      participantsProcessor ! "snap"
      Thread.sleep(250)
      verify(snapshotWriterMock, atLeastOnce).save(anyString, anyString)
      ()
    }

    it("accept a snapshot offer") {
      val snapshotFilename = "testfilename"
      val participants = (1 to 2).map { _ => factory.createParticipant }
      val snapshotParticipant = participants(1)
      val snapshotState = ParticipantsProcessor.SnapshotState(Set(snapshotParticipant))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      participants.foreach(participantRepository.put)
      participantsProcessor ? "snap"
      Thread.sleep(250)
      participantsProcessor ! "persistence_restart"
      participantRepository.removeAll

      Thread.sleep(250)

      participantRepository.getValues.size must be (1)
      participantRepository.getByKey(snapshotParticipant.id) mustSucceed { repoParticipant =>
        repoParticipant.uniqueId must be (snapshotParticipant.uniqueId)
        ()
      }
    }

  }

}
