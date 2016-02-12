/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  '../enumSharedSpec',
  'biobankApp'
], function(angular, mocks, _, enumSharedSpec) {
  'use strict';

  describe('StudyStatus', function() {

    var context = {};

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (StudyStatus) {
      context.enumerationClass = StudyStatus;
      context.valueMap = [
        [ 'DisabledStudy', 'DISABLED' ],
        [ 'EnabledStudy',  'ENABLED' ],
        [ 'RetiredStudy',  'RETIRED' ]
      ];
    }));

    enumSharedSpec(context);
  });

});
