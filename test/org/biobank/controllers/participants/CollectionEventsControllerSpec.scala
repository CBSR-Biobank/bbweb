package org.biobank.controllers.participants

import org.biobank.fixture._
import org.biobank.domain.{ AnnotationTypeId, AnnotationOption, AnnotationValueType }
import org.biobank.domain.study._
import org.biobank.domain.participants._
import org.biobank.fixture.ControllerFixture
import org.biobank.infrastructure.CollectionEventTypeAnnotationTypeData

import play.api.test.Helpers._
import play.api.libs.json._
import org.scalatest.Tag
import org.joda.time.DateTime
import com.github.nscala_time.time.Imports._
import org.scalatest.matchers.Matcher
import org.scalatest.matchers.MatchResult

/**
 * Tests the REST API for [[CollectionEvents]].
 */
class CollectionEventsControllerSpec extends ControllerFixture {
  import org.biobank.TestUtils._
  import org.biobank.AnnotationTestUtils._
  import org.biobank.domain.JsonHelper._

  def uri(participantId: ParticipantId): String =
    s"/participants/${participantId.id}/cevents"

  def uri(participantId: ParticipantId, cevent: CollectionEvent): String =
    uri(participantId) + s"/${cevent.id.id}"

  def uri(participantId: ParticipantId, cevent: CollectionEvent, version: Long): String =
    uri(participantId) + s"/${cevent.id.id}/$version"

  def uri(participant: Participant): String =
    s"/participants/${participant.id.id}/cevents"

  def uri(participant: Participant, cevent: CollectionEvent): String =
    uri(participant) + s"/${cevent.id.id}"

  def uri(participant: Participant, cevent: CollectionEvent, version: Long): String =
    uri(participant) + s"/${cevent.id.id}/$version"

  def uriWithQuery(participant: Participant, cevent: CollectionEvent): String =
    uri(participant) + s"?ceventId=${cevent.id.id}"

  def uriWithVisitNumber(participant: Participant, cevent: CollectionEvent): String =
    uri(participant) + s"/visitNumber/${cevent.visitNumber}"

  /** Converts a collectionEvent into an Add command.
   */
  def collectionEventToAddCmd(collectionEvent: CollectionEvent) = {
    Json.obj(
      "participantId"         -> collectionEvent.participantId.id,
      "collectionEventTypeId" -> collectionEvent.collectionEventTypeId,
      "timeCompleted"         -> collectionEvent.timeCompleted,
      "visitNumber"           -> collectionEvent.visitNumber,
      "annotations"           -> collectionEvent.annotations.map(annotation =>
        annotationToJson(annotation))
    )
  }

  /** Converts a collectionEvent into an Update command.
   */
  def collectionEventToUpdateCmd(collectionEvent: CollectionEvent) = {
    collectionEventToAddCmd(collectionEvent) ++ Json.obj(
      "id"              -> collectionEvent.id.id,
      "expectedVersion" -> Some(collectionEvent.version)
    )
  }

