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

class SpecimenLinkAnnotTypeControllerSpec extends StudyAnnotTypeControllerSpec[SpecimenLinkAnnotationType] {
  import TestGlobal._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def uri(study: Study): String = s"/studies/${study.id.id}/slannottypes"

  private def addOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val annotType = factory.createSpecimenLinkAnnotationType.copy(studyId = study.id)
    specimenLinkAnnotationTypeRepository.put(annotType)

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

    val annotType = factory.createSpecimenLinkAnnotationType.copy(studyId = study.id)
    specimenLinkAnnotationTypeRepository.put(annotType)

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

    val annotType = factory.createSpecimenLinkAnnotationType
    specimenLinkAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      DELETE,
      uri(study, annotType, annotType.version),
      BAD_REQUEST)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  "Collection Event Type REST API" when {

    "GET /studies/slannottypes" must {
      "list none" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list a single specimen link annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        specimenLinkAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1
        compareObj(jsonList(0), annotType)
      }

      "get a single specimen link annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        specimenLinkAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, uriWithQuery(study, annotType))
        (json \ "status").as[String] must include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, annotType)
      }

      "list multiple specimen link annotation types" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotTypes = List(
          factory.createSpecimenLinkAnnotationType,
          factory.createSpecimenLinkAnnotationType)
        annotTypes map { annotType => specimenLinkAnnotationTypeRepository.put(annotType) }

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]

        jsonList must have size annotTypes.size
        (jsonList zip annotTypes).map { item => compareObj(item._1, item._2) }
        ()
      }

      "fail for invalid study id" in {
        val study = factory.createDisabledStudy

        val json = makeRequest(GET, uri(study), BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid study ID when using an annotation type id" in {
        val study = factory.createDisabledStudy
        val annotType = factory.createSpecimenLinkAnnotationType

        val json = makeRequest(GET, uriWithQuery(study, annotType), BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid specimen link annotation type id" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType

        val json = makeRequest(GET, uriWithQuery(study, annotType), NOT_FOUND)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("annotation type does not exist")
      }
    }

    "POST /studies/slannottypes" must {
      "add a specimen link annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        val json = makeRequest(POST, uri(study), json = annotTypeToAddCmdJson(annotType))
        (json \ "status").as[String] must include ("success")
      }

      "not add a specimen link annotation type to an enabled study" in {
        addOnNonDisabledStudy(factory.createEnabledStudy)
      }

      "not add a specimen link annotation type to an retired study" in {
        addOnNonDisabledStudy(factory.createRetiredStudy)
      }

      "fail when adding and study IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType

        val study2 = factory.createDisabledStudy

        val json = makeRequest(POST,
                               uri(study2),
                               BAD_REQUEST,
                               json = annotTypeToAddCmdJson(annotType))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("study id mismatch")
      }

    }

    "PUT /studies/slannottypes" must {
      "update a specimen link annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        specimenLinkAnnotationTypeRepository.put(annotType)

        val annotType2 = factory.createSpecimenLinkAnnotationType.copy(
          id = annotType.id,
          version = annotType.version
        )

        val json = makeRequest(PUT,
                               uri(study, annotType2),
                               json = annotTypeToUpdateCmdJson(annotType2))

        (json \ "status").as[String] must include ("success")
      }

      "not update a specimen link annotation type on an enabled study" in {
        updateOnNonDisabledStudy(factory.createEnabledStudy)
      }

      "not update a specimen link annotation type on an retired study" in {
        updateOnNonDisabledStudy(factory.createRetiredStudy)
      }

      "fail when updating and study IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        specimenLinkAnnotationTypeRepository.put(annotType)

        val annotType2 = factory.createSpecimenLinkAnnotationType.copy(
          id = annotType.id,
          version = annotType.version
        )

        val study2 = factory.createDisabledStudy

        val json = makeRequest(PUT,
                               uri(study2, annotType2),
                               BAD_REQUEST,
                               json = annotTypeToUpdateCmdJson(annotType2))

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("study id mismatch")
      }

      "fail when updating and annotation type IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        specimenLinkAnnotationTypeRepository.put(annotType)

        val annotType2 = factory.createSpecimenLinkAnnotationType

        val json = makeRequest(PUT,
                               uri(study, annotType2),
                               BAD_REQUEST,
                               json = annotTypeToUpdateCmdJson(annotType))

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("annotation type id mismatch")
      }

    }

    "DELETE /studies/slannottypes" must {
      "remove a specimen link annotation type" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createSpecimenLinkAnnotationType
        specimenLinkAnnotationTypeRepository.put(annotType)

        val json = makeRequest(DELETE, uri(study, annotType, annotType.version))

        (json \ "status").as[String] must include ("success")
      }

      "not remove a specimen link annotation type on an enabled study" in {
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }

      "not remove a specimen link annotation type on an retired study" in {
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }
  }

}
