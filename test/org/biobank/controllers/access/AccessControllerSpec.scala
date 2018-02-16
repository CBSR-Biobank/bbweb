package org.biobank.controllers.access

import java.time.OffsetDateTime
import org.biobank.controllers.PagedResultsSpec
import org.biobank.domain._
import org.biobank.domain.access._
import org.biobank.domain.user._
import org.biobank.dto.EntityInfoDto
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.Inside
import play.api.libs.json._
import play.api.test.Helpers._

/**
 * Tests the roles and permissions REST API for [[User Access]].
 *
 * Tests for [[Membership]]s in AccessControllerMembershipSpec.scala.
 */
class AccessControllerSpec
    extends AccessControllerSpecCommon
    with JsonHelper
    with UserFixtures
    with Inside {
  import org.biobank.TestUtils._

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
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri       = uri("roles") + s"?limit=$limit",
            offset    = 0,
            total     = RoleId.maxId.toLong,
            maybeNext = Some(2),
            maybePrev = None)
        jsonItems must have size limit.toLong
        jsonItems
          .map { json => (json \ "id").as[String] }
          .foreach { jsonId => RoleId.withName(jsonId) }
      }

      it("list a single role filtered by name") {
        accessItemRepository.getByKey(AccessItemId(RoleId.ShippingUser.toString)) mustSucceed { role =>
          val jsonItem = PagedResultsSpec(this)
            .singleItemResult(uri("roles"), Map("filter" -> s"'name::${role.name}'"))

          (jsonItem \ "accessItemType").as[String] must be (AccessItem.roleAccessItemType.toString)

          (jsonItem \ "name").as[String] must be (role.name)

          (jsonItem \ "parentData").as[Set[EntityInfoDto]].size must be > 0

          (jsonItem \ "childData").as[Set[EntityInfoDto]].size must be > 0
        }
      }

      it("list roles when sorted by name") {
        val name = nameGenerator.next[String]
        val role = factory.createRole.copy(name = name)
        accessItemRepository.put(role)

        val jsonItems = PagedResultsSpec(this)
          .multipleItemsResult(uri         = uri("roles"),
                               queryParams = Map("sort" -> "name"),
                               offset      = 0,
                               total       = RoleId.maxId.toLong + 1,
                               maybeNext = Some(2),
                               maybePrev = None)
        val firstJsonItem = jsonItems(0)

        (firstJsonItem \ "accessItemType").as[String] must be (AccessItem.roleAccessItemType.toString)

        (firstJsonItem \ "name").as[String] must be (name)

        (firstJsonItem \ "parentData").as[Set[EntityInfoDto]].size must be (0)

        (firstJsonItem \ "childData").as[Set[EntityInfoDto]].size must be (0)
      }

      it("fail when using an invalid query parameters") {
        PagedResultsSpec(this).failWithInvalidParams(uri("roles"))
      }

    }

    describe("GET /api/access/roles/names") {
      val createEntity = (name: String) => factory.createRole.copy(name = name)
      val baseUrl = uri("roles", "names")

      it should behave like accessEntityNameSharedBehaviour(createEntity, baseUrl)
    }

    describe("GET /api/access/items/names") {
      val createEntity = (name: String) => factory.createPermission.copy(name = name)
      val baseUrl = uri("items", "names")

      it should behave like accessEntityNameSharedBehaviour(createEntity, baseUrl)
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

        val reply = makeRequest(POST, uri("roles"), json)

        (reply \ "status").as[String] must include ("success")

        val jsonId = (reply \ "data" \ "id").as[String]
        val itemId = AccessItemId(jsonId)
        jsonId.length must be > 0

        accessItemRepository.getByKey(itemId) mustSucceed { item =>
          inside(item) { case repoRole: Role =>
            compareObj((reply \ "data").as[JsObject], repoRole)
            repoRole must have (
              'id             (itemId),
              'version        (0L)
            )
            repoRole.userIds must contain (f.user.id)
            checkTimeStamps(repoRole, OffsetDateTime.now, None)
          }
        }
      }

      it("fails when adding a second role with a name that already exists") {
        val f = new RoleFixture
        val json = roleToAddJson(f.role)
        accessItemRepository.put(f.role)
        val reply = makeRequest(POST, uri("roles"), BAD_REQUEST, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex("EntityCriteriaError: name already used:")
      }

      it("attempt to create role fails if user does not exist") {
        val f = new RoleFixture
        userRepository.remove(f.user)
        val json = roleToAddJson(f.role)
        val reply = makeRequest(POST, uri("roles"), NOT_FOUND, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex("IdNotFound: user id")
      }

      it("attempt to create membership fails if parent does not exist") {
        val f = new RoleFixture
        val parentRole = factory.createRole
        val role = f.role.copy(parentIds = Set(parentRole.id))
        val json = roleToAddJson(role)
        val reply = makeRequest(POST, uri("roles"), NOT_FOUND, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex("IdNotFound: role id")
      }

      it("attempt to create membership fails if child does not exist") {
        val f = new RoleFixture
        val childRole = factory.createRole
        val role = f.role.copy(childrenIds = Set(childRole.id))
        val json = roleToAddJson(role)
        val reply = makeRequest(POST, uri("roles"), NOT_FOUND, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex("IdNotFound: access item id")
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
        val reply = makeRequest(POST, uri("roles", "name", f.role.id.id), json)

        (reply \ "status").as[String] must include ("success")

        val jsonId = (reply \ "data" \ "id").as[String]
        val itemId = AccessItemId(jsonId)
        jsonId.length must be > 0

        accessItemRepository.getByKey(itemId) mustSucceed { item =>
          inside(item) { case repoRole: Role =>
            compareObj((reply \ "data").as[JsObject], repoRole)
            repoRole must have (
              'id             (itemId),
              'version        (f.role.version + 1),
              'name           (newName)
            )
            checkTimeStamps(repoRole, OffsetDateTime.now, OffsetDateTime.now)
          }
        }
      }

      it("fails when updating to name already used by another role") {
        val f = new RoleFixture
        val role = factory.createRole
        val json = updateNameJson(f.role, role.name)

        Set(f.role, role).foreach(addToRepository)
        val reply = makeRequest(POST, uri("roles", "name", f.role.id.id), BAD_REQUEST, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex("EntityCriteriaError: name already used:")
      }

      it("fail when updating to something with less than 2 characters") {
        val f = new RoleFixture
        val json = updateNameJson(f.role, "a")

        accessItemRepository.put(f.role)
        val reply = makeRequest(POST, uri("roles", "name", f.role.id.id), BAD_REQUEST, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must startWith ("InvalidName")
      }

      it("fail when updating and role ID does not exist") {
        val f = new RoleFixture
        notFound(uri("roles", "name", f.role.id.id),
                 updateNameJson(f.role, nameGenerator.next[Role]))
      }

      it("fail when updating with invalid version") {
        val f = new RoleFixture
        val json = updateNameJson(f.role, nameGenerator.next[Role]) ++
        Json.obj("expectedVersion" -> Some(f.role.version + 10L))
        accessItemRepository.put(f.role)
        hasInvalidVersion(POST, uri("roles", "name", f.role.id.id), json)
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
          val reply = makeRequest(POST, uri("roles", "description", f.role.id.id), json)

          (reply \ "status").as[String] must include ("success")

          val jsonId = (reply \ "data" \ "id").as[String]
          val itemId = AccessItemId(jsonId)
          jsonId.length must be > 0

          accessItemRepository.getByKey(itemId) mustSucceed { item =>
            inside(item) { case repoRole: Role =>
              compareObj((reply \ "data").as[JsObject], repoRole)
              repoRole must have (
                'id             (itemId),
                'version        (f.role.version + 1),
                'description    (newDescription)
              )
              checkTimeStamps(repoRole, OffsetDateTime.now, OffsetDateTime.now)
            }
          }
        }
      }

      it("fail when updating and role ID does not exist") {
        val f = new RoleFixture
        notFound(uri("roles", "description", f.role.id.id),
                 updateDescriptionJson(f.role, Some(nameGenerator.next[Role])))
      }

      it("fail when updating with invalid version") {
        val f = new RoleFixture
        val json = updateDescriptionJson(f.role, Some(nameGenerator.next[Role])) ++
        Json.obj("expectedVersion" -> Some(f.role.version + 10L))
        accessItemRepository.put(f.role)
        hasInvalidVersion(POST, uri("roles") + s"/description/${f.role.id}", json)
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
        val reply = makeRequest(POST, uri("roles", "user", f.role.id.id), json)

        (reply \ "status").as[String] must include ("success")

        val jsonId = (reply \ "data" \ "id").as[String]
        val itemId = AccessItemId(jsonId)
        jsonId.length must be > 0

        accessItemRepository.getByKey(itemId) mustSucceed { item =>
          inside(item) { case repoRole: Role =>
            compareObj((reply \ "data").as[JsObject], repoRole)
            repoRole must have (
              'id             (itemId),
              'version        (f.role.version + 1)
            )
            repoRole.userIds must contain (user.id)
            checkTimeStamps(repoRole, OffsetDateTime.now, OffsetDateTime.now)
          }
        }
      }

      it("cannot add the same user more than once") {
        val f = new RoleFixture
        val json = addUserJson(f.role, f.user)

        accessItemRepository.put(f.role)
        val reply = makeRequest(POST, uri("roles", "user", f.role.id.id), BAD_REQUEST, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("user ID is already in role")
      }

      it("cannot add a user that does not exist") {
        val f = new RoleFixture
        val user = factory.createRegisteredUser
        val json = addUserJson(f.role, user)

        accessItemRepository.put(f.role)
        val reply = makeRequest(POST, uri("roles", "user", f.role.id.id), NOT_FOUND, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("IdNotFound: user id")
      }

      it("fail when updating and role ID does not exist") {
        val f = new RoleFixture
        val user = factory.createRegisteredUser
        val json = addUserJson(f.role, user)
        userRepository.put(user)
        notFound(uri("roles", "user", f.role.id.id), json)
      }

      it("fail when updating with invalid version") {
        val f = new RoleFixture
        val user = factory.createRegisteredUser
        val json = addUserJson(f.role, user) ++
        Json.obj("expectedVersion" -> Some(f.role.version + 10L))
        Set(f.role, user).foreach(addToRepository)
        hasInvalidVersion(POST, uri("roles", "user", f.role.id.id), json)
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
        val reply = makeRequest(POST, uri("roles", "parent", f.role.id.id), json)

        (reply \ "status").as[String] must include ("success")

        val jsonId = (reply \ "data" \ "id").as[String]
        val itemId = AccessItemId(jsonId)
        jsonId.length must be > 0

        accessItemRepository.getByKey(itemId) mustSucceed { item =>
          inside(item) { case repoRole: Role =>
            compareObj((reply \ "data").as[JsObject], repoRole)
            repoRole must have (
              'id             (itemId),
              'version        (f.role.version + 1)
            )
            repoRole.parentIds must contain (parentRole.id)
            checkTimeStamps(repoRole, OffsetDateTime.now, OffsetDateTime.now)
          }
        }
      }

      it("cannot add self as a parent role") {
        val f = new RoleFixture
        val json = addParentRoleJson(f.role, f.role)

        Set(f.role).foreach(addToRepository)
        val reply = makeRequest(POST, uri("roles", "parent", f.role.id.id), BAD_REQUEST, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("parent ID cannot be self")
      }

      it("cannot add the same parent role more than once") {
        val f = new RoleFixture
        val parentRole = factory.createRole
        val role = f.role.copy(parentIds = Set(parentRole.id))
        val json = addParentRoleJson(role, parentRole)

        Set(role, parentRole).foreach(addToRepository)
        val reply = makeRequest(POST, uri("roles", "parent", role.id.id), BAD_REQUEST, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("parent ID is already in role")
      }

      it("cannot add a parent role that does not exist") {
        val f = new RoleFixture
        val parentRole = factory.createRole
        val json = addParentRoleJson(f.role, parentRole)

        Set(f.role).foreach(addToRepository)
        val reply = makeRequest(POST, uri("roles", "parent", f.role.id.id), NOT_FOUND, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex(s"IdNotFound: access item id.*${parentRole.id.id}")
      }

      it("fail when updating and role ID does not exist") {
        val f = new RoleFixture
        val parentRole = factory.createRole
        val json = addParentRoleJson(f.role, parentRole)
        accessItemRepository.put(parentRole)
        notFound(uri("roles", "parent", f.role.id.id), json)
      }

      it("fail when updating with invalid version") {
        val f = new RoleFixture
        val parentRole = factory.createRole
        val json = addParentRoleJson(f.role, parentRole) ++
        Json.obj("expectedVersion" -> Some(f.role.version + 10L))
        Set(f.role, parentRole).foreach(addToRepository)
        hasInvalidVersion(POST, uri("roles", "parent", f.role.id.id), json)
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
          val reply = makeRequest(POST, uri("roles", "child", f.role.id.id), json)

          (reply \ "status").as[String] must include ("success")

          val jsonId = (reply \ "data" \ "id").as[String]
          val itemId = AccessItemId(jsonId)
          jsonId.length must be > 0

          accessItemRepository.getByKey(itemId) mustSucceed { item =>
            inside(item) { case repoRole: Role =>
              compareObj((reply \ "data").as[JsObject], repoRole)
              repoRole must have (
                'id             (itemId),
                'version        (f.role.version + 1)
              )
              repoRole.childrenIds must contain (child.id)
              checkTimeStamps(repoRole, OffsetDateTime.now, OffsetDateTime.now)
            }
          }
        }
      }

      it("cannot add self as a parent role") {
        val f = new RoleFixture
        val json = addChildJson(f.role, f.role)

        Set(f.role).foreach(addToRepository)
        val reply = makeRequest(POST, uri("roles", "child", f.role.id.id), BAD_REQUEST, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("child ID cannot be self")
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
          val reply = makeRequest(POST, uri("roles", "child", role.id.id), BAD_REQUEST, json)

          (reply \ "status").as[String] must include ("error")

          (reply \ "message").as[String] must include ("child ID is already in role")
        }
      }

      it("cannot add a child role that does not exist") {
        val f = new RoleFixture
        val child = factory.createRole
        val json = addChildJson(f.role, child)

        Set(f.role).foreach(addToRepository)
        val reply = makeRequest(POST, uri("roles", "child", f.role.id.id), NOT_FOUND, json)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex(s"IdNotFound: access item id.*${child.id.id}")
      }

      it("fail when updating and role ID does not exist") {
        val f = new RoleFixture
        val child = factory.createRole
        val json = addChildJson(f.role, child)
        accessItemRepository.put(child)
        notFound(uri("roles", "child", f.role.id.id), json)
      }

      it("fail when updating with invalid version") {
        val f = new RoleFixture
        val child = factory.createRole
        val json = addChildJson(f.role, child) ++
        Json.obj("expectedVersion" -> Some(f.role.version + 10L))
        Set(f.role, child).foreach(addToRepository)
        hasInvalidVersion(POST, uri("roles", "child", f.role.id.id), json)
      }

    }

  }

  describe("DELETE /api/access/roles/user/:roleId/:version/:userId") {

    it("can remove a user") {
      val f = new RoleFixture
      val url = uri("roles", "user", f.role.id.id, f.role.version.toString, f.user.id.id)

      addToRepository(f.role)
      val reply = makeRequest(DELETE, url)

      (reply \ "status").as[String] must include ("success")

      val jsonId = (reply \ "data" \ "id").as[String]
      val itemId = AccessItemId(jsonId)
      jsonId.length must be > 0

      accessItemRepository.getByKey(itemId) mustSucceed { item =>
        inside(item) { case repoRole: Role =>
          compareObj((reply \ "data").as[JsObject], repoRole)
          repoRole must have (
            'id             (itemId),
            'version        (f.role.version + 1)
          )
          repoRole.userIds must not contain (f.user.id)
          checkTimeStamps(repoRole, f.role.timeAdded, OffsetDateTime.now)
        }
      }
    }

    it("cannot remove a user not in the role") {
      val f = new RoleFixture
      val user = factory.createRegisteredUser
      Set(f.role, user).map(addToRepository)
      val url = uri("roles", "user", f.role.id.id, f.role.version.toString, user.id.id)
      val reply = makeRequest(DELETE, url, BAD_REQUEST)

      (reply \ "status").as[String] must include ("error")

      (reply \ "message").as[String] must include ("user ID is not in role")
    }

    it("cannot remove a user that does not exist") {
      val f = new RoleFixture
      val user = factory.createRegisteredUser
      Set(f.role).map(addToRepository)
      val url = uri("roles", "user", f.role.id.id, f.role.version.toString, user.id.id)
      val reply = makeRequest(DELETE, url, NOT_FOUND)

      (reply \ "status").as[String] must include ("error")

      (reply \ "message").as[String] must include ("IdNotFound: user id")
    }

    it("fail when removing and role ID does not exist") {
      val f = new RoleFixture
      val user = factory.createRegisteredUser
      val url = uri("roles", "user", f.role.id.id, f.role.version.toString, user.id.id)
      Set(user).map(addToRepository)
      notFoundOnDelete(url)
    }

    it("fail when removing with invalid version") {
      val f = new RoleFixture
      val user = factory.createRegisteredUser
      val url = uri("roles", "user", f.role.id.id, (f.role.version + 10L).toString, user.id.id)
      Set(f.role, user).map(addToRepository)
      hasInvalidVersion(DELETE, url)
    }

  }

  describe("DELETE /api/access/roles/parent/:roleId/:version/:parentId") {

    it("can remove a parent role") {
      val f = new RoleFixture
      val parentRole = factory.createRole
      val role = f.role.copy(parentIds = Set(parentRole.id))
      val url = uri("roles", "parent", role.id.id, role.version.toString, parentRole.id.id)

      Set(role, parentRole).foreach(addToRepository)
      val reply = makeRequest(DELETE, url)

      (reply \ "status").as[String] must include ("success")

      val jsonId = (reply \ "data" \ "id").as[String]
      val itemId = AccessItemId(jsonId)
      jsonId.length must be > 0

      accessItemRepository.getByKey(itemId) mustSucceed { item =>
        inside(item) { case repoRole: Role =>
          compareObj((reply \ "data").as[JsObject], repoRole)
          repoRole must have (
            'id             (itemId),
            'version        (role.version + 1)
          )
          repoRole.parentIds must not contain (parentRole.id)
          checkTimeStamps(repoRole, role.timeAdded, OffsetDateTime.now)
        }
      }
    }

    it("cannot remove a parent role not in the role") {
      val f = new RoleFixture
      val parentRole = factory.createRole
      Set(f.role, parentRole).map(addToRepository)
      val url = uri("roles", "parent", f.role.id.id, f.role.version.toString, parentRole.id.id)
      val reply = makeRequest(DELETE, url, BAD_REQUEST)

      (reply \ "status").as[String] must include ("error")

      (reply \ "message").as[String] must include ("parent ID not in role")
    }

    it("cannot remove a parent role that does not exist") {
      val f = new RoleFixture
      val parentRole = factory.createRole
      Set(f.role).map(addToRepository)
      val url = uri("roles", "parent", f.role.id.id, f.role.version.toString, parentRole.id.id)
      val reply = makeRequest(DELETE, url, NOT_FOUND)

      (reply \ "status").as[String] must include ("error")

      (reply \ "message").as[String] must include ("IdNotFound: access item id")
    }

    it("fail when removing and role ID does not exist") {
      val f = new RoleFixture
      val parentRole = factory.createRole
      val url = uri("roles", "parent", f.role.id.id, f.role.version.toString, parentRole.id.id)
      Set(parentRole).map(addToRepository)
      notFoundOnDelete(url)
    }

    it("fail when removing with invalid version") {
      val f = new RoleFixture
      val parentRole = factory.createRole
      val url = uri("roles", "parent", f.role.id.id, (f.role.version + 10L).toString, parentRole.id.id)
      Set(f.role, parentRole).map(addToRepository)
      hasInvalidVersion(DELETE, url)
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
        val reply = makeRequest(DELETE, url)

        (reply \ "status").as[String] must include ("success")

        val jsonId = (reply \ "data" \ "id").as[String]
        val itemId = AccessItemId(jsonId)
        jsonId.length must be > 0

        accessItemRepository.getByKey(itemId) mustSucceed { item =>
          inside(item) { case repoRole: Role =>
            compareObj((reply \ "data").as[JsObject], repoRole)
            repoRole must have (
              'id             (itemId),
              'version        (role.version + 1)
            )
            repoRole.childrenIds must not contain (child.id)
            checkTimeStamps(repoRole, role.timeAdded, OffsetDateTime.now)
          }
        }
      }
    }
  }

  describe("DELETE /api/access/roles/:roleId/:version") {

    it("can remove a role") {
      val f = new RoleFixture
      val url = uri("roles", f.role.id.id, f.role.version.toString)

      accessItemRepository.put(f.role)
      val reply = makeRequest(DELETE, url)

      (reply \ "status").as[String] must include ("success")

      (reply \ "data").as[Boolean] must be (true)

      accessItemRepository.getByKey(f.role.id) mustFail ("IdNotFound: access item id.*")
    }

    it("cannot remove a role that does not exist") {
      val f = new RoleFixture
      val url = uri("roles", f.role.id.id, f.role.version.toString)
      notFoundOnDelete(url)
    }

    it("fail when removing with invalid version") {
      val f = new RoleFixture
      val url = uri("roles", f.role.id.id, (f.role.version + 10L).toString)
      accessItemRepository.put(f.role)
      hasInvalidVersion(DELETE, url)
    }

  }

  private def notFound(url: String, json: JsValue): Unit = {
    notFound(POST, url, json, "IdNotFound: role id")
  }

  private def notFoundOnDelete(url: String): Unit = {
    notFound(DELETE, url, JsNull, "IdNotFound: role id")
  }

}
