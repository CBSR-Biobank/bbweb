/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  './enumSharedSpec',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, enumSharedSpec, testUtils) {
  'use strict';

  describe('AnatomicalSourceType', function() {

    var context = {},
        values = [
          'Blood',
          'Brain',
          'Colon',
          'Kidney',
          'Ascending Colon',
          'Descending Colon',
          'Transverse Colon',
          'Duodenum',
          'Hair',
          'Ileum',
          'Jejenum',
          'Stomach Antrum',
          'Stomach Body',
          'Stool',
          'Toe Nails',
          'Urine'
        ];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (AnatomicalSourceType) {
      context.enumerationClass = AnatomicalSourceType;
      context.valueMap = _.map(values, function (value) {
        return [ value, testUtils.camelCaseToUnderscore(value)];
      });
    }));

    enumSharedSpec(context);
  });

});
