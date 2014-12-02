package org.biobank.controllers.study

import org.biobank.fixture._
import org.biobank.domain.{ AnnotationTypeId, AnnotationOption }
import org.biobank.domain.study.{ Study, StudyId, Participant, ParticipantId, ParticipantAnnotation }
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

  val nameGenerator = new NameGenerator(this.getClass)

  def uri(study: Study): String = s"/studies/${study.id.id}/participants"

  def uri(study: Study, participant: Participant): String =
    s"/studies/${study.id.id}/participants/${participant.id.id}"

  def annotationOptionToJson(annotationOption: AnnotationOption) = {
    Json.obj(
      "annotationTypeId" -> annotationOption.annotationTypeId,
      "value"            -> annotationOption.value
    )
  }

  /** Converts a participant annotation into a Json object.
    */
  def annotationToJson(annotation: ParticipantAnnotation) = {
    val json = Json.obj(
      "participantId"    -> annotation.participantId,
      "annotationTypeId" -> annotation.annotationTypeId,
      "stringValue"      -> annotation.stringValue,
      "numberValue"      -> annotation.numberValue
    )

    annotation.selectedValues.fold {
      json
    } { values =>
      json ++ Json.obj(
        "selectedValues" -> values.map(value => annotationOptionToJson(value))
        // "selectedValues" -> values.map(value =>
        //   Json.obj(
        //     "annotationTypeId" -> annotation.annotationTypeId,
        //     "value"            -> value)
        // )
      )
    }
  }

  /** Converts a participant into an Add command.
    */
  def participantToAddCmd(participant: Participant) = {
    Json.obj(
      "studyId"     -> participant.studyId.id,
      "uniqueId"    -> participant.uniqueId,
      "annotations" -> participant.annotations.map(annotation => annotationToJson(annotation))
    )
  }

  /** Converts a participant into an Update command.
    */
  def participantToUpdateCmd(participant: Participant) = {
    participantToAddCmd(participant) ++ Json.obj(
      "id"              -> participant.id.id,
      "expectedVersion" -> Some(participant.version)
    )
  }

  "Study REST API" when {

    "GET /studies/{studyId}/participants/{id}" must {

      "get participant" in new App(fakeApp) {
        doLogin

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.defaultParticipant.copy(
          annotations = Set(factory.createParticipantAnnotation))
        participantRepository.put(participant)

        val json = makeRequest(GET, uri(factory.defaultEnabledStudy, participant))
        (json \ "status").as[String] must include ("success")
        val jsObj = (json \ "data").as[JsObject]
        compareObj(jsObj, participant)
      }

      "get participant, no annotations" in new App(fakeApp) {
        doLogin

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        participantRepository.put(participant)

        val json = makeRequest(GET, uri(factory.defaultEnabledStudy, participant))
        (json \ "status").as[String] must include ("success")
        val jsObj = (json \ "data").as[JsObject]
        compareObj(jsObj, participant)
      }

    }

    "POST /studies/{studyId}/participants" must {

      "add a participant with no annotation types" in new App(fakeApp) {
        doLogin

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        val json = makeRequest(POST, uri(study), json = participantToAddCmd(participant))
          (json \ "status").as[String] must include ("success")
      }

      "add a participant with annotation types" taggedAs(Tag("1")) in new App(fakeApp) {
        doLogin

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType.copy(
          studyId = study.id, required = true);
        factory.defaultParticipantAnnotationType(annotType)
        participantAnnotationTypeRepository.put(annotType)

        val participant = factory.createParticipant.copy(
          annotations = Set(factory.createParticipantAnnotation))

        val json = makeRequest(POST, uri(study), json = participantToAddCmd(participant))
          (json \ "status").as[String] must include ("success")
      }

      "fail when adding participant with duplicate uniqueId" in new App(fakeApp) {
        doLogin

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        participantRepository.put(participant)

        // participant already in repository, request to add another with same uniqueId should fail
        val json = makeRequest(POST, uri(study), FORBIDDEN, json = participantToAddCmd(participant))
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("participant with unique ID already exists")
      }

      "fail when missing a required annotation type" taggedAs(Tag("1")) in new App(fakeApp) {
        doLogin

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType.copy(
          studyId = study.id, required = true);
        participantAnnotationTypeRepository.put(annotType)
        factory.defaultParticipantAnnotationType(annotType)

        val participant = factory.createParticipant
        val json = makeRequest(POST, uri(study), BAD_REQUEST, json = participantToAddCmd(participant))
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("missing required annotation type")
      }

      "fail for an invalid annotation type" in new App(fakeApp) {
        doLogin

        // annotation type belongs to a different study
        val annotType = factory.createParticipantAnnotationType
        participantAnnotationTypeRepository.put(annotType)
        val annotation = factory.createParticipantAnnotation

        val study = factory.createEnabledStudy
        studyRepository.put(study)
        val participant = factory.createParticipant.copy(annotations = Set(annotation))

        val json = makeRequest(POST, uri(study), BAD_REQUEST, json = participantToAddCmd(participant))
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("annotation type(s) do not belong to study")
      }
    }

    "PUT /studies/{studyId}/participants" must {

      "update a participant with no annotation types" in new App(fakeApp) {
        doLogin

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        participantRepository.put(participant)

        val json = makeRequest(PUT, uri(study, participant), json = participantToUpdateCmd(participant))
          (json \ "status").as[String] must include ("success")
      }

      "update a participant with annotation types" in new App(fakeApp) {
        doLogin
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        participantRepository.put(participant)

        val annotType = factory.createParticipantAnnotationType.copy(studyId = study.id)
        participantAnnotationTypeRepository.put(annotType)
        val annotation = factory.createParticipantAnnotation

        val p2 = participant.copy(annotations = Set(annotation))

        val json = makeRequest(PUT, uri(study, p2), json = participantToUpdateCmd(p2))
          (json \ "status").as[String] must include ("success")
      }

      "fail when missing a required annotation type" in new App(fakeApp) {
        doLogin
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType.copy(
          studyId = study.id, required = true)
        participantAnnotationTypeRepository.put(annotType)
        val participant = factory.createParticipant.copy(
          annotations = Set(factory.createParticipantAnnotation))
        participantRepository.put(participant)

        val p2 = participant.copy(annotations = Set.empty)

        val json = makeRequest(PUT, uri(study, p2), BAD_REQUEST, json = participantToUpdateCmd(p2))
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("missing required annotation type")
      }

      "fail for an invalid annotation type" in new App(fakeApp) {
        doLogin

        // annotation type belongs to a different study
        val annotType = factory.createParticipantAnnotationType
        participantAnnotationTypeRepository.put(annotType)
        val annotation = factory.createParticipantAnnotation

        val study = factory.createEnabledStudy
        studyRepository.put(study)
        val participant = factory.createParticipant
        participantRepository.put(participant)

        val p2 = factory.createParticipant.copy(annotations = Set(annotation))

        val json = makeRequest(PUT, uri(study, p2), BAD_REQUEST, json = participantToUpdateCmd(p2))
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("annotation type(s) do not belong to study")
      }

    }

  }

}

