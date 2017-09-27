/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('adminService', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin.prototype);
      this.injectDependencies('adminService', '$httpBackend');
    }));

    it('gets the aggregate counts', function(done) {
      var counts = { studies: 1,
                     centres: 2,
                     users: 3
                   };

      this.$httpBackend.whenGET('/dtos/counts').respond({
        status: 'success',
        data: counts
      });

      this.adminService.aggregateCounts().then(function (reply) {
        expect(reply).toEqual(counts);
        done();
      });
      this.$httpBackend.flush();
    });

  });

});
