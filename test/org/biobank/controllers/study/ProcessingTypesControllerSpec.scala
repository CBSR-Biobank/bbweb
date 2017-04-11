package org.biobank.controllers.study

import org.biobank.domain.JsonHelper
import org.biobank.domain.study._
import org.biobank.domain.study.{ Study, ProcessingType }
import org.biobank.fixture.ControllerFixture
import org.biobank.fixture._
import play.api.libs.json._
import play.api.test.Helpers._

@org.scalatest.Ignore
class ProcessingTypesControllerSpec extends ControllerFixture with JsonHelper {

  def uri(study: Study): String = s"/studies/proctypes/${study.id.id}"

  def uri(study: Study, procType: ProcessingType): String =
    uri(study) + s"/${procType.id.id}"

  def uriWithQuery(study: Study, procType: ProcessingType): String =
    uri(study) + s"?procTypeId=${procType.id.id}"

  def uri(study: Study, procType: ProcessingType, version: Long): String =
    uri(study, procType) + s"/${version}"

  private def procTypeToAddCmdJson(procType: ProcessingType) = {
    Json.obj(
      "studyId"     -> procType.studyId.id,
      "name"        -> procType.name,
      "description" -> procType.description,
      "enabled"     -> procType.enabled
    )
  }

  private def procTypeToUpdateCmdJson(procType: ProcessingType) = {
    procTypeToAddCmdJson(procType) ++ Json.obj(
      "id"              -> procType.id.id,
      "expectedVersion" -> Some(procType.version)
    )
  }

  def addOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)
    val procType = factory.createProcessingType.copy(studyId = study.id)

    val json = makeRequest(
      POST,
      uri(study),
      BAD_REQUEST,
      procTypeToAddCmdJson(procType))

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex ("InvalidStatus: study not disabled")

    ()
  }

  def updateOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val procType = factory.createProcessingType.copy(studyId = study.id)
    processingTypeRepository.put(procType)

    val procType2 = factory.createProcessingType.copy(studyId = study.id)

    val json = makeRequest(
      PUT,
      uri(study, procType2),
      BAD_REQUEST,
      procTypeToUpdateCmdJson(procType2))

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex ("InvalidStatus: study not disabled")

    ()
  }

  def removeOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val procType = factory.createProcessingType
    processingTypeRepository.put(procType)

    val json = makeRequest(
      DELETE,
      uri(study, procType, procType.version),
      BAD_REQUEST)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex ("InvalidStatus: study not disabled")

    ()
  }

  "Processing Type REST API" when {

    "GET /studies/proctypes" must {
      "list none" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, uri(study))
          (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list a single processing type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val procType = factory.createProcessingType
        processingTypeRepository.put(procType)

        val json = makeRequest(GET, uri(study))
          (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1
        compareObj(jsonList(0), procType)
      }

      "get a single processing type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val procType = factory.createProcessingType
        processingTypeRepository.put(procType)

        val json = makeRequest(GET, uriWithQuery(study, procType)).as[JsObject]
          (json \ "status").as[String] must include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, procType)
      }

      "list multiple processing types" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val proctypes = List(factory.createProcessingType, factory.createProcessingType)

        proctypes map { procType => processingTypeRepository.put(procType) }

        val json = makeRequest(GET, uri(study))
          (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]

        jsonList must have size proctypes.size.toLong
          (jsonList zip proctypes).map { item => compareObj(item._1, item._2) }
        ()
      }

      "fail for an invalid study ID" in {
        val study = factory.createDisabledStudy

        val json = makeRequest(GET, uri(study), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*study")
      }

      "fail for an invalid study ID when using an processing type id" in {
        val study = factory.createDisabledStudy
        val procType = factory.createProcessingType

        val json = makeRequest(GET, uriWithQuery(study, procType), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*study")
      }

      "fail for an invalid processing type id" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val procType = factory.createProcessingType

        val json = makeRequest(GET, uriWithQuery(study, procType), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*processing type")
      }
    }

    "POST /studies/proctypes" must {

      "add a processing type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val procType = factory.createProcessingType
        val json = makeRequest(
          POST,
          uri(study),
          json = procTypeToAddCmdJson(procType))

        (json \ "status").as[String] must include ("success")
      }

      "not add a processing type to an enabled study" in {
        addOnNonDisabledStudy(factory.createEnabledStudy)
      }

      "not add a processing type to an retired study" in {
        addOnNonDisabledStudy(factory.createRetiredStudy)
      }

      "allow adding a processing type with same name on two different studies" in {
        val commonName = nameGenerator.next[ProcessingType]

        (0 until 2).foreach { x =>
          val study = factory.createDisabledStudy
          studyRepository.put(study)

          val pt = factory.createProcessingType.copy(name = commonName)

          val cmdJson = procTypeToAddCmdJson(pt)
          val json = makeRequest(POST, uri(study), json = cmdJson)
          (json \ "status").as[String] must include ("success")
        }
      }
    }

    "PUT /studies/proctypes" must {
      "update a processing type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val procType = factory.createProcessingType
        processingTypeRepository.put(procType)

        val procType2 = factory.createProcessingType.copy(
          id = procType.id,
          version = procType.version
        )

        val json = makeRequest(
          PUT,
          uri(study, procType2),
          json = procTypeToUpdateCmdJson(procType2))

        (json \ "status").as[String] must include ("success")
      }

      "not update a processing type on an enabled study" in {
        updateOnNonDisabledStudy(factory.createEnabledStudy)
      }

      "not update a processing type on an retired study" in {
        updateOnNonDisabledStudy(factory.createRetiredStudy)
      }

      "allow a updating processing types on two different studies to same name" in {
        val commonName = nameGenerator.next[ProcessingType]

        (0 until 2).map { study =>
          val study = factory.createDisabledStudy
          studyRepository.put(study)
          val pt = factory.createProcessingType
          processingTypeRepository.put(pt)
          (study, pt)
        } foreach { case (study: Study, pt: ProcessingType) =>
            val cmdJson = procTypeToUpdateCmdJson(pt.copy(name = commonName))
            val json = makeRequest(PUT, uri(study, pt), json = cmdJson)
            (json \ "status").as[String] must include ("success")
        }
      }
    }

    "DELETE /studies/proctypes/:studyId/:id/:ver" must {
      "remove a processing type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val procType = factory.createProcessingType
        processingTypeRepository.put(procType)

        val json = makeRequest(DELETE, uri(study, procType, procType.version))

        (json \ "status").as[String] must include ("success")
      }

      "not remove a processing type on an enabled study" in {
        removeOnNonDisabledStudy(factory.createEnabledStudy)
      }

      "not remove a processing type on an retired study" in {
        removeOnNonDisabledStudy(factory.createRetiredStudy)
      }
    }
  }

}
