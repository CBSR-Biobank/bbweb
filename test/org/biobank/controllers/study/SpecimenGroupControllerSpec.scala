package org.biobank.controllers.study

import org.biobank.fixture._
import org.biobank.controllers.BbwebPlugin
import org.biobank.domain.study.{ Study, SpecimenGroup }
import org.biobank.fixture.ControllerFixture
import org.biobank.domain.JsonHelper._

import play.api.test.Helpers._
import play.api.test.WithApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import org.joda.time.DateTime
import com.typesafe.plugin._
import play.api.Play.current
import org.scalatestplus.play._

class SpecimenGroupControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def addToNonDisabledStudy(study: Study, sg: SpecimenGroup) = {

    use[BbwebPlugin].studyRepository.put(study)

    val cmdJson = Json.obj(
      "studyId"                     -> study.id.id,
      "name"                        -> sg.name,
      "description"                 -> sg.description,
      "units"                       -> sg.units,
      "anatomicalSourceType"        -> sg.anatomicalSourceType.toString,
      "preservationType"            -> sg.preservationType.toString,
      "preservationTemperatureType" -> sg.preservationTemperatureType.toString,
      "specimenType"                -> sg.specimenType.toString)
    val json = makeRequest(POST, "/studies/sgroups", BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  def updateOnNonDisabledStudy(
    study: Study,
    sg: SpecimenGroup) = {
    use[BbwebPlugin].studyRepository.put(study)
    use[BbwebPlugin].specimenGroupRepository.put(sg)

    val sg2 = factory.createSpecimenGroup
    val cmdJson = Json.obj(
      "studyId"                     -> study.id.id,
      "id"                          -> sg.id.id,
      "expectedVersion"             -> Some(sg.version),
      "name"                        -> sg2.name,
      "description"                 -> sg2.description,
      "units"                       -> sg2.units,
      "anatomicalSourceType"        -> sg2.anatomicalSourceType.toString,
      "preservationType"            -> sg2.preservationType.toString,
      "preservationTemperatureType" -> sg2.preservationTemperatureType.toString,
      "specimenType"                -> sg2.specimenType.toString)
    val json = makeRequest(PUT, s"/studies/sgroups/${sg.id.id}", BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  def removeOnNonDisabledStudy(
    study: Study,
    sg: SpecimenGroup) = {
    use[BbwebPlugin].studyRepository.put(study)
    use[BbwebPlugin].specimenGroupRepository.put(sg)

    val json = makeRequest(
      DELETE,
      s"/studies/sgroups/${sg.studyId.id}/${sg.id.id}/${sg.version}",
      BAD_REQUEST)

    (json \ "status").as[String] must include ("error")
    (json \ "message").as[String] must include ("is not disabled")
  }

  "Specimen Group REST API" when {

    "GET /studies/sgroups" must {
      "list none" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val json = makeRequest(GET, s"/studies/sgroups/${study.id.id}")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list a single specimen group" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        use[BbwebPlugin].specimenGroupRepository.put(sg)

        val json = makeRequest(GET, s"/studies/sgroups/${study.id.id}")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1
        compareObj(jsonList(0), sg)
      }

      "get a single specimen group" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        use[BbwebPlugin].specimenGroupRepository.put(sg)

        val json = makeRequest(GET, s"/studies/sgroups/${study.id.id}?sgId=${sg.id.id}").as[JsObject]
        (json \ "status").as[String] must include ("success")
        val jsonObj = (json \ "data")
        compareObj(jsonObj, sg)
      }

      "list multiple specimen groups" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val sgroups = List(factory.createSpecimenGroup, factory.createSpecimenGroup)
        sgroups map { sg => use[BbwebPlugin].specimenGroupRepository.put(sg) }

        val json = makeRequest(GET, s"/studies/sgroups/${study.id.id}")
        (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size sgroups.size
          (jsonList zip sgroups).map { item => compareObj(item._1, item._2) }
      }

      "fail for an invalid study ID" in new App(fakeApp) {
        doLogin
        val studyId = nameGenerator.next[Study]

        val json = makeRequest(GET, s"/studies/sgroups/$studyId", BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid study ID when using an specimen group id" in new App(fakeApp) {
        doLogin
        val studyId = nameGenerator.next[Study]
        val sgId = nameGenerator.next[SpecimenGroup]

        val json = makeRequest(GET, s"/studies/sgroups/$studyId?sgId=$sgId", BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid study id")
      }

      "fail for an invalid specimen group id" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val sgId = nameGenerator.next[SpecimenGroup]

        val json = makeRequest(GET, s"/studies/sgroups/${study.id}?sgId=$sgId", BAD_REQUEST)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("specimen group does not exist")
      }

    }

    "POST /studies/sgroups" must {
      "add a specimen group" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        val cmdJson = Json.obj(
          "studyId"                     -> sg.studyId.id,
          "name"                        -> sg.name,
          "description"                 -> sg.description,
          "units"                       -> sg.units,
          "anatomicalSourceType"        -> sg.anatomicalSourceType.toString,
          "preservationType"            -> sg.preservationType.toString,
          "preservationTemperatureType" -> sg.preservationTemperatureType.toString,
          "specimenType"                -> sg.specimenType.toString)
        val json = makeRequest(POST, "/studies/sgroups", json = cmdJson)

        (json \ "status").as[String] must include ("success")
      }
    }

    "POST /studies/sgroups" must {
      "not add a specimen group to enabled study" in new App(fakeApp) {
        doLogin
        addToNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail,
          factory.createSpecimenGroup)
      }
    }

    "POST /studies/sgroups" must {
      "not add a specimen group to retired study" in new App(fakeApp) {
        doLogin
        addToNonDisabledStudy(
          factory.createDisabledStudy.retire | fail,
          factory.createSpecimenGroup)
      }
    }

    "PUT /studies/sgroups" must {
      "update a specimen group" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        use[BbwebPlugin].specimenGroupRepository.put(sg)

        val sg2 = factory.createSpecimenGroup
        val cmdJson = Json.obj(
          "studyId"                     -> study.id.id,
          "id"                          -> sg.id.id,
          "expectedVersion"             -> Some(sg.version),
          "name"                        -> sg2.name,
          "description"                 -> sg2.description,
          "units"                       -> sg2.units,
          "anatomicalSourceType"        -> sg2.anatomicalSourceType.toString,
          "preservationType"            -> sg2.preservationType.toString,
          "preservationTemperatureType" -> sg2.preservationTemperatureType.toString,
          "specimenType"                -> sg2.specimenType.toString)
        val json = makeRequest(PUT, s"/studies/sgroups/${sg.id.id}", json = cmdJson)

        (json \ "status").as[String] must include ("success")
      }
    }

