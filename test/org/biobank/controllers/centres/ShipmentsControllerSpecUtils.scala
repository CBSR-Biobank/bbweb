package org.biobank.controllers.centres

import java.time.OffsetDateTime
import org.biobank.domain.JsonHelper
import org.biobank.domain.centres._
import org.biobank.fixtures.Url
import play.api.libs.json._

private[centres] trait ShipmentsControllerSpecUtils extends JsonHelper {
  import org.biobank.matchers.DateMatchers._

  protected def uri(paths: String*): Url = {
    val baseUri = "/api/shipments"
    val path = if (paths.isEmpty) baseUri
               else baseUri + "/" + paths.mkString("/")
    return new Url(path)
  }

  protected def uri(shipment: Shipment): Url = uri(shipment.id.id)

  protected def uri(shipment: Shipment, path: String): Url = uri(path, shipment.id.id)

  //------------ REMOVE THIS AFTER TESTING ---------------
  protected def listUri: Url = uri("list")

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
    shipment.timePacked must beOptionalTimeWithinSeconds(timePacked, 5L)
    shipment.timeSent must beOptionalTimeWithinSeconds(timeSent, 5L)
    shipment.timeReceived must beOptionalTimeWithinSeconds(timeReceived, 5L)
    shipment.timeUnpacked must beOptionalTimeWithinSeconds(timeUnpacked, 5L)
    ()
  }

  def compareTimestamps(shipment:      Shipment,
                        timePacked:    Option[OffsetDateTime],
                        timeSent:      Option[OffsetDateTime],
                        timeReceived:  Option[OffsetDateTime],
                        timeUnpacked:  Option[OffsetDateTime],
                        timeCompleted: Option[OffsetDateTime]): Unit = {
    shipment.timeCompleted must beOptionalTimeWithinSeconds(timeCompleted, 5L)
    compareTimestamps(shipment, timePacked, timeSent, timeReceived, timeUnpacked)
  }

  def compareTimestamps(shipment1: Shipment, shipment2: Shipment): Unit = {
    compareTimestamps(shipment1,
                      shipment2.timePacked,
                      shipment2.timeSent,
                      shipment2.timeReceived,
                      shipment2.timeUnpacked)
  }

}
