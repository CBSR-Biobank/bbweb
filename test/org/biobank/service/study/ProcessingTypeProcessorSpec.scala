package org.biobank.service

import org.biobank.fixture._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  AnatomicalSourceType,
  AnnotationTypeId,
  AnnotationValueType,
  DomainError,
  DomainValidation,
  PreservationType,
  PreservationTemperatureType,
  RepositoriesComponent,
  SpecimenType
}
import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._

import org.slf4j.LoggerFactory
import akka.pattern.ask
import org.scalatest.OptionValues._
import org.joda.time.DateTime
import org.scalatest.Tag
import org.scalatest.BeforeAndAfterEach
import scalaz._
import scalaz.Scalaz._

/**
  * Tests for actor ProcessingTypeProcessorSpec. These are written using ScalaTest.
  *
  */
class ProcessingTypeProcessorSpec extends StudiesProcessorFixture {
  import org.biobank.TestUtils._

  private val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  var disabledStudy: DisabledStudy = null

  private def askAddCommand(procType: ProcessingType): DomainValidation[ProcessingTypeAddedEvent] = {
    val cmd = AddProcessingTypeCmd(
      procType.studyId.id,
      procType.name,
      procType.description,
      procType.enabled)
    ask(studiesProcessor, cmd).mapTo[DomainValidation[ProcessingTypeAddedEvent]].futureValue
  }

  private def askUpdateCommand(procType: ProcessingType): DomainValidation[ProcessingTypeUpdatedEvent] = {
    val cmd = UpdateProcessingTypeCmd(
      procType.studyId.id,
      procType.id.id,
      procType.version,
      procType.name,
      procType.description,
      procType.enabled)
    ask(studiesProcessor, cmd).mapTo[DomainValidation[ProcessingTypeUpdatedEvent]].futureValue
  }

  private def askRemoveCommand(procType: ProcessingType): DomainValidation[ProcessingTypeRemovedEvent] = {
    val cmd = RemoveProcessingTypeCmd(
      procType.studyId.id,
      procType.id.id,
      procType.version)
    ask(studiesProcessor, cmd).mapTo[DomainValidation[ProcessingTypeRemovedEvent]].futureValue
  }

  // create the study to be used for each tests*
  override def beforeEach: Unit = {
    disabledStudy = factory.createDisabledStudy
    studyRepository.put(disabledStudy)
    ()
  }

  "A study processor" can {

    "add a processing type" in {
      val procType = factory.createProcessingType

      askAddCommand(procType) shouldSucceed { event =>
        event shouldBe a[ProcessingTypeAddedEvent]
        event should have(
          'name (procType.name),
          'description (procType.description),
          'enabled (procType.enabled))

        processingTypeRepository.allForStudy(disabledStudy.id) should have size 1
        processingTypeRepository.withId(
          disabledStudy.id, ProcessingTypeId(event.processingTypeId)) shouldSucceed { repoPt =>
          repoPt.version should be(0)
          checkTimeStamps(repoPt, DateTime.now, None)
        }
      }
    }

    "not add a processing type to a study that does not exist" in {
      val study2 = factory.createDisabledStudy
      val procType = factory.createProcessingType
      askAddCommand(procType) shouldFail s"${study2.id.id}.*not found"
    }

    "not add a processing type with a name that already exists" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)
      askAddCommand(procType) shouldFail "name already exists"
    }

    "update a processing type" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)

      val procType2 = procType.copy(name = nameGenerator.next[String])

      askUpdateCommand(procType2) shouldSucceed { event =>
        event shouldBe a[ProcessingTypeUpdatedEvent]
        event should have(
          'name (procType2.name),
          'description (procType2.description),
          'enabled (procType2.enabled))

        processingTypeRepository.allForStudy(disabledStudy.id) should have size 1
        processingTypeRepository.withId(
          disabledStudy.id, ProcessingTypeId(event.processingTypeId)) shouldSucceed { repoPt =>
          repoPt.version should be(1)
          checkTimeStamps(repoPt, procType.timeAdded, DateTime.now)
        }
      }
    }

    "not update a processing type to name that already exists" in {
      val procType1 = factory.createProcessingType
      processingTypeRepository.put(procType1)

      val procType2 = factory.createProcessingType
      processingTypeRepository.put(procType2)

      val procType3 = procType2.copy(name = procType1.name)
      askUpdateCommand(procType3) shouldFail "name already exists"
    }

    "not update a processing type to wrong study" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val procType2 = procType.copy(studyId = study2.id)
      askUpdateCommand(procType2) shouldFail "study does not have processing type"
    }

    "not update a processing type with an invalid version" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)

      val procTypeBadVersion = procType.copy(version = procType.version + 1)
      askUpdateCommand(procTypeBadVersion) shouldFail "doesn't match current version"
    }

    "remove a processing type" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)

      askRemoveCommand(procType) shouldSucceed { event =>
        event shouldBe a[ProcessingTypeRemovedEvent]
        val v = processingTypeRepository.withId(disabledStudy.id, ProcessingTypeId(event.processingTypeId))
        v shouldFail "processing type does not exist"
      }
    }

    "not remove a processing type with an invalid version" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)

      val procTypeBadVersion = procType.copy(version = procType.version - 2)
      askRemoveCommand(procTypeBadVersion) shouldFail "expected version doesn't match current version"
    }

  }
}
