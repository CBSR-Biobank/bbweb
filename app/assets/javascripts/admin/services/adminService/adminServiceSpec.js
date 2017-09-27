/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('adminService', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin.prototype);
      this.injectDependencies('adminService', '$httpBackend');
    });
  });

  it('gets the aggregate counts', function(done) {
    var counts = { studies: 1,
                   centres: 2,
                   users: 3
                 };

    this.$httpBackend.whenGET('/api/dtos/counts').respond({
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
