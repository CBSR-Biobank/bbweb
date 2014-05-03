package org.biobank.domain.study

import org.biobank.domain.DomainSpec
import org.biobank.domain.AnnotationTypeId
import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnatomicalSourceType
import org.biobank.domain.PreservationType
import org.biobank.domain.PreservationTemperatureType
import org.biobank.domain.SpecimenType

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

      val specimenGroup = SpecimenGroup.create(studyId, id, version, name, description, units,
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
    }

    "be updated" in {
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

      val specimenGroup = SpecimenGroup.create(studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType) | fail

      val name2 = nameGenerator.next[SpecimenGroup]
      val description2 = some(nameGenerator.next[SpecimenGroup])
      val units2 = nameGenerator.next[SpecimenGroup]
      val anatomicalSourceType2 = AnatomicalSourceType.Colon
      val preservationType2 = PreservationType.RnaLater
      val preservationTemperatureType2 = PreservationTemperatureType.RoomTemperature
      val specimenType2 = SpecimenType.Plasma

      val updatedSg = specimenGroup.update(Some(0L), name2, description2, units2,
        anatomicalSourceType2, preservationType2, preservationTemperatureType2, specimenType2) | fail

      updatedSg should have (
        'studyId                       (studyId),
        'id                            (id),
        'version                       (1L),
        'name                          (name2),
        'description                   (description2),
        'units                         (units2),
        'anatomicalSourceType          (anatomicalSourceType2),
        'preservationType              (preservationType2),
        'preservationTemperatureType   (preservationTemperatureType2),
        'specimenType                  (specimenType2)
      )
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

      val v = SpecimenGroup.create(studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("study id is null or empty"))
      }
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

      SpecimenGroup.create(studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("specimen group id is null or empty"))
      }
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

       val validation = SpecimenGroup.create(studyId, id, version, name, description, units,
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

      val sg = SpecimenGroup.create(studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType) | fail

      val validation = sg.update(Some(10L), name, description, units,
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

      SpecimenGroup.create(studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("name is null or empty"))
      }

      name = ""
      SpecimenGroup.create(studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("name is null or empty"))
      }
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

      SpecimenGroup.create(studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("description is null or empty"))
      }

      description = Some("")
      SpecimenGroup.create(studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("description is null or empty"))
      }
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

      SpecimenGroup.create(studyId, id, version, name, description, units,
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should (have length 1 and contain("units is null or empty"))
      }
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
        anatomicalSourceType, preservationType, preservationTemperatureType, specimenType) match {
        case Success(user) => fail
        case Failure(err) =>
          err.list should have length 2
          err.list.head should be ("invalid version value: -2")
          err.list.tail.head should be ("units is null or empty")
      }
    }

  }

}
