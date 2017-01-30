/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  /**
   * Displays the shipments originating from, or destined to, a centre.
   *
   * @param {domain.centres.Centre} centre - The centre to display shipments for.
   *
   * @param {Array<domain.centres.ShipmentState>} shipmentStatesFilter - the states to filter shipments by.
   */
  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentsTable/shipmentsTable.html',
    controller: ShipmentsTableController,
    controllerAs: 'vm',
    bindings: {
      centre:               '<',
      shipmentStatesFilter: '<'
    }
  };

  ShipmentsTableController.$inject = [
    '$state',
    'Shipment',
    'ShipmentState',
    'timeService'
  ];

  /*
   * Controller for this component.
   */
  function ShipmentsTableController($state, Shipment, ShipmentState, timeService) {
    var vm = this;

    vm.$onChanges       = onChanges;
    vm.states           = initStates();
    vm.centreLocations  = _.keyBy(vm.centreLocations, 'locationId');
    vm.tableDataLoading = true;
    vm.limit            = 5;
    vm.shipmentDates    = {};
    vm.tableController  = null;

    vm.getTableData        = getTableData;
    vm.shipmentInformation = shipmentInformation;

    //--

    function onChanges(changesObj) {
      if (changesObj.shipmentStatesFilter) {
        vm.shipmentStatesFilter = changesObj.shipmentStatesFilter.currentValue;
        reloadTableData();
      }
    }

    function initStates() {
      return _.concat({ label: 'Any',  value: '' }, _.map(ShipmentState, function (state) {
        return { label: state, value: state.toLowerCase() };
      }));
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

      if (searchPredicateObject.courierName) {
        filters.push('courierName:like:' + searchPredicateObject.courierName);
      }

      if (searchPredicateObject.trackingNumber) {
        filters.push('trackingNumber:like:' + searchPredicateObject.trackingNumber);
      }

      if (vm.shipmentStatesFilter.length > 0) {
        filters.push('state:in:(' + vm.shipmentStatesFilter.join(',') + ')');
      }

      if (filters.length > 0) {
        options.filter = filters.join(';');
      }

      if (sortOrder) {
        options.sort = '-' + options.sort;
      }

      vm.tableDataLoading = true;
      Shipment.list(vm.centre.id, options).then(function (paginationResult) {
        vm.shipments = paginationResult.items;
        vm.hasShipments = (vm.shipments.length > 0);
        tableState.pagination.numberOfPages = paginationResult.maxPages;
        vm.tableDataLoading = false;

        vm.shipmentDates = {};
        _.each(vm.shipments, function (shipment) {
          if (shipment.isCreated) {
            vm.shipmentDates[shipment.id] = timeService.dateToDisplayString(shipment.timeAdded);
          } else {
            vm.shipmentDates[shipment.id] = timeService.dateToDisplayString(shipment.timePacked);
          }
        });
      });
    }

    function reloadTableData() {
      if (vm.tableController) {
        getTableData(vm.tableController.tableState());
      }
    }

    function shipmentInformation(shipment) {
      if (shipment.state === ShipmentState.CREATED) {
        $state.go('home.shipping.addItems', { shipmentId: shipment.id });
      } else if (shipment.state === ShipmentState.UNPACKED) {
        $state.go('home.shipping.unpack.info', { shipmentId: shipment.id });
      } else {
        $state.go('home.shipping.shipment', { shipmentId: shipment.id });
      }
    }
  }

  return component;
});
