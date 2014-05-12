package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.fixture.NameGenerator

import org.scalatest.OptionValues._
import org.slf4j.LoggerFactory
import com.github.nscala_time.time.Imports._
import scalaz._
import scalaz.Scalaz._

class StudySpec extends DomainSpec {

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

      study should have (
        'id (id),
        'version (0L),
        'name (name),
        'description (description)
      )

      (study.addedDate to DateTime.now).millis should be < 100L
      study.lastUpdateDate should be (None)
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

      updatedStudy should have (
        'id (id),
        'version (1L),
        'name (name2),
        'description (description2)
      )

      updatedStudy.addedDate should be (study.addedDate)
      val updateDate = updatedStudy.lastUpdateDate | fail
      (updateDate to DateTime.now).millis should be < 100L
    }

    "be enabled" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val study = DisabledStudy.create(id, version, name, description) | fail
      study shouldBe a[DisabledStudy]

      val enabledStudy = study.enable(Some(0L), 1, 1) | fail
      enabledStudy shouldBe a[EnabledStudy]

      enabledStudy.addedDate should be (study.addedDate)
      var updateDate = enabledStudy.lastUpdateDate | fail
      (updateDate to DateTime.now).millis should be < 100L
    }

    "disable an enabled study" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val study = DisabledStudy.create(id, version, name, description) | fail
      study shouldBe a[DisabledStudy]

      val enabledStudy = study.enable(Some(0L), 1, 1) | fail

      val disabledStudy = enabledStudy.disable(Some(1L)) | fail
      disabledStudy shouldBe a[DisabledStudy]

      disabledStudy.addedDate should be (study.addedDate)
      val updateDate = disabledStudy.lastUpdateDate | fail
      (updateDate to DateTime.now).millis should be < 100L
    }

    "be retired" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val study = DisabledStudy.create(id, version, name, description) | fail
      study shouldBe a[DisabledStudy]

      val retiredStudy = study.retire(Some(0L)) | fail
      retiredStudy shouldBe a[RetiredStudy]

      retiredStudy.addedDate should be (study.addedDate)
      val updateDate = retiredStudy.lastUpdateDate | fail
      (updateDate to DateTime.now).millis should be < 100L
    }

    "unretire a study" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val study = DisabledStudy.create(id, version, name, description) | fail
      study shouldBe a[DisabledStudy]

      val retiredStudy = study.retire(Some(0L)) | fail
      val disabledStudy =retiredStudy.unretire(Some(1L)) | fail
      disabledStudy shouldBe a[DisabledStudy]

      disabledStudy.addedDate should be (study.addedDate)
      val updateDate = disabledStudy.lastUpdateDate | fail
      (updateDate to DateTime.now).millis should be < 100L
    }

  }

  "A study" should {

    "not be created with an empty id" in {
      val id = StudyId("")
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      DisabledStudy.create(id, version, name, description).fold(
        err => err.list should (have length 1 and contain("id is null or empty")),
        user => fail
      )
    }

    "not be created with an invalid version" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -2L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      DisabledStudy.create(id, version, name, description).fold(
        err => err.list should (have length 1 and contain("invalid version value: -2")),
        user => fail
      )
    }

    "not be created with an null or empty name" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      var name: String = null
      val description = some(nameGenerator.next[Study])

      DisabledStudy.create(id, version, name, description).fold(
        err => err.list should (have length 1 and contain("name is null or empty")),
        user => fail
      )

      name = ""
      DisabledStudy.create(id, version, name, description).fold(
        err => err.list should (have length 1 and contain("name is null or empty")),
        user => fail
      )
    }

    "not be created with an empty description option" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      var description: Option[String] = Some(null)

      DisabledStudy.create(id, version, name, description).fold(
        err => err.list should (have length 1 and contain("description is null or empty")),
        user => fail
      )

      description = Some("")
      DisabledStudy.create(id, version, name, description).fold(
        err => err.list should (have length 1 and contain("description is null or empty")),
        user => fail
      )
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
      validation should be ('failure)

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
      validation should be ('failure)

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
      validation should be ('success)

      val study = validation | fail
      val validation2 = study.enable(Some(0L), 0, 0)
      validation2 should be ('failure)

      validation2.swap.map { err =>
        err.list should have length 1
        err.list.head should include ("no specimen groups")
      }
    }
  }

}
