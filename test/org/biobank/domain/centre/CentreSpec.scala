package org.biobank.domain.centre

import org.biobank.domain.DomainSpec
import org.biobank.fixture.NameGenerator

import org.slf4j.LoggerFactory
import com.github.nscala_time.time.Imports._
import scalaz.Scalaz._

class CentreSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A centre" can {

    "be created" in {
      val centre = factory.createDisabledCentre
      DisabledCentre.create(id          = centre.id,
                            version     = 0,
                            name        = centre.name,
                            description = centre.description,
                            studyIds    = Set.empty,
                            locations   = Set.empty
      ).mustSucceed { s =>
        s mustBe a[DisabledCentre]

        s must have (
          'id          (centre.id),
          'version     (0L),
          'name        (centre.name),
          'description (centre.description)
        )

        centre.studyIds must have size 0
        centre.locations must have size 0

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

        updatedCentre.studyIds must have size 0
        updatedCentre.locations must have size 0

        checkTimeStamps(updatedCentre, DateTime.now, DateTime.now)
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

        updatedCentre.studyIds must have size 0
        updatedCentre.locations must have size 0

        checkTimeStamps(updatedCentre, DateTime.now, DateTime.now)
      }
    }

    "be enabled if it has at least one location" in {
      val location = factory.createLocation
      val centre = factory.createDisabledCentre.copy(locations = Set(location))

      centre.enable() mustSucceed { enabledCentre =>
        enabledCentre mustBe a[EnabledCentre]
        enabledCentre.timeAdded mustBe (centre.timeAdded)
      }
    }

    "not be enabled if it has no locations" in {
      val centre = factory.createDisabledCentre
      centre.enable() mustFail ".*centre does not have locations.*"
    }

    "disable an enabled centre" in {
      val centre = factory.createEnabledCentre
      centre.disable mustSucceed { disabledCentre =>
        disabledCentre mustBe a[DisabledCentre]
        disabledCentre.timeAdded mustBe (centre.timeAdded)
      }
    }

  }

  "A centre" can {

    def createFrom(centre: DisabledCentre) = {
      DisabledCentre.create(id          = centre.id,
                            version     = centre.version,
                            name        = centre.name,
                            description = centre.description,
                            studyIds    = centre.studyIds,
                            locations   = centre.locations)
    }

    "not be created with an empty id" in {
      val centre = factory.createDisabledCentre.copy(id = CentreId(""))
      createFrom(centre) mustFail "IdRequired"
    }

    "not be created with an invalid version" in {
      val centre = factory.createDisabledCentre.copy(version = -2L)
      createFrom(centre) mustFail "InvalidVersion"
    }

    "not be created with a null or empty name" in {
      List("", null).foreach { name =>
        var centre = factory.createDisabledCentre.copy(name = name)
        createFrom(centre) mustFail "InvalidName"
      }
    }

    "not be created with an empty description option" in {
      List(Some(""), Some(null)).foreach { description =>
        var centre = factory.createDisabledCentre.copy(description = description)
        createFrom(centre) mustFail "InvalidDescription"
      }
    }

    "have more than one validation fail" in {
      var centre = factory.createDisabledCentre.copy(version = -2L, name = null)
      createFrom(centre) mustFail ("InvalidVersion",  "InvalidName")
    }

  }

  "A centre" can {

    "add a studyId" in {
      val centre = factory.createDisabledCentre.copy(studyIds = Set.empty)
      val study = factory.createDisabledStudy

      centre.withStudyId(study.id) mustSucceed { c =>
        c.studyIds must have size 1
        c.studyIds must contain (study.id)
      }
    }

    "remove a studyId" in {
      val study = factory.createDisabledStudy
      val centre = factory.createDisabledCentre.copy(studyIds = Set(study.id))

      centre.removeStudyId(study.id) mustSucceed { c =>
        c.studyIds must have size 0
      }
    }

  }

  "A centre" can {

    "add a location" in {
      val centre = factory.createDisabledCentre.copy(locations = Set.empty)
      val location = factory.createLocation

      centre.withLocation(location) mustSucceed { c =>
        c.locations must have size 1
        c.locations must contain (location)
      }
    }

    "replace a location" in {
      val location = factory.createLocation
      val location2 = factory.createLocation.copy(uniqueId = location.uniqueId)
      val centre = factory.createDisabledCentre.copy(locations = Set(location))

      centre.withLocation(location2) mustSucceed { c =>
        c.locations must have size 1
        c.locations must contain (location2)
      }
    }

    "remove a location" in {
      val location = factory.createLocation
      val centre = factory.createDisabledCentre.copy(locations = Set(location))

      centre.removeLocation(location.uniqueId) mustSucceed { c =>
        c.studyIds must have size 0
      }
    }

  }

}
