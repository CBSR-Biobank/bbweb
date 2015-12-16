package org.biobank.service.study

import org.biobank.fixture._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
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

import akka.pattern._
import akka.testkit.{ TestActors, TestKit, ImplicitSender }
import org.joda.time.DateTime
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import akka.actor.Props

import scalaz.Scalaz._

/**
 * Tests for actor StudiesProcessorSpec. These are written using ScalaTest.
 *
 */
class StudiesProcessorSpec extends TestFixture {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  private def askAddCommand(study: Study): DomainValidation[StudyEvent]  = {
    val cmd = AddStudyCmd(None, study.name, study.description)
    ask(studiesProcessor, cmd).mapTo[DomainValidation[StudyEvent]].futureValue
  }

  private def askUpdateCommand(study: Study): DomainValidation[StudyEvent] = {
    val cmd = UpdateStudyCmd(None, study.id.id, study.version, study.name, study.description)
    ask(studiesProcessor, cmd).mapTo[DomainValidation[StudyEvent]].futureValue
  }

  "A study processor" can {

    "add a study" in {
      val study = factory.createDisabledStudy

      askAddCommand(study) mustSucceed { event =>
        event mustBe a [StudyEvent]
        event.id.size must be > 0

        val addedEvent = event.getAdded

        addedEvent must have (
          'name        (Some(study.name)),
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
      askAddCommand(study) mustFailContains "study with name already exists"
    }

    "not add add a new study with a name less than 2 characters" in {
      val study = factory.createDisabledStudy.copy(name = "a")
      askAddCommand(study) mustFail "InvalidName"
    }

    "update a study leaving name unchanged" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val study2 = study.copy(description = Some(nameGenerator.next[String]))

      askUpdateCommand(study2) mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (study.id.id)

        val updatedEvent = event.getUpdated
        updatedEvent must have (
          'name        (Some(study2.name)),
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
        event mustBe a[StudyEvent]
        event.id must be (study.id.id)

        event.getUpdated must have (
          'name        (Some(study2.name)),
          'description (study2.description)
        )

        studyRepository.getDisabled(StudyId(event.id)) mustSucceed { repoStudy =>
          repoStudy.version must be > study.version
          checkTimeStamps(repoStudy, study.timeAdded, DateTime.now)
        }

        // update something other than the name
        val study3 = study2.copy(version     = event.getUpdated.getVersion,
                                 description = Some(nameGenerator.next[Study]))

        askUpdateCommand(study3) mustSucceed { event =>
          event.getUpdated must have (
            'name        (Some(study3.name)),
            'description (study3.description)
          )
          studyRepository.getDisabled(StudyId(event.id)) mustSucceed { repoStudy =>
            checkTimeStamps(repoStudy, study.timeAdded, DateTime.now)
          }
        }
      }
    }

    "not update a study to name that is used by another study" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val study2 = factory.createDisabledStudy
      studyRepository.put(study2)

      val study3 = study2.copy(name = study.name)
      askUpdateCommand(study3) mustFailContains "name already exists"
    }

    "not update a study's name to something less than 2 characters" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val study2 = study.copy(name = "a")
      askUpdateCommand(study2) mustFail "InvalidName"
    }

