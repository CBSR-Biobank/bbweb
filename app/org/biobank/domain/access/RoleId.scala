package org.biobank.domain.access

import play.api.libs.json._
import org.biobank.infrastructure.EnumUtils._

/**
 * These will be used as IDs for Role access items.
 *
 * They should be usable as slugs.
 *
 */
@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object RoleId extends Enumeration {
  type RoleId = Value

  val WebsiteAdministrator: Value  = Value("website-administrator")
  val UserAdministrator: Value     = Value("user-administrator")

  val SpecimenCollector: Value     = Value("specimen-collector")
  val SpecimenProcessor: Value     = Value("specimen-processor")

  val StudyAdministrator: Value    = Value("study-administrator")
  val StudyUser: Value             = Value("study-user")

  val CentreAdministrator: Value   = Value("centre-administrator")
  val CentreUser: Value            = Value("centre-user")

  val ShippingAdministrator: Value = Value("shipping-administrator")
  val ShippingUser: Value          = Value("shipping-user")

  implicit val roleFormat: Format[RoleId] =
    enumFormat(org.biobank.domain.access.RoleId)
}
