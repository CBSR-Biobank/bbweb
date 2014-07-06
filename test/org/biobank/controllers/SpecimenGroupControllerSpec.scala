package org.biobank.controllers

import org.biobank.domain.study.{ Study, SpecimenGroup }
import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.JsonHelper._

import play.api.test.Helpers._
import play.api.test.WithApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class SpecimenGroupControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  def addToNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study,
    sg: SpecimenGroup) = {

    appRepositories.studyRepository.put(study)

    val cmdJson = Json.obj(
      "studyId"                     -> study.id.id,
      "name"                        -> sg.name,
      "description"                 -> sg.description,
      "units"                       -> sg.units,
      "anatomicalSourceType"        -> sg.anatomicalSourceType.toString,
      "preservationType"            -> sg.preservationType.toString,
      "preservationTemperatureType" -> sg.preservationTemperatureType.toString,
      "specimenType"                -> sg.specimenType.toString)
    val json = makeRequest(POST, "/studies/sgroups", BAD_REQUEST, cmdJson)

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("study is not disabled")
  }

  def updateOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study,
    sg: SpecimenGroup) = {
    appRepositories.studyRepository.put(study)
    appRepositories.specimenGroupRepository.put(sg)

    val sg2 = factory.createSpecimenGroup
    val cmdJson = Json.obj(
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
    val json = makeRequest(PUT, s"/studies/sgroups/${sg.id.id}", BAD_REQUEST, cmdJson)

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("study is not disabled")
  }

  def removeOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study,
    sg: SpecimenGroup) = {
    appRepositories.studyRepository.put(study)
    appRepositories.specimenGroupRepository.put(sg)

    val json = makeRequest(
      DELETE,
      s"/studies/sgroups/${sg.studyId.id}/${sg.id.id}/${sg.version}",
      BAD_REQUEST)

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("study is not disabled")
  }

  "Specimen Group REST API" when {

    "GET /studies/sgroups" should {
      "list none" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val json = makeRequest(GET, s"/studies/sgroups/${study.id.id}")
        val jsonList = json.as[List[JsObject]]
        jsonList should have size 0
      }
    }

    "GET /studies/sgroups" should {
      "list a single specimen group" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        appRepositories.specimenGroupRepository.put(sg)

        val json = makeRequest(GET, s"/studies/sgroups/${study.id.id}")
        val jsonList = json.as[List[JsObject]]
        jsonList should have size 1
        compareObj(jsonList(0), sg)
      }
    }

    "GET /studies/sgroups" should {
      "get a single specimen group" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        appRepositories.specimenGroupRepository.put(sg)

        val jsonObj = makeRequest(GET, s"/studies/sgroups/${study.id.id}?sgId=${sg.id.id}").as[JsObject]
        compareObj(jsonObj, sg)
      }
    }

    "GET /studies/sgroups" should {
      "list multiple specimen groups" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val sgroups = List(factory.createSpecimenGroup, factory.createSpecimenGroup)
        sgroups map { sg => appRepositories.specimenGroupRepository.put(sg) }

        val json = makeRequest(GET, s"/studies/sgroups/${study.id.id}")
        val jsonList = json.as[List[JsObject]]
        jsonList should have size sgroups.size
          (jsonList zip sgroups).map { item => compareObj(item._1, item._2) }
      }
    }

    "POST /studies/sgroups" should {
      "add a specimen group" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        val cmdJson = Json.obj(
          "studyId"                     -> sg.studyId.id,
          "name"                        -> sg.name,
          "description"                 -> sg.description,
          "units"                       -> sg.units,
          "anatomicalSourceType"        -> sg.anatomicalSourceType.toString,
          "preservationType"            -> sg.preservationType.toString,
          "preservationTemperatureType" -> sg.preservationTemperatureType.toString,
          "specimenType"                -> sg.specimenType.toString)
        val json = makeRequest(POST, "/studies/sgroups", json = cmdJson)

        (json \ "status").as[String] should include ("success")
      }
    }

    "POST /studies/sgroups" should {
      "not add a specimen group to enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        addToNonDisabledStudy(
          new AppRepositories,
          factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail,
          factory.createSpecimenGroup)
      }
    }

    "POST /studies/sgroups" should {
      "not add a specimen group to retired study" in new WithApplication(fakeApplication()) {
        doLogin
        addToNonDisabledStudy(
          new AppRepositories,
          factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail,
          factory.createSpecimenGroup)
      }
    }

    "PUT /studies/sgroups" should {
      "update a specimen group" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        appRepositories.specimenGroupRepository.put(sg)

        val sg2 = factory.createSpecimenGroup
        val cmdJson = Json.obj(
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
        val json = makeRequest(PUT, s"/studies/sgroups/${sg.id.id}", json = cmdJson)

        (json \ "status").as[String] should include ("success")
      }
    }

    "PUT /studies/sgroups" should {
      "not update a specimen group on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        updateOnNonDisabledStudy(
          new AppRepositories,
          factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail,
          factory.createSpecimenGroup)
      }
    }

    "PUT /studies/sgroups" should {
      "not update a specimen group on an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        updateOnNonDisabledStudy(
          new AppRepositories,
          factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail,
          factory.createSpecimenGroup)
      }
    }

    "DELETE /studies/sgroups" should {
      "remove a specimen group" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        appRepositories.specimenGroupRepository.put(sg)

        val json = makeRequest(
          DELETE,
          s"/studies/sgroups/${sg.studyId.id}/${sg.id.id}/${sg.version}")

        (json \ "status").as[String] should include ("success")
      }
    }

    "DELETE /studies/sgroups" should {
      "not remove a specimen group from an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        removeOnNonDisabledStudy(
          new AppRepositories,
          factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail,
          factory.createSpecimenGroup)
      }
    }

    "DELETE /studies/sgroups" should {
      "not remove a specimen group from an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        removeOnNonDisabledStudy(
          new AppRepositories,
          factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail,
          factory.createSpecimenGroup)
      }
    }

  }

}
