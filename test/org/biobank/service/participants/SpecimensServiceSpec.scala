package org.biobank.services.participants
import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.centres._
import org.biobank.domain.studies._
import org.biobank.domain.participants._
import org.biobank.domain.users._
import org.biobank.services.SortString
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.TableDrivenPropertyChecks._

/**
 * Primarily these are tests that exercise the User Access aspect of SpecimenService.
 */
class SpecimensServiceSpec
    extends ProcessorTestFixture
    with ParticipantsServiceFixtures
    with ScalaFutures {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.SpecimenCommands._

  class UsersWithSpecimenAccessFixture extends UsersWithCeventAccessFixture {
    val location = factory.createLocation
    val centre = factory.createEnabledCentre.copy(studyIds = Set(enabledStudy.id),
                                                  locations = Set(location))
    val specimen = factory.createUsableSpecimen.copy(specimenDescriptionId = specimenDesc.id,
                                                     locationId = location.id)
    Set(specimen, centre).foreach(addToRepository)
    ceventSpecimenRepository.put(CeventSpecimen(cevent.id, specimen.id))
  }

  protected val nameGenerator = new NameGenerator(this.getClass)

  protected val accessItemRepository = app.injector.instanceOf[AccessItemRepository]

  protected val membershipRepository = app.injector.instanceOf[MembershipRepository]

  protected val userRepository = app.injector.instanceOf[UserRepository]

  protected val centreRepository = app.injector.instanceOf[CentreRepository]

  protected val studyRepository = app.injector.instanceOf[StudyRepository]

  protected val collectionEventTypeRepository = app.injector.instanceOf[CollectionEventTypeRepository]

  protected val participantRepository = app.injector.instanceOf[ParticipantRepository]

  protected val collectionEventRepository = app.injector.instanceOf[CollectionEventRepository]

  protected val specimenRepository = app.injector.instanceOf[SpecimenRepository]

  protected val ceventSpecimenRepository = app.injector.instanceOf[CeventSpecimenRepository]

  private val specimensService = app.injector.instanceOf[SpecimensService]

  override protected def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit = {
    entity match {
      case e: Specimen => specimenRepository.put(e)
      case e: Centre   => centreRepository.put(e)
      case e           => super.addToRepository(e)
    }
  }

  private def getSpecimenInfo(specimen: Specimen) = {
    SpecimenInfo(inventoryId           = specimen.inventoryId,
                 specimenDescriptionId = specimen.specimenDescriptionId.id,
                 timeCreated           = specimen.timeCreated,
                 locationId            = specimen.locationId.id,
                 amount                = specimen.amount)
  }

  private def getAddSpecimensCmd(userId: UserId, ceventId: CollectionEventId, specimen: Specimen) = {
    AddSpecimensCmd(sessionUserId     = userId.id,
                    collectionEventId = ceventId.id,
                    specimenData      = List(getSpecimenInfo(specimen)))
  }

  private def getRemoveSpecimenCmd(userId: UserId, ceventId: CollectionEventId, specimen: Specimen) = {
    RemoveSpecimenCmd(sessionUserId     = userId.id,
                      id                = specimen.id.id,
                      collectionEventId = ceventId.id,
                      expectedVersion   = specimen.version)
  }

  // private def updateCommandsTable(sessionUserId:   UserId,
  //                                 collectionEvent: CollectionEvent,
  //                                 specimen:        Specimen) = {
  //   val specimenInfo = SpecimenInfo(inventoryId           = specimen.inventoryId,
  //                                   specimenDescriptionId = specimen.specimenDescriptionId.id,
  //                                   timeCreated           = specimen.timeCreated,
  //                                   locationId            = specimen.locationId.id,
  //                                   amount                = specimen.amount)
  //   Table("specimen type update commands",
  //         MoveSpecimensCmd(
  //           sessionUserId     = sessionUserId.id,
  //           collectionEventId = collectionEvent.id.id,
  //           expectedVersion   = specimen.version,
  //           specimenData      = Set(specimenInfo)
  //         )
  //   )
  // }

  override def beforeEach() {
    super.beforeEach()
    collectionEventTypeRepository.removeAll
    participantRepository.removeAll
    collectionEventRepository.removeAll
    specimenRepository.removeAll
  }

  describe("SpecimenService") {

    describe("when getting a specimen") {

      it("users can access") {
        val f = new UsersWithSpecimenAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          specimensService.get(user.id, f.specimen.id)
            .mustSucceed { specimen =>
              specimen.id must be (f.specimen.id)
            }
        }
      }

      it("users cannot access") {
        val f = new UsersWithSpecimenAccessFixture
        info("no membership user")
        specimensService.get(f.noMembershipUser.id, f.specimen.id)
          .mustFail("Unauthorized")

        info("no permission user")
        specimensService.get(f.nonStudyPermissionUser.id, f.specimen.id)
          .mustFail("Unauthorized")

      }

    }

    describe("when getting a specimen by inventory ID") {

      it("users can access") {
        val f = new UsersWithSpecimenAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          specimensService.getByInventoryId(user.id, f.specimen.inventoryId)
            .mustSucceed { specimen =>
              specimen.id must be (f.specimen.id)
            }
        }
      }

      it("users cannot access") {
        val f = new UsersWithSpecimenAccessFixture
        info("no membership user")
        specimensService.getByInventoryId(f.noMembershipUser.id, f.specimen.inventoryId)
          .mustFail("Unauthorized")

        info("no permission user")
        specimensService.getByInventoryId(f.nonStudyPermissionUser.id, f.specimen.inventoryId)
          .mustFail("Unauthorized")

      }

    }

    describe("when listing specimens") {

      it("users can access") {
        val f = new UsersWithSpecimenAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          specimensService.list(user.id, f.cevent.id, new SortString(""))
            .mustSucceed { result =>
              result must have size 1
            }
        }
      }

      it("users cannot access") {
        val f = new UsersWithSpecimenAccessFixture
        info("no membership user")
        specimensService.list(f.noMembershipUser.id, f.cevent.id, new SortString(""))
          .mustFail("Unauthorized")

        info("no permission user")
        specimensService.list(f.nonStudyPermissionUser.id, f.cevent.id, new SortString(""))
          .mustFail("Unauthorized")
      }

    }

    describe("when adding a specimen") {

      it("users can access") {
        val f = new UsersWithSpecimenAccessFixture
        forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
          val cmd = getAddSpecimensCmd(user.id, f.cevent.id, f.specimen)
          specimenRepository.removeAll
          specimensService.processCommand(cmd).futureValue mustSucceed { reply =>
            reply.participantId must be (f.participant.id)
          }
        }
      }

      it("users cannot access") {
        val f = new UsersWithSpecimenAccessFixture
        forAll (f.usersCannotAddOrUpdateTable) { (user, label) =>
          val cmd = getAddSpecimensCmd(user.id, f.cevent.id, f.specimen)
          specimenRepository.removeAll
          specimensService.processCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }

    }

    //   describe("when updating a specimen") {

    //     it("users with access") {
    //       val f = new UsersWithSpecimenAccessFixture
    //       forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
    //         info(label)
    //         forAll(updateCommandsTable(user.id, f.specimen, f.specimenAnnotation)) { cmd =>
    //           val specimen = cmd match {
    //               case _: AddSpecimenAnnotationCmd =>
    //                 f.specimen.copy(annotations = Set.empty[Annotation])
    //               case _ =>
    //                 f.specimen
    //             }

    //           specimenRepository.put(specimen) // restore it to it's previous state
    //           specimensService.processCommand(cmd).futureValue mustSucceed { reply =>
    //             reply.id must be (f.specimen.id)
    //           }
    //         }
    //       }
    //     }

    //     it("users without access") {
    //       val f = new UsersWithSpecimenAccessFixture
    //       forAll (f.usersCannotAddOrUpdateTable) { (user, label) =>
    //         forAll(updateCommandsTable(user.id, f.specimen, f.specimenAnnotation)) { cmd =>
    //           specimensService.processCommand(cmd).futureValue mustFail "Unauthorized"
    //         }
    //       }
    //     }

    //   }

    describe("when removing a specimen") {

      it("users with access") {
        val f = new UsersWithSpecimenAccessFixture
        forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
          info(label)
          val cmd = getRemoveSpecimenCmd(user.id, f.cevent.id, f.specimen)
          specimenRepository.put(f.specimen) // restore it to it's previous state
          ceventSpecimenRepository.put(CeventSpecimen(f.cevent.id, f.specimen.id))
          specimensService.processRemoveCommand(cmd).futureValue mustSucceed { reply =>
            reply must be (true)
          }
        }
      }

      it("users without access") {
        val f = new UsersWithSpecimenAccessFixture
        forAll (f.usersCannotAddOrUpdateTable) { (user, label) =>
          info(label)
          val cmd = getRemoveSpecimenCmd(user.id, f.cevent.id, f.specimen)
          specimensService.processRemoveCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }
    }

  }

}
