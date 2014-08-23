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
import com.typesafe.plugin._
import play.api.Play.current

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
    slTypeCommonToAddCmdJson(slType) ++ annotTypeJson(slType)
  }

  private def slTypeToUpdateCmdJson(slType: SpecimenLinkType) = {
    val result = Json.obj(
      "id"              -> slType.id.id,
      "expectedVersion" -> Some(slType.version)
    )

    result ++ slTypeCommonToAddCmdJson(slType) ++ annotTypeJson(slType)
  }

  def addOnNonDisabledStudy(
    study: Study,
    procType: ProcessingType) {
    use[BbwebPlugin].studyRepository.put(study)
    use[BbwebPlugin].processingTypeRepository.put(procType)

    val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
    use[BbwebPlugin].specimenGroupRepository.put(inputSg)
    use[BbwebPlugin].specimenGroupRepository.put(outputSg)
    use[BbwebPlugin].specimenLinkTypeRepository.put(slType)

    val json = makeRequest(
      POST,
      "/studies/sltypes",
      BAD_REQUEST,
      slTypeToAddCmdJson(slType))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("study is not disabled")
  }

  def updateOnNonDisabledStudy(
    study: Study,
    procType: ProcessingType) {
    use[BbwebPlugin].studyRepository.put(study)
    use[BbwebPlugin].processingTypeRepository.put(procType)

    val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
    use[BbwebPlugin].specimenGroupRepository.put(inputSg)
    use[BbwebPlugin].specimenGroupRepository.put(outputSg)
    use[BbwebPlugin].specimenLinkTypeRepository.put(slType)

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
    study: Study,
    procType: ProcessingType) {
    use[BbwebPlugin].studyRepository.put(study)
    use[BbwebPlugin].processingTypeRepository.put(procType)

    val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
    use[BbwebPlugin].specimenGroupRepository.put(inputSg)
    use[BbwebPlugin].specimenGroupRepository.put(outputSg)
    use[BbwebPlugin].specimenLinkTypeRepository.put(slType)

    val json = makeRequest(
      DELETE,
      s"/studies/sltypes/${slType.processingTypeId.id}/${slType.id.id}/${slType.version}",
      BAD_REQUEST)

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("study is not disabled")
  }

  "SpecimenLink Type REST API" when {

    "GET /studies/sltypes" should {
      "list none" in new WithApplication(fakeApplication()) {
        doLogin
        val procType = factory.createProcessingType
        use[BbwebPlugin].processingTypeRepository.put(procType)

        val json = makeRequest(GET, s"/studies/sltypes/${procType.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 0
      }
    }

    "GET /studies/sltypes" should {
      "list a single specimen link type" in new WithApplication(fakeApplication()) {
        doLogin
        val procType = factory.createProcessingType
        use[BbwebPlugin].processingTypeRepository.put(procType)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        use[BbwebPlugin].specimenGroupRepository.put(inputSg)
        use[BbwebPlugin].specimenGroupRepository.put(outputSg)
        use[BbwebPlugin].specimenLinkTypeRepository.put(slType)

        val json = makeRequest(GET, s"/studies/sltypes/${procType.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 1
        compareObj(jsonList(0), slType)
      }
    }

    "GET /studies/sltypes" should {
      "get a single specimen link type" in new WithApplication(fakeApplication()) {
        doLogin
        val procType = factory.createProcessingType
        use[BbwebPlugin].processingTypeRepository.put(procType)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        use[BbwebPlugin].specimenGroupRepository.put(inputSg)
        use[BbwebPlugin].specimenGroupRepository.put(outputSg)
        use[BbwebPlugin].specimenLinkTypeRepository.put(slType)

        val json = makeRequest(GET, s"/studies/sltypes/${procType.id.id}?slTypeId=${slType.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, slType)
      }
    }

    "GET /studies/sltypes" should {
      "list multiple specimen link types" in new WithApplication(fakeApplication()) {
        doLogin
        val procType = factory.createProcessingType
        use[BbwebPlugin].processingTypeRepository.put(procType)

        val sltypes = List(factory.createSpecimenLinkType, factory.createSpecimenLinkType)

        sltypes map { slType => use[BbwebPlugin].specimenLinkTypeRepository.put(slType) }

        val json = makeRequest(GET, s"/studies/sltypes/${procType.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]

        jsonList should have size sltypes.size
          (jsonList zip sltypes).map { item => compareObj(item._1, item._2) }
        ()
      }
    }

    "POST /studies/sltypes" should {
      "add a specimen link type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val procType = factory.createProcessingType
        use[BbwebPlugin].processingTypeRepository.put(procType)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        use[BbwebPlugin].specimenGroupRepository.put(inputSg)
        use[BbwebPlugin].specimenGroupRepository.put(outputSg)

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
        val study = use[BbwebPlugin].studyRepository.put(
          factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
        addOnNonDisabledStudy(study, factory.createProcessingType)
      }
    }

    "POST /studies/sltypes" should {
      "not add a specimen link type to an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        val study = use[BbwebPlugin].studyRepository.put(
          factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
        addOnNonDisabledStudy(study, factory.createProcessingType)
      }
    }

    "PUT /studies/sltypes" should {
      "should update a specimen link type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val procType = factory.createProcessingType
        use[BbwebPlugin].processingTypeRepository.put(procType)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        use[BbwebPlugin].specimenGroupRepository.put(inputSg)
        use[BbwebPlugin].specimenGroupRepository.put(outputSg)
        use[BbwebPlugin].specimenLinkTypeRepository.put(slType)

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
        val study = use[BbwebPlugin].studyRepository.put(
          factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
        updateOnNonDisabledStudy(study, factory.createProcessingType)
      }
    }

    "PUT /studies/sltypes" should {
      "not update a specimen link type on an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        val study = use[BbwebPlugin].studyRepository.put(
          factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
        updateOnNonDisabledStudy(study, factory.createProcessingType)
      }
    }

    "DELETE /studies/sltypes" should {
      "remove a specimen link type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val procType = factory.createProcessingType
        use[BbwebPlugin].processingTypeRepository.put(procType)

        val (slType, inputSg, outputSg) = factory.createSpecimenLinkTypeAndSpecimenGroups
        use[BbwebPlugin].specimenGroupRepository.put(inputSg)
        use[BbwebPlugin].specimenGroupRepository.put(outputSg)
        use[BbwebPlugin].specimenLinkTypeRepository.put(slType)

        val json = makeRequest(
          DELETE,
          s"/studies/sltypes/${slType.processingTypeId.id}/${slType.id.id}/${slType.version}")

        (json \ "status").as[String] should include ("success")
      }
    }

    "DELETE /studies/sltypes" should {
      "not remove a specimen link type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        val study = use[BbwebPlugin].studyRepository.put(
          factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
        removeOnNonDisabledStudy(study, factory.createProcessingType)
      }
    }

    "DELETE /studies/sltypes" should {
      "not remove a specimen link type on an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        val study = use[BbwebPlugin].studyRepository.put(
          factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
        removeOnNonDisabledStudy(study, factory.createProcessingType)
      }
    }
  }

}
