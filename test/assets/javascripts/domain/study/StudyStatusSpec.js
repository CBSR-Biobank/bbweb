/**
 * Jasmine test suite
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  '../enumSharedSpec',
  'biobank.testUtils',
  'biobankApp'
], function(angular, mocks, _, enumSharedSpec, testUtils) {
  'use strict';

  describe('StudyStatus', function() {

    var context = {},
        statuses = ['Disabled', 'Enabled', 'Retired'];

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (StudyStatus) {
      context.enumerationClass = StudyStatus;
      context.valueMap = _.map(statuses, function (value) {
        return [ value, testUtils.camelCaseToUnderscore(value)];
      });
    }));

    enumSharedSpec(context);
  });

});
