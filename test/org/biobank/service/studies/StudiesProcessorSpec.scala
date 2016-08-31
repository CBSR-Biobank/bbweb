package org.biobank.service.studies

import akka.pattern._
import org.biobank.fixture._
import org.slf4j.LoggerFactory
import org.biobank.service.ServiceValidation
import akka.testkit.TestKit
import scalaz.Scalaz._

class StudiesProcessorSpec extends TestFixture {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.StudyCommands._
  import org.biobank.infrastructure.event.StudyEvents._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  val persistenceId = "studies-processor-id"

  override def afterAll {
    TestKit.shutdownActorSystem(system)
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
      studyRepository.removeAll
      studiesProcessor ! "persistence_restart"

      Thread.sleep(1000)

      studyRepository.getValues.map { s => s.name } must contain (study.name)
    }

    "allow a snapshot request" in {
      val studies = (1 to 2).map { _ => factory.createDisabledStudy }
      studies.foreach(studyRepository.put)
      studiesProcessor ! "snap"
    }

    "accept a snapshot offer" in {
      val studies = (1 to 2).map { _ => factory.createDisabledStudy }
      studies.foreach(studyRepository.put)
      studiesProcessor ? "snap"

      Thread.sleep(1000)

      studyRepository.removeAll
      studiesProcessor ! "persistence_restart"

      Thread.sleep(1000)

      studies.foreach { study =>
        studyRepository.getByKey(study.id) mustSucceed { repoStudy =>
          repoStudy.name must be (study.name)
        }
      }
    }

  }

}
