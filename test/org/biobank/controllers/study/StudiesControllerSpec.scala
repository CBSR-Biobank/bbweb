package org.biobank.controllers.study

import org.biobank.controllers._
import org.biobank.dto._
import org.biobank.domain.study._
import org.biobank.domain.JsonHelper._
import org.biobank.fixture.ControllerFixture

import play.api.test.Helpers._
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory

/**
 * Tests the REST API for [[Study]].
 */
class StudiesControllerSpec extends ControllerFixture {

  def uri: String = "/studies"

  def uri(study: Study): String = uri + s"/${study.id.id}"

  def compareStudyNameDto(json: JsValue, study: Study) {
    compareObj(json, StudyNameDto(study.id.id, study.name, study.getClass.getSimpleName))
  }

  def compareObjs(jsonList: List[JsObject], studies: List[Study]) = {
    val studiesMap = studies.map { study => (study.id, study) }.toMap
    jsonList.foreach { jsonObj =>
      val jsonId = StudyId((jsonObj \ "id").as[String])
      compareObj(jsonObj, studiesMap(jsonId))
    }
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

      "list multiple studies" taggedAs(Tag("1")) in {
        val studies = List(factory.createDisabledStudy, factory.createDisabledStudy)
        .map{ study => studyRepository.put(study) }

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          offset = 0,
          total = studies.size,
          maybeNext = None,
          maybePrev = None)
        jsonItems must have size studies.size
        log.info(s"--> $jsonItems")
        compareObjs(jsonItems, studies)
      }

      "list a single study when filtered by name" in {
        val studies = List(factory.createDisabledStudy, factory.createEnabledStudy)
        .map { study => studyRepository.put(study) }

        val jsonItem = PagedResultsSpec(this)
        .singleItemResult(uri, Map("filter" -> studies(0).name))
        compareObj(jsonItem, studies(0))
      }

      "list a single disabled study when filtered by status" in {
        val studies = List(factory.createDisabledStudy,
                           factory.createEnabledStudy,
                           factory.createRetiredStudy).map { study => studyRepository.put(study) }

        val jsonItem = PagedResultsSpec(this).singleItemResult(uri, Map("status" -> "disabled"))
        compareObj(jsonItem, studies(0))
      }

      "list disabled studies when filtered by status" in {
        val studies = List(factory.createDisabledStudy,
                           factory.createDisabledStudy,
                           factory.createEnabledStudy,
                           factory.createEnabledStudy)
        .map { study => studyRepository.put(study) }

        val expectedStudies = List(studies(0), studies(1))
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("status" -> "disabled"),
          offset = 0,
          total = expectedStudies.size,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size expectedStudies.size
        compareObjs(jsonItems, expectedStudies)
      }

      "list enabled studies when filtered by status" in {
        val studies = List(
          factory.createDisabledStudy,
          factory.createDisabledStudy,
          factory.createEnabledStudy,
          factory.createEnabledStudy)
        .map { study => studyRepository.put(study) }

        val expectedStudies = List(studies(2), studies(3))
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("status" -> "enabled"),
          offset = 0,
          total = expectedStudies.size,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size expectedStudies.size
        compareObjs(jsonItems, expectedStudies)
      }

      "list studies sorted by name" in {
        val studies = List(
          factory.createDisabledStudy.copy(name = "CTR3"),
          factory.createDisabledStudy.copy(name = "CTR2"),
          factory.createEnabledStudy.copy(name = "CTR1"),
          factory.createEnabledStudy.copy(name = "CTR0"))
        .map { study => studyRepository.put(study) }

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
        val studies = List(
          factory.createEnabledStudy,
          factory.createDisabledStudy)
        .map { study => studyRepository.put(study) }

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("sort" -> "status"),
          offset = 0,
          total = studies.size,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size studies.size
        compareObj(jsonItems(0), studies(1))
        compareObj(jsonItems(1), studies(0))
      }

