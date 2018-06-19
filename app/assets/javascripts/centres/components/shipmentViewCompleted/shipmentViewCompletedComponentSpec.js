/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ShippingComponentTestSuiteMixin } from 'test/mixins/ShippingComponentTestSuiteMixin';
import ngModule from '../../index'

describe('shipmentViewCompletedComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ShippingComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'shipmentReceiveTasksService',
                              'Factory');
      this.addCustomMatchers();

      this.createController = (shipment) =>
        this.createControllerInternal(
          '<shipment-view-completed shipment="vm.shipment"></shipment-view-completed>',
          { shipment: shipment },
          'shipmentViewCompleted');
    });
  });

  it('has valid scope', function() {
    var shipment = this.createShipment();
    this.createController(shipment);

    const taskData = this.shipmentReceiveTasksService.getTaskData();

    expect(this.controller.progressInfo).toBeDefined();
    expect(this.controller.progressInfo).toBeArrayOfSize(Object.keys(taskData).length);
    taskData.forEach(taskInfo => {
      taskInfo.status = true;
      expect(this.controller.progressInfo).toContain(taskInfo);
    });
  });

  describe('returning to unpacked state', function() {

    beforeEach(function() {
      this.injectDependencies('$state', 'modalService', 'Shipment', 'notificationsService');
      this.shipment = this.createShipment();

      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(this.$state, 'go').and.returnValue(null);

      this.createController(this.shipment);
    });

    it('user can return shipment to unpacked state', function() {
      spyOn(this.Shipment.prototype, 'unpack').and.returnValue(this.$q.when(this.shipment));
      this.controller.returnToUnpackedState();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith(
        'home.shipping.shipment.unpack.info',
        { shipmentId: this.shipment.id});
    });

    it('user is informed if shipment cannot be returned to unpacked state', function() {
      spyOn(this.Shipment.prototype, 'unpack').and.returnValue(this.$q.reject('simulated error'));
      spyOn(this.notificationsService, 'updateError').and.returnValue(null);
      this.controller.returnToUnpackedState();
      this.scope.$digest();
      expect(this.notificationsService.updateError).toHaveBeenCalled();
    });

  });

});
