package org.biobank.controllers.study

import org.biobank.domain.study.StudyId
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.domain.JsonHelper._
import org.biobank.fixture.ControllerFixture
import play.api.test.Helpers._
import play.api.test.WithApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import com.typesafe.plugin.use
import org.scalatestplus.play._

/**
  * Tests the REST API for [[Study]].
  */
class StudiesControllerSpec extends ControllerFixture {
  import TestGlobal._

  val log = LoggerFactory.getLogger(this.getClass)

  "Study REST API" when {

    "GET /studies" must {
      "list none" in new App(fakeApp) {
        doLogin
        val json = makeRequest(GET, "/studies")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0

        log.info(s"repo: ${}")
      }

      "list a study" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        log.info(s"repo: ${}")

        val json = makeRequest(GET, "/studies")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have length 1
        compareObj(jsonList(0), study)
      }

      "list multiple studies" in new App(fakeApp) {
        doLogin
        val studies = List(factory.createDisabledStudy, factory.createDisabledStudy)
        studyRepository.removeAll
        studies.map(study => studyRepository.put(study))

        val json = makeRequest(GET, "/studies")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size studies.size

        (jsonList zip studies).map { item => compareObj(item._1, item._2) }
      }
    }

    "POST /studies" must {
      "add a study" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        val cmdJson = Json.obj(
          "name" -> study.name,
          "description" -> study.description)
        val json = makeRequest(POST, "/studies", json = cmdJson)

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
        val json = makeRequest(PUT, s"/studies/${study.id.id}", json = cmdJson)

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
        val json = makeRequest(GET, s"/studies/${study.id.id}")
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
        val json = makeRequest(POST, "/studies/enable", json = cmdJson)

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
        val json = makeRequest(POST, "/studies/enable", BAD_REQUEST, cmdJson)

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
        val json = makeRequest(POST, "/studies/disable", json = cmdJson)

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
        val json = makeRequest(POST, "/studies/retire", json = cmdJson)

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
        val json = makeRequest(POST, "/studies/unretire", json = cmdJson)

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
        val json = makeRequest(GET, "/studies/valuetypes")
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }


    "GET /studies/anatomicalsrctypes" must {
      "list all" in new App(fakeApp) {
        doLogin
        val json = makeRequest(GET, "/studies/anatomicalsrctypes")
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    "GET /studies/specimentypes" must {
      "list all" in new App(fakeApp) {
        doLogin
        val json = makeRequest(GET, "/studies/specimentypes")
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    "GET /studies/preservtypes" must {
      "list all" in new App(fakeApp) {
        doLogin
        val json = makeRequest(GET, "/studies/preservtypes")
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    "GET /studies/preservtemptypes " must {
      "list all" in new App(fakeApp) {
        doLogin
        val json = makeRequest(GET, "/studies/preservtemptypes")
        val values = (json \ "data").as[List[String]]
        values.size must be > 0
      }
    }

    "GET /studies/sgvaluetypes " must {
      "list all" in new App(fakeApp) {
        doLogin
        val json = makeRequest(GET, "/studies/sgvaluetypes")
        val jsonObj = (json \ "data").as[JsObject]
        (jsonObj \ "anatomicalSourceType").as[List[String]].size        must be > 0
        (jsonObj \ "preservationType").as[List[String]].size            must be > 0
        (jsonObj \ "preservationTemperatureType").as[List[String]].size must be > 0
        (jsonObj \ "specimenType").as[List[String]].size                must be > 0
      }
    }

    "GET /studies/dto/processing " must {
      "return empty results for new study" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(GET, s"/studies/dto/processing/${study.id}")
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

        val json = makeRequest(GET, s"/studies/dto/processing/${study.id}")
        val jsonObj = (json \ "data").as[JsObject]

        (jsonObj \ "processingTypes").as[List[JsObject]].size mustBe (1)
        (jsonObj \ "specimenLinkTypes").as[List[JsObject]].size mustBe (1)
        (jsonObj \ "specimenLinkAnnotationTypes").as[List[JsObject]].size mustBe (1)
        (jsonObj \ "specimenGroups").as[List[JsObject]].size mustBe (1)
      }
    }

  }

}
