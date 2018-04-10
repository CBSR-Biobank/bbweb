package org.biobank.domain

import play.api.libs.json._
import org.biobank.infrastructure.EnumUtils._

/**
  * A standardized set of regions from a [[org.biobank.domain.participants.Participant Participant]] where a
  * [[org.biobank.domain.participants.Specimen Specimen]] is collected from.
  */
@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object AnatomicalSourceType extends Enumeration {
  type AnatomicalSourceType = Value

  val Blood: Value           = Value("Blood")
  val Brain: Value           = Value("Brain")
  val Colon: Value           = Value("Colon")
  val Kidney: Value          = Value("Kidney")
  val ColonAscending: Value  = Value("Ascending Colon")
  val ColonDescending: Value = Value("Descending Colon")
  val ColonTransverse: Value = Value("Transverse Colon")
  val Duodenum: Value        = Value("Duodenum")
  val Hair: Value            = Value("Hair")
  val Ileum: Value           = Value("Ileum")
  val Jejenum: Value         = Value("Jejenum")
  val StomachAntrum: Value   = Value("Stomach Antrum")
  val StomachBody: Value     = Value("Stomach Body")
  val Stool: Value           = Value("Stool")
  val ToeNails: Value        = Value("Toe Nails")
  val Urine: Value           = Value("Urine")

  implicit val anatomicalSourceTypeFormat: Format[AnatomicalSourceType] =
    enumFormat(org.biobank.domain.AnatomicalSourceType)
}
