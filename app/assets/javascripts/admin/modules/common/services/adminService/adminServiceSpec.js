/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { TestSuiteMixin } from 'test/mixins/TestSuiteMixin';
import ngModule from '../../index'

describe('adminService', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, TestSuiteMixin);
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
