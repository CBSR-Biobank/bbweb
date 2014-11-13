package org.biobank.controllers.study

import org.biobank.domain.study.{ Study, StudyId }
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.JsonHelper._
import org.biobank.fixture.ControllerFixture
import play.api.test.Helpers._
import play.api.test.WithApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.scalatestplus.play._
/**
  * Tests the REST API for [[Study]].
  */
class ParticipantsControllerSpec extends ControllerFixture {
  import TestGlobal._

  val log = LoggerFactory.getLogger(this.getClass)

  def uri(study: Study): String = s"/studies/${study.id.id}/participants"

  "Study REST API" when {

    "GET /studies/{studyId}/participants" must {

      "list none" in new App(fakeApp) {
        doLogin

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list one" in new App(fakeApp) {
        doLogin

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        participantRepository.put(participant)

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1
        compareObj(jsonList(0), participant)
      }

      "list one with annotations" in new App(fakeApp) {
        doLogin

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType
        participantAnnotationTypeRepository.put(annotType)

        val participant = factory.createParticipant
        participantRepository.put(participant)

        val json = makeRequest(GET, uri(study))
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1
        compareObj(jsonList(0), participant)
      }

      "list multiple patients" in new App(fakeApp) {
        doLogin
        ???
      }

      "fail for an invalid study id" in new App(fakeApp) {
        doLogin
        ???
      }

      "fail for an invalid study ID when using a patient ID" in new App(fakeApp) {
        doLogin
        ???
      }

      "fail for an invalid patient ID" in new App(fakeApp) {
        doLogin
        ???
      }

    }

    "POST /studies/{studyId}/participants" must {

      "add a patient with no annotation types" in new App(fakeApp) {
        doLogin
        ???
      }

      "add a patient with annotation types" in new App(fakeApp) {
        doLogin
        ???
      }

      "fail when missing a required annotation type" in new App(fakeApp) {
        doLogin
        ???
      }

    }

    "PUT /studies/{studyId}/participants" must {

      "update a patient with no annotation types" in new App(fakeApp) {
        doLogin
        ???
      }

      "update a patient with annotation types" in new App(fakeApp) {
        doLogin
        ???
      }

      "fail when missing a required annotation type" in new App(fakeApp) {
        doLogin
        ???
      }

    }

  }

}

