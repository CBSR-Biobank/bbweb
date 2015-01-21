package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.fixture.NameGenerator

import org.slf4j.LoggerFactory
import com.github.nscala_time.time.Imports._
import org.scalatest.Tag
import scalaz._
import scalaz.Scalaz._

class StudySpec extends DomainSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A study" can {

    "be created" in{
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val v = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description)
      v mustSucceed { study =>
        study mustBe a[DisabledStudy]

        study must have (
          'id (id),
          'version (0L),
          'name (name),
          'description (description)
        )

        (study.timeAdded to DateTime.now).millis must be < 500L
        study.timeModified mustBe (None)
      }
    }

    "be updated" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val v = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description)
      v mustSucceed { study =>
        study mustBe a[DisabledStudy]

        val name2 = nameGenerator.next[Study]
        val description2 = some(nameGenerator.next[Study])

        study.update(name2, description2) mustSucceed { updatedStudy =>
          updatedStudy must have (
            'id (id),
            'version (1L),
            'name (name2),
            'description (description2)
          )

          updatedStudy.timeAdded mustBe (study.timeAdded)
        }
      }

    }

    "be enabled" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val v = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description)
      v mustSucceed { study =>
        study mustBe a[DisabledStudy]

        study.enable(1, 1) mustSucceed { enabledStudy =>
          enabledStudy mustBe a[EnabledStudy]
          enabledStudy.timeAdded mustBe (study.timeAdded)
        }
      }
    }

    "disable an enabled study" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val v = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description)
      v mustSucceed { study =>
        study.enable(1, 1) mustSucceed { enabledStudy =>
          enabledStudy.disable mustSucceed { disabledStudy =>
            disabledStudy mustBe a[DisabledStudy]
            disabledStudy.timeAdded mustBe (study.timeAdded)
          }
        }
      }
    }

    "be retired" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val v = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description)
      v mustSucceed { study =>
        study mustBe a[DisabledStudy]

        study.retire mustSucceed { retiredStudy =>
          retiredStudy mustBe a[RetiredStudy]
          retiredStudy.timeAdded mustBe (study.timeAdded)
        }
      }
    }

    "unretire a study" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val v = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description)
      v mustSucceed { study =>
        study mustBe a[DisabledStudy]

        study.retire mustSucceed { retiredStudy =>
          retiredStudy.unretire mustSucceed { disabledStudy =>
            disabledStudy mustBe a[DisabledStudy]
            disabledStudy.timeAdded mustBe (study.timeAdded)
          }
        }
      }
    }

  }

  "A study" must {

    "not be created with an empty id" in {
      val id = StudyId("")
      val version = -1L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val v = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description)
      v mustFail "IdRequired"
    }

    "not be created with an invalid version" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -2L
      val name = nameGenerator.next[Study]
      val description = some(nameGenerator.next[Study])

      val v = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description)
      v mustFail "InvalidVersion"
    }

    "not be created with an null or empty name" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      var name: String = null
      val description = some(nameGenerator.next[Study])

      val v = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description)
      v mustFail "InvalidName"

      name = ""
      val v2 = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description)
      v2 mustFail "InvalidName"
    }

    "not be created with an empty description option" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -1L
      val name = nameGenerator.next[Study]
      var description: Option[String] = Some(null)

      val v = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description)
      v mustFail "NonEmptyDescription"

      description = Some("")
      val v2 = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description)
      v2 mustFail "NonEmptyDescription"
    }

    "have more than one validation fail" in {
      val id = StudyId(nameGenerator.next[Study])
      val version = -2L
      val name = ""
      val description = Some(nameGenerator.next[Study])

      val validation = DisabledStudy.create(id, version, org.joda.time.DateTime.now, name, description)
      validation.mustFail("InvalidVersion",  "InvalidName")
    }

    "not be enabled without prior configuration" in {
      val id = StudyId(nameGenerator.next[Study])
      val name = nameGenerator.next[Study]
      val v = DisabledStudy.create(id, -1L, org.joda.time.DateTime.now, name, None)
      v mustSucceed { study =>
        study.enable(0, 0) mustFail "no specimen groups"
      }
    }
  }

}
