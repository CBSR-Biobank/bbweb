package org.biobank.controllers

import org.biobank.fixture.NameGenerator
import org.biobank.domain.centre._
import org.biobank.domain.{ Location, LocationId }
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.infrastructure.event.CentreEvents._
import org.biobank.domain.JsonHelper._
import org.biobank.fixture.ControllerFixture

import play.api.test.Helpers._
import play.api.test.WithApplication
import play.api.libs.json._
import org.slf4j.LoggerFactory
import com.typesafe.plugin._
import play.api.Play.current
import play.api.test.FakeApplication
import org.scalatest.Tag
import org.scalatestplus.play._

/**
  * Tests the REST API for [[Centre]]s.
  */
class CentresControllerSpec extends ControllerFixture {
  import TestGlobal._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

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
        val json = makeRequest(GET, uri)
          (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data" \ "items").as[List[JsObject]]
        jsonList must have size 0

        (json \ "data" \ "offset").as[Long] must be (0)
          (json \ "data" \ "total").as[Long] must be (0)
          (json \ "data" \ "prev").as[Option[Int]] must be (None)
          (json \ "data" \ "next").as[Option[Int]] must be (None)
      }

      "list a centre" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val json = makeRequest(GET, uri)
          (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data" \ "items").as[List[JsObject]]
        jsonList must have length 1
        compareObj(jsonList(0), centre)
      }

      "list multiple centres" in {
        val centres = List(factory.createDisabledCentre, factory.createDisabledCentre)
          .map{ centre => centreRepository.put(centre) }

        val json = makeRequest(GET, uri)
          (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data" \ "items").as[List[JsObject]]
        jsonList must have size centres.size
        compareObjs(jsonList, centres)
      }

      "list a single centre when filtered by name" in {
        val centres = List(factory.createDisabledCentre, factory.createEnabledCentre)
          .map { centre => centreRepository.put(centre) }

        val json = makeRequest(GET, uri + "?filter=" + centres(0).name)
          (json \ "status").as[String] must include ("success")
        val jsonList = (json \ "data" \ "items").as[List[JsObject]]
        jsonList must have size 1
        compareObj(jsonList(0), centres(0))

        (json \ "data" \ "offset").as[Long] must be (0)
          (json \ "data" \ "total").as[Long] must be (1)
          (json \ "data" \ "prev").as[Option[Int]] must be (None)
          (json \ "data" \ "next").as[Option[Int]] must be (None)
      }

      "list a single disabled centre when filtered by status" taggedAs(Tag("1")) in {
        val centres = List(
          factory.createDisabledCentre,
          factory.createEnabledCentre,
          factory.createEnabledCentre)
          .map { centre => centreRepository.put(centre) }

        val json = makeRequest(GET, uri + "?status=disabled")
          (json \ "status").as[String] must include ("success")

        val jsonList = (json \ "data" \ "items").as[List[JsObject]]
        jsonList must have size 1
        compareObj(jsonList(0), centres(0))

        (json \ "data" \ "offset").as[Long] must be (0)
          (json \ "data" \ "total").as[Long] must be (1)
          (json \ "data" \ "prev").as[Option[Int]] must be (None)
          (json \ "data" \ "next").as[Option[Int]] must be (None)
      }

      "list disabled centres when filtered by status" in {
        val centres = List(
          factory.createDisabledCentre,
          factory.createDisabledCentre,
          factory.createEnabledCentre,
          factory.createEnabledCentre)
          .map { centre => centreRepository.put(centre) }

        val json = makeRequest(GET, uri + "?status=disabled")
          (json \ "status").as[String] must include ("success")

        val jsonList = (json \ "data" \ "items").as[List[JsObject]]
        jsonList must have size 2
        compareObj(jsonList(0), centres(0))

        (json \ "data" \ "offset").as[Long] must be (0)
          (json \ "data" \ "total").as[Long] must be (1)
          (json \ "data" \ "prev").as[Option[Int]] must be (None)
          (json \ "data" \ "next").as[Option[Int]] must be (None)
      }

      "list enabled centres when filtered by status" in {
      }

      "list centres sorted by name" in {
      }

      "list centres sorted by status" in {
      }

      "list a single centre when using paged query" in {
      }

      "list centres sorted by status in descending order" in {
      }

      "fail when using an invalid status" in {
      }

      "fail when using an invalid page number" in {
      }

      "fail when using an invalid page number that exeeds limits" in {
      }

      "fail when using an invalid page size" in {
      }

      "fail when using an invalid sort order" in {
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
          (json \ "message").as[String] must include ("no centre with id")
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

        val json = makeRequest(GET, uri(centre), BAD_REQUEST)

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
          (json \ "message").as[String] must include ("no centre with id")
      }
    }

