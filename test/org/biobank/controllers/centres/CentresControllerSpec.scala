package org.biobank.controllers

import org.biobank.domain.centre._
import org.biobank.domain.{ Location, LocationId, LocationRepository }
import org.biobank.domain.study.{ Study, StudyRepository }
import org.biobank.domain.JsonHelper._
import org.biobank.fixture.ControllerFixture

import play.api.test.Helpers._
import play.api.libs.json._
import org.slf4j.LoggerFactory
import play.api.Play.current
import play.api.test.FakeApplication
import org.scalatest.Tag

/**
  * Tests the REST API for [[Centre]]s.
  */
class CentresControllerSpec extends ControllerFixture {
  import org.biobank.TestUtils._

  def uri: String = "/centres"

  def uri(centre: Centre): String = uri + s"/${centre.id.id}"

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

  def compareObjs(jsonList: List[JsObject], centres: List[Centre]) = {
    val centresMap = centres.map { centre => (centre.id, centre) }.toMap
    jsonList.foreach { jsonObj =>
      val jsonId = CentreId((jsonObj \ "id").as[String])
      compareObj(jsonObj, centresMap(jsonId))
    }
  }

  "Centre REST API" when {

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
        val centres = List(
          factory.createDisabledCentre,
          factory.createEnabledCentre,
          factory.createEnabledCentre)
          .map { centre => centreRepository.put(centre) }

        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri, Map("status" -> "disabled"))
        compareObj(jsonItem, centres(0))
      }

      "list disabled centres when filtered by status" in {
        val centres = List(
          factory.createDisabledCentre,
          factory.createDisabledCentre,
          factory.createEnabledCentre,
          factory.createEnabledCentre)
          .map { centre => centreRepository.put(centre) }

        val expectedCentres = List(centres(0), centres(1))
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("status" -> "disabled"),
          offset = 0,
          total = expectedCentres.size,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size expectedCentres.size
        compareObjs(jsonItems, expectedCentres)
      }

      "list enabled centres when filtered by status" in {
        val centres = List(
          factory.createDisabledCentre,
          factory.createDisabledCentre,
          factory.createEnabledCentre,
          factory.createEnabledCentre)
          .map { centre => centreRepository.put(centre) }

        val expectedCentres = List(centres(2), centres(3))
        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
          uri = uri,
          queryParams = Map("status" -> "enabled"),
          offset = 0,
          total = expectedCentres.size,
          maybeNext = None,
          maybePrev = None)

        jsonItems must have size expectedCentres.size
        compareObjs(jsonItems, expectedCentres)
      }

      "list centres sorted by name" in {
        val centres = List(
          factory.createDisabledCentre.copy(name = "CTR3"),
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
        val centres = List(
          factory.createEnabledCentre,
          factory.createDisabledCentre)
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
        val centres = List(
          factory.createEnabledCentre,
          factory.createDisabledCentre)
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
        val centres = List(
          factory.createDisabledCentre.copy(name = "CTR3"),
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
        val centres = List(
          factory.createDisabledCentre.copy(name = "CTR3"),
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
        val centres = List(
          factory.createDisabledCentre,
          factory.createDisabledCentre,
          factory.createEnabledCentre
        )
        centres.foreach { c => centreRepository.put(c) }

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
        val json = makeRequest(POST, uri, json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val eventCentreId = (json \ "data" \ "id").as[String]
        val validation = centreRepository.getByKey(CentreId(eventCentreId))
        validation mustBe ('success)
        validation map { repoCentre =>
          repoCentre.name mustBe ((json \ "data" \ "name").as[String])
        }
        ()
      }

      "add a centre with no description" in {
        val cmdJson = Json.obj("name" -> nameGenerator.next[String])
        val json = makeRequest(POST, uri, json = cmdJson)

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

    "PUT /centres/:id" must {
      "update a centre" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val cmdJson = Json.obj(
          "id"              -> centre.id.id,
          "expectedVersion" -> Some(centre.version),
          "name"            -> centre.name,
          "description"     -> centre.description)
        val json = makeRequest(PUT, uri(centre), json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val eventCentreId = (json \ "data" \ "id").as[String]
        val validation = centreRepository.getByKey(CentreId(eventCentreId))
        validation mustBe ('success)
        validation map { repoCentre =>
          repoCentre.name mustBe ((json \ "data" \ "name").as[String])
          repoCentre.version mustBe ((json \ "data" \ "version").as[Long])
        }
        ()
      }

      "update a centre with no description" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val cmdJson = Json.obj(
          "id"              -> centre.id.id,
          "expectedVersion" -> Some(centre.version),
          "name"            -> centre.name)
        val json = makeRequest(PUT, uri(centre), json = cmdJson)

        (json \ "status").as[String] must include ("success")
      }

      "not update an invalid centre" in {

        val centre = factory.createDisabledCentre
        val cmdJson = Json.obj(
          "id"              -> centre.id.id,
          "expectedVersion" -> Some(0L),
          "name"            -> nameGenerator.next[String])
        val json = makeRequest(PUT, uri(centre), NOT_FOUND, json = cmdJson)

        (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include regex ("centre.*does not exist")
      }

      "not update a centre with an invalid version" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val cmdJson = Json.obj(
          "id"              -> centre.id.id,
          "expectedVersion" -> Some(centre.version + 1),
          "name"            -> centre.name,
          "description"     -> centre.description)
        val json = makeRequest(PUT, uri(centre), BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("expected version doesn't match current version")
      }

      "not update a centre with a duplicate name" in {
        val centres = List(factory.createDisabledCentre, factory.createDisabledCentre)
        centres.foreach { centre =>
          centreRepository.put(centre)
        }

        val duplicateName = centres(0).name

        val cmdJson = Json.obj(
          "id"              -> centres(1).id.id,
          "expectedVersion" -> Some(centres(1).version),
          "name"            -> duplicateName,
          "description"     -> centres(1).description)
        val json = makeRequest(PUT, uri(centres(1)), FORBIDDEN, json = cmdJson)

        (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("centre with name already exists")
      }

      "fail when updating an enabled centre" in {
        val centre = factory.createEnabledCentre
        centreRepository.put(centre)

        val cmdJson = Json.obj(
          "id"              -> centre.id.id,
          "expectedVersion" -> Some(centre.version),
          "name"            -> centre.name,
          "description"     -> centre.description)
        val json = makeRequest(PUT, uri(centre), BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("centre is not disabled")
      }
    }

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

    "POST /centres/enable" must {
      "enable a centre" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val cmdJson = Json.obj(
          "id" -> centre.id.id,
          "expectedVersion" -> Some(centre.version))
        val json = makeRequest(POST, uri(centre) + "/enable", json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val eventCentreId = (json \ "data" \ "id").as[String]
        val validation = centreRepository.getByKey(CentreId(eventCentreId))
        validation mustBe ('success)
        validation map { repoCentre =>
          repoCentre.version mustBe ((json \ "data" \ "version").as[Long])
        }
        ()
      }

      "not enable an invalid centre" in {
        val centre = factory.createDisabledCentre

        val cmdJson = Json.obj(
          "id" -> centre.id.id,
          "expectedVersion" -> Some(0L))
        val json = makeRequest(POST, uri(centre) + "/enable", NOT_FOUND, json = cmdJson)

        (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include regex ("centre.*does not exist")
      }

      "fail when attempting to enable and the centre ids differ" in {
        val centre = factory.createDisabledCentre

        val cmdJson = Json.obj(
          "id" -> nameGenerator.next[Centre],
          "expectedVersion" -> Some(0L))
        val json = makeRequest(POST, uri(centre) + "/enable", BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include regex ("centre id mismatch")
      }
    }

    "POST /centres/disable" must {
      "disable a centre" in {
        val centre = factory.createEnabledCentre
        centreRepository.put(centre)

        val cmdJson = Json.obj(
          "id" -> centre.id.id,
          "expectedVersion" -> Some(centre.version))
        val json = makeRequest(POST, uri(centre) + "/disable", json = cmdJson)

        (json \ "status").as[String] must include ("success")

        val eventCentreId = (json \ "data" \ "id").as[String]
        val validation = centreRepository.getByKey(CentreId(eventCentreId))
        validation mustBe ('success)
        validation map { repoCentre =>
          repoCentre.version mustBe ((json \ "data" \ "version").as[Long])
        }
        ()
      }

      "not disable an invalid centre" in {
        val centre = factory.createDisabledCentre

        val cmdJson = Json.obj(
          "id" -> centre.id.id,
          "expectedVersion" -> Some(0L))
        val json = makeRequest(POST, uri(centre) + "/disable", NOT_FOUND, json = cmdJson)

        (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("does not exist")
      }

      "fail when attempting to disable and the centre ids differ" in {
        val centre = factory.createEnabledCentre

        val cmdJson = Json.obj(
          "id" -> nameGenerator.next[Centre],
          "expectedVersion" -> Some(0L))
        val json = makeRequest(POST, uri(centre) + "/disable", BAD_REQUEST, json = cmdJson)

        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include regex ("centre id mismatch")
      }
    }

    "GET /centres/location" must {
      "list none" in {

        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val json = makeRequest(GET, uri(centre) + s"/locations")
          (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list a centre location" in {

        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val location = factory.createLocation
        locationRepository.put(location)
        centreLocationsRepository.put(CentreLocation(centre.id, location.id))

        val json = makeRequest(GET, uri(centre) + s"/locations")
          (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 1
        compareObj(jsonList(0), location)
      }

      "list multiple centre locations" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val locations = List(factory.createLocation, factory.createLocation)
          .map { loc => (loc.id, loc) }.toMap

        locations.values.foreach{ location =>
          locationRepository.put(location)
          centreLocationsRepository.put(CentreLocation(centre.id, location.id))
        }

        val json = makeRequest(GET, uri(centre) + "/locations")
          (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size locations.size
        jsonList.foreach{ jsonObj =>
          val jsonId = LocationId((jsonObj \ "id").as[String])
          compareObj(jsonObj, locations(jsonId))
        }
      }

      "list a specific centre location" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val locations = List(factory.createLocation, factory.createLocation)
        val locationsMap = locations.map { loc => (loc.id, loc) }.toMap

        locations.foreach{ location =>
          locationRepository.put(location)
          centreLocationsRepository.put(CentreLocation(centre.id, location.id))
        }

        val json = makeRequest(GET, uri(centre) + s"/locations?locationId=${locations(0).id}")
          (json \ "status").as[String] must include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        val jsonId = LocationId((jsonObj \ "id").as[String])
        compareObj(jsonObj, locationsMap(jsonId))
      }

      "does not list an invalid location" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val inavlidLocId = nameGenerator.next[String]

        val json = makeRequest(GET,
                               uri(centre) + s"/locations?locationId=$inavlidLocId",
                               NOT_FOUND)

        (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("invalid location id")

      }
    }

    "POST /centres/location" must {

      def validateJsonLocation(jsonObj: JsObject, location: Location, centre: Centre): Unit = {
        (jsonObj \ "name").as[String]           mustBe (location.name)
        (jsonObj \ "street").as[String]         mustBe (location.street)
        (jsonObj \ "city").as[String]           mustBe (location.city)
        (jsonObj \ "province").as[String]       mustBe (location.province)
        (jsonObj \ "postalCode").as[String]     mustBe (location.postalCode)
        (jsonObj \ "countryIsoCode").as[String] mustBe (location.countryIsoCode)
        (jsonObj \ "poBoxNumber").asOpt[String] mustBe (location.poBoxNumber)

        val eventLocationId = (jsonObj \ "id").as[String]
        locationRepository.getByKey(LocationId(eventLocationId)) mustSucceed { loc =>
          loc mustBe a[Location]
          loc must have (
            'name           (location.name),
            'street         (location.street),
            'city           (location.city),
            'province       (location.province),
            'postalCode     (location.postalCode),
            'poBoxNumber    (location.poBoxNumber),
            'countryIsoCode (location.countryIsoCode)
          )
        }

        centreLocationsRepository.getByKey(LocationId(eventLocationId)) mustSucceed { item =>
          item mustBe a[CentreLocation]
          item must have (
            'locationId (LocationId(eventLocationId)),
            'centreId   (centre.id)
          )
        }
      }

      "add a location to a disabled centre" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val location = factory.createLocation
        val json = makeRequest(POST,
                               uri(centre) + "/locations",
                               json = jsonAddCentreLocationCmd(location, centre.id))

        val jsonObj = (json \ "data").as[JsObject]
        validateJsonLocation(jsonObj, location, centre)
      }

      "add a location to an enabled centre" in {
        val centre = factory.createEnabledCentre
        centreRepository.put(centre)
        val location = factory.createLocation
        val json = makeRequest(POST,
                               uri(centre) + "/locations",
                               json = jsonAddCentreLocationCmd(location, centre.id))

        val jsonObj = (json \ "data").as[JsObject]
        validateJsonLocation(jsonObj, location, centre)
      }

      "fail on attempt to add a location to an invalid centre" in {
        val centre = factory.createDisabledCentre
        val location = factory.createLocation
        val jsonResponse = makeRequest(POST, uri(centre) + "/locations", NOT_FOUND,
          json = jsonAddCentreLocationCmd(location, centre.id))

        (jsonResponse \ "status").as[String] must include ("error")
          (jsonResponse \ "message").as[String] must include regex ("centre.*does not exist")
      }
    }

    "DELETE /centres/location" must {
      "delete a location from a centre" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val locations = List(factory.createLocation, factory.createLocation)
        locations.foreach{ location =>
          locationRepository.put(location)
          centreLocationsRepository.put(CentreLocation(centre.id, location.id))
        }

        locations.foreach { location =>
          val json = makeRequest(DELETE, uri(centre) + s"/locations/${location.id.id}")
            (json \ "status").as[String] must include ("success")
        }
      }

      "delete a location from an invalid centre" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val location = factory.createLocation
        locationRepository.put(location)
        centreLocationsRepository.put(CentreLocation(centre.id, location.id))

        val centre2 = factory.createDisabledCentre
        val json = makeRequest(DELETE, uri(centre2) + s"/locations/${location.id.id}", NOT_FOUND)
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include regex ("centre.*does not exist")
      }

      "delete an invalid location from a centre" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val locationId = LocationId(nameGenerator.next[String])
        val json = makeRequest(DELETE, uri(centre) + s"/locations/${locationId.id}", NOT_FOUND)
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include regex ("location.*does not exist")
      }

    }

    "GET /centres/:centreId/studies" must {

      "list none" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val json = makeRequest(GET, uri(centre) + s"/studies")
          (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data").as[List[JsObject]]
        jsonList must have size 0
      }

      "list a centre study" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val study = factory.createDisabledStudy
        studyRepository.put(study)
        val id = StudyCentreId(nameGenerator.next[String])
        centreStudiesRepository.put(StudyCentre(id, study.id, centre.id))

        val json = makeRequest(GET, uri(centre) + "/studies")
          (json \ "status").as[String] must include ("success")
        val studyIdList = (json \ "data").as[List[String]]
        studyIdList must have size 1
        studyIdList(0) mustBe (study.id.id)
      }

      "list multiple centre studies" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val studies = List(factory.createDisabledStudy, factory.createDisabledStudy)
        studies.foreach{ study =>
          studyRepository.put(study)
          val id = StudyCentreId(nameGenerator.next[StudyCentreId])
          centreStudiesRepository.put(StudyCentre(id, study.id, centre.id))
        }

        val json = makeRequest(GET, uri(centre) + "/studies")
          (json \ "status").as[String] must include ("success")
        val studyIdList = (json \ "data").as[List[String]]
        studyIdList must have size studies.size
        studies.foreach{ study => studyIdList must contain (study.id.id) }
      }
    }

    "POST /centres/:centerId/studies" must {

      def validateAddedStudy(jsonObj: JsObject, study: Study, centre: Centre): Unit = {
        (jsonObj \ "id").as[String] mustBe (study.id.id)

        compareObj(jsonObj, study)

        val repoValues = centreStudiesRepository.getValues.toList
        repoValues must have size 1
        repoValues(0) must have (
          'centreId (centre.id),
          'studyId  (study.id)
        )
      }

      "add a study to a centre" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj(
          "centreId" -> centre.id.id,
          "studyId"  -> study.id.id)
        val json = makeRequest(POST, uri(centre) + s"/studies/${study.id.id}", json = cmdJson)
        validateAddedStudy((json \ "data").as[JsObject], study, centre)
      }

      "add a study to an enabled centre" in {
        val centre = factory.createEnabledCentre
        centreRepository.put(centre)

        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj("centreId" -> centre.id.id, "studyId"  -> study.id.id)
        val json = makeRequest(POST,
                               uri(centre) + s"/studies/${study.id.id}",
                               json = cmdJson)
        validateAddedStudy((json \ "data").as[JsObject], study, centre)
      }

      "fail when adding a study to a centre and they are already linked" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val studyCentreId = StudyCentreId(nameGenerator.next[StudyCentre])
        centreStudiesRepository.put(StudyCentre(studyCentreId, study.id, centre.id))

        val cmdJson = Json.obj(
          "centreId" -> centre.id.id,
          "studyId"  -> study.id.id)
        val json = makeRequest(POST,
                               uri(centre) + s"/studies/${study.id.id}",
                               BAD_REQUEST,
                               json = cmdJson)
        (json \ "status").as[String] must include ("error")
        (json \ "message").as[String] must include ("centre and study already linked")
      }

      "fail when adding a study to a centre and the centre ids do not match" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj(
          "centreId" -> nameGenerator.next[Centre],
          "studyId"  -> study.id.id)
        val json = makeRequest(POST, uri(centre) + s"/studies/${study.id.id}", BAD_REQUEST, json = cmdJson)
        (json \ "status").as[String] mustBe ("error")
        (json \ "message").as[String] must include("centre id mismatch")
      }

      "fail when adding a study to a centre and the study ids do not match" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj(
          "centreId" -> centre.id.id,
          "studyId"  -> nameGenerator.next[Study])
        val json = makeRequest(POST, uri(centre) + s"/studies/${study.id.id}", BAD_REQUEST, json = cmdJson)
        (json \ "status").as[String] mustBe ("error")
        (json \ "message").as[String] must include("study id mismatch")
      }
    }

    "DELETE /centres/studies" must {
      "remove a study from a centre" in {

        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val study = factory.createDisabledStudy
        studyRepository.put(study)
        val id = StudyCentreId(nameGenerator.next[StudyCentreId])
        centreStudiesRepository.put(StudyCentre(id, study.id, centre.id))

        val json = makeRequest(DELETE, uri(centre) + s"/studies/${study.id.id}")
          (json \ "status").as[String] must include ("success")
      }

      "fail on attempt remove an invalid study from a centre" in {

        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val studyId = StudyCentreId(nameGenerator.next[StudyCentreId])

        val json = makeRequest(DELETE, uri(centre) + s"/studies/${studyId}", BAD_REQUEST)
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("centre and study not linked")
      }

    }

  }

}
