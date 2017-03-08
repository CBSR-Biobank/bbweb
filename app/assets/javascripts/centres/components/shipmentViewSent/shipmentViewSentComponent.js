/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var component = {
    templateUrl : '/assets/javascripts/centres/components/shipmentViewSent/shipmentViewSent.html',
    controller: ShipmentViewSentController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  ShipmentViewSentController.$inject = [
    '$state',
    'gettextCatalog',
    'modalInput',
    'notificationsService',
    'timeService',
    'stateHelper',
    'modalService',
    'SHIPMENT_RECEIVE_PROGRESS_ITEMS',
    'shipmentSkipToUnpackedModalService'
  ];

  /**
   *
   */
  function ShipmentViewSentController($state,
                                      gettextCatalog,
                                      modalInput,
                                      notificationsService,
                                      timeService,
                                      stateHelper,
                                      modalService,
                                      SHIPMENT_RECEIVE_PROGRESS_ITEMS,
                                      shipmentSkipToUnpackedModalService) {
    var vm = this;

    vm.returnToPackedState = returnToPackedState;
    vm.receiveShipment     = receiveShipment;
    vm.unpackShipment      = unpackShipment;

    vm.progressInfo = {
      items: SHIPMENT_RECEIVE_PROGRESS_ITEMS,
      current: 1
    };

    //----

    function returnToPackedState() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to place this shipment in <b>packed</b> state?'))
        .then(function () {
          return vm.shipment.pack(vm.shipment.timePacked)
            .catch(notificationsService.updateErrorAndReject);
        })
        .then(stateHelper.reloadAndReinit);
    }

    function receiveShipment() {
      modalInput.dateTime(gettextCatalog.getString('Date and time shipment was received'),
                          gettextCatalog.getString('Time received'),
                          new Date(),
                          { required: true }).result
        .then(function (timeReceived) {
          return vm.shipment.receive(timeService.dateAndTimeToUtcString(timeReceived))
            .catch(notificationsService.updateErrorAndReject);
        })
        .then(stateHelper.reloadAndReinit);
    }

    function unpackShipment() {
      return shipmentSkipToUnpackedModalService.open().result
        .then(function (timeResult) {
          return vm.shipment.skipToStateUnpacked(timeService.dateAndTimeToUtcString(timeResult.timeReceived),
                                                 timeService.dateAndTimeToUtcString(timeResult.timeUnpacked))
            .catch(notificationsService.updateErrorAndReject);
        })
        .then(function (shipment) {
          return $state.go('home.shipping.unpack.info', { shipmentId: shipment.id});
        });
    }
  }

  return component;
});
