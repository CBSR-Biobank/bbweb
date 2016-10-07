/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * refresh - value to update to force a table refresh.
   *
   * onGetSpecimens - uses a paged API to return an object with properties:
   *   - items: the shipment specimens to display in the table
   *   - maxPages: the total number of pages the rest of the specimens can be found in
   *
   * onRemoveSpecimen - called when remove action is pressed by user.
   */
  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentSpecimensTable/shipmentSpecimensTable.html',
    controller: ShipmentSpecimensTableController,
    controllerAs: 'vm',
    bindings: {
      defaultSortField: '@',
      refresh:            '<',
      onGetSpecimens:     '&',
      onRemoveSpecimen:   '&',
      readOnly:           '<',
      noSpecimensMessage: '@'
    }
  };

  ShipmentSpecimensTableController.$inject = [
    'BbwebError'
  ];

  /**
   *
   */
  function ShipmentSpecimensTableController(BbwebError) {
    var vm = this;

    vm.$onChanges        = onChanges;
    vm.shipmentSpecimens = [];
    vm.pageSize          = 10;
    vm.tableController   = undefined;
    vm.getTableData      = getTableData;
    vm.removeSpecimen    = removeSpecimen;

    //---

    function onChanges() {
      if (vm.tableController) {
        reloadTableData();
      }
    }

    function getTableData(tableState, controller) {
      var pagination    = tableState.pagination,
          sortPredicate = tableState.sort.predicate || vm.defaultSortField,
          sortOrder     = tableState.sort.reverse || false,
          options = {
            sort:     sortPredicate,
            page:     1 + (pagination.start / vm.pageSize),
            pageSize: vm.pageSize,
            order:    sortOrder ? 'desc' : 'asc'
          };

      if (!vm.tableController && controller) {
        vm.tableController = controller;
      }

      vm.tableDataLoading = true;

      vm.onGetSpecimens()(options).then(function (result) {
        vm.shipmentSpecimens = result.items;
        tableState.pagination.numberOfPages = result.maxPages;
        vm.tableDataLoading = false;
      });
    }

    function removeSpecimen(ss) {
      if (vm.readOnly) {
        throw new BbwebError('cannot revmove specimen when in read only mode');
      }
      vm.onRemoveSpecimen()(ss);
    }

    function reloadTableData() {
      getTableData(vm.tableController.tableState());
    }
  }

  return component;
});
