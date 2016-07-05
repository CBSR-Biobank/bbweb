package org.biobank.controllers.participants

import org.biobank.controllers._
import org.biobank.domain.participants._
import org.biobank.domain.study._
import org.biobank.domain.{ Annotation, AnnotationType, AnnotationValueType, DomainValidation }
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.test.Helpers._

/**
 * Tests the REST API for [[Participants]].
 */
class ParticipantsControllerSpec extends StudyAnnotationsControllerSharedSpec[Participant] {

  import org.biobank.TestUtils._
  import org.biobank.AnnotationTestUtils._

  def uri: String = "/participants"

  def uri(study: Study): String = uri + s"/${study.id.id}"

  def uriUniqueId(study: Study, participant: Participant): String =
    uri + s"/uniqueId/${study.id.id}/${participant.uniqueId}"

  def uri(study: Study, participant: Participant): String =
    uri(study) + s"/${participant.id.id}"

  def uri(participant: Participant): String =
    uri + s"/${participant.id.id}"

  def updateUri(participant: Participant, path: String): String =
    uri + s"/$path/${participant.id.id}"

  protected def createEntity(annotationTypes: Set[AnnotationType],
                             annotations:     Set[Annotation]): Participant = {
    val study = factory.createEnabledStudy.copy(annotationTypes = annotationTypes)
    studyRepository.put(study)

    val participant = factory.createParticipant.copy(annotations = annotations)
    participantRepository.put(participant)

    participant
  }

  protected def entityFromRepository(id: String): DomainValidation[Participant] = {
    participantRepository.getByKey(ParticipantId(id))
  }

  protected def entityName(): String = "participant"

  protected def updateUri(participant: Participant): String = updateUri(participant, "annot")

  protected def getStudy(participant: Participant): DomainValidation[EnabledStudy] = {
      studyRepository.getEnabled(participant.studyId)
  }

  def participantToAddJson(participant: Participant, annotations: List[Annotation] = List.empty) = {
    val annots = if (annotations.isEmpty) participant.annotations
                 else annotations
    Json.obj(
      "uniqueId"    -> participant.uniqueId,
      "annotations" -> annots.map(annotationToJson(_))
    )
  }

  /** Converts a participant into an Update command.
   */
  def participantToUpdateCmd(participant: Participant) = {
    participantToAddJson(participant) ++ Json.obj(
      "id"              -> participant.id.id,
      "expectedVersion" -> Some(participant.version)
    )
  }

  def createAnnotationType() = {
    AnnotationType(
      uniqueId      = nameGenerator.next[AnnotationType],
      name          = nameGenerator.next[AnnotationType],
      description   = None,
      valueType     = AnnotationValueType.Text,
      maxValueCount = None,
      options       = Seq.empty,
      required      = false)
  }

