package org.biobank.controllers

import com.github.nscala_time.time.Imports._
import org.biobank.domain.JsonHelper
import org.biobank.domain.user._
import org.biobank.fixture.ControllerFixture
import play.api.libs.json._
import play.api.mvc.Cookie
import play.api.test.Helpers._
import play.api.test._

/**
 * Tests the REST API for [[User]].
 */
class UsersControllerSpec extends ControllerFixture with JsonHelper {
  import org.biobank.TestUtils._

  def uri: String = "/users"

  def uri(user: User): String = uri + s"/${user.id.id}"

  def updateUri(user: User, path: String): String = uri + s"/$path/${user.id.id}"

  def createRegisteredUserInRepository(plainPassword: String): RegisteredUser = {
    val salt = passwordHasher.generateSalt

    val user = factory.createRegisteredUser.copy(
      salt = salt,
      password = passwordHasher.encrypt(plainPassword, salt))
    userRepository.put(user)
    user
  }

  def createActiveUserInRepository(plainPassword: String): ActiveUser = {
    val salt = passwordHasher.generateSalt

    val user = factory.createActiveUser.copy(
      salt = salt,
      password = passwordHasher.encrypt(plainPassword, salt))
    userRepository.put(user)
    user
  }

  def createLockedUserInRepository(plainPassword: String): LockedUser = {
    val salt = passwordHasher.generateSalt

    val user = factory.createLockedUser.copy(
      salt = salt,
      password = passwordHasher.encrypt(plainPassword, salt))
    userRepository.put(user)
    user
  }

  def compareObjs(jsonList: List[JsObject], users: List[User]) = {
    val usersMap = users.map { user => (user.id, user) }.toMap
    jsonList.foreach { jsonObj =>
      val jsonId = UserId((jsonObj \ "id").as[String])
      compareObj(jsonObj, usersMap(jsonId))
    }
  }

