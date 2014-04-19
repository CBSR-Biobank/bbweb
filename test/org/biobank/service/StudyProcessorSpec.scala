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

  "A study processor" should {

    "add a study" in {
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val cmd = AddStudyCmd(name, description)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[StudyAddedEvent]]
	.futureValue
      validation should be success

      validation map { event =>
        event shouldBe a [StudyAddedEvent]
        //event.id.toString should be > 0

        event should have (
          'name (name),
          'description (description)
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
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      var cmd: StudyCommand = AddStudyCmd(name, description)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[StudyAddedEvent]]
	.futureValue
      validation should be success

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

      validation2 should be success
    }

    "not add add a new study with a duplicate name" in {
      val name = nameGenerator.next[Study]

      val cmd = AddStudyCmd(name, None)
      ask(studyProcessor, cmd).mapTo[DomainValidation[StudyAddedEvent]]
	.futureValue should be success

      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[StudyAddedEvent]].futureValue
      validation should be success

      validation.swap.map { err =>
        err.list should have length 1
	err.list.head should include ("study with name already exists")
      }
    }

    "be able to update a study with new a name or description" in {
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])

      val validation1 = ask(studyProcessor, AddStudyCmd(name, None))
	.mapTo[DomainValidation[StudyAddedEvent]]
	.futureValue
      validation1 should be success

      val event = validation1.getOrElse(fail)
      val name2 = nameGenerator.next[Study]
      val description2 = Some(nameGenerator.next[Study])

      val validation2 = ask(studyProcessor,
	UpdateStudyCmd(event.id, Some(0), name2, description2))
	.mapTo[DomainValidation[StudyUpdatedEvent]]
	.futureValue
      validation2 should be success

      validation2 map { event =>
	event.name should be (name2)
	event.description should be (description2)
      }

      val study = studyRepository.studyWithId(StudyId(event.id)).getOrElse(fail)
      study.version should be (1L)

      // update something other than the name
      val validation3 = ask(studyProcessor, UpdateStudyCmd(event.id, Some(0), name2, none))
	.mapTo[DomainValidation[StudyUpdatedEvent]]
	.futureValue
      validation3 should be success

      validation3 map { event =>
	event.name should be (name2)
        event.description should be (None)
      }
    }

    "not update a study to name that is used by another study" in {
      val name = nameGenerator.next[Study]
      val name2 = nameGenerator.next[Study]

      val validation1 = ask(studyProcessor, AddStudyCmd(name, None))
	.mapTo[DomainValidation[StudyAddedEvent]]
	.futureValue
      validation1 should be success

      val study1AddedEvent = validation1.getOrElse(fail)

      val validation2 = ask(studyProcessor, AddStudyCmd(name2, None))
	.mapTo[DomainValidation[StudyAddedEvent]]
	.futureValue
      validation2 should be success

      val validation3 = ask(studyProcessor,
	UpdateStudyCmd(study1AddedEvent.id, Some(0L), name2, None))
	.mapTo[DomainValidation[StudyAddedEvent]]
	.futureValue
      validation3 should be failure

      validation3.swap.map { err =>
        err.list should have length 1
        err.head should include ("name already exists")
      }
    }

    //    "not be updated with invalid version" in {
    //      val name = nameGenerator.next[Study]
    //      val study1 = await(studyService.addStudy(new AddStudyCmd(name, Some(name)))) | null
    //
    //      val name2 = nameGenerator.next[Study]
    //      val versionOption = Some(study1.version + 1)
    //      val study2 = await(studyService.updateStudy(
    //        new UpdateStudyCmd(study1.id, versionOption, name2, None)))
    //
    //      study2 must beFailing.like {
    //        case msgs => msgs.head must contain("doesn't match current version")
    //      }
    //    }
    //
    //    "be enabled" in {
    //      val name = nameGenerator.next[Study]
    //      val units = nameGenerator.next[String]
    //      val anatomicalSourceType = AnatomicalSourceType.Blood
    //      val preservationType = PreservationType.FreshSpecimen
    //      val preservationTempType = PreservationTemperatureType.Minus80celcius
    //      val specimenType = SpecimenType.FilteredUrine
    //
    //      val event1 = await(studyService.addStudy(new AddStudyCmd(name, Some(name)))) | null
    //      studyRepository.studyWithId(StudyId(event1.id)) must beSuccessful
    //
    //      val sg1 = await(studyService.addSpecimenGroup(
    //        new AddSpecimenGroupCmd(event1.id, name, Some(name), units, anatomicalSourceType,
    //          preservationType, preservationTempType, specimenType)))
    //      sg1 must beSuccessful.like {
    //        case x =>
    //          specimenGroupRepository.specimenGroupWithId(StudyId(event1.id), x.specimenGroupId) must beSuccessful
    //          specimenGroupRepository.allSpecimenGroupsForStudy(StudyId(event1.id)).size mustEqual 1
    //      }
    //
    //      val cet1 = await(studyService.addCollectionEventType(
    //        new AddCollectionEventTypeCmd(event1.id, name, Some(name), true,
    //          Set.empty, Set.empty)))
    //      cet1 must beSuccessful.like {
    //        case x =>
    //          collectionEventTypeRepository.collectionEventTypeWithId(
    //            StudyId(event1.id), x.collectionEventTypeId) must beSuccessful
    //          collectionEventTypeRepository.allCollectionEventTypesForStudy(
    //            StudyId(event1.id)).size mustEqual 1
    //      }
    //
    //      val event2 = await(studyService.enableStudy(new EnableStudyCmd(event1.id, Some(1L))))
    //      event2 must beSuccessful.like {
    //        case e =>
    //          studyRepository.studyWithId(StudyId(e.id)) must beSuccessful.like {
    //            case s => s must beAnInstanceOf[EnabledStudy]
    //          }
    //      }
    //    } tag ("tag1")
    //
    //    "be disabled" in {
    //      val name = nameGenerator.next[Study]
    //      val units = nameGenerator.next[String]
    //      val anatomicalSourceType = AnatomicalSourceType.Blood
    //      val preservationType = PreservationType.FreshSpecimen
    //      val preservationTempType = PreservationTemperatureType.Minus80celcius
    //      val specimenType = SpecimenType.FilteredUrine
    //
    //      val event1 = await(studyService.addStudy(new AddStudyCmd(name, Some(name)))) | null
    //
    //      await(studyService.addSpecimenGroup(
    //        new AddSpecimenGroupCmd(event1.id, name, Some(name), units, anatomicalSourceType,
    //          preservationType, preservationTempType, specimenType)))
    //
    //      await(studyService.addCollectionEventType(
    //        new AddCollectionEventTypeCmd(event1.id, name, Some(name), true,
    //          Set.empty, Set.empty)))
    //
    //      val event2 = await(studyService.enableStudy(
    //        new EnableStudyCmd(event1.id, Some(1L)))) | null
    //
    //      studyRepository.studyWithId(StudyId(event2.id)) must beSuccessful.like {
    //        case e => e must beAnInstanceOf[EnabledStudy]
    //      }
    //
    //      val event3 = await(studyService.disableStudy(
    //        new DisableStudyCmd(event2.id, Some(2L))))
    //
    //      event3 must beSuccessful.like {
    //        case e =>
    //          studyRepository.studyWithId(StudyId(e.id)) must beSuccessful.like {
    //            case s => s must beAnInstanceOf[DisabledStudy]
    //          }
    //      }
    //    }
  }
}
