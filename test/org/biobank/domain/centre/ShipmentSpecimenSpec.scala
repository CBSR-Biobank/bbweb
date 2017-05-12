package org.biobank.domain.centre

import org.biobank.domain.{ DomainSpec, DomainValidation }
import org.biobank.domain.participants.SpecimenId
import org.biobank.fixture.NameGenerator
import com.github.nscala_time.time.Imports._
import org.slf4j.LoggerFactory
import scalaz.Scalaz._

class ShipmentSpecimenSpec extends DomainSpec {

  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  def createFrom(shipmentSpecimen: ShipmentSpecimen): DomainValidation[ShipmentSpecimen] =
    ShipmentSpecimen.create(id                  = shipmentSpecimen.id,
                            version             = shipmentSpecimen.version,
                            shipmentId          = shipmentSpecimen.shipmentId,
                            specimenId          = shipmentSpecimen.specimenId,
                            state               = shipmentSpecimen.state,
                            shipmentContainerId = shipmentSpecimen.shipmentContainerId)

  describe("A shipment specimen") {

    describe("can be created") {

      it("when valid arguments are used") {
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(version = 0L)
        createFrom(shipmentSpecimen).mustSucceed { s =>
          s must have (
            'id                  (shipmentSpecimen.id),
            'version             (0L),
            'shipmentId          (shipmentSpecimen.shipmentId),
            'specimenId          (shipmentSpecimen.specimenId),
            'state               (shipmentSpecimen.state),
            'shipmentContainerId (shipmentSpecimen.shipmentContainerId)
          )

          checkTimeStamps(s, DateTime.now, None)
        }
      }

    }

    describe("cannot be created") {

      it("with an invalid ID") {
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(id = ShipmentSpecimenId(""))
        createFrom(shipmentSpecimen) mustFail "IdRequired"
      }

      it("with an invalid version") {
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(version = -2L)
        createFrom(shipmentSpecimen) mustFail "InvalidVersion"
      }

      it("with an invalid shipment ID") {
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = ShipmentId(""))
        createFrom(shipmentSpecimen) mustFail "ShipmentIdRequired"
      }

      it("with an invalid specimen ID") {
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(specimenId = SpecimenId(""))
        createFrom(shipmentSpecimen) mustFail "SpecimenIdRequired"
      }

      it("with an invalid shipment container ID") {
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(
            shipmentContainerId = Some(ShipmentContainerId("")))
        createFrom(shipmentSpecimen) mustFail "ShipmentContainerIdInvalid"
      }

    }

  }

}
