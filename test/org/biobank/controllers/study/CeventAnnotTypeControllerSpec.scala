package org.biobank.controllers.study

import org.biobank.fixture._
import org.biobank.domain.study._
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
import org.scalatestplus.play._

class CeventAnnotTypeControllerSpec extends ControllerFixture {
  import TestGlobal._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

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
    studyRepository.put(study)
    val annotType = factory.createCollectionEventAnnotationType
    collectionEventAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      POST,
      "/studies/ceannottypes",
      BAD_REQUEST,
      annotTypeToAddCmdJson(annotType))

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  private def updateOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val annotType = factory.createCollectionEventAnnotationType
    collectionEventAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      PUT,
      s"/studies/ceannottypes/${annotType.id.id}",
      BAD_REQUEST,
      annotTypeToUpdateCmdJson(annotType))

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  def removeOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val sg = factory.createSpecimenGroup
    specimenGroupRepository.put(sg)

    val annotType = factory.createCollectionEventAnnotationType
    collectionEventAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      DELETE,
      s"/studies/ceannottypes/${annotType.studyId.id}/${annotType.id.id}/${annotType.version}",
      BAD_REQUEST)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  "Collection Event Type REST API" when {
    "GET /studies/ceannottypes" must {
      "list none" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, s"/studies/ceannottypes/${study.id.id}")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list a single collection event annotation type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createCollectionEventAnnotationType
        collectionEventAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, s"/studies/ceannottypes/${study.id.id}")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1
        compareObj(jsonList(0), annotType)
      }

      "get a single collection event annotation type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createCollectionEventAnnotationType
        collectionEventAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, s"/studies/ceannottypes/${study.id.id}?annotTypeId=${annotType.id.id}").as[JsObject]
        (json \ "status").as[String] must include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, annotType)
      }

      "list multiple collection event annotation types" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotTypes = List(
          factory.createCollectionEventAnnotationType,
          factory.createCollectionEventAnnotationType)
        annotTypes map { annotType => collectionEventAnnotationTypeRepository.put(annotType) }

        val json = makeRequest(GET, s"/studies/ceannottypes/${study.id.id}")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]

        jsonList must have size annotTypes.size
          (jsonList zip annotTypes).map { item => compareObj(item._1, item._2) }
        ()
      }

      "fail for an invalid study ID" in new App(fakeApp) {
        doLogin
        val studyId = nameGenerator.next[Study]

        val json = makeRequest(GET, s"/studies/ceannottypes/$studyId", BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid study ID when using an annotation type id" in new App(fakeApp) {
        doLogin
        val studyId = nameGenerator.next[Study]
        val annotTypeId = nameGenerator.next[CollectionEventAnnotationType]

        val json = makeRequest(GET, s"/studies/ceannottypes/$studyId?annotTypeId=$annotTypeId", BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid collection event annotation type id" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotTypeId = nameGenerator.next[CollectionEventAnnotationType]

        val json = makeRequest(GET, s"/studies/ceannottypes/${study.id}?annotTypeId=$annotTypeId", BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("annotation type does not exist")
      }

    }

    "POST /studies/ceannottypes" must {
      "add a collection event annotation type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createCollectionEventAnnotationType
        val json = makeRequest(POST, "/studies/ceannottypes", json = annotTypeToAddCmdJson(annotType))
          (json \ "status").as[String] must include ("success")
      }
    }

    "POST /studies/ceannottypes" must {
      "not add a collection event annotation type to an enabled study" in new App(fakeApp) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "POST /studies/ceannottypes" must {
      "not add a collection event annotation type to an retired study" in new App(fakeApp) {
        doLogin
        addOnNonDisabledStudy(factory.createDisabledStudy.retire | fail)
      }
    }

    "PUT /studies/ceannottypes" must {
      "update a collection event annotation type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createCollectionEventAnnotationType
        collectionEventAnnotationTypeRepository.put(annotType)

        val annotType2 = factory.createCollectionEventAnnotationType.copy(
          id = annotType.id,
          version = annotType.version
        )

        val json = makeRequest(PUT,
          s"/studies/ceannottypes/${annotType.id.id}",
          json = annotTypeToUpdateCmdJson(annotType2))

        (json \ "status").as[String] must include ("success")
      }
    }

    "PUT /studies/ceannottypes" must {
      "not update a collection event annotation type on an enabled study" in new App(fakeApp) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "PUT /studies/ceannottypes" must {
      "not update a collection event annotation type on an retired study" in new App(fakeApp) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "DELETE /studies/ceannottypes" must {
      "remove a collection event annotation type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createCollectionEventAnnotationType
        collectionEventAnnotationTypeRepository.put(annotType)

        val json = makeRequest(
          DELETE,
          s"/studies/ceannottypes/${annotType.studyId.id}/${annotType.id.id}/${annotType.version}")

        (json \ "status").as[String] must include ("success")
      }
    }

    "DELETE /studies/ceannottypes" must {
      "not remove a collection event annotation type on an enabled study" in new App(fakeApp) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "DELETE /studies/ceannottypes" must {
      "not remove a collection event annotation type on an retired study" in new App(fakeApp) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }
  }

}
