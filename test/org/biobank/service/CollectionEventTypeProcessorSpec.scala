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
  PreservationType,
  PreservationTemperatureType,
  SpecimenType
}
import org.biobank.domain.study._
import org.biobank.infrastructure.command.StudyCommands._

import org.slf4j.LoggerFactory

import akka.pattern.ask
import scalaz._
import scalaz.Scalaz._

class CollectionEventTypeProcessorSpec extends StudyProcessorFixture {

  private val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  var disabledStudy: DisabledStudy = null

  // create the study to be used for tests
  override def beforeAll: Unit = {
    val name = nameGenerator.next[Study]
    disabledStudy = DisabledStudy.create(studyRepository.nextIdentity, -1, name, None) | fail
    studyRepository.put(disabledStudy)
  }

  "A study processor" can {

    "add a collection event type" in {
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])
      val recurring = true

      // specimen groups and annotation types tested separately below
      var cmd = AddCollectionEventTypeCmd(
	disabledStudy.id.id, name, description, recurring, List.empty, List.empty)
      val validation = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[CollectionEventTypeAddedEvent]]
	.futureValue
      validation should be ('success)

      validation map { event =>
	event shouldBe a[CollectionEventTypeAddedEvent]
	event should have (
          'name        (name),
          'description (description),
	  'recurring   (recurring)
	)

        val cet = collectionEventTypeRepository.collectionEventTypeWithId(
          disabledStudy.id, CollectionEventTypeId(event.collectionEventTypeId)) | fail
	cet.version should be (0)
        collectionEventTypeRepository.allCollectionEventTypesForStudy(disabledStudy.id) should have size 1
      }

      val name2 = nameGenerator.next[Study]
      val recurring2 = false

      cmd = AddCollectionEventTypeCmd(
	disabledStudy.id.id, name2, None, recurring2, List.empty, List.empty)
      val validation2 = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[CollectionEventTypeAddedEvent]]
	.futureValue
      validation2 should be ('success)

      validation2 map { event =>
	event shouldBe a[CollectionEventTypeAddedEvent]
	event should have (
          'name        (name2),
          'description (None),
	  'recurring   (recurring2)
	)

        val cet = collectionEventTypeRepository.collectionEventTypeWithId(
          disabledStudy.id, CollectionEventTypeId(event.collectionEventTypeId)) | fail
	cet.version should be (0)
        collectionEventTypeRepository.allCollectionEventTypesForStudy(disabledStudy.id) should have size 2
      }
    }

    "not add a collection event type with a name that already exists" in {
      val id = collectionEventTypeRepository.nextIdentity
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])
      val recurring = true

      val cet = CollectionEventType.create(disabledStudy.id, id, -1L, name, description,
	recurring, List.empty, List.empty) | fail
      collectionEventTypeRepository.put(cet)

      val cmd = AddCollectionEventTypeCmd(
	disabledStudy.id.id, name, description, recurring, List.empty, List.empty)
      val validation = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[CollectionEventTypeAddedEvent]]
	.futureValue

      validation should be ('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("name already exists")
      }
    }

    "update a collection event type" in {
      val id = collectionEventTypeRepository.nextIdentity
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])
      val recurring = true

      val cet = CollectionEventType.create(disabledStudy.id, id, -1L, name, description,
	recurring, List.empty, List.empty) | fail
      collectionEventTypeRepository.put(cet)

      val name2 = nameGenerator.next[Study]
      val description2 = None
      val recurring2 = false

      val cmd = UpdateCollectionEventTypeCmd(
	disabledStudy.id.id, id.id, Some(0L), name2, description2, recurring2, List.empty, List.empty)
      val validation = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[CollectionEventTypeUpdatedEvent]]
	.futureValue

      validation should be ('success)
      validation map { event =>
	event shouldBe a[CollectionEventTypeUpdatedEvent]
	event should have (
          'name        (name2),
          'description (description2),
	  'recurring   (recurring2)
	)
      }
    }

    "not update a collection event type to name that already exists" in {
      val id = collectionEventTypeRepository.nextIdentity
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])
      val recurring = true

      val cet = CollectionEventType.create(disabledStudy.id, id, -1L, name, description,
	recurring, List.empty, List.empty) | fail
      collectionEventTypeRepository.put(cet)

      val id2 = collectionEventTypeRepository.nextIdentity
      val name2= nameGenerator.next[Study]

      val cet2 = CollectionEventType.create(disabledStudy.id, id2, -1L, name2, description,
	recurring, List.empty, List.empty) | fail
      collectionEventTypeRepository.put(cet2)

      val cmd = UpdateCollectionEventTypeCmd(
	disabledStudy.id.id, id2.id, Some(0L), name, description, recurring, List.empty, List.empty)
      val validation = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[CollectionEventTypeUpdatedEvent]]
	.futureValue

      validation should be ('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("name already exists")
      }
    }

    "not be update a collection event type to wrong study" in {
      val id = collectionEventTypeRepository.nextIdentity
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])
      val recurring = true

      val cet = CollectionEventType.create(disabledStudy.id, id, -1L, name, description,
	recurring, List.empty, List.empty) | fail
      collectionEventTypeRepository.put(cet)

      val studyName = nameGenerator.next[Study]
      val study2 = DisabledStudy.create(studyRepository.nextIdentity, -1, studyName, None) | fail
      studyRepository.put(study2)

      val cmd = UpdateCollectionEventTypeCmd(
	study2.id.id, id.id, Some(0L), name, description, recurring, List.empty, List.empty)
      val validation = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[CollectionEventTypeUpdatedEvent]]
	.futureValue

      validation should be ('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("study does not have collection event type")
      }
    }

    "not update a collection event type with an invalid version" in {
      val id = collectionEventTypeRepository.nextIdentity
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])
      val recurring = true

      val cet = CollectionEventType.create(disabledStudy.id, id, -1L, name, description,
	recurring, List.empty, List.empty) | fail
      collectionEventTypeRepository.put(cet)

      val cmd = UpdateCollectionEventTypeCmd(
	disabledStudy.id.id, id.id, Some(-1L), name, description, recurring, List.empty, List.empty)
      val validation = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[CollectionEventTypeUpdatedEvent]]
	.futureValue

      validation should be ('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("doesn't match current version")
      }
    }

    "can remove a collection event type" in {
      val id = collectionEventTypeRepository.nextIdentity
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])
      val recurring = true

      val cet = CollectionEventType.create(disabledStudy.id, id, -1L, name, description,
	recurring, List.empty, List.empty) | fail
      collectionEventTypeRepository.put(cet)

      val cmd = RemoveCollectionEventTypeCmd(disabledStudy.id.id, id.id, Some(0L))
      val validation = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[CollectionEventTypeRemovedEvent]]
	.futureValue

      validation should be ('success)
      validation map { event => event shouldBe a[CollectionEventTypeRemovedEvent] }
    }

    "not remove a collection event type  with an invalid version" in {
      val id = collectionEventTypeRepository.nextIdentity
      val name = nameGenerator.next[Study]
      val description = Some(nameGenerator.next[Study])
      val recurring = true

      val cet = CollectionEventType.create(disabledStudy.id, id, -1L, name, description,
	recurring, List.empty, List.empty) | fail
      collectionEventTypeRepository.put(cet)

      val cmd = RemoveCollectionEventTypeCmd(disabledStudy.id.id, id.id, Some(-1L))
      val validation = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[CollectionEventTypeRemovedEvent]]
	.futureValue

      validation should be ('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("version mismatch")
      }
    }

    "add a specimen group to a collection event type" in {
      val sgId = specimenGroupRepository.nextIdentity
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val sg = SpecimenGroup.create(disabledStudy.id, sgId, -1L, name, None, units,
	anatomicalSourceType, preservationType, preservationTempType, specimenType) | fail
      specimenGroupRepository.put(sg)

      val maxCount = 10
      val amount = Some(BigDecimal(1.1))
      val maxCount2 = 5
      val amount2 = None
      val specimenGroupData = List(
	CollectionEventTypeSpecimenGroup(sg.id.id, maxCount, amount),
	CollectionEventTypeSpecimenGroup(sg.id.id, maxCount2, amount2))

      val cmd = AddCollectionEventTypeCmd(
	disabledStudy.id.id, name, None, recurring = true, specimenGroupData, List.empty)
      val validation = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[CollectionEventTypeAddedEvent]]
	.futureValue

      validation should be ('success)
      validation map { event =>
	event shouldBe a[CollectionEventTypeAddedEvent]
        event.specimenGroupData should have length (2)

	event.specimenGroupData(0) should have (
	  'specimenGroupId (sgId.id),
	  'maxCount (maxCount),
	  'amount (amount)
	)

	event.specimenGroupData(1) should have (
	  'specimenGroupId (sgId.id),
	  'maxCount (maxCount2),
	  'amount (amount2)
	)
      }
    }

    "update a collection event type and add specimen groups" in {
      val sgId = specimenGroupRepository.nextIdentity
      val sgName = nameGenerator.next[Study]
      val sg = SpecimenGroup.create(disabledStudy.id, sgId, -1L, sgName, None, "mL",
	AnatomicalSourceType.Blood, PreservationType.FreshSpecimen,
	PreservationTemperatureType.Minus80celcius, SpecimenType.BuffyCoat) | fail
      specimenGroupRepository.put(sg)

      val cetId2 = collectionEventTypeRepository.nextIdentity
      val cetName2 = nameGenerator.next[CollectionEventType]
      val cet = CollectionEventType.create(disabledStudy.id, cetId2, -1L, cetName2, None,
	recurring = true, List.empty, List.empty) | fail
      collectionEventTypeRepository.put(cet)

      val maxCount = 10
      val amount = Some(BigDecimal(1.1))
      val specimenGroupData = List(
	CollectionEventTypeSpecimenGroup(sg.id.id, maxCount, amount))

      val name = nameGenerator.next[Study]
      val cmd = AddCollectionEventTypeCmd(
	disabledStudy.id.id, name, None, recurring = true, specimenGroupData, List.empty)
      val validation = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[CollectionEventTypeAddedEvent]]
	.futureValue

      validation should be ('success)
      validation map { event =>
	event shouldBe a[CollectionEventTypeAddedEvent]
        event.specimenGroupData should have length (1)

	event.specimenGroupData(0) should have (
	  'specimenGroupId (sgId.id),
	  'maxCount (maxCount),
	  'amount (amount)
	)
      }
    }

    "not update a specimen group if it used by collection event type" in {
      val sgId = specimenGroupRepository.nextIdentity
      val sgName = nameGenerator.next[SpecimenGroup]

      val sg = SpecimenGroup.create(disabledStudy.id, sgId, -1L, sgName, None, "mL",
	AnatomicalSourceType.Blood, PreservationType.FreshSpecimen,
	PreservationTemperatureType.Minus80celcius, SpecimenType.BuffyCoat) | fail
      specimenGroupRepository.put(sg)

      val specimenGroupData = List(
        CollectionEventTypeSpecimenGroup(sg.id.id, 1, Some(BigDecimal(1.1))))

      val cetId = collectionEventTypeRepository.nextIdentity
      val cetName = nameGenerator.next[CollectionEventType]
      val cet = CollectionEventType.create(disabledStudy.id, cetId, -1L, cetName, None,
	recurring = true, specimenGroupData, List.empty) | fail
      collectionEventTypeRepository.put(cet)

      val cmd = new UpdateSpecimenGroupCmd(disabledStudy.id.id, sgId.id,
	Some(sg.version), sgName, None, "mL", AnatomicalSourceType.Blood, PreservationType.FreshSpecimen,
          PreservationTemperatureType.Minus80celcius, SpecimenType.CdpaPlasma)
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupUpdatedEvent]]
	.futureValue
      validation should be ('failure)

      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("specimen group is in use by collection event type")
      }
    }

    "remove a specimen group from collection event type" in {
      val sgId = specimenGroupRepository.nextIdentity
      val sgName = nameGenerator.next[SpecimenGroup]

      val sg = SpecimenGroup.create(disabledStudy.id, sgId, -1L, sgName, None, "mL",
	AnatomicalSourceType.Blood, PreservationType.FreshSpecimen,
	PreservationTemperatureType.Minus80celcius, SpecimenType.BuffyCoat) | fail
      specimenGroupRepository.put(sg)

      val specimenGroupData = List(
        CollectionEventTypeSpecimenGroup(sg.id.id, 1, Some(BigDecimal(1.1))))

      val cetId = collectionEventTypeRepository.nextIdentity
      val cetName = nameGenerator.next[CollectionEventType]
      val cet = CollectionEventType.create(disabledStudy.id, cetId, -1L, cetName, None,
	recurring = true, specimenGroupData, List.empty) | fail
      collectionEventTypeRepository.put(cet)

      val cmd = UpdateCollectionEventTypeCmd(
	disabledStudy.id.id, cetId.id, Some(0L), cetName, None, recurring = true,
	List.empty, List.empty)
      val validation = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[CollectionEventTypeUpdatedEvent]]
	.futureValue

      validation should be ('success)
      validation map { event =>
	event shouldBe a[CollectionEventTypeUpdatedEvent]
        event.specimenGroupData should have length (0)
      }
    }

    "not remove a specimen group if used by collection event type" in {
      val sgId = specimenGroupRepository.nextIdentity
      val sgName = nameGenerator.next[SpecimenGroup]

      val sg = SpecimenGroup.create(disabledStudy.id, sgId, -1L, sgName, None, "mL",
	AnatomicalSourceType.Blood, PreservationType.FreshSpecimen,
	PreservationTemperatureType.Minus80celcius, SpecimenType.BuffyCoat) | fail
      specimenGroupRepository.put(sg)

      val specimenGroupData = List(
        CollectionEventTypeSpecimenGroup(sg.id.id, 1, Some(BigDecimal(1.1))))

      val cetId = collectionEventTypeRepository.nextIdentity
      val cetName = nameGenerator.next[CollectionEventType]
      val cet = CollectionEventType.create(disabledStudy.id, cetId, -1L, cetName, None,
	recurring = true, specimenGroupData, List.empty) | fail
      collectionEventTypeRepository.put(cet)

      val cmd = new RemoveSpecimenGroupCmd(disabledStudy.id.id, sg.id.id, Some(sg.version))
      val validation = ask(studyProcessor, cmd).mapTo[DomainValidation[SpecimenGroupRemovedEvent]]
	.futureValue

      validation should be ('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("specimen group is in use by collection event type")
      }
    }

    "not add a specimen group from a different study" in {
      val studyName = nameGenerator.next[Study]
      val study2 = DisabledStudy.create(studyRepository.nextIdentity, -1, studyName, None) | fail
      studyRepository.put(study2)

      val sgId = specimenGroupRepository.nextIdentity
      val sgName = nameGenerator.next[SpecimenGroup]

      val sg = SpecimenGroup.create(study2.id, sgId, -1L, sgName, None, "mL",
	AnatomicalSourceType.Blood, PreservationType.FreshSpecimen,
	PreservationTemperatureType.Minus80celcius, SpecimenType.BuffyCoat) | fail
      specimenGroupRepository.put(sg)

      val specimenGroupData = List(CollectionEventTypeSpecimenGroup(sg.id.id, 2, Some(BigDecimal(1.1))))

      val cetName = nameGenerator.next[CollectionEventType]
      var cmd: CollectionEventTypeCommand = AddCollectionEventTypeCmd(
	disabledStudy.id.id, cetName, None, recurring = true, specimenGroupData, List.empty)
      val validation = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[CollectionEventTypeAddedEvent]]
	.futureValue

      validation should be ('failure)
      validation.swap map { err =>
        err.list should have length 1
        err.list.head should include ("specimen group(s) do not belong to study")
      }

      // try updating a study with a specimen group from a different study
      val cetId2 = collectionEventTypeRepository.nextIdentity
      val cetName2 = nameGenerator.next[CollectionEventType]
      val cet = CollectionEventType.create(disabledStudy.id, cetId2, -1L, cetName2, None,
	recurring = true, specimenGroupData, List.empty) | fail
      collectionEventTypeRepository.put(cet)

      cmd = UpdateCollectionEventTypeCmd(
	disabledStudy.id.id, cetId2.id, Some(0L), cetName2, None, recurring = true,
	specimenGroupData, List.empty)
      val validation2 = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[CollectionEventTypeUpdatedEvent]]
	.futureValue

      validation2 should be ('failure)
      validation2.swap map { err =>
        err.list should have length 1
        err.list.head should include ("specimen group(s) do not belong to study")
      }
    }

    "add an annotation type to a colleciton event" in {
      val annotId = collectionEventAnnotationTypeRepository.nextIdentity
      val name = nameGenerator.next[CollectionEventAnnotationType]
      val required = true

      val annotType = CollectionEventAnnotationType.create(disabledStudy.id, annotId, -1L, name, None,
      AnnotationValueType.Date) | fail
      collectionEventAnnotationTypeRepository.put(annotType)

      val annotTypeData = List(CollectionEventTypeAnnotationType(annotType.id.id, required))

      var cmd = AddCollectionEventTypeCmd(
	disabledStudy.id.id, name, None, recurring = true, List.empty, annotTypeData)
      val validation = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[CollectionEventTypeAddedEvent]]
	.futureValue

      validation should be ('success)
      validation map { event =>
	event shouldBe a[CollectionEventTypeAddedEvent]
        event.annotationTypeData should have length (1)

	event.annotationTypeData(0) should have (
	  'annotationTypeId (annotType.id.id),
	  'required         (required)
	)
      }
    }

    //    "not be updated if used by collection event type" in {
    //      val name = nameGenerator.next[CollectionEventAnnotationType]
    //
    //      val at1 = await(studyService.addCollectionEventAnnotationType(
    //        AddCollectionEventAnnotationTypeCmd(studyId.id, name, Some(name),
    //          AnnotationValueType.Date))) | null
    //
    //      collectionEventAnnotationTypeRepository.annotationTypeWithId(
    //        studyId, AnnotationTypeId(at1.annotationTypeId)) must beSuccessful
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
    //      val at2 = await(studyService.updateCollectionEventAnnotationType(
    //        UpdateCollectionEventAnnotationTypeCmd(at1.annotationTypeId, Some(at1.version), studyId.id,
    //          name, Some(name), AnnotationValueType.Number)))
    //
    //      at2 must beFailing.like {
    //        case msgs => msgs.head must contain(
    //          "annotation type is in use by collection event type")
    //      }
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
