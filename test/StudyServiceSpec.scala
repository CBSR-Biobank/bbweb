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

import test._
import service.{ StudyService, StudyProcessor }
import domain.{
  AnatomicalSourceType,
  AnnotationTypeId,
  ConcurrencySafeEntity,
  DomainValidation,
  DomainError,
  PreservationType,
  PreservationTemperatureType,
  SpecimenType
}
import domain.study._
import infrastructure._
import infrastructure.commands._

import scalaz._
import Scalaz._

@RunWith(classOf[JUnitRunner])
class StudyServiceSpec extends EventsourcedSpec[StudyServiceFixture.Fixture] {
  import fixture._

  def getStudy(v: DomainValidation[Study]): Study = {
    v match {
      case Success(s) => s
      case _ => throw new Error("null study, validation failed")
    }
  }

  "add a study" in {
    fragmentName: String =>
      val name = new NameGenerator(fragmentName).next[Study]
      val study = entityResult(studyService.addStudy(new AddStudyCmd(name, name)))
      studyRepository.getMap must not be empty
      studyRepository.getMap must haveKey(study.id)
      studyRepository.getByKey(study.id) must beSome.like {
        case s =>
          s.name must be(name)
          s.description must be(name)
      }
  }

  "update a study" in {
    fragmentName: String =>
      val ng = new NameGenerator(fragmentName)
      val name = ng.next[Study]
      val study = entityResult(studyService.addStudy(new AddStudyCmd(name, name)))

      val name2 = ng.next[Study]
      val study2 = entityResult(studyService.updateStudy(new UpdateStudyCmd(study.id.toString,
        study.versionOption, name2, name2)))

      studyRepository.getMap must not be empty
      studyRepository.getMap must haveKey(study2.id)
      studyRepository.getByKey(study2.id) must beSome.like {
        case s =>
          s.version must beEqualTo(study.version + 1)
          s.name must be(name2)
          s.description must be(name2)
      }
  }

