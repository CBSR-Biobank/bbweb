/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentUnpack/shipmentUnpack.html',
    controller: ShipmentUnpackController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  ShipmentUnpackController.$inject = [
    '$state',
    'Shipment',
    'modalService',
    'gettextCatalog',
    'notificationsService',
    'shipmentReceiveProgressItems'
  ];

  /**
   *
   */
  function ShipmentUnpackController($state,
                                    Shipment,
                                    modalService,
                                    gettextCatalog,
                                    notificationsService,
                                    shipmentReceiveProgressItems) {
    var vm = this;

    vm.$onInit = onInit;
    vm.returnToReceivedState = returnToReceivedState;

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
          return vm.shipment.received(vm.shipment.timeReceived)
            .then(function () {
              $state.go('home.shipping.shipment', { shipmentId: vm.shipment.id }, { reload: true });
            })
            .catch(notificationsService.updateError);
        });

    }
  }

  return component;
});
