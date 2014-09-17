package org.biobank.controllers.study

import org.biobank.controllers.BbwebPlugin
import org.biobank.domain.study.{ Study, ProcessingType }
import org.biobank.fixture.ControllerFixture
import org.biobank.domain.JsonHelper._

import play.api.test.Helpers._
import play.api.test.WithApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import com.typesafe.plugin._
import play.api.Play.current

class ProcessingTypeControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  private def procTypeToAddCmdJson(procType: ProcessingType) = {
    Json.obj(
      "studyId"     -> procType.studyId.id,
      "name"        -> procType.name,
      "description" -> procType.description,
      "enabled"     -> procType.enabled
    )
  }

  private def procTypeToUpdateCmdJson(procType: ProcessingType) = {
    Json.obj(
      "studyId"         -> procType.studyId.id,
      "id"              -> procType.id.id,
      "expectedVersion" -> Some(procType.version),
      "name"            -> procType.name,
      "description"     -> procType.description,
      "enabled"         -> procType.enabled
    )
  }

  def addOnNonDisabledStudy(study: Study) {
    use[BbwebPlugin].studyRepository.put(study)

    val sg = factory.createSpecimenGroup
    use[BbwebPlugin].specimenGroupRepository.put(sg)

    val procType = factory.createProcessingType

    val json = makeRequest(
      POST,
      "/studies/proctypes",
      BAD_REQUEST,
      procTypeToAddCmdJson(procType))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("is not disabled")
  }

  def updateOnNonDisabledStudy(study: Study) {
    use[BbwebPlugin].studyRepository.put(study)

    val procType = factory.createProcessingType
    use[BbwebPlugin].processingTypeRepository.put(procType)

    val procType2 = factory.createProcessingType

    val json = makeRequest(
      PUT,
      s"/studies/proctypes/${procType.id.id}",
      BAD_REQUEST,
      procTypeToUpdateCmdJson(procType2))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("is not disabled")
  }

  def removeOnNonDisabledStudy(study: Study) {
    use[BbwebPlugin].studyRepository.put(study)

    val procType = factory.createProcessingType
    use[BbwebPlugin].processingTypeRepository.put(procType)

    val json = makeRequest(
      DELETE,
      s"/studies/proctypes/${procType.studyId.id}/${procType.id.id}/${procType.version}",
      BAD_REQUEST)

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("is not disabled")
  }

  "Processing Type REST API" when {

    "GET /studies/proctypes" should {
      "list none" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val json = makeRequest(GET, s"/studies/proctypes/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 0
      }
    }

    "GET /studies/proctypes" should {
      "list a single processing type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val procType = factory.createProcessingType
        use[BbwebPlugin].processingTypeRepository.put(procType)

        val json = makeRequest(GET, s"/studies/proctypes/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 1
        compareObj(jsonList(0), procType)
      }
    }

    "GET /studies/proctypes" should {
      "get a single processing type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val procType = factory.createProcessingType
        use[BbwebPlugin].processingTypeRepository.put(procType)

        val json = makeRequest(GET, s"/studies/proctypes/${study.id.id}?procTypeId=${procType.id.id}").as[JsObject]
        (json \ "status").as[String] should include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, procType)
      }
    }

    "GET /studies/proctypes" should {
      "list multiple processing types" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val proctypes = List(factory.createProcessingType, factory.createProcessingType)

        proctypes map { procType => use[BbwebPlugin].processingTypeRepository.put(procType) }

        val json = makeRequest(GET, s"/studies/proctypes/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]

        jsonList should have size proctypes.size
          (jsonList zip proctypes).map { item => compareObj(item._1, item._2) }
        ()
      }
    }

    "POST /studies/proctypes" should {
      "add a processing type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val procType = factory.createProcessingType
        val json = makeRequest(
          POST,
          "/studies/proctypes",
          json = procTypeToAddCmdJson(procType))

        (json \ "status").as[String] should include ("success")
      }
    }

    "POST /studies/proctypes" should {
      "not add a processing type to an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "POST /studies/proctypes" should {
      "not add a processing type to an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "PUT /studies/proctypes" should {
      "update a processing type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val procType = factory.createProcessingType
        use[BbwebPlugin].processingTypeRepository.put(procType)

        val procType2 = factory.createProcessingType.copy(
          id = procType.id,
          version = procType.version
        )

        val json = makeRequest(
          PUT,
          s"/studies/proctypes/${procType.id.id}",
          json = procTypeToUpdateCmdJson(procType2))

        (json \ "status").as[String] should include ("success")
      }
    }

    "PUT /studies/proctypes" should {
      "not update a processing type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "PUT /studies/proctypes" should {
      "not update a processing type on an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "DELETE /studies/proctypes" should {
      "remove a processing type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val procType = factory.createProcessingType
        use[BbwebPlugin].processingTypeRepository.put(procType)

        val json = makeRequest(
          DELETE,
          s"/studies/proctypes/${procType.studyId.id}/${procType.id.id}/${procType.version}")

        (json \ "status").as[String] should include ("success")
      }
    }

    "DELETE /studies/proctypes" should {
      "not remove a processing type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "DELETE /studies/proctypes" should {
      "not remove a processing type on an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }
  }

}
