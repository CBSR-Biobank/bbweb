package org.biobank.domain.participants

import org.biobank.fixture.NameGenerator
import org.biobank.domain._
import org.biobank.domain.containers.{ ContainerId, ContainerSchemaPositionId }
import org.biobank.domain.study.SpecimenGroupId

import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class SpecimenSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A usable specimen" can {

    "be created" when {

      "valid arguments are used" in {
        val specimen = factory.createUsableSpecimen

        val v = UsableSpecimen.create(
                id               = specimen.id,
                specimenGroupId  = specimen.specimenGroupId,
                version          = -1,
                timeCreated      = DateTime.now,
                originLocationId = specimen.originLocationId,
                locationId       = specimen.locationId,
                containerId      = specimen.containerId,
                positionId       = specimen.positionId,
                amount           = specimen.amount)

        v mustSucceed { spc =>
          spc must have (
            'id              (specimen.id),
            'specimenGroupId (specimen.specimenGroupId),
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

    "not be created" when {

      "an empty id is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(""),
                specimenGroupId  = SpecimenGroupId(nameGenerator.next[SpecimenGroupId]),
                version          = -1,
                timeCreated      = DateTime.now,
                originLocationId = LocationId(nameGenerator.next[LocationId]),
                locationId       = LocationId(nameGenerator.next[LocationId]),
                containerId      = None,
                positionId       = None,
                amount           = BigDecimal(1.01))
        v mustFail "IdRequired"
      }

      "an empty specimen group id is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(nameGenerator.next[SpecimenId]),
                specimenGroupId  = SpecimenGroupId(""),
                version          = -1,
                timeCreated      = DateTime.now,
                originLocationId = LocationId(nameGenerator.next[LocationId]),
                locationId       = LocationId(nameGenerator.next[LocationId]),
                containerId      = None,
                positionId       = None,
                amount           = BigDecimal(1.01))
        v mustFail "InvalidSpecimenGroupId"
      }

      "an invalid version number is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(nameGenerator.next[SpecimenId]),
                specimenGroupId  = SpecimenGroupId(nameGenerator.next[SpecimenGroupId]),
                version          = -2,
                timeCreated      = DateTime.now,
                originLocationId = LocationId(nameGenerator.next[LocationId]),
                locationId       = LocationId(nameGenerator.next[LocationId]),
                containerId      = None,
                positionId       = None,
                amount           = BigDecimal(1.01))
        v mustFail "InvalidVersion"
      }

      "an empty origin location id is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(nameGenerator.next[SpecimenId]),
                specimenGroupId  = SpecimenGroupId(nameGenerator.next[SpecimenGroupId]),
                version          = -1,
                timeCreated      = DateTime.now,
                originLocationId = LocationId(""),
                locationId       = LocationId(nameGenerator.next[LocationId]),
                containerId      = None,
                positionId       = None,
                amount           = BigDecimal(1.01))
        v mustFail "OriginLocationIdInvalid"
      }

      "an empty location id is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(nameGenerator.next[SpecimenId]),
                specimenGroupId  = SpecimenGroupId(nameGenerator.next[SpecimenGroupId]),
                version          = -1,
                timeCreated      = DateTime.now,
                originLocationId = LocationId(nameGenerator.next[LocationId]),
                locationId       = LocationId(""),
                containerId      = None,
                positionId       = None,
                amount           = BigDecimal(1.01))
        v mustFail "LocationIdInvalid"
      }

      "an empty container id is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(nameGenerator.next[SpecimenId]),
                specimenGroupId  = SpecimenGroupId(nameGenerator.next[SpecimenGroupId]),
                version          = -1,
                timeCreated      = DateTime.now,
                originLocationId = LocationId(nameGenerator.next[LocationId]),
                locationId       = LocationId(nameGenerator.next[LocationId]),
                containerId      = Some(ContainerId("")),
                positionId       = None,
                amount           = BigDecimal(1.01))
        v mustFail "ContainerIdInvalid"
      }

      "an empty position id is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(nameGenerator.next[SpecimenId]),
                specimenGroupId  = SpecimenGroupId(nameGenerator.next[SpecimenGroupId]),
                version          = -1,
                timeCreated      = DateTime.now,
                originLocationId = LocationId(nameGenerator.next[LocationId]),
                locationId       = LocationId(nameGenerator.next[LocationId]),
                containerId      = None,
                positionId       = Some(ContainerSchemaPositionId("")),
                amount           = BigDecimal(1.01))
        v mustFail "PositionInvalid"
      }

      "an negative amount is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(nameGenerator.next[SpecimenId]),
                specimenGroupId  = SpecimenGroupId(nameGenerator.next[SpecimenGroupId]),
                version          = -1,
                timeCreated      = DateTime.now,
                originLocationId = LocationId(nameGenerator.next[LocationId]),
                locationId       = LocationId(nameGenerator.next[LocationId]),
                containerId      = None,
                positionId       = None,
                amount           = BigDecimal(-1))
        v mustFail "AmountInvalid"
      }

    }

  }
}

