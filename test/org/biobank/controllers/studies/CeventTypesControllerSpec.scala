package org.biobank.controllers.studies

import java.time.OffsetDateTime
import org.biobank.controllers._
import org.biobank.domain.Slug
import org.biobank.domain.annotations._
import org.biobank.domain.studies._
import org.biobank.dto._
import org.biobank.fixtures._
import org.biobank.matchers.PagedResultsMatchers
import org.scalatest.matchers.{MatchResult, Matcher}
import play.api.libs.json._
import play.api.mvc._
import play.api.test.Helpers._
import scala.concurrent.Future

class CeventTypesControllerSpec
    extends ControllerFixture
    with AnnotationTypeJson
    with PagedResultsSharedSpec
    with PagedResultsMatchers {

  import org.biobank.TestUtils._
  import org.biobank.matchers.EntityMatchers._
  import org.biobank.matchers.JsonMatchers._

  class EventTypeFixture(numEventTypes: Int = 1,
                         hasSpecimenDefinition: Boolean = false) {
    val study = factory.createDisabledStudy
    val eventTypes = (0 until numEventTypes)
      .map {_ =>
        val specimenDefinitions =
          if (hasSpecimenDefinition) Set(factory.createCollectionSpecimenDefinition)
          else Set.empty[CollectionSpecimenDefinition]

        factory.createCollectionEventType.copy(studyId = study.id,
                                               specimenDefinitions = specimenDefinitions)
      }
      .toList

    studyRepository.put(study)
    eventTypes.foreach(addToRepository)
  }

  private def uri(paths: String*): Url = {
    val path = if (paths.isEmpty) "/api/studies/cetypes"
               else "/api/studies/cetypes/" + paths.mkString("/")
    return new Url(path)
  }

  private def urlName(cet: CollectionEventType)                    = uri("name", cet.id.id)
  private def urlDescription(cet: CollectionEventType)             = uri("description", cet.id.id)
  private def urlRecurring(cet: CollectionEventType)               = uri("recurring", cet.id.id)
  private def urlAddAnnotationType(cet: CollectionEventType)       = uri("annottype", cet.id.id)
  private def urlAddSepecimenDescription(cet: CollectionEventType) = uri("spcdef", cet.id.id)

  private def urlUpdateAnnotationType(cet: CollectionEventType, annotType: AnnotationType) =
    uri("annottype", cet.id.id, annotType.id.id)

  private def urlUpdateSpecimenDefinition(cet: CollectionEventType, sd: SpecimenDefinition) =
    uri("spcdef", cet.id.id, sd.id.id)

  private def cetToAddCmd(cet: CollectionEventType) = {
    Json.obj("studyId"              -> cet.studyId.id,
             "name"                 -> cet.name,
             "description"          -> cet.description,
             "recurring"            -> cet.recurring,
             "specimenDefinitions" -> cet.specimenDefinitions,
             "annotationTypes"      -> cet.annotationTypes)
  }

  describe("Collection Event Type REST API") {

    describe("GET /api/studies/cetypes/:studySlug/:eventTypeSlug") {

      it("get a single collection event type") {
        val f = new EventTypeFixture
        val cet = f.eventTypes(0)
        val reply = makeAuthRequest(GET, uri(f.study.slug.id, cet.slug.id).path).value
        reply must beOkResponseWithJsonReply

        val replyCet = (contentAsJson(reply) \ "data").validate[CollectionEventType]
        replyCet must be (jsSuccess)
        replyCet.get must matchCollectionEventType(cet)
      }

      it("fail for an invalid study slug") {
        val study = factory.createDisabledStudy
        val cet = factory.createCollectionEventType
        val reply = makeAuthRequest(GET, uri(study.slug.id, cet.slug.id).path).value
        reply must beNotFoundWithMessage("EntityCriteriaNotFound.*study slug")
      }

      it("fail for an invalid collection event type slug") {
        val study = factory.createDisabledStudy
        val cet = factory.createCollectionEventType
        studyRepository.put(study)
        val reply = makeAuthRequest(GET, uri(study.slug.id, cet.slug.id).path).value
        reply must beNotFoundWithMessage("EntityCriteriaNotFound.*collection event type slug")
      }

    }

    describe("GET /api/studies/cetypes") {

      it("list none") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)
        val url = new Url(uri(study.slug.id).path)
        url must beEmptyResults
      }

      describe("list a single collection event type") {
        listSingleEventType() { () =>
          val f = new EventTypeFixture
          (uri(f.study.slug.id), f.eventTypes(0))
        }
      }

      describe("get all collection event types for a study") {
        listMultipleEventTypes() { () =>
          val f = new EventTypeFixture(3)
          (uri(f.study.slug.id), f.eventTypes.sortWith(_.name < _.name))
        }
      }

      describe("list collection event types sorted by name") {

        describe("in ascending order") {
          listMultipleEventTypes() { () =>
            val f = new EventTypeFixture(3)
            (new Url(uri(f.study.slug.id) + "?sort=name"), f.eventTypes.sortWith(_.name < _.name))
          }
        }

        describe("in descending order") {
          listMultipleEventTypes() { () =>
            val f = new EventTypeFixture(3)
            (new Url(uri(f.study.slug.id) + "?sort=-name"), f.eventTypes.sortWith(_.name > _.name))
          }
        }

      }

      describe("list the first Collection Event Type in a paged query") {
        listSingleEventType() { () =>
          val f = new EventTypeFixture(3)
          (new Url(uri(f.study.slug.id) + s"?filter=name::${f.eventTypes(0).name}"), f.eventTypes(0))
        }
      }

      describe("list the last Collection Event Type in a paged query") {
        listSingleEventType() { () =>
          val f = new EventTypeFixture(3)
          (new Url(uri(f.study.slug.id) + s"?filter=name::${f.eventTypes(2).name}"), f.eventTypes(2))
        }
      }

      it("fail for invalid study id") {
        val study = factory.createDisabledStudy
        val reply = makeAuthRequest(GET, uri(study.slug.id).path).value
        reply must beNotFoundWithMessage("EntityCriteriaNotFound.*study")
      }

      describe("fail when using an invalid query parameters") {
        pagedQueryShouldFailSharedBehaviour { () =>
          val f = new EventTypeFixture(3)
          uri(f.study.slug.id)
        }
      }

    }

    describe("GET /api/studies/cetypes/names")  {

      it("list multiple event type names in ascending order") {
        val f = new EventTypeFixture(2)

        val eventTypes = Seq(f.eventTypes(0).copy(name = "ET1"),
                             f.eventTypes(1).copy(name = "ET2"))
        val dtos = eventTypes.sortWith(_.name < _.name).map(EntityInfoDto(_))
        eventTypes.foreach(addToRepository)

        val reply = makeAuthRequest(GET, uri("names", f.study.id.id) + "?order=asc").value
        reply must beOkResponseWithJsonReply

        val replyDtos = (contentAsJson(reply) \ "data").validate[List[EntityInfoDto]]
        replyDtos must be (jsSuccess)
        replyDtos.get must equal (dtos)
      }

      it("list a single event type when using a filter") {
        val f = new EventTypeFixture(2)

        val eventTypes = Seq(f.eventTypes(0).copy(name = "ET1"),
                             f.eventTypes(1).copy(name = "ET2"))
        eventTypes.foreach(addToRepository)

        val reply = makeAuthRequest(GET, uri() + s"/names/${f.study.id}?filter=name::ET1").value
        reply must beOkResponseWithJsonReply

        val replyDtos = (contentAsJson(reply) \ "data").validate[List[EntityInfoDto]]
        replyDtos must be (jsSuccess)
        replyDtos.get must have length (1)
        replyDtos.get.foreach(_ must equal (EntityInfoDto(eventTypes(0))))
      }

      it("list nothing when using a name filter for name not in system") {
        val f = new EventTypeFixture(2)

        val eventTypes = Seq(f.eventTypes(0).copy(name = "ET1"),
                             f.eventTypes(1).copy(name = "ET2"))
        eventTypes.foreach(addToRepository)

        val reply = makeAuthRequest(GET, uri() + s"/names/${f.study.id}?filter=name::xxx").value
        reply must beOkResponseWithJsonReply

        val replyDtos = (contentAsJson(reply) \ "data").validate[List[EntityInfoDto]]
        replyDtos must be (jsSuccess)
        replyDtos.get must have length (0)
      }

      it("fail for invalid sort field") {
        val f = new EventTypeFixture(2)
        val eventTypes = Seq(f.eventTypes(0).copy(name = "ET1"),
                             f.eventTypes(1).copy(name = "ET2"))
        eventTypes.foreach(addToRepository)

        val reply = makeAuthRequest(GET, uri() + s"/names/${f.study.id}?sort=xxx").value
        reply must beBadRequestWithMessage("invalid sort field")
      }
    }

    describe("GET /api/studies/cetypes/spcdef/:studySlug") {

      it("can retrieve specimen definitions for a study") {
        val f = new EventTypeFixture(2, true)
        val expectedReply = f.eventTypes
          .sortBy(_.name)
          .map { eventType =>
            val definitionNames = eventType.specimenDefinitions.map { definition =>
                EntityInfoDto(definition.id.id, definition.slug, definition.name)
              }
            SpecimenDefinitionNames(eventType.id.id,
                                    eventType.slug,
                                    eventType.name,
                                    definitionNames)
          }
          .toList

        val reply = makeAuthRequest(GET, uri("spcdef", f.study.slug.id).path).value
        reply must beOkResponseWithJsonReply

        val replyDtos = (contentAsJson(reply) \ "data").validate[List[SpecimenDefinitionNames]]
        replyDtos must be (jsSuccess)
        replyDtos.get must equal (expectedReply)
      }

      it("fail for an invalid study slug") {
        val study = factory.createDisabledStudy
        val reply = makeAuthRequest(GET, uri("spcdef", study.slug.id).path).value
        reply must beNotFoundWithMessage("EntityCriteriaNotFound.*study slug")
      }

    }

    describe("POST /api/studies/cetypes/:studyId") {

      it("add a collection event type") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType
        val reply = makeAuthRequest(POST, uri(study.id.id).path, cetToAddCmd(cet)).value
        reply must beOkResponseWithJsonReply

        val replyId = (contentAsJson(reply) \ "data" \ "id").validate[CollectionEventTypeId]
        replyId must be (jsSuccess)

        val updatedCet = cet.copy(id = replyId.get, timeAdded = OffsetDateTime.now)
        reply must matchUpdatedEventType(updatedCet)
      }

      it("allow adding a collection event type with same name on two different studies") {
        val cet = factory.createCollectionEventType

        List(factory.createDisabledStudy, factory.createDisabledStudy) foreach { study =>
          studyRepository.put(study)

          val reqJson = cetToAddCmd(cet.copy(studyId = study.id))
          val reply = makeAuthRequest(POST, uri(study.id.id).path, reqJson).value
          reply must beOkResponseWithJsonReply

          val replyId = (contentAsJson(reply) \ "data" \ "id").validate[CollectionEventTypeId]
          replyId must be (jsSuccess)

          val replySlug = (contentAsJson(reply) \ "data" \ "slug").validate[Slug]
          replySlug must be (jsSuccess)

          val updatedCet = cet.copy(id        = replyId.get,
                                    slug      = replySlug.get,
                                    timeAdded = OffsetDateTime.now)
          reply must matchUpdatedEventType(updatedCet)
        }
      }

      describe("not add a collection event type to an enabled study") {
        addOnNonDisabledStudySharedBehaviour(factory.createEnabledStudy)
      }

      describe("not add a collection event type to an retired study") {
        addOnNonDisabledStudySharedBehaviour(factory.createRetiredStudy)
      }

      it("fail when adding and study IDs is invalid") {
        val study = factory.createDisabledStudy
        val cet = factory.createCollectionEventType
        val reply = makeAuthRequest(POST, uri(study.id.id).path, cetToAddCmd(cet)).value
        reply must beNotFoundWithMessage("IdNotFound.*study")
      }

      it("fail when adding a collection event type with a duplicate name to the same study") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val ceventType = factory.createCollectionEventType
        collectionEventTypeRepository.put(ceventType)

        val ceventType2 = factory.createCollectionEventType.copy(name = ceventType.name)
        val reply = makeAuthRequest(POST, uri(study.id.id).path, cetToAddCmd(ceventType2)).value
        reply must beForbiddenRequestWithMessage("collection event type with name already exists")
      }

    }

    describe("DELETE /api/studies/cetypes/:studyId/:id/:ver") {

      it("remove a collection event type") {
        val f = new EventTypeFixture
        val cet = f.eventTypes(0)
        val reply = makeAuthRequest(DELETE, uri(f.study.id.id, cet.id.id, cet.version.toString).path).value
        reply must beOkResponseWithJsonReply
        collectionEventTypeRepository.getByKey(cet.id) mustFail "IdNotFound.*collection event type.*"
      }

      describe("not remove a collection event type on an enabled study") {
        removeOnNonDisabledStudySharedBehaviour(factory.createEnabledStudy)
      }

      describe("not remove a collection event type on an retired study") {
        removeOnNonDisabledStudySharedBehaviour(factory.createRetiredStudy)
      }

      ignore("not remove a collection event type that is in use") {
        fail("write this test")
      }

    }

    describe("POST /api/studies/cetypes/name/:id") {

      it("update a collection event type's name") {
        val f = new EventTypeFixture
        val cet = f.eventTypes(0)
        val newName = nameGenerator.next[CollectionEventType]
        val reply = makeAuthRequest(POST,
                                    urlName(cet).path,
                                    Json.obj("studyId"         -> cet.studyId.id,
                                             "expectedVersion" -> Some(cet.version),
                                             "name"            -> newName)).value
        reply must beOkResponseWithJsonReply
        val updatedCet = cet.copy(version      = cet.version + 1,
                                  slug         = Slug(newName),
                                  name         = newName,
                                  timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedEventType(updatedCet)
      }

      it("allow updating to the same name on collection event types of two different studies") {
        val studyCetTuples = (1 to 2).map { _ =>
            val study = factory.createDisabledStudy
            studyRepository.put(study)

            val cet = factory.createCollectionEventType
            collectionEventTypeRepository.put(cet)

            (study, cet)
          }

        val commonName = nameGenerator.next[CollectionEventType]
        studyCetTuples.zipWithIndex.foreach { case ((study, cet), index) =>
          val reply = makeAuthRequest(POST,
                                      urlName(cet).path,
                                      Json.obj("studyId"         -> cet.studyId,
                                               "id"              -> cet.id.id,
                                               "expectedVersion" -> cet.version,
                                               "name"            -> commonName)).value
          reply must beOkResponseWithJsonReply

          val newSlug =  if (index <= 0) Slug(commonName)
                         else Slug(commonName + "-1")

          val updatedCet = cet.copy(version      = cet.version + 1,
                                    slug         = newSlug,
                                    name         = commonName,
                                    timeModified = Some(OffsetDateTime.now))
          reply must matchUpdatedEventType(updatedCet)
        }
      }

      it("fail when updating name to one already in use in the same study") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cetList = (1 to 2).map { _ =>
            val cet = factory.createCollectionEventType
            collectionEventTypeRepository.put(cet)
            cet
          }.toList

        val duplicateName = cetList(0).name
        val reply = makeAuthRequest(POST,
                                    urlName(cetList(1)).path,
                                    Json.obj("studyId"         -> cetList(1).studyId,
                                             "id"              -> cetList(1).id.id,
                                             "expectedVersion" -> cetList(1).version,
                                             "name"            -> duplicateName)).value
        reply must beForbiddenRequestWithMessage("collection event type with name already exists")
      }

      describe("not update a collection event type's name on an enabled study") {
        updateOnNonDisabledStudySharedBehaviour(factory.createEnabledStudy) { cet =>
          (urlName(cet), Json.obj("name" -> nameGenerator.next[CollectionEventType]))
        }
      }

      describe("not update a collection event type's name on an retired study") {
        updateOnNonDisabledStudySharedBehaviour(factory.createRetiredStudy) { cet =>
          (urlName(cet), Json.obj("name" -> nameGenerator.next[CollectionEventType]))
        }
      }

      describe("fail when updating name and collection event type ID is invalid") {
        updateOnInvalidCeventTypeSharedBehaviour { cet =>
          (urlName(cet), Json.obj("name" -> nameGenerator.next[CollectionEventType]))
        }
      }

      describe("fail when updating name with an invalid version") {
        updateWithInvalidVersionSharedBehaviour { cet =>
          (urlName(cet), Json.obj("name" -> nameGenerator.next[CollectionEventType]))
        }
      }
    }

    describe("POST /api/studies/cetypes/description/:id") {

      it("update a collection event type's description") {
        val f = new EventTypeFixture
        val cet = f.eventTypes(0)
        val newDescription = Some(nameGenerator.next[CollectionEventType])
        val reply = makeAuthRequest(POST,
                                    urlDescription(cet).path,
                                    Json.obj("studyId"         -> cet.studyId.id,
                                             "expectedVersion" -> Some(cet.version),
                                             "description"     -> newDescription)).value
        reply must beOkResponseWithJsonReply
        val updatedCet = cet.copy(version      = cet.version + 1,
                                  description  = newDescription,
                                  timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedEventType(updatedCet)
      }

      describe("not update a collection event type's description on an enabled study") {
        updateOnNonDisabledStudySharedBehaviour(factory.createEnabledStudy) { cet =>
          (urlDescription(cet), Json.obj("description" -> nameGenerator.next[CollectionEventType]))
        }
      }

      describe("not update a collection event type's description on an retired study") {
        updateOnNonDisabledStudySharedBehaviour(factory.createRetiredStudy) { cet =>
          (urlDescription(cet), Json.obj("description" -> nameGenerator.next[CollectionEventType]))
        }
      }

      describe("fail when updating description and collection event type ID is invalid") {
        updateOnInvalidCeventTypeSharedBehaviour { cet =>
          (urlDescription(cet), Json.obj("description" -> nameGenerator.next[CollectionEventType]))
        }

      }

      describe("fail when updating description with an invalid version") {
        updateWithInvalidVersionSharedBehaviour { cet =>
          (urlDescription(cet), Json.obj("description" -> nameGenerator.next[CollectionEventType]))
        }
      }
    }

    describe("POST /api/studies/cetypes/recurring/:id") {

      it("update a collection event type's recurring setting") {
        val f = new EventTypeFixture
        val cet = f.eventTypes(0)

        Set(true, false).zipWithIndex.foreach { case (recurring, index) =>
          val reply = makeAuthRequest(POST, urlRecurring(cet).path,
                                      Json.obj("studyId"         -> cet.studyId.id,
                                               "expectedVersion" -> Some(cet.version + index),
                                               "recurring"       -> recurring)).value
          reply must beOkResponseWithJsonReply
          val updatedCet = cet.copy(version      = cet.version + index + 1,
                                    recurring    = recurring,
                                    timeModified = Some(OffsetDateTime.now))
          reply must matchUpdatedEventType(updatedCet)
        }
      }

      describe("not update a collection event type's recurring on an enabled study") {
        updateOnNonDisabledStudySharedBehaviour(factory.createEnabledStudy) { cet =>
          (urlRecurring(cet), Json.obj("recurring" -> false))
        }
      }

      describe("not update a collection event type's recurring on an retired study") {
        updateOnNonDisabledStudySharedBehaviour(factory.createRetiredStudy) { cet =>
          (urlRecurring(cet), Json.obj("recurring" -> false))
        }
      }

      describe("fail when updating recurring and collection event type ID is invalid") {
        updateOnInvalidCeventTypeSharedBehaviour { cet =>
          (urlRecurring(cet), Json.obj("recurring" -> false))
        }
      }

      describe("fail when updating recurring with an invalid version") {
        updateWithInvalidVersionSharedBehaviour { cet =>
          (urlRecurring(cet), Json.obj("recurring" -> false))
        }
      }
    }

    describe("POST /api/studies/cetypes/annottypes/:id") {

      it("add an annotation type") {
        val f = new EventTypeFixture
        val cet = f.eventTypes(0)
        val annotType = factory.createAnnotationType

        val reqJson = Json.obj("id"              -> cet.id.id,
                               "studyId"         -> cet.studyId.id,
                               "expectedVersion" -> Some(cet.version)) ++ annotationTypeToJsonNoId(annotType)

        val reply = makeAuthRequest(POST, urlAddAnnotationType(cet).path, reqJson).value
        reply must beOkResponseWithJsonReply

        val newAnnotationTypeId =
          (contentAsJson(reply) \ "data" \ "annotationTypes" \ 0 \ "id").validate[AnnotationTypeId]
        newAnnotationTypeId must be (jsSuccess)

        val updatedAnnotationType = annotType.copy(id = newAnnotationTypeId.get)
        val updatedCet = cet.copy(version         = cet.version + 1,
                                  annotationTypes = Set(updatedAnnotationType),
                                  timeModified    = Some(OffsetDateTime.now))
        reply must matchUpdatedEventType(updatedCet)
      }

      describe("fail when adding annotation type and collection event type ID does not exist") {
        updateOnInvalidCeventTypeSharedBehaviour { cet =>
          (urlAddAnnotationType(cet), annotationTypeToJsonNoId(factory.createAnnotationType))
        }
      }

      describe("fail when adding annotation type and an invalid version") {
        updateWithInvalidVersionSharedBehaviour { cet =>
          (urlAddAnnotationType(cet), annotationTypeToJsonNoId(factory.createAnnotationType))
        }
      }

      describe("not add an annotation type on an enabled study") {
        updateOnNonDisabledStudySharedBehaviour(factory.createEnabledStudy) { cet =>
          (urlAddAnnotationType(cet), annotationTypeToJsonNoId(factory.createAnnotationType))
        }
      }

      describe("not add an annotation type on an retired study") {
        updateOnNonDisabledStudySharedBehaviour(factory.createRetiredStudy) { cet =>
          (urlAddAnnotationType(cet), annotationTypeToJsonNoId(factory.createAnnotationType))
        }
      }

      describe("fail when adding annotation type and collection event type ID is invalid") {
        updateOnInvalidCeventTypeSharedBehaviour { cet =>
          (urlAddAnnotationType(cet), annotationTypeToJsonNoId(factory.createAnnotationType))
        }
      }
    }

    describe("POST /api/studies/cetypes/annottype/:cetId/:annotationTypeId") {

      it("update an annotation type") {
        val f = new EventTypeFixture
        val cet = f.eventTypes(0)
        val annotationType = factory.createAnnotationType
        collectionEventTypeRepository.put(cet.copy(annotationTypes = Set(annotationType)))

        val updatedAnnotationType =
          annotationType.copy(description = Some(nameGenerator.next[CollectionEventType]))

        val reqJson = Json.obj("id"              -> cet.id.id,
                               "studyId"         -> cet.studyId.id,
                               "expectedVersion" -> Some(cet.version)) ++
          annotationTypeToJson(updatedAnnotationType)

        val url = urlAddAnnotationType(cet) + s"/${annotationType.id}"
        val reply = makeAuthRequest(POST, url, reqJson).value
        reply must beOkResponseWithJsonReply

        val updatedCet = cet.copy(version         = cet.version + 1,
                                  annotationTypes = Set(updatedAnnotationType),
                                  timeModified    = Some(OffsetDateTime.now))
        reply must matchUpdatedEventType(updatedCet)
      }

      describe("fail when updating annotation type and collection event type ID does not exist") {
        updateOnInvalidCeventTypeSharedBehaviour { cet =>
          val annotationType = factory.createAnnotationType
          (urlUpdateAnnotationType(cet, annotationType), annotationTypeToJson(annotationType))
        }
      }

      describe("fail when updating annotation type and an invalid version") {
        updateWithInvalidVersionSharedBehaviour { cet =>
          val annotationType = factory.createAnnotationType
          (urlUpdateAnnotationType(cet, annotationType), annotationTypeToJson(annotationType))
        }
      }

      describe("not add an annotation type on an enabled study") {
        updateOnNonDisabledStudySharedBehaviour(factory.createEnabledStudy) { cet =>
          val annotationType = factory.createAnnotationType
          (urlUpdateAnnotationType(cet, annotationType), annotationTypeToJson(annotationType))
        }
      }

      describe("not add an annotation type on an retired study") {
        updateOnNonDisabledStudySharedBehaviour(factory.createRetiredStudy) { cet =>
          val annotationType = factory.createAnnotationType
          (urlUpdateAnnotationType(cet, annotationType), annotationTypeToJson(annotationType))
        }
      }

      describe("fail when adding annotation type and collection event type ID is invalid") {
        updateOnInvalidCeventTypeSharedBehaviour { cet =>
          val annotationType = factory.createAnnotationType
          (urlUpdateAnnotationType(cet, annotationType), annotationTypeToJson(annotationType))
        }
      }
    }

    describe("DELETE /api/studies/cetypes/annottype/:id/:ver/:uniqueId") {

      it("remove an annotation type") {
        val f = new EventTypeFixture
        val cet = f.eventTypes(0)
        val annotationType = factory.createAnnotationType
        collectionEventTypeRepository.put(cet.copy(annotationTypes = Set(annotationType)))

        val url = uri("annottype", f.study.id.id, cet.id.id, cet.version.toString, annotationType.id.id)
        val reply = makeAuthRequest(DELETE, url.path).value
        reply must beOkResponseWithJsonReply
        val updatedCet = cet.copy(version         = cet.version + 1,
                                  annotationTypes = Set.empty[AnnotationType],
                                  timeModified    = Some(OffsetDateTime.now))
        reply must matchUpdatedEventType(updatedCet)
      }

      it("fail when removing annotation type and an invalid version") {
        val f = new EventTypeFixture
        val cet = f.eventTypes(0)
        val badVersion = cet.version + 1
        val annotationType = factory.createAnnotationType
        collectionEventTypeRepository.put(cet.copy(annotationTypes = Set(annotationType)))

        val url = uri("annottype", f.study.id.id, cet.id.id, badVersion.toString, annotationType.id.id)
        val reply = makeAuthRequest(DELETE, url.path).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

      it("fail when removing annotation type and study ID does not exist") {
        val studyId = nameGenerator.next[Study]
        val cetId = nameGenerator.next[CollectionEventType]

        val reply = makeAuthRequest(DELETE, uri("annottype", studyId, cetId, "0", "xyz").path).value
        reply must beNotFoundWithMessage("IdNotFound.*study")
      }

      it("fail when removing annotation type and collection event type ID does not exist") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)
        val cetId = nameGenerator.next[CollectionEventType]

        val reply = makeAuthRequest(DELETE, uri("annottype", study.id.id, cetId, "0", "xyz").path).value
        reply must beNotFoundWithMessage("IdNotFound.*collection event type")
      }

      it("fail when removing an annotation type that does not exist") {
        val f = new EventTypeFixture
        val cet = f.eventTypes(0)
        val badUniqueId = nameGenerator.next[Study]
        val annotationType = factory.createAnnotationType

        collectionEventTypeRepository.put(cet.copy(annotationTypes = Set(annotationType)))

        val url = uri("annottype", f.study.id.id, cet.id.id, cet.version.toString, badUniqueId)
        val reply = makeAuthRequest(DELETE, url.path).value
        reply must beNotFoundWithMessage("annotation type does not exist")
      }

      it("fail when removing an annotation type on a non disabled study") {
        List(factory.createEnabledStudy, factory.createRetiredStudy).foreach { study =>
          studyRepository.put(study)

          val annotationType = factory.createAnnotationType
          val cet = factory.createCollectionEventType.copy(studyId = study.id,
                                                           annotationTypes = Set(annotationType))
          collectionEventTypeRepository.put(cet)

          val url = uri("annottype", study.id.id, cet.id.id, cet.version.toString, annotationType.id.id)
          val reply = makeAuthRequest(DELETE, url.path).value
          reply must beBadRequestWithMessage("InvalidStatus: study not disabled")
        }
      }

    }

    describe("POST /api/studies/cetypes/spcdef/:id") {

      it("add a specimen spec") {
        val f = new EventTypeFixture
        val cet = f.eventTypes(0)
        val definition = factory.createCollectionSpecimenDefinition

        val reqJson = Json.obj("id"              -> cet.id.id,
                               "studyId"         -> cet.studyId.id,
                               "expectedVersion" -> Some(cet.version)) ++
        collectionSpecimenDefinitionToJsonNoId(definition)

        val reply = makeAuthRequest(POST, urlAddSepecimenDescription(cet).path, reqJson).value
        reply must beOkResponseWithJsonReply

        val newSpecimenDefinitionId =
          (contentAsJson(reply) \ "data" \ "specimenDefinitions" \ 0 \ "id").validate[SpecimenDefinitionId]
        newSpecimenDefinitionId must be (jsSuccess)

        val updatedSpecimenDefinition = definition.copy(id = newSpecimenDefinitionId.get)
        val updatedCet = cet.copy(version             = cet.version + 1,
                                  specimenDefinitions = Set(updatedSpecimenDefinition),
                                  timeModified        = Some(OffsetDateTime.now))
        reply must matchUpdatedEventType(updatedCet)
      }

      describe("fail when adding specimen spec and collection event type ID does not exist") {
        updateOnInvalidCeventTypeSharedBehaviour { cet =>
          (urlAddSepecimenDescription(cet),
           collectionSpecimenDefinitionToJsonNoId(factory.createCollectionSpecimenDefinition))
        }
      }

      describe("fail when adding specimen spec and an invalid version") {
        updateWithInvalidVersionSharedBehaviour { cet =>
          (urlAddSepecimenDescription(cet),
           collectionSpecimenDefinitionToJsonNoId(factory.createCollectionSpecimenDefinition))
        }
      }

      describe("not add an specimen spec on an enabled study") {
        updateOnNonDisabledStudySharedBehaviour(factory.createEnabledStudy) { cet =>
          (urlAddSepecimenDescription(cet),
           collectionSpecimenDefinitionToJsonNoId(factory.createCollectionSpecimenDefinition))
        }
      }

      describe("not add a specimen spec on an retired study") {
        updateOnNonDisabledStudySharedBehaviour(factory.createRetiredStudy) { cet =>
          (urlAddSepecimenDescription(cet),
           collectionSpecimenDefinitionToJsonNoId(factory.createCollectionSpecimenDefinition))
        }
      }

      describe("fail when adding specimen spec and collection event type ID is invalid") {
        updateOnInvalidCeventTypeSharedBehaviour { cet =>
          (urlAddSepecimenDescription(cet),
           collectionSpecimenDefinitionToJsonNoId(factory.createCollectionSpecimenDefinition))
        }
      }
    }

    describe("POST /api/studies/cetypes/spcdef/:cetId/:specimenDefinitionId") {

      it("update a specimen definition") {
        val f = new EventTypeFixture
        val cet = f.eventTypes(0)
        val specimenDefinition = factory.createCollectionSpecimenDefinition
        collectionEventTypeRepository.put(cet.copy(specimenDefinitions = Set(specimenDefinition)))

        val updatedSpecimenDefinition =
          specimenDefinition.copy(description = Some(nameGenerator.next[CollectionEventType]))

        val reqJson = Json.obj("id"              -> cet.id.id,
                               "studyId"         -> cet.studyId.id,
                               "expectedVersion" -> Some(cet.version)) ++
          collectionSpecimenDefinitionToJsonNoId(updatedSpecimenDefinition)

        val url = urlAddSepecimenDescription(cet) + s"/${specimenDefinition.id}"
        val reply = makeAuthRequest(POST, url, reqJson).value

        val updatedCet = cet.copy(version             = cet.version + 1,
                                  specimenDefinitions = Set(updatedSpecimenDefinition),
                                  timeModified        = Some(OffsetDateTime.now))
        reply must matchUpdatedEventType(updatedCet)
      }

      describe("fail when updating specimen definition and collection event type ID does not exist") {
        updateOnInvalidCeventTypeSharedBehaviour { cet =>
          val specimenDefinition = factory.createCollectionSpecimenDefinition
          (urlUpdateSpecimenDefinition(cet, specimenDefinition),
           collectionSpecimenDefinitionToJson(specimenDefinition))
        }
      }

      describe("fail when updating specimen description and an invalid version") {
        updateWithInvalidVersionSharedBehaviour { cet =>
          val specimenDefinition = factory.createCollectionSpecimenDefinition
          (urlUpdateSpecimenDefinition(cet, specimenDefinition),
           collectionSpecimenDefinitionToJson(specimenDefinition))
        }
      }

      describe("not add an specimen description on an enabled study") {
        updateOnNonDisabledStudySharedBehaviour(factory.createEnabledStudy) { cet =>
          val specimenDefinition = factory.createCollectionSpecimenDefinition
          (urlUpdateSpecimenDefinition(cet, specimenDefinition),
           collectionSpecimenDefinitionToJson(specimenDefinition))
        }
      }

      describe("not add an specimen description on an retired study") {
        updateOnNonDisabledStudySharedBehaviour(factory.createRetiredStudy) { cet =>
          val specimenDefinition = factory.createCollectionSpecimenDefinition
          (urlUpdateSpecimenDefinition(cet, specimenDefinition),
           collectionSpecimenDefinitionToJson(specimenDefinition))
        }
      }

      describe("fail when adding specimen description and collection event type ID is invalid") {
        updateOnInvalidCeventTypeSharedBehaviour { cet =>
          val specimenDefinition = factory.createCollectionSpecimenDefinition
          (urlUpdateSpecimenDefinition(cet, specimenDefinition),
           collectionSpecimenDefinitionToJson(specimenDefinition))
        }
      }
    }

    describe("DELETE /api/studies/cetypes/spcdef/:id/:ver/:uniqueId") {

      it("remove an specimen spec") {
        val f = new EventTypeFixture
        val cet = f.eventTypes(0)
        val specimenDefinition = factory.createCollectionSpecimenDefinition
        collectionEventTypeRepository.put(cet.copy(specimenDefinitions = Set(specimenDefinition)))

        val url = uri("spcdef", f.study.id.id, cet.id.id, cet.version.toString, specimenDefinition.id.id)
        val reply = makeAuthRequest(DELETE, url.path).value
        reply must beOkResponseWithJsonReply
        val updatedCet = cet.copy(version             = cet.version + 1,
                                  specimenDefinitions = Set.empty[CollectionSpecimenDefinition],
                                  timeModified        = Some(OffsetDateTime.now))
        reply must matchUpdatedEventType(updatedCet)
      }

      it("fail when removing specimen spec and an invalid version") {
        val f = new EventTypeFixture
        val cet = f.eventTypes(0)
        val specimenDefinition = factory.createCollectionSpecimenDefinition
        collectionEventTypeRepository.put(cet.copy(specimenDefinitions = Set(specimenDefinition)))

        val badVersion = cet.version + 1
        val url = uri("spcdef", f.study.id.id, cet.id.id, badVersion.toString, specimenDefinition.id.id)
        val reply = makeAuthRequest(DELETE, url.path).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

      it("fail when removing specimen spec and study ID does not exist") {
        val studyId = nameGenerator.next[Study]
        val cetId = nameGenerator.next[CollectionEventType]

        val reply = makeAuthRequest(DELETE, uri("spcdef", studyId, cetId, "0", "xyz").path).value
        reply must beNotFoundWithMessage("IdNotFound.*study")
      }

      it("fail when removing specimen spec and collection event type ID does not exist") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)
        val cetId = nameGenerator.next[CollectionEventType]

        val reply = makeAuthRequest(DELETE, uri("spcdef", study.id.id, cetId, "0", "xyz").path).value
        reply must beNotFoundWithMessage("IdNotFound.*collection event type")
      }

      it("fail when removing an specimen spec that does not exist") {
        val f = new EventTypeFixture
        val cet = f.eventTypes(0)
        val badUniqueId = nameGenerator.next[Study]
        val specimenDefinition = factory.createCollectionSpecimenDefinition

        collectionEventTypeRepository.put(cet.copy(specimenDefinitions = Set(specimenDefinition)))

        val url = uri("spcdef", f.study.id.id, cet.id.id, cet.version.toString, badUniqueId)
        val reply = makeAuthRequest(DELETE, url.path).value
        reply must beNotFoundWithMessage("specimen definition does not exist")
      }


      it("fail when removing an specimen spec on a non disabled study") {
        List(factory.createEnabledStudy, factory.createRetiredStudy).foreach { study =>
          studyRepository.put(study)

          val specimenDefinition = factory.createCollectionSpecimenDefinition
          val cet = factory.createCollectionEventType.copy(studyId = study.id,
                                                           specimenDefinitions = Set(specimenDefinition))
          collectionEventTypeRepository.put(cet)

          val url = uri("spcdef", study.id.id, cet.id.id, cet.version.toString, specimenDefinition.id.id)
          val reply = makeAuthRequest(DELETE, url.path).value
          reply must beBadRequestWithMessage("InvalidStatus: study not disabled")
        }
      }

    }

  }

  private def addOnNonDisabledStudySharedBehaviour(study: Study) {

    it("should be a bad request") {
      studyRepository.put(study)

      val cet = factory.createCollectionEventType.copy(
          studyId         = study.id,
          specimenDefinitions   = Set(factory.createCollectionSpecimenDefinition),
          annotationTypes = Set(factory.createAnnotationType))

      val reply = makeAuthRequest(POST, uri(study.id.id).path, cetToAddCmd(cet)).value
      reply must beBadRequestWithMessage("InvalidStatus: study not disabled")
    }
  }

  private def updateWithInvalidVersionSharedBehaviour(func: CollectionEventType => (Url, JsValue)) {

    it("should be a bad request") {
      val f = new EventTypeFixture
      val cet = f.eventTypes(0)
      var reqJson = Json.obj("id"              -> cet.id.id,
                             "studyId"         -> f.study.id,
                             "expectedVersion" -> (cet.version + 1))
      val (url, json) = func(cet)
      if (json != JsNull) {
        reqJson = reqJson ++ json.as[JsObject]
      }

      val reply = makeAuthRequest(POST, url.path, reqJson).value
      reply must beBadRequestWithMessage (".*expected version doesn't match current version.*")
    }
  }

  private def updateOnInvalidCeventTypeSharedBehaviour(func: CollectionEventType => (Url, JsValue)) {

    it("should be not found") {
      val study = factory.createDisabledStudy
      studyRepository.put(study)
      val cet = factory.createCollectionEventType
      var reqJson = Json.obj("id"             -> cet.id.id,
                             "studyId"         -> study.id,
                             "expectedVersion" -> cet.version)
      val (url, json) = func(cet)
      if (json != JsNull) {
        reqJson = reqJson ++ json.as[JsObject]
      }

      val reply = makeAuthRequest(POST, url.path, reqJson).value
      reply must beNotFoundWithMessage("IdNotFound.*collection event type")
    }
  }


  private def updateOnNonDisabledStudySharedBehaviour(study: Study)
                                                     (func: CollectionEventType => (Url, JsValue)) {
    it("show be a bad request") {
      study must not be an [DisabledStudy]
      studyRepository.put(study)

      val cet = factory.createCollectionEventType.copy(
          studyId              = study.id,
          specimenDefinitions = Set(factory.createCollectionSpecimenDefinition),
          annotationTypes      = Set(factory.createAnnotationType))
      collectionEventTypeRepository.put(cet)

      var reqJson = Json.obj("studyId"         -> study.id.id,
                             "id"              -> cet.id.id,
                             "expectedVersion" -> cet.version)
      val (url, json) = func(cet)
      if (json != JsNull) {
        reqJson = reqJson ++ json.as[JsObject]
      }

      val reply = makeAuthRequest(POST, url.path, reqJson).value
      reply must beBadRequestWithMessage("InvalidStatus: study not disabled")
    }
  }

  private def removeOnNonDisabledStudySharedBehaviour(study: Study) {

    it("should be bad request") {
      studyRepository.put(study)

      val cet = factory.createCollectionEventType.copy(
          studyId         = study.id,
          specimenDefinitions   = Set(factory.createCollectionSpecimenDefinition),
          annotationTypes = Set(factory.createAnnotationType))
      collectionEventTypeRepository.put(cet)

      val reply = makeAuthRequest(DELETE, uri(study.id.id, cet.id.id, cet.version.toString).path).value
      reply must beBadRequestWithMessage("InvalidStatus: study not disabled")
    }
  }

  private def listSingleEventType(offset:    Long = 0,
                                  maybeNext: Option[Int] = None,
                                  maybePrev: Option[Int] = None)
                                 (setupFunc: () => (Url, CollectionEventType)) = {

    it("list single collection event types") {
      val (url, expectedEventType) = setupFunc()
      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beSingleItemResults(offset, maybeNext, maybePrev)

      val replyEventTypes = (json \ "data" \ "items").validate[List[CollectionEventType]]
      replyEventTypes must be (jsSuccess)
      replyEventTypes.get.foreach { _ must matchCollectionEventType(expectedEventType) }
    }
  }

  private def listMultipleEventTypes(offset:    Long = 0,
                                     maybeNext: Option[Int] = None,
                                     maybePrev: Option[Int] = None)
                                    (setupFunc: () => (Url, List[CollectionEventType])) = {

    it("list multiple collection event types") {
      val (url, expectedEventTypes) = setupFunc()

      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beMultipleItemResults(offset = offset,
                                      total = expectedEventTypes.size.toLong,
                                      maybeNext = maybeNext,
                                      maybePrev = maybePrev)

      val replyEventTypes = (json \ "data" \ "items").validate[List[CollectionEventType]]
      replyEventTypes must be (jsSuccess)

      (replyEventTypes.get zip expectedEventTypes).foreach { case (replyEventType, eventType) =>
        replyEventType must matchCollectionEventType(eventType)
      }
    }

  }

  private def matchUpdatedEventType(eventType: CollectionEventType) =
    new Matcher[Future[Result]] {
      def apply (left: Future[Result]) = {
        val replyEventType = (contentAsJson(left) \ "data").validate[CollectionEventType]
        val jsSuccessMatcher = jsSuccess(replyEventType)

        if (!jsSuccessMatcher.matches) {
          jsSuccessMatcher
        } else {
          val replyMatcher = matchCollectionEventType(eventType)(replyEventType.get)

          if (!replyMatcher.matches) {
            MatchResult(false,
                        s"reply does not match expected: ${replyMatcher.failureMessage}",
                        s"reply matches expected: ${replyMatcher.failureMessage}")
          } else {
            matchRepositoryEventType(eventType)
          }
        }
      }
    }

  private def matchRepositoryEventType =
    new Matcher[CollectionEventType] {
      def apply (left: CollectionEventType) = {
        collectionEventTypeRepository.getByKey(left.id).fold(
          err => {
            MatchResult(false, s"not found in repository: ${err.head}", "")

          },
          repoCet => {
            val repoMatcher = matchCollectionEventType(left)(repoCet)
            MatchResult(repoMatcher.matches,
                        s"repository event type does not match expected: ${repoMatcher.failureMessage}",
                        s"repository event type matches expected: ${repoMatcher.failureMessage}")
          }
        )
      }
    }

  protected def collectionSpecimenDefinitionToJson(desc: CollectionSpecimenDefinition): JsObject = {
    Json.obj("id"                      -> desc.id,
             "slug"                    -> desc.slug,
             "name"                    -> desc.name,
             "description"             -> desc.description,
             "units"                   -> desc.units,
             "anatomicalSourceType"    -> desc.anatomicalSourceType.toString,
             "preservationType"        -> desc.preservationType.toString,
             "preservationTemperature" -> desc.preservationTemperature.toString,
             "specimenType"            -> desc.specimenType.toString,
             "maxCount"                -> desc.maxCount,
             "amount"                  -> desc.amount)
  }

  protected def collectionSpecimenDefinitionToJsonNoId(spec: CollectionSpecimenDefinition): JsObject = {
    collectionSpecimenDefinitionToJson(spec) - "id"
  }

}
