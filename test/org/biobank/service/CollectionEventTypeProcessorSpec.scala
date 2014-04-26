package org.biobank.service

import org.biobank.fixture._
import org.biobank.infrastructure.CollectionEventTypeAnnotationType
import org.biobank.infrastructure.CollectionEventTypeSpecimenGroup
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  AnatomicalSourceType,
  AnnotationTypeId,
  AnnotationValueType,
  DomainError,
  DomainValidation,
  Factory,
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

class CollectionEventTypeProcessorSpec extends StudyProcessorFixture with BeforeAndAfterEach {

  private val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  val factory = new Factory(
    nameGenerator,
    studyRepository,
    collectionEventTypeRepository,
    collectionEventAnnotationTypeRepository,
    specimenGroupRepository)

  var disabledStudy: DisabledStudy = null

  // create the study to be used for each tests*
  override def beforeEach: Unit = {
    disabledStudy = factory.createDisabledStudy
    studyRepository.put(disabledStudy)
  }

  "A study processor" can {

    "add a collection event type" taggedAs (Tag("single")) in {
      val cet = factory.createCollectionEventType

      // specimen groups and annotation types tested separately below
      val cmd = AddCollectionEventTypeCmd(
        disabledStudy.id.id, cet.name, cet.description, cet.recurring, List.empty, List.empty)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventTypeAddedEvent]]
        .futureValue

