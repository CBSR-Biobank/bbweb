/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';

describe('shipmentViewCompletedComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ShippingComponentTestSuiteMixin, testUtils) {
      _.extend(this, ShippingComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'SHIPMENT_RECEIVE_PROGRESS_ITEMS',
                              'Factory');
      testUtils.addCustomMatchers();

      this.createController = (shipment) =>
        ShippingComponentTestSuiteMixin.createController.call(
          this,
          '<shipment-view-completed shipment="vm.shipment"></shipment-view-completed>',
          { shipment: shipment },
          'shipmentViewCompleted');
    });
  });

  it('has valid scope', function() {
    var shipment = this.createShipment();
    this.createController(shipment);

    expect(this.controller.progressInfo).toBeDefined();
    expect(this.controller.progressInfo.items).toBeArrayOfSize(this.SHIPMENT_RECEIVE_PROGRESS_ITEMS.length);
    expect(this.controller.progressInfo.items).toContainAll(this.SHIPMENT_RECEIVE_PROGRESS_ITEMS);
    expect(this.controller.progressInfo.current).toBe(4);
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
