package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.domain.{AnnotationType, JsonHelper}
import org.biobank.domain.study._
import org.biobank.dto._
import org.biobank.fixture.ControllerFixture
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.test.Helpers._

/**
 * Tests the REST API for [[Study]].
 */
class StudiesControllerSpec extends ControllerFixture with JsonHelper {
  import org.biobank.TestUtils._

  private def uri(): String = "/studies/"

  private def uri(path: String): String = uri + s"$path"

  private def uri(study: Study): String = uri + s"${study.id.id}"

  private def uri(study: Study, path: String): String = uri(path) + s"/${study.id.id}"

  private def urlName(study: Study)        = uri(study, "name")
  private def urlDescription(study: Study) = uri(study, "description")
  private def urlDisable(study: Study)     = uri(study, "disable")
  private def urlEnable(study: Study)      = uri(study, "enable")
  private def urlRetire(study: Study)      = uri(study, "retire")
  private def urlUnretire(study: Study)    = uri(study, "unretire")

  private def urlAddAnnotationType(study: Study) = uri(study, "pannottype")

  private def urlUpdateAnnotationType(annotType: AnnotationType) =
      (study: Study) => urlAddAnnotationType(study) + s"/${annotType.id}"

  private def compareNameDto(json: JsValue, study: Study): Unit = {
    compareObj(json, NameDto(study.id.id, study.name, study.state.id))
    ()
  }

  private def compareObjs(jsonList: List[JsObject], studies: List[Study]) = {
    val studiesMap = studies.map { study => (study.id, study) }.toMap
    jsonList.foreach { jsonObj =>
      val jsonId = StudyId((jsonObj \ "id").as[String])
      compareObj(jsonObj, studiesMap(jsonId))
    }
  }

  private def checkInvalidStudyId(jsonField: JsObject, urlFunc: Study => String): Unit = {
    val invalidStudy = factory.createDisabledStudy.copy(id = StudyId(nameGenerator.next[Study]))
    val cmdJson = Json.obj("expectedVersion" -> 0L) ++ jsonField
    val json = makeRequest(POST, urlFunc(invalidStudy), NOT_FOUND, cmdJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex("IdNotFound.*study")

    ()
  }

  private def checkInvalidStudyId(urlFunc: Study => String): Unit = {
    checkInvalidStudyId(Json.obj(), urlFunc)
  }

  private def updateWithInvalidVersion(jsonField: JsObject, urlFunc: Study => String): Unit = {
    val study = factory.createDisabledStudy
    studyRepository.put(study)

    val cmdJson = Json.obj("expectedVersion" -> Some(study.version + 1)) ++ jsonField

    val json = makeRequest(POST, urlFunc(study), BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include ("expected version doesn't match current version")

    ()
  }

  private def updateWithInvalidVersion(urlFunc: Study => String): Unit = {
    updateWithInvalidVersion(Json.obj(), urlFunc)
  }

  private def updateNonDisabledStudy[T <: Study](study: T,
                                                 jsonField: JsObject,
                                                 urlFunc: Study => String): Unit = {
    study match {
      case s: DisabledStudy => fail("study should not be disabled")
      case _ => // do nothing
    }

    studyRepository.put(study)

    val cmdJson = Json.obj("expectedVersion" -> Some(study.version)) ++ jsonField

    val json = makeRequest(POST, urlFunc(study), BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex("InvalidStatus: study not disabled")

    ()
  }

  describe("Study REST API") {

    describe("GET /studies") {

      it("list none") {
        PagedResultsSpec(this).emptyResults(uri)
      }

      it("list a study") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)
        val jsonItem = PagedResultsSpec(this).singleItemResult(uri)
        compareObj(jsonItem, study)
      }

      it("list multiple studies") {
        val studies = List(factory.createDisabledStudy,
                           factory.createDisabledStudy)
        studies.foreach(studyRepository.put)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri = uri,
            offset = 0,
            total = studies.size.toLong,
            maybeNext = None,
            maybePrev = None)
        jsonItems must have size studies.size.toLong
        compareObjs(jsonItems, studies)
      }

      it("list a single study when filtered by name") {
        val studies = List(factory.createDisabledStudy, factory.createEnabledStudy)
        val study = studies(0)
        studies.foreach(studyRepository.put)

        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri, Map("filter" -> s"name::${study.name}"))
        compareObj(jsonItem, study)
      }

      it("list a single disabled study when filtered by status") {
        val studies = List(factory.createDisabledStudy,
                           factory.createEnabledStudy,
                           factory.createRetiredStudy)
        studies.foreach(studyRepository.put)
        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri, Map("filter" -> "state::disabled"))
        compareObj(jsonItem, studies(0))
      }

