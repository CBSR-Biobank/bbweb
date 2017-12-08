package org.biobank.domain.access

import play.api.libs.json._
import org.biobank.infrastructure.EnumUtils._

@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object RoleId extends Enumeration {
  type RoleId = Value

  val WebsiteAdministrator: Value  = Value("Website Administrator")
  val UserAdministrator: Value     = Value("User Administrator")

  val SpecimenCollector: Value     = Value("Specimen Collector")
  val SpecimenProcessor: Value     = Value("Specimen Processor")

  val StudyAdministrator: Value    = Value("Study Administrator")
  val StudyUser: Value             = Value("Study User")

  val CentreAdministrator: Value   = Value("Centre Administrator")
  val CentreUser: Value            = Value("Centre User")

  val ShippingAdministrator: Value = Value("Shipping Administrator")
  val ShippingUser: Value          = Value("Shipping User")

  implicit val roleFormat: Format[RoleId] =
    enumFormat(org.biobank.domain.access.RoleId)
}
