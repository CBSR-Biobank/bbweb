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
      val r = result(studyService.addStudy(name, name))
      r must beSuccessful

      val s = getStudy(r)
      s.id.id must not be empty
      s.name must be(name)
      s.description must be(name)
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

      val r = result(studyService.addStudy(name, name))
      r must beSuccessful
      val study = getStudy(r)

      val r2 = result(studyService.addSpecimenGroup(study.id, study.versionOption, name, name,
        units, anatomicalSourceId, preservationId, specimenTypeId))
      r2 must beSuccessful

      val study2 = getStudy(r2)
      study2.specimenGroups must have size (1)
      val sg = study2.specimenGroups.filter(_.name.equals(name))
      sg must have size (1)

      val name2 = ng.next[Study]
      val r3 = result(studyService.addSpecimenGroup(study2.id, study2.versionOption, name2, name2,
        units, anatomicalSourceId, preservationId, specimenTypeId))
      r3 must beSuccessful

      val study3 = getStudy(r3)
      study3.specimenGroups must have size (2)
      val sg2 = study2.specimenGroups.filter(_.name.equals(name2))
      sg2 must have size (1)
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
  }
}
