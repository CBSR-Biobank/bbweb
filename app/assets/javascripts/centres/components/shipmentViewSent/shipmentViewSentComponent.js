/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  var component = {
    template: require('./shipmentViewSent.html'),
    controller: ShipmentViewSentController,
    controllerAs: 'vm',
    bindings: {
      shipment: '<'
    }
  };

  ShipmentViewSentController.$inject = [
    '$q',
    '$state',
    'gettextCatalog',
    'modalInput',
    'notificationsService',
    'timeService',
    'modalService',
    'SHIPMENT_RECEIVE_PROGRESS_ITEMS',
    'shipmentSkipToUnpackedModalService'
  ];

  /*
   * Controller for this component.
   */
  function ShipmentViewSentController($q,
                                      $state,
                                      gettextCatalog,
                                      modalInput,
                                      notificationsService,
                                      timeService,
                                      modalService,
                                      SHIPMENT_RECEIVE_PROGRESS_ITEMS,
                                      shipmentSkipToUnpackedModalService) {
    var vm = this;
    vm.$onInit = onInit;

    //--

    function onInit() {
      vm.timeReceived        = new Date();
      vm.returnToPackedState = returnToPackedState;
      vm.tagAsLost           = tagAsLost;
      vm.receiveShipment     = receiveShipment;
      vm.unpackShipment      = unpackShipment;

      vm.progressInfo = {
        items: SHIPMENT_RECEIVE_PROGRESS_ITEMS,
        current: 1
      };
    }

    function returnToPackedState() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to place this shipment in <b>Packed</b> state?'))
        .then(function () {
          return vm.shipment.pack(vm.shipment.timePacked)
            .then(function () {
              $state.reload();
            })
            .catch(notificationsService.updateError);
        });
    }

    function tagAsLost() {
      modalService.modalOkCancel(
        gettextCatalog.getString('Please confirm'),
        gettextCatalog.getString('Are you sure you want to tag this shipment as <b>Lost</b>?'))
        .then(function () {
          return vm.shipment.lost()
            .then(function () {
              $state.reload();
            })
            .catch(notificationsService.updateError);
        });
    }

    function receiveShipment() {
      modalInput.dateTime(gettextCatalog.getString('Date and time shipment was received'),
                          gettextCatalog.getString('Time received'),
                          vm.timeReceived,
                          { required: true }).result
        .then(function (timeReceived) {
          return vm.shipment.receive(timeService.dateAndTimeToUtcString(timeReceived))
            .then(function () {
              $state.reload();
            })
            .catch(function (err) {
              if (err.message === 'TimeReceivedBeforeSent') {
                err.message = gettextCatalog.getString('The received time is before the sent time');
              }
              notificationsService.updateError(err);
            });
        });
    }

    function unpackShipment() {
      return shipmentSkipToUnpackedModalService.open().result
        .then(function (timeResult) {
          return vm.shipment.skipToStateUnpacked(timeService.dateAndTimeToUtcString(timeResult.timeReceived),
                                                 timeService.dateAndTimeToUtcString(timeResult.timeUnpacked))
            .then(function (shipment) {
              return $state.go('home.shipping.shipment.unpack.info', { shipmentId: shipment.id});
            })
            .catch(function (err) {
              var newErr = {};
              if (err.message === 'TimeReceivedBeforeSent') {
                newErr.message =
                  gettextCatalog.getString('The received time is before the sent time');
              } else if (err.message === 'TimeUnpackedBeforeReceived') {
                newErr.message =
                  gettextCatalog.getString('The unpacked time is before the received time');
              }
              notificationsService.updateError(newErr);
            });
        });
    }
  }

  return component;
});
