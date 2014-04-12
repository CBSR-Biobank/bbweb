package service

import fixture._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  AnatomicalSourceType,
  AnnotationValueType,
  PreservationType,
  PreservationTemperatureType,
  SpecimenType
}
import org.biobank.domain.study._

import akka.actor._
import akka.pattern.ask
import akka.testkit.TestKitBase
import akka.testkit.ImplicitSender
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import scala.util.{ Success, Failure }
import scala.concurrent.ExecutionContext.Implicits.global

import scalaz._
import scalaz.Scalaz._

@RunWith(classOf[JUnitRunner])
class StudyServiceSpec extends StudyProcessorFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  override val nameGenerator = new NameGenerator(this.getClass.getName)

  //  "Study" can {
  //
  //    "be added" in {
  //      val name = nameGenerator.next[Study]
  //      val event = await(studyService.addStudy(new AddStudyCmd(name, Some(name))))
  //
  //      event must beSuccessful.like {
  //        case e: StudyAddedEvent =>
  //          e.name must be(name)
  //          e.description must beSome(name)
  //          studyRepository.studyWithId(StudyId(e.id)) must beSuccessful.like {
  //            case s: DisabledStudy =>
  //              s.version must beEqualTo(e.version)
  //          }
  //      }
  //    }
  //
  //    "not be added if same name exists" in {
  //      val name = nameGenerator.next[Study]
  //      await(studyService.addStudy(new AddStudyCmd(name, Some(name)))) must beSuccessful
  //
  //      await(studyService.addStudy(new AddStudyCmd(name, Some(name)))) must beFailing.like {
  //        case msgs => msgs.head must contain("name already exists")
  //      }
  //    }
  //
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
  //  }
}