      "list studies sorted by status in descending order" in {
        val studies = List(
          factory.createEnabledStudy,
          factory.createDisabledStudy)
        .map { study => studyRepository.put(study) }

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
        val studies = List(
          factory.createDisabledStudy.copy(name = "CTR3"),
          factory.createDisabledStudy.copy(name = "CTR2"),
          factory.createEnabledStudy.copy(name = "CTR1"),
          factory.createEnabledStudy.copy(name = "CTR0"))
        .map { study => studyRepository.put(study) }

        val jsonItem = PagedResultsSpec(this).singleItemResult(
          uri = uri,
          queryParams = Map("sort" -> "name", "pageSize" -> "1"),
          total = studies.size,
          maybeNext = Some(2))

        compareObj(jsonItem, studies(3))
      }

      "list the last study when using paged query" in {
        val studies = List(
          factory.createDisabledStudy.copy(name = "CTR3"),
          factory.createDisabledStudy.copy(name = "CTR2"),
          factory.createEnabledStudy.copy(name = "CTR1"),
          factory.createEnabledStudy.copy(name = "CTR0"))
        .map { study => studyRepository.put(study) }

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

    "POST /studies" must {
      "add a study" in {
        val study = factory.createDisabledStudy
        val cmdJson = Json.obj(
          "name" -> study.name,
          "description" -> study.description)
        val json = makeRequest(POST, uri, json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val eventStudyId = (json \ "data" \ "id").as[String]
        studyRepository.getByKey(StudyId(eventStudyId)).fold(
          err => fail(err.list.toList.mkString),
          repoStudy => repoStudy.name mustBe ((json \ "data" \ "name").as[String])
        )
      }
    }

    "PUT /studies/:id" must {

      "update a study" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj(
          "id"              -> study.id.id,
          "expectedVersion" -> Some(study.version),
          "name"            -> study.name,
          "description"     -> study.description)
        val json = makeRequest(PUT, uri(study), json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val eventStudyId = (json \ "data" \ "id").as[String]
        studyRepository.getByKey(StudyId(eventStudyId)).fold(
          err => fail(err.list.toList.mkString),
          repoStudy => {
            repoStudy.name mustBe ((json \ "data" \ "name").as[String])
            repoStudy.version mustBe ((json \ "data" \ "version").as[Long])
          }
        )
      }

      "fail when updating and study IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj(
          "id"              -> nameGenerator.next[Study],
          "expectedVersion" -> Some(study.version),
          "name"            -> study.name,
          "description"     -> study.description)
        val json = makeRequest(PUT, uri(study), BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("study id mismatch")
      }
    }

    "GET /studies/:id" must {
      "read a study" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)
        val json = makeRequest(GET, uri(study))
        compareObj((json \ "data").get, study)
      }
    }

    "POST /studies/enable" must {
      "enable a study" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)
        specimenGroupRepository.put(factory.createSpecimenGroup)
        collectionEventTypeRepository.put(factory.createCollectionEventType)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study) + "/enable", json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val eventStudyId = (json \ "data" \ "id").as[String]
        studyRepository.getByKey(StudyId(eventStudyId)).fold(
          err => fail(err.list.toList.mkString),
          repoStudy => repoStudy.version mustBe ((json \ "data" \ "version").as[Long])
        )
      }

      "not enable a study when it has no specimen groups" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cet = factory.createCollectionEventType
        collectionEventTypeRepository.put(cet)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study) + "/enable", BAD_REQUEST, cmdJson)

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("no specimen groups")
      }

      "not enable a study  when it has no collection event types" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        specimenGroupRepository.put(sg)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study) + "/enable", BAD_REQUEST, cmdJson)

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("no collection event types")
      }

      "fail when enabling a study and the study IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)
        specimenGroupRepository.put(factory.createSpecimenGroup)
        collectionEventTypeRepository.put(factory.createCollectionEventType)

        val cmdJson = Json.obj(
          "id" -> nameGenerator.next[Study],
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study) + "/enable", BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("study id mismatch")
      }
    }

    "POST /studies/disable" must {
      "disable a study" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study) + "/disable", json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val eventStudyId = (json \ "data" \ "id").as[String]
        studyRepository.getByKey(StudyId(eventStudyId)).fold(
          err => fail(err.list.toList.mkString),
          repoStudy => repoStudy.version mustBe ((json \ "data" \ "version").as[Long])
        )
      }

      "fail when disabling a study and the study IDs do not match" in {
        val study = factory.createEnabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> nameGenerator.next[Study],
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study) + "/disable", BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("study id mismatch")
      }
    }

    "POST /studies/retire" must {
      "retire a study" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study) + "/retire", json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val eventStudyId = (json \ "data" \ "id").as[String]
        studyRepository.getByKey(StudyId(eventStudyId)).fold(
          err => fail(err.list.toList.mkString),
          repoStudy => repoStudy.version mustBe ((json \ "data" \ "version").as[Long])
        )
      }

      "fail when retiring a study and the study IDs do not match" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> nameGenerator.next[Study],
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study) + "/retire", BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("study id mismatch")
      }
    }

    "POST /studies/unretire" must {
      "unretire a study" in {
        val study = factory.createRetiredStudy
        studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study) + "/unretire", json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val eventStudyId = (json \ "data" \ "id").as[String]
        studyRepository.getByKey(StudyId(eventStudyId)).fold(
          err => fail(err.list.toList.mkString),
          repoStudy => repoStudy.version mustBe ((json \ "data" \ "version").as[Long])
        )
      }

      "fail when retiring a study and the study IDs do not match" in {
        val study = factory.createRetiredStudy
        studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> nameGenerator.next[Study],
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study) + "/unretire", BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("study id mismatch")
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

    "GET /studies/dto/collection " must {
      "return empty results for new study" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, uri(study) + s"/dto/collection")
        val jsonObj = (json \ "data").as[JsObject]

        (jsonObj \ "collectionEventTypes").as[List[JsObject]].size mustBe (0)
        (jsonObj \ "collectionEventAnnotationTypes").as[List[JsObject]].size mustBe (0)
        (jsonObj \ "collectionEventAnnotationTypeIdsInUse").as[List[String]].size mustBe (0)
        (jsonObj \ "specimenGroups").as[List[JsObject]].size mustBe (0)
      }

      "return valid results for study" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        collectionEventAnnotationTypeRepository.put(factory.createCollectionEventAnnotationType)
        specimenGroupRepository.put(factory.createSpecimenGroup)

        val cet = factory.createCollectionEventType.copy(
          specimenGroupData = List(factory.createCollectionEventTypeSpecimenGroupData),
          annotationTypeData = List(factory.createCollectionEventTypeAnnotationTypeData))

        collectionEventTypeRepository.put(cet)

        val json = makeRequest(GET, uri(study) + s"/dto/collection")
        val jsonObj = (json \ "data").as[JsObject]

        (jsonObj \ "collectionEventTypes").as[List[JsObject]].size mustBe (1)
        (jsonObj \ "collectionEventAnnotationTypes").as[List[JsObject]].size mustBe (1)
        (jsonObj \ "collectionEventAnnotationTypeIdsInUse").as[List[String]].size mustBe (1)
        (jsonObj \ "specimenGroups").as[List[JsObject]].size mustBe (1)
      }
    }

    "GET /studies/dto/processing " must {
      "return empty results for new study" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, uri(study) + s"/dto/processing")
        val jsonObj = (json \ "data").as[JsObject]

        (jsonObj \ "processingTypes").as[List[JsObject]].size mustBe (0)
        (jsonObj \ "specimenLinkTypes").as[List[JsObject]].size mustBe (0)
        (jsonObj \ "specimenLinkAnnotationTypes").as[List[JsObject]].size mustBe (0)
        (jsonObj \ "specimenGroups").as[List[JsObject]].size mustBe (0)
      }

      "return valid results for study" in {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        processingTypeRepository.put(factory.createProcessingType)
        specimenLinkTypeRepository.put(factory.createSpecimenLinkType)
        specimenLinkAnnotationTypeRepository.put(factory.createSpecimenLinkAnnotationType)
        specimenGroupRepository.put(factory.createSpecimenGroup)

        val json = makeRequest(GET, uri(study) + s"/dto/processing")
        val jsonObj = (json \ "data").as[JsObject]

        (jsonObj \ "processingTypes").as[List[JsObject]].size mustBe (1)

        (jsonObj \ "specimenLinkTypes").as[List[JsObject]].size mustBe (1)
        (jsonObj \ "specimenLinkAnnotationTypes").as[List[JsObject]].size mustBe (1)
        (jsonObj \ "specimenGroups").as[List[JsObject]].size mustBe (1)
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

        compareStudyNameDto(jsonList(0), study1)
        compareStudyNameDto(jsonList(1), study2)
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

        compareStudyNameDto(jsonList(0), study1)
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


  }

}