  "enable a study" in {
    fragmentName: String =>
      val ng = new NameGenerator(fragmentName)
      val name = ng.next[Study]
      val units = ng.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val study = entityResult(studyService.addStudy(new AddStudyCmd(name, name)))

      val sg = entityResult(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study.id.toString, name, name, units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType)))

      entityResult(studyService.enableStudy(new EnableStudyCmd(study.id.toString, study.versionOption)))

      studyRepository.getByKey(study.id) must beSome.like {
        case s => s must beAnInstanceOf[EnabledStudy]
      }
  }

  "disable a study" in {
    fragmentName: String =>
      val ng = new NameGenerator(fragmentName)
      val name = ng.next[Study]
      val units = ng.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val study = entityResult(studyService.addStudy(new AddStudyCmd(name, name)))

      val sg = entityResult(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study.id.toString, name, name, units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType)))

      val study2 = entityResult(studyService.enableStudy(
        new EnableStudyCmd(study.id.toString, study.versionOption)))

      studyRepository.getByKey(study2.id) must beSome.like {
        case s => s must beAnInstanceOf[EnabledStudy]
      }

      entityResult(studyService.disableStudy(
        new DisableStudyCmd(study2.id.toString, study2.versionOption)))

      studyRepository.getByKey(study.id) must beSome.like {
        case s => s must beAnInstanceOf[DisabledStudy]
      }
  }

  "add a study with duplicate name" in {
    fragmentName: String =>
      val name = new NameGenerator(fragmentName).next[Study]
      result(studyService.addStudy(new AddStudyCmd(name, name))) must beSuccessful

      val r = result(studyService.addStudy(new AddStudyCmd(name, name)))
      (r match {
        case Failure(msglist) => msglist.head must contain("name already exists")
        case _ => failure
      }): Result
  }

  "add specimen groups" in {
    fragmentName: String =>
      val ng = new NameGenerator(fragmentName)
      val name = ng.next[Study]
      val units = ng.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val study1 = entityResult(studyService.addStudy(new AddStudyCmd(name, name)))

      val sg1 = entityResult(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study1.id.toString, name, name, units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType)))

      specimenGroupRepository.getMap must haveKey(sg1.id)
      specimenGroupRepository.getByKey(sg1.id) must beSome.like {
        case x =>
          x.description must be(name)
          x.units must be(units)
          x.anatomicalSourceType must be(anatomicalSourceType)
          x.preservationType must be(preservationType)
          x.preservationTemperatureType must be(preservationTempType)
          x.specimenType must be(specimenType)
      }

      val name2 = ng.next[Study]
      val sg2 = entityResult(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study1.id.toString, name2, name2, units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType)))

      specimenGroupRepository.getMap must haveKey(sg2.id)
      specimenGroupRepository.getByKey(sg2.id) must beSome.like {
        case x =>
          x.description must be(name2)
          x.description must be(name2)
          x.units must be(units)
          x.anatomicalSourceType must be(anatomicalSourceType)
          x.preservationType must be(preservationType)
          x.preservationTemperatureType must be(preservationTempType)
          x.specimenType must be(specimenType)
      }
  }

  "update specimen groups" in {
    fragmentName: String =>
      val ng = new NameGenerator(fragmentName)
      val name = ng.next[Study]
      val units = ng.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val study1 = entityResult(studyService.addStudy(new AddStudyCmd(name, name)))
      val sg1 = entityResult(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study1.id.toString, name, name, units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType)))

      val name2 = ng.next[Study]
      val units2 = ng.next[String]
      val anatomicalSourceType2 = AnatomicalSourceType.Brain
      val preservationType2 = PreservationType.FrozenSpecimen
      val preservationTempType2 = PreservationTemperatureType.Minus180celcius
      val specimenType2 = SpecimenType.DnaBlood

      val sg2 = entityResult(studyService.updateSpecimenGroup(
        new UpdateSpecimenGroupCmd(study1.id.toString, sg1.id.toString, sg1.versionOption, name2,
          name2, units2, anatomicalSourceType2, preservationType2, preservationTempType2, specimenType2)))

      specimenGroupRepository.getByKey(sg2.id) must beSome.like {
        case x =>
          x.version must beEqualTo(sg1.version + 1)
          x.name must be(name2)
          x.description must be(name2)
          x.units must be(units2)
          x.anatomicalSourceType must be(anatomicalSourceType2)
          x.preservationType must be(preservationType2)
          x.preservationTemperatureType must be(preservationTempType2)
          x.specimenType must be(specimenType2)
      }
  }

  "remove specimen group" in {
    fragmentName: String =>
      val ng = new NameGenerator(fragmentName)
      val name = ng.next[Study]
      val units = ng.next[String]
      val anatomicalSourceType = AnatomicalSourceType.Blood
      val preservationType = PreservationType.FreshSpecimen
      val preservationTempType = PreservationTemperatureType.Minus80celcius
      val specimenType = SpecimenType.FilteredUrine

      val study1 = entityResult(studyService.addStudy(new AddStudyCmd(name, name)))

      val sg1 = entityResult(studyService.addSpecimenGroup(
        new AddSpecimenGroupCmd(study1.id.toString, name, name, units, anatomicalSourceType,
          preservationType, preservationTempType, specimenType)))

      specimenGroupRepository.getMap must haveKey(sg1.id)
      entityResult(studyService.removeSpecimenGroup(
        new RemoveSpecimenGroupCmd(study1.id.toString, sg1.id.toString, sg1.versionOption)))

      specimenGroupRepository.getMap must not haveKey (sg1.id)
  }
}

object StudyServiceFixture {

  class Fixture extends EventsourcingFixture[DomainValidation[ConcurrencySafeEntity[_]]] {

    val studyRepository = new ReadWriteRepository[StudyId, Study](v => v.id)
    val specimenGroupRepository = new ReadWriteRepository[SpecimenGroupId, SpecimenGroup](v => v.id)
    val collectionEventTypeRepository = new ReadWriteRepository[CollectionEventTypeId, CollectionEventType](v => v.id)

    val cetAnnotationTypeRepo =
      new ReadWriteRepository[AnnotationTypeId, CollectionEventAnnotationType](v => v.id)

    val sg2cetRepo =
      new ReadWriteRepository[String, SpecimenGroupCollectionEventType](v => v.id)

    val cet2atRepo =
      new ReadWriteRepository[String, CollectionEventTypeAnnotationType](v => v.id)

    val studyProcessor = extension.processorOf(Props(
      new StudyProcessor(
        studyRepository,
        specimenGroupRepository,
        collectionEventTypeRepository,
        cetAnnotationTypeRepo,
        sg2cetRepo,
        cet2atRepo) with Emitter with Eventsourced { val id = 1 }))

    val studyService = new StudyService(studyRepository, specimenGroupRepository,
      collectionEventTypeRepository, studyProcessor)

    def result[T <: ConcurrencySafeEntity[_]](f: Future[DomainValidation[T]]) = {
      Await.result(f, timeout.duration)
    }

    def entityResult[T <: ConcurrencySafeEntity[_]](f: Future[DomainValidation[T]]): T = {
      result(f) match {
        case Success(e) => e
        case Failure(msg) => throw new Error("null entity, validation failed: " + msg)
      }
    }
  }
}
