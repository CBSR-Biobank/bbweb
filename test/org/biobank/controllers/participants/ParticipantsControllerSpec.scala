package org.biobank.controllers.participants

import java.time.OffsetDateTime
import org.biobank.controllers._
import org.biobank.domain._
import org.biobank.domain.annotations._
import org.biobank.domain.participants._
import org.biobank.domain.studies._
import play.api.libs.json._
import play.api.test.Helpers._

/**
 * Tests the REST API for [[Participants]].
 */
class ParticipantsControllerSpec extends StudyAnnotationsControllerSharedSpec[Participant] {

  import org.biobank.TestUtils._
  import org.biobank.AnnotationTestUtils._
  import org.biobank.matchers.EntityMatchers._

  class Fixture {
    val study = factory.createEnabledStudy
    val participant = factory.createParticipant

    Set(study, participant).foreach(addToRepository)
  }

  private def uri(paths: String*): String = {
    val basePath = "/api/participants"
    if (paths.isEmpty) basePath
    else s"$basePath/" + paths.mkString("/")
  }

  def uri(study: Study): String = uri(study.id.id)

  def uri(study: Study, participant: Participant): String = uri(study.id.id, participant.id.id)

  def uri(participant: Participant): String = uri(participant.id.id)

  def updateUri(participant: Participant, path: String): String = uri(path, participant.id.id)

  protected def createEntity(annotationTypes: Set[AnnotationType],
                             annotations:     Set[Annotation]): Participant = {
    val f = new Fixture
    studyRepository.put(f.study.copy(annotationTypes = annotationTypes))

    val participant = f.participant.copy(annotations = annotations)
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

  def addOnNonEnabledStudy(study: Study, participant: Participant) = {
    study must not be an [EnabledStudy]

    studyRepository.put(study)

    val cmdJson = participantToAddJson(participant);
    val json = makeRequest(POST, uri(study), BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex("InvalidStatus: study not enabled")
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

    (json \ "message").as[String] must include regex("InvalidStatus: study not enabled")
  }

  describe("Study REST API") {

    describe("GET /api/participants/:studyId/:id") {

      it("get participant") {

        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.defaultParticipant.copy(
          annotations = Set(factory.createAnnotationWithValues(factory.defaultAnnotationType)))
        participantRepository.put(participant)

        val json = makeRequest(GET, uri(factory.defaultEnabledStudy, participant))
        (json \ "status").as[String] must include ("success")
        val jsObj = (json \ "data").as[JsObject]
        compareObj(jsObj, participant)
      }

      it("get participant with no annotations") {
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

    describe("GET /api/participants/:slug") {

      it("can retrieve a participant by slug") {
        val f = new Fixture
        val json = makeRequest(GET, uri(f.participant.slug.id))

        (json \ "status").as[String] must include ("success")

        val jsObj = (json \ "data").as[JsObject]
        compareObj(jsObj, f.participant)
      }

      it("must return NOT_FOUND for a participant slug that does not exist") {
        val f = new Fixture
        participantRepository.remove(f.participant)
        val json = makeRequest(GET, uri(f.participant.slug.id), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaNotFound.*participant.*slug")
      }

    }

    describe("POST /api/participants/:studyId") {

      it("add a participant with no annotation types") {
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

          pt must beEntityWithTimeStamps(participant.timeAdded, None, 5L)
        }
      }

      it("add a participant with annotations") {
        val annotTypes = createAnnotationsAndTypes
        val annotations = annotTypes.values.toSet
        val study = factory.createEnabledStudy.copy(annotationTypes = annotTypes.keys.toSet)
        studyRepository.put(study)

        val participant = factory.createParticipant.copy(annotations = annotations)

        val json = makeRequest(POST, uri(study), json = participantToAddJson(participant))
        (json \ "status").as[String] must include ("success")

        val jsonAnnotations = (json \ "data" \ "annotations").as[List[JsObject]]
        jsonAnnotations must have size annotations.size.toLong

        jsonAnnotations.foreach { jsonAnnotation =>
          val jsonAnnotationTypeId = (jsonAnnotation \ "annotationTypeId").as[String]
          val annotation = annotations.find( x => x.annotationTypeId.id == jsonAnnotationTypeId).value
          compareAnnotation(jsonAnnotation, annotation)
        }
      }

      it("fail when adding participant with duplicate uniqueId") {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        participantRepository.put(participant)

        // participant already in repository, request to add another with same uniqueId should fail
        val json = makeRequest(POST, uri(study), FORBIDDEN, json = participantToAddJson(participant))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("participant with unique ID already exists")
      }

      it("fail when missing a required annotation type") {
        val annotType = factory.createAnnotationType.copy(required = true);
        val study = factory.createEnabledStudy.copy(annotationTypes = Set(annotType))
        studyRepository.put(study)

        val participant = factory.createParticipant
        val json = makeRequest(POST, uri(study), BAD_REQUEST, json = participantToAddJson(participant))
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("missing required annotation type")
      }

      it("fail when participant has annotations and the study does not have any annotation types") {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val annotation = factory.createAnnotation
        val participant = factory.createParticipant.copy(annotations = Set(annotation))

        val json = makeRequest(POST, uri(study), BAD_REQUEST, json = participantToAddJson(participant))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("no annotation types")
      }

      it("for an annotation with an invalid annotation type id") {
        val annotType = factory.createAnnotationType
        val annotation = factory.createAnnotation
          .copy(annotationTypeId = AnnotationTypeId(nameGenerator.next[AnnotationType]))

        val study = factory.createEnabledStudy.copy(annotationTypes = Set(annotType))
        studyRepository.put(study)

        val participant = factory.createParticipant.copy(annotations = Set(annotation))

        val json = makeRequest(POST, uri(study), BAD_REQUEST, json = participantToAddJson(participant))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("annotation(s) do not belong to annotation types")
      }

      it("fail for more than one annotation with the same annotation type") {
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

      it("not add a participant on an disabled study") {
        val disabledStudy = factory.createDisabledStudy
        val participant = factory.createParticipant.copy(studyId = disabledStudy.id)
        addOnNonEnabledStudy(disabledStudy, participant)
      }

      it("not add a participant on an retired study") {
        val retiredStudy = factory.createRetiredStudy
        val participant = factory.createParticipant.copy(studyId = retiredStudy.id)
        addOnNonEnabledStudy(retiredStudy, participant)
      }
    }

    describe("POST /api/participants/uniqueId/:id") {

      it("update a participant's unique id") {
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

          pt must beEntityWithTimeStamps(participant.timeAdded, Some(OffsetDateTime.now), 5L)
        }
      }

      it("fail when updating a participant's unique id to one already used") {
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

      it("not update a participant on an disabled study") {
        val disabledStudy = factory.createDisabledStudy
        val participant = factory.createParticipant.copy(studyId = disabledStudy.id)
        updateOnNonEnabledStudy(disabledStudy,
                                participant,
                                "uniqueId",
                                Json.obj("uniqueId" -> participant.uniqueId))
      }

      it("not update a participant on an retired study") {
        val retiredStudy = factory.createRetiredStudy
        val participant = factory.createParticipant.copy(studyId = retiredStudy.id)
        updateOnNonEnabledStudy(retiredStudy,
                                participant,
                                "uniqueId",
                                Json.obj("uniqueId" -> participant.uniqueId))
      }

    }

    describe("POST /api/participants/annot/:id") {

      annotationTypeUpdateSharedBehaviour

    }

    describe("DELETE /api/participants/annot/:id/:annotTypeId/:ver") {

      annotationTypeRemoveSharedBehaviour

    }

  }

}
