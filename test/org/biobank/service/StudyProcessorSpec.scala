package org.biobank.service

import org.biobank.fixture._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  AnatomicalSourceType,
  AnnotationValueType,
  DomainError,
  DomainValidation,
  PreservationType,
  PreservationTemperatureType,
  RepositoryComponentImpl,
  SpecimenType
}
import org.biobank.domain.study._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Await
import akka.pattern.{ ask, gracefulStop }
import akka.actor.{ Props, PoisonPill }
import org.scalatest.Tag
import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

class StudyProcessorSpec extends StudyProcessorFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  private def askAddCommand(
    study: Study)(
    resultFunc: DomainValidation[StudyAddedEvent] => Unit): Unit = {
    val cmd = AddStudyCmd(
      study.name,
      study.description)
    val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[StudyAddedEvent]]
      .futureValue
    resultFunc(validation)
  }

  private def askUpdateCommand(
    study: Study)(
    resultFunc: DomainValidation[StudyUpdatedEvent] => Unit): Unit = {
    val cmd = UpdateStudyCmd(
      study.id.id,
      study.version,
      study.name,
      study.description)
    val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[StudyUpdatedEvent]]
      .futureValue
    resultFunc(validation)
  }


  "A study processor" can {

    "add a study" in {
      val study = factory.createDisabledStudy

      askAddCommand(study) { validation =>
        validation should be ('success)
        validation map { event =>
          event shouldBe a [StudyAddedEvent]
          event should have (
            'name (study.name),
            'description (study.description)
          )

          val repoStudy = studyRepository.getByKey(StudyId(event.id)) | fail
          repoStudy shouldBe a[DisabledStudy]
        }
      }
    }

    "not add add a new study with a duplicate name" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val validation = ask(studyProcessor, AddStudyCmd(study.name, study.description))
        .mapTo[DomainValidation[StudyAddedEvent]].futureValue
      validation should be ('failure)

      val study2 = study.copy()

      askAddCommand(study2) { validation =>
        validation.swap.map { err =>
          err.list should have length 1
          err.list.head should include ("study with name already exists")
        }
      }
    }

    "update a study with the same name" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val study2 = study.copy(description = Some(nameGenerator.next[String]))

      askUpdateCommand(study2) { validation =>
        validation map { event =>
          event shouldBe a[StudyUpdatedEvent]
          event should have (
            'name (study2.name),
            'description (study2.description)
          )
        }
      }
    }

    "update a study with new a name or description" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val study2 = study.copy(name = nameGenerator.next[Study])

      askUpdateCommand(study2) { validation =>
        validation should be ('success)
        validation map { event =>
          event shouldBe a[StudyUpdatedEvent]
          event should have (
            'name (study2.name),
            'description (study2.description)
          )
        }

        val repoStudy = studyRepository.getByKey(study.id) | fail
        repoStudy.version should be (1L)
      }

      // update something other than the name
      val study3 = study2.copy(
        version = study.version + 1,
        description = Some(nameGenerator.next[Study]))

      askUpdateCommand(study3) { validation =>
        validation should be ('success)
        validation map { event =>
          event should have (
            'name (study3.name),
            'description (study3.description)
          )
        }
      }
    }

    "not update a study to name that is used by another study" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val study3 = study2.copy(name = study.name)

      askUpdateCommand(study3) { validation =>
        validation.swap.map { err =>
          err.list should have length 1
          err.head should include ("name already exists")
        }
      }
    }

    "not be updated with invalid version" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val study2 = study.copy(version = study.version + 1)

      askUpdateCommand(study2) { validation =>
        validation should be ('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include ("doesn't match current version")
        }
      }
    }

    "enable a study" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val validation = ask(studyProcessor, EnableStudyCmd(study.id.toString, 0L))
        .mapTo[DomainValidation[StudyEnabledEvent]]
        .futureValue
      validation should be ('success)

      validation map { event =>
        event shouldBe a[StudyEnabledEvent]
        val study = studyRepository.getByKey(StudyId(event.id)) | fail
        study shouldBe a[EnabledStudy]
      }
    }

    "disable an enabled study" in {
      val enabledStudy = factory.createEnabledStudy
      studyRepository.put(enabledStudy)

      val validation = ask(studyProcessor, DisableStudyCmd(enabledStudy.id.toString, 1L))
        .mapTo[DomainValidation[StudyDisabledEvent]]
        .futureValue

      validation should be ('success)
      validation map { event =>
        event shouldBe a[StudyDisabledEvent]
        val study = studyRepository.getByKey(StudyId(event.id)) | fail
        study shouldBe a[DisabledStudy]
      }
    }

    "retire a study" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val validation = ask(studyProcessor, RetireStudyCmd(study.id.toString, 0L))
        .mapTo[DomainValidation[StudyRetiredEvent]]
        .futureValue
      validation should be ('success)

      validation map { event =>
        event shouldBe a[StudyRetiredEvent]
        val study = studyRepository.getByKey(StudyId(event.id)) | fail
        study shouldBe a[RetiredStudy]
      }
    }


    "be recovered from journal" ignore {
      /*
       * Not sure if this is a good test, or how to do it correctly - ignoring it for now
       */
      val study = factory.createDisabledStudy

      var cmd: StudyCommand = AddStudyCmd(study.name, study.description)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[StudyAddedEvent]]
        .futureValue

      validation should be ('success)
      val event = validation.getOrElse(fail)

      Thread.sleep(10)

      Await.result(gracefulStop(studyProcessor, 5 seconds, PoisonPill), 6 seconds)

      // restart
      val newStudyProcessor = system.actorOf(Props(new StudyProcessor), "studyproc")

      Thread.sleep(10)

      val newName = nameGenerator.next[Study]
      val newDescription = some(nameGenerator.next[Study])

      cmd = UpdateStudyCmd(event.id, 0, newName, newDescription)
      val validation2 = ask(newStudyProcessor, cmd).mapTo[DomainValidation[StudyUpdatedEvent]]
        .futureValue

      validation2 should be ('success)
      validation2 map { event => event shouldBe a[StudyUpdatedEvent] }
    }
  }
}
