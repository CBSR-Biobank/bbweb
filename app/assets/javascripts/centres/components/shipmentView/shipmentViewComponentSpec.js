/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';

describe('shipmentViewComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ShippingComponentTestSuiteMixin) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Shipment',
                              'ShipmentState',
                              'factory');
      this.createController = function (shipment) {
        ShippingComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<shipment-view shipment="vm.shipment"></shipment-view>',
          { shipment: shipment },
          'shipmentView');
      };
    });
  });

  it('has valid header for shipment state', function() {
    var self = this,
        shipment,
        headerByState = {};

    headerByState[this.ShipmentState.CREATED]   = '';
    headerByState[this.ShipmentState.PACKED]    = 'Packed shipment';
    headerByState[this.ShipmentState.SENT]      = 'Sent shipment';
    headerByState[this.ShipmentState.RECEIVED]  = 'Received shipment';
    headerByState[this.ShipmentState.UNPACKED]  = '';
    headerByState[this.ShipmentState.COMPLETED] = 'Completed shipment';
    headerByState[this.ShipmentState.LOST]      = 'Lost shipment';

    _.keys(this.ShipmentState).forEach((key) => {
      var state = self.ShipmentState[key];
      shipment = self.createShipment(state);
      self.createController(shipment);
      expect(self.controller.pageHeader).toBe(headerByState[state]);
    });
  });

});
