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
  RepositoriesComponentImpl,
  SpecimenType
}
import org.biobank.domain.study._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Await
import akka.pattern.{ ask, gracefulStop }
import akka.actor.{ Props, PoisonPill }
import org.joda.time.DateTime
import org.scalatest.Tag
import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

class StudiesProcessorSpec extends StudiesProcessorFixture {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  private def askAddCommand(study: Study): DomainValidation[StudyAddedEvent]  = {
    val cmd = AddStudyCmd(study.name, study.description)
    ask(studiesProcessor, cmd).mapTo[DomainValidation[StudyAddedEvent]].futureValue
  }

  private def askUpdateCommand(study: Study): DomainValidation[StudyUpdatedEvent] = {
    val cmd = UpdateStudyCmd(study.id.id, study.version, study.name, study.description)
    ask(studiesProcessor, cmd).mapTo[DomainValidation[StudyUpdatedEvent]].futureValue
  }


  "A study processor" can {

    "add a study" in {
      val study = factory.createDisabledStudy

      askAddCommand(study).fold(
        err => fail(err.list.mkString),
        event => {
          event shouldBe a [StudyAddedEvent]
          event should have (
            'name (study.name),
            'description (study.description)
          )

          studyRepository.getDisabled(StudyId(event.id)).fold(
            err => fail(err.list.mkString),
            repoStudy => checkTimeStamps(repoStudy, DateTime.now, None)
          )
        }
      )
    }

    "not add add a new study with a duplicate name" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val validation = ask(studiesProcessor, AddStudyCmd(study.name, study.description))
        .mapTo[DomainValidation[StudyAddedEvent]].futureValue
      validation should be ('failure)

      val study2 = study.copy()

      askAddCommand(study2).fold(
        err => {
          err.list should have length 1
          err.list.head should include ("study with name already exists")
        },
        event => fail("command should fail")
      )
    }

    "update a study with the same name" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val study2 = study.copy(description = Some(nameGenerator.next[String]))

      askUpdateCommand(study2).fold(
        err => fail(err.list.mkString),
        event => {
          event shouldBe a[StudyUpdatedEvent]
          event should have (
            'name (study2.name),
            'description (study2.description)
          )
          studyRepository.getDisabled(StudyId(event.id)).fold(
            err => fail(err.list.mkString),
            repoStudy => checkTimeStamps(repoStudy, study.addedDate, DateTime.now)
          )
        }
      )
    }

    "update a study with new a name or description" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val study2 = study.copy(name = nameGenerator.next[Study])

      askUpdateCommand(study2).fold(
        err => fail(err.list.mkString),
        event => {
          event shouldBe a[StudyUpdatedEvent]
          event should have (
            'name (study2.name),
            'description (study2.description)
          )

          studyRepository.getDisabled(StudyId(event.id)).fold(
            err => fail(err.list.mkString),
            repoStudy => {
              repoStudy.version should be (1L)
              checkTimeStamps(repoStudy, study.addedDate, DateTime.now)
            }
          )
        }
      )

      // update something other than the name
      val study3 = study2.copy(
        version = study.version + 1,
        description = Some(nameGenerator.next[Study]))

      askUpdateCommand(study3).fold(
        err => fail(err.list.mkString),
        event => {
          event should have (
            'name (study3.name),
            'description (study3.description)
          )
          studyRepository.getDisabled(StudyId(event.id)).fold(
            err => fail(err.list.mkString),
            repoStudy => checkTimeStamps(repoStudy, study.addedDate, DateTime.now)
          )
        }
      )
    }

    "not update a study to name that is used by another study" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val study3 = study2.copy(name = study.name)

      askUpdateCommand(study3).fold(
        err => {
          err.list should have length 1
          err.head should include ("name already exists")
        },
        event => fail("command should fail")
      )
    }

    "not be updated with invalid version" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val study2 = study.copy(version = study.version + 1)

      askUpdateCommand(study2).fold(
        err => {
          err.list should have length 1
          err.list.head should include ("doesn't match current version")
        },
        event => fail("command should fail")
      )
    }

    "enable a study" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val v = ask(studiesProcessor, EnableStudyCmd(study.id.toString, 0L))
        .mapTo[DomainValidation[StudyEnabledEvent]]
        .futureValue

      v.fold(
        err => fail(err.list.mkString),
        event => {
          event shouldBe a[StudyEnabledEvent]
          studyRepository.getEnabled(StudyId(event.id)).fold(
            err => fail(err.list.mkString),
            repoStudy => checkTimeStamps(repoStudy, study.addedDate, DateTime.now)
          )
        }
      )
    }

    "disable an enabled study" in {
      val enabledStudy = factory.createEnabledStudy
      studyRepository.put(enabledStudy)

      val v = ask(studiesProcessor, DisableStudyCmd(enabledStudy.id.toString, 1L))
        .mapTo[DomainValidation[StudyDisabledEvent]]
        .futureValue

      v.fold(
        err => fail(err.list.mkString),
        event => {
          event shouldBe a[StudyDisabledEvent]
          studyRepository.getDisabled(StudyId(event.id)).fold(
            err => fail(err.list.mkString),
            repoStudy => checkTimeStamps(repoStudy, enabledStudy.addedDate, DateTime.now)
          )
        }
      )
    }

    "retire a study" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val v = ask(studiesProcessor, RetireStudyCmd(study.id.toString, 0L))
        .mapTo[DomainValidation[StudyRetiredEvent]]
        .futureValue
      v.fold(
        err => fail(err.list.mkString),
        event => {
          event shouldBe a[StudyRetiredEvent]
          studyRepository.getRetired(StudyId(event.id)).fold(
            err => fail(err.list.mkString),
            repoStudy => checkTimeStamps(repoStudy, study.addedDate, DateTime.now)
          )
        }
      )
    }


    "be recovered from journal" ignore {
      /*
       * Not sure if this is a good test, or how to do it correctly - ignoring it for now
       */
      val study = factory.createDisabledStudy

      var cmd: StudyCommand = AddStudyCmd(study.name, study.description)
      val validation = ask(studiesProcessor, cmd).mapTo[DomainValidation[StudyAddedEvent]]
        .futureValue

      validation should be ('success)
      val event = validation.getOrElse(fail)

      Thread.sleep(10)

      Await.result(gracefulStop(studiesProcessor, 5 seconds, PoisonPill), 6 seconds)

      // restart
      val newStudiesProcessor = system.actorOf(Props(new StudiesProcessor), "studyproc")

      Thread.sleep(10)

      val newName = nameGenerator.next[Study]
      val newDescription = some(nameGenerator.next[Study])

      cmd = UpdateStudyCmd(event.id, 0, newName, newDescription)
      val validation2 = ask(newStudiesProcessor, cmd).mapTo[DomainValidation[StudyUpdatedEvent]]
        .futureValue

      validation2 should be ('success)
      validation2 map { event => event shouldBe a[StudyUpdatedEvent] }
    }
  }
}
