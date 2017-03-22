/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentViewReceived/shipmentViewReceived.html',
    controller: ShipmentViewReceivedController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  ShipmentViewReceivedController.$inject = [
    '$state',
    'gettextCatalog',
    'modalInput',
    'notificationsService',
    'timeService',
    'modalService',
    'stateHelper',
    'SHIPMENT_RECEIVE_PROGRESS_ITEMS'
  ];

  /**
   *
   */
  function ShipmentViewReceivedController($state,
                                          gettextCatalog,
                                          modalInput,
                                          notificationsService,
                                          timeService,
                                          modalService,
                                          stateHelper,
                                          SHIPMENT_RECEIVE_PROGRESS_ITEMS) {
    var vm = this;

    vm.timeUnpacked      = new Date();
    vm.unpackShipment    = unpackShipment;
    vm.returnToSentState = returnToSentState;

    vm.progressInfo = {
      items: SHIPMENT_RECEIVE_PROGRESS_ITEMS,
      current: 2
    };

    //----

    function unpackShipment() {
      modalInput.dateTime(gettextCatalog.getString('Date and time shipment was unpacked'),
                          gettextCatalog.getString('Time unpacked'),
                          vm.timeUnpacked,
                          { required: true }).result
        .then(function (timeUnpacked) {
          return vm.shipment.unpack(timeService.dateAndTimeToUtcString(timeUnpacked))
            .catch(notificationsService.updateErrorAndReject);
        })
        .then(function (shipment) {
          $state.go('home.shipping.shipment.unpack.info', { shipmentId: shipment.id });
        });
    }

    function returnToSentState() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to place this shipment in <b>sent</b> state?'))
        .then(function () {
          return vm.shipment.send(vm.shipment.timeSent)
            .catch(notificationsService.updateErrorAndReject);
        })
        .then(stateHelper.reloadAndReinit);
    }
  }

  return component;
});
