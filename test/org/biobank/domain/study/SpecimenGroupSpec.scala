package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnatomicalSourceType
import org.biobank.domain.PreservationType
import org.biobank.domain.PreservationTemperatureType
import org.biobank.domain.SpecimenType

import com.github.nscala_time.time.Imports._
import scalaz.Scalaz._

class SpecimenGroupSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val nameGenerator = new NameGenerator(this.getClass)

  "A specimen group" can {

    "be created" in {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = 0L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperatureType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      val v = SpecimenGroup.create(
        studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType)
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
          'preservationTemperatureType   (preservationTemperatureType),
          'specimenType                  (specimenType)
        )

        (specimenGroup.timeAdded to DateTime.now).millis must be < 100L
        specimenGroup.timeModified mustBe (None)
      }
    }

    "be updated" in {
      val specimenGroup = factory.createSpecimenGroup

      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Colon
      val preservationType = PreservationType.RnaLater
      val preservationTemperatureType = PreservationTemperatureType.RoomTemperature
      val specimenType = SpecimenType.Plasma

      specimenGroup.update(name,
                           description,
                           units,
                           anatomicalSourceType,
                           preservationType,
                           preservationTemperatureType,
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
          'preservationTemperatureType (preservationTemperatureType),
          'specimenType                (specimenType)
        )

        (specimenGroup.timeAdded to updatedSg.timeAdded).millis must be < 100L
        updatedSg.timeModified must not be (None)
      }
    }

  }

  "A specimen group" must {

    "not be created with an empty study id" in {
      val studyId = StudyId("")
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = 0L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperatureType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      SpecimenGroup.create(
        studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).fold(
        err => err.list.toList must (have length 1 and contain("IdRequired")),
        user => fail
      )
    }

    "not be created with an empty id" in {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId("")
      val version = 0L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperatureType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      SpecimenGroup.create(
        studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).fold(
        err => err.list.toList must (have length 1 and contain("IdRequired")),
        user => fail
      )
    }

    "not be created with an invalid version" in {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = -2L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperatureType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

       val validation = SpecimenGroup.create(
         studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType)
      validation mustFail "InvalidVersion"
    }

    "not be created with an null or empty name" in {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = 0L
      var name: String = null
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperatureType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      SpecimenGroup.create(
        studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).fold(
        err => err.list.toList must (have length 1 and contain("NameRequired")),
        user => fail
      )

      name = ""
      SpecimenGroup.create(
        studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).fold(
        err => err.list.toList must (have length 1 and contain("NameRequired")),
        user => fail
      )
    }

    "not be created with an empty description option" in {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = 0L
      val name = nameGenerator.next[SpecimenGroup]
      var description: Option[String] = Some(null)
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperatureType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      SpecimenGroup.create(
        studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).fold(
        err => err.list.toList must (have length 1 and contain("InvalidDescription")),
        user => fail
      )

      description = Some("")
      SpecimenGroup.create(studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).fold(
        err => err.list.toList must (have length 1 and contain("InvalidDescription")),
        user => fail
      )
    }

    "not be created with null or empty units" in {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = 0L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = ""
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperatureType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      SpecimenGroup.create(studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).fold(
        err => err.list.toList must (have length 1 and contain("UnitsRequired")),
        user => fail
      )
    }

    "have more than one validation fail" in {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = -2L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = ""
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperatureType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      SpecimenGroup.create(studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).fold(
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
