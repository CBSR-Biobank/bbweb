/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ShippingComponentTestSuiteMixin } from 'test/mixins/ShippingComponentTestSuiteMixin';
import moment from 'moment';
import ngModule from '../../index'

describe('shipmentViewPackedComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ShippingComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'toastr',
                              'Shipment',
                              'shipmentSendTasksService',
                              'modalInput',
                              'notificationsService',
                              'modalService',
                              'Factory');
      this.addCustomMatchers();
      this.createController = (shipment) =>
        this.createControllerInternal(
          '<shipment-view-packed shipment="vm.shipment"></shipment-view-packed>',
          { shipment: shipment },
          'shipmentViewPacked');
    });
  });

  it('has valid scope', function() {
    var shipment = this.createShipment();
    this.createController(shipment);

    const taskData = this.shipmentSendTasksService.getTaskData();

    expect(this.controller.progressInfo).toBeDefined();
    expect(this.controller.progressInfo).toBeArrayOfSize(Object.keys(taskData).length);
    taskData.forEach((taskInfo) => {
      taskInfo.status = true;
      expect(this.controller.progressInfo).toContain(taskInfo);
    });
  });

  describe('when sending a shipment', function() {

    beforeEach(function() {
      spyOn(this.modalInput, 'dateTime').and.returnValue({ result: this.$q.when(new Date()) });
      this.shipment = this.createShipment();
      this.createController(this.shipment);
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
      var self = this,
          errorMsgs = [
            'TimeSentBeforePacked',
            'simulated error'
          ];

      spyOn(this.toastr, 'error').and.returnValue(null);

      errorMsgs.forEach(function (errMsg, index) {
        var args;

        self.Shipment.prototype.send =
          jasmine.createSpy().and.returnValue(self.$q.reject(errMsg));
        self.controller.sendShipment();
        self.scope.$digest();
        expect(self.toastr.error.calls.count()).toBe(index + 1);

        if (errMsg === 'TimeReceivedBeforeSent') {
          args = self.toastr.error.calls.argsFor(index);
          expect(args[0]).toContain('The sent time is before the packed time');
        }
      });
    });

  });

  describe('adding more items', function() {

    beforeEach(function() {
      this.shipment = this.createShipment();

      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(this.$state, 'go').and.returnValue(null);

      this.createController(this.shipment);
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
      spyOn(this.notificationsService, 'updateError').and.returnValue(null);
      this.controller.addMoreItems();
      this.scope.$digest();
      expect(this.notificationsService.updateError).toHaveBeenCalled();
    });

  });

});
