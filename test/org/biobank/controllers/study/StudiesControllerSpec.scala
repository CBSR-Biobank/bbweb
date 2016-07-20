package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.domain.JsonHelper
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

  def uri(): String = "/studies"

  def uri(path: String): String = uri + s"/$path"

  def uri(study: Study): String = uri + s"/${study.id.id}"

  def uri(study: Study, path: String): String = uri(path) + s"/${study.id.id}"

  def compareNameDto(json: JsValue, study: Study) {
    compareObj(json, NameDto(study.id.id, study.name, study.getClass.getSimpleName))
  }

  def compareObjs(jsonList: List[JsObject], studies: List[Study]) = {
    val studiesMap = studies.map { study => (study.id, study) }.toMap
    jsonList.foreach { jsonObj =>
      val jsonId = StudyId((jsonObj \ "id").as[String])
      compareObj(jsonObj, studiesMap(jsonId))
    }
  }

  def checkInvalidStudyId(path: String, jsonField: JsObject): Unit = {
    val invalidStudyId = nameGenerator.next[Study]
    val cmdJson = Json.obj("expectedVersion" -> 0L) ++ jsonField

    val json = makeRequest(POST, s"/studies/$path/$invalidStudyId", NOT_FOUND, cmdJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex("IdNotFound.*study")
  }

  def checkInvalidStudyId(url: String): Unit = {
    checkInvalidStudyId(url, Json.obj())
  }

  def updateWithInvalidVersion(path: String, jsonField: JsObject): Unit = {
    val study = factory.createDisabledStudy
    studyRepository.put(study)

    val cmdJson = Json.obj("expectedVersion" -> Some(study.version + 1)) ++ jsonField

    val json = makeRequest(POST, uri(study, path), BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include ("expected version doesn't match current version")
  }

  def updateWithInvalidVersion(url: String): Unit = {
    updateWithInvalidVersion(url, Json.obj())
  }

  def updateNonDisabledStudy[T <: Study](study: T, path: String, jsonField: JsObject): Unit = {
    study match {
      case s: DisabledStudy => fail("study should not be disabled")
      case _ => // do nothing
    }

    studyRepository.put(study)

    val cmdJson = Json.obj("expectedVersion" -> Some(study.version)) ++ jsonField

    val json = makeRequest(POST, uri(study, path), BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex("InvalidStatus.*study not disabled")
  }

  "Study REST API" when {

    "GET /studies" must {

      "list none" in {
        PagedResultsSpec(this).emptyResults(uri)
      }

      "list a study" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)
        val jsonItem = PagedResultsSpec(this).singleItemResult(uri)
        compareObj(jsonItem, study)
      }

      "list multiple studies" in {
        val studies = List(factory.createDisabledStudy,
                           factory.createDisabledStudy)
        studies.foreach(studyRepository.put)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri = uri,
            offset = 0,
            total = studies.size,
            maybeNext = None,
            maybePrev = None)
        jsonItems must have size studies.size
        compareObjs(jsonItems, studies)
      }

      "list a single study when filtered by name" in {
        val studies = List(factory.createDisabledStudy, factory.createEnabledStudy)
        studies.foreach(studyRepository.put)

        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri, Map("filter" -> studies(0).name))
        compareObj(jsonItem, studies(0))
      }

      "list a single disabled study when filtered by status" in {
        val studies = List(factory.createDisabledStudy,
                           factory.createEnabledStudy,
                           factory.createRetiredStudy)
        studies.foreach(studyRepository.put)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri, Map("status" -> "DisabledStudy"))
        compareObj(jsonItem, studies(0))
      }

      "list disabled studies when filtered by status" in {
        val studies = List(factory.createDisabledStudy,
                           factory.createDisabledStudy,
                           factory.createEnabledStudy,
                           factory.createEnabledStudy)
        studies.foreach(studyRepository.put)

        val expectedStudies = List(studies(0), studies(1))
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri = uri,
            queryParams = Map("status" -> "DisabledStudy"),
            offset = 0,
            total = expectedStudies.size,
            maybeNext = None,
            maybePrev = None)

        jsonItems must have size expectedStudies.size
        compareObjs(jsonItems, expectedStudies)
      }

      "list enabled studies when filtered by status" in {
        val studies = List(factory.createDisabledStudy,
                           factory.createDisabledStudy,
                           factory.createEnabledStudy,
                           factory.createEnabledStudy)
        studies.foreach(studyRepository.put)

        val expectedStudies = List(studies(2), studies(3))
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri = uri,
            queryParams = Map("status" -> "EnabledStudy"),
            offset = 0,
            total = expectedStudies.size,
            maybeNext = None,
            maybePrev = None)

        jsonItems must have size expectedStudies.size
        compareObjs(jsonItems, expectedStudies)
      }

      "list studies sorted by name" in {
        val studies = List(factory.createDisabledStudy.copy(name = "CTR3"),
                           factory.createDisabledStudy.copy(name = "CTR2"),
                           factory.createEnabledStudy.copy(name = "CTR1"),
                           factory.createEnabledStudy.copy(name = "CTR0"))
        studies.foreach(studyRepository.put)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri = uri,
            queryParams = Map("sort" -> "name"),
            offset = 0,
            total = studies.size,
            maybeNext = None,
            maybePrev = None)

        jsonItems must have size studies.size
        compareObj(jsonItems(0), studies(3))
        compareObj(jsonItems(1), studies(2))
        compareObj(jsonItems(2), studies(1))
        compareObj(jsonItems(3), studies(0))
      }

      "list studies sorted by status" in {
        val studies = List(factory.createEnabledStudy,
                           factory.createDisabledStudy)
        studies.foreach(studyRepository.put)
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri         = uri,
            queryParams = Map("sort" -> "status"),
            offset      = 0,
            total       = studies.size,
            maybeNext   = None,
            maybePrev   = None)

        jsonItems must have size studies.size
        compareObj(jsonItems(0), studies(1))
        compareObj(jsonItems(1), studies(0))
      }

      "list studies sorted by status in descending order" in {
        val studies = List(factory.createEnabledStudy,
                           factory.createDisabledStudy)
        studies.foreach(studyRepository.put)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri = uri,
            queryParams = Map("sort" -> "status", "order" -> "desc"),
            offset = 0,
            total = studies.size,
            maybeNext = None,
            maybePrev = None)

        jsonItems must have size studies.size
        compareObj(jsonItems(0), studies(0))
        compareObj(jsonItems(1), studies(1))
      }

      "list a single study when using paged query" in {
        val studies = List(factory.createDisabledStudy.copy(name = "CTR3"),
                           factory.createDisabledStudy.copy(name = "CTR2"),
                           factory.createEnabledStudy.copy(name = "CTR1"),
                           factory.createEnabledStudy.copy(name = "CTR0"))
        studies.foreach(studyRepository.put)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri = uri,
            queryParams = Map("sort" -> "name", "pageSize" -> "1"),
            total = studies.size,
            maybeNext = Some(2))

        compareObj(jsonItem, studies(3))
      }

      "list the last study when using paged query" in {
        val studies = List(factory.createDisabledStudy.copy(name = "CTR3"),
                           factory.createDisabledStudy.copy(name = "CTR2"),
                           factory.createEnabledStudy.copy(name = "CTR1"),
                           factory.createEnabledStudy.copy(name = "CTR0"))
        studies.foreach(studyRepository.put)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri = uri,
            queryParams = Map("sort" -> "name", "page" -> "4", "pageSize" -> "1"),
            total = 4,
            offset = 3,
            maybeNext = None,
            maybePrev = Some(3))

        compareObj(jsonItem, studies(0))
      }

      "fail when using an invalid query parameters" in {
        PagedResultsSpec(this).failWithInvalidParams(uri)
      }

    }

    "GET /studies/:id" must {

      "read a study" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)
        val json = makeRequest(GET, uri(study))
        compareObj((json \ "data").get, study)
      }

      "fails for an invalid study ID" in {
        val studyId = nameGenerator.next[Study]
        val json = makeRequest(GET, s"/studies/$studyId", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex("IdNotFound.*study")
      }

    }

    "POST /studies" must {

      "add a study" in {
        val study = factory.createDisabledStudy
        val cmdJson = Json.obj(
            "name" -> study.name,
            "description" -> study.description)
        val json = makeRequest(POST, "/studies", json = cmdJson)

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

      "not add add a study with a duplicate name" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(POST,
                               "/studies",
                               BAD_REQUEST,
                               Json.obj("name"        -> study.name,
                                        "description" -> study.description))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*name already used")
      }

      "not add add a new study with a name less than 2 characters" in {
        val json = makeRequest(POST,
                               "/studies",
                               BAD_REQUEST,
                               Json.obj("name" -> "a"))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must startWith ("InvalidName")
      }

    }

    "POST /studies/name/:id" must {

      "update a study's name" in {
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

          repoStudy.annotationTypes must have size study.annotationTypes.size
          checkTimeStamps(repoStudy, DateTime.now, DateTime.now)
        }
      }

      "not update a study with a duplicate name" in {
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

      "fail when updating a study's name to something with less than 2 characters" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(POST,
                               uri(study, "name"),
                               BAD_REQUEST,
                               Json.obj(
                                 "expectedVersion" -> Some(study.version),
                                 "name"            -> "a"))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must startWith ("InvalidName")
      }

      "fail when updating name and study ID does not exist" in {
        checkInvalidStudyId("name", Json.obj("name" -> nameGenerator.next[Study]))
      }

      "fail when updating name with invalid version" in {
        updateWithInvalidVersion("name", Json.obj("name" -> nameGenerator.next[Study]))
      }

      "fail when updating name on an enabled study" in {
        List(factory.createEnabledStudy, factory.createRetiredStudy).foreach { study =>
          updateNonDisabledStudy(study, "name", Json.obj("name" -> nameGenerator.next[Study]))
        }
      }

    }

    "POST /studies/description/:id" must {

      "update a study's description" in {
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

          repoStudy.annotationTypes must have size study.annotationTypes.size
          checkTimeStamps(repoStudy, DateTime.now, DateTime.now)
        }
      }

      "fail when updating description and study ID does not exist" in {
        checkInvalidStudyId("description",
                            Json.obj("description" -> nameGenerator.next[Study]))
      }

      "fail when updating description with invalid version" in {
        updateWithInvalidVersion("description",
                                 Json.obj("description" -> nameGenerator.next[Study]))
      }

      "fail when updating description on a non disabled study" in {
        List(factory.createEnabledStudy, factory.createRetiredStudy).foreach { study =>
          updateNonDisabledStudy(study, "description", Json.obj("description" -> nameGenerator.next[Study]))
        }
      }

    }

    "POST /studies/pannottype/:id" must {

      "add a participant annotation type" in {
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

          repoStudy.annotationTypes.head.uniqueId must not be empty
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

      "fail when adding annotation type and study ID does not exist" in {
        checkInvalidStudyId("pannottype",
                            annotationTypeToJsonNoId(factory.createAnnotationType))
      }

      "fail when adding annotation type and an invalid version" in {
        updateWithInvalidVersion("pannottype",
                            annotationTypeToJsonNoId(factory.createAnnotationType))
      }

      "fail when adding an annotation type on a non disabled study" in {
        List(factory.createEnabledStudy, factory.createRetiredStudy).foreach { study =>
          updateNonDisabledStudy(study,
                                 "pannottype",
                                 annotationTypeToJsonNoId(factory.createAnnotationType))
        }
      }

    }

    "DELETE /studies/pannottype/:id/:ver/:uniqueId" must {

      "remove a participant annotation type" in {
        val annotationType = factory.createAnnotationType
        val study = factory.createDisabledStudy.copy(annotationTypes = Set(annotationType))
        studyRepository.put(study)

        val json = makeRequest(
            DELETE, uri(study, "pannottype") + s"/${study.version}/${annotationType.uniqueId}")

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

          repoStudy.annotationTypes must have size (study.annotationTypes.size - 1)
          checkTimeStamps(repoStudy, DateTime.now, DateTime.now)
        }
      }

      "fail when removing annotation type and an invalid version" in {
        val annotationType = factory.createAnnotationType
        val study = factory.createDisabledStudy.copy(annotationTypes = Set(annotationType))
        val badVersion = study.version + 1
        studyRepository.put(study)

        val json = makeRequest(DELETE,
                               uri(study, "pannottype") + s"/$badVersion/${annotationType.uniqueId}",
                               BAD_REQUEST)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("expected version doesn't match current version")
      }

      "fail when removing annotation type and study ID does not exist" in {
        val studyId = nameGenerator.next[Study]

        val json = makeRequest(DELETE, s"/studies/pannottype/$studyId/0/xyz", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex("IdNotFound.*study")
      }

      "fail when removing an annotation type that does not exist" in {
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

      "fail when removing an annotation type on a non disabled study" in {
        val annotationType = factory.createAnnotationType
        val enabledStudy = factory.createEnabledStudy.copy(annotationTypes = Set(annotationType))
        val retiredStudy = factory.createRetiredStudy.copy(annotationTypes = Set(annotationType))

        List(enabledStudy, retiredStudy).foreach { study =>
          studyRepository.put(study)

          val json = makeRequest(DELETE,
                                 uri(study, "pannottype") + s"/${study.version}/${annotationType.uniqueId}",
                                 BAD_REQUEST)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex("InvalidStatus.*study not disabled")
        }
      }

    }

    "POST /studies/enable/:id" must {

      "enable a study" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType.copy(
            studyId       = study.id,
            specimenSpecs = Set(factory.createCollectionSpecimenSpec))
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

          repoStudy.annotationTypes must have size (study.annotationTypes.size)
          checkTimeStamps(repoStudy, DateTime.now, DateTime.now)
        }
      }

      "not enable a study when it has no specimen groups" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType
        collectionEventTypeRepository.put(cet)

        val cmdJson = Json.obj("expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study, "enable"), BAD_REQUEST, cmdJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("no collection specimen specs")
      }

      "not enable a study when it has no collection event types" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj("expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study, "enable"), BAD_REQUEST, cmdJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("no collection event types")
      }

      "fail when enabling a study and the study ID is invalid" in {
        val study = factory.createDisabledStudy
        val cmdJson = Json.obj("expectedVersion" -> Some(study.version))

        val json = makeRequest(POST, uri(study, "enable"), NOT_FOUND, json = cmdJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex("IdNotFound.*study")
      }

      "fail when enabling and study ID does not exit" in {
        checkInvalidStudyId("enable")
      }

      "fail when enabling a study and the version is invalid" in {
        updateWithInvalidVersion("enable")
      }
    }

    "POST /studies/disable/:id" must {

      "disable a study" in {
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

          repoStudy.annotationTypes must have size (study.annotationTypes.size)
          checkTimeStamps(repoStudy, DateTime.now, DateTime.now)
        }
      }

      "fail when disabling and study ID does not exit" in {
        checkInvalidStudyId("disable")
      }

      "fail when disabling a study and the version is invalid" in {
        updateWithInvalidVersion("disable")
      }
    }

    "POST /studies/retire/:id" must {

      "retire a study" in {
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

          repoStudy.annotationTypes must have size (study.annotationTypes.size)
          checkTimeStamps(repoStudy, DateTime.now, DateTime.now)
        }
      }

      "fail when retiring and study ID does not exit" in {
        checkInvalidStudyId("retire")
      }

      "fail when retiring a study and the version is invalid" in {
        updateWithInvalidVersion("retire")
      }
    }

    "POST /studies/unretire/:id" must {

      "unretire a study" in {
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

          repoStudy.annotationTypes must have size (study.annotationTypes.size)
          checkTimeStamps(repoStudy, DateTime.now, DateTime.now)
        }
      }

      "fail when unretiring and study ID does not exit" in {
        checkInvalidStudyId("unretire")
      }

      "fail when unretiring a study and the version is invalid" in {
        updateWithInvalidVersion("unretire")
      }
    }

    "GET /studies/counts" must {

      "return empty counts" in {
        val json = makeRequest(GET, uri + "/counts")

        (json \ "status").as[String] must include ("success")

        (json \ "data" \ "total").as[Long] must be (0)

        (json \ "data" \ "disabledCount").as[Long] must be (0)

        (json \ "data" \ "enabledCount").as[Long] must be (0)

        (json \ "data" \ "retiredCount").as[Long] must be (0)
      }

      "return valid counts"  in {
        val studies = List(factory.createDisabledStudy,
                           factory.createDisabledStudy,
                           factory.createDisabledStudy,
                           factory.createEnabledStudy,
                           factory.createEnabledStudy,
                           factory.createRetiredStudy)

        studies.foreach { c => studyRepository.put(c) }

        val json = makeRequest(GET, uri + "/counts")

        (json \ "status").as[String] must include ("success")

        (json \ "data" \ "total").as[Long] must be (6)

        (json \ "data" \ "disabledCount").as[Long] must be (3)

        (json \ "data" \ "enabledCount").as[Long] must be (2)

        (json \ "data" \ "retiredCount").as[Long] must be (1)
      }

    }

    "GET /studies/valuetypes" must {
      "list all" in {
        val json = makeRequest(GET, uri + "/valuetypes")
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    "GET /studies/anatomicalsrctypes" must {
      "list all" in {
        val json = makeRequest(GET, uri + "/anatomicalsrctypes")
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    "GET /studies/specimentypes" must {
      "list all" in {
        val json = makeRequest(GET, uri + "/specimentypes")
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    "GET /studies/preservtypes" must {
      "list all" in {
        val json = makeRequest(GET, uri + "/preservtypes")
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    "GET /studies/preservtemptypes " must {
      "list all" in {
        val json = makeRequest(GET, uri + "/preservtemptypes")
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    "GET /studies/sgvaluetypes " must {
      "list all" in {
        val json = makeRequest(GET, uri + "/sgvaluetypes")
        val jsonObj = (json \ "data").as[JsObject]
          (jsonObj \ "anatomicalSourceType").as[List[String]].size        must be > 0
          (jsonObj \ "preservationType").as[List[String]].size            must be > 0
          (jsonObj \ "preservationTemperatureType").as[List[String]].size must be > 0
          (jsonObj \ "specimenType").as[List[String]].size                must be > 0
      }
    }

    "GET /studies/names" must {

      "list multiple study names in ascending order" in {

        val study1 = factory.createDisabledStudy.copy(name = "ST1")
        val study2 = factory.createDisabledStudy.copy(name = "ST2")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, "/studies/names?order=asc")
                              (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size studies.size

        compareNameDto(jsonList(0), study1)
        compareNameDto(jsonList(1), study2)
      }

      "list single study when using a filter" in {
        val study1 = factory.createDisabledStudy.copy(name = "ABC")
        val study2 = factory.createDisabledStudy.copy(name = "DEF")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, "/studies/names?filter=ABC")
                              (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1

        compareNameDto(jsonList(0), study1)
      }

      "list nothing when using an invalid filter" in {
        val study1 = factory.createDisabledStudy.copy(name = "ABC")
        val study2 = factory.createDisabledStudy.copy(name = "DEF")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, "/studies/names?filter=xxx")
                              (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "fail for invalid order parameter" in {
        val study1 = factory.createDisabledStudy.copy(name = "ST1")
        val study2 = factory.createDisabledStudy.copy(name = "ST2")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, "/studies/names?order=xxxx", BAD_REQUEST)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("invalid order requested")
      }

    }

    "GET /studies/centres/:id" must {

      "111 list the centres associated with a study" in {
        val study = factory.createEnabledStudy
        val location = factory.createLocation
        val centre = factory.createEnabledCentre.copy(studyIds = Set(study.id), locations = Set(location))

        studyRepository.put(study)
        centreRepository.put(centre)

        val json = makeRequest(GET, s"/studies/centres/${study.id}")

        (json \ "status").as[String] must include ("success")

        val jsonCentreLocations = (json \ "data").as[List[JsObject]]
        jsonCentreLocations must have length 1
        val jsonCentreLocation  = jsonCentreLocations(0)

        (jsonCentreLocation \ "centreId").as[String] must be (centre.id.id)

        (jsonCentreLocation \ "locationId").as[String] must be (location.uniqueId)

        (jsonCentreLocation \ "centreName").as[String] must be (centre.name)

        (jsonCentreLocation \ "locationName").as[String] must be (location.name)
      }

    }

  }

}
