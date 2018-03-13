package org.biobank.domain.study

import java.time.OffsetDateTime
import org.biobank.TestUtils
import org.biobank.domain.DomainSpec
import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnatomicalSourceType
import org.biobank.domain.PreservationType
import org.biobank.domain.PreservationTemperature
import org.biobank.domain.SpecimenType
import scalaz.Scalaz._

class SpecimenGroupSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val nameGenerator = new NameGenerator(this.getClass)

  describe("A specimen group can") {

    it("be created") {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = 0L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperature = PreservationTemperature.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      val v = SpecimenGroup.create(studyId,
                                   id,
                                   version,
                                   name,
                                   description,
                                   units,
                                   anatomicalSourceType,
                                   preservationType,
                                   preservationTemperature,
                                   specimenType)
      v mustSucceed { specimenGroup =>
        specimenGroup mustBe a[SpecimenGroup]

        specimenGroup must have (
          'studyId                       (studyId),
          'id                            (id),
          'version                       (0L),
          'name                          (name),
          'description                   (description),
          'units                         (units),
          'anatomicalSourceType          (anatomicalSourceType),
          'preservationType              (preservationType),
          'preservationTemperature   (preservationTemperature),
          'specimenType                  (specimenType)
        )

        TestUtils.checkTimeStamps(specimenGroup.timeAdded, OffsetDateTime.now)
        specimenGroup.timeModified mustBe (None)
        ()
      }
    }

    it("be updated") {
      val specimenGroup = factory.createSpecimenGroup

      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Colon
      val preservationType = PreservationType.RnaLater
      val preservationTemperature = PreservationTemperature.RoomTemperature
      val specimenType = SpecimenType.Plasma

      specimenGroup.update(name,
                           description,
                           units,
                           anatomicalSourceType,
                           preservationType,
                           preservationTemperature,
                           specimenType
      ).mustSucceed { updatedSg =>
        updatedSg must have (
          'studyId                     (specimenGroup.studyId),
          'id                          (specimenGroup.id),
          'version                     (specimenGroup.version + 1),
          'name                        (name),
          'description                 (description),
          'units                       (units),
          'anatomicalSourceType        (anatomicalSourceType),
          'preservationType            (preservationType),
          'preservationTemperature (preservationTemperature),
          'specimenType                (specimenType)
        )

        TestUtils.checkTimeStamps(specimenGroup.timeAdded, updatedSg.timeAdded)
        updatedSg.timeModified must not be (None)
        ()
      }
    }

  }

  describe("A specimen group") {

    it("not be created with an empty study id") {
      val studyId = StudyId("")
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = 0L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperature = PreservationTemperature.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      SpecimenGroup.create(
        studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperature, specimenType).fold(
        err => err.list.toList must (have length 1 and contain("IdRequired")),
        user => fail
      )
    }

    it("not be created with an empty id") {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId("")
      val version = 0L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperature = PreservationTemperature.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      SpecimenGroup.create(
        studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperature, specimenType).fold(
        err => err.list.toList must (have length 1 and contain("IdRequired")),
        user => fail
      )
    }

    it("not be created with an invalid version") {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = -2L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperature = PreservationTemperature.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

       val validation = SpecimenGroup.create(
         studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperature, specimenType)
      validation mustFail "InvalidVersion"
    }

    it("not be created with an null or empty name") {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = 0L
      var name: String = null
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperature = PreservationTemperature.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      SpecimenGroup.create(
        studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperature, specimenType).fold(
        err => err.list.toList must (have length 1 and contain("NameRequired")),
        user => fail
      )

      name = ""
      SpecimenGroup.create(
        studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperature, specimenType).fold(
        err => err.list.toList must (have length 1 and contain("NameRequired")),
        user => fail
      )
    }

    it("not be created with an empty description option") {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = 0L
      val name = nameGenerator.next[SpecimenGroup]
      var description: Option[String] = Some(null)
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperature = PreservationTemperature.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      SpecimenGroup.create(
        studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperature, specimenType).fold(
        err => err.list.toList must (have length 1 and contain("InvalidDescription")),
        user => fail
      )

      description = Some("")
      SpecimenGroup.create(studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperature, specimenType).fold(
        err => err.list.toList must (have length 1 and contain("InvalidDescription")),
        user => fail
      )
    }

    it("not be created with null or empty units") {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = 0L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = ""
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperature = PreservationTemperature.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      SpecimenGroup.create(studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperature, specimenType).fold(
        err => err.list.toList must (have length 1 and contain("UnitsRequired")),
        user => fail
      )
    }

    it("have more than one validation fail") {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = -2L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = ""
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperature = PreservationTemperature.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      SpecimenGroup.create(studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperature, specimenType).fold(
        err => {
          err.list must have length 2
          err.list.toList.head mustBe ("InvalidVersion")
          err.list.toList.tail.head mustBe ("UnitsRequired")
        },
        user => fail
      )
    }

  }

}
