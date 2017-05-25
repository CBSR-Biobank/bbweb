package org.biobank.controllers.centres

import org.biobank.controllers.PagedResultsSpec
import org.biobank.domain.JsonHelper
import org.biobank.domain.Location
import org.biobank.domain.centre._
import org.biobank.domain.study.Study
import org.biobank.dto._
import org.biobank.fixture.ControllerFixture
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.test.Helpers._
import scala.language.reflectiveCalls

/**
  * Tests the REST API for [[Centre]]s.
  */
class CentresControllerSpec extends ControllerFixture with JsonHelper {
  import org.biobank.TestUtils._

  def uri(): String = "/centres/"

  def uri(path: String): String = uri + s"$path"

  def uri(centre: Centre): String = uri + s"${centre.id.id}"

  def uri(centre: Centre, path: String): String = uri(path) + s"/${centre.id.id}"

  def centreLocationToJson(centre: Centre, location: Location): JsObject = {
    Json.obj(
      "expectedVersion" -> centre.version,
      "name"            -> location.name,
      "street"          -> location.street,
      "city"            -> location.city,
      "province"        -> location.province,
      "postalCode"      -> location.postalCode,
      "poBoxNumber"     -> location.poBoxNumber,
      "countryIsoCode"  -> location.countryIsoCode)
  }

  def centreLocationToUpdateJson(centre: Centre, location: Location): JsObject = {
    centreLocationToJson(centre, location) ++ Json.obj("locationId" -> location.id.id)
  }

  def compareObjs(jsonList: List[JsObject], centres: List[Centre]) = {
    val centresMap = centres.map { centre => (centre.id, centre) }.toMap
    jsonList.foreach { jsonObj =>
      val jsonId = CentreId((jsonObj \ "id").as[String])
      compareObj(jsonObj, centresMap(jsonId))
    }
  }

