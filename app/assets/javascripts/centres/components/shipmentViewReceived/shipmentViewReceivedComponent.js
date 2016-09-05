/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

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
    'shipmentReceiveProgressItems'
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
                                          shipmentReceiveProgressItems) {
    var vm = this;

    vm.unpackShipment       = unpackShipment;
    vm.returnToSentState = returnToSentState;

    vm.progressInfo = {
      items: shipmentReceiveProgressItems,
      current: 2
    };

    //----

    function unpackShipment() {
      if (_.isUndefined(vm.timeUnpacked)) {
        vm.timeUnpacked = new Date();
      }
      return modalInput.dateTime(gettextCatalog.getString('Date and time shipment was unpacked'),
                                 gettextCatalog.getString('Time unpacked'),
                                 vm.timeUnpacked,
                                 { required: true }).result
        .then(function (timeUnpacked) {
          return vm.shipment.unpacked(timeService.dateToUtcString(timeUnpacked));
        })
        .then(function (shipment) {
          return $state.go('home.shipping.shipment.unpack', { shipmentId: shipment.id });
        })
        .catch(notificationsService.updateError);
    }

    function returnToSentState() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to place this shipment in <b>sent</b> state?'))
        .then(function () {
          return vm.shipment.sent(vm.shipment.timeSent)
            .then(stateHelper.reloadAndReinit)
            .catch(notificationsService.updateError);
        });
    }
  }

  return component;
});
