package org.biobank.services.centres

import java.time.OffsetDateTime
import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.centres._
import org.biobank.domain.studies._
import org.biobank.domain.participants._
import org.biobank.domain.users._
import org.biobank.services.FilterString
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.TableDrivenPropertyChecks._

/**
 * Primarily these are tests that exercise the User Access aspect of SpecimenService.
 */
class ShipmentsServiceSpec
    extends CentresServiceFixtures
    with ShipmentSpecFixtures
    with ScalaFutures {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.command.ShipmentCommands._
  import org.biobank.infrastructure.command.ShipmentSpecimenCommands._

  class UsersWithShipmentAccessFixture {
    val fromLocation = factory.createLocation
    val toLocation   = factory.createLocation
    val fromCentre   = factory.createEnabledCentre.copy(locations = Set(fromLocation))
    val toCentre     = factory.createEnabledCentre.copy(locations = Set(toLocation))
    val shipment     = factory.createShipment(fromCentre, toCentre)

    val allCentresAdminUser        = factory.createActiveUser
    val centreOnlyShippingAdminUser = factory.createActiveUser
    val shippingUser                = factory.createActiveUser
    val noMembershipUser            = factory.createActiveUser
    val noShippingPermissionUser    = factory.createActiveUser

    val allCentresMembership = factory.createMembership.copy(
        userIds = Set(allCentresAdminUser.id),
        centreData = MembershipEntitySet(true, Set.empty[CentreId]))

    val centreOnlyMembership = factory.createMembership.copy(
        userIds = Set(centreOnlyShippingAdminUser.id, shippingUser.id),
        centreData = MembershipEntitySet(false, Set(fromCentre.id, toCentre.id)))

    val noCentresMembership = factory.createMembership.copy(
        userIds = Set(noMembershipUser.id, noShippingPermissionUser.id),
        centreData = MembershipEntitySet(false, Set.empty[CentreId]))

    val usersCanReadTable = Table(("users with read access", "label"),
                                  (allCentresAdminUser,         "all centres admin user"),
                                  (centreOnlyShippingAdminUser, "centre only shipping admin user"),
                                  (shippingUser,                "non-admin shipping user"))

    val usersCanAddOrUpdateTable = Table(("users with update access", "label"),
                                         (allCentresAdminUser,         "all centres admin user"),
                                         (centreOnlyShippingAdminUser, "centre only shipping admin user"),
                                         (shippingUser,                "non-admin centre user"))

    val usersCannotAddOrUpdateTable = Table(("users with update access", "label"),
                                            (noMembershipUser,         "no memberships user"),
                                            (noShippingPermissionUser, "no shipping permission user"))

    val usersWithoutAccess = Table(("users that can remove specimens", "label"),
                                   (noMembershipUser,         "no memberships user"),
                                   (noShippingPermissionUser, "no shipping permission user"))


    Set(fromCentre,
        toCentre,
        shipment,
        allCentresAdminUser,
        centreOnlyShippingAdminUser,
        shippingUser,
        noMembershipUser,
        noShippingPermissionUser,
        allCentresMembership,
        centreOnlyMembership,
        noCentresMembership
    ).foreach(addToRepository)

    addUserToRole(allCentresAdminUser, RoleId.ShippingAdministrator)
    addUserToRole(centreOnlyShippingAdminUser, RoleId.ShippingAdministrator)
    addUserToRole(shippingUser, RoleId.ShippingUser)
    addUserToRole(noMembershipUser, RoleId.ShippingUser)
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

  protected val shipmentRepository = app.injector.instanceOf[ShipmentRepository]

  protected val shipmentSpecimenRepository = app.injector.instanceOf[ShipmentSpecimenRepository]

  private val shipmentsService = app.injector.instanceOf[ShipmentsService]

  override protected def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit = {
    entity match {
      case e: Shipment            => shipmentRepository.put(e)
      case e: ShipmentSpecimen    => shipmentSpecimenRepository.put(e)
      case e: Specimen            => specimenRepository.put(e)
      case e: CollectionEventType => collectionEventTypeRepository.put(e)
      case e: Participant         => participantRepository.put(e)
      case e: CollectionEvent     => collectionEventRepository.put(e)
      case e                      => super.addToRepository(e)
    }
  }

  private def getAddShipmentCmd(userId: UserId, shipment: CreatedShipment) = {
    AddShipmentCmd(sessionUserId  = userId.id,
                   courierName    = shipment.courierName,
                   trackingNumber = shipment.trackingNumber,
                   fromLocationId = shipment.fromLocationId.id,
                   toLocationId   = shipment.toLocationId.id)
  }

  private def getRemoveShipmentCmd(userId: UserId, shipment: CreatedShipment) = {
    ShipmentRemoveCmd(sessionUserId   = userId.id,
                      id              = shipment.id.id,
                      expectedVersion = shipment.version)
  }

  def shipmentSpecimenFixture() = {
    val f = new UsersWithShipmentAccessFixture
    val ceventFixture = new CollectionEventFixture
    val specimen = factory.createUsableSpecimen.copy(originLocationId = f.fromLocation.id,
                                                     locationId       = f.fromLocation.id)
    val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                               specimenId = specimen.id)

    Set(f.allCentresMembership, f.centreOnlyMembership).foreach { membership =>
      addToRepository(membership.copy(
                        studyData = MembershipEntitySet(false, Set(ceventFixture.study.id))))
    }

    ceventSpecimenRepository.put(CeventSpecimen(ceventFixture.cevent.id, specimen.id))
    Set(ceventFixture.study,
        ceventFixture.ceventType,
        ceventFixture.participant,
        ceventFixture.cevent,
        specimen,
        shipmentSpecimen).foreach(addToRepository)

    (f, specimen, shipmentSpecimen)
  }

  private def updateCommandsTable(sessionUserId:   UserId, shipment: Shipment) = {
    Table("shipment update commands",
          UpdateShipmentCourierNameCmd(
            sessionUserId     = sessionUserId.id,
            id                = shipment.id.id,
            expectedVersion   = shipment.version,
            courierName       = shipment.courierName
          ),
          UpdateShipmentTrackingNumberCmd(
            sessionUserId     = sessionUserId.id,
            id                = shipment.id.id,
            expectedVersion   = shipment.version,
            trackingNumber    = shipment.trackingNumber
          ),
          UpdateShipmentFromLocationCmd(
            sessionUserId     = sessionUserId.id,
            id                = shipment.id.id,
            expectedVersion   = shipment.version,
            locationId        = shipment.fromLocationId.id
          ),
          UpdateShipmentToLocationCmd(
            sessionUserId     = sessionUserId.id,
            id                = shipment.id.id,
            expectedVersion   = shipment.version,
            locationId        = shipment.toLocationId.id
          )
    )
  }

  private def changeStateCommandsTable(sessionUserId: UserId, shipment: Shipment) = {
    val shipments = Map(
        ( Shipment.createdState,   shipment),
        ( Shipment.packedState,    makePackedShipment(shipment)),
        ( Shipment.sentState,      makeSentShipment(shipment)),
        ( Shipment.receivedState,  makeReceivedShipment(shipment)),
        ( Shipment.unpackedState,  makeUnpackedShipment(shipment)),
        ( Shipment.lostState,      makeLostShipment(shipment)))

    Table(("shipment", "command"), (
            shipments(Shipment.packedState),
            CreatedShipmentCmd(sessionUserId     = sessionUserId.id,
                               id                = shipments(Shipment.packedState).id.id,
                               expectedVersion   = shipments(Shipment.packedState).version)
          ),(
            shipments(Shipment.createdState),
            PackShipmentCmd(sessionUserId     = sessionUserId.id,
                            id                = shipments(Shipment.createdState).id.id,
                            expectedVersion   = shipments(Shipment.createdState).version,
                            datetime          = OffsetDateTime.now)
          ),(
            shipments(Shipment.packedState),
            SendShipmentCmd(sessionUserId     = sessionUserId.id,
                            id                = shipments(Shipment.packedState).id.id,
                            expectedVersion   = shipments(Shipment.packedState).version,
                            datetime          = OffsetDateTime.now)
          ),(
            shipments(Shipment.sentState),
            ReceiveShipmentCmd(sessionUserId     = sessionUserId.id,
                               id                = shipments(Shipment.sentState).id.id,
                               expectedVersion   = shipments(Shipment.sentState).version,
                               datetime          = OffsetDateTime.now)
          ),(
            shipments(Shipment.receivedState),
            UnpackShipmentCmd(sessionUserId     = sessionUserId.id,
                              id                = shipments(Shipment.receivedState).id.id,
                              expectedVersion   = shipments(Shipment.receivedState).version,
                              datetime          = OffsetDateTime.now)
          ),(
            shipments(Shipment.unpackedState),
            CompleteShipmentCmd(sessionUserId     = sessionUserId.id,
                                id                = shipments(Shipment.unpackedState).id.id,
                                expectedVersion   = shipments(Shipment.unpackedState).version,
                                datetime          = OffsetDateTime.now)
          ),(
            shipments(Shipment.sentState),
            LostShipmentCmd(sessionUserId     = sessionUserId.id,
                            id                = shipments(Shipment.sentState).id.id,
                            expectedVersion   = shipments(Shipment.sentState).version)
          ),(
            shipments(Shipment.createdState),
            ShipmentSkipStateToSentCmd(sessionUserId     = sessionUserId.id,
                                       id                = shipments(Shipment.createdState).id.id,
                                       expectedVersion   = shipments(Shipment.createdState).version,
                                       timePacked        = OffsetDateTime.now,
                                       timeSent          = OffsetDateTime.now)
          ),(
            shipments(Shipment.sentState),
            ShipmentSkipStateToUnpackedCmd(sessionUserId     = sessionUserId.id,
                                           id                = shipments(Shipment.sentState).id.id,
                                           expectedVersion   = shipments(Shipment.sentState).version,
                                           timeReceived      = OffsetDateTime.now,
                                           timeUnpacked      = OffsetDateTime.now)
          ))
  }

  private def shipmentSpecimenCommandsTable(sessionUserId:    UserId,
                                            shipment:         Shipment,
                                            specimen:         Specimen,
                                            shipmentSpecimen: ShipmentSpecimen) = {
    val shipments = Map(
        ( Shipment.createdState,   shipment),
        ( Shipment.unpackedState,  makeUnpackedShipment(shipment)))

    Table(("shipment", "shipment specimen command"),(
            shipments(Shipment.createdState),
            ShipmentAddSpecimensCmd(
              sessionUserId         = sessionUserId.id,
              shipmentId            = shipmentSpecimen.shipmentId.id,
              shipmentContainerId   = None,
              specimenInventoryIds  = List(specimen.inventoryId))
          ),(
            shipments(Shipment.createdState),
            ShipmentSpecimenRemoveCmd(
              sessionUserId         = sessionUserId.id,
              shipmentId            = shipmentSpecimen.shipmentId.id,
              expectedVersion       = shipmentSpecimen.version,
              shipmentSpecimenId    = shipmentSpecimen.id.id)
            // ),(
            // this command has not been implemented yet
            //
            // ShipmentSpecimenUpdateContainerCmd(
            //   sessionUserId         = sessionUserId.id,
            //   shipmentId            = shipment.id.id,
            //   shipmentContainerId   = None,
            //   specimenInventoryIds  = List(specimen.inventoryId)
            // ),
          ),(
            shipments(Shipment.unpackedState),
            ShipmentSpecimensPresentCmd(
              sessionUserId         = sessionUserId.id,
              shipmentId            = shipmentSpecimen.shipmentId.id,
              specimenInventoryIds  = List(specimen.inventoryId))
          ),(
            shipments(Shipment.unpackedState),
            ShipmentSpecimensReceiveCmd(
              sessionUserId         = sessionUserId.id,
              shipmentId            = shipmentSpecimen.shipmentId.id,
              specimenInventoryIds  = List(specimen.inventoryId))
          ),(
            shipments(Shipment.unpackedState),
            ShipmentSpecimenMissingCmd(
              sessionUserId         = sessionUserId.id,
              shipmentId            = shipmentSpecimen.shipmentId.id,
              specimenInventoryIds  = List(specimen.inventoryId))
          ),(
            shipments(Shipment.unpackedState),
            ShipmentSpecimenExtraCmd(
              sessionUserId         = sessionUserId.id,
              shipmentId            = shipmentSpecimen.shipmentId.id,
              specimenInventoryIds  = List(specimen.inventoryId)
            )
          ))
  }

  override def beforeEach() {
    super.beforeEach()
    collectionEventTypeRepository.removeAll
    participantRepository.removeAll
    collectionEventRepository.removeAll
    specimenRepository.removeAll
    centreRepository.removeAll
    shipmentRepository.removeAll
    shipmentSpecimenRepository.removeAll
  }

  describe("SpecimenService") {

    describe("when getting a shipment") {

      it("users can access") {
        val f = new UsersWithShipmentAccessFixture

        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          shipmentsService.getShipment(user.id, f.shipment.id)
            .mustSucceed { specimen =>
              specimen.id must be (f.shipment.id)
            }
        }
      }

      it("users cannot access") {
        val f = new UsersWithShipmentAccessFixture
        info("no membership user")
        shipmentsService.getShipment(f.noMembershipUser.id, f.shipment.id)
          .mustFail("Unauthorized")

        info("no permission user")
        shipmentsService.getShipment(f.noShippingPermissionUser.id, f.shipment.id)
          .mustFail("Unauthorized")

      }

    }

    describe("when listing shipments") {

      it("users can access") {
        val f = new UsersWithShipmentAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          shipmentsService.getShipments(user.id, new FilterString(""))
            .mustSucceed { result =>
              result must have size 1
            }
        }
      }

      it("users cannot access") {
        val f = new UsersWithShipmentAccessFixture
        info("no membership user")
        shipmentsService.getShipments(f.noMembershipUser.id, new FilterString(""))
          .mustSucceed { result =>
            result must have size 0
          }

        info("no permission user")
        shipmentsService.getShipments(f.noShippingPermissionUser.id, new FilterString(""))
          .mustFail("Unauthorized")
      }

    }

    describe("when getting a shipment specimen") {

      it("users can access") {
        val (f, _, shipmentSpecimen) = shipmentSpecimenFixture
        forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
          info(label)
          shipmentsService.getShipmentSpecimen(user.id, f.shipment.id, shipmentSpecimen.id)
            .mustSucceed { reply =>
              reply.id must be (shipmentSpecimen.id)
            }
        }
      }

      it("users cannot access") {
        val (f, _, shipmentSpecimen) = shipmentSpecimenFixture
        forAll (f.usersWithoutAccess) { (user, label) =>
          info("label")
          shipmentsService.getShipmentSpecimen(f.noMembershipUser.id, f.shipment.id, shipmentSpecimen.id)
            .mustFail("Unauthorized")
        }

      }

    }

    describe("when listing shipment specimens") {

      it("users can access") {
        val (f, _, _) = shipmentSpecimenFixture
        forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
          info(label)
          shipmentsService.getShipmentSpecimens(user.id,
                                                f.shipment.id,
                                                new FilterString(""))
            .mustSucceed { reply =>
              reply must have size 1
            }
        }
      }

      it("users cannot access") {
        val (f, _, _) = shipmentSpecimenFixture
        info("no membership user")
        shipmentsService.getShipmentSpecimens(f.noMembershipUser.id,
                                              f.shipment.id,
                                              new FilterString(""))
          .mustFail("Unauthorized")

        info("no permission user")
        shipmentsService.getShipmentSpecimens(f.noShippingPermissionUser.id,
                                              f.shipment.id,
                                              new FilterString(""))
          .mustFail("Unauthorized")
      }

    }

    describe("when determining if a specimen can be added to a shipment") {

      it("users can access") {
        val (f, specimen, shipmentSpecimen) = shipmentSpecimenFixture
        forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
          info(label)
          shipmentSpecimenRepository.remove(shipmentSpecimen)
          shipmentsService.shipmentCanAddSpecimen(user.id,
                                                  f.shipment.id,
                                                  specimen.inventoryId)
            .mustSucceed { reply =>
              reply.inventoryId must be (specimen.inventoryId)
            }
        }
      }

      it("users cannot access") {
        val (f, specimen, shipmentSpecimen) = shipmentSpecimenFixture
        shipmentSpecimenRepository.remove(shipmentSpecimen)

        forAll (f.usersWithoutAccess) { (user, label) =>
          info(label)
          shipmentsService.shipmentCanAddSpecimen(user.id,
                                                  f.shipment.id,
                                                  specimen.inventoryId)
            .mustFail("Unauthorized")
        }
      }

    }

    describe("when adding a shipment") {

      it("users can access") {
        val f = new UsersWithShipmentAccessFixture
        forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
          val cmd = getAddShipmentCmd(user.id, f.shipment)
          shipmentRepository.removeAll
          shipmentsService.processCommand(cmd).futureValue mustSucceed { reply =>
            reply.courierName must be (f.shipment.courierName)
          }
        }
      }

      it("users cannot access") {
        val f = new UsersWithShipmentAccessFixture
        forAll (f.usersCannotAddOrUpdateTable) { (user, label) =>
          val cmd = getAddShipmentCmd(user.id, f.shipment)
          shipmentRepository.removeAll
          shipmentsService.processCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }

    }

    describe("when updating a shipment") {

      it("users with access") {
        val f = new UsersWithShipmentAccessFixture
        forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
          info(label)
          forAll(updateCommandsTable(user.id, f.shipment)) { cmd =>
            shipmentRepository.put(f.shipment) // restore it to it's previous state
            shipmentsService.processCommand(cmd).futureValue mustSucceed { reply =>
              reply.id must be (f.shipment.id)
            }
          }
        }
      }

      it("users without access") {
        val f = new UsersWithShipmentAccessFixture
        forAll (f.usersCannotAddOrUpdateTable) { (user, label) =>
          forAll(updateCommandsTable(user.id, f.shipment)) { cmd =>
            shipmentsService.processCommand(cmd).futureValue mustFail "Unauthorized"
          }
        }
      }

    }

    describe("when changing state on a shipment") {

      it("users with access") {
        val (f, _, shipmentSpecimen) = shipmentSpecimenFixture
        forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
          info(label)
          forAll(changeStateCommandsTable(user.id, f.shipment)) { (shipment, cmd) =>
            shipment match {
              case s: UnpackedShipment =>
                addToRepository(shipmentSpecimen.copy(state = ShipmentItemState.Received))
              case _ =>
                addToRepository(shipmentSpecimen.copy(state = ShipmentItemState.Present))
            }

            addToRepository(shipment)
            shipmentsService.processCommand(cmd).futureValue mustSucceed { reply =>
              reply.id must be (shipment.id)
            }
          }
        }
      }

      it("users without access") {
        val (f, _, _) = shipmentSpecimenFixture
        forAll (f.usersCannotAddOrUpdateTable) { (user, label) =>
          info(label)
          forAll(changeStateCommandsTable(user.id, f.shipment)) { (shipment, cmd) =>
            addToRepository(f.shipment)
            shipmentsService.processCommand(cmd).futureValue mustFail "Unauthorized"
          }
        }
      }

    }

    describe("when updating shipment specimens") {

      it("users with access") {
        val (f, specimen, shipmentSpecimen) = shipmentSpecimenFixture
        forAll (f.usersCanAddOrUpdateTable) { (user, label) =>
          info(label)
          forAll(shipmentSpecimenCommandsTable(user.id,
                                               f.shipment,
                                               specimen,
                                               shipmentSpecimen)) { (shipment, cmd) =>
            // set up pre-condition
            addToRepository(shipment)
            shipmentSpecimenRepository.removeAll
            cmd match {
              case _: ShipmentAddSpecimensCmd | _: ShipmentSpecimenExtraCmd =>
              case c: ShipmentSpecimensPresentCmd =>
                shipmentSpecimenRepository.put(shipmentSpecimen.copy(state = ShipmentItemState.Received))
              case c =>
                shipmentSpecimenRepository.put(shipmentSpecimen)
            }

            val v = shipmentsService.processShipmentSpecimenCommand(cmd).futureValue
            v mustSucceed { reply =>
              reply.id must be (f.shipment.id)
            }
          }
        }
      }

      it("users without access") {
        val (f, specimen, shipmentSpecimen) = shipmentSpecimenFixture
        forAll (f.usersCannotAddOrUpdateTable) { (user, label) =>
          info(label)
          forAll(shipmentSpecimenCommandsTable(user.id,
                                               f.shipment,
                                               specimen,
                                               shipmentSpecimen)) { (shipment, cmd) =>
            // set up pre-condition
            addToRepository(shipment)
            shipmentSpecimenRepository.removeAll
            cmd match {
              case _: ShipmentAddSpecimensCmd | _: ShipmentSpecimenExtraCmd =>
              case c: ShipmentSpecimensPresentCmd =>
                shipmentSpecimenRepository.put(shipmentSpecimen.copy(state = ShipmentItemState.Received))
              case c =>
                shipmentSpecimenRepository.put(shipmentSpecimen)
            }

            shipmentsService.processShipmentSpecimenCommand(cmd).futureValue mustFail "Unauthorized"
          }
        }
      }

    }

    describe("when removing a shipment") {

      it("users with access") {
        val f = new UsersWithShipmentAccessFixture

        val usersCanRemove = Table(("users that can remove specimens", "label"),
                                   (f.allCentresAdminUser,         "all centres admin user"),
                                   (f.centreOnlyShippingAdminUser, "centre only shipping admin user"))

        forAll (usersCanRemove) { (user, label) =>
          info(label)
          val cmd = getRemoveShipmentCmd(user.id, f.shipment)
          shipmentRepository.put(f.shipment) // restore it to it's previous state
          shipmentsService.removeShipment(cmd).futureValue mustSucceed { reply =>
            reply must be (true)
          }
        }
      }

      it("users without access") {
        val f = new UsersWithShipmentAccessFixture

        val usersCannotRemove = Table(("users that can remove specimens", "label"),
                                      (f.shippingUser,                "non-admin shipping user"),
                                      (f.noMembershipUser,         "no memberships user"),
                                      (f.noShippingPermissionUser, "no shipping permission user"))

        forAll (usersCannotRemove) { (user, label) =>
          info(label)
          val cmd = getRemoveShipmentCmd(user.id, f.shipment)
          shipmentsService.removeShipment(cmd).futureValue mustFail "Unauthorized"
        }
      }

    }

  }

}
