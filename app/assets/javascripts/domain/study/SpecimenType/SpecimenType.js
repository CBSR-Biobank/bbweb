/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * A standardized set of classifications that describe <em>what</em> a {@link domain.participant.Specimen
   * Specimen} is. Potential examples include: urine, whole blood, plasma, nail, protein, etc.
   *
   * @enum {string}
   * @memberOf domain.studies
   */
  var SpecimenType = {
    BUFFY_COAT:                   'Buffy coat',
    CDPA_PLASMA:                  'CDPA Plasma',
    CENTRIFUGED_URINE:            'Centrifuged Urine',
    CORD_BLOOD_MONONUCLEAR_CELLS: 'Cord Blood Mononuclear Cells',
    DNA_BLOOD:                    'DNA (Blood)',
    DNA_WHITE_BLOOD_CELLS:        'DNA (White blood cells)',
    DESCENDING_COLON:             'Descending Colon',
    DUODENUM:                     'Duodenum',
    FILTERED_URINE:               'Filtered Urine',
    FINGER_NAILS:                 'Finger Nails',
    HAIR:                         'Hair',
    HEMODIALYSATE:                'Hemodialysate',
    HEPARIN_BLOOD:                'Heparin Blood',
    ILEUM:                        'Ileum',
    JEJUNUM:                      'Jejunum',
    LITHIUM_HEPARIN_PLASMA:       'Lithium Heparin Plasma',
    MECONIUM_BABY:                'Meconium - BABY',
    NAN3_URINE:                   'NaN3 Urine',
    PAXGENE:                      'Paxgene' ,
    PERITONEAL_DIALYSATE:         'Peritoneal Dialysate',
    PLASMA_NA_HEPARIN_DAD:        'Plasma (Na Heparin) - DAD',
    PLASMA:                       'Plasma',
    PLATELET_FREE_PLASMA:         'Platelet free plasma',
    RNA:                          'RNA',
    RNA_CBMC:                     'RNA CBMC',
    RNA_LATER_BIOPSIES:           'RNAlater Biopsies',
    SERUM:                        'Serum',
    SODIUM_AZIDE_URINE:           'SodiumAzideUrine',
    SOURCE_WATER:                 'Source Water',
    STOOL:                        'Stool',
    TAP_WATER:                    'Tap Water',
    TRANSVERSE_COLON:             'Transverse Colon',
    URINE:                        'Urine',
    WHOLE_BLOOD:                  'Whole Blood',
    WHOLE_BLOOD_CPDA:             'Whole Blood CPDA',
    WHOLE_BLOOD_EDTA:             'Whole Blood EDTA',
    WHOLE_BLOOD_LI_HEP:           'Whole Blood Li Hep'
  };

  return SpecimenType;
});
