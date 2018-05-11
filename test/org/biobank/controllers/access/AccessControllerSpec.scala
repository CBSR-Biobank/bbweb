package org.biobank.controllers.access

import java.time.OffsetDateTime
import org.biobank.controllers.PagedResultsSharedSpec
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.users._
import org.biobank.fixtures.Url
import org.biobank.matchers.PagedResultsMatchers
import org.biobank.dto._
import org.biobank.dto.access._
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.Inside
import play.api.mvc._
import play.api.libs.json._
import play.api.test.Helpers._
import scala.concurrent.Future

/**
 * Tests the roles and permissions REST API for [[User Access]].
 *
 * Tests for [[Membership]]s in AccessControllerMembershipSpec.scala.
 */
class AccessControllerSpec
    extends AccessControllerSpecCommon
    with UserFixtures
    with PagedResultsSharedSpec
    with PagedResultsMatchers
    with Inside {
  import org.biobank.TestUtils._
  import org.biobank.matchers.DtoMatchers._
  import org.biobank.matchers.EntityMatchers._
  import org.biobank.matchers.JsonMatchers._

  class ActiveUserFixture {
    val user = factory.createActiveUser
    userRepository.put(user)
    addMembershipForUser(user)
  }

  class RoleFixture {
    val user = factory.createActiveUser
    val role = factory.createRole.copy(userIds = Set(user.id))
    Set(user).foreach(addToRepository)
  }

  private def addMembershipForUser(user: User) = {
    val membership = factory.createMembership.copy(userIds = Set(user.id))
    membershipRepository.put(membership)
  }

  describe("Access REST API (roles and permissions)") {

    describe("GET /api/access/roles") {

      it("list the first page of roles") {
        val limit = 5
        val reply = makeAuthRequest(GET, uri("roles") + s"?limit=$limit").value
        reply must beOkResponseWithJsonReply

        val json = contentAsJson(reply)
        json must beMultipleItemResults(offset    = 0,
                                        total     = RoleId.maxId.toLong,
                                        maybeNext = Some(2),
                                        maybePrev = None)

        val dtos = (json \ "data" \ "items").validate[List[RoleDto]]
        dtos must be (jsSuccess)
      }

      describe("list a single role filtered by name") {
        listSingleRole() { () =>
          val role = accessItemRepository.getRole(AccessItemId(RoleId.ShippingUser.toString))
            .toOption.value

          (new Url(uri("roles") + s"?filter='name::${role.name}'"), role)
        }
      }

      it("list roles when sorted by name") {
        val name = nameGenerator.next[String]
        val role = factory.createRole.copy(name = name)
        accessItemRepository.put(role)

        val reply = makeAuthRequest(GET, uri("roles") + s"?sort=name").value
        reply must beOkResponseWithJsonReply

        val json = contentAsJson(reply)
        json must beMultipleItemResults(offset    = 0,
                                        total     = RoleId.maxId.toLong + 1,
                                        maybeNext = Some(2),
                                        maybePrev = None)

        val dtos = (json \ "data" \ "items").validate[List[RoleDto]]
        dtos must be (jsSuccess)
        dtos.get(0) must matchDtoToRole (role)
      }

      describe("fail when using an invalid query parameters") {
        pagedQueryShouldFailSharedBehaviour(() => new Url(uri("roles")))
      }
    }

    describe("GET /api/access/roles/names") {
      val createEntity = (name: String) => factory.createRole.copy(name = name)
      val baseUrl = uri("roles", "names")
      it must behave like accessEntityNameSharedBehaviour(createEntity, baseUrl) {
        (dtos: List[EntityInfoDto], roles: List[Role]) =>
        (dtos zip roles).foreach { case (dto, role) =>
          dto must matchEntityInfoDtoToRole(role)
        }
      }
    }

    describe("GET /api/access/items/names") {
      val createEntity = (name: String) => factory.createPermission.copy(name = name)
      val baseUrl = uri("items", "names")
      it should behave like accessEntityNameSharedBehaviour(createEntity, baseUrl) {
          (replyAccessItemNames: List[AccessItemNameDto], accessItems: List[AccessItem]) =>
          (replyAccessItemNames zip accessItems).foreach { case (replyAccessItemName, accessItem) =>
            replyAccessItemName must matchDtoToAccessItem(accessItem)
          }
        }
    }

    describe("POST /api/access/roles") {

      def roleToAddJson(role: Role): JsValue =
        Json.obj("name"        -> role.name,
                 "description" -> role.description,
                 "userIds"     -> role.userIds.map(_.toString),
                 "parentIds"   -> role.parentIds.map(_.toString),
                 "childrenIds" -> role.childrenIds.map(_.toString))


      it("can create a role") {
        val f = new RoleFixture
        val json = roleToAddJson(f.role)

        val reply = makeAuthRequest(POST, uri("roles"), json).value
        reply must beOkResponseWithJsonReply

        val jsonId = (contentAsJson(reply) \ "data" \ "id").validate[AccessItemId]
        jsonId must be (jsSuccess)

        val updatedRole = f.role.copy(id = jsonId.get,
                                      timeAdded = OffsetDateTime.now)
        reply must matchUpdatedRole (updatedRole)
      }

      it("fails when adding a second role with a name that already exists") {
        val f = new RoleFixture
        val json = roleToAddJson(f.role)
        accessItemRepository.put(f.role)
        val reply = makeAuthRequest(POST, uri("roles"), json).value
        reply must beBadRequestWithMessage("EntityCriteriaError: name already used:")
      }

      it("attempt to create role fails if user does not exist") {
        val f = new RoleFixture
        userRepository.remove(f.user)
        val json = roleToAddJson(f.role)
        val reply = makeAuthRequest(POST, uri("roles"), json).value
        reply must beNotFoundWithMessage("IdNotFound: user id")
      }

      it("attempt to create membership fails if parent does not exist") {
        val f = new RoleFixture
        val parentRole = factory.createRole
        val role = f.role.copy(parentIds = Set(parentRole.id))
        val json = roleToAddJson(role)
        val reply = makeAuthRequest(POST, uri("roles"), json).value
        reply must beNotFoundWithMessage("IdNotFound: role id")
      }

      it("attempt to create membership fails if child does not exist") {
        val f = new RoleFixture
        val childRole = factory.createRole
        val role = f.role.copy(childrenIds = Set(childRole.id))
        val json = roleToAddJson(role)
        val reply = makeAuthRequest(POST, uri("roles"), json).value
        reply must beNotFoundWithMessage("IdNotFound: access item id")
      }

    }

    describe("POST /api/access/roles/name/:roleId") {

      def updateNameJson(role: Role, name: String) = {
        Json.obj("expectedVersion" -> role.version, "name" -> name)
      }

      it("can update the name") {
        val f = new RoleFixture
        val newName = nameGenerator.next[String]
        val json = updateNameJson(f.role, newName)

        accessItemRepository.put(f.role)
        val reply = makeAuthRequest(POST, uri("roles", "name", f.role.id.id), json).value
        reply must beOkResponseWithJsonReply

        val updatedRole = f.role.copy(version      = f.role.version + 1,
                                      slug         = Slug(newName),
                                      name         = newName,
                                      timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedRole (updatedRole)
      }

      it("fails when updating to name already used by another role") {
        val f = new RoleFixture
        val role = factory.createRole
        val json = updateNameJson(f.role, role.name)

        Set(f.role, role).foreach(addToRepository)
        val reply = makeAuthRequest(POST, uri("roles", "name", f.role.id.id), json).value
        reply must beBadRequestWithMessage("EntityCriteriaError: name already used:")
      }

      it("fail when updating to something with less than 2 characters") {
        val f = new RoleFixture
        val json = updateNameJson(f.role, "a")

        accessItemRepository.put(f.role)
        val reply = makeAuthRequest(POST, uri("roles", "name", f.role.id.id), json).value
        reply must beBadRequestWithMessage("InvalidName")
      }

      it("fail when updating and role ID does not exist") {
        val f = new RoleFixture
        val reply = makeAuthRequest(POST,
                                    uri("roles", "name", f.role.id.id),
                                    updateNameJson(f.role, nameGenerator.next[Role]))
        reply.value must beNotFoundWithMessage("IdNotFound: role id")
      }

      it("fail when updating with invalid version") {
        val f = new RoleFixture
        val json = updateNameJson(f.role, nameGenerator.next[Role]) ++
        Json.obj("expectedVersion" -> Some(f.role.version + 10L))
        accessItemRepository.put(f.role)
        val reply = makeAuthRequest(POST, uri("roles", "name", f.role.id.id), json).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

    }

    describe("POST /api/access/roles/description/:roleId") {

      def updateDescriptionJson(role: Role, description: Option[String]) = {
        Json.obj("expectedVersion" -> role.version) ++
        JsObject(
          Seq[(String, JsValue)]() ++
            description.map("description" -> Json.toJson(_)))
      }

      it("update a description") {
        val descriptionValues = Table("descriptions", Some(nameGenerator.next[String]), None)
        forAll(descriptionValues) { newDescription =>
          val f = new RoleFixture
          val json = updateDescriptionJson(f.role, newDescription)

          accessItemRepository.put(f.role)
          val reply = makeAuthRequest(POST, uri("roles", "description", f.role.id.id), json).value
          reply must beOkResponseWithJsonReply

          val updatedRole = f.role.copy(version      = f.role.version + 1,
                                        description  = newDescription,
                                        timeModified = Some(OffsetDateTime.now))
          reply must matchUpdatedRole (updatedRole)
        }
      }

      it("fail when updating and role ID does not exist") {
        val f = new RoleFixture
        val reply = makeAuthRequest(POST,
                                    uri("roles", "description", f.role.id.id),
                                    updateDescriptionJson(f.role, Some(nameGenerator.next[Role])))
        reply.value must beNotFoundWithMessage("IdNotFound: role id")
      }

      it("fail when updating with invalid version") {
        val f = new RoleFixture
        val json = updateDescriptionJson(f.role, Some(nameGenerator.next[Role])) ++
        Json.obj("expectedVersion" -> Some(f.role.version + 10L))
        accessItemRepository.put(f.role)
        val reply = makeAuthRequest(POST, uri("roles") + s"/description/${f.role.id}", json).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

    }

    describe("POST /api/access/roles/user/:roleId") {

      def addUserJson(role: Role, user: User) = {
        Json.obj("userId" -> user.id.id, "expectedVersion" -> role.version)
      }

      it("can add a user") {
        val f = new RoleFixture
        val user = factory.createRegisteredUser
        val json = addUserJson(f.role, user)

        Set(f.role, user).foreach(addToRepository)
        val reply = makeAuthRequest(POST, uri("roles", "user", f.role.id.id), json).value
        reply must beOkResponseWithJsonReply

        val updatedRole = f.role.copy(version      = f.role.version + 1,
                                      userIds      = f.role.userIds + user.id,
                                      timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedRole (updatedRole)
      }

      it("cannot add the same user more than once") {
        val f = new RoleFixture
        val json = addUserJson(f.role, f.user)

        accessItemRepository.put(f.role)
        val reply = makeAuthRequest(POST, uri("roles", "user", f.role.id.id), json).value
        reply must beBadRequestWithMessage("user ID is already in role")
      }

      it("cannot add a user that does not exist") {
        val f = new RoleFixture
        val user = factory.createRegisteredUser
        val json = addUserJson(f.role, user)

        accessItemRepository.put(f.role)
        val reply = makeAuthRequest(POST, uri("roles", "user", f.role.id.id), json).value
        reply must beNotFoundWithMessage("IdNotFound: user id")
      }

      it("fail when updating and role ID does not exist") {
        val f = new RoleFixture
        val user = factory.createRegisteredUser
        val json = addUserJson(f.role, user)
        userRepository.put(user)
        val reply = makeAuthRequest(POST, uri("roles", "user", f.role.id.id), json).value
        reply must beNotFoundWithMessage("IdNotFound: role id")

      }

      it("fail when updating with invalid version") {
        val f = new RoleFixture
        val user = factory.createRegisteredUser
        val json = addUserJson(f.role, user) ++
        Json.obj("expectedVersion" -> Some(f.role.version + 10L))
        Set(f.role, user).foreach(addToRepository)
        val reply = makeAuthRequest(POST, uri("roles", "user", f.role.id.id), json).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

    }

    describe("POST /api/access/roles/parent/:roleId") {

      def addParentRoleJson(role: Role, parentRole: Role) = {
        Json.obj("parentRoleId" -> parentRole.id.id, "expectedVersion" -> role.version)
      }

      it("can add a parent role") {
        val f = new RoleFixture
        val parentRole = factory.createRole
        val json = addParentRoleJson(f.role, parentRole)

        Set(f.role, parentRole).foreach(addToRepository)
        val reply = makeAuthRequest(POST, uri("roles", "parent", f.role.id.id), json).value
        reply must beOkResponseWithJsonReply

        val updatedRole = f.role.copy(version      = f.role.version + 1,
                                      parentIds    = f.role.parentIds + parentRole.id,
                                      timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedRole (updatedRole)
      }

      it("cannot add self as a parent role") {
        val f = new RoleFixture
        val json = addParentRoleJson(f.role, f.role)

        Set(f.role).foreach(addToRepository)
        val reply = makeAuthRequest(POST, uri("roles", "parent", f.role.id.id), json).value
        reply must beBadRequestWithMessage("parent ID cannot be self")
      }

      it("cannot add the same parent role more than once") {
        val f = new RoleFixture
        val parentRole = factory.createRole
        val role = f.role.copy(parentIds = Set(parentRole.id))
        val json = addParentRoleJson(role, parentRole)

        Set(role, parentRole).foreach(addToRepository)
        val reply = makeAuthRequest(POST, uri("roles", "parent", role.id.id), json).value
        reply must beBadRequestWithMessage("parent ID is already in role")
      }

      it("cannot add a parent role that does not exist") {
        val f = new RoleFixture
        val parentRole = factory.createRole
        val json = addParentRoleJson(f.role, parentRole)

        Set(f.role).foreach(addToRepository)
        val reply = makeAuthRequest(POST, uri("roles", "parent", f.role.id.id), json).value
        reply must beNotFoundWithMessage(s"IdNotFound: access item id.*${parentRole.id.id}")
      }

      it("fail when updating and role ID does not exist") {
        val f = new RoleFixture
        val parentRole = factory.createRole
        val json = addParentRoleJson(f.role, parentRole)
        accessItemRepository.put(parentRole)
        val reply = makeAuthRequest(POST,
                                    uri("roles", "parent", f.role.id.id),
                                    json)
        reply.value must beNotFoundWithMessage("IdNotFound: role id")
      }

      it("fail when updating with invalid version") {
        val f = new RoleFixture
        val parentRole = factory.createRole
        val json = addParentRoleJson(f.role, parentRole) ++
        Json.obj("expectedVersion" -> Some(f.role.version + 10L))
        Set(f.role, parentRole).foreach(addToRepository)
        val reply = makeAuthRequest(POST, uri("roles", "parent", f.role.id.id), json).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

    }

    describe("POST /api/access/roles/child/:roleId") {

      def addChildJson(role: Role, child: AccessItem) = {
        Json.obj("childRoleId" -> child.id.id, "expectedVersion" -> role.version)
      }

      it("can add a child role or permission") {
        val f = new RoleFixture
        val children = Table("possible children",
                             factory.createRole,
                             factory.createPermission)
        forAll(children) { child =>
          info(s"child type: ${child.accessItemType.id}")
          val json = addChildJson(f.role, child)

          Set(f.role, child).foreach(addToRepository)
          val reply = makeAuthRequest(POST, uri("roles", "child", f.role.id.id), json).value
          reply must beOkResponseWithJsonReply

          val updatedRole = f.role.copy(version      = f.role.version + 1,
                                        childrenIds  = f.role.childrenIds + child.id,
                                        timeModified = Some(OffsetDateTime.now))
          reply must matchUpdatedRole (updatedRole)
        }
      }

      it("cannot add self as a parent role") {
        val f = new RoleFixture
        val json = addChildJson(f.role, f.role)

        Set(f.role).foreach(addToRepository)
        val reply = makeAuthRequest(POST, uri("roles", "child", f.role.id.id), json).value
        reply must beBadRequestWithMessage("child ID cannot be self")
      }

      it("cannot add the same child role more than once") {
        val f = new RoleFixture
        val children = Table("possible children",
                             factory.createRole,
                             factory.createPermission)
        forAll(children) { child =>
          info(s"child type: ${child.accessItemType.id}")
          val role = f.role.copy(childrenIds = Set(child.id))
          val json = addChildJson(role, child)

          Set(role, child).foreach(addToRepository)
          val reply = makeAuthRequest(POST, uri("roles", "child", role.id.id), json).value
          reply must beBadRequestWithMessage("child ID is already in role")
        }
      }

      it("cannot add a child role that does not exist") {
        val f = new RoleFixture
        val child = factory.createRole
        val json = addChildJson(f.role, child)

        Set(f.role).foreach(addToRepository)
        val reply = makeAuthRequest(POST, uri("roles", "child", f.role.id.id), json).value
        reply must beNotFoundWithMessage(s"IdNotFound: access item id.*${child.id.id}")
      }

      it("fail when updating and role ID does not exist") {
        val f = new RoleFixture
        val child = factory.createRole
        val json = addChildJson(f.role, child)
        accessItemRepository.put(child)
        val reply = makeAuthRequest(POST, uri("roles", "child", f.role.id.id), json).value
        reply must beNotFoundWithMessage("IdNotFound: role id")
      }

      it("fail when updating with invalid version") {
        val f = new RoleFixture
        val child = factory.createRole
        val json = addChildJson(f.role, child) ++
        Json.obj("expectedVersion" -> Some(f.role.version + 10L))
        Set(f.role, child).foreach(addToRepository)
        val reply = makeAuthRequest(POST, uri("roles", "child", f.role.id.id), json).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

    }

  }

  describe("DELETE /api/access/roles/user/:roleId/:version/:userId") {

    it("can remove a user") {
      val f = new RoleFixture
      val url = uri("roles", "user", f.role.id.id, f.role.version.toString, f.user.id.id)

      addToRepository(f.role)
      val reply = makeAuthRequest(DELETE, url).value
      reply must beOkResponseWithJsonReply

      val updatedRole = f.role.copy(version      = f.role.version + 1,
                                    userIds      = f.role.userIds - f.user.id,
                                    timeModified = Some(OffsetDateTime.now))
      reply must matchUpdatedRole (updatedRole)
    }

    it("cannot remove a user not in the role") {
      val f = new RoleFixture
      val user = factory.createRegisteredUser
      Set(f.role, user).map(addToRepository)
      val url = uri("roles", "user", f.role.id.id, f.role.version.toString, user.id.id)
      val reply = makeAuthRequest(DELETE, url).value
      reply must beBadRequestWithMessage("user ID is not in role")
    }

    it("cannot remove a user that does not exist") {
      val f = new RoleFixture
      val user = factory.createRegisteredUser
      Set(f.role).map(addToRepository)
      val url = uri("roles", "user", f.role.id.id, f.role.version.toString, user.id.id)
      val reply = makeAuthRequest(DELETE, url).value
      reply must beNotFoundWithMessage ("IdNotFound: user id")
    }

    it("fail when removing and role ID does not exist") {
      val f = new RoleFixture
      val user = factory.createRegisteredUser
      val url = uri("roles", "user", f.role.id.id, f.role.version.toString, user.id.id)
      Set(user).map(addToRepository)

      val reply = makeAuthRequest(DELETE, url).value
      reply must beNotFoundWithMessage("IdNotFound: role id")
    }

    it("fail when removing with invalid version") {
      val f = new RoleFixture
      val user = factory.createRegisteredUser
      val url = uri("roles", "user", f.role.id.id, (f.role.version + 10L).toString, user.id.id)
      Set(f.role, user).map(addToRepository)
      val reply = makeAuthRequest(DELETE, url).value
      reply must beBadRequestWithMessage("expected version doesn't match current version")
    }

  }

  describe("DELETE /api/access/roles/parent/:roleId/:version/:parentId") {

    it("can remove a parent role") {
      val f = new RoleFixture
      val parentRole = factory.createRole
      val role = f.role.copy(parentIds = Set(parentRole.id))
      val url = uri("roles", "parent", role.id.id, role.version.toString, parentRole.id.id)

      Set(role, parentRole).foreach(addToRepository)
      val reply = makeAuthRequest(DELETE, url).value
      reply must be

      val updatedRole = f.role.copy(version      = f.role.version + 1,
                                    parentIds    = f.role.parentIds - parentRole.id,
                                    timeModified = Some(OffsetDateTime.now))
      reply must matchUpdatedRole (updatedRole)
    }

    it("cannot remove a parent role not in the role") {
      val f = new RoleFixture
      val parentRole = factory.createRole
      Set(f.role, parentRole).map(addToRepository)
      val url = uri("roles", "parent", f.role.id.id, f.role.version.toString, parentRole.id.id)
      val reply = makeAuthRequest(DELETE, url).value
      reply must beBadRequestWithMessage("parent ID not in role")
    }

    it("cannot remove a parent role that does not exist") {
      val f = new RoleFixture
      val parentRole = factory.createRole
      Set(f.role).map(addToRepository)
      val url = uri("roles", "parent", f.role.id.id, f.role.version.toString, parentRole.id.id)
      val reply = makeAuthRequest(DELETE, url).value
      reply must beNotFoundWithMessage("IdNotFound: access item id")
    }

    it("fail when removing and role ID does not exist") {
      val f = new RoleFixture
      val parentRole = factory.createRole
      val url = uri("roles", "parent", f.role.id.id, f.role.version.toString, parentRole.id.id)
      Set(parentRole).map(addToRepository)
      val reply = makeAuthRequest(DELETE, url).value
      reply must beNotFoundWithMessage("IdNotFound: role id")
    }

    it("fail when removing with invalid version") {
      val f = new RoleFixture
      val parentRole = factory.createRole
      val url = uri("roles", "parent", f.role.id.id, (f.role.version + 10L).toString, parentRole.id.id)
      Set(f.role, parentRole).map(addToRepository)
      val reply = makeAuthRequest(DELETE, url).value
      reply must beBadRequestWithMessage("expected version doesn't match current version")
    }

  }

  describe("DELETE /api/access/roles/child/:roleId/:version/:childId") {

    it("can remove a child role") {
      val f = new RoleFixture
      val children = Table("possible children",
                           factory.createRole,
                           factory.createPermission)
      forAll(children) { child =>
        val role = f.role.copy(childrenIds = Set(child.id))
        val url = uri("roles", "child", role.id.id, role.version.toString, child.id.id)

        Set(role, child).foreach(addToRepository)
        val reply = makeAuthRequest(DELETE, url).value

        val updatedRole = f.role.copy(version      = f.role.version + 1,
                                      childrenIds  = f.role.childrenIds - child.id,
                                      timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedRole (updatedRole)
      }
    }
  }

  describe("DELETE /api/access/roles/:roleId/:version") {

    it("can remove a role") {
      val f = new RoleFixture
      val url = uri("roles", f.role.id.id, f.role.version.toString)

      accessItemRepository.put(f.role)
      val reply = makeAuthRequest(DELETE, url).value
      reply must beOkResponseWithJsonReply

      val result = (contentAsJson(reply) \ "data").validate[Boolean]
      result must be (jsSuccess)

      accessItemRepository.getByKey(f.role.id) mustFail ("IdNotFound: access item id.*")
    }

    it("cannot remove a role that does not exist") {
      val f = new RoleFixture
      val url = uri("roles", f.role.id.id, f.role.version.toString)
      val reply = makeAuthRequest(DELETE, url).value
      reply must beNotFoundWithMessage("IdNotFound: role id")
    }

    it("fail when removing with invalid version") {
      val f = new RoleFixture
      val url = uri("roles", f.role.id.id, (f.role.version + 10L).toString)
      accessItemRepository.put(f.role)
      val reply = makeAuthRequest(DELETE, url).value
      reply must beBadRequestWithMessage("expected version doesn't match current version")
    }

  }

  private def listSingleRole(offset:    Long = 0,
                             maybeNext:    Option[Int] = None,
                             maybePrev: Option[Int] = None)
                            (setupFunc: () => (Url, Role)) = {

    it("list single role") {
      val (url, expectedRole) = setupFunc()
      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beSingleItemResults(offset, maybeNext, maybePrev)

      val replyDtos = (json \ "data" \ "items").validate[List[RoleDto]]
      replyDtos must be (jsSuccess)
      replyDtos.get.foreach { _ must matchDtoToRole (expectedRole) }
    }
  }

  // matches the updated role against the DTO returned by the server and the role in the repository
  private def matchUpdatedRole(role: Role) =
    new Matcher[Future[Result]] {
      def apply (left: Future[Result]) = {
        val dto = (contentAsJson(left) \ "data").validate[RoleDto]
        val jsSuccessMatcher = jsSuccess(dto)

        if (!jsSuccessMatcher.matches) {
          jsSuccessMatcher
        } else {
          val replyMatcher = matchDtoToRole(role)(dto.get)

          if (!replyMatcher.matches) {
            MatchResult(false,
                        s"reply does not match expected: ${replyMatcher.failureMessage}",
                        s"reply matches expected: ${replyMatcher.failureMessage}")
          } else {
            matchRepositoryRole(role)
          }
        }
      }
    }

  private def matchRepositoryRole =
    new Matcher[Role] {
      def apply (left: Role) = {
        accessItemRepository.getRole(left.id).fold(
          err => {
            MatchResult(false, s"not found in repository: ${err.head}", "")

          },
          repoCet => {
            val repoMatcher = matchRole(repoCet)(left)
            MatchResult(repoMatcher.matches,
                        s"repository role does not match expected: ${repoMatcher.failureMessage}",
                        s"repository role matches expected: ${repoMatcher.failureMessage}")
          }
        )
      }
    }

}
