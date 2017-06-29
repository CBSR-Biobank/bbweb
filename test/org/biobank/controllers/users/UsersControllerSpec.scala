package org.biobank.controllers.users

import com.github.nscala_time.time.Imports._
import org.biobank.Global
import org.biobank.controllers.PagedResultsSpec
import org.biobank.domain.JsonHelper
import org.biobank.domain.user._
import org.biobank.fixture.ControllerFixture
import org.scalatest.prop.TableDrivenPropertyChecks._
import play.api.libs.json._
import play.api.mvc.Cookie
import play.api.test.Helpers._
import play.api.test._

/**
 * Tests the REST API for [[User]].
 */
class UsersControllerSpec extends ControllerFixture with JsonHelper with UserFixtures {
  import org.biobank.TestUtils._

  class activeUserFixture {
    val user = factory.createActiveUser
    userRepository.put(user)
    addMembershipForUser(user)
  }

  private def uri: String = "/users/"

  private def uri(user: User): String = uri + s"${user.id.id}"

  private def uri(path: String): String = uri + s"$path"

  private def updateUri(user: User, path: String): String = uri(path) + s"/${user.id.id}"

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

  describe("Users REST API") {

    describe("GET /users") {

      it("lists the default user") {
        val jsonItem = PagedResultsSpec(this).singleItemResult(uri)
        (jsonItem \ "id").as[String] must be (Global.DefaultUserId.id)
      }

      it("list multiple users") {
        val users = (0 until 2).map(_ => factory.createRegisteredUser).toList
        users.foreach(userRepository.put)

        val jsonItems = multipleItemsResultWithDefaultUser(uri       = uri,
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
        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri, Map("filter" -> s"name::${user.name}"))
        compareObj(jsonItem, users(0))
      }

      it("list a single user when filtered by email") {
        val users = List(factory.createRegisteredUser.copy(email = "user1@test.com"),
                         factory.createRegisteredUser.copy(email = "user2@test.com"))
        val user = users(0)
        users.foreach(userRepository.put)

        val jsonItem = PagedResultsSpec(this).singleItemResult(uri, Map("filter" -> s"email::${user.email}"))
        compareObj(jsonItem, users(0))
      }

      it("list a single registered user when filtered by state") {
        val users = List(factory.createRegisteredUser.copy(email = "user1@test.com"),
                         factory.createActiveUser.copy(email = "user2@test.com"),
                         factory.createActiveUser.copy(email = "user3@test.com"))
        users.foreach(userRepository.put)

        val jsonItem = PagedResultsSpec(this).singleItemResult(uri, Map("filter" -> "state::registered"))
        compareObj(jsonItem, users(0))
      }

      it("list active users when filtered by state") {
        val users = List(factory.createRegisteredUser.copy(email = "user1@test.com"),
                         factory.createActiveUser.copy(email = "user2@test.com"),
                         factory.createActiveUser.copy(email = "user3@test.com"))
        users.foreach(userRepository.put)

        val expectedUsers = List(users(1), users(2))
        val jsonItems = multipleItemsResultWithDefaultUser(uri = uri,
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
            uri = uri,
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
          val jsonItems = multipleItemsResultWithDefaultUser(uri         = uri,
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
          val jsonItems = multipleItemsResultWithDefaultUser(uri         = uri,
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
          val jsonItems = multipleItemsResultWithDefaultUser(uri = uri,
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
            uri         = uri,
            queryParams = Map("filter" -> "email:like:test",
                              "sort"   -> "email", "limit" -> "1"),
            total       = users.size.toLong,
            maybeNext   = Some(2))

        compareObj(jsonItem, users(2))
      }

      it("fail when using an invalid query parameters") {
        PagedResultsSpec(this).failWithInvalidParams(uri)
      }

    }

    describe("GET /users/counts") {

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

    describe("POST /users") {

      it("register a user") {
        val user = factory.createRegisteredUser
        val reqJson = Json.obj("name" -> user.name,
                               "email" -> user.email,
                               "password" -> "testpassword",
                               "avatarUrl" -> user.avatarUrl)
        val json = makeRequest(POST, uri, json = reqJson)

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

          checkTimeStamps(repoUser, DateTime.now, None)
        }
      }

      it("fail on registering an existing user") {
        val user = factory.createRegisteredUser
        userRepository.put(user)

        val reqJson = Json.obj("name" -> user.name,
                               "email" -> user.email,
                               "password" -> "testpassword",
                               "avatarUrl" -> user.avatarUrl)
        val json = makeRequest(POST, uri, FORBIDDEN, json = reqJson)

        (json \ "status").as[String] must be ("error")

        (json \ "message").as[String] must include regex("email already registered")
      }
    }

    describe("POST /users/name/:id") {

      it("update a user's name") {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> user.version, "name" -> user.name)
        val json = makeRequest(POST, updateUri(user, "name"), reqJson)

        (json \ "status").as[String] must be ("success")
        (json \ "data" \ "version").as[Int] must be(user.version + 1)
        (json \ "data" \ "name").as[String] must be(user.name)
      }

      it("not update a user's name with an invalid name") {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version),
                               "name"            -> "a")
        val json = makeRequest(POST, updateUri(user, "name"), BAD_REQUEST, json = reqJson)

        (json \ "status").as[String] must be ("error")
        (json \ "message").as[String] must include("InvalidName")
      }

      it("not update a user's name when an invalid version number is used") {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version + 1),
                               "name"            -> user.name)
        val json = makeRequest(POST, updateUri(user, "name"), BAD_REQUEST, reqJson)

        (json \ "status").as[String] must be ("error")

        (json \ "message").as[String] must include ("expected version doesn't match current version")
      }
    }

    describe("POST /users/email/:id") {

      it("update a user's email") {
        val user = factory.createActiveUser.copy(timeAdded = DateTime.lastMonth)
        userRepository.put(user)

        val newEmail = nameGenerator.nextEmail[User]

        val reqJson = Json.obj("expectedVersion" -> Some(user.version),
                               "email"           -> newEmail)
        val json = makeRequest(POST, updateUri(user, "email"), json = reqJson)

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

          checkTimeStamps(repoUser, user.timeAdded, DateTime.now)
        }
      }

      it("not update a user's email with an invalid email address") {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version),
                               "email" -> "abcdef")
        val json = makeRequest(POST, updateUri(user, "email"), BAD_REQUEST, json = reqJson)

        (json \ "status").as[String] must be ("error")
        (json \ "message").as[String] must include("InvalidEmail")
      }

      it("not update a user's email if an invalid version number is used ") {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version + 1),
                               "email" -> user.email)
        val json = makeRequest(POST, updateUri(user, "email"), BAD_REQUEST, reqJson)

        (json \ "status").as[String] must be ("error")

        (json \ "message").as[String] must include ("expected version doesn't match current version")
      }

    }

    describe("POST /users/password/:id") {

      it("update a user's password") {
        val plainPassword = nameGenerator.next[User]
        val newPassword = nameGenerator.next[User]
        val salt = passwordHasher.generateSalt
        val encryptedPassword = passwordHasher.encrypt(plainPassword, salt)
        val user = factory.createActiveUser.copy(password  = encryptedPassword,
                                                 salt      = salt,
                                                 timeAdded = DateTime.lastMonth)
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version),
                               "currentPassword" -> plainPassword,
                               "newPassword"     -> newPassword)
        val json = makeRequest(POST, updateUri(user, "password"), json = reqJson)

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

          checkTimeStamps(repoUser, user.timeAdded, DateTime.now)
        }
      }

      it("not update a user's password with an empty current password") {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version),
                               "currentPassword" -> "",
                               "newPassword"     -> "abcdef")
        val json = makeRequest(POST, updateUri(user, "password"), BAD_REQUEST, json = reqJson)

        (json \ "status").as[String] must be ("error")
      }

      it("not update a user's password with an empty new password") {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version),
                               "currentPassword" -> "abcdef",
                               "newPassword" -> "")
        val json = makeRequest(POST, updateUri(user, "password"), BAD_REQUEST, json = reqJson)

        (json \ "status").as[String] must be ("error")
      }

      it("fail when attempting to update a user's password with a bad version number") {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version + 1),
                               "currentPassword" -> "abcdef",
                               "newPassword" -> "")
        val json = makeRequest(POST, updateUri(user, "password"), BAD_REQUEST, reqJson)

        (json \ "status").as[String] must be ("error")

        (json \ "message").as[String] must include ("expected version doesn't match current version")
      }
    }

    describe("POST /users/avatarurl/:id") {

      it("update a user's avatar URL") {
        val user = factory.createActiveUser.copy(timeAdded = DateTime.lastMonth)
        userRepository.put(user)

        val newAvatarUrl = nameGenerator.nextUrl[User]

        val reqJson = Json.obj("expectedVersion" -> Some(user.version),
                               "avatarUrl" -> newAvatarUrl)
        val json = makeRequest(POST, updateUri(user, "avatarurl"), json = reqJson)

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

          checkTimeStamps(repoUser, user.timeAdded, DateTime.now)
        }
      }

      it("remove a user's avatar URL") {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version))
        val json = makeRequest(POST, updateUri(user, "avatarurl"), json = reqJson)

        (json \ "status").as[String] must be ("success")
        (json \ "data" \ "version").as[Int] must be(user.version + 1)
        (json \ "data" \ "avatarUrl").asOpt[String] mustBe None
      }

      it("not update a user's avatar URL if URL is invalid") {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version),
                               "avatarUrl" -> "abcdef")
        val json = makeRequest(POST, updateUri(user, "avatarurl"), BAD_REQUEST, json = reqJson)

        (json \ "status").as[String] must be ("error")
        (json \ "message").as[String] must include("InvalidUrl")
      }

      it("not update a user's avatar URL if URL is empty") {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version),
                               "avatarUrl" -> "")
        val json = makeRequest(POST, updateUri(user, "avatarurl"), BAD_REQUEST, json = reqJson)

        (json \ "status").as[String] must be ("error")
      }

      it("not update a user's avatar URL if an invalid version number is used") {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version + 1),
                               "avatarUrl" -> user.avatarUrl)
        val json = makeRequest(POST, updateUri(user, "avatarurl"), BAD_REQUEST, json = reqJson)

        (json \ "status").as[String] must be ("error")

        (json \ "message").as[String] must include ("expected version doesn't match current version")
      }
    }

    describe("GET /users/:id") {

      it("return a user") {
        val f = new activeUserFixture
        val json = makeRequest(GET, uri(f.user))
        (json \ "status").as[String] must be ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, f.user)
      }

      it("return not found for an invalid user") {
        val user = factory.createActiveUser
        val json = makeRequest(GET, uri(user), NOT_FOUND)
        (json \ "status").as[String] must be ("error")
        (json \ "message").as[String] must include("IdNotFound")
      }
    }

    describe("POST /users/activate") {

      it("activate a user") {
        val user = factory.createRegisteredUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version))
        val json = makeRequest(POST, updateUri(user, "activate"), json = reqJson)

        (json \ "status").as[String] must be ("success")
      }

      it("must not activate a user with an invalid version number") {
        val user = factory.createRegisteredUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version + 1))
        val json = makeRequest(POST, updateUri(user, "activate"), BAD_REQUEST, reqJson)

        (json \ "status").as[String] must be ("error")

        (json \ "message").as[String] must include ("expected version doesn't match current version")
      }

    }

    describe("POST /users/lock") {

      it("lock a user") {
        val users = Table("users that can be locked",
                          factory.createRegisteredUser,
                          factory.createActiveUser)
        forAll(users) { user =>
          info(s"when ${user.state}")
          userRepository.put(user)

          val reqJson = Json.obj("expectedVersion" -> Some(user.version))
          val json = makeRequest(POST, updateUri(user, "lock"), json = reqJson)

          (json \ "status").as[String] must be ("success")
        }
      }

      it("must not lock a user when an invalid version number is used") {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version + 1))
        val json = makeRequest(POST, updateUri(user, "lock"), BAD_REQUEST, reqJson)

        (json \ "status").as[String] must be ("error")

        (json \ "message").as[String] must include ("expected version doesn't match current version")
      }

      it("must not lock a locked user") {
        val user = factory.createLockedUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version))
        val json = makeRequest(POST, updateUri(user, "lock"), BAD_REQUEST, reqJson)

        (json \ "status").as[String] must be ("error")

        (json \ "message").as[String] must include ("user not registered or active")
      }

    }

    describe("POST /users/unlock") {

      it("must unlock a user") {
        val user = factory.createLockedUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version))
        val json = makeRequest(POST, updateUri(user, "unlock"), json = reqJson)

        (json \ "status").as[String] must be ("success")
      }

      it("must not unlock a user if a invalid version number is used") {
        val user = factory.createLockedUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version + 1))
        val json = makeRequest(POST, updateUri(user, "unlock"), BAD_REQUEST, reqJson)

        (json \ "status").as[String] must be ("error")

        (json \ "message").as[String] must include ("expected version doesn't match current version")
      }

      it("must not unlock a registered or active user") {
        val users = Table("user that can't be unlocked",
                          factory.createRegisteredUser,
                          factory.createActiveUser)
        forAll(users) { user =>
          info(s"${user.state}")
          userRepository.put(user)

          val reqJson = Json.obj("expectedVersion" -> Some(user.version))
          val json = makeRequest(POST, updateUri(user, "unlock"), BAD_REQUEST, reqJson)

          (json \ "status").as[String] must be ("error")

          (json \ "message").as[String] must include ("user not locked")
        }
      }

    }

    describe("POST /users/login") {

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
        val resp = route(app, FakeRequest(GET, uri)
          .withHeaders("X-XSRF-TOKEN" -> badToken)
          .withCookies(Cookie("XSRF-TOKEN", badToken)))
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
        val resp = route(app, FakeRequest(GET, uri)
          .withHeaders("X-XSRF-TOKEN" -> validToken)
          .withCookies(Cookie("XSRF-TOKEN", badToken)))
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
        val fakeRequest = FakeRequest(POST, updateUri(user, "lock"))
        .withJsonBody(reqJson)
        .withHeaders("X-XSRF-TOKEN" -> validToken)
        .withCookies(Cookie("XSRF-TOKEN", badToken))

        //log.info(s"makeRequest: request: $fakeRequest")

        val resp = route(app, fakeRequest)
        resp must not be (None)
        resp.map { result =>
          // log.info(s"makeRequest: status: ${status(result)}, result: ${contentAsString(result)}")
          status(result) mustBe (UNAUTHORIZED)
          val body = contentAsString(result)
          body mustBe empty
        }
        ()
      }

      it("not allow requests missing XSRF-TOKEN cookie") {
        val resp = route(app, FakeRequest(GET, uri))
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

        val resp = route(app, FakeRequest(GET, uri).withCookies(Cookie("XSRF-TOKEN", token)))
        resp must not be (None)
        resp.map { result =>
          status(result) mustBe (UNAUTHORIZED)
          val body = contentAsString(result)
          body mustBe empty
        }
        ()
      }
    }

    describe("POST /logout") {

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
        json = makeRequest(GET, uri, UNAUTHORIZED, JsNull, token)
        json must be (JsNull)
      }
    }

    describe("POST /users/passreset") {

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

    describe("GET /users/authenticate") {

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

    describe("GET /users/studies") {

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
}
