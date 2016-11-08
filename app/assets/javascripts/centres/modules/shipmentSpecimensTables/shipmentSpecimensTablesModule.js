/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 *
 * See: http://jasonwatmore.com/post/2014/03/25/angularjs-a-better-way-to-implement-a-base-controller
 *
 */
define(function (require) {
  'use strict';

  var angular = require('angular'),
      //_       = require('lodash'),
      name    = 'biobank.shipmentSpecimensTables',
      module;

  function specimenTableActionDirective()  {
    var directive = {
      restrict: 'E',
      replace: true,
      templateUrl : '/assets/javascripts/centres/modules/shipmentSpecimensTables/specimenTableAction.html',
      scope: {
        action:            '=',
        onActionSelected:  '&'
      }
    };
    return directive;
  }

  /**
   * refresh - value to update to force a table refresh.
   *
   * onGetSpecimens - uses a paged API to return an object with properties:
   *   - items: the shipment specimens to display in the table
   *   - maxPages: the total number of pages the rest of the specimens can be found in
   *
   * onActionSelected - called when the action button is pressed by user.
   */
  var shipmentSpecimensViewTableComponent = {
    templateUrl : '/assets/javascripts/centres/modules/shipmentSpecimensTables/shipmentSpecimensTable.html',
    controller: 'ShipmentSpecimensViewTableController',
    controllerAs: 'vm',
    bindings: {
      defaultSortField:   '@',
      onGetSpecimens:     '&',
      noSpecimensMessage: '@'
    }
  };

  /**
   * refresh - value to update to force a table refresh.
   *
   * onGetSpecimens - uses a paged API to return an object with properties:
   *   - items: the shipment specimens to display in the table
   *   - maxPages: the total number of pages the rest of the specimens can be found in
   *
   * actions: actions are rendered as buttons. This binding contains an array of action information. The
   * information for a single action is an object with the following attributes: "id", "title", "class" and
   * "icon".
   *
   * onActionSelected - called when the action button is pressed by user.
   */
  var shipmentSpecimensActionsTableComponent = {
    templateUrl : '/assets/javascripts/centres/modules/shipmentSpecimensTables/shipmentSpecimensTable.html',
    controller: 'ShipmentSpecimensActionsTableController',
    controllerAs: 'vm',
    bindings: {
      defaultSortField:   '@',
      refresh:            '<',
      showItemState:      '<',
      onGetSpecimens:     '&',
      noSpecimensMessage: '@',
      actions:        '<',
      onActionSelected:   '&'
    }
  };

  module = angular.module(name, [])
    .directive('specimenTableAction',                      specimenTableActionDirective)
    .component('shipmentSpecimensViewTable',               shipmentSpecimensViewTableComponent)
    .component('shipmentSpecimensActionsTable',            shipmentSpecimensActionsTableComponent)
    .controller('ShipmentSpecimensTableController',        ShipmentSpecimensTableController)
    .controller('ShipmentSpecimensViewTableController',    ShipmentSpecimensViewTableController)
    .controller('ShipmentSpecimensActionsTableController', ShipmentSpecimensActionsTableController);

  ShipmentSpecimensTableController.$inject = ['vm', 'BbwebError'];

  function ShipmentSpecimensTableController(vm, BbwebError) {
    vm.$onChanges        = onChanges;
    vm.shipmentSpecimens = [];
    vm.readOnly          = true;
    vm.limit          = 10;
    vm.tableController   = undefined;
    vm.getTableData      = getTableData;
    vm.removeSpecimen    = removeSpecimen;
    vm.tableDataLoading  = true;

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
            page:     1 + (pagination.start / vm.limit),
            limit: vm.limit,
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

  ShipmentSpecimensViewTableController.$inject = ['$controller', 'BbwebError'];

  function ShipmentSpecimensViewTableController($controller, BbwebError) {
    var vm = this;
    $controller('ShipmentSpecimensTableController', { vm: vm, BbwebError: BbwebError });
    vm.refresh = 0;
  }

  ShipmentSpecimensActionsTableController.$inject = ['$controller', 'BbwebError'];

  function ShipmentSpecimensActionsTableController($controller, BbwebError) {
    var vm = this;
    $controller('ShipmentSpecimensTableController', { vm: vm, BbwebError: BbwebError });
    vm.readOnly = false;
    vm.actionSelected = actionSelected;

    //--

    function actionSelected(shipmentSpecimen, action) {
      vm.onActionSelected()(shipmentSpecimen, action);
    }
  }

  return {
    name: name,
    module: module
  };
});
