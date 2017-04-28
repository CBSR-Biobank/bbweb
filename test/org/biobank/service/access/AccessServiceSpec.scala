package org.biobank.service.access

import org.biobank.fixture._
import org.biobank.domain.ConcurrencySafeEntity
import org.biobank.domain.access._
import org.biobank.domain.user.{User, UserRepository}
import org.biobank.domain.study.{Study, StudyRepository}
import org.biobank.domain.centre.{Centre, CentreRepository}

class AccessServiceSpec extends TestFixture {

  import org.biobank.TestUtils._

  val accessItemRepository = app.injector.instanceOf[AccessItemRepository]

  val accessService = app.injector.instanceOf[AccessService]

  val membershipRepository = app.injector.instanceOf[MembershipRepository]

  val userRepository = app.injector.instanceOf[UserRepository]

  val studyRepository = app.injector.instanceOf[StudyRepository]

  val centreRepository = app.injector.instanceOf[CentreRepository]

  case class PermissionFixture(user: User, role: Role, permission: Permission)

  case class MembershipFixture(user:       User,
                               study:      Study,
                               centre:     Centre) extends {
    val membership = factory.createMembership.copy(userIds   = Set(user.id),
                                                   studyIds  = Set(study.id),
                                                   centreIds = Set(centre.id))
  }

  def permissionFixture = PermissionFixture(user       = factory.createActiveUser,
                                            role       = factory.createRole,
                                            permission = factory.createPermission)

  def membershipFixture = MembershipFixture(user   = factory.createActiveUser,
                                            study  = factory.createEnabledStudy,
                                            centre = factory.createEnabledCentre)

  def addToRepository[T <: ConcurrencySafeEntity[_]](entity: T): Unit = {
    entity match {
      case u: User       => userRepository.put(u)
      case s: Study      => studyRepository.put(s)
      case c: Centre     => centreRepository.put(c)
      case i: AccessItem => accessItemRepository.put(i)
      case m: Membership => membershipRepository.put(m)
      case _             => fail("invalid entity")
    }
  }

  override def beforeEach() {
    accessItemRepository.removeAll
    membershipRepository.removeAll
    super.beforeEach()
  }

