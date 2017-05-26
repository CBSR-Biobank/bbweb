package org.biobank.service.studies

import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.study._
import org.biobank.domain.user._
import org.biobank.fixture._
import org.biobank.service.users.UserServiceFixtures
import org.scalatest.prop.TableDrivenPropertyChecks._

trait StudiesServiceFixtures extends ProcessorTestFixture with UserServiceFixtures {

  class UsersWithStudyAccessFixture {
    val study               = factory.createDisabledStudy
    val allStudiesAdminUser = factory.createActiveUser
    val studyOnlyAdminUser  = factory.createActiveUser
    val studyUser           = factory.createActiveUser
    val noMembershipUser    = factory.createActiveUser

    val allStudiesMembership = factory.createMembership.copy(
        userIds = Set(allStudiesAdminUser.id),
        studyInfo = MembershipStudyInfo(true, Set.empty[StudyId]))

    val studyOnlyMembership = factory.createMembership.copy(
        userIds = Set(studyOnlyAdminUser.id, studyUser.id),
        studyInfo = MembershipStudyInfo(false, Set(study.id)))

    val noStudiesMembership    = factory.createMembership.copy(
        userIds = Set(noMembershipUser.id),
        studyInfo = MembershipStudyInfo(false, Set.empty[StudyId]))

    def usersCanReadTable() = Table(("users with read access", "label"),
                                    (allStudiesAdminUser, "all studies admin user"),
                                    (studyOnlyAdminUser,  "study only admin user"),
                                    (studyUser,           "non-admin study user"))

    def usersCanUpdateTable() = Table(("users with update access", "label"),
                                      (allStudiesAdminUser, "all studies admin user"),
                                      (studyOnlyAdminUser,  "study only admin user"))

    def usersCannotUpdateTable() = Table(("users with update access", "label"),
                                         (studyUser,         "study user"),
                                         (noMembershipUser,  "non membership user"))
    Set(study,
        allStudiesAdminUser,
        studyOnlyAdminUser,
        studyUser,
        noMembershipUser,
        allStudiesMembership,
        studyOnlyMembership,
        noStudiesMembership
    ).foreach(addToRepository)

    addUserToStudyAdminRole(allStudiesAdminUser)
    addUserToStudyAdminRole(studyOnlyAdminUser)
    addUserToRole(studyUser, RoleId.StudyUser)
    addUserToRole(noMembershipUser, RoleId.StudyUser)
  }

  class UserWithNoStudyAccessFixture {
    val study                  = factory.createDisabledStudy
    val nonStudyPermissionUser = factory.createActiveUser

    val noStudiesMembership    = factory.createMembership.copy(
        userIds = Set(nonStudyPermissionUser.id),
        studyInfo = MembershipStudyInfo(false, Set.empty[StudyId]))

    Set(study, nonStudyPermissionUser, noStudiesMembership).foreach(addToRepository)
  }

  protected val factory: Factory

  protected val accessItemRepository: AccessItemRepository

  protected val membershipRepository: MembershipRepository

  protected val userRepository: UserRepository

  protected val studyRepository: StudyRepository

  protected val collectionEventTypeRepository: CollectionEventTypeRepository

  protected def addUserToStudyAdminRole(user: User): Unit = {
    addUserToRole(user, RoleId.StudyAdministrator)
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
