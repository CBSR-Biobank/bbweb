package org.biobank.controllers

import org.biobank.domain.centre._
import org.biobank.domain.Location
import org.biobank.domain.study.{ Study, StudyRepository }
import org.biobank.domain.JsonHelper
import org.biobank.fixture.ControllerFixture

import org.joda.time.DateTime
import play.api.test.Helpers._
import play.api.libs.json._
import org.slf4j.LoggerFactory
import play.api.Play.current
import play.api.test.FakeApplication
import org.scalatest.Tag

/**
  * Tests the REST API for [[Centre]]s.
  */
class CentresControllerSpec extends ControllerFixture with JsonHelper {
  import org.biobank.TestUtils._

  def uri(): String = "/centres"

  def uri(path: String): String = uri + s"/$path"

  def uri(centre: Centre): String = uri + s"/${centre.id.id}"

  def uri(centre: Centre, path: String): String = uri(path) + s"/${centre.id.id}"


  def jsonAddCentreLocationCmd(centre: Centre, location: Location): JsObject = {
    Json.obj(
      "id"              -> centre.id,
      "expectedVersion" -> centre.version,
      "name"            -> location.name,
      "street"          -> location.street,
      "city"            -> location.city,
      "province"        -> location.province,
      "postalCode"      -> location.postalCode,
      "poBoxNumber"     -> location.poBoxNumber,
      "countryIsoCode"  -> location.countryIsoCode)
  }

  def compareObjs(jsonList: List[JsObject], centres: List[Centre]) = {
    val centresMap = centres.map { centre => (centre.id, centre) }.toMap
    jsonList.foreach { jsonObj =>
      val jsonId = CentreId((jsonObj \ "id").as[String])
      compareObj(jsonObj, centresMap(jsonId))
    }
  }

  def checkInvalidCentreId(path: String, jsonField: JsObject): Unit = {
    var invalidCentreId = nameGenerator.next[Centre]
    var cmdJson = Json.obj("id"              -> nameGenerator.next[Centre],
                           "expectedVersion" -> 0L) ++ jsonField

    val json = makeRequest(POST, s"/centres/$path/$invalidCentreId", NOT_FOUND, cmdJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must startWith ("centre with id does not exist")
  }

  def checkInvalidCentreId(url: String): Unit = {
    checkInvalidCentreId(url, Json.obj())
  }

  def updateWithInvalidVersion(path: String, jsonField: JsObject): Unit = {
    val centre = factory.createDisabledCentre
    centreRepository.put(centre)

    var cmdJson = Json.obj("id"              -> centre.id.id,
                           "expectedVersion" -> Some(centre.version + 1)) ++ jsonField

    val json = makeRequest(POST, uri(centre, path), BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include ("expected version doesn't match current version")
  }

  def updateWithInvalidVersion(url: String): Unit = {
    updateWithInvalidVersion(url, Json.obj())
  }

  def updateEnabledCentre(centre: EnabledCentre, path: String, jsonField: JsObject): Unit = {
    centreRepository.put(centre)

    var cmdJson = Json.obj("expectedVersion" -> Some(centre.version)) ++ jsonField

    val json = makeRequest(POST, uri(centre, path), BAD_REQUEST, cmdJson)

    (json \ "status").as[String] must include ("error")

    (json \ "message").as[String] must include ("centre is not disabled")
  }

  "Centre REST API" when {

    "GET /centres/:id" must {

      "read a centre" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)
        val json = makeRequest(GET, uri(centre))
          (json \ "status").as[String] must include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, centre)
      }

      "not read an invalid centre" in {
        val centre = factory.createDisabledCentre

        val json = makeRequest(GET, uri(centre), NOT_FOUND)

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("invalid centre id")
      }
    }

