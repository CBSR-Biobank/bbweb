package org.biobank.domain

import play.api.libs.json._
import org.biobank.infrastructure.EnumUtils._

/** A standardized set of classifications that describe what a [[Specimen]] is.
  */
@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object SpecimenType extends Enumeration {
  type SpecimenType = Value

  val BuffyCoat                 = Value("Buffy coat")
  val CdpaPlasma                = Value("CDPA Plasma")
  val CentrifugedUrine          = Value("Centrifuged Urine")
  val CordBloodMononuclearCells = Value("Cord Blood Mononuclear Cells")
  val DnaBlood                  = Value("DNA (Blood)")
  val DnaWhiteBloodCells        = Value("DNA (White blood cells)")
  val DescendingColon           = Value("Descending Colon")
  val Duodenum                  = Value("Duodenum")
  val FilteredUrine             = Value("Filtered Urine")
  val FingerNails               = Value("Finger Nails")
  val Hair                      = Value("Hair")
  val Hemodialysate             = Value("Hemodialysate")
  val HeparinBlood              = Value("Heparin Blood")
  val Ileum                     = Value("Ileum")
  val Jejunum                   = Value("Jejunum")
  val LithiumHeparinPlasma      = Value("Lithium Heparin Plasma")
  val MeconiumBaby              = Value("Meconium - BABY")
  val Paxgene                   = Value("Paxgene")
  val PeritonealDialysate       = Value("Peritoneal Dialysate")
  val PlasmaNaHeparinDad        = Value("Plasma (Na Heparin) - DAD")
  val Plasma                    = Value("Plasma")
  val PlateletFreePlasma        = Value("Platelet free plasma")
  val Rna                       = Value("RNA")
  val RnaCBMC                   = Value("RNA CBMC")
  val RnaLaterBiopsies          = Value("RNAlater Biopsies")
  val Serum                     = Value("Serum")
  val SodiumAzideUrine          = Value("SodiumAzideUrine")
  val SourceWater               = Value("Source Water")
  val TapWater                  = Value("Tap Water")
  val TransverseColon           = Value("Transverse Colon")
  val WholeBloodEdta            = Value("Whole Blood EDTA")

  implicit val specimenTypeReads: Format[SpecimenType] = enumFormat(SpecimenType)
}
