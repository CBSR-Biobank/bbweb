package org.biobank.controllers.study

import org.biobank.fixture._
import org.biobank.domain.{ AnnotationTypeId, AnnotationOption, AnnotationValueType }
import org.biobank.domain.study._
import org.biobank.domain.JsonHelper._
import org.biobank.fixture.ControllerFixture

import play.api.test.Helpers._
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime

/**
 * Tests the REST API for [[Participants]].
 */
class ParticipantsControllerSpec extends ControllerFixture {

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
      "annotationTypeId" -> annotation.annotationTypeId,
      "stringValue"      -> annotation.stringValue,
      "numberValue"      -> annotation.numberValue
    )

    json ++ Json.obj("selectedValues" -> annotation.selectedValues.map(value => annotationOptionToJson(value)))
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

  def createParticipantAnnotationType(study: Study) = {
    ParticipantAnnotationType(
      study.id, AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType]),
      0L, DateTime.now, None, nameGenerator.next[ParticipantAnnotationType], None,
      AnnotationValueType.Text, None, Seq.empty, false)
  }


  "Study REST API" when {

    "GET /studies/{studyId}/participants/{id}" must {

      "get participant" in {

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

      "get participant with no annotations" in {
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

    "GET /studies/:studyId/participants/uniqueId/:id" must {

      "must return false for a participant ID that exists" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        var participant = factory.createParticipant
        participantRepository.put(participant)

        val json = makeRequest(GET, uri(study) + "/uniqueId/" + participant.uniqueId)
        (json \ "status").as[String] must include ("success")
        val jsObj = (json \ "data").as[JsObject]
        compareObj(jsObj, participant)
      }

      "must return BAD_REQUEST for a participant ID that exists but in a different study" in {
        var participant = factory.createParticipant
        participantRepository.put(participant)

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, uri(study) + "/uniqueId/" + participant.uniqueId, BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("study does not have participant")
      }

      "must return NOT_FOUND for a participant ID that does not exist" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        var participantUniqueId = nameGenerator.next[Participant]

        val json = makeRequest(GET, uri(study) + "/uniqueId/" + participantUniqueId, NOT_FOUND)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("participant does not exist")
      }

    }

    "POST /studies/{studyId}/participants" must {

      "add a participant with no annotation types" taggedAs(Tag("1")) in {

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        val json = makeRequest(POST, uri(study), json = participantToAddCmd(participant))
        (json \ "status").as[String] must include ("success")
      }

      "add a participant with annotation types" in {

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

      "fail when adding participant with duplicate uniqueId" in {

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        participantRepository.put(participant)

        // participant already in repository, request to add another with same uniqueId should fail
        val json = makeRequest(POST, uri(study), FORBIDDEN, json = participantToAddCmd(participant))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("participant with unique ID already exists")
      }

      "fail when missing a required annotation type" in {

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

      "fail for an invalid annotation type" in {

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

      "fail for more than one annotation with the same annotation type" in {

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val pat = createParticipantAnnotationType(study)
        factory.defaultParticipantAnnotationType(pat)
        participantAnnotationTypeRepository.put(pat)

        val annotation = factory.createParticipantAnnotation
        val annotation2 = annotation.copy(stringValue = Some(nameGenerator.next[ParticipantAnnotation]))

        val participant = factory.createParticipant.copy(
          annotations = Set(annotation, annotation2))

        val json = makeRequest(POST, uri(study), BAD_REQUEST, json = participantToAddCmd(participant))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("duplicate annotation types in annotations")
      }

      "fail when adding and study IDs do not match" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        participantRepository.put(participant)

        val study2 = factory.createEnabledStudy

        val json = makeRequest(POST, uri(study2), BAD_REQUEST, json = participantToAddCmd(participant))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("study id mismatch")
      }
    }

    "PUT /studies/{studyId}/participants" must {

      "update a participant with no annotation types" in {

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        participantRepository.put(participant)

        val json = makeRequest(PUT, uri(study, participant), json = participantToUpdateCmd(participant))
        (json \ "status").as[String] must include ("success")
      }

      "update a participant with annotation types" in {
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

      "update a participant to remove an annotation type" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val annotType = factory.createParticipantAnnotationType.copy(studyId = study.id)
        participantAnnotationTypeRepository.put(annotType)
        val annotation = factory.createParticipantAnnotation

        val participant = factory.createParticipant.copy(annotations = Set(annotation))
        participantRepository.put(participant)

        val p2 = participant.copy(annotations = Set.empty)

        val json = makeRequest(PUT, uri(study, p2), json = participantToUpdateCmd(p2))
        (json \ "status").as[String] must include ("success")
      }

      "fail when missing a required annotation type" in {
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

      "fail for an invalid annotation type" in {
        // annotation type belongs to a different study
        val annotType = factory.createParticipantAnnotationType
        participantAnnotationTypeRepository.put(annotType)
        val annotation = factory.createParticipantAnnotation

        val study = factory.createEnabledStudy
        studyRepository.put(study)
        val participant = factory.createParticipant
        participantRepository.put(participant)

        val p2 = participant.copy(annotations = Set(annotation))

        val json = makeRequest(PUT, uri(study, p2), BAD_REQUEST, json = participantToUpdateCmd(p2))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("annotation type(s) do not belong to study")
      }

      "fail for more than one annotation with the same annotation type" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val pat = createParticipantAnnotationType(study)
        factory.defaultParticipantAnnotationType(pat)
        participantAnnotationTypeRepository.put(pat)

        val annotation = factory.createParticipantAnnotation
        val annotation2 = annotation.copy(stringValue = Some(nameGenerator.next[ParticipantAnnotation]))

        val participant = factory.createParticipant
        participantRepository.put(participant)

        val p2 = participant.copy(annotations = Set(annotation, annotation2))

        val json = makeRequest(PUT, uri(study, p2), BAD_REQUEST, json = participantToUpdateCmd(p2))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("duplicate annotation types in annotations")
      }

      "fail when updating and study IDs do not match" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        participantRepository.put(participant)

        val study2 = factory.createEnabledStudy

        val json = makeRequest(PUT,
                               uri(study2, participant),
                               BAD_REQUEST,
                               json = participantToUpdateCmd(participant))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("study id mismatch")
      }

      "fail when updating and participant IDs do not match" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        participantRepository.put(participant)

        val participant2 = factory.createParticipant

        val json = makeRequest(PUT,
                               uri(study, participant2),
                               BAD_REQUEST,
                               json = participantToUpdateCmd(participant))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("participant id mismatch")
      }
    }

  }

}

