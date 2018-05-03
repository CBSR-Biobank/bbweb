package org.biobank.controllers.centres

import java.time.OffsetDateTime
import org.biobank.controllers.PagedResultsSharedSpec
import org.biobank.domain.{EntityState, LocationId}
import org.biobank.domain.centres._
import org.biobank.dto._
import org.biobank.fixtures.Url
import org.biobank.matchers.PagedResultsMatchers
import org.scalatest.matchers.{MatchResult, Matcher}
import org.scalatest.prop.TableDrivenPropertyChecks._
import play.api.libs.json._
import play.api.mvc._
import play.api.test.Helpers._
import scala.concurrent.Future

/**
 * Tests the REST API for [[Shipment]]s.
 *
 * Tests for [[ShipmentSpecimen]]s in ShipmentSpecimensControllerSpec.scala.
 */
class ShipmentsControllerSpec
    extends ShipmentsControllerSpecFixtures
    with ShipmentsControllerSpecUtils
    with PagedResultsSharedSpec
    with PagedResultsMatchers {

  import org.biobank.TestUtils._
  import org.biobank.matchers.JsonMatchers._
  import org.biobank.matchers.DtoMatchers._
  import org.biobank.matchers.EntityMatchers._

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

  describe("Shipment REST API") {

    describe("GET /api/shipments/list/:centreId") {

      describe("list a shipment") {

        describe("when filtered by 'from centre'") {
          listSingleShipment() { () =>
            val f = createdShipmentFixture
            shipmentRepository.put(f.shipment)
            (uri(s"list?filter=fromCentre:in:${f.fromCentre.name}"), f.shipment)
          }
        }

        describe("when filtered by 'to centre'") {
          listSingleShipment() { () =>
            val f = createdShipmentFixture
            shipmentRepository.put(f.shipment)
            (uri(s"list?filter=toCentre:in:${f.toCentre.name}"), f.shipment)
          }
        }

        describe("when filtered by 'with centre'") {
          listSingleShipment() { () =>
            val f = createdShipmentFixture
            shipmentRepository.put(f.shipment)
            (uri(s"list?filter=withCentre:in:(${f.fromCentre.name},${f.toCentre.name})"), f.shipment)
          }
        }

      }

      describe("list multiple shipments") {

        describe("when filtered by 'from centre'") {
          listMultipleShipments() { () =>
            val f = createdShipmentsFixture(2)
            val shipments = f.shipmentMap.values.toList.sortWith(_.courierName < _.courierName)
            shipments.foreach(shipmentRepository.put)
            (uri(s"list?filter=fromCentre:in:${f.fromCentre.name}"), shipments)
          }
        }

        describe("when filtered by 'to centre'") {
          listMultipleShipments() { () =>
            val f = createdShipmentsFixture(2)
            val shipments = f.shipmentMap.values.toList.sortWith(_.courierName < _.courierName)
            shipments.foreach(shipmentRepository.put)
            (uri(s"list?filter=toCentre:in:${f.toCentre.name}"), shipments)
          }
        }

        describe("when filtered by 'with centre'") {
          listMultipleShipments() { () =>
            val f = createdShipmentsFixture(2)
            val shipments = f.shipmentMap.values.toList.sortWith(_.courierName < _.courierName)
            shipments.foreach(shipmentRepository.put)
            (uri(s"list?filter=withCentre:in:(${f.fromCentre.name},${f.toCentre.name})"), shipments)
          }
        }

      }

      it("list a shipment when using a 'not equal to' filter on centre name") {
        val f = createdShipmentsFixture(2)
        val shipments = f.shipmentMap.values.toList.sortWith(_.courierName < _.courierName)
        shipments.foreach(shipmentRepository.put)

        val filters = Table("centre filters",
                            s"fromCentre:ne:${f.fromCentre.name}",
                            s"toCentre:ne:${f.toCentre.name}",
                            s"withCentre:ne:(${f.fromCentre.name},${f.toCentre.name})")

        forAll(filters) { centreNameFilter =>
          uri(s"list?filter=$centreNameFilter") must beEmptyResults
        }
      }

      it("list a single shipment when filtered by state") {
        val f = allShipmentsFixture
        f.shipments.values.foreach(shipmentRepository.put)

        forAll(centreFilters(f.fromCentre, f.toCentre)) { centreNameFilter =>
          forAll(states) { state =>
            info(s"$state shipment")
            val url = listUri.path + s"?filter=$centreNameFilter;state::$state"
            val reply = makeAuthRequest(GET, url).value
            reply must beOkResponseWithJsonReply

            val json = contentAsJson(reply)
            json must beSingleItemResults()

            val dtosValidation = (json \ "data" \ "items").validate[List[ShipmentDto]]
            dtosValidation must be (jsSuccess)
            dtosValidation.get.foreach { _ must matchDtoToShipment(f.shipments.get(state).value) }
          }
        }
      }

      describe("list multiple shipments when filtered by states") {
        listMultipleShipments() { () =>
          val shipmentStates = List(Shipment.createdState, Shipment.unpackedState)
          val f = allShipmentsFixture
          f.shipments.values.foreach(shipmentRepository.put)
          val url = new Url(listUri.path +
                              s"""?filter=state:in:(${shipmentStates.mkString(",")})&sort=state""")
          val expectedShipments = List(f.shipments(Shipment.createdState),
                                       f.shipments(Shipment.unpackedState))
          (url, expectedShipments)
        }
      }

      it("fail when using an invalid filter string") {
        val invalidFilterString = "xxx"
        val reply = makeAuthRequest(GET, listUri + s"?filter=$invalidFilterString").value
        reply must beBadRequestWithMessage("could not parse filter expression:")
      }

      it("fail when using an invalid state filter") {
        val invalidStateName = nameGenerator.next[Shipment]
        val reply = makeAuthRequest(GET, listUri + s"?filter=state::$invalidStateName").value
        reply must beNotFoundWithMessage("shipment state does not exist:")
      }

      describe("list a single shipment when filtered by courier name") {
        listSingleShipment() { () =>
          val f = createdShipmentsFixture(2)
          f.shipmentMap.values.foreach(shipmentRepository.put)
          val shipment = f.shipmentMap.values.head
          val filter = s"courierName::${shipment.courierName}"
          (new Url(listUri.path + s"?filter=$filter"), shipment)
        }
      }

      describe("list a single shipment when using a 'like' filter on courier name") {
        listSingleShipment() { () =>
          val f = createdShipmentsFixture(2)
          val shipments = f.shipmentMap.values.toList
          val shipment = shipments(0).copy(courierName = "ABC")
          val filter = s"courierName:like:b"

          shipmentRepository.put(shipment)
          shipmentRepository.put(shipments(1).copy(courierName = "DEF"))

          (new Url(listUri.path + s"?filter=$filter"), shipment)
        }
      }

      describe("list multiple shipments when filtered by courier name") {
        listMultipleShipments() { () =>
          val numShipments = 2
          val f = createdShipmentsFixture(numShipments)
          val shipments = f.shipmentMap.values.toList
          val courierNames = shipments.map(_.courierName)

          shipments.foreach(shipmentRepository.put)
          val filter = s"""courierName:in:(${courierNames.mkString(",")})"""

          (new Url(listUri.path + s"?filter=$filter"), List(shipments(0), shipments(1)))
        }
      }

      describe("list a single shipment when filtered by tracking number") {
        listSingleShipment() { () =>
          val f = createdShipmentsFixture(2)
          f.shipmentMap.values.foreach(shipmentRepository.put)
          val shipment = f.shipmentMap.values.head
          val url = new Url(listUri.path + s"?filter=trackingNumber::${shipment.trackingNumber}")
          (url, shipment)
        }
      }

      describe("list a single shipment when filtered with a logical expression") {
        listSingleShipment() { () =>
          val f = createdShipmentsFixture(2)
          val shipments = f.shipmentMap.values.toList
          val shipment = shipments(0)
          val filter = s"""courierName::${shipment.courierName};trackingNumber::${shipment.trackingNumber}"""
          shipments.foreach(shipmentRepository.put)
          (new Url(listUri.path + s"?filter=$filter"), shipment)
        }
      }


      describe("list shipments sorted by courier name") {
        def commonSetup: List[Shipment] = {
          val f = centresFixture
          val shipments = List("FedEx", "UPS", "Canada Post")
            .map { name =>
              factory.createShipment(f.fromCentre, f.toCentre).copy(courierName = name)
            }
            .toList
          shipments.foreach(shipmentRepository.put)
          shipments
        }

        describe("sorted in ascending order") {
          listMultipleShipments() { () =>
            (new Url(listUri.path + s"?sort=courierName"),
             commonSetup.sortWith(_.courierName < _.courierName))
          }
        }

        describe("sorted in descending order") {
          listMultipleShipments() { () =>
            (new Url(listUri.path + s"?sort=-courierName"),
             commonSetup.sortWith(_.courierName > _.courierName))
          }
        }

      }

      describe("list shipments sorted by tracking number") {

        def commonSetup: List[Shipment] = {
          val f = centresFixture
          val shipments = List("TN2", "TN3", "TN1")
            .map { trackingNumber =>
              factory.createShipment(f.fromCentre, f.toCentre).copy(trackingNumber = trackingNumber)
            }
            .toList
          shipments.foreach(shipmentRepository.put)
          shipments
        }

        describe("sorted in ascending order") {
          listMultipleShipments() { () =>
            (new Url(listUri.path + s"?sort=trackingNumber"),
             commonSetup.sortWith(_.trackingNumber < _.trackingNumber))
          }
        }

        describe("sorted in descending order") {
          listMultipleShipments() { () =>
            (new Url(listUri.path + s"?sort=-trackingNumber"),
             commonSetup.sortWith(_.trackingNumber > _.trackingNumber))
          }
        }

      }

      describe("list a single shipment when using paged query") {

        def commonSetup = {
          val f = centresFixture
          val shipments = List("FedEx", "UPS", "Canada Post")
            .map { name =>
              factory.createShipment(f.fromCentre, f.toCentre).copy(courierName = name)
            }.toList
          shipments.foreach(shipmentRepository.put)
          shipments
        }

        describe("for first shipment") {
          listSingleShipment(maybeNext = Some(2)) { () =>
            val shipments = commonSetup
            (new Url(listUri.path + s"?sort=courierName&limit=1"), shipments(2))
          }
        }

        describe("for last shipment") {
          listSingleShipment(offset = 2, maybePrev = Some(2)) { () =>
            val shipments = commonSetup
            (new Url(listUri.path + s"?sort=courierName&page=3&limit=1"), shipments(1))
          }
        }
      }

      describe("list a single shipment when using a 'like' filter on tracking number") {
        listSingleShipment() { () =>
          val f = createdShipmentsFixture(2)
          val shipments = f.shipmentMap.values.toList
          val shipment = shipments(0).copy(trackingNumber = "ABC")
          val filter = s"trackingNumber:like:b"
          shipmentRepository.put(shipment)
          shipmentRepository.put(shipments(1).copy(trackingNumber = "DEF"))
          (new Url(listUri.path + s"?filter=$filter"), shipment)
        }
      }

      describe("fail when using an invalid query parameters") {
        pagedQueryShouldFailSharedBehaviour(() => listUri)
      }

    }

    describe("GET /api/shipments/:id") {

      it("get a shipment") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val reply = makeAuthRequest(GET, uri(f.shipment).path).value
        reply must beOkResponseWithJsonReply

        val dto = (contentAsJson(reply) \ "data").validate[ShipmentDto]
        dto must be (jsSuccess)
        dto.get must matchDtoToShipment(f.shipment)
      }

      it("returns an error for an invalid shipment ID") {
        val shipment = factory.createShipment
        val reply = makeAuthRequest(GET, uri(shipment).path).value
        reply must beNotFoundWithMessage("IdNotFound.*shipment")
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
        val reply = makeAuthRequest(POST, uri("").path, shipmentToAddJson(f.shipment)).value
        reply must beOkResponseWithJsonReply

        val shipmentId = (contentAsJson(reply) \ "data" \ "id").validate[ShipmentId]
        shipmentId must be (jsSuccess)
        val updatedShipment = f.shipment.copy(id = shipmentId.get)
        reply must matchUpdatedShipment(updatedShipment)
      }

      it("fail when adding a shipment with no courier name") {
        val shipment = createdShipmentFixture.shipment.copy(courierName = "")
        val reply = makeAuthRequest(POST, uri("").path, shipmentToAddJson(shipment)).value
        reply must beBadRequestWithMessage("CourierNameInvalid")
      }

      it("fail when adding a shipment with no tracking number") {
        val shipment = createdShipmentFixture.shipment.copy(trackingNumber = "")
        val reply = makeAuthRequest(POST, uri("").path, shipmentToAddJson(shipment)).value
        reply must beBadRequestWithMessage("TrackingNumberInvalid")
      }

      it("fail when adding a shipment with no FROM location id") {
        val shipment = createdShipmentFixture.shipment.copy(fromLocationId = LocationId(""))
        val reply = makeAuthRequest(POST, uri("").path, shipmentToAddJson(shipment)).value
        reply must beNotFoundWithMessage("EntityCriteriaError.*centre with location id")
      }

      it("fail when adding a shipment with no TO location id") {
        val shipment = createdShipmentFixture.shipment.copy(toLocationId = LocationId(""))
        val reply = makeAuthRequest(POST, uri("").path, shipmentToAddJson(shipment)).value
        reply must beNotFoundWithMessage("EntityCriteriaError.*centre with location id")
      }

    }


    describe("POST /api/shipments/courier/:id") {

      it("allow updating the courier name") {
        val f = createdShipmentFixture
        val newCourier = nameGenerator.next[Shipment]
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "courierName"     -> newCourier)
        val reply = makeAuthRequest(POST, uri(f.shipment, "courier").path, updateJson).value
        reply must beOkResponseWithJsonReply

        val updatedShipment = f.shipment.copy(version      = f.shipment.version + 1,
                                              courierName  = newCourier,
                                              timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedShipment(updatedShipment)
      }

      it("not allow updating the courier name to an empty string") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "courierName"     -> "")
        val reply = makeAuthRequest(POST, uri(f.shipment, "courier").path, updateJson).value
        reply must beBadRequestWithMessage("CourierNameInvalid")
      }

      it("must not allow updating the courier name on a shipment not in created state") {
        val f = allShipmentsFixture

        nonCreatedStates.foreach { state =>
          val shipment = f.shipments(state)
          shipmentRepository.put(shipment)
          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "courierName"     -> nameGenerator.next[String])

          val reply = makeAuthRequest(POST, uri(shipment, "courier").path, updateJson).value
          reply must beBadRequestWithMessage("InvalidState: shipment not created")
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
        val reply = makeAuthRequest(POST, uri(f.shipment, "trackingnumber").path, updateJson).value
        reply must beOkResponseWithJsonReply

        val updatedShipment = f.shipment.copy(version        = f.shipment.version + 1,
                                              trackingNumber = newTrackingNumber,
                                              timeModified   = Some(OffsetDateTime.now))
        reply must matchUpdatedShipment(updatedShipment)
      }

      it("not allow updating the tracking number to an empty string") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "trackingNumber"     -> "")
        val reply = makeAuthRequest(POST, uri(f.shipment, "trackingnumber").path, updateJson).value
        reply must beBadRequestWithMessage("TrackingNumberInvalid")
      }

      it("must not allow updating the tracking number on a shipment not in created state") {
        val f = allShipmentsFixture

        nonCreatedStates.foreach { state =>
          val shipment = f.shipments(state)
          shipmentRepository.put(shipment)
          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "trackingNumber"  -> nameGenerator.next[String])

          val reply = makeAuthRequest(POST, uri(shipment, "trackingnumber").path, updateJson).value
          reply must beBadRequestWithMessage("InvalidState: shipment not created")
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
        val reply = makeAuthRequest(POST, uri(f.shipment, "fromlocation").path, updateJson).value
        reply must beOkResponseWithJsonReply

        val updatedShipment = f.shipment.copy(version        = f.shipment.version + 1,
                                              fromCentreId   = centre.id,
                                              fromLocationId = newLocation.id,
                                              timeModified   = Some(OffsetDateTime.now))
        reply must matchUpdatedShipment(updatedShipment)
      }

      it("not allow updating the from location to an empty string") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "locationId"      -> "")
        val reply = makeAuthRequest(POST, uri(f.shipment, "fromlocation").path, updateJson).value
        reply must beNotFoundWithMessage("EntityCriteriaError.*centre with location id")
      }

      it("not allow updating the from location to an invalid id") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val badLocation = factory.createLocation

        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "locationId"      -> badLocation.id.id)
        val reply = makeAuthRequest(POST, uri(f.shipment, "fromlocation").path, updateJson).value
        reply must beNotFoundWithMessage("EntityCriteriaError.*centre with location id")
      }

      it("must not allow updating the from location on a shipment not in created state") {
        val f = allShipmentsFixture
        val badLocation = factory.createLocation

        nonCreatedStates.foreach { state =>
          val shipment = f.shipments(state)
          shipmentRepository.put(shipment)
          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "locationId"      -> badLocation.id.id)

          val reply = makeAuthRequest(POST, uri(shipment, "fromlocation").path, updateJson).value
          reply must beBadRequestWithMessage("InvalidState: shipment not created")
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
        val reply = makeAuthRequest(POST, uri(f.shipment, "tolocation").path, updateJson).value
        reply must beOkResponseWithJsonReply

        val updatedShipment = f.shipment.copy(version      = f.shipment.version + 1,
                                              toCentreId   = centre.id,
                                              toLocationId = newLocation.id,
                                              timeModified = Some(OffsetDateTime.now))
        reply must matchUpdatedShipment(updatedShipment)
      }

      it("not allow updating the TO location to an empty string") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)
        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "locationId"      -> "")
        val reply = makeAuthRequest(POST, uri(f.shipment, "tolocation").path, updateJson).value
        reply must beNotFoundWithMessage("EntityCriteriaError.*centre with location id")
      }

      it("not allow updating the TO location to an invalid id") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val badLocation = factory.createLocation

        val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                  "locationId"      -> badLocation.id.id)
        val reply = makeAuthRequest(POST, uri(f.shipment, "tolocation").path, updateJson).value
        reply must beNotFoundWithMessage("EntityCriteriaError.*centre with location id")
      }

      it("must not allow updating the TO location on a shipment not in created state") {
        val f = allShipmentsFixture
        val badLocation = factory.createLocation

        nonCreatedStates.foreach { state =>
          val shipment = f.shipments(state)
          shipmentRepository.put(shipment)
          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "locationId"      -> badLocation.id.id)

          val reply = makeAuthRequest(POST, uri(shipment, "tolocation").path, updateJson).value
          reply must beBadRequestWithMessage("InvalidState: shipment not created")
        }
      }

    }

    describe("POST /api/shipments/state/:id") {

      describe("for all states") {

        it("fail requests to update the state on a shipment that does not exist") {
          val f = createdShipmentFixture
          val time = OffsetDateTime.now.minusDays(10)

          forAll(states) { state =>
            info(s"for $state state")
            val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                      "datetime"        -> time)
            val reply = makeAuthRequest(POST, uri(f.shipment, s"state/$state").path, updateJson).value
            reply must beNotFoundWithMessage("IdNotFound.*shipment.*")
          }
        }

      }

      describe("for CREATED state") {

        describe("change to CREATED state from PACKED state") {
          changeStateSharedBehaviour { () =>
            val f = packedShipmentFixture
            val updatedShipment = f.shipment.created
            ChangeStateInfo(f.shipment, updatedShipment, Shipment.createdState, None)
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
            val reply = makeAuthRequest(POST, uri(shipment, s"state/created").path, updateJson).value
            reply must beBadRequestWithMessage("InvalidState: shipment is not packed")
          }
        }
      }

      describe("for PACKED state") {

        describe("change to PACKED state from CREATED") {
          changeStateSharedBehaviour { () =>
            val f = allShipmentsFixture
            val shipment = f.shipments(Shipment.createdState).asInstanceOf[CreatedShipment]
            addSpecimenToShipment(shipment, f.fromCentre)
            val stateChangeTime = OffsetDateTime.now.minusDays(10)
            val updatedShipment = shipment.pack(stateChangeTime)
            ChangeStateInfo(shipment, updatedShipment, Shipment.packedState, Some(stateChangeTime))
          }
        }

        describe("change to PACKED state from SENT") {
          changeStateSharedBehaviour { () =>
            val f = allShipmentsFixture
            val shipment = f.shipments(Shipment.sentState).asInstanceOf[SentShipment]
            addSpecimenToShipment(shipment, f.fromCentre)
            val stateChangeTime = OffsetDateTime.now.minusDays(10)
            val updatedShipment = shipment.backToPacked
            ChangeStateInfo(shipment, updatedShipment, Shipment.packedState, Some(stateChangeTime))
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
            val reply = makeAuthRequest(POST, uri(shipment, "state/packed").path, updateJson).value
            reply must beBadRequestWithMessage("InvalidState: shipment has no specimens")
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
            val reply = makeAuthRequest(POST, uri(shipment, "state/packed").path, updateJson).value
            reply must beBadRequestWithMessage("InvalidState: cannot change to packed state")
          }
        }
      }

      describe("for SENT state") {

        describe("change to SENT state from PACKED") {
          changeStateSharedBehaviour { () =>
            val f = allShipmentsFixture
            val shipment = f.shipments(Shipment.packedState).asInstanceOf[PackedShipment]
            addSpecimenToShipment(shipment, f.fromCentre)
            val stateChangeTime = OffsetDateTime.now.minusDays(10)
            val updatedShipment = shipment.send(stateChangeTime).toOption.value
            ChangeStateInfo(shipment, updatedShipment, Shipment.sentState, Some(stateChangeTime))
          }
        }

        describe("change to SENT state from RECEIVED") {
          changeStateSharedBehaviour { () =>
            val f = allShipmentsFixture
            val shipment = f.shipments(Shipment.receivedState).asInstanceOf[ReceivedShipment]
            addSpecimenToShipment(shipment, f.fromCentre)
            val stateChangeTime = OffsetDateTime.now.minusDays(10)
            val updatedShipment = shipment.backToSent
            ChangeStateInfo(shipment, updatedShipment, Shipment.sentState, Some(stateChangeTime))
          }
        }

        describe("change to SENT state from LOST") {
          changeStateSharedBehaviour { () =>
            val f = allShipmentsFixture
            val shipment = f.shipments(Shipment.lostState).asInstanceOf[LostShipment]
            addSpecimenToShipment(shipment, f.fromCentre)
            val stateChangeTime = OffsetDateTime.now.minusDays(10)
            val updatedShipment = shipment.backToSent
            ChangeStateInfo(shipment, updatedShipment, Shipment.sentState, Some(stateChangeTime))
          }
        }

        it("fail when updating state to SENT where time is less than packed time") {
          val f = packedShipmentFixture
          shipmentRepository.put(f.shipment)
          val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                    "datetime"        -> f.shipment.timePacked.get.minusDays(1))
          val reply = makeAuthRequest(POST, uri(f.shipment, "state/sent").path, updateJson).value
          reply must beBadRequestWithMessage("TimeSentBeforePacked")
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
            val reply = makeAuthRequest(POST, uri(shipment, s"state/sent").path, updateJson).value
            reply must beBadRequestWithMessage("InvalidState: cannot change to sent state")
          }
        }

      }

      describe("for RECEIVED state") {

        describe("change to RECEIVED state from SENT") {
          changeStateSharedBehaviour { () =>
            val f = allShipmentsFixture
            val shipment = f.shipments(Shipment.sentState).asInstanceOf[SentShipment]
            addSpecimenToShipment(shipment, f.fromCentre)
            val stateChangeTime = shipment.timeSent.get.plusDays(1)
            val updatedShipment = shipment.receive(stateChangeTime).toOption.value
            ChangeStateInfo(shipment, updatedShipment, Shipment.receivedState, Some(stateChangeTime))
          }
        }

        describe("change to RECEIVED state from UNPACKED") {
          changeStateSharedBehaviour { () =>
            val f = allShipmentsFixture
            val shipment = f.shipments(Shipment.unpackedState).asInstanceOf[UnpackedShipment]
            addSpecimenToShipment(shipment, f.fromCentre)
            val stateChangeTime = shipment.timeSent.get.plusDays(1)
            val updatedShipment = shipment.backToReceived
            ChangeStateInfo(shipment, updatedShipment, Shipment.receivedState, Some(stateChangeTime))
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
            val reply = makeAuthRequest(POST, uri(shipment, s"state/received").path, updateJson).value
            reply must beBadRequestWithMessage("InvalidState: cannot change to received state")
          }
        }

        it("fail for updating state to RECEIVED where time is less than sent time") {
          val f = sentShipmentFixture
          shipmentRepository.put(f.shipment)
          val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                    "datetime"        -> f.shipment.timeSent.get.minusDays(1))

          val reply = makeAuthRequest(POST, uri(f.shipment, "state/received").path, updateJson).value
          reply must beBadRequestWithMessage("TimeReceivedBeforeSent")
        }

        it("fail to change from UNPACKED to RECEIVED if some specimens are not in PRESENT state") {
          val f = specimensFixture(1)

          val shipment = makeUnpackedShipment(f.shipment)
          shipmentRepository.put(shipment)

          val specimen = f.specimens.head
          val shipmentSpecimen =
            factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                specimenId = specimen.id,
                                                state      = ShipmentItemState.Received)
          shipmentSpecimenRepository.put(shipmentSpecimen)

          val updateJson = Json.obj("expectedVersion" -> shipment.version,
                                    "datetime"        -> shipment.timeReceived)

          val reply = makeAuthRequest(POST, uri(f.shipment, "state/received").path, updateJson).value
          reply must beBadRequestWithMessage(
            "InvalidState: cannot change to received state, items have already been processed")
        }

        ignore("fail to change from UNPACKED to RECEIVED if some containers are not in PRESENT state") {
          fail("needs implementation")
        }
      }

      describe("for UNPACKED state") {

        describe("change to UNPACKED state from RECEIVED") {
          changeStateSharedBehaviour { () =>
            val f = allShipmentsFixture
            val shipment = f.shipments(Shipment.receivedState).asInstanceOf[ReceivedShipment]
            addSpecimenToShipment(shipment, f.fromCentre)
            val stateChangeTime = OffsetDateTime.now.plusDays(1)
            val updatedShipment = shipment.unpack(stateChangeTime).toOption.value
            ChangeStateInfo(shipment, updatedShipment, Shipment.unpackedState, Some(stateChangeTime))
          }
        }

        describe("change to UNPACKED state from COMPLETED") {
          changeStateSharedBehaviour { () =>
            val f = allShipmentsFixture
            val shipment = f.shipments(Shipment.completedState).asInstanceOf[CompletedShipment]
            addSpecimenToShipment(shipment, f.fromCentre)
            val stateChangeTime = OffsetDateTime.now.plusDays(1)
            val updatedShipment = shipment.backToUnpacked
            ChangeStateInfo(shipment, updatedShipment, Shipment.unpackedState, Some(stateChangeTime))
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
            val reply = makeAuthRequest(POST, uri(shipment, s"state/unpacked").path, updateJson).value
          reply must beBadRequestWithMessage("InvalidState: cannot change to unpacked state")
          }
        }

      }

      describe("for COMPLETED state") {

        describe("change to COMPLETED state from UNPACKED") {
          changeStateSharedBehaviour { () =>
            val f = allShipmentsFixture
            val shipment = f.shipments(Shipment.unpackedState).asInstanceOf[UnpackedShipment]
            val stateChangeTime = OffsetDateTime.now.plusDays(1)
            val updatedShipment = shipment.complete(stateChangeTime).toOption.value
            ChangeStateInfo(shipment, updatedShipment, Shipment.completedState, Some(stateChangeTime))
          }
        }

        it("must not change to COMPLETED state from UNPACKED if there are present specimens") {
          val f = unpackedShipmentFixture
          val timeCompleted = f.shipment.timeUnpacked.get.plusDays(1)

          shipmentRepository.put(f.shipment)
          addSpecimenToShipment(f.shipment, f.fromCentre)

          val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                    "datetime"        -> timeCompleted)
          val reply = makeAuthRequest(POST, uri(f.shipment, "state/completed").path, updateJson).value
          reply must beBadRequestWithMessage("InvalidState: shipment has specimens in present state")
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
            val reply = makeAuthRequest(POST, uri(shipment, s"state/completed").path, updateJson).value
            reply must beBadRequestWithMessage("InvalidState: cannot change to completed state")
          }
        }

      }

      describe("for LOST state") {

        describe("change to SENT state from LOST") {
          changeStateSharedBehaviour { () =>
            val f = allShipmentsFixture
            val shipment = f.shipments(Shipment.sentState).asInstanceOf[SentShipment]
            val stateChangeTime = OffsetDateTime.now.plusDays(1)
            val updatedShipment = shipment.lost
            ChangeStateInfo(shipment, updatedShipment, Shipment.lostState, Some(stateChangeTime))
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
            val reply = makeAuthRequest(POST, uri(shipment, s"state/lost").path, updateJson).value
            reply must beBadRequestWithMessage("InvalidState: cannot change to lost state")
          }
        }
      }

    }

    describe("POST /api/shipments/state/skip-to-sent/:id") {

      describe("switch from CREATED to SENT state") {
        skipStateSharedBehaviour { () =>
          val f = createdShipmentFixture
          val timePacked = OffsetDateTime.now.minusDays(10)
          val timeSent = timePacked.plusDays(1)
          val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                    "timePacked"      -> timePacked,
                                    "timeSent"        -> timeSent)
          val updatedShipment = f.shipment.skipToSent(timePacked, timeSent).toOption.value
          SkipStateInfo(f.shipment, updatedShipment, "state/skip-to-sent", updateJson)
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
          val reply = makeAuthRequest(POST, uri(shipment, "state/skip-to-sent").path, updateJson).value
          reply must beBadRequestWithMessage("InvalidState: shipment not created")
        }
      }

    }

    describe("POST /api/shipments/state/skip-to-unpacked/:id") {

      describe("switch from CREATED to SENT state") {
        skipStateSharedBehaviour { () =>
          val f = sentShipmentFixture
          val timeReceived = f.shipment.timeSent.fold { OffsetDateTime.now } { t => t.plusDays(1) }
          val timeUnpacked = timeReceived.plusDays(1)
          val updateJson = Json.obj("expectedVersion" -> f.shipment.version,
                                    "timeReceived"    -> timeReceived,
                                    "timeUnpacked"    -> timeUnpacked)
          val updatedShipment = f.shipment.skipToUnpacked(timeReceived, timeUnpacked).toOption.value
          SkipStateInfo(f.shipment, updatedShipment, "state/skip-to-unpacked", updateJson)
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
          val reply = makeAuthRequest(POST, uri(shipment, "state/skip-to-unpacked").path, updateJson).value
          reply must beBadRequestWithMessage("InvalidState: shipment not sent")
        }
      }

    }

    describe("DELETE /api/shipments/:id/:ver") {

      it("must delete a shipment in created state") {
        val f = createdShipmentFixture
        shipmentRepository.put(f.shipment)

        val reply = makeAuthRequest(DELETE, uri(f.shipment) + s"/${f.shipment.version}").value
        reply must beOkResponseWithJsonReply
        shipmentRepository.getByKey(f.shipment.id) mustFail "IdNotFound.*shipment id.*"
      }

      it("fail on attempt to delete a shipment not in the system") {
        val f = createdShipmentFixture
        val reply = makeAuthRequest(DELETE, uri(f.shipment) + s"/${f.shipment.version}").value
        reply must beNotFoundWithMessage("IdNotFound.*shipment id")
      }

      it("must not delete a shipment not in created state") {
        val f = allShipmentsFixture

        nonCreatedStates.foreach { state =>
          val shipment = f.shipments(state)
          shipmentRepository.put(shipment)
          val reply = makeAuthRequest(DELETE, uri(shipment) + s"/${shipment.version}").value
          reply must beBadRequestWithMessage("InvalidState: shipment not created")
        }
      }

      it("attempt to remove a shipment containing specimens fails") {
        val f = specimensFixture(1)
        val specimen = f.specimens.head
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = f.shipment.id,
                                                                   specimenId = specimen.id)
        shipmentSpecimenRepository.put(shipmentSpecimen)
        val reply = makeAuthRequest(DELETE, uri(f.shipment) + s"/${f.shipment.version}").value
        reply must beBadRequestWithMessage("shipment has specimens.*")
      }
    }
  }

  private def listSingleShipment(offset:    Long = 0,
                                 maybeNext: Option[Int] = None,
                                 maybePrev: Option[Int] = None)
                                (setupFunc: () => (Url, Shipment)) = {

    it("list single shipment") {
      val (url, expectedShipment) = setupFunc()
      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beSingleItemResults(offset, maybeNext, maybePrev)

      val dtosValidation = (json \ "data" \ "items").validate[List[ShipmentDto]]
      dtosValidation must be (jsSuccess)
      dtosValidation.get.foreach { _ must matchDtoToShipment(expectedShipment) }
    }
  }

  private def listMultipleShipments(offset:    Long = 0,
                                    maybeNext: Option[Int] = None,
                                    maybePrev: Option[Int] = None)
                                   (setupFunc: () => (Url, List[Shipment])) = {

    it("list multiple shipments") {
      val (url, expectedShipments) = setupFunc()

      val reply = makeAuthRequest(GET, url.path).value
      reply must beOkResponseWithJsonReply

      val json = contentAsJson(reply)
      json must beMultipleItemResults(offset = offset,
                                      total = expectedShipments.size.toLong,
                                      maybeNext = maybeNext,
                                      maybePrev = maybePrev)

      val dtosValidation = (json \ "data" \ "items").validate[List[ShipmentDto]]
      dtosValidation must be (jsSuccess)

      (dtosValidation.get zip expectedShipments).foreach { case (dto, shipment) =>
        dto must matchDtoToShipment(shipment)
      }
    }

  }

  def matchUpdatedShipment(shipment: Shipment) =
    new Matcher[Future[Result]] {
      def apply (left: Future[Result]) = {
        val replyDto = (contentAsJson(left) \ "data").validate[ShipmentDto]
        val jsSuccessMatcher = jsSuccess(replyDto)

        if (!jsSuccessMatcher.matches) {
          jsSuccessMatcher
        } else {
          val replyMatcher = matchDtoToShipment(shipment)(replyDto.get)

          if (!replyMatcher.matches) {
            MatchResult(false,
                        s"reply does not match expected: ${replyMatcher.failureMessage}",
                        s"reply matches expected: ${replyMatcher.failureMessage}")
          } else {
            matchRepositoryShipment(shipment)
          }
        }
      }
    }

  def matchRepositoryShipment =
    new Matcher[Shipment] {
      def apply (left: Shipment) = {
        shipmentRepository.getByKey(left.id).fold(
          err => {
            MatchResult(false, s"not found in repository: ${err.head}", "")

          },
          repoCet => {
            val repoMatcher = matchShipment(left)(repoCet)
            MatchResult(repoMatcher.matches,
                        s"repository shipment does not match expected: ${repoMatcher.failureMessage}",
                        s"repository shipment matches expected: ${repoMatcher.failureMessage}")
          }
        )
      }
    }

  private case class ChangeStateInfo(shipment:        Shipment,
                                     updatedShipment: Shipment,
                                     newState:        EntityState,
                                     timeMaybe:       Option[OffsetDateTime])

  private def changeStateSharedBehaviour(setupFunc: () => ChangeStateInfo) = {

    it("must change state") {
      val info = setupFunc()
      shipmentRepository.put(info.shipment)
      val baseJson = Json.obj("expectedVersion" -> info.shipment.version)
      val reqJson = info.timeMaybe.fold { baseJson } { time => baseJson ++ Json.obj("datetime" -> time) }
      val url = uri(info.shipment, s"state/${info.newState}").path
      val reply = makeAuthRequest(POST, url, reqJson).value
      reply must beOkResponseWithJsonReply
      reply must matchUpdatedShipment(info.updatedShipment)
    }
  }

  private case class SkipStateInfo(shipment:        Shipment,
                                   updatedShipment: Shipment,
                                   uriPath:         String,
                                   json:            JsValue)

  private def skipStateSharedBehaviour(setupFunc: () => SkipStateInfo) = {

    it("must change state") {
      val info = setupFunc()
      shipmentRepository.put(info.shipment)
      val url = uri(info.shipment, info.uriPath).path
      val reply = makeAuthRequest(POST, url, info.json).value
      reply must beOkResponseWithJsonReply
      reply must matchUpdatedShipment(info.updatedShipment)
    }
  }


}
