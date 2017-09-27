/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks           = require('angularMocks'),
      _               = require('lodash'),
      shipmentSpecimensControllerSharedBehaviour =
      require('../../../test/shipmentSpecimensControllerSharedBehaviour');

  describe('shipmentSpecimensAddComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ShippingComponentTestSuiteMixin, ServerReplyMixin) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype, ServerReplyMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/shipmentSpecimensAdd/shipmentSpecimensAdd.html',
        '/assets/javascripts/shipmentSpecimens/components/ssSpecimensPagedTable/ssSpecimensPagedTable.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Shipment',
                              'ShipmentSpecimen',
                              'notificationsService',
                              'modalService',
                              'factory');

      this.createController = function (shipment, readOnly) {
        ShippingComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<shipment-specimens-add shipment="vm.shipment" read-only="vm.readOnly"></shipment-specimens-add>',
          {
            shipment: shipment,
            readOnly: readOnly
          },
          'shipmentSpecimensAdd');
      };
    }));

    it('should have valid scope', function() {
      var shipment = this.createShipment(),
          readOnly = true;

      this.createController(shipment, readOnly);

      expect(this.controller.shipment).toBe(shipment);
      expect(this.controller.readOnly).toBe(readOnly);
      expect(this.controller.inventoryIds).toBeEmptyString();
      expect(this.controller.refreshSpecimensTable).toBe(0);
      expect(this.controller.addSpecimens).toBeFunction();
      expect(this.controller.removeShipmentSpecimen).toBeFunction();
    });

    describe('when adding specimens to shipment', function() {

      it('can add a specimen', function() {
        var shipment = this.createShipment(),
            refreshCount;

        spyOn(this.Shipment.prototype, 'addSpecimens').and.returnValue(this.$q.when(shipment));
        spyOn(this.notificationsService, 'success').and.returnValue(null);

        this.createController(shipment);
        this.controller.inventoryIds = this.factory.stringNext();
        refreshCount = this.controller.refreshSpecimensTable;
        this.controller.addSpecimens();
        this.scope.$digest();

        expect(this.notificationsService.success).toHaveBeenCalled();
        expect(this.controller.inventoryIds).toBeEmptyString();
        expect(this.controller.refreshSpecimensTable).toBe(refreshCount + 1);
      });

      it('informs the user when a specimen cannot be added', function() {
        var self = this,
            errors = [
              this.errorReply('invalid specimen inventory IDs'),
              this.errorReply('specimens are already in an active shipment'),
              this.errorReply('invalid centre for specimen inventory IDs'),
              this.errorReply('simulated error'),
              'simulated error'
            ];

        spyOn(this.modalService, 'modalOk').and.returnValue(this.$q.when(null));

        this.createController(this.createShipment());
        this.controller.inventoryIds = this.factory.stringNext();

        errors.forEach(function (error, index) {
          self.Shipment.prototype.addSpecimens =
            jasmine.createSpy().and.returnValue(self.$q.reject(error));
          self.controller.addSpecimens();
          self.scope.$digest();
          expect(self.modalService.modalOk.calls.count()).toBe(index + 1);
        });
      });

      it('nothing done when user has not entered any inventory IDs', function() {
        spyOn(this.Shipment.prototype, 'addSpecimens').and.returnValue(null);

        this.createController(this.createShipment());
        this.createController(this.createShipment());
        this.controller.inventoryIds = '';
        this.controller.addSpecimens();
        this.scope.$digest();

        expect(this.Shipment.prototype.addSpecimens).not.toHaveBeenCalled();
      });

    });

    describe('removing a shipment', function() {

      it('can remove a shipment', function() {
        var shipment = this.createShipment(),
            shipmentSpecimen = new this.ShipmentSpecimen(
              this.factory.shipmentSpecimen({ shipmentId: shipment.id })),
            refreshCount;

        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
        spyOn(this.ShipmentSpecimen.prototype, 'remove').and.returnValue(this.$q.when(true));
        spyOn(this.notificationsService, 'success').and.returnValue(null);
        spyOn(this.$state, 'go').and.returnValue(null);

        this.createController(shipment);
        refreshCount = this.controller.refreshSpecimensTable;
        this.controller.removeShipmentSpecimen(shipmentSpecimen);
        this.scope.$digest();

        expect(this.ShipmentSpecimen.prototype.remove).toHaveBeenCalled();
        expect(this.notificationsService.success).toHaveBeenCalled();
        expect(this.controller.refreshSpecimensTable).toBe(refreshCount + 1);
      });

      it('removal of a shipment can be cancelled', function() {
        var shipment = this.createShipment(),
            shipmentSpecimen = new this.ShipmentSpecimen(
              this.factory.shipmentSpecimen({ shipmentId: shipment.id })),
            refreshCount;

        spyOn(this.Shipment.prototype, 'remove').and.returnValue(this.$q.when(true));

        this.createController(shipment);
        refreshCount = this.controller.refreshSpecimensTable;
        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.reject('Cancel'));
        this.controller.removeShipmentSpecimen(shipmentSpecimen);
        this.scope.$digest();

        expect(this.Shipment.prototype.remove).not.toHaveBeenCalled();
        expect(this.controller.refreshSpecimensTable).toBe(refreshCount);
      });

    });

    describe('(shared)', function() {

      shipmentSpecimensControllerSharedBehaviour();

    });

  });

});