      it("list disabled studies when filtered by state") {
        val studies = List(factory.createDisabledStudy,
                           factory.createDisabledStudy,
                           factory.createEnabledStudy,
                           factory.createEnabledStudy)
        studies.foreach(studyRepository.put)

        val expectedStudies = List(studies(0), studies(1))
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri = uri,
            queryParams = Map("filter" -> "state::disabled"),
            offset = 0,
            total = expectedStudies.size.toLong,
            maybeNext = None,
            maybePrev = None)

        jsonItems must have size expectedStudies.size.toLong
        compareObjs(jsonItems, expectedStudies)
      }

      it("list enabled studies when filtered by state") {
        val studies = List(factory.createDisabledStudy,
                           factory.createDisabledStudy,
                           factory.createEnabledStudy,
                           factory.createEnabledStudy)
        studies.foreach(studyRepository.put)

        val expectedStudies = List(studies(2), studies(3))
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri = uri,
            queryParams = Map("filter" -> "state::enabled"),
            offset = 0,
            total = expectedStudies.size.toLong,
            maybeNext = None,
            maybePrev = None)

        jsonItems must have size expectedStudies.size.toLong
        compareObjs(jsonItems, expectedStudies)
      }

      it("fail on attempt to list studies filtered by an invalid state name") {
        val invalidStateName = "state::" + nameGenerator.next[Study]
        val reply = makeRequest(GET,
                                uri + s"?filter=$invalidStateName",
                                NOT_FOUND)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex (
          "InvalidState: entity state does not exist")
      }

      it("list studies sorted by name") {
        val studies = List(factory.createDisabledStudy.copy(name = "CTR3"),
                           factory.createDisabledStudy.copy(name = "CTR2"),
                           factory.createEnabledStudy.copy(name = "CTR1"),
                           factory.createEnabledStudy.copy(name = "CTR0"))
        studies.foreach(studyRepository.put)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri = uri,
            queryParams = Map("sort" -> "name"),
            offset = 0,
            total = studies.size.toLong,
            maybeNext = None,
            maybePrev = None)

        jsonItems must have size studies.size.toLong
        compareObj(jsonItems(0), studies(3))
        compareObj(jsonItems(1), studies(2))
        compareObj(jsonItems(2), studies(1))
        compareObj(jsonItems(3), studies(0))
      }

      it("list studies sorted by state") {
        val studies = List(factory.createEnabledStudy,
                           factory.createDisabledStudy)
        studies.foreach(studyRepository.put)
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri         = uri,
            queryParams = Map("sort" -> "state"),
            offset      = 0,
            total       = studies.size.toLong,
            maybeNext   = None,
            maybePrev   = None)

        jsonItems must have size studies.size.toLong
        compareObj(jsonItems(0), studies(1))
        compareObj(jsonItems(1), studies(0))
      }

      it("list studies sorted by state in descending order") {
        val studies = List(factory.createEnabledStudy,
                           factory.createDisabledStudy)
        studies.foreach(studyRepository.put)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri = uri,
            queryParams = Map("sort" -> "-state"),
            offset = 0,
            total = studies.size.toLong,
            maybeNext = None,
            maybePrev = None)

        jsonItems must have size studies.size.toLong
        compareObj(jsonItems(0), studies(0))
        compareObj(jsonItems(1), studies(1))
      }

      it("fail on attempt to list studies sorted by an invalid state name") {
        val invalidStateName = nameGenerator.next[Study]
        val reply = makeRequest(GET,
                                uri + s"?sort=$invalidStateName",
                                BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("could not parse sort expression")
      }

      it("list a single study when using paged query") {
        val studies = List(factory.createDisabledStudy.copy(name = "CTR3"),
                           factory.createDisabledStudy.copy(name = "CTR2"),
                           factory.createEnabledStudy.copy(name = "CTR1"),
                           factory.createEnabledStudy.copy(name = "CTR0"))
        studies.foreach(studyRepository.put)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri = uri,
            queryParams = Map("sort" -> "name", "limit" -> "1"),
            total = studies.size.toLong,
            maybeNext = Some(2))

        compareObj(jsonItem, studies(3))
      }

      it("list the last study when using paged query") {
        val studies = List(factory.createDisabledStudy.copy(name = "CTR3"),
                           factory.createDisabledStudy.copy(name = "CTR2"),
                           factory.createEnabledStudy.copy(name = "CTR1"),
                           factory.createEnabledStudy.copy(name = "CTR0"))
        studies.foreach(studyRepository.put)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri = uri,
            queryParams = Map("sort" -> "name", "page" -> "4", "limit" -> "1"),
            total = 4,
            offset = 3,
            maybeNext = None,
            maybePrev = Some(3))

        compareObj(jsonItem, studies(0))
      }

      it("fail when using an invalid query parameters") {
        PagedResultsSpec(this).failWithInvalidParams(uri)
      }

    }

    describe("GET /studies/:id") {

      it("read a study") {
        val study = factory.createEnabledStudy
        studyRepository.put(study)
        val json = makeRequest(GET, uri(study))
        compareObj((json \ "data").get, study)
      }

      it("fails for an invalid study ID") {
        val studyId = nameGenerator.next[Study]
        val json = makeRequest(GET, s"/studies/$studyId", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex("IdNotFound.*study")
      }

    }

    describe("POST /studies") {

      it("add a study") {
        val study = factory.createDisabledStudy
        val cmdJson = Json.obj(
            "name" -> study.name,
            "description" -> study.description)
        val json = makeRequest(POST, uri, json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val jsonId = (json \ "data" \ "id").as[String]
        val studyId = StudyId(jsonId)
        jsonId.length must be > 0

        studyRepository.getByKey(studyId) mustSucceed { repoStudy =>
          compareObj((json \ "data").as[JsObject], repoStudy)

          repoStudy must have (
            'id          (studyId),
            'version     (0L),
            'name        (study.name),
            'description (study.description)
          )

          repoStudy.annotationTypes must have size 0
          checkTimeStamps(repoStudy, DateTime.now, None)
        }
      }

      it("not add add a study with a duplicate name") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(POST,
                               uri,
                               BAD_REQUEST,
                               Json.obj("name"        -> study.name,
                                        "description" -> study.description))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*name already used")
      }

      it("not add add a new study with a name less than 2 characters") {
        val json = makeRequest(POST,
                               uri,
                               BAD_REQUEST,
                               Json.obj("name" -> "a"))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must startWith ("InvalidName")
      }

    }

    describe("POST /studies/name/:id") {

      it("update a study's name") {
        val newName = nameGenerator.next[Study]
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj("expectedVersion" -> Some(study.version),
                               "name"            -> newName)
        val json = makeRequest(POST, uri(study, "name"), json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val jsonId = (json \ "data" \ "id").as[String]
        jsonId must be (study.id.id)

        studyRepository.getByKey(study.id) mustSucceed { repoStudy =>
          compareObj((json \ "data").as[JsObject], repoStudy)

          repoStudy must have (
            'id          (study.id),
            'version     (study.version + 1),
            'name        (newName),
            'description (study.description)
          )

          repoStudy.annotationTypes must have size study.annotationTypes.size.toLong
          checkTimeStamps(repoStudy, DateTime.now, DateTime.now)
        }
      }

      it("not update a study with a duplicate name") {
        val studies = (1 to 2).map { _ =>
            val study = factory.createDisabledStudy
            studyRepository.put(study)
            study
        }

        val json = makeRequest(POST,
                               uri(studies(0), "name"),
                               BAD_REQUEST,
                               Json.obj(
                                 "expectedVersion" -> Some(studies(0).version),
                                 "name"            -> studies(1).name))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*name already used")
      }

      it("fail when updating a study's name to something with less than 2 characters") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(POST,
                               uri(study, "name"),
                               BAD_REQUEST,
                               Json.obj("expectedVersion" -> Some(study.version),
                                        "name"            -> "a"))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must startWith ("InvalidName")
      }

      it("fail when updating name and study ID does not exist") {
        checkInvalidStudyId(Json.obj("name" -> nameGenerator.next[Study]), urlName)
      }

      it("fail when updating name with invalid version") {
        updateWithInvalidVersion(Json.obj("name" -> nameGenerator.next[Study]), urlName)
      }

      it("fail when updating name on an enabled study") {
        List(factory.createEnabledStudy, factory.createRetiredStudy).foreach { study =>
          updateNonDisabledStudy(study, Json.obj("name" -> nameGenerator.next[Study]), urlName)
        }
      }

    }

    describe("POST /studies/description/:id") {

      it("update a study's description") {
        val newDescription = Some(nameGenerator.next[Study])
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj("expectedVersion" -> study.version,
                               "description"     -> newDescription)
        val json = makeRequest(POST, uri(study, "description"), json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val jsonId = (json \ "data" \ "id").as[String]
        jsonId must be (study.id.id)

        studyRepository.getByKey(study.id) mustSucceed { repoStudy =>
          compareObj((json \ "data").as[JsObject], repoStudy)

          repoStudy must have (
            'id          (study.id),
            'version     (study.version + 1),
            'name        (study.name),
            'description (newDescription)
          )

          repoStudy.annotationTypes must have size study.annotationTypes.size.toLong
          checkTimeStamps(repoStudy, DateTime.now, DateTime.now)
        }
      }

      it("fail when updating description and study ID does not exist") {
        checkInvalidStudyId(Json.obj("description" -> nameGenerator.next[Study]), urlDescription)
      }

      it("fail when updating description with invalid version") {
        updateWithInvalidVersion(Json.obj("description" -> nameGenerator.next[Study]), urlDescription)
      }

      it("fail when updating description on a non disabled study") {
        List(factory.createEnabledStudy, factory.createRetiredStudy).foreach { study =>
          updateNonDisabledStudy(study,
                                 Json.obj("description" -> nameGenerator.next[Study]),
                                 urlDescription)
        }
      }

    }

    describe("POST /studies/pannottype/:id") {

      it("add a participant annotation type") {
        val annotType = factory.createAnnotationType
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj(
            "id"              -> study.id.id,
            "expectedVersion" -> Some(study.version),
            "name"            -> annotType.name,
            "description"     -> annotType.description,
            "valueType"       -> annotType.valueType,
            "options"         -> annotType.options,
            "required"        -> annotType.required)
        val json = makeRequest(POST, uri(study, "pannottype"), json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val jsonId = StudyId((json \ "data" \ "id").as[String])
        jsonId must be (study.id)

        studyRepository.getByKey(jsonId) mustSucceed { repoStudy =>
          compareObj((json \ "data").as[JsObject], repoStudy)

          repoStudy must have (
            'id          (study.id),
            'version     (study.version + 1),
            'name        (study.name),
            'description (study.description)
            )

          repoStudy.annotationTypes must have size 1

          repoStudy.annotationTypes.head.id.id must not be empty
          repoStudy.annotationTypes.head must have (
            'name          (annotType.name),
            'description   (annotType.description),
            'valueType     (annotType.valueType),
            'maxValueCount (annotType.maxValueCount),
            'options       (annotType.options),
            'required      (annotType.required)
          )

          checkTimeStamps(repoStudy, DateTime.now, DateTime.now)
        }
      }

      it("fail when adding annotation type and study ID does not exist") {
        checkInvalidStudyId(annotationTypeToJsonNoId(factory.createAnnotationType), urlAddAnnotationType)
      }

      it("fail when adding annotation type and an invalid version") {
        updateWithInvalidVersion(annotationTypeToJsonNoId(factory.createAnnotationType),
                                 urlAddAnnotationType)
      }

      it("fail when adding an annotation type on a non disabled study") {
        List(factory.createEnabledStudy, factory.createRetiredStudy).foreach { study =>
          updateNonDisabledStudy(study,
                                 annotationTypeToJsonNoId(factory.createAnnotationType),
                                 urlAddAnnotationType)
        }
      }

    }

    describe("POST /studies/pannottype/:id/:annotTypeId") {

      it("update a participant annotation type") {
        val annotType = factory.createAnnotationType
        val updatedAnnotType = annotType.copy(name        = nameGenerator.next[Study],
                                              description = Some(nameGenerator.next[Study]))
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj(
            "id"               -> study.id.id,
            "expectedVersion"  -> Some(study.version),
            "annotationTypeId" -> updatedAnnotType.id,
            "name"             -> updatedAnnotType.name,
            "description"      -> updatedAnnotType.description,
            "valueType"        -> updatedAnnotType.valueType,
            "options"          -> updatedAnnotType.options,
            "required"         -> updatedAnnotType.required)
        val json = makeRequest(POST, uri(study, "pannottype") + s"/${annotType.id}", json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val jsonId = StudyId((json \ "data" \ "id").as[String])
        jsonId must be (study.id)

        studyRepository.getByKey(jsonId) mustSucceed { repoStudy =>
          compareObj((json \ "data").as[JsObject], repoStudy)

          repoStudy must have (
            'id          (study.id),
            'version     (study.version + 1),
            'name        (study.name),
            'description (study.description)
            )

          repoStudy.annotationTypes must have size 1

          repoStudy.annotationTypes.head.id.id must not be empty
          repoStudy.annotationTypes.head must have (
            'name          (updatedAnnotType.name),
            'description   (updatedAnnotType.description),
            'valueType     (updatedAnnotType.valueType),
            'maxValueCount (updatedAnnotType.maxValueCount),
            'options       (updatedAnnotType.options),
            'required      (updatedAnnotType.required)
          )

          checkTimeStamps(repoStudy, DateTime.now, DateTime.now)
        }
      }

      it("fail when updating annotation type and study ID does not exist") {
        val annotType = factory.createAnnotationType
        checkInvalidStudyId(annotationTypeToJson(annotType), urlUpdateAnnotationType(annotType))
      }

      it("fail when updating annotation type and an invalid version") {
        val annotType = factory.createAnnotationType
        updateWithInvalidVersion(annotationTypeToJson(factory.createAnnotationType),
                                 urlUpdateAnnotationType(annotType))
      }

      it("fail when updating an annotation type on a non disabled study") {
        val annotType = factory.createAnnotationType
        List(factory.createEnabledStudy, factory.createRetiredStudy).foreach { study =>
          updateNonDisabledStudy(study,
                                 annotationTypeToJson(annotType),
                                 urlUpdateAnnotationType(annotType))
        }
      }

    }

    describe("DELETE /studies/pannottype/:id/:ver/:uniqueId") {

      it("remove a participant annotation type") {
        val annotationType = factory.createAnnotationType
        val study = factory.createDisabledStudy.copy(annotationTypes = Set(annotationType))
        studyRepository.put(study)

        val json = makeRequest(
            DELETE, uri(study, "pannottype") + s"/${study.version}/${annotationType.id}")

        (json \ "status").as[String] must include ("success")

        val jsonId = StudyId((json \ "data" \ "id").as[String])
        jsonId must be (study.id)

        studyRepository.getByKey(jsonId) mustSucceed { repoStudy =>
          compareObj((json \ "data").as[JsObject], repoStudy)

          repoStudy must have (
            'id          (study.id),
            'version     (study.version + 1),
            'name        (study.name),
            'description (study.description)
            )

          repoStudy.annotationTypes must have size (study.annotationTypes.size.toLong - 1)
          checkTimeStamps(repoStudy, DateTime.now, DateTime.now)
        }
      }

      it("fail when removing annotation type and an invalid version") {
        val annotationType = factory.createAnnotationType
        val study = factory.createDisabledStudy.copy(annotationTypes = Set(annotationType))
        val badVersion = study.version + 1
        studyRepository.put(study)

        val json = makeRequest(DELETE,
                               uri(study, "pannottype") + s"/$badVersion/${annotationType.id}",
                               BAD_REQUEST)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("expected version doesn't match current version")
      }

      it("fail when removing annotation type and study ID does not exist") {
        val studyId = nameGenerator.next[Study]

        val json = makeRequest(DELETE, s"/studies/pannottype/$studyId/0/xyz", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex("IdNotFound.*study")
      }

      it("fail when removing an annotation type that does not exist") {
        val badUniqueId = nameGenerator.next[Study]
        val annotationType = factory.createAnnotationType
        val study = factory.createDisabledStudy.copy(annotationTypes = Set(annotationType))
        studyRepository.put(study)

        val json = makeRequest(DELETE,
                               uri(study, "pannottype") + s"/${study.version}/$badUniqueId",
                               NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must startWith ("annotation type does not exist")
      }

      it("fail when removing an annotation type on a non disabled study") {
        val annotationType = factory.createAnnotationType
        val enabledStudy = factory.createEnabledStudy.copy(annotationTypes = Set(annotationType))
        val retiredStudy = factory.createRetiredStudy.copy(annotationTypes = Set(annotationType))

        List(enabledStudy, retiredStudy).foreach { study =>
          studyRepository.put(study)

          val json = makeRequest(DELETE,
                                 uri(study, "pannottype") + s"/${study.version}/${annotationType.id}",
                                 BAD_REQUEST)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex("InvalidStatus: study not disabled")
        }
      }

    }

    describe("POST /studies/enable/:id") {

      it("enable a study") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType.copy(
            studyId              = study.id,
            specimenDescriptions = Set(factory.createCollectionSpecimenDescription))
        collectionEventTypeRepository.put(cet)

        val cmdJson = Json.obj("expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study, "enable"), json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val jsonId = StudyId((json \ "data" \ "id").as[String])
        jsonId must be (study.id)

        studyRepository.getByKey(jsonId) mustSucceed { repoStudy =>
          repoStudy mustBe a[EnabledStudy]
          compareObj((json \ "data").as[JsObject], repoStudy)

          repoStudy must have (
            'id          (study.id),
            'version     (study.version + 1),
            'name        (study.name),
            'description (study.description)
            )

          repoStudy.annotationTypes must have size (study.annotationTypes.size.toLong)
          checkTimeStamps(repoStudy, DateTime.now, DateTime.now)
        }
      }

      it("not enable a study when it has no specimen groups") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType
        collectionEventTypeRepository.put(cet)

        val cmdJson = Json.obj("expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study, "enable"), BAD_REQUEST, cmdJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("no collection specimen specs")
      }

      it("not enable a study when it has no collection event types") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj("expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study, "enable"), BAD_REQUEST, cmdJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("no collection event types")
      }

      it("fail when enabling a study and the study ID is invalid") {
        val study = factory.createDisabledStudy
        val cmdJson = Json.obj("expectedVersion" -> Some(study.version))

        val json = makeRequest(POST, uri(study, "enable"), NOT_FOUND, json = cmdJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex("IdNotFound.*study")
      }

      it("fail when enabling and study ID does not exit") {
        checkInvalidStudyId(urlEnable)
      }

      it("fail when enabling a study and the version is invalid") {
        updateWithInvalidVersion(urlEnable)
      }
    }

    describe("POST /studies/disable/:id") {

      it("disable a study") {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj("expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study, "disable"), json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val jsonId = StudyId((json \ "data" \ "id").as[String])
        jsonId must be (study.id)

        studyRepository.getByKey(jsonId) mustSucceed { repoStudy =>
          repoStudy mustBe a[DisabledStudy]
          compareObj((json \ "data").as[JsObject], repoStudy)

          repoStudy must have (
            'id          (study.id),
            'version     (study.version + 1),
            'name        (study.name),
            'description (study.description)
            )

          repoStudy.annotationTypes must have size (study.annotationTypes.size.toLong)
          checkTimeStamps(repoStudy, DateTime.now, DateTime.now)
        }
      }

      it("fail when disabling and study ID does not exit") {
        checkInvalidStudyId(urlDisable)
      }

      it("fail when disabling a study and the version is invalid") {
        updateWithInvalidVersion(urlDisable)
      }
    }

    describe("POST /studies/retire/:id") {

      it("retire a study") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj("expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study, "retire"), json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val jsonId = StudyId((json \ "data" \ "id").as[String])
        jsonId must be (study.id)

        studyRepository.getByKey(jsonId) mustSucceed { repoStudy =>
          repoStudy mustBe a[RetiredStudy]
          compareObj((json \ "data").as[JsObject], repoStudy)

          repoStudy must have (
            'id          (study.id),
            'version     (study.version + 1),
            'name        (study.name),
            'description (study.description)
            )

          repoStudy.annotationTypes must have size (study.annotationTypes.size.toLong)
          checkTimeStamps(repoStudy, DateTime.now, DateTime.now)
        }
      }

      it("fail when retiring and study ID does not exit") {
        checkInvalidStudyId(urlRetire)
      }

      it("fail when retiring a study and the version is invalid") {
        updateWithInvalidVersion(urlRetire)
      }
    }

    describe("POST /studies/unretire/:id") {

      it("unretire a study") {
        val study = factory.createRetiredStudy
        studyRepository.put(study)

        val cmdJson = Json.obj("expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study, "unretire"), json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val jsonId = StudyId((json \ "data" \ "id").as[String])
        jsonId must be (study.id)

        studyRepository.getByKey(jsonId) mustSucceed { repoStudy =>
          repoStudy mustBe a[DisabledStudy]
          compareObj((json \ "data").as[JsObject], repoStudy)

          repoStudy must have (
            'id          (study.id),
            'version     (study.version + 1),
            'name        (study.name),
            'description (study.description)
            )

          repoStudy.annotationTypes must have size (study.annotationTypes.size.toLong)
          checkTimeStamps(repoStudy, DateTime.now, DateTime.now)
        }
      }

      it("fail when unretiring and study ID does not exit") {
        checkInvalidStudyId(urlUnretire)
      }

      it("fail when unretiring a study and the version is invalid") {
        updateWithInvalidVersion(urlUnretire)
      }
    }

    describe("GET /studies/counts") {

      it("return empty counts") {
        val json = makeRequest(GET, uri("counts"))

        (json \ "status").as[String] must include ("success")

        (json \ "data" \ "total").as[Long] must be (0)

        (json \ "data" \ "disabledCount").as[Long] must be (0)

        (json \ "data" \ "enabledCount").as[Long] must be (0)

        (json \ "data" \ "retiredCount").as[Long] must be (0)
      }

      it("return valid counts") {
        val studies = List(factory.createDisabledStudy,
                           factory.createDisabledStudy,
                           factory.createDisabledStudy,
                           factory.createEnabledStudy,
                           factory.createEnabledStudy,
                           factory.createRetiredStudy)

        studies.foreach { c => studyRepository.put(c) }

        val json = makeRequest(GET, uri("counts"))

        (json \ "status").as[String] must include ("success")

        (json \ "data" \ "total").as[Long] must be (6)

        (json \ "data" \ "disabledCount").as[Long] must be (3)

        (json \ "data" \ "enabledCount").as[Long] must be (2)

        (json \ "data" \ "retiredCount").as[Long] must be (1)
      }

    }

    describe("GET /studies/valuetypes") {
      it("list all") {
        val json = makeRequest(GET, uri("valuetypes"))
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    describe("GET /studies/anatomicalsrctypes") {
      it("list all") {
        val json = makeRequest(GET, uri("anatomicalsrctypes"))
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    describe("GET /studies/specimentypes") {
      it("list all") {
        val json = makeRequest(GET, uri("specimentypes"))
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    describe("GET /studies/preservtypes") {
      it("list all") {
        val json = makeRequest(GET, uri("preservtypes"))
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    describe("GET /studies/preservtemptypes ") {
      it("list all") {
        val json = makeRequest(GET, uri("preservtemptypes"))
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    describe("GET /studies/sgvaluetypes ") {
      it("list all") {
        val json = makeRequest(GET, uri("sgvaluetypes"))
        val jsonObj = (json \ "data").as[JsObject]
          (jsonObj \ "anatomicalSourceType").as[List[String]].size        must be > 0
          (jsonObj \ "preservationType").as[List[String]].size            must be > 0
          (jsonObj \ "preservationTemperatureType").as[List[String]].size must be > 0
          (jsonObj \ "specimenType").as[List[String]].size                must be > 0
      }
    }

    describe("GET /studies/names") {

      it("list multiple study names in ascending order") {

        val study1 = factory.createDisabledStudy.copy(name = "ST1")
        val study2 = factory.createDisabledStudy.copy(name = "ST2")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, uri("names") + "?order=asc")
                              (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size studies.size.toLong

        compareNameDto(jsonList(0), study1)
        compareNameDto(jsonList(1), study2)
      }

      it("list single study when using a filter") {
        val study1 = factory.createDisabledStudy.copy(name = "ABC")
        val study2 = factory.createDisabledStudy.copy(name = "DEF")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, uri("names") + "?filter=name::ABC")
                              (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1

        compareNameDto(jsonList(0), study1)
      }

      it("list nothing when using a name filter for name not in system") {
        val study1 = factory.createDisabledStudy.copy(name = "ABC")
        val study2 = factory.createDisabledStudy.copy(name = "DEF")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, uri("names") + "?filter=name::xxx")
                              (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      it("fail for invalid sort field") {
        val study1 = factory.createDisabledStudy.copy(name = "ST1")
        val study2 = factory.createDisabledStudy.copy(name = "ST2")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, uri("names") + "?sort=xxxx", BAD_REQUEST)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("invalid sort field")
      }

    }

    describe("GET /studies/centres/:id") {

      it("list the centres associated with a study") {
        val study = factory.createEnabledStudy
        val location = factory.createLocation
        val centre = factory.createEnabledCentre.copy(studyIds = Set(study.id), locations = Set(location))

        studyRepository.put(study)
        centreRepository.put(centre)

        val json = makeRequest(GET, uri("centres") + s"/${study.id}")

        (json \ "status").as[String] must include ("success")

        val jsonCentreLocations = (json \ "data").as[List[JsObject]]
        jsonCentreLocations must have length 1
        val jsonCentreLocation  = jsonCentreLocations(0)

        (jsonCentreLocation \ "centreId").as[String] must be (centre.id.id)

        (jsonCentreLocation \ "locationId").as[String] must be (location.id.id)

        (jsonCentreLocation \ "centreName").as[String] must be (centre.name)

        (jsonCentreLocation \ "locationName").as[String] must be (location.name)
      }

    }

  }

}
