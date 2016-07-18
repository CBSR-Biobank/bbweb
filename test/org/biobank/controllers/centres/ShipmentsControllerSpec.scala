package org.biobank.controllers.centres

import org.biobank.TestUtils
import com.github.nscala_time.time.Imports._
import org.biobank.controllers.PagedResultsSpec
import org.biobank.dto.ShipmentSpecimenDto
import org.biobank.domain.JsonHelper
import org.biobank.domain.centre._
import org.biobank.domain.centre.ShipmentItemState._
import org.biobank.domain.participants._
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

  val nonCreatedStates = List(ShipmentState.Created,
                              ShipmentState.Packed,
                              ShipmentState.Sent,
                              ShipmentState.Received,
                              ShipmentState.Unpacked,
                              ShipmentState.Lost)

  def uri(): String = "/shipments"

  def uri(shipment: Shipment): String = uri + s"/${shipment.id.id}"

  def uri(path: String): String = uri + s"/$path"

  def uri(shipment: Shipment, path: String): String = uri(path) + s"/${shipment.id.id}"

  def uriSpecimen(shipment: Shipment): String =
    uri + s"/specimens/${shipment.id.id}"

  def uriSpecimen(shipment: Shipment, shipmentSpecimen: ShipmentSpecimen, path: String): String =
    uri + s"/specimens/$path/${shipment.id.id}/${shipmentSpecimen.id.id}"

  def uriSpecimen(shipment: Shipment, shipmentSpecimen: ShipmentSpecimen): String =
    uriSpecimen(shipment) + s"/${shipmentSpecimen.id.id}"

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

  def createdShipmentFixture = {
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

  def allShipmentsFixture = {
    val centres = centresFixture
    new {
      val fromCentre = centres.fromCentre
      val toCentre = centres.toCentre
      val shipments = Map(
          ShipmentState.Created
            -> factory.createLostShipment.copy(fromLocationId = fromCentre.locations.head.uniqueId,
                                               toLocationId = toCentre.locations.head.uniqueId),
          ShipmentState.Packed
            -> factory.createPackedShipment.copy(fromLocationId = fromCentre.locations.head.uniqueId,
                                                 toLocationId = toCentre.locations.head.uniqueId),
          ShipmentState.Sent
            -> factory.createSentShipment.copy(fromLocationId = fromCentre.locations.head.uniqueId,
                                               toLocationId = toCentre.locations.head.uniqueId),
          ShipmentState.Received
            -> factory.createReceivedShipment.copy(fromLocationId = fromCentre.locations.head.uniqueId,
                                                   toLocationId = toCentre.locations.head.uniqueId),
          ShipmentState.Unpacked
            -> factory.createUnpackedShipment.copy(fromLocationId = fromCentre.locations.head.uniqueId,
                                                   toLocationId = toCentre.locations.head.uniqueId),
          ShipmentState.Lost
            -> factory.createLostShipment.copy(fromLocationId = fromCentre.locations.head.uniqueId,
                                               toLocationId = toCentre.locations.head.uniqueId))
    }
  }

  def specimensFixture(numSpecimens: Int) = {
    val f = createdShipmentFixture
    val _study = factory.createEnabledStudy
    val _specimenSpec = factory.createCollectionSpecimenSpec
    val _ceventType = factory.createCollectionEventType.copy(studyId = _study.id,
                                                             specimenSpecs = Set(_specimenSpec),
                                                             annotationTypes = Set.empty)
    val _participant = factory.createParticipant.copy(studyId = _study.id)
    val _cevent = factory.createCollectionEvent
    val _specimens = (1 to numSpecimens).map { _ =>
        factory.createUsableSpecimen.copy(originLocationId = f.fromCentre.locations.head.uniqueId,
                                          locationId = f.fromCentre.locations.head.uniqueId)
      }.toList

    centreRepository.put(f.fromCentre)
    centreRepository.put(f.toCentre)
    studyRepository.put(_study)
    collectionEventTypeRepository.put(_ceventType)
    participantRepository.put(_participant)
    collectionEventRepository.put(_cevent)
    _specimens.foreach { specimen =>
      specimenRepository.put(specimen)
      ceventSpecimenRepository.put(CeventSpecimen(_cevent.id, specimen.id))
    }
    shipmentRepository.put(f.shipment)

    new {
      val fromCentre   = f.fromCentre
      val toCentre     = f.toCentre
      val study        = _study
      val specimenSpec = _specimenSpec
      val ceventType   = _ceventType
      val participant  = _participant
      val cevent       = _cevent
      val specimens    = _specimens
      val shipment     = f.shipment
    }
  }

  def shipmentSpecimensFixture(numSpecimens: Int) = {
    val f = specimensFixture(numSpecimens)

    val map = f.specimens.zipWithIndex.map { case (specimen, index) =>
        val updatedSpecimen = specimen.copy(inventoryId = s"inventoryId_$index")
        specimenRepository.put(updatedSpecimen)
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                                   specimenId = specimen.id)
        val name = f.fromCentre.locationName(specimen.locationId).fold(e => "error", n => n)
        val dto = shipmentSpecimen.createDto(updatedSpecimen,
                                             name,
                                             f.ceventType.specimenSpecs.head.units)
        (updatedSpecimen.id, (updatedSpecimen, shipmentSpecimen, dto))
      }.toMap

    map.values.foreach(x => shipmentSpecimenRepository.put(x._2))

    new {
      val fromCentre          = f.fromCentre
      val toCentre            = f.toCentre
      val study               = f.study
      val specimenSpec        = f.specimenSpec
      val ceventType          = f.ceventType
      val participant         = f.participant
      val cevent              = f.cevent
      val specimens           = f.specimens
      val shipment            = f.shipment
      val shipmentSpecimenMap = map
    }
  }

  def makePackedShipment(shipment: Shipment): Shipment = {
    shipment.packed(DateTime.now).fold(err => fail("could not make a packed shipment"), s => s)
  }

  def makeSentShipment(shipment: Shipment): Shipment = {
    makePackedShipment(shipment).sent(DateTime.now).fold(
      err => fail("could not make a sent shipment"), s => s)
  }

  def makeReceivedShipment(shipment: Shipment): Shipment = {
    makeSentShipment(shipment).received(DateTime.now).fold(
      err => fail("could not make a received shipment"), s => s)
  }

  def makeUnpackedShipment(shipment: Shipment): Shipment = {
    makeReceivedShipment(shipment).unpacked(DateTime.now).fold(
      err => fail("could not make a unpacked shipment"), s => s)
  }

  def makeLostShipment(shipment: Shipment): Shipment = {
    makeSentShipment(shipment).lost.fold(
      err => fail("could not make a lost shipment"), s => s)
  }

  def compareObj(json: JsValue, dto: ShipmentSpecimenDto) = {
    (json \ "id").as[String] mustBe (dto.id)
    (json \ "shipmentId").as[String] mustBe (dto.shipmentId)
    (json \ "state").as[String] mustBe (dto.state)
    (json \ "specimenId").as[String] mustBe (dto.specimenId)
    (json \ "inventoryId").as[String] mustBe (dto.inventoryId)
    (json \ "version").as[Long] mustBe (dto.version)
    TestUtils.checkTimeStamps(dto.timeAdded, (json \ "timeAdded").as[DateTime])
    TestUtils.checkOpionalTime(dto.timeModified, (json \ "timeModified").asOpt[DateTime])
    (json \ "locationId").as[String] mustBe (dto.locationId)
    (json \ "locationName").as[String] mustBe (dto.locationName)
    TestUtils.checkTimeStamps(dto.timeCreated, (json \ "timeCreated").as[DateTime])
    (json \ "amount").as[BigDecimal] mustBe (dto.amount)
    (json \ "units").as[String] mustBe (dto.units)
    (json \ "status").as[String] mustBe (dto.status)
  }

  val stateToUrl = Map(
      ShipmentItemState.Received -> "received",
      ShipmentItemState.Missing  -> "missing",
      ShipmentItemState.Extra    -> "extra"

    )

  def changeShipmentSpecimenState(state: ShipmentItemState): Unit = {
    val f = specimensFixture(1)

    val shipment = makeUnpackedShipment(f.shipment)
    shipmentRepository.put(shipment)

    val specimen = f.specimens.head
    val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                               specimenId = specimen.id)
    shipmentSpecimenRepository.put(shipmentSpecimen)
    val url = uriSpecimen(f.shipment, shipmentSpecimen, stateToUrl(state))

    val updateJson = Json.obj("shipmentId"      -> shipment.id.id,
                              "id"              -> shipmentSpecimen.id.id,
                              "expectedVersion" -> shipmentSpecimen.version)

    val reply = makeRequest(POST, url, updateJson)

    (reply \ "status").as[String] must include ("success")

    val ssId = ShipmentSpecimenId((reply \ "data" \ "id").as[String])

    shipmentSpecimenRepository.getByKey(ssId) mustSucceed { repoSs =>

      repoSs must have (
        'id         (ssId),
        'version    (shipmentSpecimen.version + 1),
        'shipmentId (shipment.id),
        'specimenId (specimen.id),
        'state      (state)
      )

      TestUtils.checkTimeStamps(repoSs.timeAdded, shipmentSpecimen.timeAdded)
      TestUtils.checkOpionalTime(repoSs.timeModified, Some(DateTime.now))
    }
  }

  def changeShipmentSpecimenStateNotUnpacked(state: ShipmentItemState) = {

    def attemptStateChange(shipment: Shipment, specimen: Specimen) = {
      shipmentRepository.put(shipment)

      val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = shipment.id,
                                                                 specimenId = specimen.id)
      shipmentSpecimenRepository.put(shipmentSpecimen)
      val url = uriSpecimen(shipment, shipmentSpecimen, stateToUrl(state))

      val updateJson = Json.obj("shipmentId"      -> shipment.id.id,
                                "id"              -> shipmentSpecimen.id.id,
                                "expectedVersion" -> shipmentSpecimen.version)

      val reply = makeRequest(POST, url, BAD_REQUEST, updateJson)

      (reply \ "status").as[String] must include ("error")

      (reply \ "message").as[String] must include (
        "EntityCriteriaError: shipment is not in unpacked state")
    }

    val f = specimensFixture(1)

    attemptStateChange(f.shipment, f.specimens.head)

    val packedShipment = makePackedShipment(f.shipment)
    attemptStateChange(packedShipment, f.specimens.head)

    val sentShipment = makeSentShipment(f.shipment)
    attemptStateChange(sentShipment, f.specimens.head)

    val receivedShipment = makeReceivedShipment(f.shipment)
    attemptStateChange(receivedShipment, f.specimens.head)

    val lostShipment = makeLostShipment(f.shipment)
    attemptStateChange(lostShipment, f.specimens.head)
  }

  def changeShipmentSpecimenStateInvalidId(state: ShipmentItemState) = {
    val f = specimensFixture(1)
    val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                               specimenId = f.specimens.head.id)
    val url = uriSpecimen(f.shipment, shipmentSpecimen, stateToUrl(state))

    val updateJson = Json.obj("shipmentId"      -> f.shipment.id.id,
                              "id"              -> shipmentSpecimen.id.id,
                              "expectedVersion" -> shipmentSpecimen.version)

    val reply = makeRequest(POST, url, NOT_FOUND, updateJson)

    (reply \ "status").as[String] must include ("error")

    (reply \ "message").as[String] must include ("IdNotFound: shipment specimen id")
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
        val f = createdShipmentFixture
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


    "POST /shipment/courier/:id" must {

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

    "POST /shipment/trackingnumber/:id" must {

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

    "POST /shipment/fromlocation/:id" must {

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

    "POST /shipment/tolocation/:id" must {

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

    "POST /shipment/packed/:id" must {

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
    }

    "GET /shipments/specimens/:id" must {

      "work for shipment with no specimens" in {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri       = uri(f.shipment, "specimens"),
            offset    = 0,
            total     = 0,
            maybeNext = None,
            maybePrev = None)
        jsonItems must have size 0
      }

      "work for shipment with one specimen" in {
        val f = specimensFixture(1)

        val specimen = f.specimens.head
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                                   specimenId = specimen.id)

        shipmentSpecimenRepository.put(shipmentSpecimen)

        val jsonItem = PagedResultsSpec(this).singleItemResult(uri(f.shipment, "specimens"))

        f.fromCentre.locationName(specimen.locationId) mustSucceed { name =>
          val dto = shipmentSpecimen.createDto(
              specimen,
              name,
              f.ceventType.specimenSpecs.head.units)
          compareObj(jsonItem, dto)
        }
      }

      "work for shipment with more than one specimen" in {
        val numSpecimens = 2
        val f = specimensFixture(numSpecimens)

        val shipmentSpecimenMap = f.specimens.map { specimen =>
            val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                                       specimenId = specimen.id)
            specimen -> shipmentSpecimen
          }.toMap

        shipmentSpecimenMap.values.foreach(shipmentSpecimenRepository.put)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri       = uri(f.shipment, "specimens"),
            offset    = 0,
            total     = numSpecimens,
            maybeNext = None,
            maybePrev = None)

        shipmentSpecimenMap.zipWithIndex.foreach { case ((specimen, shipmentSpecimen), index) =>
          f.fromCentre.locationName(specimen.locationId) mustSucceed { name =>
            val dto = shipmentSpecimen.createDto(
                specimen,
                name,
                f.ceventType.specimenSpecs.head.units)
            compareObj(jsonItems(index), dto)
          }
        }
      }

      "list a single specimen when using paged query" in {
        val numSpecimens = 2
        val f = shipmentSpecimensFixture(numSpecimens)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri         = uri(f.shipment, "specimens"),
            queryParams = Map("pageSize" -> "1"),
            total       = numSpecimens,
            maybeNext   = Some(2))


        compareObj(jsonItem, f.shipmentSpecimenMap.values.head._3)
      }

      "list the last specimen when using paged query" in {
        val numSpecimens = 2
        val f = shipmentSpecimensFixture(numSpecimens)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri         = uri(f.shipment, "specimens"),
            queryParams = Map("page" -> "2", "pageSize" -> "1"),
            total       = numSpecimens,
            offset      = 1,
            maybeNext   = None,
            maybePrev   = Some(1))

        compareObj(jsonItem, f.shipmentSpecimenMap.values.toList(1)._3)
      }

      "fail when using an invalid query parameters" in {
        val f = shipmentSpecimensFixture(2)
        val url = uri(f.shipment, "specimens")
        PagedResultsSpec(this).failWithNegativePageNumber(url)
        PagedResultsSpec(this).failWithInvalidPageNumber(url)
        PagedResultsSpec(this).failWithNegativePageSize(url)
        PagedResultsSpec(this).failWithInvalidPageSize(url, 100);
        PagedResultsSpec(this).failWithInvalidSort(url)
      }
    }

    "GET /shipments/specimens/:shId/:shSpcId" must {

      "get a shipment specimen" in {
        val f = shipmentSpecimensFixture(1)
        val shipmentSpecimen = f.shipmentSpecimenMap.values.head._2
        val dto = f.shipmentSpecimenMap.values.head._3

        val json = makeRequest(GET, uri(f.shipment, "specimens") + s"/${shipmentSpecimen.id}")

        (json \ "status").as[String] must include ("success")

        val jsonObj = (json \ "data").as[JsObject]

        compareObj(jsonObj, dto)
      }

      "fails for an invalid shipment id" in {
        val f = shipmentSpecimensFixture(1)
        val shipmentSpecimen = f.shipmentSpecimenMap.values.head._2

        val badShipment = factory.createShipment

        val json = makeRequest(GET, uri(badShipment, "specimens") + s"/${shipmentSpecimen.id}", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*shipment id")
      }

      "fails for an invalid shipment specimen id" in {
        val f = shipmentSpecimensFixture(1)
        val badShipmentSpecimen = factory.createShipmentSpecimen

        val json = makeRequest(GET, uri(f.shipment, "specimens") + s"/${badShipmentSpecimen.id}", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*shipment specimen id")
      }

    }

    "POST /shipments/specimens/:id" must {

      "add a specimen to a shipment" in {
        val f = specimensFixture(1)
        val specimen = f.specimens.head
        val addJson = Json.obj("shipmentId" -> f.shipment.id.id, "specimenId" -> specimen.id.id)
        val reply = makeRequest(POST, uriSpecimen(f.shipment), addJson)

        (reply \ "status").as[String] must include ("success")

        val replyId = ShipmentSpecimenId((reply \ "data" \ "id").as[String])

        shipmentSpecimenRepository.getByKey(replyId) mustSucceed { repoSs =>

          repoSs must have (
            'id         (replyId),
            'version    (0L),
            'shipmentId (f.shipment.id),
            'specimenId (specimen.id),
            'state      (ShipmentItemState.Present)
            )

          TestUtils.checkTimeStamps(repoSs.timeAdded, DateTime.now)
          TestUtils.checkOpionalTime(repoSs.timeModified, None)
        }
      }

      "not add a specimen to a shipment which is not in the system" in {
        val f = specimensFixture(1)
        val shipment = factory.createShipment
        val specimen = f.specimens.head
        val addJson = Json.obj("shipmentId" -> shipment.id.id, "specimenId" -> specimen.id.id)
        val reply = makeRequest(POST, uriSpecimen(shipment), NOT_FOUND, addJson)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex ("IdNotFound.*shipment id")
      }

      "not add a specimen to a shipment not in created state" in {
        def tryAddOnBadShipment(shipment: Shipment, specimen: UsableSpecimen): Unit = {
          val addJson = Json.obj("shipmentId" -> shipment.id.id, "specimenId" -> specimen.id.id)

          shipmentRepository.put(shipment)

          val reply = makeRequest(POST, uriSpecimen(shipment), BAD_REQUEST, addJson)

          (reply \ "status").as[String] must include ("error")

          (reply \ "message").as[String] must include regex ("EntityCriteriaError.*not in created state")
        }

        val f = specimensFixture(1)
        val specimen = f.specimens.head

        info("packed shipment")
        tryAddOnBadShipment(makePackedShipment(f.shipment), specimen)
        info("sent shipment")
        tryAddOnBadShipment(makeSentShipment(f.shipment), specimen)
        info("received shipment")
        tryAddOnBadShipment(makeReceivedShipment(f.shipment), specimen)
        info("unpacked shipment")
        tryAddOnBadShipment(makeUnpackedShipment(f.shipment), specimen)
        info("lost shipment")
        tryAddOnBadShipment(makeLostShipment(f.shipment), specimen)
      }
    }

    "POST /shipments/specimens/received/:shId/:shSpcId" must {

      "change the status on a shipment specimen" in {
        changeShipmentSpecimenState(ShipmentItemState.Received)
      }

      "fails for a shipment not in unpacked state" in {
        changeShipmentSpecimenStateNotUnpacked(ShipmentItemState.Received)
      }

      "fails for a shipment specimen not in the system" in {
        changeShipmentSpecimenStateInvalidId(ShipmentItemState.Received)
      }

    }

    "POST /shipments/specimens/missing/:shId/:shSpcId" must {

      "change the status on a shipment specimen" in {
        changeShipmentSpecimenState(ShipmentItemState.Missing)
      }

      "fails for a shipment not in packed state" in {
        changeShipmentSpecimenStateNotUnpacked(ShipmentItemState.Missing)
      }

      "fails for a shipment not in the system" in {
        changeShipmentSpecimenStateInvalidId(ShipmentItemState.Missing)
      }

    }

    "POST /shipments/specimens/extra/:shId/:shSpcId"  must {

      "change the status on a shipment specimen" in {
        changeShipmentSpecimenState(ShipmentItemState.Extra)
      }

      "fails for a shipment not in packed state" in {
        changeShipmentSpecimenStateNotUnpacked(ShipmentItemState.Extra)
      }

      "fails for a shipment not in the system" in {
        changeShipmentSpecimenStateInvalidId(ShipmentItemState.Extra)
      }

    }


    "DELETE /shipments/specimens/:shId/:shSpcId/:ver" must {

      "must delete a specimen from shipment in created state" in {
        val f = specimensFixture(1)

        val specimen = f.specimens.head
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                                   specimenId = specimen.id)
        shipmentSpecimenRepository.put(shipmentSpecimen)
        val url = uriSpecimen(f.shipment, shipmentSpecimen) + s"/${shipmentSpecimen.version}"

        val json = makeRequest(DELETE, url)

        (json \ "status").as[String] must include ("success")

        shipmentSpecimenRepository.getByKey(shipmentSpecimen.id) mustFail "IdNotFound.*shipment specimen.*"
      }

      "must not delete a specimen from a shipment not in created state" in {
        def removeShipment(shipment: Shipment, specimen: Specimen): Unit = {
          shipmentRepository.put(shipment)
          val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = shipment.id,
                                                                     specimenId = specimen.id)

          val url = uriSpecimen(shipment, shipmentSpecimen) + s"/${shipmentSpecimen.version}"
          shipmentSpecimenRepository.put(shipmentSpecimen)

          val json = makeRequest(DELETE, url, BAD_REQUEST)

          (json \ "status").as[String] must include ("error")

          (json \ "message").as[String] must include regex (
            "EntityCriteriaError.*shipment is not in created state")

          shipmentSpecimenRepository.getByKey(shipmentSpecimen.id).leftMap { _ =>
            fail("should still be in repository")
          }
          ()
        }

        val f = specimensFixture(1)
        val specimen = f.specimens.head

        info("packed shipment")
        removeShipment(makePackedShipment(f.shipment), specimen)
        info("sent shipment")
        removeShipment(makeSentShipment(f.shipment), specimen)
        info("received shipment")
        removeShipment(makeReceivedShipment(f.shipment), specimen)
        info("unpacked shipment")
        removeShipment(makeUnpackedShipment(f.shipment), specimen)
        info("lost shipment")
        removeShipment(makeLostShipment(f.shipment), specimen)
      }

    }

  }

}
