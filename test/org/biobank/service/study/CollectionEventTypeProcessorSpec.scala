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
  RepositoriesComponent,
  SpecimenType
}
import org.biobank.domain.study._

import org.slf4j.LoggerFactory
import org.scalatest.OptionValues._
import org.joda.time.DateTime
import akka.pattern.ask

import scalaz._
import scalaz.Scalaz._

/**
  * Tests for actor CollectionEventTypeProcessor. These are written using ScalaTest.
  *
  */
class CollectionEventTypeProcessorSpec extends StudiesProcessorFixture {
  import org.biobank.TestUtils._

  private val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  var disabledStudy: DisabledStudy = null

  // create the study to be used for each tests*
  override def beforeEach: Unit = {
    disabledStudy = factory.createDisabledStudy
    studyRepository.put(disabledStudy)
    ()
  }

  private def askAddCommand(ceventType: CollectionEventType)
      : DomainValidation[CollectionEventTypeAddedEvent] = {
    val cmd = AddCollectionEventTypeCmd(
      ceventType.studyId.id,
      ceventType.name,
      ceventType.description,
      ceventType.recurring,
      ceventType.specimenGroupData,
      ceventType.annotationTypeData)
    ask(studiesProcessor, cmd).mapTo[DomainValidation[CollectionEventTypeAddedEvent]].futureValue
  }

  private def askUpdateCommand(ceventType: CollectionEventType)
      : DomainValidation[CollectionEventTypeUpdatedEvent] = {
    val cmd = UpdateCollectionEventTypeCmd(
      ceventType.studyId.id,
      ceventType.id.id,
      ceventType.version,
      ceventType.name,
      ceventType.description,
      ceventType.recurring,
      ceventType.specimenGroupData,
      ceventType.annotationTypeData)
    ask(studiesProcessor, cmd).mapTo[DomainValidation[CollectionEventTypeUpdatedEvent]].futureValue
  }

  private def askRemoveCommand(ceventType: CollectionEventType)
      : DomainValidation[CollectionEventTypeRemovedEvent] = {
    val cmd = RemoveCollectionEventTypeCmd(
      ceventType.studyId.id,
      ceventType.id.id,
      ceventType.version)
    ask(studiesProcessor, cmd).mapTo[DomainValidation[CollectionEventTypeRemovedEvent]].futureValue
  }

  "A study processor" can {

    "add a collection event type" in {
      val cet = factory.createCollectionEventType

      askAddCommand(cet) mustSucceed { event =>
        event mustBe a[CollectionEventTypeAddedEvent]
        event must have(
          'studyId     (cet.studyId.id),
          'name        (cet.name),
          'description (cet.description),
          'recurring   (cet.recurring))

        collectionEventTypeRepository.allForStudy(disabledStudy.id) must have size 1
        collectionEventTypeRepository.withId(
          disabledStudy.id, CollectionEventTypeId(event.collectionEventTypeId)) mustSucceed { repoCet =>
          repoCet.version mustBe(0)
          checkTimeStamps(repoCet, DateTime.now, None)
        }
      }
    }


    "not add a collection event type to a study that does not exist" in {
      val study2 = factory.createDisabledStudy
      val cet = factory.createCollectionEventType
      askAddCommand(cet) mustFail s"invalid study id: ${study2.id.id}"
    }

    "not add a collection event type with a name that already exists" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)
      askAddCommand(cet) mustFail "name already exists"
    }

    "update a collection event type" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cet2 = cet.copy(
        name        = nameGenerator.next[CollectionEventType],
        description = Some(nameGenerator.next[CollectionEventType]),
        recurring   = !cet.recurring)

      askUpdateCommand(cet2) mustSucceed { event =>
        event mustBe a[CollectionEventTypeUpdatedEvent]
        event must have(
          'version (cet.version + 1),
          'name (cet2.name),
          'description (cet2.description),
          'recurring (cet2.recurring))

        collectionEventTypeRepository.allForStudy(disabledStudy.id) must have size 1
        collectionEventTypeRepository.withId(
          disabledStudy.id, CollectionEventTypeId(event.collectionEventTypeId)) mustSucceed { repoCet =>
          checkTimeStamps(repoCet, cet.timeAdded, DateTime.now)
        }
      }
    }