    "POST /centres/disable" must {
      "disable a centre" in {
        val centre = factory.createDisabledCentre.enable | fail
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
          (json \ "message").as[String] must include ("no centre with id")
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

        val json = makeRequest(GET, uri(centre) + s"/locations?locationId=$inavlidLocId", BAD_REQUEST)

        (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("invalid location id")

      }
    }

    "POST /centres/location" must {
      "add a location" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val location = factory.createLocation
        val json = makeRequest(POST, uri(centre) + "/locations",
          json = jsonAddCentreLocationCmd(location, centre.id))

        val jsonEvent = (json \ "data").as[JsObject]
        val eventLocationId = (json \ "data" \ "locationId").as[String]
        val validation = locationRepository.getByKey(LocationId(eventLocationId))
        validation mustBe ('success)
        validation map { repoLocation =>
          repoLocation mustBe a[Location]
          repoLocation must have (
            'name           (location.name),
            'street         (location.street),
            'city           (location.city),
            'province       (location.province),
            'postalCode     (location.postalCode),
            'poBoxNumber    (location.poBoxNumber),
            'countryIsoCode (location.countryIsoCode)
          )
        }

        val validation2 = centreLocationsRepository.getByKey(LocationId(eventLocationId))
        validation2 mustBe ('success)
        validation2 map { item =>
          item mustBe a[CentreLocation]
          item must have (
            'locationId (LocationId(eventLocationId)),
            'centreId   (centre.id)
          )
        }
        ()
      }

      "fail on attempt to add a location to an invalid centre" in {

        val centre = factory.createDisabledCentre
        val location = factory.createLocation
        val jsonResponse = makeRequest(POST, uri(centre) + "/locations", NOT_FOUND,
          json = jsonAddCentreLocationCmd(location, centre.id))

        (jsonResponse \ "status").as[String] must include ("error")
          (jsonResponse \ "message").as[String] must include ("no centre with id")
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
          (json \ "message").as[String] must include ("no centre with id")
      }

      "delete an invalid location from a centre" in {
        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val locationId = LocationId(nameGenerator.next[String])
        val json = makeRequest(DELETE, uri(centre) + s"/locations/${locationId.id}", NOT_FOUND)
          (json \ "status").as[String] must include ("error")
          (json \ "message").as[String] must include ("no location with id")
      }

    }

    "GET /centres/studies" must {
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

    "POST /centres/studies" must {
      "add a study to a centre" in {

        val centre = factory.createDisabledCentre
        centreRepository.put(centre)

        val study = factory.createDisabledStudy
        studyRepository.put(study)

        val cmdJson = Json.obj(
          "centreId" -> centre.id.id,
          "studyId"  -> study.id.id)
        val json = makeRequest(POST, uri(centre) + s"/studies/${study.id.id}", json = cmdJson)
          (json \ "data" \ "centreId").as[String] mustBe (centre.id.id)
          (json \ "data" \ "studyId").as[String] mustBe (study.id.id)

        val repoValues = centreStudiesRepository.getValues.toList
        repoValues must have size 1
        repoValues(0) must have (
          'centreId (centre.id),
          'studyId  (study.id)
        )
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
