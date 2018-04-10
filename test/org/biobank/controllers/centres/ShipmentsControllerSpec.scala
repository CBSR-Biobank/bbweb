package org.biobank.controllers.centres

import java.time.OffsetDateTime
import org.biobank.controllers.PagedResultsSpec
import org.biobank.domain.{EntityState, LocationId}
import org.biobank.domain.centres._
import org.scalatest.prop.TableDrivenPropertyChecks._
import play.api.libs.json._
import play.api.test.Helpers._

/**
 * Tests the REST API for [[Shipment]]s.
 *
 * Tests for [[ShipmentSpecimen]]s in ShipmentSpecimensControllerSpec.scala.
 */
class ShipmentsControllerSpec
    extends ShipmentsControllerSpecFixtures
    with ShipmentsControllerSpecUtils {

  import org.biobank.TestUtils._

  val states = Table("state",
                     Shipment.createdState,
                     Shipment.packedState,
                     Shipment.sentState,
                     Shipment.receivedState,
                     Shipment.unpackedState,
                     Shipment.lostState)

  def centreFilters(fromCentre: Centre, toCentre: Centre) =
    Table("centre filters",
          s"fromCentre:in:${fromCentre.name}",
          s"toCentre:in:${toCentre.name}",
          s"withCentre:in:(${fromCentre.name},${toCentre.name})")

  def skipStateCommon(shipment:   Shipment,
                      uriPath:    String,
                      updateJson: JsValue) = {

    shipmentRepository.put(shipment)
    val json = makeRequest(POST, uri(shipment, uriPath), updateJson)

    (json \ "status").as[String] must include ("success")

    shipmentRepository.getByKey(shipment.id) mustSucceed { repoShipment =>
      compareObj((json \ "data").as[JsObject], repoShipment)

      repoShipment must have (
        'id             (shipment.id),
        'version        (shipment.version + 1),
        'courierName    (shipment.courierName),
        'trackingNumber (shipment.trackingNumber),
        'fromLocationId (shipment.fromLocationId),
        'toLocationId   (shipment.toLocationId))

      checkTimeStamps(repoShipment, shipment.timeAdded, OffsetDateTime.now)
    }
  }

  describe("Shipment REST API") {

    describe("GET /api/shipments/list/:centreId") {

      it("list a shipment") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        forAll(centreFilters(f.fromCentre, f.toCentre)) { centreNameFilter =>
          val jsonItem = PagedResultsSpec(this).singleItemResult(listUri,
                                                                 Map("filter" -> centreNameFilter))
          compareObj(jsonItem, f.shipment)
        }
      }

      it("list multiple shipments") {
        val f = createdShipmentsFixture(2)
        f.shipmentMap.values.foreach(shipmentRepository.put)

        forAll(centreFilters(f.fromCentre, f.toCentre)) { centreNameFilter =>
          val jsonItems = PagedResultsSpec(this).multipleItemsResult(
              uri         = listUri,
              queryParams = Map("filter" -> centreNameFilter),
              offset      = 0,
              total       = f.shipmentMap.size.toLong,
              maybeNext   = None,
              maybePrev   = None)
          jsonItems must have size f.shipmentMap.size.toLong
          compareObjs(jsonItems, f.shipmentMap)
        }
      }

      it("list a shipment when using a not equal to filter on centre name") {
        val f = createdShipmentsFixture(1)
        f.shipmentMap.values.foreach(shipmentRepository.put)

        val filters = Table("centre filters",
                            s"fromCentre:ne:${f.fromCentre.name}",
                            s"toCentre:ne:${f.toCentre.name}",
                            s"withCentre:ne:(${f.fromCentre.name},${f.toCentre.name})")

        forAll(filters) { centreNameFilter =>
          val jsonItems = PagedResultsSpec(this).multipleItemsResult(
              uri         = listUri,
              queryParams = Map("filter" -> centreNameFilter),
              offset      = 0,
              total       = 0,
              maybeNext   = None,
              maybePrev   = None)
          jsonItems must have size 0
        }
      }

      it("list a single shipment when filtered by state") {
        val f = allShipmentsFixture
        f.shipments.values.foreach(shipmentRepository.put)

        forAll(centreFilters(f.fromCentre, f.toCentre)) { centreNameFilter =>
          forAll(states) { state =>
            info(s"$state shipment")
            val jsonItem = PagedResultsSpec(this)
              .singleItemResult(listUri, Map("filter" -> s"$centreNameFilter;state::$state"))
            compareObj(jsonItem, f.shipments.get(state).value)
          }
        }
      }

      it("list multiple shipments when filtered by states") {
        val shipmentStates = List(Shipment.createdState, Shipment.unpackedState)
        val f = allShipmentsFixture
        f.shipments.values.foreach(shipmentRepository.put)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri         = listUri,
            queryParams = Map("filter" -> s"""state:in:(${shipmentStates.mkString(",")})""",
                              "sort"   -> "state"),
            offset      = 0,
            total       = shipmentStates.size.toLong,
            maybeNext   = None,
            maybePrev   = None)

        jsonItems must have size shipmentStates.size.toLong
        compareObj(jsonItems(0), f.shipments(Shipment.createdState))
        compareObj(jsonItems(1), f.shipments(Shipment.unpackedState))
      }

      it("fail when using an invalid filter string") {
        val invalidFilterString = "xxx"

        val reply = makeRequest(GET, listUri + s"?filter=$invalidFilterString", BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex ("could not parse filter expression:")
      }

      it("fail when using an invalid state filter") {
        val invalidStateName = nameGenerator.next[Shipment]

        val reply = makeRequest(GET, listUri + s"?filter=state::$invalidStateName", NOT_FOUND)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex ("shipment state does not exist:")
      }

      it("list a single shipment when filtered by courier name") {
        val f = createdShipmentsFixture(2)
        f.shipmentMap.values.foreach(shipmentRepository.put)
        val shipment = f.shipmentMap.values.head
        val filter = s"courierName::${shipment.courierName}"
        val jsonItem = PagedResultsSpec(this).singleItemResult(listUri , Map("filter" -> filter))
        compareObj(jsonItem, shipment)
      }

      it("list a single shipment when using a 'like' filter on courier name") {
        val f = createdShipmentsFixture(2)
        val shipments = f.shipmentMap.values.toList
        val shipment = shipments(0).copy(courierName = "ABC")
        val filter = s"courierName:like:b"

        shipmentRepository.put(shipment)
        shipmentRepository.put(shipments(1).copy(courierName = "DEF"))

        val jsonItem = PagedResultsSpec(this).singleItemResult(listUri, Map("filter" -> filter))
        compareObj(jsonItem, shipment)
      }

      it("list multiple shipments when filtered by courier name") {
        val numShipments = 2
        val f = createdShipmentsFixture(numShipments)
        val shipments = f.shipmentMap.values.toList
        val courierNames = shipments.map(_.courierName)

        shipments.foreach(shipmentRepository.put)
        val filter = s"""courierName:in:(${courierNames.mkString(",")})"""

        val jsonItems =
          PagedResultsSpec(this).multipleItemsResult(uri         = listUri,
                                                     queryParams = Map("filter" -> filter),
                                                     offset      = 0,
                                                     total       = numShipments.toLong,
                                                     maybeNext   = None,
                                                     maybePrev   = None)
        jsonItems must have size numShipments.toLong
        compareObj(jsonItems(0), shipments(0))
        compareObj(jsonItems(1), shipments(1))
      }

      it("list a single shipment when filtered by tracking number") {
        val f = createdShipmentsFixture(2)
        f.shipmentMap.values.foreach(shipmentRepository.put)
        val shipment = f.shipmentMap.values.head
        val jsonItem = PagedResultsSpec(this).singleItemResult(
            listUri,
            Map("filter" -> s"trackingNumber::${shipment.trackingNumber}"))
        compareObj(jsonItem, shipment)
      }

      it("list a single shipment when filtered with a logical expression") {
        val numShipments = 2
        val f = createdShipmentsFixture(numShipments)
        val shipments = f.shipmentMap.values.toList
        val shipment = shipments(0)
        val expressions = Table(
            "expression",
            s"""courierName::${shipment.courierName};trackingNumber::${shipment.trackingNumber}""",
            s"""courierName::${shipment.courierName},trackingNumber::${shipment.trackingNumber}"""
          )

        forAll(expressions) { expression =>
          shipments.foreach(shipmentRepository.put)
          val jsonItem =
            PagedResultsSpec(this).singleItemResult(uri         = listUri,
                                                    queryParams = Map("filter" -> expression))
          compareObj(jsonItem, shipment)
        }
      }

      it("list shipments sorted by courier name") {
        val f = centresFixture
        val shipments = List("FedEx", "UPS", "Canada Post").map { name =>
            factory.createShipment(f.fromCentre, f.toCentre).copy(courierName = name)
          }.toList
        shipments.foreach(shipmentRepository.put)

        val sortExprs = Table("sort by", "courierName", "-courierName")

        forAll(sortExprs) { sortExpr =>
          val jsonItems = PagedResultsSpec(this)
            .multipleItemsResult(uri       = listUri,
                                 queryParams = Map("sort" -> sortExpr),
                                 offset    = 0,
                                 total     = shipments.size.toLong,
                                 maybeNext = None,
                                 maybePrev = None)

          jsonItems must have size shipments.size.toLong
          if (sortExpr == sortExprs(0)) {
            compareObj(jsonItems(0), shipments(2))
            compareObj(jsonItems(1), shipments(0))
            compareObj(jsonItems(2), shipments(1))
          } else {
            compareObj(jsonItems(0), shipments(1))
            compareObj(jsonItems(1), shipments(0))
            compareObj(jsonItems(2), shipments(2))
          }
        }
      }

      it("list shipments sorted by tracking number") {
        val f = centresFixture
        val shipments = List("TN2", "TN3", "TN1")
          .map { trackingNumber =>
            factory.createShipment(f.fromCentre, f.toCentre).copy(trackingNumber = trackingNumber)
          }.toList
        shipments.foreach(shipmentRepository.put)

        val sortExprs = Table("sort by", "trackingNumber", "-trackingNumber")
        forAll(sortExprs) { sortExpr =>
          val jsonItems = PagedResultsSpec(this)
            .multipleItemsResult(uri         = listUri,
                                 queryParams = Map("sort" -> sortExpr),
                                 offset      = 0,
                                 total       = shipments.size.toLong,
                                 maybeNext   = None,
                                 maybePrev   = None)
          jsonItems must have size shipments.size.toLong
          if (sortExpr == sortExprs(0)) {
            compareObj(jsonItems(0), shipments(2))
            compareObj(jsonItems(1), shipments(0))
            compareObj(jsonItems(2), shipments(1))
          } else {
            compareObj(jsonItems(0), shipments(1))
            compareObj(jsonItems(1), shipments(0))
            compareObj(jsonItems(2), shipments(2))
          }
        }
      }

      it("list a single shipment when using paged query") {
        val f = centresFixture
        val shipments = List("FedEx", "UPS", "Canada Post")
          .map { name =>
            factory.createShipment(f.fromCentre, f.toCentre).copy(courierName = name)
          }.toList
        shipments.foreach(shipmentRepository.put)
        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri       = listUri,
                            queryParams = Map("sort" -> "courierName", "limit" -> "1"),
                            total     = shipments.size.toLong,
                            maybeNext = Some(2))
        compareObj(jsonItem, shipments(2))
      }

      it("list the last shipment when using paged query") {
        val f = centresFixture
        val shipments = List("FedEx", "UPS", "Canada Post")
          .map { name =>
            factory.createShipment(f.fromCentre, f.toCentre).copy(courierName = name)
          }.toList
        shipments.foreach(shipmentRepository.put)
        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri         = listUri,
                            queryParams = Map("sort" -> "courierName", "page" -> "3", "limit" -> "1"),
                            total       = shipments.size.toLong,
                            offset      = 2,
                            maybeNext   = None,
                            maybePrev   = Some(2))
        compareObj(jsonItem, shipments(1))
      }

      it("list a single shipment when using a 'like' filter on tracking number") {
        val f = createdShipmentsFixture(2)
        val shipments = f.shipmentMap.values.toList
        val shipment = shipments(0).copy(trackingNumber = "ABC")
        val filter = s"trackingNumber:like:b"

        shipmentRepository.put(shipment)
        shipmentRepository.put(shipments(1).copy(trackingNumber = "DEF"))

        val jsonItem = PagedResultsSpec(this).singleItemResult(listUri, Map("filter" -> filter))
        compareObj(jsonItem, shipment)
      }

      it("fail when using an invalid query parameters") {
        PagedResultsSpec(this).failWithNegativePageNumber(listUri)
        PagedResultsSpec(this).failWithInvalidPageNumber(listUri)
        PagedResultsSpec(this).failWithNegativePageSize(listUri)
        PagedResultsSpec(this).failWithInvalidPageSize(listUri, 100);
        PagedResultsSpec(this).failWithInvalidSort(listUri)
      }

    }

    describe("GET /api/shipments/:id") {

      it("get a shipment") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val json = makeRequest(GET, uri(f.shipment))
                              (json \ "status").as[String] must include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, f.shipment)
      }

      it("returns an error for an invalid shipment ID") {
        val shipment = factory.createShipment

        val json = makeRequest(GET, uri(shipment), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*shipment")
      }
    }

    describe("POST /api/shipments") {

      def shipmentToAddJson(shipment: Shipment) =
        Json.obj("courierName"    -> shipment.courierName,
                 "trackingNumber" -> shipment.trackingNumber,
                 "fromLocationId" -> shipment.fromLocationId,
                 "toLocationId"   -> shipment.toLocationId,
                 "timePacked"     -> shipment.timePacked)

      it("add a shipment") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val json = makeRequest(POST, uri, shipmentToAddJson(f.shipment))

        (json \ "status").as[String] must include ("success")

        val jsonId = (json \ "data" \ "id").as[String]
        val shipmentId = ShipmentId(jsonId)
        jsonId.length must be > 0

        shipmentRepository.getByKey(shipmentId) mustSucceed { repoShipment =>
          repoShipment mustBe a[CreatedShipment]
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (shipmentId),
            'version        (0L),
            'courierName    (f.shipment.courierName),
            'trackingNumber (f.shipment.trackingNumber),
            'fromLocationId (f.shipment.fromLocationId),
            'toLocationId   (f.shipment.toLocationId))

          checkTimeStamps(repoShipment, OffsetDateTime.now, None)
          compareTimestamps(repoShipment, f.shipment)
        }
      }

      it("fail when adding a shipment with no courier name") {
        val shipment = createdShipmentFixture.shipment.copy(courierName = "")
        val json = makeRequest(POST, uri, BAD_REQUEST, shipmentToAddJson(shipment))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("CourierNameInvalid")
      }

      it("fail when adding a shipment with no tracking number") {
        val shipment = createdShipmentFixture.shipment.copy(trackingNumber = "")
        val json = makeRequest(POST, uri, BAD_REQUEST, shipmentToAddJson(shipment))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("TrackingNumberInvalid")
      }

      it("fail when adding a shipment with no FROM location id") {
        val shipment = createdShipmentFixture.shipment.copy(fromLocationId = LocationId(""))
        val json = makeRequest(POST, uri, NOT_FOUND, shipmentToAddJson(shipment))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*centre with location id")
      }

      it("fail when adding a shipment with no TO location id") {
        val shipment = createdShipmentFixture.shipment.copy(toLocationId = LocationId(""))
        val json = makeRequest(POST, uri, NOT_FOUND, shipmentToAddJson(shipment))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*centre with location id")
      }

    }


    describe("POST /api/shipments/courier/:id") {

      it("allow updating the courier name") {
        val f = createdShipmentFixture
        val newCourier = nameGenerator.next[Shipment]
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "courierName"     -> newCourier)
        val json = makeRequest(POST, uri(f.shipment, "courier"), updateJson)
                              (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          repoShipment mustBe a[CreatedShipment]
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (f.shipment.id),
            'version        (f.shipment.version + 1),
            'courierName    (newCourier),
            'trackingNumber (f.shipment.trackingNumber),
            'fromLocationId (f.shipment.fromLocationId),
            'toLocationId   (f.shipment.toLocationId))

          checkTimeStamps(repoShipment, f.shipment.timeAdded, OffsetDateTime.now)
          compareTimestamps(repoShipment, f.shipment)
        }
      }

      it("not allow updating the courier name to an empty string") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "courierName"     -> "")
        val json = makeRequest(POST, uri(f.shipment, "courier"), BAD_REQUEST, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("CourierNameInvalid")
      }

      it("must not allow updating the courier name on a shipment not in created state") {
        val f = allShipmentsFixture

        nonCreatedStates.foreach { state =>
          val shipment = f.shipments(state)
          shipmentRepository.put(shipment)
          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "courierName"     -> nameGenerator.next[String])

          val json = makeRequest(POST, uri(shipment, "courier"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex ("InvalidState: shipment not created")
        }
      }

    }

    describe("POST /api/shipments/trackingnumber/:id") {

      it("allow updating the tracking number") {
        val f = createdShipmentFixture
        val newTrackingNumber = nameGenerator.next[Shipment]
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "trackingNumber"  -> newTrackingNumber)
        val json = makeRequest(POST, uri(f.shipment, "trackingnumber"), updateJson)
                              (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          repoShipment mustBe a[CreatedShipment]
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (f.shipment.id),
            'version        (f.shipment.version + 1),
            'courierName    (f.shipment.courierName),
            'trackingNumber (newTrackingNumber),
            'fromLocationId (f.shipment.fromLocationId),
            'toLocationId   (f.shipment.toLocationId))

          checkTimeStamps(repoShipment, f.shipment.timeAdded, OffsetDateTime.now)
          compareTimestamps(repoShipment, f.shipment)
        }
      }

      it("not allow updating the tracking number to an empty string") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "trackingNumber"     -> "")
        val json = makeRequest(POST, uri(f.shipment, "trackingnumber"), BAD_REQUEST, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("TrackingNumberInvalid")
      }

      it("must not allow updating the tracking number on a shipment not in created state") {
        val f = allShipmentsFixture

        nonCreatedStates.foreach { state =>
          val shipment = f.shipments(state)
          shipmentRepository.put(shipment)
          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "trackingNumber"  -> nameGenerator.next[String])

          val json = makeRequest(POST, uri(shipment, "trackingnumber"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex ("InvalidState: shipment not created")
        }
      }

    }

    describe("POST /api/shipments/fromlocation/:id") {

      it("allow updating the location the shipment is from") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val newLocation = factory.createLocation
        val centre = factory.createEnabledCentre.copy(locations = Set(newLocation))
        centreRepository.put(centre)

        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "locationId"      -> newLocation.id.id)
        val json = makeRequest(POST, uri(f.shipment, "fromlocation"), updateJson)
                              (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          repoShipment mustBe a[CreatedShipment]
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (f.shipment.id),
            'version        (f.shipment.version + 1),
            'courierName    (f.shipment.courierName),
            'trackingNumber (f.shipment.trackingNumber),
            'fromLocationId (newLocation.id),
            'toLocationId   (f.shipment.toLocationId))

          checkTimeStamps(repoShipment, f.shipment.timeAdded, OffsetDateTime.now)
          compareTimestamps(repoShipment, f.shipment)
        }
      }

      it("not allow updating the from location to an empty string") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "locationId"      -> "")
        val json = makeRequest(POST, uri(f.shipment, "fromlocation"), NOT_FOUND, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*centre with location id")
      }

      it("not allow updating the from location to an invalid id") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val badLocation = factory.createLocation

        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "locationId"      -> badLocation.id.id)
        val json = makeRequest(POST, uri(f.shipment, "fromlocation"), NOT_FOUND, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*centre with location id")
      }

      it("must not allow updating the from location on a shipment not in created state") {
        val f = allShipmentsFixture
        val badLocation = factory.createLocation

        nonCreatedStates.foreach { state =>
          val shipment = f.shipments(state)
          shipmentRepository.put(shipment)
          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "locationId"      -> badLocation.id.id)

          val json = makeRequest(POST, uri(shipment, "fromlocation"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex ("InvalidState: shipment not created")
        }
      }

    }

    describe("POST /api/shipments/tolocation/:id") {

      it("allow updating the location the shipment is going to") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val newLocation = factory.createLocation
        val centre = factory.createEnabledCentre.copy(locations = Set(newLocation))
        centreRepository.put(centre)

        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "locationId"      -> newLocation.id.id)
        val json = makeRequest(POST, uri(f.shipment, "tolocation"), updateJson)
                              (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          repoShipment mustBe a[CreatedShipment]
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (f.shipment.id),
            'version        (f.shipment.version + 1),
            'courierName    (f.shipment.courierName),
            'trackingNumber (f.shipment.trackingNumber),
            'fromLocationId (f.shipment.fromLocationId),
            'toLocationId   (newLocation.id))

          checkTimeStamps(repoShipment, f.shipment.timeAdded, OffsetDateTime.now)
          compareTimestamps(repoShipment, f.shipment)
        }
      }

      it("not allow updating the TO location to an empty string") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "locationId"      -> "")
        val json = makeRequest(POST, uri(f.shipment, "tolocation"), NOT_FOUND, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*centre with location id")
      }

      it("not allow updating the TO location to an invalid id") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val badLocation = factory.createLocation

        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "locationId"      -> badLocation.id.id)
        val json = makeRequest(POST, uri(f.shipment, "tolocation"), NOT_FOUND, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*centre with location id")
      }

      it("must not allow updating the TO location on a shipment not in created state") {
        val f = allShipmentsFixture
        val badLocation = factory.createLocation

        nonCreatedStates.foreach { state =>
          val shipment = f.shipments(state)
          shipmentRepository.put(shipment)
          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "locationId"      -> badLocation.id.id)

          val json = makeRequest(POST, uri(shipment, "tolocation"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex ("InvalidState: shipment not created")
        }
      }

    }

    describe("POST /api/shipments/state/:id") {

      def changeStateCommon(shipment:  Shipment,
                            newState:  EntityState,
                            timeMaybe: Option[OffsetDateTime]) = {
        shipmentRepository.put(shipment)
        val baseJson = Json.obj("expectedVersion" -> shipment.version)
        val updateJson = timeMaybe.fold { baseJson } { time =>
            baseJson ++ Json.obj("datetime" -> time) }

        val json = makeRequest(POST, uri(shipment, s"state/$newState"), updateJson)

        (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(shipment.id) mustSucceed { repoShipment =>
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (shipment.id),
            'version        (shipment.version + 1),
            'courierName    (shipment.courierName),
            'trackingNumber (shipment.trackingNumber),
            'fromLocationId (shipment.fromLocationId),
            'toLocationId   (shipment.toLocationId))

          checkTimeStamps(repoShipment, shipment.timeAdded, OffsetDateTime.now)
        }
      }

      describe("for all states") {

        it("fail requests to update the state on a shipment that does not exist") {
          val f = createdShipmentFixture
          val time = OffsetDateTime.now.minusDays(10)

          forAll(states) { state =>
            info(s"for $state state")
            val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                      "datetime"        -> time)
            val json = makeRequest(POST, uri(f.shipment, s"state/$state"), NOT_FOUND, updateJson)

            (json \ "status").as[String] must include ("error")

            (json \ "message").as[String] must include regex ("IdNotFound.*shipment.*")
          }
        }

      }

      describe("for CREATED state") {

        it("change to CREATED state from PACKED state") {
          val f = packedShipmentFixture

          changeStateCommon(f.shipment, Shipment.createdState, None)
          shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
            repoShipment mustBe a[CreatedShipment]
            compareTimestamps(shipment     = repoShipment,
                              timePacked   = None,
                              timeSent     = None,
                              timeReceived = None,
                              timeUnpacked = None)
          }
        }

        it("not change to CREATED state from a state other than PACKED") {
          val f = allShipmentsFixture
          f.shipments.values.foreach(shipmentRepository.put)

          val states = Table("state",
                             Shipment.sentState,
                             Shipment.receivedState,
                             Shipment.unpackedState,
                             Shipment.lostState)

          forAll(states) { state =>
            info(s"from $state state")
            val shipment = f.shipments(state)
            val updateJson = Json.obj("expectedVersion" -> shipment.version)
            val json = makeRequest(POST, uri(shipment, s"state/created"), BAD_REQUEST, updateJson)

            (json \ "status").as[String] must include ("error")

            (json \ "message").as[String] must include ("InvalidState: shipment is not packed")
          }
        }
      }

      describe("for PACKED state") {

        it("change to PACKED state from other valid states") {
          val f = allShipmentsFixture
          val testStates = Table("state", Shipment.createdState, Shipment.sentState)

          forAll(testStates) { state =>
            info(s"change to $state state")

            val stateChangeTime = OffsetDateTime.now.minusDays(10)
            val shipment = f.shipments(state)
            addSpecimenToShipment(shipment, f.fromCentre)

            changeStateCommon(shipment, Shipment.packedState, Some(stateChangeTime))
            shipmentRepository.getByKey(shipment.id) mustSucceed { repoShipment =>
              repoShipment mustBe a[PackedShipment]
              repoShipment.version must be (shipment.version + 1)
              compareTimestamps(repoShipment,
                                Some(stateChangeTime),
                                None,
                                None,
                                None)
            }
          }
        }

        it("not change to PACKED state if no specimens in shipment") {
          val f = allShipmentsFixture
          f.shipments.values.foreach(shipmentRepository.put)

          val testStates = Table("state", Shipment.createdState, Shipment.sentState)

          forAll(testStates) { state =>
            info(s"change to $state state")
            val shipment = f.shipments(state)
            val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                      "datetime"        -> OffsetDateTime.now)
            val json = makeRequest(POST, uri(shipment, "state/packed"), BAD_REQUEST, updateJson)

            (json \ "status").as[String] must include ("error")

            (json \ "message").as[String] must include ("InvalidState: shipment has no specimens")
          }
        }

        it("not change to PACKED state from an invalid state") {
          val f = allShipmentsFixture
          f.shipments.values.foreach(shipmentRepository.put)

          val states = Table("state",
                             Shipment.packedState,
                             Shipment.receivedState,
                             Shipment.unpackedState,
                             Shipment.lostState)

          forAll(states) { state =>
            info(s"from state $state")
            val shipment = f.shipments(state)

            addSpecimenToShipment(shipment, f.fromCentre)

            val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                      "datetime"        -> OffsetDateTime.now)
            val json = makeRequest(POST, uri(shipment, "state/packed"), BAD_REQUEST, updateJson)

            (json \ "status").as[String] must include ("error")

            (json \ "message").as[String] must include ("InvalidState: cannot change to packed state")
          }
        }
      }

      describe("for SENT state") {

        it("change to SENT state from") {
          val f = allShipmentsFixture

          val testStates = Table("state name",
                                 Shipment.packedState,
                                 Shipment.receivedState,
                                 Shipment.lostState)

          forAll(testStates) { state =>
            info(s"$state state")
            val shipment =  f.shipments(state)
            val time = shipment.timePacked.get.plusDays(1)

            changeStateCommon(shipment, Shipment.sentState, Some(time))
            shipmentRepository.getByKey(shipment.id) mustSucceed { repoShipment =>
              repoShipment mustBe a[SentShipment]
              compareTimestamps(repoShipment,
                                shipment.timePacked,
                                Some(time),
                                None,
                                None)
            }
          }
        }

        it("fail when updating state to SENT where time is less than packed time") {
          val f = packedShipmentFixture
          shipmentRepository.put(f.shipment)
          val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                    "datetime"        -> f.shipment.timePacked.get.minusDays(1))

          val json = makeRequest(POST, uri(f.shipment, "state/sent"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include ("TimeSentBeforePacked")
        }

        it("not change to SENT state from an invalid state") {
          val f = allShipmentsFixture
          f.shipments.values.foreach(shipmentRepository.put)

          val states = Table("state",
                             Shipment.createdState,
                             Shipment.sentState,
                             Shipment.unpackedState)

          forAll(states) { state =>
            info(s"from state $state")
            val shipment = f.shipments(state)
            val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                      "datetime"        -> OffsetDateTime.now)
            val json = makeRequest(POST, uri(shipment, s"state/sent"), BAD_REQUEST, updateJson)

            (json \ "status").as[String] must include ("error")

            (json \ "message").as[String] must include ("InvalidState: cannot change to sent state")
          }
        }

      }

      describe("for RECEIVED state") {

        it("change to RECEIVED state from") {
          val f = allShipmentsFixture

          val testStates = Table("state name",
                                 Shipment.sentState,
                                 Shipment.unpackedState)

          forAll(testStates) { state =>
            info(s"$state state")
            val shipment =  f.shipments(state)
            val time = shipment.timeSent.get.plusDays(1)

            changeStateCommon(shipment, Shipment.receivedState, Some(time))
            shipmentRepository.getByKey(shipment.id) mustSucceed { repoShipment =>
              repoShipment mustBe a[ReceivedShipment]
              compareTimestamps(repoShipment,
                                shipment.timePacked,
                                shipment.timeSent,
                                Some(time),
                                None)
            }
          }
        }

        it("not change to RECEIVED state from an invalid state") {
          val f = allShipmentsFixture
          f.shipments.values.foreach(shipmentRepository.put)

          val states = Table("state",
                             Shipment.createdState,
                             Shipment.packedState,
                             Shipment.receivedState,
                             Shipment.lostState)

          forAll(states) { state =>
            info(s"from state $state")
            val shipment = f.shipments(state)
            val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                      "datetime"        -> OffsetDateTime.now)
            val json = makeRequest(POST, uri(shipment, s"state/received"), BAD_REQUEST, updateJson)

            (json \ "status").as[String] must include ("error")

            (json \ "message").as[String] must include ("InvalidState: cannot change to received state")
          }
        }

        it("fail for updating state to RECEIVED where time is less than sent time") {
          val f = sentShipmentFixture
          shipmentRepository.put(f.shipment)
          val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                    "datetime"        -> f.shipment.timeSent.get.minusDays(1))

          val json = makeRequest(POST, uri(f.shipment, "state/received"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include ("TimeReceivedBeforeSent")
        }

        it("fail to change from UNPACKED to RECEIVED if some specimens are not in PRESENT state") {
          val f = specimensFixture(1)

          val shipment = makeUnpackedShipment(f.shipment)
          shipmentRepository.put(shipment)

          val specimen = f.specimens.head
          val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                                     specimenId = specimen.id,
                                                                     state      = ShipmentItemState.Received)
          shipmentSpecimenRepository.put(shipmentSpecimen)

          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "datetime"        -> shipment.timeReceived)

          val json = makeRequest(POST, uri(f.shipment, "state/received"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include (
            "InvalidState: cannot change to received state, items have already been processed")
        }

        ignore("fail to change from UNPACKED to RECEIVED if some containers are not in PRESENT state") {
          fail("needs implementation")
        }
      }

      describe("for UNPACKED state") {

        it("change to UNPACKED state from") {
          val f = allShipmentsFixture

          val testStates = Table("state name",
                                 Shipment.receivedState,
                                 Shipment.completedState)

          forAll(testStates) { state =>
            info(s"$state state")
            val shipment =  f.shipments(state)
            val time = shipment.timeReceived.get.plusDays(1)

            changeStateCommon(shipment, Shipment.unpackedState, Some(time))
            shipmentRepository.getByKey(shipment.id) mustSucceed { repoShipment =>
              repoShipment mustBe a[UnpackedShipment]
              compareTimestamps(repoShipment,
                                shipment.timePacked,
                                shipment.timeSent,
                                shipment.timeReceived,
                                Some(time))
            }
          }
        }

        it("not change to UNPACKED state from an invalid state") {
          val f = allShipmentsFixture
          f.shipments.values.foreach(shipmentRepository.put)

          val states = Table("state",
                             Shipment.createdState,
                             Shipment.packedState,
                             Shipment.sentState,
                             Shipment.lostState)

          forAll(states) { state =>
            info(s"from state $state")
            val shipment = f.shipments(state)
            val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                      "datetime"        -> OffsetDateTime.now)
            val json = makeRequest(POST, uri(shipment, s"state/unpacked"), BAD_REQUEST, updateJson)

            (json \ "status").as[String] must include ("error")

            (json \ "message").as[String] must include ("InvalidState: cannot change to unpacked state")
          }
        }

      }

      describe("for COMPLETED state") {

        it("change to COMPLETED state from UNPACKED") {
          val f = unpackedShipmentFixture
          val timeCompleted = f.shipment.timeUnpacked.get.plusDays(1)

          changeStateCommon(f.shipment, Shipment.completedState, Some(timeCompleted))
          shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
            repoShipment mustBe a[CompletedShipment]
            repoShipment.version must be (f.shipment.version + 1)
            compareTimestamps(repoShipment,
                              f.shipment.timePacked,
                              f.shipment.timeSent,
                              f.shipment.timeReceived,
                              f.shipment.timeUnpacked,
                              Some(timeCompleted))
          }
        }

        it("must not change to COMPLETED state from UNPACKED if there are present specimens") {
          val f = unpackedShipmentFixture
          val timeCompleted = f.shipment.timeUnpacked.get.plusDays(1)

          shipmentRepository.put(f.shipment)
          addSpecimenToShipment(f.shipment, f.fromCentre)

          val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                    "datetime"        -> timeCompleted)
          val json = makeRequest(POST, uri(f.shipment, "state/completed"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include ("InvalidState: shipment has specimens in present state")
        }

        it("not change to COMPLETED state from an invalid state") {
          val f = allShipmentsFixture
          f.shipments.values.foreach(shipmentRepository.put)

          val states = Table("state",
                             Shipment.createdState,
                             Shipment.packedState,
                             Shipment.sentState,
                             Shipment.receivedState,
                             Shipment.lostState)

          forAll(states) { state =>
            info(s"from state $state")
            val shipment = f.shipments(state)
            val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                      "datetime"        -> OffsetDateTime.now)
            val json = makeRequest(POST, uri(shipment, s"state/completed"), BAD_REQUEST, updateJson)

            (json \ "status").as[String] must include ("error")

            (json \ "message").as[String] must include ("InvalidState: cannot change to completed state")
          }
        }

      }

      describe("for LOST state") {

        it("allow setting a shipment's state to LOST") {
          val f = sentShipmentFixture

          changeStateCommon(f.shipment, Shipment.lostState, None)
          shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
            repoShipment mustBe a[LostShipment]
            compareTimestamps(f.shipment, repoShipment)
          }
        }

        it("not change to LOST state from an invalid state") {
          val f = allShipmentsFixture
          f.shipments.values.foreach(shipmentRepository.put)

          val states = Table("state",
                             Shipment.createdState,
                             Shipment.packedState,
                             Shipment.receivedState,
                             Shipment.unpackedState)

          forAll(states) { state =>
            info(s"from state $state")
            val shipment = f.shipments(state)
            val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                      "datetime"        -> OffsetDateTime.now)
            val json = makeRequest(POST, uri(shipment, s"state/lost"), BAD_REQUEST, updateJson)

            (json \ "status").as[String] must include ("error")

            (json \ "message").as[String] must include ("InvalidState: cannot change to lost state")
          }
        }
      }

    }

    describe("POST /api/shipments/state/skip-to-sent/:id") {

      it("switch from CREATED to SENT state") {
        val f = createdShipmentFixture
        val timePacked = OffsetDateTime.now.minusDays(10)
        val timeSent = timePacked.plusDays(1)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "timePacked"      -> timePacked,
                                  "timeSent"        -> timeSent)

        skipStateCommon(f.shipment, "state/skip-to-sent", updateJson)
        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          repoShipment mustBe a[SentShipment]
          compareTimestamps(shipment     = repoShipment,
                            timePacked   = Some(timePacked),
                            timeSent     = Some(timeSent),
                            timeReceived = None,
                            timeUnpacked = None)
        }
      }

      it("fails when skipping to SENT state from state") {
        val f = allShipmentsFixture

        val fromStates = Table("from states",
                               Shipment.packedState,
                               Shipment.sentState,
                               Shipment.receivedState,
                               Shipment.unpackedState,
                               Shipment.lostState)
        forAll(fromStates) { fromState =>
          info(s"$fromState")
          val shipment =  f.shipments(fromState)
          val time = OffsetDateTime.now.minusDays(10)
          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "timePacked"      -> time,
                                    "timeSent"        -> time)
          shipmentRepository.put(shipment)
          val json = makeRequest(POST, uri(shipment, "state/skip-to-sent"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex ("InvalidState: shipment not created")
        }
      }

    }

    describe("POST /api/shipments/state/skip-to-unpacked/:id") {

      it("switch from SENT to UNPACKED state") {
        val f = sentShipmentFixture
        val timeReceived = f.shipment.timeSent.fold { OffsetDateTime.now } { t => t.plusDays(1) }
        val timeUnpacked = timeReceived.plusDays(1)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "timeReceived"    -> timeReceived,
                                  "timeUnpacked"    -> timeUnpacked)
        skipStateCommon(f.shipment, "state/skip-to-unpacked", updateJson)
        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          compareTimestamps(shipment     = repoShipment,
                            timePacked   = f.shipment.timePacked,
                            timeSent     = f.shipment.timeSent,
                            timeReceived = Some(timeReceived),
                            timeUnpacked = Some(timeUnpacked))
        }
      }

      it("fails when skipping to UNPACKED state from state") {
        val f = allShipmentsFixture

        val fromStates = Table("from states",
                               Shipment.createdState,
                               Shipment.packedState,
                               Shipment.receivedState,
                               Shipment.unpackedState,
                               Shipment.lostState)
        forAll(fromStates) { fromState =>
          info(s"$fromState")
          val shipment =  f.shipments(fromState)
          val time = shipment.timeSent.fold { OffsetDateTime.now } { t => t }
          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "timeReceived"    -> time,
                                    "timeUnpacked"    -> time)
          shipmentRepository.put(shipment)
          val json = makeRequest(POST, uri(shipment, "state/skip-to-unpacked"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex ("InvalidState: shipment not sent")
        }
      }

    }

    describe("DELETE /api/shipments/:id/:ver") {

      it("must delete a shipment in created state") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val json = makeRequest(DELETE, uri(f.shipment) + s"/${f.shipment.version}")

        (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(f.shipment.id) mustFail "IdNotFound.*shipment id.*"
      }

      it("fail on attempt to delete a shipment not in the system") {
        val f = createdShipmentFixture

        val json = makeRequest(DELETE, uri(f.shipment) + s"/${f.shipment.version}", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*shipment id")
      }

      it("must not delete a shipment not in created state") {
        val f = allShipmentsFixture

        nonCreatedStates.foreach { state =>
          val shipment = f.shipments(state)
          shipmentRepository.put(shipment)

          val json = makeRequest(DELETE, uri(shipment) + s"/${shipment.version}", BAD_REQUEST)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex ("InvalidState: shipment not created")
        }
      }

      it("attempt to remove a shipment containing specimens fails") {
        val f = specimensFixture(1)

        val specimen = f.specimens.head
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                                   specimenId = specimen.id)
        shipmentSpecimenRepository.put(shipmentSpecimen)

        val json = makeRequest(DELETE, uri(f.shipment) + s"/${f.shipment.version}", BAD_REQUEST)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("shipment has specimens.*")
      }
    }
  }

}
