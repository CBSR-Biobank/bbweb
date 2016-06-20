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
object ShipmentItemState extends Enumeration {
  type ShipmentItemState = Value
  val Present  = Value("Present")
  val Received = Value("Receiving")
  val Missing  = Value("Missing")
  val Extra    = Value("Extra")

  implicit val shipmentItemStateFormat: Format[ShipmentItemState] =
    enumFormat(ShipmentItemState)

}
