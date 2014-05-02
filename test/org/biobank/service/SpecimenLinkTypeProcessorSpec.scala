package org.biobank.service

import org.biobank.fixture._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  DomainError,
  DomainValidation
}
import org.biobank.domain.study._

import org.slf4j.LoggerFactory
import akka.pattern.ask
import scalaz._
import scalaz.Scalaz._

class SpecimenLinkTypeProcessorSpec extends StudyProcessorFixture {

  private val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A study processor" can {

    "add a specimen link type" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slt = factory.createSpecimenLinkType

      val cmd = AddSpecimenLinkTypeCmd(pt.id.id, slt.expectedInputChange, slt.expectedOutputChange,
        slt.inputCount, slt.outputCount, slt.inputGroupId, slt.outputGroupId,
        slt.inputContainerTypeId, slt.outputContainerTypeId, slt.annotationTypeData)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[SpecimenLinkTypeAddedEvent]]
        .futureValue

      validation should be('success)
      validation map { event =>
        event shouldBe a[SpecimenLinkTypeAddedEvent]
        event should have(
	  'processingTypeId      (pt.id.id),
	  'version               (slt.version),
	  'expectedInputChange   (slt.expectedInputChange),
	  'expectedOutputChange  (slt.expectedOutputChange),
	  'inputCount            (slt.inputCount),
	  'outputCount           (slt.outputCount),
	  'inputGroupId          (slt.inputGroupId),
	  'outputGroupId         (slt.outputGroupId),
	  'inputContainerTypeId  (slt.inputContainerTypeId),
	  'outputContainerTypeId (slt.outputContainerTypeId),
	  'annotationTypeData    (slt.annotationTypeData)
	)

        val slt2 = specimenLinkTypeRepository.withId(
          pt.id, SpecimenLinkTypeId(event.specimenLinkTypeId)) | fail
        slt2.version should be (0)
        specimenLinkTypeRepository.allForProcessingType(pt.id) should have size 1
      }
    }

    "not add a specimen link type with the same specimen group as input and output" in {
      val pt = factory.createProcessingType
      processingTypeRepository.put(pt)

      val slt = factory.createSpecimenLinkType
      specimenLinkTypeRepository.put(slt)

      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val cmd = AddSpecimenLinkTypeCmd(pt.id.id, slt.expectedInputChange, slt.expectedOutputChange,
        slt.inputCount, slt.outputCount, sg.id, sg.id,
        slt.inputContainerTypeId, slt.outputContainerTypeId, slt.annotationTypeData)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[SpecimenLinkTypeAddedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
	log.info(s"error: ${err.list.head}")
        err.list.head should include("name already exists")
      }
    }

    "update a specimen link type" in {
    }

    "not update a specimen link type to name that already exists" in {
    }

    "not update a specimen link type to wrong study" in {
    }

    "not update a specimen link type with an invalid version" in {
    }

    "remove a specimen link type" in {
    }

    "not remove a specimen link type  with an invalid version" in {
    }

    "add a specimen group to a specimen link type" in {
    }

    "update a specimen link type and add specimen groups" in {
    }

    "not update a specimen group if it used by specimen link type" in {
    }

    "remove a specimen group from specimen link type" in {
    }

    "not remove a specimen group if used by specimen link type" in {
    }

    "not add a specimen group from a different study" in {
    }

    "not update a specimen link type with a specimen group from a different study" in {
    }

    "add an annotation type to a specimen link" in {
    }

    "not update an annotation type if used by specimen link type" in {
    }

    "remove an annotation type from specimen link type" in {
    }

    "not remove an annotation type if it is used by specimen link type" in {
    }

    "not add an annotation type if it is in wrong study" in {

    }
  }
}

