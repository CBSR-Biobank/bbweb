package org.biobank.controllers

import org.biobank.domain.study.{ Study, ProcessingType, SpecimenLinkType }
import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.JsonHelper._

import play.api.test.Helpers._
import play.api.test.WithApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class SpecimenLinkTypeControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  private def annotTypeJson(slType: SpecimenLinkType) = {
    if (!slType.annotationTypeData.isEmpty) {
      Json.obj(
        "annotationTypeData"    -> Json.arr(
          Json.obj(
            "annotationTypeId"  -> slType.annotationTypeData(0).annotationTypeId,
            "required"          -> slType.annotationTypeData(0).required
          ))
      )
    } else {
      Json.obj(
        "annotationTypeData"    ->  Json.arr()
      )
    }
  }

  private def slTypeCommonToAddCmdJson(slType: SpecimenLinkType) = {
    Json.obj(
      "processingTypeId"      -> slType.processingTypeId.id,
      "expectedInputChange"   -> slType.expectedInputChange,
      "expectedOutputChange"  -> slType.expectedOutputChange,
      "inputCount"            -> slType.inputCount,
      "outputCount"           -> slType.outputCount,
      "inputGroupId"          -> slType.inputGroupId.id,
      "outputGroupId"         -> slType.outputGroupId.id,
      "inputContainerTypeId"  -> slType.inputContainerTypeId.map(_.id),
      "outputContainerTypeId" -> slType.outputContainerTypeId.map(_.id)
    )
  }

  private def slTypeToAddCmdJson(slType: SpecimenLinkType) = {
    val result = Json.obj("type" -> "AddSpecimenLinkTypeCmd")

    result ++ slTypeCommonToAddCmdJson(slType) ++ annotTypeJson(slType)
  }

  private def slTypeToUpdateCmdJson(slType: SpecimenLinkType) = {
    val result = Json.obj(
      "id"              -> slType.id.id,
      "expectedVersion" -> Some(slType.version)
    )

    result ++ slTypeCommonToAddCmdJson(slType) ++ annotTypeJson(slType)
  }

  private def slTypeToRemoveCmdJson(slType: SpecimenLinkType) = {
    Json.obj(
      "processingTypeId" -> slType.processingTypeId.id,
      "id"               -> slType.id.id,
      "expectedVersion"  -> Some(slType.version)
    )
  }

  def addOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study,
    procType: ProcessingType) {
    appRepositories.studyRepository.put(study)
    appRepositories.processingTypeRepository.put(procType)

    val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
    appRepositories.specimenGroupRepository.put(inputSg)
    appRepositories.specimenGroupRepository.put(outputSg)
    appRepositories.specimenLinkTypeRepository.put(slType)

    val json = makeRequest(
      POST,
      "/studies/sltypes",
      BAD_REQUEST,
      slTypeToAddCmdJson(slType))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("study is not disabled")
  }

  def updateOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study,
    procType: ProcessingType) {
    appRepositories.studyRepository.put(study)
    appRepositories.processingTypeRepository.put(procType)

    val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
    appRepositories.specimenGroupRepository.put(inputSg)
    appRepositories.specimenGroupRepository.put(outputSg)
    appRepositories.specimenLinkTypeRepository.put(slType)

    val slType2 = factory.createSpecimenLinkType

    val json = makeRequest(
      PUT,
      s"/studies/sltypes/${slType.id.id}",
      BAD_REQUEST,
      slTypeToUpdateCmdJson(slType2))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("study is not disabled")
  }

  def removeOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study,
    procType: ProcessingType) {
    appRepositories.studyRepository.put(study)
    appRepositories.processingTypeRepository.put(procType)

    val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
    appRepositories.specimenGroupRepository.put(inputSg)
    appRepositories.specimenGroupRepository.put(outputSg)
    appRepositories.specimenLinkTypeRepository.put(slType)

    val json = makeRequest(
      DELETE,
      s"/studies/sltypes/${slType.id.id}",
      BAD_REQUEST,
      slTypeToRemoveCmdJson(slType))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("study is not disabled")
  }

  "SpecimenLink Type REST API" when {

    "GET /studies/sltypes" should {
      "list none" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val procType = factory.createProcessingType
        appRepositories.processingTypeRepository.put(procType)

        val json = makeRequest(GET, s"/studies/sltypes/${procType.id.id}")
        val jsonList = json.as[List[JsObject]]
        jsonList should have size 0
      }
    }

    "GET /studies/sltypes" should {
      "list a single specimen link type" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val procType = factory.createProcessingType
        appRepositories.processingTypeRepository.put(procType)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        appRepositories.specimenGroupRepository.put(inputSg)
        appRepositories.specimenGroupRepository.put(outputSg)
        appRepositories.specimenLinkTypeRepository.put(slType)

        val json = makeRequest(GET, s"/studies/sltypes/${procType.id.id}")
        val jsonList = json.as[List[JsObject]]
        jsonList should have size 1
        compareObj(jsonList(0), slType)
      }
    }

    "GET /studies/sltypes" should {
      "get a single specimen link type" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val procType = factory.createProcessingType
        appRepositories.processingTypeRepository.put(procType)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        appRepositories.specimenGroupRepository.put(inputSg)
        appRepositories.specimenGroupRepository.put(outputSg)
        appRepositories.specimenLinkTypeRepository.put(slType)

        val jsonObj = makeRequest(GET, s"/studies/sltypes/${procType.id.id}?slTypeId=${slType.id.id}").as[JsObject]
        compareObj(jsonObj, slType)
      }
    }

    "GET /studies/sltypes" should {
      "list multiple specimen link types" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val procType = factory.createProcessingType
        appRepositories.processingTypeRepository.put(procType)

        val sltypes = List(factory.createSpecimenLinkType, factory.createSpecimenLinkType)

        sltypes map { slType => appRepositories.specimenLinkTypeRepository.put(slType) }

        val json = makeRequest(GET, s"/studies/sltypes/${procType.id.id}")
        val jsonList = json.as[List[JsObject]]

        jsonList should have size sltypes.size
          (jsonList zip sltypes).map { item => compareObj(item._1, item._2) }
        ()
      }
    }

    "POST /studies/sltypes" should {
      "add a specimen link type" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val procType = factory.createProcessingType
        appRepositories.processingTypeRepository.put(procType)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        appRepositories.specimenGroupRepository.put(inputSg)
        appRepositories.specimenGroupRepository.put(outputSg)

        val json = makeRequest(
          POST,
          "/studies/sltypes",
          json = slTypeToAddCmdJson(slType))

        (json \ "status").as[String] should include ("success")
      }
    }

    "POST /studies/sltypes" should {
      "not add a specimen link type to an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories
        val study = appRepositories.studyRepository.put(
          factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
        addOnNonDisabledStudy(appRepositories, study, factory.createProcessingType)
      }
    }

    "POST /studies/sltypes" should {
      "not add a specimen link type to an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories
        val study = appRepositories.studyRepository.put(
          factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
        addOnNonDisabledStudy(appRepositories, study, factory.createProcessingType)
      }
    }

    "PUT /studies/sltypes" should {
      "should update a specimen link type" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val procType = factory.createProcessingType
        appRepositories.processingTypeRepository.put(procType)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        appRepositories.specimenGroupRepository.put(inputSg)
        appRepositories.specimenGroupRepository.put(outputSg)
        appRepositories.specimenLinkTypeRepository.put(slType)

        val slType2 = factory.createSpecimenLinkType.copy(
          id = slType.id,
          version = slType.version,
          inputGroupId = slType.inputGroupId,
          outputGroupId = slType.outputGroupId
        )

        val json = makeRequest(
          PUT,
          s"/studies/sltypes/${slType.id.id}",
          json = slTypeToUpdateCmdJson(slType2))

        (json \ "status").as[String] should include ("success")
      }
    }

    "PUT /studies/sltypes" should {
      "not update a specimen link type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories
        val study = appRepositories.studyRepository.put(
          factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
        updateOnNonDisabledStudy(appRepositories, study, factory.createProcessingType)
      }
    }

    "PUT /studies/sltypes" should {
      "not update a specimen link type on an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories
        val study = appRepositories.studyRepository.put(
          factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
        updateOnNonDisabledStudy(appRepositories, study, factory.createProcessingType)
      }
    }

    "DELETE /studies/sltypes" should {
      "remove a specimen link type" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val procType = factory.createProcessingType
        appRepositories.processingTypeRepository.put(procType)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        appRepositories.specimenGroupRepository.put(inputSg)
        appRepositories.specimenGroupRepository.put(outputSg)
        appRepositories.specimenLinkTypeRepository.put(slType)

        val json = makeRequest(
          DELETE,
          s"/studies/sltypes/${slType.id.id}",
          json = slTypeToRemoveCmdJson(slType))

        (json \ "status").as[String] should include ("success")
      }
    }

    "DELETE /studies/sltypes" should {
      "not remove a specimen link type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories
        val study = appRepositories.studyRepository.put(
          factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
        removeOnNonDisabledStudy(appRepositories, study, factory.createProcessingType)
      }
    }

    "DELETE /studies/sltypes" should {
      "not remove a specimen link type on an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories
        val study = appRepositories.studyRepository.put(
          factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
        removeOnNonDisabledStudy(appRepositories, study, factory.createProcessingType)
      }
    }
  }

}
