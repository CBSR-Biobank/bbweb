package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.fixture.NameGenerator

import org.slf4j.LoggerFactory
import com.github.nscala_time.time.Imports._
import scalaz._
import scalaz.Scalaz._

class StudySpec extends DomainSpec {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A study" can {

    "be created" in{
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val v = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description)
      val study = v.getOrElse(fail("could not create study"))
      study mustBe a[DisabledStudy]

      study must have (
        'id (id),
        'version (0L),
        'name (name),
        'description (description)
      )

      (study.timeAdded to DateTime.now).millis must be < 100L
      study.timeModified mustBe (None)
    }

    "be updated" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val study = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description) | fail
      study mustBe a[DisabledStudy]

      val name2 = nameGenerator.next[Study]
      val description2 = some(nameGenerator.next[Study])

      val updatedStudy = study.update(name2, description2) | fail

      updatedStudy must have (
        'id (id),
        'version (1L),
        'name (name2),
        'description (description2)
      )

      updatedStudy.timeAdded mustBe (study.timeAdded)
    }

    "be enabled" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val study = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description) | fail
      study mustBe a[DisabledStudy]

      val enabledStudy = study.enable(1, 1) | fail
      enabledStudy mustBe a[EnabledStudy]

      enabledStudy.timeAdded mustBe (study.timeAdded)
    }

    "disable an enabled study" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val study = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description) | fail
      study mustBe a[DisabledStudy]

      val enabledStudy = study.enable(1, 1) | fail

      val disabledStudy = enabledStudy.disable | fail
      disabledStudy mustBe a[DisabledStudy]

      disabledStudy.timeAdded mustBe (study.timeAdded)
    }

    "be retired" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val study = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description) | fail
      study mustBe a[DisabledStudy]

      val retiredStudy = study.retire | fail
      retiredStudy mustBe a[RetiredStudy]

      retiredStudy.timeAdded mustBe (study.timeAdded)
    }

    "unretire a study" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val study = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description) | fail
      study mustBe a[DisabledStudy]

      val retiredStudy = study.retire | fail
      val disabledStudy =retiredStudy.unretire | fail
      disabledStudy mustBe a[DisabledStudy]

      disabledStudy.timeAdded mustBe (study.timeAdded)
    }

  }

  "A study" must {

    "not be created with an empty id" in {
      val id = StudyId("")
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description).fold(
        err => err.list must (have length 1 and contain("IdRequired")),
        user => fail
      )
    }

    "not be created with an invalid version" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -2L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description).fold(
        err => err.list must (have length 1 and contain("InvalidVersion")),
        user => fail
      )
    }

    "not be created with an null or empty name" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      var name: String = null
      val description = some(nameGenerator.next[Study])

      DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description).fold(
        err => err.list must (have length 1 and contain("NameRequired")),
        user => fail
      )

      name = ""
      DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description).fold(
        err => err.list must (have length 1 and contain("NameRequired")),
        user => fail
      )
    }

    "not be created with an empty description option" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      var description: Option[String] = Some(null)

      DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description).fold(
        err => err.list must (have length 1 and contain("NonEmptyDescription")),
        user => fail
      )

      description = Some("")
      DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description).fold(
        err => err.list must (have length 1 and contain("NonEmptyDescription")),
        user => fail
      )
    }

    "have more than one validation fail" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -2L
      val name = ""
      val description = Some(nameGenerator.next[Study])

      val validation = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description)
      validation mustBe ('failure)

      validation.swap.map { err =>
        err.list must have length 2
        err.list.head mustBe ("InvalidVersion")
        err.list.tail.head mustBe ("NameRequired")
      }
    }

    "no be enabled without prior configuration" in {
      val id = StudyId(nameGenerator.next[Study])
      val name = nameGenerator.next[Study]
      val validation = DisabledStudy.create(id, -1L, org.joda.time.DateTime.now, name, None)
      validation mustBe ('success)

      val study = validation | fail
      val validation2 = study.enable(0, 0)
      validation2 mustBe ('failure)

      validation2.swap.map { err =>
        err.list must have length 1
        err.list.head must include ("no specimen groups")
      }
    }
  }

}