      validation should be('success)
      validation map { event =>
        event shouldBe a[CollectionEventTypeAddedEvent]
        event should have(
          'name(cet.name),
          'description(cet.description),
          'recurring(cet.recurring))

        log.info(s"event: $event")

        val cet2 = collectionEventTypeRepository.collectionEventTypeWithId(
          disabledStudy.id, CollectionEventTypeId(event.collectionEventTypeId)) | fail
        cet2.version should be(0)
        collectionEventTypeRepository.allCollectionEventTypesForStudy(disabledStudy.id) should have size 1
      }
    }

    "not add a collection event type with a name that already exists" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cmd = AddCollectionEventTypeCmd(
        disabledStudy.id.id, cet.name, cet.description, cet.recurring, List.empty, List.empty)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventTypeAddedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("name already exists")
      }
    }

    "update a collection event type" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cet2 = factory.createCollectionEventType

      val cmd = UpdateCollectionEventTypeCmd(
        disabledStudy.id.id, cet.id.id, cet2.versionOption, cet2.name, cet2.description, cet2.recurring,
        List.empty, List.empty)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventTypeUpdatedEvent]]
        .futureValue

      validation should be('success)
      validation map { event =>
        event shouldBe a[CollectionEventTypeUpdatedEvent]
        event should have(
          'name(cet2.name),
          'description(cet2.description),
          'recurring(cet2.recurring))
      }
    }

    "not update a collection event type to name that already exists" in {
      val cet1 = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet1)

      val cet2 = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet2)

      val cmd = UpdateCollectionEventTypeCmd(
        disabledStudy.id.id, cet2.id.id, cet2.versionOption, cet1.name, cet2.description,
        cet2.recurring, List.empty, List.empty)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventTypeUpdatedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("name already exists")
      }
    }

    "not be update a collection event type to wrong study" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val cmd = UpdateCollectionEventTypeCmd(
        study2.id.id, cet.id.id, cet.versionOption, cet.name, cet.description, cet.recurring,
        List.empty, List.empty)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventTypeUpdatedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("study does not have collection event type")
      }
    }

    "not update a collection event type with an invalid version" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cmd = UpdateCollectionEventTypeCmd(
        disabledStudy.id.id, cet.id.id, Some(cet.version + 1), cet.name, cet.description,
        cet.recurring, List.empty, List.empty)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventTypeUpdatedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("doesn't match current version")
      }
    }

    "can remove a collection event type" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cmd = RemoveCollectionEventTypeCmd(disabledStudy.id.id, cet.id.id, cet.versionOption)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventTypeRemovedEvent]]
        .futureValue

      validation should be('success)
      validation map { event => event shouldBe a[CollectionEventTypeRemovedEvent] }
    }

    "not remove a collection event type  with an invalid version" in {
      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cmd = RemoveCollectionEventTypeCmd(disabledStudy.id.id, cet.id.id, Some(cet.version + 1))
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventTypeRemovedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("version mismatch")
      }
    }

    "add a specimen group to a collection event type" in {
      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val specimenGroupData = List(
        factory.createCollectionEventTypeSpecimenGroup,
        factory.createCollectionEventTypeSpecimenGroup)

      val cet = factory.createCollectionEventType
      val cmd = AddCollectionEventTypeCmd(
        disabledStudy.id.id, cet.name, cet.description, cet.recurring, specimenGroupData, List.empty)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventTypeAddedEvent]]
        .futureValue

      validation should be('success)
      validation map { event =>
        event shouldBe a[CollectionEventTypeAddedEvent]
        event.specimenGroupData should have length (2)

        event.specimenGroupData(0) should have(
          'specimenGroupId(sg.id.id),
          'maxCount(specimenGroupData(0).maxCount),
          'amount(specimenGroupData(0).amount))

        event.specimenGroupData(1) should have(
          'specimenGroupId(sg.id.id),
          'maxCount(specimenGroupData(1).maxCount),
          'amount(specimenGroupData(1).amount))
      }
    }

    "update a collection event type and add specimen groups" in {
      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroup)

      val cmd = UpdateCollectionEventTypeCmd(
        cet.studyId.id, cet.id.id, cet.versionOption, cet.name, cet.description, cet.recurring,
        specimenGroupData, List.empty)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventTypeUpdatedEvent]]
        .futureValue

      validation should be('success)
      validation map { event =>
        event shouldBe a[CollectionEventTypeUpdatedEvent]
        event.specimenGroupData should have length (1)

        event.specimenGroupData(0) should have(
          'specimenGroupId(sg.id.id),
          'maxCount(specimenGroupData(0).maxCount),
          'amount(specimenGroupData(0).amount))
      }
    }

    "not update a specimen group if it used by collection event type" in {
      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      var cet = factory.createCollectionEventType
      cet = cet.update(cet.versionOption, cet.name, cet.description, cet.recurring,
        List(factory.createCollectionEventTypeSpecimenGroup), List.empty) | fail
      collectionEventTypeRepository.put(cet)

      val cmd = new UpdateSpecimenGroupCmd(sg.studyId.id, sg.id.id,
        sg.versionOption, sg.name, sg.description, sg.units, sg.anatomicalSourceType,
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

      var cet = factory.createCollectionEventType
      cet = cet.update(cet.versionOption, cet.name, cet.description, cet.recurring,
        List(factory.createCollectionEventTypeSpecimenGroup), List.empty) | fail
      collectionEventTypeRepository.put(cet)

      val cmd = UpdateCollectionEventTypeCmd(
        cet.studyId.id, cet.id.id, cet.versionOption, cet.name, cet.description, cet.recurring,
        List.empty, List.empty)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventTypeUpdatedEvent]]
        .futureValue

      validation should be('success)
      validation map { event =>
        event shouldBe a[CollectionEventTypeUpdatedEvent]
        event.specimenGroupData should have length (0)
      }
    }

    "not remove a specimen group if used by collection event type" in {
      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroup)

      var cet = factory.createCollectionEventType
      cet = cet.update(cet.versionOption, cet.name, cet.description, cet.recurring,
        specimenGroupData, List.empty) | fail
      collectionEventTypeRepository.put(cet)

      val cmd = new RemoveSpecimenGroupCmd(sg.studyId.id, sg.id.id, sg.versionOption)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupRemovedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("specimen group is in use by collection event type")
      }
    }

    "not add a specimen group from a different study" in {
      val specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroup)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val cet = factory.createCollectionEventType
      val cmd = AddCollectionEventTypeCmd(
        cet.studyId.id, cet.name, cet.description, cet.recurring, specimenGroupData, List.empty)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventTypeAddedEvent]]
        .futureValue

      validation should be('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include("specimen group(s) do not belong to study")
      }
    }

    "not update a collection event type with a specimen group from a different study" in {
      val specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroup)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val cmd = UpdateCollectionEventTypeCmd(
        cet.studyId.id, cet.id.id, cet.versionOption, cet.name, cet.description, cet.recurring,
        specimenGroupData, List.empty)
      val validation2 = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventTypeUpdatedEvent]]
        .futureValue

      validation2 should be('failure)
      validation2.swap map { err =>
        err.list should have length 1
        err.list.head should include("specimen group(s) do not belong to study")
      }
    }

    "add an annotation type to a colleciton event" in {
      val annotationType = factory.defaultCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotationType)
      val annotTypeData = List(factory.createCollectionEventTypeAnnotationType)

      val cet = factory.createCollectionEventType
      var cmd = AddCollectionEventTypeCmd(
        annotationType.studyId.id, cet.name, cet.description, cet.recurring, List.empty, annotTypeData)
      val validation = ask(studyProcessor, cmd)
        .mapTo[DomainValidation[CollectionEventTypeAddedEvent]]
        .futureValue

      validation should be('success)
      validation map { event =>
        event shouldBe a[CollectionEventTypeAddedEvent]
        event.annotationTypeData should have length (1)

        event.annotationTypeData(0) should have(
          'annotationTypeId(annotTypeData(0).annotationTypeId),
          'required(annotTypeData(0).required))
      }
    }

    "not be updated if used by collection event type" in {
      val annotationType = factory.defaultCollectionEventAnnotationType
      collectionEventAnnotationTypeRepository.put(annotationType)

      var cet = factory.createCollectionEventType
      cet = cet.update(cet.versionOption, cet.name, cet.description, cet.recurring,
        List.empty, List(factory.createCollectionEventTypeAnnotationType)) | fail
      collectionEventTypeRepository.put(cet)

      val cmd = UpdateCollectionEventAnnotationTypeCmd(
        annotationType.studyId.id, annotationType.id.id, annotationType.versionOption,
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

    //    "be removed from collection event type" in {
    //      val name = nameGenerator.next[CollectionEventTypeAnnotationType]
    //
    //      val at1 = await(studyService.addCollectionEventAnnotationType(
    //        new AddCollectionEventAnnotationTypeCmd(studyId.id, name, Some(name),
    //          AnnotationValueType.Date, Some(0), Some(Map.empty[String, String])))) | null
    //
    //      val annotationTypeData = Set(
    //        CollectionEventTypeAnnotationType(at1.annotationTypeId, true))
    //
    //      val cet1 = await(studyService.addCollectionEventType(
    //        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), true,
    //          Set.empty, annotationTypeData))) | null
    //
    //      collectionEventTypeRepository.collectionEventTypeWithId(
    //        studyId, cet1.collectionEventTypeId) must beSuccessful.like {
    //          case x =>
    //            x.annotationTypeData.size mustEqual 1
    //        }
    //
    //      val cet2 = await(studyService.updateCollectionEventType(
    //        new UpdateCollectionEventTypeCmd(
    //          cet1.collectionEventTypeId, Some(cet1.version), studyId.id,
    //          name, Some(name), recurring = true, Set.empty, Set.empty))) | null
    //
    //      collectionEventTypeRepository.collectionEventTypeWithId(
    //        studyId, cet1.collectionEventTypeId) must beSuccessful.like {
    //          case x =>
    //            x.annotationTypeData.size mustEqual 0
    //        }
    //    }
    //
    //    "not be removed if used by collection event type" in {
    //      val name = nameGenerator.next[CollectionEventAnnotationType]
    //
    //      val at1 = await(studyService.addCollectionEventAnnotationType(
    //        AddCollectionEventAnnotationTypeCmd(studyId.id, name, Some(name),
    //          AnnotationValueType.Date))) | null
    //
    //      collectionEventAnnotationTypeRepository.annotationTypeWithId(studyId,
    //        AnnotationTypeId(at1.annotationTypeId)) must beSuccessful
    //
    //      val annotationTypeData = Set(
    //        CollectionEventTypeAnnotationType(at1.annotationTypeId, true))
    //
    //      val cet1 = await(studyService.addCollectionEventType(
    //        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), true,
    //          Set.empty, annotationTypeData))) | null
    //
    //      collectionEventTypeRepository.collectionEventTypeWithId(
    //        studyId, cet1.collectionEventTypeId) must beSuccessful
    //
    //      val at2 = await(studyService.removeCollectionEventAnnotationType(
    //        RemoveCollectionEventAnnotationTypeCmd(at1.annotationTypeId, Some(at1.version), studyId.id)))
    //
    //      at2 must beFailing.like {
    //        case msgs => msgs.head must contain(
    //          "annotation type is in use by collection event type")
    //      }
    //
    //    }
    //
    //    "be removed from collection event type" in {
    //      val name = nameGenerator.next[CollectionEventTypeAnnotationType]
    //
    //      val at1 = await(studyService.addCollectionEventAnnotationType(
    //        new AddCollectionEventAnnotationTypeCmd(studyId.id, name, Some(name),
    //          AnnotationValueType.Date, Some(0), Some(Map.empty[String, String])))) | null
    //
    //      val annotationTypeData = Set(
    //        CollectionEventTypeAnnotationType(at1.annotationTypeId, true))
    //
    //      val cet1 = await(studyService.addCollectionEventType(
    //        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), true,
    //          Set.empty, annotationTypeData))) | null
    //
    //      collectionEventTypeRepository.collectionEventTypeWithId(
    //        studyId, cet1.collectionEventTypeId) must beSuccessful.like {
    //          case x =>
    //            x.annotationTypeData.size mustEqual 1
    //        }
    //
    //      val cet2 = await(studyService.updateCollectionEventType(
    //        new UpdateCollectionEventTypeCmd(cet1.collectionEventTypeId, Some(cet1.version), studyId.id,
    //          name, Some(name), recurring = true, Set.empty, Set.empty))) | null
    //
    //      collectionEventTypeRepository.collectionEventTypeWithId(
    //        studyId, cet1.collectionEventTypeId) must beSuccessful.like {
    //          case x =>
    //            x.annotationTypeData.size mustEqual 0
    //        }
    //    }
    //
    //    "not be added if annotation type in wrong study" in {
    //      val name2 = nameGenerator.next[Study]
    //      val study2 = await(studyService.addStudy(new AddStudyCmd(name2, Some(name2)))) | null
    //
    //      // this one is in correct study
    //      val at1 = await(studyService.addCollectionEventAnnotationType(
    //        new AddCollectionEventAnnotationTypeCmd(studyId.id,
    //          nameGenerator.next[CollectionEventTypeAnnotationType], None,
    //          AnnotationValueType.Date, Some(0), Some(Map.empty[String, String])))) | null
    //
    //      // this one is in other study
    //      val at2 = await(studyService.addCollectionEventAnnotationType(
    //        new AddCollectionEventAnnotationTypeCmd(study2.id,
    //          nameGenerator.next[CollectionEventTypeAnnotationType], None,
    //          AnnotationValueType.Date, Some(0), Some(Map.empty[String, String])))) | null
    //
    //      val annotationTypeData = Set(
    //        CollectionEventTypeAnnotationType(at1.annotationTypeId, true),
    //        CollectionEventTypeAnnotationType(at2.annotationTypeId, true))
    //
    //      val name = nameGenerator.next[CollectionEventType]
    //      val cet1 = await(studyService.addCollectionEventType(
    //        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), true,
    //          Set.empty, annotationTypeData)))
    //
    //      cet1 must beFailing.like {
    //        case msgs => msgs.head must contain("annotation type(s) do not belong to study")

  }
}
