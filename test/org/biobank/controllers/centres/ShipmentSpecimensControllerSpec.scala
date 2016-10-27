package org.biobank.controllers.centres
import com.github.nscala_time.time.Imports._
import org.biobank.TestUtils
import org.biobank.controllers.PagedResultsSpec
import org.biobank.dto.{CentreLocationInfo}
import org.biobank.domain.centre._
import org.biobank.domain.centre.ShipmentItemState._
import org.biobank.domain.participants._
import play.api.libs.json._
import play.api.test.Helpers._
import scala.language.reflectiveCalls

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
    s"/shipments/specimens/${shipment.id.id}"

  def uri(shipment: Shipment, shipmentSpecimen: ShipmentSpecimen, path: String): String =
    s"/shipments/specimens/$path/${shipment.id.id}/${shipmentSpecimen.id.id}"

  def uri(shipment: Shipment, shipmentSpecimen: ShipmentSpecimen): String =
    s"/shipments/specimens/${shipment.id.id}/${shipmentSpecimen.id.id}"

  "Shipment specimens REST API" when {

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

        val originLocationName = f.fromCentre.locationName(specimen.originLocationId)
          .fold(e => "error", n => n)
        val centreLocationInfo = CentreLocationInfo(f.fromCentre.id.id,
                                                    specimen.originLocationId,
                                                    originLocationName)

        val dto = shipmentSpecimen.createDto(
            specimen.createDto(f.cevent, f.specimenSpec, centreLocationInfo, centreLocationInfo))
        compareObj(jsonItem, dto)
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
            total     = numSpecimens.toLong,
            maybeNext = None,
            maybePrev = None)

        shipmentSpecimenMap.zipWithIndex.foreach { case ((specimen, shipmentSpecimen), index) =>
          val originLocationName = f.fromCentre.locationName(specimen.originLocationId)
            .fold(e => "error", n => n)

          val centreLocationInfo = CentreLocationInfo(f.fromCentre.id.id,
                                                      specimen.originLocationId,
                                                      originLocationName)
          val dto = shipmentSpecimen.createDto(
              specimen.createDto(f.cevent, f.specimenSpec, centreLocationInfo, centreLocationInfo))
          compareObj(jsonItems(index), dto)
        }
      }

      "list shipment specimens by item state" in {
        val numSpecimens = 4
        val f = shipmentSpecimensFixture(numSpecimens)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri         = uri(f.shipment, "specimens"),
            queryParams = Map("pageSize" -> "1"),
            total       = numSpecimens.toLong,
            maybeNext   = Some(2))


        compareObj(jsonItem, f.shipmentSpecimenMap.values.head._4)
      }

      "list shipment specimens filtered by item state" in {
        val numSpecimens = ShipmentItemState.values.size
        val f = shipmentSpecimensFixture(numSpecimens)

        val shipmentSpecimensMap = f.shipmentSpecimenMap.values.zip(ShipmentItemState.values).
          map { case ((spc, spcDto, shipSpc, shipSpcDto), itemState) =>
            val shipmentSpecimen = shipSpc.copy(state = itemState)
            val shipmentSpecimenDto = shipmentSpecimen.createDto(spcDto)
            shipmentSpecimenRepository.put(shipmentSpecimen)
            (itemState, shipmentSpecimen, shipmentSpecimenDto)
          }

        shipmentSpecimensMap.foreach { case (itemState, shipmentSpecimen, shipmentSpecimenDto ) =>
          val jsonItem = PagedResultsSpec(this).singleItemResult(
              uri         = uri(f.shipment, "specimens"),
              queryParams = Map("stateFilter" -> itemState.toString),
              total       = 1,
              maybeNext   = None)

          compareObj(jsonItem, shipmentSpecimenDto)
        }
      }

      "fail for an invalid item state for a shipment specimen" in {
        val f = createdShipmentFixture
        val invalidStateName = nameGenerator.next[ShipmentSpecimen]

        val reply = makeRequest(GET,
                                uri(f.shipment, "specimens") + s"?stateFilter=$invalidStateName",
                                BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex ("invalid shipment item state")
      }

      "list a single specimen when using paged query" in {
        val numSpecimens = 2
        val f = shipmentSpecimensFixture(numSpecimens)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri         = uri(f.shipment, "specimens"),
            queryParams = Map("pageSize" -> "1"),
            total       = numSpecimens.toLong,
            maybeNext   = Some(2))


        compareObj(jsonItem, f.shipmentSpecimenMap.values.head._4)
      }

      "list the last specimen when using paged query" in {
        val numSpecimens = 2
        val f = shipmentSpecimensFixture(numSpecimens)

        val jsonItem = PagedResultsSpec(this).singleItemResult(
            uri         = uri(f.shipment, "specimens"),
            queryParams = Map("page" -> "2", "pageSize" -> "1"),
            total       = numSpecimens.toLong,
            offset      = 1,
            maybeNext   = None,
            maybePrev   = Some(1))

        compareObj(jsonItem, f.shipmentSpecimenMap.values.toList(1)._4)
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

      "list specimens in descending order by state" in {
        val numSpecimens = ShipmentItemState.values.size
        val f = shipmentSpecimensFixture(numSpecimens)

        val shipmentSpecimensMap = f.shipmentSpecimenMap.values.zip(ShipmentItemState.values).
          map { case ((spc, spcDto, shipSpc, shipSpcDto), itemState) =>
            val shipmentSpecimen = shipSpc.copy(state = itemState)
            val shipmentSpecimenDto = shipmentSpecimen.createDto(spcDto)
            shipmentSpecimenRepository.put(shipmentSpecimen)
            (itemState, (shipmentSpecimen, shipmentSpecimenDto))
          }.toMap

        val jsonItems = PagedResultsSpec(this).multipleItemsResult(
            uri         = uri(f.shipment, "specimens"),
            queryParams = Map("sort" -> "state", "order" -> "desc"),
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

    "GET /shipments/specimens/:shId/:shSpcId" must {

      "get a shipment specimen" in {
        val f = shipmentSpecimensFixture(1)
        val shipmentSpecimen = f.shipmentSpecimenMap.values.head._3
        val dto = f.shipmentSpecimenMap.values.head._4

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

    "GET /shipments/specimens/canadd/:shId/:invId" must {

      "can add a specimen inventory Id" in {
        val f = specimensFixture(1)
        shipmentRepository.put(f.shipment)
        val specimen = f.specimens.head
        specimenRepository.put(specimen)

        val originLocationName = f.fromCentre.locationName(specimen.originLocationId).
          fold(e => "error", n => n)
        val centreLocationInfo = CentreLocationInfo(f.fromCentre.id.id,
                                                    specimen.originLocationId,
                                                    originLocationName)
        val specimenDto =
          specimen.createDto(f.cevent, f.specimenSpec, centreLocationInfo, centreLocationInfo)


        val url = uri(f.shipment, "specimens/canadd") + s"/${specimen.inventoryId}"
        val reply = makeRequest(GET, url)

        (reply \ "status").as[String] must include ("success")

        val jsonObj = (reply \ "data").as[JsObject]
        compareObj(jsonObj, specimenDto)
      }

      "fail when adding a specimen inventory Id already in the shipment" in {
        val f = shipmentSpecimensFixture(1)
        val specimen = f.shipmentSpecimenMap.values.head._1

        val url = uri(f.shipment, "specimens/canadd") + s"/${specimen.inventoryId}"
        val reply = makeRequest(GET, url, BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("specimen already in shipment")
      }

      "cannot add an specimen inventory Id that does not exist" in {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val invalidInventoryId = nameGenerator.next[Specimen]
        val url = uri(f.shipment, "specimens/canadd") + s"/$invalidInventoryId"
        val reply = makeRequest(GET, url, NOT_FOUND)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include (
          "EntityCriteriaError: specimen with inventory ID not found")
      }

      "cannot add a specimen inventory Id that not present at shipment's from centre" in {
        val f = specimensFixture(1)
        val specimen = f.specimens.head.copy(locationId = f.toCentre.locations.head.uniqueId)
        specimenRepository.put(specimen)

        val url = uri(f.shipment, "specimens/canadd") + s"/${specimen.inventoryId}"
        val reply = makeRequest(GET, url, BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("specimen not at shipment's from location")
      }

      "fails for a specimen already in another active shipment" in {
        val f = shipmentSpecimensFixture(1)
        val specimen = f.shipmentSpecimenMap.values.head._1
        val newShipment = factory.createShipment(f.fromCentre, f.toCentre)
        shipmentRepository.put(newShipment)

        val url = uri(newShipment, "specimens/canadd") + s"/${specimen.inventoryId}"
        val reply = makeRequest(GET, url, BAD_REQUEST)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include (
          "EntityCriteriaError: specimen is already in active shipment")
      }
    }

    "POST /shipments/specimens/:id" must {

      "add a specimen to a shipment" in {
        val f = specimensFixture(1)
        val specimen = f.specimens.head
        val addJson = Json.obj("shipmentId" -> f.shipment.id.id, "specimenId" -> specimen.id.id)
        val reply = makeRequest(POST, uri(f.shipment), addJson)

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
        val reply = makeRequest(POST, uri(shipment), NOT_FOUND, addJson)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include regex ("IdNotFound.*shipment id")
      }

      "not add a specimen to a shipment not in created state" in {
        def tryAddOnBadShipment(shipment: Shipment, specimen: UsableSpecimen): Unit = {
          val addJson = Json.obj("shipmentId" -> shipment.id.id, "specimenId" -> specimen.id.id)

          shipmentRepository.put(shipment)

          val reply = makeRequest(POST, uri(shipment), BAD_REQUEST, addJson)

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

      "not add a specimen from a different centre to a shipment" in {
        val f = specimensFixture(1)
        shipmentRepository.put(f.shipment)
        val specimen = f.specimens.head.copy(locationId = f.toCentre.locations.head.uniqueId)
        specimenRepository.put(specimen)

        val addJson = Json.obj("shipmentId" -> f.shipment.id.id, "specimenId" -> specimen.id.id)
        val reply = makeRequest(POST, uri(f.shipment), BAD_REQUEST, addJson)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include (
          "EntityCriteriaError: specimen not present at shipment's originating centre")
      }
    }

    "adding specimens to shipments" must {

      val stateToUrl = Map(ShipmentItemState.Received -> "received",
                           ShipmentItemState.Missing  -> "missing",
                           ShipmentItemState.Extra    -> "extra")

      def changeShipmentSpecimenState(state: ShipmentItemState): Unit = {
        val f = specimensFixture(1)

        val shipment = makeUnpackedShipment(f.shipment)
        shipmentRepository.put(shipment)

        val specimen = f.specimens.head
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                                   specimenId = specimen.id)
        shipmentSpecimenRepository.put(shipmentSpecimen)
        val url = uri(f.shipment, shipmentSpecimen, stateToUrl(state))

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
          val url = uri(shipment, shipmentSpecimen, stateToUrl(state))

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
        val url = uri(f.shipment, shipmentSpecimen, stateToUrl(state))

        val updateJson = Json.obj("shipmentId"      -> f.shipment.id.id,
                                  "id"              -> shipmentSpecimen.id.id,
                                  "expectedVersion" -> shipmentSpecimen.version)

        val reply = makeRequest(POST, url, NOT_FOUND, updateJson)

        (reply \ "status").as[String] must include ("error")

        (reply \ "message").as[String] must include ("IdNotFound: shipment specimen id")
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

    }


    "DELETE /shipments/specimens/:shId/:shSpcId/:ver" must {

      "must delete a specimen from shipment in created state" in {
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

      "must not delete a specimen from a shipment not in created state" in {
        def removeShipment(shipment: Shipment, specimen: Specimen): Unit = {
          shipmentRepository.put(shipment)
          val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = shipment.id,
                                                                     specimenId = specimen.id)

          val url = uri(shipment, shipmentSpecimen) + s"/${shipmentSpecimen.version}"
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
