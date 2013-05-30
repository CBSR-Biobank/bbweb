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
      val study = studyResult(studyService.addStudy(name, name))
      study.id.id must not be empty
      study.name must be(name)
      study.description must be(name)
  }

  "add a study with duplicate name" in {
    fragmentName: String =>
      val name = new NameGenerator(fragmentName).next[Study]
      result(studyService.addStudy(name, name)) must beSuccessful

      val r = result(studyService.addStudy(name, name))
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

      val study1 = studyResult(studyService.addStudy(name, name))

      val study2 = studyResult(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study1.id.toString, study1.versionOption, name,
          name, units, anatomicalSourceId, preservationId, specimenTypeId)))
      study2.specimenGroups must have size (1)
      study2.specimenGroups.filter(_._2.name.equals(name)) must have size (1)

      val name2 = ng.next[Study]
      val study3 = studyResult(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study2.id.toString, study2.versionOption, name2, name2,
          units, anatomicalSourceId, preservationId, specimenTypeId)))
      study3.specimenGroups must have size (2)
      study3.specimenGroups.filter(_._2.name.equals(name2)) must have size (1)
  }

  "update specimen groups" in {
    fragmentName: String =>
      val ng = new NameGenerator(fragmentName)
      val name = ng.next[Study]
      val units = ng.next[String]
      val anatomicalSourceId = new AnatomicalSourceId(ng.next[String])
      val preservationId = new PreservationId(ng.next[String])
      val specimenTypeId = new SpecimenTypeId(ng.next[String])

      val study1 = studyResult(studyService.addStudy(name, name))
      val study2 = studyResult(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study1.id.toString, study1.versionOption, name,
          name, units, anatomicalSourceId, preservationId, specimenTypeId)))

      val sg = study2.specimenGroups.values.head

      val name2 = ng.next[Study]
      val units2 = ng.next[String]
      val anatomicalSourceId2 = new AnatomicalSourceId(ng.next[String])
      val preservationId2 = new PreservationId(ng.next[String])
      val specimenTypeId2 = new SpecimenTypeId(ng.next[String])

      val study3 = studyResult(studyService.updateSpecimenGroup(
        new UpdateSpecimenGroupCmd(study2.id.toString, study2.versionOption, sg.id.toString, name2,
          name2, units2, anatomicalSourceId2, preservationId2, specimenTypeId2)))

      val sg2 = study3.specimenGroups.values.head
      sg2.name must be(name2)
      sg2.description must be(name2)
      sg2.units must be(units2)
      sg2.anatomicalSourceId must be(anatomicalSourceId2)
      sg2.preservationId must be(preservationId2)
      sg2.specimenTypeId must be(specimenTypeId2)
  }
}

object StudyServiceFixture {

  class Fixture extends EventsourcingFixture[DomainValidation[Study]] {

    val studiesRef = Ref(Map.empty[domain.study.StudyId, Study])

    val studyProcessor = extension.processorOf(Props(
      new StudyProcessor(studiesRef) with Emitter with Eventsourced { val id = 1 }))

    val studyService = new StudyService(studiesRef, studyProcessor)

    def result[T <: Study](f: Future[DomainValidation[T]]) = {
      Await.result(f, timeout.duration)
    }

    def studyResult[T <: Study](f: Future[DomainValidation[T]]): Study = {
      result(f) match {
        case Success(s) => s
        case _ => throw new Error("null study, validation failed")
      }
    }
  }
}
