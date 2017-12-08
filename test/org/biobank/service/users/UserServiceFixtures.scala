package org.biobank.service.users

import org.biobank.Global
import org.biobank.domain.access._
import org.biobank.fixture.NameGenerator
import org.biobank.domain._
import org.biobank.domain.user._

trait UserServiceFixtures {

  import org.biobank.TestUtils._
  import org.biobank.domain.access.AccessItem._
  import org.biobank.domain.access.RoleId._

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
      extends UsersFixture(adminUser, nonAdminUser, user) {
    Set(adminUser, nonAdminUser, user).foreach { user =>
      val membership = factory.createMembership.copy(userIds = Set(user.id))
      membershipRepository.put(membership)
    }

  }

  protected val factory: Factory

  protected val nameGenerator: NameGenerator

  protected val accessItemRepository: AccessItemRepository

  protected val membershipRepository: MembershipRepository

  protected val userRepository: UserRepository

  protected def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit

  protected def addUserToRole(user: User, roleId: RoleId): Unit = {
    accessItemRepository.getRole(roleId) mustSucceed { role =>
      accessItemRepository.put(role.copy(userIds = role.userIds + user.id))
    }
  }

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

}
