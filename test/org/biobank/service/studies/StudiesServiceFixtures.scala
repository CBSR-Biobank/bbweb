package org.biobank.services.studies

import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.studies._
import org.biobank.domain.users._
import org.biobank.fixture._
import org.biobank.services.users.UserServiceFixtures
import org.scalatest.prop.TableDrivenPropertyChecks._

trait StudiesServiceFixtures extends ProcessorTestFixture with UserServiceFixtures {

  class UsersWithStudyAccessFixture {
    val study                  = factory.createDisabledStudy
    val allStudiesAdminUser    = factory.createActiveUser
    val studyOnlyAdminUser     = factory.createActiveUser
    val studyUser              = factory.createActiveUser
    val noMembershipUser       = factory.createActiveUser
    val nonStudyPermissionUser = factory.createActiveUser

    val allStudiesMembership = factory.createMembership.copy(
        userIds = Set(allStudiesAdminUser.id),
        studyData = MembershipEntitySet(true, Set.empty[StudyId]))

    val studyOnlyMembership = factory.createMembership.copy(
        userIds = Set(studyOnlyAdminUser.id, studyUser.id),
        studyData = MembershipEntitySet(false, Set(study.id)))

    val noStudiesMembership = factory.createMembership.copy(
        userIds = Set(noMembershipUser.id, nonStudyPermissionUser.id),
        studyData = MembershipEntitySet(false, Set.empty[StudyId]))

    def usersCanReadTable() = Table(("users with read access", "label"),
                                    (allStudiesAdminUser, "all studies admin user"),
                                    (studyOnlyAdminUser,  "study only admin user"),
                                    (studyUser,           "non-admin study user"))

    def usersCannotReadTable() = Table(("users without read access", "label"),
                                       (noMembershipUser,       "non membership user"),
                                       (nonStudyPermissionUser, "non study permission user"))

    def usersCanAddOrUpdateTable() = Table(("users with update access", "label"),
                                      (allStudiesAdminUser, "all studies admin user"),
                                      (studyOnlyAdminUser,  "study only admin user"))

    def usersCannotAddOrUpdateTable() = Table(("users without update access", "label"),
                                         (studyUser,              "study user"),
                                         (noMembershipUser,       "non membership user"),
                                         (nonStudyPermissionUser, "non study permission user"))
    Set(study,
        allStudiesAdminUser,
        studyOnlyAdminUser,
        studyUser,
        noMembershipUser,
        nonStudyPermissionUser,
        allStudiesMembership,
        studyOnlyMembership,
        noStudiesMembership
    ).foreach(addToRepository)

    addUserToStudyAdminRole(allStudiesAdminUser)
    addUserToStudyAdminRole(studyOnlyAdminUser)
    addUserToRole(studyUser, RoleId.StudyUser)
    addUserToRole(noMembershipUser, RoleId.StudyUser)
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
      case _                      => fail(s"invalid entity: ${entity}")
    }
  }

  override def beforeEach() {
    super.beforeEach()
    removeUsersFromRepository
    restoreRoles
    studyRepository.removeAll
  }
}
