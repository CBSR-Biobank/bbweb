package org.biobank.controllers.participants

import java.time.format.DateTimeFormatter
import java.time.OffsetDateTime
import org.biobank.controllers._
import org.biobank.domain._
import org.biobank.domain.annotations._
import org.biobank.domain.participants._
import org.biobank.domain.studies._
import org.biobank.fixtures.Url
import org.biobank.matchers.PagedResultsMatchers
import org.scalatest.matchers.{MatchResult, Matcher}
import play.api.mvc._
import play.api.libs.json._
import play.api.test.Helpers._
import scala.concurrent.Future
import scalaz.Validation.FlatMap._

/**
 * Tests the REST API for [[CollectionEvents]].
 */
class CollectionEventsControllerSpec
    extends StudyAnnotationsControllerSharedSpec[CollectionEvent]
    with PagedResultsMatchers
    with PagedResultsSharedSpec {

  import org.biobank.TestUtils._
  import org.biobank.AnnotationTestUtils._
  import org.biobank.matchers.EntityMatchers._
  import org.biobank.matchers.JsonMatchers._

  class Fixture {
    val study = factory.createEnabledStudy
    val ceventType = factory.createCollectionEventType.copy(studyId = study.id,
                                                            annotationTypes = Set.empty)
    val participant = factory.createParticipant.copy(studyId = study.id)

    Set(study, ceventType, participant)foreach(addToRepository)
  }

  private def uri(): String = "/api/participants/cevents"

  private def uri(collectionEvent: CollectionEvent): String =
    uri + s"/${collectionEvent.id}"

  private def uri(participantId: ParticipantId): String =
    uri + s"/${participantId.id}"

  private def uri(participantId: ParticipantId, cevent: CollectionEvent, version: Long): String =
    uri(participantId) + s"/${cevent.id.id}/$version"

  private def uri(participant: Participant): String =
    uri(participant.id)

  private def uri(participant: Participant, cevent: CollectionEvent, version: Long): String =
    uri(participant.id, cevent, version)

  private def uriWithVisitNumber(participant: Participant, cevent: CollectionEvent): String =
    uri + s"/visitNumber/${participant.id.id}/${cevent.visitNumber}"

  private def listUri(participantId: ParticipantId): String =
    uri + s"/list/${participantId.id}"

  private def updateUri(cevent: CollectionEvent, path: String): String =
    uri + s"/$path/${cevent.id.id}"

  describe("Collection Event REST API") {

    describe("GET /api/participants/cevents/:ceventId") {

      it("get a single collection event for a participant") {
        new Fixture
        val cevents = (0 until 2).map { x =>
            val cevent = factory.createCollectionEvent
            collectionEventRepository.put(cevent)
            cevent
          }

        val ceventToGet = cevents(0)
        val reply = makeAuthRequest(GET, uri(ceventToGet)).value
        reply must beOkResponseWithJsonReply

        val replyCevent = (contentAsJson(reply) \ "data").validate[CollectionEvent]
        replyCevent must be (jsSuccess)
        replyCevent.get must matchCollectionEvent(ceventToGet)
      }

      it("fail when querying for a single collection event and ID is invalid") {
        new Fixture
        val cevent = factory.createCollectionEvent
        val reply = makeAuthRequest(GET, uri(cevent)).value
        reply must beNotFoundWithMessage("IdNotFound: collection event id")
      }

    }

    describe("GET /api/participants/cevents/list/{participantId}") {

      it("list none") {
        val f = new Fixture
        new Url(listUri(f.participant.id)) must beEmptyResults
      }

      describe("list a single collection event") {
        listSingleCollectionEvent() { () =>
          val f = new Fixture
          val cevent = factory.createCollectionEvent
          collectionEventRepository.put(cevent)
          (new Url(listUri(f.participant.id)), cevent)
        }
      }

      describe("get all collection events for a participant") {
        listMultipleCollectionEvents() { () =>
          val f = new Fixture
          val cevents = (0 until 2).map { _ =>
              val cevent = factory.createCollectionEvent
              collectionEventRepository.put(cevent)
              cevent
            }.toList

          (new Url(listUri(f.participant.id)), cevents)
        }
      }

      describe("list collection events sorted by visit number") {
        def commonSetup = {
          val f = new Fixture
          val cevents = (1 to 4).map { visitNumber =>
              val cevent = factory.createCollectionEvent.copy(visitNumber = visitNumber)
              collectionEventRepository.put(cevent)
              cevent
            }.toList
          (f.participant, cevents)
        }

        describe("in ascending order") {
          listMultipleCollectionEvents() { () =>
            val (participant, cevents) = commonSetup
            (new Url(listUri(participant.id) + "?sort=visitNumber"),
             cevents.sortWith(_.visitNumber < _.visitNumber))
          }
        }

        describe("in descending order") {
          listMultipleCollectionEvents() { () =>
            val (participant, cevents) = commonSetup
            (new Url(listUri(participant.id) + "?sort=-visitNumber"),
             cevents.sortWith(_.visitNumber > _.visitNumber))
          }
        }
      }

      describe("list collection events sorted by time completed") {
        def commonSetup = {
          val f = new Fixture
          val cevents = (1 to 4).map { visitNumber =>
              val cevent = factory.createCollectionEvent.copy(visitNumber = visitNumber)
              collectionEventRepository.put(cevent)
              cevent
            }.toList
          (f.participant, cevents)
        }

        describe("in ascending order") {
          listMultipleCollectionEvents() { () =>
            val (participant, cevents) = commonSetup
            (new Url(listUri(participant.id) + "?sort=timeCompleted"), cevents.sortBy(_.timeCompleted))
          }
        }

        describe("in descending order") {
          listMultipleCollectionEvents() { () =>
            val (participant, cevents) = commonSetup
            (new Url(listUri(participant.id) + "?sort=-timeCompleted"),
             cevents.sortBy(_.timeCompleted).reverse)
          }
        }

      }

      describe("list the first collection event in a paged query") {
        listSingleCollectionEvent(maybeNext = Some(2)) { () =>
          val f = new Fixture
          val cevents = (1 to 4).map { hour =>
              val cevent = factory.createCollectionEvent.copy(
                  timeCompleted = OffsetDateTime.now.withHour(hour))
              collectionEventRepository.put(cevent)
              cevent
            }.toList
          (new Url(listUri(f.participant.id) + "?sort=timeCompleted&limit=1"), cevents(0))
        }
      }

      describe("list the last collection event in a paged query") {
        listSingleCollectionEvent(offset = 3, maybePrev = Some(3)) { () =>
          val f = new Fixture
          val cevents = (1 to 4).map { hour =>
              val cevent = factory.createCollectionEvent.copy(
                  timeCompleted = OffsetDateTime.now.withHour(hour))
              collectionEventRepository.put(cevent)
              cevent
            }.toList
          (new Url(listUri(f.participant.id) + "?sort=timeCompleted&page=4&limit=1"), cevents(3))
        }
      }

      describe("fail when using an invalid query parameters") {
        pagedQueryShouldFailSharedBehaviour { () =>
          val f = new Fixture
          new Url(listUri(f.participant.id))
        }
      }

      it("list request fails for invalid participant id") {
        val participant = factory.createParticipant
        val reply = makeAuthRequest(GET, listUri(participant.id)).value
        reply must beNotFoundWithMessage("IdNotFound.*participant id")
      }

    }

    describe("GET /api/participants/cevents/visitNumber/:participantId/:vn") {

      it("get a collection event by visit number") {
        val f = new Fixture
        val cevents = (1 to 2).map { visitNumber =>
            val cevent = factory.createCollectionEvent.copy(visitNumber = visitNumber)
            collectionEventRepository.put(cevent)
            cevent
          }

        val ceventToGet = cevents(0)
        val reply = makeAuthRequest(GET, uriWithVisitNumber(f.participant, ceventToGet)).value
        reply must beOkResponseWithJsonReply

        val replyCevent = (contentAsJson(reply) \ "data").validate[CollectionEvent]
        replyCevent must be (jsSuccess)
        replyCevent.get must matchCollectionEvent(ceventToGet)
      }

      it("fail for invalid participant id when querying for a collection event with a visit number") {
        val participant = factory.createParticipant
        val cevent = factory.createCollectionEvent
        studyRepository.put(factory.defaultEnabledStudy)
        val reply = makeAuthRequest(GET, uriWithVisitNumber(participant, cevent)).value
        reply must beNotFoundWithMessage("NotFound.*collection event")
      }

      it("fail when querying for a collection event with a visit number") {
        val f = new Fixture
        val cevent = factory.createCollectionEvent
        val reply = makeAuthRequest(GET, uriWithVisitNumber(f.participant, cevent)).value
        reply must beNotFoundWithMessage("collection event does not exist")
      }
    }

    describe("POST /api/participants/cevents/:participantId") {

      it("add a collection event with no annotations") {
        val f = new Fixture
        val cevent = factory.createCollectionEvent
        cevent.annotations must have size 0
        val reply = makeAuthRequest(POST, uri(f.participant), collectionEventToAddJson(cevent)).value
        reply must beOkResponseWithJsonReply

        val replyId = (contentAsJson(reply) \ "data" \ "id").validate[CollectionEventId]
        replyId must be (jsSuccess)

        val updatedCevent = cevent.copy(id = replyId.get,
                                        slug = Slug(s"visit-number-${cevent.visitNumber}"),
                                        timeAdded = OffsetDateTime.now)
        reply must matchUpdatedCollectionEvent(updatedCevent)
      }

      it("fail when adding and visit number that is already used") {
        val f = new Fixture
        val cevent = factory.createCollectionEvent
        collectionEventRepository.put(cevent)
        val reply = makeAuthRequest(POST, uri(f.participant), collectionEventToAddJson(cevent)).value
        reply must beForbiddenRequestWithMessage(
          "a collection event with this visit number already exists")
      }

      it("add a collection event with annotations") {
        val f = new Fixture
        val annotTypes = createAnnotationsAndTypes
        val annotations = annotTypes.values.toSet
        collectionEventTypeRepository.put(f.ceventType.copy(annotationTypes = annotTypes.keys.toSet))

        val cevent = factory.createCollectionEvent
        val reply = makeAuthRequest(POST,
                                    uri(f.participant),
                                    collectionEventToAddJson(cevent, annotations.toList)).value
        reply must beOkResponseWithJsonReply

        val replyId = (contentAsJson(reply) \ "data" \ "id").validate[CollectionEventId]
        replyId must be (jsSuccess)
        val updatedCevent = cevent.copy(id = replyId.get,
                                        slug = Slug(s"visit-number-${cevent.visitNumber}"),
                                        annotations = annotations,
                                        timeAdded = OffsetDateTime.now)
        reply must matchUpdatedCollectionEvent(updatedCevent)
      }

      it("fail when tyring to add a collection event with an empty, non required, number annotation") {
        val f = new Fixture
        val annotType = factory
          .createAnnotationType(AnnotationValueType.Number, None, Seq.empty)
          .copy(required = false)
        val annotation = factory.createAnnotation.copy(numberValue = Some(""))
        collectionEventTypeRepository.put(f.ceventType.copy(annotationTypes = Set(annotType)))
        val cevent = factory.createCollectionEvent
        val reply = makeAuthRequest(POST,
                                    uri(f.participant),
                                    collectionEventToAddJson(cevent, List(annotation))).value
        reply must beBadRequestWithMessage("InvalidNumberString")
      }

      it("fail when adding and participant and collection event type not in same study") {
        val f = new Fixture
        val otherStudy = factory.createDisabledStudy
        val otherCeventType = factory.createCollectionEventType.copy(studyId = otherStudy.id)

        studyRepository.put(otherStudy)
        collectionEventTypeRepository.put(otherCeventType)

        val cevent = factory.createCollectionEvent
        val reply = makeAuthRequest(POST, uri(f.participant), collectionEventToAddJson(cevent)).value
        reply must beBadRequestWithMessage(
            "participant and collection event type not in the same study")
      }

      it("fail when adding collection event with duplicate visit number") {
        val f = new Fixture
        val cevent1 = factory.createCollectionEvent
        collectionEventRepository.put(cevent1)

        val cevent2 = factory.createCollectionEvent.copy(visitNumber = cevent1.visitNumber)
        val reply = makeAuthRequest(POST, uri(f.participant), collectionEventToAddJson(cevent2)).value
        reply must beForbiddenRequestWithMessage(
          "a collection event with this visit number already exists")
      }

      it("fail when missing a required annotation type") {
        val f = new Fixture
        val annotType = factory.createAnnotationType.copy(required = true)

        collectionEventTypeRepository.put(f.ceventType.copy(annotationTypes = Set(annotType)))
        val cevent = factory.createCollectionEvent.copy(annotations = Set.empty)
        val reply = makeAuthRequest(POST, uri(f.participant), collectionEventToAddJson(cevent)).value
        reply must beBadRequestWithMessage("missing required annotation type")
      }

      it("fail when using annotations and collection event type has no annotations") {
        val f = new Fixture
        val annotation = factory.createAnnotation
          .copy(annotationTypeId = AnnotationTypeId(nameGenerator.next[Annotation]))

        val cevent = factory.createCollectionEvent
        val reply = makeAuthRequest(POST,
                                    uri(f.participant),
                                    collectionEventToAddJson(cevent, List(annotation))).value
        reply must beBadRequestWithMessage("no annotation types")
      }

      it("fail for an annotation with an invalid annotation type id") {
        val f = new Fixture
        val annotType = factory.createAnnotationType

        // update the collection event type with annotation type data
        collectionEventTypeRepository.put(f.ceventType.copy(annotationTypes = Set(annotType)))

        val annotation = factory.createAnnotation.copy(
            annotationTypeId = AnnotationTypeId(nameGenerator.next[Annotation]))

        val cevent = factory.createCollectionEvent
        val reply = makeAuthRequest(POST,
                                    uri(f.participant),
                                    collectionEventToAddJson(cevent, List(annotation))).value
        reply must beBadRequestWithMessage("annotation.*do not belong to annotation types")
      }

      it("fail for more than one annotation with the same annotation type ID") {
        val f = new Fixture
        val annotType = factory.createAnnotationType

        // update the collection event type with annotation type data
        collectionEventTypeRepository.put(f.ceventType.copy(annotationTypes = Set(annotType)))

        val annotation = factory.createAnnotationWithValues(annotType)
        annotation.stringValue mustBe defined

        val cevent = factory.createCollectionEvent
        val annotations = List(annotation,
                               annotation.copy(stringValue = Some(nameGenerator.next[Annotation])))
        val reply = makeAuthRequest(POST,
                                    uri(f.participant),
                                    collectionEventToAddJson(cevent, annotations)).value
        reply must beBadRequestWithMessage("duplicate annotations")
      }

      describe("fail when adding collection event when the study is not enabled") {
        describe("when study is disabled") {
          addOnNonEnabledStudySharedBehaviour { () =>
            val f = new Fixture
            val study = f.study.disable.toOption.value
            val cevent = factory.createCollectionEvent
            (study, cevent)
          }
        }

        describe("when study is retired") {
          addOnNonEnabledStudySharedBehaviour { () =>
            val f = new Fixture
            val study = f.study.disable.toOption.value.retire.toOption.value
            val cevent = factory.createCollectionEvent
            (study, cevent)
          }
        }
      }

    }

    describe("POST /api/participants/cevents/visitNumber/:ceventId") {

      it("update the visit number on a collection event") {
        new Fixture
        val cevent = factory.createCollectionEvent
        val newVisitNumber = cevent.visitNumber + 1

        collectionEventRepository.put(cevent)
        cevent.annotations must have size 0

        val reply = makeAuthRequest(POST,
                                    updateUri(cevent, "visitNumber"),
                                    Json.obj("expectedVersion" -> Some(cevent.version),
                                             "visitNumber"     -> newVisitNumber)).value
        reply must beOkResponseWithJsonReply
        val updatedCevent = cevent.copy(version      = cevent.version + 1,
                                        visitNumber  = newVisitNumber,
                                        timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedCollectionEvent(updatedCevent)
      }

      it("fail when updating visit number to one already used") {
        new Fixture
        val cevents = (1 to 2).map { visitNumber =>
            val cevent = factory.createCollectionEvent.copy(visitNumber = visitNumber)
            collectionEventRepository.put(cevent)
            cevent
          }
        val ceventToUpdate = cevents(0)
        val duplicateVisitNumber = cevents(1).visitNumber

        val reply = makeAuthRequest(POST,
                                    updateUri(ceventToUpdate, "visitNumber"),
                                    Json.obj("expectedVersion" -> Some(ceventToUpdate.version),
                                             "visitNumber"     -> duplicateVisitNumber)).value
        reply must beForbiddenRequestWithMessage(
          "a collection event with this visit number already exists")
      }

      describe("not update a collection event's visit number on a non enabled study") {
        describe("when study is disabled") {
          updateOnNonEnabledStudySharedBehaviour { () =>
            val f = new Fixture
            val study = f.study.disable.toOption.value
            val cevent = factory.createCollectionEvent.copy(participantId = f.participant.id,
                                                            collectionEventTypeId = f.ceventType.id)
            collectionEventRepository.put(cevent)
            (study, cevent, "visitNumber", Json.obj("visitNumber" -> 2))
          }
        }

        describe("when study is retired") {
          updateOnNonEnabledStudySharedBehaviour { () =>
            val f = new Fixture
            val study = f.study.disable.toOption.value.retire.toOption.value
            val cevent = factory.createCollectionEvent.copy(participantId = f.participant.id,
                                                            collectionEventTypeId = f.ceventType.id)
            collectionEventRepository.put(cevent)
            (study, cevent, "visitNumber", Json.obj("visitNumber" -> 2))
          }
        }
      }

      describe("fail when updating visit number and collection event ID is invalid") {
        updateOnInvalidCeventSharedBehaviour { () =>
          val f = new Fixture
          val cevent = factory.createCollectionEvent.copy(participantId = f.participant.id,
                                                          collectionEventTypeId = f.ceventType.id)
          (cevent, "visitNumber", Json.obj("visitNumber" -> 1))
        }
      }

      describe("fail when updating visit number with an invalid version") {
        updateWithInvalidVersionSharedBehaviour { () =>
          val f = new Fixture
          (f.participant, f.ceventType, "visitNumber", Json.obj("visitNumber" -> 1))
        }
      }

    }

    describe("POST /api/participants/cevents/timeCompleted/:ceventId") {

      it("update the time completed on a collection event") {
        new Fixture
        val cevent = factory.createCollectionEvent
        val newTimeCompleted = cevent.timeCompleted.minusMonths(2)

        collectionEventRepository.put(cevent)
        cevent.annotations must have size 0
        val reply = makeAuthRequest(POST,
                                    updateUri(cevent, "timeCompleted"),
                                    Json.obj("expectedVersion" -> Some(cevent.version),
                                             "timeCompleted"     -> newTimeCompleted)).value
        reply must beOkResponseWithJsonReply
        val updatedCevent = cevent.copy(version       = cevent.version + 1,
                                        timeCompleted = newTimeCompleted,
                                        timeModified  = Some(OffsetDateTime.now))
        reply must matchUpdatedCollectionEvent(updatedCevent)
      }

      describe("not update a collection event's time completed on a non enabled study") {
        describe("when study is disabled") {
          updateOnNonEnabledStudySharedBehaviour { () =>
            val f = new Fixture
            val study = f.study.disable.toOption.value
            val cevent = factory.createCollectionEvent.copy(participantId = f.participant.id,
                                                            collectionEventTypeId = f.ceventType.id)
            collectionEventRepository.put(cevent)
            (study, cevent, "timeCompleted", Json.obj("timeCompleted" -> OffsetDateTime.now.minusMonths(2)))
          }
        }

        describe("when study is retired") {
          updateOnNonEnabledStudySharedBehaviour { () =>
            val f = new Fixture
            val study = f.study.disable.toOption.value.retire.toOption.value
            val cevent = factory.createCollectionEvent.copy(participantId = f.participant.id,
                                                            collectionEventTypeId = f.ceventType.id)
            collectionEventRepository.put(cevent)
            (study, cevent, "timeCompleted", Json.obj("timeCompleted" -> OffsetDateTime.now.minusMonths(2)))
          }
        }
      }

      describe("fail when updating time completed and collection event ID is invalid") {
        updateOnInvalidCeventSharedBehaviour { () =>
          val f = new Fixture
          val cevent = factory.createCollectionEvent.copy(participantId = f.participant.id,
                                                          collectionEventTypeId = f.ceventType.id)
          val timeCompleted = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
          (cevent, "timeCompleted", Json.obj("timeCompleted" -> timeCompleted))
        }
      }

      describe("fail when updating time completed with an invalid version") {
        updateWithInvalidVersionSharedBehaviour { () =>
          val f = new Fixture
          val timeCompleted = OffsetDateTime.now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
          (f.participant, f.ceventType, "timeCompleted", Json.obj("timeCompleted" -> timeCompleted))
        }
      }

    }

    describe("POST /api/participants/cevents/annot/:ceventId") {

      annotationTypeUpdateSharedBehaviour

      describe("fail when adding annotation and collection event ID is invalid") {
        updateOnInvalidCeventSharedBehaviour { () =>
          val f = new Fixture
          val annotationType = factory.createAnnotationType
          val annotation = factory.createAnnotation
          collectionEventTypeRepository.put(f.ceventType.copy(annotationTypes = Set(annotationType)))
          val cevent = factory.createCollectionEvent.copy(participantId         = f.participant.id,
                                                          collectionEventTypeId = f.ceventType.id)
          (cevent, "annot", annotationToJson(annotation))
        }
      }

    }

    describe("DELETE /api/participants/cevents/annot/:ceventId/:annotTypeId/:ver") {

      annotationTypeRemoveSharedBehaviour

    }

    describe("DELETE /api/participants/cevents/:participantId/:ceventId/:ver") {

      it("remove a collection event") {
        val f = new Fixture
        val cevent = factory.createCollectionEvent
        collectionEventRepository.put(cevent)
        val reply = makeAuthRequest(DELETE, uri(f.participant, cevent, cevent.version)).value
        reply must beOkResponseWithJsonReply

        val result = (contentAsJson(reply) \ "data").validate[Boolean]
        result must be (jsSuccess)
        result.get must be (true)
        collectionEventRepository.getByKey(cevent.id) mustFail("IdNotFound: collection event.*")
      }

      describe("not remove a collection event from an disabled study") {
        removeOnNonEnabledStudySharedBehaviour { () =>
          val f = new Fixture
          val disabledStudy = f.study.disable.toOption.value
          (disabledStudy, factory.createCollectionEvent)
        }
      }

      describe("not remove a collection event from an retired study") {
        removeOnNonEnabledStudySharedBehaviour { () =>
          val f = new Fixture
          val retiredStudy = f.study.disable.toOption.value.retire.toOption.value
          (retiredStudy, factory.createCollectionEvent)
        }
      }

    }

  }

  private def addOnNonEnabledStudySharedBehaviour(setupFunc: () => (Study, CollectionEvent)) = {

    it("must be bad request") {
      val (study, cevent) = setupFunc()
      study must not be an [EnabledStudy]
      studyRepository.put(study)
      val reqJson = collectionEventToAddJson(cevent)
      val reply = makeAuthRequest(POST, uri(cevent.participantId), reqJson).value
      reply must beBadRequestWithMessage("InvalidStatus: study not enabled")
    }
  }

  private def updateOnNonEnabledStudySharedBehaviour(setupFunc: () => (Study,
                                                                      CollectionEvent,
                                                                      String,
                                                                      JsObject)) = {
    it("must be bad request") {
      val (study, cevent, url, jsonField) = setupFunc()
      study must not be an [EnabledStudy]
      studyRepository.put(study)
      collectionEventRepository.put(cevent)
      val reqJson = jsonField ++ Json.obj("expectedVersion" -> cevent.version)
      val reply = makeAuthRequest(POST, updateUri(cevent, url), reqJson).value
      reply must beBadRequestWithMessage("InvalidStatus: study not enabled")
    }
  }

  private def updateOnInvalidCeventSharedBehaviour(setupFunc: () => (CollectionEvent,
                                                                    String,
                                                                    JsObject)) = {
    it("must be not found") {
      val (cevent, url, jsonField) = setupFunc()
      val reqJson = jsonField ++ Json.obj("expectedVersion" -> cevent.version)
      val reply = makeAuthRequest(POST, updateUri(cevent, url), reqJson).value
      reply must beNotFoundWithMessage("IdNotFound.*collection event")
    }
  }

  private def removeOnNonEnabledStudySharedBehaviour(setupFunc: () => (Study, CollectionEvent)) = {
    it("must be bad request") {
      val (study, cevent) = setupFunc()
      study must not be an [EnabledStudy]

      studyRepository.put(study)
      collectionEventRepository.put(cevent)
      val reply = makeAuthRequest(DELETE, uri(cevent.participantId, cevent, cevent.version)).value
      reply must beBadRequestWithMessage("InvalidStatus: study not enabled")
    }
  }

  private def updateWithInvalidVersionSharedBehaviour(setupFunc: () => (Participant,
                                                                       CollectionEventType,
                                                                       String,
                                                                       JsObject)) = {
    it("must be bad request") {
      val (participant, ceventType, url, jsonField) = setupFunc()
      val cevent = factory.createCollectionEvent.copy(participantId = participant.id,
                                                      collectionEventTypeId = ceventType.id)
      collectionEventRepository.put(cevent)

      val reqJson = jsonField ++ Json.obj("expectedVersion" -> (cevent.version + 1))

      val reply = makeAuthRequest(POST, updateUri(cevent, url), reqJson).value
      reply must beBadRequestWithMessage(".*expected version doesn't match current version.*")
    }
  }

  private def listSingleCollectionEvent(offset:    Long = 0,
                                        maybeNext: Option[Int] = None,
                                        maybePrev: Option[Int] = None)
                                       (setupFunc: () => (Url, CollectionEvent)) = {

    it("list single collectionEvent") {
      val (url, expectedCollectionEvent) = setupFunc()
      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beSingleItemResults(offset, maybeNext, maybePrev)

      val replyEvents = (json \ "data" \ "items").validate[List[CollectionEvent]]
      replyEvents must be (jsSuccess)
      replyEvents.get.foreach { _ must matchCollectionEvent(expectedCollectionEvent) }
    }
  }

  private def listMultipleCollectionEvents(offset:    Long = 0,
                                           maybeNext: Option[Int] = None,
                                           maybePrev: Option[Int] = None)
                                          (setupFunc: () => (Url, List[CollectionEvent])) = {

    it("list multiple collectionEvents") {
      val (url, expectedCollectionEvents) = setupFunc()

      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beMultipleItemResults(offset = offset,
                                      total = expectedCollectionEvents.size.toLong,
                                      maybeNext = maybeNext,
                                      maybePrev = maybePrev)

      val replyEvents = (json \ "data" \ "items").validate[List[CollectionEvent]]
      replyEvents must be (jsSuccess)

      (replyEvents.get zip expectedCollectionEvents).foreach { case (replyEvent, collectionEvent) =>
        replyEvent must matchCollectionEvent(collectionEvent)
      }
    }

  }

  def matchUpdatedCollectionEvent(collectionEvent: CollectionEvent) =
    new Matcher[Future[Result]] {
      def apply (left: Future[Result]) = {
        val replyCevent = (contentAsJson(left) \ "data").validate[CollectionEvent]
        val jsSuccessMatcher = jsSuccess(replyCevent)

        if (!jsSuccessMatcher.matches) {
          jsSuccessMatcher
        } else {
          val replyMatcher = matchCollectionEvent(collectionEvent)(replyCevent.get)

          if (!replyMatcher.matches) {
            MatchResult(false,
                        s"reply does not match expected: ${replyMatcher.failureMessage}",
                        s"reply matches expected: ${replyMatcher.failureMessage}")
          } else {
            matchRepositoryCollectionEvent(collectionEvent)
          }
        }
      }
    }

  def matchRepositoryCollectionEvent =
    new Matcher[CollectionEvent] {
      def apply (left: CollectionEvent) = {
        collectionEventRepository.getByKey(left.id).fold(
          err => {
            MatchResult(false, s"not found in repository: ${err.head}", "")

          },
          repoCet => {
            val repoMatcher = matchCollectionEvent(left)(repoCet)
            MatchResult(repoMatcher.matches,
                        s"repository collectionEvent does not match expected: ${repoMatcher.failureMessage}",
                        s"repository collectionEvent matches expected: ${repoMatcher.failureMessage}")
          }
        )
      }
    }

  /**
   * Converts a collectionEvent into an Add command.
   */
  private def collectionEventToAddJson(collectionEvent: CollectionEvent,
                                       annotations: List[Annotation] = List.empty) = {
    Json.obj(
      "collectionEventTypeId" -> collectionEvent.collectionEventTypeId,
      "timeCompleted"         -> collectionEvent.timeCompleted,
      "visitNumber"           -> collectionEvent.visitNumber,
      "annotations"           -> annotations.map(annotationToJson(_))
    )
  }

  protected def createEntity(annotationTypes: Set[AnnotationType],
                             annotations:     Set[Annotation]): CollectionEvent = {
    val f = new Fixture
    collectionEventTypeRepository.put(f.ceventType.copy(annotationTypes = annotationTypes))
    val cevent = factory.createCollectionEvent.copy(annotations = annotations)
    collectionEventRepository.put(cevent)
    cevent
  }

  protected def entityFromRepository(id: String): DomainValidation[CollectionEvent] = {
    collectionEventRepository.getByKey(CollectionEventId(id))
  }

  protected def entityName(): String = "collection event"

  protected def updateUri(cevent: CollectionEvent): String = updateUri(cevent, "annot")

  protected def getStudy(cevent: CollectionEvent): DomainValidation[EnabledStudy] = {
    for {
      participant <- participantRepository.getByKey(cevent.participantId)
      study       <- studyRepository.getEnabled(participant.studyId)
    } yield study
  }

}
