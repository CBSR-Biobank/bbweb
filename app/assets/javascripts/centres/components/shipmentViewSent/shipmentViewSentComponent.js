/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

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
    'shipmentReceiveProgressItems',
    'ShipmentState',
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
                                      shipmentReceiveProgressItems,
                                      ShipmentState,
                                      shipmentSkipToUnpackedModalService) {
    var vm = this;

    vm.returnToPackedState = returnToPackedState;
    vm.receiveShipment     = receiveShipment;
    vm.unpackShipment      = unpackShipment;

    vm.progressInfo = {
      items: shipmentReceiveProgressItems,
      current: 1
    };

    //----

    function returnToPackedState() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to place this shipment in <b>packed</b> state?'))
        .then(function () {
          return vm.shipment.pack(vm.shipment.timePacked)
            .then(stateHelper.reloadAndReinit)
            .catch(notificationsService.updateError);
        });
    }

    function receiveShipment() {
      if (_.isUndefined(vm.timeReceived)) {
        vm.timeReceived = new Date();
      }
      return modalInput.dateTime(gettextCatalog.getString('Date and time shipment was received'),
                                 gettextCatalog.getString('Time received'),
                                 vm.timeReceived,
                                 { required: true }).result
        .then(function (timeReceived) {
          return vm.shipment.receive(timeService.dateAndTimeToUtcString(timeReceived))
            .then(stateHelper.reloadAndReinit)
            .catch(notificationsService.updateError);
        });
    }

    function unpackShipment() {
      vm.timeReceived = new Date();
      vm.timeUnpacked = new Date();
      return shipmentSkipToUnpackedModalService.open().result
        .then(function (timeResult) {
          return vm.shipment.skipToStateUnpacked(timeService.dateAndTimeToUtcString(timeResult.timeReceived),
                                                 timeService.dateAndTimeToUtcString(timeResult.timeUnpacked))
            .then(function (shipment) {
              return $state.go('home.shipping.unpack.info', { shipmentId: shipment.id});
            })
            .catch(notificationsService.updateError);
        });
    }
  }

  return component;
});
