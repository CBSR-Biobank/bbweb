/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  //SpecimenType.$inject = [];

  /**
   *
   */
  function SpecimenType() {
    var ALL_VALUES = [
      BUFFY_COAT(),
      CDPA_PLASMA(),
      CENTRIFUGED_URINE(),
      CORD_BLOOD_MONONUCLEAR_CELLS(),
      DNA_BLOOD(),
      DNA_WHITE_BLOOD_CELLS(),
      DESCENDING_COLON(),
      DUODENUM(),
      FILTERED_URINE(),
      FINGER_NAILS(),
      HAIR(),
      HEMODIALYSATE(),
      HEPARIN_BLOOD(),
      ILEUM(),
      JEJUNUM(),
      LITHIUM_HEPARIN_PLASMA(),
      MECONIUM_BABY(),
      PAXGENE(),
      PERITONEAL_DIALYSATE(),
      PLASMA_NA_HEPARIN_DAD(),
      PLASMA(),
      PLATELET_FREE_PLASMA(),
      RNA(),
      RNA_CBMC(),
      RNA_LATER_BIOPSIES(),
      SERUM(),
      SODIUM_AZIDE_URINE(),
      SOURCE_WATER(),
      TAP_WATER(),
      TRANSVERSE_COLON(),
    ];

    var service = {
      BUFFY_COAT:                   BUFFY_COAT,
      CDPA_PLASMA:                  CDPA_PLASMA,
      CENTRIFUGED_URINE:            CENTRIFUGED_URINE,
      CORD_BLOOD_MONONUCLEAR_CELLS: CORD_BLOOD_MONONUCLEAR_CELLS,
      DNA_BLOOD:                    DNA_BLOOD,
      DNA_WHITE_BLOOD_CELLS:        DNA_WHITE_BLOOD_CELLS,
      DESCENDING_COLON:             DESCENDING_COLON,
      DUODENUM:                     DUODENUM,
      FILTERED_URINE:               FILTERED_URINE,
      FINGER_NAILS:                 FINGER_NAILS,
      HAIR:                         HAIR,
      HEMODIALYSATE:                HEMODIALYSATE,
      HEPARIN_BLOOD:                HEPARIN_BLOOD,
      ILEUM:                        ILEUM,
      JEJUNUM:                      JEJUNUM,
      LITHIUM_HEPARIN_PLASMA:       LITHIUM_HEPARIN_PLASMA,
      MECONIUM_BABY:                MECONIUM_BABY,
      PAXGENE:                      PAXGENE,
      PERITONEAL_DIALYSATE:         PERITONEAL_DIALYSATE,
      PLASMA_NA_HEPARIN_DAD:        PLASMA_NA_HEPARIN_DAD,
      PLASMA:                       PLASMA,
      PLATELET_FREE_PLASMA:         PLATELET_FREE_PLASMA,
      RNA:                          RNA,
      RNA_CBMC:                     RNA_CBMC,
      RNA_LATER_BIOPSIES:           RNA_LATER_BIOPSIES,
      SERUM:                        SERUM,
      SODIUM_AZIDE_URINE:           SODIUM_AZIDE_URINE,
      SOURCE_WATER:                 SOURCE_WATER,
      TAP_WATER:                    TAP_WATER,
      TRANSVERSE_COLON:             TRANSVERSE_COLON,
      values:                       values
    };
    return service;

    //-------

    function BUFFY_COAT()                   { return 'Buffy coat'; }
    function CDPA_PLASMA()                  { return 'CDPA Plasma'; }
    function CENTRIFUGED_URINE()            { return 'Centrifuged Urine'; }
    function CORD_BLOOD_MONONUCLEAR_CELLS() { return 'Cord Blood Mononuclear Cells'; }
    function DNA_BLOOD()                    { return 'DNA (Blood)'; }
    function DNA_WHITE_BLOOD_CELLS()        { return 'DNA (White blood cells)'; }
    function DESCENDING_COLON()             { return 'Descending Colon'; }
    function DUODENUM()                     { return 'Duodenum'; }
    function FILTERED_URINE()               { return 'Filtered Urine'; }
    function FINGER_NAILS()                 { return 'Finger Nails'; }
    function HAIR()                         { return 'Hair'; }
    function HEMODIALYSATE()                { return 'Hemodialysate'; }
    function HEPARIN_BLOOD()                { return 'Heparin Blood'; }
    function ILEUM()                        { return 'Ileum'; }
    function JEJUNUM()                      { return 'Jejunum'; }
    function LITHIUM_HEPARIN_PLASMA()       { return 'Lithium Heparin Plasma'; }
    function MECONIUM_BABY()                { return 'Meconium - BABY'; }
    function PAXGENE()                      { return 'Paxgene'; }
    function PERITONEAL_DIALYSATE()         { return 'Peritoneal Dialysate'; }
    function PLASMA_NA_HEPARIN_DAD()        { return 'Plasma (Na Heparin) - DAD'; }
    function PLASMA()                       { return 'Plasma'; }
    function PLATELET_FREE_PLASMA()         { return 'Platelet free plasma'; }
    function RNA()                          { return 'RNA'; }
    function RNA_CBMC()                     { return 'RNA CBMC'; }
    function RNA_LATER_BIOPSIES()           { return 'RNAlater Biopsies'; }
    function SERUM()                        { return 'Serum'; }
    function SODIUM_AZIDE_URINE()           { return 'SodiumAzideUrine'; }
    function SOURCE_WATER()                 { return 'Source Water'; }
    function TAP_WATER()                    { return 'Tap Water'; }
    function TRANSVERSE_COLON()             { return 'Transverse Colon'; }

    function values()                       { return ALL_VALUES; }

  }

  return SpecimenType;
});
