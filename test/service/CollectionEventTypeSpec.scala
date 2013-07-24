package service

import fixture._
import domain.{
  AnatomicalSourceType,
  AnnotationValueType,
  PreservationType,
  PreservationTemperatureType,
  SpecimenType
}
import domain.study._
import service.commands._

import org.specs2.scalaz.ValidationMatchers._
import org.slf4j.LoggerFactory
//import scala.math.BigDecimal.double2bigDecimal
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CollectionEventTypeSpec extends StudyFixture {

  args(
    //include = "tag1",
    sequential = true) // forces all tests to be run sequentially

  private val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(classOf[CollectionEventTypeSpec].getName)
  val studyName = nameGenerator.next[Study]
  val study = await(studyService.addStudy(new AddStudyCmd(studyName, Some(studyName)))) | null

  "Collection event type" can {

    "be added" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(AddCollectionEventTypeCmd(
        study.id.id, name, Some(name), recurring, Set.empty, Set.empty)))

      cet1 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(0)
          x.name must be(name)
          x.description must beSome(name)
          x.recurring must beEqualTo(recurring)
          CollectionEventTypeRepository.collectionEventTypeWithId(study.id, x.id) must beSuccessful.like {
            case y =>
              y.version must beEqualTo(x.version)
          }
          CollectionEventTypeRepository.allCollectionEventTypesForStudy(study.id).size mustEqual 1
      }

      val name2 = nameGenerator.next[Study]
      val recurring2 = false

      val cet2 = await(studyService.addCollectionEventType(AddCollectionEventTypeCmd(
        study.id.id, name2, None, recurring2, Set.empty, Set.empty)))

      cet2 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(0)
          x.name must be(name2)
          x.description must beNone
          x.recurring must beEqualTo(recurring2)
          CollectionEventTypeRepository.collectionEventTypeWithId(study.id, x.id) must beSuccessful.like {
            case y =>
              y.version must beEqualTo(x.version)
          }
          CollectionEventTypeRepository.allCollectionEventTypesForStudy(study.id).size mustEqual 2

      }
    }

    "not be added if name already exists" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(AddCollectionEventTypeCmd(
        study.id.id, name, Some(name), recurring, Set.empty, Set.empty)))

      cet1 must beSuccessful

      val cet2 = await(studyService.addCollectionEventType(AddCollectionEventTypeCmd(
        study.id.id, name, Some(name), recurring, Set.empty, Set.empty)))

      cet2 must beFailing.like { case msgs => msgs.head must contain("already exists") }
    }

    "be updated" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, name, Some(name), recurring,
          Set.empty, Set.empty))) | null

      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful

      val name2 = nameGenerator.next[Study]
      val recurring2 = false

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(cet1.id.id, cet1.versionOption, study.id.id,
          name2, Some(name2), recurring2, Set.empty, Set.empty)))

      cet2 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(cet1.version + 1)
          x.name must be(name2)
          x.description must beSome(name2)
          x.recurring must beEqualTo(recurring2)

          CollectionEventTypeRepository.collectionEventTypeWithId(study.id, x.id) must beSuccessful.like {
            case y =>
              y.version must beEqualTo(x.version)
          }
      }
    }

    "not be updated to name that already exists" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, name, Some(name), recurring,
          Set.empty, Set.empty))) | null
      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful

      val name2 = nameGenerator.next[Study]
      val recurring2 = true
      val cet2 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, name2, Some(name2), recurring2,
          Set.empty, Set.empty))) | null
      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet2.id) must beSuccessful

      val cet3 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(cet2.id.id, cet2.versionOption, study.id.id,
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
        new AddCollectionEventTypeCmd(study.id.id, name, Some(name), recurring,
          Set.empty, Set.empty))) | null

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(cet1.id.id, cet1.versionOption, study2.id.id,
          name2, Some(name2), recurring, Set.empty, Set.empty)))
      cet2 must beFailing.like { case msgs => msgs.head must contain("study does not have collection event type") }
    }

    "not be updated with invalid version" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, name, Some(name), recurring,
          Set.empty, Set.empty))) | null

      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful

      val name2 = nameGenerator.next[Study]
      val recurring2 = false
      val versionOption = Some(cet1.version + 1)

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(cet1.id.id, versionOption, study.id.id,
          name2, Some(name2), recurring2, Set.empty, Set.empty)))
      cet2 must beFailing.like {
        case msgs => msgs.head must contain("doesn't match current version")
      }
    }

    "be removed" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, name, None, recurring,
          Set.empty, Set.empty))) | null
      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful

      await(studyService.removeCollectionEventType(
        new RemoveCollectionEventTypeCmd(cet1.id.id, cet1.versionOption, study.id.id)))

      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beFailing
    }

    "not be removed with invalid version" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, name, Some(name), recurring,
          Set.empty, Set.empty))) | null
      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful

      val versionOption = Some(cet1.version + 1)
      val cet2 = await(studyService.removeCollectionEventType(
        new RemoveCollectionEventTypeCmd(cet1.id.id, versionOption, study.id.id)))
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
        AddSpecimenGroupCmd(study.id.id, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null
      SpecimenGroupRepository.specimenGroupWithId(study.id, sg1.id) must beSuccessful

      val count = 10
      val amount = BigDecimal(1.1)
      val count2 = 5
      val amount2 = BigDecimal(0.1)
      val SpecimenGroupData = Set(
        CollectionEventTypeSpecimenGroup(sg1.id.id, count, amount),
        CollectionEventTypeSpecimenGroup(sg1.id.id, count2, amount2))

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, name, Some(name), recurring = true,
          SpecimenGroupData, Set.empty))) | null

      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful.like {
        case cet =>
          cet.specimenGroupData.size mustEqual 2
          cet.specimenGroupData.exists(sgData =>
            sgData.specimenGroupId.equals(sg1.id.id)
              && sgData.count.equals(count)
              && sgData.amount.equals(amount)) mustEqual true
          cet.specimenGroupData.exists(sgData =>
            sgData.specimenGroupId.equals(sg1.id.id)
              && sgData.count.equals(count2)
              && sgData.amount.equals(amount2)) mustEqual true
      }
    }

    "not be updated if used by collection event type" in {
      val sgName = nameGenerator.next[SpecimenGroup]

      val sg1 = await(studyService.addSpecimenGroup(
        AddSpecimenGroupCmd(study.id.id, sgName, Some(sgName), "mL", AnatomicalSourceType.Blood,
          PreservationType.FreshSpecimen, PreservationTemperatureType.Minus80celcius,
          SpecimenType.BuffyCoat))) | null
      SpecimenGroupRepository.specimenGroupWithId(study.id, sg1.id) must beSuccessful

      val SpecimenGroupData = Set(
        CollectionEventTypeSpecimenGroup(sg1.id.id, 1, BigDecimal(1.1)))

      val cetName = nameGenerator.next[CollectionEventType]
      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, cetName, Some(cetName), recurring = true,
          SpecimenGroupData, Set.empty))) | null

      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful

      val sg2 = await(studyService.updateSpecimenGroup(
        UpdateSpecimenGroupCmd(sg1.id.id, sg1.versionOption, study.id.id, sgName, Some(sgName),
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
        AddSpecimenGroupCmd(study.id.id, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null

      val count = 10
      val amount = BigDecimal(1.1)
      val count2 = 5
      val amount2 = BigDecimal(0.1)
      val SpecimenGroupData = Set(
        CollectionEventTypeSpecimenGroup(sg1.id.id, count, amount),
        CollectionEventTypeSpecimenGroup(sg1.id.id, count2, amount2))

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, name, Some(name), recurring = true,
          SpecimenGroupData, Set.empty))) | null

      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful.like {
        case x =>
          x.specimenGroupData.size mustEqual 2
      }

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(cet1.id.id, cet1.versionOption, study.id.id,
          name, Some(name), recurring = true, Set.empty, Set.empty))) | null

      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful.like {
        case x =>
          x.specimenGroupData.size mustEqual 0
      }
    }

    "not be removed if used by collection event type" in {
      val sgName = nameGenerator.next[SpecimenGroup]

      val sg1 = await(studyService.addSpecimenGroup(
        AddSpecimenGroupCmd(study.id.id, sgName, Some(sgName), "mL", AnatomicalSourceType.Blood,
          PreservationType.FreshSpecimen, PreservationTemperatureType.Minus80celcius,
          SpecimenType.BuffyCoat))) | null
      SpecimenGroupRepository.specimenGroupWithId(study.id, sg1.id) must beSuccessful

      val SpecimenGroupData = Set(
        CollectionEventTypeSpecimenGroup(sg1.id.id, 1, BigDecimal(1.1)))

      val cetName = nameGenerator.next[CollectionEventType]
      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, cetName, Some(cetName), recurring = true,
          SpecimenGroupData, Set.empty))) | null

      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful

      val sg2 = await(studyService.removeSpecimenGroup(
        RemoveSpecimenGroupCmd(sg1.id.id, sg1.versionOption, study.id.id)))

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
        AddSpecimenGroupCmd(study.id.id, name3, Some(name3), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null

      // this one is in wrong study
      val sg2 = await(studyService.addSpecimenGroup(
        AddSpecimenGroupCmd(study2.id.id, name3, Some(name3), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null

      val count = 10
      val amount = BigDecimal(1.1)
      val SpecimenGroupData = Set(
        CollectionEventTypeSpecimenGroup(sg1.id.id, count, amount),
        CollectionEventTypeSpecimenGroup(sg2.id.id, count, amount))

      val name = nameGenerator.next[CollectionEventType]
      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, name, Some(name), true,
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
        new AddCollectionEventAnnotationTypeCmd(study.id.id, name, Some(name),
          AnnotationValueType.Date))) | null

      CollectionEventAnnotationTypeRepository.annotationTypeWithId(study.id, at1.id) must beSuccessful

      val annotationTypeData = Set(
        CollectionEventTypeAnnotationType(at1.id.id, true))

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, name, Some(name), true,
          Set.empty, annotationTypeData))) | null

      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful.like {
        case x =>
          x.annotationTypeData.size mustEqual 1
          x.annotationTypeData.exists(atData =>
            atData.annotationTypeId.equals(at1.id.id)
              && atData.required.equals(required)) mustEqual true
      }
    }

    "not be updated if used by collection event type" in {
      val name = nameGenerator.next[CollectionEventAnnotationType]

      val at1 = await(studyService.addCollectionEventAnnotationType(
        AddCollectionEventAnnotationTypeCmd(study.id.id, name, Some(name),
          AnnotationValueType.Date))) | null

      CollectionEventAnnotationTypeRepository.annotationTypeWithId(study.id, at1.id) must beSuccessful

      val annotationTypeData = Set(
        CollectionEventTypeAnnotationType(at1.id.id, true))

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, name, Some(name), true,
          Set.empty, annotationTypeData))) | null

      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful

      val at2 = await(studyService.updateCollectionEventAnnotationType(
        UpdateCollectionEventAnnotationTypeCmd(at1.id.id, at1.versionOption, study.id.id,
          name, Some(name), AnnotationValueType.Number)))

      at2 must beFailing.like {
        case msgs => msgs.head must contain(
          "annotation type is in use by collection event type")
      }
    }

    "be removed from collection event type" in {
      val name = nameGenerator.next[CollectionEventTypeAnnotationType]

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.id, name, Some(name),
          AnnotationValueType.Date, Some(0), Some(Map.empty[String, String])))) | null

      val annotationTypeData = Set(
        CollectionEventTypeAnnotationType(at1.id.id, true))

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, name, Some(name), true,
          Set.empty, annotationTypeData))) | null

      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful.like {
        case x =>
          x.annotationTypeData.size mustEqual 1
        // FIXME: add test for values of the SpecimenGroupCollectionEventType
      }

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(cet1.id.id, cet1.versionOption, study.id.id,
          name, Some(name), recurring = true, Set.empty, Set.empty))) | null

      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful.like {
        case x =>
          x.annotationTypeData.size mustEqual 0
      }
    }

    "not be removed if used by collection event type" in {
      val name = nameGenerator.next[CollectionEventAnnotationType]

      val at1 = await(studyService.addCollectionEventAnnotationType(
        AddCollectionEventAnnotationTypeCmd(study.id.id, name, Some(name),
          AnnotationValueType.Date))) | null

      CollectionEventAnnotationTypeRepository.annotationTypeWithId(study.id, at1.id) must beSuccessful

      val annotationTypeData = Set(
        CollectionEventTypeAnnotationType(at1.id.id, true))

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, name, Some(name), true,
          Set.empty, annotationTypeData))) | null

      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful

      val at2 = await(studyService.removeCollectionEventAnnotationType(
        RemoveCollectionEventAnnotationTypeCmd(at1.id.id, at1.versionOption, study.id.id)))

      at2 must beFailing.like {
        case msgs => msgs.head must contain(
          "annotation type is in use by collection event type")
      }

    }

    "be removed from collection event type" in {
      val name = nameGenerator.next[CollectionEventTypeAnnotationType]

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.id, name, Some(name),
          AnnotationValueType.Date, Some(0), Some(Map.empty[String, String])))) | null

      val annotationTypeData = Set(
        CollectionEventTypeAnnotationType(at1.id.id, true))

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, name, Some(name), true,
          Set.empty, annotationTypeData))) | null

      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful.like {
        case x =>
          x.annotationTypeData.size mustEqual 1
        // FIXME: add test for values of the SpecimenGroupCollectionEventType
      }

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(cet1.id.id, cet1.versionOption, study.id.id,
          name, Some(name), recurring = true, Set.empty, Set.empty))) | null

      CollectionEventTypeRepository.collectionEventTypeWithId(study.id, cet1.id) must beSuccessful.like {
        case x =>
          x.annotationTypeData.size mustEqual 0
      }
    }

    "not be added if annotation type in wrong study" in {
      val name2 = nameGenerator.next[Study]
      val study2 = await(studyService.addStudy(new AddStudyCmd(name2, Some(name2)))) | null

      // this one is in correct study
      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.id,
          nameGenerator.next[CollectionEventTypeAnnotationType], None,
          AnnotationValueType.Date, Some(0), Some(Map.empty[String, String])))) | null

      // this one is in other study
      val at2 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study2.id.id,
          nameGenerator.next[CollectionEventTypeAnnotationType], None,
          AnnotationValueType.Date, Some(0), Some(Map.empty[String, String])))) | null

      val annotationTypeData = Set(
        CollectionEventTypeAnnotationType(at1.id.id, true),
        CollectionEventTypeAnnotationType(at2.id.id, true))

      val name = nameGenerator.next[CollectionEventType]
      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.id, name, Some(name), true,
          Set.empty, annotationTypeData)))

      cet1 must beFailing.like {
        case msgs => msgs.head must contain("annotation type(s) do not belong to study")
      }
    } tag ("tag1")
  }
}