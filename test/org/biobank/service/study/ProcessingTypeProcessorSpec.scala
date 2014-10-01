package org.biobank.service.study

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
  SpecimenType
}
import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._

import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import org.scalatest.Tag
import scalaz._
import scalaz.Scalaz._

/**
  * Tests for actor ProcessingTypeProcessorSpec. These are written using ScalaTest.
  *
  */
class ProcessingTypeProcessorSpec extends TestFixture {
  import org.biobank.TestUtils._

  private val log = LoggerFactory.getLogger(this.getClass)

  val studyRepository = inject [StudyRepository]

  val processingTypeRepository = inject [ProcessingTypeRepository]

  val studiesProcessor = injectActorRef [StudiesProcessor]

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

      askAddCommand(procType) mustSucceed { event =>
        event mustBe a[ProcessingTypeAddedEvent]
        event must have(
          'name (procType.name),
          'description (procType.description),
          'enabled (procType.enabled))

        processingTypeRepository.allForStudy(disabledStudy.id) must have size 1
        processingTypeRepository.withId(
          disabledStudy.id, ProcessingTypeId(event.processingTypeId)) mustSucceed { repoPt =>
          repoPt.version mustBe(0)
          checkTimeStamps(repoPt, DateTime.now, None)
        }
      }
    }

    "not add a processing type to a study that does not exist" in {
      val study2 = factory.createDisabledStudy
      val procType = factory.createProcessingType
      askAddCommand(procType) mustFail s"invalid study id: ${study2.id.id}"
    }

    "not add a processing type with a name that already exists" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)
      askAddCommand(procType) mustFail "name already exists"
    }

    "update a processing type" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)

      val procType2 = procType.copy(name = nameGenerator.next[String])

      askUpdateCommand(procType2) mustSucceed { event =>
        event mustBe a[ProcessingTypeUpdatedEvent]
        event must have(
          'name (procType2.name),
          'description (procType2.description),
          'enabled (procType2.enabled))

        processingTypeRepository.allForStudy(disabledStudy.id) must have size 1
        processingTypeRepository.withId(
          disabledStudy.id, ProcessingTypeId(event.processingTypeId)) mustSucceed { repoPt =>
          repoPt.version mustBe(1)
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
      askUpdateCommand(procType3) mustFail "name already exists"
    }

    "not update a processing type to wrong study" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val procType2 = procType.copy(studyId = study2.id)
      askUpdateCommand(procType2) mustFail "study does not have processing type"
    }

    "not update a processing type with an invalid version" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)

      val procTypeBadVersion = procType.copy(version = procType.version + 1)
      askUpdateCommand(procTypeBadVersion) mustFail "doesn't match current version"
    }

    "remove a processing type" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)

      askRemoveCommand(procType) mustSucceed { event =>
        event mustBe a[ProcessingTypeRemovedEvent]
        val v = processingTypeRepository.withId(disabledStudy.id, ProcessingTypeId(event.processingTypeId))
        v mustFail "processing type does not exist"
      }
    }

    "not remove a processing type with an invalid version" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)

      val procTypeBadVersion = procType.copy(version = procType.version - 2)
      askRemoveCommand(procTypeBadVersion) mustFail "expected version doesn't match current version"
    }

  }
}
