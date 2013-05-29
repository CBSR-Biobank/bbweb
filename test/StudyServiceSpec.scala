import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref

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
    import fixture._
    val name = nameGenerator.next(classOf[Study])
    result(studyService.addStudy(name, name)) must beSuccessful
  }

  "add a study with duplicate name" in {
    import fixture._
    val name = nameGenerator.next(classOf[String])
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
