/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  './enumSharedSpec',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, enumSharedSpec, testUtils) {
  'use strict';

  describe('AnnotationValueType', function() {

    var context = {},
        values = [
          'Text', 'Number', 'DateTime', 'Select'
        ];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (AnnotationValueType) {

      context.enumerationClass = AnnotationValueType;
      context.valueMap = _.map(values, function (value) {
        return [ value, testUtils.camelCaseToUnderscore(value)];
      });
    }));

    enumSharedSpec(context);
  });

});
