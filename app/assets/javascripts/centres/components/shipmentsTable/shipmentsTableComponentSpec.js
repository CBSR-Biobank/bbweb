/**
 * Jasmine test suite
 *
 */
/* global angular */

import { ShippingComponentTestSuiteMixin } from 'test/mixins/ShippingComponentTestSuiteMixin';
import _ from 'lodash';
import ngModule from '../../index'

describe('shipmentsTableComponent', function() {

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, ShippingComponentTestSuiteMixin);

      this.injectDependencies('$q',
                              '$state',
                              'Centre',
                              'Shipment',
                              'ShipmentState',
                              'SHIPMENT_TYPES',
                              'Factory');

      this.centre = new this.Centre(this.Factory.centre());
      this.createController =
        (centre,
         shipmentTypes = this.SHIPMENT_TYPES.INCOMING,
         statesToDisplay = []) => {
           this.createControllerInternal(
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

    const stateInfo = Object.values(this.ShipmentState).map((state) => ({
      label: state,
      value: state
    }));

    this.controller.states.forEach((state) => {
      if (state.label !== 'any') {
        expect(stateInfo).toContain({ label: state.label, value: state.value });
      }
    })
  });

  it('table reloads correctly with different search predicates and reverse order', function() {
    var shipment = this.createShipment(),
        searchPredicateObjects = [
          { courierName:    'test' },
          { trackingNumber: 'test' }
        ],
        shipmentTypes;

    shipment.state = this.ShipmentState.PACKED;
    spyOn(this.Shipment, 'list').and.returnValue(this.$q.when(this.Factory.pagedResult([ shipment ])));

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
    Object.keys(this.ShipmentState).forEach((state) => {
      var shipment = this.Factory.shipment({ state: state }),
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