    "not be updated with invalid version" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val study2 = study.copy(version = study.version + 1)
      askUpdateCommand(study2) mustFailContains "doesn't match current version"
    }

    "enable a study" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      ask(studiesProcessor, EnableStudyCmd(None, study.id.toString, study.version))
      .mapTo[DomainValidation[StudyEvent]]
      .futureValue
      .mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (study.id.id)
        event.getEnabled.version must be (Some(study.version + 1))
        studyRepository.getEnabled(StudyId(event.id)).fold(
          err => fail(err.list.toList.mkString),
          repoStudy => checkTimeStamps(repoStudy, study.timeAdded, DateTime.now)
        )
      }
    }

    "not enable a study with no specimen groups" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val cet = factory.createCollectionEventType
      collectionEventTypeRepository.put(cet)

      val v = ask(studiesProcessor, EnableStudyCmd(None, study.id.toString, study.version))
      .mapTo[DomainValidation[StudyEvent]]
      .futureValue
      v mustFail "no specimen groups"
    }

    "not enable a study with no collection event types" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val sg = factory.createSpecimenGroup
      specimenGroupRepository.put(sg)

      val v = ask(studiesProcessor, EnableStudyCmd(None, study.id.toString, study.version))
      .mapTo[DomainValidation[StudyEvent]]
      .futureValue
      v mustFail "no collection event types"
    }

    "not update an enabled study" in {
      val study = factory.createEnabledStudy
      studyRepository.put(study)
      askUpdateCommand(study) mustFailContains "is not disabled"
    }

    "disable an enabled study" in {
      val enabledStudy = factory.createEnabledStudy
      studyRepository.put(enabledStudy)

      ask(studiesProcessor, DisableStudyCmd(None, enabledStudy.id.toString, enabledStudy.version))
      .mapTo[DomainValidation[StudyEvent]]
      .futureValue
      .mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (enabledStudy.id.id)
        event.getDisabled.version must be (Some(enabledStudy.version + 1))
        studyRepository.getDisabled(StudyId(event.id)) mustSucceed { repoStudy =>
          checkTimeStamps(repoStudy, enabledStudy.timeAdded, DateTime.now)
        }
      }
    }

    "not disable a study that does not exist" in {
      val studyId = nameGenerator.next[Study]

      val v = ask(studiesProcessor, DisableStudyCmd(None, studyId, 0L))
      .mapTo[DomainValidation[StudyEvent]]
      .futureValue
      v mustFail s"invalid study id: $studyId"
    }

    "retire a study" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val v = ask(studiesProcessor, RetireStudyCmd(None, study.id.toString, study.version))
      .mapTo[DomainValidation[StudyEvent]]
      .futureValue
      v mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (study.id.id)
        event.getRetired.version must be (Some(study.version + 1))
        studyRepository.getRetired(StudyId(event.id)) mustSucceed { repoStudy =>
          checkTimeStamps(repoStudy, study.timeAdded, DateTime.now)
        }
      }
    }

    "not update a retired study" in {
      val study = factory.createEnabledStudy
      studyRepository.put(study)
      askUpdateCommand(study) mustFailContains "is not disabled"
    }

    "unretire a study" in {
      val study = factory.createRetiredStudy
      studyRepository.put(study)

      val v = ask(studiesProcessor, UnretireStudyCmd(None, study.id.toString, study.version))
      .mapTo[DomainValidation[StudyEvent]]
      .futureValue
      v mustSucceed { event =>
        event mustBe a[StudyEvent]
        event.id must be (study.id.id)
        event.getUnretired.version must be (Some(study.version + 1))
        studyRepository.getDisabled(StudyId(event.id)) mustSucceed { repoStudy =>
          checkTimeStamps(repoStudy, study.timeAdded, DateTime.now)
        }
      }
    }

    "not unretire a disabled study" in {
      val study = factory.createDisabledStudy
      studyRepository.put(study)

      val v = ask(studiesProcessor, UnretireStudyCmd(None, study.id.toString, study.version))
      .mapTo[DomainValidation[StudyEvent]]
      .futureValue
      v mustFailContains "is not retired"
    }

    "not unretire an enabled study" in {
      val study = factory.createEnabledStudy
      studyRepository.put(study)

      val v = ask(studiesProcessor, UnretireStudyCmd(None, study.id.toString, study.version))
      .mapTo[DomainValidation[StudyEvent]]
      .futureValue
      v mustFailContains "is not retired"
    }

    "be recovered from journal" ignore {
      // val study = factory.createDisabledStudy

      // askAddCommand(study) mustSucceed { event =>
      //   event mustBe a [StudyEvent]

      //   val study2 = study.copy(
      //     id          = StudyId(event.id),
      //     name        = nameGenerator.next[Study],
      //     description = some(nameGenerator.next[Study]))

      //   askUpdateCommand(study2) mustSucceed { event =>
      //     event mustBe a[StudyEvent]
      //   }
      // }

      // // restart the processor
      // val newStudiesProcessor = system.actorOf(Props(new StudiesProcessor), "study-processor-id")
    }
  }
}
