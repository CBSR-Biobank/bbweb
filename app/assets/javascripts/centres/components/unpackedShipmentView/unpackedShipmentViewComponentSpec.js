/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ServerReplyMixin } from 'test/mixins/ServerReplyMixin';
import { ShippingComponentTestSuiteMixin } from 'test/mixins/ShippingComponentTestSuiteMixin';
import ngModule from '../../index'

describe('unpackedShipmentViewComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function(TestUtils) {
      var self = this;

      Object.assign(this, ShippingComponentTestSuiteMixin, ServerReplyMixin);
      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Shipment',
                              'shipmentReceiveTasksService',
                              'modalService',
                              'notificationsService',
                              'Factory');

      this.createController = (shipment) => {
        self.Shipment.get = jasmine.createSpy().and.returnValue(self.$q.when(shipment));

        ShippingComponentTestSuiteMixin.createController.call(
          this,
          '<unpacked-shipment-view shipment="vm.shipment"><unpacked-shipment-view>',
          { shipment: shipment },
          'unpackedShipmentView');
      };

      TestUtils.addCustomMatchers();
    });
  });

 it('has valid scope', function() {
    var shipment = this.createShipment();
    this.createController(shipment);

    expect(this.controller.active).toEqual(0);
    expect(this.controller.tabs).toBeNonEmptyArray();

    const taskData = this.shipmentReceiveTasksService.getTaskData();
    expect(this.controller.progressInfo).toBeDefined();
    expect(this.controller.progressInfo).toBeArrayOfSize(Object.keys(taskData).length);
    taskData.forEach((taskInfo, index) => {
      taskInfo.status = (index < 3);
      expect(this.controller.progressInfo).toContain(taskInfo);
    });
  });

  it('has valid tabs', function() {
    var expectedHeadings = [
      'Information',
      'Unpack specimens',
      'Received specimens',
      'Missing specimens',
      'Extra specimens'
    ];

    this.createController(this.createShipment());
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
      this.injectDependencies('ShipmentSpecimen');
      this.shipment = this.createShipment();
      this.createController(this.shipment);
    });

    it('user can return shipment to received state', function() {
      spyOn(this.$state, 'go').and.returnValue(null);
      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
      spyOn(this.ShipmentSpecimen, 'list').and.returnValue(this.$q.when(this.Factory.pagedResult([])));
      spyOn(this.Shipment.prototype, 'receive').and.returnValue(this.$q.when(this.shipment));

      this.controller.returnToReceivedState();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.shipping.shipment',
                                                  { shipmentId: this.shipment.id },
                                                  { reload: true });
    });

    it('user is informed if shipment cannot be returned to received state', function() {
      var shipmentSpecimen = new this.ShipmentSpecimen(this.Factory.shipmentSpecimen());

      spyOn(this.ShipmentSpecimen, 'list').and.returnValue(
        this.$q.when(this.Factory.pagedResult([ shipmentSpecimen ])));
      spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.when('OK'));

      this.controller.returnToReceivedState();
      this.scope.$digest();
      expect(this.modalService.modalOk).toHaveBeenCalled();
    });

  });

  describe('shipment can be completed', function() {

    beforeEach(function() {
      this.injectDependencies('ShipmentSpecimen');
      this.shipment = this.createShipment();
      this.createController(this.shipment);
    });

    it('user can place shipment in completed state', function() {
      this.injectDependencies('modalInput');
      spyOn(this.ShipmentSpecimen, 'list').and.returnValue(this.$q.when(this.Factory.pagedResult([])));
      spyOn(this.modalInput, 'dateTime').and.returnValue({ result: this.$q.when(new Date()) });
      spyOn(this.Shipment.prototype, 'complete').and.returnValue(this.$q.when(this.shipment));
      spyOn(this.$state, 'go').and.returnValue(null);

      this.controller.completeShipment();
      this.scope.$digest();
      expect(this.$state.go).toHaveBeenCalledWith('home.shipping.shipment',
                                                  { shipmentId: this.shipment.id },
                                                  { reload: true });
    });

    it('user is informed if shipment cannot be placed in completed state', function() {
      var shipmentSpecimen = new this.ShipmentSpecimen(this.Factory.shipmentSpecimen());

      spyOn(this.ShipmentSpecimen, 'list').and.returnValue(
        this.$q.when(this.Factory.pagedResult([ shipmentSpecimen ])));
      spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.when('OK'));

      this.controller.completeShipment();
      this.scope.$digest();
      expect(this.modalService.modalOk).toHaveBeenCalled();
    });
  });

});
