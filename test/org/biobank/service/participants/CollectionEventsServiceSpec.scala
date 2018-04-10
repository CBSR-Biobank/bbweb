package org.biobank.services.participants

import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.annotations._
import org.biobank.domain.studies._
import org.biobank.domain.participants._
import org.biobank.domain.users._
import org.biobank.services.{FilterString, SortString}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.TableDrivenPropertyChecks._

/**
 * Primarily these are tests that exercise the User Access aspect of CollectionEventService.
 */
class CollectionEventServiceSpec
    extends ProcessorTestFixture
    with ParticipantsServiceFixtures
    with ScalaFutures {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.commands.CollectionEventCommands._

  protected val nameGenerator = new NameGenerator(this.getClass)

  protected val accessItemRepository = app.injector.instanceOf[AccessItemRepository]

  protected val membershipRepository = app.injector.instanceOf[MembershipRepository]

  protected val userRepository = app.injector.instanceOf[UserRepository]

  protected val studyRepository = app.injector.instanceOf[StudyRepository]

  protected val collectionEventTypeRepository = app.injector.instanceOf[CollectionEventTypeRepository]

  protected val collectionEventRepository = app.injector.instanceOf[CollectionEventRepository]

  protected val participantRepository = app.injector.instanceOf[ParticipantRepository]

  private val ceventsService = app.injector.instanceOf[CollectionEventsService]

  private def updateCommandsTable(sessionUserId: UserId,
                                  cevent:        CollectionEvent,
                                  annotation:    Annotation) = {
    Table("collection event type update commands",
          UpdateCollectionEventVisitNumberCmd(
            sessionUserId   = sessionUserId.id,
            id              = cevent.id.id,
            expectedVersion = cevent.version,
            visitNumber     = cevent.visitNumber
          ),
          UpdateCollectionEventTimeCompletedCmd(
            sessionUserId   = sessionUserId.id,
            id              = cevent.id.id,
            expectedVersion = cevent.version,
            timeCompleted   = cevent.timeCompleted
          ),
          CollectionEventUpdateAnnotationCmd(
            sessionUserId    = sessionUserId.id,
            id               = cevent.id.id,
            expectedVersion  = cevent.version,
            annotationTypeId = annotation.annotationTypeId.id,
            stringValue      = annotation.stringValue,
            numberValue      = annotation.numberValue,
            selectedValues   = annotation.selectedValues
          ),
          RemoveCollectionEventAnnotationCmd(
            sessionUserId    = sessionUserId.id,
            id               = cevent.id.id,
            expectedVersion  = cevent.version,
            annotationTypeId = annotation.annotationTypeId.id
          )
    )
  }

  override def beforeEach() {
    super.beforeEach()
    collectionEventTypeRepository.removeAll
    participantRepository.removeAll
    collectionEventRepository.removeAll
  }

  describe("CollectionEventService") {

    describe("when getting a collection event") {

      it("users can access") {
        val f = new UsersWithCeventAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          ceventsService.get(user.id, f.cevent.id)
            .mustSucceed { result =>
              result.id must be (f.cevent.id.id)
            }
        }
      }

      it("users cannot access") {
        val f = new UsersWithCeventAccessFixture
        info("no membership user")
        ceventsService.get(f.noMembershipUser.id, f.cevent.id)
          .mustFail("Unauthorized")

        info("no permission user")
        ceventsService.get(f.nonStudyPermissionUser.id, f.cevent.id)
          .mustFail("Unauthorized")

      }

    }

    describe("when getting a collection event by visit number") {

      it("users can access") {
        val f = new UsersWithCeventAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          ceventsService.getByVisitNumber(user.id, f.participant.id, f.cevent.visitNumber)
            .mustSucceed { result =>
              result.id must be (f.cevent.id.id)
            }
        }
      }

      it("users cannot access") {
        val f = new UsersWithCeventAccessFixture
        info("no membership user")
        ceventsService.getByVisitNumber(f.noMembershipUser.id, f.participant.id, f.cevent.visitNumber)
          .mustFail("Unauthorized")

        info("no permission user")
        ceventsService.getByVisitNumber(f.nonStudyPermissionUser.id, f.participant.id, f.cevent.visitNumber)
          .mustFail("Unauthorized")

      }

    }

    describe("when listing all collection events") {

      it("users can access") {
        val f = new UsersWithCeventAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          ceventsService.list(user.id, f.participant.id, new FilterString(""), new SortString(""))
            .mustSucceed { result =>
              result must have size 1
            }
        }
      }

      it("users cannot access") {
        val f = new UsersWithCeventAccessFixture
        info("no membership user")
        ceventsService.list(f.noMembershipUser.id,
                            f.participant.id,
                            new FilterString(""),
                            new SortString(""))
          .mustFail("Unauthorized")

        info("no permission user")
        ceventsService.list(f.nonStudyPermissionUser.id,
                            f.participant.id,
                            new FilterString(""),
                            new SortString(""))
          .mustFail("Unauthorized")
      }

    }

    describe("when adding a collection event type") {

      it("users can access") {
        val f = new UsersWithCeventAccessFixture
        forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
          val cmd = AddCollectionEventCmd(sessionUserId         = user.id.id,
                                          participantId         = f.participant.id.id,
                                          collectionEventTypeId = f.ceventType.id.id,
                                          timeCompleted         = f.cevent.timeCompleted,
                                          visitNumber           = f.cevent.visitNumber,
                                          annotations           = f.cevent.annotations.toList)

          collectionEventRepository.removeAll
          ceventsService.processCommand(cmd).futureValue mustSucceed { reply =>
            reply.participantId must be (f.participant.id.id)
          }
        }
      }

      it("users cannot access") {
        val f = new UsersWithCeventAccessFixture
        forAll (f.usersCannotAddOrUpdateTable) { (user, label) =>
          val cmd = AddCollectionEventCmd(sessionUserId         = user.id.id,
                                          participantId         = f.participant.id.id,
                                          collectionEventTypeId = f.ceventType.id.id,
                                          timeCompleted         = f.cevent.timeCompleted,
                                          visitNumber           = f.cevent.visitNumber,
                                          annotations           = f.cevent.annotations.toList)
          collectionEventRepository.removeAll
          ceventsService.processCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }

    }

    describe("when updating a collection event type") {

      it("users with access") {
        val f = new UsersWithCeventAccessFixture
        forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
          info(label)
          forAll(updateCommandsTable(user.id, f.cevent, f.ceventAnnotation)) { cmd =>
            val cevent = cmd match {
                case _: CollectionEventUpdateAnnotationCmd =>
                  f.cevent.copy(annotations = Set.empty[Annotation])
                case _ =>
                  f.cevent
              }

            collectionEventRepository.put(cevent) // restore it to it's previous state
            ceventsService.processCommand(cmd).futureValue mustSucceed { reply =>
              reply.id must be (f.cevent.id.id)
            }
          }
        }
      }

      it("users without access") {
        val f = new UsersWithCeventAccessFixture
        forAll (f.usersCannotAddOrUpdateTable) { (user, label) =>
          forAll(updateCommandsTable(user.id, f.cevent, f.ceventAnnotation)) { cmd =>
            ceventsService.processCommand(cmd).futureValue mustFail "Unauthorized"
          }
        }
      }

    }

    describe("when removing a collection event type") {

      it("users with access") {
        val f = new UsersWithCeventAccessFixture
        forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
          info(label)
          val cmd = RemoveCollectionEventCmd(sessionUserId   = user.id.id,
                                             id              = f.cevent.id.id,
                                             participantId   = f.cevent.participantId.id,
                                             expectedVersion = f.cevent.version)

          collectionEventRepository.put(f.cevent) // restore it to it's previous state
          ceventsService.processRemoveCommand(cmd).futureValue mustSucceed { reply =>
            reply must be (true)
          }
        }
      }

      it("users without access") {
        val f = new UsersWithCeventAccessFixture
        forAll (f.usersCannotAddOrUpdateTable) { (user, label) =>
          info(label)
          val cmd = RemoveCollectionEventCmd(sessionUserId   = user.id.id,
                                             id              = f.cevent.id.id,
                                             participantId   = f.cevent.participantId.id,
                                             expectedVersion = f.cevent.version)

          ceventsService.processRemoveCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }
    }

  }

}
