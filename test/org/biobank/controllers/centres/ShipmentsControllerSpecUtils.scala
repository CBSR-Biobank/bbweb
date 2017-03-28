package org.biobank.controllers.centres

import com.github.nscala_time.time.Imports._
import org.biobank.domain.JsonHelper
import org.biobank.domain.centre._
import org.joda.time.DateTime
import play.api.libs.json._

private[centres] trait ShipmentsControllerSpecUtils extends JsonHelper {
  import org.biobank.TestUtils._

  def uri(): String = "/shipments/"

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
                        timePacked:   Option[DateTime],
                        timeSent:     Option[DateTime],
                        timeReceived: Option[DateTime],
                        timeUnpacked: Option[DateTime]): Unit = {
    checkOpionalTime(timePacked,   shipment.timePacked)
    checkOpionalTime(timeSent,     shipment.timeSent)
    checkOpionalTime(timeReceived, shipment.timeReceived)
    checkOpionalTime(timeUnpacked, shipment.timeUnpacked)
  }

  def compareTimestamps(shipment:      Shipment,
                        timePacked:    Option[DateTime],
                        timeSent:      Option[DateTime],
                        timeReceived:  Option[DateTime],
                        timeUnpacked:  Option[DateTime],
                        timeCompleted: Option[DateTime]): Unit = {
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
