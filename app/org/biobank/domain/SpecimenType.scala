package org.biobank.domain

import play.api.libs.json._
import org.biobank.infrastructure.EnumUtils._

/** A standardized set of classifications that describe what a [[domain.participants.Specimen Specimen]] is.
  */
@SuppressWarnings(Array("org.wartremover.warts.Enumeration"))
object SpecimenType extends Enumeration {
  type SpecimenType = Value

  val BuffyCoat: Value                 = Value("Buffy coat")
  val CdpaPlasma: Value                = Value("CDPA Plasma")
  val CentrifugedUrine: Value          = Value("Centrifuged Urine")
  val CordBloodMononuclearCells: Value = Value("Cord Blood Mononuclear Cells")
  val DnaBlood: Value                  = Value("DNA (Blood)")
  val DnaWhiteBloodCells: Value        = Value("DNA (White blood cells)")
  val DescendingColon: Value           = Value("Descending Colon")
  val Duodenum: Value                  = Value("Duodenum")
  val FilteredUrine: Value             = Value("Filtered Urine")
  val FingerNails: Value               = Value("Finger Nails")
  val Hair: Value                      = Value("Hair")
  val Hemodialysate: Value             = Value("Hemodialysate")
  val HeparinBlood: Value              = Value("Heparin Blood")
  val Ileum: Value                     = Value("Ileum")
  val Jejunum: Value                   = Value("Jejunum")
  val LithiumHeparinPlasma: Value      = Value("Lithium Heparin Plasma")
  val MeconiumBaby: Value              = Value("Meconium - BABY")
  val NaN3Urine: Value                 = Value("NaN3 Urine")
  val Paxgene: Value                   = Value("Paxgene")
  val PeritonealDialysate: Value       = Value("Peritoneal Dialysate")
  val PlasmaNaHeparinDad: Value        = Value("Plasma (Na Heparin) - DAD")
  val Plasma: Value                    = Value("Plasma")
  val PlateletFreePlasma: Value        = Value("Platelet free plasma")
  val Rna: Value                       = Value("RNA")
  val RnaCBMC: Value                   = Value("RNA CBMC")
  val RnaLaterBiopsies: Value          = Value("RNAlater Biopsies")
  val Serum: Value                     = Value("Serum")
  val SodiumAzideUrine: Value          = Value("SodiumAzideUrine")
  val SourceWater: Value               = Value("Source Water")
  val Stool: Value                     = Value("Stool")
  val TapWater: Value                  = Value("Tap Water")
  val Urine: Value                     = Value("Urine")
  val TransverseColon: Value           = Value("Transverse Colon")
  val WholeBlood: Value                = Value("Whole Blood")
  val WholeBloodCpda: Value            = Value("Whole Blood CPDA")
  val WholeBloodEdta: Value            = Value("Whole Blood EDTA")
  val WholeBloodLiHep: Value           = Value("Whole Blood Li Hep")

  implicit val specimenTypeReads: Format[SpecimenType] = enumFormat(SpecimenType)
}
