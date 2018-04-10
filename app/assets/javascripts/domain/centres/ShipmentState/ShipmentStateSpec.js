/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import ngModule from '../../index'

describe('ShipmentState', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function (ShipmentState) {
      this.ShipmentState = ShipmentState;
    });
  });

  it('should have values', function () {
    expect(Object.keys(this.ShipmentState)).not.toBeEmptyArray();
  });

});
