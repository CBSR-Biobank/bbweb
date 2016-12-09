/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl: '/assets/javascripts/centres/components/unpackedShipmentReceive/unpackedShipmentReceive.html',
    controller: UnpackedShipmentReceiveController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  UnpackedShipmentReceiveController.$inject = [
    '$q',
    '$scope',
    'Shipment',
    'ShipmentSpecimen',
    'ShipmentItemState',
    'gettextCatalog'
  ];

  /**
   * Loads all Shipment Specimens in PRESENT state.
   */
  function UnpackedShipmentReceiveController($q,
                                             $scope,
                                             Shipment,
                                             ShipmentSpecimen,
                                             ShipmentItemState,
                                             gettextCatalog) {
    var vm = this;

    vm.$onInit = onInit;
    vm.refreshNonReceivedSpecimensTable = 0;

    vm.actions =  [
      {
        id:    'tag-as-extra',
        class: 'btn-warning',
        title: gettextCatalog.getString('Tag as extra'),
        icon:  'glyphicon-question-sign'
      }, {
        id:    'tag-as-missing',
        class: 'btn-warning',
        title: gettextCatalog.getString('Tag as missing'),
        icon:  'glyphicon-cloud'
      }
    ];

    vm.getPresentSpecimens = getPresentSpecimens;
    vm.onInventoryIdsSubmit = onInventoryIdsSubmit;
    vm.nonReceivedSpecimensTableActionSelected = nonReceivedSpecimensTableActionSelected;

    //----

    function onInit() {
      $scope.$emit('tabbed-page-update', 'shipment-present-selected');
    }

    function getPresentSpecimens(options) {
      if (!vm.shipment) { return $q.when({ items: [], maxPages: 0 }); }

      _.extend(options, { stateFilter: ShipmentItemState.PRESENT });

      return ShipmentSpecimen.list(vm.shipment.id, options)
        .then(function (paginatedResult) {
          return { items: paginatedResult.items, maxPages: paginatedResult.maxPages };
        });
    }

    /**
     * Inventory IDs entered by the user
     */
    function onInventoryIdsSubmit() {
      var inventoryIds = _.map(vm.inventoryIds.split(','), function (nonTrimmedInventoryId) {
        return nonTrimmedInventoryId.trim();
      });
      return Shipment.tagSpecimensAsReceived(inventoryIds);
    }

    function nonReceivedSpecimensTableActionSelected(shipmentSpecimen, action) {
      console.log('nonReceivedSpecimensTableActionSelected', shipmentSpecimen.id, action);
    }

  }

  return component;
});
