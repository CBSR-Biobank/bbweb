package org.biobank.domain.centre

import org.biobank.infrastructure.EnumUtils._
import play.api.libs.json._

/**
 * The values from this enumerations are saved in the database. Therefore, DO NOT CHANGE THESE ENUM IDS
 * (unless you are prepared to write an upgrade script). However, order and enum name can be modified freely.
 *
 * <p>Also, values in this enumeration should probably never be deleted, unless they are not used in
 * <em>any</em> database. Instead, they should be deprecated and probably always return false when checking
 * allow-ability.
 */
@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object ShipmentState extends Enumeration {
  type ShipmentState = Value
  val Created  = Value("created")
  val Packed   = Value("packed")
  val Sent     = Value("sent")
  val Received = Value("received")
  val Unpacked = Value("unpacked")
  val Lost     = Value("lost")

  implicit val shipmentStateFormat: Format[ShipmentState] = enumFormat(ShipmentState)

}
