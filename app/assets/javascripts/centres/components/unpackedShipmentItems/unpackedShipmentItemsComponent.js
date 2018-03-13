/**
 * AngularJS Components used in {@link domain.centres.Shipment Shipping}
 *
 * @namespace centres.components.unpackedShipmentItems
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Loads all Shipment Specimens in PRESENT state.
 */
/* @ngInject */
function UnpackedShipmentItemsController($q,
                                         $scope,
                                         Shipment,
                                         ShipmentSpecimen,
                                         ShipmentItemState,
                                         modalService,
                                         notificationsService,
                                         gettextCatalog) {
  var vm = this;
  vm.$onInit = onInit;

  //----

  function onInit() {
    vm.refreshSpecimensTable = 0;

    vm.actions =  [
      {
        id:    'tag-as-extra',
        class: 'btn-warning',
        title: gettextCatalog.getString('Tag as present'),
        icon:  'glyphicon-arrow-left'
      }
    ];

    vm.getSpecimens         = getSpecimens;
    vm.tableActionSelected  = tableActionSelected;

    $scope.$emit('tabbed-page-update', 'tab-selected');

    if (vm.itemState === ShipmentItemState.RECEIVED) {
      vm.panelHeading = gettextCatalog.getString('Received specimens in this shipment');
      vm.noSpecimensMessage = gettextCatalog.getString('No received specimens present in this shipment');
    } else if (vm.itemState === ShipmentItemState.MISSING) {
      vm.panelHeading = gettextCatalog.getString('Missing specimens in this shipment');
      vm.noSpecimensMessage = gettextCatalog.getString('No missing specimens present in this shipment');
    } else {
      throw new Error('invalid item state: ' + vm.itemState);
    }
  }

  function getSpecimens(options) {
    if (!vm.shipment) { return $q.when({ items: [], maxPages: 0 }); }

    _.extend(options, { filter: 'state:in:' + vm.itemState });

    return ShipmentSpecimen.list(vm.shipment.id, options)
      .then(function (paginatedResult) {
        return { items: paginatedResult.items, maxPages: paginatedResult.maxPages };
      });
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
        notificationsService.success(gettextCatalog.getString('Specimen returnted to unpacked'));
      })
      .catch(function () {});
  }

}

/**
 * An AngularJS component that displays the {@link domain.centres.ShipmentItemState RECEIVED} or {@link
 * domain.centres.ShipmentItemState MISSING} items in an unpacked {@link domain.centres.Shipment Shipment}.
 *
 * @memberOf centres.components.unpackedShipmentItems
 *
 * @param {domain.centres.Shipment} shipment - the shipment to display information for.
 *
 * @param {domain.centres.ShipmentItemState} itemState - the state of the items to display: either {@link
 * domain.centres.ShipmentItemState RECEIVED} or {@link domain.centres.ShipmentItemState MISSING}.
 */
const unpackedShipmentItemsComponent = {
  template: require('./unpackedShipmentItems.html'),
  controller: UnpackedShipmentItemsController,
  controllerAs: 'vm',
  bindings: {
    shipment:  '<',
    itemState: '@'
  }
};

export default ngModule => ngModule.component('unpackedShipmentItems', unpackedShipmentItemsComponent)
