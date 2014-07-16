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
  RepositoryComponent,
  SpecimenType
}
import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._

import org.slf4j.LoggerFactory
import akka.pattern.ask
import org.scalatest.Tag
import org.scalatest.BeforeAndAfterEach
import scalaz._
import scalaz.Scalaz._

class ProcessingTypeProcessorSpec extends StudyProcessorFixture {

  private val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  var disabledStudy: DisabledStudy = null

  private def askAddCommand(
    procType: ProcessingType)(
    resultFunc: DomainValidation[ProcessingTypeAddedEvent] => Unit): Unit = {
    val cmd = AddProcessingTypeCmd(
      procType.studyId.id,
      procType.name,
      procType.description,
      procType.enabled)
    val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[ProcessingTypeAddedEvent]]
      .futureValue
    resultFunc(validation)
  }

  private def askUpdateCommand(
    procType: ProcessingType)(
    resultFunc: DomainValidation[ProcessingTypeUpdatedEvent] => Unit): Unit = {
    val cmd = UpdateProcessingTypeCmd(
      procType.studyId.id,
      procType.id.id,
      procType.version,
      procType.name,
      procType.description,
      procType.enabled)
    val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[ProcessingTypeUpdatedEvent]]
      .futureValue
    resultFunc(validation)
  }

  private def askRemoveCommand(
    procType: ProcessingType)(
    resultFunc: DomainValidation[ProcessingTypeRemovedEvent] => Unit): Unit = {
    val cmd = RemoveProcessingTypeCmd(
      procType.studyId.id,
      procType.id.id,
      procType.version)
    val validation = ask(studyProcessor, cmd)
      .mapTo[DomainValidation[ProcessingTypeRemovedEvent]]
      .futureValue
    resultFunc(validation)
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

      askAddCommand(procType){ validation =>
        validation should be('success)
        validation map { event =>
          event shouldBe a[ProcessingTypeAddedEvent]
          event should have(
            'name (procType.name),
            'description (procType.description),
            'enabled (procType.enabled))

          val procType2 = processingTypeRepository.withId(
            disabledStudy.id, ProcessingTypeId(event.processingTypeId)) | fail
          procType2.version should be(0)
            processingTypeRepository.allForStudy(disabledStudy.id) should have size 1
        }
      }
    }

    "not add a processing type to a study that does not exist" in {
      val study2 = factory.createDisabledStudy
      val procType = factory.createProcessingType

      askAddCommand(procType){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include regex s"${study2.id.id}.*not found"
        }
      }
    }

    "not add a processing type with a name that already exists" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)

      askAddCommand(procType){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("name already exists")
        }
      }
    }

    "update a processing type" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)

      val procType2 = procType.copy(name = nameGenerator.next[String])

      askUpdateCommand(procType2) { validation =>
        validation should be('success)
        validation map { event =>
          event shouldBe a[ProcessingTypeUpdatedEvent]
          event should have(
            'name (procType2.name),
            'description (procType2.description),
            'enabled (procType2.enabled))
        }
      }
    }

    "not update a processing type to name that already exists" in {
      val procType1 = factory.createProcessingType
      processingTypeRepository.put(procType1)

      val procType2 = factory.createProcessingType
      processingTypeRepository.put(procType2)

      val procType3 = procType2.copy(name = procType1.name)

      askUpdateCommand(procType3) { validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("name already exists")
        }
      }
    }

    "not update a processing type to wrong study" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val procType2 = procType.copy(studyId = study2.id)

      askUpdateCommand(procType2) { validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("study does not have processing type")
        }
      }
    }

    "not update a processing type with an invalid version" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)

      val procTypeBadVersion = procType.copy(version = procType.version + 1)

      askUpdateCommand(procTypeBadVersion) { validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("doesn't match current version")
        }
      }
    }

    "remove a processing type" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)

      askRemoveCommand(procType){ validation =>
        validation should be('success)
        validation map { event => event shouldBe a[ProcessingTypeRemovedEvent] }
      }
    }

    "not remove a processing type  with an invalid version" in {
      val procType = factory.createProcessingType
      processingTypeRepository.put(procType)

      val procTypeBadVersion = procType.copy(version = procType.version - 2)

      askRemoveCommand(procTypeBadVersion){ validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("version mismatch")
        }
      }
    }

  }
}
