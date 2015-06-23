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

  describe('PreservationTemperatureType', function() {

    var context = {},
        valueMap = [
          [ '4 C',                 'PLUS_4_CELCIUS'    ],
          [ '-20 C',               'MINUS_20_CELCIUS'  ],
          [ '-80 C',               'MINUS_80_CELCIUS'  ],
          [ '-180 C',              'MINUS_180_CELCIUS' ],
          [ 'Room Temperature',    'ROOM_TEMPERATURE'  ]
        ];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (PreservationTemperatureType) {
      context.enumerationClass = PreservationTemperatureType;
      context.valueMap = valueMap;
    }));

    enumSharedSpec(context);
  });

});
