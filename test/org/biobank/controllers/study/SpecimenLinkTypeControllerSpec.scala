package org.biobank.controllers.study

import org.biobank.domain.JsonHelper
import org.biobank.domain.study._
import org.biobank.domain.study.{ Study, ProcessingType, SpecimenLinkType }
import org.biobank.fixture.ControllerFixture
import org.biobank.fixture._
import play.api.libs.json._
import play.api.test.Helpers._

@org.scalatest.Ignore
class SpecimenLinkTypeControllerSpec extends ControllerFixture with JsonHelper {

  def uri(procType: ProcessingType): String = s"/studies/proctypes/sltypes/${procType.id.id}"

  def uri(procType: ProcessingType, slType: SpecimenLinkType): String =
    uri(procType) + s"/${slType.id.id}"

  def uriWithQuery(procType: ProcessingType, slType: SpecimenLinkType): String =
    uri(procType) + s"?slTypeId=${slType.id.id}"

  def uri(procType: ProcessingType, slType: SpecimenLinkType, version: Long): String =
    uri(procType, slType) + s"/${version}"

  private def slTypeToAddCmdJson(slType: SpecimenLinkType) = {
    Json.obj(
      "processingTypeId"      -> slType.processingTypeId.id,
      "expectedInputChange"   -> slType.expectedInputChange,
      "expectedOutputChange"  -> slType.expectedOutputChange,
      "inputCount"            -> slType.inputCount,
      "outputCount"           -> slType.outputCount,
      "inputGroupId"          -> slType.inputGroupId.id,
      "outputGroupId"         -> slType.outputGroupId.id,
      "inputContainerTypeId"  -> slType.inputContainerTypeId.map(_.id),
      "outputContainerTypeId" -> slType.outputContainerTypeId.map(_.id),
      "annotationTypeData"    -> slType.annotationTypeData.map { at =>
        Json.obj(
          "annotationTypeId"  -> at.annotationTypeId,
          "required"          -> at.required
        )
      }
    )
  }

  private def slTypeToUpdateCmdJson(slType: SpecimenLinkType) = {
    slTypeToAddCmdJson(slType) ++ Json.obj(
      "id"              -> slType.id.id,
      "expectedVersion" -> Some(slType.version)
    )
  }

  def addOnNonDisabledStudy(study: Study, procType: ProcessingType) = {
    studyRepository.put(study)
    processingTypeRepository.put(procType.copy(studyId = study.id))

    val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
    specimenGroupRepository.put(inputSg)
    specimenGroupRepository.put(outputSg)
    specimenLinkTypeRepository.put(slType)

    val json = makeRequest(
      POST,
      uri(procType),
      BAD_REQUEST,
      slTypeToAddCmdJson(slType))

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex ("InvalidStatus.*study not disabled")
  }

  def updateOnNonDisabledStudy(study: Study, procType: ProcessingType) = {
    studyRepository.put(study)
    processingTypeRepository.put(procType.copy(studyId = study.id))

    val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
    specimenGroupRepository.put(inputSg)
    specimenGroupRepository.put(outputSg)
    specimenLinkTypeRepository.put(slType)

    val slType2 = factory.createSpecimenLinkType.copy(id = slType.id)

    val json = makeRequest(
      PUT,
      uri(procType, slType),
      BAD_REQUEST,
      slTypeToUpdateCmdJson(slType2))

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex ("InvalidStatus.*study not disabled")
  }

  def removeOnNonDisabledStudy(study: Study, procType: ProcessingType) = {
    studyRepository.put(study)
    processingTypeRepository.put(procType.copy(studyId = study.id))

    val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
    specimenGroupRepository.put(inputSg)
    specimenGroupRepository.put(outputSg)
    specimenLinkTypeRepository.put(slType)

    val json = makeRequest(DELETE, uri(procType, slType, slType.version), BAD_REQUEST)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex ("InvalidStatus.*study not disabled")
  }

  def createEntities()(fn: (Study, ProcessingType) => Unit): Unit = {
    val disabledStudy = factory.createDisabledStudy
    studyRepository.put(disabledStudy)

    val procType = factory.createProcessingType
    processingTypeRepository.put(procType)

    fn(disabledStudy, procType)
  }

