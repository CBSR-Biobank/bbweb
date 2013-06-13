import test._
import fixture._
import infrastructure._
import service.{ StudyService, StudyProcessor }
import domain.{
  AnatomicalSourceType,
  AnnotationTypeId,
  AnnotationValueType,
  ConcurrencySafeEntity,
  PreservationType,
  PreservationTemperatureType,
  SpecimenType
}
import AnnotationValueType._
import domain.study._
import infrastructure._
import infrastructure.commands._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import org.specs2.specification.BeforeExample
import org.specs2.scalaz.ValidationMatchers._
import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.execute.Result
import akka.actor._
import akka.util.Timeout
import org.eligosource.eventsourced.core._

import scalaz._
import Scalaz._

@RunWith(classOf[JUnitRunner])
class CollectionEventTypeSpec extends StudyFixture {
  sequential // forces all tests to be run sequentially

  val nameGenerator = new NameGenerator(classOf[CollectionEventTypeSpec].getName)
  val studyName = nameGenerator.next[Study]
  val study = await(studyService.addStudy(new AddStudyCmd(studyName, studyName))) | null

  "Collection event type" can {

    "be added" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, name, recurring)))

      cet1 must beSuccessful.like {
        case x =>
          collectionEventTypeRepository.getMap must haveKey(x.id)
          x.version must beEqualTo(0)
          x.name must be(name)
          x.description must be(name)
          x.recurring must beEqualTo(recurring)
      }

      val name2 = nameGenerator.next[Study]
      val recurring2 = false

      val cet2 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name2, name2, recurring2)))

      cet2 must beSuccessful.like {
        case x =>
          collectionEventTypeRepository.getMap must haveKey(x.id)
          x.version must beEqualTo(0)
          x.name must be(name2)
          x.description must be(name2)
          x.recurring must beEqualTo(recurring2)
      }
    }

    "not be added if name already exists" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val study = await(studyService.addStudy(new AddStudyCmd(name, name))) | null
      await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, name, recurring)))

      val cet = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, name, recurring)))
      cet must beFailing.like { case msgs => msgs.head must contain("already exists") }
    }

    "be updated" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val study1 = await(studyService.addStudy(new AddStudyCmd(name, name))) | null

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study1.id.toString, name, name, recurring))) | null

      collectionEventTypeRepository.getMap must haveKey(cet1.id)
      collectionEventTypeRepository.getByKey(cet1.id) must beSuccessful.like {
        case x =>
          x.version must beEqualTo(0)
          x.name must be(name)
          x.description must be(name)
          x.recurring must beEqualTo(recurring)
      }

      val name2 = nameGenerator.next[Study]
      val recurring2 = false

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(study1.id.toString, cet1.id.toString, cet1.versionOption,
          name2, name2, recurring2))) | null

      collectionEventTypeRepository.getMap must haveKey(cet2.id)
      collectionEventTypeRepository.getByKey(cet2.id) must beSuccessful.like {
        case x =>
          x.version must beEqualTo(cet1.version + 1)
          x.name must be(name2)
          x.description must be(name2)
          x.recurring must beEqualTo(recurring2)
      }
    }

    "not be updated to name that already exists" in {
      ko
    }

    "not be updated to wrong study" in {
      val name = nameGenerator.next[Study]

      val name2 = nameGenerator.next[Study]
      val recurring = true

      val study1 = await(studyService.addStudy(new AddStudyCmd(name, name))) | null
      val study2 = await(studyService.addStudy(new AddStudyCmd(name2, name2))) | null

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study1.id.toString, name, name, recurring))) | null

      val cet2 = await(studyService.updateCollectionEventType(
        new UpdateCollectionEventTypeCmd(study2.id.toString, cet1.id.toString, cet1.versionOption,
          name2, name2, recurring)))
      cet2 must beFailing.like { case msgs => msgs.head must contain("does not belong to study") }
    }

    "be removed" in {
      val name = nameGenerator.next[Study]
      val recurring = true

      val study1 = await(studyService.addStudy(new AddStudyCmd(name, name))) | null

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study1.id.toString, name, name, recurring))) | null
      collectionEventTypeRepository.getMap must haveKey(cet1.id)

      await(studyService.removeCollectionEventType(
        new RemoveCollectionEventTypeCmd(study1.id.toString, cet1.id.toString, cet1.versionOption)))

      collectionEventTypeRepository.getMap must not haveKey (cet1.id)
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
        AddSpecimenGroupCmd(study.id.toString, name, name, units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null
      specimenGroupRepository.getMap must haveKey(sg1.id)

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, name, recurring = true))) | null
      collectionEventTypeRepository.getMap must haveKey(cet1.id)

      val sg2cet1 = await(studyService.addSpecimenGroupToCollectionEventType(
        AddSpecimenGroupToCollectionEventTypeCmd(study.id.toString,
          sg1.id.toString, cet1.id.toString, 1, 1.0))) | null

      sg2cetRepo.getMap must haveKey(sg2cet1.id)
    }

    "can be removed" in {
      val name = nameGenerator.next[Study]
      val units = nameGenerator.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val sg1 = await(studyService.addSpecimenGroup(
        AddSpecimenGroupCmd(study.id.toString, name, name, units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType))) | null

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, name, recurring = true))) | null

      val sg2cet1 = await(studyService.addSpecimenGroupToCollectionEventType(
        AddSpecimenGroupToCollectionEventTypeCmd(study.id.toString,
          sg1.id.toString, cet1.id.toString, 1, 1.0))) | null

      val sg2cet2 = await(studyService.removeSpecimenGroupFromCollectionEventType(
        RemoveSpecimenGroupFromCollectionEventTypeCmd(sg2cet1.id.toString, study.id.toString)))

      sg2cetRepo.getMap must not haveKey (sg2cet1.id)
    }
  }

  "Annotation type -> collection event type" can {

    "can be added" in {
      val name = nameGenerator.next[CollectionEventTypeAnnotationType]
      val required = true

      val cet1 = await(studyService.addCollectionEventType(
        new AddCollectionEventTypeCmd(study.id.toString, name, name, true))) | null

      val at1 = await(studyService.addCollectionEventAnnotationType(
        new AddCollectionEventAnnotationTypeCmd(study.id.toString, name, name,
          AnnotationValueType.Date, 0, Map.empty[String, String]))) | null

      val at2cet = await(studyService.addAnnotationTypeToCollectionEventType(
        AddAnnotationTypeToCollectionEventTypeCmd(study.id.toString,
          cet1.id.toString, at1.id.toString, required)))

      at2cet must beSuccessful.like {
        case x =>
          x.annotationTypeId must beEqualTo(at1.id)
          x.collectionEventTypeId must beEqualTo(cet1.id)
          x.required must beEqualTo(required)
          at2cetRepo.getMap must haveKey(x.id)
      }
    }
  }
}