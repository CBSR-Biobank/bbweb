/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('shipmentUnpackComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ShippingComponentTestSuiteMixin, testUtils) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/shipmentUnpack/shipmentUnpack.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Shipment',
                              'ShipmentSpecimen',
                              'Specimen',
                              'SHIPMENT_RECEIVE_PROGRESS_ITEMS',
                              'modalService',
                              'factory');
      testUtils.addCustomMatchers();

      this.createScope = function (shipment) {
        ShippingComponentTestSuiteMixin.prototype.createScope.call(
          this,
          '<shipment-unpack shipment="vm.shipment"><shipment-unpack>',
          { shipment: shipment },
          'shipmentUnpack');
      };
    }));

    it('has valid scope', function() {
      var shipment = this.createShipment();
      this.createGetShipmentSpy(shipment);
      this.createScope(shipment);

      expect(this.controller.inventoryId).toBeEmptyString();
      expect(this.controller.actions).toBeArrayOfObjects();
      expect(this.controller.progressInfo).toBeDefined();
      expect(this.controller.progressInfo.items).toBeArrayOfSize(this.SHIPMENT_RECEIVE_PROGRESS_ITEMS.length);
      expect(this.controller.progressInfo.items).toContainAll(this.SHIPMENT_RECEIVE_PROGRESS_ITEMS);
      expect(this.controller.progressInfo.current).toBe(3);
    });

    it('returns shipment to received state', function() {
      var shipment = this.createShipment();
      shipment.timeReceived = new Date();

      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(this.Shipment.prototype, 'receive').and.returnValue(this.$q.when(shipment));
      spyOn(this.$state, 'go').and.returnValue(null);

      this.createGetShipmentSpy(shipment);
      this.createScope(shipment);

      this.controller.returnToReceivedState();
      this.scope.$digest();

      expect(this.$state.go).toHaveBeenCalledWith('home.shipping.shipment',
                                                  { shipmentId: shipment.id},
                                                  { reload: true });
    });

    describe('when retrieving shipment specimens', function() {

      beforeEach(function() {
        this.shipment = this.createShipment();
        this.shipmentSpecimen = new this.ShipmentSpecimen(this.factory.shipmentSpecimen());
        this.createShipmentSpecimensListSpy([ this.shipmentSpecimen ]);

        this.createGetShipmentSpy(this.shipment);
        this.createScope(this.shipment);
      });

      it('retrieves specimens by PRESENT state', function() {
        var self = this;

        this.controller.getPresentSpecimens().then(function (reply) {
          expect(reply).toBeObject();
          expect(reply.items).toBeArrayOfSize(1);
          expect(reply.items).toContain(self.shipmentSpecimen);
        });
        this.scope.$digest();
      });

      it('retrieves specimens by RECEIVED state', function() {
        var self = this;

        this.controller.getReceivedSpecimens().then(function (reply) {
          expect(reply).toBeObject();
          expect(reply.items).toBeArrayOfSize(1);
          expect(reply.items).toContain(self.shipmentSpecimen);
        });
        this.scope.$digest();
      });

    });

  });

});
