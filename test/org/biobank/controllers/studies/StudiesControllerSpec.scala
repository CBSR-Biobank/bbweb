package org.biobank.controllers.studies

import java.time.OffsetDateTime
import org.biobank.controllers.PagedResultsSharedSpec
import org.biobank.domain._
import org.biobank.domain.annotations._
import org.biobank.domain.{JsonHelper, Slug}
import org.biobank.domain.studies._
import org.biobank.dto.NameAndStateDto
import org.biobank.fixtures.{ControllerFixture, Url}
import org.biobank.matchers.PagedResultsMatchers
import org.biobank.services.centres.CentreLocation
import org.biobank.services.studies.StudyCountsByStatus
import org.scalatest.prop.TableDrivenPropertyChecks._
import play.api.libs.json._
import play.api.test.Helpers._
import scala.language.reflectiveCalls

/**
 * Tests the REST API for [[domain.studies.Study Study]].
 */
class StudiesControllerSpec
    extends ControllerFixture
    with JsonHelper
    with PagedResultsSharedSpec
    with PagedResultsMatchers {

  import org.biobank.TestUtils._
  import org.biobank.matchers.JsonMatchers._
  import org.biobank.matchers.EntityMatchers._

  class CollectionFixture {
    val study = factory.createEnabledStudy
    val specimenDefinition = factory.createCollectionSpecimenDefinition
    val ceventType = factory.createCollectionEventType.copy(studyId               = study.id,
                                                            specimenDefinitions = Set(specimenDefinition),
                                                            annotationTypes      = Set.empty)
    val participant = factory.createParticipant.copy(studyId = study.id)
    val cevent = factory.createCollectionEvent
    val centre = factory.createEnabledCentre.copy(studyIds  = Set(study.id),
                                                  locations = Set(factory.createLocation))

    Set(centre, study, ceventType, participant, cevent).foreach(addToRepository)
  }

  private def uri(paths: String*): String = {
    val basePath = "/api/studies"
    if (paths.isEmpty) basePath
    else basePath + "/" + paths.mkString("/")
  }

  private def urlName(study: Study)        = uri("name", study.id.id)
  private def urlDescription(study: Study) = uri("description", study.id.id)
  private def urlDisable(study: Study)     = uri("disable", study.id.id)
  private def urlEnable(study: Study)      = uri("enable", study.id.id)
  private def urlRetire(study: Study)      = uri("retire", study.id.id)
  private def urlUnretire(study: Study)    = uri("unretire", study.id.id)

  private def urlAddAnnotationType(study: Study) = uri("pannottype", study.id.id)

  private def urlUpdateAnnotationType(annotType: AnnotationType) =
    (study: Study) => urlAddAnnotationType(study) + s"/${annotType.id}"

  describe("Study REST API") {

    describe("GET /api/studies/collectionStudies") {

      it("returns the studies that a user can collect specimens for") {
        val f = new CollectionFixture
        val reply = makeAuthRequest(GET, uri("collectionStudies")).value
        reply must beOkResponseWithJsonReply

        val json = contentAsJson(reply)
        val dtos = (json \ "data").validate[List[NameAndStateDto]]
        dtos must be (jsSuccess)
        dtos.get must have size 1
        dtos.get(0) must equal (NameAndStateDto(f.study.id.id,
                                                f.study.slug,
                                                f.study.name,
                                                f.study.state.id))
      }

      it("when study disabled, returns zero studies") {
        val f = new CollectionFixture
        f.study.disable.map(addToRepository)

        val reply = makeAuthRequest(GET, uri("collectionStudies")).value
        reply must beOkResponseWithJsonReply

        val json = contentAsJson(reply)
        val dtos = (json \ "data").validate[List[NameAndStateDto]]
        dtos must be (jsSuccess)
        dtos.get must have size 0
      }

      it("when centre disabled, returns zero studies") {
        val f = new CollectionFixture
        f.centre.disable.map(addToRepository)

        val reply = makeAuthRequest(GET, uri("collectionStudies")).value
        reply must beOkResponseWithJsonReply

        val json = contentAsJson(reply)
        val dtos = (json \ "data").validate[List[NameAndStateDto]]
        dtos must be (jsSuccess)
        dtos.get must have size 0
      }

    }

    describe("GET /api/studies/counts") {

      it("return empty counts") {
        val reply = makeAuthRequest(GET, uri("counts")).value
        reply must beOkResponseWithJsonReply
        val json = contentAsJson(reply)
        val counts = (json \ "data").validate[StudyCountsByStatus]
        counts must be (jsSuccess)
        counts.get must equal (StudyCountsByStatus(0, 0, 0, 0))
      }

      it("return valid counts") {
        val studies = List(factory.createDisabledStudy,
                           factory.createDisabledStudy,
                           factory.createDisabledStudy,
                           factory.createEnabledStudy,
                           factory.createEnabledStudy,
                           factory.createRetiredStudy)

        studies.foreach(studyRepository.put)

        val reply = makeAuthRequest(GET, uri("counts")).value
        reply must beOkResponseWithJsonReply
        val json = contentAsJson(reply)
        val counts = (json \ "data").validate[StudyCountsByStatus]
        counts must be (jsSuccess)
        counts.get must equal (StudyCountsByStatus(6, 3, 2, 1))
      }

    }

    describe("GET /api/studies/search") {

      it("list none") {
        val url = new Url(uri("search"))
        url must beEmptyResults
      }

      describe("list a study") {

        listSingleStudy() { () =>
          val study = factory.createDisabledStudy
          studyRepository.put(study)

          (new Url(uri("search")), study)
        }

      }

      describe("list multiple studies") {

        listMultipleStudies() { () =>
          val studies = List(factory.createDisabledStudy,
                             factory.createDisabledStudy)
          studies.foreach(studyRepository.put)
          (new Url(uri("search")), studies.sortWith(_.name < _.name))
        }

      }

      describe("list a single study when filtered by name") {

        listSingleStudy() { () =>
          val studies = List(factory.createDisabledStudy, factory.createEnabledStudy)
          studies.foreach(studyRepository.put)
          (new Url(uri("search") + s"?filter=name::${studies(0).name}"), studies(0))
        }

      }

      describe("list a single disabled study when filtered by status") {

        listSingleStudy() { () =>
          val studies = List(factory.createDisabledStudy,
                             factory.createEnabledStudy,
                           factory.createRetiredStudy)
          studies.foreach(studyRepository.put)
          (new Url(uri("search") + s"?filter=state::disabled"), studies(0))
        }

      }

      describe("list studies when filtered by state") {

        def commonSetup = {
          val studies = List(factory.createDisabledStudy,
                             factory.createDisabledStudy,
                             factory.createEnabledStudy,
                             factory.createEnabledStudy)
          studies.foreach(studyRepository.put)
          studies
        }

        describe("for disabled") {

          listMultipleStudies() { () =>
            (new Url(uri("search") + s"?filter=state::disabled"),
             commonSetup.filter { c => c.state == Study.disabledState  }.sortWith(_.name < _.name))
          }

        }

        describe("for enabled") {

          listMultipleStudies() { () =>
            (new Url(uri("search") + s"?filter=state::enabled"),
             commonSetup.filter { c => c.state == Study.enabledState  }.sortWith(_.name < _.name))
          }

        }

        it("fail with an invalid state name") {
          val invalidStateName = "state::" + nameGenerator.next[Study]
          val reply = makeAuthRequest(GET, uri("search") + s"?filter=$invalidStateName").value
          reply must beNotFoundWithMessage ("InvalidState: entity state does not exist")
        }

      }

      describe("list studies sorted by name") {

        def commonSetup = {
          val studies = List(factory.createDisabledStudy.copy(name = "CTR3"),
                             factory.createDisabledStudy.copy(name = "CTR2"),
                             factory.createEnabledStudy.copy(name = "CTR1"),
                             factory.createEnabledStudy.copy(name = "CTR0"))
          studies.foreach(studyRepository.put)
          studies
        }

        describe("in ascending order") {

          listMultipleStudies() { () =>
            (new Url(uri("search") + s"?sort=name"), commonSetup.sortWith(_.name < _.name))
          }

        }

        describe("in decending order") {

          listMultipleStudies() { () =>
            (new Url(uri("search") + s"?sort=-name"), commonSetup.sortWith(_.name > _.name))
          }
        }

      }

      describe("list studies sorted by state") {

        def commonSetup = {
          val studies = List(factory.createDisabledStudy, factory.createEnabledStudy)
          studies.foreach(studyRepository.put)
          studies
        }

        describe("in ascending order") {

          listMultipleStudies() { () =>
            (new Url(uri("search") + s"?sort=state"), commonSetup.sortWith(_.state.id < _.state.id))
          }

        }

        describe("in decending order") {

          listMultipleStudies() { () =>
            (new Url(uri("search") + s"?sort=-state"), commonSetup.sortWith(_.state.id > _.state.id))
          }
        }

        it("fail with an invalid state name") {
          val invalidStateName = nameGenerator.next[Study]
          val reply = makeAuthRequest(GET, uri("search") + s"?sort=$invalidStateName").value
          reply must beBadRequestWithMessage ("could not parse sort expression")
        }

      }

      describe("list a single study when using paged query") {

        def commonSetup = {
          val studies = List(factory.createDisabledStudy.copy(name = "CTR3"),
                             factory.createDisabledStudy.copy(name = "CTR2"),
                             factory.createEnabledStudy.copy(name = "CTR1"),
                             factory.createEnabledStudy.copy(name = "CTR0"))
          studies.foreach(studyRepository.put)
          studies
        }

        describe("fist page") {

          listSingleStudy(maybeNext = Some(2)) { () =>
            (new Url(uri("search") + s"?sort=name&limit=1"), commonSetup(3))
          }

        }

        describe("last page") {

          listSingleStudy(offset = 3, maybePrev = Some(3)) { () =>
            (new Url(uri("search") + s"?sort=name&page=4&limit=1"), commonSetup(0))
          }

        }

      }

      describe("fail when using an invalid query parameters") {

        pagedQueryShouldFailSharedBehaviour(() => new Url(uri("search")))

      }

    }

    describe("GET /api/studies/:slug") {

      it("read a study by slug") {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val reply = makeAuthRequest(GET, uri(study.slug.id)).value
        reply must beOkResponseWithJsonReply

        val json = contentAsJson(reply)
        json must containValue((JsPath \ "data" ), Json.toJson(study))
      }

      it("fails for an invalid study ID") {
        val study = factory.createEnabledStudy
        val reply = makeAuthRequest(GET, uri(study.slug.id)).value
        reply must beNotFoundWithMessage ("EntityCriteriaNotFound: study slug")
      }

    }

    describe("POST /api/studies") {

      it("add a study") {
        val study = factory.createDisabledStudy
        val reqJson = Json.obj("name" -> study.name,
                               "description" -> study.description)

        val reply = makeAuthRequest(POST, uri(""), reqJson).value
        reply must beOkResponseWithJsonReply

        val replyStudy = (contentAsJson(reply) \ "data").validate[Study]
        replyStudy must be (jsSuccess)

        studyRepository.getByKey(replyStudy.get.id) mustSucceed { repoStudy =>
          val updatedStudy = study.copy(id = replyStudy.get.id)
          replyStudy.get must matchStudy (updatedStudy)
          repoStudy must matchStudy (updatedStudy)
        }
      }

      it("not add add a study with a duplicate name") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val resp = makeAuthRequest(POST, uri(""), Json.obj("name" -> study.name,
                                                           "description" -> study.description))
        resp.value must beBadRequestWithMessage("EntityCriteriaError.*name already used")
      }

      it("not add add a new study an empty name") {
        val reply = makeAuthRequest(POST, uri(""), Json.obj("name" -> ""))
        reply.value must beBadRequestWithMessage ("InvalidName")
      }

    }

    describe("POST /api/studies/name/:id") {

      it("update a study's name") {
        val newName = nameGenerator.next[Study]
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val reqJson = Json.obj("expectedVersion" -> Some(study.version),
                               "name"            -> newName)
        val reply = makeAuthRequest(POST, uri("name", study.id.id), reqJson).value
        reply must beOkResponseWithJsonReply

        val replyStudy = (contentAsJson(reply) \ "data").validate[Study]
        replyStudy must be (jsSuccess)

        val updatedStudy = study.copy(version      = study.version + 1,
                                      name         = newName,
                                      slug         = Slug(newName),
                                      timeModified = Some(OffsetDateTime.now))
        replyStudy.get must matchStudy (updatedStudy)
        studyRepository.getByKey(study.id) mustSucceed { repoStudy =>
          repoStudy must matchStudy (updatedStudy)
        }
      }

      it("not update a study with a duplicate name") {
        val studies = (1 to 2).map { _ =>
            val study = factory.createDisabledStudy
            studyRepository.put(study)
            study
          }

        val reply = makeAuthRequest(POST,
                                    uri("name", studies(0).id.id),
                                    Json.obj("expectedVersion" -> Some(studies(0).version),
                                             "name"            -> studies(1).name))
        reply.value must beBadRequestWithMessage("EntityCriteriaError.*name already used")
      }

      it("fail when updating a study's name to something with less than 2 characters") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val reply = makeAuthRequest(POST,
                                   uri("name", study.id.id),
                                   Json.obj("expectedVersion" -> Some(study.version),
                                            "name"            -> "a"))
        reply.value must beBadRequestWithMessage("InvalidName")
      }

      describe("fail when updating name and study ID does not exist") {
        checkInvalidStudyIdSharedBehaviour(Json.obj("name" -> nameGenerator.next[Study]), urlName)
      }

      describe("fail when updating name with invalid version") {

        updateWithInvalidVersionSharedBehaviour(urlName,
                                                Json.obj("name" -> nameGenerator.next[Study]))

      }

      describe("fail when updating name on a non disabled study") {

        updateNonDisabledStudySharedBehaviour(Json.obj("name" -> nameGenerator.next[Study]),
                                              urlName)
      }

    }

    describe("POST /api/studies/description/:id") {

      it("update a study's description") {
        val newDescription = Some(nameGenerator.next[Study])
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val reqJson = Json.obj("expectedVersion" -> study.version,
                               "description"     -> newDescription)
        val reply = makeAuthRequest(POST, uri("description", study.id.id), reqJson).value
        reply must beOkResponseWithJsonReply

        val replyStudy = (contentAsJson(reply) \ "data").validate[Study]
        replyStudy must be (jsSuccess)

        val updatedStudy = study.copy(version      = study.version + 1,
                                      description  = newDescription,
                                      timeModified = Some(OffsetDateTime.now))
        replyStudy.get must matchStudy (updatedStudy)
        studyRepository.getByKey(study.id) mustSucceed { repoStudy =>
          repoStudy must matchStudy (updatedStudy)
        }
      }

      describe("fail when updating description and study ID does not exist") {
        checkInvalidStudyIdSharedBehaviour(Json.obj("description" -> nameGenerator.next[Study]),
                                           urlDescription)
      }

      describe("fail when updating description with invalid version") {

        updateWithInvalidVersionSharedBehaviour(urlDescription,
                                                Json.obj("description" -> nameGenerator.next[Study]))

      }

      describe("fail when updating description on a non disabled study") {

        updateNonDisabledStudySharedBehaviour(Json.obj("description" -> nameGenerator.next[Study]),
                                              urlDescription)

      }

    }

    describe("POST /api/studies/pannottype/:id") {

      it("add a participant annotation type") {
        val annotType = factory.createAnnotationType
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val reqJson = Json.obj(
            "id"              -> study.id.id,
            "expectedVersion" -> Some(study.version),
            "name"            -> annotType.name,
            "description"     -> annotType.description,
            "valueType"       -> annotType.valueType,
            "options"         -> annotType.options,
            "required"        -> annotType.required)

        val reply = makeAuthRequest(POST, uri("pannottype", study.id.id), reqJson).value
        reply must beOkResponseWithJsonReply

        val replyStudy = (contentAsJson(reply) \ "data").validate[Study]
        replyStudy must be (jsSuccess)

        val updatedAnnotationType = annotType.copy(id = replyStudy.get.annotationTypes.head.id)
        val updatedStudy = study.copy(version         = study.version + 1,
                                      annotationTypes = Set(updatedAnnotationType),
                                      timeModified    = Some(OffsetDateTime.now))
        replyStudy.get must matchStudy (updatedStudy)
        studyRepository.getByKey(study.id) mustSucceed { repoStudy =>
          repoStudy must matchStudy (updatedStudy)
        }
      }

      describe("fail when adding annotation type and study ID does not exist") {
        checkInvalidStudyIdSharedBehaviour(annotationTypeToJsonNoId(factory.createAnnotationType),
                                           urlAddAnnotationType)
      }

      describe("fail when adding annotation type and an invalid version") {

        updateWithInvalidVersionSharedBehaviour(urlAddAnnotationType,
                                                annotationTypeToJsonNoId(factory.createAnnotationType))

      }

      describe("fail when adding an annotation type on a non disabled study") {

        updateNonDisabledStudySharedBehaviour(annotationTypeToJsonNoId(factory.createAnnotationType),
                                              urlAddAnnotationType)
      }

    }

    describe("POST /api/studies/pannottype/:id/:annotTypeId") {

      it("update a participant annotation type") {
        val annotType = factory.createAnnotationType
        val newName = nameGenerator.next[Study]
        val study = factory.createDisabledStudy.copy(annotationTypes = Set(annotType))
        studyRepository.put(study)

        val reqJson = Json.obj(
            "id"               -> study.id.id,
            "expectedVersion"  -> Some(study.version),
            "annotationTypeId" -> annotType.id,
            "name"             -> newName,
            "description"      -> annotType.description,
            "valueType"        -> annotType.valueType,
            "options"          -> annotType.options,
            "required"         -> annotType.required)

        val reply = makeAuthRequest(POST, uri("pannottype", study.id.id, annotType.id.id), reqJson).value
        reply must beOkResponseWithJsonReply

        val replyStudy = (contentAsJson(reply) \ "data").validate[Study]
        replyStudy must be (jsSuccess)

        val updatedAnnotationType = annotType.copy(slug = Slug(newName),
                                                   name = newName)
        val updatedStudy = study.copy(version         = study.version + 1,
                                      annotationTypes = Set(updatedAnnotationType),
                                      timeModified    = Some(OffsetDateTime.now))
        replyStudy.get must matchStudy (updatedStudy)
        studyRepository.getByKey(study.id) mustSucceed { repoStudy =>
          repoStudy must matchStudy (updatedStudy)
        }
      }

      describe("fail when updating annotation type and study ID does not exist") {
        val annotType = factory.createAnnotationType
        checkInvalidStudyIdSharedBehaviour(annotationTypeToJson(annotType),
                                           urlUpdateAnnotationType(annotType))
      }

      describe("fail when updating annotation type and an invalid version") {

        val annotType = factory.createAnnotationType

        updateWithInvalidVersionSharedBehaviour(urlUpdateAnnotationType(annotType),
                                                annotationTypeToJson(annotType))

      }

      describe("fail when updating an annotation type on a non disabled study") {
        val annotType = factory.createAnnotationType

        updateNonDisabledStudySharedBehaviour(annotationTypeToJson(annotType),
                                              urlUpdateAnnotationType(annotType))
      }

    }

    describe("DELETE /api/studies/pannottype/:id/:ver/:uniqueId") {

      it("remove a participant annotation type") {
        val annotationType = factory.createAnnotationType
        val study = factory.createDisabledStudy.copy(annotationTypes = Set(annotationType))
        studyRepository.put(study)

        val url = uri("pannottype", study.id.id, s"${study.version}/${annotationType.id}")
        val reply = makeAuthRequest(DELETE, url).value
        reply must beOkResponseWithJsonReply

        val replyStudy = (contentAsJson(reply) \ "data").validate[Study]
        replyStudy must be (jsSuccess)

        val updatedStudy = study.copy(version         = study.version + 1,
                                      annotationTypes = Set.empty[AnnotationType],
                                      timeModified    = Some(OffsetDateTime.now))
        replyStudy.get must matchStudy (updatedStudy)
        studyRepository.getByKey(replyStudy.get.id) mustSucceed { repoStudy =>
          repoStudy must matchStudy (updatedStudy)
        }
      }

      it("fail when removing annotation type and an invalid version") {
        val annotationType = factory.createAnnotationType
        val study = factory.createDisabledStudy.copy(annotationTypes = Set(annotationType))
        val badVersion = study.version + 1
        studyRepository.put(study)

        val url = uri("pannottype", study.id.id, s"${badVersion}/${annotationType.id}")
        val reply = makeAuthRequest(DELETE, url).value
        reply must beBadRequestWithMessage("expected version doesn't match current version")
      }

      it("fail when removing annotation type and study ID does not exist") {
        val studyId = nameGenerator.next[Study]
        val reply = makeAuthRequest(DELETE, uri(s"pannottype/$studyId/0/xyz"))
        reply.value must beNotFoundWithMessage("IdNotFound.*study")
      }

      it("fail when removing an annotation type that does not exist") {
        val badUniqueId = nameGenerator.next[Study]
        val annotationType = factory.createAnnotationType
        val study = factory.createDisabledStudy.copy(annotationTypes = Set(annotationType))
        studyRepository.put(study)

        val reply = makeAuthRequest(DELETE,
                                    uri("pannottype", study.id.id, s"${study.version}/$badUniqueId"))
        reply.value must beNotFoundWithMessage("annotation type does not exist")
      }

      it("fail when removing an annotation type on a non disabled study") {
        val annotationType = factory.createAnnotationType
        val enabledStudy = factory.createEnabledStudy.copy(annotationTypes = Set(annotationType))
        val retiredStudy = factory.createRetiredStudy.copy(annotationTypes = Set(annotationType))

        forAll(Table("studies", enabledStudy, retiredStudy)) { study =>
          studyRepository.put(study)

          val reply = makeAuthRequest(
              DELETE,
              uri("pannottype", study.id.id, s"${study.version}/${annotationType.id}"))

          reply.value must beBadRequestWithMessage("InvalidStatus: study not disabled")
        }
      }

    }

    describe("POST /api/studies/enable/:id") {

      it("enable a study") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType.copy(
            studyId              = study.id,
            specimenDefinitions = Set(factory.createCollectionSpecimenDefinition))
        collectionEventTypeRepository.put(cet)

        val reqJson = Json.obj("expectedVersion" -> Some(study.version))
        val reply = makeAuthRequest(POST, uri("enable", study.id.id), reqJson).value
        reply must beOkResponseWithJsonReply

        val replyStudy = (contentAsJson(reply) \ "data").validate[Study]
        replyStudy must be (jsSuccess)

        study.enable.mustSucceed { updatedStudy =>
          replyStudy.get must matchStudy (updatedStudy)
          studyRepository.getByKey(replyStudy.get.id) mustSucceed { repoStudy =>
            repoStudy must matchStudy (updatedStudy)
          }
        }
      }

      it("not enable a study when it has no specimen groups") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType
        collectionEventTypeRepository.put(cet)

        val reqJson = Json.obj("expectedVersion" -> Some(study.version))
        val reply = makeAuthRequest(POST, uri("enable", study.id.id), reqJson)
        reply.value must beBadRequestWithMessage("no collection specimen specs")
      }

      it("not enable a study when it has no collection event types") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val reqJson = Json.obj("expectedVersion" -> Some(study.version))
        val reply = makeAuthRequest(POST, uri("enable", study.id.id), reqJson)
        reply.value must beBadRequestWithMessage("no collection event types")
      }

      it("not enable an already enabled study") {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val updateJson = Json.obj("expectedVersion" -> Some(study.version))
        val reply = makeAuthRequest(POST, uri("enable", study.id.id), updateJson).value
        reply must beBadRequestWithMessage("InvalidStatus: study not disabled")
      }

      it("fail when enabling a study and the study ID is invalid") {
        val study = factory.createDisabledStudy
        val reqJson = Json.obj("expectedVersion" -> Some(study.version))

        val reply = makeAuthRequest(POST, uri("enable", study.id.id), reqJson)
        reply.value must beNotFoundWithMessage("IdNotFound.*study")
      }

      describe("fail when enabling and study ID does not exit") {

        checkInvalidStudyIdSharedBehaviour(JsNull, urlEnable)
      }

      describe("fail when enabling a study and the version is invalid") {

        updateWithInvalidVersionSharedBehaviour(urlEnable)

      }
    }

    describe("POST /api/studies/disable/:id") {

      it("disable a study") {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val reqJson = Json.obj("expectedVersion" -> Some(study.version))
        val reply = makeAuthRequest(POST, uri("disable", study.id.id), reqJson).value
        reply must beOkResponseWithJsonReply

        val replyStudy = (contentAsJson(reply) \ "data").validate[Study]
        replyStudy must be (jsSuccess)

        study.disable mustSucceed { updatedStudy =>
          replyStudy.get must matchStudy (updatedStudy)
          studyRepository.getByKey(replyStudy.get.id) mustSucceed { repoStudy =>
            repoStudy must matchStudy (updatedStudy)
          }
        }
      }

      it("not disable an already disabled study") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val updateJson = Json.obj("expectedVersion" -> Some(study.version))
        val reply = makeAuthRequest(POST, uri("disable", study.id.id), updateJson).value
        reply must beBadRequestWithMessage("InvalidStatus: study not enabled")
      }

      describe("fail when disabling and study ID does not exit") {
        checkInvalidStudyIdSharedBehaviour(JsNull, urlDisable)
      }

      describe("fail when disabling a study and the version is invalid") {
        updateWithInvalidVersionSharedBehaviour(urlDisable)
      }
    }

    describe("POST /api/studies/retire/:id") {

      it("retire a study") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val reqJson = Json.obj("expectedVersion" -> Some(study.version))

        val reply = makeAuthRequest(POST, uri("retire", study.id.id), reqJson).value
        reply must beOkResponseWithJsonReply

        val replyStudy = (contentAsJson(reply) \ "data").validate[Study]
        replyStudy must be (jsSuccess)

        study.retire mustSucceed { updatedStudy =>
          replyStudy.get must matchStudy (updatedStudy)
          studyRepository.getByKey(replyStudy.get.id) mustSucceed { repoStudy =>
            repoStudy must matchStudy (updatedStudy)
          }
        }
      }

      it("not retire an already retired study") {
        val study = factory.createRetiredStudy
        studyRepository.put(study)

        val updateJson = Json.obj("expectedVersion" -> Some(study.version))
        val reply = makeAuthRequest(POST, uri("retire", study.id.id), updateJson).value
        reply must beBadRequestWithMessage("InvalidStatus: study not disabled")
      }

      describe("fail when retiring and study ID does not exit") {
        checkInvalidStudyIdSharedBehaviour(JsNull, urlRetire)
      }

      describe("fail when retiring a study and the version is invalid") {
        updateWithInvalidVersionSharedBehaviour(urlRetire)
      }
    }

    describe("POST /api/studies/unretire/:id") {

      it("unretire a study") {
        val study = factory.createRetiredStudy
        studyRepository.put(study)

        val reqJson = Json.obj("expectedVersion" -> Some(study.version))
        val reply = makeAuthRequest(POST, uri("unretire", study.id.id), reqJson).value
        reply must beOkResponseWithJsonReply

        val replyStudy = (contentAsJson(reply) \ "data").validate[Study]
        replyStudy must be (jsSuccess)

        study.unretire mustSucceed { updatedStudy =>
          replyStudy.get must matchStudy (updatedStudy)
          studyRepository.getByKey(replyStudy.get.id) mustSucceed { repoStudy =>
            repoStudy must matchStudy (updatedStudy)
          }
        }
      }

      it("not unretire an already disabled study") {
        val studyTable = Table("study", factory.createDisabledStudy, factory.createEnabledStudy)

        forAll(studyTable) { study =>
          studyRepository.put(study)
          val updateJson = Json.obj("expectedVersion" -> Some(study.version))
          val reply = makeAuthRequest(POST, uri("unretire", study.id.id), updateJson).value
          reply must beBadRequestWithMessage("InvalidStatus: study not retired")
        }
      }

      describe("fail when unretiring and study ID does not exit") {
        checkInvalidStudyIdSharedBehaviour(JsNull, urlUnretire)
      }

      describe("fail when unretiring a study and the version is invalid") {
        updateWithInvalidVersionSharedBehaviour(urlUnretire)
      }
    }

    describe("GET /api/studies/valuetypes") {

      it("list all value types") {
        val reply = makeAuthRequest(GET, uri("valuetypes")).value
        reply must beOkResponseWithJsonReply
        val values = (contentAsJson(reply) \ "data").validate[List[String]]
        values must be (jsSuccess)
        values.get.size must be > 0
        values.get.foreach { value =>
          AnnotationValueType.withName(value)
        }
      }
    }

    describe("GET /api/studies/anatomicalsrctypes") {
      it("list all anatomical source types") {
        val reply = makeAuthRequest(GET, uri("anatomicalsrctypes")).value
        reply must beOkResponseWithJsonReply
        val values = (contentAsJson(reply) \ "data").validate[List[String]]
        values must be (jsSuccess)
        values.get.size must be > 0
        values.get.foreach { value =>
          AnatomicalSourceType.withName(value)
        }
      }
    }

    describe("GET /api/studies/specimentypes") {
      it("list all specimen types") {
        val reply = makeAuthRequest(GET, uri("specimentypes")).value
        reply must beOkResponseWithJsonReply
        val values = (contentAsJson(reply) \ "data").validate[List[String]]
        values must be (jsSuccess)
        values.get.size must be > 0
        values.get.foreach { value =>
          SpecimenType.withName(value)
        }
      }
    }

    describe("GET /api/studies/preservtypes") {
      it("list all preservation types") {
        val reply = makeAuthRequest(GET, uri("preservtypes")).value
        reply must beOkResponseWithJsonReply
        val values = (contentAsJson(reply) \ "data").validate[List[String]]
        values must be (jsSuccess)
        values.get.size must be > 0
        values.get.foreach { value =>
          PreservationType.withName(value)
        }
      }
    }

    describe("GET /api/studies/preservtemptypes ") {
      it("list all preservation temperatures") {
        val reply = makeAuthRequest(GET, uri("preservtemptypes")).value
        reply must beOkResponseWithJsonReply
        val values = (contentAsJson(reply) \ "data").validate[List[String]]
        values must be (jsSuccess)
        values.get.size must be > 0
        values.get.foreach { value =>
          PreservationTemperature.withName(value)
        }
      }
    }

    describe("GET /api/studies/sgvaluetypes ") {
      it("list all specimen ") {
        val reply = makeAuthRequest(GET, uri("sgvaluetypes")).value
        reply must beOkResponseWithJsonReply
        val json = contentAsJson(reply)

        (json \ "data" \ "anatomicalSourceType").as[List[String]].size    must be > 0

        (json \ "data" \ "preservationType").as[List[String]].size        must be > 0

        (json \ "data" \ "preservationTemperature").as[List[String]].size must be > 0

        (json \ "data" \ "specimenType").as[List[String]].size            must be > 0
      }
    }

    describe("GET /api/studies/names") {

      def fixture = {
        val _studies = List(factory.createDisabledStudy.copy(name = "ABC"),
                            factory.createDisabledStudy.copy(name = "DEF"))

        studyRepository.removeAll
        _studies.foreach(studyRepository.put)

        new {
          val studies = _studies
        }
      }

      it("list multiple study names in ascending order") {
        val f = fixture
        val reply = makeAuthRequest(GET, uri("names") + "?order=asc").value
        reply must beOkResponseWithJsonReply
        val dtos = (contentAsJson(reply) \ "data" ).validate[Seq[NameAndStateDto]]
        dtos must be (jsSuccess)
        dtos.get must equal (Seq(NameAndStateDto(f.studies(0)), NameAndStateDto(f.studies(1))))
      }

      it("list single study when using a filter") {
        val f = fixture
        val reply = makeAuthRequest(GET, uri("names") + "?filter=name::ABC").value
        reply must beOkResponseWithJsonReply
        val dtos = (contentAsJson(reply) \ "data" ).validate[Seq[NameAndStateDto]]
        dtos must be (jsSuccess)
        dtos.get must equal (Seq(NameAndStateDto(f.studies(0))))
      }

      it("list nothing when using a name filter for name not in system") {
        fixture // create studies to populate repository
        val reply = makeAuthRequest(GET, uri("names") + "?filter=name::xxx").value
        reply must beOkResponseWithJsonReply
        val dtos = (contentAsJson(reply) \ "data" ).validate[Seq[NameAndStateDto]]
        dtos must be (jsSuccess)
        dtos.get must equal (Seq.empty[NameAndStateDto])
      }

      it("fail for invalid sort field") {
        val reply = makeAuthRequest(GET, uri("names") + "?sort=xxxx").value
        reply must beBadRequestWithMessage("invalid sort field")
      }

    }

    describe("GET /api/studies/centres/:id") {

      it("list the centres associated with a study") {
        val study = factory.createEnabledStudy
        val location = factory.createLocation
        val centre = factory.createEnabledCentre.copy(studyIds = Set(study.id),
                                                      locations = Set(location))

        Set(study, centre).foreach(addToRepository)

        val reply = makeAuthRequest(GET, uri("centres") + s"/${study.id}").value
        reply must beOkResponseWithJsonReply

        val dtos = (contentAsJson(reply) \ "data" ).validate[Seq[CentreLocation]]
        dtos must be (jsSuccess)
        dtos.get must equal (Seq(CentreLocation(centre, location)))
      }

    }

  }

  private def updateWithInvalidVersionSharedBehaviour(urlFunc: Study => String, json: JsValue = JsNull) {

    it("should return bad request") {
      val study = factory.createDisabledStudy
      studyRepository.put(study)
      var requestJson = Json.obj("expectedVersion" -> Json.toJson(Some(study.version + 1)))
      if (json != JsNull) {
        requestJson = requestJson ++ json.as[JsObject]
      }
      val reply = makeAuthRequest(POST, urlFunc(study), requestJson)
      reply.value must beBadRequestWithMessage("InvalidVersion")
    }

  }

  private def updateNonDisabledStudySharedBehaviour(jsonField: JsObject,
                                                    urlFunc:   Study => String): Unit = {
    it("should return bad request") {

      val studiesTable = Table(("study", "state"),
                               (factory.createEnabledStudy, "enabled"),
                               (factory.createRetiredStudy, "retored"))

      forAll (studiesTable) { (study, label) =>
        info(label)
        studyRepository.put(study)
        val requestJson = Json.obj("expectedVersion" -> Some(study.version)) ++ jsonField
        val reply = makeAuthRequest(POST, urlFunc(study), requestJson)
        reply.value must beBadRequestWithMessage ("InvalidStatus: study not disabled")
      }
    }

  }

  private def checkInvalidStudyIdSharedBehaviour(json: JsValue, urlFunc: Study => String): Unit = {

    it("should return not found") {
      val invalidStudy = factory.createDisabledStudy
      var reqJson = Json.obj("expectedVersion" -> 0L)
      if (json != JsNull) {
        reqJson = reqJson ++ json.as[JsObject]
      }
      val reply = makeAuthRequest(POST, urlFunc(invalidStudy), reqJson).value
      reply must beNotFoundWithMessage ("IdNotFound.*study")
    }
  }

  private def listSingleStudy(offset:    Long = 0,
                               maybeNext: Option[Int] = None,
                               maybePrev: Option[Int] = None)
                              (setupFunc: () => (Url, Study)) = {

    it("list single study") {
      val (url, expectedStudy) = setupFunc()
      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beSingleItemResults(offset, maybeNext, maybePrev)

      val replyStudies = (json \ "data" \ "items").validate[List[Study]]
      replyStudies must be (jsSuccess)
      replyStudies.get.foreach { _ must matchStudy(expectedStudy) }
    }
  }

  private def listMultipleStudies(offset:    Long = 0,
                                  maybeNext: Option[Int] = None,
                                  maybePrev: Option[Int] = None)
                                 (setupFunc: () => (Url, List[Study])) = {

    it("list multiple studies") {
      val (url, expectedStudies) = setupFunc()

      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beMultipleItemResults(offset = offset,
                                      total = expectedStudies.size.toLong,
                                      maybeNext = maybeNext,
                                      maybePrev = maybePrev)

      val replyStudies = (json \ "data" \ "items").validate[List[Study]]
      replyStudies must be (jsSuccess)

      (replyStudies.get zip expectedStudies).foreach { case (replyStudy, study) =>
        replyStudy must matchStudy(study)
      }
    }

  }

}
