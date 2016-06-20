package org.biobank.domain.centre

import org.biobank.domain.{ DomainFreeSpec, DomainValidation }
import org.biobank.domain.participants.SpecimenId
import org.biobank.fixture.NameGenerator
import com.github.nscala_time.time.Imports._
import org.slf4j.LoggerFactory
import scalaz.Scalaz._

class ShipmentSpecimenSpec extends DomainFreeSpec {

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

  "A shipment specimen" - {

    "can be created" - {

      "when valid arguments are used" in {
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

    "cannot be created" - {

      "with an invalid ID" in {
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(id = ShipmentSpecimenId(""))
        createFrom(shipmentSpecimen) mustFail "IdRequired"
      }

      "with an invalid version" in {
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(version = -2L)
        createFrom(shipmentSpecimen) mustFail "InvalidVersion"
      }

      "with an invalid shipment ID" in {
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(shipmentId = ShipmentId(""))
        createFrom(shipmentSpecimen) mustFail "ShipmentIdRequired"
      }

      "with an invalid specimen ID" in {
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(specimenId = SpecimenId(""))
        createFrom(shipmentSpecimen) mustFail "SpecimenIdRequired"
      }

      "with an invalid shipment container ID" in {
        val shipmentSpecimen = factory.createShipmentSpecimen.copy(
            shipmentContainerId = Some(ShipmentContainerId("")))
        createFrom(shipmentSpecimen) mustFail "ShipmentContainerIdInvalid"
      }

    }

  }

}
