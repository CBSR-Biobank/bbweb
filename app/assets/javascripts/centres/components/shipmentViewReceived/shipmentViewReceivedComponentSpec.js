/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';
import moment from 'moment';

describe('shipmentViewReceivedComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ShippingComponentTestSuiteMixin, testUtils) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype);

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
                              'factory');
      testUtils.addCustomMatchers();
      this.createController = (shipment) =>
        ShippingComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<shipment-view-received shipment="vm.shipment"></shipment-view-received>',
          { shipment: shipment },
          'shipmentViewReceived');
    });
  });

  it('has valid scope', function() {
    var shipment = this.createShipment();
    this.createController(shipment);

    expect(this.controller.progressInfo).toBeDefined();
    expect(this.controller.progressInfo.items).toBeArrayOfSize(this.SHIPMENT_RECEIVE_PROGRESS_ITEMS.length);
    expect(this.controller.progressInfo.items).toContainAll(this.SHIPMENT_RECEIVE_PROGRESS_ITEMS);
    expect(this.controller.progressInfo.current).toBe(2);
  });

  describe('when unpacking a shipment', function() {

    beforeEach(function() {
      spyOn(this.modalInput, 'dateTime').and.returnValue({ result: this.$q.when(new Date()) });
      this.shipment = this.createShipment();
      this.createController(this.shipment);
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
      var self = this,
          errorMsgs = [
            'TimeUnpackedBeforeReceived',
            'simulated error'
          ];

      spyOn(this.toastr, 'error').and.returnValue(null);
      errorMsgs.forEach(function (errMsg, index) {
        var args;

        self.Shipment.prototype.unpack =
          jasmine.createSpy().and.returnValue(self.$q.reject(errMsg));

        self.controller.unpackShipment();
        self.scope.$digest();
        expect(self.toastr.error.calls.count()).toBe(index + 1);

        if (errMsg === 'TimeReceivedBeforeSent') {
          args = self.toastr.error.calls.argsFor(index);
          expect(args[0]).toContain('The unpacked time is before the received time');
        }
      });
    });

  });

  describe('returning to sent state', function() {

    beforeEach(function() {
      this.shipment = this.createShipment();

      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(this.$state, 'reload').and.returnValue(null);

      this.createController(this.shipment);
    });

    it('user can return shipment to sent state', function() {
      spyOn(this.Shipment.prototype, 'send').and.returnValue(this.$q.when(this.shipment));
      this.controller.returnToSentState();
      this.scope.$digest();
      expect(this.$state.reload).toHaveBeenCalled();
    });

    it('user is informed if shipment cannot be returned to sent state', function() {
      spyOn(this.Shipment.prototype, 'send').and.returnValue(this.$q.reject('simulated error'));
      spyOn(this.notificationsService, 'updateError').and.returnValue(null);
      this.controller.returnToSentState();
      this.scope.$digest();
      expect(this.notificationsService.updateError).toHaveBeenCalled();
    });

  });

});
