/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';
import sharedBehaviour from '../../../test/shipmentSpecimensControllerSharedBehaviour';

describe('shipmentSpecimensViewComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ShippingComponentTestSuiteMixin) {
      _.extend(this, ShippingComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Shipment',
                              'factory');
      this.createController = (shipment, readOnly) => {
        readOnly = readOnly || false;
        ShippingComponentTestSuiteMixin.createController.call(
          this,
          '<shipment-specimens-view shipment="vm.shipment" read-only="vm.readOnly"></shipment-specimens-view',
          {
            shipment: shipment,
            readOnly: readOnly
          },
          'shipmentSpecimensView');
      };
    });
  });

  it('has valid scope', function() {
    var shipment = this.createShipment(),
        readOnly = true;

    this.createController(shipment, readOnly);

    expect(this.controller.shipment).toBe(shipment);
    expect(this.controller.readOnly).toBe(readOnly);
  });

  describe('(shared)', function() {

    sharedBehaviour();

  });

});
