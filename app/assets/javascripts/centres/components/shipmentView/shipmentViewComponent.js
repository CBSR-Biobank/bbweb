/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentView/shipmentView.html',
    controller: ShipmentViewController,
    controllerAs: 'vm',
    bindings: {
      shipmentId: '<'
    }
  };

  ShipmentViewController.$inject = [
    '$state',
    'Shipment',
    'ShipmentState',
    'shipmentProgressItems',
    'modalInput',
    'notificationsService',
    'timeService'
  ];

  /**
   *
   */
  function ShipmentViewController($state,
                                  Shipment,
                                  ShipmentState,
                                  shipmentProgressItems,
                                  modalInput,
                                  notificationsService,
                                  timeService) {
    var vm = this;

    vm.$onInit = onInit;
    vm.sendShipment = sendShipment;

    vm.shipment = null;

    //--

    function onInit() {
      Shipment.get(vm.shipmentId).then(function (shipment) {
        vm.shipment = shipment;
       });
    }

    function sendShipment() {
      return modalInput.dateTime('Date and time shipment was sent',
                                 'Time sent',
                                 vm.timeSent,
                                 { required: true })
        .result
        .then(function (timeSent) {
          return vm.shipment.sent(timeService.dateToUtcString(timeSent));
        })
        .then(function (shipment) {
          return $state.go('home.shipping');
        })
        .catch(notificationsService.updateError);
    }
  }

  return component;
});
