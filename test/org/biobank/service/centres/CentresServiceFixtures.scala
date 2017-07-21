package org.biobank.service.centres

import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.centre._
import org.biobank.domain.study._
import org.biobank.domain.user._
import org.biobank.fixture._
import org.biobank.service.users.UserServiceFixtures
import org.scalatest.prop.TableDrivenPropertyChecks._

trait CentresServiceFixtures extends ProcessorTestFixture with UserServiceFixtures {

  class UsersWithCentreAccessFixture {
    val location             = factory.createLocation
    val centre               = factory.createDisabledCentre.copy(locations = Set(location))

    val allCentresAdminUser    = factory.createActiveUser
    val centreOnlyAdminUser    = factory.createActiveUser
    val centreUser             = factory.createActiveUser
    val noMembershipUser       = factory.createActiveUser
    val noCentrePermissionUser = factory.createActiveUser

    val allCentresMembership = factory.createMembership.copy(
        userIds = Set(allCentresAdminUser.id),
        centreData = MembershipEntityData(true, Set.empty[CentreId]))

    val centreOnlyMembership = factory.createMembership.copy(
        userIds = Set(centreOnlyAdminUser.id, centreUser.id),
        centreData = MembershipEntityData(false, Set(centre.id)))

    val noCentresMembership = factory.createMembership.copy(
        userIds = Set(noMembershipUser.id, noCentrePermissionUser.id),
        centreData = MembershipEntityData(false, Set.empty[CentreId]))

    def usersCanReadTable() = Table(("users with read access", "label"),
                                    (allCentresAdminUser, "all centres admin user"),
                                    (centreOnlyAdminUser,  "centre only admin user"),
                                    (centreUser,           "non-admin centre user"))

    def usersCanAddOrUpdateTable() = Table(("users with update access", "label"),
                                      (allCentresAdminUser, "all centres admin user"),
                                      (centreOnlyAdminUser,  "centre only admin user"))

    def usersCannotAddOrUpdateTable() = Table(("users with update access", "label"),
                                         (centreUser,             "non-admin centre user"),
                                         (noMembershipUser,       "all centres admin user"),
                                         (noCentrePermissionUser, "centre only admin user"))
    Set(centre,
        allCentresAdminUser,
        centreOnlyAdminUser,
        centreUser,
        noMembershipUser,
        noCentrePermissionUser,
        allCentresMembership,
        centreOnlyMembership,
        noCentresMembership
    ).foreach(addToRepository)

    addUserToCentreAdminRole(allCentresAdminUser)
    addUserToCentreAdminRole(centreOnlyAdminUser)
    addUserToRole(centreUser, RoleId.CentreUser)
    addUserToRole(noMembershipUser, RoleId.CentreUser)
  }

  protected val factory: Factory

  protected val accessItemRepository: AccessItemRepository

  protected val membershipRepository: MembershipRepository

  protected val userRepository: UserRepository

  protected val centreRepository: CentreRepository

  protected val studyRepository: StudyRepository

  private def addUserToCentreAdminRole(user: User): Unit = {
    addUserToRole(user, RoleId.CentreAdministrator)
  }

  override protected def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit = {
    entity match {
      case u: User       => userRepository.put(u)
      case i: AccessItem => accessItemRepository.put(i)
      case m: Membership => membershipRepository.put(m)
      case c: Centre     => centreRepository.put(c)
      case s: Study      => studyRepository.put(s)
      case _             => fail("invalid entity")
    }
  }

}
