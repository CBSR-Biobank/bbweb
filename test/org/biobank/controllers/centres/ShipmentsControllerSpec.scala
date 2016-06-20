package org.biobank.controllers.centres

import com.github.nscala_time.time.Imports._
import org.biobank.controllers.PagedResultsSpec
import org.biobank.domain.JsonHelper
import org.biobank.domain.centre._
import org.biobank.fixture.ControllerFixture
import play.api.libs.json._
import play.api.test.Helpers._
import scala.language.reflectiveCalls

/**
 * Tests the REST API for [[Shipments]]s.
 */
class ShipmentsControllerSpec extends ControllerFixture with JsonHelper {
  import org.biobank.TestUtils._
  import org.biobank.infrastructure.JsonUtils._

  def uri(): String = "/shipments"

  def uri(shipment: Shipment): String = uri + s"/${shipment.id.id}"

  def uri(path: String): String = uri + s"/$path"

  def uri(shipment: Shipment, path: String): String = uri(path) + s"/${shipment.id.id}"

  def compareObjs(jsonList: List[JsObject], shipmentsMap: Map[ShipmentId, Shipment]) = {
    jsonList.foreach { jsonObj =>
      val jsonId = ShipmentId((jsonObj \ "id").as[String])
      compareObj(jsonObj, shipmentsMap(jsonId))
    }
  }

  def compareTimestamps(shipment:     Shipment,
                        timePacked:   Option[DateTime],
                        timeSent:     Option[DateTime],
                        timeReceived: Option[DateTime],
                        timeUnpacked: Option[DateTime]): Unit = {
    checkOpionalTime(timePacked,   shipment.timePacked)
    checkOpionalTime(timeSent,     shipment.timeSent)
    checkOpionalTime(timeReceived, shipment.timeReceived)
    checkOpionalTime(timeUnpacked, shipment.timeUnpacked)
  }

  def compareTimestamps(shipment1: Shipment, shipment2: Shipment): Unit = {
    compareTimestamps(shipment1,
                      shipment2.timePacked,
                      shipment2.timeSent,
                      shipment2.timeReceived,
                      shipment2.timeUnpacked)
  }

  def centresFixture = {
    val centres = (1 to 2).map { _ =>
        val location = factory.createLocation
        factory.createEnabledCentre.copy(locations = Set(location))
      }
    centres.foreach(centreRepository.put)
    new {
      val fromCentre = centres(0)
      val toCentre = centres(1)
    }
  }

  def fixture = {
    val centres = centresFixture
    new {
      val fromCentre = centres.fromCentre
      val toCentre = centres.toCentre
      val shipment = factory.createShipment.copy(
          fromLocationId = fromCentre.locations.head.uniqueId,
          toLocationId = toCentre.locations.head.uniqueId)
    }
  }

  def packedShipmentFixture = {
    val centres = centresFixture
    new {
      val fromCentre = centres.fromCentre
      val toCentre = centres.toCentre
      val shipment = factory.createPackedShipment.copy(
          fromLocationId = fromCentre.locations.head.uniqueId,
          toLocationId = toCentre.locations.head.uniqueId)
    }
  }

  def sentShipmentFixture = {
    val centres = centresFixture
    new {
      val fromCentre = centres.fromCentre
      val toCentre = centres.toCentre
      val shipment = factory.createSentShipment.copy(
          fromLocationId = fromCentre.locations.head.uniqueId,
          toLocationId = toCentre.locations.head.uniqueId)
    }
  }

  def receivedShipmentFixture = {
    val centres = centresFixture
    new {
      val fromCentre = centres.fromCentre
      val toCentre = centres.toCentre
      val shipment = factory.createReceivedShipment.copy(
          fromLocationId = fromCentre.locations.head.uniqueId,
          toLocationId = toCentre.locations.head.uniqueId)
    }
  }

  def unpackedShipmentFixture = {
    val centres = centresFixture
    new {
      val fromCentre = centres.fromCentre
      val toCentre = centres.toCentre
      val shipment = factory.createUnpackedShipment.copy(
          fromLocationId = fromCentre.locations.head.uniqueId,
          toLocationId = toCentre.locations.head.uniqueId)
    }
  }

  def lostShipmentFixture = {
    val centres = centresFixture
    new {
      val fromCentre = centres.fromCentre
      val toCentre = centres.toCentre
      val shipment = factory.createLostShipment.copy(
          fromLocationId = fromCentre.locations.head.uniqueId,
          toLocationId = toCentre.locations.head.uniqueId)
    }
  }

