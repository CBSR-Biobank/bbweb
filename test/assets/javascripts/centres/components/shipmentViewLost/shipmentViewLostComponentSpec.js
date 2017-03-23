/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('shipmentViewLostComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ShippingComponentTestSuiteMixin) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/shipmentViewLost/shipmentViewLost.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'modalService',
                              'Shipment',
                              'notificationsService',
                              'factory');

      this.createScope = function (shipment) {
        ShippingComponentTestSuiteMixin.prototype.createScope.call(
          this,
          '<shipment-view-lost shipment="vm.shipment"></shipment-view-lost>',
          { shipment: shipment },
          'shipmentViewLost');
      };
    }));

    it('has valid scope', function() {
      var shipment = this.createShipment();
      this.createScope(shipment);
      expect(this.controller.shipment).toBe(shipment);
    });

    it('user can return shipment to sent state', function() {
      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(this.Shipment.prototype, 'send').and.returnValue(this.$q.when(this.shipment));
      spyOn(this.$state, 'go').and.returnValue(null);

      this.shipment = this.createShipment();
      this.createScope(this.shipment);
      this.controller.returnToSentState();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.shipping.shipment',
                                                  { shipmentId: this.shipment.id },
                                                  { reload: true });
    });

  });

});
