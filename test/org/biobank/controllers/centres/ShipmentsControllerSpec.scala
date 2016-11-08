package org.biobank.controllers.centres

import com.github.nscala_time.time.Imports._
import org.biobank.controllers.PagedResultsSpec
import org.biobank.domain.centre.ShipmentState._
import org.biobank.domain.centre._
import org.joda.time.DateTime
import org.scalatest.prop.TableDrivenPropertyChecks._
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

  def skipStateCommon(shipment:   Shipment,
                      newState:   ShipmentState,
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
        'state          (newState),
        'courierName    (shipment.courierName),
        'trackingNumber (shipment.trackingNumber),
        'fromLocationId (shipment.fromLocationId),
        'toLocationId   (shipment.toLocationId))

      checkTimeStamps(repoShipment, shipment.timeAdded, DateTime.now)
    }
  }

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
            total     = f.shipmentMap.size.toLong,
            maybeNext = None,
            maybePrev = None)
        jsonItems must have size f.shipmentMap.size.toLong

        compareObjs(jsonItems, f.shipmentMap)
      }

      "list a single shipment when filtered by state" in {
        val f = allShipmentsFixture
        f.shipments.values.foreach(shipmentRepository.put)
        ShipmentState.values.foreach { state =>
          info(s"$state shipment")
          val jsonItem = PagedResultsSpec(this)
            .singleItemResult(listUri(f.fromCentre), Map("filter" -> s"state::$state"))
          compareObj(jsonItem, f.shipments.get(state).value)
        }
      }

      "fail when using an invalid filter string" in {
        val f = centresFixture
        val invalidFilterString = "xxx"
        val reply = makeRequest(GET, listUri(f.fromCentre) + s"?filter=$invalidFilterString", BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex ("could not parse filter expression:")
      }

      "fail when using an invalid state filter" in {
        val f = centresFixture
        val invalidStateName = nameGenerator.next[Shipment]

        val reply = makeRequest(GET, listUri(f.fromCentre) + s"?filter=state::$invalidStateName", BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex ("shipment state does not exist:")
      }

      "list a single shipment when filtered by courier name" in {
        val f = createdShipmentFixture(2)
        f.shipmentMap.values.foreach(shipmentRepository.put)
        val shipment = f.shipmentMap.values.head
        val uri = s"courierName::${shipment.courierName}"
        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(listUri(f.fromCentre), Map("filter" -> uri))
        compareObj(jsonItem, shipment)
      }

      "list multiple shipments when filtered by courier name" in {
        val numShipments = 2
        val f = createdShipmentFixture(numShipments)
        val shipments = f.shipmentMap.values.toList
        val courierNames = shipments.map(_.courierName)

        shipments.foreach(shipmentRepository.put)
        val uri = s"""courierName:in:(${courierNames.mkString(",")})"""

        val jsonItems =
          PagedResultsSpec(this).multipleItemsResult(uri         = listUri(f.fromCentre),
                                                     queryParams = Map("filter" -> uri),
                                                     offset      = 0,
                                                     total       = numShipments.toLong,
                                                     maybeNext   = None,
                                                     maybePrev   = None)
        jsonItems must have size numShipments.toLong
        compareObj(jsonItems(0), shipments(0))
        compareObj(jsonItems(1), shipments(1))
      }

      "list a single shipment when filtered by tracking number" in {
        val f = createdShipmentFixture(2)
        f.shipmentMap.values.foreach(shipmentRepository.put)
        val shipment = f.shipmentMap.values.head
        val jsonItem = PagedResultsSpec(this).singleItemResult(
            listUri(f.fromCentre),
            Map("filter" -> s"trackingNumber::${shipment.trackingNumber}"))
        compareObj(jsonItem, shipment)
      }

      "111 list a single shipment when filtered with a logical expression" in {
        val numShipments = 2
        val f = createdShipmentFixture(numShipments)
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
            PagedResultsSpec(this).singleItemResult(uri         = listUri(f.fromCentre),
                                                    queryParams = Map("filter" -> expression))
          compareObj(jsonItem, shipment)
        }
      }

      "list shipments sorted by courier name" in {
        val f = centresFixture
        val shipments = List("FedEx", "UPS", "Canada Post").
          map { name =>
            factory.createShipment(f.fromCentre, f.toCentre).copy(courierName = name)
          }.
          toList
        shipments.foreach(shipmentRepository.put)

        val orders = Table("order", "asc", "desc")
        forAll(orders) { order =>
          val sortExpression = if (order == "asc") "courierName"
                               else "-courierName"
          val jsonItems = PagedResultsSpec(this)
            .multipleItemsResult(uri       = listUri(f.fromCentre),
                                 queryParams = Map("sort" -> sortExpression),
                                 offset    = 0,
                                 total     = shipments.size.toLong,
                                 maybeNext = None,
                                 maybePrev = None)

          jsonItems must have size shipments.size.toLong
          if (order == "asc") {
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

      "list shipments sorted by tracking number" in {
        val f = centresFixture
        val shipments = List("TN2", "TN3", "TN1")
          .map { trackingNumber =>
            factory.createShipment(f.fromCentre, f.toCentre).copy(trackingNumber = trackingNumber)
          }.toList
        shipments.foreach(shipmentRepository.put)

        val orders = Table("order", "asc", "desc")
        forAll(orders) { order =>
          val sortExpression = if (order == "asc") "trackingNumber"
                               else "-trackingNumber"

          val jsonItems = PagedResultsSpec(this)
            .multipleItemsResult(uri       = listUri(f.fromCentre),
                                 queryParams = Map("sort" -> sortExpression),
                                 offset    = 0,
                                 total     = shipments.size.toLong,
                                 maybeNext = None,
                                 maybePrev = None)

          jsonItems must have size shipments.size.toLong
          if (order == "asc") {
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

      "list a single shipment when using paged query" in {
        val f = centresFixture
        val shipments = List("FedEx", "UPS", "Canada Post")
          .map { name =>
            factory.createShipment(f.fromCentre, f.toCentre).copy(courierName = name)
          }.toList
        shipments.foreach(shipmentRepository.put)
        val jsonItem = PagedResultsSpec(this)
          .singleItemResult(uri       = listUri(f.fromCentre),
                            queryParams = Map("sort" -> "courierName", "limit" -> "1"),
                            total     = shipments.size.toLong,
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
                            queryParams = Map("sort" -> "courierName", "page" -> "3", "limit" -> "1"),
                            total     = shipments.size.toLong,
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

      "fail when adding a shipment with no courier name" in {
        val shipment = createdShipmentFixture.shipment.copy(courierName = "")
        val json = makeRequest(POST, uri, BAD_REQUEST, shipmentToAddJson(shipment))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("CourierNameInvalid")
      }

      "fail when adding a shipment with no tracking number" in {
        val shipment = createdShipmentFixture.shipment.copy(trackingNumber = "")
        val json = makeRequest(POST, uri, BAD_REQUEST, shipmentToAddJson(shipment))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include ("TrackingNumberInvalid")
      }

      "fail when adding a shipment with no FROM location id" in {
        val shipment = createdShipmentFixture.shipment.copy(fromLocationId = "")
        val json = makeRequest(POST, uri, NOT_FOUND, shipmentToAddJson(shipment))

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("EntityCriteriaError.*centre with location id")
      }

      "fail when adding a shipment with no TO location id" in {
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

    "POST /shipments/state/:id" must {

      def changeStateCommon(shipment: Shipment,
                            newState: ShipmentState,
                            timeMaybe:     Option[DateTime]) = {
        shipmentRepository.put(shipment)
        val baseJson = Json.obj("expectedVersion" -> shipment.version,
                                "newState"        -> newState)

        val updateJson = timeMaybe.fold { baseJson } { time =>
            baseJson ++ Json.obj("datetime" -> time) }

        val json = makeRequest(POST, uri(shipment, "state"), updateJson)

        (json \ "status").as[String] must include ("success")

        shipmentRepository.getByKey(shipment.id) mustSucceed { repoShipment =>
          compareObj((json \ "data").as[JsObject], repoShipment)

          repoShipment must have (
            'id             (shipment.id),
            'version        (shipment.version + 1),
            'state          (newState),
            'courierName    (shipment.courierName),
            'trackingNumber (shipment.trackingNumber),
            'fromLocationId (shipment.fromLocationId),
            'toLocationId   (shipment.toLocationId))

          checkTimeStamps(repoShipment, shipment.timeAdded, DateTime.now)
        }
      }

      "for all states" should {

        "fail requests to update the state on a shipment that does not exist" in {
          val f = createdShipmentFixture
          val time = DateTime.now.minusDays(10)

          ShipmentState.values.foreach { state =>
            info(s"for $state state")

            val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                      "newState"        -> state,
                                      "datetime"        -> time)

            val json = makeRequest(POST, uri(f.shipment, "state"), NOT_FOUND, updateJson)

            (json \ "status").as[String] must include ("error")

            (json \ "message").as[String] must include regex ("IdNotFound.*shipment.*")
          }

        }

      }

      "for CREATED state" should {

        "change to CREATED state from PACKED state" in {
          val f = packedShipmentFixture

          changeStateCommon(f.shipment, ShipmentState.Created, None)
          shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
            compareTimestamps(shipment     = repoShipment,
                              timePacked   = None,
                              timeSent     = None,
                              timeReceived = None,
                              timeUnpacked = None)
          }
        }
      }

      "for PACKED state" should {

        "change to SENT state from" in {
          val f = allShipmentsFixture
          val timePacked = DateTime.now.minusDays(10)

          val fromStates = Table("fromStates", ShipmentState.Created, ShipmentState.Sent)
          forAll(fromStates) { fromState =>
            info(s"$fromState state")

            val shipment = f.shipments(fromState)
            changeStateCommon(shipment, ShipmentState.Packed, Some(timePacked))
            shipmentRepository.getByKey(shipment.id) mustSucceed { repoShipment =>
              compareTimestamps(repoShipment,
                                Some(timePacked),
                                None,
                                None,
                                None)
            }
          }
        }

      }

      "for SENT state" must {

        "change to SENT state from" in {
          val f = allShipmentsFixture

          val fromStates = Table("fromStates", ShipmentState.Packed, ShipmentState.Received)
          forAll(fromStates) { fromState =>
            info(s"$fromState state")
            val shipment =  f.shipments(fromState)
            val time = shipment.timePacked.get.plusDays(1)

            changeStateCommon(shipment, ShipmentState.Sent, Some(time))
            shipmentRepository.getByKey(shipment.id) mustSucceed { repoShipment =>
              compareTimestamps(repoShipment,
                                shipment.timePacked,
                                Some(time),
                                None,
                                None)
            }
          }
        }

        "fail when updating state to SENT where time is less than packed time" in {
          val f = packedShipmentFixture
          shipmentRepository.put(f.shipment)
          val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                    "newState"        -> ShipmentState.Sent,
                                    "datetime"        -> f.shipment.timePacked.get.minusDays(1))

          val json = makeRequest(POST, uri(f.shipment, "state"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include ("TimeSentBeforePacked")
        }

      }

      "for RECEIVED state" must {

        "change to RECEIVED state from" in {
          val f = allShipmentsFixture

          val fromStates = Table("fromStates", ShipmentState.Sent, ShipmentState.Unpacked)
          forAll(fromStates) { fromState =>
            info(s"$fromState state")
            val shipment =  f.shipments(fromState)
            val time = shipment.timeSent.get.plusDays(1)

            changeStateCommon(shipment, ShipmentState.Received, Some(time))
            shipmentRepository.getByKey(shipment.id) mustSucceed { repoShipment =>
              compareTimestamps(repoShipment,
                                shipment.timePacked,
                                shipment.timeSent,
                                Some(time),
                                None)
            }
          }
        }

        "fail for updating state to RECEIVED where time is less than sent time" in {
          val f = sentShipmentFixture
          shipmentRepository.put(f.shipment)
          val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                    "newState"        -> ShipmentState.Received,
                                    "datetime"        -> f.shipment.timeSent.get.minusDays(1))

          val json = makeRequest(POST, uri(f.shipment, "state"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include ("TimeReceivedBeforeSent")
        }

      }

      "for UNPACKED state" must {

        "change to UNPACKED state from RECEIVED" in {
          val f = receivedShipmentFixture
          val timeUnpacked = f.shipment.timeReceived.get.plusDays(1)

          changeStateCommon(f.shipment, ShipmentState.Unpacked, Some(timeUnpacked))
          shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
            compareTimestamps(repoShipment,
                              f.shipment.timePacked,
                              f.shipment.timeSent,
                              f.shipment.timeReceived,
                              Some(timeUnpacked))
          }
        }

        "fail for updating state to RECEIVED where time is less than sent time" in {
          val f = receivedShipmentFixture
          shipmentRepository.put(f.shipment)
          val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                    "newState"        -> ShipmentState.Unpacked,
                                    "datetime"        -> f.shipment.timeReceived.get.minusDays(1))

          val json = makeRequest(POST, uri(f.shipment, "state"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include ("TimeUnpackedBeforeReceived")
        }
      }

      "for LOST state" must {

        "allow setting a shipment's state to LOST" in {
          val f = sentShipmentFixture

          changeStateCommon(f.shipment, ShipmentState.Lost, None)
          shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
            compareTimestamps(f.shipment, repoShipment)
          }
        }
      }

    }

    "POST /shipments/state/skip-to-sent/:id" must {

      "switch from CREATED to SENT state" in {
        val f = createdShipmentFixture
        val timePacked = DateTime.now.minusDays(10)
        val timeSent = timePacked.plusDays(1)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "timePacked"      -> timePacked,
                                  "timeSent"        -> timeSent)

        skipStateCommon(f.shipment, ShipmentState.Sent, "state/skip-to-sent", updateJson)
        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          compareTimestamps(shipment     = repoShipment,
                            timePacked   = Some(timePacked),
                            timeSent     = Some(timeSent),
                            timeReceived = None,
                            timeUnpacked = None)
        }
      }

      "fails when skipping to SENT state from state" in {
        val f = allShipmentsFixture

        val fromStates = Table("from states",
                               ShipmentState.Packed,
                               ShipmentState.Sent,
                               ShipmentState.Received,
                               ShipmentState.Unpacked,
                               ShipmentState.Lost)
        forAll(fromStates) { fromState =>
          info(s"$fromState")
          val shipment =  f.shipments(fromState)
          val time = DateTime.now.minusDays(10)
          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "timePacked"      -> time,
                                    "timeSent"        -> time)
          shipmentRepository.put(shipment)
          val json = makeRequest(POST, uri(shipment, "state/skip-to-sent"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex ("InvalidStateTransition.*SENT")
        }
      }

    }

    "POST /shipments/state/skip-to-unpacked/:id" must {

      "switch from SENT to UNPACKED state" in {
        val f = sentShipmentFixture
        val timeReceived = f.shipment.timeSent.fold { DateTime.now } { t => t.plusDays(1) }
        val timeUnpacked = timeReceived.plusDays(1)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "timeReceived"    -> timeReceived,
                                  "timeUnpacked"    -> timeUnpacked)
        skipStateCommon(f.shipment, ShipmentState.Unpacked, "state/skip-to-unpacked", updateJson)
        shipmentRepository.getByKey(f.shipment.id) mustSucceed { repoShipment =>
          compareTimestamps(shipment     = repoShipment,
                            timePacked   = f.shipment.timePacked,
                            timeSent     = f.shipment.timeSent,
                            timeReceived = Some(timeReceived),
                            timeUnpacked = Some(timeUnpacked))
        }
      }

      "fails when skipping to UNPACKED state from state" in {
        val f = allShipmentsFixture

        val fromStates = Table("from states",
                               ShipmentState.Created,
                               ShipmentState.Packed,
                               ShipmentState.Received,
                               ShipmentState.Unpacked,
                               ShipmentState.Lost)
        forAll(fromStates) { fromState =>
          info(s"$fromState")
          val shipment =  f.shipments(fromState)
          val time = shipment.timeSent.fold { DateTime.now } { t => t }
          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "timeReceived"    -> time,
                                    "timeUnpacked"    -> time)
          shipmentRepository.put(shipment)
          val json = makeRequest(POST, uri(shipment, "state/skip-to-unpacked"), BAD_REQUEST, updateJson)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex ("InvalidStateTransition.*UNPACKED")
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

      "fail on attempt to delete a shipment not in the system"  in {
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
