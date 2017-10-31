/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('CentreCounts', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(TestSuiteMixin) {
      _.extend(this, TestSuiteMixin);
      this.injectDependencies('$httpBackend', 'CentreCounts');
    });
  });

  afterEach(function() {
    this.$httpBackend.verifyNoOutstandingExpectation();
    this.$httpBackend.verifyNoOutstandingRequest();
  });

  it('it should be created with defaults', function() {
    var counts = new this.CentreCounts();
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

    this.$httpBackend.whenGET(this.url('centres/counts')).respond({
      status: 'success',
      data: response
    });

    this.CentreCounts.get().then((counts) => {
      expect(counts).toEqual(jasmine.any(this.CentreCounts));
      expect(counts.total).toBe(response.total);
      expect(counts.disabled).toBe(response.disabledCount);
      expect(counts.enabled).toBe(response.enabledCount);
    });

    this.$httpBackend.flush();
  });

});
