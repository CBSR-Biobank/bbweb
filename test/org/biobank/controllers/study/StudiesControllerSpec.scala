package org.biobank.controllers.study

import org.biobank.domain.study.{ Study, StudyId }
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.JsonHelper._
import org.biobank.fixture.ControllerFixture
import play.api.test.Helpers._
import play.api.test.WithApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory

/**
  * Tests the REST API for [[Study]].
  */
class StudiesControllerSpec extends ControllerFixture {
  import TestGlobal._

  val log = LoggerFactory.getLogger(this.getClass)

  def uri: String = "/studies"

  def uri(study: Study): String = uri + s"/${study.id.id}"

  def compareStudyNameDto(json: JsValue, study: Study) {
    compareObj(json, StudyNameDto(study.id.id, study.name))
  }

  "Study REST API" when {

    "GET /studies" must {

      "list none" taggedAs(Tag("1")) in new App(fakeApp) {
        doLogin
        val json = makeRequest(GET, uri)
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list a study" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        log.debug(s"repo: ${}")

        val json = makeRequest(GET, uri)
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have length 1
        compareStudyNameDto(jsonList(0), study)
      }

      "list multiple studies" in new App(fakeApp) {
        doLogin
        val studies = List(factory.createDisabledStudy, factory.createDisabledStudy)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, uri)
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size studies.size

        (jsonList zip studies).map { item => compareStudyNameDto(item._1, item._2) }
      }

      "list a single study with a name query string" in new App(fakeApp) {
        doLogin

        val study1 = factory.createDisabledStudy.copy(name = "ABC")
        val study2 = factory.createDisabledStudy.copy(name = "XYZ")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, uri + "?filter=" + study1.name)
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1
        compareStudyNameDto(jsonList(0), study1)
      }

      "list multiple studies in ascending order" in new App(fakeApp) {
        doLogin

        val study1 = factory.createDisabledStudy.copy(name = "ST1")
        val study2 = factory.createDisabledStudy.copy(name = "ST2")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, uri + "?order=ascending")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size studies.size

