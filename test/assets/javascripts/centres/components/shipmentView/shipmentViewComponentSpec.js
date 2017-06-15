/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('shipmentViewComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ShippingComponentTestSuiteMixin) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype);

      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/shipmentView/shipmentView.html',
        '/assets/javascripts/centres/components/shippingInfoView/shippingInfoView.html',
        '/assets/javascripts/centres/components/shipmentViewPacked/shipmentViewPacked.html',
        '/assets/javascripts/common/components/collapsiblePanel/collapsiblePanel.html',
        '/assets/javascripts/common/components/progressTracker/progressTracker.html',
        '/assets/javascripts/common/directives/statusLine/statusLine.html',
        '/assets/javascripts/centres/components/shipmentViewSent/shipmentViewSent.html',
        '/assets/javascripts/centres/components/shipmentViewReceived/shipmentViewReceived.html',
        '/assets/javascripts/centres/components/shipmentViewCompleted/shipmentViewCompleted.html',
        '/assets/javascripts/centres/components/shipmentViewLost/shipmentViewLost.html',
        '/assets/javascripts/common/components/breadcrumbs/breadcrumbs.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Shipment',
                              'ShipmentState',
                              'factory');
      this.createScope = function (shipment) {
        ShippingComponentTestSuiteMixin.prototype.createScope.call(
          this,
          '<shipment-view shipment="vm.shipment"></shipment-view>',
          { shipment: shipment },
          'shipmentView');
      };
    }));

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

      _.forEach(_.keys(this.ShipmentState), function (key) {
        var state = self.ShipmentState[key];
        shipment = self.createShipment(state);
        self.createScope(shipment);
        expect(self.controller.pageHeader).toBe(headerByState[state]);
      });
    });

  });

});
