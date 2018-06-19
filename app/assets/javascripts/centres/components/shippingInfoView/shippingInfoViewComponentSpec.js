/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ShippingComponentTestSuiteMixin } from 'test/mixins/ShippingComponentTestSuiteMixin';
import ngModule from '../../index'

describe('shippingInfoViewComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ShippingComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Shipment',
                              'modalInput',
                              'notificationsService',
                              'centreLocationsModalService',
                              'Factory');

      this.createController = (shipment) =>
        this.createControllerInternal(
          '<shipping-info-view shipment="vm.shipment"><shipping-info-view>',
          { shipment: shipment },
          'shippingInfoView');
    });
  });

  describe('updates to courier name', function() {
    var context = {};

    beforeEach(function() {
      context.modalService = 'modalInput';
      context.modalInputFuncName = 'text';
      context.modalInputReturnValue = this.Factory.stringNext();
      context.shipmentUpdateFuncName = 'updateCourierName';
      context.controllerUpdateFuncName = 'editCourierName';
    });

    showSharedBehaviour(context);
  });

  describe('updates to tracking number', function() {
    var context = {};

    beforeEach(function() {
      context.modalService = 'modalInput';
      context.modalInputFuncName = 'text';
      context.modalInputReturnValue = this.Factory.stringNext();
      context.shipmentUpdateFuncName = 'updateTrackingNumber';
      context.controllerUpdateFuncName = 'editTrackingNumber';
    });

    showSharedBehaviour(context);
  });

  describe('updates to FROM location', function() {
    var context = {};

    beforeEach(function() {
      context.modalService = 'centreLocationsModalService';
      context.modalInputFuncName = 'open';
      context.modalInputReturnValue = this.Factory.stringNext();
      context.shipmentUpdateFuncName = 'updateFromLocation';
      context.controllerUpdateFuncName = 'editFromLocation';
    });

    showSharedBehaviour(context);
  });

  describe('updates to TO location', function() {
    var context = {};

    beforeEach(function() {
      context.modalService = 'centreLocationsModalService';
      context.modalInputFuncName = 'open';
      context.modalInputReturnValue = this.Factory.stringNext();
      context.shipmentUpdateFuncName = 'updateToLocation';
      context.controllerUpdateFuncName = 'editToLocation';
    });

    showSharedBehaviour(context);
  });

  function showSharedBehaviour(context) {

    describe('for tracking number', function() {

      beforeEach(function() {
        this.shipment = this.createShipment();

        spyOn(this[context.modalService], context.modalInputFuncName)
          .and.returnValue({ result: this.$q.when(context.modalInputReturnValue)});

        this.createController(this.shipment);
      });

      it('can update', function() {
        spyOn(this.Shipment.prototype, context.shipmentUpdateFuncName)
          .and.returnValue(this.$q.when(this.shipment));
        spyOn(this.notificationsService, 'success').and.returnValue(null);

        this.controller[context.controllerUpdateFuncName]();
        this.scope.$digest();

        expect(this.Shipment.prototype[context.shipmentUpdateFuncName]).toHaveBeenCalled();
        expect(this.notificationsService.success).toHaveBeenCalled();
      });

      it('informs user if update fails', function() {
        spyOn(this.Shipment.prototype, context.shipmentUpdateFuncName)
          .and.returnValue(this.$q.reject('simulated error'));
        spyOn(this.notificationsService, 'updateError').and.returnValue(null);
        this.controller[context.controllerUpdateFuncName]();
        this.scope.$digest();

        expect(this.notificationsService.updateError).toHaveBeenCalled();
      });

    });

  }

});
