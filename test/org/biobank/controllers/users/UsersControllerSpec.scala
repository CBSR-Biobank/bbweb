package org.biobank.controllers.users

import java.time.OffsetDateTime
import org.biobank.Global
import org.biobank.controllers.PagedResultsSpec
import org.biobank.dto.NameAndStateDto
import org.biobank.domain.access.{AccessItemId, Role}
import org.biobank.domain.{JsonHelper, Slug}
import org.biobank.domain.user._
import org.biobank.fixture.ControllerFixture
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.Inside
import play.api.libs.json._
import play.api.mvc.{Cookie, Cookies}
import play.api.test.Helpers._
import play.api.test._

/**
 * Tests the REST API for [[User]].
 */
class UsersControllerSpec extends ControllerFixture with JsonHelper with UserFixtures with Inside {
  import org.biobank.TestUtils._

  class activeUserFixture {
    val user = factory.createActiveUser
    userRepository.put(user)
    addMembershipForUser(user)
  }

  describe("Users REST API") {

    describe("GET /api/users/search") {

      it("lists the default user") {
        val jsonItem = PagedResultsSpec(this).singleItemResult(uri("search"))

        (jsonItem \ "id").as[String] must be (Global.DefaultUserId.id)
      }

      it("list multiple users") {
        val users = (0 until 2).map(_ => factory.createRegisteredUser).toList
        users.foreach(userRepository.put)

        val jsonItems = multipleItemsResultWithDefaultUser(uri       = uri("search"),
                                                           offset    = 0,
                                                           total     = users.size.toLong,
                                                           maybeNext = None,
                                                           maybePrev = None)
        jsonItems must have size users.size.toLong
        compareObjs(jsonItems, users)
      }

      it("list a single user when filtered by name") {
        val users = List(factory.createRegisteredUser.copy(name = "user1"),
                         factory.createRegisteredUser.copy(name = "user2"))
        val user = users(0)
        users.foreach(userRepository.put)
        val jsonItem = PagedResultsSpec(this).singleItemResult(uri("search"),
                                                               Map("filter" -> s"name::${user.name}"))
        compareObj(jsonItem, users(0))
      }

      it("list a single user when filtered by email") {
        val users = List(factory.createRegisteredUser.copy(email = "user1@test.com"),
                         factory.createRegisteredUser.copy(email = "user2@test.com"))
        val user = users(0)
        users.foreach(userRepository.put)

        val jsonItem = PagedResultsSpec(this).singleItemResult(uri("search"),
                                                               Map("filter" -> s"email::${user.email}"))
        compareObj(jsonItem, users(0))
      }

      it("list a single registered user when filtered by state") {
        val users = List(factory.createRegisteredUser.copy(email = "user1@test.com"),
                         factory.createActiveUser.copy(email = "user2@test.com"),
                         factory.createActiveUser.copy(email = "user3@test.com"))
        users.foreach(userRepository.put)

        val jsonItem = PagedResultsSpec(this).singleItemResult(uri("search"),
                                                               Map("filter" -> "state::registered"))
        compareObj(jsonItem, users(0))
      }

      it("list active users when filtered by state") {
        val users = List(factory.createRegisteredUser.copy(email = "user1@test.com"),
                         factory.createActiveUser.copy(email = "user2@test.com"),
                         factory.createActiveUser.copy(email = "user3@test.com"))
        users.foreach(userRepository.put)

        val expectedUsers = List(users(1), users(2))
        val jsonItems = multipleItemsResultWithDefaultUser(uri = uri("search"),
                                                           queryParams = Map("filter" -> "state::active"),
                                                           offset = 0,
                                                           total = expectedUsers.size.toLong,
                                                           maybeNext = None,
                                                           maybePrev = None)

        jsonItems must have size expectedUsers.size.toLong
        compareObjs(jsonItems, expectedUsers)
      }

      it("list locked users when filtered by state") {
        val users = List(factory.createActiveUser.copy(email = "user1@test.com"),
                         factory.createLockedUser.copy(email = "user2@test.com"),
                         factory.createLockedUser.copy(email = "user3@test.com"))
        users.foreach(userRepository.put)

        val expectedUsers = List(users(1), users(2))
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri = uri("search"),
            queryParams = Map("filter" -> "state::locked"),
            offset = 0,
            total = expectedUsers.size.toLong,
            maybeNext = None,
            maybePrev = None)

        jsonItems must have size expectedUsers.size.toLong
        compareObjs(jsonItems, expectedUsers)
      }

      it("list users sorted by name") {
        val users = List(factory.createRegisteredUser.copy(name = "user3"),
                         factory.createRegisteredUser.copy(name = "user2"),
                         factory.createRegisteredUser.copy(name = "user1"))
        users.foreach(userRepository.put)

        val sortExprs = Table("sort by", "name", "-name")
        forAll(sortExprs) { sortExpr =>
          val jsonItems = multipleItemsResultWithDefaultUser(uri         = uri("search"),
                                                             queryParams = Map("sort" -> sortExpr),
                                                             offset      = 0,
                                                             total       = users.size.toLong,
                                                             maybeNext   = None,
                                                             maybePrev   = None)

          jsonItems must have size users.size.toLong
          if (sortExpr == sortExprs(0)) {
            compareObj(jsonItems(0), users(2))
            compareObj(jsonItems(1), users(1))
            compareObj(jsonItems(2), users(0))
          } else {
            compareObj(jsonItems(0), users(0))
            compareObj(jsonItems(1), users(1))
            compareObj(jsonItems(2), users(2))
          }
        }
      }

      it("list users sorted by email") {
        val users = List(factory.createRegisteredUser.copy(email = "user3@test.com"),
                         factory.createActiveUser.copy(email = "user2@test.com"),
                         factory.createActiveUser.copy(email = "user1@test.com"))
        users.foreach(userRepository.put)

        val sortExprs = Table("sort by", "email", "-email")
        forAll(sortExprs) { sortExpr =>
          val jsonItems = multipleItemsResultWithDefaultUser(uri         = uri("search"),
                                                             queryParams = Map("sort" -> sortExpr),
                                                             offset      = 0,
                                                             total       = users.size.toLong,
                                                             maybeNext   = None,
                                                             maybePrev   = None)

          jsonItems must have size users.size.toLong
          if (sortExpr == sortExprs(0)) {
            compareObj(jsonItems(0), users(2))
            compareObj(jsonItems(1), users(1))
            compareObj(jsonItems(2), users(0))
          } else {
            compareObj(jsonItems(0), users(0))
            compareObj(jsonItems(1), users(1))
            compareObj(jsonItems(2), users(2))
          }
        }
      }

      it("list users sorted by state") {
        val users = List(factory.createRegisteredUser.copy(email = "user3@test.com"),
                         factory.createLockedUser.copy(email = "user2@test.com"),
                         factory.createActiveUser.copy(email = "user1@test.com"))
        users.foreach(userRepository.put)

        val sortExprs = Table("sort by", "state", "-state")
        forAll(sortExprs) { sortExpr =>
          val jsonItems = multipleItemsResultWithDefaultUser(uri = uri("search"),
                                                             queryParams = Map("sort" -> sortExpr),
                                                             offset = 0,
                                                             total = users.size.toLong,
                                                             maybeNext = None,
                                                             maybePrev = None)

          jsonItems must have size users.size.toLong
          if (sortExpr == sortExprs(0)) {
            compareObj(jsonItems(0), users(2))
            compareObj(jsonItems(1), users(1))
            compareObj(jsonItems(2), users(0))
          } else {
            compareObj(jsonItems(0), users(0))
            compareObj(jsonItems(1), users(1))
            compareObj(jsonItems(2), users(2))
          }
        }
      }

      it("list a single user when using paged query") {
        val users = List(factory.createRegisteredUser.copy(email = "user3@test.com"),
                         factory.createLockedUser.copy(email = "user2@test.com"),
                         factory.createActiveUser.copy(email = "user1@test.com"))
        users.foreach(userRepository.put)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri         = uri("search"),
            queryParams = Map("filter" -> "email:like:test",
                              "sort"   -> "email", "limit" -> "1"),
            total       = users.size.toLong,
            maybeNext   = Some(2))

        compareObj(jsonItem, users(2))
      }

