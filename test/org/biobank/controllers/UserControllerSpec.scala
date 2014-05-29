package org.biobank.controllers

import org.biobank.infrastructure.command.UserCommands._
import org.biobank.service.json.JsonHelper._
import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.User._
import play.api.test.Helpers._
import play.api.test.FakeApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

/**
  * Tests the REST API for [[User]].
  */
class UserControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  describe("User REST API") {

    describe("GET /users") {
      it("should list none") {
        running(fakeApplication) {
          val json = makeJsonRequest(GET, "/users")
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 0
        }
      }

      it("should list a user") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val user = factory.createRegisteredUser
          appRepositories.userRepository.put(user)

          val json = makeJsonRequest(GET, "/users")
          val jsonList = json.as[List[JsObject]]
          jsonList should have length 1
          compareObj(jsonList(0), user)
        }
      }
    }

    describe("GET /users") {
      it("should list multiple users") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val users = List(factory.createRegisteredUser, factory.createRegisteredUser)
          log.info(s"user: $users")
          //appRepositories.userRepository.removeAll
          users.map(user => appRepositories.userRepository.put(user))

          val json = makeJsonRequest(GET, "/users")
          val jsonList = json.as[List[JsObject]]
          jsonList should have size users.size

          (jsonList zip users).map { item => compareObj(item._1, item._2) }
        }
      }
    }

    describe("POST /users") {
      it("should add a user") {
        running(fakeApplication) {
          val user = factory.createRegisteredUser
          val cmdJson = Json.obj(
            "type"      -> "RegisterUserCmd",
            "name"      -> user.name,
            "email"     -> user.email,
            "password"  -> "testpassword",
            "avatarUrl" -> user.avatarUrl)
          val json = makeJsonRequest(POST, "/users", json = cmdJson)

          (json \ "message").as[String] should include ("user added")
        }
      }
    }

    describe("PUT /users/:id") {
      it("should update a user") {
        running(fakeApplication) {
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
          val json = makeJsonRequest(PUT, s"/users/${user.id.id}", json = cmdJson)

          (json \ "message").as[String] should include ("user updated")
        }
      }
    }

    describe("GET /users/:id") {
      it("should read a user") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val user = factory.createRegisteredUser.activate(Some(0), org.joda.time.DateTime.now) | fail
          appRepositories.userRepository.put(user)
          val json = makeJsonRequest(GET, s"/users/${user.id.id}")
          compareObj(json, user)
        }
      }
    }

    describe("PUT /users/activate") {
      it("should activate a user") {
        running(fakeApplication) {

          val appRepositories = new AppRepositories

          val user = factory.createRegisteredUser
          appRepositories.userRepository.put(user)

          val cmdJson = Json.obj(
            "type"            -> "ActivateUserCmd",
            "expectedVersion" -> Some(user.version),
            "email"           -> user.id.id)
          val json = makeJsonRequest(PUT, s"/users/activate/${user.id.id}", json = cmdJson)

          (json \ "message").as[String] should include ("user activated")
        }
      }
    }

    describe("PUT /users/lock") {
      it("should lock a user") {
        running(fakeApplication) {

          val appRepositories = new AppRepositories

          val user = factory.createRegisteredUser.activate(Some(0), DateTime.now) | fail
          appRepositories.userRepository.put(user)

          val cmdJson = Json.obj(
            "type"            -> "LockUserCmd",
            "expectedVersion" -> Some(user.version),
            "email"           -> user.id.id)
          val json = makeJsonRequest(PUT, s"/users/lock/${user.id.id}", json = cmdJson)

          (json \ "message").as[String] should include ("user locked")
        }
      }
    }

    describe("PUT /users/unlock") {
      it("should unlock a user", Tag("single")) {
        running(fakeApplication) {

          val appRepositories = new AppRepositories

          val user = factory.createRegisteredUser.activate(Some(0), DateTime.now) | fail
          val lockedUser = user.lock(user.versionOption, DateTime.now) | fail
          appRepositories.userRepository.put(lockedUser)

          val cmdJson = Json.obj(
            "type"            -> "UnlockUserCmd",
            "expectedVersion" -> Some(lockedUser.version),
            "email"           -> lockedUser.id.id)
          val json = makeJsonRequest(PUT, s"/users/unlock/${lockedUser.id.id}", json = cmdJson)

          (json \ "message").as[String] should include ("user unlocked")
        }
      }
    }

  }
}
