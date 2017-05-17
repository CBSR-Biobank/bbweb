package org.biobank.service.users

import org.biobank.Global
import org.biobank.domain.access._
import org.biobank.fixture.NameGenerator
import org.biobank.domain._
import org.biobank.domain.user._

trait UserServiceFixtures {

  import org.biobank.TestUtils._
  import org.biobank.domain.access.AccessItem._

  class UsersOfAllStates(val registeredUser: RegisteredUser,
                         val activeUser:     ActiveUser,
                         val lockedUser:     LockedUser)

  class AdminUserFixture(val adminUser: ActiveUser)

  class UsersFixture(adminUser:        ActiveUser,
                     val nonAdminUser: ActiveUser,
                     val user:         ActiveUser)
      extends AdminUserFixture(adminUser)

  class UsersFixtureWithPassword(adminUser:             ActiveUser,
                                 nonAdminUser:          ActiveUser,
                                 user:                  ActiveUser,
                                 val userPlainPassword: String)
      extends UsersFixture(adminUser, nonAdminUser, user)

  protected val factory: Factory

  protected val nameGenerator: NameGenerator

  protected val accessItemRepository: AccessItemRepository

  protected val userRepository: UserRepository

  protected def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit

  // Removes all users but the protected default user
  protected def removeUsersFromRepository(): Unit = {
    userRepository.getValues
      .filter { u => u.id != Global.DefaultUserId }
      .foreach(userRepository.remove)
  }

  protected def restoreRoles(): Unit = {
    accessItemRepository.getValues
      .collect { case r: Role => r }
      .filter { r => r.id != RoleId.WebsiteAdministrator }
      .foreach(r => accessItemRepository.put(r.copy(userIds = Set.empty[UserId])))
    accessItemRepository.getRole(RoleId.WebsiteAdministrator).foreach { r =>
      accessItemRepository.put(r.copy(userIds = Set(Global.DefaultUserId)))
    }
  }

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

  protected def usersFixture() = {
    val f = new UsersFixture(factory.createActiveUser,
                             factory.createActiveUser,
                             factory.createActiveUser)
    Set(f.adminUser, f.nonAdminUser, f.user).foreach(addToRepository)
    addUserToUserAdminRole(f.adminUser.id)
    f
  }

}
