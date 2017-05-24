package org.biobank.service.centres

import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.ConcurrencySafeEntity
import org.biobank.domain.centre._
import org.biobank.domain.user._
import org.biobank.service.{FilterString, SortString}
import org.biobank.service.users._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.TableDrivenPropertyChecks._

/**
 * Primarily these are tests that exercise the User Access aspect of CentresService.
 */
class CentresServiceSpec
    extends ProcessorTestFixture
    with UserServiceFixtures
    with ScalaFutures {

  import org.biobank.TestUtils._
  import org.biobank.domain.access.AccessItem._
  import org.biobank.infrastructure.command.CentreCommands._

  class UsersCentreFixture(val adminUser:    ActiveUser,
                           val nonAdminUser: ActiveUser,
                           val membership:   Membership,
                           val centre:       DisabledCentre)

  class CentreOfAllStatesFixure(val adminUser:     ActiveUser,
                               val nonAdminUser:   ActiveUser,
                               val membership:     Membership,
                               val disabledCentre: DisabledCentre,
                               val enabledCentre:  EnabledCentre)

  protected val nameGenerator = new NameGenerator(this.getClass)

  protected val accessItemRepository = app.injector.instanceOf[AccessItemRepository]

  protected val membershipRepository = app.injector.instanceOf[MembershipRepository]

  protected val userRepository = app.injector.instanceOf[UserRepository]

  private val centreRepository = app.injector.instanceOf[CentreRepository]

  private val centresService = app.injector.instanceOf[CentresService]

  private def addUserToCentreAdminRole(userId: UserId): Unit = {
    accessItemRepository.getRole(RoleId.CentreAdministrator) mustSucceed { role =>
      accessItemRepository.put(role.copy(userIds = role.userIds + userId))
    }
  }

  private def centresOfAllStatesFixure() = {
    val adminUser = factory.createActiveUser
    val disabledCentre = factory.createDisabledCentre
    val enabledCentre = factory.createEnabledCentre
    val membershipCentreInfo = MembershipCentreInfo(false,
                                                    Set(disabledCentre.id, enabledCentre.id))
    val membership = factory.createMembership.copy(userIds = Set(adminUser.id),
                                                   centreInfo = membershipCentreInfo)
    val f = new CentreOfAllStatesFixure(adminUser,
                                       factory.createActiveUser,
                                       membership,
                                       disabledCentre,
                                       enabledCentre)
    Set(f.adminUser,
        f.nonAdminUser,
        f.membership,
        f.disabledCentre,
        f.enabledCentre
    ).foreach(addToRepository)

    addUserToCentreAdminRole(f.adminUser.id)
    f
  }

  override protected def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit = {
    entity match {
      case u: User       => userRepository.put(u)
      case i: AccessItem => accessItemRepository.put(i)
      case s: Centre      => centreRepository.put(s)
      case m: Membership => membershipRepository.put(m)
      case _             => fail("invalid entity")
    }
  }

  protected def usersCentreFixture() = {
    val adminUser = factory.createActiveUser
    val centre = factory.createDisabledCentre
    val membership = factory.createMembership.copy(userIds = Set(adminUser.id),
                                                   centreInfo = MembershipCentreInfo(false, Set(centre.id)))
    val f = new UsersCentreFixture(adminUser,
                                   factory.createActiveUser,
                                   membership,
                                   centre)
    Set(f.adminUser, f.nonAdminUser, f.membership, f.centre).foreach(addToRepository)
    addUserToCentreAdminRole(f.adminUser.id)
    f
  }
  private def updateCommandsTable(sessionUserId: UserId, centre: Centre, annotationType: AnnotationType) = {
    Table("centre update commands",
          UpdateCentreNameCmd(
            sessionUserId   = sessionUserId.id,
            id              = centre.id.id,
            expectedVersion = centre.version,
            name            = nameGenerator.next[String]
          ),
          UpdateCentreDescriptionCmd(
            sessionUserId   = sessionUserId.id,
            id              = centre.id.id,
            expectedVersion = centre.version,
            description     = Some(nameGenerator.next[String])
          )
    )
  }

  private def stateChangeCommandsTable(sessionUserId:  UserId,
                                       disabledCentre:  DisabledCentre,
                                       enabledCentre:   EnabledCentre) =
    Table("user sate change commands",
          EnableCentreCmd(
            sessionUserId   = sessionUserId.id,
            id              = disabledCentre.id.id,
            expectedVersion = disabledCentre.version
          ),
          DisableCentreCmd(
            sessionUserId   = sessionUserId.id,
            id              = enabledCentre.id.id,
            expectedVersion = enabledCentre.version
          )
    )

  override def beforeEach() {
    super.beforeEach()
    removeUsersFromRepository
    restoreRoles
    centreRepository.removeAll
  }

  describe("CentresService") {

    describe("a user with the Centre Admin role is allowed to") {

      it("retrieve centre counts") {
        val f = usersCentreFixture
        centresService.getCentresCount(f.adminUser.id) mustSucceed { count =>
          count must be (1)
        }
      }

      it("retrieve centre counts by status") {
        val f = usersCentreFixture
        centresService.getCountsByStatus(f.adminUser.id) mustSucceed { counts =>
          counts.total must be (1)
        }
      }

      it("retrieve centres") {
        val f = usersCentreFixture
        centresService.getCentres(f.adminUser.id, new FilterString(""), new SortString(""))
          .mustSucceed { centres =>
            centres must have length (1)
          }
      }

      it("retrieve centre names") {
        val f = usersCentreFixture
        centresService.getCentreNames(f.adminUser.id, new FilterString(""), new SortString(""))
          .mustSucceed { centres =>
            centres must have length (1)
          }
      }

      it("retrieve a centre") {
        val f = usersCentreFixture
        centresService.getCentre(f.adminUser.id, f.centre.id) mustSucceed { result =>
          result.id must be (f.centre.id)
        }
      }

      it("update a centre") {
        val f = usersCentreFixture
        val annotationType = factory.createAnnotationType

        forAll(updateCommandsTable(f.adminUser.id, f.centre, annotationType)) { cmd =>
          val centre = cmd match {
              // case _: CentreUpdateParticipantAnnotationTypeCmd | _: UpdateCentreRemoveAnnotationTypeCmd =>
              //   f.centre.copy(annotationTypes = Set(annotationType))
              case _ =>
                f.centre
            }

          centreRepository.put(centre) // restore the centre to it's previous state
          centresService.processCommand(cmd).futureValue mustSucceed { s =>
            s.id must be (centre.id)
          }
        }
      }

      it("change a centre's state") {
        val f = centresOfAllStatesFixure
        forAll(stateChangeCommandsTable(f.adminUser.id,
                                        f.disabledCentre,
                                        f.enabledCentre)) { cmd =>
          Set(f.disabledCentre, f.enabledCentre).foreach(addToRepository)
          centresService.processCommand(cmd).futureValue mustSucceed { s =>
            s.id.id must be (cmd.id)
          }
        }
      }

    }

    describe("a user without the Centre Admin role is not allowed to") {

      it("retrieve centre counts") {
        val f = usersCentreFixture
        centresService.getCentresCount(f.nonAdminUser.id) mustFail "Unauthorized"
      }

      it("retrieve centre counts by status") {
        val f = usersCentreFixture
        centresService.getCountsByStatus(f.nonAdminUser.id) mustFail "Unauthorized"
      }

      it("retrieve centres") {
        val f = usersCentreFixture
        centresService.getCentres(f.nonAdminUser.id, new FilterString(""), new SortString(""))
          .mustFail("Unauthorized")
      }

      it("retrieve centre names") {
        val f = usersCentreFixture
        centresService.getCentreNames(f.nonAdminUser.id, new FilterString(""), new SortString(""))
          .mustFail("Unauthorized")
      }

      it("retrieve a centre") {
        val f = usersCentreFixture
        val centre = factory.createEnabledCentre
        centreRepository.put(centre)
        centresService.getCentre(f.nonAdminUser.id, centre.id) mustFail "Unauthorized"
      }

      it("update a centre") {
        val f = usersCentreFixture
        val annotationType = factory.createAnnotationType

        forAll(updateCommandsTable(f.nonAdminUser.id, f.centre, annotationType)) { cmd =>
          centreRepository.put(f.centre) // restore the centre to it's previous state
          centresService.processCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }

      it("change a centre's state") {
        val f = centresOfAllStatesFixure
        forAll(stateChangeCommandsTable(f.nonAdminUser.id,
                                        f.disabledCentre,
                                        f.enabledCentre)) { cmd =>
          Set(f.disabledCentre, f.enabledCentre).foreach(addToRepository)
          centresService.processCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }

    }

  }

}
