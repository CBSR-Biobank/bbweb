/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      mocks   = require('angularMocks'),
      moment  = require('moment'),
      _       = require('lodash');

  describe('shipmentViewPackedComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ShippingComponentTestSuiteMixin, testUtils) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/shipmentViewPacked/shipmentViewPacked.html',
        '/assets/javascripts/common/components/progressTracker/progressTracker.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Shipment',
                              'SHIPMENT_SEND_PROGRESS_ITEMS',
                              'modalInput',
                              'notificationsService',
                              'modalService',
                              'factory');
      testUtils.addCustomMatchers();
      this.createScope = function (shipment) {
        ShippingComponentTestSuiteMixin.prototype.createScope.call(
          this,
          '<shipment-view-packed shipment="vm.shipment"></shipment-view-packed>',
          { shipment: shipment },
          'shipmentViewPacked');
      };
    }));

    it('has valid scope', function() {
      var shipment = this.createShipment();
      this.createScope(shipment);

      expect(this.controller.progressInfo).toBeDefined();
      expect(this.controller.progressInfo.items).toBeArrayOfSize(this.SHIPMENT_SEND_PROGRESS_ITEMS.length);
      expect(this.controller.progressInfo.items).toContainAll(this.SHIPMENT_SEND_PROGRESS_ITEMS);
      expect(this.controller.progressInfo.current).toBe(3);
    });

    describe('when sending a shipment', function() {

      beforeEach(function() {
        spyOn(this.modalInput, 'dateTime').and.returnValue({ result: this.$q.when(new Date()) });
        this.shipment = this.createShipment();
        this.createScope(this.shipment);
      });

      it('can send a shipment', function() {
        var self = this,
            timeNow = new Date();

        timeNow.setSeconds(0);
        spyOn(this.Shipment.prototype, 'send').and.returnValue(this.$q.when(this.shipment));
        spyOn(this.$state, 'go').and.returnValue(null);

        this.controller.sendShipment();
        this.scope.$digest();

        expect(this.Shipment.prototype.send).toHaveBeenCalledWith(moment(timeNow).utc().format());
        expect(self.$state.go).toHaveBeenCalledWith('home.shipping');
      });

      it('user is informed if shipment cannot be sent', function() {
        spyOn(this.Shipment.prototype, 'send').and.returnValue(this.$q.reject('simulated error'));
        spyOn(this.notificationsService, 'updateErrorAndReject').and.returnValue(null);

        this.controller.sendShipment();
        this.scope.$digest();
        expect(this.notificationsService.updateErrorAndReject).toHaveBeenCalled();
      });

    });

    describe('adding more items', function() {

      beforeEach(function() {
        this.shipment = this.createShipment();

        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
        spyOn(this.$state, 'go').and.returnValue(null);

        this.createScope(this.shipment);
      });

      it('user can add more items to the shipment', function() {
        spyOn(this.Shipment.prototype, 'created').and.returnValue(this.$q.when(this.shipment));
        this.controller.addMoreItems();
        this.scope.$digest();
        expect(this.$state.go).toHaveBeenCalledWith('home.shipping.addItems',
                                                    { shipmentId: this.shipment.id});
      });

      it('user is informed if more items cannot be added to the shipment', function() {
        spyOn(this.Shipment.prototype, 'created').and.returnValue(this.$q.reject('simulated error'));
        spyOn(this.notificationsService, 'updateErrorAndReject').and.returnValue(null);
        this.controller.addMoreItems();
        this.scope.$digest();
        expect(this.notificationsService.updateErrorAndReject).toHaveBeenCalled();
      });

    });

  });

});
