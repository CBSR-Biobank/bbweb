/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks   = require('angularMocks'),
      moment  = require('moment'),
      _       = require('lodash');

  describe('shipmentViewReceivedComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ShippingComponentTestSuiteMixin, testUtils) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/shipmentViewReceived/shipmentViewReceived.html',
        '/assets/javascripts/common/components/progressTracker/progressTracker.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'stateHelper',
                              'Shipment',
                              'ShipmentState',
                              'SHIPMENT_RECEIVE_PROGRESS_ITEMS',
                              'modalInput',
                              'notificationsService',
                              'modalService',
                              'factory');
      testUtils.addCustomMatchers();
      this.createScope = function (shipment) {
        ShippingComponentTestSuiteMixin.prototype.createScope.call(
          this,
          '<shipment-view-received shipment="vm.shipment"></shipment-view-received>',
          { shipment: shipment },
          'shipmentViewReceived');
      };
    }));

    it('has valid scope', function() {
      var shipment = this.createShipment();
      this.createScope(shipment);

      expect(this.controller.progressInfo).toBeDefined();
      expect(this.controller.progressInfo.items).toBeArrayOfSize(this.SHIPMENT_RECEIVE_PROGRESS_ITEMS.length);
      expect(this.controller.progressInfo.items).toContainAll(this.SHIPMENT_RECEIVE_PROGRESS_ITEMS);
      expect(this.controller.progressInfo.current).toBe(2);
    });

    describe('when unpacking a shipment', function() {

      beforeEach(function() {
        spyOn(this.modalInput, 'dateTime').and.returnValue({ result: this.$q.when(new Date()) });
        this.shipment = this.createShipment();
        this.createScope(this.shipment);
      });

      it('can unpack the shipment', function() {
        var self = this,
            timeNow = new Date();

        timeNow.setSeconds(0);
        spyOn(this.Shipment.prototype, 'unpack').and.returnValue(this.$q.when(this.shipment));
        spyOn(this.$state, 'go').and.returnValue(null);

        this.controller.unpackShipment();
        this.scope.$digest();

        expect(this.Shipment.prototype.unpack).toHaveBeenCalledWith(moment(timeNow).utc().format());
        expect(self.$state.go).toHaveBeenCalledWith('home.shipping.shipment.unpack.info',
                                                    { shipmentId: this.shipment.id });
      });

      it('user is informed if shipment cannot be unpacked', function() {
        var error = this.$q.reject('simulated error');
        spyOn(this.Shipment.prototype, 'unpack').and.returnValue(error);
        spyOn(this.notificationsService, 'updateErrorAndReject').and.returnValue(error);

        this.controller.unpackShipment();
        this.scope.$digest();
        expect(this.notificationsService.updateErrorAndReject).toHaveBeenCalled();
      });

    });

    describe('returning to sent state', function() {

      beforeEach(function() {
        this.shipment = this.createShipment();

        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
        spyOn(this.stateHelper, 'reloadAndReinit').and.returnValue(null);

        this.createScope(this.shipment);
      });

      it('user can return shipment to sent state', function() {
        spyOn(this.Shipment.prototype, 'send').and.returnValue(this.$q.when(this.shipment));
        this.controller.returnToSentState();
        this.scope.$digest();
        expect(this.stateHelper.reloadAndReinit).toHaveBeenCalled();
      });

      it('user is informed if shipment cannot be returned to sent state', function() {
        var error = this.$q.reject('simulated error');
        spyOn(this.Shipment.prototype, 'send').and.returnValue(error);
        spyOn(this.notificationsService, 'updateErrorAndReject').and.returnValue(error);
        this.controller.returnToSentState();
        this.scope.$digest();
        expect(this.notificationsService.updateErrorAndReject).toHaveBeenCalled();
      });

    });

  });

});