  def addOnNonEnabledStudy(study: Study, participant: Participant) = {
    study must not be an [EnabledStudy]

    studyRepository.put(study)

    val cmdJson = participantToAddJson(participant);
    val json = makeRequest(POST, uri(study), BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex("InvalidStatus.*study not enabled")
  }

  def updateOnNonEnabledStudy(study:       Study,
                              participant: Participant,
                              path:        String,
                              jsonField:   JsObject) = {
    study must not be an [EnabledStudy]

    studyRepository.put(study)
    participantRepository.put(participant)

    val participant2 = factory.createParticipant.copy(id      = participant.id,
                                                      studyId = participant.studyId)
    val json = makeRequest(POST,
                           updateUri(participant2, path),
                           BAD_REQUEST,
                           jsonField ++ Json.obj("expectedVersion" -> participant.version))

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex("InvalidStatus.*study not enabled")
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
           (factory.createAnnotationType(vt, Some(1), options),
            factory.createAnnotation)
         case _ =>
           (factory.createAnnotationType(vt, None, Seq.empty),
            factory.createAnnotation)
       }
     }.toList ++ List(
       (factory.createAnnotationType(AnnotationValueType.Select,
                                     Some(2),
                                     options),
        factory.createAnnotation))).toMap
  }


  "Study REST API" when {

    "GET /participants/:studyId/:id" must {

      "get participant" in {

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.defaultParticipant.copy(
          annotations = Set(factory.createAnnotation))
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

    "GET /participants/uniqueId/:studyId/:id" must {

      "must return false for a participant ID that exists" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        participantRepository.put(participant)

        val json = makeRequest(GET, uriUniqueId(study, participant))

        (json \ "status").as[String] must include ("success")

        val jsObj = (json \ "data").as[JsObject]
        compareObj(jsObj, participant)
      }

      "must return BAD_REQUEST for a participant ID that exists but in a different study" in {
        val participant = factory.createParticipant
        participantRepository.put(participant)

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, uriUniqueId(study, participant), BAD_REQUEST)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*participant")
      }

      "must return NOT_FOUND for a participant ID that does not exist" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant

        val json = makeRequest(GET, uriUniqueId(study, participant), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaNotFound.*participant.*unique ID")
      }

    }

    "POST /participants/:studyId" must {

      "add a participant with no annotation types" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        val json = makeRequest(POST, uri(study), participantToAddJson(participant))
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
        val annotTypes = createAnnotationsAndTypes
        val annotations = annotTypes.values.toSet

        val study = factory.createEnabledStudy.copy(annotationTypes = annotTypes.keys.toSet)
        studyRepository.put(study)

        val participant = factory.createParticipant.copy(annotations = annotations)

        val json = makeRequest(POST, uri(study), json = participantToAddJson(participant))
        (json \ "status").as[String] must include ("success")

        val jsonAnnotations = (json \ "data" \ "annotations").as[List[JsObject]]
        jsonAnnotations must have size annotations.size

        jsonAnnotations.foreach { jsonAnnotation =>
          val jsonAnnotationTypeId = (jsonAnnotation \ "annotationTypeId").as[String]
          val annotation = annotations.find( x => x.annotationTypeId == jsonAnnotationTypeId)
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
        val json = makeRequest(POST, uri(study), FORBIDDEN, json = participantToAddJson(participant))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("participant with unique ID already exists")
      }

      "fail when missing a required annotation type" in {
        val annotType = factory.createAnnotationType.copy(required = true);
        val study = factory.createEnabledStudy.copy(annotationTypes = Set(annotType))
        studyRepository.put(study)

        val participant = factory.createParticipant
        val json = makeRequest(POST, uri(study), BAD_REQUEST, json = participantToAddJson(participant))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("missing required annotation type")
      }

      "fail when participant has annotations and the study does not have any annotation types" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val annotation = factory.createAnnotation
        val participant = factory.createParticipant.copy(annotations = Set(annotation))

        val json = makeRequest(POST, uri(study), BAD_REQUEST, json = participantToAddJson(participant))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("no annotation types")
      }

      "for an annotation with an invalid annotation type id" in {
        val annotType = factory.createAnnotationType
        val annotation = factory.createAnnotation.copy(annotationTypeId = nameGenerator.next[AnnotationType])

        val study = factory.createEnabledStudy.copy(annotationTypes = Set(annotType))
        studyRepository.put(study)

        val participant = factory.createParticipant.copy(annotations = Set(annotation))

        val json = makeRequest(POST, uri(study), BAD_REQUEST, json = participantToAddJson(participant))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("annotation(s) do not belong to annotation types")
      }

      "fail for more than one annotation with the same annotation type" in {
        val pat = createAnnotationType
        val study = factory.createEnabledStudy.copy(annotationTypes = Set(pat))
        studyRepository.put(study)

        val annotation = factory.createAnnotation
        val annotation2 = annotation
          .copy(stringValue = Some(nameGenerator.next[Annotation]))

        val participant = factory.createParticipant

        val json = makeRequest(POST,
                               uri(study),
                               BAD_REQUEST,
                               participantToAddJson(participant, List(annotation, annotation2)))

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("duplicate annotations")
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

    "POST /participants/uniqueId/:id" must {

      "update a participant's unique id" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant: Participant = factory.createParticipant
        participantRepository.put(participant)

        val json = makeRequest(POST,
                               updateUri(participant, "uniqueId"),
                               Json.obj("uniqueId"        -> participant.uniqueId,
                                        "expectedVersion" -> participant.version))

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

      "fail when updating a participant's unique id to one already used" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participants = (1 to 2).map { _ =>
            val participant = factory.createParticipant
            participantRepository.put(participant)
            participant
          }

        val participantToUpdate = participants(1)
        val duplicateUniqueId   = participants(0).uniqueId

        val json = makeRequest(POST,
                               updateUri(participantToUpdate, "uniqueId"),
                               FORBIDDEN,
                               Json.obj("uniqueId"        -> duplicateUniqueId,
                                        "expectedVersion" -> participantToUpdate.version))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("participant with unique ID already exists")
      }

      "not update a participant on an disabled study" in {
        val disabledStudy = factory.createDisabledStudy
        val participant = factory.createParticipant.copy(studyId = disabledStudy.id)
        updateOnNonEnabledStudy(disabledStudy,
                                participant,
                                "uniqueId",
                                Json.obj("uniqueId" -> participant.uniqueId))
      }

      "not update a participant on an retired study" in {
        val retiredStudy = factory.createRetiredStudy
        val participant = factory.createParticipant.copy(studyId = retiredStudy.id)
        updateOnNonEnabledStudy(retiredStudy,
                                participant,
                                "uniqueId",
                                Json.obj("uniqueId" -> participant.uniqueId))
      }

    }

    "POST /participants/annot/:id" must {

      annotationTypeUpdateSharedBehaviour

    }

    "DELETE /participants/annot/:id/:annotTypeId/:ver" must {

      annotationTypeRemoveSharedBehaviour

    }

  }

}
