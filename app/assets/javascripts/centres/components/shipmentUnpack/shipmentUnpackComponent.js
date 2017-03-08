/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  var component = {
    templateUrl: '/assets/javascripts/centres/components/shipmentUnpack/shipmentUnpack.html',
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
    'SHIPMENT_RECEIVE_PROGRESS_ITEMS',
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
                                    SHIPMENT_RECEIVE_PROGRESS_ITEMS,
                                    ShipmentState,
                                    ShipmentSpecimen,
                                    ShipmentItemState) {
    var vm = this;

    vm.$onInit = onInit;
    vm.inventoryId = '';
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

    vm.nonReceivedSpecimensTableActionSelected = nonReceivedSpecimensTableActionSelected;

    vm.returnToReceivedState = returnToReceivedState;
    vm.getPresentSpecimens = getPresentSpecimens;
    vm.getReceivedSpecimens = getReceivedSpecimens;
    vm.onInventoryIdSubmit = onInventoryIdSubmit;

    vm.progressInfo = {
      items: SHIPMENT_RECEIVE_PROGRESS_ITEMS,
      current: 3
    };

    //----

    function onInit() {
      // reload the shipment
      Shipment.get(vm.shipment.id)
        .then(function (shipment) {
          vm.shipment = shipment;
        })
        .catch(function () {
          // TODO: instead display message that shipment does not exist
          $state.go('404', null, { location: false });
        });
    }

    function returnToReceivedState() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to place this shipment in <b>received</b> state?'))
        .then(function () {
          return vm.shipment.receive(vm.shipment.timeReceived)
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
      return getSpecimensByItemState(ShipmentItemState.PRESENT, options);
    }

    function getReceivedSpecimens(options) {
      return getSpecimensByItemState(ShipmentItemState.RECEIVED, options);
    }

    // Inventory ID entered by the user
    function onInventoryIdSubmit() {
      // TODO: finish this implementation
      vm.inventoryId = '';
    }

    function nonReceivedSpecimensTableActionSelected(shipmentSpecimen, action) {
      // TODO: finish this implementation
      console.log('nonReceivedSpecimensTableActionSelected', shipmentSpecimen.id, action);
    }
  }

  return component;
});
