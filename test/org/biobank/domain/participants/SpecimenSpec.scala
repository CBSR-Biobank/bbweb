package org.biobank.domain.participants

import org.biobank.fixture.NameGenerator
import org.biobank.domain._
import org.biobank.domain.containers.{ ContainerId, ContainerSchemaPositionId }
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class SpecimenSpec extends DomainFreeSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def createFrom(specimen: Specimen): DomainValidation[Specimen] =
    UsableSpecimen.create(id               = specimen.id,
                          inventoryId      = specimen.inventoryId,
                          specimenSpecId   = specimen.specimenSpecId,
                          version          = specimen.version,
                          timeCreated      = specimen.timeCreated,
                          originLocationId = specimen.originLocationId,
                          locationId       = specimen.locationId,
                          containerId      = specimen.containerId,
                          positionId       = specimen.positionId,
                          amount           = specimen.amount)

  "A usable specimen" - {

    "can be created" - {

      "when valid arguments are used" in {
        val specimen = factory.createUsableSpecimen.copy(version = 0L)
        createFrom(specimen) mustSucceed { spc =>
          spc must have (
            'id              (specimen.id),
            'inventoryId     (specimen.inventoryId),
            'specimenSpecId  (specimen.specimenSpecId),
            'version         (0),
            'originLocationId(specimen.originLocationId),
            'locationId      (specimen.locationId),
            'containerId     (specimen.containerId),
            'positionId      (specimen.positionId),
            'amount          (specimen.amount)
          )

          checkTimeStamps(specimen, spc.timeAdded, spc.timeModified)
          checkTimeStamps(specimen.timeCreated, spc.timeCreated)
        }
      }

    }

    "can be updated" - {

      "with a new inventory ID" in {
        val specimen = factory.createUsableSpecimen
        val newInventoryId = nameGenerator.next[Specimen]

        specimen.withInventoryId(newInventoryId) mustSucceed { s =>
          s.inventoryId must be (newInventoryId)
          s.version must be (specimen.version + 1)
          checkTimeStamps(s, specimen.timeAdded, DateTime.now)
        }
      }

      "with a new amount" in {
        val specimen = factory.createUsableSpecimen
        val newAmount = specimen.amount + 1

        specimen.withAmount(newAmount) mustSucceed { s =>
          s.amount must be (newAmount)
          s.version must be (specimen.version + 1)
          checkTimeStamps(s, specimen.timeAdded, DateTime.now)
        }
      }

      "with a new origin location" in {
        val specimen = factory.createUsableSpecimen
        val newLocation = factory.createLocation

        specimen.withOriginLocation(newLocation) mustSucceed { s =>
          s.originLocationId must be (newLocation.uniqueId)
          s.version must be (specimen.version + 1)
          checkTimeStamps(s, specimen.timeAdded, DateTime.now)
        }
      }

      "with a new location" in {
        val specimen = factory.createUsableSpecimen
        val newLocation = factory.createLocation

        specimen.withLocation(newLocation) mustSucceed { s =>
          s.locationId must be (newLocation.uniqueId)
          s.version must be (specimen.version + 1)
          checkTimeStamps(s, specimen.timeAdded, DateTime.now)
        }
      }

      "with a new position" in {
        val specimen = factory.createUsableSpecimen
        val newPosition = ContainerSchemaPositionId(nameGenerator.next[Specimen])

        specimen.withPosition(newPosition) mustSucceed { s =>
          s.positionId mustBe Some(newPosition)
          s.version must be (specimen.version + 1)
          checkTimeStamps(s, specimen.timeAdded, DateTime.now)
        }
      }
    }

    "can be made unusable" - {

      "from a usable specimen" in {
        val specimen = factory.createUsableSpecimen

        specimen.makeUnusable mustSucceed { s =>
          s mustBe a[UnusableSpecimen]
          s.version must be (specimen.version + 1)
          checkTimeStamps(s, specimen.timeAdded, DateTime.now)
        }
      }

    }

    "cannot be created" - {

      "with an empty id" in {
        val specimen = factory.createUsableSpecimen.copy(id = SpecimenId(""))
        createFrom(specimen) mustFail "IdRequired"
      }

      "with an empty inventory id" in {
        val specimen = factory.createUsableSpecimen.copy(inventoryId = "")
        createFrom(specimen) mustFail "InventoryIdInvalid"
      }

      "with an empty specimen spec id" in {
        val specimen = factory.createUsableSpecimen.copy(specimenSpecId = "")
        createFrom(specimen) mustFail "SpecimenSpecIdInvalid"
      }

      "with an invalid version number" in {
        val specimen = factory.createUsableSpecimen.copy(version = -2)
        createFrom(specimen) mustFail "InvalidVersion"
      }

      "with an empty origin location id" in {
        val specimen = factory.createUsableSpecimen.copy(originLocationId = "")
        createFrom(specimen) mustFail "OriginLocationIdInvalid"
      }

      "with an empty location id" in {
        val specimen = factory.createUsableSpecimen.copy(locationId = "")
        createFrom(specimen) mustFail "LocationIdInvalid"
      }

      "with an empty container id" in {
        val specimen = factory.createUsableSpecimen.copy(containerId = Some(ContainerId("")))
        createFrom(specimen) mustFail "ContainerIdInvalid"
      }

      "with an empty position id" in {
        val specimen = factory.createUsableSpecimen.copy(positionId = Some(ContainerSchemaPositionId("")))
        createFrom(specimen) mustFail "PositionInvalid"
      }

      "with a negative amount" in {
        val specimen = factory.createUsableSpecimen.copy(amount           = BigDecimal(-1))
        createFrom(specimen) mustFail "AmountInvalid"
      }

    }

  }

  "cannot be updated" - {

    "with an invalid inventory ID" in {
      val specimen = factory.createUsableSpecimen
      specimen.withInventoryId("") mustFail "InventoryIdInvalid"
    }

    "with an invalid amount" in {
      val specimen = factory.createUsableSpecimen
      specimen.withAmount(BigDecimal("-1")) mustFail "AmountInvalid"
    }

    "with an invalid origin location" in {
      val specimen = factory.createUsableSpecimen
      val newLocation = factory.createLocation.copy(uniqueId = "")
      specimen.withOriginLocation(newLocation) mustFail "LocationIdInvalid"
    }

    "with an invalid location" in {
      val specimen = factory.createUsableSpecimen
      val newLocation = factory.createLocation.copy(uniqueId = "")
      specimen.withLocation(newLocation) mustFail "LocationIdInvalid"
    }

    "with an invalid position" in {
      val specimen = factory.createUsableSpecimen
      val newPosition = ContainerSchemaPositionId("")

      specimen.withPosition(newPosition) mustFail "PositionInvalid"
    }
  }

  "can be made unusable" - {

    "from a usable specimen" in {
      val specimen = factory.createUsableSpecimen

      specimen.makeUnusable mustSucceed { s =>
        s mustBe a[UnusableSpecimen]
        s.version must be (specimen.version + 1)
        checkTimeStamps(s, specimen.timeAdded, DateTime.now)
      }
    }

  }

  "A usable specimen" - {

    "can be made usable" - {

      "from a unusable specimen" in {
        val specimen = factory.createUnusableSpecimen

        specimen.makeUsable mustSucceed { s =>
          s mustBe a[UsableSpecimen]
          s.version must be (specimen.version + 1)
          checkTimeStamps(s, specimen.timeAdded, DateTime.now)
        }
      }

    }

  }

  "Specimens can be compared" - {

    "by specimen ID" in {
      val (specimen1, specimen2) = (factory.createUsableSpecimen.copy(id = SpecimenId("A")),
                                    factory.createUsableSpecimen.copy(id = SpecimenId("B")))
      Specimen.compareById(specimen1, specimen2) mustBe true
      Specimen.compareById(specimen2, specimen1) mustBe false
    }

    "by inventory ID" in {
      val (specimen1, specimen2) = (factory.createUsableSpecimen.copy(inventoryId = "A"),
                                    factory.createUsableSpecimen.copy(inventoryId = "B"))
      Specimen.compareByInventoryId(specimen1, specimen2) mustBe true
      Specimen.compareByInventoryId(specimen2, specimen1) mustBe false
    }

    "by time created" in {
      val (specimen1, specimen2) =
        (factory.createUsableSpecimen.copy(timeCreated = DateTime.now.minusDays(1)),
         factory.createUsableSpecimen.copy(timeCreated = DateTime.now))
      Specimen.compareByTimeCreated(specimen1, specimen2) mustBe true
      Specimen.compareByTimeCreated(specimen2, specimen1) mustBe false
    }

    "by status" in {
      val (specimen1, specimen2) = (factory.createUnusableSpecimen, factory.createUsableSpecimen)
      Specimen.compareByStatus(specimen1, specimen2) mustBe true
      Specimen.compareByStatus(specimen2, specimen1) mustBe false
    }
  }
}
