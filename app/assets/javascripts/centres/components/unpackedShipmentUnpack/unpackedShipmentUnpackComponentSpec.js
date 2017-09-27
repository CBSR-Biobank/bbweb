/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';

describe('unpackedShipmentUnpackComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ShippingComponentTestSuiteMixin, ServerReplyMixin) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype, ServerReplyMixin.prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'Shipment',
                              'ShipmentSpecimen',
                              'ShipmentItemState',
                              'modalService',
                              'factory');

      this.createController = (shipment) =>
        ShippingComponentTestSuiteMixin.prototype.createController.call(
          this,
          '<unpacked-shipment-unpack shipment="vm.shipment""><unpacked-shipment-unpack>',
          { shipment:  shipment },
          'unpackedShipmentUnpack');
    });
  });

  it('has valid scope for shipment item state RECEIVED', function() {
    var shipment = this.createShipment();

    this.createController(shipment);
    expect(this.controller.refreshTable).toBe(0);
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

  describe('for getting present specimens', function() {

    it('retrieves extra specimens', function() {
      var self = this,
          shipment = this.createShipment(),
          shipmentSpecimens = [ new this.ShipmentSpecimen(this.factory.shipmentSpecimen()) ],
          promiseSucceeded = false,
          args;

      spyOn(this.ShipmentSpecimen, 'list')
        .and.returnValue(this.$q.when(this.factory.pagedResult(shipmentSpecimens)));

      this.createController(shipment);
      this.controller.getPresentSpecimens().then(function (result) {
        expect(result.items).toBeArrayOfSize(1);
        expect(result.items[0]).toEqual(jasmine.any(self.ShipmentSpecimen));
        expect(result.maxPages).toBeDefined();
        promiseSucceeded = true;
      });
      this.scope.$digest();
      args = this.ShipmentSpecimen.list.calls.argsFor(0);
      expect(args[1].filter).toBe('state:in:' + this.ShipmentItemState.PRESENT);
      expect(promiseSucceeded).toBeTrue();
    });

    it('returns empty array if shipment is undefined', function() {
      var shipmentSpecimens = [ new this.ShipmentSpecimen(this.factory.shipmentSpecimen()) ],
          promiseSucceeded = false;

      spyOn(this.ShipmentSpecimen, 'list')
        .and.returnValue(this.$q.when(this.factory.pagedResult(shipmentSpecimens)));

      this.createController();
      this.controller.shipment = undefined;
      this.controller.getPresentSpecimens().then(function (result) {
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

      spyOn(this.Shipment.prototype, 'tagSpecimensAsReceived').and.returnValue(this.$q.when(true));

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
            this.errorReply('EntityCriteriaError: invalid inventory Ids: xxxx'),
            this.errorReply('EntityCriteriaError: specimens not in this shipment: xxxx'),
            this.errorReply('EntityCriteriaError: shipment specimens not present: xxx'),
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

        self.Shipment.prototype.tagSpecimensAsReceived =
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

    it('when inventory ID holds a null value', function() {
      var self = this,
          shipment = this.createShipment();

      spyOn(this.Shipment.prototype, 'tagSpecimensAsReceived').and.callThrough();
      this.createController(shipment);
      this.controller.inventoryIds = null;
      expect(self.controller.onInventoryIdsSubmit()).toBeNull();
      expect(this.Shipment.prototype.tagSpecimensAsReceived).not.toHaveBeenCalled();
    });


  });

  it('specimen can be tagged as missing', function() {
    this.shipment = this.createShipment();
    this.shipmentSpecimen = new this.ShipmentSpecimen(this.factory.shipmentSpecimen());

    spyOn(this.Shipment.prototype, 'tagSpecimensAsMissing').and.returnValue(this.$q.when(this.shipment));

    this.createController(this.shipment);
    this.tableUpdateCount = this.controller.refreshTable;
    this.controller.tableActionSelected(this.shipmentSpecimen);
    this.scope.$digest();

    expect(this.Shipment.prototype.tagSpecimensAsMissing).toHaveBeenCalled();
    expect(this.controller.refreshTable).toBe(this.tableUpdateCount + 1);
  });

});
