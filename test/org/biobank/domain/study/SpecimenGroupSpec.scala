package org.biobank.domain.study

import org.biobank.domain.AnnotationTypeId
import org.biobank.fixture.NameGenerator
import org.biobank.domain.AnatomicalSourceType
import org.biobank.domain.PreservationType
import org.biobank.domain.PreservationTemperatureType
import org.biobank.domain.SpecimenType

import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import scalaz._
import scalaz.Scalaz._


class SpecimenGroupSpec extends WordSpecLike with Matchers {

  val nameGenerator = new NameGenerator(this.getClass.getName)

  "A specimen group type" can {

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

      val v = SpecimenGroup.create(studyId, id, version, name, description, units,
	anatomicalSourceType, preservationType, preservationTemperatureType, specimenType)
      val cet = v.getOrElse(fail)
      cet shouldBe a[SpecimenGroup]
    }

  }

  "A specimen group type" should {

    "not be created with an empty study id" in {
      fail
    }

    "not be created with an empty id" in {
      fail
    }

    "not be created with an invalid version" in {
      fail
    }

    "not be created with an null or empty name" in {
      fail
    }

    "not be created with an empty description option" in {
      fail
    }

    "not be created with null or empty units" in {
      fail
    }

  }

}
