/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('unpackedShipmentViewComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ShippingComponentTestSuiteMixin, ServerReplyMixin, testUtils) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype, ServerReplyMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/unpackedShipmentView/unpackedShipmentView.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Shipment',
                              'SHIPMENT_RECEIVE_PROGRESS_ITEMS',
                              'modalService',
                              'notificationsService',
                              'factory');

      this.createScope = function (shipment) {
        ShippingComponentTestSuiteMixin.prototype.createScope.call(
          this,
          '<unpacked-shipment-view shipment="vm.shipment"><unpacked-shipment-view>',
          { shipment: shipment },
          'unpackedShipmentView');
      };

      testUtils.addCustomMatchers();
    }));

    it('has valid scope', function() {
      var shipment = this.createShipment();
      this.createScope(shipment);

      expect(this.controller.active).toEqual(0);
      expect(this.controller.tabs).toBeNonEmptyArray();
      expect(this.controller.progressInfo).toBeDefined();
      expect(this.controller.progressInfo.items).toBeArrayOfSize(this.SHIPMENT_RECEIVE_PROGRESS_ITEMS.length);
      expect(this.controller.progressInfo.items).toContainAll(this.SHIPMENT_RECEIVE_PROGRESS_ITEMS);
      expect(this.controller.progressInfo.current).toBe(3);
    });

    it('has valid tabs', function() {
      var expectedHeadings = [
            'Information',
            'Unpack specimens',
            'Received specimens',
            'Missing specimens',
            'Extra specimens'
          ];

      this.createScope(this.createShipment());
      expect(this.controller.tabs[0].active).toBeTrue();
      expect(this.controller.tabs).toBeArrayOfSize(expectedHeadings.length);
      this.controller.tabs.forEach(function (tab, index) {
        expect(tab.heading).toBe(expectedHeadings[index]);
        expect(tab.active).toBe(index === 0);
        expect(tab.sref).toBeNonEmptyString();
      });
    });

    describe('returning to received state', function() {

      beforeEach(function() {
        this.shipment = this.createShipment();

        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
        spyOn(this.$state, 'go').and.returnValue(null);

        this.createScope(this.shipment);
      });

      it('user can return shipment to sent state', function() {
        this.injectDependencies('ShipmentSpecimen');

        spyOn(this.ShipmentSpecimen, 'list').and.returnValue(this.$q.when(this.factory.pagedResult([])));
        spyOn(this.Shipment.prototype, 'receive').and.returnValue(this.$q.when(this.shipment));

        this.controller.returnToReceivedState();
        this.scope.$digest();
        expect(this.$state.go).toHaveBeenCalledWith('home.shipping.shipment',
                                                    { shipmentId: this.shipment.id },
                                                    { reload: true });
      });

      it('user is informed if shipment cannot be returned to sent state', function() {
        var error = this.$q.reject('simulated error');

        this.injectDependencies('ShipmentSpecimen');
        spyOn(this.ShipmentSpecimen, 'list').and.returnValue(this.$q.when(this.factory.pagedResult([])));
        spyOn(this.Shipment.prototype, 'receive').and.returnValue(error);
        spyOn(this.notificationsService, 'updateErrorAndReject').and.returnValue(error);

        this.controller.returnToReceivedState();
        this.scope.$digest();
        expect(this.notificationsService.updateErrorAndReject).toHaveBeenCalled();
      });

    });

  });

});
