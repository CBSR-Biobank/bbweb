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
import org.scalatest.{ WordSpecLike, Matchers, BeforeAndAfterAll, Tag }
import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

//@RunWith(classOf[JUnitRunner])
class StudyProcessorSpec extends StudyProcessorFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  override val nameGenerator = new NameGenerator(this.getClass.getName)

  "A study processor" can {

    "add a study" in {
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val cmd = AddStudyCmd(name, description)
      val future = ask(studyProcessor, cmd).mapTo[DomainValidation[StudyAddedEvent]].futureValue

      future match {
        case Success(event) =>
          event shouldBe a [StudyAddedEvent]
          //event.id.toString should be > 0

          event should have (
            'name (name),
            'description (description)
          )

          studyRepository.studyWithId(StudyId(event.id)) map { study =>
            study shouldBe a[DisabledStudy]
          }

        case Failure(msg) =>
          val errors = msg.list.mkString(", ")
          fail(s"Error: $errors")
      }
    }

    "be recovered from journal" ignore {
      /*
       * Not sure if this is a good test, or how to do it correctly - ignoring it for now
       */
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      var cmd: StudyCommand = AddStudyCmd(name, description)
      val event1 = ask(studyProcessor, cmd).mapTo[DomainValidation[StudyAddedEvent]]
	.futureValue.getOrElse(fail)
      event1 shouldBe a [StudyAddedEvent]

      Thread.sleep(10)

      Await.result(gracefulStop(studyProcessor, 5 seconds, PoisonPill), 6 seconds)

      // restart
      val newStudyProcessor = system.actorOf(Props(new StudyProcessorImpl), "studyproc")

      Thread.sleep(10)

      val newName = nameGenerator.next[Study]
      val newDescription = some(nameGenerator.next[Study])

      cmd = UpdateStudyCmd(event1.id, Some(0), newName, newDescription)
      val event2 = ask(newStudyProcessor, cmd).mapTo[DomainValidation[StudyUpdatedEvent]]
	.futureValue.getOrElse(fail)
      event2 shouldBe a [StudyUpdatedEvent]

    }
  }

  "A study processor" should {

    "not add add a new study with a name that exists" in {
      val name = nameGenerator.next[Study]
      val cmd = AddStudyCmd(name, None)

      val f = ask(studyProcessor, cmd).mapTo[DomainValidation[StudyAddedEvent]]
      f.futureValue should be success

      ask(studyProcessor, cmd).mapTo[DomainValidation[StudyAddedEvent]].futureValue match {
	case Success(study) => fail
	case Failure(err) =>
          err.list should have length 1
	  err.list.head should include ("study with name already exists")
      }
    }

    //    "be updated" in {
    //      val name = nameGenerator.next[Study]
    //      val event1 = await(studyService.addStudy(new AddStudyCmd(name, Some(name)))) | null
    //
    //      val name2 = nameGenerator.next[Study]
    //      val study2 = await(studyService.updateStudy(
    //        new UpdateStudyCmd(event1.id, Some(0), name2, None)))
    //
    //      study2 must beSuccessful.like {
    //        case s =>
    //          s.name must be(name2)
    //          s.description must beNone
    //          studyRepository.studyWithId(StudyId(s.id)) must beSuccessful.like {
    //            case s =>
    //              s.version must beEqualTo(event1.version + 1)
    //          }
    //
    //          // update something other than the name
    //          val study3 = await(studyService.updateStudy(
    //            new UpdateStudyCmd(event1.id, Some(1), name2)))
    //          study3 must beSuccessful
    //      }
    //    }
    //
    //    "not be updated to name that exists" in {
    //      val name = nameGenerator.next[Study]
    //      val study1 = await(studyService.addStudy(new AddStudyCmd(name, Some(name)))) | null
    //
    //      val name2 = nameGenerator.next[Study]
    //      val study2 = await(studyService.addStudy(new AddStudyCmd(name2, Some(name2)))) | null
    //
    //      val study3 = await(studyService.updateStudy(
    //        new UpdateStudyCmd(study2.id, Some(0), name, Some(name))))
    //      study3 must beFailing.like {
    //        case msgs => msgs.head must contain("name already exists")
    //      }
    //
    //    }
    //
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
