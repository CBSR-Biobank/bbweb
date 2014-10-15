package org.biobank.service.study

import org.biobank.fixture._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.{
  AnatomicalSourceType,
  AnnotationValueType,
  DomainError,
  DomainValidation,
  PreservationType,
  PreservationTemperatureType,
  SpecimenType
}
import org.biobank.domain.study._

import org.joda.time.DateTime
import org.scalatest.Tag
import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

/**
  * Tests for actor StudiesProcessorSpec. These are written using ScalaTest.
  *
  */
class StudiesProcessorSpec extends TestFixture {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val studyRepository = inject [StudyRepository]

  val specimenGroupRepository = inject [SpecimenGroupRepository]

  val collectionEventTypeRepository = inject [CollectionEventTypeRepository]

  val studiesProcessor = injectActorRef [StudiesProcessor] ("studies")

  val nameGenerator = new NameGenerator(this.getClass)

  private def askAddCommand(study: Study): DomainValidation[StudyAddedEvent]  = {
    val cmd = AddStudyCmd(study.name, study.description)
    ask(studiesProcessor, cmd).mapTo[DomainValidation[StudyAddedEvent]].futureValue
  }

  private def askUpdateCommand(study: Study): DomainValidation[StudyUpdatedEvent] = {
    val cmd = UpdateStudyCmd(study.id.id, study.version, study.name, study.description)
    ask(studiesProcessor, cmd).mapTo[DomainValidation[StudyUpdatedEvent]].futureValue
  }

