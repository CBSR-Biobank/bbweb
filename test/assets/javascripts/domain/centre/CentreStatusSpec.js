/**
 * Jasmine test suite
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('CentreStatus', function() {

   beforeEach(mocks.module('biobankApp', 'biobank.test'));

    var CentreStatus;

    beforeEach(inject(function (_CentreStatus_) {
      CentreStatus = _CentreStatus_;
    }));

    it('should have expected functions', function() {
      expect(CentreStatus.DISABLED).toBeFunction();
      expect(CentreStatus.ENABLED).toBeFunction();
      expect(CentreStatus.values).toBeFunction();
    });

    it('should have correct number of values', function() {
      var values = CentreStatus.values();
      expect(values).toBeArrayOfSize(2);
    });

    it('should have correct values', function() {
      var values;

      expect(CentreStatus.DISABLED()).toBe('Disabled');
      expect(CentreStatus.ENABLED()).toBe('Enabled');

      values = CentreStatus.values();
      expect(values).toContain('Disabled');
      expect(values).toContain('Enabled');
    });

  });

});