      it("fail when using an invalid query parameters") {
        PagedResultsSpec(this).failWithInvalidParams(uri("search"))
      }

    }

    describe("GET /api/users/names") {

      def userToDto(user: User): NameAndStateDto =
        NameAndStateDto(user.id.id, user.slug, user.name, user.state.id)

      describe("must return user names") {

        class Fixture {
          val users = (1 to 2).map {_ => factory.createActiveUser }
          val nameDtos = users.map(userToDto).toSeq
          users.foreach(userRepository.put)
        }

        it("in ascending order") {
          val f = new Fixture
          val nameDtos = f.nameDtos.sortWith { (a, b) => (a.name compareToIgnoreCase b.name) < 0 }

          val json = makeRequest(GET, uri("names") + "?sort=name")

          (json \ "status").as[String] must include ("success")

          val jsonObjs = jsonUsersFilterOutDefaultUser((json \ "data").as[List[JsObject]])

          jsonObjs.size must be (nameDtos.size)
          jsonObjs.zip(nameDtos).foreach { case (jsonObj, nameDtos) =>
            compareObj(jsonObj, nameDtos)
          }
        }

        it("in reverse order") {
          val f = new Fixture
          val nameDtos = f.nameDtos.sortWith { (a, b) => (a.name compareToIgnoreCase b.name) > 0 }

          val json = makeRequest(GET, uri("names") + "?sort=-name")

          (json \ "status").as[String] must include ("success")

          val jsonObjs = jsonUsersFilterOutDefaultUser((json \ "data").as[List[JsObject]])

          jsonObjs.size must be (nameDtos.size)
          jsonObjs.zip(nameDtos).foreach { case (jsonObj, nameDtos) =>
            compareObj(jsonObj, nameDtos)
          }
        }
      }

      it("must return user names filtered by name") {
        val users = (1 to 2).map {_ => factory.createActiveUser }
        users.foreach(userRepository.put)
        val user = users.head

        val json = makeRequest(GET, uri("names") + s"?filter=name::${user.name}")

        (json \ "status").as[String] must include ("success")

        val jsonObjs = (json \ "data").as[List[JsObject]]

        jsonObjs.size must be (1)
        compareObj(jsonObjs(0), userToDto(user))
      }

    }

    describe("GET /api/users/counts") {

      def checkCounts(json:            JsValue,
                      registeredCount: Long,
                      activeCount:     Long,
                      lockedCount:     Long) = {
        (json \ "total").as[Long] must be (registeredCount + activeCount + lockedCount)

        (json \ "registeredCount").as[Long] must be (registeredCount)

        (json \ "activeCount").as[Long] must be (activeCount)

        (json \ "lockedCount").as[Long] must be (lockedCount)
      }

      it("return empty counts") {
        val json = makeRequest(GET, uri("counts"))
                              (json \ "status").as[String] must include ("success")
        checkCounts(json            = (json \ "data").get,
                    registeredCount = 0,
                    activeCount     = 1, // +1 for the default user
                    lockedCount     = 0)
      }

      it("return valid counts") {
        val users = List(factory.createRegisteredUser,
                         factory.createRegisteredUser,
                         factory.createRegisteredUser,
                         factory.createActiveUser,
                         factory.createActiveUser,
                         factory.createLockedUser)
        users.foreach { c => userRepository.put(c) }

        val json = makeRequest(GET, uri("counts"))
                              (json \ "status").as[String] must include ("success")
        checkCounts(json            = (json \ "data").get,
                    registeredCount = 3,
                    activeCount     = 2 + 1, // +1 for the default user
                    lockedCount     = 1)
      }

    }

    describe("POST /api/users") {

      it("register a user") {
        val user = factory.createRegisteredUser
        val reqJson = Json.obj("name" -> user.name,
                               "email" -> user.email,
                               "password" -> "testpassword",
                               "avatarUrl" -> user.avatarUrl)
        val json = makeRequest(POST, uri(""), json = reqJson)

        (json \ "status").as[String] must be ("success")

        val jsonId = (json \ "data" \ "id").as[String]
        jsonId.length must be > 0
        val userId = UserId(jsonId)

        userRepository.getByKey(userId) mustSucceed { repoUser =>
          compareObj((json \ "data").as[JsObject], repoUser)

          repoUser must have (
            'id          (userId),
            'version     (0L),
            'name        (user.name),
            'email       (user.email),
            'avatarUrl   (user.avatarUrl)
          )

          checkTimeStamps(repoUser, OffsetDateTime.now, None)
        }
      }

      it("users with the same name (different emails) get different slugs") {
        val name = faker.Name.name
        val responses = (0 until 2).map { _ =>
            val user = factory.createActiveUser.copy(name = name)
            val reqJson = Json.obj("name" -> user.name,
                                   "email" -> user.email,
                                   "password" -> "testpassword",
                                   "avatarUrl" -> user.avatarUrl)
            val response = makeRequest(POST, uri(""), json = reqJson)

            (response \ "status").as[String] must be ("success")
            response
          }

        (responses(0) \ "data" \ "id") must not equal ((responses(1) \ "data" \ "id"))

        (responses(0) \ "data" \ "slug") must not equal ((responses(1) \ "data" \ "slug"))

        (responses(0) \ "data" \ "name") must equal ((responses(1) \ "data" \ "name"))
      }

    }

    describe("GET /api/users/:slug") {

      it("return a user") {
        val f = new activeUserFixture
        val json = makeRequest(GET, uri(f.user.slug))

        (json \ "status").as[String] must be ("success")

        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, f.user)
      }

      it("return not found for an invalid user") {
        val user = factory.createActiveUser
        notFound(GET, uri(user), JsNull, "EntityCriteriaNotFound: user slug")
      }
    }

    describe("POST /api/users/update/:id") {

      describe("when updating name") {

        it("update a user's name") {
          val user = factory.createActiveUser
          userRepository.put(user)
          val newName = s"${faker.Name.first_name} ${faker.Name.last_name}"
          val json = makeUpdateRequest(user, "name", JsString(newName))

          (json \ "status").as[String] must be ("success")

          (json \ "data" \ "version").as[Int] must be(user.version + 1)

          (json \ "data" \ "name").as[String] must be(newName)

          userRepository.getByKey(user.id) mustSucceed { repoUser =>
            compareObj((json \ "data").as[JsObject], repoUser)

            repoUser must have (
              'id          (user.id),
              'version     (user.version + 1),
              'name        (newName),
              'email       (user.email),
              'avatarUrl   (user.avatarUrl)
            )

            checkTimeStamps(repoUser, user.timeAdded, OffsetDateTime.now)
          }
        }

        it("users with the same name (different emails) get different slugs") {
          val users = (0 until 2).map { _ =>
              val user = factory.createActiveUser
              userRepository.put(user)
              user
            }

          val dupName = users(1).name
          val response = makeUpdateRequest(users(0), "name", JsString(dupName))

          (response \ "data" \ "id").as[String] must equal (users(0).id.id)

          (response \ "data" \ "id").as[String] must not equal (users(1).id.id)

          (response \ "data" \ "slug").as[String] must not equal (Slug(dupName))

          (response \ "data" \ "slug").as[String] must include (Slug(dupName))

          (response \ "data" \ "name").as[String] must equal (dupName)
        }

        it("not update a user's name when an invalid version number is used") {
          val user = factory.createActiveUser
          userRepository.put(user)
          val json = Json.obj("expectedVersion" -> (user.version + 1L),
                              "property"        -> "name",
                              "newValue"        -> user.name)
          badRequest(POST, updateUri(user, "update"), json, "InvalidVersion")
        }

        it("not update a user's name with invalid values") {
          val user = factory.createActiveUser
          userRepository.put(user)

          forAll(Table(
                   ( "value", "error message" ),
                   ( "", "NonEmptyString" ),
                   ( "$#%", "InvalidName" )
                 )) { (value, errMsg) =>

            val json = Json.obj("expectedVersion" -> user.version,
                                "property"        -> "name",
                                "newValue"        -> value)
            badRequest(POST, updateUri(user, "update"), json, errMsg)
          }
        }
      }

      describe("when updating email") {

        it("update a user's email") {
          val user = factory.createActiveUser.copy(timeAdded = OffsetDateTime.now.minusMonths(1))
          userRepository.put(user)
          val newEmail = nameGenerator.nextEmail[User]
          val json = makeUpdateRequest(user, "email", JsString(newEmail))

          (json \ "status").as[String] must be ("success")

          (json \ "data" \ "version").as[Int] must be(user.version + 1)

          (json \ "data" \ "email").as[String] must be(newEmail)

          userRepository.getByKey(user.id) mustSucceed { repoUser =>
            compareObj((json \ "data").as[JsObject], repoUser)

            repoUser must have (
              'id          (user.id),
              'version     (user.version + 1),
              'name        (user.name),
              'email       (newEmail),
              'avatarUrl   (user.avatarUrl)
            )

            checkTimeStamps(repoUser, user.timeAdded, OffsetDateTime.now)
          }
        }

        it("not update a user's email with an invalid email address") {
          val user = factory.createActiveUser
          userRepository.put(user)
          val json = Json.obj("expectedVersion" -> user.version,
                              "property"        -> "email",
                              "newValue"        -> faker.Lorem.sentence(3))
          badRequest(POST, updateUri(user, "update"), json, "InvalidEmail")
        }

        it("not update a user's email if an invalid version number is used") {
          val user = factory.createActiveUser
          userRepository.put(user)
          val json = Json.obj("expectedVersion" -> (user.version + 10L),
                              "property"        -> "email",
                              "newValue"        -> user.email)
          badRequest(POST, updateUri(user, "update"), json, "InvalidVersion")
        }

      }

      describe("when updating password") {

        it("update a user's password") {
          val plainPassword = nameGenerator.next[User]
          val newPassword = nameGenerator.next[User]
          val salt = passwordHasher.generateSalt
          val encryptedPassword = passwordHasher.encrypt(plainPassword, salt)
          val user = factory.createActiveUser.copy(password  = encryptedPassword,
                                                   salt      = salt,
                                                   timeAdded = OffsetDateTime.now.minusMonths(1))
          userRepository.put(user)

          val json = makeUpdateRequest(user,
                                       "password",
                                       Json.obj("currentPassword" -> plainPassword,
                                                "newPassword"     -> newPassword))

          (json \ "status").as[String] must be ("success")

          (json \ "data" \ "id").as[String] must be (user.id.id)

          (json \ "data" \ "version").as[Long] must be (user.version + 1)

          userRepository.getByKey(user.id) mustSucceed { repoUser =>
            compareObj((json \ "data").as[JsObject], repoUser)

            repoUser must have (
              'id          (user.id),
              'version     (user.version + 1),
              'name        (user.name),
              'email       (user.email),
              'avatarUrl   (user.avatarUrl)
            )

            checkTimeStamps(repoUser, user.timeAdded, OffsetDateTime.now)
          }
        }

        it("not update a user's password with an invalid current password") {
          val plainPassword = nameGenerator.next[String]
          val user = createActiveUserInRepository(plainPassword)

          forAll(Table(
                   ( "value" ),
                   ( ""                      ),
                   ( faker.Lorem.sentence(3) )
                 )) { value =>
            val newValue = Json.obj("currentPassword" -> value,
                                    "newPassword"     -> faker.Lorem.sentence(3))
            val json = Json.obj("expectedVersion" -> user.version,
                                "property"        -> "password",
                                "newValue"        -> newValue)
            badRequest(POST, updateUri(user, "update"), json, "InvalidPassword")
          }
        }

        it("not update a user's password with an empty new password") {
          val plainPassword = nameGenerator.next[String]
          val user = createActiveUserInRepository(plainPassword)
          val newValue = Json.obj("currentPassword" -> plainPassword,
                                  "newPassword"     -> "")
          val json = Json.obj("expectedVersion" -> user.version,
                              "property"        -> "password",
                              "newValue"        -> newValue)
          badRequest(POST, updateUri(user, "update"), json, "InvalidNewPassword")
        }

        it("fail when attempting to update a user's password with a bad version number") {
          val plainPassword = nameGenerator.next[String]
          val user = createActiveUserInRepository(plainPassword)
          val newValue = Json.obj("currentPassword" -> plainPassword,
                                  "newPassword"     -> faker.Lorem.sentence(3))
          val json = Json.obj("expectedVersion" -> (user.version + 10L),
                              "property"        -> "password",
                              "newValue"        -> newValue)
          badRequest(POST, updateUri(user, "update"), json, "InvalidVersion")
        }
      }

      describe("when updating avatar URL") {

        it("update a user's avatar URL") {
          val user = factory.createActiveUser.copy(timeAdded = OffsetDateTime.now.minusMonths(1))
          userRepository.put(user)
          val newAvatarUrl = nameGenerator.nextUrl[User]
          val json = makeUpdateRequest(user, "avatarUrl", JsString(newAvatarUrl))

          (json \ "status").as[String] must be ("success")

          (json \ "data" \ "version").as[Int] must be(user.version + 1)

          (json \ "data" \ "avatarUrl").as[String] must be(newAvatarUrl)

          userRepository.getByKey(user.id) mustSucceed { repoUser =>
            compareObj((json \ "data").as[JsObject], repoUser)

            repoUser must have (
              'id          (user.id),
              'version     (user.version + 1),
              'name        (user.name),
              'email       (user.email),
              'avatarUrl   (Some(newAvatarUrl))
            )

            checkTimeStamps(repoUser, user.timeAdded, OffsetDateTime.now)
          }
        }

        it("remove a user's avatar URL") {
          val user = factory.createActiveUser
          userRepository.put(user)

          val json = makeUpdateRequest(user, "avatarUrl", JsString(""))

          (json \ "status").as[String] must be ("success")

          (json \ "data" \ "version").as[Int] must be(user.version + 1)

          (json \ "data" \ "avatarUrl").asOpt[String] mustBe None
        }

        it("not update a user's avatar URL if URL is invalid") {
          val user = factory.createActiveUser
          userRepository.put(user)
          val json = Json.obj("expectedVersion" -> user.version,
                              "property"        -> "avatarUrl",
                              "newValue"        -> "bad url")
          badRequest(POST, updateUri(user, "update"), json, "InvalidUrl")
        }

        it("not update a user's avatar URL if an invalid version number is used") {
          val user = factory.createActiveUser
          userRepository.put(user)
          val json = Json.obj("expectedVersion" -> (user.version + 10L),
                              "property"        -> "avatarUrl",
                              "newValue"        -> nameGenerator.nextUrl[User])
          badRequest(POST, updateUri(user, "update"), json, "InvalidVersion")
        }
      }

      describe("when activating a user") {

        userChangeStateSharedBehaviour(factory.createRegisteredUser,
                                       List[User](factory.createActiveUser, factory.createLockedUser),
                                       "activate",
                                       "active")

      }

      describe("when locking a user") {

        userChangeStateSharedBehaviour(factory.createActiveUser,
                                       List[User](factory.createLockedUser),
                                       "lock",
                                       "locked")

      }

      describe("when unlocking a user") {

        userChangeStateSharedBehaviour(factory.createLockedUser,
                                       List[User](factory.createActiveUser, factory.createRegisteredUser),
                                       "unlock",
                                       "active")

      }

    }

    describe("POST /api/users/roles") {

      def addRoleToUserJson(user: User, role: Role): JsObject = {
        Json.obj("expectedVersion" -> user.version,
                 "roleId"          -> role.id)
      }

      it("can add a role to a user") {
        val user = factory.createActiveUser
        val role = factory.createRole
        Set(user, role).foreach(addToRepository)

        val reply = makeRequest(POST, uri("roles", user.id.id), addRoleToUserJson(user, role))

        (reply \ "status").as[String] must be ("success")

        val jsonId = (reply \ "data" \ "id").as[String]

        userRepository.getByKey(UserId(jsonId)) mustSucceed { repoUser =>
          compareObj((reply \ "data").as[JsObject], repoUser)

          repoUser must have (
            'id          (user.id),
            'version     (user.version),
            'name        (user.name),
            'email       (user.email),
            'avatarUrl   (user.avatarUrl),
            'state       (user.state.id))

          checkTimeStamps(repoUser, user.timeAdded, user.timeModified)
        }

        val roles = (reply \ "data" \ "roles").as[List[JsObject]]
        roles.length must be (1)

        val roleId = (roles(0) \ "id").as[String]

        accessItemRepository.getByKey(AccessItemId(roleId)) mustSucceed { item =>
          inside(item) { case repoRole: Role =>
            repoRole must have (
              'id             (role.id),
              'version        (role.version + 1)
            )
            repoRole.userIds must contain (user.id)
            checkTimeStamps(repoRole, OffsetDateTime.now, OffsetDateTime.now)
          }
        }

        val roleSlug = (roles(0) \ "slug").as[String]
        val roleName = (roles(0) \ "name").as[String]

        roleSlug must be (role.slug)
        roleName must be (role.name)
      }

      it("cannot add the same user more than once") {
        val user = factory.createActiveUser
        val role = factory.createRole.copy(userIds = Set(user.id))
        Set(user, role).foreach(addToRepository)

        badRequest(POST,
                   uri("roles", user.id.id),
                   addRoleToUserJson(user, role),
                   "EntityCriteriaError: user ID is already in role")
      }

      it("cannot add a user that does not exist") {
        val user = factory.createActiveUser
        val role = factory.createRole.copy(userIds = Set(user.id))
        Set(role).foreach(addToRepository)
        notFound(POST,
                 uri("roles", user.id.id),
                 addRoleToUserJson(user, role),
                 "IdNotFound: user id")
      }

      it("cannot add a role that does not exist") {
        val user = factory.createActiveUser
        val role = factory.createRole.copy(userIds = Set(user.id))
        Set(user).foreach(addToRepository)
        notFound(POST,
                 uri("roles", user.id.id),
                 addRoleToUserJson(user, role),
                 "IdNotFound: role id")
      }

      // cannot test for invalid version, since version check is determined internally by server

    }

    describe("POST /api/users/login") {

      it("allow a user to log in") {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)

        val reqJson = Json.obj("email" -> user.email,
                               "password" -> plainPassword)
        val json = makeRequest(POST, uri("login"), json = reqJson)

        (json \ "status").as[String] must be ("success")

        (json \ "data" \ "email").as[String] must be (user.email)
      }

      it("prevent an invalid user from logging in") {
        val invalidUser = nameGenerator.nextEmail[String]
        val reqJson = Json.obj("email" -> invalidUser,
                               "password" -> nameGenerator.next[String])
        val json = makeRequest(POST, uri("login"), UNAUTHORIZED, json = reqJson)
        json must be (JsNull)
      }

      it("prevent a user logging in with bad password") {
        val user = createRegisteredUserInRepository(nameGenerator.next[String])
        val invalidPassword = nameGenerator.next[String]
        val reqJson = Json.obj("email" -> user.email,
                               "password" -> invalidPassword)
        val json = makeRequest(POST, uri("login"), UNAUTHORIZED, json = reqJson)
        json must be (JsNull)
      }

      it("not allow a locked user to log in") {
        val plainPassword = nameGenerator.next[User]
        val lockedUser = createLockedUserInRepository(plainPassword)

        val reqJson = Json.obj("email" -> lockedUser.email,
                               "password" -> plainPassword)
        val json = makeRequest(POST, uri("login"), UNAUTHORIZED, json = reqJson)
        json must be (JsNull)
      }

      it("not allow a request with an invalid token") {
        val badToken = nameGenerator.next[String]

        // this request is valid since user is logged in
        val fakeRequest = FakeRequest(GET, uri(""))
          .withHeaders("X-XSRF-TOKEN" -> badToken,
                       "Set-Cookie" -> Cookies.encodeCookieHeader(Seq(Cookie("XSRF-TOKEN", badToken))))
        val resp = route(app, fakeRequest)
        resp must not be (None)
        resp.map { result =>
          status(result) mustBe (UNAUTHORIZED)
          val body = contentAsString(result)
          body mustBe empty
        }
        ()
      }

      it("not allow mismatched tokens in request for an non asyncaction") {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)
        val validToken = doLogin(user.email, plainPassword)
        val badToken = nameGenerator.next[String]

        // this request is valid since user is logged in
        val fakeRequest = FakeRequest(GET, uri(""))
          .withHeaders("X-XSRF-TOKEN" -> validToken,
                       "Set-Cookie" -> Cookies.encodeCookieHeader(Seq(Cookie("XSRF-TOKEN", badToken))))
        val resp = route(app, fakeRequest)
        resp must not be (None)
        resp.map { result =>
          status(result) mustBe (UNAUTHORIZED)
          val body = contentAsString(result)
          body mustBe empty
        }
        ()
      }

      it("not allow mismatched tokens in request for an async action") {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)

        val validToken = doLogin(user.email, plainPassword)
        val badToken = nameGenerator.next[String]

        val reqJson = Json.obj("expectedVersion" -> Some(user.version))

        // this request is valid since user is logged in
        val fakeRequest = FakeRequest(GET, uri("names"))
          .withJsonBody(reqJson)
          .withHeaders("X-XSRF-TOKEN" -> validToken,
                       "Set-Cookie" -> Cookies.encodeCookieHeader(Seq(Cookie("XSRF-TOKEN", badToken))))

        //log.info(s"makeRequest: request: $fakeRequest")

        val resp = route(app, fakeRequest)
        resp must not be (None)
        resp.map { result =>
          //log.info(s"makeRequest: status: ${status(result)}, result: ${contentAsString(result)}")
          status(result) mustBe (UNAUTHORIZED)
          val body = contentAsString(result)
          body mustBe empty
        }
        ()
      }

      it("not allow requests missing XSRF-TOKEN cookie") {
        val resp = route(app, FakeRequest(GET, uri("")))
        resp must not be (None)
        resp.map { result =>
          status(result) mustBe (UNAUTHORIZED)
          val body = contentAsString(result)
          body mustBe empty
        }
        ()
      }

      it("not allow requests missing X-XSRF-TOKEN in header") {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)
        val token = doLogin(user.email, plainPassword)

        val fakeRequest = FakeRequest(GET, uri(""))
          .withHeaders("Set-Cookie" -> Cookies.encodeCookieHeader(Seq(Cookie("XSRF-TOKEN", token))))
        val resp = route(app, fakeRequest)
        resp must not be (None)
        resp.map { result =>
          status(result) mustBe (UNAUTHORIZED)
          val body = contentAsString(result)
          body mustBe empty
        }
        ()
      }
    }

    describe("POST /api/logout") {

      it("disallow access to logged out users") {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)
        val token = doLogin(user.email, plainPassword)

        // this request is valid since user is logged in
        var json = makeRequest(GET, uri("authenticate"), OK, JsNull, token)
                              (json \ "data" \ "id").as[String] must be (user.id.id)

        // the user is now logged out
        json = makeRequest(POST, uri("logout"), OK, JsNull, token)
                          (json \ "status").as[String] must be ("success")

        // the following request must fail
        json = makeRequest(GET, uri(""), UNAUTHORIZED, JsNull, token)
        json must be (JsNull)
      }
    }

    describe("POST /api/users/passreset") {

      it("allow an active user to reset his/her password") {
        val user = createActiveUserInRepository(nameGenerator.next[String])
        val json = makeRequest(POST,
                               uri("passreset"),
                               Json.obj("email" -> user.email))
                              (json \ "status").as[String] must be ("success")
      }

      it("not allow a registered user to reset his/her password") {
        val user = createRegisteredUserInRepository(nameGenerator.next[String])
        val reqJson = Json.obj("email" -> user.email)
        val json = makeRequest(POST, uri("passreset"), UNAUTHORIZED, reqJson)
        json must be (JsNull)
      }

      it("not allow a locked user to reset his/her password") {
        val lockedUser = factory.createLockedUser
        userRepository.put(lockedUser)

        val reqJson = Json.obj("email" -> lockedUser.email)
        val json = makeRequest(POST, uri("passreset"), UNAUTHORIZED, reqJson)
        json must be (JsNull)
      }

      it("not allow a password reset on an invalid email address") {
        val reqJson = Json.obj("email" -> nameGenerator.nextEmail[User])
        val json = makeRequest(POST, uri("passreset"), UNAUTHORIZED, reqJson)
        json must be (JsNull)
      }

    }

    describe("GET /api/users/authenticate") {

      it("allow a user to authenticate") {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)
        val token = doLogin(user.email, plainPassword)

        val authReplyJson = makeRequest(GET, uri("authenticate"), OK, JsNull, token = token)

        (authReplyJson \ "status").as[String] must be ("success")

        (authReplyJson \ "data" \ "email").as[String] must be (user.email)
      }

      it("not allow a locked user to authenticate") {
        val plainPassword = nameGenerator.next[String]
        val activeUser = createActiveUserInRepository(plainPassword)
        val token = doLogin(activeUser.email, plainPassword)
        token.length must be > 0

        val lockedUser = activeUser.lock | fail
        userRepository.put(lockedUser)

        val reply = makeRequest(GET, uri("authenticate"), UNAUTHORIZED, JsNull, token = token)
        reply must be (JsNull)
      }
    }

    describe("GET /api/users/studies") {

      // these tests can only test the studies the default user has access to, since the
      // test framework logs in as the default user.
      //
      // Tests for other types of users can be found in AccessServiceSpec.

      it("returns no studies for default user") {
        // no studies have been added, default user has access to all studies
        PagedResultsSpec(this).emptyResults(uri("studies"))
      }

      it("returns a study for the default user") {
        val study = factory.createEnabledStudy
        studyRepository.put(study)
        val jsonItem = PagedResultsSpec(this).singleItemResult(uri("studies"))

        (jsonItem \ "id").as[String] must be (study.id.id)
      }

    }

  }

  describe("DELETE /api/users/roles/:userId/:version/:roleId") {

    it("can remove a user") {
      val user = factory.createActiveUser
      val role = factory.createRole.copy(userIds = Set(user.id))
      Set(user, role).foreach(addToRepository)

      val url = uri("roles", user.id.id, user.version.toString, role.id.id)
      val reply = makeRequest(DELETE, url)

      (reply \ "status").as[String] must include ("success")

      val jsonId = (reply \ "data" \ "id").as[String]
      val userId = UserId(jsonId)
      jsonId.length must be > 0

      userRepository.getByKey(userId) mustSucceed { repoUser =>
        compareObj((reply \ "data").as[JsObject], repoUser)

        repoUser must have (
          'id          (user.id),
          'version     (user.version),
          'name        (user.name),
          'email       (user.email),
          'avatarUrl   (user.avatarUrl),
          'state       (user.state.id))

        checkTimeStamps(repoUser, user.timeAdded, user.timeModified)
      }

      val roles = (reply \ "data" \ "roles").as[List[JsObject]]
      roles.length must be (0)

      accessItemRepository.getByKey(role.id) mustSucceed { item =>
        inside(item) { case repoRole: Role =>
          repoRole must have (
            'id             (role.id),
            'version        (role.version + 1)
          )
          repoRole.userIds must not contain (user.id)
          checkTimeStamps(repoRole, role.timeAdded, OffsetDateTime.now)
        }
      }
    }

    it("cannot remove a user not in the role") {
      val user = factory.createActiveUser
      val role = factory.createRole
      Set(user, role).foreach(addToRepository)

      val url = uri("roles", user.id.id, user.version.toString, role.id.id)
      badRequest(DELETE, url, JsNull, "EntityCriteriaError: user ID is not in role")
    }

    it("cannot remove a user that does not exist") {
      val user = factory.createActiveUser
      val role = factory.createRole
      Set(role).foreach(addToRepository)

      val url = uri("roles", user.id.id, user.version.toString, role.id.id)
      notFound(DELETE, url, JsNull, "IdNotFound: user id")
    }

    it("111 fail when removing and role ID does not exist") {
      val user = factory.createActiveUser
      val role = factory.createRole
      Set(user).foreach(addToRepository)

      val url = uri("roles", user.id.id, user.version.toString, role.id.id)
      notFound(DELETE, url, JsNull, "IdNotFound: role id")
    }

    // cannot test for invalid version, since version check is determined internally by server

  }

  def userChangeStateSharedBehaviour(user:            User,
                                     wrongStateUsers: List[User],
                                     stateAction:     String,
                                     newState:        String) {

    it(s"can $stateAction a user") {
      userRepository.put(user)
      val json = makeUpdateRequest(user, "state", JsString(stateAction))

      (json \ "status").as[String] must be ("success")

      (json \ "data" \ "version").as[Int] must be(user.version + 1)

      (json \ "data" \ "state").as[String] must be(newState)

      userRepository.getByKey(user.id) mustSucceed { repoUser =>
        compareObj((json \ "data").as[JsObject], repoUser)

        repoUser must have (
          'id          (user.id),
          'version     (user.version + 1),
          'name        (user.name),
          'email       (user.email),
          'avatarUrl   (user.avatarUrl),
          'state       (newState))

        checkTimeStamps(repoUser, user.timeAdded, OffsetDateTime.now)
      }
    }

    it("must not change a user's state with an invalid version number") {
      userRepository.put(user)
      val json = Json.obj("expectedVersion" -> (user.version + 10L),
                          "property"        -> "state",
                          "newValue"        -> stateAction)
      badRequest(POST, updateUri(user, "update"), json, "InvalidVersion")
    }

    it("must not change a user to the wrong state") {
      forAll(Table("user in wrong state", wrongStateUsers:_*)) { user =>
        info(s"must not $stateAction a user currently in ${user.state} state")
        userRepository.put(user)

        val json = Json.obj("expectedVersion" -> user.version,
                            "property"        -> "state",
                            "newValue"        -> stateAction)
        badRequest(POST, updateUri(user, "update"), json, "InvalidStatus")
      }
    }

  }

  private def uri(paths: String*): String = {
    val basePath = "/api/users"
    if (paths.isEmpty) basePath
    else s"$basePath/" + paths.mkString("/")
  }

  private def uri(user: User): String = uri(user.id.id)

  private def updateUri(user: User, path: String): String = uri(path, user.id.id)

  private def addMembershipForUser(user: User) = {
    val membership = factory.createMembership.copy(userIds = Set(user.id))
    membershipRepository.put(membership)
  }

  private def createRegisteredUserInRepository(plainPassword: String): RegisteredUser = {
    val user = createRegisteredUser(plainPassword)
    userRepository.put(user)
    addMembershipForUser(user)
    user
  }

  private def createActiveUserInRepository(plainPassword: String): ActiveUser = {
    val user = createActiveUser(plainPassword)
    userRepository.put(user)
    addMembershipForUser(user)
    user
  }

  private def createLockedUserInRepository(plainPassword: String): LockedUser = {
    val user = createLockedUser(plainPassword)
    userRepository.put(user)
    addMembershipForUser(user)
    user
  }

  private def compareObjs(jsonList: List[JsObject], users: List[User]) = {
    val usersMap = users.map { user => (user.id, user) }.toMap
    jsonList.foreach { jsonObj =>
      val jsonId = UserId((jsonObj \ "id").as[String])
      compareObj(jsonObj, usersMap(jsonId))
    }
  }

  private def jsonUsersFilterOutDefaultUser(jsonList: List[JsObject]): List[JsObject] = {
    jsonList.filter(json => (json \ "id").as[String] != Global.DefaultUserId.id)
  }

  private def multipleItemsResultWithDefaultUser(uri:         String,
                                                 queryParams: Map[String, String] =  Map.empty,
                                                 offset:      Long,
                                                 total:       Long,
                                                 maybeNext:   Option[Int],
                                                 maybePrev:   Option[Int]) = {
    val jsonUsers = PagedResultsSpec(this).multipleItemsResult(uri,
                                                               queryParams,
                                                               offset,
                                                               total + 1, // +1 for the default user
                                                               maybeNext,
                                                               maybePrev)
    jsonUsersFilterOutDefaultUser(jsonUsers)
  }

  def makeUpdateRequest(user:           User,
                        property:       String,
                        newValue:       JsValue,
                        expectedStatus: Int = OK): JsValue = {
    var json = Json.obj("expectedVersion" -> user.version,
                        "property"        -> property)

    if (newValue !== JsNull) {
      json = json ++ Json.obj("newValue" -> newValue)
    }
    makeRequest(POST, updateUri(user, "update"), expectedStatus, json)
  }
}
