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

      var cmd = AddCollectionEventTypeCmd(
	disabledStudy.id.id, name, description, recurring, List.empty, List.empty)
      val validation = ask(studyProcessor, cmd)
	.mapTo[DomainValidation[CollectionEventTypeAddedEvent]]
	.futureValue
      validation should be ('success)

      validation map { event =>
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


    //  "Specimen group -> collection event type" can {
    //
    //    "be added" in {
    //      val name = nameGenerator.next[Study]
    //      val units = nameGenerator.next[String]
    //      val anatomicalSourceType = AnatomicalSourceType.Blood
    //      val preservationType = PreservationType.FreshSpecimen
    //      val preservationTempType = PreservationTemperatureType.Minus80celcius
    //      val specimenType = SpecimenType.FilteredUrine
    //
    //      val sg1 = await(studyService.addSpecimenGroup(
    //        AddSpecimenGroupCmd(studyId.id, name, Some(name), units, anatomicalSourceType,
    //          preservationType, preservationTempType, specimenType))) | null
    //      specimenGroupRepository.specimenGroupWithId(
    //        studyId, sg1.specimenGroupId) must beSuccessful
    //
    //      val count = 10
    //      val amount = BigDecimal(1.1)
    //      val count2 = 5
    //      val amount2 = BigDecimal(0.1)
    //      val SpecimenGroupData = Set(
    //        CollectionEventTypeSpecimenGroup(sg1.specimenGroupId, count, amount),
    //        CollectionEventTypeSpecimenGroup(sg1.specimenGroupId, count2, amount2))
    //
    //      val cet1 = await(studyService.addCollectionEventType(
    //        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), recurring = true,
    //          SpecimenGroupData, Set.empty))) | null
    //
    //      collectionEventTypeRepository.collectionEventTypeWithId(
    //        studyId, cet1.collectionEventTypeId) must beSuccessful.like {
    //          case cet =>
    //            cet.specimenGroupData.size mustEqual 2
    //            cet.specimenGroupData.exists(sgData =>
    //              sgData.specimenGroupId.equals(sg1.specimenGroupId)
    //                && sgData.maxCount.equals(count)
    //                && sgData.amount.equals(amount)) mustEqual true
    //            cet.specimenGroupData.exists(sgData =>
    //              sgData.specimenGroupId.equals(sg1.specimenGroupId)
    //                && sgData.maxCount.equals(count2)
    //                && sgData.amount.equals(amount2)) mustEqual true
    //        }
    //    }
    //
    //    "not be updated if used by collection event type" in {
    //      val sgName = nameGenerator.next[SpecimenGroup]
    //
    //      val sg1 = await(studyService.addSpecimenGroup(
    //        AddSpecimenGroupCmd(studyId.id, sgName, Some(sgName), "mL", AnatomicalSourceType.Blood,
    //          PreservationType.FreshSpecimen, PreservationTemperatureType.Minus80celcius,
    //          SpecimenType.BuffyCoat))) | null
    //      specimenGroupRepository.specimenGroupWithId(
    //        studyId, sg1.specimenGroupId) must beSuccessful
    //
    //      val SpecimenGroupData = Set(
    //        CollectionEventTypeSpecimenGroup(sg1.specimenGroupId, 1, BigDecimal(1.1)))
    //
    //      val cetName = nameGenerator.next[CollectionEventType]
    //      val cet1 = await(studyService.addCollectionEventType(
    //        new AddCollectionEventTypeCmd(studyId.id, cetName, Some(cetName), recurring = true,
    //          SpecimenGroupData, Set.empty))) | null
    //
    //      collectionEventTypeRepository.collectionEventTypeWithId(
    //        studyId, cet1.collectionEventTypeId) must beSuccessful
    //
    //      val sg2 = await(studyService.updateSpecimenGroup(
    //        UpdateSpecimenGroupCmd(sg1.specimenGroupId, Some(cet1.version), studyId.id, sgName, Some(sgName),
    //          "mL", AnatomicalSourceType.Blood, PreservationType.FreshSpecimen,
    //          PreservationTemperatureType.Minus80celcius, SpecimenType.CdpaPlasma)))
    //
    //      sg2 must beFailing.like {
    //        case msgs => msgs.head must contain(
    //          "specimen group is in use by collection event type")
    //      }
    //    }
    //
    //    "be removed from collection event type" in {
    //      val name = nameGenerator.next[Study]
    //      val units = nameGenerator.next[String]
    //      val anatomicalSourceType = AnatomicalSourceType.Blood
    //      val preservationType = PreservationType.FreshSpecimen
    //      val preservationTempType = PreservationTemperatureType.Minus80celcius
    //      val specimenType = SpecimenType.FilteredUrine
    //
    //      val sg1 = await(studyService.addSpecimenGroup(
    //        AddSpecimenGroupCmd(studyId.id, name, Some(name), units, anatomicalSourceType,
    //          preservationType, preservationTempType, specimenType))) | null
    //
    //      val count = 10
    //      val amount = BigDecimal(1.1)
    //      val count2 = 5
    //      val amount2 = BigDecimal(0.1)
    //      val SpecimenGroupData = Set(
    //        CollectionEventTypeSpecimenGroup(sg1.specimenGroupId, count, amount),
    //        CollectionEventTypeSpecimenGroup(sg1.specimenGroupId, count2, amount2))
    //
    //      val cet1 = await(studyService.addCollectionEventType(
    //        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), recurring = true,
    //          SpecimenGroupData, Set.empty))) | null
    //
    //      collectionEventTypeRepository.collectionEventTypeWithId(
    //        studyId, cet1.collectionEventTypeId) must beSuccessful.like {
    //          case x =>
    //            x.specimenGroupData.size mustEqual 2
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
    //            x.specimenGroupData.size mustEqual 0
    //        }
    //    }
    //
    //    "not be removed if used by collection event type" in {
    //      val sgName = nameGenerator.next[SpecimenGroup]
    //
    //      val sg1 = await(studyService.addSpecimenGroup(
    //        AddSpecimenGroupCmd(studyId.id, sgName, Some(sgName), "mL", AnatomicalSourceType.Blood,
    //          PreservationType.FreshSpecimen, PreservationTemperatureType.Minus80celcius,
    //          SpecimenType.BuffyCoat))) | null
    //      specimenGroupRepository.specimenGroupWithId(
    //        studyId, sg1.specimenGroupId) must beSuccessful
    //
    //      val SpecimenGroupData = Set(
    //        CollectionEventTypeSpecimenGroup(sg1.specimenGroupId, 1, BigDecimal(1.1)))
    //
    //      val cetName = nameGenerator.next[CollectionEventType]
    //      val cet1 = await(studyService.addCollectionEventType(
    //        new AddCollectionEventTypeCmd(studyId.id, cetName, Some(cetName), recurring = true,
    //          SpecimenGroupData, Set.empty))) | null
    //
    //      collectionEventTypeRepository.collectionEventTypeWithId(
    //        studyId, cet1.collectionEventTypeId) must beSuccessful
    //
    //      val sg2 = await(studyService.removeSpecimenGroup(
    //        RemoveSpecimenGroupCmd(sg1.specimenGroupId, Some(sg1.version), studyId.id)))
    //
    //      sg2 must beFailing.like {
    //        case msgs => msgs.head must contain(
    //          "specimen group is in use by collection event type")
    //      }
    //    }
    //
    //    "not be added if specimen group in wrong study" in {
    //      val name2 = nameGenerator.next[Study]
    //      val study2 = await(studyService.addStudy(new AddStudyCmd(name2, Some(name2)))) | null
    //
    //      val name3 = nameGenerator.next[SpecimenGroup]
    //      val units = nameGenerator.next[String]
    //      val anatomicalSourceType = AnatomicalSourceType.Blood
    //      val preservationType = PreservationType.FreshSpecimen
    //      val preservationTempType = PreservationTemperatureType.Minus80celcius
    //      val specimenType = SpecimenType.FilteredUrine
    //
    //      // this one is in correct study
    //      val sg1 = await(studyService.addSpecimenGroup(
    //        AddSpecimenGroupCmd(studyId.id, name3, Some(name3), units, anatomicalSourceType,
    //          preservationType, preservationTempType, specimenType))) | null
    //
    //      // this one is in wrong study
    //      val sg2 = await(studyService.addSpecimenGroup(
    //        AddSpecimenGroupCmd(study2.id, name3, Some(name3), units, anatomicalSourceType,
    //          preservationType, preservationTempType, specimenType))) | null
    //
    //      val count = 10
    //      val amount = BigDecimal(1.1)
    //      val SpecimenGroupData = Set(
    //        CollectionEventTypeSpecimenGroup(sg1.specimenGroupId, count, amount),
    //        CollectionEventTypeSpecimenGroup(sg2.specimenGroupId, count, amount))
    //
    //      val name = nameGenerator.next[CollectionEventType]
    //      val cet1 = await(studyService.addCollectionEventType(
    //        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), true,
    //          SpecimenGroupData, Set.empty)))
    //
    //      cet1 must beFailing.like {
    //        case msgs => msgs.head must contain("specimen group(s) do not belong to study")
    //      }
    //    }
    //  }
    //
    //  "Annotation type -> collection event type" can {
    //
    //    "be added" in {
    //      val name = nameGenerator.next[CollectionEventAnnotationType]
    //      val required = true
    //
    //      val at1 = await(studyService.addCollectionEventAnnotationType(
    //        new AddCollectionEventAnnotationTypeCmd(studyId.id, name, Some(name),
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
    //        studyId, cet1.collectionEventTypeId) must beSuccessful.like {
    //          case x =>
    //            x.annotationTypeData.size mustEqual 1
    //            x.annotationTypeData.exists(atData =>
    //              atData.annotationTypeId.equals(at1.annotationTypeId)
    //                && atData.required.equals(required)) mustEqual true
    //        }
    //    }
    //
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
    //      }
    //    }

  }
}
