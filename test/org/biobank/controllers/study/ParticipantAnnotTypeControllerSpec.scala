package org.biobank.controllers.study

import org.biobank.fixture._
import org.biobank.domain.study.{ Study, ParticipantAnnotationType }
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

class ParticipantAnnotTypeControllerSpec extends ControllerFixture {
  import TestGlobal._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  private def annotTypeToAddCmdJson(annotType: ParticipantAnnotationType) = {
    Json.obj(
      "studyId"       -> annotType.studyId.id,
      "name"          -> annotType.name,
      "description"   -> annotType.description,
      "valueType"     -> annotType.valueType.toString,
      "maxValueCount" -> annotType.maxValueCount,
      "options"       -> annotType.options,
      "required"      -> annotType.required
    )
  }

  private def annotTypeToUpdateCmdJson(annotType: ParticipantAnnotationType) = {
    Json.obj(
      "studyId"         -> annotType.studyId.id,
      "id"              -> annotType.id.id,
      "expectedVersion" -> Some(annotType.version),
      "name"            -> annotType.name,
      "valueType"       -> annotType.valueType.toString,
      "maxValueCount"   -> annotType.maxValueCount,
      "options"         -> annotType.options,
      "required"        -> annotType.required
    )
  }

  private def addOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val annotType = factory.createParticipantAnnotationType
    participantAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      POST,
      "/studies/pannottypes",
      BAD_REQUEST,
      annotTypeToAddCmdJson(annotType))

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  private def updateOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val annotType = factory.createParticipantAnnotationType
    participantAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      PUT,
      s"/studies/pannottypes/${annotType.id.id}",
      BAD_REQUEST,
      annotTypeToUpdateCmdJson(annotType))

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  def removeOnNonDisabledStudy(study: Study) {
    studyRepository.put(study)

    val sg = factory.createSpecimenGroup
    specimenGroupRepository.put(sg)

    val annotType = factory.createParticipantAnnotationType
    participantAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      DELETE,
      s"/studies/pannottypes/${annotType.studyId.id}/${annotType.id.id}/${annotType.version}",
      BAD_REQUEST)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  "Participant Type REST API" when {

    "GET /studies/pannottypes" must {
      "list none" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, s"/studies/pannottypes/${study.id.id}")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list a single participant annotation type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        participantAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, s"/studies/pannottypes/${study.id.id}")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1
        compareObj(jsonList(0), annotType)
      }

      "list multiple participant annotation types" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotTypes = List(
          factory.createParticipantAnnotationType,
          factory.createParticipantAnnotationType)
        annotTypes map { annotType => participantAnnotationTypeRepository.put(annotType) }

        val json = makeRequest(GET, s"/studies/pannottypes/${study.id.id}")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]

        jsonList must have size annotTypes.size
          (jsonList zip annotTypes).map { item => compareObj(item._1, item._2) }
        ()
      }

      "fail for an invalid study ID" in new App(fakeApp) {
        doLogin
        val studyId = nameGenerator.next[Study]

        val json = makeRequest(GET, s"/studies/pannottypes/$studyId", BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid study ID when using an annotation type id" in new App(fakeApp) {
        doLogin
        val studyId = nameGenerator.next[Study]
        val annotTypeId = nameGenerator.next[ParticipantAnnotationType]

        val json = makeRequest(GET, s"/studies/pannottypes/$studyId?annotTypeId=$annotTypeId", BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid participant annotation type id" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotTypeId = nameGenerator.next[ParticipantAnnotationType]

        val json = makeRequest(GET, s"/studies/pannottypes/${study.id}?annotTypeId=$annotTypeId", BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("annotation type does not exist")
      }

    }

    "POST /studies/pannottypes" must {
      "add a participant annotation type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        val json = makeRequest(POST, "/studies/pannottypes", json = annotTypeToAddCmdJson(annotType))
          (json \ "status").as[String] must include ("success")
      }
    }

    "POST /studies/pannottypes" must {
      "not add a participant annotation type to an enabled study" in new App(fakeApp) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "POST /studies/pannottypes" must {
      "not add a participant annotation type to an retired study" in new App(fakeApp) {
        doLogin
        addOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "PUT /studies/pannottypes" must {
      "update a participant annotation type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        participantAnnotationTypeRepository.put(annotType)

        val annotType2 = factory.createParticipantAnnotationType.copy(
          id = annotType.id,
          version = annotType.version
        )

        val json = makeRequest(PUT,
          s"/studies/pannottypes/${annotType.id.id}",
          json = annotTypeToUpdateCmdJson(annotType2))

        (json \ "status").as[String] must include ("success")
      }
    }

    "PUT /studies/pannottypes" must {
      "not update a participant annotation type on an enabled study" in new App(fakeApp) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }

    "PUT /studies/pannottypes" must {
      "not update a participant annotation type on an retired study" in new App(fakeApp) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail)
      }
    }

    "DELETE /studies/pannottype" must {
      "remove a participant annotation type" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        participantAnnotationTypeRepository.put(annotType)

        val json = makeRequest(
          DELETE,
          s"/studies/pannottypes/${annotType.studyId.id}/${annotType.id.id}/${annotType.version}")

        (json \ "status").as[String] must include ("success")
      }
    }

    "DELETE /studies/pannottypes" must {
      "not remove a participant annotation type on an enabled study" in new App(fakeApp) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail)
      }
    }
  }

}
