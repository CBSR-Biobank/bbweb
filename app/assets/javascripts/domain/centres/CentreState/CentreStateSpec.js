/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import ngModule from '../../index'

describe('CentreState', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(CentreState) {
      this.CentreState = CentreState;
    });
  });

  it('should have values', function () {
    expect(Object.keys(this.CentreState)).not.toBeEmptyArray();
  });

});
