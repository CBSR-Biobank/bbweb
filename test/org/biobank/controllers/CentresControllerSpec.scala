package org.biobank.controllers

import org.biobank.fixture.NameGenerator
import org.biobank.domain.centre._
import org.biobank.domain.{ Location, LocationId }
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.infrastructure.event.CentreEvents._
import org.biobank.service.json.JsonHelper._
import org.biobank.fixture.ControllerFixture
import org.biobank.service.json.Centre._

import play.api.test.Helpers._
import play.api.test.WithApplication
import play.api.libs.json._
import org.scalatest.Tag
import org.slf4j.LoggerFactory
import com.typesafe.plugin._
import play.api.Play.current

/**
  * Tests the REST API for [[Centre]]s.
  */
class CentresControllerSpec extends ControllerFixture {

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "Centre REST API" when {

    "GET /centres" should {
      "list none" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, "/centres")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 0
      }

      "list a centre" in new WithApplication(fakeApplication()) {
        doLogin
        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val json = makeRequest(GET, "/centres")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have length 1
        compareObj(jsonList(0), centre)
      }

      "list multiple centres" in new WithApplication(fakeApplication()) {
        doLogin
        val centres = List(factory.createDisabledCentre, factory.createDisabledCentre)
          .map{ centre => (centre.id, centre) }.toMap

        use[BbwebPlugin].centreRepository.removeAll
        centres.values.foreach(centre => use[BbwebPlugin].centreRepository.put(centre))

        val json = makeRequest(GET, "/centres")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size centres.size
        jsonList.foreach{ jsonObj =>
          val jsonId = CentreId((jsonObj \ "id").as[String])
          compareObj(jsonObj, centres(jsonId))
        }
      }
    }

    "POST /centres" should {
      "add a centre" in new WithApplication(fakeApplication()) {
        doLogin
        val centre = factory.createDisabledCentre
        val cmdJson = Json.obj("name" -> centre.name, "description" -> centre.description)
        val json = makeRequest(POST, "/centres", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventCentreId = (json \ "data" \ "id").as[String]
        val validation = use[BbwebPlugin].centreRepository.getByKey(CentreId(eventCentreId))
        validation should be ('success)
        validation map { repoCentre =>
          repoCentre.name should be ((json \ "data" \ "name").as[String])
        }
      }

      "add a centre with no description" in new WithApplication(fakeApplication()) {
        doLogin
        val cmdJson = Json.obj("name" -> nameGenerator.next[String])
        val json = makeRequest(POST, "/centres", json = cmdJson)

        (json \ "status").as[String] should include ("success")
      }

      "not add a centre with a name that is too short" in new WithApplication(fakeApplication()) {
        doLogin
        val cmdJson = Json.obj("name" -> "A")
        val json = makeRequest(POST, "/centres", BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] should include ("error")
      }

      "not add a centre with a duplicate name" in new WithApplication(fakeApplication()) {
        doLogin
        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val cmdJson = Json.obj("name" -> centre.name)
        val json = makeRequest(POST, "/centres", BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("centre with name already exists")
      }
    }

    "PUT /centres/:id" should {
      "update a centre" in new WithApplication(fakeApplication()) {
        doLogin
        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val cmdJson = Json.obj(
          "id"              -> centre.id.id,
          "expectedVersion" -> Some(centre.version),
          "name"            -> centre.name,
          "description"     -> centre.description)
        val json = makeRequest(PUT, s"/centres/${centre.id.id}", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventCentreId = (json \ "data" \ "id").as[String]
        val validation = use[BbwebPlugin].centreRepository.getByKey(CentreId(eventCentreId))
        validation should be ('success)
        validation map { repoCentre =>
          repoCentre.name should be ((json \ "data" \ "name").as[String])
          repoCentre.version should be ((json \ "data" \ "version").as[Long])
        }
      }

      "update a centre with no description" in new WithApplication(fakeApplication()) {
        doLogin
        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val cmdJson = Json.obj(
          "id"              -> centre.id.id,
          "expectedVersion" -> Some(centre.version),
          "name"            -> centre.name)
        val json = makeRequest(PUT, s"/centres/${centre.id.id}", json = cmdJson)

        (json \ "status").as[String] should include ("success")
      }

      "not update an invalid centre" in new WithApplication(fakeApplication()) {
        doLogin

        val centreId = nameGenerator.next[String]
        val cmdJson = Json.obj(
          "id"              -> centreId,
          "expectedVersion" -> Some(0L),
          "name"            -> nameGenerator.next[String])
        val json = makeRequest(PUT, s"/centres/${centreId}", BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("no centre with id")
      }

      "not update a centre with an invalid version" in new WithApplication(fakeApplication()) {
        doLogin
        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val cmdJson = Json.obj(
          "id"              -> centre.id.id,
          "expectedVersion" -> Some(centre.version + 1),
          "name"            -> centre.name,
          "description"     -> centre.description)
        val json = makeRequest(PUT, s"/centres/${centre.id.id}", BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("expected version doesn't match current version")
      }

      "not update a centre with a duplicate name" in new WithApplication(fakeApplication()) {
        doLogin
        val centres = List(factory.createDisabledCentre, factory.createDisabledCentre)
        centres.foreach { centre =>
          use[BbwebPlugin].centreRepository.put(centre)
        }

        val duplicateName = centres(0).name

        val cmdJson = Json.obj(
          "id"              -> centres(1).id.id,
          "expectedVersion" -> Some(centres(1).version),
          "name"            -> duplicateName,
          "description"     -> centres(1).description)
        val json = makeRequest(PUT, s"/centres/${centres(1).id.id}", BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("centre with name already exists")
      }

    }

    "GET /centres/:id" should {
      "read a centre" in new WithApplication(fakeApplication()) {
        doLogin
        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)
        val json = makeRequest(GET, s"/centres/${centre.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, centre)
      }

      "not read an invalid centre" in new WithApplication(fakeApplication()) {
        doLogin
        val json = makeRequest(GET, s"/centres/" + nameGenerator.next[String], BAD_REQUEST)

        (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("not found")
      }
    }

    "POST /centres/enable" should {
      "enable a centre" in new WithApplication(fakeApplication()) {
        doLogin
        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val cmdJson = Json.obj(
          "id" -> centre.id.id,
          "expectedVersion" -> Some(centre.version))
        val json = makeRequest(POST, "/centres/enable", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventCentreId = (json \ "data" \ "id").as[String]
        val validation = use[BbwebPlugin].centreRepository.getByKey(CentreId(eventCentreId))
        validation should be ('success)
        validation map { repoCentre =>
          repoCentre.version should be ((json \ "data" \ "version").as[Long])
        }
      }

      "not enable an invalid centre" in new WithApplication(fakeApplication()) {
        doLogin

        val cmdJson = Json.obj(
          "id" -> nameGenerator.next[String],
          "expectedVersion" -> Some(0L))
        val json = makeRequest(POST, "/centres/enable", BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("no centre with id")
      }
    }

    "POST /centres/disable" should {
      "disable a centre" in new WithApplication(fakeApplication()) {
        doLogin
        val centre = factory.createDisabledCentre.enable(Some(0), org.joda.time.DateTime.now) | fail
        use[BbwebPlugin].centreRepository.put(centre)

        val cmdJson = Json.obj(
          "id" -> centre.id.id,
          "expectedVersion" -> Some(centre.version))
        val json = makeRequest(POST, "/centres/disable", json = cmdJson)

        (json \ "status").as[String] should include ("success")

        val eventCentreId = (json \ "data" \ "id").as[String]
        val validation = use[BbwebPlugin].centreRepository.getByKey(CentreId(eventCentreId))
        validation should be ('success)
        validation map { repoCentre =>
          repoCentre.version should be ((json \ "data" \ "version").as[Long])
        }
      }

      "not disable an invalid centre" in new WithApplication(fakeApplication()) {
        doLogin
        val cmdJson = Json.obj(
          "id" -> nameGenerator.next[String],
          "expectedVersion" -> Some(0L))
        val json = makeRequest(POST, "/centres/disable", BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("no centre with id")
      }
    }

    "GET /centres/location" should {
      "list none" in new WithApplication(fakeApplication()) {
        doLogin

        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val json = makeRequest(GET, s"/centres/locations/${centre.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 0
      }

      "list a centre location" in new WithApplication(fakeApplication()) {
        doLogin

        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val location = factory.createLocation
        use[BbwebPlugin].locationRepository.put(location)
        use[BbwebPlugin].centreLocationRepository.put(CentreLocation(centre.id, location.id))

        val json = makeRequest(GET, s"/centres/locations/${centre.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 1
        compareObj(jsonList(0), location)
      }

      "list multiple centre locations" in new WithApplication(fakeApplication()) {
        doLogin
        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val locations = List(factory.createLocation, factory.createLocation)
          .map { loc => (loc.id, loc) }.toMap

        locations.values.foreach{ location =>
          use[BbwebPlugin].locationRepository.put(location)
          use[BbwebPlugin].centreLocationRepository.put(CentreLocation(centre.id, location.id))
        }

        val json = makeRequest(GET, s"/centres/locations/${centre.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size locations.size
        jsonList.foreach{ jsonObj =>
          val jsonId = LocationId((jsonObj \ "id").as[String])
          compareObj(jsonObj, locations(jsonId))
        }
      }

      "list a specific centre location" in new WithApplication(fakeApplication()) {
        doLogin
        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val locations = List(factory.createLocation, factory.createLocation)
        val locationsMap = locations.map { loc => (loc.id, loc) }.toMap

        locations.foreach{ location =>
          use[BbwebPlugin].locationRepository.put(location)
          use[BbwebPlugin].centreLocationRepository.put(CentreLocation(centre.id, location.id))
        }

        val json = makeRequest(GET, s"/centres/locations/${centre.id.id}?locationId=${locations(0).id}")
        (json \ "status").as[String] should include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        val jsonId = LocationId((jsonObj \ "id").as[String])
        compareObj(jsonObj, locationsMap(jsonId))
      }

      "does not list an invalid location" in new WithApplication(fakeApplication()) {
        doLogin
        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val inavlidLocId = nameGenerator.next[String]

        val json = makeRequest(GET, s"/centres/locations/${centre.id.id}?locationId=$inavlidLocId", BAD_REQUEST)

        (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("location does not exist")

      }
    }

    def jsonAddCentreLocationCmd(location: Location, centreId: CentreId): JsValue = {
      Json.obj(
        "centreId"       -> centreId.id,
        "name"           -> location.name,
        "street"         -> location.street,
        "city"           -> location.city,
        "province"       -> location.province,
        "postalCode"     -> location.postalCode,
        "poBoxNumber"    -> location.poBoxNumber,
        "countryIsoCode" -> location.countryIsoCode)
    }

    "POST /centres/location" should {
      "add a location" in new WithApplication(fakeApplication()) {
        doLogin
        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val location = factory.createLocation
        val json = makeRequest(POST, "/centres/locations",
          json = jsonAddCentreLocationCmd(location, centre.id))

        val jsonEvent = (json \ "data").as[JsObject]
        val eventLocationId = (json \ "data" \ "locationId").as[String]
        val validation = use[BbwebPlugin].locationRepository.getByKey(LocationId(eventLocationId))
        validation should be ('success)
        validation map { repoLocation =>
          repoLocation shouldBe a[Location]
          repoLocation should have (
            'name           (location.name),
            'street         (location.street),
            'city           (location.city),
            'province       (location.province),
            'postalCode     (location.postalCode),
            'poBoxNumber    (location.poBoxNumber),
            'countryIsoCode (location.countryIsoCode)
          )
        }

        val validation2 = use[BbwebPlugin].centreLocationRepository.getByKey(LocationId(eventLocationId))
        validation2 should be ('success)
        validation2 map { item =>
          item shouldBe a[CentreLocation]
          item should have (
            'locationId (LocationId(eventLocationId)),
            'centreId   (centre.id)
          )
        }
      }

      "fail on attempt to add a location to an invalid centre" in new WithApplication(fakeApplication()) {
        doLogin

        val centreId = CentreId(nameGenerator.next[String])
        val location = factory.createLocation
        val jsonResponse = makeRequest(POST, "/centres/locations", BAD_REQUEST,
          json = jsonAddCentreLocationCmd(location, centreId))

        (jsonResponse \ "status").as[String] should include ("error")
          (jsonResponse \ "message").as[String] should include ("no centre with id")
      }
    }

    "DELETE /centres/location" should {
      "delete a location from a centre" in new WithApplication(fakeApplication()) {
        doLogin
        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val locations = List(factory.createLocation, factory.createLocation)
        locations.foreach{ location =>
          use[BbwebPlugin].locationRepository.put(location)
          use[BbwebPlugin].centreLocationRepository.put(CentreLocation(centre.id, location.id))
        }

        locations.foreach { location =>
          val json = makeRequest(DELETE, s"/centres/locations/${centre.id.id}/${location.id.id}")
            (json \ "status").as[String] should include ("success")
        }
      }

      "delete a location from an invalid centre" in new WithApplication(fakeApplication()) {
        doLogin
        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val location = factory.createLocation
        use[BbwebPlugin].locationRepository.put(location)
        use[BbwebPlugin].centreLocationRepository.put(CentreLocation(centre.id, location.id))

        val centreId = CentreId(nameGenerator.next[String])
        val json = makeRequest(DELETE, s"/centres/locations/${centreId.id}/${location.id.id}", BAD_REQUEST)
          (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("no centre with id")
      }

      "delete an invalid location from a centre" in new WithApplication(fakeApplication()) {
        doLogin
        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val locationId = LocationId(nameGenerator.next[String])
        val json = makeRequest(DELETE, s"/centres/locations/${centre.id.id}/${locationId.id}", NOT_FOUND)
          (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("not found")
      }

    }

    "GET /centres/studies" should {
      "list none" in new WithApplication(fakeApplication()) {
        doLogin

        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val json = makeRequest(GET, s"/centres/studies/${centre.id.id}")
        (json \ "status").as[String] should include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList should have size 0
      }

      "list a centre study" in new WithApplication(fakeApplication()) {
        doLogin

        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)
        val id = StudyCentreId(nameGenerator.next[String])
        use[BbwebPlugin].studyCentreRepository.put(StudyCentre(id, study.id, centre.id))

        val json = makeRequest(GET, s"/centres/studies/${centre.id.id}")
        (json \ "status").as[String] should include ("success")
        val studyIdList = (json \ "data").as[List[String]]
        studyIdList should have size 1
        studyIdList(0) should be (study.id.id)
      }

      "list multiple centre studies" in new WithApplication(fakeApplication()) {
        doLogin

        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val studies = List(factory.createDisabledStudy, factory.createDisabledStudy)
        studies.foreach{ study =>
          use[BbwebPlugin].studyRepository.put(study)
          val id = StudyCentreId(nameGenerator.next[String])
          use[BbwebPlugin].studyCentreRepository.put(StudyCentre(id, study.id, centre.id))
        }

        val json = makeRequest(GET, s"/centres/studies/${centre.id.id}")
        (json \ "status").as[String] should include ("success")
        val studyIdList = (json \ "data").as[List[String]]
        studyIdList should have size studies.size
        studies.foreach{ study => studyIdList should contain (study.id.id) }
      }
    }

    "POST /centres/studies" should {
      "add a centre to study link" in new WithApplication(fakeApplication()) {
        doLogin

        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)

        val cmdJson = Json.obj(
          "centreId" -> centre.id.id,
          "studyId"  -> study.id.id)
        val json = makeRequest(POST, "/centres/studies", json = cmdJson)
          (json \ "data" \ "centreId").as[String] should be (centre.id.id)
          (json \ "data" \ "studyId").as[String] should be (study.id.id)

        val repoValues = use[BbwebPlugin].studyCentreRepository.getValues.toList
        repoValues should have size 1
        repoValues(0) should have (
          'centreId (centre.id),
          'studyId  (study.id)
        )
      }

    }

    "DELETE /centres/studies" should {
      "remove a centre to study link" in new WithApplication(fakeApplication()) {
        doLogin

        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val study = factory.createDisabledStudy
        use[BbwebPlugin].studyRepository.put(study)
        val id = StudyCentreId(nameGenerator.next[String])
        use[BbwebPlugin].studyCentreRepository.put(StudyCentre(id, study.id, centre.id))

        val json = makeRequest(DELETE, s"/centres/studies/${centre.id.id}/${study.id.id}")
          (json \ "status").as[String] should include ("success")
      }

      "fail on attempt remove an invalid centre to study link" in new WithApplication(fakeApplication()) {
        doLogin

        val centre = factory.createDisabledCentre
        use[BbwebPlugin].centreRepository.put(centre)

        val studyId = StudyCentreId(nameGenerator.next[String])

        val json = makeRequest(DELETE, s"/centres/studies/${centre.id.id}/${studyId}", BAD_REQUEST)
          (json \ "status").as[String] should include ("error")
          (json \ "message").as[String] should include ("centre and study not linked")
      }

    }

  }

}
