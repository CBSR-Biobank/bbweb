package service

import fixture._
import domain.{
  AnatomicalSourceType,
  AnnotationTypeId,
  AnnotationValueType,
  PreservationType,
  PreservationTemperatureType,
  SpecimenType
}
import domain.study._
import service.commands.StudyCommands._

import org.specs2.scalaz.ValidationMatchers._
import org.specs2.mutable._
import org.specs2.time.NoTimeConversions
import org.slf4j.LoggerFactory
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CollectionEventTypeSpec extends StudyCommandFixture {

  args(
    //include = "tag1",
    sequential = true) // forces all tests to be run sequentially

  private val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(classOf[CollectionEventTypeSpec].getName)
  val studyName = nameGenerator.next[Study]
  val studyEvent = await(studyService.addStudy(new AddStudyCmd(studyName, Some(studyName)))) | null
  val studyId = StudyId(studyEvent.id)

  "Collection event type" can {

    "be added" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(AddCollectionEventTypeCmd(
        studyId.id, name, Some(name), recurring, Set.empty, Set.empty)))

      cet1 must beSuccessful.like {
        case x =>
          x.name must be(name)
          x.description must beSome(name)
          x.recurring must beEqualTo(recurring)
          collectionEventTypeRepository.collectionEventTypeWithId(
            studyId, x.collectionEventTypeId) must beSuccessful.like {
              case y =>
                y.version must beEqualTo(0)
            }
          collectionEventTypeRepository.allCollectionEventTypesForStudy(studyId).size mustEqual 1
      }

      val name2 = nameGenerator.next[Study]
      val recurring2 = false

      val cet2 = await(studyService.addCollectionEventType(AddCollectionEventTypeCmd(
        studyId.id, name2, None, recurring2, Set.empty, Set.empty)))

      cet2 must beSuccessful.like {
        case x =>
          x.name must be(name2)
          x.description must beNone
          x.recurring must beEqualTo(recurring2)
          collectionEventTypeRepository.collectionEventTypeWithId(
            studyId, x.collectionEventTypeId) must beSuccessful.like {
              case y =>
                y.version must beEqualTo(0)
            }
          collectionEventTypeRepository.allCollectionEventTypesForStudy(studyId).size mustEqual 2

      }
    }

    "not be added if name already exists" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(AddCollectionEventTypeCmd(
        studyId.id, name, Some(name), recurring, Set.empty, Set.empty)))

      cet1 must beSuccessful

      val cet2 = await(studyService.addCollectionEventType(AddCollectionEventTypeCmd(
        studyId.id, name, Some(name), recurring, Set.empty, Set.empty)))

      cet2 must beFailing.like { case msgs => msgs.head must contain("already exists") }
    }

    "be updated" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), recurring,
          Set.empty, Set.empty))) | null

      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful

      val name2 = nameGenerator.next[Study]
      val recurring2 = false

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(
          cet1.collectionEventTypeId, Some(cet1.version), studyId.id,
          name2, Some(name2), recurring2, Set.empty, Set.empty)))

      cet2 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(1L)
          x.name must be(name2)
          x.description must beSome(name2)
          x.recurring must beEqualTo(recurring2)

          collectionEventTypeRepository.collectionEventTypeWithId(
            studyId, x.collectionEventTypeId) must beSuccessful.like {
              case y =>
                y.version must beEqualTo(x.version)
            }
      }
    }

    "not be updated to name that already exists" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), recurring,
          Set.empty, Set.empty))) | null
      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful

      val name2 = nameGenerator.next[Study]
      val recurring2 = true
      val cet2 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, name2, Some(name2), recurring2,
          Set.empty, Set.empty))) | null
      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet2.collectionEventTypeId) must beSuccessful

      val cet3 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(
          cet2.collectionEventTypeId, Some(cet2.version), studyId.id,
          name, Some(name), recurring, Set.empty, Set.empty)))
      cet3 must beFailing.like {
        case msgs => msgs.head must contain("name already exists")
      }
    }

    "not be updated to wrong study" in {
      val name = nameGenerator.next[Study]
      val name2 = nameGenerator.next[Study]
      val recurring = true

      val study2 = await(studyService.addStudy(new AddStudyCmd(name2, Some(name2)))) | null

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), recurring,
          Set.empty, Set.empty))) | null

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(
          cet1.collectionEventTypeId, Some(cet1.version), study2.id,
          name2, Some(name2), recurring, Set.empty, Set.empty)))
      cet2 must beFailing.like { case msgs => msgs.head must contain("study does not have collection event type") }
    }

    "not be updated with invalid version" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), recurring,
          Set.empty, Set.empty))) | null

      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful

      val name2 = nameGenerator.next[Study]
      val recurring2 = false
      val versionOption = Some(cet1.version + 1)

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(cet1.collectionEventTypeId, versionOption, studyId.id,
          name2, Some(name2), recurring2, Set.empty, Set.empty)))
      cet2 must beFailing.like {
        case msgs => msgs.head must contain("doesn't match current version")
      }
    }

    "be removed" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, name, None, recurring,
          Set.empty, Set.empty))) | null
      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful

      await(studyService.removeCollectionEventType(
        new RemoveCollectionEventTypeCmd(cet1.collectionEventTypeId, Some(cet1.version), studyId.id)))

      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beFailing
    }

    "not be removed with invalid version" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), recurring,
          Set.empty, Set.empty))) | null
      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful

      val versionOption = Some(cet1.version + 1)
      val cet2 = await(studyService.removeCollectionEventType(
        new RemoveCollectionEventTypeCmd(cet1.collectionEventTypeId, versionOption, studyId.id)))
      cet2 must beFailing.like {
        case msgs => msgs.head must contain("doesn't match current version")
      }
    }
  }

  "Specimen group -> collection event type" can {

    "be added" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val sg1 = await(studyService.addSpecimenGroup(
        AddSpecimenGroupCmd(studyId.id, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null
      specimenGroupRepository.specimenGroupWithId(
        studyId, sg1.specimenGroupId) must beSuccessful

      val count = 10
      val amount = BigDecimal(1.1)
      val count2 = 5
      val amount2 = BigDecimal(0.1)
      val SpecimenGroupData = Set(
        CollectionEventTypeSpecimenGroup(sg1.specimenGroupId, count, amount),
        CollectionEventTypeSpecimenGroup(sg1.specimenGroupId, count2, amount2))

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), recurring = true,
          SpecimenGroupData, Set.empty))) | null

      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful.like {
          case cet =>
            cet.specimenGroupData.size mustEqual 2
            cet.specimenGroupData.exists(sgData =>
              sgData.specimenGroupId.equals(sg1.specimenGroupId)
                && sgData.maxCount.equals(count)
                && sgData.amount.equals(amount)) mustEqual true
            cet.specimenGroupData.exists(sgData =>
              sgData.specimenGroupId.equals(sg1.specimenGroupId)
                && sgData.maxCount.equals(count2)
                && sgData.amount.equals(amount2)) mustEqual true
        }
    }

    "not be updated if used by collection event type" in {
      val sgName = nameGenerator.next[SpecimenGroup]

      val sg1 = await(studyService.addSpecimenGroup(
        AddSpecimenGroupCmd(studyId.id, sgName, Some(sgName), "mL", AnatomicalSourceType.Blood,
          PreservationType.FreshSpecimen, PreservationTemperatureType.Minus80celcius,
          SpecimenType.BuffyCoat))) | null
      specimenGroupRepository.specimenGroupWithId(
        studyId, sg1.specimenGroupId) must beSuccessful

      val SpecimenGroupData = Set(
        CollectionEventTypeSpecimenGroup(sg1.specimenGroupId, 1, BigDecimal(1.1)))

      val cetName = nameGenerator.next[CollectionEventType]
      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, cetName, Some(cetName), recurring = true,
          SpecimenGroupData, Set.empty))) | null

      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful

      val sg2 = await(studyService.updateSpecimenGroup(
        UpdateSpecimenGroupCmd(sg1.specimenGroupId, Some(cet1.version), studyId.id, sgName, Some(sgName),
          "mL", AnatomicalSourceType.Blood, PreservationType.FreshSpecimen,
          PreservationTemperatureType.Minus80celcius, SpecimenType.CdpaPlasma)))

      sg2 must beFailing.like {
        case msgs => msgs.head must contain(
          "specimen group is in use by collection event type")
      }
    }

    "be removed from collection event type" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val sg1 = await(studyService.addSpecimenGroup(
        AddSpecimenGroupCmd(studyId.id, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null

      val count = 10
      val amount = BigDecimal(1.1)
      val count2 = 5
      val amount2 = BigDecimal(0.1)
      val SpecimenGroupData = Set(
        CollectionEventTypeSpecimenGroup(sg1.specimenGroupId, count, amount),
        CollectionEventTypeSpecimenGroup(sg1.specimenGroupId, count2, amount2))

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), recurring = true,
          SpecimenGroupData, Set.empty))) | null

      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful.like {
          case x =>
            x.specimenGroupData.size mustEqual 2
        }

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(
          cet1.collectionEventTypeId, Some(cet1.version), studyId.id,
          name, Some(name), recurring = true, Set.empty, Set.empty))) | null

      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful.like {
          case x =>
            x.specimenGroupData.size mustEqual 0
        }
    }

    "not be removed if used by collection event type" in {
      val sgName = nameGenerator.next[SpecimenGroup]

      val sg1 = await(studyService.addSpecimenGroup(
        AddSpecimenGroupCmd(studyId.id, sgName, Some(sgName), "mL", AnatomicalSourceType.Blood,
          PreservationType.FreshSpecimen, PreservationTemperatureType.Minus80celcius,
          SpecimenType.BuffyCoat))) | null
      specimenGroupRepository.specimenGroupWithId(
        studyId, sg1.specimenGroupId) must beSuccessful

      val SpecimenGroupData = Set(
        CollectionEventTypeSpecimenGroup(sg1.specimenGroupId, 1, BigDecimal(1.1)))

      val cetName = nameGenerator.next[CollectionEventType]
      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, cetName, Some(cetName), recurring = true,
          SpecimenGroupData, Set.empty))) | null

      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful

      val sg2 = await(studyService.removeSpecimenGroup(
        RemoveSpecimenGroupCmd(sg1.specimenGroupId, Some(sg1.version), studyId.id)))

      sg2 must beFailing.like {
        case msgs => msgs.head must contain(
          "specimen group is in use by collection event type")
      }
    }

    "not be added if specimen group in wrong study" in {
      val name2 = nameGenerator.next[Study]
      val study2 = await(studyService.addStudy(new AddStudyCmd(name2, Some(name2)))) | null

      val name3 = nameGenerator.next[SpecimenGroup]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      // this one is in correct study
      val sg1 = await(studyService.addSpecimenGroup(
        AddSpecimenGroupCmd(studyId.id, name3, Some(name3), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null

      // this one is in wrong study
      val sg2 = await(studyService.addSpecimenGroup(
        AddSpecimenGroupCmd(study2.id, name3, Some(name3), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null

      val count = 10
      val amount = BigDecimal(1.1)
      val SpecimenGroupData = Set(
        CollectionEventTypeSpecimenGroup(sg1.specimenGroupId, count, amount),
        CollectionEventTypeSpecimenGroup(sg2.specimenGroupId, count, amount))

      val name = nameGenerator.next[CollectionEventType]
      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), true,
          SpecimenGroupData, Set.empty)))

      cet1 must beFailing.like {
        case msgs => msgs.head must contain("specimen group(s) do not belong to study")
      }
    }
  }

  "Annotation type -> collection event type" can {

    "be added" in {
      val name = nameGenerator.next[CollectionEventAnnotationType]
      val required = true

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(studyId.id, name, Some(name),
          AnnotationValueType.Date))) | null

      collectionEventAnnotationTypeRepository.annotationTypeWithId(
        studyId, AnnotationTypeId(at1.annotationTypeId)) must beSuccessful

      val annotationTypeData = Set(
        CollectionEventTypeAnnotationType(at1.annotationTypeId, true))

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), true,
          Set.empty, annotationTypeData))) | null

      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful.like {
          case x =>
            x.annotationTypeData.size mustEqual 1
            x.annotationTypeData.exists(atData =>
              atData.annotationTypeId.equals(at1.annotationTypeId)
                && atData.required.equals(required)) mustEqual true
        }
    }

    "not be updated if used by collection event type" in {
      val name = nameGenerator.next[CollectionEventAnnotationType]

      val at1 = await(studyService.addCollectionEventAnnotationType(
        AddCollectionEventAnnotationTypeCmd(studyId.id, name, Some(name),
          AnnotationValueType.Date))) | null

      collectionEventAnnotationTypeRepository.annotationTypeWithId(
        studyId, AnnotationTypeId(at1.annotationTypeId)) must beSuccessful

      val annotationTypeData = Set(
        CollectionEventTypeAnnotationType(at1.annotationTypeId, true))

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), true,
          Set.empty, annotationTypeData))) | null

      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful

      val at2 = await(studyService.updateCollectionEventAnnotationType(
        UpdateCollectionEventAnnotationTypeCmd(at1.annotationTypeId, Some(at1.version), studyId.id,
          name, Some(name), AnnotationValueType.Number)))

      at2 must beFailing.like {
        case msgs => msgs.head must contain(
          "annotation type is in use by collection event type")
      }
    }

    "be removed from collection event type" in {
      val name = nameGenerator.next[CollectionEventTypeAnnotationType]

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(studyId.id, name, Some(name),
          AnnotationValueType.Date, Some(0), Some(Map.empty[String, String])))) | null

      val annotationTypeData = Set(
        CollectionEventTypeAnnotationType(at1.annotationTypeId, true))

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), true,
          Set.empty, annotationTypeData))) | null

      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful.like {
          case x =>
            x.annotationTypeData.size mustEqual 1
        }

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(
          cet1.collectionEventTypeId, Some(cet1.version), studyId.id,
          name, Some(name), recurring = true, Set.empty, Set.empty))) | null

      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful.like {
          case x =>
            x.annotationTypeData.size mustEqual 0
        }
    }

    "not be removed if used by collection event type" in {
      val name = nameGenerator.next[CollectionEventAnnotationType]

      val at1 = await(studyService.addCollectionEventAnnotationType(
        AddCollectionEventAnnotationTypeCmd(studyId.id, name, Some(name),
          AnnotationValueType.Date))) | null

      collectionEventAnnotationTypeRepository.annotationTypeWithId(studyId,
        AnnotationTypeId(at1.annotationTypeId)) must beSuccessful

      val annotationTypeData = Set(
        CollectionEventTypeAnnotationType(at1.annotationTypeId, true))

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), true,
          Set.empty, annotationTypeData))) | null

      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful

      val at2 = await(studyService.removeCollectionEventAnnotationType(
        RemoveCollectionEventAnnotationTypeCmd(at1.annotationTypeId, Some(at1.version), studyId.id)))

      at2 must beFailing.like {
        case msgs => msgs.head must contain(
          "annotation type is in use by collection event type")
      }

    }

    "be removed from collection event type" in {
      val name = nameGenerator.next[CollectionEventTypeAnnotationType]

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(studyId.id, name, Some(name),
          AnnotationValueType.Date, Some(0), Some(Map.empty[String, String])))) | null

      val annotationTypeData = Set(
        CollectionEventTypeAnnotationType(at1.annotationTypeId, true))

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), true,
          Set.empty, annotationTypeData))) | null

      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful.like {
          case x =>
            x.annotationTypeData.size mustEqual 1
        }

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(cet1.collectionEventTypeId, Some(cet1.version), studyId.id,
          name, Some(name), recurring = true, Set.empty, Set.empty))) | null

      collectionEventTypeRepository.collectionEventTypeWithId(
        studyId, cet1.collectionEventTypeId) must beSuccessful.like {
          case x =>
            x.annotationTypeData.size mustEqual 0
        }
    }

    "not be added if annotation type in wrong study" in {
      val name2 = nameGenerator.next[Study]
      val study2 = await(studyService.addStudy(new AddStudyCmd(name2, Some(name2)))) | null

      // this one is in correct study
      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(studyId.id,
          nameGenerator.next[CollectionEventTypeAnnotationType], None,
          AnnotationValueType.Date, Some(0), Some(Map.empty[String, String])))) | null

      // this one is in other study
      val at2 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study2.id,
          nameGenerator.next[CollectionEventTypeAnnotationType], None,
          AnnotationValueType.Date, Some(0), Some(Map.empty[String, String])))) | null

      val annotationTypeData = Set(
        CollectionEventTypeAnnotationType(at1.annotationTypeId, true),
        CollectionEventTypeAnnotationType(at2.annotationTypeId, true))

      val name = nameGenerator.next[CollectionEventType]
      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(studyId.id, name, Some(name), true,
          Set.empty, annotationTypeData)))

      cet1 must beFailing.like {
        case msgs => msgs.head must contain("annotation type(s) do not belong to study")
      }
    } tag ("tag1")
  }
}