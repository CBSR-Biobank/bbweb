package org.biobank.service

import org.biobank.fixture._
import org.biobank.infrastructure.CollectionEventTypeAnnotationTypeData
import org.biobank.infrastructure.CollectionEventTypeSpecimenGroupData
import org.biobank.infrastructure.command.StudyCommands._
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

import org.slf4j.LoggerFactory

import akka.pattern.ask
import scalaz._
import scalaz.Scalaz._

class CollectionEventTypeProcessorSpec extends StudyProcessorFixture {

  private val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  var disabledStudy: DisabledStudy = null

  // create the study to be used for each tests*
  override def beforeEach: Unit = {
    disabledStudy = factory.createDisabledStudy
    studyRepository.put(disabledStudy)
    ()
  }

  private def askAddCommand(
    ceventType: CollectionEventType)(
    resultFunc: DomainValidation[CollectionEventTypeAddedEvent] => Unit): Unit = {
    val cmd = AddCollectionEventTypeCmd(
      ceventType.studyId.id,
      ceventType.name,
      ceventType.description,
      ceventType.recurring,
      ceventType.specimenGroupData,
      ceventType.annotationTypeData)
    val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[CollectionEventTypeAddedEvent]]
      .futureValue
    resultFunc(validation)
  }

  private def askUpdateCommand(
    ceventType: CollectionEventType)(
    resultFunc: DomainValidation[CollectionEventTypeUpdatedEvent] => Unit): Unit = {
    val cmd = UpdateCollectionEventTypeCmd(
      ceventType.studyId.id,
      ceventType.id.id,
      ceventType.version,
      ceventType.name,
      ceventType.description,
      ceventType.recurring,
      ceventType.specimenGroupData,
      ceventType.annotationTypeData)
    val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[CollectionEventTypeUpdatedEvent]]
      .futureValue
    resultFunc(validation)
  }

  private def askRemoveCommand(
    ceventType: CollectionEventType)(
    resultFunc: DomainValidation[CollectionEventTypeRemovedEvent] => Unit): Unit = {
    val cmd = RemoveCollectionEventTypeCmd(
      ceventType.studyId.id,
      ceventType.id.id,
      ceventType.version)
    val validation = ask(studyProcessor, cmd)
      .mapTo[DomainValidation[CollectionEventTypeRemovedEvent]]
      .futureValue
    resultFunc(validation)
  }

  "A study processor" can {

    "add a collection event type" in {
      val cet = factory.createCollectionEventType

      askAddCommand(cet) { validation =>
        validation should be('success)
        validation map { event =>
          event shouldBe a[CollectionEventTypeAddedEvent]
          event should have(
            'studyId     (cet.studyId.id),
            'name        (cet.name),
            'description (cet.description),
            'recurring   (cet.recurring))

          val cet2 = collectionEventTypeRepository.withId(
            disabledStudy.id, CollectionEventTypeId(event.collectionEventTypeId)) | fail
          cet2.version should be(0)
          collectionEventTypeRepository.allForStudy(disabledStudy.id) should have size 1
        }
      }
    }


    "not add a collection event type to a study that does not exist" in {
      val study2 = factory.createDisabledStudy
      val cet = factory.createCollectionEventType

      askAddCommand(cet) { validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include regex s"${study2.id.id}.*not found"
        }
      }
    }

    "not add a collection event type with a name that already exists" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      askAddCommand(cet) { validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("name already exists")
        }
      }
    }

    "update a collection event type" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cet2 = factory.createCollectionEventType

      val cet3 = cet.copy(
        name = cet2.name,
        description = cet2.description,
        recurring = cet2.recurring)

      askUpdateCommand(cet3) { validation =>
        validation should be('success)
        validation map { event =>
          event shouldBe a[CollectionEventTypeUpdatedEvent]
          event should have(
            'version (cet.version + 1),
            'name (cet2.name),
            'description (cet2.description),
            'recurring (cet2.recurring))
        }
      }
    }

    "not update a collection event type to name that already exists" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cet2 = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet2)

      val cet3 = cet.copy(name = cet2.name)

      askUpdateCommand(cet3) { validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("name already exists")
        }
      }
    }

    "not update a collection event type to wrong study" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val cet2 = cet.copy(studyId = study2.id)

      askUpdateCommand(cet2) { validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("study does not have collection event type")
        }
      }
    }

    "not update a collection event type with an invalid version" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cet2 = cet.copy(version = cet.version + 1)

      askUpdateCommand(cet2) { validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("doesn't match current version")
        }
      }
    }

    "remove a collection event type" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      askRemoveCommand(cet) { validation =>
        validation should be('success)
        validation map { event => event shouldBe a[CollectionEventTypeRemovedEvent] }
      }
    }

    "not remove a collection event type  with an invalid version" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cet2 = cet.copy(version = cet.version - 1)

      askRemoveCommand(cet2) { validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("version mismatch")
        }
      }
    }

    "add a specimen group to a collection event type" in {
      val cet = factory.createCollectionEventType

      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val sgData = List(
        factory.createCollectionEventTypeSpecimenGroupData,
        factory.createCollectionEventTypeSpecimenGroupData)

      val cet2 = cet.copy(specimenGroupData = sgData)

      askAddCommand(cet2) { validation =>
        validation should be('success)
        validation map { event =>
          event shouldBe a[CollectionEventTypeAddedEvent]
          event.specimenGroupData should have length (2)

          event.specimenGroupData(0) should have(
            'specimenGroupId (sg.id.id),
            'maxCount (sgData(0).maxCount),
            'amount (sgData(0).amount))

          event.specimenGroupData(1) should have(
            'specimenGroupId(sg.id.id),
            'maxCount(sgData(1).maxCount),
            'amount(sgData(1).amount))
        }
      }
    }

    "update a collection event type and add specimen groups" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val sgData = List(factory.createCollectionEventTypeSpecimenGroupData)

      val cet2 = cet.copy(specimenGroupData = sgData)

      askUpdateCommand(cet2) { validation =>
        validation should be('success)
        validation map { event =>
          event shouldBe a[CollectionEventTypeUpdatedEvent]
          event.specimenGroupData should have length (1)

          event.specimenGroupData(0) should have(
            'specimenGroupId (sg.id.id),
            'maxCount (sgData(0).maxCount),
            'amount (sgData(0).amount))
        }
      }
    }

    "not update a specimen group if it used by collection event type" in {
      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val cet = factory.createCollectionEventType.copy(
        specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData))
      collectionEventTypeRepository.put(cet)

      val cmd = new UpdateSpecimenGroupCmd(sg.studyId.id, sg.id.id,
        sg.version, sg.name, sg.description, sg.units, sg.anatomicalSourceType,
        sg.preservationType, sg.preservationTemperatureType, sg.specimenType)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupUpdatedEvent]]
        .futureValue
      validation should be('failure)

      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("specimen group is in use by collection event type")
      }
    }

    "remove a specimen group from collection event type" in {
      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val cet = factory.createCollectionEventType.copy(
        specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData))
      collectionEventTypeRepository.put(cet)

      val cet2 = cet.copy(specimenGroupData = List.empty)

      askUpdateCommand(cet2) { validation =>
        validation should be('success)
        validation map { event =>
          event shouldBe a[CollectionEventTypeUpdatedEvent]
          event.specimenGroupData should have length (0)
        }
      }
    }

    "not remove a specimen group if used by collection event type" in {
      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val cet = factory.createCollectionEventType.copy(
        specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData))
      collectionEventTypeRepository.put(cet)

      val cmd = new RemoveSpecimenGroupCmd(sg.studyId.id, sg.id.id, sg.version)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupRemovedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("specimen group is in use by collection event type")
      }
    }

    "not add a specimen group from a different study" in {
      val specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val cet = factory.createCollectionEventType.copy(
        specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData))

      askAddCommand(cet) { validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("specimen group(s) do not belong to study")
        }
      }
    }

    "not update a collection event type with a specimen group from a different study" in {
      val specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cet2 = cet.copy(
        specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData))

      askUpdateCommand(cet2) { validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("specimen group(s) do not belong to study")
        }
      }
    }

    "add an annotation type to a collection event" in {
      val annotationType = factory.defaultCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotationType)

      val annotTypeData = List(factory.createCollectionEventTypeAnnotationTypeData)

      val cet = factory.createCollectionEventType.copy(
        annotationTypeData = annotTypeData)

      askAddCommand(cet) { validation =>
        validation should be('success)
        validation map { event =>
          event shouldBe a[CollectionEventTypeAddedEvent]
          event.annotationTypeData should have length (1)

          event.annotationTypeData(0) should have(
            'annotationTypeId (annotTypeData(0).annotationTypeId),
            'required (annotTypeData(0).required))
        }
      }
    }

    "not update an annotation type if used by collection event type" in {
      val annotationType = factory.defaultCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotationType)

      val cet = factory.createCollectionEventType.copy(
        annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))
      collectionEventTypeRepository.put(cet)

      val cmd = UpdateCollectionEventAnnotationTypeCmd(
        annotationType.studyId.id, annotationType.id.id, annotationType.version,
        annotationType.name, annotationType.description, annotationType.valueType)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventAnnotationTypeUpdatedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("annotation type is in use by collection event type")
      }
    }

    "remove an annotation type from collection event type" in {
      val annotationType = factory.defaultCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotationType)

      val cet = factory.createCollectionEventType.copy(
        annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))
      collectionEventTypeRepository.put(cet)

      val cet2 = cet.copy(annotationTypeData = List.empty)

      askUpdateCommand(cet2) { validation =>
        validation should be('success)
        validation map { event =>
          event.annotationTypeData should have length 0
        }
      }
    }

    "not remove an annotation type if it is used by collection event type" in {
      val annotationType = factory.defaultCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotationType)

      val cet = factory.createCollectionEventType.copy(
        annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))
      collectionEventTypeRepository.put(cet)

      val cmd = RemoveCollectionEventAnnotationTypeCmd(
        annotationType.studyId.id, annotationType.id.id, annotationType.version)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventAnnotationTypeRemovedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("annotation type is in use by collection event type")
      }
    }

    "not add an annotation type if it is in wrong study" in {
      val annotationType = factory.defaultCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotationType)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cet2 = cet.copy(
        annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

      askUpdateCommand(cet2) { validation =>
        validation should be('failure)
        validation.swap map { err =>
          err.list should have length 1
          err.list.head should include("annotation type(s) do not belong to study")
        }
      }
    }
  }
}
