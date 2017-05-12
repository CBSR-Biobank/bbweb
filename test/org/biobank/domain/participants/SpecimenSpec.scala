package org.biobank.domain.participants

import org.biobank.fixture.NameGenerator
import org.biobank.domain._
import org.biobank.domain.containers.{ ContainerId, ContainerSchemaPositionId }
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class SpecimenSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def createFrom(specimen: Specimen): DomainValidation[Specimen] =
    UsableSpecimen.create(id               = specimen.id,
                          inventoryId      = specimen.inventoryId,
                          specimenSpecId   = specimen.specimenSpecId,
                          version          = specimen.version,
                          timeAdded        = DateTime.now,
                          timeCreated      = specimen.timeCreated,
                          originLocationId = specimen.originLocationId,
                          locationId       = specimen.locationId,
                          containerId      = specimen.containerId,
                          positionId       = specimen.positionId,
                          amount           = specimen.amount)

  describe("A usable specimen") {

    describe("can be created") {

      it("when valid arguments are used") {
        val specimen = factory.createUsableSpecimen.copy(version = 0L)
        createFrom(specimen) mustSucceed { spc =>
          spc must have (
            'id              (specimen.id),
            'inventoryId     (specimen.inventoryId),
            'specimenSpecId  (specimen.specimenSpecId),
            'version         (0),
            'originLocationId(specimen.originLocationId.id),
            'locationId      (specimen.locationId.id),
            'containerId     (specimen.containerId),
            'positionId      (specimen.positionId),
            'amount          (specimen.amount)
          )

          checkTimeStamps(specimen, spc.timeAdded, spc.timeModified)
          checkTimeStamps(specimen.timeCreated, spc.timeCreated)
        }
      }

    }

    describe("can be updated") {

      it("with a new inventory ID") {
        val specimen = factory.createUsableSpecimen
        val newInventoryId = nameGenerator.next[Specimen]

        specimen.withInventoryId(newInventoryId) mustSucceed { s =>
          s.inventoryId must be (newInventoryId)
          s.version must be (specimen.version + 1)
          checkTimeStamps(s, specimen.timeAdded, DateTime.now)
        }
      }

      it("with a new amount") {
        val specimen = factory.createUsableSpecimen
        val newAmount = specimen.amount + 1

        specimen.withAmount(newAmount) mustSucceed { s =>
          s.amount must be (newAmount)
          s.version must be (specimen.version + 1)
          checkTimeStamps(s, specimen.timeAdded, DateTime.now)
        }
      }

      it("with a new origin location") {
        val specimen = factory.createUsableSpecimen
        val newLocation = factory.createLocation

        specimen.withOriginLocation(newLocation) mustSucceed { s =>
          s.originLocationId must be (newLocation.uniqueId)
          s.version must be (specimen.version + 1)
          checkTimeStamps(s, specimen.timeAdded, DateTime.now)
        }
      }

      it("with a new location") {
        val specimen = factory.createUsableSpecimen
        val newLocation = factory.createLocation

        specimen.withLocation(newLocation) mustSucceed { s =>
          s.locationId must be (newLocation.uniqueId)
          s.version must be (specimen.version + 1)
          checkTimeStamps(s, specimen.timeAdded, DateTime.now)
        }
      }

      it("with a new position") {
        val specimen = factory.createUsableSpecimen
        val newPosition = ContainerSchemaPositionId(nameGenerator.next[Specimen])

        specimen.withPosition(newPosition) mustSucceed { s =>
          s.positionId mustBe Some(newPosition)
          s.version must be (specimen.version + 1)
          checkTimeStamps(s, specimen.timeAdded, DateTime.now)
        }
      }
    }

    describe("can be made unusable") {

      it("from a usable specimen") {
        val specimen = factory.createUsableSpecimen

        specimen.makeUnusable mustSucceed { s =>
          s mustBe a[UnusableSpecimen]
          s.version must be (specimen.version + 1)
          checkTimeStamps(s, specimen.timeAdded, DateTime.now)
        }
      }

    }

    describe("cannot be created") {

      it("with an empty id") {
        val specimen = factory.createUsableSpecimen.copy(id = SpecimenId(""))
        createFrom(specimen) mustFail "IdRequired"
      }

      it("with an empty inventory id") {
        val specimen = factory.createUsableSpecimen.copy(inventoryId = "")
        createFrom(specimen) mustFail "InventoryIdInvalid"
      }

      it("with an empty specimen spec id") {
        val specimen = factory.createUsableSpecimen.copy(specimenSpecId = "")
        createFrom(specimen) mustFail "SpecimenSpecIdInvalid"
      }

      it("with an invalid version number") {
        val specimen = factory.createUsableSpecimen.copy(version = -2)
        createFrom(specimen) mustFail "InvalidVersion"
      }

      it("with an empty origin location id") {
        val specimen = factory.createUsableSpecimen.copy(originLocationId = LocationId(""))
        createFrom(specimen) mustFail "OriginLocationIdInvalid"
      }

      it("with an empty location id") {
        val specimen = factory.createUsableSpecimen.copy(locationId = LocationId(""))
        createFrom(specimen) mustFail "LocationIdInvalid"
      }

      it("with an empty container id") {
        val specimen = factory.createUsableSpecimen.copy(containerId = Some(ContainerId("")))
        createFrom(specimen) mustFail "ContainerIdInvalid"
      }

      it("with an empty position id") {
        val specimen = factory.createUsableSpecimen.copy(positionId = Some(ContainerSchemaPositionId("")))
        createFrom(specimen) mustFail "PositionInvalid"
      }

      it("with a negative amount") {
        val specimen = factory.createUsableSpecimen.copy(amount           = BigDecimal(-1))
        createFrom(specimen) mustFail "AmountInvalid"
      }

    }

  }

  describe("cannot be updated") {

    it("with an invalid inventory ID") {
      val specimen = factory.createUsableSpecimen
      specimen.withInventoryId("") mustFail "InventoryIdInvalid"
    }

    it("with an invalid amount") {
      val specimen = factory.createUsableSpecimen
      specimen.withAmount(BigDecimal("-1")) mustFail "AmountInvalid"
    }

    it("with an invalid origin location") {
      val specimen = factory.createUsableSpecimen
      val newLocation = factory.createLocation.copy(uniqueId = LocationId(""))
      specimen.withOriginLocation(newLocation) mustFail "LocationIdInvalid"
    }

    it("with an invalid location") {
      val specimen = factory.createUsableSpecimen
      val newLocation = factory.createLocation.copy(uniqueId = LocationId(""))
      specimen.withLocation(newLocation) mustFail "LocationIdInvalid"
    }

    it("with an invalid position") {
      val specimen = factory.createUsableSpecimen
      val newPosition = ContainerSchemaPositionId("")

      specimen.withPosition(newPosition) mustFail "PositionInvalid"
    }
  }

  describe("can be made unusable") {

    it("from a usable specimen") {
      val specimen = factory.createUsableSpecimen

      specimen.makeUnusable mustSucceed { s =>
        s mustBe a[UnusableSpecimen]
        s.version must be (specimen.version + 1)
        checkTimeStamps(s, specimen.timeAdded, DateTime.now)
      }
    }

  }

  describe("A usable specimen") {

    describe("can be made usable") {

      it("from a unusable specimen") {
        val specimen = factory.createUnusableSpecimen

        specimen.makeUsable mustSucceed { s =>
          s mustBe a[UsableSpecimen]
          s.version must be (specimen.version + 1)
          checkTimeStamps(s, specimen.timeAdded, DateTime.now)
        }
      }

    }

  }

  describe("Specimens can be compared") {

    it("by specimen ID") {
      val (specimen1, specimen2) = (factory.createUsableSpecimen.copy(id = SpecimenId("A")),
                                    factory.createUsableSpecimen.copy(id = SpecimenId("B")))
      Specimen.compareById(specimen1, specimen2) mustBe true
      Specimen.compareById(specimen2, specimen1) mustBe false
    }

    it("by inventory ID") {
      val (specimen1, specimen2) = (factory.createUsableSpecimen.copy(inventoryId = "A"),
                                    factory.createUsableSpecimen.copy(inventoryId = "B"))
      Specimen.compareByInventoryId(specimen1, specimen2) mustBe true
      Specimen.compareByInventoryId(specimen2, specimen1) mustBe false
    }

    it("by time created") {
      val (specimen1, specimen2) =
        (factory.createUsableSpecimen.copy(timeCreated = DateTime.now.minusDays(1)),
         factory.createUsableSpecimen.copy(timeCreated = DateTime.now))
      Specimen.compareByTimeCreated(specimen1, specimen2) mustBe true
      Specimen.compareByTimeCreated(specimen2, specimen1) mustBe false
    }

    it("by state") {
      val (specimen1, specimen2) = (factory.createUnusableSpecimen, factory.createUsableSpecimen)
      Specimen.compareByState(specimen1, specimen2) mustBe true
      Specimen.compareByState(specimen2, specimen1) mustBe false
    }
  }
}
