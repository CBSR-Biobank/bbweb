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

class CeventAnnotTypeControllerSpec
    extends StudyAnnotTypeControllerSpec[CollectionEventAnnotationType] {
  import TestGlobal._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def uri(study: Study): String = s"/studies/${study.id.id}/ceannottypes"

  private def addOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)
    val annotType = factory.createCollectionEventAnnotationType
    collectionEventAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      POST,
      uri(study),
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
      uri(study, annotType),
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
      uri(study, annotType, annotType.version),
      BAD_REQUEST)

    (json \ "status").as[String] must include ("error")
      (json \ "message").as[String] must include ("is not disabled")
  }

  "Collection Event Type REST API" when {
    "GET /studies/ceannottypes" must {
      "list none" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, uri(study))
          (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list a single collection event annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createCollectionEventAnnotationType
        collectionEventAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, uri(study))
          (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1
        compareObj(jsonList(0), annotType)
      }

      "get a single collection event annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createCollectionEventAnnotationType
        collectionEventAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, uriWithQuery(study, annotType)).as[JsObject]
          (json \ "status").as[String] must include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, annotType)
      }

      "list multiple collection event annotation types" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotTypes = List(
          factory.createCollectionEventAnnotationType,
          factory.createCollectionEventAnnotationType)
        annotTypes map { annotType => collectionEventAnnotationTypeRepository.put(annotType) }

        val json = makeRequest(GET, uri(study))
          (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]

        jsonList must have size annotTypes.size
          (jsonList zip annotTypes).map { item => compareObj(item._1, item._2) }
        ()
      }

      "fail for an invalid study ID" in {
        val study = factory.createDisabledStudy

        val json = makeRequest(GET, uri(study), BAD_REQUEST)
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid study ID when using an annotation type id" in {
        val study = factory.createDisabledStudy
        val annotType = factory.createCollectionEventAnnotationType

        val json = makeRequest(GET, uriWithQuery(study, annotType), BAD_REQUEST)
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid collection event annotation type id" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createCollectionEventAnnotationType

        val json = makeRequest(GET, uriWithQuery(study, annotType), BAD_REQUEST)
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("annotation type does not exist")
      }

    }

    "POST /studies/ceannottypes" must {
      "add a collection event annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createCollectionEventAnnotationType
        val json = makeRequest(POST, uri(study), json = annotTypeToAddCmdJson(annotType))
          (json \ "status").as[String] must include ("success")
      }

      "not add a collection event annotation type to an enabled study" in {
        addOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }

      "not add a collection event annotation type to an retired study" in {
        addOnNonDisabledStudy(factory.createDisabledStudy.retire | fail)
      }
    }

    "PUT /studies/ceannottypes" must {
      "update a collection event annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createCollectionEventAnnotationType
        collectionEventAnnotationTypeRepository.put(annotType)

        val annotType2 = factory.createCollectionEventAnnotationType.copy(
          id = annotType.id,
          version = annotType.version
        )

        val json = makeRequest(PUT, uri(study, annotType), json = annotTypeToUpdateCmdJson(annotType2))

        (json \ "status").as[String] must include ("success")
      }

      "not update a collection event annotation type on an enabled study" in {
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }

      "not update a collection event annotation type on an retired study" in {
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "DELETE /studies/ceannottypes" must {
      "remove a collection event annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createCollectionEventAnnotationType
        collectionEventAnnotationTypeRepository.put(annotType)

        val json = makeRequest(DELETE, uri(study, annotType, annotType.version))

        (json \ "status").as[String] must include ("success")
      }
    }

    "DELETE /studies/ceannottypes" must {
      "not remove a collection event annotation type on an enabled study" in {
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "DELETE /studies/ceannottypes" must {
      "not remove a collection event annotation type on an retired study" in {
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }
  }

}
