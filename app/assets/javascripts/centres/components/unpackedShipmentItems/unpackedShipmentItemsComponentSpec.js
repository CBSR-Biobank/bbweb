/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ShippingComponentTestSuiteMixin } from 'test/mixins/ShippingComponentTestSuiteMixin';
import ngModule from '../../index'

describe('unpackedShipmentItemsComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ShippingComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'ShipmentItemState',
                              'Shipment',
                              'ShipmentSpecimen',
                              'modalService',
                              'Factory');

      this.createController = (shipment, itemState) =>
        this.createControllerInternal(
          `<unpacked-shipment-items shipment="vm.shipment"
                                    item-state="${itemState}">
           <unpacked-shipment-items>`,
          { shipment:  shipment },
          'unpackedShipmentItems');
    });
  });

  it('has valid scope for shipment item state RECEIVED', function() {
    var shipment = this.createShipment();

    this.createController(shipment, this.ShipmentItemState.RECEIVED);
    expect(this.controller.actions).toBeNonEmptyArray();
    expect(this.controller.panelHeading).toBe('Received specimens in this shipment');
    expect(this.controller.noSpecimensMessage).toBe('No received specimens present in this shipment');
  });

  it('has valid scope for shipment item state MISSING', function() {
    var shipment = this.createShipment();

    this.createController(shipment, this.ShipmentItemState.MISSING);
    expect(this.controller.actions).toBeNonEmptyArray();
    expect(this.controller.panelHeading).toBe('Missing specimens in this shipment');
    expect(this.controller.noSpecimensMessage).toBe('No missing specimens present in this shipment');
  });

  it('has valid scope for shipment item state not in RECEIVED or MISSING', function() {
    var shipment = this.createShipment();

    expect(
      () => this.createController(shipment, this.ShipmentItemState.EXTRA)
    ).toThrowError(/invalid item state/);
  });

  it('emits event when created', function() {
    var shipment = this.createShipment(),
        eventEmitted = false;

    this.$rootScope.$on('tabbed-page-update', function (event, arg) {
      expect(arg).toBe('tab-selected');
      eventEmitted = true;
    });

    this.createController(shipment, this.ShipmentItemState.MISSING);
    expect(eventEmitted).toBeTrue();
  });

  describe('can get RECEIVED shipments', function() {
    var context = {};

    beforeEach(function() {
      var self = this;

      context.createController = function () {
        self.createController(self.createShipment(), self.ShipmentItemState.RECEIVED);
      };
    });

    getSpecimensSharedBehaviour(context);
  });

  describe('can get MISSING shipments', function() {
    var context = {};

    beforeEach(function() {
      var self = this;

      context.createController = function () {
        self.createController(self.createShipment(), self.ShipmentItemState.MISSING);
      };
    });

    getSpecimensSharedBehaviour(context);
  });

  describe('when a table action is selected', function() {

    beforeEach(function() {
      this.shipment = this.createShipment();
      this.shipmentSpecimen = this.ShipmentSpecimen.create(this.Factory.shipmentSpecimen());

      spyOn(this.Shipment.prototype, 'tagSpecimensAsPresent').and.returnValue(this.$q.when(this.shipment));
      this.createController(this.shipment, this.ShipmentItemState.RECEIVED);
      this.tableUpdateCount = this.controller.refreshSpecimensTable;
    });


    it('shipment specimen can be returned to unpacked state', function() {
      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.when('OK'));

      this.controller.tableActionSelected(this.shipmentSpecimen);
      this.scope.$digest();

      expect(this.Shipment.prototype.tagSpecimensAsPresent).toHaveBeenCalled();
      expect(this.controller.refreshSpecimensTable).toBe(this.tableUpdateCount + 1);
    });

    it('table action can be cancelled', function() {
      spyOn(this.modalService, 'modalOkCancel').and.returnValue(this.$q.reject('Cancel'));

      this.controller.tableActionSelected(this.shipmentSpecimen);
      this.scope.$digest();

      expect(this.Shipment.prototype.tagSpecimensAsPresent).not.toHaveBeenCalled();
      expect(this.controller.refreshSpecimensTable).toBe(this.tableUpdateCount);
    });

  });

  function getSpecimensSharedBehaviour(context) {

    describe('for getting extra specimens', function() {

      it('retrieves extra specimens', function() {
        var self = this,
            shipmentSpecimens = [ this.ShipmentSpecimen.create(this.Factory.shipmentSpecimen()) ],
            promiseSucceeded = false;

        spyOn(this.ShipmentSpecimen, 'list')
          .and.returnValue(this.$q.when(this.Factory.pagedResult(shipmentSpecimens)));

        context.createController();
        this.controller.getSpecimens().then(function (result) {
          expect(result.items).toBeArrayOfSize(1);
          expect(result.items[0]).toEqual(jasmine.any(self.ShipmentSpecimen));
          expect(result.maxPages).toBeDefined();
          promiseSucceeded = true;
        });
        this.scope.$digest();
        expect(promiseSucceeded).toBeTrue();
      });

      it('returns empty array if shipment is undefined', function() {
        var shipmentSpecimens = [ this.ShipmentSpecimen.create(this.Factory.shipmentSpecimen()) ],
            promiseSucceeded = false;

        spyOn(this.ShipmentSpecimen, 'list')
          .and.returnValue(this.$q.when(this.Factory.pagedResult(shipmentSpecimens)));

        context.createController();
        this.controller.shipment = undefined;
        this.controller.getSpecimens().then(function (result) {
          expect(result.items).toBeArrayOfSize(0);
          expect(result.maxPages).toBeDefined();
          promiseSucceeded = true;
        });
        this.scope.$digest();
        expect(promiseSucceeded).toBeTrue();
      });

    });

  }

});
