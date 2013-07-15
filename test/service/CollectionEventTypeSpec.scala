package service

import fixture._
import infrastructure._
import domain.{ AnatomicalSourceType, AnnotationValueType, PreservationType, PreservationTemperatureType, SpecimenType }
import domain.AnnotationValueType._
import domain.study._
import infrastructure._
import infrastructure.commands._
import scala.concurrent._
import scala.concurrent.duration._
import org.specs2.scalaz.ValidationMatchers._
import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import akka.actor._
import org.eligosource.eventsourced.core._
import scalaz._
import scalaz.Scalaz._
import scala.math.BigDecimal.double2bigDecimal

@RunWith(classOf[JUnitRunner])
class CollectionEventTypeSpec extends StudyFixture {
  sequential // forces all tests to be run sequentially

  val nameGenerator = new NameGenerator(classOf[CollectionEventTypeSpec].getName)
  val studyName = nameGenerator.next[Study]
  val study = await(studyService.addStudy(new AddStudyCmd(studyName, Some(studyName)))) | null

  "Collection event type" can {

    "be added" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, Some(name), recurring,
          Set.empty, Set.empty)))

      cet1 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(0)
          x.name must be(name)
          x.description must beSome(name)
          x.recurring must beEqualTo(recurring)
          collectionEventTypeRepository.getMap must haveKey(x.id)
          collectionEventTypeRepository.getByKey(x.id) must beSuccessful.like {
            case y =>
              y.version must beEqualTo(x.version)
          }
      }

      val name2 = nameGenerator.next[Study]
      val recurring2 = false

      val cet2 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name2, None, recurring2,
          Set.empty, Set.empty)))

      cet2 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(0)
          x.name must be(name2)
          x.description must beNone
          x.recurring must beEqualTo(recurring2)
          collectionEventTypeRepository.getMap must haveKey(x.id)
          collectionEventTypeRepository.getByKey(x.id) must beSuccessful.like {
            case y =>
              y.version must beEqualTo(x.version)
          }
      }
    }

    "not be added if name already exists" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, Some(name), recurring,
          Set.empty, Set.empty)))

      val cet = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, Some(name), recurring,
          Set.empty, Set.empty)))
      cet must beFailing.like { case msgs => msgs.head must contain("already exists") }
    }

    "be updated" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, Some(name), recurring,
          Set.empty, Set.empty))) | null

      collectionEventTypeRepository.getMap must haveKey(cet1.id)

      val name2 = nameGenerator.next[Study]
      val recurring2 = false

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(cet1.id.toString, cet1.versionOption, study.id.toString,
          name2, Some(name2), recurring2, Set.empty, Set.empty)))

      cet2 must beSuccessful.like {
        case x =>
          x.version must beEqualTo(cet1.version + 1)
          x.name must be(name2)
          x.description must beSome(name2)
          x.recurring must beEqualTo(recurring2)
          collectionEventTypeRepository.getMap must haveKey(x.id)
          collectionEventTypeRepository.getByKey(x.id) must beSuccessful.like {
            case y =>
              y.version must beEqualTo(x.version)
          }
      }
    }

    "not be updated to name that already exists" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, Some(name), recurring,
          Set.empty, Set.empty))) | null
      collectionEventTypeRepository.getMap must haveKey(cet1.id)

      val name2 = nameGenerator.next[Study]
      val recurring2 = true
      val cet2 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name2, Some(name2), recurring2,
          Set.empty, Set.empty))) | null
      collectionEventTypeRepository.getMap must haveKey(cet1.id)

      val cet3 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(cet2.id.toString, cet2.versionOption, study.id.toString,
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
        new AddCollectionEventTypeCmd(study.id.toString, name, Some(name), recurring,
          Set.empty, Set.empty))) | null

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(cet1.id.toString, cet1.versionOption, study2.id.toString,
          name2, Some(name2), recurring, Set.empty, Set.empty)))
      cet2 must beFailing.like { case msgs => msgs.head must contain("does not belong to study") }
    }

    "not be updated with invalid version" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, Some(name), recurring,
          Set.empty, Set.empty))) | null

      collectionEventTypeRepository.getMap must haveKey(cet1.id)

      val name2 = nameGenerator.next[Study]
      val recurring2 = false
      val versionOption = Some(cet1.version + 1)

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(cet1.id.toString, versionOption, study.id.toString,
          name2, Some(name2), recurring2, Set.empty, Set.empty)))
      cet2 must beFailing.like {
        case msgs => msgs.head must contain("doesn't match current version")
      }
    }

    "be removed" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, None, recurring,
          Set.empty, Set.empty))) | null
      collectionEventTypeRepository.getMap must haveKey(cet1.id)

      await(studyService.removeCollectionEventType(
        new RemoveCollectionEventTypeCmd(cet1.id.toString, cet1.versionOption, study.id.toString)))

      collectionEventTypeRepository.getMap must not haveKey (cet1.id)
    }

    "not be removed with invalid version" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, Some(name), recurring,
          Set.empty, Set.empty))) | null
      collectionEventTypeRepository.getMap must haveKey(cet1.id)

      val versionOption = Some(cet1.version + 1)
      val cet2 = await(studyService.removeCollectionEventType(
        new RemoveCollectionEventTypeCmd(cet1.id.toString, versionOption, study.id.toString)))
      cet2 must beFailing.like {
        case msgs => msgs.head must contain("doesn't match current version")
      }
    }
  }

  "Specimen group -> collection event type" can {

    "can be added" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val sg1 = await(studyService.addSpecimenGroup(
        AddSpecimenGroupCmd(study.id.toString, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null
      specimenGroupRepository.getMap must haveKey(sg1.id)

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, Some(name), recurring = true,
          Set(sg1.id.id), Set.empty))) | null

      SpecimenGroupCollectionEventTypeRepository.getValues.filter(
        x => x.collectionEventTypeId.equals(cet1.id)) must contain(
          (x: SpecimenGroupCollectionEventType) =>
            x.specimenGroupId mustEqual (sg1.id))
    }

    "can be removed" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val sg1 = await(studyService.addSpecimenGroup(
        AddSpecimenGroupCmd(study.id.toString, name, Some(name), units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, Some(name), recurring = true,
          Set(sg1.id.id), Set.empty))) | null

      SpecimenGroupCollectionEventTypeRepository.getValues.filter(
        x => x.collectionEventTypeId.equals(cet1.id)) must contain(
          (x: SpecimenGroupCollectionEventType) =>
            x.specimenGroupId mustEqual (sg1.id))

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(cet1.id.toString, cet1.versionOption, study.id.toString,
          name, Some(name), recurring = true, Set.empty, Set.empty))) | null

      CollectionEventTypeAnnotationTypeRepository.getValues.filter(
        x => x.collectionEventTypeId.equals(cet1.id)) must contain(
          (x: CollectionEventTypeAnnotationType) =>
            x.annotationTypeId mustNotEqual (sg1.id))
    }
  }

  "Annotation type -> collection event type" can {

    "can be added" in {
      val name = nameGenerator.next[CollectionEventTypeAnnotationType]
      val required = true

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.toString, name, Some(name),
          AnnotationValueType.Date, Some(0), Some(Map.empty[String, String])))) | null

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, Some(name), true,
          Set.empty, Set(at1.id.id)))) | null

      CollectionEventTypeAnnotationTypeRepository.getValues.filter(
        x => x.collectionEventTypeId.equals(cet1.id)) must contain(
          (x: CollectionEventTypeAnnotationType) =>
            x.annotationTypeId mustEqual (at1.id))
    }

    "can be removed" in {
      val name = nameGenerator.next[CollectionEventTypeAnnotationType]
      val required = true

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.toString, name, Some(name),
          AnnotationValueType.Date, Some(0), Some(Map.empty[String, String])))) | null

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, Some(name), true,
          Set.empty, Set(at1.id.id)))) | null

      CollectionEventTypeAnnotationTypeRepository.getValues.filter(
        x => x.collectionEventTypeId.equals(cet1.id)) must contain(
          (x: CollectionEventTypeAnnotationType) =>
            x.annotationTypeId mustEqual (at1.id))

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(cet1.id.toString, cet1.versionOption, study.id.toString,
          name, Some(name), recurring = true, Set.empty, Set.empty))) | null

      CollectionEventTypeAnnotationTypeRepository.getValues.filter(
        x => x.collectionEventTypeId.equals(cet1.id)) must contain(
          (x: CollectionEventTypeAnnotationType) =>
            x.annotationTypeId mustNotEqual (at1.id))
    }
  }
}