  "SpecimenLink Type REST API" when {

    "GET /studies/sltypes" must {
      "list none" in {
        createEntities() { (study, processingType) =>
          val json = makeRequest(GET, uri(processingType))
          (json \ "status").as[String] must include ("success")
          val jsonList = (json \ "data").as[List[JsObject]]
          jsonList must have size 0
        }
      }

      "list a single specimen link type" in {
        createEntities() { (study, procType) =>
          val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
          specimenGroupRepository.put(inputSg)
          specimenGroupRepository.put(outputSg)
          specimenLinkTypeRepository.put(slType)

          val json = makeRequest(GET, uri(procType))
          (json \ "status").as[String] must include ("success")
          val jsonList = (json \ "data").as[List[JsObject]]
          jsonList must have size 1
          compareObj(jsonList(0), slType)
        }
      }

      "get a single specimen link type" in {
        createEntities() { (study, procType) =>

          val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
          specimenGroupRepository.put(inputSg)
          specimenGroupRepository.put(outputSg)
          specimenLinkTypeRepository.put(slType)

          val json = makeRequest(GET, uriWithQuery(procType, slType))
          (json \ "status").as[String] must include ("success")
          val jsonObj = (json \ "data").as[JsObject]
          compareObj(jsonObj, slType)
        }
      }

      "list multiple specimen link types" in {
        createEntities() { (study, procType) =>
          val sltypes = List(factory.createSpecimenLinkType, factory.createSpecimenLinkType)

          sltypes map { slType => specimenLinkTypeRepository.put(slType) }

          val json = makeRequest(GET, uri(procType))
          (json \ "status").as[String] must include ("success")
          val jsonList = (json \ "data").as[List[JsObject]]

          jsonList must have size sltypes.size
          (jsonList zip sltypes).map { item => compareObj(item._1, item._2) }
          ()
        }
      }

      "fail for invalid processing type id" in {
        val procType = factory.createProcessingType

        val json = makeRequest(GET, uri(procType), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*processing type")
      }

      "fail for an invalid procesing type ID when using an specimen link type id" in {
        val procType = factory.createProcessingType
        val slType = factory.createSpecimenLinkType

        val json = makeRequest(GET, uriWithQuery(procType, slType), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*processing type")
      }

      "fail for an invalid specimen link type id" in {
        createEntities() { (study, procType) =>
          val slType = factory.createSpecimenLinkType
          val json = makeRequest(GET, uriWithQuery(procType, slType), NOT_FOUND)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex ("specimen link type does not exist")
        }
      }

    }

    "POST /studies/sltypes" must {
      "add a specimen link type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val procType = factory.createProcessingType
        processingTypeRepository.put(procType)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        specimenGroupRepository.put(inputSg)
        specimenGroupRepository.put(outputSg)

        val json = makeRequest(POST, uri(procType), json = slTypeToAddCmdJson(slType))

        (json \ "status").as[String] must include ("success")
      }

      "not add a specimen link type to an enabled study" in {
        addOnNonDisabledStudy(factory.createEnabledStudy, factory.createProcessingType)
      }

      "not add a specimen link type to an retired study" in {
        addOnNonDisabledStudy(factory.createRetiredStudy, factory.createProcessingType)
      }

      "fail when adding and processing type IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val procType = factory.createProcessingType
        processingTypeRepository.put(procType)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        specimenGroupRepository.put(inputSg)
        specimenGroupRepository.put(outputSg)

        val procType2 = factory.createProcessingType

        val json = makeRequest(POST, uri(procType2), NOT_FOUND, json = slTypeToAddCmdJson(slType))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*processing type")
      }

    }

    "PUT /studies/sltypes" must {
      "must update a specimen link type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val procType = factory.createProcessingType
        processingTypeRepository.put(procType)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        specimenGroupRepository.put(inputSg)
        specimenGroupRepository.put(outputSg)
        specimenLinkTypeRepository.put(slType)

        val slType2 = factory.createSpecimenLinkType.copy(
          id = slType.id,
          version = slType.version,
          inputGroupId = slType.inputGroupId,
          outputGroupId = slType.outputGroupId
        )

        val json = makeRequest(PUT, uri(procType, slType2), json = slTypeToUpdateCmdJson(slType2))
        (json \ "status").as[String] must include ("success")
      }

      "not update a specimen link type on an enabled study" in {
        updateOnNonDisabledStudy(factory.createEnabledStudy, factory.createProcessingType)
      }

      "not update a specimen link type on an retired study" in {
        updateOnNonDisabledStudy(factory.createRetiredStudy, factory.createProcessingType)
      }

      "fail when updating and processing type IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val procType = factory.createProcessingType
        processingTypeRepository.put(procType)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        specimenGroupRepository.put(inputSg)
        specimenGroupRepository.put(outputSg)
        specimenLinkTypeRepository.put(slType)

        val procType2 = factory.createProcessingType

        val json = makeRequest(PUT,
                               uri(procType2, slType),
                               NOT_FOUND,
                               slTypeToUpdateCmdJson(slType))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*processing type")
      }

      "fail when updating and specimen link type IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val procType = factory.createProcessingType
        processingTypeRepository.put(procType)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        specimenGroupRepository.put(inputSg)
        specimenGroupRepository.put(outputSg)
        specimenLinkTypeRepository.put(slType)

        val slType2 = factory.createSpecimenLinkType

        val json = makeRequest(PUT,
                               uri(procType, slType2),
                               BAD_REQUEST,
                               json = slTypeToUpdateCmdJson(slType))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("specimen link type id mismatch")
      }

    }

    "DELETE /studies/sltypes" must {
      "remove a specimen link type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val procType = factory.createProcessingType
        processingTypeRepository.put(procType)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        specimenGroupRepository.put(inputSg)
        specimenGroupRepository.put(outputSg)
        specimenLinkTypeRepository.put(slType)

        val json = makeRequest(DELETE, uri(procType, slType, slType.version))

        (json \ "status").as[String] must include ("success")
      }

      "not remove a specimen link type on an enabled study" in {
        removeOnNonDisabledStudy(factory.createEnabledStudy, factory.createProcessingType)
      }

      "not remove a specimen link type on an retired study" in {
        removeOnNonDisabledStudy(factory.createRetiredStudy, factory.createProcessingType)
      }
    }
  }

}
