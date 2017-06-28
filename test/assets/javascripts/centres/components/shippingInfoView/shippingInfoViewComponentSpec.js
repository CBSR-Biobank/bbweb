/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('shippingInfoViewComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ShippingComponentTestSuiteMixin) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/shippingInfoView/shippingInfoView.html',
        '/assets/javascripts/common/components/collapsiblePanel/collapsiblePanel.html',
        '/assets/javascripts/common/components/statusLine/statusLine.html',
        '/assets/javascripts/common/modalInput/modalInput.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Shipment',
                              'modalInput',
                              'notificationsService',
                              'centreLocationsModalService',
                              'factory');

      this.createScope = function (shipment) {
        ShippingComponentTestSuiteMixin.prototype.createScope.call(
          this,
          '<shipping-info-view shipment="vm.shipment"><shipping-info-view>',
          { shipment: shipment },
          'shippingInfoView');
      };
    }));

    describe('updates to courier name', function() {
      var context = {};

      beforeEach(function() {
        context.modalService = 'modalInput';
        context.modalInputFuncName = 'text';
        context.modalInputReturnValue = this.factory.stringNext();
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
        context.modalInputReturnValue = this.factory.stringNext();
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
        context.modalInputReturnValue = this.factory.stringNext();
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
        context.modalInputReturnValue = this.factory.stringNext();
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

          this.createScope(this.shipment);
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

});
