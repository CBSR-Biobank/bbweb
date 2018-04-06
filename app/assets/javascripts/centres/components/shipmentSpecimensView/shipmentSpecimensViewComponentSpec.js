/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ShippingComponentTestSuiteMixin } from 'test/mixins/ShippingComponentTestSuiteMixin';
import ngModule from '../../index'
import sharedBehaviour from 'test/behaviours/shipmentSpecimensControllerSharedBehaviour';

describe('shipmentSpecimensViewComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ShippingComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Shipment',
                              'Factory');
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
