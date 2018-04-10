package org.biobank.services.centres

import akka.actor.ActorRef
import akka.pattern._
import javax.inject.{ Inject, Named }
import org.biobank.fixture._
import org.biobank.domain.centres.CentreRepository
import org.biobank.services._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scalaz.Scalaz._

case class NamedCentresProcessor @Inject() (@Named("centresProcessor") processor: ActorRef)

class CentresProcessorSpec extends ProcessorTestFixture {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.CentreCommands._
  import org.biobank.infrastructure.event.CentreEvents._

  val log = LoggerFactory.getLogger(this.getClass)

  val centreRepository = app.injector.instanceOf[CentreRepository]

  val centresProcessor = app.injector.instanceOf[NamedCentresProcessor].processor

  val nameGenerator = new NameGenerator(this.getClass)

  override def beforeEach() {
    centreRepository.removeAll
    super.beforeEach()
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
      centresProcessor ! "persistence_restart"
      centreRepository.removeAll

      Thread.sleep(250)

      centreRepository.getValues.size must be (1)
      centreRepository.getValues.map { c => c.name } must contain (centre.name)
    }

    it("allow a snapshot request", PersistenceTest) {
      val centres = (1 to 2).map { _ => factory.createDisabledCentre }
      centres.foreach(centreRepository.put)

      centresProcessor ! "snap"
      Thread.sleep(250)
      verify(snapshotWriterMock, atLeastOnce).save(anyString, anyString)
      ()
    }

    it("accept a snapshot offer", PersistenceTest) {
      val snapshotFilename = "testfilename"
      val centres = (1 to 2).map { _ => factory.createDisabledCentre }
      val snapshotCentre = centres(1)
      val snapshotState = CentresProcessor.SnapshotState(Set(snapshotCentre))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      centres.foreach(centreRepository.put)
      centresProcessor ? "snap"
      Thread.sleep(250)
      centresProcessor ! "persistence_restart"
      centreRepository.removeAll

      Thread.sleep(250)

      centreRepository.getValues.size must be (1)
      centreRepository.getByKey(snapshotCentre.id) mustSucceed { repoCentre =>
        repoCentre.name must be (snapshotCentre.name)
        ()
      }
    }

  }

}
