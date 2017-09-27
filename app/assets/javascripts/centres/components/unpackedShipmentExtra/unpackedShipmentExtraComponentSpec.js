/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('unpackedShipmentExtraComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ShippingComponentTestSuiteMixin, ServerReplyMixin) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype, ServerReplyMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/unpackedShipmentExtra/unpackedShipmentExtra.html',
        '/assets/javascripts/common/components/collapsiblePanel/collapsiblePanel.html',
        '/assets/javascripts/shipmentSpecimens/components/ssSpecimensPagedTable/ssSpecimensPagedTable.html',
        '/assets/javascripts/common/services/modalService/modal.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Shipment',
                              'ShipmentSpecimen',
                              'ShipmentItemState',
                              'modalService',
                              'domainNotificationService',
                              'notificationsService',
                              'factory');

      this.createController = function (shipment) {
        ShippingComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<unpacked-shipment-extra shipment="vm.shipment"><unpacked-shipment-extra>',
          { shipment: shipment },
          'unpackedShipmentExtra');
      };
    }));

    it('has valid scope', function() {
      var shipment = this.createShipment();

      this.createController(shipment);
      expect(this.controller.refreshTable).toEqual(0);
      expect(this.controller.actions).toBeNonEmptyArray();
    });

    it('emits event when created', function() {
      var shipment = this.createShipment(),
          eventEmitted = false;

      this.$rootScope.$on('tabbed-page-update', function (event, arg) {
        expect(arg).toBe('tab-selected');
        eventEmitted = true;
      });

      this.createController(shipment);
      expect(eventEmitted).toBeTrue();
    });

    describe('for getting extra specimens', function() {

      it('retrieves extra specimens', function() {
        var self = this,
            shipment = this.createShipment(),
            shipmentSpecimens = [ new this.ShipmentSpecimen(this.factory.shipmentSpecimen()) ],
            promiseSucceeded = false,
            args;

        spyOn(this.ShipmentSpecimen, 'list')
          .and.returnValue(this.$q.when(this.factory.pagedResult(shipmentSpecimens)));

        this.createController(shipment);
        this.controller.getExtraSpecimens().then(function (result) {
          expect(result.items).toBeArrayOfSize(1);
          expect(result.items[0]).toEqual(jasmine.any(self.ShipmentSpecimen));
          expect(result.maxPages).toBeDefined();
          promiseSucceeded = true;
        });
        this.scope.$digest();
        args = this.ShipmentSpecimen.list.calls.argsFor(0);
        expect(args[1].filter).toBe('state:in:' + this.ShipmentItemState.EXTRA);
        expect(promiseSucceeded).toBeTrue();
      });

      it('returns empty array if shipment is undefined', function() {
        var shipmentSpecimens = [ new this.ShipmentSpecimen(this.factory.shipmentSpecimen()) ],
            promiseSucceeded = false;

        spyOn(this.ShipmentSpecimen, 'list')
          .and.returnValue(this.$q.when(this.factory.pagedResult(shipmentSpecimens)));

        this.createController();
        this.controller.shipment = undefined;
        this.controller.getExtraSpecimens().then(function (result) {
          expect(result.items).toBeArrayOfSize(0);
          expect(result.maxPages).toBeDefined();
          promiseSucceeded = true;
        });
        this.scope.$digest();
        expect(promiseSucceeded).toBeTrue();
      });

    });

    describe('when user enters inventory IDs', function() {

      it('when valid inventory IDs are entered', function() {
        var shipment = this.createShipment(),
            tableRefreshCount;

        spyOn(this.Shipment.prototype, 'tagSpecimensAsExtra').and.returnValue(this.$q.when(true));

        this.createController(shipment);
        this.controller.inventoryIds = this.factory.stringNext() + ','  + this.factory.stringNext();
        tableRefreshCount = this.controller.refreshTable;
        this.controller.onInventoryIdsSubmit();
        this.scope.$digest();
        expect(this.controller.inventoryIds).toBeEmptyString();
        expect(this.controller.refreshTable).toBe(tableRefreshCount + 1);
      });

      it('when INVALID inventory IDs are entered', function() {
        var self = this,
            shipment = this.createShipment(),
            errors = [
              this.errorReply('EntityCriteriaError: specimen inventory IDs already in this shipment: xxxx'),
              this.errorReply('EntityCriteriaError: specimens are already in an active shipment: xxxx'),
              this.errorReply('EntityCriteriaError: invalid inventory Ids: xxx'),
              this.errorReply('EntityCriteriaError: invalid centre for specimen inventory IDs: xxx'),
              this.errorReply(this.factory.stringNext()),
              this.factory.stringNext()
            ],
            tableRefreshCount;

        spyOn(this.modalService, 'modalOk').and.returnValues.apply(null, errors);

        this.createController(shipment);
        this.controller.inventoryIds = this.factory.stringNext() + ','  + this.factory.stringNext();
        tableRefreshCount = this.controller.refreshTable;

        errors.forEach(function (error, index) {
          var args;

          self.Shipment.prototype.tagSpecimensAsExtra =
            jasmine.createSpy().and.returnValue(self.$q.reject(error));
          self.controller.onInventoryIdsSubmit();
          self.scope.$digest();
          expect(self.controller.refreshTable).toBe(tableRefreshCount);
          expect(self.modalService.modalOk.calls.count()).toBe(index + 1);

          // check that modal message contains the invalid inventory ID
          args = self.modalService.modalOk.calls.argsFor(index);
          if (args[0] === 'Invalid inventory IDs') {
            expect(args[1]).toContain('xxx');
          }
        });
      });

    });

    describe('removing a specimen', function() {

      beforeEach(function() {
        this.shipment = this.createShipment();
        this.specimen = this.factory.specimen();
        this.shipmentSpecimen = new this.ShipmentSpecimen(
          this.factory.shipmentSpecimen({ specimen: this.specimen }));

        spyOn(this.domainNotificationService, 'removeEntity').and.callThrough();
        spyOn(this.notificationsService, 'success').and.returnValue(null);
      });


      it('a specimen can be removed', function() {
        var tableRefreshCount;

        spyOn(this.ShipmentSpecimen.prototype, 'remove').and.returnValue(this.$q.when(true));

        this.createController(this.shipment);
        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));
        tableRefreshCount = this.controller.refreshTable;
        this.controller.tableActionSelected(this.shipmentSpecimen);
        this.scope.$digest();

        expect(this.domainNotificationService.removeEntity).toHaveBeenCalled();
        expect(this.controller.refreshTable).toBe(tableRefreshCount + 1);
        expect(this.notificationsService.success).toHaveBeenCalled();
      });

      it('user can cancel removing a specimen', function() {
        var tableRefreshCount;

        spyOn(this.ShipmentSpecimen.prototype, 'remove').and.returnValue(this.$q.when(true));

        this.createController(this.shipment);
        tableRefreshCount = this.controller.refreshTable;
        spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.reject('Cancel'));
        this.controller.tableActionSelected(this.shipmentSpecimen);
        this.scope.$digest();

        expect(this.domainNotificationService.removeEntity).toHaveBeenCalled();
        expect(this.controller.refreshTable).toBe(tableRefreshCount);
        expect(this.ShipmentSpecimen.prototype.remove).not.toHaveBeenCalled();
        expect(this.notificationsService.success).not.toHaveBeenCalled();
      });

    });

  });

});
