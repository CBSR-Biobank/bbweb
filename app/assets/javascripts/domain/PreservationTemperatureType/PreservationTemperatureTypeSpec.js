/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';

describe('PreservationTemperatureType', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function (PreservationTemperatureType) {
      this.PreservationTemperatureType = PreservationTemperatureType;
    });
  });

  it('should have values', function () {
    expect(_.keys(this.PreservationTemperatureType)).not.toBeEmptyArray();
  });

});
