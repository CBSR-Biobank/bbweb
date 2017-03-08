/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  /*eslint no-unused-vars: ["error", { "varsIgnorePattern": "angular" }]*/

  var angular         = require('angular'),
      mocks           = require('angularMocks'),
      _               = require('lodash');

  describe('unpackedShipmentInfoComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ShippingComponentTestSuiteMixin, ServerReplyMixin) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype, ServerReplyMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/shippingInfoView/shippingInfoView.html',
        '/assets/javascripts/common/components/collapsablePanel/collapsablePanel.html',
        '/assets/javascripts/common/directives/statusLine/statusLine.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'factory');

      this.createScope = function (shipment) {
        ShippingComponentTestSuiteMixin.prototype.createScope.call(
          this,
          '<unpacked-shipment-info shipment="vm.shipment"><unpacked-shipment-info>',
          { shipment: shipment },
          'unpackedShipmentInfo');
      };
    }));

    it('emits event when created', function() {
      var shipment = this.createShipment(),
          eventEmitted = false;

      this.$rootScope.$on('tabbed-page-update', function (event, arg) {
        expect(arg).toBe('tab-selected');
        eventEmitted = true;
      });

      this.createScope(shipment);
      expect(eventEmitted).toBeTrue();
    });

  });

});