    "GET /centres" must {

      "list none" in {
        PagedResultsSpec(this).emptyResults(uri)
      }

      "list a centre" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)
        val jsonItem = PagedResultsSpec(this).singleItemResult(uri)
        compareObj(jsonItem, centre)
      }

      "list multiple centres" in {
        val centres = List(factory.createDisabledCentre, factory.createDisabledCentre)
          .map{ centre => centreRepository.put(centre) }

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          offset = 0,
          total = centres.size,
          maybeNext = None,
          maybePrev = None)
        jsonItems must have size centres.size
        compareObjs(jsonItems, centres)
      }

      "list a single centre when filtered by name" in {
        val centres = List(factory.createDisabledCentre, factory.createEnabledCentre)
          .map { centre => centreRepository.put(centre) }

        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri, Map("filter" -> centres(0).name))
        compareObj(jsonItem, centres(0))
      }

      "list a single disabled centre when filtered by status" in {
        val centres = List(factory.createDisabledCentre,
                           factory.createEnabledCentre,
                           factory.createEnabledCentre)
          .map { centre => centreRepository.put(centre) }

        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri, Map("status" -> "DisabledCentre"))
        compareObj(jsonItem, centres(0))
      }

      "list disabled centres when filtered by status" in {
        val centres = List(factory.createDisabledCentre,
                           factory.createDisabledCentre,
                           factory.createEnabledCentre,
                           factory.createEnabledCentre)
          .map { centre => centreRepository.put(centre) }

        val expectedCentres = List(centres(0), centres(1))
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("status" -> "DisabledCentre"),
          offset = 0,
          total = expectedCentres.size,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size expectedCentres.size
        compareObjs(jsonItems, expectedCentres)
      }

      "list enabled centres when filtered by status" in {
        val centres = List(factory.createDisabledCentre,
                           factory.createDisabledCentre,
                           factory.createEnabledCentre,
                           factory.createEnabledCentre)
          .map { centre => centreRepository.put(centre) }

        val expectedCentres = List(centres(2), centres(3))
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("status" -> "EnabledCentre"),
          offset = 0,
          total = expectedCentres.size,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size expectedCentres.size
        compareObjs(jsonItems, expectedCentres)
      }

      "list centres sorted by name" in {
        val centres = List(factory.createDisabledCentre.copy(name = "CTR3"),
                           factory.createDisabledCentre.copy(name = "CTR2"),
                           factory.createEnabledCentre.copy(name = "CTR1"),
                           factory.createEnabledCentre.copy(name = "CTR0"))
          .map { centre => centreRepository.put(centre) }

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("sort" -> "name"),
          offset = 0,
          total = centres.size,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size centres.size
        compareObj(jsonItems(0), centres(3))
        compareObj(jsonItems(1), centres(2))
        compareObj(jsonItems(2), centres(1))
        compareObj(jsonItems(3), centres(0))
      }

      "list centres sorted by status" in {
        val centres = List(factory.createEnabledCentre, factory.createDisabledCentre)
          .map { centre => centreRepository.put(centre) }

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("sort" -> "status"),
          offset = 0,
          total = centres.size,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size centres.size
        compareObj(jsonItems(0), centres(1))
        compareObj(jsonItems(1), centres(0))
      }

      "list centres sorted by status in descending order" in {
        val centres = List(factory.createEnabledCentre, factory.createDisabledCentre)
          .map { centre => centreRepository.put(centre) }

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("sort" -> "status", "order" -> "desc"),
          offset = 0,
          total = centres.size,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size centres.size
        compareObj(jsonItems(0), centres(0))
        compareObj(jsonItems(1), centres(1))
      }

      "list a single centre when using paged query" in {
        val centres = List(factory.createDisabledCentre.copy(name = "CTR3"),
                           factory.createDisabledCentre.copy(name = "CTR2"),
                           factory.createEnabledCentre.copy(name = "CTR1"),
                           factory.createEnabledCentre.copy(name = "CTR0"))
          .map { centre => centreRepository.put(centre) }

        val jsonItem = PagedResultsSpec(this).singleItemResult(
          uri = uri,
          queryParams = Map("sort" -> "name", "pageSize" -> "1"),
          total = centres.size,
          maybeNext = Some(2))

        compareObj(jsonItem, centres(3))
      }

      "list the last centre when using paged query" in {
        val centres = List(factory.createDisabledCentre.copy(name = "CTR3"),
                           factory.createDisabledCentre.copy(name = "CTR2"),
                           factory.createEnabledCentre.copy(name = "CTR1"),
                           factory.createEnabledCentre.copy(name = "CTR0"))
          .map { centre => centreRepository.put(centre) }

        val jsonItem = PagedResultsSpec(this).singleItemResult(
          uri = uri,
          queryParams = Map("sort" -> "name", "page" -> "4", "pageSize" -> "1"),
          total = 4,
          offset = 3,
          maybeNext = None,
          maybePrev = Some(3))

        compareObj(jsonItem, centres(0))
      }

      "fail when using an invalid query parameters" in {
        PagedResultsSpec(this).failWithInvalidParams(uri)
      }

    }

    "GET /centres/counts" must {

      "return empty counts" in {
        val json = makeRequest(GET, uri + "/counts")
        (json \ "status").as[String] must include ("success")
        (json \ "data" \ "total").as[Long] must be (0)
        (json \ "data" \ "disabledCount").as[Long] must be (0)
        (json \ "data" \ "enabledCount").as[Long] must be (0)
      }

      "return valid counts" in {
        val centres = List(factory.createDisabledCentre,
                           factory.createDisabledCentre,
                           factory.createEnabledCentre)
          .foreach { c => centreRepository.put(c) }

        val json = makeRequest(GET, uri + "/counts")
        (json \ "status").as[String] must include ("success")
        (json \ "data" \ "total").as[Long] must be (3)
        (json \ "data" \ "disabledCount").as[Long] must be (2)
        (json \ "data" \ "enabledCount").as[Long] must be (1)
      }

    }

    "POST /centres" must {

      "add a centre" in {
        val centre = factory.createDisabledCentre
        val cmdJson = Json.obj("name" -> centre.name, "description" -> centre.description)
        val json = makeRequest(POST, uri, cmdJson)

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

      "add a centre with no description" in {
        val cmdJson = Json.obj("name" -> nameGenerator.next[String])
        val json = makeRequest(POST, uri, cmdJson)

        (json \ "status").as[String] must include ("success")
      }

      "not add a centre with a name that is too short" in {
        val cmdJson = Json.obj("name" -> "A")
        val json = makeRequest(POST, uri, BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] must include ("error")
      }

      "fail when adding a centre with a duplicate name" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val cmdJson = Json.obj("name" -> centre.name)
        val json = makeRequest(POST, uri, FORBIDDEN, json = cmdJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("centre with name already exists")
      }
    }

    "POST /centres/name/:id" must {

      "update a centre's name" in {
        val newName = nameGenerator.next[Centre]
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val cmdJson = Json.obj("expectedVersion" -> Some(centre.version),
                               "name"            -> newName)
        val json = makeRequest(POST, uri(centre, "name"), json = cmdJson)

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

          repoCentre.studyIds must have size centre.studyIds.size
          repoCentre.locations must have size centre.locations.size
          checkTimeStamps(repoCentre, centre.timeAdded, DateTime.now)
        }
      }

      "not update a centre with a duplicate name" in {
        val centres = (1 to 2).map { _ =>
            val centre = factory.createDisabledCentre
            centreRepository.put(centre)
            centre
          }

        val duplicateName = centres(0).name

        val cmdJson = Json.obj("expectedVersion" -> Some(centres(1).version),
                               "name"            -> duplicateName)
        val json = makeRequest(POST, uri(centres(1), "name"), FORBIDDEN, json = cmdJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("centre with name already exists")
      }

      "not update name on an invalid centre" in {
        checkInvalidCentreId("name", Json.obj("name" -> nameGenerator.next[Study]))
      }

      "fail when updating name with invalid version" in {
        updateWithInvalidVersion("name", Json.obj("name" -> nameGenerator.next[Study]))
      }

      "fail when updating name on an enabled centre" in {
        val centre = factory.createEnabledCentre
        updateEnabledCentre(centre, "name", Json.obj("name" -> nameGenerator.next[Centre]))
      }

    }

    "POST /centres/description/:id" must {

      "update a centre's description" in {
        val newDescription = nameGenerator.next[Centre]
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val cmdJson = Json.obj("expectedVersion" -> Some(centre.version),
                               "description"     -> newDescription)
        val json = makeRequest(POST, uri(centre, "description"), json = cmdJson)

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

          repoCentre.studyIds must have size centre.studyIds.size
          repoCentre.locations must have size centre.locations.size
          checkTimeStamps(repoCentre, centre.timeAdded, DateTime.now)
        }
      }

      "not update description an invalid centre" in {
        checkInvalidCentreId("description", Json.obj("description" -> nameGenerator.next[Study]))
      }

      "fail when updating name with invalid version" in {
        updateWithInvalidVersion("description", Json.obj("description" -> nameGenerator.next[Study]))
      }

      "fail when updating description on an enabled centre" in {
        val centre = factory.createEnabledCentre
        updateEnabledCentre(centre, "description", Json.obj("description" -> nameGenerator.next[Centre]))
      }

    }

    "POST /centres/enable/:id" must {

      "enable a centre" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val cmdJson = Json.obj("expectedVersion" -> Some(centre.version))
        val json = makeRequest(POST, uri(centre, "enable"), json = cmdJson)

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

          repoCentre.studyIds must have size centre.studyIds.size
          repoCentre.locations must have size centre.locations.size
          checkTimeStamps(repoCentre, centre.timeAdded, DateTime.now)
        }
      }

      "not enable an invalid centre" in {
        val centre = factory.createDisabledCentre

        val cmdJson = Json.obj("expectedVersion" -> Some(0L))
        val json = makeRequest(POST, uri(centre, "enable"), NOT_FOUND, json = cmdJson)

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include regex ("centre.*does not exist")
      }

    }

    "POST /centres/disable/:id" must {

      "disable a centre" in {
        val centre = factory.createEnabledCentre
        centreRepository.put(centre)

        val cmdJson = Json.obj("expectedVersion" -> Some(centre.version))
        val json = makeRequest(POST, uri(centre, "disable"), json = cmdJson)

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

          repoCentre.studyIds must have size centre.studyIds.size
          repoCentre.locations must have size centre.locations.size
          checkTimeStamps(repoCentre, centre.timeAdded, DateTime.now)
        }
      }

      "not disable an invalid centre" in {
        val centre = factory.createDisabledCentre

        val cmdJson = Json.obj("expectedVersion" -> Some(0L))
        val json = makeRequest(POST, uri(centre, "disable"), NOT_FOUND, json = cmdJson)

        (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("does not exist")
      }

    }

    "POST /centres/location/:id" must {

      def compareLocationsIgnoringId(loc1: Location, loc2: Location) = {
        loc1 must have (
          'name           (loc2.name),
          'street         (loc2.street),
          'city           (loc2.city),
          'province       (loc2.province),
          'postalCode     (loc2.postalCode),
          'countryIsoCode (loc2.countryIsoCode),
          'poBoxNumber    (loc2.poBoxNumber)
        )
      }

      def validateJsonLocation(jsonObj: JsObject, location: Location): Unit = {
        (jsonObj \ "uniqueId").as[String]       must not be empty
        (jsonObj \ "name").as[String]           mustBe (location.name)
        (jsonObj \ "street").as[String]         mustBe (location.street)
        (jsonObj \ "city").as[String]           mustBe (location.city)
        (jsonObj \ "province").as[String]       mustBe (location.province)
        (jsonObj \ "postalCode").as[String]     mustBe (location.postalCode)
        (jsonObj \ "countryIsoCode").as[String] mustBe (location.countryIsoCode)
        (jsonObj \ "poBoxNumber").asOpt[String] mustBe (location.poBoxNumber)
      }

      "add a location to a disabled centre" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val location = factory.createLocation
        val json = makeRequest(POST,
                               uri(centre, "locations"),
                               jsonAddCentreLocationCmd(centre, location))

        (json \ "status").as[String] must include ("success")

        val jsonLocations = (json \ "data" \ "locations").as[List[JsObject]]
        jsonLocations must have length 1
        validateJsonLocation(jsonLocations(0), location)

        val replyCentreId = CentreId((json \ "data" \ "id").as[String])

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

          repoCentre.studyIds must have size centre.studyIds.size
          repoCentre.locations must have size (centre.locations.size + 1)
          checkTimeStamps(repoCentre, centre.timeAdded, DateTime.now)
        }
      }

      "fail on attempt to add a location to an invalid centre" in {
        val centre = factory.createDisabledCentre
        val location = factory.createLocation
        val jsonResponse = makeRequest(POST,
                                       uri(centre, "locations"),
                                       NOT_FOUND,
                                       jsonAddCentreLocationCmd(centre, location))

        (jsonResponse \ "status").as[String] must include ("error")

        (jsonResponse \ "message").as[String] must include regex ("centre.*does not exist")
      }

      "fail when adding a location on an enabled centre" in {
        val centre = factory.createEnabledCentre
        val location = factory.createLocation
        updateEnabledCentre(centre, "locations", jsonAddCentreLocationCmd(centre, location))
      }
    }

    "DELETE /centres/locations/:id/:ver/:uniqueId" must {

      "delete a location from a centre" in {
        val locations = List(factory.createLocation, factory.createLocation)
        var locationsSet = locations.toSet
        var centre: Centre = factory.createDisabledCentre.copy(locations = locationsSet)
        centreRepository.put(centre)

        var expectedVersion = centre.version

        locations.foreach { location =>
          val json = makeRequest(DELETE,
                                 uri(centre, "locations") + s"/$expectedVersion/${location.uniqueId}")

          expectedVersion = expectedVersion + 1
          (json \ "status").as[String] must include ("success")

          locationsSet = locationsSet - location

          val jsonId = (json \ "data" \ "id").as[String]
          jsonId must be (centre.id.id)

          centreRepository.getByKey(centre.id) mustSucceed { repoCentre =>
            compareObj((json \ "data").as[JsObject], repoCentre)

            repoCentre must have (
              'id          (centre.id),
              'version     (expectedVersion),
              'name        (centre.name),
              'description (centre.description)
            )

            repoCentre.studyIds must have size centre.studyIds.size
            repoCentre.locations must have size locationsSet.size
            checkTimeStamps(repoCentre, centre.timeAdded, DateTime.now)
          }
        }
      }

      "fail when deleting a location from an invalid centre" in {
        val location = factory.createLocation
        val centre = factory.createDisabledCentre.copy(locations = Set(location))
        centreRepository.put(centre)

        val centre2 = factory.createDisabledCentre
        val json = makeRequest(DELETE,
                               uri(centre2, "locations") + s"/${centre.version}/${location.uniqueId}",
                               NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("centre.*does not exist")
      }

      "fail when deleting an invalid location from a centre" in {
        val location = factory.createLocation
        val centre = factory.createDisabledCentre.copy(locations = Set.empty)
        centreRepository.put(centre)

        val locationId = nameGenerator.next[String]
        val json = makeRequest(DELETE,
                               uri(centre, "locations") + s"/${centre.version}/${location.uniqueId}",
                               NOT_FOUND)
                              (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("location.*does not exist")
      }

    }

    "POST /centres/studies/:centerId" must {

      "add a study to a centre" in {
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

          repoCentre.studyIds must have size (centre.studyIds.size + 1)
          repoCentre.locations must have size centre.locations.size
          checkTimeStamps(repoCentre, centre.timeAdded, DateTime.now)
        }
      }

      "fail when adding a study that does not exist" in {
        val centre = factory.createDisabledCentre.copy(studyIds = Set.empty)
        centreRepository.put(centre)

        val study = factory.createDisabledStudy

        val json = makeRequest(POST,
                               uri(centre, "studies"),
                               NOT_FOUND,
                               Json.obj("expectedVersion" -> centre.version,
                                        "studyId"         -> study.id))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("study with id not found")
      }

      "fail when adding a study on an enabled centre" in {
        val centre = factory.createEnabledCentre
        val study = factory.createDisabledStudy
        updateEnabledCentre(centre, "studies", Json.obj("studyId" -> study.id))
      }

    }

    "DELETE /centres/studies/:id/:ver/:studyId" must {

      "remove a study" in {
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

          repoCentre.studyIds must have size (centre.studyIds.size - 1)
          repoCentre.locations must have size centre.locations.size
          checkTimeStamps(repoCentre, centre.timeAdded, DateTime.now)
        }
      }

      "fail on attempt remove an invalid study from a centre" in {
        val invalidStudyId = nameGenerator.next[Study]
        val centre = factory.createDisabledCentre.copy(studyIds = Set.empty)
        centreRepository.put(centre)

        val json = makeRequest(DELETE,
                               uri(centre, "studies") + s"/${centre.version}/$invalidStudyId",
                               NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("study with id not found")
      }

      "fail when removing a study on an enabled centre" in {
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

  }

}
