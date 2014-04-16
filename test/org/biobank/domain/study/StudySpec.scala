package org.biobank.domain.study

import org.biobank.fixture.NameGenerator

import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.OptionValues._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.slf4j.LoggerFactory
import scalaz._
import scalaz.Scalaz._

/**
 * Note: to run from Eclipse uncomment the @RunWith line. To run from SBT the line should be
 * commented out.
 *
 */
//@RunWith(classOf[JUnitRunner])
class StudySpec extends WordSpecLike with Matchers {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass.getName)

  "A study" can {

    "be created" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val v = DisabledStudy.create(id, version, name, description)
      val study = v.getOrElse(fail("could not create study"))
      study shouldBe a[DisabledStudy]

      study.id should be(id)
      study.version should be(0L)
      study.name should be(name)
      study.description should be(description)
    }

    "be enabled" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val v = DisabledStudy.create(id, version, name, description)
      val disabledStudy = v.getOrElse(fail("could not create study"))
      disabledStudy shouldBe a[DisabledStudy]

      val enabledStudy = disabledStudy.enable.getOrElse(fail("could not enable study"))
      enabledStudy shouldBe a[EnabledStudy]
    }

    "disable an enabled study" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val v = DisabledStudy.create(id, version, name, description)
      val disabledStudy = v.getOrElse(fail("could not create study"))
      disabledStudy shouldBe a[DisabledStudy]

      val enabledStudy = disabledStudy.enable.getOrElse(fail("could not enable study"))
      val disabledStudy2 = enabledStudy.disable.getOrElse(fail("could not disable study"))
      disabledStudy2 shouldBe a[DisabledStudy]
    }

    "be retired" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val v = DisabledStudy.create(id, version, name, description)
      val disabledStudy = v.getOrElse(fail("could not create study"))
      disabledStudy shouldBe a[DisabledStudy]

      val retiredStudy = disabledStudy.retire.getOrElse(fail("could not retire study"))
      retiredStudy shouldBe a[RetiredStudy]
    }

    "unretire a study" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val v = DisabledStudy.create(id, version, name, description)
      val disabledStudy = v.getOrElse(fail("could not create study"))
      disabledStudy shouldBe a[DisabledStudy]

      val retiredStudy = disabledStudy.retire.getOrElse(fail("could not retire study"))
      val disabledStudy2 =retiredStudy.unretire.getOrElse(fail("could not disable study"))
      disabledStudy2 shouldBe a[DisabledStudy]
    }

  }

  "A study" should {

    "not be created with an empty id" in {
      val id = StudyId("")
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      DisabledStudy.create(id, version, name, description) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("study id is null or empty"))
      }
    }

    "not be created with an invalid version" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -2L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      DisabledStudy.create(id, version, name, description) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("invalid version value: -2"))
      }
    }

    "not be created with an null or empty name" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      var name: String = null
      val description = some(nameGenerator.next[Study])

      DisabledStudy.create(id, version, name, description) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("name is null or empty"))
      }

      name = ""
      DisabledStudy.create(id, version, name, description) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("name is null or empty"))
      }
    }

    "not be created with an empty description option" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      var description: Option[String] = Some(null)

      DisabledStudy.create(id, version, name, description) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("description is null or empty"))
      }

      description = Some("")
      DisabledStudy.create(id, version, name, description) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("description is null or empty"))
      }
    }

    "have more than one validation fail" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -2L
      val name = ""
      var description: Option[String] = Some(nameGenerator.next[Study])

      DisabledStudy.create(id, version, name, description) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should have length 2
	  err.list.head should be ("invalid version value: -2")
	  err.list.tail.head should be ("name is null or empty")
      }
    }

  }

}