    "not update a collection event type to name that already exists" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cet2 = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet2)

      val cet3 = cet.copy(name = cet2.name)
      askUpdateCommand(cet3) mustFail "name already exists"
    }

    "not update a collection event type to wrong study" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val cet2 = cet.copy(studyId = study2.id)
      askUpdateCommand(cet2) mustFail "study does not have collection event type"
    }

    "not update a collection event type with an invalid version" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cet2 = cet.copy(version = cet.version + 1)
      askUpdateCommand(cet2) mustFail "doesn't match current version"
    }

    "remove a collection event type" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)
      askRemoveCommand(cet) mustSucceed { event =>
        event mustBe a[CollectionEventTypeRemovedEvent]
      }
    }

    "not remove a collection event type  with an invalid version" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cet2 = cet.copy(version = cet.version - 1)
      askRemoveCommand(cet2) mustFail "expected version doesn't match current version"
    }

    "add a specimen group to a collection event type" in {
      val cet = factory.createCollectionEventType

      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val sgData = List(
        factory.createCollectionEventTypeSpecimenGroupData,
        factory.createCollectionEventTypeSpecimenGroupData)

      val cet2 = cet.copy(specimenGroupData = sgData)

      askAddCommand(cet2) mustSucceed { event =>
        event mustBe a[CollectionEventTypeAddedEvent]
        event.specimenGroupData must have length (2)

        event.specimenGroupData(0) must have(
          'specimenGroupId (sg.id.id),
          'maxCount (sgData(0).maxCount),
          'amount (sgData(0).amount))

        event.specimenGroupData(1) must have(
          'specimenGroupId(sg.id.id),
          'maxCount(sgData(1).maxCount),
          'amount(sgData(1).amount))

        collectionEventTypeRepository.withId(
          disabledStudy.id, CollectionEventTypeId(event.collectionEventTypeId)) mustSucceed { repoCet =>
          repoCet.version mustBe(0)
          checkTimeStamps(repoCet, DateTime.now, None)
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
      askUpdateCommand(cet2) mustSucceed { event =>
        event mustBe a[CollectionEventTypeUpdatedEvent]
        event.specimenGroupData must have length (1)

        event.specimenGroupData(0) must have(
          'specimenGroupId (sg.id.id),
          'maxCount (sgData(0).maxCount),
          'amount (sgData(0).amount))

        collectionEventTypeRepository.withId(
          disabledStudy.id, CollectionEventTypeId(event.collectionEventTypeId)) mustSucceed { repoCet =>
          repoCet.version mustBe(1)
          checkTimeStamps(repoCet, cet.timeAdded, DateTime.now)
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
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[SpecimenGroupUpdatedEvent]]
        .futureValue
      v mustFail "specimen group is in use by collection event type"
    }

    "remove a specimen group from collection event type" in {
      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val cet = factory.createCollectionEventType.copy(
        specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData))
      collectionEventTypeRepository.put(cet)

      val cet2 = cet.copy(specimenGroupData = List.empty)
      askUpdateCommand(cet2) mustSucceed { event =>
        event mustBe a[CollectionEventTypeUpdatedEvent]
        event.specimenGroupData must have length (0)

        collectionEventTypeRepository.withId(
          disabledStudy.id, CollectionEventTypeId(event.collectionEventTypeId)) mustSucceed { repoCet =>
          repoCet.version mustBe(1)
          checkTimeStamps(repoCet, cet.timeAdded, DateTime.now)
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
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[SpecimenGroupRemovedEvent]]
        .futureValue
      v mustFail "specimen group is in use by collection event type"
    }

    "not add a specimen group from a different study" in {
      val specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val cet = factory.createCollectionEventType.copy(
        specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData))
      askAddCommand(cet) mustFail "specimen group.+do not belong to study"
    }

    "not update a collection event type with a specimen group from a different study" in {
      val specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cet2 = cet.copy(
        specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData))
      askUpdateCommand(cet2)  mustFail "specimen group.+do not belong to study"
    }

    "add an annotation type to a collection event" in {
      val annotationType = factory.defaultCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotationType)

      val annotTypeData = List(factory.createCollectionEventTypeAnnotationTypeData)

      val cet = factory.createCollectionEventType.copy(
        annotationTypeData = annotTypeData)

      askAddCommand(cet) mustSucceed { event =>
        event mustBe a[CollectionEventTypeAddedEvent]
        event.annotationTypeData must have length (1)

        event.annotationTypeData(0) must have(
          'annotationTypeId (annotTypeData(0).annotationTypeId),
          'required (annotTypeData(0).required))

        collectionEventTypeRepository.withId(
          disabledStudy.id, CollectionEventTypeId(event.collectionEventTypeId)) mustSucceed { repoCet =>
          checkTimeStamps(repoCet, DateTime.now, None)
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
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventAnnotationTypeUpdatedEvent]]
        .futureValue

      v mustFail "annotation type is in use by collection event type"
    }

    "remove an annotation type from collection event type" in {
      val annotationType = factory.defaultCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotationType)

      val cet = factory.createCollectionEventType.copy(
        annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))
      collectionEventTypeRepository.put(cet)

      val cet2 = cet.copy(annotationTypeData = List.empty)

      askUpdateCommand(cet2) mustSucceed { event =>
        event.annotationTypeData must have length 0

        collectionEventTypeRepository.withId(
          disabledStudy.id, CollectionEventTypeId(event.collectionEventTypeId)) mustSucceed { repoCet =>
          checkTimeStamps(repoCet, cet.timeAdded, DateTime.now)
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
      val v = ask(studiesProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventAnnotationTypeRemovedEvent]]
        .futureValue

      v mustFail "annotation type is in use by collection event type"
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

      askUpdateCommand(cet2) mustFail "annotation type.+do not belong to study"
    }
  }
}