  def checkInvalidCentreId(path: String, jsonField: JsObject): Unit = {
    val invalidCentreId = nameGenerator.next[Centre]
    val cmdJson = Json.obj("id"              -> nameGenerator.next[Centre],
                           "expectedVersion" -> 0L) ++ jsonField

    val json = makeRequest(POST, s"/centres/$path/$invalidCentreId", NOT_FOUND, cmdJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include regex ("IdNotFound.*centre")

    ()
  }

  def checkInvalidCentreId(url: String): Unit = {
    checkInvalidCentreId(url, Json.obj())
  }

  def updateWithInvalidVersion(path: String, jsonField: JsObject): Unit = {
    val centre = factory.createDisabledCentre
    centreRepository.put(centre)

    val cmdJson = Json.obj("id"              -> centre.id.id,
                           "expectedVersion" -> Some(centre.version + 1)) ++ jsonField

    val json = makeRequest(POST, uri(centre, path), BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include ("expected version doesn't match current version")

    ()
  }

  def updateWithInvalidVersion(url: String): Unit = {
    updateWithInvalidVersion(url, Json.obj())
  }

  def updateEnabledCentre(centre:    EnabledCentre,
                          path:      String,
                          jsonField: JsObject,
                          urlExtra:  String = ""): Unit = {
    val url = uri(centre, path) + urlExtra
    centreRepository.put(centre)

    val cmdJson = Json.obj("expectedVersion" -> Some(centre.version)) ++ jsonField

    val json = makeRequest(POST, url, BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include ("centre is not disabled")

    ()
  }

  def validateJsonLocation(jsonObj: JsObject, location: Location): Unit = {
    (jsonObj \ "id").as[String]             must not be empty

    (jsonObj \ "name").as[String]           mustBe (location.name)

    (jsonObj \ "street").as[String]         mustBe (location.street)

    (jsonObj \ "city").as[String]           mustBe (location.city)

    (jsonObj \ "province").as[String]       mustBe (location.province)

    (jsonObj \ "postalCode").as[String]     mustBe (location.postalCode)

    (jsonObj \ "poBoxNumber").asOpt[String] mustBe (location.poBoxNumber)

    (jsonObj \ "countryIsoCode").as[String] mustBe (location.countryIsoCode)

    ()
  }

  describe("Centre REST API") {

    describe("GET /centres/:id") {

      it("read a centre") {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)
        val json = makeRequest(GET, uri(centre))
          (json \ "status").as[String] must include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, centre)
      }

      it("not read an invalid centre") {
        val centre = factory.createDisabledCentre

        val json = makeRequest(GET, uri(centre), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*centre")
      }
    }

    describe("GET /centres") {

      it("list none") {
        PagedResultsSpec(this).emptyResults(uri)
      }

      it("list a centre") {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)
        val jsonItem = PagedResultsSpec(this).singleItemResult(uri)
        compareObj(jsonItem, centre)
      }

      it("list multiple centres") {
        val centres = List(factory.createDisabledCentre, factory.createDisabledCentre)
        centres.foreach(centreRepository.put)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          offset = 0,
          total = centres.size.toLong,
          maybeNext = None,
          maybePrev = None)
        jsonItems must have size centres.size.toLong
        compareObjs(jsonItems, centres)
      }

      it("list a single centre when filtered by name") {
        val centres = List(factory.createDisabledCentre, factory.createEnabledCentre)
        val centre = centres(0)
        centres.foreach(centreRepository.put)

        val jsonItem = PagedResultsSpec(this).
          singleItemResult(uri, Map("filter" -> s"name::${centre.name}"))
        compareObj(jsonItem, centres(0))
      }

      it("list a single disabled centre when filtered by state") {
        val centres = List(factory.createDisabledCentre,
                           factory.createEnabledCentre,
                           factory.createEnabledCentre)
        centres.foreach(centreRepository.put)

        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri, Map("filter" -> "state::disabled"))
        compareObj(jsonItem, centres(0))
      }

      it("list disabled centres when filtered by state") {
        val centres = List(factory.createDisabledCentre,
                           factory.createDisabledCentre,
                           factory.createEnabledCentre,
                           factory.createEnabledCentre)
        centres.foreach(centreRepository.put)

        val expectedCentres = List(centres(0), centres(1))
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("filter" -> "state::disabled"),
          offset = 0,
          total = expectedCentres.size.toLong,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size expectedCentres.size.toLong
        compareObjs(jsonItems, expectedCentres)
      }

      it("list enabled centres when filtered by status") {
        val centres = List(factory.createDisabledCentre,
                           factory.createDisabledCentre,
                           factory.createEnabledCentre,
                           factory.createEnabledCentre)
        centres.foreach(centreRepository.put)

        val expectedCentres = List(centres(2), centres(3))
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("filter" -> "state::enabled"),
          offset = 0,
          total = expectedCentres.size.toLong,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size expectedCentres.size.toLong
        compareObjs(jsonItems, expectedCentres)
      }

      it("list centres sorted by name") {
        val centres = List(factory.createDisabledCentre.copy(name = "CTR3"),
                           factory.createDisabledCentre.copy(name = "CTR2"),
                           factory.createEnabledCentre.copy(name = "CTR1"),
                           factory.createEnabledCentre.copy(name = "CTR0"))
        centres.foreach(centreRepository.put)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("sort" -> "name"),
          offset = 0,
          total = centres.size.toLong,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size centres.size.toLong
        compareObj(jsonItems(0), centres(3))
        compareObj(jsonItems(1), centres(2))
        compareObj(jsonItems(2), centres(1))
        compareObj(jsonItems(3), centres(0))
      }

      it("list centres sorted by state") {
        val centres = List(factory.createEnabledCentre, factory.createDisabledCentre)
        centres.foreach(centreRepository.put)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("sort" -> "state"),
          offset = 0,
          total = centres.size.toLong,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size centres.size.toLong
        compareObj(jsonItems(0), centres(1))
        compareObj(jsonItems(1), centres(0))
      }

      it("list centres sorted by status in descending order") {
        val centres = List(factory.createEnabledCentre, factory.createDisabledCentre)
        centres.foreach(centreRepository.put)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("sort" -> "-state"),
          offset = 0,
          total = centres.size.toLong,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size centres.size.toLong
        compareObj(jsonItems(0), centres(0))
        compareObj(jsonItems(1), centres(1))
      }

      it("fail on attempt to list centres filtered by an invalid state name") {
        val invalidStateName = "state::" + nameGenerator.next[Study]
        val reply = makeRequest(GET,
                                uri + s"?filter=$invalidStateName",
                                NOT_FOUND)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex (
          "InvalidState: entity state does not exist")
      }

      it("list a single centre when using paged query") {
        val centres = List(factory.createDisabledCentre.copy(name = "CTR3"),
                           factory.createDisabledCentre.copy(name = "CTR2"),
                           factory.createEnabledCentre.copy(name = "CTR1"),
                           factory.createEnabledCentre.copy(name = "CTR0"))
        centres.foreach(centreRepository.put)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
          uri = uri,
          queryParams = Map("sort" -> "name", "limit" -> "1"),
          total = centres.size.toLong,
          maybeNext = Some(2))

        compareObj(jsonItem, centres(3))
      }

