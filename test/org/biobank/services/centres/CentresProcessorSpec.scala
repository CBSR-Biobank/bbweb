package org.biobank.services.centres

import akka.actor._
import akka.pattern._
import javax.inject.{ Inject, Named }
import org.biobank.fixtures._
import org.biobank.domain.centres.CentreRepository
import org.biobank.domain.studies.StudyRepository
import org.biobank.services._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import play.api.libs.json._
import scala.concurrent.duration._
import scala.concurrent.Await

case class NamedCentresProcessor @Inject() (@Named("centresProcessor") processor: ActorRef)

class CentresProcessorSpec extends ProcessorTestFixture {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.commands.CentreCommands._
  import org.biobank.infrastructure.events.CentreEvents._

  private var centresProcessor = app.injector.instanceOf[NamedCentresProcessor].processor

  private val centreRepository = app.injector.instanceOf[CentreRepository]

  private val nameGenerator = new NameGenerator(this.getClass)

  override def beforeEach() {
    centreRepository.removeAll
    super.beforeEach()
  }

  private def restartProcessor(processor: ActorRef) = {
    val stopped = gracefulStop(processor, 5 seconds, PoisonPill)
    Await.result(stopped, 6 seconds)

    val actor = system.actorOf(Props(new CentresProcessor(
                                       centreRepository,
                                       app.injector.instanceOf[StudyRepository],
                                       app.injector.instanceOf[SnapshotWriter])),
                               "centres")
    Thread.sleep(250)
    actor
  }

  describe("A centres processor must") {

    it("allow for recovery from journal", PersistenceTest) {
      val centre = factory.createDisabledCentre
      val cmd = AddCentreCmd(sessionUserId = nameGenerator.next[String],
                             name          = centre.name,
                             description   = centre.description)
      val v = ask(centresProcessor, cmd).mapTo[ServiceValidation[CentreEvent]].futureValue
      v.isSuccess must be (true)
      centreRepository.getValues.map { c => c.name } must contain (centre.name)

      centreRepository.removeAll
      centresProcessor = restartProcessor(centresProcessor)

      centreRepository.getValues.size must be (1)
      centreRepository.getValues.map { c => c.name } must contain (centre.name)
    }

    it("recovers a snapshot", PersistenceTest) {
      val snapshotFilename = "testfilename"
      val centres = (1 to 2).map { _ => factory.createDisabledCentre }
      val snapshotCentre = centres(1)
      val snapshotState = CentresProcessor.SnapshotState(Set(snapshotCentre))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      centres.foreach(centreRepository.put)

      (centresProcessor ? "snap").mapTo[String].futureValue
      centreRepository.removeAll
      centresProcessor = restartProcessor(centresProcessor)

      centreRepository.getValues.size must be (1)
      centreRepository.getByKey(snapshotCentre.id) mustSucceed { repoCentre =>
        repoCentre.name must be (snapshotCentre.name)
        ()
      }
    }

  }

}
