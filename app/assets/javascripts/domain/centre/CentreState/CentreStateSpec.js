/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import ngModule from '../../index'

describe('CentreState', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(CentreState) {
      this.CentreState = CentreState;
    });
  });

  it('should have values', function () {
    expect(_.keys(this.CentreState)).not.toBeEmptyArray();
  });

});
