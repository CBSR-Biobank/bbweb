package org.biobank.service

import org.biobank.fixture._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  AnatomicalSourceType,
  AnnotationValueType,
  DomainError,
  DomainValidation,
  Factory,
  PreservationType,
  PreservationTemperatureType,
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

  val factory = new Factory(nameGenerator)

  "A study processor" should {

    "add a study" in {
      val study = factory.createDisabledStudy

      val cmd = AddStudyCmd(study.name, study.description)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[StudyAddedEvent]]
	.futureValue

      validation should be ('success)
      validation map { event =>
        event shouldBe a [StudyAddedEvent]
        event should have (
          'name (study.name),
          'description (study.description)
        )

        studyRepository.studyWithId(StudyId(event.id)) map { study =>
          study shouldBe a[DisabledStudy]
        }
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
      val newStudyProcessor = system.actorOf(Props(new StudyProcessorImpl), "studyproc")

      Thread.sleep(10)

      val newName = nameGenerator.next[Study]
      val newDescription = some(nameGenerator.next[Study])

      cmd = UpdateStudyCmd(event.id, Some(0), newName, newDescription)
      val validation2 = ask(newStudyProcessor, cmd).mapTo[DomainValidation[StudyUpdatedEvent]]
	.futureValue

      validation2 should be ('success)
      validation2 map { event => event shouldBe a[StudyUpdatedEvent] }
    }

    "not add add a new study with a duplicate name" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val validation = ask(studyProcessor, AddStudyCmd(study.name, study.description))
	.mapTo[DomainValidation[StudyAddedEvent]].futureValue
      validation should be ('failure)

      validation.swap.map { err =>
        err.list should have length 1
	err.list.head should include ("study with name already exists")
      }
    }

    "be able to update a study with the same name" in {
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])

      val disabledStudy = DisabledStudy.create(studyRepository.nextIdentity, -1, name, None) | fail
      studyRepository.put(disabledStudy)

      val description2 = Some(nameGenerator.next[Study])

      val validation2 = ask(studyProcessor,
	UpdateStudyCmd(disabledStudy.id.toString, Some(0), name, description2))
	.mapTo[DomainValidation[StudyUpdatedEvent]]
	.futureValue
      validation2 should be ('success)

      validation2 map { event =>
	event shouldBe a[StudyUpdatedEvent]
	event.name should be (name)
	event.description should be (description2)
      }
    }

    "be able to update a study with new a name or description" in {
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])

      val disabledStudy = DisabledStudy.create(studyRepository.nextIdentity, -1, name, None) | fail
      studyRepository.put(disabledStudy)

      val name2 = nameGenerator.next[Study]
      val description2 = Some(nameGenerator.next[Study])

      val validation2 = ask(studyProcessor,
	UpdateStudyCmd(disabledStudy.id.toString, Some(0), name2, description2))
	.mapTo[DomainValidation[StudyUpdatedEvent]]
	.futureValue

      validation2 should be ('success)
      validation2 map { event =>
	event shouldBe a[StudyUpdatedEvent]
	event.name should be (name2)
	event.description should be (description2)
      }

      val study = studyRepository.studyWithId(disabledStudy.id) | fail
      study.version should be (1L)

      // update something other than the name
      val validation3 = ask(studyProcessor,
	UpdateStudyCmd(disabledStudy.id.toString, Some(0), name2, None))
	.mapTo[DomainValidation[StudyUpdatedEvent]]
	.futureValue
      validation3 should be ('failure)

      validation3 map { event =>
	event.name should be (name2)
        event.description should be (None)
      }
    }

    "not update a study to name that is used by another study" in {
      val name = nameGenerator.next[Study]
      val name2 = nameGenerator.next[Study]

      val study1 = DisabledStudy.create(studyRepository.nextIdentity, -1, name, None) | fail
      studyRepository.put(study1)

      val study2 = DisabledStudy.create(studyRepository.nextIdentity, -1, name2, None) | fail
      studyRepository.put(study2)

      val validation = ask(studyProcessor, UpdateStudyCmd(study2.id.id, Some(0L), name, None))
	.mapTo[DomainValidation[StudyAddedEvent]]
	.futureValue
      validation should be ('failure)

      validation.swap.map { err =>
        err.list should have length 1
        err.head should include ("name already exists")
      }
    }

    "not be updated with invalid version" in {
      val name = nameGenerator.next[Study]
      val name2 = nameGenerator.next[Study]

      val disabledStudy = DisabledStudy.create(studyRepository.nextIdentity, -1, name, None) | fail
      studyRepository.put(disabledStudy)

      val cmd = UpdateStudyCmd(disabledStudy.id.toString, Some(10L), name2, None)
      val validation2 = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[StudyUpdatedEvent]]
	.futureValue

      validation2 should be ('failure)

      validation2.swap map { err =>
        err.list should have length 1
        err.list.head should include ("doesn't match current version")
      }
    }

    "enable a study" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val disabledStudy = DisabledStudy.create(studyRepository.nextIdentity, -1, name, None) | fail
      studyRepository.put(disabledStudy)

      val sg = SpecimenGroup.create(disabledStudy.id, specimenGroupRepository.nextIdentity, -1L,
	name, None, units, anatomicalSourceType, preservationType, preservationTempType,
	specimenType) | fail
      specimenGroupRepository.put(sg)

      val cet = CollectionEventType.create(disabledStudy.id,
	collectionEventTypeRepository.nextIdentity, -1L, name, None, true,
	List.empty, List.empty) | fail
      collectionEventTypeRepository.put(cet)

      val validation4 = ask(studyProcessor,
	EnableStudyCmd(disabledStudy.id.toString, Some(0L)))
	.mapTo[DomainValidation[StudyEnabledEvent]]
	.futureValue
      validation4 should be ('success)

      validation4 map { event =>
	event shouldBe a[StudyEnabledEvent]
        val study = studyRepository.studyWithId(StudyId(event.id)) | fail
        study shouldBe a[EnabledStudy]
      }
    }

    "disable a study" in {
      val name = nameGenerator.next[Study]

      val disabledStudy = DisabledStudy.create(studyRepository.nextIdentity, -1, name, None) | fail
      val enabledStudy = disabledStudy.enable(Some(0L), 1, 1) | fail
      studyRepository.put(enabledStudy)

      val validation = ask(studyProcessor,
	DisableStudyCmd(enabledStudy.id.toString, Some(1L)))
	.mapTo[DomainValidation[StudyDisabledEvent]]
	.futureValue

      validation should be ('success)
      validation map { event =>
	event shouldBe a[StudyDisabledEvent]
        val study = studyRepository.studyWithId(StudyId(event.id)) | fail
        study shouldBe a[DisabledStudy]
      }
    }
  }
}
