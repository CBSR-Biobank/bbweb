package org.biobank.service.participants

import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.study._
import org.biobank.domain.participants._
import org.biobank.domain.user._
import org.biobank.service.studies._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.TableDrivenPropertyChecks._

/**
 * Primarily these are tests that exercise the User Access aspect of StudiesService.
 */
class ParticipantsServiceSpec
    extends ProcessorTestFixture
    with StudiesServiceFixtures
    with ScalaFutures {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.ParticipantCommands._

  class ParticipantFixure extends UsersWithStudyAccessFixture {
    val annotationType = factory.createAnnotationType
    val annotation     = factory.createAnnotationWithValues(annotationType)
    val enabledStudy   = factory.createEnabledStudy.copy(annotationTypes = Set(annotationType))
    val specimenDesc = factory.createCollectionSpecimenDescription
    val ceventType     = factory.createCollectionEventType.copy(studyId              = enabledStudy.id,
                                                                specimenDescriptions = Set(specimenDesc))
    val participant    = factory.createParticipant.copy(studyId     = enabledStudy.id,
                                                        annotations = Set(annotation))

    Set(enabledStudy, participant, ceventType).foreach(addToRepository)
    addToRepository(studyOnlyMembership.copy(
                      studyInfo = studyOnlyMembership.studyInfo.copy(studyIds = Set(enabledStudy.id))))

  }

  protected val nameGenerator = new NameGenerator(this.getClass)

  protected val accessItemRepository = app.injector.instanceOf[AccessItemRepository]

  protected val membershipRepository = app.injector.instanceOf[MembershipRepository]

  protected val userRepository = app.injector.instanceOf[UserRepository]

  protected val studyRepository = app.injector.instanceOf[StudyRepository]

  protected val collectionEventTypeRepository = app.injector.instanceOf[CollectionEventTypeRepository]

  protected val participantRepository = app.injector.instanceOf[ParticipantRepository]

  private val participantsService = app.injector.instanceOf[ParticipantsService]

  override protected def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit = {
    entity match {
      case e: Participant => participantRepository.put(e)
      case e => super.addToRepository(e)
    }
  }

  private def commandsTable(sessionUserId: UserId, participant: Participant, annotation: Annotation) = {
    Table("participant commands",
          UpdateParticipantUniqueIdCmd(
            sessionUserId   = sessionUserId.id,
            id              = participant.id.id,
            expectedVersion = participant.version,
            uniqueId        = participant.uniqueId
          ),
          ParticipantAddAnnotationCmd(
            sessionUserId    = sessionUserId.id,
            id               = participant.id.id,
            expectedVersion  = participant.version,
            annotationTypeId = annotation.annotationTypeId.id,
            stringValue      = annotation.stringValue,
            numberValue      = annotation.numberValue,
            selectedValues   = annotation.selectedValues
          ),
          ParticipantRemoveAnnotationCmd(
            sessionUserId    = sessionUserId.id,
            id               = participant.id.id,
            expectedVersion  = participant.version,
            annotationTypeId = annotation.annotationTypeId.id
          )
    )
  }

  override def beforeEach() {
    super.beforeEach()
    collectionEventTypeRepository.removeAll
    participantRepository.removeAll
  }

  describe("ParticipantsService") {

    describe("when getting a participant") {

      it("users can access") {
        val f = new ParticipantFixure
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          participantsService.get(user.id, f.enabledStudy.id, f.participant.id)
            .mustSucceed { result =>
              result.id must be (f.participant.id)
            }
        }
      }

      it("users cannot access") {
        val f = new ParticipantFixure
        forAll (f.usersCannotReadTable) { (user, label) =>
          info(label)
          participantsService.get(user.id, f.enabledStudy.id, f.participant.id) mustFail "Unauthorized"
        }
      }

    }

    describe("when getting a participant by unique ID") {

      it("users can access") {
        val f = new ParticipantFixure
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          participantsService.getByUniqueId(user.id, f.enabledStudy.id, f.participant.uniqueId)
            .mustSucceed { result =>
              result.id must be (f.participant.id)
            }
        }
      }

      it("users cannot access") {
        val f = new ParticipantFixure
        forAll (f.usersCannotReadTable) { (user, label) =>
          info(label)
          participantsService.getByUniqueId(user.id, f.enabledStudy.id, f.participant.uniqueId)
            .mustFail("Unauthorized")
        }
      }

    }

    describe("when adding a participant") {

      it("users can access") {
        val f = new ParticipantFixure
        forAll (f.usersCanUpdateTable) { (user, label) =>
          participantRepository.removeAll
          val cmd = AddParticipantCmd(
              sessionUserId   = user.id.id,
              studyId         = f.participant.studyId.id,
              uniqueId        = f.participant.uniqueId,
              annotations     = List.empty[Annotation])
          participantsService.processCommand(cmd).futureValue mustSucceed { result =>
            result.uniqueId must be (f.participant.uniqueId)
          }
        }
      }

      it("users cannot access") {
        val f = new ParticipantFixure
        forAll (f.usersCannotUpdateTable) { (user, label) =>
          info(label)
          val cmd = AddParticipantCmd(
              sessionUserId   = user.id.id,
              studyId         = f.participant.studyId.id,
              uniqueId        = f.participant.uniqueId,
              annotations     = List.empty[Annotation])
          participantsService.processCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }

    }

    describe("when updating participants") {

      it("users can access") {
        val f = new ParticipantFixure
        forAll (f.usersCanUpdateTable) { (user, label) =>
          info(label)
          forAll(commandsTable(user.id, f.participant, f.annotation)) { cmd =>
            val participant = cmd match {
                case c: ParticipantAddAnnotationCmd => f.participant.copy(annotations = Set.empty[Annotation])
                case _ => f.participant
              }
            participantRepository.put(participant)
            participantsService.processCommand(cmd).futureValue mustSucceed { result =>
              result.id must be (f.participant.id)
            }
          }
        }
      }

      it("users cannot access") {
        val f = new ParticipantFixure
        forAll (f.usersCannotUpdateTable) { (user, label) =>
          info(label)
          forAll(commandsTable(user.id, f.participant, f.annotation)) { cmd =>
            participantsService.processCommand(cmd).futureValue mustFail "Unauthorized"
          }
        }
      }

    }

  }
}