  describe("The Access Service") {

    describe("for hasPermission") {

      it("allow access to a user that has permission through a role") {
        val f = permissionFixture
        val role = f.role.copy(userIds = Set(f.user.id))
        val permission = f.permission.copy(parentIds = Set(role.id))

        Set(f.user, role, permission).foreach(addToRepository)

        accessService.hasPermission(f.user.id, permission.id) mustSucceed { _ must be (true) }
      }

      it("allow access to a user through multiple roles") {
        val f = permissionFixture
        val parentRole = f.role.copy(userIds = Set(f.user.id))
        val childRole = factory.createRole.copy(parentIds = Set(parentRole.id))
        val permission = f.permission.copy(parentIds = Set(childRole.id))

        Set(f.user, parentRole, childRole, permission).foreach(addToRepository)

        accessService.hasPermission(f.user.id, permission.id) mustSucceed { _ must be (true) }
      }

      it("allow access to a user through role that has multiple permissions") {
        val f = permissionFixture
        val role = f.role.copy(userIds = Set(f.user.id))
        val permission1 = f.permission.copy(parentIds = Set(f.role.id))
        val permission2 = factory.createPermission.copy(parentIds = Set(f.role.id))

        Set(f.user, role, permission1, permission2).foreach(addToRepository)

        accessService.hasPermission(f.user.id, permission2.id) mustSucceed { _ must be (true) }
      }

      it("allow access to a users through different roles from a permission") {
        val f = permissionFixture
        val user1 = f.user
        val user2 = factory.createActiveUser
        val role1 = f.role.copy(userIds = Set(user1.id))
        val role2 = factory.createRole.copy(userIds = Set(user2.id))
        val permission = f.permission.copy(parentIds = Set(role1.id, role2.id))

        Set(user1, user2, role1, role2, permission).foreach(addToRepository)

        accessService.hasPermission(user1.id, permission.id) mustSucceed { _ must be (true) }

        accessService.hasPermission(user2.id, permission.id) mustSucceed { _ must be (true) }
      }

      it("allow access to a users through common role") {
        val f = permissionFixture
        val user1 = f.user
        val user2 = factory.createActiveUser
        val parentRole1 = f.role.copy(userIds = Set(user1.id))
        val parentRole2 = factory.createRole.copy(userIds = Set(user2.id))
        val childRole = factory.createRole.copy(parentIds = Set(parentRole1.id, parentRole2.id))
        val permission = f.permission.copy(parentIds = Set(childRole.id))

        Set(user1, user2, parentRole1, parentRole2, childRole, permission).foreach(addToRepository)

        accessService.hasPermission(user1.id, permission.id) mustSucceed { _ must be (true) }

        accessService.hasPermission(user2.id, permission.id) mustSucceed { _ must be (true) }
      }

      it("forbid access to a user does not have permission") {
        val f = permissionFixture
        val role = f.role.copy(userIds = Set(f.user.id))
        val permission = f.permission.copy(parentIds = Set(role.id))

        Set(f.user, role, permission).foreach(addToRepository)

        val user2 = factory.createActiveUser
        accessService.hasPermission(user2.id, permission.id) mustFail "Unauthorized"
      }

    }

    describe("for isMember") {

      it("allow a user that is a member of a study and centre") {
        val f = membershipFixture
        Set(f.user, f.study, f.centre, f.membership).foreach(addToRepository)

        accessService.isMember(f.user.id, Some(f.study.id), Some(f.centre.id)) mustSucceed { _ must be (true) }

        accessService.isMember(f.user.id, Some(f.study.id), None) mustSucceed { _ must be (true) }

        accessService.isMember(f.user.id, None, Some(f.centre.id)) mustSucceed { _ must be (true) }

        accessService.isMember(f.user.id, None, None) mustSucceed { _ must be (false) }
      }

      it("allow user that is member of all studies and all centres") {
        val f = membershipFixture
        val membership = factory.createMembership.copy(userIds = Set(f.user.id),
                                                       allStudies = true,
                                                       allCentres = true)

        Set(f.user, f.study, f.centre, membership).foreach(addToRepository)

        accessService.isMember(f.user.id, Some(f.study.id), Some(f.centre.id)) mustSucceed { _ must be (true) }

        accessService.isMember(f.user.id, Some(f.study.id), None) mustSucceed { _ must be (true) }

        accessService.isMember(f.user.id, None, Some(f.centre.id)) mustSucceed { _ must be (true) }

        accessService.isMember(f.user.id, None, None) mustSucceed { _ must be (false) }
      }

      it("forbid a user that is not a member of a study and centre") {
        val f = membershipFixture
        val membership = factory.createMembership.copy(userIds = Set(f.user.id))
        Set(f.user, f.study, f.centre, membership).foreach(addToRepository)

        accessService.isMember(f.user.id, Some(f.study.id), Some(f.centre.id)) mustSucceed { _ must be (false) }

        accessService.isMember(f.user.id, Some(f.study.id), None) mustSucceed { _ must be (false) }

        accessService.isMember(f.user.id, None, Some(f.centre.id)) mustSucceed { _ must be (false) }

        accessService.isMember(f.user.id, None, None) mustSucceed { _ must be (false) }
      }

      it("fails if user not found") {
        val f = membershipFixture
        val membership = factory.createMembership.copy(userIds = Set(f.user.id))
        Set(f.user, f.study, f.centre, membership).foreach(addToRepository)

        val user2 = factory.createActiveUser
        accessService.isMember(user2.id, Some(f.study.id), Some(f.centre.id))
          .mustFail(s"IdNotFound: user id: ${user2.id}")

        accessService.isMember(user2.id, Some(f.study.id), None) mustFail s"IdNotFound: user id: ${user2.id}"

        accessService.isMember(user2.id, None, Some(f.centre.id)) mustFail s"IdNotFound: user id: ${user2.id}"
      }

      it("fails if study not found") {
        val f = membershipFixture
        val membership = factory.createMembership.copy(userIds = Set(f.user.id))
        Set(f.user, f.study, f.centre, membership).foreach(addToRepository)

        val study2 = factory.createEnabledStudy
        accessService.isMember(f.user.id, Some(study2.id), Some(f.centre.id))
          .mustFail(s"IdNotFound: study id: ${study2.id}")

        accessService.isMember(f.user.id, Some(study2.id), None)
          .mustFail(s"IdNotFound: study id: ${study2.id}")
      }

      it("fails if centre not found") {
        val f = membershipFixture
        val membership = factory.createMembership.copy(userIds = Set(f.user.id))
        Set(f.user, f.study, f.centre, membership).foreach(addToRepository)

        val centre2 = factory.createEnabledCentre
        accessService.isMember(f.user.id, Some(f.study.id), Some(centre2.id))
          .mustFail(s"IdNotFound: centre id: ${centre2.id}")

        accessService.isMember(f.user.id, None, Some(centre2.id))
          .mustFail(s"IdNotFound: centre id: ${centre2.id}")
      }

    }
  }

}
