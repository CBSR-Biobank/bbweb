/**
 * Jasmine test suite
 *
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  function SuiteMixinFactory(ShippingComponentTestSuiteMixin) {

    function SuiteMixin() {
    }

    SuiteMixin.prototype = Object.create(ShippingComponentTestSuiteMixin.prototype);
    SuiteMixin.prototype.constructor = SuiteMixin;

    SuiteMixin.prototype.createController = function (centre, shipmentTypes, statesToDisplay) {
      shipmentTypes = shipmentTypes || this.SHIPMENT_TYPES.INCOMING;
      statesToDisplay = statesToDisplay || [];
      ShippingComponentTestSuiteMixin.prototype.createController.call(
        this,
        '<shipments-table' +
          ' centre="vm.centre"' +
          ' shipment-types="' + shipmentTypes + '"' +
          ' states-to-display="vm.statesToDisplay"' +
          '></shipments-table>',
        {
          centre: centre,
          statesToDisplay: statesToDisplay
        },
        'shipmentsTable');
    };

    SuiteMixin.prototype.createTableState = function (searchPredicatObject,
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

    SuiteMixin.prototype.addTableController = function (searchPredicatObject, sortPredicate) {
      this.controller.tableController = {
        tableState: jasmine.createSpy()
          .and.returnValue(this.createTableState(searchPredicatObject, sortPredicate))
      };
    };

    return SuiteMixin;

  }

  describe('shipmentsTableComponent', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ShippingComponentTestSuiteMixin) {
      var SuiteMixin = new SuiteMixinFactory(ShippingComponentTestSuiteMixin);
      _.extend(this, SuiteMixin.prototype);

      this.injectDependencies('$q',
                              '$rootScope',
                              '$compile',
                              '$state',
                              'Centre',
                              'Shipment',
                              'ShipmentState',
                              'SHIPMENT_TYPES',
                              'factory');
      this.putHtmlTemplates(
        '/assets/javascripts/centres/modules/shipmentsTable/shipmentsTable.html');

      this.centre = new this.Centre(this.factory.centre());
    }));

    it('scope is valid on startup', function() {
      this.createController(this.centre);
      expect(this.controller.tableDataLoading).toBeTrue();
      expect(_.map(this.controller.states, 'label')).toContain('any');

      _.map(this.ShipmentStates, function (state) {
        expect(this.controller.states).toContain({ label: state, value: state.toLowerCase() });
      });
    });

    it('table reloads correctly with different search predicates and reverse order', function() {
      var self = this,
          shipment = this.createShipment(),
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

      shipmentTypes.forEach(function (shipmentType) {
        self.createController(self.centre, shipmentType);

        searchPredicateObjects.forEach(function (searchPredicateObjects, index) {
          self.controller.getTableData(self.createTableState(searchPredicateObjects, 'courierName', true));
          self.scope.$digest();
          expect(self.Shipment.list.calls.count()).toEqual(index + 1);
          expect(self.controller.shipmentDates).toBeNonEmptyObject();
        });

        self.Shipment.list.calls.reset();
      });
    });

    it('selecting a shipment goes to correct state', function() {
      var self = this,
          callCount = 0;

      spyOn(this.$state, 'go').and.returnValue(null);

      this.createController(this.centre);
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
