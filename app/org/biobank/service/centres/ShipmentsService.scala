package org.biobank.service.centres

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.ImplementedBy
import javax.inject.{Inject, Named}
import org.biobank.domain.DomainValidation
import org.biobank.domain.centre._
import org.biobank.infrastructure.command.ShipmentCommands._
import org.biobank.infrastructure.command.ShipmentSpecimenCommands._
import org.biobank.infrastructure.event.ShipmentEvents._
import org.biobank.infrastructure.event.ShipmentSpecimenEvents._
import org.biobank.infrastructure.{AscendingOrder, SortOrder}
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[ShipmentsServiceImpl])
trait ShipmentsService {

  def getShipments(courierFilter:        String,
                   trackingNumberFilter: String,
                   sortFunc:             (Shipment, Shipment) => Boolean,
                   order:                SortOrder): Seq[Shipment]

  def getShipment(id: String): DomainValidation[Shipment]

  def getShipmentSpecimen(id: String): DomainValidation[ShipmentSpecimen]

  def processCommand(cmd: ShipmentCommand): Future[DomainValidation[Shipment]]

  def processShipmentSpecimenCommand(cmd: ShipmentSpecimenCommand): Future[DomainValidation[ShipmentSpecimen]]

}

/**
 * Handles all commands dealing with shipments, shipment specimens, and shipment containers.
 */
class ShipmentsServiceImpl @Inject() (@Named("shipmentsProcessor") val processor: ActorRef,
                                      val centreRepository:            CentreRepository,
                                      val shipmentRepository:          ShipmentRepository,
                                      val shipmentSpecimenRepository:  ShipmentSpecimenRepository)
    extends ShipmentsService {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  def getShipments(courierFilter:        String,
                   trackingNumberFilter: String,
                   sortFunc:             (Shipment, Shipment) => Boolean,
                   order:                SortOrder)
      : Seq[Shipment] = {
    val allShipments = shipmentRepository.getValues

    val shipmentsFilteredByCourier = if (!courierFilter.isEmpty) {
        val courierLowerCase = courierFilter.toLowerCase
        allShipments.filter { _.courierName.toLowerCase.contains(courierLowerCase)}
      } else {
        allShipments
      }

    val shipmentsFilteredByTrackingNumber = if (!trackingNumberFilter.isEmpty) {
        val trackingNumLowerCase = trackingNumberFilter.toLowerCase
        shipmentsFilteredByCourier.filter { _.trackingNumber.toLowerCase.contains(trackingNumLowerCase)}
      } else {
        shipmentsFilteredByCourier
      }

    val result = shipmentsFilteredByTrackingNumber.toSeq.sortWith(sortFunc)

    if (order == AscendingOrder) result
    else result.reverse
  }

  def getShipment(id: String): DomainValidation[Shipment] = {
    shipmentRepository.getByKey(ShipmentId(id))
  }

  def getShipmentSpecimen(id: String): DomainValidation[ShipmentSpecimen] = {
    shipmentSpecimenRepository.getByKey(ShipmentSpecimenId(id))
  }

  def processCommand(cmd: ShipmentCommand): Future[DomainValidation[Shipment]] = {
    ask(processor, cmd).mapTo[DomainValidation[ShipmentEvent]].map { validation =>
      for {
        event    <- validation
        shipment <- shipmentRepository.getByKey(ShipmentId(event.id))
      } yield shipment
    }
  }

  def processShipmentSpecimenCommand(cmd: ShipmentSpecimenCommand)
      : Future[DomainValidation[ShipmentSpecimen]] = {
    ask(processor, cmd).mapTo[DomainValidation[ShipmentSpecimenEvent]].map { validation =>
      for {
        event            <- validation
        shipmentSpecimen <- shipmentSpecimenRepository.getByKey(ShipmentSpecimenId(event.id))
      } yield shipmentSpecimen
    }
  }
}
