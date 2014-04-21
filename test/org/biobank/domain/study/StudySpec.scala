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

  val nameGenerator = new NameGenerator(this.getClass)

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

    "be updated" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val study = DisabledStudy.create(id, version, name, description) | fail
      study shouldBe a[DisabledStudy]

      val name2 = nameGenerator.next[Study]
      val description2 = some(nameGenerator.next[Study])

      val updatedStudy = study.update(Some(0L), name2, description2) | fail

      updatedStudy.id should be(id)
      updatedStudy.version should be(1L)
      updatedStudy.name should be(name2)
      updatedStudy.description should be(description2)
    }

    "be enabled" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val v = DisabledStudy.create(id, version, name, description)
      val disabledStudy = v.getOrElse(fail("could not create study"))
      disabledStudy shouldBe a[DisabledStudy]

      val enabledStudy = disabledStudy.enable(Some(0L), 1, 1) | fail
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

      val enabledStudy = disabledStudy.enable(Some(0L), 1, 1) | fail
      val disabledStudy2 = enabledStudy.disable(Some(1L)) | fail
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

      val retiredStudy = disabledStudy.retire(Some(0L)).getOrElse(fail("could not retire study"))
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

      val retiredStudy = disabledStudy.retire(Some(0L)).getOrElse(fail("could not retire study"))
      val disabledStudy2 =retiredStudy.unretire(Some(1L)).getOrElse(fail("could not disable study"))
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

    "not be updated with an invalid version" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val study = DisabledStudy.create(id, version, name, description) | fail
      study shouldBe a[DisabledStudy]

      val name2 = nameGenerator.next[Study]
      val description2 = some(nameGenerator.next[Study])

      val validation = study.update(Some(10L), name2, description2)
      validation should be failure

      validation.swap map { err =>
        err.list should have length 1
	err.list.head should include ("expected version doesn't match current version")
      }
    }

    "have more than one validation fail" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -2L
      val name = ""
      val description = Some(nameGenerator.next[Study])

      val validation = DisabledStudy.create(id, version, name, description)
      validation should be failure

      validation.swap.map { err =>
        err.list should have length 2
	err.list.head should be ("invalid version value: -2")
	err.list.tail.head should be ("name is null or empty")
      }
    }

    "no be enabled without prior configuration" in {
      val id = StudyId(nameGenerator.next[Study])
      val name = nameGenerator.next[Study]
      val validation = DisabledStudy.create(id, -1L, name, None)
      validation should be success

      val study = validation | fail
      val validation2 = study.enable(Some(0L), 0, 0)
      validation2 should be failure

      validation2.swap.map { err =>
        err.list should have length 1
	err.list.head should include ("no specimen groups")
      }
    }
  }

}
