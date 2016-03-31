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

  describe('CentreStatus', function() {

    var context = {};

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (CentreStatus) {
      this.CentreStatus = CentreStatus;

      context.enumerationClass = CentreStatus;
      context.valueMap = [
        [ 'DisabledCentre', 'DISABLED' ],
        [ 'EnabledCentre',  'ENABLED' ]
      ];
    }));

    it('throws error when getting label for invalid status', function() {
      var self = this;

      expect(function () {
        self.CentreStatus.label('xxx');
      }).toThrowError(/invalid status for centre/);
    });

    enumSharedSpec(context);
  });

});
