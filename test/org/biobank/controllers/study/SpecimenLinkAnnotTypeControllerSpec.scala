package org.biobank.controllers.study

import org.biobank.controllers.BbwebPlugin
import org.biobank.domain.study.{ Study, SpecimenLinkAnnotationType }
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

class SpecimenLinkAnnotTypeControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  private def annotTypeToAddCmdJson(annotType: SpecimenLinkAnnotationType) = {
    Json.obj(
      "studyId"       -> annotType.studyId.id,
      "name"          -> annotType.name,
      "description"   -> annotType.description,
      "valueType"     -> annotType.valueType.toString,
      "maxValueCount" -> annotType.maxValueCount,
      "options"       -> annotType.options
    )
  }

  private def annotTypeToUpdateCmdJson(annotType: SpecimenLinkAnnotationType) = {
    Json.obj(
      "studyId"         -> annotType.studyId.id,
      "id"              -> annotType.id.id,
      "expectedVersion" -> Some(annotType.version),
      "name"            -> annotType.name,
      "valueType"       -> annotType.valueType.toString,
      "maxValueCount"   -> annotType.maxValueCount,
      "options"         -> annotType.options
    )
  }

  private def addOnNonDisabledStudy(study: Study) {
    use[BbwebPlugin].studyRepository.put(study)

    val annotType = factory.createSpecimenLinkAnnotationType
    use[BbwebPlugin].specimenLinkAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      POST,
      "/studies/slannottype",
      BAD_REQUEST,
      annotTypeToAddCmdJson(annotType))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("is not disabled")
  }

  private def updateOnNonDisabledStudy(study: Study) {
    use[BbwebPlugin].studyRepository.put(study)

    val annotType = factory.createSpecimenLinkAnnotationType
    use[BbwebPlugin].specimenLinkAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      PUT,
      s"/studies/slannottype/${annotType.id.id}",
      BAD_REQUEST,
      annotTypeToUpdateCmdJson(annotType))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("is not disabled")
  }

  def removeOnNonDisabledStudy(study: Study) {
    use[BbwebPlugin].studyRepository.put(study)

    val sg = factory.createSpecimenGroup
    use[BbwebPlugin].specimenGroupRepository.put(sg)

    val annotType = factory.createSpecimenLinkAnnotationType
    use[BbwebPlugin].specimenLinkAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      DELETE,
      s"/studies/slannottype/${annotType.studyId.id}/${annotType.id.id}/${annotType.version}",
      BAD_REQUEST)

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("is not disabled")
  }

  "Collection Event Type REST API" when {

    "GET /studies/slannottype" should {
      "list none" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val json = makeRequest(GET, s"/studies/slannottype/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 0
      }
    }

    "GET /studies/slannottype" should {
      "list a single collection event annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        use[BbwebPlugin].specimenLinkAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, s"/studies/slannottype/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 1
        compareObj(jsonList(0), annotType)
      }
    }

    "GET /studies/slannottype" should {
      "get a single collection event annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        use[BbwebPlugin].specimenLinkAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, s"/studies/slannottype/${study.id.id}?annotTypeId=${annotType.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, annotType)
      }
    }

    "GET /studies/slannottype" should {
      "list multiple collection event annotation types" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotTypes = List(
          factory.createSpecimenLinkAnnotationType,
          factory.createSpecimenLinkAnnotationType)
        annotTypes map { annotType => use[BbwebPlugin].specimenLinkAnnotationTypeRepository.put(annotType) }

        val json = makeRequest(GET, s"/studies/slannottype/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]

        jsonList should have size annotTypes.size
          (jsonList zip annotTypes).map { item => compareObj(item._1, item._2) }
        ()
      }
    }

    "POST /studies/slannottype" should {
      "add a collection event annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        val json = makeRequest(POST, "/studies/slannottype", json = annotTypeToAddCmdJson(annotType))
          (json \ "status").as[String] should include ("success")
      }
    }

    "POST /studies/slannottype" should {
      "not add a collection event annotation type to an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "POST /studies/slannottype" should {
      "not add a collection event annotation type to an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "PUT /studies/slannottype" should {
      "update a collection event annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        use[BbwebPlugin].specimenLinkAnnotationTypeRepository.put(annotType)

        val annotType2 = factory.createSpecimenLinkAnnotationType.copy(
          id = annotType.id,
          version = annotType.version
        )

        val json = makeRequest(PUT,
          s"/studies/slannottype/${annotType.id.id}",
          json = annotTypeToUpdateCmdJson(annotType2))

        (json \ "status").as[String] should include ("success")
      }
    }

    "PUT /studies/slannottype" should {
      "not update a collection event annotation type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "PUT /studies/slannottype" should {
      "not update a collection event annotation type on an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "DELETE /studies/slannottype" should {
      "remove a collection event annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        use[BbwebPlugin].specimenLinkAnnotationTypeRepository.put(annotType)

        val json = makeRequest(
          DELETE,
          s"/studies/slannottype/${annotType.studyId.id}/${annotType.id.id}/${annotType.version}")

        (json \ "status").as[String] should include ("success")
      }
    }

    "DELETE /studies/slannottype" should {
      "not remove a collection event annotation type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "DELETE /studies/slannottype" should {
      "not remove a collection event annotation type on an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }
  }

}
