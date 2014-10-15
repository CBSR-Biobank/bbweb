package org.biobank.controllers.study

import org.biobank.fixture._
import org.biobank.domain.study.{ Study, SpecimenLinkAnnotationType }
import org.biobank.fixture.ControllerFixture
import org.biobank.domain.JsonHelper._

import com.typesafe.plugin._
import org.joda.time.DateTime
import org.scalatest.Tag
import org.scalatestplus.play._
import org.slf4j.LoggerFactory
import play.api.Play.current
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test.WithApplication

class SpecimenLinkAnnotTypeControllerSpec extends ControllerFixture {
  import TestGlobal._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

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
    studyRepository.put(study)

    val annotType = factory.createSpecimenLinkAnnotationType
    specimenLinkAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      POST,
      "/studies/slannottypes",
      BAD_REQUEST,
      annotTypeToAddCmdJson(annotType))

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  private def updateOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val annotType = factory.createSpecimenLinkAnnotationType
    specimenLinkAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      PUT,
      s"/studies/slannottypes/${annotType.id.id}",
      BAD_REQUEST,
      annotTypeToUpdateCmdJson(annotType))

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  def removeOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val sg = factory.createSpecimenGroup
    specimenGroupRepository.put(sg)

    val annotType = factory.createSpecimenLinkAnnotationType
    specimenLinkAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      DELETE,
      s"/studies/slannottypes/${annotType.studyId.id}/${annotType.id.id}/${annotType.version}",
      BAD_REQUEST)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  "Collection Event Type REST API" when {

    "GET /studies/slannottypes" must {
      "list none" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, s"/studies/slannottypes/${study.id.id}")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list a single collection event annotation type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        specimenLinkAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, s"/studies/slannottypes/${study.id.id}")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1
        compareObj(jsonList(0), annotType)
      }

      "get a single collection event annotation type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        specimenLinkAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, s"/studies/slannottypes/${study.id.id}?annotTypeId=${annotType.id.id}")
        (json \ "status").as[String] must include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, annotType)
      }

      "list multiple collection event annotation types" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotTypes = List(
          factory.createSpecimenLinkAnnotationType,
          factory.createSpecimenLinkAnnotationType)
        annotTypes map { annotType => specimenLinkAnnotationTypeRepository.put(annotType) }

        val json = makeRequest(GET, s"/studies/slannottypes/${study.id.id}")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]

        jsonList must have size annotTypes.size
          (jsonList zip annotTypes).map { item => compareObj(item._1, item._2) }
        ()
      }

      "fail for invalid study id" in new App(fakeApp) {
        doLogin
        val studyId = nameGenerator.next[Study]

        val json = makeRequest(GET, s"/studies/slannottypes/$studyId", BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid study ID when using an annotation type id" in new App(fakeApp) {
        doLogin
        val studyId = nameGenerator.next[Study]
        val annotTypeId = nameGenerator.next[SpecimenLinkAnnotationType]

        val json = makeRequest(GET, s"/studies/slannottypes/$studyId?annotTypeId=$annotTypeId", BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid specimen link annotation type id" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotTypeId = nameGenerator.next[SpecimenLinkAnnotationType]

        val json = makeRequest(GET, s"/studies/slannottypes/${study.id}?annotTypeId=$annotTypeId", BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("annotation type does not exist")
      }
    }

    "POST /studies/slannottypes" must {
      "add a collection event annotation type" taggedAs(Tag("1")) in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        val json = makeRequest(POST, "/studies/slannottypes", json = annotTypeToAddCmdJson(annotType))
          (json \ "status").as[String] must include ("success")
      }

      "not add a collection event annotation type to an enabled study" in new App(fakeApp) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }

      "not add a collection event annotation type to an retired study" in new App(fakeApp) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "PUT /studies/slannottypes" must {
      "update a collection event annotation type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        specimenLinkAnnotationTypeRepository.put(annotType)

        val annotType2 = factory.createSpecimenLinkAnnotationType.copy(
          id = annotType.id,
          version = annotType.version
        )

        val json = makeRequest(PUT,
          s"/studies/slannottypes/${annotType.id.id}",
          json = annotTypeToUpdateCmdJson(annotType2))

        (json \ "status").as[String] must include ("success")
      }
    }

    "PUT /studies/slannottypesss" must {
      "not update a collection event annotation type on an enabled study" in new App(fakeApp) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "PUT /studies/slannottypess" must {
      "not update a collection event annotation type on an retired study" in new App(fakeApp) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "DELETE /studies/slannottypes" must {
      "remove a collection event annotation type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        specimenLinkAnnotationTypeRepository.put(annotType)

        val json = makeRequest(
          DELETE,
          s"/studies/slannottypes/${annotType.studyId.id}/${annotType.id.id}/${annotType.version}")

        (json \ "status").as[String] must include ("success")
      }
    }

    "DELETE /studies/slannottypes" must {
      "not remove a collection event annotation type on an enabled study" in new App(fakeApp) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "DELETE /studies/slannottypes" must {
      "not remove a collection event annotation type on an retired study" in new App(fakeApp) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }
  }

}
