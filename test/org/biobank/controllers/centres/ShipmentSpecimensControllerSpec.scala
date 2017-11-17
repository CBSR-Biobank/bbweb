package org.biobank.controllers.centres

import java.time.OffsetDateTime
import org.biobank.TestUtils
import org.biobank.controllers.PagedResultsSpec
import org.biobank.domain.centre._
import org.biobank.domain.participants._
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.biobank.service.centres.CentreLocationInfo
import play.api.libs.json._
import play.api.test.Helpers._

/**
 * Tests the REST API for [[ShipmentSpecimen]]s.
 *
 * Tests for [[Shipment]]s in ShipmentsControllerSpec.scala.
 */
class ShipmentSpecimensControllerSpec
    extends ShipmentsControllerSpecFixtures
    with ShipmentsControllerSpecUtils {
  import org.biobank.TestUtils._

  override def uri(shipment: Shipment): String =
    uri() + s"specimens/${shipment.id.id}"

  override def uri(shipment: Shipment, path: String): String =
    uri() + s"specimens/$path/${shipment.id.id}"

  def uri(shipment: Shipment, shipmentSpecimen: ShipmentSpecimen, path: String): String =
    uri() + s"specimens/$path/${shipment.id.id}/${shipmentSpecimen.id.id}"

  def uri(shipment: Shipment, shipmentSpecimen: ShipmentSpecimen): String =
    uri() + s"specimens/${shipment.id.id}/${shipmentSpecimen.id.id}"

  describe("Shipment specimens REST API") {

    describe("GET /api/shipments/specimens/:id") {

      it("work for shipment with no specimens") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri       = uri(f.shipment),
            offset    = 0,
            total     = 0,
            maybeNext = None,
            maybePrev = None)
        jsonItems must have size 0
      }

      it("work for shipment with one specimen") {
        val f = specimensFixture(1)

        val specimen = f.specimens.head
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                                   specimenId = specimen.id)

        shipmentSpecimenRepository.put(shipmentSpecimen)

        val jsonItem = PagedResultsSpec(this).singleItemResult(uri(f.shipment))

        val originLocationName = f.fromCentre.locationName(specimen.originLocationId)
          .fold(e => "error", n => n)
        val centreLocationInfo = CentreLocationInfo(f.fromCentre.id.id,
                                                    specimen.originLocationId.id,
                                                    originLocationName)

        val dto = shipmentSpecimen.createDto(
            specimen.createDto(f.cevent, f.specimenDescription, centreLocationInfo, centreLocationInfo))
        compareObj(jsonItem, dto)
      }

      it("work for shipment with more than one specimen") {
        val numSpecimens = 2
        val f = specimensFixture(numSpecimens)

        val shipmentSpecimenMap = f.specimens.map { specimen =>
            val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                                       specimenId = specimen.id)
            specimen -> shipmentSpecimen
          }.toMap

        shipmentSpecimenMap.values.foreach(shipmentSpecimenRepository.put)

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri       = uri(f.shipment),
            offset    = 0,
            total     = numSpecimens.toLong,
            maybeNext = None,
            maybePrev = None)

        shipmentSpecimenMap.zipWithIndex.foreach { case ((specimen, shipmentSpecimen), index) =>
          val originLocationName = f.fromCentre.locationName(specimen.originLocationId)
            .fold(e => "error", n => n)

          val centreLocationInfo = CentreLocationInfo(f.fromCentre.id.id,
                                                      specimen.originLocationId.id,
                                                      originLocationName)
          val dto = shipmentSpecimen.createDto(
              specimen.createDto(f.cevent, f.specimenDescription, centreLocationInfo, centreLocationInfo))
          compareObj(jsonItems(index), dto)
        }
      }

      it("list shipment specimens by item state") {
        val numSpecimens = 4
        val f = shipmentSpecimensFixture(numSpecimens)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri         = uri(f.shipment),
            queryParams = Map("limit" -> "1"),
            total       = numSpecimens.toLong,
            maybeNext   = Some(2))


        compareObj(jsonItem, f.shipmentSpecimenMap.values.head.shipmentSpecimenDto)
      }

      it("list shipment specimens filtered by item state") {
        val numSpecimens = ShipmentItemState.values.size
        val f = shipmentSpecimensFixture(numSpecimens)

        val shipmentSpecimensMap = f.shipmentSpecimenMap.values.zip(ShipmentItemState.values).
          map { case (shipmentSpecimenData, itemState) =>
            val shipmentSpecimen = shipmentSpecimenData.shipmentSpecimen.copy(state = itemState)
            val shipmentSpecimenDto = shipmentSpecimen.createDto(shipmentSpecimenData.specimenDto)
            shipmentSpecimenRepository.put(shipmentSpecimen)
            (itemState, shipmentSpecimen, shipmentSpecimenDto)
          }

        shipmentSpecimensMap.foreach { case (itemState, shipmentSpecimen, shipmentSpecimenDto ) =>
          val jsonItem = PagedResultsSpec(this).singleItemResult(
              uri         = uri(f.shipment),
              queryParams = Map("filter" -> s"state::$itemState"),
              total       = 1,
              maybeNext   = None)

          compareObj(jsonItem, shipmentSpecimenDto)
        }
      }

      it("fail for an invalid item state for a shipment specimen") {
        val f = createdShipmentFixture
        val invalidStateName = "state::" + nameGenerator.next[ShipmentSpecimen]
        shipmentRepository.put(f.shipment)

        val reply = makeRequest(GET,
                                uri(f.shipment) + s"?filter=$invalidStateName",
                                NOT_FOUND)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex (
          "InvalidState: shipment specimen state does not exist")
      }

      it("list a single specimen when using paged query") {
        val numSpecimens = 2
        val f = shipmentSpecimensFixture(numSpecimens)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri         = uri(f.shipment),
            queryParams = Map("limit" -> "1"),
            total       = numSpecimens.toLong,
            maybeNext   = Some(2))


        compareObj(jsonItem, f.shipmentSpecimenMap.values.head.shipmentSpecimenDto)
      }

      it("list the last specimen when using paged query") {
        val numSpecimens = 2
        val f = shipmentSpecimensFixture(numSpecimens)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri         = uri(f.shipment),
            queryParams = Map("page" -> "2", "limit" -> "1"),
            total       = numSpecimens.toLong,
            offset      = 1,
            maybeNext   = None,
            maybePrev   = Some(1))

        compareObj(jsonItem, f.shipmentSpecimenMap.values.toList(1).shipmentSpecimenDto)
      }

      it("fail when using an invalid query parameters") {
        val f = shipmentSpecimensFixture(2)
        val url = uri(f.shipment)
        PagedResultsSpec(this).failWithNegativePageNumber(url)
        PagedResultsSpec(this).failWithInvalidPageNumber(url)
        PagedResultsSpec(this).failWithNegativePageSize(url)
        PagedResultsSpec(this).failWithInvalidPageSize(url, 100);
        PagedResultsSpec(this).failWithInvalidSort(url)
      }

      it("list specimens in descending order by state") {
        val numSpecimens = ShipmentItemState.values.size
        val f = shipmentSpecimensFixture(numSpecimens)

        val shipmentSpecimensMap = f.shipmentSpecimenMap.values.zip(ShipmentItemState.values).
          map { case (shipmentSpecimenData, itemState) =>
            val shipmentSpecimen = shipmentSpecimenData.shipmentSpecimen.copy(state = itemState)
            val shipmentSpecimenDto = shipmentSpecimen.createDto(shipmentSpecimenData.specimenDto)
            shipmentSpecimenRepository.put(shipmentSpecimen)
            (itemState, (shipmentSpecimen, shipmentSpecimenDto))
          }.toMap

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri         = uri(f.shipment),
            queryParams = Map("sort" -> "-state"),
            offset      = 0,
            total       = shipmentSpecimensMap.size.toLong,
            maybeNext   = None,
            maybePrev   = None)

        compareObj(jsonItems(0), shipmentSpecimensMap.get(ShipmentItemState.Received).value._2)
        compareObj(jsonItems(1), shipmentSpecimensMap.get(ShipmentItemState.Present).value._2)
        compareObj(jsonItems(2), shipmentSpecimensMap.get(ShipmentItemState.Missing).value._2)
        compareObj(jsonItems(3), shipmentSpecimensMap.get(ShipmentItemState.Extra).value._2)
      }
    }

    describe("GET /api/shipments/specimens/:shId/:shSpcId") {

      it("get a shipment specimen") {
        val f = shipmentSpecimensFixture(1)
        val shipmentSpecimen = f.shipmentSpecimenMap.values.head.shipmentSpecimen
        val dto = f.shipmentSpecimenMap.values.head.shipmentSpecimenDto

        val json = makeRequest(GET, uri(f.shipment) + s"/${shipmentSpecimen.id}")

        (json \ "status").as[String] must include ("success")

        val jsonObj = (json \ "data").as[JsObject]
        compareObj(jsonObj, dto)
      }

      it("fails for an invalid shipment id") {
        val f = shipmentSpecimensFixture(1)
        val shipmentSpecimen = f.shipmentSpecimenMap.values.head.specimenDto

        val badShipment = factory.createShipment

        val json = makeRequest(GET, uri(badShipment) + s"/${shipmentSpecimen.id}", NOT_FOUND)

        (json \ "status").as[String] must include ("error")

        (json \ "message").as[String] must include regex ("IdNotFound.*shipment id")
      }

    }

    describe("GET /api/shipments/specimens/canadd/:shId/:invId") {

      it("can add a specimen inventory Id") {
        val f = specimensFixture(1)
        shipmentRepository.put(f.shipment)
        val specimen = f.specimens.head
        specimenRepository.put(specimen)

        val originLocationName = f.fromCentre.locationName(specimen.originLocationId).
          fold(e => "error", n => n)
        val centreLocationInfo = CentreLocationInfo(f.fromCentre.id.id,
                                                    specimen.originLocationId.id,
                                                    originLocationName)
        val specimenDto =
          specimen.createDto(f.cevent, f.specimenDescription, centreLocationInfo, centreLocationInfo)

        val url = uri(f.shipment, "canadd") + s"/${specimen.inventoryId}"
        val reply = makeRequest(GET, url)

        (reply \ "status").as[String] must include ("success")

        val jsonObj = (reply \ "data").as[JsObject]
        compareObj(jsonObj, specimenDto)
      }

      it("fail when adding a specimen inventory Id already in the shipment") {
        val f = shipmentSpecimensFixture(1)
        val specimen = f.shipmentSpecimenMap.values.head.specimen

        val url = uri(f.shipment, "canadd") + s"/${specimen.inventoryId}"
        val reply = makeRequest(GET, url, BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("specimens are already in an active shipment")
      }

      it("not add a specimen inventory Id that does not exist") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val invalidInventoryId = nameGenerator.next[Specimen]
        val url = uri(f.shipment, "canadd") + s"/$invalidInventoryId"
        val reply = makeRequest(GET, url, NOT_FOUND)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include (
          "EntityCriteriaError: specimen with inventory ID not found")
      }

      it("not add a specimen inventory Id that not present at shipment's from centre") {
        val f = specimensFixture(1)
        val specimen = f.specimens.head.copy(locationId = f.toCentre.locations.head.id)
        specimenRepository.put(specimen)

        val url = uri(f.shipment, "canadd") + s"/${specimen.inventoryId}"
        val reply = makeRequest(GET, url, BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("specimen not at shipment's from location")
      }

      it("fails for a specimen already in another active shipment") {
        val f = shipmentSpecimensFixture(1)
        val specimen = f.shipmentSpecimenMap.values.head.specimen
        val newShipment = factory.createShipment(f.fromCentre, f.toCentre)
        shipmentRepository.put(newShipment)

        val url = uri(newShipment, "canadd") + s"/${specimen.inventoryId}"
        val reply = makeRequest(GET, url, BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include (
          "EntityCriteriaError: specimens are already in an active shipment")
      }
    }

    describe("POST /api/shipments/specimens/:id") {

      it("add a specimen to a shipment") {
        val f = specimensFixture(1)
        val specimen = f.specimens.head
        val addJson = Json.obj("specimenInventoryIds" -> List(specimen.inventoryId))
        val reply = makeRequest(POST, uri(f.shipment), addJson)

        (reply \ "status").as[String] must include ("success")

        val replyShipmentId = ShipmentId((reply \ "data" \ "id").as[String])

        val repoShipmentSpecimens = shipmentSpecimenRepository.allForShipment(replyShipmentId)
        repoShipmentSpecimens must have size (1)

        val repoSs = repoShipmentSpecimens.headOption.value
        repoSs must have (
          'version    (0L),
          'shipmentId (f.shipment.id),
          'specimenId (specimen.id),
          'state      (ShipmentItemState.Present)
        )

        TestUtils.checkTimeStamps(repoSs.timeAdded, OffsetDateTime.now)
        TestUtils.checkOpionalTime(repoSs.timeModified, None)
      }

      it("not add a specimen to a shipment which is not in the system") {
        val f = specimensFixture(1)
        val shipment = factory.createShipment
        val specimen = f.specimens.head
        val addJson = Json.obj("specimenInventoryIds" -> List(specimen.inventoryId))
        val reply = makeRequest(POST, uri(shipment), NOT_FOUND, addJson)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex ("IdNotFound.*shipment id")
      }

      it("not add a specimen to a shipment not in created state") {
        val f = specimensFixture(1)
        val specimen = f.specimens.head

        val nonCreatedShipments = Table("non created shipments",
                                        makePackedShipment(f.shipment),
                                        makeSentShipment(f.shipment),
                                        makeReceivedShipment(f.shipment),
                                        makeUnpackedShipment(f.shipment),
                                        makeLostShipment(f.shipment))

        forAll(nonCreatedShipments) { shipment =>
          info(s"${shipment.state} shipment")

          val addJson = Json.obj("specimenInventoryIds" -> List(specimen.inventoryId))

          shipmentRepository.put(shipment)

          val reply = makeRequest(POST, uri(shipment), BAD_REQUEST, addJson)

          (reply \ "status").as[String] must include ("error")

          (reply \ "message").as[String] must include regex ("InvalidState: shipment not created")
        }
      }

      it("not add a specimen from a different centre to a shipment") {
        val f = specimensFixture(1)
        shipmentRepository.put(f.shipment)
        val specimen = f.specimens.head.copy(locationId = f.toCentre.locations.head.id)
        specimenRepository.put(specimen)

        val addJson = Json.obj("specimenInventoryIds" -> List(specimen.inventoryId))
        val reply = makeRequest(POST, uri(f.shipment), BAD_REQUEST, addJson)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include (
          "EntityCriteriaError: invalid centre for specimen inventory IDs")
      }
    }

    describe("alter specimens in shipments") {

      val stateData = Table(
          ("shipment specimen states", "url path"),
          (ShipmentItemState.Received, "received"),
          (ShipmentItemState.Missing,  "missing")
        )

      it("change state on a shipment specimen") {
        val f = specimensFixture(1)

        val shipment = makeUnpackedShipment(f.shipment)
        shipmentRepository.put(shipment)

        val specimen = f.specimens.head
        val shipmentSpecimen: ShipmentSpecimen =
          factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id, specimenId = specimen.id)

        forAll(stateData) { case (state, urlPath) =>
          shipmentSpecimenRepository.put(shipmentSpecimen)

          val url = uri(f.shipment, urlPath)

          val reqJson = Json.obj("specimenInventoryIds" -> List(specimen.inventoryId))

          val reply = makeRequest(POST, url, reqJson)

          (reply \ "status").as[String] must include ("success")

          val replyShipmentId = ShipmentId((reply \ "data" \ "id").as[String])

          val repoShipmentSpecimens = shipmentSpecimenRepository.allForShipment(replyShipmentId)
          repoShipmentSpecimens must have size (1)

          val repoSs = repoShipmentSpecimens.headOption.value
          repoSs must have (
            'version    (shipmentSpecimen.version + 1),
            'shipmentId (shipment.id),
            'specimenId (specimen.id),
            'state      (state)
          )

          TestUtils.checkTimeStamps(repoSs.timeAdded, shipmentSpecimen.timeAdded)
          TestUtils.checkOpionalTime(repoSs.timeModified, Some(OffsetDateTime.now))
        }
      }

      it("cannot change a shipment specimen's state if shipment is not PACKED") {
        val f = specimensFixture(1)
        val shipments = Table("shipment",
                              f.shipment,
                              makePackedShipment(f.shipment),
                              makeSentShipment(f.shipment),
                              makeReceivedShipment(f.shipment))
        forAll(shipments) { shipment =>
          val specimen = f.specimens.headOption.value
          val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = shipment.id,
                                                                     specimenId = specimen.id)

          forAll(stateData) { case (state, urlPath) =>
            shipmentRepository.put(shipment)
            shipmentSpecimenRepository.put(shipmentSpecimen)

            val url = uri(shipment, urlPath)

            val reqJson = Json.obj("specimenInventoryIds" -> List(specimen.inventoryId))

            val reply = makeRequest(POST, url, BAD_REQUEST, reqJson)

            (reply \ "status").as[String] must include ("error")

            (reply \ "message").as[String] must include regex ("InvalidState: shipment not unpacked")
          }
        }
      }

      it("cannot change a shipment specimen's state if it's not in the shipment") {
        val f = specimensFixture(1)
        val shipment = makeUnpackedShipment(f.shipment)
        val specimen = f.specimens.headOption.value

        shipmentRepository.put(shipment)
        forAll(stateData) { case (state, urlPath) =>

          val url = uri(shipment, urlPath)
          val reqJson = Json.obj("specimenInventoryIds" -> List(specimen.inventoryId))

          val reply = makeRequest(POST, url, BAD_REQUEST, reqJson)

          (reply \ "status").as[String] must include ("error")

          (reply \ "message").as[String] must include ("EntityCriteriaError: specimens not in this shipment:")
        }
      }

      it("cannot change a shipment specimen's state if the specimen not in the system") {
        val f = specimensFixture(1)
        val shipment = makeUnpackedShipment(f.shipment)
        val specimen = f.specimens.headOption.value

        shipmentRepository.put(shipment)
        specimenRepository.remove(specimen)
        forAll(stateData) { case (state, urlPath) =>

          val url = uri(shipment, urlPath)
          val reqJson = Json.obj("specimenInventoryIds" -> List(specimen.inventoryId))

          val reply = makeRequest(POST, url, BAD_REQUEST, reqJson)

          (reply \ "status").as[String] must include ("error")

          (reply \ "message").as[String] must include ("EntityCriteriaError: invalid inventory Ids:")
        }
      }

      it("cannot change a shipment specimen's state if shipment specimen's state is not present") {
        val f = specimensFixture(1)
        val shipment = makeUnpackedShipment(f.shipment)
        val specimen = f.specimens.headOption.value
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = shipment.id,
                                                                   specimenId = specimen.id)

        shipmentRepository.put(shipment)
        forAll(stateData) { case (state, urlPath) =>
          shipmentSpecimenRepository.put(shipmentSpecimen.copy(state = state))

          val url = uri(shipment, urlPath)
          val reqJson = Json.obj("specimenInventoryIds" -> List(specimen.inventoryId))

          val reply = makeRequest(POST, url, BAD_REQUEST, reqJson)

          (reply \ "status").as[String] must include ("error")

          (reply \ "message").as[String] must include ("EntityCriteriaError: shipment specimens not present:")
        }
      }

      it("change a shipment specimen's state to PRESENT from another state") {
        val f = specimensFixture(1)
        val shipment = makeUnpackedShipment(f.shipment)
        val specimen = f.specimens.headOption.value
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = shipment.id,
                                                                   specimenId = specimen.id)

        shipmentRepository.put(shipment)
        forAll(stateData) { case (state, urlPath) =>
          shipmentSpecimenRepository.put(shipmentSpecimen.copy(state = state))

          val url = uri(shipment, "present")
          val reqJson = Json.obj("specimenInventoryIds" -> List(specimen.inventoryId))

          val reply = makeRequest(POST, url, reqJson)

          (reply \ "status").as[String] must include ("success")

          val replyShipmentId = ShipmentId((reply \ "data" \ "id").as[String])

          val repoShipmentSpecimens = shipmentSpecimenRepository.allForShipment(replyShipmentId)
          repoShipmentSpecimens must have size (1)

          val repoSs = repoShipmentSpecimens.headOption.value
          repoSs must have (
            'version    (shipmentSpecimen.version + 1),
            'shipmentId (shipment.id),
            'specimenId (specimen.id),
            'state      (ShipmentItemState.Present)
          )

          TestUtils.checkTimeStamps(repoSs.timeAdded, shipmentSpecimen.timeAdded)
          TestUtils.checkOpionalTime(repoSs.timeModified, Some(OffsetDateTime.now))
        }
      }

      it("fail when changing a shipment specimen's state to PRESENT when it is already PRESENT") {
        val f = specimensFixture(1)
        val shipment = makeUnpackedShipment(f.shipment)
        val specimen = f.specimens.headOption.value
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = shipment.id,
                                                                   specimenId = specimen.id)

        shipmentRepository.put(shipment)
        shipmentSpecimenRepository.put(shipmentSpecimen.copy(state = ShipmentItemState.Present))

        val url = uri(shipment, "present")
        val reqJson = Json.obj("specimenInventoryIds" -> List(specimen.inventoryId))

        val reply = makeRequest(POST, url, BAD_REQUEST, reqJson)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("EntityCriteriaError: shipment specimens are present:")
      }

      it("add a shipment specimen as EXTRA to a shipment") {
        val f = specimensFixture(1)
        val shipment = makeUnpackedShipment(f.shipment)
        val specimen = f.specimens.headOption.value

        shipmentRepository.put(shipment)
        val url = uri(shipment, "extra")
        val reqJson = Json.obj("specimenInventoryIds" -> List(specimen.inventoryId))

        val reply = makeRequest(POST, url, reqJson)

        (reply \ "status").as[String] must include ("success")

        val replyShipmentId = ShipmentId((reply \ "data" \ "id").as[String])

        val repoShipmentSpecimens = shipmentSpecimenRepository.allForShipment(replyShipmentId)
        repoShipmentSpecimens must have size (1)

        val repoSs = repoShipmentSpecimens.headOption.value
        repoSs must have (
          'version    (0),
          'shipmentId (shipment.id),
          'specimenId (specimen.id),
          'state      (ShipmentItemState.Extra)
        )

        TestUtils.checkTimeStamps(repoSs.timeAdded, OffsetDateTime.now)
        TestUtils.checkOpionalTime(repoSs.timeModified, None)
      }

      it("not add an EXTRA shipment specimen to a shipment if it is present in another shipment") {
        val f = specimensFixture(1)
        val f2 = createdShipmentFixture
        val shipment = makeUnpackedShipment(f.shipment)
        shipmentRepository.put(shipment)
        val specimen = f.specimens.headOption.value
        specimenRepository.put(specimen)

        // this shipment specimen belongs to a different shipment
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f2.shipment.id,
                                                                   specimenId = specimen.id,
                                                                   state      = ShipmentItemState.Present)
        shipmentSpecimenRepository.put(shipmentSpecimen)
        val url = uri(shipment, "extra")
        val reqJson = Json.obj("specimenInventoryIds" -> List(specimen.inventoryId))
        val reply = makeRequest(POST, url, BAD_REQUEST, reqJson)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include (
          "EntityCriteriaError: specimens are already in an active shipment")
      }

      it("not add an EXTRA shipment specimen to a shipment if it is already part of the shipment") {
        val f = specimensFixture(1)
        val shipment = makeUnpackedShipment(f.shipment)
        shipmentRepository.put(shipment)
        val specimen = f.specimens.headOption.value
        specimenRepository.put(specimen)
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = shipment.id,
                                                                   specimenId = specimen.id)
        forAll(stateData) { case (state, urlPath) =>
          shipmentSpecimenRepository.put(shipmentSpecimen.copy(state = state))
          val url = uri(shipment, "extra")
          val reqJson = Json.obj("specimenInventoryIds" -> List(specimen.inventoryId))
          val reply = makeRequest(POST, url, BAD_REQUEST, reqJson)

          (reply \ "status").as[String] must include ("error")

          (reply \ "message").as[String] must include (
            "EntityCriteriaError: specimen inventory IDs already in this shipment: ")
        }
      }

      it("not add an EXTRA shipment specimen to a shipment if specimen at a different centre") {
        val f = specimensFixture(1)
        val shipment = makeUnpackedShipment(f.shipment)
        shipmentRepository.put(shipment)
        val specimen = f.specimens.headOption.value.copy(locationId = f.toCentre.locations.head.id)
        specimenRepository.put(specimen)

        val url = uri(shipment, "extra")
        val reqJson = Json.obj("specimenInventoryIds" -> List(specimen.inventoryId))
        val reply = makeRequest(POST, url, BAD_REQUEST, reqJson)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include (
          "EntityCriteriaError: invalid centre for specimen inventory IDs")
      }

    }

    describe("DELETE /api/shipments/specimens/:shId/:shSpcId/:ver") {

      it("must remove a specimen from shipment in created state") {
        val f = specimensFixture(1)

        val specimen = f.specimens.head
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                                   specimenId = specimen.id)
        shipmentSpecimenRepository.put(shipmentSpecimen)
        val url = uri(f.shipment, shipmentSpecimen) + s"/${shipmentSpecimen.version}"

        val json = makeRequest(DELETE, url)

        (json \ "status").as[String] must include ("success")

        shipmentSpecimenRepository.getByKey(shipmentSpecimen.id) mustFail "IdNotFound.*shipment specimen.*"
      }

      it("must remove an extra specimen from shipment in unpacked state") {
        val f = specimensFixture(1)
        val shipment = makeUnpackedShipment(f.shipment)
        val specimen = f.specimens.head
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                                   specimenId = specimen.id,
                                                                   state      = ShipmentItemState.Extra)
        shipmentRepository.put(shipment)
        shipmentSpecimenRepository.put(shipmentSpecimen)
        val url = uri(f.shipment, shipmentSpecimen) + s"/${shipmentSpecimen.version}"

        val json = makeRequest(DELETE, url)

        (json \ "status").as[String] must include ("success")

        shipmentSpecimenRepository.getByKey(shipmentSpecimen.id) mustFail "IdNotFound.*shipment specimen.*"
      }

      it("must not delete a specimen from a shipment not in created or unpacked state") {
        val f = specimensFixture(1)
        val specimen = f.specimens.head
        val shipments = Table("shipment",
                              makePackedShipment(f.shipment),
                              makeSentShipment(f.shipment),
                              makeReceivedShipment(f.shipment),
                              makeLostShipment(f.shipment))
        val stateData = Table(
            "shipment specimen states",
            ShipmentItemState.Received,
            ShipmentItemState.Missing)

        forAll(shipments) { shipment =>
          forAll(stateData) { shipSpecimenState =>
            info(s"shipment state: ${shipment.state}, shipment specimen state: $shipSpecimenState")
            shipmentRepository.put(shipment)
            val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = shipment.id,
                                                                       specimenId = specimen.id,
                                                                       state      = shipSpecimenState)

            val url = uri(shipment, shipmentSpecimen) + s"/${shipmentSpecimen.version}"
            shipmentSpecimenRepository.put(shipmentSpecimen)

            val json = makeRequest(DELETE, url, BAD_REQUEST)

            (json \ "status").as[String] must include ("error")

            (json \ "message").as[String] must include (
              "EntityCriteriaError: cannot remove, shipment specimen state is invalid")

            shipmentSpecimenRepository.getByKey(shipmentSpecimen.id).leftMap { _ =>
              fail("should still be in repository")
            }
            ()
          }
        }
      }

    }

  }
}
