package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.domain.AnnotationTypeId
import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnatomicalSourceType
import org.biobank.domain.PreservationType
import org.biobank.domain.PreservationTemperatureType
import org.biobank.domain.SpecimenType
import com.github.nscala_time.time.Imports._
import scalaz._
import scalaz.Scalaz._

class SpecimenGroupSpec extends DomainSpec {

  val nameGenerator = new NameGenerator(this.getClass)

  "A specimen group" can {

    "be created" in {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = -1L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperatureType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      val specimenGroup = SpecimenGroup.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType) | fail
      specimenGroup shouldBe a[SpecimenGroup]

      specimenGroup should have (
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

      (specimenGroup.addedDate to DateTime.now).millis should be < 100L
      specimenGroup.lastUpdateDate should be (None)
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

      val updatedSg = specimenGroup.update(
        specimenGroup.versionOption, org.joda.time.DateTime.now, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType) | fail

      updatedSg should have (
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

      (specimenGroup.addedDate to updatedSg.addedDate).millis should be < 100L
      val updateDate = updatedSg.lastUpdateDate | fail
      (updateDate to DateTime.now).millis should be < 100L
    }

  }

  "A specimen group" should {

    "not be created with an empty study id" in {
      val studyId = StudyId("")
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = -1L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperatureType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      val v = SpecimenGroup.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).fold(
        err => err.list should (have length 1 and contain("id is null or empty")),
        user => fail
      )
    }

    "not be created with an empty id" in {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId("")
      val version = -1L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperatureType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      SpecimenGroup.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).fold(
        err => err.list should (have length 1 and contain("id is null or empty")),
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
         studyId, id, version, org.joda.time.DateTime.now, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType)
      validation should be ('failure)

      validation.swap.map { err =>
          err.list should (have length 1 and contain("invalid version value: -2"))
      }
    }

    "not be updated with an invalid version" in {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = -1L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperatureType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      val sg = SpecimenGroup.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType) | fail

      val validation = sg.update(Some(10L), org.joda.time.DateTime.now, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType)
      validation should be ('failure)

      validation.swap.map { err =>
        err.list should have length 1
        err.list.head should include ("expected version doesn't match current version")
      }
    }

    "not be created with an null or empty name" in {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = -1L
      var name: String = null
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperatureType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      SpecimenGroup.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).fold(
        err => err.list should (have length 1 and contain("name is null or empty")),
        user => fail
      )

      name = ""
      SpecimenGroup.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).fold(
        err => err.list should (have length 1 and contain("name is null or empty")),
        user => fail
      )
    }

    "not be created with an empty description option" in {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = -1L
      val name = nameGenerator.next[SpecimenGroup]
      var description: Option[String] = Some(null)
      val units = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperatureType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      SpecimenGroup.create(
        studyId, id, version, org.joda.time.DateTime.now, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).fold(
        err => err.list should (have length 1 and contain("description is null or empty")),
        user => fail
      )

      description = Some("")
      SpecimenGroup.create(studyId, id, version, org.joda.time.DateTime.now, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).fold(
        err => err.list should (have length 1 and contain("description is null or empty")),
        user => fail
      )
    }

    "not be created with null or empty units" in {
      val studyId = StudyId(nameGenerator.next[SpecimenGroup])
      val id = SpecimenGroupId(nameGenerator.next[SpecimenGroup])
      val version = -1L
      val name = nameGenerator.next[SpecimenGroup]
      val description = some(nameGenerator.next[SpecimenGroup])
      val units = ""
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FrozenSpecimen
      val preservationTemperatureType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.BuffyCoat

      SpecimenGroup.create(studyId, id, version, org.joda.time.DateTime.now, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).fold(
        err => err.list should (have length 1 and contain("units is null or empty")),
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

      SpecimenGroup.create(studyId, id, version, org.joda.time.DateTime.now, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType).fold(
        err => {
          err.list should have length 2
          err.list.head should be ("invalid version value: -2")
          err.list.tail.head should be ("units is null or empty")
        },
        user => fail
      )
    }

  }

}
