/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  './enumSharedSpec',
  'biobankApp'
], function(angular, mocks, enumSharedSpec) {
  'use strict';

  describe('SpecimenType', function() {

    var context = {},
        valueMap = [
          ['Buffy coat',                   'BUFFY_COAT'                   ],
          ['CDPA Plasma',                  'CDPA_PLASMA'                  ],
          ['Centrifuged Urine',            'CENTRIFUGED_URINE'            ],
          ['Cord Blood Mononuclear Cells', 'CORD_BLOOD_MONONUCLEAR_CELLS' ],
          ['DNA (Blood)',                  'DNA_BLOOD'                    ],
          ['DNA (White blood cells)',      'DNA_WHITE_BLOOD_CELLS'        ],
          ['Descending Colon',             'DESCENDING_COLON'             ],
          ['Duodenum',                     'DUODENUM'                     ],
          ['Filtered Urine',               'FILTERED_URINE'               ],
          ['Finger Nails',                 'FINGER_NAILS'                 ],
          ['Hair',                         'HAIR'                         ],
          ['Hemodialysate',                'HEMODIALYSATE'                ],
          ['Heparin Blood',                'HEPARIN_BLOOD'                ],
          ['Ileum',                        'ILEUM'                        ],
          ['Jejunum',                      'JEJUNUM'                      ],
          ['Lithium Heparin Plasma',       'LITHIUM_HEPARIN_PLASMA'       ],
          ['Meconium - BABY',              'MECONIUM_BABY'                ],
          ['Paxgene',                      'PAXGENE'                      ],
          ['Peritoneal Dialysate',         'PERITONEAL_DIALYSATE'         ],
          ['Plasma (Na Heparin) - DAD',    'PLASMA_NA_HEPARIN_DAD'        ],
          ['Plasma',                       'PLASMA'                       ],
          ['Platelet free plasma',         'PLATELET_FREE_PLASMA'         ],
          ['RNA',                          'RNA'                          ],
          ['RNA CBMC',                     'RNA_CBMC'                     ],
          ['RNAlater Biopsies',            'RNA_LATER_BIOPSIES'           ],
          ['Serum',                        'SERUM'                        ],
          ['SodiumAzideUrine',             'SODIUM_AZIDE_URINE'           ],
          ['Source Water',                 'SOURCE_WATER'                 ],
          ['Tap Water',                    'TAP_WATER'                    ],
          ['Transverse Colon',             'TRANSVERSE_COLON'             ]
        ];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (SpecimenType) {
      context.enumerationClass = SpecimenType;
      context.valueMap = valueMap;
    }));

    enumSharedSpec(context);
  });

});
