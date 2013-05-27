package test

import domain.study.Study
import domain.study.DisabledStudy
import domain.DomainValidation

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class StudySpec extends Specification {

  "Study" should {
    "add a study" in {
      val name = "studySpecName"
      val description = "studySpecDescription"
      val study = Study.add(Study.nextIdentity, name, description).toOption
      //        study.name should be name
      //         study.description should be name
      println("****************" + study)
    }

    "add a study with duplicate id" in {

    }
  }

}