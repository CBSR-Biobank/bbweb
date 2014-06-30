package org.biobank.controllers

import org.biobank.domain.study.{ Study, ParticipantAnnotationType }
import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.JsonHelper._

import play.api.test.Helpers._
import play.api.test.WithApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class ParticipantAnnotTypeControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  private def annotTypeToAddCmdJson(annotType: ParticipantAnnotationType) = {
    Json.obj(
      "type"          -> "AddParticipantAnnotationTypeCmd",
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
      "type"            -> "UpdateParticipantAnnotationTypeCmd",
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

  private def annotTypeToRemoveCmdJson(annotType: ParticipantAnnotationType) = {
    Json.obj(
      "type"            -> "RemoveParticipantAnnotationTypeCmd",
      "studyId"         -> annotType.studyId.id,
      "id"              -> annotType.id.id,
      "expectedVersion" -> Some(annotType.version)
    )
  }

  private def addOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study) {
    appRepositories.studyRepository.put(study)

    val annotType = factory.createParticipantAnnotationType
    appRepositories.participantAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      POST,
      "/admin/studies/pannottype",
      BAD_REQUEST,
      annotTypeToAddCmdJson(annotType))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("study is not disabled")
  }

  private def updateOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study) {
    appRepositories.studyRepository.put(study)

    val annotType = factory.createParticipantAnnotationType
    appRepositories.participantAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      PUT,
      s"/admin/studies/pannottype/${annotType.id.id}",
      BAD_REQUEST,
      annotTypeToUpdateCmdJson(annotType))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("study is not disabled")
  }

  def removeOnNonDisabledStudy(
    appRepositories: AppRepositories,
    study: Study) {
    appRepositories.studyRepository.put(study)

    val sg = factory.createSpecimenGroup
    appRepositories.specimenGroupRepository.put(sg)

    val annotType = factory.createParticipantAnnotationType
    appRepositories.participantAnnotationTypeRepository.put(annotType)

    val json = makeRequest(
      DELETE,
      s"/admin/studies/pannottype/${annotType.id.id}",
      BAD_REQUEST,
      annotTypeToRemoveCmdJson(annotType))

    (json \ "status").as[String] should include ("error")
    (json \ "message").as[String] should include ("study is not disabled")
  }

  "Participant Type REST API" when {

    "GET /admin/studies/pannottype" should {
      "list none" taggedAs(Tag("single")) in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val json = makeRequest(GET, s"/admin/studies/pannottype/${study.id.id}")
        val jsonList = json.as[List[JsObject]]
        jsonList should have size 0
      }
    }

    "GET /admin/studies/pannottype" should {
      "list a single participant annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        appRepositories.participantAnnotationTypeRepository.put(annotType)

        val json = makeRequest(GET, s"/admin/studies/pannottype/${study.id.id}")
        val jsonList = json.as[List[JsObject]]
        jsonList should have size 1
        compareObj(jsonList(0), annotType)
      }
    }

    "GET /admin/studies/pannottype" should {
      "list multiple participant annotation types" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val annotTypes = List(
          factory.createParticipantAnnotationType,
          factory.createParticipantAnnotationType)
        annotTypes map { annotType => appRepositories.participantAnnotationTypeRepository.put(annotType) }

        val json = makeRequest(GET, s"/admin/studies/pannottype/${study.id.id}")
        val jsonList = json.as[List[JsObject]]

        jsonList should have size annotTypes.size
          (jsonList zip annotTypes).map { item => compareObj(item._1, item._2) }
        ()
      }
    }

    "POST /admin/studies/pannottype" should {
      "add a participant annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        val json = makeRequest(POST, "/admin/studies/pannottype", json = annotTypeToAddCmdJson(annotType))
          (json \ "status").as[String] should include ("success")
      }
    }

    "POST /admin/studies/pannottype" should {
      "not add a participant annotation type to an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        addOnNonDisabledStudy(
          new AppRepositories,
          factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
      }
    }

    "POST /admin/studies/pannottype" should {
      "not add a participant annotation type to an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        addOnNonDisabledStudy(
          new AppRepositories,
          factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
      }
    }

    "PUT /admin/studies/pannottype" should {
      "update a participant annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        appRepositories.participantAnnotationTypeRepository.put(annotType)

        val annotType2 = factory.createParticipantAnnotationType.copy(
          id = annotType.id,
          version = annotType.version
        )

        val json = makeRequest(PUT,
          s"/admin/studies/pannottype/${annotType.id.id}",
          json = annotTypeToUpdateCmdJson(annotType2))

        (json \ "status").as[String] should include ("success")
      }
    }

    "PUT /admin/studies/pannottype" should {
      "not update a participant annotation type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        updateOnNonDisabledStudy(
          new AppRepositories,
          factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
      }
    }

    "PUT /admin/studies/pannottype" should {
      "not update a participant annotation type on an retired study" in new WithApplication(fakeApplication()) {
        doLogin
        updateOnNonDisabledStudy(
          new AppRepositories,
          factory.createDisabledStudy.retire(Some(0), DateTime.now) | fail)
      }
    }

    "DELETE /admin/studies/pannottype" should {
      "remove a participant annotation type" in new WithApplication(fakeApplication()) {
        doLogin
        val appRepositories = new AppRepositories

        val study = factory.createDisabledStudy
        appRepositories.studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        appRepositories.participantAnnotationTypeRepository.put(annotType)

        val json = makeRequest(
          DELETE,
          s"/admin/studies/pannottype/${annotType.id.id}",
          json = annotTypeToRemoveCmdJson(annotType))

        (json \ "status").as[String] should include ("success")
      }
    }

    "DELETE /admin/studies/pannottype" should {
      "not remove a participant annotation type on an enabled study" in new WithApplication(fakeApplication()) {
        doLogin
        removeOnNonDisabledStudy(
          new AppRepositories,
          factory.createDisabledStudy.enable(Some(0), DateTime.now, 1, 1) | fail)
      }
    }
  }

}
