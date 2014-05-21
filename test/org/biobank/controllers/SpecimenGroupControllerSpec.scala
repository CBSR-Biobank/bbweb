package org.biobank.controllers

import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.JsonHelper._

import play.api.test.Helpers._
import play.api.test.FakeApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class SpecimenGroupControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  describe("Specimen Group REST API") {
    describe("GET /studies/sgroups") {
      it("should list none"){
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/sgroups", json = idJson)
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 0
        }
      }
    }

    describe("GET /studies/sgroups") {
      it("should list a single specimen group") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val sg = factory.createSpecimenGroup
          appRepositories.specimenGroupRepository.put(sg)

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/sgroups", json = idJson)
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 1
          compareObj(jsonList(0), sg)
        }
      }
    }

    describe("GET /studies/sgroups") {
      it("should list multiple specimen groups") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val sgroups = List(factory.createSpecimenGroup, factory.createSpecimenGroup)
          sgroups map { sg => appRepositories.specimenGroupRepository.put(sg) }

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/sgroups", json = idJson)
          val jsonList = json.as[List[JsObject]]
          jsonList should have size sgroups.size
          (jsonList zip sgroups).map { item => compareObj(item._1, item._2) }
        }
      }
    }

    describe("POST /studies/sgroups") {
      it("should add a specimen group") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val sg = factory.createSpecimenGroup
          val cmdJson = Json.obj(
            "type"                        -> "AddSpecimenGroupCmd",
            "studyId"                     -> sg.studyId.id,
            "name"                        -> sg.name,
            "description"                 -> sg.description,
            "units"                       -> sg.units,
            "anatomicalSourceType"        -> sg.anatomicalSourceType.toString,
            "preservationType"            -> sg.preservationType.toString,
            "preservationTemperatureType" -> sg.preservationTemperatureType.toString,
            "specimenType"                -> sg.specimenType.toString)
          val json = makeJsonRequest(POST, "/studies/sgroups", json = cmdJson)

          (json \ "message").as[String] should include ("specimen group added")
        }
      }
    }

    describe("POST /studies/sgroups") {
      it("should not add a specimen group to enabled study") {
      running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail
          appRepositories.studyRepository.put(study)

          val sg = factory.createSpecimenGroup
          val cmdJson = Json.obj(
            "type"                        -> "AddSpecimenGroupCmd",
            "studyId"                     -> study.id.id,
            "name"                        -> sg.name,
            "description"                 -> sg.description,
            "units"                       -> sg.units,
            "anatomicalSourceType"        -> sg.anatomicalSourceType.toString,
            "preservationType"            -> sg.preservationType.toString,
            "preservationTemperatureType" -> sg.preservationTemperatureType.toString,
            "specimenType"                -> sg.specimenType.toString)
          val json = makeJsonRequest(POST, "/studies/sgroups", BAD_REQUEST, cmdJson)

          (json \ "message").as[String] should include ("study is not disabled")
        }
      }
    }

    describe("PUT /studies/sgroups") {
      it("should update a specimen group") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val sg = factory.createSpecimenGroup
          appRepositories.specimenGroupRepository.put(sg)

          val sg2 = factory.createSpecimenGroup
          val cmdJson = Json.obj(
            "type"                        -> "UpdateSpecimenGroupCmd",
            "studyId"                     -> study.id.id,
            "id"                          -> sg.id.id,
            "expectedVersion"             -> Some(sg.version),
            "name"                        -> sg2.name,
            "description"                 -> sg2.description,
            "units"                       -> sg2.units,
            "anatomicalSourceType"        -> sg2.anatomicalSourceType.toString,
            "preservationType"            -> sg2.preservationType.toString,
            "preservationTemperatureType" -> sg2.preservationTemperatureType.toString,
            "specimenType"                -> sg2.specimenType.toString)
          val json = makeJsonRequest(PUT, s"/studies/sgroups/${sg.id.id}", json = cmdJson)

          (json \ "message").as[String] should include ("specimen group updated")
        }
      }
    }

    describe("PUT /studies/sgroups") {
      it("should not update a specimen group on an enabled study") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail
          appRepositories.studyRepository.put(study)

          val sg = factory.createSpecimenGroup
          appRepositories.specimenGroupRepository.put(sg)

          val sg2 = factory.createSpecimenGroup
          val cmdJson = Json.obj(
            "type"                        -> "UpdateSpecimenGroupCmd",
            "studyId"                     -> study.id.id,
            "id"                          -> sg.id.id,
            "expectedVersion"             -> Some(sg.version),
            "name"                        -> sg2.name,
            "description"                 -> sg2.description,
            "units"                       -> sg2.units,
            "anatomicalSourceType"        -> sg2.anatomicalSourceType.toString,
            "preservationType"            -> sg2.preservationType.toString,
            "preservationTemperatureType" -> sg2.preservationTemperatureType.toString,
            "specimenType"                -> sg2.specimenType.toString)
          val json = makeJsonRequest(PUT, s"/studies/sgroups/${sg.id.id}", BAD_REQUEST, cmdJson)

          (json \ "message").as[String] should include ("study is not disabled")
        }
      }
    }

    describe("DELETE /studies/sgroups") {
      it("should remove a specimen group") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val sg = factory.createSpecimenGroup
          appRepositories.specimenGroupRepository.put(sg)

          val cmdJson = Json.obj(
            "type"            -> "RemoveSpecimenGroupCmd",
            "studyId"         -> study.id.id,
            "id"              -> sg.id.id,
            "expectedVersion" -> Some(sg.version))
          val json = makeJsonRequest(DELETE, s"/studies/sgroups/${sg.id.id}", json = cmdJson)

          (json \ "message").as[String] should include ("specimen group deleted")
        }
      }
    }

    describe("DELETE /studies/sgroups") {
      it("should not remove a specimen group from an enabled study", Tag("single")) {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail
          appRepositories.studyRepository.put(study)

          val sg = factory.createSpecimenGroup
          appRepositories.specimenGroupRepository.put(sg)

          val cmdJson = Json.obj(
            "type"            -> "RemoveSpecimenGroupCmd",
            "studyId"         -> study.id.id,
            "id"              -> sg.id.id,
            "expectedVersion" -> Some(sg.version))
          val json = makeJsonRequest(DELETE, s"/studies/sgroups/${sg.id.id}", BAD_REQUEST, cmdJson)

          (json \ "message").as[String] should include ("study is not disabled")
        }
      }
    }
  }

}
