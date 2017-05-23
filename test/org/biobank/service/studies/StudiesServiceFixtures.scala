package org.biobank.service.studies

import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.study._
import org.biobank.domain.user._
import org.biobank.fixture._
import org.biobank.service.users.UserServiceFixtures

trait StudiesServiceFixtures extends ProcessorTestFixture with UserServiceFixtures {

  import org.biobank.TestUtils._
  import org.biobank.domain.access.AccessItem._

  class UsersStudyFixture(val adminUser:    ActiveUser,
                          val nonAdminUser: ActiveUser,
                          val membership:   Membership,
                          val study:        DisabledStudy)

  protected val factory: Factory

  protected val accessItemRepository: AccessItemRepository

  protected val membershipRepository: MembershipRepository

  protected val userRepository: UserRepository

  protected val studyRepository: StudyRepository

  protected val collectionEventTypeRepository: CollectionEventTypeRepository

  protected def addUserToStudyAdminRole(userId: UserId): Unit = {
    accessItemRepository.getRole(RoleId.StudyAdministrator) mustSucceed { role =>
      accessItemRepository.put(role.copy(userIds = role.userIds + userId))
    }
  }

  protected def usersStudyFixture() = {
    val adminUser = factory.createActiveUser
    val study = factory.createDisabledStudy
    val membership = factory.createMembership.copy(userIds = Set(adminUser.id),
                                                   studyInfo = MembershipStudyInfo(false, Set(study.id)))
    val f = new UsersStudyFixture(adminUser,
                                  factory.createActiveUser,
                                  membership,
                                  study)
    Set(f.adminUser, f.nonAdminUser, f.membership, f.study).foreach(addToRepository)
    addUserToStudyAdminRole(f.adminUser.id)
    f
  }

  protected def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit = {
    entity match {
      case u: User                => userRepository.put(u)
      case i: AccessItem          => accessItemRepository.put(i)
      case s: Study               => studyRepository.put(s)
      case c: CollectionEventType => collectionEventTypeRepository.put(c)
      case m: Membership          => membershipRepository.put(m)
      case _                      => fail("invalid entity")
    }
  }

  override def beforeEach() {
    super.beforeEach()
    removeUsersFromRepository
    restoreRoles
    studyRepository.removeAll
  }
}
