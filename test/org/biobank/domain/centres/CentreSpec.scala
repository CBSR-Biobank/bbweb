package org.biobank.domain.centres

import java.time.OffsetDateTime
import org.biobank.domain.DomainSpec
import org.biobank.fixtures.NameGenerator
import org.slf4j.LoggerFactory
import scalaz.Scalaz._

class CentreSpec extends DomainSpec {
  import org.biobank.TestUtils._
  import org.biobank.matchers.EntityMatchers._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  describe("A centre") {

    it("be created") {
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

        s must beEntityWithTimeStamps(OffsetDateTime.now, None, 5L)
      }
    }

    it("have it's name updated") {
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

        updatedCentre must beEntityWithTimeStamps(OffsetDateTime.now, Some(OffsetDateTime.now), 5L)
      }
    }

    it("have it's description updated") {
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

        updatedCentre must beEntityWithTimeStamps(OffsetDateTime.now, Some(OffsetDateTime.now), 5L)
      }
    }

    it("be enabled if it has at least one location") {
      val location = factory.createLocation
      val centre = factory.createDisabledCentre.copy(locations = Set(location))

      centre.enable() mustSucceed { enabledCentre =>
        enabledCentre mustBe a[EnabledCentre]
        enabledCentre.timeAdded mustBe (centre.timeAdded)
        ()
      }
    }

    it("not be enabled if it has no locations") {
      val centre = factory.createDisabledCentre
      centre.enable() mustFail ".*centre does not have locations.*"
    }

    it("disable an enabled centre") {
      val centre = factory.createEnabledCentre
      centre.disable mustSucceed { disabledCentre =>
        disabledCentre mustBe a[DisabledCentre]
        disabledCentre.timeAdded mustBe (centre.timeAdded)
        ()
      }
    }

  }

  describe("A centre") {

    def createFrom(centre: DisabledCentre) = {
      DisabledCentre.create(id          = centre.id,
                            version     = centre.version,
                            name        = centre.name,
                            description = centre.description,
                            studyIds    = centre.studyIds,
                            locations   = centre.locations)
    }

    it("not be created with an empty id") {
      val centre = factory.createDisabledCentre.copy(id = CentreId(""))
      createFrom(centre) mustFail "IdRequired"
    }

    it("not be created with an invalid version") {
      val centre = factory.createDisabledCentre.copy(version = -2L)
      createFrom(centre) mustFail "InvalidVersion"
    }

    it("not be created with a null or empty name") {
      List("", null).foreach { name =>
        val centre = factory.createDisabledCentre.copy(name = name)
        createFrom(centre) mustFail "InvalidName"
      }
    }

    it("not be created with an empty description option") {
      List(Some(""), Some(null)).foreach { description =>
        val centre = factory.createDisabledCentre.copy(description = description)
        createFrom(centre) mustFail "InvalidDescription"
      }
    }

    it("have more than one validation fail") {
      val centre = factory.createDisabledCentre.copy(version = -2L, name = null)
      createFrom(centre) mustFail ("InvalidVersion",  "InvalidName")
    }

  }

  describe("A centre") {

    it("add a studyId") {
      val centre = factory.createDisabledCentre.copy(studyIds = Set.empty)
      val study = factory.createDisabledStudy

      centre.withStudyId(study.id) mustSucceed { c =>
        c.studyIds must have size 1
        c.studyIds must contain (study.id)
        ()
      }
    }

    it("remove a studyId") {
      val study = factory.createDisabledStudy
      val centre = factory.createDisabledCentre.copy(studyIds = Set(study.id))

      centre.removeStudyId(study.id) mustSucceed { c =>
        c.studyIds must have size 0
        ()
      }
    }

  }

  describe("A centre") {

    it("add a location") {
      val centre = factory.createDisabledCentre.copy(locations = Set.empty)
      val location = factory.createLocation

      centre.withLocation(location) mustSucceed { c =>
        c.locations must have size 1
        c.locations must contain (location)
        ()
      }
    }

    it("replace a location") {
      val location = factory.createLocation
      val location2 = factory.createLocation.copy(id = location.id)
      val centre = factory.createDisabledCentre.copy(locations = Set(location))

      centre.withLocation(location2) mustSucceed { c =>
        c.locations must have size 1
        c.locations must contain (location2)
        ()
      }
    }

    it("remove a location") {
      val location = factory.createLocation
      val centre = factory.createDisabledCentre.copy(locations = Set(location))

      centre.removeLocation(location.id) mustSucceed { c =>
        c.studyIds must have size 0
        ()
      }
    }

  }

}
