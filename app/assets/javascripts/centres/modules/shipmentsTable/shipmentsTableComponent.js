/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash'),
      sprintf = require('sprintf-js').sprintf;

  /**
   * Displays the shipments originating from, or destined to, a centre.
   *
   * @param {domain.centres.Centre} centre - The centre to display shipments for.
   *
   * @param {domain.centres.ShipmentTypes} shipmentTypes - the type of shipments to display.
   */
  var COMPONENT = {
    templateUrl : '/assets/javascripts/centres/modules/shipmentsTable/shipmentsTable.html',
    controller: ShipmentsTableController,
    controllerAs: 'vm',
    bindings: {
      centre:        '<',
      shipmentTypes: '@'
    }
  };

  ShipmentsTableController.$inject = [
    '$state',
    'Shipment',
    'ShipmentState',
    'SHIPMENT_TYPES',
    'timeService'
  ];

  /*
   * Controller for this component.
   */
  function ShipmentsTableController($state,
                                    Shipment,
                                    ShipmentState,
                                    SHIPMENT_TYPES,
                                    timeService) {
    var vm = this;

    vm.statesToDisplay  = [];
    vm.tableDataLoading = true;
    vm.limit            = 5;
    vm.shipmentDates    = {};
    vm.tableController  = null;
    vm.centreFilter     = null;

    vm.$onInit             = onInit;
    vm.getTableData        = getTableData;
    vm.shipmentInformation = shipmentInformation;

    //--

    function onInit() {
      switch (vm.shipmentTypes) {
      case SHIPMENT_TYPES.INCOMING:
        vm.centreFilter = sprintf('toCentre:in:(%s)', vm.centre.name);
        break;
      case SHIPMENT_TYPES.OUTGOING:
        vm.centreFilter = sprintf('fromCentre:in:(%s)', vm.centre.name);
        break;
      case SHIPMENT_TYPES.COMPLETED:
        vm.centreFilter = sprintf('withCentre:in:(%s)', vm.centre.name);
        break;
      default:
        throw new Error('shipmentTypes is invalid: ' + vm.shipmentTypes);
      }

      if (vm.shipmentTypes === SHIPMENT_TYPES.COMPLETED) {
        vm.statesToDisplay = [ ShipmentState.COMPLETED ];
      } else {
        vm.statesToDisplay = [
          ShipmentState.CREATED,
          ShipmentState.PACKED,
          ShipmentState.SENT,
          ShipmentState.RECEIVED,
          ShipmentState.UNPACKED,
          ShipmentState.LOST
        ];
      }

      vm.states = [{ label: 'any',  value: '' }].concat(
        vm.statesToDisplay.map(function (state) {
          return { label: state, value: state };
        }));
      vm.stateFilter = vm.states[0].value;
    }

    function getTableData(tableState, controller) {
      var pagination            = tableState.pagination,
          searchPredicateObject = tableState.search.predicateObject || {},
          sortPredicate         = tableState.sort.predicate || 'courierName',
          sortOrder             = tableState.sort.reverse || false,
          filters               = [],
          options = {
            sort:  sortPredicate,
            page:  1 + (pagination.start / vm.limit),
            limit: vm.limit
          };

      if (!vm.tableController && controller) {
        vm.tableController = controller;
      }

      if (vm.centreFilter) {
        filters.push(vm.centreFilter);
      }

      if (searchPredicateObject.courierName) {
        filters.push('courierName:like:' + searchPredicateObject.courierName);
      }

      if (searchPredicateObject.trackingNumber) {
        filters.push('trackingNumber:like:' + searchPredicateObject.trackingNumber);
      }

      if (vm.stateFilter === '') {
        filters.push('state:in:(' + vm.statesToDisplay.join(',') + ')');
      } else {
        filters.push('state:in:(' + vm.stateFilter + ')');
      }

      if (filters.length > 0) {
        options.filter = filters.join(';');
      }

      if (sortOrder) {
        options.sort = '-' + options.sort;
      }

      vm.tableDataLoading = true;
      Shipment.list(options).then(function (paginationResult) {
        vm.shipments = paginationResult.items;
        vm.hasShipments = (vm.shipments.length > 0);
        tableState.pagination.numberOfPages = paginationResult.maxPages;
        vm.tableDataLoading = false;

        vm.shipmentDates = {};
        _.each(vm.shipments, function (shipment) {
          if (shipment.isCreated()) {
            vm.shipmentDates[shipment.id] = timeService.dateToDisplayString(shipment.timeAdded);
          } else {
            vm.shipmentDates[shipment.id] = timeService.dateToDisplayString(shipment.timePacked);
          }
        });
      });
    }

    function shipmentInformation(shipment) {
      if (shipment.state === ShipmentState.CREATED) {
        $state.go('home.shipping.addItems', { shipmentId: shipment.id });
      } else if (shipment.state === ShipmentState.UNPACKED) {
        $state.go('home.shipping.shipment.unpack.info', { shipmentId: shipment.id });
      } else {
        $state.go('home.shipping.shipment', { shipmentId: shipment.id });
      }
    }
  }

  return COMPONENT;
});
