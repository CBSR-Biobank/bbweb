package org.biobank.controllers

import org.biobank.domain.study.{ Study, ProcessingType }
import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.JsonHelper._

import play.api.test.Helpers._
import play.api.test.FakeApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class ProcessingTypeControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  private def procTypeToAddCmdJson(procType: ProcessingType) = {
    Json.obj(
      "type"        -> "AddProcessingTypeCmd",
      "studyId"     -> procType.studyId.id,
      "name"        -> procType.name,
      "description" -> procType.description,
      "enabled"     -> procType.enabled
    )
  }

  private def procTypeToUpdateCmdJson(procType: ProcessingType) = {
    Json.obj(
      "type"            -> "UpdateProcessingTypeCmd",
      "studyId"         -> procType.studyId.id,
      "id"              -> procType.id.id,
      "expectedVersion" -> Some(procType.version),
      "name"            -> procType.name,
      "description"     -> procType.description,
      "enabled"         -> procType.enabled
    )
  }

  private def procTypeToRemoveCmdJson(procType: ProcessingType) = {
    Json.obj(
      "type"            -> "RemoveProcessingTypeCmd",
      "studyId"         -> procType.studyId.id,
      "id"              -> procType.id.id,
      "expectedVersion" -> Some(procType.version)
    )
  }

  def addOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study) {
    appRepositories.studyRepository.put(study)

    val sg = factory.createSpecimenGroup
    appRepositories.specimenGroupRepository.put(sg)

    val procType = factory.createProcessingType

    val json = makeJsonRequest(
      POST,
      "/studies/proctypes",
      BAD_REQUEST,
      procTypeToAddCmdJson(procType))

    (json \ "message").as[String] should include ("study is not disabled")
  }

  def updateOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study) {
    appRepositories.studyRepository.put(study)

    val procType = factory.createProcessingType
    appRepositories.processingTypeRepository.put(procType)

    val procType2 = factory.createProcessingType

    val json = makeJsonRequest(
      PUT,
      s"/studies/proctypes/${procType.id.id}",
      BAD_REQUEST,
      procTypeToUpdateCmdJson(procType2))

    (json \ "message").as[String] should include ("study is not disabled")
  }

  def removeOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study) {
    appRepositories.studyRepository.put(study)

    val procType = factory.createProcessingType
    appRepositories.processingTypeRepository.put(procType)

    val json = makeJsonRequest(
      DELETE,
      s"/studies/proctypes/${procType.id.id}",
      BAD_REQUEST,
      procTypeToRemoveCmdJson(procType))

    (json \ "message").as[String] should include ("study is not disabled")
  }

  describe("Processing Type REST API") {
    describe("GET /studies/proctypes") {
      it("should list none") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/proctypes", json = idJson)
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 0
        }
      }
    }

    describe("GET /studies/proctypes") {
      it("should list a single processing type") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val procType = factory.createProcessingType
          appRepositories.processingTypeRepository.put(procType)

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/proctypes", json = idJson)
          val jsonList = json.as[List[JsObject]]
          jsonList should have size 1
          compareObj(jsonList(0), procType)
        }
      }
    }

    describe("GET /studies/proctypes") {
      it("should list multiple processing types") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val proctypes = List(factory.createProcessingType, factory.createProcessingType)

          proctypes map { procType => appRepositories.processingTypeRepository.put(procType) }

          val idJson = Json.obj("id" -> study.id.id)
          val json = makeJsonRequest(GET, "/studies/proctypes", json = idJson)
          val jsonList = json.as[List[JsObject]]

          jsonList should have size proctypes.size
            (jsonList zip proctypes).map { item => compareObj(item._1, item._2) }
        }
      }
    }

    describe("POST /studies/proctypes") {
      it("should add a processing type") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val procType = factory.createProcessingType
          val json = makeJsonRequest(
            POST,
            "/studies/proctypes",
            json = procTypeToAddCmdJson(procType))

          (json \ "message").as[String] should include ("processing type added")
        }
      }
    }

    describe("POST /studies/proctypes") {
      it("should not add a processing type to an enabled study") {
        running(fakeApplication) {
          addOnNonDisabledStudy(
            new AppRepositories,
            factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
        }
      }
    }

    describe("POST /studies/proctypes") {
      it("should not add a processing type to an retired study") {
        running(fakeApplication) {
          addOnNonDisabledStudy(
            new AppRepositories,
            factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
        }
      }
    }

    describe("PUT /studies/proctypes") {
      it("should update a processing type") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val procType = factory.createProcessingType
          appRepositories.processingTypeRepository.put(procType)

          val procType2 = factory.createProcessingType.copy(
            id = procType.id,
            version = procType.version
          )

          val json = makeJsonRequest(
            PUT,
            s"/studies/proctypes/${procType.id.id}",
            json = procTypeToUpdateCmdJson(procType2))

          (json \ "message").as[String] should include ("processing type updated")
        }
      }
    }

    describe("PUT /studies/proctypes") {
      it("should not update a processing type on an enabled study") {
        running(fakeApplication) {
          updateOnNonDisabledStudy(
            new AppRepositories,
            factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
        }
      }
    }

    describe("PUT /studies/proctypes") {
      it("should not update a processing type on an retired study") {
        running(fakeApplication) {
          updateOnNonDisabledStudy(
            new AppRepositories,
            factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
        }
      }
    }

    describe("DELETE /studies/proctypes") {
      it("should remove a processing type") {
        running(fakeApplication) {
          val appRepositories = new AppRepositories

          val study = factory.createDisabledStudy
          appRepositories.studyRepository.put(study)

          val procType = factory.createProcessingType
          appRepositories.processingTypeRepository.put(procType)

          val json = makeJsonRequest(
            DELETE,
            s"/studies/proctypes/${procType.id.id}",
            json = procTypeToRemoveCmdJson(procType))

          (json \ "message").as[String] should include ("processing type removed")
        }
      }
    }

    describe("DELETE /studies/proctypes") {
      it("should not remove a processing type on an enabled study") {
        running(fakeApplication) {
          removeOnNonDisabledStudy(
            new AppRepositories,
            factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
        }
      }
    }

    describe("DELETE /studies/proctypes") {
      it("should not remove a processing type on an retired study") {
        running(fakeApplication) {
          removeOnNonDisabledStudy(
            new AppRepositories,
            factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
        }
      }
    }
  }

}
