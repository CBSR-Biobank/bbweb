package org.biobank.controllers

import org.biobank.domain.study.StudyId
import org.biobank.infrastructure.command.StudyCommands._
import org.biobank.infrastructure.event.StudyEvents._
import org.biobank.service.json.JsonHelper._
import org.biobank.fixture.ControllerFixture
import play.api.test.Helpers._
import play.api.test.WithApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import com.typesafe.plugin.use

/**
  * Tests the REST API for [[Study]].
  */
class StudiesControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  "Study REST API" when {

    "GET /studies" should {
      "list none" taggedAs(Tag("1")) in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/studies")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 0

        log.info(s"repo: ${use[BbwebPlugin]}")
      }

      "list a study" taggedAs(Tag("1")) in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        log.info(s"repo: ${use[BbwebPlugin]}")

        val json = makeRequest(GET, "/studies")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have length 1
        compareObj(jsonList(0), study)
      }

      "list multiple studies" in new WithApplication(fakeApplication()) {
        doLogin
        val studies = List(factory.createDisabledStudy, factory.createDisabledStudy)
        use[BbwebPlugin].studyRepository.removeAll
        studies.map(study => use[BbwebPlugin].studyRepository.put(study))

        val json = makeRequest(GET, "/studies")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size studies.size

        (jsonList zip studies).map { item => compareObj(item._1, item._2) }
      }
    }

    "POST /studies" should {
      "add a study" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        val cmdJson = Json.obj(
          "name" -> study.name,
          "description" -> study.description)
        val json = makeRequest(POST, "/studies", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "id").as[String]
        val validation = use[BbwebPlugin].studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.name should be ((json \ "data" \ "name").as[String])
        }
      }
    }

    "PUT /studies/:id" should {
      "update a study" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val cmdJson = Json.obj(
          "id"              -> study.id.id,
          "expectedVersion" -> Some(study.version),
          "name"            -> study.name,
          "description"     -> study.description)
        val json = makeRequest(PUT, s"/studies/${study.id.id}", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "id").as[String]
        val validation = use[BbwebPlugin].studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.name should be ((json \ "data" \ "name").as[String])
          repoStudy.version should be ((json \ "data" \ "version").as[Long])
        }
      }
    }

    "GET /studies/:id" should {
      "read a study" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy.enable(Some(0), org.joda.time.DateTime.now, 1, 1) | fail
        use[BbwebPlugin].studyRepository.put(study)
        val json = makeRequest(GET, s"/studies/${study.id.id}")
        compareObj((json \ "data"), study)
      }
    }

    "POST /studies/enable" should {
      "enable a study" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)
        use[BbwebPlugin].specimenGroupRepository.put(factory.createSpecimenGroup)
        use[BbwebPlugin].collectionEventTypeRepository.put(factory.createCollectionEventType)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, "/studies/enable", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "id").as[String]
        val validation = use[BbwebPlugin].studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.version should be ((json \ "data" \ "version").as[Long])
        }
      }
    }

    "POST /studies/enable" should {
      "not enable a study" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, "/studies/enable", BAD_REQUEST, cmdJson)

        (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("no specimen groups")
      }
    }

    "POST /studies/disable" should {
      "disable a study" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy.enable(Some(0), org.joda.time.DateTime.now, 1, 1) | fail
        use[BbwebPlugin].studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, "/studies/disable", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "id").as[String]
        val validation = use[BbwebPlugin].studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.version should be ((json \ "data" \ "version").as[Long])
        }
      }
    }

    "POST /studies/retire" should {
      "retire a study" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, "/studies/retire", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "id").as[String]
        val validation = use[BbwebPlugin].studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.version should be ((json \ "data" \ "version").as[Long])
        }
      }
    }

    "POST /studies/unretire" should {
      "unretire a study" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy.retire(Some(0), org.joda.time.DateTime.now) | fail
        use[BbwebPlugin].studyRepository.put(study)

        val cmdJson = Json.obj(
          "id" -> study.id.id,
          "expectedVersion" -> Some(study.version))
        val json = makeRequest(POST, "/studies/unretire", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventStudyId = (json \ "data" \ "id").as[String]
        val validation = use[BbwebPlugin].studyRepository.getByKey(StudyId(eventStudyId))
        validation should be ('success)
        validation map { repoStudy =>
          repoStudy.version should be ((json \ "data" \ "version").as[Long])
        }
      }
    }

    "GET /studies/valuetypes" should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/studies/valuetypes")
        val values = (json \ "data").as[List[String]]
        values.size should be > 0
      }
    }


    "GET /studies/anatomicalsrctypes" should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/studies/anatomicalsrctypes")
        val values = (json \ "data").as[List[String]]
        values.size should be > 0
      }
    }

    "GET /studies/specimentypes" should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/studies/specimentypes")
        val values = (json \ "data").as[List[String]]
        values.size should be > 0
      }
    }

    "GET /studies/preservtypes" should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/studies/preservtypes")
        val values = (json \ "data").as[List[String]]
        values.size should be > 0
      }
    }

    "GET /studies/preservtemptypes " should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/studies/preservtemptypes")
        val values = (json \ "data").as[List[String]]
        values.size should be > 0
      }
    }

    "GET /studies/sgvaluetypes " should {
      "list all" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/studies/sgvaluetypes")
        val jsonObj = (json \ "data").as[JsObject]
        (jsonObj \ "anatomicalSourceType").as[List[String]].size        should be > 0
        (jsonObj \ "preservationType").as[List[String]].size            should be > 0
        (jsonObj \ "preservationTemperatureType").as[List[String]].size should be > 0
        (jsonObj \ "specimenType").as[List[String]].size                should be > 0
      }
    }

    "GET /studies/dto/processing " should {
      "return empty results for new study" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val json = makeRequest(GET, s"/studies/dto/processing/${study.id}")
        val jsonObj = (json \ "data").as[JsObject]

        (jsonObj \ "processingTypes").as[List[JsObject]].size should be (0)
        (jsonObj \ "specimenLinkTypes").as[List[JsObject]].size should be (0)
        (jsonObj \ "specimenLinkAnnotationTypes").as[List[JsObject]].size should be (0)
        (jsonObj \ "specimenGroups").as[List[JsObject]].size should be (0)
      }

      "return valid results for study" in new WithApplication(fakeApplication()) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        use[BbwebPlugin].processingTypeRepository.put(factory.createProcessingType)
        use[BbwebPlugin].specimenLinkTypeRepository.put(factory.createSpecimenLinkType)
        use[BbwebPlugin].specimenLinkAnnotationTypeRepository.put(factory.createSpecimenLinkAnnotationType)
        use[BbwebPlugin].specimenGroupRepository.put(factory.createSpecimenGroup)

        val json = makeRequest(GET, s"/studies/dto/processing/${study.id}")
        val jsonObj = (json \ "data").as[JsObject]

        (jsonObj \ "processingTypes").as[List[JsObject]].size should be (1)
        (jsonObj \ "specimenLinkTypes").as[List[JsObject]].size should be (1)
        (jsonObj \ "specimenLinkAnnotationTypes").as[List[JsObject]].size should be (1)
        (jsonObj \ "specimenGroups").as[List[JsObject]].size should be (1)
      }
    }

  }

}