  def createCollectionEventAnnotationType(study: Study) = {
    CollectionEventAnnotationType(
      studyId       = study.id,
      id            = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotationType]),
      version       = 0L,
      timeAdded     = DateTime.now,
      timeModified  = None,
      name          = nameGenerator.next[CollectionEventAnnotationType],
      description   = None,
      valueType     = AnnotationValueType.Text,
      maxValueCount = None,
      options       = Seq.empty)
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
           (factory.createCollectionEventAnnotationType(vt, 1, options),
            factory.createCollectionEventAnnotation)
         case _ =>
           (factory.createCollectionEventAnnotationType(vt, 0, Seq.empty),
            factory.createCollectionEventAnnotation)
       }
     }.toList ++ List(
       (factory.createCollectionEventAnnotationType(AnnotationValueType.Select, 2, options),
        factory.createCollectionEventAnnotation))).toMap
  }

  def createEntities()(fn: (Study, Participant, CollectionEventType) => Unit): Unit = {
    var disabledStudy = factory.createDisabledStudy
    studyRepository.put(disabledStudy)

    val ceventType = factory.createCollectionEventType
    collectionEventTypeRepository.put(ceventType)

    disabledStudy.enable(1, 1) mustSucceed { enabledStudy =>
      studyRepository.put(enabledStudy)

      val participant = factory.createParticipant.copy(studyId = enabledStudy.id)
      participantRepository.put(participant)

      fn(enabledStudy, participant, ceventType)
    }
    ()
  }

  def addOnNonEnabledStudy(study: Study, cevent: CollectionEvent) = {
    study.status must not be (EnabledStudy.status)

    studyRepository.put(study)

    val cmdJson = collectionEventToAddCmd(cevent);
    val json = makeRequest(POST, uri(cevent.participantId), BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not enabled")
  }

  def updateOnNonEnabledStudy(study: Study, cevent: CollectionEvent) = {
    study.status must not be (EnabledStudy.status)

    studyRepository.put(study)
    collectionEventRepository.put(cevent)

    val cevent2 = factory.createCollectionEvent.copy(id            = cevent.id,
                                                     participantId = cevent.participantId)
    val cmdJson = collectionEventToUpdateCmd(cevent2);
    val json = makeRequest(PUT, uri(cevent2.participantId, cevent2), BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not enabled")
  }

  def removeOnNonEnabledStudy(study: Study, cevent: CollectionEvent) = {
    study.status must not be (EnabledStudy.status)

    studyRepository.put(study)
    collectionEventRepository.put(cevent)

    val json = makeRequest(DELETE, uri(cevent.participantId, cevent, cevent.version), BAD_REQUEST)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not enabled")
  }

  "Participant REST API" when {

    "GET /participants/{participantId}/cevents" must {

      "list none" in {
        createEntities() { (study, participant, ceventType) =>
          val json = makeRequest(GET, uri(participant))
          (json \ "status").as[String] must include ("success")
          val jsonList = (json \ "data").as[List[JsObject]]
          jsonList must have size 0
        }
      }

      "list a single collection event" in {
        createEntities() { (study, participant, ceventType) =>
          val cevent = factory.createCollectionEvent
          collectionEventRepository.put(cevent)

          val json = makeRequest(GET, uri(participant))
          (json \ "status").as[String] must include ("success")
          val jsonList = (json \ "data").as[List[JsObject]]
          jsonList must have size 1
          compareObj(jsonList(0), cevent)
        }
      }

      "get all collection events for a participant" in {
        createEntities() { (study, participant, ceventType) =>
          val participants = (0 until 2).map { x =>
            val participant = factory.createParticipant.copy(studyId = study.id)
            participantRepository.put(participant)
            participant
          }

          val cevents = participants.map { participant =>
            (participant.id -> (0 until 2).map { x =>
               val cevent = factory.createCollectionEvent.copy(participantId = participant.id)
               collectionEventRepository.put(cevent)
               cevent
             })
          }.toMap

          participants.foreach { participant =>
            val participantCevents = cevents(participant.id)

            val json = makeRequest(GET, uri(participant))
            (json \ "status").as[String] must include ("success")
            val jsonList = (json \ "data").as[List[JsObject]]
            jsonList must have size participantCevents.size
            jsonList.foreach { jsonItem =>
              val ceventMaybe = participantCevents.find { x => x.id.id == (jsonItem \ "id").as[String]}
              ceventMaybe mustBe defined
              ceventMaybe.map { compareObj(jsonItem, _) }
            }
          }
        }
      }

      "get a single collection event for a participant" in {
        createEntities() { (study, participant, ceventType) =>
          val cevents = (0 until 2).map { x =>
            val cevent = factory.createCollectionEvent
            collectionEventRepository.put(cevent)
            cevent
          }

          val ceventToGet = cevents(0)

          val json = makeRequest(GET, uriWithQuery(participant, ceventToGet))
          (json \ "status").as[String] must include ("success")
          val jsonObj = (json \ "data").as[JsObject]
          compareObj(jsonObj, ceventToGet)
        }
      }

      "fail for invalid participant id" in {
        val participant = factory.createParticipant

        val json = makeRequest(GET, uri(participant), NOT_FOUND)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid participant id")
      }

      "fail for invalid participant id when querying for a single collection event id" in {
        val participant = factory.createParticipant
        val cevent = factory.createCollectionEvent

        val json = makeRequest(GET, uriWithQuery(participant, cevent), NOT_FOUND)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid participant id")
      }

      "fail when querying for a single collection event id" in {
        createEntities() { (study, participant, ceventType) =>
          val cevent = factory.createCollectionEvent

          val json = makeRequest(GET, uriWithQuery(participant, cevent), NOT_FOUND)
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("collection event does not exist")
        }
      }

    }

    "GET /participants/{participantId}/cevents/visitNumber/{vn}" must {

      "get a collection event by visit number" in {
        createEntities() { (study, participant, ceventType) =>
          val cevents = (1 to 2).map { visitNumber =>
            val cevent = factory.createCollectionEvent.copy(visitNumber = visitNumber)
            collectionEventRepository.put(cevent)
            cevent
          }

          val ceventToGet = cevents(0)

          val json = makeRequest(GET, uriWithVisitNumber(participant, ceventToGet))
          (json \ "status").as[String] must include ("success")
          val jsonObj = (json \ "data").as[JsObject]
          compareObj(jsonObj, ceventToGet)
        }
      }

      "fail for invalid participant id when querying for a collection event with a visit number" in {
        val participant = factory.createParticipant
        val cevent = factory.createCollectionEvent

        studyRepository.put(factory.defaultEnabledStudy)

        val json = makeRequest(GET, uriWithVisitNumber(participant, cevent), NOT_FOUND)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid participant id")
      }

      "fail when querying for a collection event with a visit number" in {
        createEntities() { (study, participant, ceventType) =>
          val cevent = factory.createCollectionEvent

          val json = makeRequest(GET, uriWithVisitNumber(participant, cevent), NOT_FOUND)
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("collection event does not exist")
        }
      }
    }

    "POST /participants/{participantId}/cevents" must {

      "add a collection event with no annotation in" in {
        createEntities() { (study, participant, ceventType) =>
          val cevent = factory.createCollectionEvent
          cevent.annotations must have size 0

          val json = makeRequest(POST, uri(participant), json = collectionEventToAddCmd(cevent))
          (json \ "status").as[String] must include ("success")

          val id = (json \ "data" \ "id").as[String]

          collectionEventRepository.getByKey(CollectionEventId(id)) mustSucceed { ce =>
            ce must have (
              'participantId          (cevent.participantId),
              'collectionEventTypeId  (cevent.collectionEventTypeId),
              'version                (0),
              'visitNumber            (cevent.visitNumber),
              'annotations            (cevent.annotations)
            )

            (ce.timeCompleted to cevent.timeCompleted).millis must be < TimeCoparisonMillis
            checkTimeStamps(cevent, ce.timeAdded, None)
          }
        }
      }

      "add a collection event with annotation types" in {
        createEntities() { (study, participant, ceventType) =>
          val annotTypes = createAnnotationsAndTypes
          val annotations = annotTypes.values.toSet

          annotTypes.keys.foreach { annotType =>
            collectionEventAnnotationTypeRepository.put(annotType.copy(studyId = study.id))
          }

          val annotationTypeData = annotTypes.keys.map { annotType =>
            CollectionEventTypeAnnotationTypeData(annotType.id.id, false)
          }.toList

          // update the collection event type with annotation type data
          collectionEventTypeRepository.put(
            ceventType.copy(annotationTypeData = annotationTypeData))

          val cevent = factory.createCollectionEvent.copy(annotations = annotTypes.values.toSet)
          val json = makeRequest(POST, uri(participant), json = collectionEventToAddCmd(cevent))
          (json \ "status").as[String] must include ("success")

          (json \ "data" \ "annotations").as[List[JsObject]] must have size annotTypes.size
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
      }

      "fail when adding collection event with duplicate visit number" in {
        createEntities() { (study, participant, ceventType) =>
          val cevent1 = factory.createCollectionEvent
          collectionEventRepository.put(cevent1)

          val cevent2 = factory.createCollectionEvent.copy(visitNumber = cevent1.visitNumber)
          val json = makeRequest(method         = POST,
                                 path           = uri(participant),
                                 expectedStatus = FORBIDDEN,
                                 json           = collectionEventToAddCmd(cevent2))
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("a collection event with this visit number already exists")
        }
      }

      "fail when missing a required annotation type" in {
        createEntities() { (study, participant, ceventType) =>
          val annotType = factory.createCollectionEventAnnotationType
          collectionEventAnnotationTypeRepository.put(annotType)

          // update the collection event type with annotation type data
          collectionEventTypeRepository.put(
            ceventType.copy(
              annotationTypeData = List(CollectionEventTypeAnnotationTypeData(annotType.id.id, true))))

          val cevent = factory.createCollectionEvent.copy(annotations = Set.empty)
          val json = makeRequest(method         = POST,
                                 path           = uri(participant),
                                 expectedStatus = BAD_REQUEST,
                                 json           = collectionEventToAddCmd(cevent))
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("missing required annotation type(s)")
        }
      }

      "fail when using annotations and collection event type has no annotations" in {
        createEntities() { (study, participant, ceventType) =>
          val annotation = factory.createCollectionEventAnnotation.copy(
            annotationTypeId = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotation]))

          val cevent = factory.createCollectionEvent.copy(annotations = Set(annotation))
          val json = makeRequest(method         = POST,
                                 path           = uri(participant),
                                 expectedStatus = BAD_REQUEST,
                                 json           = collectionEventToAddCmd(cevent))
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("collection event type has no annotation type data")
        }
      }

      "fail for an annotation with an invalid annotation type id" in {
        createEntities() { (study, participant, ceventType) =>
          val annotType = factory.createCollectionEventAnnotationType
          collectionEventAnnotationTypeRepository.put(annotType)

          // update the collection event type with annotation type data
          collectionEventTypeRepository.put(
            ceventType.copy(
              annotationTypeData = List(CollectionEventTypeAnnotationTypeData(annotType.id.id, false))))

          val annotation = factory.createCollectionEventAnnotation.copy(
            annotationTypeId = AnnotationTypeId(nameGenerator.next[CollectionEventAnnotation]))

          val cevent = factory.createCollectionEvent.copy(annotations = Set(annotation))
          val json = makeRequest(method         = POST,
                                 path           = uri(participant),
                                 expectedStatus = BAD_REQUEST,
                                 json           = collectionEventToAddCmd(cevent))
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include (
            "annotation type(s) do not belong to collection event type")
        }
      }

      "fail for more than one annotation with the same annotation type" in {
        createEntities() { (study, participant, ceventType) =>
          val annotType = factory.createCollectionEventAnnotationType(
            AnnotationValueType.Text, 0, Seq.empty)
          collectionEventAnnotationTypeRepository.put(annotType)

          // update the collection event type with annotation type data
          collectionEventTypeRepository.put(
            ceventType.copy(
              annotationTypeData = List(CollectionEventTypeAnnotationTypeData(annotType.id.id, false))))

          val annotation = factory.createCollectionEventAnnotation
          annotation.stringValue mustBe defined
          val annotations = Set(annotation,
                                annotation.copy(stringValue = Some(nameGenerator.next[CollectionEventAnnotation])))

          val cevent = factory.createCollectionEvent.copy(annotations = annotations)
          val json = makeRequest(method         = POST,
                                 path           = uri(participant),
                                 expectedStatus = BAD_REQUEST,
                                 json           = collectionEventToAddCmd(cevent))
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("duplicate annotation types in annotations")
        }
      }

      "fail when adding and participant IDs do not match" in {
        createEntities() { (study, participant, ceventType) =>
          val cevent = factory.createCollectionEvent

          var participant2 = factory.createParticipant.copy(studyId = study.id)
          participantRepository.put(participant2)

          val json = makeRequest(method         = POST,
                                 path           = uri(participant2),
                                 expectedStatus = BAD_REQUEST,
                                 json           = collectionEventToAddCmd(cevent))
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("participant id mismatch")
        }
      }

      "not add a collection event on an disabled study" in {
        createEntities() { (study, participant, ceventType) =>
          val disabledStudy = factory.createDisabledStudy.copy(id = study.id)
          val cevent = factory.createCollectionEvent
          addOnNonEnabledStudy(disabledStudy, cevent)
        }
      }

      "not add a collection event on an retired study" in {
        createEntities() { (study, participant, ceventType) =>
          val retiredStudy = factory.createRetiredStudy.copy(id = study.id)
          val cevent = factory.createCollectionEvent
          addOnNonEnabledStudy(retiredStudy, cevent)
        }
      }
    }

    "PUT /participants/{participantId}/cevents/{id}" must {

      "update a collection event with no annotation types" in {
        createEntities() { (study, participant, ceventType) =>
          val cevent = factory.createCollectionEvent.copy(version = 0)
          cevent.annotations must have size 0
          collectionEventRepository.put(cevent)

          val cevent2 = factory.createCollectionEvent.copy(id      = cevent.id,
                                                           version = cevent.version)
          cevent2.annotations must have size 0

          val json = makeRequest(PUT, uri(participant, cevent2),
                                 json = collectionEventToUpdateCmd(cevent))
          (json \ "status").as[String] must include ("success")

          collectionEventRepository.getByKey(cevent2.id) mustSucceed { ce =>
            ce must have (
              'id                     (cevent2.id),
              'participantId          (cevent2.participantId),
              'collectionEventTypeId  (cevent2.collectionEventTypeId),
              'version                (cevent2.version + 1),
              'visitNumber            (cevent2.visitNumber),
              'annotations            (cevent2.annotations)
            )

            (ce.timeCompleted to cevent2.timeCompleted).millis must be < TimeCoparisonMillis
            checkTimeStamps(ce, cevent.timeAdded, DateTime.now)
          }
        }
      }

      "update a collection event with annotation types" in {
        createEntities() { (study, participant, ceventType) =>
          val annotTypes = createAnnotationsAndTypes

          annotTypes.keys.foreach { annotType =>
            collectionEventAnnotationTypeRepository.put(annotType.copy(studyId = study.id))
          }

          val annotationTypeData = annotTypes.keys.map { annotType =>
            CollectionEventTypeAnnotationTypeData(annotType.id.id, false)
          }.toList

          // update the collection event type with annotation type data
          collectionEventTypeRepository.put(
            ceventType.copy(annotationTypeData = annotationTypeData))

          val cevent = factory.createCollectionEvent.copy(annotations = annotTypes.values.toSet)
          collectionEventRepository.put(cevent)

          val newAnnotations = annotTypes.keys.map { annotationType =>
            val (stringValue, numberValue, selectedValues) =
              factory.createAnnotationValues(annotationType)
            CollectionEventAnnotation(annotationTypeId = annotationType.id,
                                      stringValue      = stringValue,
                                      numberValue      = numberValue,
                                      selectedValues   = selectedValues)
          }.toSet

          val cevent2 = cevent.copy(version     = 0,
                                    annotations = newAnnotations)
          val json = makeRequest(PUT, uri(participant, cevent2),
                                 json = collectionEventToUpdateCmd(cevent2))
          (json \ "status").as[String] must include ("success")

          val jsonAnnotations = (json \ "data" \ "annotations").as[List[JsObject]]
          jsonAnnotations must have size newAnnotations.size

          jsonAnnotations.foreach { jsonAnnotation =>
            val jsonAnnotationTypeId = (jsonAnnotation \ "annotationTypeId").as[String]
            val newAnnotation = newAnnotations.find( x =>
              x.annotationTypeId.id == jsonAnnotationTypeId)
            newAnnotation mustBe defined
            compareAnnotation(jsonAnnotation, newAnnotation.value)
          }
        }
      }

      "update a collection event to remove an annotation type" in {
        createEntities() { (study, participant, ceventType) =>

          val annotType = factory.createCollectionEventAnnotationType.copy(studyId = study.id)
          collectionEventAnnotationTypeRepository.put(annotType)

          val annotation = factory.createCollectionEventAnnotation
          annotation.annotationTypeId mustBe annotType.id

          // update the collection event type with annotation type data
          val ceventType2 = ceventType.copy(annotationTypeData = List(
                              CollectionEventTypeAnnotationTypeData(annotType.id.id, false)))
          collectionEventTypeRepository.put(ceventType2)

          val cevent = factory.createCollectionEvent.copy(annotations = Set(annotation))
          collectionEventRepository.put(cevent)

          val cevent2 = cevent.copy(annotations = Set.empty)
          val json = makeRequest(PUT, uri(participant, cevent2),
                                 json = collectionEventToUpdateCmd(cevent2))
          (json \ "status").as[String] must include ("success")
          (json \ "data" \ "annotations").as[List[JsObject]] must have size 0
        }
      }

      "fail when missing a required annotation" in {
        createEntities() { (study, participant, ceventType) =>

          val annotType = factory.createCollectionEventAnnotationType.copy(studyId = study.id)
          collectionEventAnnotationTypeRepository.put(annotType)

          val annotation = factory.createCollectionEventAnnotation
          annotation.annotationTypeId mustBe annotType.id

          // update the collection event type with annotation type data
          val ceventType2 = ceventType.copy(annotationTypeData = List(
                              CollectionEventTypeAnnotationTypeData(annotType.id.id, true)))
          collectionEventTypeRepository.put(ceventType2)

          val cevent = factory.createCollectionEvent.copy(annotations = Set(annotation))
          collectionEventRepository.put(cevent)

          val cevent2 = cevent.copy(annotations = Set.empty)
          val json = makeRequest(PUT, uri(participant, cevent2), BAD_REQUEST,
                                 json = collectionEventToUpdateCmd(cevent2))
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must be ("missing required annotation type(s)")
        }
      }

      "fail for an invalid annotation type" in {
        createEntities() { (study, participant, ceventType) =>

          val annotType = factory.createCollectionEventAnnotationType.copy(studyId = study.id)
          collectionEventAnnotationTypeRepository.put(annotType)

          val annotation = factory.createCollectionEventAnnotation
          annotation.annotationTypeId mustBe annotType.id

          // update the collection event type with annotation type data
          val ceventType2 = ceventType.copy(annotationTypeData = List(
                              CollectionEventTypeAnnotationTypeData(annotType.id.id, false)))
          collectionEventTypeRepository.put(ceventType2)

          val cevent = factory.createCollectionEvent.copy(annotations = Set(annotation))
          collectionEventRepository.put(cevent)

          val cevent2 = cevent.copy(
            annotations = Set(annotation.copy(annotationTypeId =
                                                AnnotationTypeId(nameGenerator.next[String]))))

          val json = makeRequest(PUT, uri(participant, cevent2), BAD_REQUEST,
                                 json = collectionEventToUpdateCmd(cevent2))
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include (
            "annotation type(s) do not belong to collection event type")
        }
      }

      "fail for more than one annotation with the same annotation type ID" in {
        createEntities() { (study, participant, ceventType) =>

          val annotType = factory.createCollectionEventAnnotationType.copy(studyId = study.id)
          collectionEventAnnotationTypeRepository.put(annotType)

          val annotation = factory.createCollectionEventAnnotation
          annotation.annotationTypeId mustBe annotType.id

          // update the collection event type with annotation type data
          val ceventType2 = ceventType.copy(annotationTypeData = List(
                              CollectionEventTypeAnnotationTypeData(annotType.id.id, false)))
          collectionEventTypeRepository.put(ceventType2)

          val cevent = factory.createCollectionEvent.copy(annotations = Set(annotation))
          collectionEventRepository.put(cevent)

          val newBadAnnotations = (1 to 2).map { x =>
            val (stringValue, numberValue, selectedValues) =
              factory.createAnnotationValues(annotType)
            CollectionEventAnnotation(annotationTypeId = annotType.id,
                                      stringValue      = stringValue,
                                      numberValue      = numberValue,
                                      selectedValues   = selectedValues)
          }.toSet

          val cevent2 = cevent.copy(annotations = newBadAnnotations)

          val json = makeRequest(PUT, uri(participant, cevent2), BAD_REQUEST,
                                 json = collectionEventToUpdateCmd(cevent2))
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include (
            "duplicate annotation types in annotations")
        }
      }

      "fail when updating and participant IDs do not match" in {
        createEntities() { (study, participant, ceventType) =>
          val cevent = factory.createCollectionEvent
          collectionEventRepository.put(cevent)

           val participant2 = factory.createParticipant

          val json = makeRequest(PUT, uri(participant2, cevent), BAD_REQUEST,
                                 json = collectionEventToUpdateCmd(cevent))
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("participant id mismatch")
        }
      }

      "fail when updating and collection event IDs do not match" in {
        createEntities() { (study, participant, ceventType) =>
          val cevent = factory.createCollectionEvent
          collectionEventRepository.put(cevent)

           val cevent2 = factory.createCollectionEvent

          val json = makeRequest(PUT, uri(participant, cevent2), BAD_REQUEST,
                                 json = collectionEventToUpdateCmd(cevent))
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("collection event id mismatch")
        }
      }

      "not update a collection event on an disabled study" in {
        createEntities() { (study, participant, ceventType) =>
          val disabledStudy = factory.createDisabledStudy.copy(id = study.id)
          val cevent = factory.createCollectionEvent
          updateOnNonEnabledStudy(disabledStudy, cevent)
        }
      }

      "not update a collection event on an retired study" in {
        createEntities() { (study, participant, ceventType) =>
          val retiredStudy = factory.createRetiredStudy.copy(id = study.id)
          val cevent = factory.createCollectionEvent
          updateOnNonEnabledStudy(retiredStudy, cevent)
        }
      }

    }

    "DELETE /participants/{participantId}/cevents/{id}/{ver}" must {

      "remove a collection event" in {
        createEntities() { (study, participant, ceventType) =>
          val cevent = factory.createCollectionEvent
          collectionEventRepository.put(cevent)

          val json = makeRequest(DELETE, uri(participant, cevent, cevent.version))
          (json \ "status").as[String] must include ("success")
        }
      }

      "not remove a collection event from an disabled study" in {
        createEntities() { (study, participant, ceventType) =>
          val disabledStudy = factory.createDisabledStudy.copy(id = study.id)
          val cevent = factory.createCollectionEvent
          removeOnNonEnabledStudy(disabledStudy, cevent)
        }
      }

      "not remove a collection event from an retired study" in {
        createEntities() { (study, participant, ceventType) =>
          val retiredStudy = factory.createRetiredStudy.copy(id = study.id)
          val cevent = factory.createCollectionEvent
          removeOnNonEnabledStudy(retiredStudy, cevent)
        }
      }

    }

  }
}

