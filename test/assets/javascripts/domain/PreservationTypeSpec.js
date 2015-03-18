/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  './enumSharedSpec',
  'biobankApp'
], function(angular, mocks, enumSharedSpec) {
  'use strict';

  describe('PreservationType', function() {

    var context = {},
        valueMap = [
          [ 'Frozen Specimen', 'FROZEN_SPECIMEN' ],
          [ 'RNA Later',       'RNA_LATER'       ],
          [ 'Fresh Specimen',  'FRESH_SPECIMEN'  ],
          [ 'Slide',           'SLIDE'           ]
        ];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (PreservationType) {
      context.enumerationClass = PreservationType;
      context.valueMap = valueMap;
    }));

    enumSharedSpec(context);
  });

});
