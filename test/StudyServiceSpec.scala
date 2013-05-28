package test

import scala.concurrent._
import scala.concurrent.duration._

import org.specs2.scalaz.ValidationMatchers._
import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

import akka.actor._
import akka.util.Timeout
import org.eligosource.eventsourced.core._

import domain._
import domain.study._

import scalaz._
import Scalaz._

@RunWith(classOf[JUnitRunner])
class StudyServiceSpec extends EventsourcedSpec[StudyProcessorSpec.Fixture] {

  "add a study" in {
    import fixture._
    val name = "studySpecName"
    val description = "studySpecDescription"
    val study = Await.result(studyService.addStudy(name, description), timeout.duration)

    study must beSuccessful
  }

  "add a study with duplicate name" in {
    import fixture._
    val name = "studySpecName"
    val description = "studySpecDescription"
    val study = Await.result(studyService.addStudy(name, description), timeout.duration)

    study must beFailing
  }

}

object StudyProcessorSpec {

  class Fixture extends EventsourcingFixture[Long] {
  }
}
