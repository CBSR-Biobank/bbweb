package org.biobank.controllers.participants

import org.biobank.fixture._
import org.biobank.domain.{ AnnotationType,  AnnotationValueType, JsonHelper }
import org.biobank.domain.study._
import org.biobank.domain.participants._
import org.biobank.controllers._
import org.biobank.fixture.ControllerFixture

import play.api.test.Helpers._
import play.api.libs.json._
import org.joda.time.DateTime
import com.github.nscala_time.time.Imports._

/**
 * Tests the REST API for [[CollectionEvents]].
 */
class CollectionEventsControllerSpec extends ControllerFixture with JsonHelper {
  import org.biobank.TestUtils._
  //import org.biobank.AnnotationTestUtils._

  def listUri(participantId: ParticipantId): String =
    s"/participants/cevents/${participantId.id}/list"

  def uri(participantId: ParticipantId): String =
    s"/participants/cevents/${participantId.id}"

  def uri(participantId: ParticipantId, cevent: CollectionEvent): String =
    uri(participantId) + s"/${cevent.id.id}"

  def uri(participantId: ParticipantId, cevent: CollectionEvent, version: Long): String =
    uri(participantId) + s"/${cevent.id.id}/$version"

  def uri(participant: Participant): String =
    uri(participant.id)

  def uri(participant: Participant, cevent: CollectionEvent): String =
    uri(participant.id, cevent)

  def uri(participant: Participant, cevent: CollectionEvent, version: Long): String =
    uri(participant.id, cevent, version)

  def uriWithQuery(participant: Participant, cevent: CollectionEvent): String =
    uri(participant.id) + s"?ceventId=${cevent.id.id}"

  def uriWithVisitNumber(participant: Participant, cevent: CollectionEvent): String =
    uri(participant.id) + s"/visitNumber/${cevent.visitNumber}"

  /** Converts a collectionEvent into an Add command.
   */
  def collectionEventToAddCmd(collectionEvent: CollectionEvent) = {
    Json.obj(
      "participantId"         -> collectionEvent.participantId.id,
      "collectionEventTypeId" -> collectionEvent.collectionEventTypeId,
      "timeCompleted"         -> collectionEvent.timeCompleted,
      "visitNumber"           -> collectionEvent.visitNumber,
      "annotations"           -> collectionEvent.annotations
    )
  }

  def compareObjs(jsonList: List[JsObject], cevents: List[CollectionEvent]) = {
    val ceventsMap = cevents.map { cevent => (cevent.id, cevent) }.toMap
    jsonList.foreach { jsonObj =>
      val jsonId = CollectionEventId((jsonObj \ "id").as[String])
      compareObj(jsonObj, ceventsMap(jsonId))
    }
  }

  /** Converts a collectionEvent into an Update command.
   */
  def collectionEventToUpdateCmd(collectionEvent: CollectionEvent) = {
    collectionEventToAddCmd(collectionEvent) ++ Json.obj(
      "id"              -> collectionEvent.id.id,
      "expectedVersion" -> Some(collectionEvent.version)
    )
  }

