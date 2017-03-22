/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks           = require('angularMocks'),
      _               = require('lodash');

  describe('shipmentsTableComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ShippingComponentTestSuiteMixin) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Centre',
                              'Shipment',
                              'ShipmentState',
                              'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/centres/components/shipmentsTable/shipmentsTable.html');

      this.centre = new this.Centre(this.factory.centre());

      this.createScope = function (centre, shipmentStatesFilter) {
        ShippingComponentTestSuiteMixin.prototype.createScope.call(
          this,
          '<shipments-table centre="vm.centre" shipment-states-filter="vm.statesFilter"></shipments-table>',
          {
            centre: centre,
            shipmentStatesFilter: shipmentStatesFilter
          },
          'shipmentsTable');
      };

      this.createTableState = function (searchPredicatObject,
                                        sortPredicate,
                                        sortOrderReverse) {
        var result = {
          sort: {
            predicate: sortPredicate,
            reverse: sortOrderReverse || false
          },
          pagination: { start: 0, totalItemCount: 0 }
        };

        if (searchPredicatObject) {
          result.search = { predicateObject: searchPredicatObject };
        }
        return result;
      };

      this.addTableController = function (searchPredicatObject,
                                          sortPredicate) {
        this.controller.tableController = {
          tableState: jasmine.createSpy()
            .and.returnValue(this.createTableState(searchPredicatObject, sortPredicate))
        };
      };
    }));

    it('scope is valid on startup', function() {
      this.createScope(this.centre);
      expect(this.controller.tableDataLoading).toBeTrue();
      expect(this.controller.states).toContain({ label: 'Any', value: '' });

      _.map(this.ShipmentStates, function (state) {
        expect(this.controller.states).toContain({ label: state, value: state.toLowerCase() });
      });
    });

    it('changes to state filter causes a table reload', function() {
      var shipment = this.createShipment(),
          statesFilter = '';

      spyOn(this.Shipment, 'list')
        .and.returnValue(this.$q.when(this.factory.pagedResult([ shipment ])));
      this.createScope(this.centre, statesFilter);
      this.addTableController({ courierName: 'test' });

      this.controller.$onChanges({
        shipmentStatesFilter: {
          currentValue: [ this.ShipmentState.CREATED ]
        }
      });
      this.scope.$digest();
      expect(this.Shipment.list).toHaveBeenCalled();
      expect(this.controller.shipmentDates).toBeNonEmptyObject();
    });

    it('table reload correctly with different search predicates and reverse order', function() {
      var self = this,
          shipment = this.createShipment(),
          searchPredicateObjects = [
            { courierName: 'test' },
            { trackingNumber: 'test' }
          ];

      shipment.state = this.ShipmentState.PACKED;
      spyOn(this.Shipment, 'list')
        .and.returnValue(this.$q.when(this.factory.pagedResult([ shipment ])));
      this.createScope(this.centre, []);

      searchPredicateObjects.forEach(function (searchPredicateObjects, index) {
        self.controller.getTableData(self.createTableState(searchPredicateObjects, 'courierName', true));
        self.scope.$digest();
        expect(self.Shipment.list.calls.count()).toEqual(index + 1);
        expect(self.controller.shipmentDates).toBeNonEmptyObject();
      });
    });

    it('selecting a shipment goes to correct state', function() {
      var self = this,
          callCount = 0;

      spyOn(this.$state, 'go').and.returnValue(null);

      this.createScope(this.centre);
      _.forEach(this.ShipmentState, function (state) {
        var shipment = self.factory.shipment({ state: state }),
            args;

        self.controller.shipmentInformation(shipment);
        args = self.$state.go.calls.argsFor(callCount);

        switch (state) {
        case self.ShipmentState.CREATED:
          expect(args[0]).toBe('home.shipping.addItems');
          break;
        case self.ShipmentState.PACKED:
        case self.ShipmentState.SENT:
        case self.ShipmentState.RECEIVED:
        case self.ShipmentState.LOST:
          expect(args[0]).toBe('home.shipping.shipment');
          break;
        case self.ShipmentState.UNPACKED:
          expect(args[0]).toBe('home.shipping.shipment.unpack.info');
          break;
        }
        expect(args[1]).toEqual({ shipmentId: shipment.id });
        callCount += 1;
      });
    });


  });

});