  "User REST API" must {

    "GET /users" must {

      "list none" in {
        PagedResultsSpec(this).emptyResults(uri)
      }

      "list a new user" in {
        val user = factory.createRegisteredUser
        userRepository.put(user)
        val jsonItem = PagedResultsSpec(this).singleItemResult(uri)
        compareObj(jsonItem, user)
      }

      "list multiple users" in {
        val users = (0 until 2).map { x =>
          factory.createRegisteredUser
        }.map(user => userRepository.put(user)).toList

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri       = uri,
          offset    = 0,
          total     = users.size,
          maybeNext = None,
          maybePrev = None)
        jsonItems must have size users.size
        compareObjs(jsonItems, users)
      }

      "list a single user when filtered by name" in {
        val users = List(factory.createRegisteredUser.copy(name = "user1"),
                         factory.createRegisteredUser.copy(name = "user2"))
        .map(user => userRepository.put(user))

        val jsonItem = PagedResultsSpec(this)
        .singleItemResult(uri, Map("nameFilter" -> users(0).name))
        compareObj(jsonItem, users(0))
      }

      "list a single user when filtered by email" in {
        val users = List(factory.createRegisteredUser.copy(email = "user1@test.com"),
                         factory.createRegisteredUser.copy(email = "user2@test.com"))
        .map(user => userRepository.put(user))

        val jsonItem = PagedResultsSpec(this)
        .singleItemResult(uri, Map("emailFilter" -> users(0).email))
        compareObj(jsonItem, users(0))
      }

      "list a single registered user when filtered by status" in {
        val users = List(factory.createRegisteredUser.copy(email = "user1@test.com"),
                         factory.createActiveUser.copy(email = "user2@test.com"),
                         factory.createActiveUser.copy(email = "user3@test.com"))
        .map(user => userRepository.put(user))

        val jsonItem = PagedResultsSpec(this)
        .singleItemResult(uri, Map("status" -> "RegisteredUser"))
        compareObj(jsonItem, users(0))
      }

      "list active users when filtered by status" in {
        val users = List(factory.createRegisteredUser.copy(email = "user1@test.com"),
                         factory.createActiveUser.copy(email = "user2@test.com"),
                         factory.createActiveUser.copy(email = "user3@test.com"))
        .map(user => userRepository.put(user))

        val expectedUsers = List(users(1), users(2))
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("status" -> "ActiveUser"),
          offset = 0,
          total = expectedUsers.size,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size expectedUsers.size
        compareObjs(jsonItems, expectedUsers)
      }

      "list locked users when filtered by status" in {
        val users = List(factory.createActiveUser.copy(email = "user1@test.com"),
                         factory.createLockedUser.copy(email = "user2@test.com"),
                         factory.createLockedUser.copy(email = "user3@test.com"))
        .map(user => userRepository.put(user))

        val expectedUsers = List(users(1), users(2))
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("status" -> "LockedUser"),
          offset = 0,
          total = expectedUsers.size,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size expectedUsers.size
        compareObjs(jsonItems, expectedUsers)
      }

      "list users sorted by name" in {
        val users = List(factory.createRegisteredUser.copy(name = "user3"),
                         factory.createRegisteredUser.copy(name = "user2"),
                         factory.createRegisteredUser.copy(name = "user1"))
        .map(user => userRepository.put(user))

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("sort" -> "name"),
          offset = 0,
          total = users.size,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size users.size
        compareObj(jsonItems(0), users(2))
        compareObj(jsonItems(1), users(1))
        compareObj(jsonItems(2), users(0))
      }

      "list users sorted by email" in {
        val users = List(factory.createRegisteredUser.copy(email = "user3@test.com"),
                         factory.createActiveUser.copy(email = "user2@test.com"),
                         factory.createActiveUser.copy(email = "user1@test.com"))
        .map(user => userRepository.put(user))

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("sort" -> "email"),
          offset = 0,
          total = users.size,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size users.size
        compareObj(jsonItems(0), users(2))
        compareObj(jsonItems(1), users(1))
        compareObj(jsonItems(2), users(0))
      }

      "list users sorted by status" in {
        val users = List(factory.createRegisteredUser.copy(email = "user3@test.com"),
                         factory.createLockedUser.copy(email = "user2@test.com"),
                         factory.createActiveUser.copy(email = "user1@test.com"))
        .map(user => userRepository.put(user))

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("sort" -> "status"),
          offset = 0,
          total = users.size,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size users.size
        compareObj(jsonItems(0), users(2))
        compareObj(jsonItems(1), users(1))
        compareObj(jsonItems(2), users(0))
      }

      "list users sorted by status in descending order" in {
        val users = List(factory.createRegisteredUser.copy(email = "user3@test.com"),
                         factory.createLockedUser.copy(email = "user2@test.com"),
                         factory.createActiveUser.copy(email = "user1@test.com"))
        .map(user => userRepository.put(user))

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("sort" -> "status", "order" -> "desc"),
          offset = 0,
          total = users.size,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size users.size
        compareObj(jsonItems(0), users(0))
        compareObj(jsonItems(1), users(1))
        compareObj(jsonItems(2), users(2))
      }

      "list a single user when using paged query" in {
        val users = List(factory.createRegisteredUser.copy(email = "user3@test.com"),
                         factory.createLockedUser.copy(email = "user2@test.com"),
                         factory.createActiveUser.copy(email = "user1@test.com"))
        .map(user => userRepository.put(user))

        val jsonItem = PagedResultsSpec(this).singleItemResult(
          uri = uri,
          queryParams = Map("sort" -> "email", "pageSize" -> "1"),
          total = users.size,
          maybeNext = Some(2))

        compareObj(jsonItem, users(2))
      }

      "fail when using an invalid query parameters" in {
        PagedResultsSpec(this).failWithInvalidParams(uri)
      }

    }

    "GET /users/counts" must {

      def checkCounts(json:            JsValue,
                      registeredCount: Long,
                      activeCount:     Long,
                      lockedCount:     Long) = {
        (json \ "total").as[Long] must be (registeredCount + activeCount + lockedCount)
        (json \ "registeredCount").as[Long] must be (registeredCount)
        (json \ "activeCount").as[Long] must be (activeCount)
        (json \ "lockedCount").as[Long] must be (lockedCount)
      }

      "return empty counts" in {
        val json = makeRequest(GET, uri + "/counts")
        (json \ "status").as[String] must include ("success")
        checkCounts(json            = (json \ "data").get,
                    registeredCount = 0,
                    activeCount     = 0,
                    lockedCount     = 0)
      }

      "return valid counts" in {
        val users = List(factory.createRegisteredUser,
                         factory.createRegisteredUser,
                         factory.createRegisteredUser,
                         factory.createActiveUser,
                         factory.createActiveUser,
                         factory.createLockedUser)
        users.foreach { c => userRepository.put(c) }

        val json = makeRequest(GET, uri + "/counts")
        (json \ "status").as[String] must include ("success")
        checkCounts(json            = (json \ "data").get,
                    registeredCount = 3,
                    activeCount     = 2,
                    lockedCount     = 1)
      }

    }

