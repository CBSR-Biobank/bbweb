package org.biobank.controllers.centres

import com.github.nscala_time.time.Imports._
import org.biobank.controllers.PagedResultsSpec
import org.biobank.domain.centre._
import play.api.libs.json._
import play.api.test.Helpers._
import scala.language.reflectiveCalls

/**
 * Tests the REST API for [[Shipment]]s.
 *
 * Tests for [[ShipmentSpecimen]]s in ShipmentSpecimensControllerSpec.scala.
 */
class ShipmentsControllerSpec
    extends ShipmentsControllerSpecFixtures
    with ShipmentsControllerSpecUtils {
  import org.biobank.TestUtils._
  import org.biobank.infrastructure.JsonUtils._

  "Shipment REST API" when {

    "GET /shipments/list/:centreId" must {

      "list none" in {
        val centre = factory.createEnabledCentre
        PagedResultsSpec(this).emptyResults(listUri(centre))
      }

      "list a shipment" in {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val jsonItem = PagedResultsSpec(this).singleItemResult(listUri(f.fromCentre))
        compareObj(jsonItem, f.shipment)
      }

      "list multiple shipments" in {
        val f = createdShipmentFixture(2)
        f.shipmentMap.values.foreach(shipmentRepository.put)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri       = listUri(f.fromCentre),
            offset    = 0,
            total     = f.shipmentMap.size,
            maybeNext = None,
            maybePrev = None)
        jsonItems must have size f.shipmentMap.size

        compareObjs(jsonItems, f.shipmentMap)
      }

      "list a single shipment when filtered by state" in {
        val f = allShipmentsFixture
        f.shipments.values.foreach(shipmentRepository.put)
        ShipmentState.values.foreach { state =>
          info(s"$state shipment")
          val jsonItem = PagedResultsSpec(this)
            .singleItemResult(listUri(f.fromCentre), Map("stateFilter" -> state.toString))
          compareObj(jsonItem, f.shipments.get(state).value)
        }
      }

      "fail when using an invalid state filter" in {
        val f = centresFixture
        val invalidStateName = nameGenerator.next[Shipment]

        val reply = makeRequest(GET, listUri(f.fromCentre) + s"?stateFilter=$invalidStateName", BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex ("invalid shipment state")
      }

      "list a single shipment when filtered by courier name" in {
        val f = createdShipmentFixture(2)
        f.shipmentMap.values.foreach(shipmentRepository.put)
        val shipment = f.shipmentMap.values.head
        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(listUri(f.fromCentre), Map("courierFilter" -> shipment.courierName))
        compareObj(jsonItem, shipment)
      }

      "list a single shipment when filtered by tracking number" in {
        val f = createdShipmentFixture(2)
        f.shipmentMap.values.foreach(shipmentRepository.put)
        val shipment = f.shipmentMap.values.head
        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(listUri(f.fromCentre), Map("trackingNumberFilter" -> shipment.trackingNumber))
        compareObj(jsonItem, shipment)
      }

      "list shipments sorted by courier name" must {

        def requestShipments(order: String) = {
          val f = centresFixture
          val shipments = List("FedEx", "UPS", "Canada Post").map { name =>
              factory.createShipment(f.fromCentre, f.toCentre).copy(courierName = name)
            }.toList
          shipments.foreach(shipmentRepository.put)
          val jsonItems = PagedResultsSpec(this)
            .multipleItemsResult(uri       = listUri(f.fromCentre),
                                 queryParams = Map("sort" -> "courierName",
                                                   "order" -> order),
                                 offset    = 0,
                                 total     = shipments.size,
                                 maybeNext = None,
                                 maybePrev = None)
          (shipments, jsonItems)
        }

        "in ascending order" in {
          val (shipments, jsonItems) = requestShipments("asc")
          jsonItems must have size shipments.size
          compareObj(jsonItems(0), shipments(2))
          compareObj(jsonItems(1), shipments(0))
          compareObj(jsonItems(2), shipments(1))
        }

        "in descending order" in {
          val (shipments, jsonItems) = requestShipments("desc")
          jsonItems must have size shipments.size
          compareObj(jsonItems(0), shipments(1))
          compareObj(jsonItems(1), shipments(0))
          compareObj(jsonItems(2), shipments(2))
        }
      }

      "list shipments sorted by tracking number" must {

        def requestShipments(order: String) = {
          val f = centresFixture
          val shipments = List("TN2", "TN3", "TN1")
            .map { trackingNumber =>
              factory.createShipment(f.fromCentre, f.toCentre).copy(trackingNumber = trackingNumber)
            }.toList
          shipments.foreach(shipmentRepository.put)
          val jsonItems = PagedResultsSpec(this)
            .multipleItemsResult(uri       = listUri(f.fromCentre),
                                 queryParams = Map("sort" -> "trackingNumber",
                                                   "order" -> order),
                                 offset    = 0,
                                 total     = shipments.size,
                                 maybeNext = None,
                                 maybePrev = None)
          (shipments, jsonItems)
        }

        "in ascending order" in {
          val (shipments, jsonItems) = requestShipments("asc")
          jsonItems must have size shipments.size
          compareObj(jsonItems(0), shipments(2))
          compareObj(jsonItems(1), shipments(0))
          compareObj(jsonItems(2), shipments(1))
        }

        "in descending order" in {
          val (shipments, jsonItems) = requestShipments("desc")
          jsonItems must have size shipments.size
          compareObj(jsonItems(0), shipments(1))
          compareObj(jsonItems(1), shipments(0))
          compareObj(jsonItems(2), shipments(2))
        }
      }


      "list a single shipment when using paged query" in {
        val f = centresFixture
        val shipments = List("FedEx", "UPS", "Canada Post")
          .map { name =>
            factory.createShipment(f.fromCentre, f.toCentre).copy(courierName = name)
          }.toList
        shipments.foreach(shipmentRepository.put)
        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri       = listUri(f.fromCentre),
                            queryParams = Map("sort" -> "courierName", "pageSize" -> "1"),
                            total     = shipments.size,
                            maybeNext = Some(2))
        compareObj(jsonItem, shipments(2))
      }

      "list the last shipment when using paged query" in {
        val f = centresFixture
        val shipments = List("FedEx", "UPS", "Canada Post")
          .map { name =>
            factory.createShipment(f.fromCentre, f.toCentre).copy(courierName = name)
          }.toList
        shipments.foreach(shipmentRepository.put)
        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri       = listUri(f.fromCentre),
                            queryParams = Map("sort" -> "courierName", "page" -> "3", "pageSize" -> "1"),
                            total     = shipments.size,
                            offset    = 2,
                            maybeNext = None,
                            maybePrev = Some(2))
        compareObj(jsonItem, shipments(1))
      }

      "fail when using an invalid query parameters" in {
        val f = centresFixture
        val url = listUri(f.fromCentre)
        PagedResultsSpec(this).failWithNegativePageNumber(url)
        PagedResultsSpec(this).failWithInvalidPageNumber(url)
        PagedResultsSpec(this).failWithNegativePageSize(url)
        PagedResultsSpec(this).failWithInvalidPageSize(url, 100);
        PagedResultsSpec(this).failWithInvalidSort(url)
      }

    }

    "GET /shipments/:id" must {

      "get a shipment" in {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val json = makeRequest(GET, uri(f.shipment))
                              (json \ "status").as[String] must include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, f.shipment)
      }

      "returns an error for an invalid shipment ID" in {
        val shipment = factory.createShipment

        val json = makeRequest(GET, uri(shipment), NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*shipment")
      }
    }

    "POST /centres" must {

      def shipmentToAddJson(shipment: Shipment) =
        Json.obj("courierName"    -> shipment.courierName,
                 "trackingNumber" -> shipment.trackingNumber,
                 "fromLocationId" -> shipment.fromLocationId,
                 "toLocationId"   -> shipment.toLocationId,
                 "timePacked"     -> shipment.timePacked)

      "add a shipment" in {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val json = makeRequest(POST, uri, shipmentToAddJson(f.shipment))

        (json \ "status").as[String] must include ("success")

        val jsonId = (json \ "data" \ "id").as[String]
        val shipmentId = ShipmentId(jsonId)
        jsonId.length must be > 0

        shipmentRepository.getByKey(shipmentId) mustSucceed { repoShipment =>
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (shipmentId),
            'version        (0L),
            'state          (f.shipment.state),
            'courierName    (f.shipment.courierName),
            'trackingNumber (f.shipment.trackingNumber),
            'fromLocationId (f.shipment.fromLocationId),
            'toLocationId   (f.shipment.toLocationId))

          checkTimeStamps(repoShipment, DateTime.now, None)
          compareTimestamps(repoShipment, f.shipment)
        }
      }

      "fails when adding a shipment with no courier name" in {
        val shipment = createdShipmentFixture.shipment.copy(courierName = "")
        val json = makeRequest(POST, uri, BAD_REQUEST, shipmentToAddJson(shipment))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("CourierNameInvalid")
      }

      "fails when adding a shipment with no tracking number" in {
        val shipment = createdShipmentFixture.shipment.copy(trackingNumber = "")
        val json = makeRequest(POST, uri, BAD_REQUEST, shipmentToAddJson(shipment))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("TrackingNumberInvalid")
      }

      "fails when adding a shipment with no FROM location id" in {
        val shipment = createdShipmentFixture.shipment.copy(fromLocationId = "")
        val json = makeRequest(POST, uri, NOT_FOUND, shipmentToAddJson(shipment))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*centre with location id")
      }

      "fails when adding a shipment with no TO location id" in {
        val shipment = createdShipmentFixture.shipment.copy(toLocationId = "")
        val json = makeRequest(POST, uri, NOT_FOUND, shipmentToAddJson(shipment))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*centre with location id")
      }

    }


    "POST /shipments/courier/:id" must {

      "allow updating the courier name" in {
        val f = createdShipmentFixture
        val newCourier = nameGenerator.next[Shipment]
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "courierName"     -> newCourier)
        val json = makeRequest(POST, uri(f.shipment, "courier"), updateJson)
                              (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (f.shipment.id),
            'version        (f.shipment.version + 1),
            'state          (f.shipment.state),
            'courierName    (newCourier),
            'trackingNumber (f.shipment.trackingNumber),
            'fromLocationId (f.shipment.fromLocationId),
            'toLocationId   (f.shipment.toLocationId))

          checkTimeStamps(repoShipment, f.shipment.timeAdded, DateTime.now)
          compareTimestamps(repoShipment, f.shipment)
        }
      }

      "not allow updating the courier name to an empty string" in {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "courierName"     -> "")
        val json = makeRequest(POST, uri(f.shipment, "courier"), BAD_REQUEST, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("CourierNameInvalid")
      }

      "must not allow updating the courier name on a shipment not in created state"  in {
        val f = allShipmentsFixture

        nonCreatedStates.foreach { state =>
          val shipment = f.shipments(state)
          shipmentRepository.put(shipment)
          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "courierName"     -> nameGenerator.next[String])

          val json = makeRequest(POST, uri(shipment, "courier"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include ("shipment is not in created state")
        }
      }

    }

    "POST /shipments/trackingnumber/:id" must {

      "allow updating the tracking number" in {
        val f = createdShipmentFixture
        val newTrackingNumber = nameGenerator.next[Shipment]
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "trackingNumber"  -> newTrackingNumber)
        val json = makeRequest(POST, uri(f.shipment, "trackingnumber"), updateJson)
                              (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (f.shipment.id),
            'version        (f.shipment.version + 1),
            'state          (f.shipment.state),
            'courierName    (f.shipment.courierName),
            'trackingNumber (newTrackingNumber),
            'fromLocationId (f.shipment.fromLocationId),
            'toLocationId   (f.shipment.toLocationId))

          checkTimeStamps(repoShipment, f.shipment.timeAdded, DateTime.now)
          compareTimestamps(repoShipment, f.shipment)
        }
      }

      "not allow updating the tracking number to an empty string" in {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "trackingNumber"     -> "")
        val json = makeRequest(POST, uri(f.shipment, "trackingnumber"), BAD_REQUEST, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("TrackingNumberInvalid")
      }

      "must not allow updating the tracking number on a shipment not in created state"  in {
        val f = allShipmentsFixture

        nonCreatedStates.foreach { state =>
          val shipment = f.shipments(state)
          shipmentRepository.put(shipment)
          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "trackingNumber"  -> nameGenerator.next[String])

          val json = makeRequest(POST, uri(shipment, "trackingnumber"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include ("shipment is not in created state")
        }
      }

    }

    "POST /shipments/fromlocation/:id" must {

      "allow updating the location the shipment is from" in {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val newLocation = factory.createLocation
        val centre = factory.createEnabledCentre.copy(locations = Set(newLocation))
        centreRepository.put(centre)

        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "locationId"      -> newLocation.uniqueId)
        val json = makeRequest(POST, uri(f.shipment, "fromlocation"), updateJson)
                              (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (f.shipment.id),
            'version        (f.shipment.version + 1),
            'state          (f.shipment.state),
            'courierName    (f.shipment.courierName),
            'trackingNumber (f.shipment.trackingNumber),
            'fromLocationId (newLocation.uniqueId),
            'toLocationId   (f.shipment.toLocationId))

          checkTimeStamps(repoShipment, f.shipment.timeAdded, DateTime.now)
          compareTimestamps(repoShipment, f.shipment)
        }
      }

      "not allow updating the from location to an empty string" in {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "locationId"      -> "")
        val json = makeRequest(POST, uri(f.shipment, "fromlocation"), NOT_FOUND, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*centre with location id")
      }

      "not allow updating the from location to an invalid id" in {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val badLocation = factory.createLocation

        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "locationId"      -> badLocation.uniqueId)
        val json = makeRequest(POST, uri(f.shipment, "fromlocation"), NOT_FOUND, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*centre with location id")
      }

      "must not allow updating the from location on a shipment not in created state"  in {
        val f = allShipmentsFixture
        val badLocation = factory.createLocation

        nonCreatedStates.foreach { state =>
          val shipment = f.shipments(state)
          shipmentRepository.put(shipment)
          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "locationId"      -> badLocation.uniqueId)

          val json = makeRequest(POST, uri(shipment, "fromlocation"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include ("shipment is not in created state")
        }
      }

    }

    "POST /shipments/tolocation/:id" must {

      "allow updating the location the shipment is going to" in {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val newLocation = factory.createLocation
        val centre = factory.createEnabledCentre.copy(locations = Set(newLocation))
        centreRepository.put(centre)

        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "locationId"      -> newLocation.uniqueId)
        val json = makeRequest(POST, uri(f.shipment, "tolocation"), updateJson)
                              (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (f.shipment.id),
            'version        (f.shipment.version + 1),
            'state          (f.shipment.state),
            'courierName    (f.shipment.courierName),
            'trackingNumber (f.shipment.trackingNumber),
            'fromLocationId (f.shipment.fromLocationId),
            'toLocationId   (newLocation.uniqueId))

          checkTimeStamps(repoShipment, f.shipment.timeAdded, DateTime.now)
          compareTimestamps(repoShipment, f.shipment)
        }
      }

      "not allow updating the TO location to an empty string" in {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "locationId"      -> "")
        val json = makeRequest(POST, uri(f.shipment, "tolocation"), NOT_FOUND, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*centre with location id")
      }

      "not allow updating the TO location to an invalid id" in {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val badLocation = factory.createLocation

        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "locationId"      -> badLocation.uniqueId)
        val json = makeRequest(POST, uri(f.shipment, "tolocation"), NOT_FOUND, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*centre with location id")
      }

      "must not allow updating the TO location on a shipment not in created state"  in {
        val f = allShipmentsFixture
        val badLocation = factory.createLocation

        nonCreatedStates.foreach { state =>
          val shipment = f.shipments(state)
          shipmentRepository.put(shipment)
          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "locationId"      -> badLocation.uniqueId)

          val json = makeRequest(POST, uri(shipment, "tolocation"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include ("shipment is not in created state")
        }
      }

    }

    "POST /shipments/created/:id" must {

      "allow setting a shipment back to CREATED state from PACKED state" in {
        val f = packedShipmentFixture
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version)
        val json = makeRequest(POST, uri(f.shipment, "created"), updateJson)

        (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (f.shipment.id),
            'version        (f.shipment.version + 1),
            'state          (ShipmentState.Created),
            'courierName    (f.shipment.courierName),
            'trackingNumber (f.shipment.trackingNumber),
            'fromLocationId (f.shipment.fromLocationId),
            'toLocationId   (f.shipment.toLocationId))

          checkTimeStamps(repoShipment, f.shipment.timeAdded, DateTime.now)
          compareTimestamps(shipment     = repoShipment,
                            timePacked   = None,
                            timeSent     = None,
                            timeReceived = None,
                            timeUnpacked = None)
        }

      }
    }

    "POST /shipments/packed/:id" must {

      "allow updating the time the shipment was packed" in {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val timePacked = DateTime.now.minusDays(10)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "time"            -> timePacked)
        val json = makeRequest(POST, uri(f.shipment, "packed"), updateJson)
                              (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (f.shipment.id),
            'version        (f.shipment.version + 1),
            'state          (ShipmentState.Packed),
            'courierName    (f.shipment.courierName),
            'trackingNumber (f.shipment.trackingNumber),
            'fromLocationId (f.shipment.fromLocationId),
            'toLocationId   (f.shipment.toLocationId))

          checkTimeStamps(repoShipment, f.shipment.timeAdded, DateTime.now)
          compareTimestamps(repoShipment,
                            Some(timePacked),
                            f.shipment.timeSent,
                            f.shipment.timeReceived,
                            f.shipment.timeUnpacked)
        }
      }
    }

    "POST /shipments/sent/:id" must {

      "allow setting a shipment's state to SENT" in {
        val f = packedShipmentFixture
        shipmentRepository.put(f.shipment)

        val timeSent = f.shipment.timePacked.get.plusDays(1)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "time"            -> timeSent)

        val json = makeRequest(POST, uri(f.shipment, "sent"), updateJson)

        (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (f.shipment.id),
            'version        (f.shipment.version + 1),
            'state          (ShipmentState.Sent),
            'courierName    (f.shipment.courierName),
            'trackingNumber (f.shipment.trackingNumber),
            'fromLocationId (f.shipment.fromLocationId),
            'toLocationId   (f.shipment.toLocationId))

          checkTimeStamps(repoShipment, f.shipment.timeAdded, DateTime.now)
          compareTimestamps(repoShipment,
                            f.shipment.timePacked,
                            Some(timeSent),
                            f.shipment.timeReceived,
                            f.shipment.timeUnpacked)
        }
      }

      "fails for updating state to SENT on a shipment that does not exist" in {
        val f = packedShipmentFixture
        val timeSent = f.shipment.timePacked.get.plusDays(1)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "time"            -> timeSent)

        val json = makeRequest(POST, uri(f.shipment, "sent"), NOT_FOUND, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*shipment.*")
      }

      "fails for updating state to SENT where time is less than packed time" in {
        val f = packedShipmentFixture
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "time"            -> f.shipment.timePacked.get.minusDays(1))

        val json = makeRequest(POST, uri(f.shipment, "sent"), BAD_REQUEST, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("TimeSentBeforePacked")
      }

    }

    "POST /shipments/received/:id" must {

      "allow setting a shipment's state to RECEIVED" in {
        val f = sentShipmentFixture
        shipmentRepository.put(f.shipment)

        val timeReceived = f.shipment.timeSent.get.plusDays(1)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "time"            -> timeReceived)

        val json = makeRequest(POST, uri(f.shipment, "received"), updateJson)

        (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (f.shipment.id),
            'version        (f.shipment.version + 1),
            'state          (ShipmentState.Received),
            'courierName    (f.shipment.courierName),
            'trackingNumber (f.shipment.trackingNumber),
            'fromLocationId (f.shipment.fromLocationId),
            'toLocationId   (f.shipment.toLocationId))

          checkTimeStamps(repoShipment, f.shipment.timeAdded, DateTime.now)
          compareTimestamps(repoShipment,
                            f.shipment.timePacked,
                            f.shipment.timeSent,
                            Some(timeReceived),
                            f.shipment.timeUnpacked)
        }
      }

      "fails for updating state to RECEIVED where time is less than sent time" in {
        val f = sentShipmentFixture
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "time"            -> f.shipment.timeSent.get.minusDays(1))

        val json = makeRequest(POST, uri(f.shipment, "received"), BAD_REQUEST, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("TimeReceivedBeforeSent")
      }

    }

    "POST /shipments/unpacked/:id" must {

      "allow setting a shipment's state to UNPACKED" in {
        val f = receivedShipmentFixture
        shipmentRepository.put(f.shipment)

        val timeUnpacked = f.shipment.timeReceived.get.plusDays(1)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "time"            -> timeUnpacked)

        val json = makeRequest(POST, uri(f.shipment, "unpacked"), updateJson)

        (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (f.shipment.id),
            'version        (f.shipment.version + 1),
            'state          (ShipmentState.Unpacked),
            'courierName    (f.shipment.courierName),
            'trackingNumber (f.shipment.trackingNumber),
            'fromLocationId (f.shipment.fromLocationId),
            'toLocationId   (f.shipment.toLocationId))

          checkTimeStamps(repoShipment, f.shipment.timeAdded, DateTime.now)
          compareTimestamps(repoShipment,
                            f.shipment.timePacked,
                            f.shipment.timeSent,
                            f.shipment.timeReceived,
                            Some(timeUnpacked))
        }
      }

      "fails for updating state to RECEIVED where time is less than sent time" in {
        val f = receivedShipmentFixture
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "time"            -> f.shipment.timeReceived.get.minusDays(1))

        val json = makeRequest(POST, uri(f.shipment, "unpacked"), BAD_REQUEST, updateJson)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("TimeUnpackedBeforeReceived")
      }

    }

    "POST /shipments/lost/:id" must {

      "allow setting a shipment's state to LOST" in {
        val f = sentShipmentFixture
        shipmentRepository.put(f.shipment)

        val json = makeRequest(POST,
                               uri(f.shipment, "lost"),
                               Json.obj("expectedVersion" -> f.shipment.version))

        (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (f.shipment.id),
            'version        (f.shipment.version + 1),
            'state          (ShipmentState.Lost),
            'courierName    (f.shipment.courierName),
            'trackingNumber (f.shipment.trackingNumber),
            'fromLocationId (f.shipment.fromLocationId),
            'toLocationId   (f.shipment.toLocationId))

          checkTimeStamps(repoShipment, f.shipment.timeAdded, DateTime.now)
          compareTimestamps(f.shipment, repoShipment)
        }
      }
    }

    "DELETE /shipments/:id/:ver" must {

      "must delete a shipment in created state" in {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val json = makeRequest(DELETE, uri(f.shipment) + s"/${f.shipment.version}")

        (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(f.shipment.id) mustFail "IdNotFound.*shipment id.*"
      }

      "fails on attempt to delete a shipment not in the system"  in {
        val f = createdShipmentFixture

        val json = makeRequest(DELETE, uri(f.shipment) + s"/${f.shipment.version}", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*shipment id")
      }

      "must not delete a shipment not in created state"  in {
        val f = allShipmentsFixture

        nonCreatedStates.foreach { state =>
          val shipment = f.shipments(state)
          shipmentRepository.put(shipment)

          val json = makeRequest(DELETE, uri(shipment) + s"/${shipment.version}", BAD_REQUEST)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include ("shipment is not in created state")
        }
      }

      "attempt to remove a shipment containing specimens fails" in {
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
