package org.biobank.controllers.study

import org.biobank.controllers.BbwebPlugin
import org.biobank.domain.study.{ Study, CollectionEventAnnotationType }
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

class CeventAnnotTypeControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  private def annotTypeToAddCmdJson(annotType: CollectionEventAnnotationType) = {
    Json.obj(
      "studyId"       -> annotType.studyId.id,
      "name"          -> annotType.name,
      "description"   -> annotType.description,
      "valueType"     -> annotType.valueType.toString,
      "maxValueCount" -> annotType.maxValueCount,
      "options"       -> annotType.options
    )
  }

  private def annotTypeToUpdateCmdJson(annotType: CollectionEventAnnotationType) = {
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
    val annotType = factory.createCollectionEventAnnotationType
    use[BbwebPlugin].collectionEventAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      POST,
      "/studies/ceannottype",
      BAD_REQUEST,
      annotTypeToAddCmdJson(annotType))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("is not disabled")
  }

  private def updateOnNonDisabledStudy(study: Study) {
    use[BbwebPlugin].studyRepository.put(study)

    val annotType = factory.createCollectionEventAnnotationType
    use[BbwebPlugin].collectionEventAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      PUT,
      s"/studies/ceannottype/${annotType.id.id}",
      BAD_REQUEST,
      annotTypeToUpdateCmdJson(annotType))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("is not disabled")
  }

  def removeOnNonDisabledStudy(study: Study) {
    use[BbwebPlugin].studyRepository.put(study)

    val sg = factory.createSpecimenGroup
    use[BbwebPlugin].specimenGroupRepository.put(sg)

    val annotType = factory.createCollectionEventAnnotationType
    use[BbwebPlugin].collectionEventAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      DELETE,
      s"/studies/ceannottype/${annotType.studyId.id}/${annotType.id.id}/${annotType.version}",
      BAD_REQUEST)

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("is not disabled")
  }

  "Collection Event Type REST API" when {
    "GET /studies/ceannottype" should {
      "list none" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val json = makeRequest(GET, s"/studies/ceannottype/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 0
      }
    }

    "GET /studies/ceannottype" should {
      "list a single collection event annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotType = factory.createCollectionEventAnnotationType
        use[BbwebPlugin].collectionEventAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, s"/studies/ceannottype/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 1
        compareObj(jsonList(0), annotType)
      }
    }

    "GET /studies/ceannottype" should {
      "get a single collection event annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotType = factory.createCollectionEventAnnotationType
        use[BbwebPlugin].collectionEventAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, s"/studies/ceannottype/${study.id.id}?annotTypeId=${annotType.id.id}").as[JsObject]
        (json \ "status").as[String] should include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, annotType)
      }
    }

    "GET /studies/ceannottype" should {
      "list multiple collection event annotation types" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotTypes = List(
          factory.createCollectionEventAnnotationType,
          factory.createCollectionEventAnnotationType)
        annotTypes map { annotType => use[BbwebPlugin].collectionEventAnnotationTypeRepository.put(annotType) }

        val json = makeRequest(GET, s"/studies/ceannottype/${study.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]

        jsonList should have size annotTypes.size
          (jsonList zip annotTypes).map { item => compareObj(item._1, item._2) }
        ()
      }
    }

    "POST /studies/ceannottype" should {
      "add a collection event annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotType = factory.createCollectionEventAnnotationType
        val json = makeRequest(POST, "/studies/ceannottype", json = annotTypeToAddCmdJson(annotType))
          (json \ "status").as[String] should include ("success")
      }
    }

    "POST /studies/ceannottype" should {
      "not add a collection event annotation type to an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "POST /studies/ceannottype" should {
      "not add a collection event annotation type to an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "PUT /studies/ceannottype" should {
      "update a collection event annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotType = factory.createCollectionEventAnnotationType
        use[BbwebPlugin].collectionEventAnnotationTypeRepository.put(annotType)

        val annotType2 = factory.createCollectionEventAnnotationType.copy(
          id = annotType.id,
          version = annotType.version
        )

        val json = makeRequest(PUT,
          s"/studies/ceannottype/${annotType.id.id}",
          json = annotTypeToUpdateCmdJson(annotType2))

        (json \ "status").as[String] should include ("success")
      }
    }

    "PUT /studies/ceannottype" should {
      "not update a collection event annotation type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "PUT /studies/ceannottype" should {
      "not update a collection event annotation type on an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "DELETE /studies/ceannottype" should {
      "remove a collection event annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val annotType = factory.createCollectionEventAnnotationType
        use[BbwebPlugin].collectionEventAnnotationTypeRepository.put(annotType)

        val json = makeRequest(
          DELETE,
          s"/studies/ceannottype/${annotType.studyId.id}/${annotType.id.id}/${annotType.version}")

        (json \ "status").as[String] should include ("success")
      }
    }

    "DELETE /studies/ceannottype" should {
      "not remove a collection event annotation type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "DELETE /studies/ceannottype" should {
      "not remove a collection event annotation type on an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }
  }

}