        compareStudyNameDto(jsonList(0), study1)
        compareStudyNameDto(jsonList(1), study2)
      }

      "list multiple studies in descending order" in new App(fakeApp) {
        doLogin

        val study1 = factory.createDisabledStudy.copy(name = "ST1")
        val study2 = factory.createDisabledStudy.copy(name = "ST2")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, uri + "?order=descending")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size studies.size

        compareStudyNameDto(jsonList(0), study2)
        compareStudyNameDto(jsonList(1), study1)
      }

      "fail for invalid order parameter" in new App(fakeApp) {
        doLogin

        val study1 = factory.createDisabledStudy.copy(name = "ST1")
        val study2 = factory.createDisabledStudy.copy(name = "ST2")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, uri + "?order=xxxx", BAD_REQUEST)

        (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("invalid order requested")
      }

      "list studies sorted by name" in new App(fakeApp) {
        doLogin

        val study1 = factory.createDisabledStudy.copy(name = "ST1").enable(1, 1) | fail
        val study2 = factory.createDisabledStudy.copy(name = "ST2")

        val studies = List(study1, study2)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, uri + "?sort=name")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 2

        compareStudyNameDto(jsonList(0), study1)
        compareStudyNameDto(jsonList(1), study2)
      }

      "list studies sorted by status" in new App(fakeApp) {
        doLogin

        val study1 = factory.createDisabledStudy.copy(name = "ST1").enable(1, 1) | fail
        val study2 = factory.createDisabledStudy.copy(name = "ST2")

        val studies = List(study1, study2)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, uri + "?sort=status")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 2

        compareStudyNameDto(jsonList(0), study2)
        compareStudyNameDto(jsonList(1), study1)
      }

      "fail for an invalid sort column" in new App(fakeApp) {
        doLogin

        val study1 = factory.createDisabledStudy.copy(name = "ST1")
        val study2 = factory.createDisabledStudy.copy(name = "ST2").enable(1, 1) | fail

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, uri + "?sort=description", BAD_REQUEST)

        (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("invalid sort field")
      }

      "list single study when using paged query" in new App(fakeApp) {
        doLogin

        val study1 = factory.createDisabledStudy.copy(name = "ST1")
        val study2 = factory.createDisabledStudy.copy(name = "ST2")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, uri + "?pageSize=1")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1
      }

      "fail when using page that exeeds limits" in new App(fakeApp) {
        doLogin

        val study1 = factory.createDisabledStudy.copy(name = "ST1")
        val study2 = factory.createDisabledStudy.copy(name = "ST2")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, uri + "?page=3&pageSize=1", BAD_REQUEST)
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("invalid page requested")
      }

      "fail when using a negative page number" in new App(fakeApp) {
        doLogin

        val study1 = factory.createDisabledStudy.copy(name = "ST1")
        val study2 = factory.createDisabledStudy.copy(name = "ST2")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, uri + "?page=-1&pageSize=1", BAD_REQUEST)
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("page is invalid")
      }

      "fail when using a negative pageSzie" in new App(fakeApp) {
        doLogin

        val study1 = factory.createDisabledStudy.copy(name = "ST1")
        val study2 = factory.createDisabledStudy.copy(name = "ST2")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, uri + "?page=1&pageSize=-1", BAD_REQUEST)
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("page size is invalid")
      }
    }

    "POST /studies" must {
      "add a study" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        val cmdJson = Json.obj(
          "name" -> study.name,
          "description" -> study.description)
        val json = makeRequest(POST, uri, json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val eventStudyId = (json \ "data" \ "id").as[String]
        studyRepository.getByKey(StudyId(eventStudyId)).fold(
          err => fail(err.list.mkString),
          repoStudy => repoStudy.name mustBe ((json \ "data" \ "name").as[String])
        )
      }
    }

    "PUT /studies/:id" must {
      "update a study" in new App(fakeApp) {
        doLogin
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
          err => fail(err.list.mkString),
          repoStudy => {
            repoStudy.name mustBe ((json \ "data" \ "name").as[String])
            repoStudy.version mustBe ((json \ "data" \ "version").as[Long])
          }
        )
      }
    }

    "GET /studies/:id" must {
      "read a study" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy.enable(1, 1) | fail
        studyRepository.put(study)
        val json = makeRequest(GET, uri(study))
        compareObj((json \ "data"), study)
      }
    }

    "POST /studies/enable" must {
      "enable a study" in new App(fakeApp) {
        doLogin
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
          err => fail(err.list.mkString),
           repoStudy => repoStudy.version mustBe ((json \ "data" \ "version").as[Long])
         )
      }
    }

    "POST /studies/enable" must {
      "not enable a study" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study) + "/enable", BAD_REQUEST, cmdJson)

        (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("no specimen groups")
      }
    }

    "POST /studies/disable" must {
      "disable a study" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy.enable(1, 1) | fail
        studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study) + "/disable", json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val eventStudyId = (json \ "data" \ "id").as[String]
        studyRepository.getByKey(StudyId(eventStudyId)).fold(
          err => fail(err.list.mkString),
          repoStudy => repoStudy.version mustBe ((json \ "data" \ "version").as[Long])
        )
      }
    }

    "POST /studies/retire" must {
      "retire a study" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study) + "/retire", json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val eventStudyId = (json \ "data" \ "id").as[String]
        studyRepository.getByKey(StudyId(eventStudyId)).fold(
          err => fail(err.list.mkString),
          repoStudy => repoStudy.version mustBe ((json \ "data" \ "version").as[Long])
        )
      }
    }

    "POST /studies/unretire" must {
      "unretire a study" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy.retire | fail
        studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, uri(study) + "/unretire", json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val eventStudyId = (json \ "data" \ "id").as[String]
        studyRepository.getByKey(StudyId(eventStudyId)).fold(
          err => fail(err.list.mkString),
          repoStudy => repoStudy.version mustBe ((json \ "data" \ "version").as[Long])
        )
      }
    }

    "GET /studies/valuetypes" must {
      "list all" in new App(fakeApp) {
        doLogin
        val json = makeRequest(GET, uri + "/valuetypes")
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    "GET /studies/anatomicalsrctypes" must {
      "list all" in new App(fakeApp) {
        doLogin
        val json = makeRequest(GET, uri + "/anatomicalsrctypes")
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    "GET /studies/specimentypes" must {
      "list all" in new App(fakeApp) {
        doLogin
        val json = makeRequest(GET, uri + "/specimentypes")
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    "GET /studies/preservtypes" must {
      "list all" in new App(fakeApp) {
        doLogin
        val json = makeRequest(GET, uri + "/preservtypes")
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    "GET /studies/preservtemptypes " must {
      "list all" in new App(fakeApp) {
        doLogin
        val json = makeRequest(GET, uri + "/preservtemptypes")
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    "GET /studies/sgvaluetypes " must {
      "list all" in new App(fakeApp) {
        doLogin
        val json = makeRequest(GET, uri + "/sgvaluetypes")
        val jsonObj = (json \ "data").as[JsObject]
        (jsonObj \ "anatomicalSourceType").as[List[String]].size        must be > 0
        (jsonObj \ "preservationType").as[List[String]].size            must be > 0
        (jsonObj \ "preservationTemperatureType").as[List[String]].size must be > 0
        (jsonObj \ "specimenType").as[List[String]].size                must be > 0
      }
    }

    "GET /studies/dto/collection " must {
      "return empty results for new study" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, uri(study) + s"/dto/collection")
        val jsonObj = (json \ "data").as[JsObject]

        (jsonObj \ "collectionEventTypes").as[List[JsObject]].size mustBe (0)
        (jsonObj \ "collectionEventAnnotationTypes").as[List[JsObject]].size mustBe (0)
        (jsonObj \ "collectionEventAnnotationTypesInUse").as[List[String]].size mustBe (0)
        (jsonObj \ "specimenGroups").as[List[JsObject]].size mustBe (0)
      }

      "return valid results for study" in new App(fakeApp) {
        doLogin
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
        (jsonObj \ "collectionEventAnnotationTypesInUse").as[List[String]].size mustBe (1)
        (jsonObj \ "specimenGroups").as[List[JsObject]].size mustBe (1)
      }
    }

    "GET /studies/dto/processing " must {
      "return empty results for new study" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, uri(study) + s"/dto/processing")
        val jsonObj = (json \ "data").as[JsObject]

        (jsonObj \ "processingTypes").as[List[JsObject]].size mustBe (0)
        (jsonObj \ "specimenLinkTypes").as[List[JsObject]].size mustBe (0)
        (jsonObj \ "specimenLinkAnnotationTypes").as[List[JsObject]].size mustBe (0)
        (jsonObj \ "specimenGroups").as[List[JsObject]].size mustBe (0)
      }

      "return valid results for study" in new App(fakeApp) {
        doLogin
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

      "list multiple study names in ascending order" in new App(fakeApp) {
        doLogin

        val study1 = factory.createDisabledStudy.copy(name = "ST1")
        val study2 = factory.createDisabledStudy.copy(name = "ST2")

        val studies = List(study2, study1)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, "/studies/names?order=ascending")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size studies.size

        compareStudyNameDto(jsonList(0), study1)
        compareStudyNameDto(jsonList(1), study2)
      }

      "list single study when using a filter" in new App(fakeApp) {
        doLogin

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

      "fail for invalid order parameter" in new App(fakeApp) {
        doLogin

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