      it("list the last centre when using paged query") {
        val centres = List(factory.createDisabledCentre.copy(name = "CTR3"),
                           factory.createDisabledCentre.copy(name = "CTR2"),
                           factory.createEnabledCentre.copy(name = "CTR1"),
                           factory.createEnabledCentre.copy(name = "CTR0"))
        centres.foreach(centreRepository.put)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
          uri = uri,
          queryParams = Map("sort" -> "name", "page" -> "4", "limit" -> "1"),
          total = 4,
          offset = 3,
          maybeNext = None,
          maybePrev = Some(3))

        compareObj(jsonItem, centres(0))
      }

      it("fail when using an invalid query parameters") {
        PagedResultsSpec(this).failWithInvalidParams(uri)
      }

    }

    describe("GET /centres/counts") {

      it("return empty counts") {
        val json = makeRequest(GET, uri("counts"))
        (json \ "status").as[String] must include ("success")
        (json \ "data" \ "total").as[Long] must be (0)
        (json \ "data" \ "disabledCount").as[Long] must be (0)
        (json \ "data" \ "enabledCount").as[Long] must be (0)
      }

      it("return valid counts") {
        val centres = List(factory.createDisabledCentre,
                           factory.createDisabledCentre,
                           factory.createEnabledCentre)
        centres.foreach(centreRepository.put)
        val json = makeRequest(GET, uri("counts"))
        (json \ "status").as[String] must include ("success")
        (json \ "data" \ "total").as[Long] must be (3)
        (json \ "data" \ "disabledCount").as[Long] must be (2)
        (json \ "data" \ "enabledCount").as[Long] must be (1)
      }

    }

    describe("POST /centres") {

      it("add a centre") {
        val centre = factory.createDisabledCentre
        val addJson = Json.obj("name" -> centre.name, "description" -> centre.description)
        val json = makeRequest(POST, uri, addJson)

        (json \ "status").as[String] must include ("success")

        val jsonId = (json \ "data" \ "id").as[String]
        val centreId = CentreId(jsonId)
        jsonId.length must be > 0

        centreRepository.getByKey(centreId) mustSucceed { repoCentre =>
          compareObj((json \ "data").as[JsObject], repoCentre)

          repoCentre must have (
            'id          (centreId),
            'version     (0L),
            'name        (centre.name),
            'description (centre.description)
            )

          repoCentre.studyIds must have size 0
          repoCentre.locations must have size 0
          checkTimeStamps(repoCentre, DateTime.now, None)
        }
      }

      it("add a centre with no description") {
        val addJson = Json.obj("name" -> nameGenerator.next[String])
        val json = makeRequest(POST, uri, addJson)

        (json \ "status").as[String] must include ("success")
      }

      it("not add a centre with a name that is too short") {
        val addJson = Json.obj("name" -> "A")
        val json = makeRequest(POST, uri, BAD_REQUEST, json = addJson)

        (json \ "status").as[String] must include ("error")
      }

      it("fail when adding a centre with a duplicate name") {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val addJson = Json.obj("name" -> centre.name)
        val json = makeRequest(POST, uri, FORBIDDEN, json = addJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("centre with name already exists")
      }
    }

    describe("POST /centres/name/:id") {

      it("update a centre's name") {
        val newName = nameGenerator.next[Centre]
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val updateJson = Json.obj("expectedVersion" -> Some(centre.version),
                               "name"            -> newName)
        val json = makeRequest(POST, uri(centre, "name"), json = updateJson)

        (json \ "status").as[String] must include ("success")

        val jsonId = (json \ "data" \ "id").as[String]
        jsonId must be (centre.id.id)

        centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
          compareObj((json \ "data").as[JsObject], repoCentre)

          repoCentre must have (
            'id          (centre.id),
            'version     (centre.version + 1),
            'name        (newName),
            'description (centre.description)
            )

          repoCentre.studyIds must have size centre.studyIds.size.toLong
          repoCentre.locations must have size centre.locations.size.toLong
          checkTimeStamps(repoCentre, centre.timeAdded, DateTime.now)
        }
      }

      it("not update a centre with a duplicate name") {
        val centres = (1 to 2).map { _ =>
            val centre = factory.createDisabledCentre
            centreRepository.put(centre)
            centre
          }

        val duplicateName = centres(0).name

        val updateJson = Json.obj("expectedVersion" -> Some(centres(1).version),
                               "name"            -> duplicateName)
        val json = makeRequest(POST, uri(centres(1), "name"), FORBIDDEN, json = updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("centre with name already exists")
      }

      it("not update name on an invalid centre") {
        checkInvalidCentreId("name", Json.obj("name" -> nameGenerator.next[Study]))
      }

      it("fail when updating name with invalid version") {
        updateWithInvalidVersion("name", Json.obj("name" -> nameGenerator.next[Study]))
      }

      it("fail when updating name on an enabled centre") {
        val centre = factory.createEnabledCentre
        updateEnabledCentre(centre, "name", Json.obj("name" -> nameGenerator.next[Centre]))
      }

    }

    describe("POST /centres/description/:id") {

      it("update a centre's description") {
        val newDescription = nameGenerator.next[Centre]
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val updateJson = Json.obj("expectedVersion" -> Some(centre.version),
                               "description"     -> newDescription)
        val json = makeRequest(POST, uri(centre, "description"), json = updateJson)

        (json \ "status").as[String] must include ("success")

        val jsonId = (json \ "data" \ "id").as[String]
        jsonId must be (centre.id.id)

        centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
          compareObj((json \ "data").as[JsObject], repoCentre)

          repoCentre must have (
            'id          (centre.id),
            'version     (centre.version + 1),
            'name        (centre.name),
            'description (Some(newDescription))
            )

          repoCentre.studyIds must have size centre.studyIds.size.toLong
          repoCentre.locations must have size centre.locations.size.toLong
          checkTimeStamps(repoCentre, centre.timeAdded, DateTime.now)
        }
      }

      it("not update description an invalid centre") {
        checkInvalidCentreId("description", Json.obj("description" -> nameGenerator.next[Study]))
      }

      it("fail when updating name with invalid version") {
        updateWithInvalidVersion("description", Json.obj("description" -> nameGenerator.next[Study]))
      }

      it("fail when updating description on an enabled centre") {
        val centre = factory.createEnabledCentre
        updateEnabledCentre(centre, "description", Json.obj("description" -> nameGenerator.next[Centre]))
      }

    }

    describe("POST /centres/enable/:id") {

      it("enable a centre with at least one location") {
        val location = factory.createLocation
        val centre = factory.createDisabledCentre.copy(locations = Set(location))
        centreRepository.put(centre)

        val updateJson = Json.obj("expectedVersion" -> Some(centre.version))
        val json = makeRequest(POST, uri(centre, "enable"), json = updateJson)

        (json \ "status").as[String] must include ("success")
        val jsonId = (json \ "data" \ "id").as[String]
        jsonId must be (centre.id.id)

        centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
          repoCentre mustBe a[EnabledCentre]
          compareObj((json \ "data").as[JsObject], repoCentre)

          repoCentre must have (
            'id          (centre.id),
            'version     (centre.version + 1),
            'name        (centre.name),
            'description (centre.description)
            )

          repoCentre.studyIds must have size centre.studyIds.size.toLong
          repoCentre.locations must have size centre.locations.size.toLong
          checkTimeStamps(repoCentre, centre.timeAdded, DateTime.now)
        }
      }

      it("not enable a centre without locations") {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val updateJson = Json.obj("expectedVersion" -> Some(centre.version))
        val json = makeRequest(POST, uri(centre, "enable"), BAD_REQUEST, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*not have locations")
      }

      it("not enable an invalid centre") {
        val centre = factory.createDisabledCentre

        val updateJson = Json.obj("expectedVersion" -> Some(0L))
        val json = makeRequest(POST, uri(centre, "enable"), NOT_FOUND, json = updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*centre")
      }

    }

    describe("POST /centres/disable/:id") {

      it("disable a centre") {
        val centre = factory.createEnabledCentre
        centreRepository.put(centre)

        val updateJson = Json.obj("expectedVersion" -> Some(centre.version))
        val json = makeRequest(POST, uri(centre, "disable"), json = updateJson)

        (json \ "status").as[String] must include ("success")

        val jsonId = (json \ "data" \ "id").as[String]
        jsonId must be (centre.id.id)

        centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
          repoCentre mustBe a[DisabledCentre]
          compareObj((json \ "data").as[JsObject], repoCentre)

          repoCentre must have (
            'id          (centre.id),
            'version     (centre.version + 1),
            'name        (centre.name),
            'description (centre.description)
            )

          repoCentre.studyIds must have size centre.studyIds.size.toLong
          repoCentre.locations must have size centre.locations.size.toLong
          checkTimeStamps(repoCentre, centre.timeAdded, DateTime.now)
        }
      }

      it("not disable an invalid centre") {
        val centre = factory.createDisabledCentre

        val updateJson = Json.obj("expectedVersion" -> Some(0L))
        val json = makeRequest(POST, uri(centre, "disable"), NOT_FOUND, json = updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*centre")
      }

    }

    describe("POST /centres/locations/:id") {

      it("add a location to a disabled centre") {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val location = factory.createLocation
        val json = makeRequest(POST,
                               uri(centre, "locations"),
                               centreLocationToJson(centre, location))

        (json \ "status").as[String] must include ("success")

        val jsonLocations = (json \ "data" \ "locations").as[List[JsObject]]
        jsonLocations must have length 1
        validateJsonLocation(jsonLocations(0), location)

        val jsonId = (json \ "data" \ "id").as[String]
        jsonId must be (centre.id.id)

        centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
          compareObj((json \ "data").as[JsObject], repoCentre)

          repoCentre must have (
            'id          (centre.id),
            'version     (centre.version + 1),
            'name        (centre.name),
            'description (centre.description)
            )

          repoCentre.studyIds must have size centre.studyIds.size.toLong
          repoCentre.locations must have size (1)
          checkTimeStamps(repoCentre, centre.timeAdded, DateTime.now)

          val repoLocation = repoCentre.locations.head

          repoLocation.id.id must not be empty
          repoLocation must have (
            'name           (location.name),
            'street         (location.street),
            'city           (location.city),
            'province       (location.province),
            'postalCode     (location.postalCode),
            'poBoxNumber    (location.poBoxNumber),
            'countryIsoCode (location.countryIsoCode)
          )
          ()
        }
      }

      it("fail on attempt to add a location to an invalid centre") {
        val centre = factory.createDisabledCentre
        val location = factory.createLocation
        val jsonResponse = makeRequest(POST,
                                       uri(centre, "locations"),
                                       NOT_FOUND,
                                       centreLocationToJson(centre, location))

        (jsonResponse \ "status").as[String] must include ("error")

        (jsonResponse \ "message").as[String] must include regex ("IdNotFound.*centre")
      }

      it("fail when adding a location on an enabled centre") {
        val centre = factory.createEnabledCentre
        val location = factory.createLocation
        updateEnabledCentre(centre, "locations", centreLocationToJson(centre, location))
      }
    }

    describe("DELETE /centres/locations/:centreId/:ver/:locationId") {

      it("delete a location from a centre") {
        val locations = List(factory.createLocation, factory.createLocation)
        var locationsSet = locations.toSet
        val centre: Centre = factory.createDisabledCentre.copy(locations = locationsSet)
        centreRepository.put(centre)

        locations.zipWithIndex.foreach { case (location, index) =>
          val expectedVersion = centre.version + index
          val json = makeRequest(DELETE,
                                 uri(centre, "locations") + s"/$expectedVersion/${location.id}")

          (json \ "status").as[String] must include ("success")

          locationsSet = locationsSet - location

          val jsonId = (json \ "data" \ "id").as[String]
          jsonId must be (centre.id.id)

          centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
            compareObj((json \ "data").as[JsObject], repoCentre)

            repoCentre must have (
              'id          (centre.id),
              'version     (expectedVersion + 1),
              'name        (centre.name),
              'description (centre.description)
            )

            repoCentre.studyIds must have size centre.studyIds.size.toLong
            repoCentre.locations must have size locationsSet.size.toLong
            checkTimeStamps(repoCentre, centre.timeAdded, DateTime.now)
          }
        }
      }

      it("fail when deleting a location from an invalid centre") {
        val location = factory.createLocation
        val centre = factory.createDisabledCentre.copy(locations = Set(location))
        centreRepository.put(centre)

        val centre2 = factory.createDisabledCentre
        val json = makeRequest(DELETE,
                               uri(centre2, "locations") + s"/${centre.version}/${location.id}",
                               NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*centre")
      }

      it("fail when deleting an invalid location from a centre") {
        val location = factory.createLocation
        val centre = factory.createDisabledCentre.copy(locations = Set.empty)
        centreRepository.put(centre)

        val json = makeRequest(DELETE,
                               uri(centre, "locations") + s"/${centre.version}/${location.id}",
                               NOT_FOUND)
                              (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("location.*does not exist")
      }

    }

    describe("POST /centres/studies/:centerId") {

      it("add a study to a centre") {
        val centre = factory.createDisabledCentre.copy(studyIds = Set.empty)
        centreRepository.put(centre)

        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val json = makeRequest(POST,
                               uri(centre, "studies"),
                               Json.obj("expectedVersion" -> centre.version,
                                        "studyId"         -> study.id))

        (json \ "status").as[String] must include ("success")

        val jsonId = (json \ "data" \ "id").as[String]
        jsonId must be (centre.id.id)

        centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
          compareObj((json \ "data").as[JsObject], repoCentre)

          repoCentre must have (
            'id          (centre.id),
            'version     (centre.version + 1),
            'name        (centre.name),
            'description (centre.description)
            )

          repoCentre.studyIds must have size (centre.studyIds.size.toLong + 1L)
          repoCentre.locations must have size centre.locations.size.toLong
          checkTimeStamps(repoCentre, centre.timeAdded, DateTime.now)
        }
      }

      it("fail when adding a study that does not exist") {
        val centre = factory.createDisabledCentre.copy(studyIds = Set.empty)
        centreRepository.put(centre)

        val study = factory.createDisabledStudy

        val json = makeRequest(POST,
                               uri(centre, "studies"),
                               NOT_FOUND,
                               Json.obj("expectedVersion" -> centre.version,
                                        "studyId"         -> study.id))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*study")
      }

      it("fail when adding a study on an enabled centre") {
        val centre = factory.createEnabledCentre
        val study = factory.createDisabledStudy
        updateEnabledCentre(centre, "studies", Json.obj("studyId" -> study.id))
      }

    }

    describe("DELETE /centres/studies/:id/:ver/:studyId") {

      it("remove a study") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val centre = factory.createDisabledCentre.copy(studyIds = Set(study.id))
        centreRepository.put(centre)

        val json = makeRequest(DELETE, uri(centre, "studies") + s"/${centre.version}/${study.id.id}")

        (json \ "status").as[String] must include ("success")

        val jsonId = (json \ "data" \ "id").as[String]
        jsonId must be (centre.id.id)

        centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
          compareObj((json \ "data").as[JsObject], repoCentre)

          repoCentre must have (
            'id          (centre.id),
            'version     (centre.version + 1),
            'name        (centre.name),
            'description (centre.description)
            )

          repoCentre.studyIds must have size (centre.studyIds.size.toLong - 1)
          repoCentre.locations must have size centre.locations.size.toLong
          checkTimeStamps(repoCentre, centre.timeAdded, DateTime.now)
        }
      }

      it("fail on attempt remove an invalid study from a centre") {
        val invalidStudyId = nameGenerator.next[Study]
        val centre = factory.createDisabledCentre.copy(studyIds = Set.empty)
        centreRepository.put(centre)

        val json = makeRequest(DELETE,
                               uri(centre, "studies") + s"/${centre.version}/$invalidStudyId",
                               NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*study")
      }

      it("fail when removing a study on an enabled centre") {
        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val centre = factory.createEnabledCentre.copy(studyIds = Set(study.id))
        centreRepository.put(centre)

        val json = makeRequest(DELETE,
                               uri(centre, "studies") + s"/${centre.version}/${study.id.id}",
                               BAD_REQUEST)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("centre is not disabled")
      }

    }

    describe("GET /centres/names") {

      describe("must return centre names") {

        def createFixture() = {
          val _centres = (1 to 2).map {_ => factory.createDisabledCentre }
          val _nameDtos = _centres.map(_.nameDto).toSeq
          _centres.foreach(centreRepository.put)

          new {
            val centres = _centres
            val nameDtos = _nameDtos
          }
        }

        it("in ascending order") {
          val f = createFixture
          val nameDtos = f.nameDtos.sortWith { (a, b) => (a.name compareToIgnoreCase b.name) < 0 }

          val json = makeRequest(GET, uri("names"))

          (json \ "status").as[String] must include ("success")

          val jsonObjs = (json \ "data").as[List[JsObject]]

          jsonObjs.size must be (nameDtos.size)
          jsonObjs.zip(nameDtos).foreach { case (jsonObj, nameDtos) =>
            compareObj(jsonObj, nameDtos)
          }
        }

        it("in reverse order") {
          val f = createFixture
          val nameDtos = f.nameDtos.sortWith { (a, b) => (a.name compareToIgnoreCase b.name) > 0 }

          val json = makeRequest(GET, uri("names") + "?sort=-name")

          (json \ "status").as[String] must include ("success")

          val jsonObjs = (json \ "data").as[List[JsObject]]

          jsonObjs.size must be (nameDtos.size)
          jsonObjs.zip(nameDtos).foreach { case (jsonObj, nameDtos) =>
            compareObj(jsonObj, nameDtos)
          }
        }
      }

      it("must return centre names filtered by name") {
        val centres = (1 to 2).map {_ => factory.createDisabledCentre }
        centres.foreach(centreRepository.put)
        val centre = centres.head

        val json = makeRequest(GET, uri("names") + s"?filter=name::${centre.name}")

        (json \ "status").as[String] must include ("success")

        val jsonObjs = (json \ "data").as[List[JsObject]]

        jsonObjs.size must be (1)
        compareObj(jsonObjs(0), centre.nameDto)
      }

    }

    describe("POST /centres/locations") {

      it("return centre locations") {
        val location = factory.createLocation
        val centre = factory.createDisabledCentre.copy(locations = Set(location))
        centreRepository.put(centre)

        val reqJson = Json.obj("filter" -> "", "limit" -> 10)
        val reply = makeRequest(POST, uri("locations"), reqJson)

        (reply \ "status").as[String] must include ("success")

        val jsonObjs = (reply \ "data").as[List[JsObject]]

        jsonObjs.size must be (1)
        compareObj(jsonObjs(0),
                   CentreLocationInfo(centre.id.id, location.id.id, centre.name, location.name))
      }

      it("return centre locations filtered by name") {
        val centres = (1 to 2).map {_ =>
            factory.createDisabledCentre.copy(locations = Set(factory.createLocation))
          }
        val centre = centres(0)
        val location = centre.locations.head
        centres.foreach(centreRepository.put)

        val reqJson = Json.obj("filter" -> location.name, "limit" -> 10)
        val reply = makeRequest(POST, uri("locations"), reqJson)

        (reply \ "status").as[String] must include ("success")

        val jsonObjs = (reply \ "data").as[List[JsObject]]

        jsonObjs.size must be (1)
        compareObj(jsonObjs(0),
                   CentreLocationInfo(centre.id.id, location.id.id, centre.name, location.name))
      }

      it("return centre locations sorted by name") {
        val centres = (1 to 2).map {_ =>
            factory.createDisabledCentre.copy(locations = Set(factory.createLocation))
          }
        centres.foreach(centreRepository.put)

        val centreLocationsByName = centres.
          map { centre =>
            val location = centre.locations.head
            CentreLocationInfo(centre.id.id, location.id.id, centre.name, location.name)
          }.
          toSeq.
          sortWith { (a, b) => (a.name compareToIgnoreCase b.name) < 0 }.
          toList

        val reqJson = Json.obj("filter" -> "", "limit" -> 10)
        val reply = makeRequest(POST, uri("locations"), reqJson)

        (reply \ "status").as[String] must include ("success")

        val jsonObjs = (reply \ "data").as[List[JsObject]]

        jsonObjs.size must be (centres.size)
        jsonObjs.zip(centreLocationsByName).foreach { case (jsonObj, centreLocation) =>
          compareObj(jsonObj, centreLocation)
        }
      }

    }

  }

}
