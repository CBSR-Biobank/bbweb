package org.biobank.controllers.participants

import org.biobank.fixture._
import org.biobank.domain.{ AnnotationTypeId, AnnotationOption, AnnotationValueType }
import org.biobank.domain.study._
import org.biobank.domain.participants._
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
  import org.biobank.TestUtils._
  import org.biobank.AnnotationTestUtils._

  def uri(study: Study): String = s"/studies/${study.id.id}/participants"

  def uri(study: Study, participant: Participant): String =
    s"/studies/${study.id.id}/participants/${participant.id.id}"

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
      studyId       = study.id,
      id            = AnnotationTypeId(nameGenerator.next[ParticipantAnnotationType]),
      version       = 0L,
      timeAdded     = DateTime.now,
      timeModified  = None,
      name          = nameGenerator.next[ParticipantAnnotationType],
      description   = None,
      valueType     = AnnotationValueType.Text,
      maxValueCount = None,
      options       = Seq.empty,
      required      = false)
  }

  def addOnNonEnabledStudy(study: Study, participant: Participant) = {
    study.status must not be (EnabledStudy.status)

    studyRepository.put(study)

    val cmdJson = participantToAddCmd(participant);
    val json = makeRequest(POST, uri(study), BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not enabled")
  }

  def updateOnNonEnabledStudy(study: Study, participant: Participant) = {
    study.status must not be (EnabledStudy.status)

    studyRepository.put(study)
    participantRepository.put(participant)

    val participant2 = factory.createParticipant.copy(id      = participant.id,
                                                      studyId = participant.studyId)
    val json = makeRequest(PUT,
                           uri(study, participant2),
                           BAD_REQUEST,
                           participantToUpdateCmd(participant2))

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not enabled")
  }

  /**
   * create pairs of annotation types and annotation of each value type plus a second of type select that
   * allows multiple selections
   *
   * the result is a map where the keys are the annotation types and the values are the corresponding
   * annotations
   */
  def createAnnotationsAndTypes() = {
    val options = Seq(nameGenerator.next[String],
                      nameGenerator.next[String],
                      nameGenerator.next[String])

    (AnnotationValueType.values.map { vt =>
       vt match {
         case AnnotationValueType.Select   =>
           (factory.createParticipantAnnotationType(vt, 1, options, true),
            factory.createParticipantAnnotation)
         case _ =>
           (factory.createParticipantAnnotationType(vt, 0, Seq.empty, true),
            factory.createParticipantAnnotation)
       }
     }.toList ++ List(
       (factory.createParticipantAnnotationType(AnnotationValueType.Select, 2, options, true),
        factory.createParticipantAnnotation))).toMap
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

      "add a participant with no annotation types" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        val json = makeRequest(POST, uri(study), json = participantToAddCmd(participant))
        (json \ "status").as[String] must include ("success")

        val id = (json \ "data" \ "id").as[String]

        participantRepository.getByKey(ParticipantId(id)) mustSucceed { pt =>
          pt must have (
            'version     (0),
            'uniqueId    (participant.uniqueId),
            'annotations (participant.annotations)
          )

          checkTimeStamps(participant, pt.timeAdded, None)
        }
      }

      "add a participant with annotations" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val annotTypes = createAnnotationsAndTypes
        val annotations = annotTypes.values.toSet

        annotTypes.keys.foreach { annotType =>
          participantAnnotationTypeRepository.put(annotType.copy(studyId = study.id))
        }

        val participant = factory.createParticipant.copy(annotations = annotations)

        val json = makeRequest(POST, uri(study), json = participantToAddCmd(participant))
        (json \ "status").as[String] must include ("success")

        val jsonAnnotations = (json \ "data" \ "annotations").as[List[JsObject]]
        jsonAnnotations must have size annotations.size

        jsonAnnotations.foreach { jsonAnnotation =>
          val jsonAnnotationTypeId = (jsonAnnotation \ "annotationTypeId").as[String]
          val annotation = annotations.find( x =>
            x.annotationTypeId.id == jsonAnnotationTypeId)
          annotation mustBe defined
          compareAnnotation(jsonAnnotation, annotation.value)
        }
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

      "not add a participant on an disabled study" in {
        val disabledStudy = factory.createDisabledStudy
        val participant = factory.createParticipant.copy(studyId = disabledStudy.id)
        addOnNonEnabledStudy(disabledStudy, participant)
      }

      "not add a participant on an retired study" in {
        val retiredStudy = factory.createRetiredStudy
        val participant = factory.createParticipant.copy(studyId = retiredStudy.id)
        addOnNonEnabledStudy(retiredStudy, participant)
      }
    }

    "PUT /studies/{studyId}/participants" must {

      "update a participant with no annotation types" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant: Participant = factory.createParticipant.copy(version = 0L)
        participantRepository.put(participant)

        val json = makeRequest(PUT, uri(study, participant),
                               json = participantToUpdateCmd(participant))
        (json \ "status").as[String] must include ("success")
        val id = (json \ "data" \ "id").as[String]

        participantRepository.getByKey(ParticipantId(id)) mustSucceed { pt =>
          pt must have (
            'version                (participant.version + 1),
            'uniqueId               (participant.uniqueId),
            'annotations            (participant.annotations)
          )

          checkTimeStamps(pt, participant.timeAdded, DateTime.now)
        }
      }

      "update a participant with annotation types" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val annotTypes = createAnnotationsAndTypes

        annotTypes.keys.foreach { annotType =>
          participantAnnotationTypeRepository.put(annotType.copy(studyId = study.id))
        }

        val participant = factory.createParticipant.copy(annotations = annotTypes.values.toSet)
        participantRepository.put(participant)

        val newAnnotations = annotTypes.keys.map { annotationType =>
          val (stringValue, numberValue, selectedValues) =
            factory.createAnnotationValues(annotationType)
          ParticipantAnnotation(annotationTypeId = annotationType.id,
                                stringValue      = stringValue,
                                numberValue      = numberValue,
                                selectedValues   = selectedValues)
        }.toSet

        val p2 = participant.copy(version = 0, annotations = newAnnotations)

        val json = makeRequest(PUT, uri(study, p2), json = participantToUpdateCmd(p2))
        (json \ "status").as[String] must include ("success")
        val jsonAnnotations = (json \ "data" \ "annotations").as[List[JsObject]]

        jsonAnnotations.foreach { jsonAnnotation =>
          val jsonAnnotationTypeId = (jsonAnnotation \ "annotationTypeId").as[String]
          val newAnnotation = newAnnotations.find( x =>
            x.annotationTypeId.id == jsonAnnotationTypeId)
          newAnnotation mustBe defined
          compareAnnotation(jsonAnnotation, newAnnotation.value)
        }
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

      "fail when missing a required annotation" in {
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

      "fail for more than one annotation with the same annotation type ID" in {
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

      "not update a participant on an disabled study" in {
        val disabledStudy = factory.createDisabledStudy
        val participant = factory.createParticipant.copy(studyId = disabledStudy.id)
        updateOnNonEnabledStudy(disabledStudy, participant)
      }

      "not update a participant on an retired study" in {
        val retiredStudy = factory.createRetiredStudy
        val participant = factory.createParticipant.copy(studyId = retiredStudy.id)
        updateOnNonEnabledStudy(retiredStudy, participant)
      }
    }

  }

}

