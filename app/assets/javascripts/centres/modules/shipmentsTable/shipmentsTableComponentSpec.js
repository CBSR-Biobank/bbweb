/**
 * Jasmine test suite
 *
 */
/* global angular */

import _ from 'lodash';

describe('shipmentsTableComponent', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(ShippingComponentTestSuiteMixin) {
      _.extend(this, ShippingComponentTestSuiteMixin.prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Centre',
                              'Shipment',
                              'ShipmentState',
                              'SHIPMENT_TYPES',
                              'factory');

      this.centre = new this.Centre(this.factory.centre());
      this.createController = (centre, shipmentTypes, statesToDisplay) => {
        shipmentTypes = shipmentTypes || this.SHIPMENT_TYPES.INCOMING;
        statesToDisplay = statesToDisplay || [];
        ShippingComponentTestSuiteMixin.prototype.createController.call(
          this,
          `<shipments-table
             centre="vm.centre"
             shipment-types="${shipmentTypes}"
             states-to-display="vm.statesToDisplay">
           </shipments-table>`,
          {
            centre: centre,
            statesToDisplay: statesToDisplay
          },
          'shipmentsTable');
      };

      this.createTableState = (searchPredicatObject, sortPredicate, sortOrderReverse) => {
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

      this.addTableController = (searchPredicatObject, sortPredicate) => {
        this.controller.tableController = {
          tableState: jasmine.createSpy()
            .and.returnValue(this.createTableState(searchPredicatObject, sortPredicate))
        };
      };
    });
  });

  it('scope is valid on startup', function() {
    this.createController(this.centre);
    expect(this.controller.tableDataLoading).toBeTrue();
    expect(_.map(this.controller.states, 'label')).toContain('any');

    _.map(this.ShipmentStates, function (state) {
      expect(this.controller.states).toContain({ label: state, value: state.toLowerCase() });
    });
  });

  it('table reloads correctly with different search predicates and reverse order', function() {
    var shipment = this.createShipment(),
        searchPredicateObjects = [
          { courierName:    'test' },
          { trackingNumber: 'test' }
        ],
        shipmentTypes;

    shipment.state = this.ShipmentState.PACKED;
    spyOn(this.Shipment, 'list').and.returnValue(this.$q.when(this.factory.pagedResult([ shipment ])));

    shipmentTypes = [
      this.SHIPMENT_TYPES.INCOMING,
      this.SHIPMENT_TYPES.OUTGOING,
      this.SHIPMENT_TYPES.COMPLETED,
    ];

    shipmentTypes.forEach((shipmentType) => {
      this.createController(this.centre, shipmentType);

      searchPredicateObjects.forEach((searchPredicateObjects, index) => {
        this.controller.getTableData(this.createTableState(searchPredicateObjects, 'courierName', true));
        this.scope.$digest();
        expect(this.Shipment.list.calls.count()).toEqual(index + 1);
        expect(this.controller.shipmentDates).toBeNonEmptyObject();
      });

      this.Shipment.list.calls.reset();
    });
  });

  it('selecting a shipment goes to correct state', function() {
    var callCount = 0;

    spyOn(this.$state, 'go').and.returnValue(null);

    this.createController(this.centre);
    _.keys(this.ShipmentState).forEach((state) => {
      var shipment = this.factory.shipment({ state: state }),
          args;

      this.controller.shipmentInformation(shipment);
      args = this.$state.go.calls.argsFor(callCount);

      switch (state) {
      case this.ShipmentState.CREATED:
        expect(args[0]).toBe('home.shipping.addItems');
        break;
      case this.ShipmentState.PACKED:
      case this.ShipmentState.SENT:
      case this.ShipmentState.RECEIVED:
      case this.ShipmentState.LOST:
        expect(args[0]).toBe('home.shipping.shipment');
        break;
      case this.ShipmentState.UNPACKED:
        expect(args[0]).toBe('home.shipping.shipment.unpack.info');
        break;
      }
      expect(args[1]).toEqual({ shipmentId: shipment.id });
      callCount += 1;
    });
  });


});
