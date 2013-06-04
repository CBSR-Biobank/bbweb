import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref

import org.specs2.specification.BeforeExample
import org.specs2.scalaz.ValidationMatchers._
import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.execute.Result

import akka.actor._
import akka.util.Timeout
import org.eligosource.eventsourced.core._

import domain._
import domain.study._
import service.commands._
import service._
import test._

import scalaz._
import Scalaz._

@RunWith(classOf[JUnitRunner])
class StudyServiceSpec extends EventsourcedSpec[StudyServiceFixture.Fixture] {
  import fixture._

  def getStudy(v: DomainValidation[Study]): Study = {
    v match {
      case Success(s) => s
      case _ => throw new Error("null study, validation failed")
    }
  }

  "add a study" in {
    fragmentName: String =>
      val name = new NameGenerator(fragmentName).next[Study]
      val study = entityResult(studyService.addStudy(new AddStudyCmd(name, name)))
      studyRepository.getMap must not be empty
      studyRepository.getMap must haveKey(study.id)
      studyRepository.getByKey(study.id) must beSome.like {
        case s => s.description must be(name)
      }
  }

  "enable a study" in {
    fragmentName: String =>
      val ng = new NameGenerator(fragmentName)
      val name = ng.next[Study]
      val units = ng.next[String]
      val anatomicalSourceId = new AnatomicalSourceId(ng.next[String])
      val preservationId = new PreservationId(ng.next[String])
      val specimenTypeId = new SpecimenTypeId(ng.next[String])

      val study = entityResult(studyService.addStudy(new AddStudyCmd(name, name)))

      val sg = entityResult(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study.id.toString, name, name, units, anatomicalSourceId,
          preservationId, specimenTypeId)))

      entityResult(studyService.enableStudy(new EnableStudyCmd(study.id.toString, study.versionOption)))

      studyRepository.getByKey(study.id) must beSome.like {
        case s => s must beAnInstanceOf[EnabledStudy]
      }
  }

  "add a study with duplicate name" in {
    fragmentName: String =>
      val name = new NameGenerator(fragmentName).next[Study]
      result(studyService.addStudy(new AddStudyCmd(name, name))) must beSuccessful

      val r = result(studyService.addStudy(new AddStudyCmd(name, name)))
      (r match {
        case Failure(msglist) => msglist.head must contain("name already exists")
        case _ => failure
      }): Result
  }

  "add specimen groups" in {
    fragmentName: String =>
      val ng = new NameGenerator(fragmentName)
      val name = ng.next[Study]
      val units = ng.next[String]
      val anatomicalSourceId = new AnatomicalSourceId(ng.next[String])
      val preservationId = new PreservationId(ng.next[String])
      val specimenTypeId = new SpecimenTypeId(ng.next[String])

      val study1 = entityResult(studyService.addStudy(new AddStudyCmd(name, name)))

      val sg1 = entityResult(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study1.id.toString, name, name, units, anatomicalSourceId,
          preservationId, specimenTypeId)))

      specimenGroupRepository.getMap must haveKey(sg1.id)
      specimenGroupRepository.getByKey(sg1.id) must beSome.like {
        case x =>
          x.description must be(name)
          x.units must be(units)
          x.anatomicalSourceId must be(anatomicalSourceId)
          x.preservationId must be(preservationId)
          x.specimenTypeId must be(specimenTypeId)
      }

      val name2 = ng.next[Study]
      val sg2 = entityResult(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study1.id.toString, name2, name2, units, anatomicalSourceId,
          preservationId, specimenTypeId)))

      specimenGroupRepository.getMap must haveKey(sg2.id)
      specimenGroupRepository.getByKey(sg2.id) must beSome.like {
        case x =>
          x.description must be(name2)
          x.description must be(name2)
          x.units must be(units)
          x.anatomicalSourceId must be(anatomicalSourceId)
          x.preservationId must be(preservationId)
          x.specimenTypeId must be(specimenTypeId)
      }
  }

  "update specimen groups" in {
    fragmentName: String =>
      val ng = new NameGenerator(fragmentName)
      val name = ng.next[Study]
      val units = ng.next[String]
      val anatomicalSourceId = new AnatomicalSourceId(ng.next[String])
      val preservationId = new PreservationId(ng.next[String])
      val specimenTypeId = new SpecimenTypeId(ng.next[String])

      val study1 = entityResult(studyService.addStudy(new AddStudyCmd(name, name)))
      val sg1 = entityResult(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study1.id.toString, name, name, units, anatomicalSourceId,
          preservationId, specimenTypeId)))

      val name2 = ng.next[Study]
      val units2 = ng.next[String]
      val anatomicalSourceId2 = new AnatomicalSourceId(ng.next[String])
      val preservationId2 = new PreservationId(ng.next[String])
      val specimenTypeId2 = new SpecimenTypeId(ng.next[String])

      val sg2 = entityResult(studyService.updateSpecimenGroup(
        new UpdateSpecimenGroupCmd(study1.id.toString, sg1.id.toString, sg1.versionOption, name2,
          name2, units2, anatomicalSourceId2, preservationId2, specimenTypeId2)))

      specimenGroupRepository.getByKey(sg2.id) must beSome.like {
        case x =>
          x.name must be(name2)
          x.description must be(name2)
          x.units must be(units2)
          x.anatomicalSourceId must be(anatomicalSourceId2)
          x.preservationId must be(preservationId2)
          x.specimenTypeId must be(specimenTypeId2)
      }
  }
}

object StudyServiceFixture {

  class Fixture extends EventsourcingFixture[DomainValidation[ConcurrencySafeEntity[_]]] {

    val studyRepository = new Repository[StudyId, Study](v => v.id)
    val specimenGroupRepository = new Repository[SpecimenGroupId, SpecimenGroup](v => v.id)

    val studyProcessor = extension.processorOf(Props(
      new StudyProcessor(studyRepository, specimenGroupRepository) with Emitter with Eventsourced { val id = 1 }))

    val studyService = new StudyService(studyRepository, specimenGroupRepository, studyProcessor)

    def result[T <: ConcurrencySafeEntity[_]](f: Future[DomainValidation[T]]) = {
      Await.result(f, timeout.duration)
    }

    def entityResult[T <: ConcurrencySafeEntity[_]](f: Future[DomainValidation[T]]): T = {
      result(f) match {
        case Success(e) => e
        case Failure(msg) => throw new Error("null entity, validation failed: " + msg)
      }
    }
  }
}
