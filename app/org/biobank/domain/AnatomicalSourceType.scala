package org.biobank.domain

import play.api.libs.json._
import org.biobank.infrastructure.EnumUtils._

/**
  * A standardized set of regions from a [[org.biobank.domain.participant.Participant]] where a [[Specimen]]
  * is collected from.
  */
@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object AnatomicalSourceType extends Enumeration {
  type AnatomicalSourceType = Value

  val Blood           = Value("Blood")
  val Brain           = Value("Brain")
  val Colon           = Value("Colon")
  val Kidney          = Value("Kidney")
  val ColonAscending  = Value("Ascending Colon")
  val ColonDescending = Value("Descending Colon")
  val ColonTransverse = Value("Transverse Colon")
  val Duodenum        = Value("Duodenum")
  val Hair            = Value("Hair")
  val Ileum           = Value("Ileum")
  val Jejenum         = Value("Jejenum")
  val StomachAntrum   = Value("Stomach Antrum")
  val StomachBody     = Value("Stomach Body")
  val Stool           = Value("Stool")
  val ToeNails        = Value("Toe Nails")
  val Urine           = Value("Urine")

  implicit val anatomicalSourceTypeFormat: Format[AnatomicalSourceType] =
    enumFormat(org.biobank.domain.AnatomicalSourceType)
}
