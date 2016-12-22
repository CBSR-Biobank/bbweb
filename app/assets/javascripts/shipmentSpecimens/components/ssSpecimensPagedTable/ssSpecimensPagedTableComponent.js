/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  /**
   * Displays Shipment Specimens in a table.
   *
   * @param {string} defaultSortField - The column to sort by initially.
   *
   * @param {int} refresh - Increment this counter refresh the table.
   *
   * @param {boolean} showItemState - Set this to TRUE to how an additional column that displays the Shipment
   *    Specimen state.
   *
   * @param {function} onGetSpecimens - A callback that return the Shipment Specimens to display in a single
   *    page. This function takes has an object as a parameter, called {@link options}, that contains the page
   *    number to return results for. This should return an object with the following keys: items, and
   *    maxPages.
   *
   * @param {string} noSpecimensMessage - The text to display on the page where there are no Shipment
   *    Specimens to display.
   *
   * @param {object[]} actions - When this array is not empty, an additional column is displayed that contains
   *    one or more buttons the user can press to manipulate individual rows in the table.
   *
   * @param {funtion} onActionSelected - this function is called when one of the {@link action} buttons is
   *    pressed.
   */
  var component = {
    templateUrl : '/assets/javascripts/shipmentSpecimens/components/ssSpecimensPagedTable/ssSpecimensPagedTable.html',
    controller: Controller,
    controllerAs: 'vm',
    bindings: {
      defaultSortField:   '@',
      refresh:            '<',
      showItemState:      '<',
      onGetSpecimens:     '&',
      noSpecimensMessage: '@',
      actions:            '<',
      onActionSelected:   '&'
    }
  };

  Controller.$inject = ['$controller', 'BbwebError'];

  /**
   * Controller for the component.
   */
  function Controller($controller, BbwebError) {
    var vm = this;

    vm.$onChanges        = onChanges;
    vm.shipmentSpecimens = [];
    vm.limit             = 10;
    vm.tableController   = undefined;
    vm.getTableData      = getTableData;
    vm.tableDataLoading  = true;
    vm.refresh           = 0;
    vm.hasActions        = _.isArray(vm.actions) && (vm.actions.length > 0);
    vm.actionSelected    = actionSelected;

    //--

    /**
     * Parent components can trigger a table reload by calling this function.
     */
    function onChanges() {
      if (vm.tableController) {
        reloadTableData();
      }
    }

    /**
     * Called by Smart Table to get the table data.
     */
    function getTableData(tableState, controller) {
      var pagination    = tableState.pagination,
          sortPredicate = tableState.sort.predicate || vm.defaultSortField,
          revSortOrder  = tableState.sort.reverse || false,
          options = {
            sort:  (revSortOrder ? '-' : '') + sortPredicate,
            page:  1 + (pagination.start / vm.limit),
            limit: vm.limit
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

    /**
     * Forces table data to be reloaded.
     */
    function reloadTableData() {
      if (!vm.tableController) {
        throw new BbwebError('illegal state: cannot reload table data');
      }
      getTableData(vm.tableController.tableState());
    }

    function actionSelected(shipmentSpecimen, action) {
      vm.onActionSelected()(shipmentSpecimen, action);
    }
  }

  return component;
});