    "POST /users" must {

      "register a user" in {
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

      "fail on registering an existing user" in {
        val user = factory.createRegisteredUser
        userRepository.put(user)

        val reqJson = Json.obj("name" -> user.name,
                               "email" -> user.email,
                               "password" -> "testpassword",
                               "avatarUrl" -> user.avatarUrl)
        val json = makeRequest(POST, uri, FORBIDDEN, json = reqJson)

        (json \ "status").as[String] must be ("error")
        (json \ "message").as[String] must startWith("already registered")
      }
    }

    "POST /users/name/:id" must {

      "update a user's name" in {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> user.version, "name" -> user.name)
        val json = makeRequest(POST, updateUri(user, "name"), reqJson)

        (json \ "status").as[String] must be ("success")
        (json \ "data" \ "version").as[Int] must be(user.version + 1)
        (json \ "data" \ "name").as[String] must be(user.name)
      }

      "not update a user's name with an invalid name" in {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version),
                               "name"            -> "a")
        val json = makeRequest(POST, updateUri(user, "name"), BAD_REQUEST, json = reqJson)

        (json \ "status").as[String] must be ("error")
        (json \ "message").as[String] must include("InvalidName")
      }
    }

    "POST /users/email/:id" must {

      "update a user's email" in {
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

      "not update a user's email with an invalid email address" in {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version),
                               "email" -> "abcdef")
        val json = makeRequest(POST, updateUri(user, "email"), BAD_REQUEST, json = reqJson)

        (json \ "status").as[String] must be ("error")
        (json \ "message").as[String] must include("InvalidEmail")
      }

    }

    "POST /users/password/:id" must {

      "update a user's password" in {
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

      "not update a user's password with an empty current password" in {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version),
                               "currentPassword" -> "",
                               "newPassword"     -> "abcdef")
        val json = makeRequest(POST, updateUri(user, "password"), BAD_REQUEST, json = reqJson)

        (json \ "status").as[String] must be ("error")
      }

      "not update a user's password with an empty new password" in {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version),
                               "currentPassword" -> "abcdef",
                               "newPassword" -> "")
        val json = makeRequest(POST, updateUri(user, "password"), BAD_REQUEST, json = reqJson)

        (json \ "status").as[String] must be ("error")
      }
    }

    "POST /users/avatarurl/:id" must {

      "update a user's avatar URL" in {
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

      "remove a user's avatar URL" in {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version))
        val json = makeRequest(POST, updateUri(user, "avatarurl"), json = reqJson)

        (json \ "status").as[String] must be ("success")
        (json \ "data" \ "version").as[Int] must be(user.version + 1)
        (json \ "data" \ "avatarUrl").asOpt[String] mustBe None
      }

      "not update a user's avatar URL if URL is invalid" in {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version),
                               "avatarUrl" -> "abcdef")
        val json = makeRequest(POST, updateUri(user, "avatarurl"), BAD_REQUEST, json = reqJson)

        (json \ "status").as[String] must be ("error")
        (json \ "message").as[String] must include("InvalidUrl")
      }

      "not update a user's avatar URL if URL is empty" in {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version),
                               "avatarUrl" -> "")
        val json = makeRequest(POST, updateUri(user, "avatarurl"), BAD_REQUEST, json = reqJson)

        (json \ "status").as[String] must be ("error")
      }
    }

    "GET /users/:id" must {

      "return a user" in {
        val user = factory.createActiveUser
        userRepository.put(user)
        val json = makeRequest(GET, uri(user))
        (json \ "status").as[String] must be ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, user)
      }

      "return not found for an invalid user" in {
        val userId = nameGenerator.next[User]
        val json = makeRequest(GET, uri + s"/$userId", NOT_FOUND)
        (json \ "status").as[String] must be ("error")
        (json \ "message").as[String] must include("IdNotFound")
      }
    }

    "POST /users/activate" must {

      "activate a user" in {
        val user = factory.createRegisteredUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version))
        val json = makeRequest(POST, updateUri(user, "activate"), json = reqJson)

        (json \ "status").as[String] must be ("success")
      }

    }

    "POST /users/lock" must {

      "lock a user" in {
        val user = factory.createActiveUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version))
        val json = makeRequest(POST, updateUri(user, "lock"), json = reqJson)

        (json \ "status").as[String] must be ("success")
      }

    }

    "POST /users/unlock" must {

      "must unlock a user" in {
        val user = factory.createLockedUser
        userRepository.put(user)

        val reqJson = Json.obj("expectedVersion" -> Some(user.version))
        val json = makeRequest(POST, updateUri(user, "unlock"), json = reqJson)

        (json \ "status").as[String] must be ("success")
      }

    }

    "POST /login" must {

      "allow a user to log in" in {
        val plainPassword = nameGenerator.next[String]
        val user = createRegisteredUserInRepository(plainPassword)

        val reqJson = Json.obj("email" -> user.email,
                               "password" -> plainPassword)
        val json = makeRequest(POST, "/login", json = reqJson)

        (json \ "data").as[String].length must be > 0
      }

      "prevent an invalid user from logging in" in {
        val invalidUser = nameGenerator.nextEmail[String]
        val reqJson = Json.obj("email" -> invalidUser,
                               "password" -> nameGenerator.next[String])
        val json = makeRequest(POST, "/login", FORBIDDEN, json = reqJson)

        (json \ "status").as[String] must be ("error")
        (json \ "message").as[String] must include("invalid email")
      }

      "prevent a user logging in with bad password" in {
        val user = createRegisteredUserInRepository(nameGenerator.next[String])
        val invalidPassword = nameGenerator.next[String]
        val reqJson = Json.obj("email" -> user.email,
                               "password" -> invalidPassword)
        val json = makeRequest(POST, "/login", FORBIDDEN, json = reqJson)

        (json \ "status").as[String] must be ("error")

        (json \ "message").as[String] must include("InvalidPassword")
      }

      "not allow a locked user to log in" in {
        val plainPassword = nameGenerator.next[User]
        val lockedUser = createLockedUserInRepository(plainPassword)

        val reqJson = Json.obj("email" -> lockedUser.email,
                               "password" -> plainPassword)
        val json = makeRequest(POST, "/login", FORBIDDEN, json = reqJson)

        (json \ "status").as[String] must be ("error")

        (json \ "message").as[String] must include regex("InvalidStatus.*locked")
      }

      "not allow a request with an invalid token" in {
        val badToken = nameGenerator.next[String]

        // this request is valid since user is logged in
        val resp = route(app, FakeRequest(GET, uri)
          .withHeaders("X-XSRF-TOKEN" -> badToken)
          .withCookies(Cookie("XSRF-TOKEN", badToken)))
        resp must not be (None)
        resp.map { result =>
          status(result) mustBe (UNAUTHORIZED)
          contentType(result) mustBe (Some("application/json"))
          val json = Json.parse(contentAsString(result))
          (json \ "status").as[String] must be ("error")
          (json \ "message").as[String] must include("InvalidToken")
        }
        ()
      }

      "not allow mismatched tokens in request for an non asyncaction" in {
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
          contentType(result) mustBe (Some("application/json"))
          val json = Json.parse(contentAsString(result))
          (json \ "status").as[String] must be ("error")
          (json \ "message").as[String] must include("tokens did not match")
        }
        ()
      }

      "not allow mismatched tokens in request for an async action" in {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)

        val validToken = doLogin(user.email, plainPassword)
        val badToken = nameGenerator.next[String]

        val reqJson = Json.obj("expectedVersion" -> Some(user.version))

        // this request is valid since user is logged in
        var fakeRequest = FakeRequest(POST, updateUri(user, "lock"))
        .withJsonBody(reqJson)
        .withHeaders("X-XSRF-TOKEN" -> validToken)
        .withCookies(Cookie("XSRF-TOKEN", badToken))

        //log.info(s"makeRequest: request: $fakeRequest")

        val resp = route(app, fakeRequest)
        resp must not be (None)
        resp.map { result =>
          // log.info(s"makeRequest: status: ${status(result)}, result: ${contentAsString(result)}")
          status(result) mustBe (UNAUTHORIZED)
          contentType(result) mustBe (Some("application/json"))
          val json = Json.parse(contentAsString(result))
          (json \ "status").as[String] must be ("error")
          (json \ "message").as[String] must include("tokens did not match")
        }
        ()
      }

      "not allow requests missing XSRF-TOKEN cookie" in {
        val resp = route(app, FakeRequest(GET, uri))
        resp must not be (None)
        resp.map { result =>
          status(result) mustBe (UNAUTHORIZED)
          contentType(result) mustBe (Some("application/json"))
          val json = Json.parse(contentAsString(result))
          (json \ "status").as[String] must be ("error")
          (json \ "message").as[String] must include("Invalid XSRF Token cookie")
        }
        ()
      }

      "not allow requests missing X-XSRF-TOKEN in header" in {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)
        val token = doLogin(user.email, plainPassword)

        val resp = route(app, FakeRequest(GET, uri).withCookies(Cookie("XSRF-TOKEN", token)))
        resp must not be (None)
        resp.map { result =>
          status(result) mustBe (UNAUTHORIZED)
          contentType(result) mustBe (Some("application/json"))
          val json = Json.parse(contentAsString(result))
          (json \ "status").as[String] must be ("error")
          (json \ "message").as[String] must include("No token")
        }
        ()
      }
    }

    "POST /logout" must {

      "disallow access to logged out users" in {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)
        val token = doLogin(user.email, plainPassword)

        // this request is valid since user is logged in
        var json = makeRequest(GET, uri, OK, JsNull, token)
        val jsonList = (json \ "data" \ "items").as[List[JsObject]]
        jsonList must have size 1

        // the user is now logged out
        json = makeRequest(POST, "/logout", OK, JsNull, token)
        (json \ "status").as[String] must be ("success")

        // the following request must fail
        json = makeRequest(GET, uri, UNAUTHORIZED, JsNull, token)

        (json \ "status").as[String] must be ("error")
        (json \ "message").as[String] must include("InvalidToken")
      }
    }

    "POST /passreset" must {

      "allow an active user to reset his/her password" in {
        val user = createActiveUserInRepository(nameGenerator.next[String])
        val json = makeRequest(POST,
                               "/passreset",
                               Json.obj("email" -> user.email))
        (json \ "status").as[String] must be ("success")
      }

      "not allow a registered user to reset his/her password" in {
        val user = createRegisteredUserInRepository(nameGenerator.next[String])
        val reqJson = Json.obj("email" -> user.email)
        val json = makeRequest(POST, "/passreset", BAD_REQUEST, reqJson)

        (json \ "status").as[String] must be ("error")

        (json \ "message").as[String] must include regex("InvalidStatus.*not active")
      }

      "not allow a locked user to reset his/her password" in {
        val lockedUser = factory.createLockedUser
        userRepository.put(lockedUser)

        val reqJson = Json.obj("email" -> lockedUser.email)
        val json = makeRequest(POST, "/passreset", BAD_REQUEST, reqJson)
        (json \ "status").as[String] must be ("error")
        (json \ "message").as[String] must include regex("InvalidStatus.*not active")
      }

      "not allow a password reset on an invalid email address" in {
        val reqJson = Json.obj("email" -> nameGenerator.nextEmail[User])
        val json = makeRequest(POST, "/passreset", NOT_FOUND, reqJson)

        (json \ "status").as[String] must be ("error")

        (json \ "message").as[String] must include("EmailNotFound")
      }

    }

    "GET /authenticate" must {

      "allow a user to authenticate" in {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)

        val reqJson = Json.obj("email" -> user.email,
                               "password" -> plainPassword)
        val json = makeRequest(POST, "/login", json = reqJson)
        val tk = (json \ "data").as[String]
        tk.length must be > 0

        val authReplyJson = makeRequest(GET, "/authenticate", OK, JsNull, token = tk)
        (authReplyJson \ "status").as[String] must be ("success")
        (authReplyJson \ "data" \ "email").as[String] must be (user.email)
      }

      "not allow a registered user to authenticate" in {
        val plainPassword = nameGenerator.next[String]
        val user = createRegisteredUserInRepository(plainPassword)

        val reqJson = Json.obj("email" -> user.email,
                               "password" -> plainPassword)
        val json = makeRequest(POST, "/login", json = reqJson)
        val tk = (json \ "data").as[String]
        tk.length must be > 0

        val authReplyJson = makeRequest(GET, "/authenticate", UNAUTHORIZED, JsNull, token = tk)

        (authReplyJson \ "status").as[String] must be ("error")

        (authReplyJson \ "message").as[String] must include regex("InvalidStatus.*not active")
      }

      "not allow a locked user to authenticate" in {
        val plainPassword = nameGenerator.next[String]
        val activeUser = createActiveUserInRepository(plainPassword)

        val reqJson = Json.obj("email" -> activeUser.email,
                               "password" -> plainPassword)
        val json = makeRequest(POST, "/login", json = reqJson)
        val tk = (json \ "data").as[String]
        tk.length must be > 0

        val lockedUser = activeUser.lock | fail
        userRepository.put(lockedUser)

        val authReplyJson = makeRequest(GET, "/authenticate", UNAUTHORIZED, JsNull, token = tk)
        (authReplyJson \ "status").as[String] must be ("error")
        (authReplyJson \ "message").as[String] must include regex("InvalidStatus.*not active")
      }
    }

  }
}
