/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentUnpack/shipmentUnpack.html',
    controller: ShipmentUnpackController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  ShipmentUnpackController.$inject = [
    '$q',
    '$state',
    'Shipment',
    'modalService',
    'gettextCatalog',
    'notificationsService',
    'shipmentReceiveProgressItems',
    'ShipmentState',
    'ShipmentSpecimen',
    'ShipmentItemState'
  ];

  /**
   *
   */
  function ShipmentUnpackController($q,
                                    $state,
                                    Shipment,
                                    modalService,
                                    gettextCatalog,
                                    notificationsService,
                                    shipmentReceiveProgressItems,
                                    ShipmentState,
                                    ShipmentSpecimen,
                                    ShipmentItemState) {
    var vm = this;

    vm.$onInit = onInit;
    vm.inventoryId = '';
    vm.returnToReceivedState = returnToReceivedState;
    vm.getPresentSpecimens = getPresentSpecimens;
    vm.getReceivedSpecimens = getReceivedSpecimens;
    vm.onInventoryIdSubmit = onInventoryIdSubmit;

    vm.progressInfo = {
      items: shipmentReceiveProgressItems,
      current: 3
    };

    //----

    function onInit() {
      // reload the shipment
      Shipment.get(vm.shipment.id).then(function (shipment) {
        vm.shipment = shipment;
      });
    }

    function returnToReceivedState() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to place this shipment in <b>received</b> state?'))
        .then(function () {
          return vm.shipment.changeState(ShipmentState.RECEIVED, vm.shipment.timeReceived)
            .then(function () {
              $state.go('home.shipping.shipment', { shipmentId: vm.shipment.id }, { reload: true });
            })
            .catch(notificationsService.updateError);
        });

    }

    function getSpecimensByItemState(itemState, options) {
      if (!vm.shipment) { return $q.when([]); }

      _.extend(options, { stateFilter: itemState });

      return ShipmentSpecimen.list(vm.shipment.id, options)
        .then(function (paginatedResult) {
          return { items: paginatedResult.items, maxPages: paginatedResult.maxPages };
        });

    }

    function getPresentSpecimens(options) {
      console.log('getPresentSpecimens');
      return getSpecimensByItemState(ShipmentItemState.PRESENT, options);
    }

    function getReceivedSpecimens(options) {
      console.log('getReceivedSpecimens');
      return getSpecimensByItemState(ShipmentItemState.RECEIVED, options);
    }

    /**
     * Inventory ID entered by the user
     */
    function onInventoryIdSubmit() {
      console.log(vm.inventoryId);
      vm.inventoryId = '';
    }
  }

  return component;
});
