package org.biobank.controllers.participants

import java.time.OffsetDateTime
import org.biobank.controllers._
import org.biobank.domain._
import org.biobank.domain.annotations._
import org.biobank.domain.participants._
import org.biobank.domain.studies._
import org.scalatest.matchers.{MatchResult, Matcher}
import play.api.libs.json._
import play.api.mvc._
import play.api.test.Helpers._
import scala.concurrent.Future

/**
 * Tests the REST API for [[Participants]].
 */
class ParticipantsControllerSpec extends StudyAnnotationsControllerSharedSpec[Participant] {

  import org.biobank.AnnotationTestUtils._
  import org.biobank.matchers.JsonMatchers._
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

  private def uri(study: Study): String = uri(study.id.id)

  private def uri(study: Study, participant: Participant): String = uri(study.id.id, participant.id.id)

  private def updateUri(participant: Participant, path: String): String = uri(path, participant.id.id)

  private def participantToAddJson(participant: Participant, annotations: List[Annotation] = List.empty) = {
    val annots = if (annotations.isEmpty) participant.annotations
                 else annotations
    Json.obj(
      "uniqueId"    -> participant.uniqueId,
      "annotations" -> annots.map(annotationToJson(_))
    )
  }

  describe("Study REST API") {

    describe("GET /api/participants/:studyId/:id") {

      it("get participant") {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.defaultParticipant.copy(
            annotations = Set(factory.createAnnotationWithValues(factory.defaultAnnotationType)))
        participantRepository.put(participant)

        val reply = makeAuthRequest(GET, uri(factory.defaultEnabledStudy, participant)).value
        reply must beOkResponseWithJsonReply

        val replyParticipant = (contentAsJson(reply) \ "data").validate[Participant]
        replyParticipant must be (jsSuccess)
        replyParticipant.get must matchParticipant(participant)
      }

      it("get participant with no annotations") {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        participantRepository.put(participant)

        val reply = makeAuthRequest(GET, uri(factory.defaultEnabledStudy, participant)).value
        reply must beOkResponseWithJsonReply
        val replyParticipant = (contentAsJson(reply) \ "data").validate[Participant]
        replyParticipant must be (jsSuccess)
        replyParticipant.get must matchParticipant(participant)
      }

    }

    describe("GET /api/participants/:slug") {

      it("can retrieve a participant by slug") {
        val f = new Fixture
        val reply = makeAuthRequest(GET, uri(f.participant.slug.id)).value
        reply must beOkResponseWithJsonReply
        val replyParticipant = (contentAsJson(reply) \ "data").validate[Participant]
        replyParticipant must be (jsSuccess)
        replyParticipant.get must matchParticipant(f.participant)
      }

      it("must return NOT_FOUND for a participant slug that does not exist") {
        val f = new Fixture
        participantRepository.remove(f.participant)
        val reply = makeAuthRequest(GET, uri(f.participant.slug.id)).value
        reply must beNotFoundWithMessage("EntityCriteriaNotFound.*participant.*slug")
      }

    }

    describe("POST /api/participants/:studyId") {

      it("add a participant with no annotation types") {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        val reply = makeAuthRequest(POST, uri(study), participantToAddJson(participant)).value
        reply must beOkResponseWithJsonReply

        val newParticipantId = (contentAsJson(reply) \ "data" \ "id").validate[ParticipantId]
        newParticipantId must be (jsSuccess)

        val updatedParticipant = participant.copy(id = newParticipantId.get,
                                                  timeAdded = OffsetDateTime.now)
        reply must matchUpdatedParticipant(updatedParticipant)
      }

      it("add a participant with annotations") {
        val annotTypes = createAnnotationsAndTypes
        val annotations = annotTypes.values.toSet
        val study = factory.createEnabledStudy.copy(annotationTypes = annotTypes.keys.toSet)
        studyRepository.put(study)

        val participant = factory.createParticipant.copy(annotations = annotations)
        val reply = makeAuthRequest(POST, uri(study), json = participantToAddJson(participant)).value
        reply must beOkResponseWithJsonReply

        val newParticipantId = (contentAsJson(reply) \ "data" \ "id").validate[ParticipantId]
        newParticipantId must be (jsSuccess)

        val updatedParticipant = participant.copy(id = newParticipantId.get,
                                                  timeAdded = OffsetDateTime.now)
        reply must matchUpdatedParticipant(updatedParticipant)
      }

      it("fail when adding participant with duplicate uniqueId") {
        val study = factory.createEnabledStudy
        studyRepository.put(study)
        val participant = factory.createParticipant
        participantRepository.put(participant)

        val reply = makeAuthRequest(POST, uri(study), participantToAddJson(participant)).value
        reply must beForbiddenRequestWithMessage("participant with unique ID already exists")
      }

      it("fail when missing a required annotation type") {
        val annotType = factory.createAnnotationType.copy(required = true);
        val study = factory.createEnabledStudy.copy(annotationTypes = Set(annotType))
        studyRepository.put(study)

        val participant = factory.createParticipant
        val reply = makeAuthRequest(POST, uri(study), participantToAddJson(participant)).value
        reply must beBadRequestWithMessage("missing required annotation type")
      }

      it("fail when participant has annotations and the study does not have any annotation types") {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val annotation = factory.createAnnotation
        val participant = factory.createParticipant.copy(annotations = Set(annotation))

        val reply = makeAuthRequest(POST, uri(study), participantToAddJson(participant)).value
        reply must beBadRequestWithMessage("no annotation types")
      }

      it("for an annotation with an invalid annotation type id") {
        val annotType = factory.createAnnotationType
        val annotation = factory.createAnnotation
          .copy(annotationTypeId = AnnotationTypeId(nameGenerator.next[AnnotationType]))

        val study = factory.createEnabledStudy.copy(annotationTypes = Set(annotType))
        studyRepository.put(study)

        val participant = factory.createParticipant.copy(annotations = Set(annotation))
        val reply = makeAuthRequest(POST, uri(study), participantToAddJson(participant)).value
        reply must beBadRequestWithMessage("annotation.*do not belong to annotation types")
      }

      it("fail for more than one annotation with the same annotation type") {
        val pat = createAnnotationType
        val study = factory.createEnabledStudy.copy(annotationTypes = Set(pat))
        studyRepository.put(study)

        val annotation = factory.createAnnotation
        val annotation2 = annotation.copy(stringValue = Some(nameGenerator.next[Annotation]))
        val participant = factory.createParticipant

        val reqJson = participantToAddJson(participant, List(annotation, annotation2))
        val reply = makeAuthRequest(POST, uri(study), reqJson).value
        reply must beBadRequestWithMessage("duplicate annotations")
      }

      describe("cannot add a participant") {
        describe("when study is disabled") {
          addOnNonEnabledStudySharedBehaviour(factory.createDisabledStudy)
        }

        describe("when study is retired") {
          addOnNonEnabledStudySharedBehaviour(factory.createRetiredStudy)
        }
      }
    }

    describe("POST /api/participants/uniqueId/:id") {

      it("update a participant's unique id") {
        val newUniqueId = nameGenerator.next[Participant]
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val participant = factory.createParticipant
        participantRepository.put(participant)

        val reply = makeAuthRequest(POST,
                                    updateUri(participant, "uniqueId"),
                                    Json.obj("uniqueId"        -> newUniqueId,
                                             "expectedVersion" -> participant.version)).value
        reply must beOkResponseWithJsonReply

        val updatedParticipant = participant.copy(version      = participant.version + 1,
                                                  slug         = Slug(newUniqueId),
                                                  uniqueId     = newUniqueId,
                                                  timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedParticipant(updatedParticipant)
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

        val reply = makeAuthRequest(POST,
                                    updateUri(participantToUpdate, "uniqueId"),
                                    Json.obj("uniqueId"        -> duplicateUniqueId,
                                             "expectedVersion" -> participantToUpdate.version)).value
        reply must beForbiddenRequestWithMessage("participant with unique ID already exists")
      }

      describe("update uniqueId on a non disable sudy") {
        describe("when study is disabled") {
          updateOnNonEnabledStudySharedBehaviour(
            factory.createDisabledStudy,
            "uniqueId",
            Json.obj("uniqueId" -> nameGenerator.next[Participant]))
        }

        describe("when study is retired") {
          updateOnNonEnabledStudySharedBehaviour(
            factory.createRetiredStudy,
            "uniqueId",
            Json.obj("uniqueId" -> nameGenerator.next[Participant]))
        }
      }

    }

    describe("POST /api/participants/annot/:id") {
      annotationTypeUpdateSharedBehaviour
    }

    describe("DELETE /api/participants/annot/:id/:annotTypeId/:ver") {
      annotationTypeRemoveSharedBehaviour
    }

  }

  private def addOnNonEnabledStudySharedBehaviour(study: Study) = {
    it("add must fail for a study that is not enabled") {
      study must not be an [EnabledStudy]
      studyRepository.put(study)
      val participant = factory.createParticipant.copy(studyId = study.id)
      val cmdJson = participantToAddJson(participant);
      val reply = makeAuthRequest(POST, uri(study), cmdJson).value
      reply must beBadRequestWithMessage("InvalidStatus: study not enabled")
    }
  }

  private def updateOnNonEnabledStudySharedBehaviour(study:     Study,
                                                     path:      String,
                                                     jsonField: JsObject) = {
    it("update must fail for a study that is not enabled") {
      study must not be an [EnabledStudy]
      studyRepository.put(study)
      val participant = factory.createParticipant.copy(studyId = study.id)
      participantRepository.put(participant)
      val participant2 = factory.createParticipant.copy(id      = participant.id,
                                                        studyId = participant.studyId)
      val reqJson = jsonField ++ Json.obj("expectedVersion" -> participant.version)
      val reply = makeAuthRequest(POST, updateUri(participant2, path), reqJson).value
      reply must beBadRequestWithMessage("InvalidStatus: study not enabled")
    }
  }

  private def matchUpdatedParticipant(participant: Participant) =
    new Matcher[Future[Result]] {
      def apply (left: Future[Result]) = {
        val replyParticipant = (contentAsJson(left) \ "data").validate[Participant]
        val jsSuccessMatcher = jsSuccess(replyParticipant)

        if (!jsSuccessMatcher.matches) {
          jsSuccessMatcher
        } else {
          val entitiesMatcher = matchParticipant(participant)(replyParticipant.get)

          if (!entitiesMatcher.matches) {
            MatchResult(false,
                        s"reply does not match expected: ${entitiesMatcher.failureMessage}",
                        s"reply matches expected: ${entitiesMatcher.failureMessage}")
          } else {
            matchRepositoryParticipant(participant)
          }
        }
      }
    }

  private def matchRepositoryParticipant =
    new Matcher[Participant] {
      def apply (left: Participant) = {
        participantRepository.getByKey(left.id).fold(
          err => {
            MatchResult(false, s"not found in repository: ${err.head}", "")

          },
          repoCet => {
            val repoMatcher = matchParticipant(left)(repoCet)
            MatchResult(repoMatcher.matches,
                        s"repository participant does not match expected: ${repoMatcher.failureMessage}",
                        s"repository participant matches expected: ${repoMatcher.failureMessage}")
          }
        )
      }
    }



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

}
