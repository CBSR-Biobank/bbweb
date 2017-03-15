/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks  = require('angularMocks'),
      moment = require('moment'),
      _      = require('lodash');

  describe('shipmentViewSentComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ShippingComponentTestSuiteMixin, testUtils) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/shipmentViewSent/shipmentViewSent.html',
        '/assets/javascripts/common/components/progressTracker/progressTracker.html',
        '/assets/javascripts/centres/services/shipmentSkipToUnpackedModal/shipmentSkipToUnpackedModal.html',
        '/assets/javascripts/common/components/dateTimePicker/dateTimePicker.html',
        '/assets/javascripts/common/modalInput/modalInput.html');

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
                              'shipmentSkipToUnpackedModalService',
                              'factory');
      testUtils.addCustomMatchers();

      this.createScope = function (shipment) {
        ShippingComponentTestSuiteMixin.prototype.createScope.call(
          this,
          '<shipment-view-sent shipment="vm.shipment"></shipment-view-sen>',
          { shipment: shipment },
          'shipmentViewSent');
      };
    }));

    it('has valid scope', function() {
      var shipment = this.createShipment();
      this.createScope(shipment);

      expect(this.controller.progressInfo).toBeDefined();
      expect(this.controller.progressInfo.items).toBeArrayOfSize(this.SHIPMENT_RECEIVE_PROGRESS_ITEMS.length);
      expect(this.controller.progressInfo.items).toContainAll(this.SHIPMENT_RECEIVE_PROGRESS_ITEMS);
      expect(this.controller.progressInfo.current).toBe(1);
    });

    describe('returning to packed state', function() {

      beforeEach(function() {
        this.shipment = this.createShipment();

        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
        spyOn(this.stateHelper, 'reloadAndReinit').and.returnValue(null);

        this.createScope(this.shipment);
      });

      it('user can return shipment to packed state', function() {
        spyOn(this.Shipment.prototype, 'pack').and.returnValue(this.$q.when(this.shipment));
        this.controller.returnToPackedState();
        this.scope.$digest();
        expect(this.stateHelper.reloadAndReinit).toHaveBeenCalled();
      });

      it('user is informed if shipment cannot be returned to packed state', function() {
        var error = this.$q.reject('simulated error');
        spyOn(this.Shipment.prototype, 'pack').and.returnValue(error);
        spyOn(this.notificationsService, 'updateErrorAndReject').and.returnValue(error);
        this.controller.returnToPackedState();
        this.scope.$digest();
        expect(this.notificationsService.updateErrorAndReject).toHaveBeenCalled();
      });

    });

    describe('when receiving a shipment', function() {

      beforeEach(function() {
        spyOn(this.modalInput, 'dateTime').and.returnValue({ result: this.$q.when(new Date()) });
        this.shipment = this.createShipment();
        this.createScope(this.shipment);
      });

      it('can receive the shipment', function() {
        var timeNow = new Date();

        timeNow.setSeconds(0);
        spyOn(this.Shipment.prototype, 'receive').and.returnValue(this.$q.when(this.shipment));
        spyOn(this.stateHelper, 'reloadAndReinit').and.returnValue(null);

        this.controller.receiveShipment();
        this.scope.$digest();

        expect(this.Shipment.prototype.receive).toHaveBeenCalledWith(moment(timeNow).utc().format());
        expect(this.stateHelper.reloadAndReinit).toHaveBeenCalled();
      });

      it('user is informed if shipment cannot be unpacked', function() {
        var error = this.$q.reject('simulated error');
        spyOn(this.Shipment.prototype, 'receive').and.returnValue(error);
        spyOn(this.notificationsService, 'updateErrorAndReject').and.returnValue(error);

        this.controller.receiveShipment();
        this.scope.$digest();
        expect(this.notificationsService.updateErrorAndReject).toHaveBeenCalled();
      });

    });

    describe('when unpacking a shipment', function() {

      beforeEach(function() {
        this.timeValue = new Date();
        this.timeValue.setSeconds(0);
        spyOn(this.shipmentSkipToUnpackedModalService, 'open').and.returnValue({
          result: this.$q.when({
            timeReceived: this.timeValue,
            timeUnpacked: this.timeValue
          })
        });
        this.shipment = this.createShipment();
        this.createScope(this.shipment);
      });

      it('can receive the shipment', function() {
        spyOn(this.Shipment.prototype, 'skipToStateUnpacked').and.returnValue(this.$q.when(this.shipment));
        spyOn(this.$state, 'go').and.returnValue(null);

        this.controller.unpackShipment();
        this.scope.$digest();

        expect(this.Shipment.prototype.skipToStateUnpacked)
          .toHaveBeenCalledWith(
            moment(this.timeValue).utc().format(),
            moment(this.timeValue).utc().format()
          );
        expect(this.$state.go).toHaveBeenCalledWith('home.shipping.unpack.info',
                                                    { shipmentId: this.shipment.id});
      });

      it('user is informed if shipment cannot be unpacked', function() {
        var error = this.$q.reject('simulated error');
        spyOn(this.Shipment.prototype, 'skipToStateUnpacked').and.returnValue(error);
        spyOn(this.notificationsService, 'updateErrorAndReject').and.returnValue(error);

        this.controller.unpackShipment();
        this.scope.$digest();
        expect(this.notificationsService.updateErrorAndReject).toHaveBeenCalled();
      });

    });
  });

});
