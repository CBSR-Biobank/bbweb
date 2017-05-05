package org.biobank.service.participants

import akka.pattern._
import org.biobank.fixture._
import org.biobank.service.ServiceValidation
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scalaz.Scalaz._

class ParticipantsProcessorSpec extends TestFixture {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.ParticipantCommands._
  import org.biobank.infrastructure.event.ParticipantEvents._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  val persistenceId = "participants-processor-id"

  override def beforeEach() {
    participantRepository.removeAll
    super.beforeEach()
  }

  "A participants processor" must {

    "allow recovery from journal" in {
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

    "allow a snapshot request" in {
      val participants = (1 to 2).map { _ => factory.createParticipant }
      participants.foreach(participantRepository.put)

      participantsProcessor ! "snap"
      Thread.sleep(250)
      verify(snapshotWriterMock, atLeastOnce).save(anyString, anyString)
      ()
    }

    "accept a snapshot offer" in {
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
