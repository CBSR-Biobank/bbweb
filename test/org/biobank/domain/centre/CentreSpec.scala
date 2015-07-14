package org.biobank.domain.centre

import org.biobank.domain.DomainSpec
import org.biobank.fixture.NameGenerator

import org.slf4j.LoggerFactory
import com.github.nscala_time.time.Imports._
import org.scalatest.Tag
import scalaz.Scalaz._

class CentreSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A centre" can {

    "be created" in {
      val centre = factory.createDisabledCentre
      val v = DisabledCentre.create(id          = centre.id,
                                    version     = -1,
                                    name        = centre.name,
                                    description = centre.description)

      v mustSucceed { s =>
        s mustBe a[DisabledCentre]

        s must have (
          'id          (centre.id),
          'version     (0L),
          'name        (centre.name),
          'description (centre.description)
        )

        checkTimeStamps(s, DateTime.now, None)
      }
    }

    "have it's name updated" in {
      val centre = factory.createDisabledCentre
      val name = nameGenerator.next[Centre]

      centre.withName(name) mustSucceed { updatedCentre =>
        updatedCentre must have (
          'id          (centre.id),
          'version     (centre.version + 1),
          'name        (name),
          'description (centre.description)
        )

        checkTimeStamps(updatedCentre, DateTime.now, None)
      }
    }

    "have it's description updated" in {
      val centre = factory.createDisabledCentre
      val description = Some(nameGenerator.next[Centre])

      centre.withDescription(description) mustSucceed { updatedCentre =>
        updatedCentre must have (
          'id          (centre.id),
          'version     (centre.version + 1),
          'name        (centre.name),
          'description (description)
        )

        checkTimeStamps(updatedCentre, DateTime.now, None)
      }
    }


    "be enabled" in {
      val centre = factory.createDisabledCentre
      centre.enable() mustSucceed { enabledCentre =>
        enabledCentre mustBe a[EnabledCentre]
        enabledCentre.timeAdded mustBe (centre.timeAdded)
      }
    }

    "disable an enabled centre" in {
      val centre = factory.createEnabledCentre
      centre.disable mustSucceed { disabledCentre =>
        disabledCentre mustBe a[DisabledCentre]
        disabledCentre.timeAdded mustBe (centre.timeAdded)
      }
    }

  }

  "A centre" must {

    "not be created with an empty id" in {
      val v = DisabledCentre.create(id          = CentreId(""),
                                    version     = -1L,
                                    name        = nameGenerator.next[Centre],
                                    description = Some(nameGenerator.next[Centre]))
      v mustFail "IdRequired"
    }

    "not be created with an invalid version" in {
      val v = DisabledCentre.create(id          = CentreId(nameGenerator.next[Centre]),
                                    version     = -2L,
                                    name        = nameGenerator.next[Centre],
                                    description = Some(nameGenerator.next[Centre]))
      v mustFail "InvalidVersion"
    }

    "not be created with an null or empty name" in {
      var v = DisabledCentre.create(id          = CentreId(nameGenerator.next[Centre]),
                                    version     = -1L,
                                    name        = null,
                                    description = some(nameGenerator.next[Centre]))
      v mustFail "InvalidName"

      v = DisabledCentre.create(id          = CentreId(nameGenerator.next[Centre]),
                                version     = -1L,
                                name        = "",
                                description = Some(nameGenerator.next[Centre]))
      v mustFail "InvalidName"
    }

    "not be created with an empty description option" in {
      var v = DisabledCentre.create(id          = CentreId(nameGenerator.next[Centre]),
                                    version     = -1L,
                                    name        = nameGenerator.next[Centre],
                                    description = Some(null))

      v mustFail "InvalidDescription"

      v = DisabledCentre.create(id          = CentreId(nameGenerator.next[Centre]),
                                version     = -1L,
                                name        = nameGenerator.next[Centre],
                                description = Some(""))
      v mustFail "InvalidDescription"
    }

    "have more than one validation fail" in {
      var v = DisabledCentre.create(id          = CentreId(nameGenerator.next[Centre]),
                                    version     = -2L,
                                    name        = null,
                                    description = some(nameGenerator.next[Centre]))
      v mustFail ("InvalidVersion",  "InvalidName")
    }

  }

}
