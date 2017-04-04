package org.biobank.service.studies

import akka.pattern._
import org.biobank.fixture._
import org.biobank.service.ServiceValidation
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.slf4j.LoggerFactory
import play.api.libs.json._
import scalaz.Scalaz._

class StudiesProcessorSpec extends TestFixture {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.StudyCommands._
  import org.biobank.infrastructure.event.StudyEvents._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  val persistenceId = "studies-processor-id"

  override def beforeEach() {
    studyRepository.removeAll
    super.beforeEach()
  }

  "A studies processor" must {

    "allow recovery from journal" in {
      val study = factory.createDisabledStudy
      val cmd = AddStudyCmd(userId      = None,
                            name        = study.name,
                            description = study.description)
      val v = ask(studiesProcessor, cmd).mapTo[ServiceValidation[StudyEvent]].futureValue
      v.isSuccess must be (true)
      studyRepository.getValues.map { s => s.name } must contain (study.name)
      studiesProcessor ! "persistence_restart"
      studyRepository.removeAll

      Thread.sleep(250)

      studyRepository.getValues.size must be (1)
      studyRepository.getValues.map { s => s.name } must contain (study.name)
    }

    "allow a snapshot request" in {
      val studies = (1 to 2).map { _ => factory.createDisabledStudy }
      studies.foreach(studyRepository.put)

      studiesProcessor ! "snap"
      Thread.sleep(250)
      verify(snapshotWriterMock, atLeastOnce).save(anyString, anyString)
      ()
    }

    "accept a snapshot offer" in {
      val snapshotFilename = "testfilename"
      val studies = (1 to 2).map { _ => factory.createDisabledStudy }
      val snapshotStudy = studies(1)
      val snapshotState = StudiesProcessor.SnapshotState(Set(snapshotStudy))

      Mockito.when(snapshotWriterMock.save(anyString, anyString)).thenReturn(snapshotFilename);
      Mockito.when(snapshotWriterMock.load(snapshotFilename))
        .thenReturn(Json.toJson(snapshotState).toString);

      studies.foreach(studyRepository.put)
      studiesProcessor ? "snap"
      Thread.sleep(250)
      studiesProcessor ! "persistence_restart"
      studyRepository.removeAll

      Thread.sleep(250)

      studyRepository.getValues.size must be (1)
      studyRepository.getByKey(snapshotStudy.id) mustSucceed { repoStudy =>
        repoStudy.name must be (snapshotStudy.name)
      }
    }

  }

}