  def createCollectionEventAnnotationType() = {
    AnnotationType(
      uniqueId      = nameGenerator.next[AnnotationType],
      name          = nameGenerator.next[AnnotationType],
      description   = None,
      valueType     = AnnotationValueType.Text,
      maxValueCount = None,
      options       = Seq.empty,
      required      = true)
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
       (factory.createAnnotationType(AnnotationValueType.Select, Some(2), options),
        factory.createAnnotation))).toMap
  }

  def createEntities(fn: (Study, Participant, CollectionEventType) => Unit): Unit = {
    var study = factory.createEnabledStudy
    studyRepository.put(study)

    val ceventType = factory.createCollectionEventType.copy(annotationTypes = Set.empty)
    collectionEventTypeRepository.put(ceventType)

    val participant = factory.createParticipant.copy(studyId = study.id)
    participantRepository.put(participant)

    fn(study, participant, ceventType)
    ()
  }

  def addOnNonEnabledStudy(study: Study, cevent: CollectionEvent) = {
    study must not be an [EnabledStudy]

    studyRepository.put(study)

    val cmdJson = collectionEventToAddCmd(cevent);
    val json = makeRequest(POST, uri(cevent.participantId), BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not enabled")
  }

  def updateOnNonEnabledStudy(study: Study, cevent: CollectionEvent) = {
    study must not be an [EnabledStudy]

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
    study must not be an [EnabledStudy]

    studyRepository.put(study)
    collectionEventRepository.put(cevent)

    val json = makeRequest(DELETE, uri(cevent.participantId, cevent, cevent.version), BAD_REQUEST)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not enabled")
  }

  "Participant REST API" when {

    "GET /participants/cevents/{participantId}?ceventId={ceventId}" must {

      "get a single collection event for a participant" in {
        createEntities { (study, participant, ceventType) =>
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

      "fail for invalid participant id when querying for a single collection event id" in {
        val participant = factory.createParticipant
        val cevent = factory.createCollectionEvent

        val json = makeRequest(GET, uriWithQuery(participant, cevent), NOT_FOUND)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid participant id")
      }

      "fail when querying for a single collection event id" in {
        createEntities { (study, participant, ceventType) =>
          val cevent = factory.createCollectionEvent

          val json = makeRequest(GET, uriWithQuery(participant, cevent), NOT_FOUND)
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("collection event does not exist")
        }
      }

    }

    "GET /participants/cevents/{participantId}/list" must {

      "list none" in {
        createEntities { (study, participant, ceventType) =>
          PagedResultsSpec(this).emptyResults(listUri(participant.id))
        }
      }

      "list a single collection event" in {
        createEntities { (study, participant, ceventType) =>
          val cevent = factory.createCollectionEvent
          collectionEventRepository.put(cevent)

          val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri = listUri(participant.id),
            offset = 0,
            total = 1,
            maybeNext = None,
            maybePrev = None)
          jsonItems must have size 1
          //log.info(s"--> $jsonItems")
          compareObjs(jsonItems, List(cevent))
        }
      }

      "get all collection events for a participant" in {
        createEntities { (study, participant, ceventType) =>
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
            val participantCevents = cevents(participant.id).toList

            val jsonItems = PagedResultsSpec(this).multipleItemsResult(
              uri       = listUri(participant.id),
              offset    = 0,
              total     = cevents.size,
              maybeNext = None,
              maybePrev = None)
            jsonItems must have size cevents.size
            //log.info(s"--> $jsonItems")
            compareObjs(jsonItems, participantCevents)
          }
        }
      }

      "list collection events sorted by visit number" in {
        createEntities { (study, participant, ceventType) =>
          val cevents = (1 to 4).map { visitNumber =>
              val cevent = factory.createCollectionEvent.copy(
                  participantId = participant.id,
                  visitNumber = visitNumber)
              collectionEventRepository.put(cevent)
              cevent
            }

          List("asc", "desc").foreach{ ordering =>
            val jsonItems = PagedResultsSpec(this).multipleItemsResult(
                uri         = listUri(participant.id),
                queryParams = Map("sort" -> "visitNumber", "order" -> ordering),
                offset      = 0,
                total       = cevents.size,
                maybeNext   = None,
                maybePrev   = None)

            jsonItems must have size cevents.size
            if (ordering == "asc") {
              compareObj(jsonItems(0), cevents(0))
              compareObj(jsonItems(1), cevents(1))
              compareObj(jsonItems(2), cevents(2))
              compareObj(jsonItems(3), cevents(3))
            } else {
              compareObj(jsonItems(0), cevents(3))
              compareObj(jsonItems(1), cevents(2))
              compareObj(jsonItems(2), cevents(1))
              compareObj(jsonItems(3), cevents(0))
            }
          }

        }
      }

      "list collection events sorted by time completed" in {
        createEntities { (study, participant, ceventType) =>
          val cevents = (1 to 4).map { hour =>
              val cevent = factory.createCollectionEvent.copy(
                  participantId = participant.id,
                  timeCompleted = DateTime.now.hour(hour))
              collectionEventRepository.put(cevent)
              cevent
            }

          List("asc", "desc").foreach{ ordering =>
            val jsonItems = PagedResultsSpec(this).multipleItemsResult(
                uri         = listUri(participant.id),
                queryParams = Map("sort" -> "timeCompleted", "order" -> ordering),
                offset      = 0,
                total       = cevents.size,
                maybeNext   = None,
                maybePrev   = None)

            jsonItems must have size cevents.size
            if (ordering == "asc") {
              compareObj(jsonItems(0), cevents(0))
              compareObj(jsonItems(1), cevents(1))
              compareObj(jsonItems(2), cevents(2))
              compareObj(jsonItems(3), cevents(3))
            } else {
              compareObj(jsonItems(0), cevents(3))
              compareObj(jsonItems(1), cevents(2))
              compareObj(jsonItems(2), cevents(1))
              compareObj(jsonItems(3), cevents(0))
            }
          }

        }
      }

      "list the first collection event in a paged query" in {
        createEntities { (study, participant, ceventType) =>
          val cevents = (1 to 4).map { hour =>
              val cevent = factory.createCollectionEvent.copy(
                  participantId = participant.id,
                  timeCompleted = DateTime.now.hour(hour))
              collectionEventRepository.put(cevent)
              cevent
            }

          val jsonItem = PagedResultsSpec(this).singleItemResult(
              uri         = listUri(participant.id),
              queryParams = Map("sort" -> "timeCompleted", "pageSize" -> "1"),
              total       = cevents.size,
              maybeNext   = Some(2))

          compareObj(jsonItem, cevents(0))
        }
      }

      "list the last collection event in a paged query" in {
        createEntities { (study, participant, ceventType) =>
          val cevents = (1 to 4).map { hour =>
              val cevent = factory.createCollectionEvent.copy(
                  participantId = participant.id,
                  timeCompleted = DateTime.now.hour(hour))
              collectionEventRepository.put(cevent)
              cevent
            }

          val jsonItem = PagedResultsSpec(this).singleItemResult(
              uri         = listUri(participant.id),
              queryParams = Map("sort" -> "timeCompleted", "page" -> "4", "pageSize" -> "1"),
              total       = cevents.size,
              offset      = 3,
              maybeNext   = None,
              maybePrev   = Some(3))

          compareObj(jsonItem, cevents(3))
        }
      }

      "fail when using an invalid query parameters" in {
        createEntities { (study, participant, ceventType) =>
          PagedResultsSpec(this).failWithInvalidParams(listUri(participant.id))
        }
      }

      "fail for invalid participant id" in {
        val participant = factory.createParticipant
        val json = makeRequest(GET, listUri(participant.id), BAD_REQUEST)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("invalid participant id")
      }

    }

    "GET /participants/cevents/{participantId}/visitNumber/{vn}" must {

      "get a collection event by visit number" in {
        createEntities { (study, participant, ceventType) =>
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
        createEntities { (study, participant, ceventType) =>
          val cevent = factory.createCollectionEvent

          val json = makeRequest(GET, uriWithVisitNumber(participant, cevent), NOT_FOUND)
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("collection event does not exist")
        }
      }
    }

    "POST /participants/cevents/{participantId}" must {

      "add a collection event with no annotation in" in {
        createEntities { (study, participant, ceventType) =>
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
        fail("needs implmenentaton")
      //   createEntities { (study, participant, ceventType) =>
      //     val annotTypes = createAnnotationsAndTypes
      //     val annotations = annotTypes.values.toSet

      //     annotTypes.keys.foreach { annotType =>
      //       annotationTypeRepository.put(annotType.copy(studyId = study.id))
      //     }

      //     val annotationTypeData = annotTypes.keys.map { annotType =>
      //       CollectionEventTypeAnnotationTypeData(annotType.id.id, false)
      //     }.toList

      //     // update the collection event type with annotation type data
      //     collectionEventTypeRepository.put(
      //       ceventType.copy(annotationTypeData = annotationTypeData))

      //     val cevent = factory.createCollectionEvent.copy(annotations = annotTypes.values.toSet)
      //     val json = makeRequest(POST, uri(participant), json = collectionEventToAddCmd(cevent))
      //     (json \ "status").as[String] must include ("success")

      //     (json \ "data" \ "annotations").as[List[JsObject]] must have size annotTypes.size
      //     val jsonAnnotations = (json \ "data" \ "annotations").as[List[JsObject]]
      //     jsonAnnotations must have size annotations.size

      //     jsonAnnotations.foreach { jsonAnnotation =>
      //       val jsonAnnotationTypeId = (jsonAnnotation \ "annotationTypeId").as[String]
      //       val annotation = annotations.find( x =>
      //         x.annotationTypeId.id == jsonAnnotationTypeId)
      //       annotation mustBe defined
      //       compareAnnotation(jsonAnnotation, annotation.value)
      //     }
      //   }
      // }

      // "fail when adding collection event for a different study than participant's" in {
      //   createEntities { (study, participant, ceventType) =>
      //     var otherStudy = factory.createDisabledStudy
      //     val otherCeventType = factory.createCollectionEventType.copy(studyId = otherStudy.id)

      //     studyRepository.put(otherStudy)
      //     collectionEventTypeRepository.put(otherCeventType)

      //     val cevent = factory.createCollectionEvent
      //     val json = makeRequest(method         = POST,
      //                            path           = uri(participant),
      //                            expectedStatus = BAD_REQUEST,
      //                            json           = collectionEventToAddCmd(cevent))
      //     (json \ "status").as[String] must include ("error")

      //     (json \ "message").as[String] must include (
      //       "participant and collection event type not in the same study")
      //   }
      }

      "fail when adding collection event with duplicate visit number" in {
        createEntities { (study, participant, ceventType) =>
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
        createEntities { (study, participant, ceventType) =>
          val annotType = factory.createAnnotationType

          collectionEventTypeRepository.put(
            ceventType.copy(annotationTypes = Set(annotType)))

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
        fail("is this test required?")
        // createEntities { (study, participant, ceventType) =>
        //   val annotation = factory.createAnnotation.copy(
        //     annotationTypeId = AnnotationTypeId(nameGenerator.next[Annotation]))

        //   val cevent = factory.createCollectionEvent.copy(annotations = Set(annotation))
        //   val json = makeRequest(method         = POST,
        //                          path           = uri(participant),
        //                          expectedStatus = BAD_REQUEST,
        //                          json           = collectionEventToAddCmd(cevent))
        //   (json \ "status").as[String] must include ("error")
        //   (json \ "message").as[String] must include ("collection event type has no annotation type data")
        // }
      }

      "fail for an annotation with an invalid annotation type id" in {
        fail("needs to be re written")
        // createEntities { (study, participant, ceventType) =>
        //   val annotType = factory.createAnnotationType
        //   annotationTypeRepository.put(annotType)

        //   // update the collection event type with annotation type data
        //   collectionEventTypeRepository.put(
        //     ceventType.copy(
        //       annotationTypeData = List(CollectionEventTypeAnnotationTypeData(annotType.id.id, false))))

        //   val annotation = factory.createAnnotation.copy(
        //     annotationTypeId = AnnotationTypeId(nameGenerator.next[Annotation]))

        //   val cevent = factory.createCollectionEvent.copy(annotations = Set(annotation))
        //   val json = makeRequest(method         = POST,
        //                          path           = uri(participant),
        //                          expectedStatus = BAD_REQUEST,
        //                          json           = collectionEventToAddCmd(cevent))
        //   (json \ "status").as[String] must include ("error")
        //   (json \ "message").as[String] must include (
        //     "annotation type(s) do not belong to collection event type")
        // }
      }

      "fail for more than one annotation with the same annotation type" in {
        fail("needs to be re written")
        // createEntities { (study, participant, ceventType) =>
        //   val annotType = factory.createAnnotationType(
        //     AnnotationValueType.Text, 0, Seq.empty)
        //   annotationTypeRepository.put(annotType)

        //   // update the collection event type with annotation type data
        //   collectionEventTypeRepository.put(
        //     ceventType.copy(
        //       annotationTypeData = List(CollectionEventTypeAnnotationTypeData(annotType.id.id, false))))

        //   val annotation = factory.createAnnotation
        //   annotation.stringValue mustBe defined
        //   val annotations = Set(annotation,
        //                         annotation.copy(stringValue = Some(nameGenerator.next[Annotation])))

        //   val cevent = factory.createCollectionEvent.copy(annotations = annotations)
        //   val json = makeRequest(method         = POST,
        //                          path           = uri(participant),
        //                          expectedStatus = BAD_REQUEST,
        //                          json           = collectionEventToAddCmd(cevent))
        //   (json \ "status").as[String] must include ("error")
        //   (json \ "message").as[String] must include ("duplicate annotation types in annotations")
        // }
      }

      "fail when adding and participant IDs do not match" in {
        createEntities { (study, participant, ceventType) =>
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
        createEntities { (study, participant, ceventType) =>
          val disabledStudy = factory.createDisabledStudy.copy(id = study.id)
          val cevent = factory.createCollectionEvent
          addOnNonEnabledStudy(disabledStudy, cevent)
        }
      }

      "not add a collection event on an retired study" in {
        createEntities { (study, participant, ceventType) =>
          val retiredStudy = factory.createRetiredStudy.copy(id = study.id)
          val cevent = factory.createCollectionEvent
          addOnNonEnabledStudy(retiredStudy, cevent)
        }
      }
    }

    "PUT /participants/cevents/{participantId}/{id}" must {

      "update a collection event with no annotation types" in {
        createEntities { (study, participant, ceventType) =>
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
        fail("needs to be re written")
        // createEntities { (study, participant, ceventType) =>
        //   val annotTypes = createAnnotationsAndTypes

        //   annotTypes.keys.foreach { annotType =>
        //     annotationTypeRepository.put(annotType.copy(studyId = study.id))
        //   }

        //   val annotationTypeData = annotTypes.keys.map { annotType =>
        //     CollectionEventTypeAnnotationTypeData(annotType.id.id, false)
        //   }.toList

        //   // update the collection event type with annotation type data
        //   collectionEventTypeRepository.put(
        //     ceventType.copy(annotationTypeData = annotationTypeData))

        //   val cevent = factory.createCollectionEvent.copy(annotations = annotTypes.values.toSet)
        //   collectionEventRepository.put(cevent)

        //   val newAnnotations = annotTypes.keys.map { annotationType =>
        //     val (stringValue, numberValue, selectedValues) =
        //       factory.createAnnotationValues(annotationType)
        //     Annotation(annotationTypeId = annotationType.id,
        //                               stringValue      = stringValue,
        //                               numberValue      = numberValue,
        //                               selectedValues   = selectedValues)
        //   }.toSet

        //   val cevent2 = cevent.copy(version     = 0,
        //                             annotations = newAnnotations)
        //   val json = makeRequest(PUT, uri(participant, cevent2),
        //                          json = collectionEventToUpdateCmd(cevent2))
        //   (json \ "status").as[String] must include ("success")

        //   val jsonAnnotations = (json \ "data" \ "annotations").as[List[JsObject]]
        //   jsonAnnotations must have size newAnnotations.size

        //   jsonAnnotations.foreach { jsonAnnotation =>
        //     val jsonAnnotationTypeId = (jsonAnnotation \ "annotationTypeId").as[String]
        //     val newAnnotation = newAnnotations.find( x =>
        //       x.annotationTypeId.id == jsonAnnotationTypeId)
        //     newAnnotation mustBe defined
        //     compareAnnotation(jsonAnnotation, newAnnotation.value)
        //   }
        // }
      }

      "update a collection event to remove an annotation" in {
        fail("needs to be re written")
        // createEntities { (study, participant, ceventType) =>

        //   val annotType = factory.createAnnotationType.copy(studyId = study.id)
        //   annotationTypeRepository.put(annotType)

        //   val annotation = factory.createAnnotation
        //   annotation.annotationTypeId mustBe annotType.id

        //   // update the collection event type with annotation type data
        //   val ceventType2 = ceventType.copy(annotationTypeData = List(
        //                       CollectionEventTypeAnnotationTypeData(annotType.id.id, false)))
        //   collectionEventTypeRepository.put(ceventType2)

        //   val cevent = factory.createCollectionEvent.copy(annotations = Set(annotation))
        //   collectionEventRepository.put(cevent)

        //   val cevent2 = cevent.copy(annotations = Set.empty)
        //   val json = makeRequest(PUT, uri(participant, cevent2),
        //                          json = collectionEventToUpdateCmd(cevent2))
        //   (json \ "status").as[String] must include ("success")
        //   (json \ "data" \ "annotations").as[List[JsObject]] must have size 0
        // }
      }

      "fail when missing a required annotation" in {
        fail("needs to be re written")
        // createEntities { (study, participant, ceventType) =>

        //   val annotType = factory.createAnnotationType.copy(studyId = study.id)
        //   annotationTypeRepository.put(annotType)

        //   val annotation = factory.createAnnotation
        //   annotation.annotationTypeId mustBe annotType.id

        //   // update the collection event type with annotation type data
        //   val ceventType2 = ceventType.copy(annotationTypeData = List(
        //                       CollectionEventTypeAnnotationTypeData(annotType.id.id, true)))
        //   collectionEventTypeRepository.put(ceventType2)

        //   val cevent = factory.createCollectionEvent.copy(annotations = Set(annotation))
        //   collectionEventRepository.put(cevent)

        //   val cevent2 = cevent.copy(annotations = Set.empty)
        //   val json = makeRequest(PUT, uri(participant, cevent2), BAD_REQUEST,
        //                          json = collectionEventToUpdateCmd(cevent2))
        //   (json \ "status").as[String] must include ("error")
        //   (json \ "message").as[String] must be ("missing required annotation type(s)")
        // }
      }

      "fail when updating collection event to a different study than participant's" in {
        createEntities { (study, participant, ceventType) =>
          val cevent1 = factory.createCollectionEvent
          collectionEventRepository.put(cevent1)

          var otherStudy = factory.createDisabledStudy
          val otherCeventType = factory.createCollectionEventType.copy(studyId = otherStudy.id)

          studyRepository.put(otherStudy)
          collectionEventTypeRepository.put(otherCeventType)

          val cevent2 = factory.createCollectionEvent.copy(id = cevent1.id)

          val json = makeRequest(method         = PUT,
                                 path           = uri(participant, cevent2),
                                 expectedStatus = BAD_REQUEST,
                                 json           = collectionEventToUpdateCmd(cevent2))

          (json \ "status").as[String] must include ("error")

          log.info(s"--> " + (json \ "message"))

          (json \ "message").as[String] must include (
            "participant and collection event type not in the same study")
        }
      }

      "fail for an invalid annotation type" in {
        fail("needs to be re written")
        // createEntities { (study, participant, ceventType) =>

        //   val annotType = factory.createAnnotationType.copy(studyId = study.id)
        //   annotationTypeRepository.put(annotType)

        //   val annotation = factory.createAnnotation
        //   annotation.annotationTypeId mustBe annotType.id

        //   // update the collection event type with annotation type data
        //   val ceventType2 = ceventType.copy(annotationTypeData = List(
        //                       CollectionEventTypeAnnotationTypeData(annotType.id.id, false)))
        //   collectionEventTypeRepository.put(ceventType2)

        //   val cevent = factory.createCollectionEvent.copy(annotations = Set(annotation))
        //   collectionEventRepository.put(cevent)

        //   val cevent2 = cevent.copy(
        //     annotations = Set(annotation.copy(annotationTypeId =
        //                                         AnnotationTypeId(nameGenerator.next[String]))))

        //   val json = makeRequest(PUT, uri(participant, cevent2), BAD_REQUEST,
        //                          json = collectionEventToUpdateCmd(cevent2))
        //   (json \ "status").as[String] must include ("error")
        //   (json \ "message").as[String] must include (
        //     "annotation type(s) do not belong to collection event type")
        // }
      }

      "fail for more than one annotation with the same annotation type ID" in {
        fail("needs to be re written")
        // createEntities { (study, participant, ceventType) =>

        //   val annotType = factory.createAnnotationType.copy(studyId = study.id)
        //   annotationTypeRepository.put(annotType)

        //   val annotation = factory.createAnnotation
        //   annotation.annotationTypeId mustBe annotType.id

        //   // update the collection event type with annotation type data
        //   val ceventType2 = ceventType.copy(annotationTypeData = List(
        //                       CollectionEventTypeAnnotationTypeData(annotType.id.id, false)))
        //   collectionEventTypeRepository.put(ceventType2)

        //   val cevent = factory.createCollectionEvent.copy(annotations = Set(annotation))
        //   collectionEventRepository.put(cevent)

        //   val newBadAnnotations = (1 to 2).map { x =>
        //     val (stringValue, numberValue, selectedValues) =
        //       factory.createAnnotationValues(annotType)
        //     Annotation(annotationTypeId = annotType.id,
        //                               stringValue      = stringValue,
        //                               numberValue      = numberValue,
        //                               selectedValues   = selectedValues)
        //   }.toSet

        //   val cevent2 = cevent.copy(annotations = newBSadAnnotations)

        //   val json = makeRequest(PUT, uri(participant, cevent2), BAD_REQUEST,
        //                          json = collectionEventToUpdateCmd(cevent2))
        //   (json \ "status").as[String] must include ("error")
        //   (json \ "message").as[String] must include (
        //     "duplicate annotation types in annotations")
        // }
      }

      "fail when updating and participant IDs do not match" in {
        createEntities { (study, participant, ceventType) =>
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
        createEntities { (study, participant, ceventType) =>
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
        createEntities { (study, participant, ceventType) =>
          val disabledStudy = factory.createDisabledStudy.copy(id = study.id)
          val cevent = factory.createCollectionEvent
          updateOnNonEnabledStudy(disabledStudy, cevent)
        }
      }

      "not update a collection event on an retired study" in {
        createEntities { (study, participant, ceventType) =>
          val retiredStudy = factory.createRetiredStudy.copy(id = study.id)
          val cevent = factory.createCollectionEvent
          updateOnNonEnabledStudy(retiredStudy, cevent)
        }
      }

    }

    "DELETE /participants/cevents/{participantId}/{id}/{ver}" must {

      "remove a collection event" in {
        createEntities { (study, participant, ceventType) =>
          val cevent = factory.createCollectionEvent
          collectionEventRepository.put(cevent)

          val json = makeRequest(DELETE, uri(participant, cevent, cevent.version))
          (json \ "status").as[String] must include ("success")
        }
      }

      "not remove a collection event from an disabled study" in {
        createEntities { (study, participant, ceventType) =>
          val disabledStudy = factory.createDisabledStudy.copy(id = study.id)
          val cevent = factory.createCollectionEvent
          removeOnNonEnabledStudy(disabledStudy, cevent)
        }
      }

      "not remove a collection event from an retired study" in {
        createEntities { (study, participant, ceventType) =>
          val retiredStudy = factory.createRetiredStudy.copy(id = study.id)
          val cevent = factory.createCollectionEvent
          removeOnNonEnabledStudy(retiredStudy, cevent)
        }
      }

    }

  }
}
