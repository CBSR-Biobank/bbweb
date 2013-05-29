import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref

import org.specs2.specification.BeforeExample
import org.specs2.scalaz.ValidationMatchers._
import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

import akka.actor._
import akka.util.Timeout
import org.eligosource.eventsourced.core._

import domain._
import domain.study.Study
import service._
import test._

import scalaz._
import Scalaz._

@RunWith(classOf[JUnitRunner])
class StudyServiceSpec extends EventsourcedSpec[StudyServiceFixture.Fixture] {

  "add a study" in {
    fragmentName: String =>
      val nameGenerator = new NameGenerator(fragmentName)
      import fixture._
      val name = nameGenerator.next[Study]
      val study = result(studyService.addStudy(name, name))
      study must beSuccessful

    //study match {
    //  case Success(s) => s.name must be name
    //}
  }

  "add a study with duplicate name" in {
    fragmentName: String =>
      val nameGenerator = new NameGenerator(fragmentName)
      import fixture._
      //val name = nameGenerator.next(Study.getClass)
      val name = nameGenerator.next[String]
      result(studyService.addStudy(name, name)) must beSuccessful

      result(studyService.addStudy(name, name)) must beFailing
  }

}

object StudyServiceFixture {

  class Fixture extends EventsourcingFixture[DomainValidation[Study]] {

    val studiesRef = Ref(Map.empty[String, Study])

    val studyProcessor = extension.processorOf(Props(
      new StudyProcessor(studiesRef) with Emitter with Eventsourced { val id = 1 }))

    val studyService = new StudyService(studiesRef, studyProcessor)

    def result[T <: Study](f: Future[DomainValidation[T]]) = {
      Await.result(f, timeout.duration)
    }
  }
}
