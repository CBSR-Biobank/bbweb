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
                              'toastr',
                              'Shipment',
                              'ShipmentState',
                              'SHIPMENT_RECEIVE_PROGRESS_ITEMS',
                              'modalInput',
                              'notificationsService',
                              'modalService',
                              'shipmentSkipToUnpackedModalService',
                              'factory');
      testUtils.addCustomMatchers();

      this.createController = function (shipment) {
        ShippingComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<shipment-view-sent shipment="vm.shipment"></shipment-view-sent>',
          { shipment: shipment },
          'shipmentViewSent');
      };
    }));

    it('has valid scope', function() {
      var shipment = this.createShipment();
      this.createController(shipment);

      expect(this.controller.progressInfo).toBeDefined();
      expect(this.controller.progressInfo.items).toBeArrayOfSize(this.SHIPMENT_RECEIVE_PROGRESS_ITEMS.length);
      expect(this.controller.progressInfo.items).toContainAll(this.SHIPMENT_RECEIVE_PROGRESS_ITEMS);
      expect(this.controller.progressInfo.current).toBe(1);
    });

    describe('returning to packed state', function() {

      beforeEach(function() {
        this.shipment = this.createShipment();

        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
        spyOn(this.$state, 'reload').and.returnValue(null);

        this.createController(this.shipment);
      });

      it('user can return shipment to packed state', function() {
        spyOn(this.Shipment.prototype, 'pack').and.returnValue(this.$q.when(this.shipment));
        this.controller.returnToPackedState();
        this.scope.$digest();
        expect(this.$state.reload).toHaveBeenCalled();
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
        this.createController(this.shipment);
      });

      it('can receive the shipment', function() {
        var timeNow = new Date();

        timeNow.setSeconds(0);
        spyOn(this.Shipment.prototype, 'receive').and.returnValue(this.$q.when(this.shipment));
        spyOn(this.$state, 'reload').and.returnValue(null);

        this.controller.receiveShipment();
        this.scope.$digest();

        expect(this.Shipment.prototype.receive).toHaveBeenCalledWith(moment(timeNow).utc().format());
        expect(this.$state.reload).toHaveBeenCalled();
      });

      it('user is informed if shipment cannot be unpacked', function() {
        var self = this,
            errorMsgs = [
              'TimeReceivedBeforeSent',
              'simulated error'
            ],
            errorPromises = errorMsgs.map(function (errMsg) {
              return self.$q.reject({ message: errMsg });
            });

        spyOn(this.Shipment.prototype, 'receive').and.returnValues.apply(null, errorPromises);
        spyOn(this.toastr, 'error').and.returnValue(null);

        errorMsgs.forEach(function (errMsg, index) {
          var args;

          self.controller.receiveShipment();
          self.scope.$digest();
          expect(self.toastr.error.calls.count()).toBe(index + 1);

          if (errMsg === 'TimeReceivedBeforeSent') {
            args = self.toastr.error.calls.argsFor(index);
            expect(args[0]).toContain('The received time is before the sent time');
          }
        });
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
        this.createController(this.shipment);
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
        expect(this.$state.go).toHaveBeenCalledWith('home.shipping.shipment.unpack.info',
                                                    { shipmentId: this.shipment.id});
      });

      it('user is informed if shipment cannot be unpacked', function() {
        var self = this,
            errorMsgs = [
              'TimeReceivedBeforeSent',
              'TimeUnpackedBeforeReceived',
              'simulated error'
            ],
            errorPromises = errorMsgs.map(function (errMsg) {
              return self.$q.reject({ message: errMsg });
            });

        spyOn(this.Shipment.prototype, 'skipToStateUnpacked').and.returnValues.apply(null, errorPromises);
        spyOn(this.toastr, 'error').and.returnValue(null);

        errorMsgs.forEach(function (errMsg, index) {
          var args;
          self.controller.unpackShipment();
          self.scope.$digest();
          expect(self.toastr.error.calls.count()).toBe(index + 1);
          args = self.toastr.error.calls.argsFor(index);

          if (errMsg === 'TimeReceivedBeforeSent') {
            expect(args[0]).toContain('The received time is before the sent time');
          } else if (errMsg === 'TimeUnpackedBeforeReceived') {
            expect(args[0]).toContain('The unpacked time is before the received time');
          }
        });
      });

    });

    describe('when tagging as lost', function() {

      beforeEach(function() {
        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      });

      it('can tag a shipment as lost', function() {
        spyOn(this.Shipment.prototype, 'lost').and.returnValue(this.$q.when(this.shipment));
        spyOn(this.$state, 'reload').and.returnValue(null);

        this.shipment = this.createShipment({ state: this.ShipmentState.SENT });
        this.createController(this.shipment);
        this.controller.tagAsLost();
        this.scope.$digest();

        expect(this.$state.reload).toHaveBeenCalled();
      });

      it('user is informed if shipment cannot be tagged as lost', function() {
        var errorPromise = this.$q.reject('simulated error');
        spyOn(this.Shipment.prototype, 'lost').and.returnValue(errorPromise);
        spyOn(this.notificationsService, 'updateErrorAndReject').and.returnValue(errorPromise);

        this.shipment = this.createShipment({ state: this.ShipmentState.SENT });
        this.createController(this.shipment);
        this.controller.tagAsLost();
        this.scope.$digest();

        expect(this.notificationsService.updateErrorAndReject).toHaveBeenCalled();
      });
    });

  });

});
