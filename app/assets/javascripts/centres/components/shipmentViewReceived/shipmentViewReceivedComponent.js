/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    template: require('./shipmentViewReceived.html'),
    controller: ShipmentViewReceivedController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  ShipmentViewReceivedController.$inject = [
    '$q',
    '$state',
    'gettextCatalog',
    'modalInput',
    'notificationsService',
    'timeService',
    'modalService',
    'SHIPMENT_RECEIVE_PROGRESS_ITEMS'
  ];

  function ShipmentViewReceivedController($q,
                                          $state,
                                          gettextCatalog,
                                          modalInput,
                                          notificationsService,
                                          timeService,
                                          modalService,
                                          SHIPMENT_RECEIVE_PROGRESS_ITEMS) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.timeUnpacked      = new Date();
      vm.unpackShipment    = unpackShipment;
      vm.returnToSentState = returnToSentState;

      vm.progressInfo = {
        items: SHIPMENT_RECEIVE_PROGRESS_ITEMS,
        current: 2
      };
    }

    function unpackShipment() {
      modalInput.dateTime(gettextCatalog.getString('Date and time shipment was unpacked'),
                          gettextCatalog.getString('Time unpacked'),
                          vm.timeUnpacked,
                          { required: true }).result
        .then(function (timeUnpacked) {
          return vm.shipment.unpack(timeService.dateAndTimeToUtcString(timeUnpacked))
            .then(function () {
              $state.go('home.shipping.shipment.unpack.info', { shipmentId: vm.shipment.id });
            })
            .catch(function (err) {
              if (err.message === 'TimeUnpackedBeforeReceived') {
                err.message = gettextCatalog.getString('The unpacked time is before the received time');
              }
              notificationsService.updateError(err);
            });
        });
    }

    function returnToSentState() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to place this shipment in <b>sent</b> state?'))
        .then(function () {
          return vm.shipment.send(vm.shipment.timeSent);
        })
        .then(function () {
          $state.reload();
        })
        .catch(notificationsService.updateError);
    }
  }

  return component;
});
