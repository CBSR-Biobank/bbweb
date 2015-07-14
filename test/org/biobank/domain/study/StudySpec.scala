package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.fixture.NameGenerator

import org.slf4j.LoggerFactory
import com.github.nscala_time.time.Imports._
import org.scalatest.Tag
import scalaz.Scalaz._

class StudySpec extends DomainSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A study" can {

    "be created" in {
      val study = factory.createDisabledStudy
      val v = DisabledStudy.create(id          = study.id,
                                   version     = -1,
                                   name        = study.name,
                                   description = study.description)

      v mustSucceed { s =>
        s mustBe a[DisabledStudy]

        s must have (
          'id          (study.id),
          'version     (0L),
          'name        (study.name),
          'description (study.description)
        )

        checkTimeStamps(s, DateTime.now, None)
      }
    }

    "have it's name updated" in {
      val study = factory.createDisabledStudy
      val name = nameGenerator.next[Study]

      study.withName(name) mustSucceed { updatedStudy =>
        updatedStudy must have (
          'id          (study.id),
          'version     (study.version + 1),
          'name        (name),
          'description (study.description)
        )

        checkTimeStamps(updatedStudy, DateTime.now, None)
      }
    }

    "have it's description updated" in {
      val study = factory.createDisabledStudy
      val description = Some(nameGenerator.next[Study])

      study.withDescription(description) mustSucceed { updatedStudy =>
        updatedStudy must have (
          'id          (study.id),
          'version     (study.version + 1),
          'name        (study.name),
          'description (description)
        )

        checkTimeStamps(updatedStudy, DateTime.now, None)
      }
    }


    "be enabled" in {
      val study = factory.createDisabledStudy
      study.enable(1, 1) mustSucceed { enabledStudy =>
        enabledStudy mustBe a[EnabledStudy]
        enabledStudy.timeAdded mustBe (study.timeAdded)
      }
    }

    "disable an enabled study" in {
      val study = factory.createEnabledStudy
      study.disable mustSucceed { disabledStudy =>
        disabledStudy mustBe a[DisabledStudy]
        disabledStudy.timeAdded mustBe (study.timeAdded)
      }
    }

    "be retired" in {
      val study = factory.createDisabledStudy
      study.retire mustSucceed { retiredStudy =>
        retiredStudy mustBe a[RetiredStudy]
        retiredStudy.timeAdded mustBe (study.timeAdded)
      }
    }

    "unretire a study" in {
      val study = factory.createRetiredStudy
      study.unretire() mustSucceed { disabledStudy =>
        disabledStudy mustBe a[DisabledStudy]
        disabledStudy.timeAdded mustBe (study.timeAdded)
      }
    }

  }

  "A study" must {

    "not be created with an empty id" in {
      val v = DisabledStudy.create(id          = StudyId(""),
                                   version     = -1L,
                                   name        = nameGenerator.next[Study],
                                   description = Some(nameGenerator.next[Study]))
      v mustFail "IdRequired"
    }

    "not be created with an invalid version" in {
      val v = DisabledStudy.create(id          = StudyId(nameGenerator.next[Study]),
                                   version     = -2L,
                                   name        = nameGenerator.next[Study],
                                   description = Some(nameGenerator.next[Study]))
      v mustFail "InvalidVersion"
    }

    "not be created with an null or empty name" in {
      var v = DisabledStudy.create(id          = StudyId(nameGenerator.next[Study]),
                                   version     = -1L,
                                   name        = null,
                                   description = some(nameGenerator.next[Study]))
      v mustFail "InvalidName"

      v = DisabledStudy.create(id          = StudyId(nameGenerator.next[Study]),
                               version     = -1L,
                               name        = "",
                               description = Some(nameGenerator.next[Study]))
      v mustFail "InvalidName"
    }

    "not be created with an empty description option" in {
      var v = DisabledStudy.create(id          = StudyId(nameGenerator.next[Study]),
                                   version     = -1L,
                                   name        = nameGenerator.next[Study],
                                   description = Some(null))

      v mustFail "InvalidDescription"

      v = DisabledStudy.create(id          = StudyId(nameGenerator.next[Study]),
                               version     = -1L,
                               name        = nameGenerator.next[Study],
                               description = Some(""))
      v mustFail "InvalidDescription"
    }

    "have more than one validation fail" in {
      var v = DisabledStudy.create(id          = StudyId(nameGenerator.next[Study]),
                                   version     = -2L,
                                   name        = null,
                                   description = some(nameGenerator.next[Study]))
      v mustFail ("InvalidVersion",  "InvalidName")
    }

    "not be enabled without prior configuration" in {
      val study = factory.createDisabledStudy
      study.enable(0, 0) mustFail "no specimen groups"
      study.enable(1, 0) mustFail "no collection event types"
    }

  }

}
