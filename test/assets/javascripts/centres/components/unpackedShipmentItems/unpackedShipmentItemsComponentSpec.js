/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('unpackedShipmentItemsComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ShippingComponentTestSuiteMixin, ServerReplyMixin) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype, ServerReplyMixin.prototype);
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/unpackedShipmentItems/unpackedShipmentItems.html',
        '/assets/javascripts/common/components/collapsablePanel/collapsablePanel.html',
        '/assets/javascripts/shipmentSpecimens/components/ssSpecimensPagedTable/ssSpecimensPagedTable.html');

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              'ShipmentItemState',
                              'Shipment',
                              'ShipmentSpecimen',
                              'modalService',
                              'factory');

      this.createScope = function (shipment, itemState) {
        ShippingComponentTestSuiteMixin.prototype.createScope.call(
          this,
          '<unpacked-shipment-items shipment="vm.shipment" item-state="' +
            itemState + '"><unpacked-shipment-items>',
          { shipment:  shipment },
          'unpackedShipmentItems');
      };
    }));

    it('has valid scope for shipment item state RECEIVED', function() {
      var shipment = this.createShipment();

      this.createScope(shipment, this.ShipmentItemState.RECEIVED);
      expect(this.controller.actions).toBeNonEmptyArray();
      expect(this.controller.panelHeading).toBe('Received specimens in this shipment');
      expect(this.controller.noSpecimensMessage).toBe('No received specimens present in this shipment');
    });

    it('has valid scope for shipment item state MISSING', function() {
      var shipment = this.createShipment();

      this.createScope(shipment, this.ShipmentItemState.MISSING);
      expect(this.controller.actions).toBeNonEmptyArray();
      expect(this.controller.panelHeading).toBe('Missing specimens in this shipment');
      expect(this.controller.noSpecimensMessage).toBe('No missing specimens present in this shipment');
    });

    it('has valid scope for shipment item state not in RECEIVED or MISSING', function() {
      var self = this,
          shipment = this.createShipment();

      expect(function () {
        self.createScope(shipment, self.ShipmentItemState.EXTRA);
      }).toThrowError(/invalid item state/);
    });

    it('emits event when created', function() {
      var shipment = this.createShipment(),
          eventEmitted = false;

      this.$rootScope.$on('tabbed-page-update', function (event, arg) {
        expect(arg).toBe('tab-selected');
        eventEmitted = true;
      });

      this.createScope(shipment, this.ShipmentItemState.MISSING);
      expect(eventEmitted).toBeTrue();
    });

    describe('can get RECEIVED shipments', function() {
      var context = {};

      beforeEach(function() {
        var self = this;

        context.createScope = function () {
          self.createScope(self.createShipment(), self.ShipmentItemState.RECEIVED);
        };
      });

      getSpecimensSharedBehaviour(context);
    });

    describe('can get MISSING shipments', function() {
      var context = {};

      beforeEach(function() {
        var self = this;

        context.createScope = function () {
          self.createScope(self.createShipment(), self.ShipmentItemState.MISSING);
        };
      });

      getSpecimensSharedBehaviour(context);
    });

    describe('when a table action is selected', function() {

      beforeEach(function() {
        this.shipment = this.createShipment();
        this.shipmentSpecimen = new this.ShipmentSpecimen(this.factory.shipmentSpecimen());

        spyOn(this.Shipment.prototype, 'tagSpecimensAsPresent').and.returnValue(this.$q.when(this.shipment));
        this.createScope(this.shipment, this.ShipmentItemState.RECEIVED);
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
              shipmentSpecimens = [ new this.ShipmentSpecimen(this.factory.shipmentSpecimen()) ],
              promiseSucceeded = false;

          spyOn(this.ShipmentSpecimen, 'list')
            .and.returnValue(this.$q.when(this.factory.pagedResult(shipmentSpecimens)));

          context.createScope();
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
          var shipmentSpecimens = [ new this.ShipmentSpecimen(this.factory.shipmentSpecimen()) ],
              promiseSucceeded = false;

          spyOn(this.ShipmentSpecimen, 'list')
            .and.returnValue(this.$q.when(this.factory.pagedResult(shipmentSpecimens)));

          context.createScope();
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

});