  "A study processor" can {

    "add a study" in {
      val study = factory.createDisabledStudy

      askAddCommand(study) mustSucceed { event =>
        event mustBe a [StudyAddedEvent]
        event must have (
          'name (study.name),
          'description (study.description)
        )

        studyRepository.getDisabled(StudyId(event.id)) mustSucceed { repoStudy =>
          checkTimeStamps(repoStudy, DateTime.now, None)
        }
      }
    }

    "not add add a new study with a duplicate name" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)
      askAddCommand(study) mustFail "study with name already exists"
    }

    "update a study leaving name unchanged" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val study2 = study.copy(description = Some(nameGenerator.next[String]))

      askUpdateCommand(study2) mustSucceed { event =>
        event mustBe a[StudyUpdatedEvent]
        event must have (
          'name (study2.name),
          'description (study2.description)
        )
        studyRepository.getDisabled(StudyId(event.id)) mustSucceed { repoStudy =>
          checkTimeStamps(repoStudy, study.timeAdded, DateTime.now)
        }
      }
    }

    "update a study with new a name or description" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val study2 = study.copy(name = nameGenerator.next[Study])

      askUpdateCommand(study2) mustSucceed { event =>
        event mustBe a[StudyUpdatedEvent]
        event must have (
          'name (study2.name),
          'description (study2.description)
        )

        studyRepository.getDisabled(StudyId(event.id)) mustSucceed { repoStudy =>
          repoStudy.version mustBe (study.version + 1)
          checkTimeStamps(repoStudy, study.timeAdded, DateTime.now)
        }
      }

      // update something other than the name
      val study3 = study2.copy(
        version = study.version + 1,
        description = Some(nameGenerator.next[Study]))

      askUpdateCommand(study3) mustSucceed { event =>
        event must have (
          'name (study3.name),
          'description (study3.description)
        )
        studyRepository.getDisabled(StudyId(event.id)) mustSucceed { repoStudy =>
          checkTimeStamps(repoStudy, study.timeAdded, DateTime.now)
        }
      }
    }

    "not update a study to name that is used by another study" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val study3 = study2.copy(name = study.name)
      askUpdateCommand(study3) mustFail "name already exists"
    }

    "not be updated with invalid version" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val study2 = study.copy(version = study.version + 1)

      askUpdateCommand(study2) mustFail "doesn't match current version"
    }

    "enable a study" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      ask(studiesProcessor, EnableStudyCmd(study.id.toString, study.version))
        .mapTo[DomainValidation[StudyEnabledEvent]]
        .futureValue
        .mustSucceed { event =>
        event mustBe a[StudyEnabledEvent]
        studyRepository.getEnabled(StudyId(event.id)).fold(
          err => fail(err.list.mkString),
          repoStudy => checkTimeStamps(repoStudy, study.timeAdded, DateTime.now)
        )
      }
    }

    "not enable a study with no specimen groups" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val v = ask(studiesProcessor, EnableStudyCmd(study.id.toString, study.version))
        .mapTo[DomainValidation[StudyEnabledEvent]]
        .futureValue
      v mustFail "no specimen groups"
    }

    "not enable a study with no collection event types" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val v = ask(studiesProcessor, EnableStudyCmd(study.id.toString, study.version))
        .mapTo[DomainValidation[StudyEnabledEvent]]
        .futureValue
      v mustFail "no collection event types"
    }

    "not update an enabled study" in {
      val study = factory.createEnabledStudy
      studyRepository.put(study)
      askUpdateCommand(study) mustFail "is not disabled"
    }

    "disable an enabled study" in {
      val enabledStudy = factory.createEnabledStudy
      studyRepository.put(enabledStudy)

      ask(studiesProcessor, DisableStudyCmd(enabledStudy.id.toString, enabledStudy.version))
        .mapTo[DomainValidation[StudyDisabledEvent]]
        .futureValue
        .mustSucceed { event =>
        event mustBe a[StudyDisabledEvent]
          studyRepository.getDisabled(StudyId(event.id)) mustSucceed { repoStudy =>
            checkTimeStamps(repoStudy, enabledStudy.timeAdded, DateTime.now)
          }
      }
    }

    "not disable a study that does not exist" in {
      val studyId = nameGenerator.next[Study]

      val v = ask(studiesProcessor, DisableStudyCmd(studyId, 0L))
        .mapTo[DomainValidation[StudyDisabledEvent]]
        .futureValue
      v mustFail s"invalid study id: $studyId"
    }

    "retire a study" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val v = ask(studiesProcessor, RetireStudyCmd(study.id.toString, study.version))
        .mapTo[DomainValidation[StudyRetiredEvent]]
        .futureValue
      v mustSucceed { event =>
        event mustBe a[StudyRetiredEvent]
        studyRepository.getRetired(StudyId(event.id)) mustSucceed { repoStudy =>
          checkTimeStamps(repoStudy, study.timeAdded, DateTime.now)
        }
      }
    }

    "not update a retired study" in {
      val study = factory.createEnabledStudy
      studyRepository.put(study)
      askUpdateCommand(study) mustFail "is not disabled"
    }

    "unretire a study" in {
      val study = factory.createRetiredStudy
      studyRepository.put(study)

      val v = ask(studiesProcessor, UnretireStudyCmd(study.id.toString, study.version))
        .mapTo[DomainValidation[StudyUnretiredEvent]]
        .futureValue
      v mustSucceed { event =>
        event mustBe a[StudyUnretiredEvent]
        studyRepository.getDisabled(StudyId(event.id)) mustSucceed { repoStudy =>
          checkTimeStamps(repoStudy, study.timeAdded, DateTime.now)
        }
      }
    }

    "not unretire a disabled study" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val v = ask(studiesProcessor, UnretireStudyCmd(study.id.toString, study.version))
        .mapTo[DomainValidation[StudyUnretiredEvent]]
        .futureValue
      v mustFail "is not retired"
    }

    "not unretire an enabled study" in {
      val study = factory.createEnabledStudy
      studyRepository.put(study)

      val v = ask(studiesProcessor, UnretireStudyCmd(study.id.toString, study.version))
        .mapTo[DomainValidation[StudyUnretiredEvent]]
        .futureValue
      v mustFail "is not retired"
    }

    "be recovered from journal" ignore {
      /*
       * Not sure if this is a good test, or how to do it correctly - ignoring it for now
       */
      // val study = factory.createDisabledStudy

      // var cmd: StudyCommand = AddStudyCmd(study.name, study.description)
      // val validation = ask(studiesProcessor, cmd).mapTo[DomainValidation[StudyAddedEvent]]
      //   .futureValue

      // validation mustBe ('success)
      // val event = validation.getOrElse(fail)

      // Thread.sleep(10)

      // Await.result(gracefulStop(studiesProcessor, 5 seconds, PoisonPill), 6 seconds)

      // // restart
      // val newStudiesProcessor = system.actorOf(Props(new StudiesProcessor), "studyproc")

      // Thread.sleep(10)

      // val newName = nameGenerator.next[Study]
      // val newDescription = some(nameGenerator.next[Study])

      // cmd = UpdateStudyCmd(event.id, 0, newName, newDescription)
      // val validation2 = ask(newStudiesProcessor, cmd).mapTo[DomainValidation[StudyUpdatedEvent]]
      //   .futureValue

      // validation2 mustSucceed { event =>
      //   event mustBe a[StudyUpdatedEvent]
      // }
      ???
    }
  }
}
