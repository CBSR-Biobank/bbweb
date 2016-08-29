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
      shipment: '<'
    }
  };

  ShipmentViewController.$inject = [
    '$state',
    'gettextCatalog',
    'modalInput',
    'notificationsService',
    'timeService'
  ];

  /**
   *
   */
  function ShipmentViewController($state,
                                  gettextCatalog,
                                  modalInput,
                                  notificationsService,
                                  timeService) {
    var vm = this;

    vm.sendShipment = sendShipment;

    //--

    function sendShipment() {
      return modalInput.dateTime(gettextCatalog.getString('Date and time shipment was sent'),
                                 gettextCatalog.getString('Time sent'),
                                 vm.timeSent,
                                 { required: true }).result
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
