package org.biobank.controllers

import org.biobank.domain._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.service.json.JsonHelper._
import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.User._
import play.api.test.Helpers._
import play.api.test.WithApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

/**
  * Tests the REST API for [[User]].
  */
class UserControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  "User REST API" should {

    "GET /users" should {
      "list the default user in the test environment" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/users")
        val jsonList = json.as[List[JsObject]]
        jsonList should have size 1
        val jsonDefaultUser = jsonList(0)
        (jsonDefaultUser \ "email").as[String] should be ("admin@admin.com")
      }

      "list a new user" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val user = factory.createRegisteredUser
        appRepositories.userRepository.put(user)

        val json = makeRequest(GET, "/users")
        val jsonList = json.as[List[JsObject]]
        jsonList should have length 2
        compareObj(jsonList(1), user)
      }
    }

    "GET /users" should {
      "list multiple users" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val users = List(factory.createRegisteredUser, factory.createRegisteredUser)
        users.map(user => appRepositories.userRepository.put(user))

        val json = makeRequest(GET, "/users")
        val jsonList = json.as[List[JsObject]].filterNot { u =>
          (u \ "id").as[String].equals("admin@admin.com")
        }

        jsonList should have size users.size

        (jsonList zip users).map { item => compareObj(item._1, item._2) }
      }
    }

    "POST /users" should {
      "add a user" in new WithApplication(fakeApplication()) {
        doLogin
        val user = factory.createRegisteredUser
        val cmdJson = Json.obj(
          "type"      -> "RegisterUserCmd",
          "name"      -> user.name,
          "email"     -> user.email,
          "password"  -> "testpassword",
          "avatarUrl" -> user.avatarUrl)
        val json = makeRequest(POST, "/users", json = cmdJson)

        (json \ "message").as[String] should include ("user added")
      }
    }

    "PUT /users/:id" should {
      "update a user" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val user = factory.createRegisteredUser.activate(Some(0), DateTime.now) | fail
        appRepositories.userRepository.put(user)

        val cmdJson = Json.obj(
          "type"            -> "UpdateUserCmd",
          "id"              -> user.id.id,
          "expectedVersion" -> Some(user.version),
          "name"            -> user.name,
          "email"           -> user.email,
          "password"        -> "testpassword",
          "avatarUrl"       -> user.avatarUrl)
        val json = makeRequest(PUT, s"/users/${user.id.id}", json = cmdJson)

        (json \ "message").as[String] should include ("user updated")
      }
    }

    "GET /users/:id" should {
      "read a user" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val user = factory.createRegisteredUser.activate(Some(0), org.joda.time.DateTime.now) | fail
        appRepositories.userRepository.put(user)
        val json = makeRequest(GET, s"/users/${user.id.id}")
        compareObj(json, user)
      }
    }

    "PUT /users/activate" should {
      "activate a user" in new WithApplication(fakeApplication()) {
        doLogin

        val appRepositories = new AppRepositories

        val user = factory.createRegisteredUser
        appRepositories.userRepository.put(user)

        val cmdJson = Json.obj(
          "type"            -> "ActivateUserCmd",
          "expectedVersion" -> Some(user.version),
          "email"           -> user.id.id)
        val json = makeRequest(PUT, s"/users/activate/${user.id.id}", json = cmdJson)

        (json \ "message").as[String] should include ("user activated")
      }
    }

    "PUT /users/lock" should {
      "lock a user" in new WithApplication(fakeApplication()) {
        doLogin

        val appRepositories = new AppRepositories

        val user = factory.createRegisteredUser.activate(Some(0), DateTime.now) | fail
        appRepositories.userRepository.put(user)

        val cmdJson = Json.obj(
          "type"            -> "LockUserCmd",
          "expectedVersion" -> Some(user.version),
          "email"           -> user.id.id)
        val json = makeRequest(PUT, s"/users/lock/${user.id.id}", json = cmdJson)

        (json \ "message").as[String] should include ("user locked")
      }
    }

    "PUT /users/unlock" should {
      "should unlock a user" in new WithApplication(fakeApplication()) {
        doLogin

        val appRepositories = new AppRepositories

        val user = factory.createRegisteredUser.activate(Some(0), DateTime.now) | fail
        val lockedUser = user.lock(user.versionOption, DateTime.now) | fail
        appRepositories.userRepository.put(lockedUser)

        val cmdJson = Json.obj(
          "type"            -> "UnlockUserCmd",
          "expectedVersion" -> Some(lockedUser.version),
          "email"           -> lockedUser.id.id)
        val json = makeRequest(PUT, s"/users/unlock/${lockedUser.id.id}", json = cmdJson)

        (json \ "message").as[String] should include ("user unlocked")
      }
    }

  }
}
