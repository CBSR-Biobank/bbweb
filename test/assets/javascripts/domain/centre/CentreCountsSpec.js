/**
 * Jasmine test suite
 */
define(['angular', 'angularMocks', 'underscore', 'biobankApp'], function(angular, mocks, _) {
  'use strict';

  describe('CentreCounts', function() {

    var CentreCounts, httpBackend;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($httpBackend, _CentreCounts_) {
      httpBackend = $httpBackend;
      CentreCounts = _CentreCounts_;
    }));

    it('it should be created with defaults', function() {
      var counts = new CentreCounts();
      expect(counts.total).toBe(0);
      expect(counts.disabled).toBe(0);
      expect(counts.enabled).toBe(0);
    });

    it('loading from server have valid  values', function() {
      var response = {
        total:         3,
        disabledCount: 2,
        enabledCount:  1
      };

      httpBackend.whenGET('/centres/counts').respond({
        status: 'success',
        data: response
      });

      CentreCounts.get().then(function(counts) {
        expect(counts).toEqual(jasmine.any(CentreCounts));
        expect(counts.total).toBe(response.total);
        expect(counts.disabled).toBe(response.disabledCount);
        expect(counts.enabled).toBe(response.enabledCount);
      });

      httpBackend.flush();
    });


  });

});
