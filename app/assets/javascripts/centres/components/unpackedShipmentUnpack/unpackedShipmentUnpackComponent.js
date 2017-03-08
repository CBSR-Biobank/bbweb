/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl: '/assets/javascripts/centres/components/unpackedShipmentUnpack/unpackedShipmentUnpack.html',
    controller: UnpackedShipmentUnpackController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  UnpackedShipmentUnpackController.$inject = [
    '$q',
    '$scope',
    'Shipment',
    'ShipmentSpecimen',
    'ShipmentItemState',
    'gettextCatalog',
    'modalService'
  ];

  /*
   * Allows user to interact with Shipment Specimens in PRESENT state.
   *
   * The user can receive the specimens, mark them as EXTRA or MISSING.
   */
  function UnpackedShipmentUnpackController($q,
                                            $scope,
                                            Shipment,
                                            ShipmentSpecimen,
                                            ShipmentItemState,
                                            gettextCatalog,
                                            modalService) {
    var vm = this;

    vm.$onInit = onInit;
    vm.refreshTable = 0;

    vm.actions =  [
      {
        id:    'tag-as-missing',
        class: 'btn-warning',
        title: gettextCatalog.getString('Tag as missing'),
        icon:  'glyphicon-cloud'
      }
    ];

    vm.getPresentSpecimens  = getPresentSpecimens;
    vm.onInventoryIdsSubmit = onInventoryIdsSubmit;
    vm.tableActionSelected  = tableActionSelected;

    //----

    function onInit() {
      $scope.$emit('tabbed-page-update', 'tab-selected');
    }

    function getPresentSpecimens(options) {
      if (!vm.shipment) { return $q.when({ items: [], maxPages: 0 }); }

      options = options || {};
      _.extend(options, { filter: 'state:in:' + ShipmentItemState.PRESENT });

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
      return vm.shipment.tagSpecimensAsReceived(inventoryIds)
        .then(function () {
          vm.inventoryIds = '';
          vm.refreshTable += 1;
        })
        .catch(function (err) {
          var modalMsg;

          if (err.message) {
            modalMsg = errorIsInvalidInventoryIds(err.message);
            if (_.isUndefined(modalMsg)) {
              modalMsg = errorIsShipSpecimensNotInShipment(err.message);
            }
            if (_.isUndefined(modalMsg)) {
              modalMsg = errorIsShipSpecimensNotPresent(err.message);
            }
          }

          if (modalMsg) {
            modalService.modalOk(gettextCatalog.getString('Invalid inventory IDs'), modalMsg);
            return;
          }

          modalService.modalOk(gettextCatalog.getString('Server error'), JSON.stringify(err));
        });
    }

    function errorIsInvalidInventoryIds(errMsg) {
      var regex = /EntityCriteriaError: invalid inventory Ids: (.*)/g,
          match = regex.exec(errMsg);
      if (match) {
        return gettextCatalog.getString('The following inventory IDs are invalid:<br>{{ids}}',
                                        { ids: match[1] });
      }
      return undefined;
    }

    function errorIsShipSpecimensNotInShipment(errMsg) {
      var regex = /EntityCriteriaError: specimens not in this shipment: (.*)/g,
          match = regex.exec(errMsg);
      if (match) {
        return gettextCatalog.getString(
          'The following inventory IDs are for specimens not present in this shipment:<br>{{ids}}',
          { ids: match[1] });
      }
      return undefined;
    }

    function errorIsShipSpecimensNotPresent(errMsg) {
      var regex = /EntityCriteriaError: shipment specimens not present: (.*)/g,
          match = regex.exec(errMsg);
      if (match) {
        return gettextCatalog.getString(
          'The following inventory IDs are for have already been unpacked:<br>{{ids}}',
          { ids: match[1] });
      }
      return undefined;
    }

    function tableActionSelected(shipmentSpecimen) {
      return vm.shipment.tagSpecimensAsMissing([ shipmentSpecimen.specimen.inventoryId ])
        .then(function () {
          vm.refreshTable += 1;
        });
    }

  }

  return component;
});
