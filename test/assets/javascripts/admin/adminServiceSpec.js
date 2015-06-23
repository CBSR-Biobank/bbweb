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
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  describe('adminService', function() {
    var adminService, $httpBackend;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function() {
      adminService = this.$injector.get('adminService');
      $httpBackend = this.$injector.get('$httpBackend');
    }));

    it('gets the aggregate counts', function(done) {
      var counts = { studies: 1,
                     centres: 2,
                     users: 3
                   };

      $httpBackend.whenGET('/counts').respond({
        status: 'success',
        data: counts
      });

      adminService.aggregateCounts().then(function (reply) {
        expect(reply).toEqual(counts);
        done();
      });
      $httpBackend.flush();
    });

  });

});