    "PUT /studies/sgroups" must {
      "not update a specimen group on an enabled study" in new App(fakeApp) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail,
          factory.createSpecimenGroup)
      }
    }

    "PUT /studies/sgroups" must {
      "not update a specimen group on an retired study" in new App(fakeApp) {
        doLogin
        updateOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail,
          factory.createSpecimenGroup)
      }
    }

    "DELETE /studies/sgroups" must {
      "remove a specimen group" in new App(fakeApp) {
        doLogin
        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val sg = factory.createSpecimenGroup
        use[BbwebPlugin].specimenGroupRepository.put(sg)

        val json = makeRequest(
          DELETE,
          s"/studies/sgroups/${sg.studyId.id}/${sg.id.id}/${sg.version}")

        (json \ "status").as[String] must include ("success")
      }
    }

    "DELETE /studies/sgroups" must {
      "not remove a specimen group from an enabled study" in new App(fakeApp) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.enable(1, 1) | fail,
          factory.createSpecimenGroup)
      }
    }

    "DELETE /studies/sgroups" must {
      "not remove a specimen group from an retired study" in new App(fakeApp) {
        doLogin
        removeOnNonDisabledStudy(
          factory.createDisabledStudy.retire | fail,
          factory.createSpecimenGroup)
      }
    }

  }

}