  "Shipment REST API" when {

    "GET /shipments" must {

      "list none" in {
        log.info(s"shipmentRepository: $shipmentRepository")
        PagedResultsSpec(this).emptyResults(uri)
      }

      "list a shipment" in {
        val shipment = factory.createShipment
        shipmentRepository.put(shipment)
        val jsonItem = PagedResultsSpec(this).singleItemResult(uri)
        compareObj(jsonItem, shipment)
      }

      "list multiple shipments" in {
        val shipments = (1 to 2)
          .map { _ =>
            val shipment = factory.createShipment
            shipment.id -> shipment
          }.toMap

        shipments.values.foreach(shipmentRepository.put)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri       = uri,
            offset    = 0,
            total     = shipments.size,
            maybeNext = None,
            maybePrev = None)
        jsonItems must have size shipments.size

        compareObjs(jsonItems, shipments)
      }

      "list a single shipment when filtered by courier name" in {
        val shipments = (1 to 2)
          .map { _ =>
            val shipment = factory.createShipment
            shipment.id -> shipment
          }.toMap
        shipments.values.foreach(shipmentRepository.put)
        val shipment = shipments.values.head
        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri, Map("courierFilter" -> shipment.courierName))
        compareObj(jsonItem, shipment)
      }

      "list a single shipment when filtered by tracking number" in {
        val shipments = (1 to 2)
          .map { _ =>
            val shipment = factory.createShipment
            shipment.id -> shipment
          }.toMap
        shipments.values.foreach(shipmentRepository.put)
        val shipment = shipments.values.head
        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri, Map("trackingNumberFilter" -> shipment.trackingNumber))
        compareObj(jsonItem, shipment)
      }

      "list shipments sorted by courier name" in {
        val shipments = List("FedEx", "UPS", "Canada Post")
          .map { name => factory.createShipment.copy(courierName = name) }.toList
        shipments.foreach(shipmentRepository.put)
        val jsonItems = PagedResultsSpec(this)
          .multipleItemsResult(uri       = uri,
                               queryParams = Map("sort" -> "courierName"),
                               offset    = 0,
                               total     = shipments.size,
                               maybeNext = None,
                               maybePrev = None)
        jsonItems must have size shipments.size
        compareObj(jsonItems(0), shipments(2))
        compareObj(jsonItems(1), shipments(0))
        compareObj(jsonItems(2), shipments(1))
      }

      "list shipments sorted by tracking number" in {
        val shipments = List("TN2", "TN3", "TN1")
          .map { trackingNumber => factory.createShipment.copy(trackingNumber = trackingNumber) }.toList
        shipments.foreach(shipmentRepository.put)
        val jsonItems = PagedResultsSpec(this)
          .multipleItemsResult(uri       = uri,
                               queryParams = Map("sort" -> "trackingNumber"),
                               offset    = 0,
                               total     = shipments.size,
                               maybeNext = None,
                               maybePrev = None)
        jsonItems must have size shipments.size
        compareObj(jsonItems(0), shipments(2))
        compareObj(jsonItems(1), shipments(0))
        compareObj(jsonItems(2), shipments(1))
      }


      "list a single shipment when using paged query" in {
        val shipments = List("FedEx", "UPS", "Canada Post")
          .map { name => factory.createShipment.copy(courierName = name) }.toList
        shipments.foreach(shipmentRepository.put)
        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri       = uri,
                            queryParams = Map("sort" -> "courierName", "pageSize" -> "1"),
                            total     = shipments.size,
                            maybeNext = Some(2))
        compareObj(jsonItem, shipments(2))
      }

      "list the last shipment when using paged query" in {
        val shipments = List("FedEx", "UPS", "Canada Post")
          .map { name => factory.createShipment.copy(courierName = name) }.toList
        shipments.foreach(shipmentRepository.put)
        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri       = uri,
                            queryParams = Map("sort" -> "courierName", "page" -> "3", "pageSize" -> "1"),
                            total     = shipments.size,
                            offset    = 2,
                            maybeNext = None,
                            maybePrev = Some(2))
        compareObj(jsonItem, shipments(1))
      }

      "fail when using an invalid query parameters" in {
        PagedResultsSpec(this).failWithNegativePageNumber(uri)
        PagedResultsSpec(this).failWithInvalidPageNumber(uri)
        PagedResultsSpec(this).failWithNegativePageSize(uri)
        PagedResultsSpec(this).failWithInvalidPageSize(uri, 100);
        PagedResultsSpec(this).failWithInvalidSort(uri)
      }

    }

    "GET /shipments/:id" must {

      "get a shipment" in {
        val shipment = factory.createShipment
        shipmentRepository.put(shipment)
        val json = makeRequest(GET, uri(shipment))
                              (json \ "status").as[String] must include ("success")
        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, shipment)
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
        val f = fixture
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
        val shipment = fixture.shipment.copy(courierName = "")
        val json = makeRequest(POST, uri, BAD_REQUEST, shipmentToAddJson(shipment))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("CourierNameInvalid")
      }

      "fails when adding a shipment with no tracking number" in {
        val shipment = fixture.shipment.copy(trackingNumber = "")
        val json = makeRequest(POST, uri, BAD_REQUEST, shipmentToAddJson(shipment))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("TrackingNumberInvalid")
      }

      "fails when adding a shipment with no FROM location id" in {
        val shipment = fixture.shipment.copy(fromLocationId = "")
        val json = makeRequest(POST, uri, NOT_FOUND, shipmentToAddJson(shipment))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.centre with location id")
      }

      "fails when adding a shipment with no TO location id" in {
        val shipment = fixture.shipment.copy(toLocationId = "")
        val json = makeRequest(POST, uri, NOT_FOUND, shipmentToAddJson(shipment))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.centre with location id")
      }

    }

  }

  "POST /shipment/courier/:id" must {

    "allow updating the courier name" in {
      val f = fixture
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
      val f = fixture
      shipmentRepository.put(f.shipment)
      val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                "courierName"     -> "")
      val json = makeRequest(POST, uri(f.shipment, "courier"), BAD_REQUEST, updateJson)

      (json \ "status").as[String] must include ("error")

      (json \ "message").as[String] must include ("CourierNameInvalid")
    }

  }

  "POST /shipment/trackingnumber/:id" must {

    "allow updating the tracking number" in {
      val f = fixture
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
      val f = fixture
      shipmentRepository.put(f.shipment)
      val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                "trackingNumber"     -> "")
      val json = makeRequest(POST, uri(f.shipment, "trackingnumber"), BAD_REQUEST, updateJson)

      (json \ "status").as[String] must include ("error")

      (json \ "message").as[String] must include ("TrackingNumberInvalid")
    }

  }

  "POST /shipment/fromlocation/:id" must {

    "allow updating the location the shipment is from" in {
      val f = fixture
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
      val f = fixture
      shipmentRepository.put(f.shipment)
      val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                "locationId"      -> "")
      val json = makeRequest(POST, uri(f.shipment, "fromlocation"), NOT_FOUND, updateJson)

      (json \ "status").as[String] must include ("error")

      (json \ "message").as[String] must include regex ("EntityCriteriaError.centre with location id")
    }

    "not allow updating the from location to an invalid id" in {
      val f = fixture
      shipmentRepository.put(f.shipment)

      val badLocation = factory.createLocation

      val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                "locationId"      -> badLocation.uniqueId)
      val json = makeRequest(POST, uri(f.shipment, "fromlocation"), NOT_FOUND, updateJson)

      (json \ "status").as[String] must include ("error")

      (json \ "message").as[String] must include regex ("EntityCriteriaError.centre with location id")
    }

  }

  "POST /shipment/tolocation/:id" must {

    "allow updating the location the shipment is going to" in {
      val f = fixture
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
      val f = fixture
      shipmentRepository.put(f.shipment)
      val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                "locationId"      -> "")
      val json = makeRequest(POST, uri(f.shipment, "tolocation"), NOT_FOUND, updateJson)

      (json \ "status").as[String] must include ("error")

      (json \ "message").as[String] must include regex ("EntityCriteriaError.centre with location id")
    }

    "not allow updating the TO location to an invalid id" in {
      val f = fixture
      shipmentRepository.put(f.shipment)

      val badLocation = factory.createLocation

      val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                "locationId"      -> badLocation.uniqueId)
      val json = makeRequest(POST, uri(f.shipment, "tolocation"), NOT_FOUND, updateJson)

      (json \ "status").as[String] must include ("error")

      (json \ "message").as[String] must include regex ("EntityCriteriaError.centre with location id")
    }

  }

  "POST /shipment/packed/:id" must {

    "111 allow updating the time the shipment was packed" in {
      val f = fixture
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

  "POST /shipment/sent/:id" must {

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

  "POST /shipment/received/:id" must {

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

  "POST /shipment/unpacked/:id" must {

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

  "POST /shipment/lost/:id" must {

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
}
