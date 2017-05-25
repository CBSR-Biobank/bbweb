package org.biobank.service.centres

import org.biobank.fixture._
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.ConcurrencySafeEntity
import org.biobank.domain.centre._
import org.biobank.domain.study._
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
  import org.biobank.infrastructure.command.CentreCommands._

  class UsersWithCentreAccessFixture {
    val location             = factory.createLocation
    val centre               = factory.createDisabledCentre.copy(locations = Set(location))
    val allCentresAdminUser  = factory.createActiveUser
    val centreOnlyAdminUser  = factory.createActiveUser
    val centreUser           = factory.createActiveUser

    val allCentresMembership = factory.createMembership.copy(
        userIds = Set(allCentresAdminUser.id),
        centreInfo = MembershipCentreInfo(true, Set.empty[CentreId]))

    val centreOnlyMembership = factory.createMembership.copy(
        userIds = Set(centreOnlyAdminUser.id, centreUser.id),
        centreInfo = MembershipCentreInfo(false, Set(centre.id)))

    def usersCanReadTable() = Table(("users with read access", "label"),
                                    (allCentresAdminUser, "all centres admin user"),
                                    (centreOnlyAdminUser,  "centre only admin user"),
                                    (centreUser,           "non-admin centre user"))

    def usersCanUpdateTable() = Table(("users with update access", "label"),
                                      (allCentresAdminUser, "all centres admin user"),
                                      (centreOnlyAdminUser,  "centre only admin user"))
    Set(centre,
        allCentresAdminUser,
        centreOnlyAdminUser,
        centreUser,
        allCentresMembership,
        centreOnlyMembership
    ).foreach(addToRepository)

    addUserToCentreAdminRole(allCentresAdminUser)
    addUserToCentreAdminRole(centreOnlyAdminUser)
    addUserToRole(centreUser, RoleId.CentreUser)
  }

  class UserWithNoCentreAccessFixture {
    val location            = factory.createLocation
    val centre              = factory.createDisabledCentre.copy(locations = Set(location))
    val nonMembershipUser       = factory.createActiveUser
    val noCentresMembership = factory.createMembership.copy(
        userIds = Set(nonMembershipUser.id),
        centreInfo = MembershipCentreInfo(false, Set.empty[CentreId]))

    Set(centre, nonMembershipUser, noCentresMembership ).foreach(addToRepository)
  }

  class CentresOfAllStatesFixure extends UsersWithCentreAccessFixture {
    val disabledCentre = centre
    val enabledCentre = factory.createEnabledCentre

    Set(disabledCentre, enabledCentre).foreach(addToRepository)
    addToRepository(centreOnlyMembership.copy(
                      centreInfo = centreOnlyMembership.centreInfo.copy(
                          centreIds = Set(disabledCentre.id, enabledCentre.id))))

  }

  protected val nameGenerator = new NameGenerator(this.getClass)

  protected val accessItemRepository = app.injector.instanceOf[AccessItemRepository]

  protected val membershipRepository = app.injector.instanceOf[MembershipRepository]

  protected val userRepository = app.injector.instanceOf[UserRepository]

  private val centreRepository = app.injector.instanceOf[CentreRepository]

  private val studyRepository = app.injector.instanceOf[StudyRepository]

  private val centresService = app.injector.instanceOf[CentresService]

  private def addUserToCentreAdminRole(user: User): Unit = {
    addUserToRole(user, RoleId.CentreAdministrator)
  }

  override protected def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit = {
    entity match {
      case u: User       => userRepository.put(u)
      case i: AccessItem => accessItemRepository.put(i)
      case s: Study      => studyRepository.put(s)
      case c: Centre     => centreRepository.put(c)
      case m: Membership => membershipRepository.put(m)
      case _             => fail("invalid entity")
    }
  }

  private def updateCommandsTable(sessionUserId: UserId,
                                  centre:        Centre,
                                  location:      Location,
                                  study:         Study) = {
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
          ),
          AddCentreLocationCmd(
            sessionUserId   = sessionUserId.id,
            id              = centre.id.id,
            expectedVersion = centre.version,
            name            = location.name,
            street          = location.street,
            city            = location.city,
            province        = location.province,
            postalCode      = location.postalCode,
            poBoxNumber     = location.poBoxNumber,
            countryIsoCode  = location.countryIsoCode
          ),
          RemoveCentreLocationCmd(
            sessionUserId   = sessionUserId.id,
            id              = centre.id.id,
            expectedVersion = centre.version,
            locationId      = location.id.id
          ),
          AddStudyToCentreCmd(
            sessionUserId   = sessionUserId.id,
            id              = centre.id.id,
            expectedVersion = centre.version,
            studyId         = study.id.id
          ),
          RemoveStudyFromCentreCmd(
            sessionUserId   = sessionUserId.id,
            id              = centre.id.id,
            expectedVersion = centre.version,
            studyId         = study.id.id
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
        val f = new UsersWithCentreAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          centresService.getCentresCount(user.id) mustSucceed { count =>
            count must be (1)
          }
        }
      }

      it("retrieve centre counts by status") {
        val f = new UsersWithCentreAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          centresService.getCountsByStatus(user.id) mustSucceed { counts =>
            counts.total must be (1)
          }
        }
      }

      it("retrieve centres ") {
        val f = new UsersWithCentreAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          centresService.getCentres(user.id, new FilterString(""), new SortString(""))
            .mustSucceed { centres =>
              centres must have length (1)
            }
        }
      }

      it("retrieve centre names") {
        val f = new UsersWithCentreAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          centresService.getCentreNames(user.id, new FilterString(""), new SortString(""))
            .mustSucceed { centres =>
              centres must have length (1)
            }
        }
      }

      it("retrieve a centre") {
        val f = new UsersWithCentreAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          centresService.getCentre(user.id, f.centre.id) mustSucceed { result =>
            result.id must be (f.centre.id)
          }
        }
      }

      it("search locations") {
        val f = new UsersWithCentreAccessFixture
        forAll (f.usersCanReadTable) { (user, label) =>
          info(label)
          val cmd = SearchCentreLocationsCmd(sessionUserId = user.id.id,
                                             filter        = "",
                                             limit         = 10)
          centresService.searchLocations(cmd).mustSucceed { centres =>
            centres must have size (1)
          }
        }
      }

      it("update a centre") {
        val f = new UsersWithCentreAccessFixture
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        forAll (f.usersCanUpdateTable) { (user, label) =>
          info(label)
          forAll(updateCommandsTable(user.id, f.centre, f.location, study)) { cmd =>
            val centre = cmd match {
                case _: AddCentreLocationCmd =>
                  f.centre.copy(locations = Set.empty[Location])
                case _: RemoveStudyFromCentreCmd =>
                  f.centre.copy(studyIds = Set(study.id))
                case _ =>
                  f.centre
            }

            centreRepository.put(centre) // restore the centre to it's previous state
            centresService.processCommand(cmd).futureValue mustSucceed { s =>
              s.id must be (centre.id)
            }
          }
        }
      }

      it("change a centre's state") {
        val f = new CentresOfAllStatesFixure
        forAll (f.usersCanUpdateTable) { (user, label) =>
          info(label)
          forAll(stateChangeCommandsTable(user.id,
                                          f.disabledCentre,
                                          f.enabledCentre)) { cmd =>
            Set(f.disabledCentre, f.enabledCentre).foreach(addToRepository)
            centresService.processCommand(cmd).futureValue mustSucceed { s =>
              s.id.id must be (cmd.id)
            }
          }
        }
      }

    }

    describe("a user without the Centre Admin role is not allowed to") {

      it("retrieve centre counts") {
        val f = new UserWithNoCentreAccessFixture
        centresService.getCentresCount(f.nonMembershipUser.id) mustFail "Unauthorized"
      }

      it("retrieve centre counts by status") {
        val f = new UserWithNoCentreAccessFixture
        centresService.getCountsByStatus(f.nonMembershipUser.id) mustFail "Unauthorized"
      }

      it("retrieve centres") {
        val f = new UserWithNoCentreAccessFixture
        centresService.getCentres(f.nonMembershipUser.id, new FilterString(""), new SortString(""))
          .mustFail("Unauthorized")
      }

      it("retrieve centre names") {
        val f = new UserWithNoCentreAccessFixture
        centresService.getCentreNames(f.nonMembershipUser.id, new FilterString(""), new SortString(""))
          .mustFail("Unauthorized")
      }

      it("retrieve a centre") {
        val f = new UserWithNoCentreAccessFixture
        val centre = factory.createEnabledCentre
        centreRepository.put(centre)
        centresService.getCentre(f.nonMembershipUser.id, centre.id) mustFail "Unauthorized"
      }

      it("search locations") {
        val f = new UserWithNoCentreAccessFixture
        val cmd = SearchCentreLocationsCmd(sessionUserId = f.nonMembershipUser.id.id,
                                           filter        = "",
                                           limit         = 10)
        centresService.searchLocations(cmd) mustFail "Unauthorized"
      }

      it("update a centre") {
        val f = new UserWithNoCentreAccessFixture
        val study = factory.createDisabledStudy
        studyRepository.put(study)
        forAll(updateCommandsTable(f.nonMembershipUser.id, f.centre, f.location, study)) { cmd =>
          centreRepository.put(f.centre) // restore the centre to it's previous state
          centresService.processCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }

      it("change a centre's state") {
        val f1 = new UserWithNoCentreAccessFixture
        val f2 = new CentresOfAllStatesFixure
        forAll(stateChangeCommandsTable(f1.nonMembershipUser.id,
                                        f2.disabledCentre,
                                        f2.enabledCentre)) { cmd =>
          Set(f2.disabledCentre, f2.enabledCentre).foreach(addToRepository)
          centresService.processCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }

    }

    describe("centres membership") {

      it("user has access to all centres corresponding his membership") {
        val secondCentre = factory.createDisabledCentre  // should show up in results
        addToRepository(secondCentre)

        val f = new UsersWithCentreAccessFixture
        centresService.getCentres(f.allCentresAdminUser.id, new FilterString(""), new SortString(""))
          .mustSucceed { reply =>
            reply must have size (2)
            val centreIds = reply.map(c => c.id)
            centreIds must contain (f.centre.id)
            centreIds must contain (secondCentre.id)
          }
      }

      it("user has access only to centres corresponding his membership") {
        val secondCentre = factory.createDisabledCentre  // should show up in results
        addToRepository(secondCentre)

        val f = new UsersWithCentreAccessFixture
        centresService.getCentres(f.centreOnlyAdminUser.id, new FilterString(""), new SortString(""))
          .mustSucceed { reply =>
            reply must have size (1)
            reply.map(c => c.id) must contain (f.centre.id)
          }
      }

      it("user does not have access to centre if not in membership") {
        val f = new UsersWithCentreAccessFixture

        // remove all studies from membership
        val noCentresMembership = f.centreOnlyMembership.copy(
            centreInfo = f.centreOnlyMembership.centreInfo.copy(centreIds = Set.empty[CentreId]))
        addToRepository(noCentresMembership)

        // should not show up in results
        val secondStudy = factory.createDisabledStudy
        addToRepository(secondStudy)

        centresService.getCentres(f.centreUser.id, new FilterString(""), new SortString(""))
          .mustSucceed { reply =>
            reply must have size (0)
          }
      }

    }

  }

}
