package org.biobank.services.users

import org.biobank.domain.access._
import org.biobank.domain.centres.CentreRepository
import org.biobank.domain.studies.{StudyId, StudyRepository}
import org.biobank.domain.users._
import org.biobank.fixture.{NameGenerator, ProcessorTestFixture}
import org.biobank.services.{FilterAndSortQuery, FilterString, PagedQuery, PasswordHasher, SortString}
import org.biobank.services.access._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.TableDrivenPropertyChecks._

/**
 * Primarily these are tests that exercise the User Access aspect of UsersService.
 */
class UsersServiceSpec
    extends ProcessorTestFixture
    with AccessServiceFixtures
    with UserServiceFixtures
    with UserFixtures
    with ScalaFutures {

  import org.biobank.TestUtils._
  import org.biobank.infrastructure.commands.UserCommands._
  import org.biobank.domain.access.AccessItem._

  val usersService = app.injector.instanceOf[UsersService]

  val accessService = app.injector.instanceOf[AccessService]

  val accessItemRepository = app.injector.instanceOf[AccessItemRepository]

  val membershipRepository = app.injector.instanceOf[MembershipRepository]

  val userRepository = app.injector.instanceOf[UserRepository]

  val studyRepository = app.injector.instanceOf[StudyRepository]

  val centreRepository = app.injector.instanceOf[CentreRepository]

  val passwordHasher = app.injector.instanceOf[PasswordHasher]

  val nameGenerator = new NameGenerator(this.getClass)

  protected def addUserToUserAdminRole(userId: UserId): Unit = {
    accessItemRepository.getRole(RoleId.UserAdministrator) mustSucceed { role =>
      accessItemRepository.put(role.copy(userIds = role.userIds + userId))
    }
  }

  protected def adminUserFixture() = {
    val f = new AdminUserFixture(factory.createActiveUser)
    addToRepository(f.adminUser)
    addUserToUserAdminRole(f.adminUser.id)
    f
  }

  def usersFixture() = {
    val plainPassword = nameGenerator.next[User]
    val f = new UsersFixtureWithPassword(factory.createActiveUser,
                                         factory.createActiveUser,
                                         createActiveUser(plainPassword),
                                         plainPassword)
    Set(f.adminUser, f.nonAdminUser, f.user).foreach(addToRepository)
    addUserToUserAdminRole(f.adminUser.id)
    f
  }

  def commandsTable(sessionUserId: UserId, user: User, userPlainPassword: String) =
    Table("user update commands",
          UpdateUserNameCmd(
            sessionUserId   = sessionUserId.id,
            id              = user.id.id,
            expectedVersion = user.version,
            name            = faker.Name.name),
          UpdateUserEmailCmd(
            sessionUserId   = sessionUserId.id,
            id              = user.id.id,
            expectedVersion = user.version,
            email           = nameGenerator.nextEmail[User]),
          UpdateUserPasswordCmd(
            sessionUserId   = sessionUserId.id,
            id              = user.id.id,
            expectedVersion = user.version,
            currentPassword = userPlainPassword,
            newPassword     = nameGenerator.next[String]),
          UpdateUserAvatarUrlCmd(
            sessionUserId   = sessionUserId.id,
            id              = user.id.id,
            expectedVersion = user.version,
            avatarUrl       = Some(nameGenerator.nextUrl[String])))

  def stateChangeCommandsTable(sessionUserId:  UserId,
                               registeredUser: RegisteredUser,
                               activeUser:     ActiveUser,
                               lockedUser:     LockedUser) =
    Table("user sate change commands",
          ActivateUserCmd(
            sessionUserId   = sessionUserId.id,
            id              = registeredUser.id.id,
            expectedVersion = registeredUser.version),
          LockUserCmd(
            sessionUserId   = sessionUserId.id,
            id              = activeUser.id.id,
            expectedVersion = activeUser.version),
          UnlockUserCmd(
            sessionUserId   = sessionUserId.id,
            id              = lockedUser.id.id,
            expectedVersion = lockedUser.version))

  override def beforeEach() {
    super.beforeEach()
    removeUsersFromRepository
    restoreRoles
  }

  describe("UsersService") {

    describe("a user with the User Admin role is allowed to") {

      it("retrieve a user") {
        val f = usersFixture
        usersService.getUserIfAuthorized(f.adminUser.id, f.user.id) mustSucceed { u =>
          u.id must be (f.user.id.id)
        }
      }

      it("retrieve users") {
        val query = PagedQuery(new FilterString(""), new SortString(""), 0 , 1)
        val f = usersFixture
        usersService.getUsers(f.adminUser.id, query).futureValue
          .mustSucceed { results =>
            results.items must have length (userRepository.getValues.size.toLong)
          }
      }

      it("user counts by status") {
        val f = adminUserFixture
        usersService.getCountsByStatus(f.adminUser.id) mustSucceed { counts =>
          counts.total must be (2) // 2 for default user and admin user added by this test
        }
      }

      it("update a user") {
        val f = usersFixture
        forAll(commandsTable(f.adminUser.id, f.user, f.userPlainPassword)) { cmd =>
          userRepository.put(f.user) // restore the user to it's previous state
          usersService.processCommand(cmd).futureValue mustSucceed { u =>
            u.id must be (f.user.id.id)
          }
        }
      }

      it("change a user's state") {
        val f = usersFixture
        val u = usersOfAllStates
        val table = stateChangeCommandsTable(f.adminUser.id, u.registeredUser, u.activeUser, u.lockedUser)
        Set(u.registeredUser, u.activeUser, u.lockedUser).foreach(userRepository.put)
        forAll(table) { cmd =>
          usersService.processCommand(cmd).futureValue mustSucceed { u =>
            u.id must be (cmd.id)
          }
        }
      }

    }

    describe("a user without the User Admin role is not allowed to") {

      it("retrieve a user") {
        val f = usersFixture
        usersService.getUserIfAuthorized(f.nonAdminUser.id, f.user.id) mustFail "Unauthorized"
      }

      it("retrieve users") {
        val f = usersFixture
        val query = PagedQuery(new FilterString(""), new SortString(""), 0 , 1)

        usersService.getUsers(f.nonAdminUser.id, query).futureValue
          .mustFail("Unauthorized")
      }

      it("user counts by status") {
        val f = usersFixture
        usersService.getCountsByStatus(f.nonAdminUser.id) mustFail "Unauthorized"
      }

      it("update a user") {
        val f = usersFixture
        forAll(commandsTable(f.nonAdminUser.id, f.user, f.userPlainPassword)) { cmd =>
          userRepository.put(f.user) // restore the user to it's previous state
          usersService.processCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }

      it("change a user's state") {
        val f = usersFixture
        val u = usersOfAllStates
        val table = stateChangeCommandsTable(f.nonAdminUser.id, u.registeredUser, u.activeUser, u.lockedUser)
        Set(u.registeredUser, u.activeUser, u.lockedUser).foreach(userRepository.put)
        forAll(table) { cmd =>
          usersService.processCommand(cmd).futureValue mustFail "Unauthorized"
        }
      }

    }

    describe("studies membership") {

      it("user has access to all studies corresponding his membership") {
        val f = membershipFixture
        val membership = f.membership.copy(studyData  = MembershipEntitySet(true, Set.empty[StudyId]))
        val query = FilterAndSortQuery(new FilterString(""), new SortString(""))

        Set(f.user, f.study, membership).foreach(addToRepository)
        usersService.getUserStudies(f.user.id, query).futureValue mustSucceed { reply =>
          reply must have size (1)
          reply must contain (f.study.id)
        }
      }

      it("user has access to studies corresponding his membership") {
        val f = membershipFixture
        val query = FilterAndSortQuery(new FilterString(""), new SortString(""))

        Set(f.user, f.study, f.membership).foreach(addToRepository)
        usersService.getUserStudies(f.user.id, query).futureValue mustSucceed { reply =>
          reply must have size (1)
          reply must contain (f.study.id)
        }
      }

    }

  }

}
