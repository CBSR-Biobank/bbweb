/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl: '/assets/javascripts/centres/components/unpackedShipmentItems/unpackedShipmentItems.html',
    controller: UnpackedShipmentItemsController,
    controllerAs: 'vm',
    bindings: {
      shipment:  '<',
      itemState: '@'
    }
  };

  UnpackedShipmentItemsController.$inject = [
    '$q',
    '$scope',
    'Shipment',
    'ShipmentSpecimen',
    'ShipmentItemState',
    'modalService',
    'gettextCatalog'
  ];

  /*
   * Loads all Shipment Specimens in PRESENT state.
   */
  function UnpackedShipmentItemsController($q,
                                           $scope,
                                           Shipment,
                                           ShipmentSpecimen,
                                           ShipmentItemState,
                                           modalService,
                                           gettextCatalog) {
    var vm = this;

    vm.$onInit = onInit;
    vm.refreshSpecimensTable = 0;

    vm.actions =  [
      {
        id:    'tag-as-extra',
        class: 'btn-warning',
        title: gettextCatalog.getString('Tag as present'),
        icon:  'glyphicon-arrow-left'
      }
    ];

    vm.getPresentSpecimens  = getPresentSpecimens;
    vm.onInventoryIdsSubmit = onInventoryIdsSubmit;
    vm.tableActionSelected  = tableActionSelected;

    //----

    function onInit() {
      $scope.$emit('tabbed-page-update', 'tab-selected');

      if (vm.itemState === ShipmentItemState.RECEIVED) {
        vm.noSpecimensMessage = gettextCatalog.getString('No received specimens present in this shipment');
      } else if (vm.itemState === ShipmentItemState.MISSING) {
        vm.noSpecimensMessage = gettextCatalog.getString('No missing specimens present in this shipment');
      } else {
        throw new Error('invalid item state: ' + vm.itemState);
      }
    }

    function getPresentSpecimens(options) {
      if (!vm.shipment) { return $q.when({ items: [], maxPages: 0 }); }

      _.extend(options, { filter: 'state:in:' + vm.itemState });

      return ShipmentSpecimen.list(vm.shipment.id, options)
        .then(function (paginatedResult) {
          return { items: paginatedResult.items, maxPages: paginatedResult.maxPages };
        });
    }

    /*
     * Inventory IDs entered by the user
     */
    function onInventoryIdsSubmit() {
      var inventoryIds = _.map(vm.inventoryIds.split(','), function (nonTrimmedInventoryId) {
        return nonTrimmedInventoryId.trim();
      });
      return vm.shipment.tagSpecimensAsReceived(inventoryIds);
    }

    function tableActionSelected(shipmentSpecimen) {
      modalService.modalOkCancel(
        gettextCatalog.getString('Confirm action'),
        gettextCatalog.getString(
          'Are you sure you want to return specimen with inventory ID <b>{{id}}</b> to unpacked state?',
          { id: shipmentSpecimen.specimen.inventoryId }))
        .then(function () {
          return vm.shipment.tagSpecimensAsPresent([ shipmentSpecimen.specimen.inventoryId ]);
        })
        .then(function () {
          vm.refreshSpecimensTable += 1;
        });
    }

  }

  return component;
});
