/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

var component = {
  template: require('./unpackedShipmentUnpack.html'),
  controller: UnpackedShipmentUnpackController,
  controllerAs: 'vm',
  bindings: {
    shipment: '<'
  }
};

/*
 * Allows user to interact with Shipment Specimens in PRESENT state.
 *
 * The user can receive the specimens, mark them as EXTRA or MISSING.
 */
/* @ngInject */
function UnpackedShipmentUnpackController($q,
                                          $controller,
                                          $scope,
                                          Shipment,
                                          ShipmentSpecimen,
                                          ShipmentItemState,
                                          gettextCatalog,
                                          modalService,
                                          notificationsService) {
  var vm = this;
  vm.$onInit = onInit;

  //----

  function onInit() {
    $controller('UnpackBaseController', { vm:             vm,
                                          modalService:   modalService,
                                          gettextCatalog: gettextCatalog });

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
    if (!vm.inventoryIds) {
      return null;
    }

    var inventoryIds = vm.inventoryIds.split(',').map((nonTrimmedInventoryId) => nonTrimmedInventoryId.trim());
    return vm.shipment.tagSpecimensAsReceived(inventoryIds)
      .then(function () {
        vm.inventoryIds = '';
        vm.refreshTable += 1;
        notificationsService.success(gettextCatalog.getString('Specimen(s) received'));
      })
      .catch(function (err) {
        var modalMsg;

        if (err.message) {
          modalMsg = vm.errorIsShipSpecimensNotInShipment(err.message);

          if (modalMsg && (inventoryIds.length === 1)) {
            return checkIfTagAsExtra(inventoryIds[0]);
          }

          if (_.isUndefined(modalMsg)) {
            modalMsg = vm.errorIsInvalidInventoryIds(err.message);
          }
          if (_.isUndefined(modalMsg)) {
            modalMsg = vm.errorIsShipSpecimensNotPresent(err.message);
          }
        }

        if (modalMsg) {
          return modalService.modalOk(gettextCatalog.getString('Invalid inventory IDs'), modalMsg);
        }

        return modalService.modalOk(gettextCatalog.getString('Server error'), JSON.stringify(err));
      });
  }

  function checkIfTagAsExtra(inventoryId) {
    return modalService.modalOkCancel(
      gettextCatalog.getString('Invalid inventory IDs'),
      gettextCatalog.getString(
        'Specimen with  inventory ID <b>{{inventoryId}}</b> is not in this shipment. Mark it as extra?',
        { inventoryId : inventoryId }))
      .then(function () {
        vm.tagSpecimensAsExtra([ inventoryId ]);
      })
      .then(function () {
        notificationsService.success(gettextCatalog.getString('Specimen marked as extra'));
      });
  }

  function tableActionSelected(shipmentSpecimen) {
    return vm.shipment.tagSpecimensAsMissing([ shipmentSpecimen.specimen.inventoryId ])
      .then(function () {
        vm.refreshTable += 1;
      });
  }

}

export default ngModule => ngModule.component('unpackedShipmentUnpack', component)
