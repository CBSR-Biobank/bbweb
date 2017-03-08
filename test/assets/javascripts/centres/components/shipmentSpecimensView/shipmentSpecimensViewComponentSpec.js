/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var angular         = require('angular'),
      mocks           = require('angularMocks'),
      _               = require('lodash');

  describe('shipmentSpecimensViewComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ShippingComponentTestSuiteMixin) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/shipmentSpecimensView/shipmentSpecimensView.html',
        '/assets/javascripts/common/components/collapsablePanel/collapsablePanel.html',
        '/assets/javascripts/shipmentSpecimens/components/ssSpecimensPagedTable/ssSpecimensPagedTable.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Shipment',
                              'factory');
      this.createScope = function (shipment, readOnly) {
        ShippingComponentTestSuiteMixin.prototype.createScope.call(
          this,
          '<shipment-specimens-view shipment="vm.shipment" read-only="vm.readOnly"></shipment-specimens-view',
          {
            shipment: shipment,
            readOnly: readOnly
          },
          'shipmentSpecimensView');
      };
    }));

    it('has valid scope', function() {
      var shipment = this.createShipment(),
          readOnly = true;

      this.createScope(shipment, readOnly);

      expect(this.controller.shipment).toBe(shipment);
      expect(this.controller.readOnly).toBe(readOnly);
    });

  });

});
