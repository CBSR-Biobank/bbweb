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
object ShipmentItemState extends Enumeration {
  type ShipmentItemState = Value
  val Present: Value  = Value("present")
  val Received: Value = Value("received")
  val Missing: Value  = Value("missing")
  val Extra: Value    = Value("extra")

  implicit val shipmentItemStateFormat: Format[ShipmentItemState] =
    enumFormat(ShipmentItemState)

}
