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
          appRepositories.userRepository.removeAll
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
            "type"      -> "AddUserCmd",
            "name"      -> user.name,
            "email"     -> user.email,
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

          val user = factory.createRegisteredUser
          appRepositories.userRepository.put(user)

          val cmdJson = Json.obj(
            "type"            -> "UpdateUserCmd",
            "id"              -> user.id.id,
            "expectedVersion" -> Some(user.version),
            "name"      -> user.name,
            "email"     -> user.email,
            "avatarUrl" -> user.avatarUrl)
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

    describe("POST /users/enable") {
      it("should enable a user") {
        running(fakeApplication) {

          val appRepositories = new AppRepositories

          val user = factory.createRegisteredUser
          appRepositories.userRepository.put(user)
          appRepositories.specimenGroupRepository.put(factory.createSpecimenGroup)
          appRepositories.collectionEventTypeRepository.put(factory.createCollectionEventType)

          val cmdJson = Json.obj(
            "type" -> "EnableUserCmd",
            "id" -> user.id.id,
            "expectedVersion" -> Some(user.version))
          val json = makeJsonRequest(POST, "/users/enable", json = cmdJson)

          (json \ "message").as[String] should include ("user enabled")
        }
      }
    }

    describe("POST /users/enable") {
      it("should not enable a user") {
        running(fakeApplication) {

          val appRepositories = new AppRepositories

          val user = factory.createRegisteredUser
          appRepositories.userRepository.put(user)

          val cmdJson = Json.obj(
            "type" -> "EnableUserCmd",
            "id" -> user.id.id,
            "expectedVersion" -> Some(user.version))
          val json = makeJsonRequest(POST, "/users/enable", BAD_REQUEST, cmdJson)

          (json \ "message").as[String] should include ("no specimen groups")
        }
      }
    }

    describe("POST /users/disable") {
      it("should disable a user") {
        running(fakeApplication) {

          val appRepositories = new AppRepositories

          val user = factory.createRegisteredUser.activate(Some(0), org.joda.time.DateTime.now) | fail
          appRepositories.userRepository.put(user)

          val cmdJson = Json.obj(
            "type" -> "DisableUserCmd",
            "id" -> user.id.id,
            "expectedVersion" -> Some(user.version))
          val json = makeJsonRequest(POST, "/users/disable", json = cmdJson)

          (json \ "message").as[String] should include ("user disabled")
        }
      }
    }

    describe("POST /users/retire") {
      it("should retire a user") {
        running(fakeApplication) {

          val appRepositories = new AppRepositories

          val user = factory.createRegisteredUser
          appRepositories.userRepository.put(user)

          val cmdJson = Json.obj(
            "type" -> "RetireUserCmd",
            "id" -> user.id.id,
            "expectedVersion" -> Some(user.version))
          val json = makeJsonRequest(POST, "/users/retire", json = cmdJson)

          (json \ "message").as[String] should include ("user retired")
        }
      }
    }

    describe("POST /users/unretire") {
      it("should unretire a user") {
        running(fakeApplication) {

          val appRepositories = new AppRepositories

          val user = factory.createRegisteredUser.activate(Some(0), org.joda.time.DateTime.now) | fail
          appRepositories.userRepository.put(user)

          val cmdJson = Json.obj(
            "type" -> "UnretireUserCmd",
            "id" -> user.id.id,
            "expectedVersion" -> Some(user.version))
          val json = makeJsonRequest(POST, "/users/unretire", json = cmdJson)

          (json \ "message").as[String] should include ("user unretired")
        }
      }
    }

  }

}
