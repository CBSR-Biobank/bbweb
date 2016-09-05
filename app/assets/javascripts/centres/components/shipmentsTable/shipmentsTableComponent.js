/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentsTable/shipmentsTable.html',
    controller: ShipmentsTableController,
    controllerAs: 'vm',
    bindings: {
      centre: '<'
    }
  };

  ShipmentsTableController.$inject = [
    '$log',
    '$state',
    'Shipment',
    'ShipmentState'
  ];

  /**
   * Displays the shipments originating from, or destined to, a centre.
   */
  function ShipmentsTableController($log, $state, Shipment, ShipmentState) {
    var vm = this;

    vm.states           = initStates();
    vm.stateFilter      = '';
    vm.centreLocations  = _.keyBy(vm.centreLocations, 'locationId');
    vm.tableDataLoading = true;
    vm.pageSize         = 5;

    vm.getTableData        = getTableData;
    vm.shipmentInformation = shipmentInformation;

    //--

    function initStates() {
      return _.concat({ label: 'Any',  value: '' }, _.map(ShipmentState, function (state) {
        return { label: state, value: state.toLowerCase() };
      })) ;
    }

    function getTableData(tableState) {
      var pagination            = tableState.pagination,
          searchPredicateObject = tableState.search.predicateObject || {},
          sortPredicate         = tableState.sort.predicate || 'courierName',
          sortOrder             = tableState.sort.reverse || false,
          options = {
            courierFilter:        searchPredicateObject.courierName || '',
            trackingNumberFilter: searchPredicateObject.trackingNumber || '',
            stateFilter:          vm.stateFilter,
            sort:                 sortPredicate,
            page:                 1 + (pagination.start / vm.pageSize),
            pageSize:             vm.pageSize,
            order:                sortOrder ? 'desc' : 'asc'
          };

      vm.tableDataLoading = true;
      Shipment.list(vm.centre.id, options).then(function (paginationResult) {
        vm.shipments = paginationResult.items;
        vm.hasShipments = (vm.shipments.length > 0);
        tableState.pagination.numberOfPages = paginationResult.maxPages;
        vm.tableDataLoading = false;
      });
    }

    function shipmentInformation(shipment) {
      if (shipment.state === ShipmentState.CREATED) {
        $state.go('home.shipping.addItems', { shipmentId: shipment.id });
      } else if (shipment.state === ShipmentState.UNPACKED) {
        $state.go('home.shipping.shipment.unpack', { shipmentId: shipment.id });
      } else {
        $state.go('home.shipping.shipment', { shipmentId: shipment.id });
      }
    }
  }

  return component;
});
