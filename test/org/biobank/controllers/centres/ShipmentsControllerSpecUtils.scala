package org.biobank.controllers.centres

import java.time.OffsetDateTime
import org.biobank.domain.JsonHelper
import org.biobank.domain.centres._
import play.api.libs.json._

private[centres] trait ShipmentsControllerSpecUtils extends JsonHelper {
  import org.biobank.TestUtils._

  def uri(): String = "/api/shipments/"

  def uri(shipment: Shipment): String = uri + s"${shipment.id.id}"

  def uri(path: String): String = uri + s"$path"

  def uri(shipment: Shipment, path: String): String = uri(path) + s"/${shipment.id.id}"

  //------------ REMOVE THIS AFTER TESTING ---------------
  def listUri: String = uri("list")

  def compareObjs(jsonList: List[JsObject], shipmentsMap: Map[ShipmentId, Shipment]) = {
    jsonList.foreach { jsonObj =>
      val jsonId = ShipmentId((jsonObj \ "id").as[String])
      compareObj(jsonObj, shipmentsMap(jsonId))
    }
  }

  def compareTimestamps(shipment:     Shipment,
                        timePacked:   Option[OffsetDateTime],
                        timeSent:     Option[OffsetDateTime],
                        timeReceived: Option[OffsetDateTime],
                        timeUnpacked: Option[OffsetDateTime]): Unit = {
    checkOpionalTime(timePacked,   shipment.timePacked)
    checkOpionalTime(timeSent,     shipment.timeSent)
    checkOpionalTime(timeReceived, shipment.timeReceived)
    checkOpionalTime(timeUnpacked, shipment.timeUnpacked)
  }

  def compareTimestamps(shipment:      Shipment,
                        timePacked:    Option[OffsetDateTime],
                        timeSent:      Option[OffsetDateTime],
                        timeReceived:  Option[OffsetDateTime],
                        timeUnpacked:  Option[OffsetDateTime],
                        timeCompleted: Option[OffsetDateTime]): Unit = {
    checkOpionalTime(timePacked,    shipment.timePacked)
    checkOpionalTime(timeSent,      shipment.timeSent)
    checkOpionalTime(timeReceived,  shipment.timeReceived)
    checkOpionalTime(timeUnpacked,  shipment.timeUnpacked)
    checkOpionalTime(timeCompleted, shipment.timeCompleted)
  }

  def compareTimestamps(shipment1: Shipment, shipment2: Shipment): Unit = {
    compareTimestamps(shipment1,
                      shipment2.timePacked,
                      shipment2.timeSent,
                      shipment2.timeReceived,
                      shipment2.timeUnpacked)
  }

}
