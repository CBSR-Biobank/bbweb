package org.biobank.controllers.users

import java.time.OffsetDateTime
import org.biobank.Global
import org.biobank.controllers.PagedResultsSpec
import org.biobank.dto._
import org.biobank.dto.access._
import org.biobank.domain.access._
import org.biobank.domain.{JsonHelper, Slug}
import org.biobank.domain.users._
import org.biobank.fixtures.{ControllerFixture, Url}
import org.biobank.matchers.PagedResultsMatchers
import org.biobank.services.users.UserCountsByStatus
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.Inside
import play.api.libs.json._
import play.api.mvc.{Cookie, Cookies, Result}
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test._
import scala.concurrent.Future

/**
 * Tests the REST API for [[User]].
 *
 * NOTE: In some tests, this test suite does not check the role and membership information returned by the
 * server.
 */
class UsersControllerSpec
    extends ControllerFixture
    with JsonHelper
    with UserFixtures
    with PagedResultsMatchers
    with Inside {
  import org.biobank.TestUtils._
  import org.biobank.matchers.JsonMatchers._
  import org.biobank.matchers.EntityMatchers._
  import org.biobank.matchers.DtoMatchers._

  class activeUserFixture {
    val user = factory.createActiveUser
    userRepository.put(user)
    addMembershipForUser(user)
  }

  describe("Users REST API") {

    describe("GET /api/users/search") {

      describe("lists the default user") {
        listSingleUser() { () =>
          val defaultUser = userRepository.getByKey(Global.DefaultUserId).toOption.value
          (new Url(uri("search")), defaultUser)
        }
      }

      describe("list multiple users") {
        listMultipleUsers() { () =>
          val defaultUser = userRepository.getByKey(Global.DefaultUserId).toOption.value
          val users = (0 until 2).map(_ => factory.createRegisteredUser).toList
          users.foreach(userRepository.put)
          (new Url(uri("search")), defaultUser :: users)
        }
      }

      describe("list a single user when filtered by name") {
        listSingleUser() { () =>
          val users = List(factory.createRegisteredUser.copy(name = "user1"),
                           factory.createRegisteredUser.copy(name = "user2"))
          users.foreach(userRepository.put)
          (new Url(uri("search") + s"?filter=name::${users(0).name}"), users(0))
        }
      }

      describe("list a single user when filtered by email") {
        listSingleUser() { () =>
          val users = List(factory.createRegisteredUser.copy(email = "user1@test.com"),
                           factory.createRegisteredUser.copy(email = "user2@test.com"))
          users.foreach(userRepository.put)
          (new Url(uri("search") + s"?filter=email::${users(0).email}"), users(0))
        }
      }

      describe("when filtered by state") {

        def commonSetup = {
          val users = List(factory.createRegisteredUser.copy(email = "user1@test.com"),
                           factory.createActiveUser.copy(email = "user2@test.com"),
                           factory.createLockedUser.copy(email = "user3@test.com"))
          users.foreach(userRepository.put)
          users
        }


        describe("list registered users") {
          listMultipleUsers() { () =>
            (new Url(uri("search") + "?filter=state::registered"), List(commonSetup(0)))
          }
        }

        describe("list active users") {
          listMultipleUsers() { () =>
            val defaultUser = userRepository.getByKey(Global.DefaultUserId).toOption.value
            (new Url(uri("search") + "?filter=state::active"), List(defaultUser, commonSetup(1)))
          }
        }

        describe("list locked users") {
          listMultipleUsers() { () =>
            (new Url(uri("search") + "?filter=state::locked"), List(commonSetup(2)))
          }
        }
      }

      describe("when sorted by name") {
        def commonSetup = {
          val defaultUser = userRepository.getByKey(Global.DefaultUserId).toOption.value
          val users =
            defaultUser :: List(factory.createRegisteredUser.copy(name = "user3"),
                               factory.createRegisteredUser.copy(name = "user2"),
                               factory.createRegisteredUser.copy(name = "user1"))
          users.foreach(userRepository.put)
          users
        }

        describe("in acending order") {
          listMultipleUsers() { () =>
            val users = commonSetup.sortWith(_.name < _.name)
            (new Url(uri("search") + "?sort=name"), users)
          }
        }

        describe("in descending order") {
          listMultipleUsers() { () =>
            val users = commonSetup.sortWith(_.name > _.name)
            (new Url(uri("search") + "?sort=-name"), users)
          }
        }

      }

      describe("when sorted by email") {
        def commonSetup = {
          val defaultUser = userRepository.getByKey(Global.DefaultUserId).toOption.value
          val users =
            defaultUser :: List(factory.createRegisteredUser.copy(email = "user3@test.com"),
                               factory.createActiveUser.copy(email = "user2@test.com"),
                               factory.createActiveUser.copy(email = "user1@test.com"))
          users.foreach(userRepository.put)
          users
        }

        describe("in acending order") {
          listMultipleUsers() { () =>
            (new Url(uri("search") + "?sort=email"), commonSetup.sortWith(_.email < _.email))
          }
        }

        describe("in descending order") {
          listMultipleUsers() { () =>
            (new Url(uri("search") + "?sort=-email"), commonSetup.sortWith(_.email > _.email))
          }
        }

      }

      describe("when sorted by state") {
        def commonSetup = {
          val defaultUser = userRepository.getByKey(Global.DefaultUserId).toOption.value
          val users =
            defaultUser :: List(factory.createRegisteredUser,
                               factory.createLockedUser)
          users.foreach(userRepository.put)
          users
        }

        describe("in acending order") {
          listMultipleUsers() { () =>
            (new Url(uri("search") + "?sort=state"), commonSetup.sortWith(_.state.id < _.state.id))
          }
        }

        describe("in descending order") {
          listMultipleUsers() { () =>
            (new Url(uri("search") + "?sort=-state"), commonSetup.sortWith(_.state.id > _.state.id))
          }
        }
      }

      describe("list a single user when using paged query") {
        listSingleUser(maybeNext = Some(2)) { () =>
          val users = List(factory.createRegisteredUser.copy(email = "user3@test.com"),
                           factory.createLockedUser.copy(email = "user2@test.com"),
                           factory.createActiveUser.copy(email = "user1@test.com"))
          users.foreach(userRepository.put)
          (new Url(uri("search") + "?filter=email:like:test&sort=email&limit=1"), users(2))
        }
      }

      it("fail when using an invalid query parameters") {
        PagedResultsSpec(this).failWithInvalidParams(uri("search"))
      }

    }

    describe("GET /api/users/names") {

      class Fixture {
        val defaultUser = userRepository.getByKey(Global.DefaultUserId).toOption.value
        val users = (1 to 2).map {_ => factory.createActiveUser }.toSeq :+ defaultUser
        val nameDtos = users.map(NameAndStateDto.apply(_)).toSeq
        users.foreach(userRepository.put)
      }

      it("in ascending order") {
        val f = new Fixture
        val nameDtos = f.nameDtos.sortWith { (a, b) => (a.name compareToIgnoreCase b.name) < 0 }

        val reply = makeAuthRequest(GET, uri("names") + "?sort=name").value
        reply must beOkResponseWithJsonReply

        (contentAsJson(reply) \ "data").get must matchNameAndStateDtos (nameDtos)
      }

      it("in reverse order") {
        val f = new Fixture
        val nameDtos = f.nameDtos.sortWith { (a, b) => (a.name compareToIgnoreCase b.name) > 0 }

        val reply = makeAuthRequest(GET, uri("names") + "?sort=-name").value
        reply must beOkResponseWithJsonReply

        (contentAsJson(reply) \ "data").get must matchNameAndStateDtos (nameDtos)
      }

      it("must return user names filtered by name") {
        val users = (1 to 2).map {_ => factory.createActiveUser }.toSeq
        users.foreach(userRepository.put)
        val user = users(0)

        val reply = makeAuthRequest(GET, uri("names") + s"?filter=name::${user.name}").value
        reply must beOkResponseWithJsonReply

        (contentAsJson(reply) \ "data").get must matchNameAndStateDtos (Seq(NameAndStateDto(user)))
      }

    }

    describe("GET /api/users/counts") {

      it("return empty counts") {
        val reply = makeAuthRequest(GET, uri("counts")).value
        reply must beOkResponseWithJsonReply

        val counts = (contentAsJson(reply) \ "data").validate[UserCountsByStatus]
        counts must be (jsSuccess)
        counts.get must equal(UserCountsByStatus(1, 0, 1, 0))
      }

      it("return valid counts") {
        val users = List(factory.createRegisteredUser,
                         factory.createRegisteredUser,
                         factory.createRegisteredUser,
                         factory.createActiveUser,
                         factory.createActiveUser,
                         factory.createLockedUser)
        users.foreach { c => userRepository.put(c) }

        val reply = makeAuthRequest(GET, uri("counts")).value
        reply must beOkResponseWithJsonReply

        val counts = (contentAsJson(reply) \ "data").validate[UserCountsByStatus]
        counts must be (jsSuccess)
        counts.get must equal(UserCountsByStatus(7, 3, 2 + 1, 1)) // +1 to active for the default user
      }

    }

    describe("GET /api/users/:slug") {

      it("return a user") {
        val f = new activeUserFixture
        val reply = makeAuthRequest(GET, uri(f.user.slug.id)).value
        reply must beOkResponseWithJsonReply

        val replyDto = (contentAsJson(reply) \ "data").validate[UserDto]
        replyDto must be (jsSuccess)
        replyDto.get must matchDtoToUser (f.user)
      }

      it("return not found for an invalid user") {
        val user = factory.createActiveUser
        val reply = makeAuthRequest(GET, uri(user), JsNull)
        reply.value must beNotFoundWithMessage ("EntityCriteriaNotFound: user slug")
      }
    }

    describe("POST /api/users") {

      it("register a user") {
        val user = factory.createRegisteredUser
        val reqJson = Json.obj("name" -> user.name,
                               "email" -> user.email,
                               "password" -> "testpassword",
                               "avatarUrl" -> user.avatarUrl)
        val reply = makeAuthRequest(POST, uri(""), reqJson).value
        reply must beOkResponseWithJsonReply

        val replyDto = (contentAsJson(reply) \ "data").validate[UserDto]
        replyDto must be (jsSuccess)

        val userId = UserId(replyDto.get.id)
        userRepository.getByKey(userId) mustSucceed { repoStudy =>
          val updatedUser = user.copy(id = userId)
          replyDto.get must matchDtoToUser (updatedUser)
          repoStudy must matchUser (updatedUser)
        }
      }

      it("users with the same name (different emails) get different slugs") {
        val name = faker.Name.name
        val replyDtos = (0 until 2).map { _ =>
            val user = factory.createActiveUser.copy(name = name)
            val reqJson = Json.obj("name" -> user.name,
                                   "email" -> user.email,
                                   "password" -> "testpassword",
                                   "avatarUrl" -> user.avatarUrl)
            val reply = makeAuthRequest(POST, uri(""), reqJson).value
            reply must beOkResponseWithJsonReply

            val replyDto = (contentAsJson(reply) \ "data").validate[UserDto]
            replyDto must be (jsSuccess)
            replyDto.get
          }

        replyDtos(0).id must not equal(replyDtos(1).id)
        replyDtos(0).slug must not equal(replyDtos(1).slug)
        replyDtos(0).name must equal(replyDtos(1).name)
      }

      it("cannot register a user with an email address already in the system") {
        val user = factory.createActiveUser
        userRepository.put(user)
        val reqJson = Json.obj("name" -> user.name,
                               "email" -> user.email,
                               "password" -> "testpassword",
                               "avatarUrl" -> user.avatarUrl)
        val reply = makeAuthRequest(POST, uri(""), reqJson).value
        reply must beForbiddenRequestWithMessage("email already registered")
      }

      it("not add a user with an empty email address") {
        val user = factory.createActiveUser
        userRepository.put(user)
        val reqJson = Json.obj("name" -> user.name,
                               "email" -> "",
                               "password" -> "testpassword",
                               "avatarUrl" -> user.avatarUrl)
        val reply = makeAuthRequest(POST, uri(""), reqJson).value
        reply must beBadRequestWithMessage("InvalidEmail")
      }

    }

    describe("POST /api/users/update/:id") {

      describe("when updating name") {

        it("update a user's name") {
          val user = factory.createActiveUser
          userRepository.put(user)
          val newName = s"${faker.Name.first_name} ${faker.Name.last_name}"
          val updatedUser = user.copy(version      = user.version + 1,
                                      name         = newName,
                                      slug         = Slug(newName),
                                      timeModified = Some(OffsetDateTime.now))

          val reply = makeUpdateRequest(user, "name", JsString(newName)).value
          reply must beOkResponseWithJsonReply

          val replyDto = (contentAsJson(reply) \ "data").validate[UserDto]
          replyDto must be (jsSuccess)
          replyDto.get must matchDtoToUser (updatedUser)

          userRepository.getByKey(user.id) mustSucceed { repoUser =>
            repoUser must matchUser(updatedUser)
          }
        }

        it("users with the same name (different emails) get different slugs") {
          val users = (0 until 2).map { _ =>
              val user = factory.createActiveUser
              userRepository.put(user)
              user
            }

          val dupName = users(1).name
          val reply = makeUpdateRequest(users(0), "name", JsString(dupName)).value
          reply must beOkResponseWithJsonReply

          val replyDto = (contentAsJson(reply) \ "data").validate[UserDto]
          replyDto must be (jsSuccess)

          replyDto.get.id must equal (users(0).id.id)
          replyDto.get.slug must not equal (Slug(dupName))
          replyDto.get.slug.toString must include (Slug(dupName).toString)
          replyDto.get.name must equal (dupName)
        }

        it("not update a user's name when an invalid version number is used") {
          val user = factory.createActiveUser
          userRepository.put(user)
          val json = Json.obj("expectedVersion" -> (user.version + 1L),
                              "property"        -> "name",
                              "newValue"        -> user.name)

          val reply = makeAuthRequest(POST, updateUri(user, "update"), json)
          reply.value must beBadRequestWithMessage("InvalidVersion")
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
            val reply = makeAuthRequest(POST, updateUri(user, "update"), json)
            reply.value must beBadRequestWithMessage(errMsg)
          }
        }
      }

      describe("when updating email") {

        it("update a user's email") {
          val user = factory.createActiveUser.copy(timeAdded = OffsetDateTime.now.minusMonths(1))
          userRepository.put(user)
          val newEmail = nameGenerator.nextEmail[User]
          val updatedUser = user.copy(version      = user.version + 1,
                                      email        = newEmail,
                                      timeModified = Some(OffsetDateTime.now))

          val reply = makeUpdateRequest(user, "email", JsString(newEmail)).value
          reply must beOkResponseWithJsonReply

          val replyDto = (contentAsJson(reply) \ "data").validate[UserDto]
          replyDto must be (jsSuccess)
          replyDto.get must matchDtoToUser (updatedUser)

          userRepository.getByKey(user.id) mustSucceed { _ must matchUser(updatedUser) }
        }

        it("not update a user's email with an invalid email address") {
          val user = factory.createActiveUser
          userRepository.put(user)
          val json = Json.obj("expectedVersion" -> user.version,
                              "property"        -> "email",
                              "newValue"        -> faker.Lorem.sentence(3))
          val reply = makeAuthRequest(POST, updateUri(user, "update"), json)
          reply.value must beBadRequestWithMessage("InvalidEmail")
        }

        it("not update a user's email if an invalid version number is used") {
          val user = factory.createActiveUser
          userRepository.put(user)
          val json = Json.obj("expectedVersion" -> (user.version + 10L),
                              "property"        -> "email",
                              "newValue"        -> user.email)
          val reply = makeAuthRequest(POST, updateUri(user, "update"), json)
          reply.value must beBadRequestWithMessage("InvalidVersion")
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

          val updatedUser = user.copy(version      = user.version + 1,
                                      timeModified = Some(OffsetDateTime.now))

          val reqJson = Json.obj("currentPassword" -> plainPassword,
                                 "newPassword"     -> newPassword)
          val reply = makeUpdateRequest(user, "password", reqJson).value
          reply must beOkResponseWithJsonReply

          val replyDto = (contentAsJson(reply) \ "data").validate[UserDto]
          replyDto must be (jsSuccess)
          replyDto.get must matchDtoToUser (updatedUser)

          userRepository.getByKey(user.id) mustSucceed { repoUser =>
            repoUser must matchUser(updatedUser)
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
            val reply = makeAuthRequest(POST, updateUri(user, "update"), json)
            reply.value must beBadRequestWithMessage("InvalidPassword")
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
          val reply = makeAuthRequest(POST, updateUri(user, "update"), json)
          reply.value must beBadRequestWithMessage ("InvalidNewPassword")
        }

        it("fail when attempting to update a user's password with a bad version number") {
          val plainPassword = nameGenerator.next[String]
          val user = createActiveUserInRepository(plainPassword)
          val newValue = Json.obj("currentPassword" -> plainPassword,
                                  "newPassword"     -> faker.Lorem.sentence(3))
          val json = Json.obj("expectedVersion" -> (user.version + 10L),
                              "property"        -> "password",
                              "newValue"        -> newValue)
          val reply = makeAuthRequest(POST, updateUri(user, "update"), json)
          reply.value must beBadRequestWithMessage("InvalidVersion")
        }
      }

      describe("when updating avatar URL") {

        it("update a user's avatar URL") {
          val user = factory.createActiveUser.copy(timeAdded = OffsetDateTime.now.minusMonths(1))
          userRepository.put(user)
          val newAvatarUrl = nameGenerator.nextUrl[User]

          val updatedUser = user.copy(version      = user.version + 1,
                                      avatarUrl    = Some(newAvatarUrl),
                                      timeModified = Some(OffsetDateTime.now))

          val reply = makeUpdateRequest(user, "avatarUrl", JsString(newAvatarUrl)).value
          reply must beOkResponseWithJsonReply

          val replyDto = (contentAsJson(reply) \ "data").validate[UserDto]
          replyDto must be (jsSuccess)
          replyDto.get must matchDtoToUser (updatedUser)

          userRepository.getByKey(user.id) mustSucceed { repoUser =>
            repoUser must matchUser(updatedUser)
          }
        }

        it("remove a user's avatar URL") {
          val user = factory.createActiveUser
          userRepository.put(user)

          val updatedUser = user.copy(version      = user.version + 1,
                                      avatarUrl    = None,
                                      timeModified = Some(OffsetDateTime.now))

          val reply = makeUpdateRequest(user, "avatarUrl", JsString("")).value
          reply must beOkResponseWithJsonReply

          val replyDto = (contentAsJson(reply) \ "data").validate[UserDto]
          replyDto must be (jsSuccess)
          replyDto.get must matchDtoToUser (updatedUser)

          userRepository.getByKey(user.id) mustSucceed { repoUser =>
            repoUser must matchUser(updatedUser)
          }
        }

        it("not update a user's avatar URL if URL is invalid") {
          val user = factory.createActiveUser
          userRepository.put(user)
          val json = Json.obj("expectedVersion" -> user.version,
                              "property"        -> "avatarUrl",
                              "newValue"        -> "bad url")
          val reply = makeAuthRequest(POST, updateUri(user, "update"), json)
          reply.value must beBadRequestWithMessage(
            "InvalidUrl")
        }

        it("not update a user's avatar URL if an invalid version number is used") {
          val user = factory.createActiveUser
          userRepository.put(user)
          val json = Json.obj("expectedVersion" -> (user.version + 10L),
                              "property"        -> "avatarUrl",
                              "newValue"        -> nameGenerator.nextUrl[User])
          val reply = makeAuthRequest(POST, updateUri(user, "update"), json)
          reply.value must beBadRequestWithMessage(
            "InvalidVersion")
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

    describe("POST /api/users/roles/:userId") {

      def addRoleToUserJson(user: User, role: Role): JsObject = {
        Json.obj("expectedVersion" -> user.version,
                 "roleId"          -> role.id)
      }

      it("can add a role to a user") {
        val user = factory.createActiveUser
        val role = factory.createRole
        Set(user, role).foreach(addToRepository)

        val reply = makeAuthRequest(POST, uri("roles", user.id.id), addRoleToUserJson(user, role)).value
        reply must beOkResponseWithJsonReply

        val updatedRole = role.copy(version      = user.version + 1,
                                    timeModified = Some(OffsetDateTime.now))

        val replyDto = (contentAsJson(reply) \ "data").validate[UserDto]
        replyDto must be (jsSuccess)
        replyDto.get must matchDtoToUser (user)

        userRepository.getByKey(user.id) mustSucceed { repoUser =>
          repoUser must matchUser(user)
        }

        val roleDtos = (contentAsJson(reply) \ "data" \ "roles").validate[Set[UserRoleDto]]
        roleDtos must be (jsSuccess)

        roleDtos.get.size must be (1)
        roleDtos.get.foreach { _ must matchDtoToRole (updatedRole) }
      }

      it("cannot add the same role more than once") {
        val user = factory.createActiveUser
        val role = factory.createRole.copy(userIds = Set(user.id))
        Set(user, role).foreach(addToRepository)

        val reply = makeAuthRequest(POST,
                                    uri("roles", user.id.id),
                                    addRoleToUserJson(user, role))
        reply.value must beBadRequestWithMessage("EntityCriteriaError: user ID is already in role")
      }

      it("cannot add a role to a user that does not exist") {
        val user = factory.createActiveUser
        val role = factory.createRole.copy(userIds = Set(user.id))
        Set(role).foreach(addToRepository)
        val reply = makeAuthRequest(POST,
                                    uri("roles", user.id.id),
                                    addRoleToUserJson(user, role))
        reply.value must beNotFoundWithMessage("IdNotFound: user id")
      }

      it("cannot add a role that does not exist") {
        val user = factory.createActiveUser
        val role = factory.createRole.copy(userIds = Set(user.id))
        Set(user).foreach(addToRepository)
        val reply = makeAuthRequest(POST,
                                    uri("roles", user.id.id),
                                    addRoleToUserJson(user, role))
        reply.value must beNotFoundWithMessage ("IdNotFound: role id")
      }

      it("cannot add a role to a user with a wrong user version") {
        val user = factory.createActiveUser
        val role = factory.createRole
        Set(user, role).foreach(addToRepository)

        val reply = makeAuthRequest(POST,
                                    uri("roles", user.id.id),
                                    addRoleToUserJson(user, role) ++ Json.obj("expectedVersion" -> (user.version + 10L)))
        reply.value must beBadRequestWithMessage(
          "InvalidVersion.*ActiveUser: expected version doesn't match current version")
      }

    }

    describe("POST /api/users/memberships/:userId") {

      def addMembershipToUserJson(user: User, membership: Membership): JsObject = {
        Json.obj("expectedVersion" -> user.version,
                 "membershipId"    -> membership.id)
      }

      it("can add a membership to a user") {
        val user = factory.createActiveUser
        val membership = factory.createMembership
        Set(user, membership).foreach(addToRepository)

        val reply = makeAuthRequest(POST,
                                    uri("memberships", user.id.id),
                                    addMembershipToUserJson(user, membership)).value
        reply must beOkResponseWithJsonReply

        val userDto = (contentAsJson(reply) \ "data").validate[UserDto]
        userDto must be (jsSuccess)

        userDto.get must matchDtoToUser(user)
        userRepository.getByKey(user.id) mustSucceed { repoUser =>
          repoUser must matchUser(user)
        }

        val updatedMembership = membership.copy(version = membership.version + 1,
                                                timeModified = Some(OffsetDateTime.now))
        val userMembership = UserMembership.create(updatedMembership, user.id)
        val membershipDto = (contentAsJson(reply) \ "data" \ "membership").validate[UserMembershipDto]
        membershipDto must be (jsSuccess)
        membershipDto.get must matchDtoToUserMembership(userMembership)
      }

      it("if user is member of another membership, they are removed when added to a new one") {
        val user = factory.createActiveUser
        val membershipExisting = factory.createMembership.copy(userIds = Set(user.id))
        val membershipNew = factory.createMembership
        Set(user, membershipExisting, membershipNew).foreach(addToRepository)

        val reply = makeAuthRequest(POST,
                                    uri("memberships", user.id.id),
                                    addMembershipToUserJson(user, membershipNew)).value
        reply must beOkResponseWithJsonReply

        val userDto = (contentAsJson(reply) \ "data").validate[UserDto]
        userDto must be (jsSuccess)

        userDto.get must matchDtoToUser(user)
        userRepository.getByKey(user.id) mustSucceed { repoUser =>
          repoUser must matchUser(user)
        }

        val updatedNewMembership = membershipNew.copy(version = membershipNew.version + 1,
                                                      userIds = Set(user.id),
                                                      timeModified = Some(OffsetDateTime.now))
        val userMembership = UserMembership.create(updatedNewMembership, user.id)
        val membershipDto = (contentAsJson(reply) \ "data" \ "membership").validate[UserMembershipDto]
        membershipDto must be (jsSuccess)
        membershipDto.get must matchDtoToUserMembership(userMembership)

        membershipRepository.getByKey(membershipExisting.id) mustSucceed { m =>
          m.userIds.find(_ == user.id) mustBe None
        }

        membershipRepository.getByKey(membershipNew.id) mustSucceed { m =>
          m.userIds.find(_ == user.id) must be ('defined)
        }
      }

      it("cannot add the same user more than once to a membership") {
        val user = factory.createActiveUser
        val membership = factory.createMembership.copy(userIds = Set(user.id))
        Set(user, membership).foreach(addToRepository)

        val reply = makeAuthRequest(POST,
                                    uri("memberships", user.id.id),
                                    addMembershipToUserJson(user, membership)).value
        reply must beBadRequestWithMessage ("EntityCriteriaError: user ID is already in membership")
      }

      it("cannot add a user that does not exist to a membership") {
        val user = factory.createActiveUser
        val membership = factory.createMembership.copy(userIds = Set(user.id))
        Set(membership).foreach(addToRepository)

        val reply = makeAuthRequest(POST,
                                    uri("memberships", user.id.id),
                                    addMembershipToUserJson(user, membership))
        reply.value must beNotFoundWithMessage ("IdNotFound: user id")
      }

      it("cannot add a membership that does not exist") {
        val user = factory.createActiveUser
        val membership = factory.createMembership.copy(userIds = Set(user.id))
        Set(user).foreach(addToRepository)

        val reply = makeAuthRequest(POST,
                                    uri("memberships", user.id.id),
                                    addMembershipToUserJson(user, membership))
        reply.value must beNotFoundWithMessage ("IdNotFound: membership id")
      }

      it("cannot add a role to a user with a wrong user version") {
        val user = factory.createActiveUser
        val membership = factory.createMembership
        Set(user, membership).foreach(addToRepository)

        val reply = makeAuthRequest(
            POST,
            uri("memberships", user.id.id),
            addMembershipToUserJson(user, membership) ++ Json.obj("expectedVersion" -> (user.version + 10L))
          )
        reply.value must beBadRequestWithMessage(
          "InvalidVersion.*ActiveUser: expected version doesn't match current version")
      }

    }

    describe("POST /api/users/login") {

      it("allow a user to log in") {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)

        val reqJson = Json.obj("email" -> user.email,
                               "password" -> plainPassword)
        val reply = makeAuthRequest(POST, uri("login"), reqJson).value
        reply must beOkResponseWithJsonReply

        val dto = (contentAsJson(reply) \ "data").validate[UserDto]
        dto must be (jsSuccess)
        dto.get.email must equal (user.email)
      }

      it("prevent an invalid user from logging in") {
        val invalidUser = nameGenerator.nextEmail[String]
        val reqJson = Json.obj("email" -> invalidUser,
                               "password" -> nameGenerator.next[String])
        val reply = makeAuthRequest(POST, uri("login"), reqJson).value
        reply must beUnauthorizedNoContent
      }

      it("prevent a user logging in with bad password") {
        val user = createRegisteredUserInRepository(nameGenerator.next[String])
        val invalidPassword = nameGenerator.next[String]
        val reqJson = Json.obj("email" -> user.email,
                               "password" -> invalidPassword)
        val reply = makeAuthRequest(POST, uri("login"), reqJson).value
        reply must beUnauthorizedNoContent
      }

      it("not allow a locked user to log in") {
        val plainPassword = nameGenerator.next[User]
        val lockedUser = createLockedUserInRepository(plainPassword)

        val reqJson = Json.obj("email" -> lockedUser.email,
                               "password" -> plainPassword)
        val reply = makeAuthRequest(POST, uri("login"), reqJson).value
        reply must beUnauthorizedNoContent
      }

      it("not allow a request with an invalid token") {
        val badToken = nameGenerator.next[String]

        // this request is valid since user is logged in
        val fakeRequest = FakeRequest(GET, uri(""))
          .withHeaders("X-XSRF-TOKEN" -> badToken,
                       "Set-Cookie" -> Cookies.encodeCookieHeader(Seq(Cookie("XSRF-TOKEN", badToken))))
        val reply = route(app, fakeRequest).value
        reply must beUnauthorizedNoContent
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
        val reply = route(app, fakeRequest).value
        reply must beUnauthorizedNoContent
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

        val reply = route(app, fakeRequest).value
        reply must beUnauthorizedNoContent
      }

      it("not allow requests missing XSRF-TOKEN cookie") {
        val reply = route(app, FakeRequest(GET, uri(""))).value
        reply must beUnauthorizedNoContent
      }

      it("not allow requests missing X-XSRF-TOKEN in header") {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)
        val token = doLogin(user.email, plainPassword)

        val fakeRequest = FakeRequest(GET, uri(""))
          .withHeaders("Set-Cookie" -> Cookies.encodeCookieHeader(Seq(Cookie("XSRF-TOKEN", token))))
        val reply = route(app, fakeRequest).value
        reply must beUnauthorizedNoContent
      }
    }

    describe("POST /api/logout") {

      it("disallow access to logged out users") {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)
        val token = doLogin(user.email, plainPassword)

        // this request is valid since user is logged in
        var reply = makeAuthRequest(GET, uri("authenticate"), JsNull, token).value
        reply must beOkResponseWithJsonReply

        // the user is now logged out
        reply = makeAuthRequest(POST, uri("logout"), JsNull, token).value
        reply must beOkResponseWithJsonReply

        // the following request must fail
        reply = makeAuthRequest(GET, uri(""), JsNull, token).value
        reply must beUnauthorizedNoContent
      }

    }

    describe("POST /api/users/passreset") {

      it("allow an active user to reset his/her password") {
        val user = createActiveUserInRepository(nameGenerator.next[String])
        val reply = makeAuthRequest(POST,
                                    uri("passreset"),
                                    Json.obj("email" -> user.email)).value
        reply must beOkResponseWithJsonReply
      }

      it("not allow a registered user to reset his/her password") {
        val user = createRegisteredUserInRepository(nameGenerator.next[String])
        val reqJson = Json.obj("email" -> user.email)
        val reply = makeAuthRequest(POST, uri("passreset"), reqJson).value
        reply must beUnauthorizedNoContent
      }

      it("not allow a locked user to reset his/her password") {
        val lockedUser = factory.createLockedUser
        userRepository.put(lockedUser)

        val reqJson = Json.obj("email" -> lockedUser.email)
        val reply = makeAuthRequest(POST, uri("passreset"), reqJson).value
        reply must beUnauthorizedNoContent
      }

      it("not allow a password reset on an invalid email address") {
        val reqJson = Json.obj("email" -> nameGenerator.nextEmail[User])
        val reply = makeAuthRequest(POST, uri("passreset"), reqJson).value
        reply must beUnauthorizedNoContent
      }

    }

    describe("GET /api/users/authenticate") {

      it("allow a user to authenticate") {
        val plainPassword = nameGenerator.next[String]
        val user = createActiveUserInRepository(plainPassword)
        val token = doLogin(user.email, plainPassword)

        val reply = makeAuthRequest(GET, uri("authenticate"), JsNull, token).value
        reply must beOkResponseWithJsonReply

        val dto = (contentAsJson(reply) \ "data").validate[UserDto]
        dto must be (jsSuccess)
        dto.get.email must equal (user.email)
      }

      it("not allow a locked user to authenticate") {
        val plainPassword = nameGenerator.next[String]
        val activeUser = createActiveUserInRepository(plainPassword)
        val token = doLogin(activeUser.email, plainPassword)
        token.length must be > 0

        val lockedUser = activeUser.lock.toOption.value
        userRepository.put(lockedUser)

        val reply = makeAuthRequest(GET, uri("authenticate"), JsNull, token).value
        reply must beUnauthorizedNoContent
      }
    }

    describe("GET /api/users/studies") {

      // these tests can only test the studies the default user has access to, since the
      // test framework logs in as the default user.
      //
      // Tests for other types of users can be found in AccessServiceSpec.

      it("returns no studies for default user") {
        val reply = makeAuthRequest(GET, uri("studies")).value
        reply must beOkResponseWithJsonReply
        val dtos = (contentAsJson(reply) \ "data").validate[Seq[NameAndStateDto]]
        dtos must be (jsSuccess)
        dtos.get must equal (Seq.empty[NameAndStateDto])

      }

      it("returns a study for the default user") {
        val study = factory.createEnabledStudy
        studyRepository.put(study)
        val reply = makeAuthRequest(GET, uri("studies")).value
        reply must beOkResponseWithJsonReply
        val dtos = (contentAsJson(reply) \ "data").validate[Seq[NameAndStateDto]]
        dtos must be (jsSuccess)
        dtos.get must equal (Seq(NameAndStateDto(study)))
      }

    }

    describe("DELETE /api/users/roles/:userId/:version/:roleId") {

      it("can remove a role from a user") {
        val user = factory.createActiveUser
        val role = factory.createRole.copy(userIds = Set(user.id))
        Set(user, role).foreach(addToRepository)

        val url = uri("roles", user.id.id, user.version.toString, role.id.id)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beOkResponseWithJsonReply

        val replyDto = (contentAsJson(reply) \ "data").validate[UserDto]
        replyDto must be (jsSuccess)
        replyDto.get must matchDtoToUser (user)

        userRepository.getByKey(user.id) mustSucceed { repoUser =>
          repoUser must matchUser(user)
        }

        val roleDtos = (contentAsJson(reply) \ "data" \ "roles").validate[Set[UserRoleDto]]
        roleDtos must be (jsSuccess)
        roleDtos.get.size must be (0)

        accessItemRepository.getByKey(role.id) mustSucceed { item =>
          inside(item) { case repoRole: Role =>
            repoRole must have (
              'id             (role.id),
              'version        (role.version + 1)
            )
            repoRole.userIds must not contain (user.id)
            repoRole must beEntityWithTimeStamps(OffsetDateTime.now, Some(OffsetDateTime.now), 5L)
          }
        }
      }

      it("cannot remove a user not in the role") {
        val user = factory.createActiveUser
        val role = factory.createRole
        Set(user, role).foreach(addToRepository)

        val url = uri("roles", user.id.id, user.version.toString, role.id.id)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage("EntityCriteriaError: user ID is not in role")
      }

      it("cannot remove a user that does not exist") {
        val user = factory.createActiveUser
        val role = factory.createRole
        Set(role).foreach(addToRepository)

        val url = uri("roles", user.id.id, user.version.toString, role.id.id)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beNotFoundWithMessage("IdNotFound: user id")}

      it("111 fail when removing and role ID does not exist") {
        val user = factory.createActiveUser
        val role = factory.createRole
        Set(user).foreach(addToRepository)

        val url = uri("roles", user.id.id, user.version.toString, role.id.id)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beNotFoundWithMessage("IdNotFound: role id")}

      it("cannot remove a role to a user with a wrong user version") {
        val user = factory.createActiveUser
        val role = factory.createRole.copy(userIds = Set(user.id))
        Set(user, role).foreach(addToRepository)

        val url = uri("roles", user.id.id, (user.version + 10L).toString, role.id.id)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage(
          "InvalidVersion.*ActiveUser: expected version doesn't match current version")
      }

    }

    describe("DELETE /api/users/memberships/:userId/:version/:membershipId") {

      it("can remove a membership from a user") {
        val user = factory.createActiveUser
        val membership = factory.createMembership.copy(userIds = Set(user.id))
        Set(user, membership).foreach(addToRepository)

        val url = uri("memberships", user.id.id, user.version.toString, membership.id.id)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beOkResponseWithJsonReply

        val userDto = (contentAsJson(reply) \ "data").validate[UserDto]
        userDto must be (jsSuccess)

        userDto.get must matchDtoToUser(user)
        userRepository.getByKey(user.id) mustSucceed { repoUser =>
          repoUser must matchUser(user)
        }

        val membershipDto = (contentAsJson(reply) \ "membership").validate[UserMembershipDto]
        membershipDto mustBe a [JsError]

        membershipRepository.getByKey(membership.id) mustSucceed { item =>
          inside(item) { case repoMembership: Membership =>
            repoMembership must have (
              'id             (membership.id),
              'version        (membership.version + 1)
            )
            repoMembership.userIds must not contain (user.id)
            repoMembership must beEntityWithTimeStamps(OffsetDateTime.now, Some(OffsetDateTime.now), 5L)
          }
        }
      }

      it("cannot remove a user not in the membership") {
        val user = factory.createActiveUser
        val membership = factory.createMembership
        Set(user, membership).foreach(addToRepository)

        val url = uri("memberships", user.id.id, user.version.toString, membership.id.id)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage(
          "EntityCriteriaError: user ID is not in membership")
      }

      it("cannot remove a user that does not exist") {
        val user = factory.createActiveUser
        val membership = factory.createMembership
        Set(membership).foreach(addToRepository)

        val url = uri("memberships", user.id.id, user.version.toString, membership.id.id)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beNotFoundWithMessage("IdNotFound: user id")}

      it("fail when removing and membership ID does not exist") {
        val user = factory.createActiveUser
        val membership = factory.createMembership
        Set(user).foreach(addToRepository)

        val url = uri("memberships", user.id.id, user.version.toString, membership.id.id)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beNotFoundWithMessage("IdNotFound: membership id")}

      it("cannot remove a membership to a user with a wrong user version") {
        val user = factory.createActiveUser
        val membership = factory.createMembership.copy(userIds = Set(user.id))
        Set(user, membership).foreach(addToRepository)

        val url = uri("memberships", user.id.id, (user.version + 10L).toString, membership.id.id)
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage(
          "InvalidVersion.*ActiveUser: expected version doesn't match current version")
      }
    }

  }

  def userChangeStateSharedBehaviour(user:            User,
                                     wrongStateUsers: List[User],
                                     stateAction:     String,
                                     newState:        String) {

    it(s"can $stateAction a user") {
      userRepository.put(user)
      val reply = makeUpdateRequest(user, "state", JsString(stateAction)).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
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

        repoUser must beEntityWithTimeStamps(user.timeAdded, Some(OffsetDateTime.now), 5L)
      }
    }

    it("must not change a user's state with an invalid version number") {
      userRepository.put(user)
      val json = Json.obj("expectedVersion" -> (user.version + 10L),
                          "property"        -> "state",
                          "newValue"        -> stateAction)
      val reply = makeAuthRequest(POST, updateUri(user, "update"), json).value
      reply must beBadRequestWithMessage("InvalidVersion")
    }

    it("must not change a user to the wrong state") {
      forAll(Table("user in wrong state", wrongStateUsers:_*)) { user =>
        info(s"must not $stateAction a user currently in ${user.state} state")
        userRepository.put(user)

        val json = Json.obj("expectedVersion" -> user.version,
                            "property"        -> "state",
                            "newValue"        -> stateAction)
        val reply = makeAuthRequest(POST, updateUri(user, "update"), json)
        reply.value must beBadRequestWithMessage("InvalidStatus")
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

  def makeUpdateRequest(user: User, property: String, newValue: JsValue): Option[Future[Result]] = {
    var json = Json.obj("expectedVersion" -> user.version,
                        "property"        -> property)

    if (newValue !== JsNull) {
      json = json ++ Json.obj("newValue" -> newValue)
    }
    makeAuthRequest(POST, updateUri(user, "update"), json)
  }

  private def listSingleUser(offset:    Long = 0,
                             maybeNext: Option[Int] = None,
                             maybePrev: Option[Int] = None)
                            (setupFunc: () => (Url, User)) = {

    it("list single user") {
      val (url, expectedUser) = setupFunc()
      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beSingleItemResults(offset, maybeNext, maybePrev)

      val replyDtos = (json \ "data" \ "items").validate[List[UserDto]]
      replyDtos must be (jsSuccess)
      replyDtos.get.foreach { _ must matchDtoToUser (expectedUser) }
    }
  }

  private def listMultipleUsers(offset:    Long = 0,
                                maybeNext: Option[Int] = None,
                                maybePrev: Option[Int] = None)
                               (setupFunc: () => (Url, List[User])) = {

    it("list multiple users") {
      val (url, expectedUsers) = setupFunc()

      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beMultipleItemResults(offset = offset,
                                      total = expectedUsers.size.toLong,
                                      maybeNext = maybeNext,
                                      maybePrev = maybePrev)

      val replyDtos = (json \ "data" \ "items").validate[List[UserDto]]
      replyDtos must be (jsSuccess)

      (replyDtos.get zip expectedUsers).foreach { case (replyDto, user) =>
        replyDto must matchDtoToUser (user)
      }
    }

  }

}
